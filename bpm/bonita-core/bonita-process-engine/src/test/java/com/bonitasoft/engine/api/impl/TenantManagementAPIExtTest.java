package com.bonitasoft.engine.api.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;

import com.bonitasoft.engine.api.TenantMode;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import com.bonitasoft.engine.businessdata.BusinessDataRepositoryException;
import com.bonitasoft.engine.service.TenantServiceAccessor;

public class TenantManagementAPIExtTest {

    @Test
    public void uninstallBusinessDataRepository() throws Exception {
        final TenantServiceAccessor accessor = mock(TenantServiceAccessor.class);
        final BusinessDataRepository repository = mock(BusinessDataRepository.class);
        final TenantManagementAPIExt tenantManagementAPI = spy(new TenantManagementAPIExt());
        doReturn(accessor).when(tenantManagementAPI).getTenantAccessor();
        when(accessor.getBusinessDataRepository()).thenReturn(repository);

        tenantManagementAPI.uninstallBusinessDataRepository();

        verify(repository).undeploy(anyLong());
    }

    @Test(expected = BusinessDataRepositoryException.class)
    public void uninstallBusinessDataRepositoryThrowException() throws Exception {
        final TenantServiceAccessor accessor = mock(TenantServiceAccessor.class);
        final BusinessDataRepository repository = mock(BusinessDataRepository.class);
        final TenantManagementAPIExt tenantManagementAPI = spy(new TenantManagementAPIExt());
        doReturn(accessor).when(tenantManagementAPI).getTenantAccessor();
        when(accessor.getBusinessDataRepository()).thenReturn(repository);
        doThrow(new SBusinessDataRepositoryException("error")).when(repository).undeploy(anyLong());

        tenantManagementAPI.uninstallBusinessDataRepository();
    }

    @Test
    public void setTenantMaintenanceModeShouldUpdateMaintenanceField() throws Exception {
        final long tenantId = 17;
        final TenantManagementAPIExt tenantManagementAPI = spy(new TenantManagementAPIExt());
        final PlatformService platformService = mock(PlatformService.class);
        doNothing().when(tenantManagementAPI).updateTenantFromId(eq(17), eq(platformService), any(EntityUpdateDescriptor.class));
        doReturn(platformService).when(tenantManagementAPI).getPlatformService();

        tenantManagementAPI.setTenantMaintenanceMode(tenantId, TenantMode.MAINTENANCE);

        final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        final String inMaintenanceKey = BuilderFactory.get(STenantBuilderFactory.class).getInMaintenanceKey();
        entityUpdateDescriptor.addField(inMaintenanceKey, true);

        verify(tenantManagementAPI).updateTenantFromId(17, platformService, entityUpdateDescriptor);
    }

    @Test
    public void deployBusinessDataRepositoryShouldHaveAnnotationAvailableOnMaintenanceTenant() throws Exception {
        // given:
        final Method method = TenantManagementAPIExt.class.getMethod("installBusinessDataRepository", byte[].class);

        // then:
        assertTrue("Annotation @AvailableOnMaintenanceTenant should be present on API method TenantManagementAPIExt.deployBusinessDataRepository()",
                method.isAnnotationPresent(AvailableOnMaintenanceTenant.class));
    }

    @Test
    public void setTenantMaintenanceModeShouldHaveAnnotationAvailableOnMaintenanceTenant() throws Exception {
        // given:
        final Method method = TenantManagementAPIExt.class.getMethod("setTenantMaintenanceMode", long.class, TenantMode.class);

        // then:
        assertTrue("Annotation @AvailableOnMaintenanceTenant should be present on API method TenantManagementAPIExt.setTenantMaintenanceMode()",
                method.isAnnotationPresent(AvailableOnMaintenanceTenant.class));
    }

    @Test
    public void isTenantInMaintenanceShouldHaveAnnotationAvailableOnMaintenanceTenant() throws Exception {
        // given:
        final Method method = TenantManagementAPIExt.class.getMethod("isTenantInMaintenance", long.class);

        // then:
        assertTrue("Annotation @AvailableOnMaintenanceTenant should be present on API method TenantManagementAPIExt.isTenantInMaintenance()",
                method.isAnnotationPresent(AvailableOnMaintenanceTenant.class));
    }

}
