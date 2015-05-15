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

import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.CallActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.FlowElementContainerDefinition;
import org.bonitasoft.engine.bpm.flownode.FlowNodeDefinition;
import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ModelFinderVisitor;
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
        // FIXME: implement me!
        return null;
    }

    @Override
    public Expression find(FlowElementContainerDefinition flowElementContainerDefinition, long expressionDefinitionId) {
        for (ActivityDefinition activity : flowElementContainerDefinition.getActivities()) {
            final Expression expression = activity.accept(this, expressionDefinitionId);
            if (getExpressionWithId(expression, expressionDefinitionId) != null) {
                return expression;
            }
        }
        for (TransitionDefinition transition : flowElementContainerDefinition.getTransitions()) {
            final Expression expression = transition.accept(this, expressionDefinitionId);
            if (getExpressionWithId(expression, expressionDefinitionId) != null) {
                return expression;
            }
        }
        //        List<GatewayDefinition> getGatewaysList();
        //
        //        List<StartEventDefinition> getStartEvents();
        //
        //        List<IntermediateCatchEventDefinition> getIntermediateCatchEvents();
        //
        //        List<IntermediateThrowEventDefinition> getIntermediateThrowEvents();
        //
        //        List<EndEventDefinition> getEndEvents();
        //
        //        List<DataDefinition> getDataDefinitions();
        //
        //        List<DocumentDefinition> getDocumentDefinitions();
        //
        //        List<ConnectorDefinition> getConnectors();
        //
        //        FlowNodeDefinition getFlowNode(long sourceId);
        //
        //        List<BusinessDataDefinition> getBusinessDataDefinitions();
        //
        //        List<DocumentListDefinition> getDocumentListDefinitions();
        return null;
    }

    @Override
    public Expression find(ActivityDefinition activityDefinition, long expressionDefinitionId) {
        final Expression displayName = activityDefinition.getDisplayName();
        if (getExpressionWithId(displayName, expressionDefinitionId) != null) {
            return displayName;
        }
        return null;
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
        for (Operation operation : callActivityDefinition.getDataInputOperations()) {
            if (operation.accept(this, expressionDefinitionId) != null) {
                return operation.accept(this, expressionDefinitionId);
            }
        }
        for (Operation operation : callActivityDefinition.getDataOutputOperations()) {
            if (operation.accept(this, expressionDefinitionId) != null) {
                return operation.accept(this, expressionDefinitionId);
            }
        }
        return null;
    }

    @Override
    public Expression find(Operation operation, long expressionDefinitionId) {
        return operation != null ? operation.getRightOperand().accept(this, expressionDefinitionId) : null;
    }

    @Override
    public Expression find(TransitionDefinition transition, long modelId) {
        return transition != null ? transition.getCondition().accept(this, modelId) : null;
    }
}
