package com.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
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

import java.lang.reflect.Method;
import java.util.Arrays;

import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.api.impl.TenantConfiguration;
import org.bonitasoft.engine.api.impl.transaction.SetServiceState;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.execution.work.RestartException;
import org.bonitasoft.engine.execution.work.TenantRestartHandler;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.platform.model.impl.STenantImpl;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.work.SWorkException;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.api.impl.transaction.PauseServiceStrategy;
import com.bonitasoft.engine.api.impl.transaction.ResumeServiceStrategy;
import com.bonitasoft.engine.business.data.BusinessDataModelRepository;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import com.bonitasoft.engine.businessdata.BusinessDataRepositoryException;
import com.bonitasoft.engine.service.BroadcastService;
import com.bonitasoft.engine.service.PlatformServiceAccessor;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.bonitasoft.engine.service.impl.BroadcastServiceLocal;

@RunWith(MockitoJUnitRunner.class)
public class TenantManagementAPIExtTest {

    @Spy
    private final TenantManagementAPIExt tenantManagementAPI = new TenantManagementAPIExt();

    @Mock
    private PlatformService platformService;

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private PlatformServiceAccessor platformServiceAccessor;

    @Mock
    private SessionService sessionService;

    @Mock
    private WorkService workService;

    @Mock
    private BusinessDataRepository businessDataRepository;

    @Mock
    private TenantServiceAccessor tenantServiceAccessor;

    @Mock
    private TenantConfiguration tenantConfiguration;

    @Mock
    private TechnicalLoggerService tenantLogger;

    @Mock
    private NodeConfiguration nodeConfiguration;

    @Mock
    private TenantRestartHandler tenantRestartHandler1;

    @Mock
    private TenantRestartHandler tenantRestartHandler2;

    @Mock
    private ClassLoaderService classLoaderService;

    @Mock
    private ProcessDefinitionService processDefinitionService;

    private long tenantId;

    private STenantImpl sTenant;

    private final BroadcastService broadcastService = new BroadcastServiceLocal();

    @Before
    public void before() throws Exception {
        tenantId = 17;

        doReturn(platformServiceAccessor).when(tenantManagementAPI).getPlatformAccessorNoException();
        final SetServiceState spyPause = spy(new SetServiceState(tenantId, new PauseServiceStrategy()));
        doReturn(spyPause).when(tenantManagementAPI).getPauseService(tenantId);
        doReturn(platformServiceAccessor).when(spyPause).getPlatformAccessor();
        final SetServiceState resumePause = spy(new SetServiceState(tenantId, new ResumeServiceStrategy()));
        doReturn(resumePause).when(tenantManagementAPI).getResumeService(tenantId);
        doReturn(platformServiceAccessor).when(resumePause).getPlatformAccessor();

        when(platformServiceAccessor.getBroadcastService()).thenReturn(broadcastService);
        when(platformServiceAccessor.getSchedulerService()).thenReturn(schedulerService);
        when(platformServiceAccessor.getPlatformService()).thenReturn(platformService);
        when(platformServiceAccessor.getPlaformConfiguration()).thenReturn(nodeConfiguration);
        when(platformServiceAccessor.getSessionService()).thenReturn(sessionService);
        when(platformServiceAccessor.getTenantServiceAccessor(tenantId)).thenReturn(tenantServiceAccessor);
        when(tenantServiceAccessor.getTenantConfiguration()).thenReturn(tenantConfiguration);
        when(tenantServiceAccessor.getProcessDefinitionService()).thenReturn(processDefinitionService);
        when(tenantConfiguration.getLifecycleServices()).thenReturn(Arrays.asList(workService, businessDataRepository));
        when(tenantServiceAccessor.getTechnicalLoggerService()).thenReturn(tenantLogger);
        when(tenantServiceAccessor.getClassLoaderService()).thenReturn(classLoaderService);
        when(tenantServiceAccessor.getDependencyService()).thenReturn(mock(DependencyService.class));

        when(nodeConfiguration.getTenantRestartHandlers()).thenReturn(Arrays.asList(tenantRestartHandler1, tenantRestartHandler2));

        doReturn(tenantId).when(tenantManagementAPI).getTenantId();
        sTenant = new STenantImpl("myTenant", "john", 123456789, STenant.PAUSED, false);
        when(platformService.getTenant(tenantId)).thenReturn(sTenant);

        doNothing().when(tenantManagementAPI).resolveDependenciesForAllProcesses();
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
        doThrow(SWorkException.class).when(workService).resume();

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
    public void resume_tenant_should_resolve_dependecies_for_deployed_processes() throws Exception {

        tenantManagementAPI.resume();

        verify(tenantManagementAPI).resolveDependenciesForAllProcesses();
    }

    @Test
    // public void should_setMaintenanceMode_to_MAINTENANCE_pause_jobs() throws Exception {
    public void setTenantMaintenanceModeShouldUpdateMaintenanceField() throws Exception {
        whenTenantIsInState(STenant.ACTIVATED);

        tenantManagementAPI.pause();

        final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        final String inMaintenanceKey = BuilderFactory.get(STenantBuilderFactory.class).getStatusKey();
        entityUpdateDescriptor.addField(inMaintenanceKey, STenant.PAUSED);

        verify(platformService).updateTenant(sTenant, entityUpdateDescriptor);
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

        final boolean present = method.isAnnotationPresent(AvailableWhenTenantIsPaused.class)
                || TenantManagementAPIExt.class.isAnnotationPresent(AvailableWhenTenantIsPaused.class);

        assertThat(present).as("Annotation @AvailableWhenTenantIsPaused should be present on API method 'resume' or directly on class TenantManagementAPIExt")
                .isTrue();
    }

    @Test
    public void pauseTenantShouldHaveAnnotationAvailableWhenTenantIsPaused() throws Exception {
        final Method method = TenantManagementAPIExt.class.getMethod("pause");

        final boolean present = method.isAnnotationPresent(AvailableWhenTenantIsPaused.class)
                || TenantManagementAPIExt.class.isAnnotationPresent(AvailableWhenTenantIsPaused.class);

        assertThat(present).as("Annotation @AvailableWhenTenantIsPaused should be present on API method 'pause' or directly on class TenantManagementAPIExt")
                .isTrue();
    }

    // @Test
    //
    // final Method method = LoginAPIExt.class.getMethod("login", long.class, String.class, String.class);
    //
    // // then:
    // assertThat(method.isAnnotationPresent(AvailableWhenTenantIsPaused.class)).as(
    // "Annotation @AvailableWhenTenantIsPaused should be present on API method LoginAPIExt.login(long, String, String)").isTrue();
    // }
    //
    // @Test
    // public void loginWithT final Method method = LoginAPIExt.class.getMethod("login", String.class, String.class);
    // final Method method = LoginAPIExt.class.getMethod("login", String.class, String.class);
    //
    // // then:
    // assertThat(method.isAnnotationPresent(AvailableWhenTenantIsPaused.class)).as(
    // "Annotation @AvailableWhenTenantIsPaused should be present on API method LoginAPIExt.login(String, String)").isTrue();
    // }

    @Test
    public void pageApi_shouldBeAvailable_in_maintenance_mode() throws Exception {
        // given:
        final Class<PageAPIExt> classPageApiExt = PageAPIExt.class;

        // then:
        assertThat(classPageApiExt.isAnnotationPresent(AvailableWhenTenantIsPaused.class)).as(
                "Annotation @AvailableOnMaintenanceTenant should be present on PageAPIExt");

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
        final Method method = TenantManagementAPIExt.class.getMethod("installBusinessDataModel", byte[].class);
        final AvailableWhenTenantIsPaused annotation = method.getAnnotation(AvailableWhenTenantIsPaused.class);

        final boolean present = annotation != null && annotation.only();
        assertThat(present).as("Annotation @AvailableWhenTenantIsPaused(only=true) should be present on API method 'installBusinessDataModel(byte[])'")
                .isTrue();
    }

    @Test
    public void uninstallBDRShouldBeAvailableWhenTenantIsPaused_ONLY() throws Exception {
        final Method method = TenantManagementAPIExt.class.getMethod("uninstallBusinessDataModel");
        final AvailableWhenTenantIsPaused annotation = method.getAnnotation(AvailableWhenTenantIsPaused.class);

        final boolean present = annotation != null && annotation.only();
        assertThat(present).as("Annotation @AvailableWhenTenantIsPaused(only=true) should be present on API method 'uninstallBusinessDataModel()'").isTrue();

    }

    @Test
    public void uninstallBusinessDataRepository() throws Exception {
        final TenantServiceAccessor accessor = mock(TenantServiceAccessor.class);
        final BusinessDataModelRepository repository = mock(BusinessDataModelRepository.class);
        final TenantManagementAPIExt tenantManagementAPI = spy(new TenantManagementAPIExt());
        doReturn(accessor).when(tenantManagementAPI).getTenantAccessor();
        when(accessor.getBusinessDataModelRepository()).thenReturn(repository);

        tenantManagementAPI.uninstallBusinessDataModel();

        verify(repository).uninstall(anyLong());
    }

    @Test(expected = BusinessDataRepositoryException.class)
    public void uninstallBusinessDataModelThrowException() throws Exception {
        final TenantServiceAccessor accessor = mock(TenantServiceAccessor.class);
        final BusinessDataModelRepository repository = mock(BusinessDataModelRepository.class);
        final TenantManagementAPIExt tenantManagementAPI = spy(new TenantManagementAPIExt());
        doReturn(accessor).when(tenantManagementAPI).getTenantAccessor();
        when(accessor.getBusinessDataModelRepository()).thenReturn(repository);
        doThrow(new SBusinessDataRepositoryException("error")).when(repository).uninstall(anyLong());

        tenantManagementAPI.uninstallBusinessDataModel();
    }

    @Test
    public void pauseTenantOnActivatedTenantShouldUpdateTenantState() throws Exception {
        final STenant tenant = mock(STenant.class);
        final TenantManagementAPIExt tenantManagementAPI = spy(new TenantManagementAPIExt());
        final PlatformServiceAccessor platformServiceAccessor = mock(PlatformServiceAccessor.class);
        doReturn(platformServiceAccessor).when(tenantManagementAPI).getPlatformAccessorNoException();
        final PlatformService platformService = mock(PlatformService.class);
        doReturn(platformService).when(platformServiceAccessor).getPlatformService();
        final long tenantId = 223L;
        doReturn(tenant).when(platformService).getTenant(tenantId);
        doReturn(STenant.ACTIVATED).when(tenant).getStatus();
        doReturn(tenantId).when(tenantManagementAPI).getTenantId();
        doNothing().when(tenantManagementAPI).updateTenant(eq(platformService), any(EntityUpdateDescriptor.class), any(STenant.class));
        doNothing().when(tenantManagementAPI).pauseServicesForTenant(eq(platformServiceAccessor), any(BroadcastService.class), eq(tenantId));
        doReturn(platformService).when(platformServiceAccessor).getPlatformService();

        tenantManagementAPI.pause();

        final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        final String statusKey = BuilderFactory.get(STenantBuilderFactory.class).getStatusKey();
        entityUpdateDescriptor.addField(statusKey, STenant.PAUSED);

        verify(tenantManagementAPI).updateTenant(platformService, entityUpdateDescriptor, tenant);
    }

    @Test
    public void tenantManagementAPIShouldHaveClassAnnotation() throws Exception {
        // then:
        assertThat(TenantManagementAPIExt.class.isAnnotationPresent(AvailableWhenTenantIsPaused.class)).as(
                "Annotation @AvailableWhenTenantIsPaused should be present on API class TenantManagementAPIExt").isTrue();
    }

}
