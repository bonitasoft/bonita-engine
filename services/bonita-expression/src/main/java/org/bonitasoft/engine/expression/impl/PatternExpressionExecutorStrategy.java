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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.NonEmptyContentExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * <code>ExpressionExecutorStrategy</code> to evaluate a 'Pattern', that is a String message into which some parameters are replaced by their values.
 * 
 * @author Emmanuel Duchastenier
 */
public class PatternExpressionExecutorStrategy extends NonEmptyContentExpressionExecutorStrategy {

    @Override
    public Serializable evaluate(final SExpression expression, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionDependencyMissingException {
        final List<SExpression> dependencies = expression.getDependencies();
        final Map<String, Object> values = new HashMap<String, Object>(dependencies.size());
        for (final SExpression exp : dependencies) {
            final String name = exp.getName();
            final Object value = resolvedExpressions.get(exp.getDiscriminant());
            if (value == null) {
                throw new SExpressionDependencyMissingException("Expression dependency not found: " + name);
            }
            values.put(name, value);
        }
        final StrSubstitutor strSubstitutor = new StrSubstitutor(values);
        return strSubstitutor.replace(expression.getContent());
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return KIND_PATTERN;
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionDependencyMissingException {
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
