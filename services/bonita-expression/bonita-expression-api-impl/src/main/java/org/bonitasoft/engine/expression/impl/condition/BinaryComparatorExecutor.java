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

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.impl.ConditionExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Elias Ricken de Medeiros
 */
public class BinaryComparatorExecutor {

    private final BinaryComparatorMapper mapper;

    private final Set<String> numericTypes;

    public BinaryComparatorExecutor(BinaryComparatorMapper mapper) {
        this.mapper = mapper;

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

    public Boolean evaluate(final Map<Integer, Object> resolvedExpressions, SExpression expression) throws SExpressionEvaluationException {
        validate(expression);
        BinaryComparator evaluator = mapper.getEvaluator(expression.getContent());
        if (evaluator == null) {
            throw new SExpressionEvaluationException("Unable to find evaluator for operator '" + expression.getContent() + "'", expression.getName());
        }
        SExpression leftExpression = expression.getDependencies().get(0);
        SExpression rightExpression = expression.getDependencies().get(1);
        final Object resolvedLeftExpr = transtypeIfApplicable(leftExpression.getReturnType(), resolvedExpressions.get(leftExpression.getDiscriminant()));
        final Object resolvedRightExpr = transtypeIfApplicable(rightExpression.getReturnType(), resolvedExpressions.get(rightExpression.getDiscriminant()));

        try {
            return evaluator.evaluate(resolvedLeftExpr, resolvedRightExpr);
        } catch (SComparisonException e) {
            throw new SExpressionEvaluationException("Unable to evaluate expression '" + expression.getName() + "'", e, expression.getName());
        }
    }

    /*
     * Transtype integer to long and float to double
     * see bug ENGINE-1261
     */
    protected Object transtypeIfApplicable(final String type, final Object object) {
        if (object == null) {
            return null;
        }
        if (numericTypes.contains(type)) {
            return new BigDecimal(String.valueOf(object));
        }
        return object;
    }

    private void validate(SExpression expression) throws SExpressionEvaluationException {
        validateNumberOfDependencies(expression);
        validateReturnType(expression);
    }

    private void validateReturnType(final SExpression expression) throws SExpressionEvaluationException {
        List<SExpression> dependencies = expression.getDependencies();
        if (!areReturnTypeCompatible(dependencies.get(0).getReturnType(), dependencies.get(1).getReturnType(), expression.getContent())) {
            throw new SExpressionEvaluationException("The two dependencies of expression '" + expression.getContent() + "' must have the same return type.",
                    expression.getName());
        }
    }

    private void validateNumberOfDependencies(final SExpression expression) throws SExpressionEvaluationException {
        int expectedNbOfDep = 2;
        int actualNbOfDep = expression.getDependencies().size();
        if (actualNbOfDep != expectedNbOfDep) {
            StringBuilder stb = new StringBuilder();
            stb.append("The expression '");
            stb.append(expression.getContent());
            stb.append("' has ");
            stb.append(actualNbOfDep);
            stb.append(" dependencies, but it must have exactly ");
            stb.append(expectedNbOfDep);
            stb.append(" dependencies.");
            throw new SExpressionEvaluationException(stb.toString(), expression.getName());
        }
    }

    protected boolean areReturnTypeCompatible(String leftReturnType, String rightReturnType, final String content) {
        return (numericTypes.contains(leftReturnType) && numericTypes.contains(rightReturnType)) || leftReturnType.equals(rightReturnType)
                || ConditionExpressionExecutorStrategy.EQUALS_COMPARATOR.equals(content);
    }

}
