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

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Elias Ricken de Medeiros
 */
public class TransitionConditionEvaluator {

    private final ExpressionResolverService resolverService;

    public TransitionConditionEvaluator(ExpressionResolverService resolverService) {
        this.resolverService = resolverService;
    }

    public Boolean evaluateCondition(final STransitionDefinition sTransitionDefinition,
            final SExpressionContext contextDependency) throws SBonitaException {
        SExpression condition = sTransitionDefinition.getCondition();
        if (condition == null) {// no condition == true but return null to say it was a transition without condition --
            return null;
        }
        if (!Boolean.class.getName().equals(condition.getReturnType())) {
            throw new SExpressionEvaluationException("Condition expression must return a boolean, on transition: " + sTransitionDefinition.getName(),
                    condition.getName());
        }
        return (Boolean) resolverService.evaluate(condition, contextDependency);
    }

}
