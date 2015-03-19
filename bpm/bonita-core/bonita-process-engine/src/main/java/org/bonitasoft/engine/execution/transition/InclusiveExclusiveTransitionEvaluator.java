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
import java.util.Iterator;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.flowmerger.FlowNodeTransitionsWrapper;

/**
 * @author Elias Ricken de Medeiros
 */
public class InclusiveExclusiveTransitionEvaluator {

    private final TransitionEvaluationStrategy strategy;

    private final TransitionConditionEvaluator evaluator;
    private final DefaultTransitionGetter defaultTransitionGetter;

    public InclusiveExclusiveTransitionEvaluator(TransitionEvaluationStrategy strategy, TransitionConditionEvaluator evaluator, DefaultTransitionGetter defaultTransitionGetter) {
        this.strategy = strategy;
        this.evaluator = evaluator;
        this.defaultTransitionGetter = defaultTransitionGetter;
    }

    public List<STransitionDefinition> evaluateTransitions(final SProcessDefinition sDefinition, final SFlowNodeInstance flowNodeInstance,
                                                               FlowNodeTransitionsWrapper transitions, final SExpressionContext sExpressionContext) throws SBonitaException {
        List<STransitionDefinition> outgoingTransitionDefinitions = transitions.getAllOutgoingTransitionDefinitions();
        final List<STransitionDefinition> chosenTransitions = evaluateNonDefaultTransitions(sExpressionContext, outgoingTransitionDefinitions);

        if (chosenTransitions.isEmpty()) {
            STransitionDefinition defaultTransition = defaultTransitionGetter.getDefaultTransition(transitions, sDefinition, flowNodeInstance);
            chosenTransitions.add(defaultTransition);
            outgoingTransitionDefinitions.add(defaultTransition);
        }

        return chosenTransitions;
    }

    private List<STransitionDefinition> evaluateNonDefaultTransitions(final SExpressionContext sExpressionContext, final List<STransitionDefinition> outgoingTransitionDefinitions) throws SBonitaException {
        final List<STransitionDefinition> chosenTransitions = new ArrayList<STransitionDefinition>(outgoingTransitionDefinitions.size());
        boolean found = false;
        Iterator<STransitionDefinition> iterator = outgoingTransitionDefinitions.iterator();
        while (iterator.hasNext() && strategy.shouldContinue(found)) {
            STransitionDefinition transitionDefinition = iterator.next();
            Boolean shouldTakeTransition = evaluator.evaluateCondition(transitionDefinition, sExpressionContext);
            if(!transitionDefinition.hasCondition() || (shouldTakeTransition != null && shouldTakeTransition)) {
                chosenTransitions.add(transitionDefinition);
                found = true;
            }
        }
        return chosenTransitions;
    }


}
