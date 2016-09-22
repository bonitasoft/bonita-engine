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

import java.util.List;

import org.bonitasoft.engine.bpm.businessdata.BusinessDataDefinition;
import org.bonitasoft.engine.bpm.connector.ConnectorDefinition;
import org.bonitasoft.engine.bpm.context.ContextEntry;
import org.bonitasoft.engine.bpm.data.DataDefinition;
import org.bonitasoft.engine.bpm.document.DocumentDefinition;
import org.bonitasoft.engine.bpm.document.DocumentListDefinition;
import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.BoundaryEventDefinition;
import org.bonitasoft.engine.bpm.flownode.CallActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.CatchEventDefinition;
import org.bonitasoft.engine.bpm.flownode.CatchMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.CorrelationDefinition;
import org.bonitasoft.engine.bpm.flownode.EndEventDefinition;
import org.bonitasoft.engine.bpm.flownode.EventDefinition;
import org.bonitasoft.engine.bpm.flownode.EventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.flownode.FlowNodeDefinition;
import org.bonitasoft.engine.bpm.flownode.GatewayDefinition;
import org.bonitasoft.engine.bpm.flownode.HumanTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.IntermediateCatchEventDefinition;
import org.bonitasoft.engine.bpm.flownode.IntermediateThrowEventDefinition;
import org.bonitasoft.engine.bpm.flownode.MessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.MultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.bpm.flownode.ReceiveTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.SendTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.StandardLoopCharacteristics;
import org.bonitasoft.engine.bpm.flownode.StartEventDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowEventDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.bpm.flownode.UserTaskDefinition;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.bpm.process.SubProcessDefinition;
import org.bonitasoft.engine.bpm.process.Visitable;
import org.bonitasoft.engine.bpm.userfilter.UserFilterDefinition;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.operation.Operation;

/**
 * author Emmanuel Duchastenier
 */
public class ExpressionFinder implements ModelFinderVisitor {

    public Expression getFoundExpression() {
        return toBeFound;
    }

    private Expression toBeFound = null;

    @Override
    public void find(Expression expression, long expressionDefinitionId) {
        if (expression.getId() == expressionDefinitionId) {
            toBeFound = expression;
        }
    }

    protected void findExpressionFromNotNullContainer(Visitable visitable, long expressionDefinitionId) {
        if (visitable != null && toBeFound == null) {
            visitable.accept(this, expressionDefinitionId);
        }
    }

    protected void getExpressionFromOperationList(List<Operation> operations, long expressionDefinitionId) {
        for (Operation operation : operations) {
            findExpressionFromNotNullContainer(operation, expressionDefinitionId);
        }
    }

    @Override
    public void find(DesignProcessDefinition designProcessDefinition, long expressionDefinitionId) {
        final List<Expression> expressions = designProcessDefinition.getStringIndexValues();
        for (Expression expression : expressions) {
            findExpressionFromNotNullContainer(expression, expressionDefinitionId);
        }
        for (ContextEntry contextEntry : designProcessDefinition.getContext()) {
            findExpressionFromNotNullContainer(contextEntry, expressionDefinitionId);
        }
        findExpressionFromNotNullContainer(designProcessDefinition.getFlowElementContainer(), expressionDefinitionId);
    }

    @Override
    public void find(FlowNodeDefinition flowNodeDefinition, long expressionDefinitionId) {
        if (flowNodeDefinition != null) {
            findExpressionFromNotNullContainer(flowNodeDefinition.getDisplayName(), expressionDefinitionId);
            findExpressionFromNotNullContainer(flowNodeDefinition.getDisplayDescription(), expressionDefinitionId);
            findExpressionFromNotNullContainer(flowNodeDefinition.getDisplayDescriptionAfterCompletion(), expressionDefinitionId);

            findExpressionFromNotNullContainer(flowNodeDefinition.getDefaultTransition(), expressionDefinitionId);

            for (TransitionDefinition transitionDefinition : flowNodeDefinition.getIncomingTransitions()) {
                findExpressionFromNotNullContainer(transitionDefinition, expressionDefinitionId);
            }
            for (TransitionDefinition transitionDefinition : flowNodeDefinition.getOutgoingTransitions()) {
                findExpressionFromNotNullContainer(transitionDefinition, expressionDefinitionId);
            }

            for (ConnectorDefinition connectorDefinition : flowNodeDefinition.getConnectors()) {
                findExpressionFromNotNullContainer(connectorDefinition, expressionDefinitionId);
            }
        }
    }

    @Override
    public void find(FlowElementContainerDefinition container, long expressionDefinitionId) {
        for (ActivityDefinition activity : container.getActivities()) {
            findExpressionFromNotNullContainer(activity, expressionDefinitionId);
        }
        for (TransitionDefinition transition : container.getTransitions()) {
            findExpressionFromNotNullContainer(transition, expressionDefinitionId);
        }
        for (GatewayDefinition gatewayDefinition : container.getGatewaysList()) {
            findExpressionFromNotNullContainer(gatewayDefinition, expressionDefinitionId);
        }
        for (StartEventDefinition startEventDefinition : container.getStartEvents()) {
            findExpressionFromNotNullContainer(startEventDefinition, expressionDefinitionId);
        }
        for (EndEventDefinition endEventDefinition : container.getEndEvents()) {
            findExpressionFromNotNullContainer(endEventDefinition, expressionDefinitionId);
        }
        for (IntermediateCatchEventDefinition catchEventDefinition : container.getIntermediateCatchEvents()) {
            findExpressionFromNotNullContainer(catchEventDefinition, expressionDefinitionId);
        }
        for (IntermediateThrowEventDefinition throwEventDefinition : container.getIntermediateThrowEvents()) {
            findExpressionFromNotNullContainer(throwEventDefinition, expressionDefinitionId);
        }
        for (DataDefinition dataDefinition : container.getDataDefinitions()) {
            findExpressionFromNotNullContainer(dataDefinition, expressionDefinitionId);
        }
        for (DocumentDefinition documentDefinition : container.getDocumentDefinitions()) {
            findExpressionFromNotNullContainer(documentDefinition, expressionDefinitionId);
        }
        for (DocumentListDefinition documentListDefinition : container.getDocumentListDefinitions()) {
            findExpressionFromNotNullContainer(documentListDefinition, expressionDefinitionId);
        }
        for (BusinessDataDefinition businessDataDefinition : container.getBusinessDataDefinitions()) {
            findExpressionFromNotNullContainer(businessDataDefinition, expressionDefinitionId);
        }
        for (ConnectorDefinition connectorDefinition : container.getConnectors()) {
            findExpressionFromNotNullContainer(connectorDefinition, expressionDefinitionId);
        }

    }

    @Override
    public void find(ActivityDefinition activityDefinition, long expressionDefinitionId) {
        findExpressionFromNotNullContainer(activityDefinition.getDisplayName(), expressionDefinitionId);
        findExpressionFromNotNullContainer(activityDefinition.getLoopCharacteristics(), expressionDefinitionId);

        for (BusinessDataDefinition businessDataDefinition : activityDefinition.getBusinessDataDefinitions()) {
            findExpressionFromNotNullContainer(businessDataDefinition, expressionDefinitionId);
        }

        for (DataDefinition dataDefinition : activityDefinition.getDataDefinitions()) {
            findExpressionFromNotNullContainer(dataDefinition, expressionDefinitionId);
        }

        getExpressionFromOperationList(activityDefinition.getOperations(), expressionDefinitionId);

        for (BoundaryEventDefinition boundaryEventDefinition : activityDefinition.getBoundaryEventDefinitions()) {
            findExpressionFromNotNullContainer(boundaryEventDefinition, expressionDefinitionId);
        }

    }

    @Override
    public void find(HumanTaskDefinition humanTaskDefinition, long expressionDefinitionId) {
        if (humanTaskDefinition != null) {
            findExpressionFromNotNullContainer(humanTaskDefinition.getUserFilter(), expressionDefinitionId);
        }
        findExpressionFromNotNullContainer(humanTaskDefinition.getExpectedDuration(), expressionDefinitionId);

    }

    @Override
    public void find(UserFilterDefinition userFilterDefinition, long expressionDefinitionId) {
        for (Expression expression : userFilterDefinition.getInputs().values()) {
            findExpressionFromNotNullContainer(expression, expressionDefinitionId);
        }

    }

    @Override
    public void find(UserTaskDefinition userTaskDefinition, long expressionDefinitionId) {
        for (ContextEntry contextEntry : userTaskDefinition.getContext()) {
            findExpressionFromNotNullContainer(contextEntry, expressionDefinitionId);
        }
    }

    @Override
    public void find(SendTaskDefinition sendTaskDefinition, long expressionDefinitionId) {
        findExpressionFromNotNullContainer(sendTaskDefinition.getMessageTrigger(), expressionDefinitionId);
    }

    @Override
    public void find(ReceiveTaskDefinition receiveTaskDefinition, long expressionDefinitionId) {
        findExpressionFromNotNullContainer(receiveTaskDefinition.getTrigger(), expressionDefinitionId);
    }

    @Override
    public void find(SubProcessDefinition subProcessDefinition, long expressionDefinitionId) {
        findExpressionFromNotNullContainer(subProcessDefinition.getSubProcessContainer(), expressionDefinitionId);
    }

    @Override
    public void find(CallActivityDefinition callActivityDefinition, long expressionDefinitionId) {
        findExpressionFromNotNullContainer(callActivityDefinition.getCallableElement(), expressionDefinitionId);
        findExpressionFromNotNullContainer(callActivityDefinition.getCallableElementVersion(), expressionDefinitionId);
        for (Expression expression : callActivityDefinition.getProcessStartContractInputs().values()) {
            findExpressionFromNotNullContainer(expression, expressionDefinitionId);
        }
        getExpressionFromOperationList(callActivityDefinition.getDataInputOperations(), expressionDefinitionId);
        getExpressionFromOperationList(callActivityDefinition.getDataOutputOperations(), expressionDefinitionId);
    }

    @Override
    public void find(Operation operation, long expressionDefinitionId) {
        findExpressionFromNotNullContainer(operation.getRightOperand(), expressionDefinitionId);
    }

    @Override
    public void find(TransitionDefinition transition, long expressionDefinitionId) {
        findExpressionFromNotNullContainer(transition.getCondition(), expressionDefinitionId);
    }

    @Override
    public void find(BusinessDataDefinition businessDataDefinition, long expressionDefinitionId) {
        findExpressionFromNotNullContainer(businessDataDefinition.getDefaultValueExpression(), expressionDefinitionId);
    }

    @Override
    public void find(DataDefinition dataDefinition, long expressionDefinitionId) {
        findExpressionFromNotNullContainer(dataDefinition.getDefaultValueExpression(), expressionDefinitionId);
    }

    @Override
    public void find(CorrelationDefinition correlationDefinition, long expressionDefinitionId) {
        findExpressionFromNotNullContainer(correlationDefinition.getKey(), expressionDefinitionId);
        findExpressionFromNotNullContainer(correlationDefinition.getValue(), expressionDefinitionId);
    }

    @Override
    public void find(CatchMessageEventTriggerDefinition catchMessageEventTriggerDefinition, long expressionDefinitionId) {
        getExpressionFromOperationList(catchMessageEventTriggerDefinition.getOperations(), expressionDefinitionId);
    }

    @Override
    public void find(ThrowMessageEventTriggerDefinition throwMessageEventTriggerDefinition, long expressionDefinitionId) {
        findExpressionFromNotNullContainer(throwMessageEventTriggerDefinition.getTargetFlowNode(), expressionDefinitionId);
        findExpressionFromNotNullContainer(throwMessageEventTriggerDefinition.getTargetProcess(), expressionDefinitionId);
        for (DataDefinition dataDefinition : throwMessageEventTriggerDefinition.getDataDefinitions()) {
            findExpressionFromNotNullContainer(dataDefinition, expressionDefinitionId);
        }
    }

    @Override
    public void find(MessageEventTriggerDefinition messageEventTriggerDefinition, long expressionDefinitionId) {
        for (CorrelationDefinition correlationDefinition : messageEventTriggerDefinition.getCorrelations()) {
            findExpressionFromNotNullContainer(correlationDefinition, expressionDefinitionId);
        }
    }

    @Override
    public void find(TimerEventTriggerDefinition timerEventTriggerDefinition, long expressionDefinitionId) {
        findExpressionFromNotNullContainer(timerEventTriggerDefinition.getTimerExpression(), expressionDefinitionId);
    }

    @Override
    public void find(ContextEntry contextEntry, long expressionDefinitionId) {
        findExpressionFromNotNullContainer(contextEntry.getExpression(), expressionDefinitionId);
    }

    @Override
    public void find(EventDefinition eventDefinition, long expressionDefinitionId) {
        for (EventTriggerDefinition eventTriggerDefinition : eventDefinition.getEventTriggers()) {
            findExpressionFromNotNullContainer(eventTriggerDefinition, expressionDefinitionId);
        }
    }

    @Override
    public void find(ThrowEventDefinition throwEventDefinition, long expressionDefinitionId) {
        for (ThrowMessageEventTriggerDefinition throwMessageEventTriggerDefinition : throwEventDefinition.getMessageEventTriggerDefinitions()) {
            findExpressionFromNotNullContainer(throwMessageEventTriggerDefinition, expressionDefinitionId);
        }
    }

    @Override
    public void find(CatchEventDefinition catchEventDefinition, long expressionDefinitionId) {
        for (TimerEventTriggerDefinition timerEventTriggerDefinition : catchEventDefinition.getTimerEventTriggerDefinitions()) {
            findExpressionFromNotNullContainer(timerEventTriggerDefinition, expressionDefinitionId);
        }
        for (CatchMessageEventTriggerDefinition catchMessageEventTriggerDefinition : catchEventDefinition.getMessageEventTriggerDefinitions()) {
            findExpressionFromNotNullContainer(catchMessageEventTriggerDefinition, expressionDefinitionId);
        }
    }

    @Override
    public void find(DocumentDefinition documentDefinition, long expressionDefinitionId) {
        findExpressionFromNotNullContainer(documentDefinition.getInitialValue(), expressionDefinitionId);
    }

    @Override
    public void find(DocumentListDefinition documentListDefinition, long expressionDefinitionId) {
        findExpressionFromNotNullContainer(documentListDefinition.getExpression(), expressionDefinitionId);
    }

    @Override
    public void find(ConnectorDefinition connectorDefinition, long expressionDefinitionId) {
        for (Expression expression : connectorDefinition.getInputs().values()) {
            findExpressionFromNotNullContainer(expression, expressionDefinitionId);
        }
        getExpressionFromOperationList(connectorDefinition.getOutputs(), expressionDefinitionId);
    }

    @Override
    public void find(StandardLoopCharacteristics standardLoopCharacteristics, long expressionDefinitionId) {
        findExpressionFromNotNullContainer(standardLoopCharacteristics.getLoopCondition(), expressionDefinitionId);
        findExpressionFromNotNullContainer(standardLoopCharacteristics.getLoopMax(), expressionDefinitionId);
    }

    @Override
    public void find(MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics, long expressionDefinitionId) {
        findExpressionFromNotNullContainer(multiInstanceLoopCharacteristics.getCompletionCondition(), expressionDefinitionId);
        findExpressionFromNotNullContainer(multiInstanceLoopCharacteristics.getLoopCardinality(), expressionDefinitionId);
    }
}
