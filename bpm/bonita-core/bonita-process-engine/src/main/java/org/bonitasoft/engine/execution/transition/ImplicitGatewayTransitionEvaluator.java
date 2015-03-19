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
package org.bonitasoft.engine.execution.transition;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.flowmerger.FlowNodeTransitionsWrapper;

/**
 * @author Elias Ricken de Medeiros
 */
public class ImplicitGatewayTransitionEvaluator {

    private final TransitionConditionEvaluator conditionEvaluator;
    private final DefaultTransitionGetter defaultTransitionGetter;

    public ImplicitGatewayTransitionEvaluator(TransitionConditionEvaluator conditionEvaluator, DefaultTransitionGetter defaultTransitionGetter) {
        this.conditionEvaluator = conditionEvaluator;
        this.defaultTransitionGetter = defaultTransitionGetter;
    }

    public List<STransitionDefinition> evaluateTransitions(final SProcessDefinition sDefinition, final SFlowNodeInstance flowNodeInstance,
            FlowNodeTransitionsWrapper transitions, final SExpressionContext sExpressionContext) throws SBonitaException {
        EvaluatedTransitions evaluatedTransitions = evaluatedTransitions(sExpressionContext, transitions.getAllOutgoingTransitionDefinitions());
        return buildChosenTransitions(evaluatedTransitions, transitions, sDefinition, flowNodeInstance);
    }

    private List<STransitionDefinition> buildChosenTransitions(final EvaluatedTransitions evaluatedTransitions, final FlowNodeTransitionsWrapper transitions,
            final SProcessDefinition sDefinition, final SFlowNodeInstance flowNodeInstance) throws SActivityExecutionException {
        final List<STransitionDefinition> chosenTransitions = new ArrayList<STransitionDefinition>(evaluatedTransitions.getUnconditionalTransitions().size()
                + evaluatedTransitions.getTrueTransitions().size());
        if (evaluatedTransitions.hasUnconditionalTransitions()) {
            chosenTransitions.addAll(evaluatedTransitions.getUnconditionalTransitions());
        }
        if (evaluatedTransitions.hasTrueTransitions()) {
            chosenTransitions.addAll(evaluatedTransitions.getTrueTransitions());
        } else if (evaluatedTransitions.hasFalseTransitons()) {
            final STransitionDefinition defaultTransition = defaultTransitionGetter.getDefaultTransition(transitions, sDefinition, flowNodeInstance);
            chosenTransitions.add(defaultTransition);
        }
        return chosenTransitions;
    }

    private EvaluatedTransitions evaluatedTransitions(final SExpressionContext sExpressionContext,
            final List<STransitionDefinition> outgoingTransitionDefinitions) throws SBonitaException {
        EvaluatedTransitions evaluatedTransitions = new EvaluatedTransitions();
        for (final STransitionDefinition sTransitionDefinition : outgoingTransitionDefinitions) {
            evaluateTransition(evaluatedTransitions, sTransitionDefinition, sExpressionContext);
        }
        return evaluatedTransitions;
    }

    private void evaluateTransition(final EvaluatedTransitions evaluatedTransitions, final STransitionDefinition sTransitionDefinition,
            final SExpressionContext sExpressionContext) throws SBonitaException {
        final Boolean condition = conditionEvaluator.evaluateCondition(sTransitionDefinition, sExpressionContext);
        if (!sTransitionDefinition.hasCondition()) {
            evaluatedTransitions.addUnconditionalTransition(sTransitionDefinition);
        } else {
            if (condition != null && condition) {
                evaluatedTransitions.addTrueTransition(sTransitionDefinition);
            } else {
                evaluatedTransitions.addFalseTransition(sTransitionDefinition);
            }
        }
    }

}
