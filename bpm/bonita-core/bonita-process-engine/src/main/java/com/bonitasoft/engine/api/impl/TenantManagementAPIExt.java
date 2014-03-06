package com.bonitasoft.engine.api.impl;

import java.util.List;
import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.api.impl.NodeConfiguration;
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
import org.bonitasoft.engine.work.WorkService;

import com.bonitasoft.engine.api.TenantManagementAPI;
import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;

@AvailableWhenTenantIsPaused
public class TenantManagementAPIExt implements TenantManagementAPI {

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
        long tenantId = getTenantId();
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
    }

    private void setTenantPaused(final boolean shouldBePaused) throws UpdateException {
        PlatformServiceAccessor platformServiceAccessor = getPlatformAccessorNoException();
        final PlatformService platformService = platformServiceAccessor.getPlatformService();
        SchedulerService schedulerService = platformServiceAccessor.getSchedulerService();
        SessionService sessionService = platformServiceAccessor.getSessionService();
        NodeConfiguration nodeConfiguration = platformServiceAccessor.getPlaformConfiguration();

        long tenantId = getTenantId();
        STenant tenant;
        try {
            tenant = platformService.getTenant(tenantId);
        } catch (STenantNotFoundException e) {
            throw new UpdateException("Tenant does not exists", e);
        }
        if (shouldBePaused && !STenant.ACTIVATED.equals(tenant.getStatus()) || !shouldBePaused && !STenant.PAUSED.equals(tenant.getStatus())) {
            throw new UpdateException("Can't " + (shouldBePaused ? "pause" : "resume") + " a tenant in state " + tenant.getStatus());
        }

        TenantServiceAccessor tenantServiceAccessor = platformServiceAccessor.getTenantServiceAccessor(tenantId);
        WorkService workService = tenantServiceAccessor.getWorkService();
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        final STenantBuilderFactory tenantBuilderFact = BuilderFactory.get(STenantBuilderFactory.class);
        if (shouldBePaused) {
            descriptor.addField(tenantBuilderFact.getStatusKey(), STenant.PAUSED);
            pauseServicesForTenant(workService, schedulerService, sessionService, tenantId);
        } else {
            descriptor.addField(tenantBuilderFact.getStatusKey(), STenant.ACTIVATED);
            resumeServicesForTenant(workService, schedulerService, tenantId, nodeConfiguration, platformServiceAccessor, tenantServiceAccessor);
        }
        updateTenantFromId(platformService, descriptor, tenant);
    }

    private void pauseServicesForTenant(final WorkService workService, final SchedulerService schedulerService, final SessionService sessionService,
            final long tenantId)
            throws UpdateException {
        try {
            schedulerService.pauseJobs(tenantId);
            sessionService.deleteSessionsOfTenantExceptTechnicalUser(tenantId);
        } catch (SSchedulerException e) {
            throw new UpdateException("Unable to pause the scheduler.", e);
        }
        try {
            workService.pause();
        } catch (SBonitaException e) {
            throw new UpdateException("Unable to pause the work service.", e);
        } catch (TimeoutException e) {
            throw new UpdateException("Unable to pause the work service.", e);
        }
    }

    private void resumeServicesForTenant(final WorkService workService, final SchedulerService schedulerService, final long tenantId,
            final NodeConfiguration nodeConfiguration, final PlatformServiceAccessor platformServiceAccessor, final TenantServiceAccessor tenantServiceAccessor)
            throws UpdateException {
        try {

            workService.resume();
            List<TenantRestartHandler> tenantRestartHandlers = nodeConfiguration.getTenantRestartHandlers();
            for (TenantRestartHandler tenantRestartHandler : tenantRestartHandlers) {
                tenantRestartHandler.handleRestart(platformServiceAccessor, tenantServiceAccessor);
            }
        } catch (SBonitaException e) {
            throw new UpdateException("Unable to resume the work service.", e);
        } catch (RestartException e) {
            throw new UpdateException("Unable to resume all elements of the work service.", e);
        }
        try {
            schedulerService.resumeJobs(tenantId);
        } catch (SSchedulerException e) {
            throw new UpdateException("Unable to resume the scheduler.", e);
        }
    }

    protected PlatformServiceAccessor getPlatformAccessorNoException() {
        try {
            return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    protected void updateTenantFromId(final PlatformService platformService, final EntityUpdateDescriptor descriptor, final STenant tenant)
            throws UpdateException {
        try {

            platformService.updateTenant(tenant, descriptor);
        } catch (SBonitaException e) {
            throw new UpdateException("Could not update the tenant maintenance mode", e);
        }
    }

}
