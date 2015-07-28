/*
 * Copyright (C) 2015 Bonitasoft S.A.
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
 */
package org.bonitasoft.engine.bpm.process.impl.internal;

import org.bonitasoft.engine.bpm.businessdata.impl.BusinessDataDefinitionImpl;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.impl.ConnectorDefinitionImpl;
import org.bonitasoft.engine.bpm.context.ContextEntryImpl;
import org.bonitasoft.engine.bpm.data.impl.DataDefinitionImpl;
import org.bonitasoft.engine.bpm.document.impl.DocumentDefinitionImpl;
import org.bonitasoft.engine.bpm.document.impl.DocumentListDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.flownode.impl.internal.AutomaticTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.BoundaryEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.CallActivityDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.CatchMessageEventTriggerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.EndEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.GatewayDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.IntermediateCatchEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.IntermediateThrowEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.MultiInstanceLoopCharacteristicsImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ReceiveTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.SendTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.StandardLoopCharacteristicsImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.StartEventDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.ThrowMessageEventTriggerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.TimerEventTriggerDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.TransitionDefinitionImpl;
import org.bonitasoft.engine.bpm.flownode.impl.internal.UserTaskDefinitionImpl;
import org.bonitasoft.engine.bpm.userfilter.impl.UserFilterDefinitionImpl;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;
import org.bonitasoft.engine.operation.impl.OperationImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
/**
 * author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class ExpressionFinderTest {

    private ExpressionFinder expressionFinder = spy(new ExpressionFinder());

    @Test
    public void findShouldRecurseTreeForSeekedExpression() throws Exception {
        DesignProcessDefinitionImpl def = new DesignProcessDefinitionImpl("name", "version");
        final ContextEntryImpl contextEntry = new ContextEntryImpl();
        contextEntry.setExpression(new ExpressionImpl());
        def.addContextEntry(contextEntry);
        final ExpressionImpl stringIndex = new ExpressionImpl();
        def.setStringIndex(1, "label1", stringIndex);
        final FlowElementContainerDefinitionImpl processContainer = new FlowElementContainerDefinitionImpl();
        def.setProcessContainer(processContainer);
        final UserTaskDefinitionImpl userTask = new UserTaskDefinitionImpl("task", "actor");
        final ContextEntryImpl taskContextEntry = new ContextEntryImpl();
        userTask.addContextEntry(taskContextEntry);
        final ExpressionImpl userTaskDisplayDesc = new ExpressionImpl(1L);
        userTask.setDisplayDescription(userTaskDisplayDesc);
        final ExpressionImpl displayDescriptionAfterCompletion = new ExpressionImpl(22L);
        userTask.setDisplayDescriptionAfterCompletion(displayDescriptionAfterCompletion);
        final TransitionDefinitionImpl userTaskDefaultTransition = new TransitionDefinitionImpl("default");
        final ExpressionImpl transitionCondition = new ExpressionImpl(333L);
        userTaskDefaultTransition.setCondition(transitionCondition);
        userTask.setDefaultTransition(userTaskDefaultTransition);
        userTask.addIncomingTransition(new TransitionDefinitionImpl("incommingTransition"));
        userTask.addOutgoingTransition(new TransitionDefinitionImpl("outgoingTransition"));
        userTask.addConnector(new ConnectorDefinitionImpl("connectorOnTask", "connDefId_", "conneVersion_", ConnectorEvent.ON_FINISH));
        userTask.addDataDefinition(new DataDefinitionImpl("myTaskData", new ExpressionImpl()));
        userTask.addBusinessDataDefinition(new BusinessDataDefinitionImpl("myBizData", new ExpressionImpl()));
        userTask.addBoundaryEventDefinition(new BoundaryEventDefinitionImpl("myBoundaryEvent"));
        final UserFilterDefinitionImpl userFilterDefinition = new UserFilterDefinitionImpl("myUserFiler", "someId", "someVersion");
        userFilterDefinition.addInput("input_name", new ExpressionImpl());
        userTask.setUserFilter(userFilterDefinition);
        userTask.setLoopCharacteristics(new MultiInstanceLoopCharacteristicsImpl(true, new ExpressionImpl()));
        processContainer.addActivity(userTask);
        final ExpressionImpl bizDataDefaultValue = new ExpressionImpl();
        processContainer.addBusinessDataDefinition(new BusinessDataDefinitionImpl("bizData", bizDataDefaultValue));
        final ExpressionImpl dataDefaultValue = new ExpressionImpl();
        processContainer.addDataDefinition(new DataDefinitionImpl("data", dataDefaultValue));
        final ConnectorDefinitionImpl connectorDefinition = new ConnectorDefinitionImpl("connector", "connDefId", "conneVersion", ConnectorEvent.ON_ENTER);
        final ExpressionImpl connectorInput = new ExpressionImpl();
        connectorDefinition.addInput("input", connectorInput);
        final OperationImpl operation = new OperationImpl();
        final ExpressionImpl connectorOperationRightOperand = new ExpressionImpl();
        operation.setRightOperand(connectorOperationRightOperand);
        connectorDefinition.addOutput(operation);
        processContainer.addConnector(connectorDefinition);
        processContainer.addTransition(new TransitionDefinitionImpl("transition"));
        processContainer.addGateway(new GatewayDefinitionImpl("myGate", GatewayType.EXCLUSIVE));
        processContainer.addEndEvent(new EndEventDefinitionImpl("end_process"));
        processContainer.addDocumentDefinition(new DocumentDefinitionImpl("myDoc"));
        processContainer.addDocumentListDefinition(new DocumentListDefinitionImpl("myDocList"));
        processContainer.addActivity(new SendTaskDefinitionImpl("sendTask", "messageName", new ExpressionImpl()));
        final CatchMessageEventTriggerDefinitionImpl receiveMessage = new CatchMessageEventTriggerDefinitionImpl("receiveMessage");
        receiveMessage.addCorrelation(new ExpressionImpl(), new ExpressionImpl());
        final OperationImpl msgOperation = new OperationImpl();
        msgOperation.setRightOperand(new ExpressionImpl());
        receiveMessage.addOperation(msgOperation);
        processContainer.addActivity(new ReceiveTaskDefinitionImpl(200L, "receiveTask", receiveMessage));
        processContainer.addActivity(new SubProcessDefinitionImpl("subProcess", false));
        final CallActivityDefinitionImpl calledProcess = new CallActivityDefinitionImpl("calledProcess");
        calledProcess.setLoopCharacteristics(new StandardLoopCharacteristicsImpl(new ExpressionImpl(), false));
        calledProcess.addProcessStartContractInput("contractInputName", new ExpressionImpl());
        final BoundaryEventDefinitionImpl boundary = new BoundaryEventDefinitionImpl("boundary");
        boundary.addTimerEventTrigger(new TimerEventTriggerDefinitionImpl(TimerType.CYCLE, new ExpressionImpl()));
        calledProcess.addBoundaryEventDefinition(boundary);
        processContainer.addActivity(calledProcess);
        final ThrowMessageEventTriggerDefinitionImpl message = new ThrowMessageEventTriggerDefinitionImpl("message");
        message.addDataDefinition(new DataDefinitionImpl("dataMsgName", new ExpressionImpl()));
        processContainer.addActivity(new SendTaskDefinitionImpl(451L, "sendTask", message));
        final AutomaticTaskDefinitionImpl autoTask = new AutomaticTaskDefinitionImpl("autoTask");
        final IntermediateThrowEventDefinitionImpl intermediateThrow = new IntermediateThrowEventDefinitionImpl("intermediateThrow");
        intermediateThrow.addMessageEventTriggerDefinition(new ThrowMessageEventTriggerDefinitionImpl("someMessage"));
        processContainer.addIntermediateThrowEvent(intermediateThrow);
        processContainer.addActivity(autoTask);

        final StartEventDefinitionImpl startEvent = new StartEventDefinitionImpl("start");
        startEvent.setDisplayName(new ExpressionImpl());
        processContainer.addStartEvent(startEvent);

        final IntermediateCatchEventDefinitionImpl intermediate_catch = new IntermediateCatchEventDefinitionImpl("intermediate_catch");
        intermediate_catch.addMessageEventTrigger(new CatchMessageEventTriggerDefinitionImpl("blabla"));
        processContainer.addIntermediateCatchEvent(intermediate_catch);

        final IntermediateThrowEventDefinitionImpl intermediate_throw = new IntermediateThrowEventDefinitionImpl("intermediate_throw");
        processContainer.addIntermediateThrowEvent(intermediate_throw);

        expressionFinder.find(def, 999L);

        verify(expressionFinder).find(contextEntry, 999L);
        verify(expressionFinder).find(stringIndex, 999L);
        verify(expressionFinder).find(userTask, 999L);
        verify(expressionFinder).find(taskContextEntry, 999L);
        verify(expressionFinder).find(bizDataDefaultValue, 999L);
        verify(expressionFinder).find(dataDefaultValue, 999L);
        verify(expressionFinder).find(connectorDefinition, 999L);
        verify(expressionFinder).find(connectorInput, 999L);
        verify(expressionFinder).find(msgOperation, 999L);
        verify(expressionFinder).find(connectorOperationRightOperand, 999L);
        verify(expressionFinder).findExpressionFromNotNullContainer(startEvent, 999L);
        verify(expressionFinder).findExpressionFromNotNullContainer(intermediate_catch, 999L);
        verify(expressionFinder).findExpressionFromNotNullContainer(intermediate_throw, 999L);
        verify(expressionFinder).find(userTaskDisplayDesc, 999L);
        verify(expressionFinder).find(displayDescriptionAfterCompletion, 999L);
        verify(expressionFinder).find(transitionCondition, 999L);
    }

    @Test
    public void findShouldFindElementIfExists() throws Exception {
        DesignProcessDefinitionImpl def = new DesignProcessDefinitionImpl("name", "version");
        final ContextEntryImpl contextEntry = new ContextEntryImpl();
        long expressionFinderId = 6987451354L;
        final ExpressionImpl expressionToBeFound = new ExpressionImpl(expressionFinderId);
        contextEntry.setExpression(expressionToBeFound);
        def.addContextEntry(contextEntry);

        expressionFinder.find(def, expressionFinderId);
        final Expression expression = expressionFinder.getFoundExpression();
        assertThat(expression).isEqualTo(expressionToBeFound);

        verify(expressionFinder).find(contextEntry, expressionFinderId);
        verify(expressionFinder).find(expressionToBeFound, expressionFinderId);
    }

    @Test
    public void findShouldStopSearchingWhenFound() throws Exception {
        DesignProcessDefinitionImpl def = new DesignProcessDefinitionImpl("name", "version");
        final ContextEntryImpl contextEntry = new ContextEntryImpl();
        long expressionFinderId = 6987451354L;
        final ExpressionImpl expressionToBeFound = new ExpressionImpl(expressionFinderId);
        contextEntry.setExpression(expressionToBeFound);
        def.addContextEntry(contextEntry);
        final FlowElementContainerDefinitionImpl processContainer = new FlowElementContainerDefinitionImpl();
        def.setProcessContainer(processContainer);
        final ExpressionImpl userTaskDisplayDesc = new ExpressionImpl(1L);
        final UserTaskDefinitionImpl userTask = new UserTaskDefinitionImpl("task", "actor");
        userTask.setDisplayDescription(userTaskDisplayDesc);
        processContainer.addActivity(userTask);

        expressionFinder.find(def, expressionFinderId);
        final Expression expression = expressionFinder.getFoundExpression();
        assertThat(expression).isEqualTo(expressionToBeFound);

        verify(expressionFinder).find(contextEntry, expressionFinderId);
        verify(expressionFinder, times(0)).find(userTaskDisplayDesc, expressionFinderId);
    }
}
