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
package org.bonitasoft.engine.expression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.expression.ExpressionConstants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Elias Ricken de Medeiros
 */
@RunWith(MockitoJUnitRunner.class)
public class EngineConstantExpressionExecutorStrategyTest {

    private static final long CONTAINER_ID = 156L;
    private static final long USER_ID = 219047231L;

    private Map<String, Object> dependencies;

    @Mock
    private ActivityInstanceService activityInstanceService;
    @Mock
    private SessionService sessionService;
    @Mock
    private ProcessInstanceService ProcessInstanceService;
    @Mock
    private SessionAccessor sessionAccessor;
    private SUserTaskInstanceImpl taskInstance;
    @InjectMocks
    private EngineConstantExpressionExecutorStrategy strategy;
    private SExpressionImpl expression = new SExpressionImpl();
    public static final long PARENT_PROCESS_INSTANCE_ID = 123456L;

    @Before
    public void setUp() throws SFlowNodeReadException, SFlowNodeNotFoundException {
        dependencies = new HashMap<>(2);
        dependencies.put(SExpressionContext.CONTAINER_ID_KEY, CONTAINER_ID);
        dependencies.put(SExpressionContext.CONTAINER_TYPE_KEY, DataInstanceContainer.ACTIVITY_INSTANCE.name());
        taskInstance = new SUserTaskInstanceImpl();
        when(activityInstanceService.getFlowNodeInstance(CONTAINER_ID)).thenReturn(taskInstance);
        taskInstance.setId(CONTAINER_ID);
        taskInstance.setParentContainerId(PARENT_PROCESS_INSTANCE_ID);
        taskInstance.setParentContainerId(PARENT_PROCESS_INSTANCE_ID);
    }

    @Test
    public void taskAssigneeUserTask() throws Exception {
        taskAssigneeOnHumanTask(SFlowNodeType.USER_TASK, mock(SUserTaskInstance.class));

    }

    @Test
    public void taskAssigneeManualTask() throws Exception {
        taskAssigneeOnHumanTask(SFlowNodeType.MANUAL_TASK, mock(SManualTaskInstance.class));

    }

    private void taskAssigneeOnHumanTask(final SFlowNodeType flowNodeType, final SHumanTaskInstance humanTaskInstance) throws SExpressionEvaluationException,
            SFlowNodeNotFoundException, SFlowNodeReadException {
        when(activityInstanceService.getFlowNodeInstance(CONTAINER_ID)).thenReturn(humanTaskInstance);
        expression.setContent(TASK_ASSIGNEE_ID.getEngineConstantName());

        final long taskAssigneeId = 10L;
        when(humanTaskInstance.getAssigneeId()).thenReturn(taskAssigneeId);
        when(humanTaskInstance.getType()).thenReturn(flowNodeType);

        final long evaluatedTaskAssigneeId = (Long) strategy.evaluate(expression, dependencies, Collections.<Integer, Object>emptyMap(),
                ContainerState.ACTIVE);
        assertEquals(taskAssigneeId, evaluatedTaskAssigneeId);
    }

    @Test
    public void taskAssigneeAutomaticTask() throws Exception {

        when(activityInstanceService.getFlowNodeInstance(CONTAINER_ID)).thenReturn(mock(SAutomaticTaskInstance.class));
        expression.setContent(TASK_ASSIGNEE_ID.getEngineConstantName());

        final long evaluatedTaskAssigneeId = (Long) strategy.evaluate(expression, dependencies, Collections.<Integer, Object>emptyMap(),
                ContainerState.ACTIVE);
        assertEquals(-1L, evaluatedTaskAssigneeId);

    }

    @Test
    public void taskAssigneeProcessInstance() throws Exception {
        dependencies.put(SExpressionContext.CONTAINER_TYPE_KEY, DataInstanceContainer.PROCESS_INSTANCE.name());
        expression.setContent(TASK_ASSIGNEE_ID.getEngineConstantName());

        final Serializable evaluatedTaskAssigneeId = strategy.evaluate(expression, dependencies, Collections.<Integer, Object>emptyMap(),
                ContainerState.ACTIVE);
        assertEquals(-1L, evaluatedTaskAssigneeId);

    }

    @Test
    public void taskAssigneeIdInEngineExecutionContext() throws Exception {
        final SUserTaskInstance taskInstance = mock(SUserTaskInstance.class);

        when(activityInstanceService.getActivityInstance(CONTAINER_ID)).thenReturn(taskInstance);
        expression.setContent(ENGINE_EXECUTION_CONTEXT.getEngineConstantName());

        final long taskAssigneeId = 10L;
        when(taskInstance.getAssigneeId()).thenReturn(taskAssigneeId);
        when(taskInstance.getType()).thenReturn(SFlowNodeType.USER_TASK);

        final EngineExecutionContext engineExecutionContext = (EngineExecutionContext) strategy.evaluate(expression, dependencies,
                Collections.<Integer, Object>emptyMap(), ContainerState.ACTIVE);
        assertEquals(taskAssigneeId, engineExecutionContext.getTaskAssigneeId());

    }

    @Test
    public void taskAssigneeIdInEngineExecutionContextAlreadyEvaluated() throws Exception {
        expression.setContent(TASK_ASSIGNEE_ID.getEngineConstantName());

        final Long taskAssigneeId = 10L;
        final EngineExecutionContext engineExecutionContext = new EngineExecutionContext();
        engineExecutionContext.setTaskAssigneeId(taskAssigneeId);
        dependencies.put(ENGINE_EXECUTION_CONTEXT.getEngineConstantName(), engineExecutionContext);

        final Long evaluatedTaskAssigneeId = (Long) strategy
                .evaluate(expression, dependencies, Collections.<Integer, Object>emptyMap(), ContainerState.ACTIVE);
        assertEquals(taskAssigneeId, evaluatedTaskAssigneeId);

        verify(activityInstanceService, never()).getActivityInstance(anyLong());

    }

    @Test
    public void processInstanceIdInEngineExecutionContextIsFound() throws Exception {
        expression.setContent(PROCESS_INSTANCE_ID.getEngineConstantName());

        final long processInstanceId = 187555L;
        final EngineExecutionContext engineExecutionContext = new EngineExecutionContext();
        engineExecutionContext.setProcessInstanceId(processInstanceId);
        dependencies.put(ENGINE_EXECUTION_CONTEXT.getEngineConstantName(), engineExecutionContext);

        final Long evaluatedProcessInstanceId = (Long) strategy.evaluate(expression, dependencies, Collections.<Integer, Object>emptyMap(),
                ContainerState.ACTIVE);
        assertEquals(processInstanceId, evaluatedProcessInstanceId.longValue());

    }

    @Test
    public void processInstanceIdNotFoundInEngineExecutionContext() throws Exception {
        expression.setContent(PROCESS_INSTANCE_ID.getEngineConstantName());

        final long processInstanceId = 799451L;

        final Serializable noValue = strategy.evaluate(expression, new HashMap<String, Object>(0), Collections.<Integer, Object>emptyMap(), ContainerState.ACTIVE);
        assertFalse(("" + processInstanceId).equals(String.valueOf(noValue)));

    }

    @Test(expected = SExpressionEvaluationException.class)
    public void engineConstantNotFound() throws Exception {
        expression.setContent("unexisting_constant_value");

        strategy.evaluate(expression, dependencies, Collections.<Integer, Object>emptyMap(), ContainerState.ACTIVE);
    }

    @Test(expected = SInvalidExpressionException.class)
    public void engineConstantWithValidationException() throws Exception {
        expression.setContent("unexisting_constant_value");

        strategy.validate(expression);
    }

    @Test
    public void fillDependenciesOnArchivedFlowNodeDoesNotThrowException() throws Exception {
        final SAActivityInstance activityInstance = mock(SAActivityInstance.class);
        when(activityInstanceService.getMostRecentArchivedActivityInstance(anyLong())).thenReturn(activityInstance);
        when(activityInstance.getLogicalGroup(anyInt())).thenReturn(PARENT_PROCESS_INSTANCE_ID);
        dependencies.put("time", System.currentTimeMillis());

        strategy.fillDependenciesFromFlowNodeInstance(dependencies, 54);
        assertEquals(PARENT_PROCESS_INSTANCE_ID, dependencies.get(PROCESS_INSTANCE_ID.getEngineConstantName()));
        assertEquals(PARENT_PROCESS_INSTANCE_ID, dependencies.get(ROOT_PROCESS_INSTANCE_ID.getEngineConstantName()));
    }

    @Test
    public void should_evaluate_LOGGED_USER_ID_return_user_id_from_session() throws Exception {
        expression.setContent(LOGGED_USER_ID.getEngineConstantName());
        doReturn(USER_ID).when(sessionService).getLoggedUserFromSession(sessionAccessor);


        Serializable loggerUserId = strategy.evaluate(expression, dependencies, Collections.<Integer, Object>emptyMap(),
                ContainerState.ACTIVE);
        assertThat(loggerUserId).as("logged user id").isEqualTo(USER_ID);
    }

    @Test
    public void should_evaluate_LOGGED_USER_ID_return_user_id_from_task_assignee() throws Exception {
        //given
        expression.setContent(LOGGED_USER_ID.getEngineConstantName());
        taskInstance.setAssigneeId(USER_ID);
        //when
        Serializable loggerUserId = strategy.evaluate(expression, dependencies, Collections.<Integer, Object>emptyMap(),
                ContainerState.ACTIVE);
        //then
        assertThat(loggerUserId).as("logged user id").isEqualTo(USER_ID);
    }

    @Test
    public void should_evaluate_LOGGED_USER_ID_return_minus1_when_exception() throws Exception {
        //given
        expression.setContent(LOGGED_USER_ID.getEngineConstantName());
        doThrow(SFlowNodeReadException.class).when(activityInstanceService).getFlowNodeInstance(anyLong());
        //when
        Serializable loggerUserId = strategy.evaluate(expression, dependencies, Collections.emptyMap(),
                ContainerState.ACTIVE);
        //then
        assertThat(loggerUserId).as("logged user id").isEqualTo(-1L);
    }

    @Test
    public void should_evaluate_LOGGED_USER_ID_return_what_is_in_context() throws Exception {
        //given
        expression.setContent(LOGGED_USER_ID.getEngineConstantName());
        dependencies.put(TASK_ASSIGNEE_ID.getEngineConstantName(), USER_ID);
        //when
        Serializable loggerUserId = strategy.evaluate(expression, dependencies, Collections.<Integer, Object>emptyMap(),
                ContainerState.ACTIVE);
        //then
        assertThat(loggerUserId).as("logged user id").isEqualTo(USER_ID);
    }

}
