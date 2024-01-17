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
package org.bonitasoft.engine.execution.state;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SCallActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SCallActivityDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.model.SCallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.StateBehaviors;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class InitializingActivityWithBoundaryEventsStateTest {

    @Mock
    private StateBehaviors stateBehaviors;
    @Mock
    private ExpressionResolverService expressionResolverService;
    @Mock
    private ProcessExecutor processExecutor;
    @Mock
    private ActivityInstanceService activityInstanceService;
    @Mock
    private ProcessDefinitionService processDefinitionService;
    @Mock
    private WorkService workService;
    @Mock
    private BPMWorkFactory workFactory;
    @InjectMocks
    InitializingActivityWithBoundaryEventsState initializingActivityWithBoundaryEventsState;

    @Before
    public void before() {
        initializingActivityWithBoundaryEventsState.setProcessExecutor(processExecutor);
    }

    @Test(expected = SActivityStateExecutionException.class)
    public void should_throw_SActivityStateExecutionException_when_callDefinition_not_exist() throws Exception {
        final long callerId = 666L;

        SProcessDefinition sProcessDefinition = mock(SProcessDefinitionImpl.class);
        SFlowElementContainerDefinition processContainer = mock(SFlowElementContainerDefinition.class);
        SCallActivityInstance sFlowNodeInstance = new SCallActivityInstance();
        sFlowNodeInstance.setId(666L);
        sFlowNodeInstance.setFlowNodeDefinitionId(555L);
        sFlowNodeInstance.setStateId(ProcessInstanceState.INITIALIZING.getId());

        when(sProcessDefinition.getProcessContainer()).thenReturn(processContainer);
        doReturn(null).when(processContainer).getFlowNode(555L);

        //when
        initializingActivityWithBoundaryEventsState.afterConnectors(sProcessDefinition, sFlowNodeInstance);

        //then
        verify(stateBehaviors).updateDisplayNameAndDescription(sProcessDefinition, sFlowNodeInstance);
        verify(stateBehaviors).updateExpectedDuration(sProcessDefinition, sFlowNodeInstance);
        verify(stateBehaviors).addAssignmentSystemCommentIfTaskWasAutoAssign(sFlowNodeInstance);
        verify(stateBehaviors, never()).registerWaitingEvent(sProcessDefinition, sFlowNodeInstance);
        verify(processExecutor, never()).start(any(), any(), any(), any(), any(), any(), eq(callerId), any(), any());
    }

    @Test
    public void should_start_target_process_with_version__and_invoke_methods() throws Exception {
        final long callerId = 666L;
        final long targProcessInstanceId = 667L;
        final long targetDefinitionId = 554L;
        SProcessDefinition sProcessDefinition = mock(SProcessDefinitionImpl.class);
        SFlowElementContainerDefinition processContainer = mock(SFlowElementContainerDefinition.class);
        SCallActivityInstance sFlowNodeInstance = new SCallActivityInstance();
        sFlowNodeInstance.setId(666L);
        sFlowNodeInstance.setFlowNodeDefinitionId(555L);
        sFlowNodeInstance.setStateId(ProcessInstanceState.INITIALIZING.getId());
        SCallActivityDefinition callActivityDefinition = spy(new SCallActivityDefinitionImpl(666, "call1"));
        Map<String, SExpression> startConctractInputs = new LinkedHashMap<>();
        startConctractInputs.put("input1", new SExpressionImpl("expr1", "content1", null, null, null, null));
        startConctractInputs.put("input2", new SExpressionImpl("expr2", "content2", null, null, null, null));
        callActivityDefinition.getProcessStartContractInputs().putAll(startConctractInputs);
        SProcessInstance targetProcess = new SProcessInstance();
        targetProcess.setId(targProcessInstanceId);
        targetProcess.setStateId(ProcessInstanceState.INITIALIZING.getId());
        final SExpressionContext context = new SExpressionContext(callerId,
                DataInstanceContainer.ACTIVITY_INSTANCE.name(), callerId);
        SExpression callableElement = mock(SExpression.class);
        SExpression callableElementVersion = mock(SExpression.class);
        when(sProcessDefinition.getProcessContainer()).thenReturn(processContainer);
        doReturn(callActivityDefinition).when(processContainer).getFlowNode(555L);
        doReturn(callableElement).when(callActivityDefinition).getCallableElement();
        doReturn(callableElementVersion).when(callActivityDefinition).getCallableElementVersion();
        doReturn("target").when(expressionResolverService).evaluate(eq(callableElement), any());
        doReturn("v1").when(expressionResolverService).evaluate(eq(callableElementVersion), any());
        doReturn(Arrays.asList("value1", "value2")).when(expressionResolverService).evaluate(anyList(), any());
        Map<String, Serializable> processInputs = new LinkedHashMap<>();
        processInputs.put("input1", "value1");
        processInputs.put("input2", "value2");

        doReturn(targetDefinitionId).when(processDefinitionService).getProcessDefinitionId(eq("target"), eq("v1"));

        doReturn(targetProcess).when(processExecutor).start(eq(targetDefinitionId), eq(-1L), eq(0L), eq(0L),
                any(), any(), eq(callerId), eq(-1L), eq(processInputs));

        //when
        initializingActivityWithBoundaryEventsState.afterConnectors(sProcessDefinition, sFlowNodeInstance);

        //then
        verify(stateBehaviors).updateDisplayNameAndDescription(sProcessDefinition, sFlowNodeInstance);
        verify(stateBehaviors).updateExpectedDuration(sProcessDefinition, sFlowNodeInstance);
        verify(stateBehaviors).addAssignmentSystemCommentIfTaskWasAutoAssign(sFlowNodeInstance);
        verify(stateBehaviors).registerWaitingEvent(sProcessDefinition, sFlowNodeInstance);
    }

    @Test
    public void should_start_latest_target_process_and_invoke_methods() throws Exception {
        final long callerId = 666L;
        final long targProcessInstanceId = 667L;
        final long targetDefinitionId = 554L;
        SProcessDefinition sProcessDefinition = mock(SProcessDefinitionImpl.class);
        SFlowElementContainerDefinition processContainer = mock(SFlowElementContainerDefinition.class);
        SCallActivityInstance sFlowNodeInstance = new SCallActivityInstance();
        sFlowNodeInstance.setId(666L);
        sFlowNodeInstance.setFlowNodeDefinitionId(555L);
        sFlowNodeInstance.setStateId(ProcessInstanceState.INITIALIZING.getId());
        SCallActivityDefinition callActivityDefinition = spy(new SCallActivityDefinitionImpl(666, "call1"));
        Map<String, SExpression> startConctractInputs = new LinkedHashMap<>();
        startConctractInputs.put("input1", new SExpressionImpl("expr1", "content1", null, null, null, null));
        startConctractInputs.put("input2", new SExpressionImpl("expr2", "content2", null, null, null, null));
        callActivityDefinition.getProcessStartContractInputs().putAll(startConctractInputs);
        SProcessInstance targetProcess = new SProcessInstance();
        targetProcess.setId(targProcessInstanceId);
        targetProcess.setStateId(ProcessInstanceState.INITIALIZING.getId());
        final SExpressionContext context = new SExpressionContext(callerId,
                DataInstanceContainer.ACTIVITY_INSTANCE.name(), callerId);
        SExpression callableElement = mock(SExpression.class);

        when(sProcessDefinition.getProcessContainer()).thenReturn(processContainer);
        doReturn(callActivityDefinition).when(processContainer).getFlowNode(555L);
        doReturn(callableElement).when(callActivityDefinition).getCallableElement();
        doReturn(null).when(callActivityDefinition).getCallableElementVersion();
        doReturn("target").when(expressionResolverService).evaluate(eq(callableElement), any());
        doReturn(Arrays.asList("value1", "value2")).when(expressionResolverService).evaluate(anyList(), any());
        Map<String, Serializable> processInputs = new LinkedHashMap<>();
        processInputs.put("input1", "value1");
        processInputs.put("input2", "value2");

        doReturn(targetDefinitionId).when(processDefinitionService).getLatestProcessDefinitionId(eq("target"));

        doReturn(targetProcess).when(processExecutor).start(eq(targetDefinitionId), eq(-1L), eq(0L), eq(0L),
                any(), any(), eq(callerId), eq(-1L), eq(processInputs));

        //when
        initializingActivityWithBoundaryEventsState.afterConnectors(sProcessDefinition, sFlowNodeInstance);

        //then
        verify(stateBehaviors).updateDisplayNameAndDescription(sProcessDefinition, sFlowNodeInstance);
        verify(stateBehaviors).updateExpectedDuration(sProcessDefinition, sFlowNodeInstance);
        verify(stateBehaviors).addAssignmentSystemCommentIfTaskWasAutoAssign(sFlowNodeInstance);
        verify(stateBehaviors).registerWaitingEvent(sProcessDefinition, sFlowNodeInstance);
    }
}
