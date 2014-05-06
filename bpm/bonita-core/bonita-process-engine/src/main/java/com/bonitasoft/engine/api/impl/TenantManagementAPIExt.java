/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.api.impl.transaction.SetServiceState;
import org.bonitasoft.engine.api.impl.transaction.platform.GetTenantInstance;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.execution.work.RestartException;
import org.bonitasoft.engine.execution.work.TenantRestartHandler;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.api.TenantManagementAPI;
import com.bonitasoft.engine.api.impl.transaction.PauseServiceStrategy;
import com.bonitasoft.engine.api.impl.transaction.ResumeServiceStrategy;
import com.bonitasoft.engine.business.data.BusinessDataModelRepository;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryDeploymentException;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import com.bonitasoft.engine.businessdata.BusinessDataRepositoryDeploymentException;
import com.bonitasoft.engine.businessdata.BusinessDataRepositoryException;
import com.bonitasoft.engine.businessdata.InvalidBusinessDataModelException;
import com.bonitasoft.engine.service.BroadcastService;
import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TaskResult;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

/**
 * @author Matthieu Chaffotte
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

    protected void resolveDependenciesForAllProcesses() {
        TenantServiceAccessor tenantAccessor = getTenantAccessor();
        tenantAccessor.getDependencyResolver().resolveDependenciesForAllProcesses(tenantAccessor);
    }

    private void setTenantPaused(final boolean shouldBePaused) throws UpdateException {
        final PlatformServiceAccessor platformServiceAccessor = getPlatformAccessorNoException();
        final PlatformService platformService = platformServiceAccessor.getPlatformService();
        final BroadcastService broadcastService = platformServiceAccessor.getBroadcastService();

        final long tenantId = getTenantId();
        STenant tenant;
        try {
            tenant = platformService.getTenant(tenantId);
        } catch (final STenantNotFoundException e) {
            throw new UpdateException("Tenant does not exist", e);
        }
        if (shouldBePaused && !STenant.ACTIVATED.equals(tenant.getStatus()) || !shouldBePaused && !STenant.PAUSED.equals(tenant.getStatus())) {
            throw new UpdateException("Can't " + (shouldBePaused ? "pause" : "resume") + " a tenant in state " + tenant.getStatus());
        }

        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        final STenantBuilderFactory tenantBuilderFact = BuilderFactory.get(STenantBuilderFactory.class);
        if (shouldBePaused) {
            descriptor.addField(tenantBuilderFact.getStatusKey(), STenant.PAUSED);
            pauseServicesForTenant(platformServiceAccessor, broadcastService, tenantId);
        } else {
            descriptor.addField(tenantBuilderFact.getStatusKey(), STenant.ACTIVATED);
            resumeServicesForTenant(platformServiceAccessor, broadcastService, tenantId);
        }
        updateTenant(platformService, descriptor, tenant);
    }

    protected void pauseServicesForTenant(final PlatformServiceAccessor platformServiceAccessor, final BroadcastService broadcastService, final long tenantId)
            throws UpdateException {

        // clustered services
        final SchedulerService schedulerService = platformServiceAccessor.getSchedulerService();
        final SessionService sessionService = platformServiceAccessor.getSessionService();
        try {
            schedulerService.pauseJobs(tenantId);
            sessionService.deleteSessionsOfTenantExceptTechnicalUser(tenantId);
        } catch (final SSchedulerException e) {
            throw new UpdateException("Unable to pause the scheduler.", e);
        }

        // on all nodes
        final SetServiceState pauseService = getPauseService(tenantId);
        final Map<String, TaskResult<Void>> result = broadcastService.execute(pauseService, tenantId);
        handleResult(result);
    }

    protected SetServiceState getPauseService(final long tenantId) {
        return new SetServiceState(tenantId, new PauseServiceStrategy());
    }

    protected SetServiceState getResumeService(final long tenantId) {
        return new SetServiceState(tenantId, new ResumeServiceStrategy());
    }

    private void resumeServicesForTenant(final PlatformServiceAccessor platformServiceAccessor, final BroadcastService broadcastService, final long tenantId)
            throws UpdateException {
        // clustered services
        final SchedulerService schedulerService = platformServiceAccessor.getSchedulerService();
        try {
            schedulerService.resumeJobs(tenantId);
        } catch (final SSchedulerException e) {
            throw new UpdateException("Unable to resume the scheduler.", e);
        }
        // on all nodes
        final SetServiceState resumeService = getResumeService(tenantId);
        final Map<String, TaskResult<Void>> result = broadcastService.execute(resumeService, tenantId);
        handleResult(result);

        final NodeConfiguration nodeConfiguration = platformServiceAccessor.getPlaformConfiguration();
        final TenantServiceAccessor tenantServiceAccessor = platformServiceAccessor.getTenantServiceAccessor(tenantId);
        try {
            final List<TenantRestartHandler> tenantRestartHandlers = nodeConfiguration.getTenantRestartHandlers();
            for (final TenantRestartHandler tenantRestartHandler : tenantRestartHandlers) {
                tenantRestartHandler.handleRestart(platformServiceAccessor, tenantServiceAccessor);
            }
        } catch (final RestartException e) {
            throw new UpdateException("Unable to resume all elements of the work service.", e);
        }

    }

    private void handleResult(final Map<String, TaskResult<Void>> result) throws UpdateException {
        for (final Entry<String, TaskResult<Void>> entry : result.entrySet()) {
            if (entry.getValue().isError()) {
                throw new UpdateException("There is at least one exception on the node " + entry.getKey(), entry.getValue().getThrowable());
            }
            if (entry.getValue().isError()) {
                throw new UpdateException("There is at least one timeout after " + entry.getValue().getTimeout() + " " + entry.getValue().getTimeunit()
                        + " on the node " + entry.getKey());
            }
        }
    }

    protected void updateTenant(final PlatformService platformService, final EntityUpdateDescriptor descriptor, final STenant tenant) throws UpdateException {
        try {
            platformService.updateTenant(tenant, descriptor);
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
