/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bonitasoft.engine.api.TenantManagementAPI;
import com.bonitasoft.engine.businessdata.BusinessDataRepositoryDeploymentException;
import com.bonitasoft.engine.businessdata.BusinessDataRepositoryException;
import com.bonitasoft.engine.businessdata.InvalidBusinessDataModelException;
import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;
import org.bonitasoft.engine.api.impl.AvailableWhenTenantIsPaused;
import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.api.impl.StarterThread;
import org.bonitasoft.engine.api.impl.transaction.PauseServiceStrategy;
import org.bonitasoft.engine.api.impl.transaction.ResumeServiceStrategy;
import org.bonitasoft.engine.api.impl.transaction.ServiceStrategy;
import org.bonitasoft.engine.api.impl.transaction.SetServiceState;
import org.bonitasoft.engine.api.impl.transaction.platform.GetTenantInstance;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.data.BusinessDataModelRepository;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.execution.work.RestartException;
import org.bonitasoft.engine.execution.work.TenantRestartHandler;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantUpdateBuilder;
import org.bonitasoft.engine.platform.model.builder.STenantUpdateBuilderFactory;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.TaskResult;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionState;

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
@AvailableWhenTenantIsPaused
public class TenantManagementAPIExt implements TenantManagementAPI {

    protected PlatformServiceAccessor getPlatformAccessorNoException() {
        try {
            return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    protected long getTenantId() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            return sessionAccessor.getTenantId();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    public boolean isPaused() {
        final long tenantId = getTenantId();
        final GetTenantInstance getTenant = new GetTenantInstance(tenantId, getPlatformAccessorNoException().getPlatformService());
        try {
            getTenant.execute();
            return getTenant.getResult().isPaused();
        } catch (final SBonitaException e) {
            throw new RetrieveException("Unable to retrieve the tenant with id " + tenantId, e);
        }
    }

    @Override
    public void pause() throws UpdateException {
        setTenantPaused(true);
    }

    @Override
    public void resume() throws UpdateException {
        setTenantPaused(false);
        resolveDependenciesForAllProcesses();
    }

    private void resolveDependenciesForAllProcesses() {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        tenantAccessor.getDependencyResolver().resolveDependenciesForAllProcesses(tenantAccessor);
    }

    private void setTenantPaused(final boolean shouldBePaused) throws UpdateException {
        final PlatformServiceAccessor platformServiceAccessor = getPlatformAccessorNoException();
        final PlatformService platformService = platformServiceAccessor.getPlatformService();

        final long tenantId = getTenantId();
        try {
            final STenant tenant = platformService.getTenant(tenantId);

            if (shouldBePaused && !STenant.ACTIVATED.equals(tenant.getStatus()) || !shouldBePaused && !STenant.PAUSED.equals(tenant.getStatus())) {
                throw new UpdateException("Can't " + (shouldBePaused ? "pause" : "resume") + " a tenant in state " + tenant.getStatus());
            }

            final STenantUpdateBuilderFactory updateBuilderFactory = BuilderFactory.get(STenantUpdateBuilderFactory.class);
            final STenantUpdateBuilder updateDescriptor = updateBuilderFactory.createNewInstance();
            if (shouldBePaused) {
                updateDescriptor.setStatus(STenant.PAUSED);
                pauseServicesForTenant(platformServiceAccessor, tenantId);
            } else {
                updateDescriptor.setStatus(STenant.ACTIVATED);
                resumeServicesForTenant(platformServiceAccessor, tenantId);
            }
            updateTenant(platformService, updateDescriptor, tenant);
        } catch (final STenantNotFoundException e) {
            throw new UpdateException("Tenant does not exist", e);
        }
    }

    protected void pauseServicesForTenant(final PlatformServiceAccessor platformServiceAccessor, final long tenantId)
            throws UpdateException {
        // clustered services
        try {
            pauseScheduler(platformServiceAccessor, tenantId);
            deleteSessionsOfTenantExceptTechnicalUser(platformServiceAccessor, tenantId);

            // on all nodes
            setTenantClassloaderAndUpdateStateOfTenantServicesWithLifecycle(platformServiceAccessor, tenantId, new PauseServiceStrategy());
        } catch (final SSchedulerException e) {
            throw new UpdateException("Unable to pause the scheduler.", e);
        }
    }

    private void deleteSessionsOfTenantExceptTechnicalUser(final PlatformServiceAccessor platformServiceAccessor, final long tenantId) {
        final SessionService sessionService = platformServiceAccessor.getTenantServiceAccessor(tenantId).getSessionService();
        sessionService.deleteSessionsOfTenantExceptTechnicalUser(tenantId);
    }

    private void resumeServicesForTenant(final PlatformServiceAccessor platformServiceAccessor, final long tenantId)
            throws UpdateException {
        // clustered services
        try {
            beforeServiceStartOfRestartHandlersOfTenant(platformServiceAccessor, tenantId);
            resumeScheduler(platformServiceAccessor, tenantId);

            // on all nodes
            setTenantClassloaderAndUpdateStateOfTenantServicesWithLifecycle(platformServiceAccessor, tenantId, new ResumeServiceStrategy());

            afterServiceStartOfRestartHandlersOfTenant(platformServiceAccessor, tenantId);
        } catch (final RestartException e) {
            throw new UpdateException("Unable to resume all elements of the work service.", e);
        } catch (final SSchedulerException e) {
            throw new UpdateException("Unable to resume the scheduler.", e);
        } catch (STransactionNotFoundException e) {
            throw new UpdateException("Unable to resume the tenant restart handlers.", e);
        }
    }

    private void beforeServiceStartOfRestartHandlersOfTenant(final PlatformServiceAccessor platformServiceAccessor, final long tenantId)
            throws RestartException {
        final NodeConfiguration nodeConfiguration = platformServiceAccessor.getPlatformConfiguration();
        final TenantServiceAccessor tenantServiceAccessor = platformServiceAccessor.getTenantServiceAccessor(tenantId);
        final List<TenantRestartHandler> tenantRestartHandlers = nodeConfiguration.getTenantRestartHandlers();
        for (final TenantRestartHandler tenantRestartHandler : tenantRestartHandlers) {
            tenantRestartHandler.beforeServicesStart(platformServiceAccessor, tenantServiceAccessor);
        }
    }

    private void afterServiceStartOfRestartHandlersOfTenant(final PlatformServiceAccessor platformServiceAccessor, final long tenantId) throws RestartException, STransactionNotFoundException {
        final NodeConfiguration nodeConfiguration = platformServiceAccessor.getPlatformConfiguration();
        final TenantServiceAccessor tenantServiceAccessor = platformServiceAccessor.getTenantServiceAccessor(tenantId);
        final STenant tenant;
        try {
            tenant = platformServiceAccessor.getPlatformService().getTenant(tenantId);
        } catch (STenantNotFoundException e) {
            throw new RestartException("Unable to restart tenant", e);
        }
        platformServiceAccessor.getTransactionService().registerBonitaSynchronization(new BonitaTransactionSynchronization() {
            @Override
            public void beforeCommit() {

            }

            @Override
            public void afterCompletion(TransactionState txState) {
                if (txState.equals(TransactionState.COMMITTED)) {
                    new StarterThread(platformServiceAccessor, nodeConfiguration, Arrays.asList(tenant),
                            tenantServiceAccessor.getSessionAccessor(), tenantServiceAccessor.getTechnicalLoggerService()).start();
                }
            }
        });

    }

    // In Protected for unit tests
    protected void setTenantClassloaderAndUpdateStateOfTenantServicesWithLifecycle(final PlatformServiceAccessor platformServiceAccessor, final long tenantId,
            final ServiceStrategy serviceStrategy) throws UpdateException {
        final BroadcastService broadcastService = platformServiceAccessor.getBroadcastService();
        final SetServiceState setServiceState = new SetServiceState(tenantId, serviceStrategy);
        final Map<String, TaskResult<Void>> result = broadcastService.execute(setServiceState, tenantId);
        handleResult(result);
    }

    private void pauseScheduler(final PlatformServiceAccessor platformServiceAccessor, final long tenantId) throws SSchedulerException {
        final SchedulerService schedulerService = platformServiceAccessor.getSchedulerService();
        schedulerService.pauseJobs(tenantId);
    }

    private void resumeScheduler(final PlatformServiceAccessor platformServiceAccessor, final long tenantId) throws SSchedulerException {
        final SchedulerService schedulerService = platformServiceAccessor.getSchedulerService();
        schedulerService.resumeJobs(tenantId);
    }

    private void handleResult(final Map<String, TaskResult<Void>> result) throws UpdateException {
        for (final Entry<String, TaskResult<Void>> entry : result.entrySet()) {
            if (entry.getValue().isError()) {
                throw new UpdateException("There is at least one exception on the node " + entry.getKey(), entry.getValue().getThrowable());
            }
            if (entry.getValue().isTimeout()) {
                throw new UpdateException("There is at least one timeout after " + entry.getValue().getTimeout() + " " + entry.getValue().getTimeunit()
                        + " on the node " + entry.getKey());
            }
        }
    }

    protected void updateTenant(final PlatformService platformService, final STenantUpdateBuilder descriptor, final STenant tenant) throws UpdateException {
        try {
            platformService.updateTenant(tenant, descriptor.done());
        } catch (final SBonitaException e) {
            throw new UpdateException("Could not update the tenant pause mode", e);
        }
    }

    protected TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    @AvailableWhenTenantIsPaused
    public String getBusinessDataModelVersion() throws BusinessDataRepositoryException {
        try {
            final BusinessDataModelRepository modelRepository = getTenantAccessor().getBusinessDataModelRepository();
            return modelRepository.getInstalledBDMVersion();
        } catch (final SBusinessDataRepositoryException e) {
            throw new BusinessDataRepositoryException(e);
        }
    }

    @Override
    @AvailableWhenTenantIsPaused(only = true)
    public String installBusinessDataModel(final byte[] zip) throws InvalidBusinessDataModelException, BusinessDataRepositoryDeploymentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        try {
            final BusinessDataModelRepository bdmRepository = tenantAccessor.getBusinessDataModelRepository();
            return bdmRepository.install(zip, tenantAccessor.getTenantId());
        } catch (final IllegalStateException e) {
            throw new InvalidBusinessDataModelException(e);
        } catch (final SBusinessDataRepositoryDeploymentException e) {
            throw new BusinessDataRepositoryDeploymentException(e);
        }
    }

    @Override
    @AvailableWhenTenantIsPaused(only = true)
    public void uninstallBusinessDataModel() throws BusinessDataRepositoryDeploymentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        try {
            final BusinessDataModelRepository bdmRepository = tenantAccessor.getBusinessDataModelRepository();
            bdmRepository.uninstall(tenantAccessor.getTenantId());
        } catch (final SBusinessDataRepositoryException sbdre) {
            throw new BusinessDataRepositoryDeploymentException(sbdre);
        }
    }

    @Override
    @AvailableWhenTenantIsPaused(only = true)
    public void cleanAndUninstallBusinessDataModel() throws BusinessDataRepositoryDeploymentException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        try {
            final BusinessDataModelRepository bdmRepository = tenantAccessor.getBusinessDataModelRepository();
            bdmRepository.dropAndUninstall(tenantAccessor.getTenantId());
        } catch (final SBusinessDataRepositoryException sbdre) {
            throw new BusinessDataRepositoryDeploymentException(sbdre);
        }
    }

    @Override
    public byte[] getClientBDMZip() throws BusinessDataRepositoryException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final BusinessDataModelRepository bdmRepository = tenantAccessor.getBusinessDataModelRepository();
        try {
            return bdmRepository.getClientBDMZip();
        } catch (final SBusinessDataRepositoryException e) {
            throw new BusinessDataRepositoryException(e);
        }
    }

}
