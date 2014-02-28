package com.bonitasoft.engine.api.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.execution.work.RestartException;
import org.bonitasoft.engine.execution.work.TenantRestartHandler;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.platform.model.impl.STenantImpl;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.work.WorkException;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.bonitasoft.engine.api.TenantMode;
import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;

public class TenantManagementAPIExtTest {

    private TenantManagementAPIExt tenantManagementAPI;

    private PlatformService platformService;

    private SchedulerService schedulerService;

    private PlatformServiceAccessor platformServiceAccessor;

    private long tenantId;

    private STenantImpl sTenant;

    private SessionService sessionService;

    private WorkService workService;

    private TenantServiceAccessor tenantServiceAccessor;

    private NodeConfiguration nodeConfiguration;

    private TenantRestartHandler tenantRestartHandler1;

    private TenantRestartHandler tenantRestartHandler2;

    @Before
    public void before() throws Exception {
        tenantManagementAPI = spy(new TenantManagementAPIExt());
        platformService = mock(PlatformService.class);
        schedulerService = mock(SchedulerService.class);
        sessionService = mock(SessionService.class);
        platformServiceAccessor = mock(PlatformServiceAccessor.class);
        tenantServiceAccessor = mock(TenantServiceAccessor.class);
        nodeConfiguration = mock(NodeConfiguration.class);
        workService = mock(WorkService.class);
        tenantRestartHandler1 = mock(TenantRestartHandler.class);
        tenantRestartHandler2 = mock(TenantRestartHandler.class);
        doReturn(platformServiceAccessor).when(tenantManagementAPI).getPlatformAccessorNoException();
        doReturn(schedulerService).when(platformServiceAccessor).getSchedulerService();
        doReturn(platformService).when(platformServiceAccessor).getPlatformService();
        doReturn(nodeConfiguration).when(platformServiceAccessor).getPlaformConfiguration();
        doReturn(sessionService).when(platformServiceAccessor).getSessionService();
        doReturn(tenantServiceAccessor).when(platformServiceAccessor).getTenantServiceAccessor(Mockito.anyLong());
        doReturn(Arrays.asList(tenantRestartHandler1, tenantRestartHandler2)).when(nodeConfiguration).getTenantRestartHandlers();

        tenantId = 17;
        doReturn(tenantId).when(tenantManagementAPI).getTenantId();
        doReturn(workService).when(tenantServiceAccessor).getWorkService();
        sTenant = new STenantImpl("myTenant", "john", 123456789, "MAINTENANCE", false, false);
        when(platformService.getTenant(tenantId)).thenReturn(sTenant);
    }

    @Test
    public void setMaintenanceModeToMAINTENANCEShouldPauseWorkService() throws Exception {

        // given a tenant moved to maintenance mode
        tenantManagementAPI.setMaintenanceMode(TenantMode.MAINTENANCE);

        // then his work service should be pause
        verify(workService).pause();
    }

    @Test
    public void setMaintenanceModeToAVAILLABLEShouldResumeWorkService() throws Exception {

        // given a tenant moved to available mode
        tenantManagementAPI.setMaintenanceMode(TenantMode.AVAILABLE);

        // then his work service should be resumed
        verify(workService).resume();
    }

    @Test(expected = UpdateException.class)
    public void should_setMaintenanceMode_to_AVAILLABLE_throw_exception_when_workservice_fail() throws Exception {
        doThrow(WorkException.class).when(workService).resume();

        // given a tenant moved to available mode
        tenantManagementAPI.setMaintenanceMode(TenantMode.AVAILABLE);
    }

    @Test
    public void should_setMaintenanceMode_to_AVAILLABLE_restart_elements() throws Exception {

        // given a tenant moved to available mode
        tenantManagementAPI.setMaintenanceMode(TenantMode.AVAILABLE);

        // then elements must be restarted
        verify(tenantRestartHandler1, times(1)).handleRestart(platformServiceAccessor, tenantServiceAccessor);
        verify(tenantRestartHandler2, times(1)).handleRestart(platformServiceAccessor, tenantServiceAccessor);
    }

    @Test(expected = UpdateException.class)
    public void should_setMaintenanceMode_to_AVAILLABLE__throw_exception_when_RestartHandler_fail() throws Exception {
        doThrow(RestartException.class).when(tenantRestartHandler2).handleRestart(platformServiceAccessor, tenantServiceAccessor);

        // given a tenant moved to available mode
        tenantManagementAPI.setMaintenanceMode(TenantMode.AVAILABLE);
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
    public void should_setMaintenanceMode_to_MAINTENANCE_delete_sessions() throws Exception {
        tenantManagementAPI.setMaintenanceMode(TenantMode.MAINTENANCE);

        verify(sessionService).deleteSessionsOfTenantExceptTechnicalUser(tenantId);
    }

    @Test
    public void should_setMaintenanceMode_to_AVAILABLE_delete_sessions() throws Exception {
        tenantManagementAPI.setMaintenanceMode(TenantMode.AVAILABLE);

        verify(sessionService, times(0)).deleteSessionsOfTenantExceptTechnicalUser(tenantId);
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

    @Test
    public void loginShouldHaveAnnotationAvailableOnMaintenanceTenant() throws Exception {
        // given:
        Method method = LoginAPIExt.class.getMethod("login", long.class, String.class, String.class);

        // then:
        assertTrue("Annotation @AvailableOnMaintenanceTenant should be present on API method LoginAPIExt.login(long, String, String)",
                method.isAnnotationPresent(AvailableOnMaintenanceTenant.class));
    }

    @Test
    public void loginWithTenantIdShouldHaveAnnotationAvailableOnMaintenanceTenant() throws Exception {
        // given:
        Method method = LoginAPIExt.class.getMethod("login", String.class, String.class);

        // then:
        assertTrue("Annotation @AvailableOnMaintenanceTenant should be present on API method LoginAPIExt.login(String, String)",
                method.isAnnotationPresent(AvailableOnMaintenanceTenant.class));
    }
}
