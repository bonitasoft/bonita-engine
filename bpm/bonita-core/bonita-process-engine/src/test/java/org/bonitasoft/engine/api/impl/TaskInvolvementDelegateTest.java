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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SExecutionException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TaskInvolvementDelegateTest {

    private static final long EXISTING_TASK = 56789465L;
    private static final long ASSIGNED_TASK = 54789465L;
    private static final long ASSIGNED_USER = 1234595L;
    private static final long USER_PENDING = 4545666L;
    private static final long PROCESS_DEFINITION_ID = 123456789L;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private ActivityInstanceService activityInstanceService;

    @Mock
    TenantServiceAccessor tenantServiceAccessor;

    @Mock
    public ProcessAPIImpl processAPI;

    @Mock
    private SUserTaskInstanceImpl humanTaskInstance;

    private SUserTaskInstanceImpl assignedHumanTaskInstance;

    @InjectMocks
    @Spy
    public TaskInvolvementDelegate taskInvolvementDelegate;

    @Before
    public void before() throws SBonitaException {
        doReturn(tenantServiceAccessor).when(taskInvolvementDelegate).getTenantServiceAccessor();
        doReturn(activityInstanceService).when(tenantServiceAccessor).getActivityInstanceService();

        humanTaskInstance = new SUserTaskInstanceImpl();
        humanTaskInstance.setId(EXISTING_TASK);
        humanTaskInstance.setLogicalGroup(1, PROCESS_DEFINITION_ID);

        assignedHumanTaskInstance = new SUserTaskInstanceImpl();
        assignedHumanTaskInstance.setId(ASSIGNED_TASK);
        assignedHumanTaskInstance.setLogicalGroup(1, PROCESS_DEFINITION_ID);
        assignedHumanTaskInstance.setAssigneeId(ASSIGNED_USER);

        doThrow(SActivityInstanceNotFoundException.class).when(activityInstanceService).getHumanTaskInstance(anyLong());
        doReturn(assignedHumanTaskInstance).when(activityInstanceService).getHumanTaskInstance(ASSIGNED_TASK);
        doReturn(humanTaskInstance).when(activityInstanceService).getHumanTaskInstance(EXISTING_TASK);
        doReturn(true).when(activityInstanceService).isTaskPendingForUser(EXISTING_TASK, USER_PENDING);
    }

    @Test
    public final void should_isInvolvedInHumanTaskInstance_return_false_if_user_do_not_exists() throws Exception {
        // When
        boolean involvedInHumanTaskInstance = taskInvolvementDelegate.isInvolvedInHumanTaskInstance(-2l, EXISTING_TASK);

        //then
        assertThat(involvedInHumanTaskInstance).as("Is involved in human task").isFalse();
    }

    @Test
    public final void should_isInvolvedInHumanTaskInstance_return_true_if_user_assigned() throws Exception {
        // When
        boolean involvedInHumanTaskInstance = taskInvolvementDelegate.isInvolvedInHumanTaskInstance(ASSIGNED_USER, ASSIGNED_TASK);

        //then
        assertThat(involvedInHumanTaskInstance).as("Is involved in human task").isTrue();

    }

    @Test
    public final void should_isInvolvedInHumanTaskInstance_return_true_if_user_in_actors() throws Exception {
        //given
        // When
        boolean involvedInHumanTaskInstance = taskInvolvementDelegate.isInvolvedInHumanTaskInstance(USER_PENDING, EXISTING_TASK);

        //then
        assertThat(involvedInHumanTaskInstance).as("Is involved in human task").isTrue();
    }

    @Test
    public final void should_isInvolvedInHumanTaskInstance_return_false_if_user_not_the_assignee() throws Exception {
        // When
        boolean involvedInHumanTaskInstance = taskInvolvementDelegate.isInvolvedInHumanTaskInstance(USER_PENDING, ASSIGNED_TASK);

        //then
        assertThat(involvedInHumanTaskInstance).as("Is involved in human task").isFalse();
    }

    @Test
    public final void should_isInvolvedInHumanTaskInstance_throw_exception_if_task_do_not_exists() throws Exception {
        expectedException.expect(ActivityInstanceNotFoundException.class);
        // When
        taskInvolvementDelegate.isInvolvedInHumanTaskInstance(ASSIGNED_USER, 45621l);
    }

    @Test
    public final void should_hasUserPendingOrAssignedTasks_return_true_when_user_has_tasks() throws Exception {
        //given
        doReturn(5L).when(activityInstanceService).getNumberOfPendingOrAssignedTasks(eq(ASSIGNED_USER), any(QueryOptions.class));

        // When
        final boolean involvedInHumanTaskInstance = taskInvolvementDelegate.hasUserPendingOrAssignedTasks(ASSIGNED_USER, 45621L);

        //then
        assertThat(involvedInHumanTaskInstance).as("should return true").isTrue();
    }

    @Test
    public final void should_hasUserPendingOrAssignedTasks_return_false_when_user_has_no_tasks() throws Exception {
        //given
        doReturn(0L).when(activityInstanceService).getNumberOfPendingOrAssignedTasks(eq(ASSIGNED_USER), any(QueryOptions.class));

        // When
        final boolean involvedInHumanTaskInstance = taskInvolvementDelegate.hasUserPendingOrAssignedTasks(ASSIGNED_USER, 45621L);

        //then
        assertThat(involvedInHumanTaskInstance).as("should return false").isFalse();
    }

    @Test
    public final void should_hasUserPendingOrAssignedTasks_fail_when_read_exception() throws Exception {
        //given
        doThrow(SBonitaReadException.class).when(activityInstanceService).getNumberOfPendingOrAssignedTasks(eq(ASSIGNED_USER), any(QueryOptions.class));

        //expect
        expectedException.expect(SExecutionException.class);

        // When
        taskInvolvementDelegate.hasUserPendingOrAssignedTasks(ASSIGNED_USER, 45621L);

    }
}
