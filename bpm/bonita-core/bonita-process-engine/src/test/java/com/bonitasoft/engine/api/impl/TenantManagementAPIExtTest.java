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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.execution.work.RestartException;
import org.bonitasoft.engine.execution.work.TenantRestartHandler;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.STenant;
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

import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import com.bonitasoft.engine.businessdata.BusinessDataRepositoryException;
import com.bonitasoft.engine.service.BroadcastService;
import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.BroadcastServiceLocal;

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

    private final BroadcastService broadcastService = new BroadcastServiceLocal();

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
        doReturn(new PauseServices(tenantId) {

            private static final long serialVersionUID = 1L;

            @Override
            PlatformServiceAccessor getPlatformAccessor() throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException,
                    ClassNotFoundException, IOException, BonitaHomeConfigurationException {
                return platformServiceAccessor;
            }
        }).when(tenantManagementAPI).createPauseServicesTask(anyLong());
        doReturn(new ResumeServices(tenantId) {

            private static final long serialVersionUID = 1L;

            @Override
            PlatformServiceAccessor getPlatformAccessor() throws BonitaHomeNotSetException, InstantiationException, IllegalAccessException,
                    ClassNotFoundException, IOException, BonitaHomeConfigurationException {
                return platformServiceAccessor;
            }
        }).when(tenantManagementAPI).createResumeServicesTask(anyLong());
        doReturn(broadcastService).when(platformServiceAccessor).getBroadcastService();
        doReturn(schedulerService).when(platformServiceAccessor).getSchedulerService();
        doReturn(platformService).when(platformServiceAccessor).getPlatformService();
        doReturn(nodeConfiguration).when(platformServiceAccessor).getPlaformConfiguration();
        doReturn(sessionService).when(platformServiceAccessor).getSessionService();
        doReturn(tenantServiceAccessor).when(platformServiceAccessor).getTenantServiceAccessor(Mockito.anyLong());
        doReturn(Arrays.asList(tenantRestartHandler1, tenantRestartHandler2)).when(nodeConfiguration).getTenantRestartHandlers();

        tenantId = 17;
        doReturn(tenantId).when(tenantManagementAPI).getTenantId();
        doReturn(workService).when(tenantServiceAccessor).getWorkService();
        sTenant = new STenantImpl("myTenant", "john", 123456789, STenant.PAUSED, false);
        when(platformService.getTenant(tenantId)).thenReturn(sTenant);
    }

    @Test
    public void pauseTenantShouldPauseWorkService() throws Exception {
        whenTenantIsInState(STenant.ACTIVATED);

        // given a tenant moved to pause mode:
        tenantManagementAPI.pause();

        // then his work service should be pause
        verify(workService).pause();
    }

    @Test
    public void resumeTenantShouldResumeWorkService() throws Exception {

        // given a tenant moved to available mode
        tenantManagementAPI.resume();

        // then his work service should be resumed
        verify(workService).resume();
    }

    @Test(expected = UpdateException.class)
    public void resumeTenant_should_throwExceptionWhenWorkserviceFail() throws Exception {
        doThrow(WorkException.class).when(workService).resume();

        // given a tenant moved to available mode
        tenantManagementAPI.resume();
    }

    @Test
    public void resumeTenant_should_restartElements() throws Exception {

        // given a tenant moved to available mode
        tenantManagementAPI.resume();

        // then elements must be restarted
        verify(tenantRestartHandler1, times(1)).handleRestart(platformServiceAccessor, tenantServiceAccessor);
        verify(tenantRestartHandler2, times(1)).handleRestart(platformServiceAccessor, tenantServiceAccessor);
    }

    @Test(expected = UpdateException.class)
    public void resumeTenant_should_throw_exception_when_RestartHandler_fail() throws Exception {
        doThrow(RestartException.class).when(tenantRestartHandler2).handleRestart(platformServiceAccessor, tenantServiceAccessor);

        // given a tenant moved to available mode
        tenantManagementAPI.resume();
    }

    @Test
    public void pauseTenant_should_pause_jobs() throws Exception {
        whenTenantIsInState(STenant.ACTIVATED);

        tenantManagementAPI.pause();

        verify(schedulerService).pauseJobs(tenantId);
    }

    @Test
    public void resumeTenant_should_pause_jobs() throws Exception {
        tenantManagementAPI.resume();

        verify(schedulerService).resumeJobs(tenantId);
    }

    @Test
    public void pauseTenant_should_delete_sessions() throws Exception {
        whenTenantIsInState(STenant.ACTIVATED);

        tenantManagementAPI.pause();

        verify(sessionService).deleteSessionsOfTenantExceptTechnicalUser(tenantId);
    }

    @Test
    public void resumeTenant_should_delete_sessions() throws Exception {
        tenantManagementAPI.resume();

        verify(sessionService, times(0)).deleteSessionsOfTenantExceptTechnicalUser(tenantId);
    }

    @Test
    public void resumeTenantShouldHaveAnnotationAvailableWhenTenantIsPaused() throws Exception {
        final Method method = TenantManagementAPIExt.class.getMethod("resume");

        assertTrue(
                "Annotation @AvailableWhenTenantIsPaused should be present on API method 'resume' or directly on class TenantManagementAPIExt",
                method.isAnnotationPresent(AvailableWhenTenantIsPaused.class)
                        || TenantManagementAPIExt.class.isAnnotationPresent(AvailableWhenTenantIsPaused.class));
    }

    @Test
    public void pauseTenantShouldHaveAnnotationAvailableWhenTenantIsPaused() throws Exception {
        final Method method = TenantManagementAPIExt.class.getMethod("pause");

        assertTrue(
                "Annotation @AvailableWhenTenantIsPaused should be present on API method 'pause' or directly on class TenantManagementAPIExt",
                method.isAnnotationPresent(AvailableWhenTenantIsPaused.class)
                        || TenantManagementAPIExt.class.isAnnotationPresent(AvailableWhenTenantIsPaused.class));
    }

    @Test
    public void loginShouldHaveAnnotationAvailableWhenTenantIsPaused() throws Exception {
        // given:
        Method method = LoginAPIExt.class.getMethod("login", long.class, String.class, String.class);

        // then:
        assertTrue("Annotation @AvailableWhenTenantIsPaused should be present on API method LoginAPIExt.login(long, String, String)",
                method.isAnnotationPresent(AvailableWhenTenantIsPaused.class));
    }

    @Test
    public void loginWithTenantIdShouldHaveAnnotationAvailableWhenTenantIsPaused() throws Exception {
        // given:
        Method method = LoginAPIExt.class.getMethod("login", String.class, String.class);

        // then:
        assertTrue("Annotation @AvailableWhenTenantIsPaused should be present on API method LoginAPIExt.login(String, String)",
                method.isAnnotationPresent(AvailableWhenTenantIsPaused.class));
    }

    @Test(expected = UpdateException.class)
    public void should_pause_on_a_paused_tenant_throw_update_exception() throws Exception {
        whenTenantIsInState(STenant.PAUSED);

        tenantManagementAPI.pause();
    }

    @Test(expected = UpdateException.class)
    public void should_pause_on_a_deactivated_tenant_throw_update_exception() throws Exception {
        whenTenantIsInState(STenant.DEACTIVATED);

        tenantManagementAPI.pause();
    }

    @Test(expected = UpdateException.class)
    public void should_resume_on_a_paused_tenant_throw_update_exception() throws Exception {
        whenTenantIsInState(STenant.ACTIVATED);

        tenantManagementAPI.resume();
    }

    @Test(expected = UpdateException.class)
    public void should_resume_on_a_deactivated_tenant_throw_update_exception() throws Exception {
        whenTenantIsInState(STenant.DEACTIVATED);

        tenantManagementAPI.resume();
    }

    private void whenTenantIsInState(final String status) throws STenantNotFoundException {
        sTenant = new STenantImpl("myTenant", "john", 123456789, status, false);
        when(platformService.getTenant(tenantId)).thenReturn(sTenant);
    }

    @Test(expected = UpdateException.class)
    public void should_pause_on_a_unexisting_tenant_throw_update_exception() throws Exception {
        doThrow(STenantNotFoundException.class).when(platformService).getTenant(tenantId);

        tenantManagementAPI.resume();
    }

    @Test
    public void installBDRShouldBeAvailableWhenTenantIsPaused_ONLY() throws Exception {
        final Method method = TenantManagementAPIExt.class.getMethod("installBusinessDataRepository", byte[].class);
        AvailableWhenTenantIsPaused annotation = method.getAnnotation(AvailableWhenTenantIsPaused.class);
        assertTrue("Annotation @AvailableWhenTenantIsPaused(only=true) should be present on API method 'installBusinessDataRepository(byte[])'",
                annotation != null && annotation.only());

    }

    @Test
    public void uninstallBDRShouldBeAvailableWhenTenantIsPaused_ONLY() throws Exception {
        final Method method = TenantManagementAPIExt.class.getMethod("uninstallBusinessDataRepository");
        AvailableWhenTenantIsPaused annotation = method.getAnnotation(AvailableWhenTenantIsPaused.class);
        assertTrue("Annotation @AvailableWhenTenantIsPaused(only=true) should be present on API method 'uninstallBusinessDataRepository()'", annotation != null
                && annotation.only());

    }

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
    public void pauseTenantOnActivatedTenantShouldUpdateTenantState() throws Exception {
        STenant tenant = mock(STenant.class);
        final TenantManagementAPIExt tenantManagementAPI = spy(new TenantManagementAPIExt());
        PlatformServiceAccessor platformServiceAccessor = mock(PlatformServiceAccessor.class);
        doReturn(platformServiceAccessor).when(tenantManagementAPI).getPlatformAccessorNoException();
        final PlatformService platformService = mock(PlatformService.class);
        doReturn(platformService).when(platformServiceAccessor).getPlatformService();
        final long tenantId = 223L;
        doReturn(tenant).when(platformService).getTenant(tenantId);
        doReturn(STenant.ACTIVATED).when(tenant).getStatus();
        doReturn(tenantId).when(tenantManagementAPI).getTenantId();
        doNothing().when(tenantManagementAPI).updateTenant(eq(platformService), any(EntityUpdateDescriptor.class), any(STenant.class));
        doNothing().when(tenantManagementAPI).pauseServicesForTenant(eq(platformServiceAccessor), any(BroadcastService.class), eq(tenantId));
        doReturn(platformService).when(tenantManagementAPI).getPlatformService();

        tenantManagementAPI.pause();

        final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        final String statusKey = BuilderFactory.get(STenantBuilderFactory.class).getStatusKey();
        entityUpdateDescriptor.addField(statusKey, STenant.PAUSED);

        verify(tenantManagementAPI).updateTenant(platformService, entityUpdateDescriptor, tenant);
    }

    @Test
    public void tenantManagementAPIShouldHaveClassAnnotation() throws Exception {
        // then:
        assertTrue("Annotation @AvailableWhenTenantIsPaused should be present on API class TenantManagementAPIExt",
                TenantManagementAPIExt.class.isAnnotationPresent(AvailableWhenTenantIsPaused.class));
    }

}
