package com.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.api.impl.transaction.platform.GetTenantInstance;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.dependency.SDependencyAlreadyExistsException;
import org.bonitasoft.engine.dependency.SDependencyCreationException;
import org.bonitasoft.engine.dependency.SDependencyException;
import org.bonitasoft.engine.dependency.model.SDependency;
import org.bonitasoft.engine.dependency.model.SDependencyMapping;
import org.bonitasoft.engine.dependency.model.builder.SDependencyBuilderFactory;
import org.bonitasoft.engine.dependency.model.builder.SDependencyMappingBuilderFactory;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.api.TenantManagementAPI;
import com.bonitasoft.engine.api.TenantMode;
import com.bonitasoft.engine.api.impl.transaction.UpdateTenant;
import com.bonitasoft.engine.platform.TenantNotFoundException;
import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import com.bonitasoft.engine.service.impl.TenantServiceSingleton;

public class TenantManagementAPIExt implements TenantManagementAPI {

    private static TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Override
    @AvailableOnMaintenanceTenant
    public void deployBusinessDataRepository(final byte[] jar) throws CreationException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final DependencyService dependencyService = tenantAccessor.getDependencyService();
        final SDependency sDependency = BuilderFactory.get(SDependencyBuilderFactory.class).createNewInstance("BDR", "1.0", "BDR.jar", jar).done();
        try {
            dependencyService.createDependency(sDependency);
            final SDependencyMapping sDependencyMapping = BuilderFactory.get(SDependencyMappingBuilderFactory.class)
                    .createNewInstance(sDependency.getId(), tenantAccessor.getTenantId(), "tenant").done();
            dependencyService.createDependencyMapping(sDependencyMapping);
            // TODO: should be in activate tenant
            tenantAccessor.getBusinessDataRespository().start();
        } catch (final SDependencyAlreadyExistsException sdaee) {
            throw new CreationException(sdaee);
        } catch (final SDependencyCreationException sdce) {
            throw new CreationException(sdce);
        } catch (final SDependencyException sde) {
            throw new CreationException(sde);
        }
    }

    @Override
    @AvailableOnMaintenanceTenant
    public boolean isTenantInMaintenance(final long tenantId) throws TenantNotFoundException {
        final GetTenantInstance getTenant = new GetTenantInstance(tenantId, getPlatformService());
        try {
            getTenant.execute();
            return getTenant.getResult().isInMaintenance();
        } catch (final STenantNotFoundException e) {
            throw new TenantNotFoundException("No tenant exists with id: " + tenantId);
        } catch (final SBonitaException e) {
            throw new RetrieveException("Unable to retrieve the tenant with id " + tenantId);
        }
    }

    @Override
    @AvailableOnMaintenanceTenant
    public void setTenantMaintenanceMode(final long tenantId, final TenantMode mode) throws UpdateException {
        final PlatformService platformService = getPlatformService();

        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        final STenantBuilderFactory tenantBuilderFact = BuilderFactory.get(STenantBuilderFactory.class);
        switch (mode) {
            case AVAILABLE:
                descriptor.addField(tenantBuilderFact.getInMaintenanceKey(), STenantBuilderFactory.AVAILABLE);
                break;
            case MAINTENANCE:
                descriptor.addField(tenantBuilderFact.getInMaintenanceKey(), STenantBuilderFactory.IN_MAINTENANCE);
                break;
            default:
                break;
        }
        updateTenantFromId(tenantId, platformService, descriptor);
    }

    protected PlatformService getPlatformService() {
        PlatformServiceAccessor platformAccessor = getPlatformAccessorNoException();
        final PlatformService platformService = platformAccessor.getPlatformService();
        return platformService;
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
            new UpdateTenant(tenantId, descriptor, platformService).execute();
        } catch (SBonitaException e) {
            throw new UpdateException("Could not update the tenant maintenance mode", e);
        }
    }

}
