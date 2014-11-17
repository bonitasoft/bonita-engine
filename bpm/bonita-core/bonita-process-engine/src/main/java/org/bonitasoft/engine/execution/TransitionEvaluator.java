/*
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.bonitasoft.engine.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.execution.flowmerger.FlowNodeTransitionsWrapper;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;

public class TransitionEvaluator {

    private final ExpressionResolverService expressionResolverService;

    public TransitionEvaluator(final ExpressionResolverService expressionResolverService) {
        this.expressionResolverService = expressionResolverService;
    }

    protected List<STransitionDefinition> evaluateOutgoingTransitions(final List<STransitionDefinition> outgoingTransitionDefinitions,
            final SProcessDefinition sDefinition, final SFlowNodeInstance flowNodeInstance) throws SBonitaException {
        // int nbOfTokenToMerge = 1;// may be > 1 in case of gateway
        // if is not a normal state don't create new elements
        if (!SStateCategory.NORMAL.equals(flowNodeInstance.getStateCategory())) {
            return Collections.emptyList();
        }
        final SExpressionContext sExpressionContext = new SExpressionContext(flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(),
                sDefinition.getId());
        if (SFlowNodeType.GATEWAY.equals(flowNodeInstance.getType())) {
            return evaluateOutgoingTransitionsForGateways(outgoingTransitionDefinitions, sDefinition, flowNodeInstance, sExpressionContext);
        } else if (SFlowNodeType.BOUNDARY_EVENT.equals(flowNodeInstance.getType())) {
            return new ArrayList<STransitionDefinition>(outgoingTransitionDefinitions);
        } else {
            return evaluateOutgoingTransitionsForActivity(outgoingTransitionDefinitions, sDefinition, flowNodeInstance, sExpressionContext);
        }
    }

    List<STransitionDefinition> evaluateOutgoingTransitionsForActivity(final List<STransitionDefinition> outgoingTransitionDefinitions,
            final SProcessDefinition sDefinition, final SFlowNodeInstance flowNodeInstance, final SExpressionContext sExpressionContext)
            throws SBonitaException {
        if (outgoingTransitionDefinitions.isEmpty()) {
            STransitionDefinition defaultTransition;
            if ((defaultTransition = getDefaultTransition(sDefinition, flowNodeInstance)) == null) {
                return Collections.emptyList();
            }
            return Collections.singletonList(defaultTransition);
        }
        return evaluateTransitionsForImplicitGateway(sDefinition, flowNodeInstance, outgoingTransitionDefinitions,
                sExpressionContext);
    }

    List<STransitionDefinition> evaluateOutgoingTransitionsForGateways(final List<STransitionDefinition> outgoingTransitionDefinitions,
            final SProcessDefinition sDefinition, final SFlowNodeInstance flowNodeInstance, final SExpressionContext sExpressionContext)
            throws SBonitaException {
        List<STransitionDefinition> chosenTransitionDefinitions;
        final SGatewayInstance gatewayInstance = (SGatewayInstance) flowNodeInstance;
        switch (gatewayInstance.getGatewayType()) {
            case EXCLUSIVE:
                chosenTransitionDefinitions = evaluateTransitionsExclusively(sDefinition, flowNodeInstance, outgoingTransitionDefinitions,
                        sExpressionContext);
                break;
            case INCLUSIVE:
                chosenTransitionDefinitions = evaluateTransitionsInclusively(sDefinition, flowNodeInstance, outgoingTransitionDefinitions,
                        sExpressionContext);
                break;
            case PARALLEL:
                chosenTransitionDefinitions = evaluateTransitionsPalely(outgoingTransitionDefinitions);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported gateway type: " + gatewayInstance.getGatewayType());
        }
        return chosenTransitionDefinitions;
    }

    List<STransitionDefinition> evaluateTransitionsExclusively(final SProcessDefinition sDefinition, final SFlowNodeInstance flowNodeInstance,
            final List<STransitionDefinition> outgoingTransitionDefinitions, final SExpressionContext sExpressionContext) throws SBonitaException {
        final List<STransitionDefinition> chosenTransitions = new ArrayList<STransitionDefinition>(outgoingTransitionDefinitions.size());
        boolean transitionFound = false;
        for (final STransitionDefinition sTransitionDefinition : outgoingTransitionDefinitions) {
            final Boolean condition = evaluateCondition(sDefinition, sTransitionDefinition, sExpressionContext);
            if (condition == null || condition) {
                transitionFound = true;
                chosenTransitions.add(sTransitionDefinition);
                break;
            }
        }
        if (!transitionFound) {
            final STransitionDefinition defaultTransition = getDefaultTransitionIfExists(sDefinition, flowNodeInstance);
            chosenTransitions.add(defaultTransition);
            outgoingTransitionDefinitions.add(defaultTransition);
        }
        return chosenTransitions;
    }

    STransitionDefinition getDefaultTransitionIfExists(final SProcessDefinition sDefinition,
            final SFlowNodeInstance flowNodeInstance) throws SActivityExecutionException {
        final STransitionDefinition defaultTransition = getDefaultTransition(sDefinition, flowNodeInstance);
        if (defaultTransition == null) {
            throwSActivityExecutionException(sDefinition, flowNodeInstance);
        }
        return defaultTransition;
    }

    private void throwSActivityExecutionException(final SProcessDefinition sDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SActivityExecutionException {
        final SActivityExecutionException exception = new SActivityExecutionException("There is no default transition on " + flowNodeInstance.getName()
                + ", but no outgoing transition had a valid condition.");
        exception.setProcessDefinitionNameOnContext(sDefinition.getName());
        exception.setProcessDefinitionVersionOnContext(sDefinition.getVersion());
        exception.setProcessInstanceIdOnContext(flowNodeInstance.getParentProcessInstanceId());
        throw exception;
    }

    List<STransitionDefinition> evaluateTransitionsInclusively(final SProcessDefinition sDefinition, final SFlowNodeInstance flowNodeInstance,
            final List<STransitionDefinition> outgoingTransitionDefinitions, final SExpressionContext sExpressionContext) throws SBonitaException {
        final List<STransitionDefinition> chosenTransitions = new ArrayList<STransitionDefinition>(outgoingTransitionDefinitions.size());
        for (final STransitionDefinition sTransitionDefinition : outgoingTransitionDefinitions) {
            final Boolean condition = evaluateCondition(sDefinition, sTransitionDefinition, sExpressionContext);
            if (condition == null || condition) {
                chosenTransitions.add(sTransitionDefinition);
            }
        }
        if (chosenTransitions.isEmpty()) {
            final STransitionDefinition defaultTransition = getDefaultTransitionIfExists(sDefinition, flowNodeInstance);
            chosenTransitions.add(defaultTransition);
            outgoingTransitionDefinitions.add(defaultTransition);
        }
        return chosenTransitions;
    }

    protected List<STransitionDefinition> evaluateTransitionsForImplicitGateway(final SProcessDefinition sDefinition,
            final SFlowNodeInstance flowNodeInstance,
            final List<STransitionDefinition> outgoingTransitionDefinitions, final SExpressionContext sExpressionContext) throws SBonitaException {
        final int transitionNumber = outgoingTransitionDefinitions.size();
        final List<STransitionDefinition> conditionalTransitions = new ArrayList<STransitionDefinition>(transitionNumber);
        final List<STransitionDefinition> conditionalFalseTransitions = new ArrayList<STransitionDefinition>(transitionNumber);
        final List<STransitionDefinition> chosenTransitions = new ArrayList<STransitionDefinition>(transitionNumber);
        final List<STransitionDefinition> normalTransitions = new ArrayList<STransitionDefinition>(transitionNumber);
        for (final STransitionDefinition sTransitionDefinition : outgoingTransitionDefinitions) {
            final Boolean condition = evaluateCondition(sDefinition, sTransitionDefinition, sExpressionContext);
            if (condition == null) {
                normalTransitions.add(sTransitionDefinition);
            } else {
                if (condition) {
                    conditionalTransitions.add(sTransitionDefinition);
                } else {
                    conditionalFalseTransitions.add(sTransitionDefinition);
                }
            }
        }
        if (!normalTransitions.isEmpty()) {
            chosenTransitions.addAll(normalTransitions);
        }
        if (!conditionalTransitions.isEmpty()) {
            chosenTransitions.addAll(conditionalTransitions);
        } else if (!conditionalFalseTransitions.isEmpty()) {
            final STransitionDefinition defaultTransition = getDefaultTransitionIfExists(sDefinition, flowNodeInstance);
            chosenTransitions.add(defaultTransition);
        }
        return chosenTransitions;
    }

    List<STransitionDefinition> evaluateTransitionsPalely(final List<STransitionDefinition> outgoingTransitionDefinitions) {
        final List<STransitionDefinition> chosenTransitions = new ArrayList<STransitionDefinition>(outgoingTransitionDefinitions.size());
        for (final STransitionDefinition sTransitionDefinition : outgoingTransitionDefinitions) {
            chosenTransitions.add(sTransitionDefinition);
        }
        return chosenTransitions;
    }

    protected STransitionDefinition getDefaultTransition(final SProcessDefinition sDefinition, final SFlowNodeInstance flowNodeInstance) {
        final SFlowElementContainerDefinition processContainer = sDefinition.getProcessContainer();
        final SFlowNodeDefinition flowNode = processContainer.getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
        return flowNode.getDefaultTransition();
    }

    protected Boolean evaluateCondition(final SProcessDefinition sDefinition, final STransitionDefinition sTransitionDefinition,
            final SExpressionContext contextDependency) throws SExpressionEvaluationException, SExpressionTypeUnknownException,
            SExpressionDependencyMissingException, SInvalidExpressionException {
        // TODO find a better method to get expression on transitionDef
        final SFlowElementContainerDefinition processContainer = sDefinition.getProcessContainer();
        final STransitionDefinition transition = processContainer.getTransition(sTransitionDefinition.getName());
        final SExpression expression = transition.getCondition();
        if (expression == null) {// no condition == true but return null to say it was a transition without condition
            return null;
        }
        if (!Boolean.class.getName().equals(expression.getReturnType())) {
            throw new SExpressionEvaluationException("Condition expression must return a boolean, on transition: " + sTransitionDefinition.getName(),
                    expression.getName());
        }
        return (Boolean) expressionResolverService.evaluate(expression, contextDependency);
    }

    FlowNodeTransitionsWrapper buildTransitionsWrapper(final SFlowNodeDefinition flowNode, final SProcessDefinition sProcessDefinition,
            final SFlowNodeInstance child) throws SBonitaException {
        final FlowNodeTransitionsWrapper transitionsDescriptor = new FlowNodeTransitionsWrapper();
        // Retrieve all outgoing transitions
        if (flowNode == null) {
            // not in definition
            transitionsDescriptor.setInputTransitionsSize(0);
            transitionsDescriptor.setAllOutgoingTransitionDefinitions(Collections.<STransitionDefinition> emptyList());
        } else {
            transitionsDescriptor.setInputTransitionsSize(flowNode.getIncomingTransitions().size());
            transitionsDescriptor.setAllOutgoingTransitionDefinitions(new ArrayList<STransitionDefinition>(flowNode.getOutgoingTransitions()));
        }

        // Evaluate all outgoing transitions, and retrieve valid outgoing transitions
        transitionsDescriptor.setValidOutgoingTransitionDefinitions(evaluateOutgoingTransitions(transitionsDescriptor.getAllOutgoingTransitionDefinitions(),
                sProcessDefinition, child));
        return transitionsDescriptor;
    }
}
