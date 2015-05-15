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
import org.bonitasoft.engine.bpm.flownode.LoopCharacteristics;
import org.bonitasoft.engine.bpm.flownode.MessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.MultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.bpm.flownode.ReceiveTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.SendTaskDefinition;
import org.bonitasoft.engine.bpm.flownode.StandardLoopCharacteristics;
import org.bonitasoft.engine.bpm.flownode.ThrowEventDefinition;
import org.bonitasoft.engine.bpm.flownode.ThrowMessageEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerDefinition;
import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.bpm.flownode.UserTaskDefinition;
import org.bonitasoft.engine.bpm.process.Container;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
import org.bonitasoft.engine.bpm.process.SubProcessDefinition;
import org.bonitasoft.engine.bpm.userfilter.UserFilterDefinition;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.operation.Operation;

/**
 * author Emmanuel Duchastenier
 */
public class ExpressionFinder implements ModelFinderVisitor<Expression> {

    private Expression getExpressionWithId(Expression expression, long expressionDefinitionId) {
        if (expression != null && expression.accept(this, expressionDefinitionId) != null) {
            return expression;
        }
        return null;
    }

    protected Expression getExpressionFromContainer(Container container, long expressionDefinitionId) {
        if (container != null) {
            final Expression expression = container.accept(this, expressionDefinitionId);
            if (expression != null) {
                return expression;
            }
        }
        return null;
    }

    protected Expression getExpressionFromOperationList(List<Operation> operations, long expressionDefinitionId) {
        for (Operation operation : operations) {
            final Expression expression = getExpressionFromContainer(operation, expressionDefinitionId);
            if (expression == null) {
                return expression;
            }
        }
        return null;
    }

    @Override
    public Expression find(Expression expression, long expressionDefinitionId) {
        return (expression != null && expression.getId() == expressionDefinitionId) ? expression : null;
    }

    @Override
    public Expression find(DesignProcessDefinition designProcessDefinition, long expressionDefinitionId) {
        for (int i = 1; i <= 5; i++) {
            final Expression expression = designProcessDefinition.getStringIndexValue(i);
            if (getExpressionWithId(expression, expressionDefinitionId) != null) {
                return expression;
            }
        }
        final FlowElementContainerDefinition flowElementContainer = designProcessDefinition.getFlowElementContainer();
        return flowElementContainer != null ? flowElementContainer.accept(this, expressionDefinitionId) : null;
    }

    @Override
    public Expression find(FlowNodeDefinition flowNodeDefinition, long expressionDefinitionId) {
        if (flowNodeDefinition == null) {
            return null;
        }

        Expression aExpression = flowNodeDefinition.getDisplayName();
        if (getExpressionWithId(aExpression, expressionDefinitionId) != null) {
            return aExpression;
        }
        aExpression = flowNodeDefinition.getDisplayDescription();
        if (getExpressionWithId(aExpression, expressionDefinitionId) != null) {
            return aExpression;
        }
        aExpression = flowNodeDefinition.getDisplayDescriptionAfterCompletion();
        if (getExpressionWithId(aExpression, expressionDefinitionId) != null) {
            return aExpression;
        }

        final Expression defaultTransition = getExpressionFromContainer(flowNodeDefinition.getDefaultTransition(), expressionDefinitionId);
        if (defaultTransition != null) {
            return defaultTransition;
        }

        for (TransitionDefinition transitionDefinition : flowNodeDefinition.getIncomingTransitions()) {
            final Expression expression = getExpressionFromContainer(transitionDefinition, expressionDefinitionId);
            if (expression != null) {
                return expression;
            }
        }
        for (TransitionDefinition transitionDefinition : flowNodeDefinition.getOutgoingTransitions()) {
            final Expression expression = getExpressionFromContainer(transitionDefinition, expressionDefinitionId);
            if (expression != null) {
                return expression;
            }
        }

        for (ConnectorDefinition connectorDefinition : flowNodeDefinition.getConnectors()) {
            final Expression expression = getExpressionFromContainer(connectorDefinition, expressionDefinitionId);
            if (expression != null) {
                return expression;
            }
        }

        return null;
    }

    @Override
    public Expression find(FlowElementContainerDefinition container, long expressionDefinitionId) {
        for (ActivityDefinition activity : container.getActivities()) {
            final Expression expression = getExpressionFromContainer(activity, expressionDefinitionId);
            if (expression != null) {
                return expression;
            }
        }
        for (TransitionDefinition transition : container.getTransitions()) {
            final Expression expression = getExpressionFromContainer(transition, expressionDefinitionId);
            if (expression != null) {
                return expression;
            }
        }
        for (GatewayDefinition gatewayDefinition : container.getGatewaysList()) {
            final Expression expression = getExpressionFromContainer(gatewayDefinition, expressionDefinitionId);
            if (expression != null) {
                return expression;
            }
        }
        for (EndEventDefinition endEventDefinition : container.getEndEvents()) {
            final Expression expression = getExpressionFromContainer(endEventDefinition, expressionDefinitionId);
            if (expression != null) {
                return expression;
            }
        }
        for (DataDefinition dataDefinition : container.getDataDefinitions()) {
            final Expression dataExpression = getExpressionFromContainer(dataDefinition, expressionDefinitionId);
            if (dataExpression != null) {
                return dataExpression;
            }
        }
        for (DocumentDefinition documentDefinition : container.getDocumentDefinitions()) {
            final Expression expression = getExpressionFromContainer(documentDefinition, expressionDefinitionId);
            if (expression != null) {
                return expression;
            }
        }
        for (DocumentListDefinition documentListDefinition : container.getDocumentListDefinitions()) {
            final Expression expression = getExpressionFromContainer(documentListDefinition, expressionDefinitionId);
            if (expression != null) {
                return expression;
            }
        }
        for (BusinessDataDefinition businessDataDefinition : container.getBusinessDataDefinitions()) {
            final Expression bizDataExpression = getExpressionFromContainer(businessDataDefinition, expressionDefinitionId);
            if (bizDataExpression != null) {
                return bizDataExpression;
            }
        }
        for (ConnectorDefinition connectorDefinition : container.getConnectors()) {
            final Expression expression = getExpressionFromContainer(connectorDefinition, expressionDefinitionId);
            if (expression != null) {
                return expression;
            }
        }
        return null;
    }

    @Override
    public Expression find(ActivityDefinition activityDefinition, long expressionDefinitionId) {
        final Expression displayName = activityDefinition.getDisplayName();
        if (getExpressionWithId(displayName, expressionDefinitionId) != null) {
            return displayName;
        }
        final LoopCharacteristics loopCharacteristics = activityDefinition.getLoopCharacteristics();
        final Expression expression = getExpressionFromContainer(loopCharacteristics, expressionDefinitionId);
        if (expression != null) {
            return expression;
        }

        for (BusinessDataDefinition businessDataDefinition : activityDefinition.getBusinessDataDefinitions()) {
            final Expression bizDataExpression = getExpressionFromContainer(businessDataDefinition, expressionDefinitionId);
            if (bizDataExpression != null) {
                return bizDataExpression;
            }
        }

        for (DataDefinition dataDefinition : activityDefinition.getDataDefinitions()) {
            final Expression dataExpression = getExpressionFromContainer(dataDefinition, expressionDefinitionId);
            if (dataExpression != null) {
                return dataExpression;
            }
        }
        final Expression expressionFromOperation = getExpressionFromOperationList(activityDefinition.getOperations(), expressionDefinitionId);
        if (expressionFromOperation != null) {
            return expressionFromOperation;
        }
        for (BoundaryEventDefinition boundaryEventDefinition : activityDefinition.getBoundaryEventDefinitions()) {
            final Expression expressionFromBoundary = getExpressionFromContainer(boundaryEventDefinition, expressionDefinitionId);
            if (expressionFromBoundary == null) {
                return expressionFromBoundary;
            }
        }

        return null;
    }

    @Override
    public Expression find(HumanTaskDefinition humanTaskDefinition, long expressionDefinitionId) {
        return humanTaskDefinition != null ? humanTaskDefinition.getUserFilter().accept(this, expressionDefinitionId) : null;
    }

    @Override
    public Expression find(UserFilterDefinition userFilterDefinition, long expressionDefinitionId) {
        for (Expression expression : userFilterDefinition.getInputs().values()) {
            if (getExpressionWithId(expression, expressionDefinitionId) != null) {
                return expression;
            }
        }
        return null;
    }

    @Override
    public Expression find(UserTaskDefinition userTaskDefinition, long expressionDefinitionId) {
        for (ContextEntry contextEntry : userTaskDefinition.getContext()) {
            final Expression contextExpression = getExpressionFromContainer(contextEntry, expressionDefinitionId);
            if (contextExpression != null) {
                return contextExpression;
            }
        }
        return null;
    }

    @Override
    public Expression find(SendTaskDefinition sendTaskDefinition, long expressionDefinitionId) {
        return getExpressionFromContainer(sendTaskDefinition.getMessageTrigger(), expressionDefinitionId);
    }

    @Override
    public Expression find(ReceiveTaskDefinition receiveTaskDefinition, long expressionDefinitionId) {
        return getExpressionFromContainer(receiveTaskDefinition.getTrigger(), expressionDefinitionId);
    }

    @Override
    public Expression find(SubProcessDefinition subProcessDefinition, long expressionDefinitionId) {
        return getExpressionFromContainer(subProcessDefinition.getSubProcessContainer(), expressionDefinitionId);
    }

    @Override
    public Expression find(CallActivityDefinition callActivityDefinition, long expressionDefinitionId) {
        final Expression callableElement = callActivityDefinition.getCallableElement();
        if (getExpressionWithId(callableElement, expressionDefinitionId) != null) {
            return callableElement;
        }
        final Expression callableElementVersion = callActivityDefinition.getCallableElementVersion();
        if (getExpressionWithId(callableElementVersion, expressionDefinitionId) != null) {
            return callableElementVersion;
        }
        for (Expression expression : callActivityDefinition.getProcessStartContractInputs().values()) {
            if (getExpressionWithId(expression, expressionDefinitionId) != null) {
                return expression;
            }
        }
        final Expression expressionInput = getExpressionFromOperationList(callActivityDefinition.getDataInputOperations(), expressionDefinitionId);
        if (expressionInput != null) {
            return expressionInput;
        }
        final Expression expressionOutput = getExpressionFromOperationList(callActivityDefinition.getDataOutputOperations(), expressionDefinitionId);
        if (expressionOutput != null) {
            return expressionOutput;
        }
        return null;
    }

    @Override
    public Expression find(Operation operation, long expressionDefinitionId) {
        return getExpressionWithId(operation.getRightOperand(), expressionDefinitionId);
    }

    @Override
    public Expression find(TransitionDefinition transition, long expressionDefinitionId) {
        return getExpressionWithId(transition.getCondition(), expressionDefinitionId);
    }

    @Override
    public Expression find(BusinessDataDefinition businessDataDefinition, long expressionDefinitionId) {
        return getExpressionWithId(businessDataDefinition.getDefaultValueExpression(), expressionDefinitionId);
    }

    @Override
    public Expression find(DataDefinition dataDefinition, long expressionDefinitionId) {
        return getExpressionWithId(dataDefinition.getDefaultValueExpression(), expressionDefinitionId);
    }

    @Override
    public Expression find(CorrelationDefinition correlationDefinition, long expressionDefinitionId) {
        final Expression keyExpression = getExpressionWithId(correlationDefinition.getKey(), expressionDefinitionId);
        if (keyExpression != null) {
            return keyExpression;
        }
        final Expression valueExpression = getExpressionWithId(correlationDefinition.getValue(), expressionDefinitionId);
        if (valueExpression != null) {
            return valueExpression;
        }
        return null;
    }

    @Override
    public Expression find(CatchMessageEventTriggerDefinition catchMessageEventTriggerDefinition, long expressionDefinitionId) {
        final Expression expression = getExpressionFromOperationList(catchMessageEventTriggerDefinition.getOperations(), expressionDefinitionId);
        if (expression != null) {
            return expression;
        }
        return null;
    }

    @Override
    public Expression find(ThrowMessageEventTriggerDefinition throwMessageEventTriggerDefinition, long expressionDefinitionId) {
        final Expression targetFlownode = getExpressionWithId(throwMessageEventTriggerDefinition.getTargetFlowNode(), expressionDefinitionId);
        if (targetFlownode != null) {
            return targetFlownode;
        }
        final Expression targetProcess = getExpressionWithId(throwMessageEventTriggerDefinition.getTargetProcess(), expressionDefinitionId);
        if (targetProcess != null) {
            return targetProcess;
        }
        for (DataDefinition dataDefinition : throwMessageEventTriggerDefinition.getDataDefinitions()) {
            final Expression dataExpression = getExpressionFromContainer(dataDefinition, expressionDefinitionId);
            if (dataExpression != null) {
                return dataExpression;
            }
        }
        return null;
    }

    @Override
    public Expression find(MessageEventTriggerDefinition messageEventTriggerDefinition, long expressionDefinitionId) {
        for (CorrelationDefinition correlationDefinition : messageEventTriggerDefinition.getCorrelations()) {
            final Expression expression = getExpressionFromContainer(correlationDefinition, expressionDefinitionId);
            if (expression != null) {
                return expression;
            }
        }
        return null;
    }

    @Override
    public Expression find(TimerEventTriggerDefinition timerEventTriggerDefinition, long expressionDefinitionId) {
        return getExpressionWithId(timerEventTriggerDefinition.getTimerExpression(), expressionDefinitionId);
    }

    @Override
    public Expression find(ContextEntry contextEntry, long expressionDefinitionId) {
        return getExpressionWithId(contextEntry.getExpression(), expressionDefinitionId);
    }

    @Override
    public Expression find(EventDefinition eventDefinition, long expressionDefinitionId) {
        for (EventTriggerDefinition eventTriggerDefinition : eventDefinition.getEventTriggers()) {
            final Expression expression = getExpressionFromContainer(eventTriggerDefinition, expressionDefinitionId);
            if (expression != null) {
                return expression;
            }
        }
        return null;
    }

    @Override
    public Expression find(ThrowEventDefinition throwEventDefinition, long expressionDefinitionId) {
        for (ThrowMessageEventTriggerDefinition throwMessageEventTriggerDefinition : throwEventDefinition.getMessageEventTriggerDefinitions()) {
            final Expression expression = getExpressionFromContainer(throwMessageEventTriggerDefinition, expressionDefinitionId);
            if (expression != null) {
                return expression;
            }
        }
        return null;
    }

    @Override
    public Expression find(CatchEventDefinition catchEventDefinition, long expressionDefinitionId) {
        for (TimerEventTriggerDefinition timerEventTriggerDefinition : catchEventDefinition.getTimerEventTriggerDefinitions()) {
            final Expression expression = getExpressionFromContainer(timerEventTriggerDefinition, expressionDefinitionId);
            if (expression != null) {
                return expression;
            }
        }
        for (CatchMessageEventTriggerDefinition catchMessageEventTriggerDefinition : catchEventDefinition.getMessageEventTriggerDefinitions()) {
            final Expression expression = getExpressionFromContainer(catchMessageEventTriggerDefinition, expressionDefinitionId);
            if (expression != null) {
                return expression;
            }
        }
        return null;
    }

    @Override
    public Expression find(DocumentDefinition documentDefinition, long expressionDefinitionId) {
        return getExpressionWithId(documentDefinition.getInitialValue(), expressionDefinitionId);
    }

    @Override
    public Expression find(DocumentListDefinition documentListDefinition, long expressionDefinitionId) {
        return getExpressionWithId(documentListDefinition.getExpression(), expressionDefinitionId);
    }

    @Override
    public Expression find(ConnectorDefinition connectorDefinition, long expressionDefinitionId) {
        for (Expression expression : connectorDefinition.getInputs().values()) {
            if (getExpressionWithId(expression, expressionDefinitionId) != null) {
                return expression;
            }
        }
        final Expression expressionFromOperation = getExpressionFromOperationList(connectorDefinition.getOutputs(), expressionDefinitionId);
        if (expressionFromOperation != null) {
            return expressionFromOperation;
        }
        return null;
    }

    @Override
    public Expression find(StandardLoopCharacteristics standardLoopCharacteristics, long expressionDefinitionId) {
        final Expression loopCondition = standardLoopCharacteristics.getLoopCondition();
        if (getExpressionWithId(loopCondition, expressionDefinitionId) != null) {
            return loopCondition;
        }
        final Expression loopMax = standardLoopCharacteristics.getLoopMax();
        if (getExpressionWithId(loopMax, expressionDefinitionId) != null) {
            return loopMax;
        }
        return null;
    }

    @Override
    public Expression find(MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics, long expressionDefinitionId) {
        final Expression completionCondition = multiInstanceLoopCharacteristics.getCompletionCondition();
        if (getExpressionWithId(completionCondition, expressionDefinitionId) != null) {
            return completionCondition;
        }
        final Expression loopCardinality = multiInstanceLoopCharacteristics.getLoopCardinality();
        if (getExpressionWithId(loopCardinality, expressionDefinitionId) != null) {
            return loopCardinality;
        }
        return null;
    }
}
