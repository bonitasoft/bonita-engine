/**
 * Copyright (C) 2019 BonitaSoft S.A.
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
package org.bonitasoft.engine.tenant;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.api.impl.NodeConfiguration;
import org.bonitasoft.engine.api.impl.TenantConfiguration;
import org.bonitasoft.engine.api.impl.transaction.SetServiceState;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.TenantLifecycleService;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.execution.work.RestartException;
import org.bonitasoft.engine.execution.work.TenantRestartHandler;
import org.bonitasoft.engine.platform.PlatformManager;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.PlatformState;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TaskResult;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.SWorkException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class TenantManagerTest {

    public static final long TENANT_ID = 12L;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private BroadcastService broadcastService;
    @Mock
    private UserTransactionService userTransactionService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private SchedulerService schedulerService;
    @Mock
    private PlatformService platformService;
    @Mock
    private NodeConfiguration nodeConfiguration;
    @Mock
    private PlatformServiceAccessor platformServiceAccessor;
    @Mock
    private TenantServiceAccessor tenantServiceAccessor;
    @Mock
    private SessionService sessionService;
    @Mock
    private SessionAccessor sessionAccessor;
    @Mock
    private ClassLoaderService classloaderService;
    private TenantConfiguration tenantConfiguration = new TenantConfiguration();
    @Mock
    private TenantLifecycleService tenantService1;
    @Mock
    private TenantLifecycleService tenantService2;
    @Mock
    private TenantRestartHandler tenantRestartHandler1;
    @Mock
    private TenantRestartHandler tenantRestartHandler2;

    private TenantManager tenantManager;
    private STenant tenant;
    @Mock
    private PlatformManager platformManager;


    @Before
    public void before() throws Exception {
        when(nodeConfiguration.getTenantRestartHandlers()).thenReturn(Arrays.asList(tenantRestartHandler1, tenantRestartHandler2));
        tenantConfiguration.setLifecycleServices(Arrays.asList(tenantService1, tenantService2));
        when(userTransactionService.executeInTransaction(any())).thenAnswer(invocationOnMock -> ((Callable) invocationOnMock.getArgument(0)).call());
        tenantManager = spy(new TenantManager(userTransactionService,
                platformService, nodeConfiguration, sessionService, sessionAccessor,
                TENANT_ID, classloaderService, tenantConfiguration,
                schedulerService, broadcastService, platformManager));
        doReturn(platformServiceAccessor).when(tenantManager).getPlatformAccessor();
        doReturn(tenantServiceAccessor).when(platformServiceAccessor).getTenantServiceAccessor(TENANT_ID);
        doReturn(nodeConfiguration).when(platformServiceAccessor).getPlatformConfiguration();
        doReturn(transactionService).when(platformServiceAccessor).getTransactionService();
        doReturn(SSession.builder().id(123).build()).when(sessionService).createSession(anyLong(), any());
        tenant = new STenant();
        when(platformService.getTenant(TENANT_ID)).thenReturn(tenant);
    }

    private static Map<String, TaskResult<String>> okFuture() {
        return singletonMap("workService", TaskResult.ok("ok"));
    }

    @Test
    public void pause_should_pause_tenant() throws Exception {
        tenant.setStatus(STenant.ACTIVATED);

        tenantManager.pause();

        verify(schedulerService).pauseJobs(TENANT_ID);
        verify(tenantService1).pause();
        verify(tenantService2).pause();
        verify(platformService).pauseTenant(TENANT_ID);
    }

    @Test
    public void resume_should_resume_tenant() throws Exception {
        tenant.setStatus(STenant.PAUSED);

        tenantManager.resume();

        verify(schedulerService).resumeJobs(TENANT_ID);
        verify(tenantService1).resume();
        verify(tenantService2).resume();
        verify(platformService).activateTenant(TENANT_ID);
    }


    @Test
    public void should_throw_exception_when_resuming_a_tenant_not_paused() {
        tenant.setStatus(STenant.ACTIVATED);

        Assertions.assertThatThrownBy(() ->
                tenantManager.resume()
        )
                .isInstanceOf(UpdateException.class)
                .hasMessage("Can't resume a tenant in state ACTIVATED");
    }

    @Test
    public void should_throw_exception_when_pausing_a_tenant_already_paused() {
        tenant.setStatus(STenant.PAUSED);

        Assertions.assertThatThrownBy(() ->
                tenantManager.pause()
        )
                .isInstanceOf(UpdateException.class)
                .hasMessage("Can't pause a tenant in state PAUSED");
    }

    @Test(expected = UpdateException.class)
    public void resume_should_throw_UpdateException_when_resuming_tenant_service_with_a_lifecycle_timeout() throws Exception {
        // Given
        TaskResult<Void> taskResult = new TaskResult<>(5L, TimeUnit.HOURS);
        doReturn(singletonMap("workService", taskResult)).when(broadcastService).executeOnOthersAndWait(any(SetServiceState.class), eq(TENANT_ID));

        // When a tenant moved to available mode
        tenantManager.resume();
    }

    @Test(expected = STenantNotFoundException.class)
    public void pause_should_throw_STenantNotFoundException_on_a_non_existing_tenant() throws Exception {
        doThrow(STenantNotFoundException.class).when(platformService).getTenant(TENANT_ID);

        tenantManager.pause();
    }

    @Test
    public void resume_should_restart_tenant_handlers() throws Exception {
        // Given
        doReturn(true).when(transactionService).isTransactionActive();
        whenTenantIsInState(STenant.PAUSED);
        doReturn(okFuture()).when(broadcastService).executeOnOthersAndWait(any(SetServiceState.class), eq(TENANT_ID));


        // When a tenant moved to available mode
        tenantManager.resume();

        // Then elements must be restarted
        verify(tenantRestartHandler1).beforeServicesStart(platformServiceAccessor, tenantServiceAccessor);
        verify(tenantRestartHandler2).beforeServicesStart(platformServiceAccessor, tenantServiceAccessor);
    }

    @Test(expected = UpdateException.class)
    public void resume_should_throw_UpdateException_when_resuming_tenant_service_with_lifecycle_fail() throws Exception {
        // Given
        TaskResult<Void> taskResult = new TaskResult<>(new SWorkException("plop"));
        doReturn(singletonMap("workService", taskResult)).when(broadcastService).executeOnOthersAndWait(any(SetServiceState.class), eq(TENANT_ID));

        // When a tenant moved to available mode
        tenantManager.resume();
    }

    @Test(expected = UpdateException.class)
    public void resume_should_throw_exception_when_tenant_restart_handlers_fail() throws Exception {
        // Given
        final TenantRestartHandler tenantRestartHandler1 = mock(TenantRestartHandler.class);
        final TenantRestartHandler tenantRestartHandler2 = mock(TenantRestartHandler.class);
        doThrow(RestartException.class).when(tenantRestartHandler2).beforeServicesStart(platformServiceAccessor, tenantServiceAccessor);
        when(nodeConfiguration.getTenantRestartHandlers()).thenReturn(Arrays.asList(tenantRestartHandler1, tenantRestartHandler2));

        // When a tenant moved to available mode
        tenantManager.resume();
    }

    @Test
    public void pause_should_update_tenant_in_pause() throws Exception {
        whenTenantIsInState(STenant.ACTIVATED);
        doReturn(okFuture()).when(broadcastService).executeOnOthersAndWait(any(SetServiceState.class), eq(TENANT_ID));

        tenantManager.pause();

        verify(platformService).pauseTenant(TENANT_ID);
    }

    @Test(expected = UpdateException.class)
    public void pause_should_throw_UpdateException_on_a_paused_tenant() throws Exception {
        whenTenantIsInState(STenant.PAUSED);

        tenantManager.pause();
    }

    @Test(expected = UpdateException.class)
    public void pause_should_throw_UpdateException_on_a_deactivated_tenant() throws Exception {
        whenTenantIsInState(STenant.DEACTIVATED);

        tenantManager.pause();
    }

    @Test(expected = UpdateException.class)
    public void resume_should_throw_UpdateException_on_an_activated_tenant() throws Exception {
        whenTenantIsInState(STenant.ACTIVATED);

        tenantManager.resume();
    }

    @Test(expected = UpdateException.class)
    public void resume_should_throw_UpdateException_on_a_deactivated_tenant() throws Exception {
        whenTenantIsInState(STenant.DEACTIVATED);

        tenantManager.resume();
    }

    @Test
    public void pause_should_delete_sessions() throws Exception {
        whenTenantIsInState(STenant.ACTIVATED);
        doReturn(okFuture()).when(broadcastService).executeOnOthersAndWait(any(SetServiceState.class), eq(TENANT_ID));

        tenantManager.pause();

        verify(sessionService).deleteSessionsOfTenantExceptTechnicalUser(TENANT_ID);
    }

    @Test
    public void resume_should_delete_sessions() throws Exception {
        whenTenantIsInState(STenant.PAUSED);
        doReturn(okFuture()).when(broadcastService).executeOnOthersAndWait(any(SetServiceState.class), eq(TENANT_ID));

        tenantManager.resume();

        verify(sessionService, times(0)).deleteSessionsOfTenantExceptTechnicalUser(TENANT_ID);
    }

    @Test
    public void resume_should_resume_jobs() throws Exception {
        whenTenantIsInState(STenant.PAUSED);
        doReturn(okFuture()).when(broadcastService).executeOnOthersAndWait(any(SetServiceState.class), eq(TENANT_ID));

        tenantManager.resume();

        verify(schedulerService).resumeJobs(TENANT_ID);
    }

    private void whenTenantIsInState(final String status) throws STenantNotFoundException {
        STenant sTenant = new STenant("myTenant", "john", 123456789, status, false);
        sTenant.setId(TENANT_ID);
        when(platformService.getTenant(TENANT_ID)).thenReturn(sTenant);
    }


    @Test
    public void pause_should_update_tenant_state_on_activated_tenant() throws Exception {
        // Given
        whenTenantIsInState(STenant.ACTIVATED);

        // When
        tenantManager.pause();

        // Then
        verify(platformService).pauseTenant(TENANT_ID);
    }

    @Test
    public void deactivate_should_stop_services_and_deactivate_tenant_in_db() throws Exception {
        whenTenantIsInState(STenant.ACTIVATED);

        tenantManager.deactivate();

        verify(tenantService1).stop();
        verify(tenantService2).stop();
        verify(schedulerService).pauseJobs(TENANT_ID);
        verify(platformService).deactivateTenant(TENANT_ID);
    }

    @Test
    public void activate_should_start_services_and_activate_tenant_in_db() throws Exception {
        whenTenantIsInState(STenant.DEACTIVATED);
        when(platformManager.getState()).thenReturn(PlatformState.STARTED);

        tenantManager.activate();

        verify(tenantService1).start();
        verify(tenantService2).start();
        verify(schedulerService).resumeJobs(TENANT_ID);
        verify(platformService).activateTenant(TENANT_ID);
        assertThat(tenantManager.isStarted()).isTrue();
    }

    @Test
    public void activate_should_not_start_services_when_platform_is_stopped() throws Exception {
        whenTenantIsInState(STenant.DEACTIVATED);
        when(platformManager.getState()).thenReturn(PlatformState.STOPPED);

        tenantManager.activate();

        verify(tenantService1, never()).start();
        verify(tenantService2, never()).start();
        verify(schedulerService, never()).resumeJobs(TENANT_ID);
        verify(platformService).activateTenant(TENANT_ID);
        assertThat(tenantManager.isStarted()).isFalse();
    }

    @Test
    public void stop_should_stop_services_only() throws Exception {
        whenTenantIsInState(STenant.ACTIVATED);
        tenantManager.start();

        tenantManager.stop();

        verify(tenantService1).stop();
        verify(tenantService2).stop();
        verify(schedulerService, never()).pauseJobs(TENANT_ID);
        verify(platformService, never()).deactivateTenant(TENANT_ID);
    }

    @Test
    public void start_should_start_services_only() throws Exception {
        whenTenantIsInState(STenant.ACTIVATED);
        when(platformManager.getState()).thenReturn(PlatformState.STARTED);

        tenantManager.start();

        verify(tenantService1).start();
        verify(tenantService2).start();
        verify(schedulerService).resumeJobs(TENANT_ID);
        verify(platformService, never()).activateTenant(TENANT_ID);
        verify(schedulerService).resumeJobs(TENANT_ID);
    }


}