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
package org.bonitasoft.engine.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SGatewayInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.execution.flowmerger.FlowNodeTransitionsWrapper;
import org.bonitasoft.engine.execution.transition.ImplicitGatewayTransitionEvaluator;
import org.bonitasoft.engine.execution.transition.InclusiveExclusiveTransitionEvaluator;
import org.bonitasoft.engine.execution.transition.ParallelGatewayTransitionEvaluator;

public class TransitionEvaluator {

    private final ImplicitGatewayTransitionEvaluator implicitGatewayTransitionEvaluator;
    private final ParallelGatewayTransitionEvaluator parallelGatewayTransitionEvaluator;
    private final InclusiveExclusiveTransitionEvaluator inclusiveTransitionEvaluator;
    private final InclusiveExclusiveTransitionEvaluator exclusiveTransitionEvaluator;

    public TransitionEvaluator(ImplicitGatewayTransitionEvaluator implicitGatewayTransitionEvaluator,
            ParallelGatewayTransitionEvaluator parallelGatewayTransitionEvaluator, InclusiveExclusiveTransitionEvaluator inclusiveTransitionEvaluator,
            InclusiveExclusiveTransitionEvaluator exclusiveTransitionEvaluator) {
        this.implicitGatewayTransitionEvaluator = implicitGatewayTransitionEvaluator;
        this.parallelGatewayTransitionEvaluator = parallelGatewayTransitionEvaluator;
        this.inclusiveTransitionEvaluator = inclusiveTransitionEvaluator;
        this.exclusiveTransitionEvaluator = exclusiveTransitionEvaluator;
    }

    protected List<STransitionDefinition> evaluateOutgoingTransitions(FlowNodeTransitionsWrapper transitions,
            final SProcessDefinition sDefinition, final SFlowNodeInstance flowNodeInstance) throws SBonitaException {
        // int nbOfTokenToMerge = 1;// may be > 1 in case of gateway
        // if is not a normal state don't create new elements
        if (!SStateCategory.NORMAL.equals(flowNodeInstance.getStateCategory())) {
            return Collections.emptyList();
        }
        final SExpressionContext sExpressionContext = new SExpressionContext(flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(),
                sDefinition.getId());
        if (SFlowNodeType.GATEWAY.equals(flowNodeInstance.getType())) {
            return evaluateOutgoingTransitionsForGateways(transitions, sDefinition, flowNodeInstance, sExpressionContext);
        } else if (SFlowNodeType.BOUNDARY_EVENT.equals(flowNodeInstance.getType())) {
            return new ArrayList<STransitionDefinition>(transitions.getAllOutgoingTransitionDefinitions());
        } else {
            return evaluateOutgoingTransitionsForActivity(transitions, sDefinition, flowNodeInstance, sExpressionContext);
        }
    }

    List<STransitionDefinition> evaluateOutgoingTransitionsForActivity(final FlowNodeTransitionsWrapper transitions, final SProcessDefinition sDefinition,
            final SFlowNodeInstance flowNodeInstance, final SExpressionContext sExpressionContext)
            throws SBonitaException {
        if (transitions.getAllOutgoingTransitionDefinitions().isEmpty()) {
            STransitionDefinition defaultTransition;
            if ((defaultTransition = getDefaultTransition(sDefinition, flowNodeInstance)) == null) {
                return Collections.emptyList();
            }
            return Collections.singletonList(defaultTransition);
        }
        return implicitGatewayTransitionEvaluator.evaluateTransitions(sDefinition, flowNodeInstance, transitions,
                sExpressionContext);
    }

    List<STransitionDefinition> evaluateOutgoingTransitionsForGateways(final FlowNodeTransitionsWrapper transitions,
            final SProcessDefinition sDefinition, final SFlowNodeInstance flowNodeInstance, final SExpressionContext sExpressionContext)
            throws SBonitaException {
        List<STransitionDefinition> chosenTransitionDefinitions;
        final SGatewayInstance gatewayInstance = (SGatewayInstance) flowNodeInstance;
        switch (gatewayInstance.getGatewayType()) {
            case EXCLUSIVE:
                chosenTransitionDefinitions = exclusiveTransitionEvaluator.evaluateTransitions(sDefinition, flowNodeInstance, transitions, sExpressionContext);
                break;
            case INCLUSIVE:
                chosenTransitionDefinitions = inclusiveTransitionEvaluator.evaluateTransitions(sDefinition, flowNodeInstance, transitions, sExpressionContext);
                break;
            case PARALLEL:
                chosenTransitionDefinitions = parallelGatewayTransitionEvaluator.evaluateTransitions(transitions);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported gateway type: " + gatewayInstance.getGatewayType());
        }
        return chosenTransitionDefinitions;
    }

    protected STransitionDefinition getDefaultTransition(final SProcessDefinition sDefinition, final SFlowNodeInstance flowNodeInstance) {
        final SFlowElementContainerDefinition processContainer = sDefinition.getProcessContainer();
        final SFlowNodeDefinition flowNode = processContainer.getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
        return flowNode.getDefaultTransition();
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
            transitionsDescriptor.setDefaultTransition(flowNode.getDefaultTransition());
        }

        // Evaluate all outgoing transitions, and retrieve valid outgoing transitions
        transitionsDescriptor.setValidOutgoingTransitionDefinitions(evaluateOutgoingTransitions(transitionsDescriptor,
                sProcessDefinition, child));
        return transitionsDescriptor;
    }
}
