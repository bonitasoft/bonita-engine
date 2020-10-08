/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.bonitasoft.engine.commons.exceptions.SLifecycleException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.configuration.NodeConfiguration;
import org.bonitasoft.engine.platform.exception.STenantNotFoundException;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.service.BroadcastService;
import org.bonitasoft.engine.service.TaskResult;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.SWorkException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class TenantStateManagerTest {

    public static final long TENANT_ID = 12L;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private BroadcastService broadcastService;
    @Mock
    private UserTransactionService userTransactionService;
    @Mock
    private SchedulerService schedulerService;
    @Mock
    private PlatformService platformService;
    @Mock
    private NodeConfiguration nodeConfiguration;
    @Mock
    private SessionService sessionService;
    @Mock
    private TenantServicesManager tenantServicesManager;

    private TenantStateManager tenantStateManager;
    private STenant tenant;

    @Before
    public void before() throws Exception {
        when(userTransactionService.executeInTransaction(any()))
                .thenAnswer(invocationOnMock -> ((Callable) invocationOnMock.getArgument(0)).call());
        tenantStateManager = new TenantStateManager(userTransactionService,
                platformService, nodeConfiguration, sessionService,
                TENANT_ID, schedulerService, broadcastService, tenantServicesManager);
        tenant = new STenant();
        when(platformService.getTenant(TENANT_ID)).thenReturn(tenant);
    }

    private static Map<String, TaskResult<String>> okFuture() {
        return singletonMap("workService", TaskResult.ok("ok"));
    }

    @Test
    public void pause_should_change_state_then_pause_tenant_and_jobs() throws Exception {
        tenant.setStatus(STenant.ACTIVATED);

        tenantStateManager.pause();

        InOrder inOrder = inOrder(schedulerService, tenantServicesManager, platformService);
        inOrder.verify(platformService).pauseTenant(TENANT_ID);
        inOrder.verify(schedulerService).pauseJobs(TENANT_ID);
        inOrder.verify(tenantServicesManager).pause();
    }

    @Test
    public void resume_should_activate_tenant_resume_services_and_resume_jobs() throws Exception {
        tenant.setStatus(STenant.PAUSED);

        tenantStateManager.resume();

        InOrder inOrder = inOrder(platformService, tenantServicesManager, schedulerService);
        inOrder.verify(platformService).activateTenant(TENANT_ID);
        inOrder.verify(tenantServicesManager).resume();
        inOrder.verify(schedulerService).resumeJobs(TENANT_ID);
    }

    @Test
    public void should_throw_exception_when_resuming_a_tenant_not_paused() {
        tenant.setStatus(STenant.ACTIVATED);

        assertThatThrownBy(() -> tenantStateManager.resume())
                .isInstanceOf(UpdateException.class)
                .hasMessage("Can't resume a tenant in state ACTIVATED");
    }

    @Test
    public void should_throw_exception_when_pausing_a_tenant_already_paused() {
        tenant.setStatus(STenant.PAUSED);

        assertThatThrownBy(() -> tenantStateManager.pause())
                .isInstanceOf(UpdateException.class)
                .hasMessage("Can't pause a tenant in state PAUSED");
    }

    @Test(expected = UpdateException.class)
    public void resume_should_throw_UpdateException_when_resuming_tenant_service_with_a_lifecycle_timeout()
            throws Exception {
        // Given
        TaskResult<Void> taskResult = new TaskResult<>(5L, TimeUnit.HOURS);
        doReturn(singletonMap("workService", taskResult)).when(broadcastService)
                .executeOnOthersAndWait(any(), eq(TENANT_ID));

        // When a tenant moved to available mode
        tenantStateManager.resume();
    }

    @Test(expected = STenantNotFoundException.class)
    public void pause_should_throw_STenantNotFoundException_on_a_non_existing_tenant() throws Exception {
        doThrow(STenantNotFoundException.class).when(platformService).getTenant(TENANT_ID);

        tenantStateManager.pause();
    }

    @Test(expected = UpdateException.class)
    public void resume_should_throw_UpdateException_when_resuming_tenant_service_with_lifecycle_fail()
            throws Exception {
        // Given
        TaskResult<Void> taskResult = new TaskResult<>(new SWorkException("plop"));
        doReturn(singletonMap("workService", taskResult)).when(broadcastService)
                .executeOnOthersAndWait(any(), eq(TENANT_ID));

        // When a tenant moved to available mode
        tenantStateManager.resume();
    }

    @Test
    public void pause_should_update_tenant_in_pause() throws Exception {
        whenTenantIsInState(STenant.ACTIVATED);
        doReturn(okFuture()).when(broadcastService).executeOnOthersAndWait(any(), eq(TENANT_ID));

        tenantStateManager.pause();

        verify(platformService).pauseTenant(TENANT_ID);
    }

    @Test(expected = UpdateException.class)
    public void pause_should_throw_UpdateException_on_a_paused_tenant() throws Exception {
        whenTenantIsInState(STenant.PAUSED);

        tenantStateManager.pause();
    }

    @Test(expected = UpdateException.class)
    public void pause_should_throw_UpdateException_on_a_deactivated_tenant() throws Exception {
        whenTenantIsInState(STenant.DEACTIVATED);

        tenantStateManager.pause();
    }

    @Test(expected = UpdateException.class)
    public void resume_should_throw_UpdateException_on_an_activated_tenant() throws Exception {
        whenTenantIsInState(STenant.ACTIVATED);

        tenantStateManager.resume();
    }

    @Test
    public void resume_should_keep_tenant_paused_on_error() throws Exception {
        whenTenantIsInState(STenant.PAUSED);
        doThrow(SLifecycleException.class).when(tenantServicesManager).resume();

        assertThatThrownBy(() -> tenantStateManager.resume()).isInstanceOf(SLifecycleException.class);
        verify(platformService).pauseTenant(TENANT_ID);
    }

    @Test(expected = UpdateException.class)
    public void resume_should_throw_UpdateException_on_a_deactivated_tenant() throws Exception {
        whenTenantIsInState(STenant.DEACTIVATED);

        tenantStateManager.resume();
    }

    @Test
    public void pause_should_delete_sessions() throws Exception {
        whenTenantIsInState(STenant.ACTIVATED);
        doReturn(okFuture()).when(broadcastService).executeOnOthersAndWait(any(), eq(TENANT_ID));

        tenantStateManager.pause();

        verify(sessionService).deleteSessionsOfTenantExceptTechnicalUser(TENANT_ID);
    }

    @Test
    public void resume_should_delete_sessions() throws Exception {
        whenTenantIsInState(STenant.PAUSED);
        doReturn(okFuture()).when(broadcastService).executeOnOthersAndWait(any(), eq(TENANT_ID));

        tenantStateManager.resume();

        verify(sessionService, times(0)).deleteSessionsOfTenantExceptTechnicalUser(TENANT_ID);
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
        tenantStateManager.pause();

        // Then
        verify(platformService).pauseTenant(TENANT_ID);
    }

    @Test
    public void deactivate_should_stop_services_and_deactivate_tenant_in_db_and_delete_sessions() throws Exception {
        whenTenantIsInState(STenant.ACTIVATED);

        tenantStateManager.deactivate();

        InOrder inOrder = inOrder(sessionService, platformService, tenantServicesManager, schedulerService);
        inOrder.verify(sessionService).deleteSessionsOfTenant(TENANT_ID);
        inOrder.verify(platformService).deactivateTenant(TENANT_ID);
        inOrder.verify(schedulerService).pauseJobs(TENANT_ID);
        inOrder.verify(tenantServicesManager).stop();
    }

    @Test
    public void activate_should_start_services_and_activate_tenant_in_db_and_resume_jobs() throws Exception {
        whenTenantIsInState(STenant.DEACTIVATED);

        tenantStateManager.activate();

        InOrder inOrder = inOrder(platformService, tenantServicesManager, schedulerService);
        inOrder.verify(platformService).activateTenant(TENANT_ID);
        inOrder.verify(tenantServicesManager).start();
        inOrder.verify(schedulerService).resumeJobs(TENANT_ID);
    }

    @Test
    public void stop_should_stop_services_only() throws Exception {
        // given:
        whenTenantIsInState(STenant.ACTIVATED);
        tenantStateManager.start();
        doReturn(true).when(nodeConfiguration).shouldClearSessions();

        // when:
        tenantStateManager.stop();

        // then:
        InOrder inOrder = inOrder(sessionService, tenantServicesManager);
        inOrder.verify(sessionService).deleteSessions();
        inOrder.verify(tenantServicesManager).stop();
        verify(schedulerService, never()).pauseJobs(TENANT_ID);
        verify(platformService, never()).deactivateTenant(TENANT_ID);
    }

    @Test
    public void start_should_call_start_on_TenantServicesManager() throws Exception {
        whenTenantIsInState(STenant.ACTIVATED);

        tenantStateManager.start();

        verify(tenantServicesManager).start();
    }

}
