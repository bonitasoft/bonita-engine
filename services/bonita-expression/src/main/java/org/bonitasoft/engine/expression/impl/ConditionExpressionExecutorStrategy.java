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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.NonEmptyContentExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.impl.condition.BinaryComparatorExecutor;
import org.bonitasoft.engine.expression.impl.condition.LogicalComplementExecutor;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ConditionExpressionExecutorStrategy extends NonEmptyContentExpressionExecutorStrategy {

    public static final String LOGICAL_COMPLEMENT_OPERATOR = "!";

    public static final String NOT_EQUALS_COMPARATOR = "!=";

    public static final String EQUALS_COMPARATOR = "==";

    public static final String GREATER_THAN_OR_EQUALS_COMPARATOR = ">=";

    public static final String lESS_THAN_OR_EQUALS_COMPARATOR = "<=";

    public static final String GREATER_THAN_COMPARATOR = ">";

    public static final String LESS_THAN_COMPARATOR = "<";

    private final List<String> validOperators;

    private final LogicalComplementExecutor logicalComplementExecutor;
    private final BinaryComparatorExecutor binaryComparatorExecutor;

    public ConditionExpressionExecutorStrategy(LogicalComplementExecutor logicalComplementExecutor, BinaryComparatorExecutor binaryComparatorExecutor) {
        this.logicalComplementExecutor = logicalComplementExecutor;
        this.binaryComparatorExecutor = binaryComparatorExecutor;
        validOperators = new ArrayList<String>(7);
        validOperators.add(LESS_THAN_COMPARATOR);
        validOperators.add(GREATER_THAN_COMPARATOR);
        validOperators.add(lESS_THAN_OR_EQUALS_COMPARATOR);
        validOperators.add(GREATER_THAN_OR_EQUALS_COMPARATOR);
        validOperators.add(EQUALS_COMPARATOR);
        validOperators.add(NOT_EQUALS_COMPARATOR);
        validOperators.add(LOGICAL_COMPLEMENT_OPERATOR);
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionEvaluationException {
        final String content = expression.getContent();
        Object result;
        if (LOGICAL_COMPLEMENT_OPERATOR.equals(content)) {
            result = logicalComplementExecutor.evaluate(resolvedExpressions, expression);
        } else {
            result = binaryComparatorExecutor.evaluate(resolvedExpressions, expression);
        }
        return result;
    }

    @Override
    public void validate(final SExpression expression) throws SInvalidExpressionException {
        super.validate(expression);
        if (!validOperators.contains(expression.getContent())) {
            throw new SInvalidExpressionException("The content of expression must be among: " + validOperators + " for expression: " + expression.toString(),
                    expression.getName());
        }
    }

    @Override
    public ExpressionKind getExpressionKind() {
        return new ExpressionKind(SExpression.TYPE_CONDITION);
    }

    @Override
    public List<Object> evaluate(final List<SExpression> expressions, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState)
            throws SExpressionEvaluationException {
        final List<Object> evaluatedExpressions = new ArrayList<Object>(expressions.size());
        for (final SExpression sExpression : expressions) {
            evaluatedExpressions.add(evaluate(sExpression, context, resolvedExpressions, containerState));
        }
        return evaluatedExpressions;
    }

    @Override
    public boolean mustPutEvaluatedExpressionInContext() {
        return false;
    }

}
