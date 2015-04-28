/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.bonitasoft.engine.api.impl.resolver.DependencyResolver;
import org.bonitasoft.engine.api.impl.transaction.PauseServiceStrategy;
import org.bonitasoft.engine.api.impl.transaction.ResumeServiceStrategy;
import org.bonitasoft.engine.api.impl.transaction.SetServiceState;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.data.BusinessDataModelRepository;
import org.bonitasoft.engine.business.data.BusinessDataRepositoryException;
import org.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.execution.work.RestartException;
import org.bonitasoft.engine.execution.work.TenantRestartHandler;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.platform.model.builder.STenantBuilderFactory;
import org.bonitasoft.engine.platform.model.builder.STenantUpdateBuilderFactory;
import org.bonitasoft.engine.platform.model.impl.STenantImpl;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TaskResult;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.work.SWorkException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class TenantAdministrationAPIImplTest {

    @Spy
    @InjectMocks
    private TenantAdministrationAPIImpl tenantManagementAPI;

    @Mock
    private TransactionService transactionService;

    @Mock
    private PlatformService platformService;

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private PlatformServiceAccessor platformServiceAccessor;

    @Mock
    private SessionService sessionService;

    @Mock
    private TenantServiceAccessor tenantServiceAccessor;

    @Mock
    private NodeConfiguration nodeConfiguration;

    @Mock
    private BroadcastService broadcastService;

    @Mock
    private DependencyResolver dependencyResolver;

    @Mock
    private TechnicalLoggerService technicalLoggerService;

    private final long tenantId = 17;

    private STenantImpl sTenant = new STenantImpl("myTenant", "john", 123456789, STenant.PAUSED, false);

    @Before
    public void before() throws Exception {
        doReturn(platformServiceAccessor).when(tenantManagementAPI).getPlatformAccessorNoException();
        doReturn(tenantId).when(tenantManagementAPI).getTenantId();
        doReturn(tenantServiceAccessor).when(tenantManagementAPI).getTenantAccessor();

        when(platformServiceAccessor.getTransactionService()).thenReturn(transactionService);
        when(platformServiceAccessor.getBroadcastService()).thenReturn(broadcastService);
        when(platformServiceAccessor.getSchedulerService()).thenReturn(schedulerService);
        when(platformServiceAccessor.getPlatformService()).thenReturn(platformService);
        when(platformServiceAccessor.getPlatformConfiguration()).thenReturn(nodeConfiguration);
        when(platformServiceAccessor.getTechnicalLoggerService()).thenReturn(technicalLoggerService);
        when(platformServiceAccessor.getTenantServiceAccessor(tenantId)).thenReturn(tenantServiceAccessor);

        when(tenantServiceAccessor.getDependencyResolver()).thenReturn(dependencyResolver);
        when(tenantServiceAccessor.getTechnicalLoggerService()).thenReturn(technicalLoggerService);
        when(tenantServiceAccessor.getSessionService()).thenReturn(sessionService);

        when(platformService.getTenant(tenantId)).thenReturn(sTenant);
    }

    @Test
    public void pause_should_pause_tenant_service_with_lifecycle() throws Exception {
        // Given
        whenTenantIsInState(STenant.ACTIVATED);

        // When a tenant moved to pause mode:
        tenantManagementAPI.pause();

        // Then tenant service with lifecycle should be pause
        verify(tenantManagementAPI).setTenantClassloaderAndUpdateStateOfTenantServicesWithLifecycle(eq(platformServiceAccessor), eq(tenantId),
                isA(PauseServiceStrategy.class));
        verify(tenantManagementAPI, never()).setTenantClassloaderAndUpdateStateOfTenantServicesWithLifecycle(eq(platformServiceAccessor), eq(tenantId),
                isA(ResumeServiceStrategy.class));
    }

    @Test
    public void resume_should_resume_tenant_service_with_lifecycle() throws Exception {
        // When a tenant moved to available mode
        tenantManagementAPI.resume();

        // Then tenant service with lifecycle should be resumed
        verify(tenantManagementAPI).setTenantClassloaderAndUpdateStateOfTenantServicesWithLifecycle(eq(platformServiceAccessor), eq(tenantId),
                isA(ResumeServiceStrategy.class));
        verify(tenantManagementAPI, never()).setTenantClassloaderAndUpdateStateOfTenantServicesWithLifecycle(eq(platformServiceAccessor), eq(tenantId),
                isA(PauseServiceStrategy.class));
    }

    @Test(expected = UpdateException.class)
    public void resume_should_throw_UpdateException_when_resuming_tenant_service_with_lifecycle_fail() throws Exception {
        // Given
        TaskResult<Void> taskResult = new TaskResult<Void>(new SWorkException("plop"));
        doReturn(Collections.singletonMap("workService", taskResult)).when(broadcastService).execute(any(SetServiceState.class), eq(tenantId));

        // When a tenant moved to available mode
        tenantManagementAPI.resume();
    }

    @Test(expected = UpdateException.class)
    public void resume_should_throw_UpdateException_when_resuming_tenant_service_with_lifecycle_is_time_out() throws Exception {
        // Given
        TaskResult<Void> taskResult = new TaskResult<Void>(5l, TimeUnit.HOURS);
        doReturn(Collections.singletonMap("workService", taskResult)).when(broadcastService).execute(any(SetServiceState.class), eq(tenantId));

        // When a tenant moved to available mode
        tenantManagementAPI.resume();
    }

    @Test
    public void resume_should_restart_tenant_handlers() throws Exception {
        // Given
        final TenantRestartHandler tenantRestartHandler1 = mock(TenantRestartHandler.class);
        final TenantRestartHandler tenantRestartHandler2 = mock(TenantRestartHandler.class);
        when(nodeConfiguration.getTenantRestartHandlers()).thenReturn(Arrays.asList(tenantRestartHandler1, tenantRestartHandler2));

        // When a tenant moved to available mode
        tenantManagementAPI.resume();

        // Then elements must be restarted
        verify(tenantRestartHandler1, times(1)).beforeServicesStart(platformServiceAccessor, tenantServiceAccessor);
        verify(tenantRestartHandler2, times(1)).beforeServicesStart(platformServiceAccessor, tenantServiceAccessor);
    }

    @Test(expected = UpdateException.class)
    public void resume_should_throw_exception_when_restart_tenant_handlers_fail() throws Exception {
        // Given
        final TenantRestartHandler tenantRestartHandler1 = mock(TenantRestartHandler.class);
        final TenantRestartHandler tenantRestartHandler2 = mock(TenantRestartHandler.class);
        doThrow(RestartException.class).when(tenantRestartHandler2).beforeServicesStart(platformServiceAccessor, tenantServiceAccessor);
        when(nodeConfiguration.getTenantRestartHandlers()).thenReturn(Arrays.asList(tenantRestartHandler1, tenantRestartHandler2));

        // When a tenant moved to available mode
        tenantManagementAPI.resume();
    }

    @Test
    public void resume_should_resolve_dependecies_for_deployed_processes() throws Exception {
        tenantManagementAPI.resume();

        verify(dependencyResolver).resolveDependenciesForAllProcesses(tenantServiceAccessor);
    }

    @Test
    public void pause_should_update_tenant_in_pause() throws Exception {
        whenTenantIsInState(STenant.ACTIVATED);

        tenantManagementAPI.pause();

        final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        final String inMaintenanceKey = BuilderFactory.get(STenantBuilderFactory.class).getStatusKey();
        entityUpdateDescriptor.addField(inMaintenanceKey, STenant.PAUSED);

        verify(platformService).updateTenant(sTenant, entityUpdateDescriptor);
    }

    @Test
    public void resume_should_pause_jobs() throws Exception {
        tenantManagementAPI.resume();

        verify(schedulerService).resumeJobs(tenantId);
    }

    @Test
    public void pause_should_delete_sessions() throws Exception {
        whenTenantIsInState(STenant.ACTIVATED);

        tenantManagementAPI.pause();

        verify(sessionService).deleteSessionsOfTenantExceptTechnicalUser(tenantId);
    }

    @Test
    public void resume_should_delete_sessions() throws Exception {
        tenantManagementAPI.resume();

        verify(sessionService, times(0)).deleteSessionsOfTenantExceptTechnicalUser(tenantId);
    }

    @Test
    public void resume_should_have_annotation_available_when_tenant_is_paused() throws Exception {
        final Method method = TenantAdministrationAPIImpl.class.getMethod("resume");

        final boolean present = method.isAnnotationPresent(AvailableWhenTenantIsPaused.class)
                || TenantAdministrationAPIImpl.class.isAnnotationPresent(AvailableWhenTenantIsPaused.class);

        assertThat(present).as("Annotation @AvailableWhenTenantIsPaused should be present on API method 'resume' or directly on class TenantManagementAPIExt")
                .isTrue();
    }

    @Test
    public void pause_should_have_annotation_available_when_tenant_is_paused() throws Exception {
        final Method method = TenantAdministrationAPIImpl.class.getMethod("pause");

        final boolean present = method.isAnnotationPresent(AvailableWhenTenantIsPaused.class)
                || TenantAdministrationAPIImpl.class.isAnnotationPresent(AvailableWhenTenantIsPaused.class);

        assertThat(present).as("Annotation @AvailableWhenTenantIsPaused should be present on API method 'pause' or directly on class TenantManagementAPIExt")
                .isTrue();
    }

    @Test
    public void pageApi_should_be_available_in_maintenance_mode() {
        // given:
        final Class<PageAPIImpl> classPageApiExt = PageAPIImpl.class;

        // then:
        assertThat(classPageApiExt.isAnnotationPresent(AvailableWhenTenantIsPaused.class)).as(
                "Annotation @AvailableOnMaintenanceTenant should be present on PageAPIIml");
    }

    @Test(expected = UpdateException.class)
    public void pause_should_throw_UpdateException_on_a_paused_tenant() throws Exception {
        whenTenantIsInState(STenant.PAUSED);

        tenantManagementAPI.pause();
    }

    @Test(expected = UpdateException.class)
    public void pause_should_throw_UpdateException_on_a_deactivated_tenant() throws Exception {
        whenTenantIsInState(STenant.DEACTIVATED);

        tenantManagementAPI.pause();
    }

    @Test(expected = UpdateException.class)
    public void resume_should_throw_UpdateException_on_a_paused_tenant() throws Exception {
        whenTenantIsInState(STenant.ACTIVATED);

        tenantManagementAPI.resume();
    }

    @Test(expected = UpdateException.class)
    public void resume_should_throw_UpdateException_on_a_deactivated_tenant() throws Exception {
        whenTenantIsInState(STenant.DEACTIVATED);

        tenantManagementAPI.resume();
    }

    private void whenTenantIsInState(final String status) throws STenantNotFoundException {
        sTenant = new STenantImpl("myTenant", "john", 123456789, status, false);
        when(platformService.getTenant(tenantId)).thenReturn(sTenant);
    }

    @Test(expected = UpdateException.class)
    public void pause_should_throw_UpdateException_on_a_unexisting_tenant() throws Exception {
        doThrow(STenantNotFoundException.class).when(platformService).getTenant(tenantId);

        tenantManagementAPI.resume();
    }

    @Test
    public void installBDR_should_be_available_when_tenant_is_paused_ONLY() throws Exception {
        final Method method = TenantAdministrationAPIImpl.class.getMethod("installBusinessDataModel", byte[].class);
        final AvailableWhenTenantIsPaused annotation = method.getAnnotation(AvailableWhenTenantIsPaused.class);

        final boolean present = annotation != null && annotation.only();
        assertThat(present).as("Annotation @AvailableWhenTenantIsPaused(only=true) should be present on API method 'installBusinessDataModel(byte[])'")
                .isTrue();
    }

    @Test
    public void uninstallBDR_should_be_available_when_tenant_is_paused_ONLY() throws Exception {
        final Method method = TenantAdministrationAPIImpl.class.getMethod("uninstallBusinessDataModel");
        final AvailableWhenTenantIsPaused annotation = method.getAnnotation(AvailableWhenTenantIsPaused.class);

        final boolean present = annotation != null && annotation.only();
        assertThat(present).as("Annotation @AvailableWhenTenantIsPaused(only=true) should be present on API method 'uninstallBusinessDataModel()'").isTrue();
    }

    @Test
    public void uninstallBusinessDataModel_should_work() throws Exception {
        // Given
        final BusinessDataModelRepository repository = mock(BusinessDataModelRepository.class);
        when(tenantServiceAccessor.getBusinessDataModelRepository()).thenReturn(repository);

        // When
        tenantManagementAPI.uninstallBusinessDataModel();

        // Then
        verify(repository).uninstall(anyLong());
    }

    @Test(expected = BusinessDataRepositoryException.class)
    public void uninstallBusinessDataModel_should_throw_BusinessDataRepositoryException() throws Exception {
        // Given
        final BusinessDataModelRepository repository = mock(BusinessDataModelRepository.class);
        when(tenantServiceAccessor.getBusinessDataModelRepository()).thenReturn(repository);
        doThrow(new SBusinessDataRepositoryException("error")).when(repository).uninstall(anyLong());

        // When
        tenantManagementAPI.uninstallBusinessDataModel();
    }

    @Test
    public void pause_should_update_tenant_state_on_activated_tenant() throws Exception {
        // Given
        sTenant.setStatus(STenant.ACTIVATED);
        doNothing().when(platformService).updateTenant(any(STenant.class), any(EntityUpdateDescriptor.class));
        doNothing().when(tenantManagementAPI).pauseServicesForTenant(eq(platformServiceAccessor), eq(tenantId));

        // When
        tenantManagementAPI.pause();

        // Then
        final EntityUpdateDescriptor entityUpdateDescriptor = new EntityUpdateDescriptor();
        entityUpdateDescriptor.addField(STenantUpdateBuilderFactory.STATUS, STenant.PAUSED);
        verify(platformService).updateTenant(sTenant, entityUpdateDescriptor);
    }

    @Test
    public void tenantManagementAPI_should_have_class_annotation() {
        // then:
        assertThat(TenantAdministrationAPIImpl.class.isAnnotationPresent(AvailableWhenTenantIsPaused.class)).as(
                "Annotation @AvailableWhenTenantIsPaused should be present on API class TenantManagementAPIExt").isTrue();
    }

}
