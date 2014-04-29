/**
 * Copyright (C) 2012-2014 BonitaSoft S.A.
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.NonEmptyContentExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.ExpressionKind;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ConditionExpressionExecutorStrategy extends NonEmptyContentExpressionExecutorStrategy {

    private static final String LOGICAL_COMPLEMENT_OPERATOR = "!";

    private static final String NOT_EQUALS_COMPARATOR = "!=";

    private static final String EQUALS_COMPARATOR = "==";

    private static final String GREATER_THAN_OR_EQUALS_COMPARATOR = ">=";

    private static final String lESS_THAN_OR_EQUALS_COMPARATOR = "<=";

    private static final String GREATER_THAN_COMPARATOR = ">";

    private static final String LESS_THAN_COMPARATOR = "<";

    private final List<String> validOperators;

    private final Set<String> numericTypes;

    public ConditionExpressionExecutorStrategy() {
        validOperators = new ArrayList<String>(7);
        validOperators.add(LESS_THAN_COMPARATOR);
        validOperators.add(GREATER_THAN_COMPARATOR);
        validOperators.add(lESS_THAN_OR_EQUALS_COMPARATOR);
        validOperators.add(GREATER_THAN_OR_EQUALS_COMPARATOR);
        validOperators.add(EQUALS_COMPARATOR);
        validOperators.add(NOT_EQUALS_COMPARATOR);
        validOperators.add(LOGICAL_COMPLEMENT_OPERATOR);
        numericTypes = new HashSet<String>();
        numericTypes.add(Integer.class.getName());
        numericTypes.add(Long.class.getName());
        numericTypes.add(Double.class.getName());
        numericTypes.add(Float.class.getName());
        numericTypes.add(Short.class.getName());
        numericTypes.add(Byte.class.getName());
        numericTypes.add(int.class.getName());
        numericTypes.add(long.class.getName());
        numericTypes.add(double.class.getName());
        numericTypes.add(float.class.getName());
        numericTypes.add(short.class.getName());
        numericTypes.add(byte.class.getName());
    }

    @Override
    public Object evaluate(final SExpression expression, final Map<String, Object> context, final Map<Integer, Object> resolvedExpressions,
            final ContainerState containerState) throws SExpressionEvaluationException {
        final List<SExpression> dependencies = expression.getDependencies();
        final String content = expression.getContent();
        Object result;
        if (!LOGICAL_COMPLEMENT_OPERATOR.equals(content)) {
            result = evaluateBinaryComparator(resolvedExpressions, dependencies, content);
        } else {
            result = evaluateBooleanOperator(resolvedExpressions, dependencies, content);
        }
        return result;
    }

    private Object evaluateBooleanOperator(final Map<Integer, Object> resolvedExpressions, final List<SExpression> dependencies, final String content)
            throws SExpressionEvaluationException {
        final String expressionName = dependencies.get(0).getName();
        throwSExpressionEvaluationExceptionIfBadDependenciesSize(dependencies, 1, content, expressionName);
        if (!Boolean.class.getName().equals(dependencies.get(0).getReturnType())) {
            throw new SExpressionEvaluationException("The dependency of expression '" + content + "' must have the return type " + Boolean.class.getName(),
                    expressionName);
        }
        return !(Boolean) resolvedExpressions.get(dependencies.get(0).getDiscriminant());
    }

    private Object evaluateBinaryComparator(final Map<Integer, Object> resolvedExpressions, final List<SExpression> dependencies, final String content)
            throws SExpressionEvaluationException {
        final String expressionName = dependencies.get(0).getName();
        throwSExpressionEvaluationExceptionIfBadDependenciesSize(dependencies, 2, content, expressionName);
        if (!returnTypeCompatible(dependencies) && !EQUALS_COMPARATOR.equals(content)) {
            throw new SExpressionEvaluationException("The two dependencies of expression '" + content + "' must have the same return type.", expressionName);
        }
        final Object resolvedLeftExpr = transtypeIfApplicable(dependencies.get(0).getReturnType(),
                resolvedExpressions.get(dependencies.get(0).getDiscriminant()));
        final Object resolvedRightExpr = transtypeIfApplicable(dependencies.get(1).getReturnType(),
                resolvedExpressions.get(dependencies.get(1).getDiscriminant()));
        Object result = null;
        if (EQUALS_COMPARATOR.equals(content)) {
            result = areEquals(resolvedLeftExpr, resolvedRightExpr);
        } else if (NOT_EQUALS_COMPARATOR.equals(content)) {
            result = !areEquals(resolvedLeftExpr, resolvedRightExpr);
        } else {
            if (resolvedLeftExpr instanceof Comparable && resolvedRightExpr instanceof Comparable) {
                @SuppressWarnings({ "rawtypes", "unchecked" })
                final Integer compare = compareTo((Comparable) resolvedLeftExpr, (Comparable) resolvedRightExpr);
                if (GREATER_THAN_COMPARATOR.equals(content)) {
                    result = compare > 0;
                } else if (GREATER_THAN_OR_EQUALS_COMPARATOR.equals(content)) {
                    result = compare >= 0;
                } else if (LESS_THAN_COMPARATOR.equals(content)) {
                    result = compare < 0;
                } else if (lESS_THAN_OR_EQUALS_COMPARATOR.equals(content)) {
                    result = compare <= 0;
                }
            } else {
                throw new SExpressionEvaluationException("The two dependencies of expression '" + content + "' must implements java.lang.Comparable.",
                        expressionName);
            }
        }
        return result;
    }

    private boolean returnTypeCompatible(final List<SExpression> dependencies) {
        final String r1 = dependencies.get(0).getReturnType();
        final String r2 = dependencies.get(1).getReturnType();
        return numericTypes.contains(r1) && numericTypes.contains(r2) || r1.equals(r2);
    }

    /*
     * Transtype integer to long and float to double
     * see bug ENGINE-1261
     */
    private Object transtypeIfApplicable(final String type, final Object object) {
        if (object == null) {
            return null;
        }
        if (numericTypes.contains(type)) {
            return new BigDecimal(String.valueOf(object));
        }
        return object;
    }

    private <T> Boolean areEquals(final T left, final T right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }

    private <T extends Comparable<Object>, R extends Comparable<Object>> Integer compareTo(final T left, final R right) {
        int result = 0;
        if (left != right) {
            if (left == null || right == null) {
                if (left == null) {
                    result = -1;
                } else {
                    result = 1;
                }
            } else {
                result = left.compareTo(right);
            }
        }
        return result;
    }

    private void throwSExpressionEvaluationExceptionIfBadDependenciesSize(final List<SExpression> dependencies, final int nbDependencies, final String content,
            final String expressionName) throws SExpressionEvaluationException {
        if (dependencies.size() != nbDependencies) {
            throw new SExpressionEvaluationException("The expression '" + content + "' must have exactly " + nbDependencies + " dependencies.", expressionName);
        }
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
