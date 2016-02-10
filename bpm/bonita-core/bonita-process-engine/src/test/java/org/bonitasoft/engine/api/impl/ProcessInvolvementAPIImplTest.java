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
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import java.util.Arrays;
import java.util.Collections;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.model.impl.SActorImpl;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.model.archive.impl.SAUserTaskInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProcessInvolvementAPIImplTest {

    private static final long EXISTING_TASK = 56789465l;
    private static final long ASSIGNED_TASK = 54789465l;
    private static final long ASSIGNED_USER = 1234595l;
    private static final long USER_PENDING = 4545666l;
    private static final long PROCESS_DEFINITION_ID = 123456789l;
    @Mock
    public ProcessAPIImpl processAPI;
    @Mock
    private TenantServiceAccessor tenantServiceAccessor;
    private SUserTaskInstanceImpl humanTaskInstance;
    private SUserTaskInstanceImpl assignedHumanTaskInstance;
    @InjectMocks
    public ProcessInvolvementAPIImpl processInvolvementAPI;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private ActivityInstanceService activityInstanceService;

    @Before
    public void before() throws SBonitaException {
        humanTaskInstance = new SUserTaskInstanceImpl();
        humanTaskInstance.setId(EXISTING_TASK);
        humanTaskInstance.setLogicalGroup(1, PROCESS_DEFINITION_ID);
        assignedHumanTaskInstance = new SUserTaskInstanceImpl();
        assignedHumanTaskInstance.setId(ASSIGNED_TASK);
        assignedHumanTaskInstance.setLogicalGroup(1, PROCESS_DEFINITION_ID);
        assignedHumanTaskInstance.setAssigneeId(ASSIGNED_USER);
        assignedHumanTaskInstance.setAssigneeId(ASSIGNED_USER);

        doReturn(tenantServiceAccessor).when(processAPI).getTenantAccessor();
        doReturn(activityInstanceService).when(tenantServiceAccessor).getActivityInstanceService();

        doThrow(SActivityInstanceNotFoundException.class).when(activityInstanceService).getHumanTaskInstance(anyLong());
        doReturn(assignedHumanTaskInstance).when(activityInstanceService).getHumanTaskInstance(ASSIGNED_TASK);
        doReturn(humanTaskInstance).when(activityInstanceService).getHumanTaskInstance(EXISTING_TASK);
        doReturn(true).when(activityInstanceService).isTaskPendingForUser(ASSIGNED_TASK, ASSIGNED_USER);
        doReturn(true).when(activityInstanceService).isTaskPendingForUser(EXISTING_TASK,USER_PENDING);
    }

    @Test
    public final void should_isInvolvedInHumanTaskInstance_return_false_if_user_do_not_exists() throws Exception {
        // When
        boolean involvedInHumanTaskInstance = processInvolvementAPI.isInvolvedInHumanTaskInstance(-2l, EXISTING_TASK);

        //then
        assertThat(involvedInHumanTaskInstance).as("Is involved in human task").isFalse();
    }

    @Test
    public final void should_isInvolvedInHumanTaskInstance_return_true_if_user_assigned() throws Exception {
        // When
        boolean involvedInHumanTaskInstance = processInvolvementAPI.isInvolvedInHumanTaskInstance(ASSIGNED_USER, ASSIGNED_TASK);

        //then
        assertThat(involvedInHumanTaskInstance).as("Is involved in human task").isTrue();
    }

    @Test
    public final void should_isInvolvedInHumanTaskInstance_return_true_if_user_in_actors() throws Exception {
        //given
        // When
        boolean involvedInHumanTaskInstance = processInvolvementAPI.isInvolvedInHumanTaskInstance(USER_PENDING, EXISTING_TASK);

        //then
        assertThat(involvedInHumanTaskInstance).as("Is involved in human task").isTrue();
    }

    @Test
    public final void should_isInvolvedInHumanTaskInstance_return_false_if_user_not_the_assignee() throws Exception {
        // When
        boolean involvedInHumanTaskInstance = processInvolvementAPI.isInvolvedInHumanTaskInstance(USER_PENDING, ASSIGNED_TASK);

        //then
        assertThat(involvedInHumanTaskInstance).as("Is involved in human task").isFalse();
    }

    @Test
    public final void should_isInvolvedInHumanTaskInstance_throw_exception_if_task_do_not_exists() throws Exception {
        expectedException.expect(ActivityInstanceNotFoundException.class);
        // When
        processInvolvementAPI.isInvolvedInHumanTaskInstance(ASSIGNED_USER, 45621l);
    }

}
