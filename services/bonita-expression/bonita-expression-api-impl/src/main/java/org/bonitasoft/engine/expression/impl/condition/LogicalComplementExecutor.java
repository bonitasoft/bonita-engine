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
package org.bonitasoft.engine.expression.impl.condition;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.impl.ConditionExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Elias Ricken de Medeiros
 */
public class LogicalComplementExecutor {

    public Boolean evaluate(final Map<Integer, Object> resolvedExpressions, SExpression expression)
            throws SExpressionEvaluationException {
        validate(expression);
        List<SExpression> dependencies = expression.getDependencies();
        Boolean sourceValue = (Boolean) resolvedExpressions.get(dependencies.get(0).getDiscriminant());
        return sourceValue != null ? !sourceValue : null;
    }

    private void validate(SExpression expression) throws SExpressionEvaluationException {
        List<SExpression> dependencies = expression.getDependencies();
        if (dependencies.size() != 1) {
            throw new SExpressionEvaluationException("The expression '" + ConditionExpressionExecutorStrategy.LOGICAL_COMPLEMENT_OPERATOR
                    + "' must have exactly 1 dependency.", expression.getName());
        }
        if (!Boolean.class.getName().equals(dependencies.get(0).getReturnType())) {
            StringBuilder stb = new StringBuilder();
            stb.append("The dependency of expression '");
            stb.append(ConditionExpressionExecutorStrategy.LOGICAL_COMPLEMENT_OPERATOR);
            stb.append("' must have the return type ");
            stb.append(Boolean.class.getName());
            stb.append(", but ");
            stb.append(dependencies.get(0).getReturnType());
            stb.append(" was found.");
            throw new SExpressionEvaluationException(stb.toString(),
                    expression.getName());
        }
    }

}
