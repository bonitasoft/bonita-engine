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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.bonitasoft.engine.commons.exceptions.SLifecycleException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.platform.configuration.NodeConfiguration;
import org.bonitasoft.engine.platform.exception.SPlatformNotFoundException;
import org.bonitasoft.engine.platform.model.SPlatform;
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
    private SPlatform platform;

    @Before
    public void before() throws Exception {
        when(userTransactionService.executeInTransaction(any()))
                .thenAnswer(invocationOnMock -> ((Callable) invocationOnMock.getArgument(0)).call());
        tenantStateManager = new TenantStateManager(userTransactionService,
                platformService, nodeConfiguration, sessionService,
                schedulerService, broadcastService, tenantServicesManager);
        platform = new SPlatform();
        when(platformService.getPlatform()).thenReturn(platform);
    }

    private static Map<String, TaskResult<String>> okFuture() {
        return singletonMap("workService", TaskResult.ok("ok"));
    }

    @Test
    public void pause_should_change_state_then_pause_platform_and_jobs() throws Exception {
        platform.setMaintenanceEnabled(false);

        tenantStateManager.pause();

        InOrder inOrder = inOrder(schedulerService, tenantServicesManager, platformService);
        inOrder.verify(platformService).pauseServices();
        inOrder.verify(schedulerService).pauseJobs();
        inOrder.verify(tenantServicesManager).pause();
    }

    @Test
    public void resume_should_resume_services_and_resume_jobs() throws Exception {
        platform.setMaintenanceEnabled(true);

        tenantStateManager.resume();

        InOrder inOrder = inOrder(platformService, tenantServicesManager, schedulerService);
        inOrder.verify(platformService).resumeServices();
        inOrder.verify(tenantServicesManager).resume();
        inOrder.verify(schedulerService).resumeJobs();
    }

    @Test
    public void should_throw_exception_when_resuming_a_platform_not_paused() {
        platform.setMaintenanceEnabled(false);

        assertThatThrownBy(() -> tenantStateManager.resume())
                .isInstanceOf(UpdateException.class)
                .hasMessage("Can't resume platform in state RESUMED");
    }

    @Test
    public void should_throw_exception_when_pausing_a_platform_already_paused() {
        platform.setMaintenanceEnabled(true);

        assertThatThrownBy(() -> tenantStateManager.pause())
                .isInstanceOf(UpdateException.class)
                .hasMessage("Can't pause platform in state PAUSED");
    }

    @Test(expected = UpdateException.class)
    public void resume_should_throw_UpdateException_when_resuming_platform_service_with_a_lifecycle_timeout()
            throws Exception {
        // Given
        TaskResult<Void> taskResult = new TaskResult<>(5L, TimeUnit.HOURS);
        doReturn(singletonMap("workService", taskResult)).when(broadcastService).executeOnOthersAndWait(any());

        // When platform moved to available mode
        tenantStateManager.resume();
    }

    @Test
    public void pause_should_throw_SPlatformNotFoundException_on_a_non_existing_platform() throws Exception {
        doThrow(SPlatformNotFoundException.class).when(platformService).getPlatform();

        assertThatThrownBy(() -> tenantStateManager.pause()).isInstanceOf(SPlatformNotFoundException.class);
    }

    @Test(expected = UpdateException.class)
    public void resume_should_throw_UpdateException_when_resuming_service_with_lifecycle_fail()
            throws Exception {
        // Given
        TaskResult<Void> taskResult = new TaskResult<>(new SWorkException("plop"));
        doReturn(singletonMap("workService", taskResult)).when(broadcastService)
                .executeOnOthersAndWait(any());

        // When platform moved to available mode
        tenantStateManager.resume();
    }

    @Test
    public void pause_should_update_platform_in_pause() throws Exception {
        whenPlatformIsInPausedStatus(false);
        doReturn(okFuture()).when(broadcastService).executeOnOthersAndWait(any());

        tenantStateManager.pause();

        verify(platformService).pauseServices();
    }

    @Test(expected = UpdateException.class)
    public void pause_should_throw_UpdateException_on_a_paused_platform() throws Exception {
        whenPlatformIsInPausedStatus(true);

        tenantStateManager.pause();
    }

    @Test(expected = UpdateException.class)
    public void resume_should_throw_UpdateException_on_an_activated_platform() throws Exception {
        whenPlatformIsInPausedStatus(false);

        tenantStateManager.resume();
    }

    @Test
    public void resume_should_keep_platform_paused_on_error() throws Exception {
        whenPlatformIsInPausedStatus(true);
        doThrow(SLifecycleException.class).when(tenantServicesManager).resume();

        assertThatThrownBy(() -> tenantStateManager.resume()).isInstanceOf(SLifecycleException.class);
        verify(platformService).pauseServices();
    }

    @Test
    public void resume_should_not_delete_sessions() throws Exception {
        whenPlatformIsInPausedStatus(true);
        doReturn(okFuture()).when(broadcastService).executeOnOthersAndWait(any());

        tenantStateManager.resume();

        verify(sessionService, times(0)).deleteSessionsExceptTechnicalUser();
    }

    private void whenPlatformIsInPausedStatus(final boolean status) throws SPlatformNotFoundException {
        SPlatform platform = new SPlatform("10.3", "10.3.0", "0.0.0", null, false, "platformAdmin", 999888777L, status);
        when(platformService.getPlatform()).thenReturn(platform);
    }

    @Test
    public void pause_should_update_platform_state_on_activated_platform() throws Exception {
        // Given
        whenPlatformIsInPausedStatus(false);

        // When
        tenantStateManager.pause();

        // Then
        verify(platformService).pauseServices();
    }

    @Test
    public void stop_should_stop_services_only() throws Exception {
        // given:
        whenPlatformIsInPausedStatus(false);
        tenantStateManager.start();
        doReturn(true).when(nodeConfiguration).shouldClearSessions();

        // when:
        tenantStateManager.stop();

        // then:
        InOrder inOrder = inOrder(sessionService, tenantServicesManager);
        inOrder.verify(sessionService).deleteSessions();
        inOrder.verify(tenantServicesManager).stop();
        verify(schedulerService, never()).pauseJobs();
    }

    @Test
    public void start_should_call_start_on_ServicesManager() throws Exception {
        whenPlatformIsInPausedStatus(false);

        tenantStateManager.start();

        verify(tenantServicesManager).start();
    }

    @Test
    public void start_should_init_services_even_if_platform_paused() throws Exception {
        whenPlatformIsInPausedStatus(true);

        tenantStateManager.start();

        verify(tenantServicesManager).initServices();

    }

}
