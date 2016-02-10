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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.connector.EngineExecutionContext;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SAutomaticTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Elias Ricken de Medeiros
 */
public class EngineConstantExpressionExecutorStrategyTest {

    private final long containerId = 1L;

    private Map<String, Object> defaultDependencyValues;

    @Mock
    private ActivityInstanceService activityInstanceService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        defaultDependencyValues = new HashMap<String, Object>(2);
        defaultDependencyValues.put(SExpressionContext.CONTAINER_ID_KEY, containerId);
        defaultDependencyValues.put(SExpressionContext.CONTAINER_TYPE_KEY, DataInstanceContainer.ACTIVITY_INSTANCE.name());
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
        final SExpression expression = mock(SExpression.class);

        when(activityInstanceService.getFlowNodeInstance(containerId)).thenReturn(humanTaskInstance);
        when(expression.getContent()).thenReturn(ExpressionConstants.TASK_ASSIGNEE_ID.getEngineConstantName());

        final long taskAssigneeId = 10L;
        when(humanTaskInstance.getAssigneeId()).thenReturn(taskAssigneeId);
        when(humanTaskInstance.getType()).thenReturn(flowNodeType);

        final EngineConstantExpressionExecutorStrategy strategy = new EngineConstantExpressionExecutorStrategy(activityInstanceService, null, null, null);
        final long evaluatedTaskAssigneeId = (Long) strategy.evaluate(expression, defaultDependencyValues, Collections.<Integer, Object> emptyMap(),
                ContainerState.ACTIVE);
        assertEquals(taskAssigneeId, evaluatedTaskAssigneeId);
    }

    @Test
    public void taskAssigneeAutomaticTask() throws Exception {
        final SExpression expression = mock(SExpression.class);
        final SAutomaticTaskInstance taskInstance = mock(SAutomaticTaskInstance.class);

        when(activityInstanceService.getFlowNodeInstance(containerId)).thenReturn(taskInstance);
        when(expression.getContent()).thenReturn(ExpressionConstants.TASK_ASSIGNEE_ID.getEngineConstantName());

        final EngineConstantExpressionExecutorStrategy strategy = new EngineConstantExpressionExecutorStrategy(activityInstanceService, null, null, null);
        final long evaluatedTaskAssigneeId = (Long) strategy.evaluate(expression, defaultDependencyValues, Collections.<Integer, Object> emptyMap(),
                ContainerState.ACTIVE);
        assertEquals(-1L, evaluatedTaskAssigneeId);

    }

    @Test
    public void taskAssigneeProcessInstance() throws Exception {
        final SExpression expression = mock(SExpression.class);
        final Map<String, Object> dependencyValues = new HashMap<String, Object>(defaultDependencyValues);
        dependencyValues.put(SExpressionContext.CONTAINER_TYPE_KEY, DataInstanceContainer.PROCESS_INSTANCE.name());

        when(expression.getContent()).thenReturn(ExpressionConstants.TASK_ASSIGNEE_ID.getEngineConstantName());

        final EngineConstantExpressionExecutorStrategy strategy = new EngineConstantExpressionExecutorStrategy(activityInstanceService, null, null, null);
        final int evaluatedTaskAssigneeId = (Integer) strategy.evaluate(expression, dependencyValues, Collections.<Integer, Object> emptyMap(),
                ContainerState.ACTIVE);
        assertEquals(-1L, evaluatedTaskAssigneeId);

    }

    @Test
    public void taskAssigneeIdInEngineExecutionContext() throws Exception {
        final SUserTaskInstance taskInstance = mock(SUserTaskInstance.class);
        final SExpression expression = mock(SExpression.class);

        when(activityInstanceService.getActivityInstance(containerId)).thenReturn(taskInstance);
        when(expression.getContent()).thenReturn(ExpressionConstants.ENGINE_EXECUTION_CONTEXT.getEngineConstantName());

        final long taskAssigneeId = 10L;
        when(taskInstance.getAssigneeId()).thenReturn(taskAssigneeId);
        when(taskInstance.getType()).thenReturn(SFlowNodeType.USER_TASK);

        final EngineConstantExpressionExecutorStrategy strategy = new EngineConstantExpressionExecutorStrategy(activityInstanceService, null, null, null);
        final EngineExecutionContext engineExecutionContext = (EngineExecutionContext) strategy.evaluate(expression, defaultDependencyValues,
                Collections.<Integer, Object> emptyMap(), ContainerState.ACTIVE);
        assertEquals(taskAssigneeId, engineExecutionContext.getTaskAssigneeId());

    }

    @Test
    public void taskAssigneeIdInEngineExecutionContextAlreadyEvaluated() throws Exception {
        final SExpression expression = mock(SExpression.class);

        when(expression.getContent()).thenReturn(ExpressionConstants.TASK_ASSIGNEE_ID.getEngineConstantName());

        final Long taskAssigneeId = 10L;
        final EngineExecutionContext engineExecutionContext = new EngineExecutionContext();
        engineExecutionContext.setTaskAssigneeId(taskAssigneeId);
        final Map<String, Object> dependencies = new HashMap<String, Object>(defaultDependencyValues);
        dependencies.put(ExpressionConstants.ENGINE_EXECUTION_CONTEXT.getEngineConstantName(), engineExecutionContext);

        final EngineConstantExpressionExecutorStrategy strategy = new EngineConstantExpressionExecutorStrategy(activityInstanceService, null, null, null);
        final Long evaluatedTaskAssigneeId = (Long) strategy
                .evaluate(expression, dependencies, Collections.<Integer, Object> emptyMap(), ContainerState.ACTIVE);
        assertEquals(taskAssigneeId, evaluatedTaskAssigneeId);

        verify(activityInstanceService, never()).getActivityInstance(anyLong());

    }

    @Test
    public void processInstanceIdInEngineExecutionContextIsFound() throws Exception {
        final SExpression expression = mock(SExpression.class);

        when(expression.getContent()).thenReturn(ExpressionConstants.PROCESS_INSTANCE_ID.getEngineConstantName());

        final long processInstanceId = 187555L;
        final EngineExecutionContext engineExecutionContext = new EngineExecutionContext();
        engineExecutionContext.setProcessInstanceId(processInstanceId);
        final Map<String, Object> dependencies = new HashMap<String, Object>(defaultDependencyValues);
        dependencies.put(ExpressionConstants.ENGINE_EXECUTION_CONTEXT.getEngineConstantName(), engineExecutionContext);

        final EngineConstantExpressionExecutorStrategy strategy = new EngineConstantExpressionExecutorStrategy(null, null, null, null);
        final Long evaluatedProcessInstanceId = (Long) strategy.evaluate(expression, dependencies, Collections.<Integer, Object> emptyMap(),
                ContainerState.ACTIVE);
        assertEquals(processInstanceId, evaluatedProcessInstanceId.longValue());

    }

    @Test
    public void processInstanceIdNotFoundInEngineExecutionContext() throws Exception {
        final SExpression expression = mock(SExpression.class);

        when(expression.getContent()).thenReturn(ExpressionConstants.PROCESS_INSTANCE_ID.getEngineConstantName());

        final long processInstanceId = 799451L;
        final Map<String, Object> dependencies = new HashMap<String, Object>(0);

        final EngineConstantExpressionExecutorStrategy strategy = new EngineConstantExpressionExecutorStrategy(null, null, null, null);
        final Serializable noValue = strategy.evaluate(expression, dependencies, Collections.<Integer, Object> emptyMap(), ContainerState.ACTIVE);
        assertFalse(("" + processInstanceId).equals(String.valueOf(noValue)));

    }

    @Test(expected = SExpressionEvaluationException.class)
    public void engineConstantNotFound() throws Exception {
        final SExpression expression = mock(SExpression.class);

        when(expression.getContent()).thenReturn("unexisting_constant_value");

        final EngineConstantExpressionExecutorStrategy strategy = new EngineConstantExpressionExecutorStrategy(null, null, null, null);
        strategy.evaluate(expression, defaultDependencyValues, Collections.<Integer, Object> emptyMap(), ContainerState.ACTIVE);
    }

    @Test(expected = SInvalidExpressionException.class)
    public void engineConstantWithValidationException() throws Exception {
        final SExpression expression = mock(SExpression.class);

        when(expression.getContent()).thenReturn("unexisting_constant_value");

        final EngineConstantExpressionExecutorStrategy strategy = new EngineConstantExpressionExecutorStrategy(null, null, null, null);
        strategy.validate(expression);
    }

    @Test
    public void fillDependenciesOnArchivedFlowNodeDoesNotThrowException() throws Exception {
        final SAActivityInstance saai = mock(SAActivityInstance.class);

        when(activityInstanceService.getMostRecentArchivedActivityInstance(anyLong())).thenReturn(saai);
        when(saai.getType()).thenReturn(SFlowNodeType.AUTOMATIC_TASK);
        long arbitraryValue = 123456L;
        when(saai.getLogicalGroup(anyInt())).thenReturn(arbitraryValue);

        final Map<String, Object> dependencies = new HashMap<String, Object>(defaultDependencyValues);
        dependencies.put("time", System.currentTimeMillis());

        final EngineConstantExpressionExecutorStrategy strategy = new EngineConstantExpressionExecutorStrategy(activityInstanceService, null, null, null);
        strategy.fillDependenciesFromFlowNodeInstance(dependencies, 54);
        assertEquals(arbitraryValue, dependencies.get(ExpressionConstants.PROCESS_INSTANCE_ID.getEngineConstantName()));
        assertEquals(arbitraryValue, dependencies.get(ExpressionConstants.ROOT_PROCESS_INSTANCE_ID.getEngineConstantName()));
    }
}
