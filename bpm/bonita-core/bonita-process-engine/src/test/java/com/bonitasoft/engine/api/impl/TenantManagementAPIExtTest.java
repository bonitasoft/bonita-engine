package com.bonitasoft.engine.api.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.platform.model.impl.STenantImpl;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.api.TenantMode;
import com.bonitasoft.engine.service.PlatformServiceAccessor;

public class TenantManagementAPIExtTest {

    private TenantManagementAPIExt tenantManagementAPI;

    private PlatformService platformService;

    private SchedulerService schedulerService;

    private PlatformServiceAccessor platformServiceAccessor;

    private long tenantId;

    private STenantImpl sTenant;

    @Before
    public void before() throws Exception {
        tenantManagementAPI = spy(new TenantManagementAPIExt());
        platformService = mock(PlatformService.class);
        schedulerService = mock(SchedulerService.class);
        platformServiceAccessor = mock(PlatformServiceAccessor.class);
        doReturn(platformServiceAccessor).when(tenantManagementAPI).getPlatformAccessorNoException();
        doReturn(schedulerService).when(platformServiceAccessor).getSchedulerService();
        doReturn(platformService).when(platformServiceAccessor).getPlatformService();
        tenantId = 17;
        doReturn(tenantId).when(tenantManagementAPI).getTenantId();
        sTenant = new STenantImpl("myTenant", "john", 123456789, "MAINTENANCE", false, false);
        when(platformService.getTenant(tenantId)).thenReturn(sTenant);
    }

    @Test
    public void setTenantMaintenanceModeShouldUpdateMaintenanceField() throws Exception {

        tenantManagementAPI.setMaintenanceMode(TenantMode.MAINTENANCE);

        EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        String inMaintenanceKey = BuilderFactory.get(STenantBuilderFactory.class).getInMaintenanceKey();
        entityUpdateDescriptor.addField(inMaintenanceKey, true);

        verify(platformService).updateTenant(sTenant, entityUpdateDescriptor);
    }

    @Test
    public void should_setMaintenanceMode_to_MAINTENANCE_pause_jobs() throws Exception {
        tenantManagementAPI.setMaintenanceMode(TenantMode.MAINTENANCE);

        verify(schedulerService).pauseJobs(tenantId);
    }

    @Test
    public void should_setMaintenanceMode_to_AVAILABLE_pause_jobs() throws Exception {
        tenantManagementAPI.setMaintenanceMode(TenantMode.AVAILABLE);

        verify(schedulerService).resumeJobs(tenantId);
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
