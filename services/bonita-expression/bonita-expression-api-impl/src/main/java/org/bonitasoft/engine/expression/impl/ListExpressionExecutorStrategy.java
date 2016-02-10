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
package org.bonitasoft.engine.expression.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.ExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * Evaluate a list of SExpression, represented by the {@link SExpression#getDependencies()} method. The result is a {@link java.util.List} of Serializable
 * objects.
 * 
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class ListExpressionExecutorStrategy implements ExpressionExecutorStrategy {

    @Override
    public void validate(final SExpression expression) {
        // nothing to validate, as Business logic resides in dependencies:
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_LIST;
    }

    @Override
    public Serializable evaluate(final SExpression expression, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) {
        final List<Object> result = new ArrayList<Object>(expression.getDependencies().size());
        for (final SExpression exp : expression.getDependencies()) {
            result.add(resolvedExpressions.get(exp.getDiscriminant()));
        }
        // Let's put this ListExpression ExpressionResult in the list of resolved dependencies:
        resolvedExpressions.put(expression.getDiscriminant(), result);
        return (Serializable) result;
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) {
        final List<Object> list = new ArrayList<Object>(expressions.size());
        for (final SExpression expression : expressions) {
            list.add(evaluate(expression, context, resolvedExpressions, containerState));
        }
        return list;
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return false;
    }

}
