/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.api.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.api.impl.transaction.PauseServiceStrategy;
import org.bonitasoft.engine.api.impl.transaction.ResumeServiceStrategy;
import org.bonitasoft.engine.api.impl.transaction.ServiceStrategy;
import org.bonitasoft.engine.api.impl.transaction.SetServiceState;
import org.bonitasoft.engine.api.impl.transaction.platform.GetTenantInstance;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.data.BusinessDataModelRepository;
import org.bonitasoft.engine.business.data.BusinessDataRepositoryDeploymentException;
import org.bonitasoft.engine.business.data.BusinessDataRepositoryException;
import org.bonitasoft.engine.business.data.InvalidBusinessDataModelException;
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
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TaskResult;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
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
public class TenantAdministrationAPIImpl implements TenantAdministrationAPI {

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
            throw new UpdateException("Unable to restart tenant handlers.", e);
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
                if(txState.equals(TransactionState.COMMITTED)) {
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
