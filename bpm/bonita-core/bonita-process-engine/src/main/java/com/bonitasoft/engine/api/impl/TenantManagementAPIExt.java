package com.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.api.impl.transaction.platform.GetTenantInstance;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.api.TenantManagementAPI;
import com.bonitasoft.engine.api.TenantMode;
import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;

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
    @AvailableOnMaintenanceTenant
    public boolean isInMaintenance() {
        long tenantId = getTenantId();
        final GetTenantInstance getTenant = new GetTenantInstance(tenantId, getPlatformAccessorNoException().getPlatformService());
        try {
            getTenant.execute();
            return getTenant.getResult().isInMaintenance();
        } catch (final SBonitaException e) {
            throw new RetrieveException("Unable to retrieve the tenant with id " + tenantId, e);
        }
    }

    @Override
    @AvailableOnMaintenanceTenant
    public void setMaintenanceMode(final TenantMode mode) throws UpdateException {
        PlatformServiceAccessor platformServiceAccessor = getPlatformAccessorNoException();
        final PlatformService platformService = platformServiceAccessor.getPlatformService();
        SchedulerService schedulerService = platformServiceAccessor.getSchedulerService();
        SessionService sessionService = platformServiceAccessor.getSessionService();

        long tenantId = getTenantId();
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        final STenantBuilderFactory tenantBuilderFact = BuilderFactory.get(STenantBuilderFactory.class);
        switch (mode) {
            case AVAILABLE:
                descriptor.addField(tenantBuilderFact.getInMaintenanceKey(), STenantBuilderFactory.AVAILABLE);
                resumeServicesForTenant(schedulerService, tenantId);
                break;
            case MAINTENANCE:
                descriptor.addField(tenantBuilderFact.getInMaintenanceKey(), STenantBuilderFactory.IN_MAINTENANCE);
                pauseServicesForTenant(schedulerService, sessionService, tenantId);
                break;
            default:
                break;
        }
        updateTenantFromId(tenantId, platformService, descriptor);
    }

    private void pauseServicesForTenant(final SchedulerService schedulerService, final SessionService sessionService, final long tenantId)
            throws UpdateException {
        try {
            schedulerService.pauseJobs(tenantId);
            sessionService.deleteSessionsOfTenantExceptTechnicalUser(tenantId);
        } catch (SSchedulerException e) {
            throw new UpdateException("Unable to pause the scheduler.", e);
        }
    }

    private void resumeServicesForTenant(final SchedulerService schedulerService, final long tenantId) throws UpdateException {
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

    protected void updateTenantFromId(final long tenantId, final PlatformService platformService, final EntityUpdateDescriptor descriptor)
            throws UpdateException {
        try {
            final STenant tenant = platformService.getTenant(tenantId);
            platformService.updateTenant(tenant, descriptor);
        } catch (SBonitaException e) {
            throw new UpdateException("Could not update the tenant maintenance mode", e);
        }
    }

}
