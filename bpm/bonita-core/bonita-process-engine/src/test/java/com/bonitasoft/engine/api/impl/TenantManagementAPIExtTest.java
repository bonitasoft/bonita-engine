package com.bonitasoft.engine.api.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;

import com.bonitasoft.engine.api.TenantMode;

public class TenantManagementAPIExtTest {

    @Test
    public void setTenantMaintenanceModeShouldUpdateMaintenanceField() throws Exception {
        long tenantId = 17;
        TenantManagementAPIExt tenantManagementAPI = spy(new TenantManagementAPIExt());
        PlatformService platformService = mock(PlatformService.class);
        doNothing().when(tenantManagementAPI).updateTenantFromId(eq(17), eq(platformService), any(EntityUpdateDescriptor.class));
        doReturn(platformService).when(tenantManagementAPI).getPlatformService();
        doReturn(tenantId).when(tenantManagementAPI).getTenantId();

        tenantManagementAPI.setMaintenanceMode(TenantMode.MAINTENANCE);

        EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        String inMaintenanceKey = BuilderFactory.get(STenantBuilderFactory.class).getInMaintenanceKey();
        entityUpdateDescriptor.addField(inMaintenanceKey, true);

        verify(tenantManagementAPI).updateTenantFromId(17, platformService, entityUpdateDescriptor);
    }

    @Test
    public void deployBusinessDataRepositoryShouldHaveAnnotationAvailableOnMaintenanceTenant() throws Exception {
        // given:
        Method method = TenantManagementAPIExt.class.getMethod("deployBusinessDataRepository", byte[].class);

        // then:
        assertTrue("Annotation @AvailableOnMaintenanceTenant should be present on API method TenantManagementAPIExt.deployBusinessDataRepository()",
                method.isAnnotationPresent(AvailableOnMaintenanceTenant.class));
    }

    @Test
    public void setTenantMaintenanceModeShouldHaveAnnotationAvailableOnMaintenanceTenant() throws Exception {
        // given:
        Method method = TenantManagementAPIExt.class.getMethod("setMaintenanceMode", TenantMode.class);

        // then:
        assertTrue("Annotation @AvailableOnMaintenanceTenant should be present on API method TenantManagementAPIExt.setTenantMaintenanceMode()",
                method.isAnnotationPresent(AvailableOnMaintenanceTenant.class));
    }

    @Test
    public void isTenantInMaintenanceShouldHaveAnnotationAvailableOnMaintenanceTenant() throws Exception {
        // given:
        Method method = TenantManagementAPIExt.class.getMethod("isInMaintenance");

        // then:
        assertTrue("Annotation @AvailableOnMaintenanceTenant should be present on API method TenantManagementAPIExt.isTenantInMaintenance()",
                method.isAnnotationPresent(AvailableOnMaintenanceTenant.class));
    }
}
