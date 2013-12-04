package com.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.builder.BuilderFactory;
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
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

import com.bonitasoft.engine.api.TenantManagementAPI;
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

}
