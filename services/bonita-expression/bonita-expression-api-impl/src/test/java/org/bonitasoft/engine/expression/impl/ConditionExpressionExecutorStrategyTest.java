/**
 * Copyright (C) 2013 BonitaSoft S.A.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class ConditionExpressionExecutorStrategyTest {

    private ConditionExpressionExecutorStrategy strategy;

    private ArrayList<SExpression> dependFirstLtSecond;

    private ArrayList<SExpression> dependFirstGtSecond;

    private ArrayList<SExpression> dependEquals;

    private HashMap<Integer, Object> resolvedDependencies;

    @Before
    public void setup() {
        strategy = new ConditionExpressionExecutorStrategy();
    }

    @Before
    public void initialiseDependencies() {

        final SExpression constExpr1 = buildExpression("5", SExpression.TYPE_CONSTANT, Integer.class.getName(), null, null);
        final SExpression constExpr2 = buildExpression("7", SExpression.TYPE_CONSTANT, Integer.class.getName(), null, null);
        final SExpression constExpr3 = buildExpression("2", SExpression.TYPE_CONSTANT, Integer.class.getName(), null, null);

        dependFirstLtSecond = new ArrayList<SExpression>(2);
        dependFirstLtSecond.add(constExpr1);
        dependFirstLtSecond.add(constExpr2);

        dependFirstGtSecond = new ArrayList<SExpression>(2);
        dependFirstGtSecond.add(constExpr2);
        dependFirstGtSecond.add(constExpr1);

        dependEquals = new ArrayList<SExpression>(2);
        dependEquals.add(constExpr1);
        dependEquals.add(constExpr1);

        resolvedDependencies = new HashMap<Integer, Object>(2);
        resolvedDependencies.put(constExpr1.getDiscriminant(), 5);
        resolvedDependencies.put(constExpr2.getDiscriminant(), 7);
        resolvedDependencies.put(constExpr3.getDiscriminant(), 2);
    }

    /**
     * @param string
     * @param typeConstant
     * @param name
     * @param object
     * @param object2
     * @return
     */
    private SExpression buildExpression(final String content, final String expressionType, final String returnType, final String interpreter,
            final List<SExpression> dependencies) {
        final SExpressionImpl eb = new SExpressionImpl();
        eb.setName(content);
        eb.setContent(content);
        eb.setExpressionType(expressionType);
        eb.setInterpreter(interpreter);
        eb.setReturnType(returnType);
        eb.setDependencies(dependencies);
        return eb;
    }

    @Test
    public void evaluateConditionExpressionGraterThan() throws Exception {
        final SExpression expr1 = buildExpression(">", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependFirstGtSecond);
        evaluateAndCheckResult(expr1, true, resolvedDependencies, ContainerState.ACTIVE);

        final SExpression expr2 = buildExpression(">", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependFirstLtSecond);
        evaluateAndCheckResult(expr2, false, resolvedDependencies, ContainerState.ACTIVE);

        final SExpression expr3 = buildExpression(">", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependEquals);
        evaluateAndCheckResult(expr3, false, resolvedDependencies, ContainerState.ACTIVE);
    }

    protected void evaluateAndCheckResult(final SExpression expression, final Object expectedValue, final Map<Integer, Object> resolvedExpression,
            final ContainerState containerState)
            throws Exception {
        final Object expressionResult = evaluate(expression, resolvedExpression, containerState);
        assertEquals(expectedValue, expressionResult);
    }

    protected Object evaluate(final SExpression expression, final Map<Integer, Object> resolvedExpressions, final ContainerState containerState)
            throws SExpressionEvaluationException {
        return strategy.evaluate(expression, new HashMap<String, Object>(0), resolvedExpressions, containerState);
    }

    protected List<Object> evaluate(final List<SExpression> expression, final Map<Integer, Object> resolvedExpressions, final ContainerState containerState)
            throws SExpressionEvaluationException {
        return strategy.evaluate(expression, new HashMap<String, Object>(0), resolvedExpressions, containerState);
    }

    @Test
    public void evaluateConditionExpressionGraterThanOrEquals() throws Exception {
        final SExpression expr1 = buildExpression(">=", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependFirstGtSecond);
        evaluateAndCheckResult(expr1, true, resolvedDependencies, ContainerState.ACTIVE);

        final SExpression expr2 = buildExpression(">=", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependFirstLtSecond);
        evaluateAndCheckResult(expr2, false, resolvedDependencies, ContainerState.ACTIVE);

        final SExpression expr3 = buildExpression(">=", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependEquals);
        evaluateAndCheckResult(expr3, true, resolvedDependencies, ContainerState.ACTIVE);
    }

    @Test
    public void evaluateConditionExpressionLowerThan() throws Exception {
        final SExpression expr1 = buildExpression("<", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependFirstGtSecond);
        evaluateAndCheckResult(expr1, false, resolvedDependencies, ContainerState.ACTIVE);

        final SExpression expr2 = buildExpression("<", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependFirstLtSecond);
        evaluateAndCheckResult(expr2, true, resolvedDependencies, ContainerState.ACTIVE);

        final SExpression expr3 = buildExpression("<", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependEquals);
        evaluateAndCheckResult(expr3, false, resolvedDependencies, ContainerState.ACTIVE);
    }

    @Test
    public void evaluateConditionExpressionLowerThanOrEquals() throws Exception {
        final SExpression expr1 = buildExpression("<=", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependFirstGtSecond);
        evaluateAndCheckResult(expr1, false, resolvedDependencies, ContainerState.ACTIVE);

        final SExpression expr2 = buildExpression("<=", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependFirstLtSecond);
        evaluateAndCheckResult(expr2, true, resolvedDependencies, ContainerState.ACTIVE);

        final SExpression expr3 = buildExpression("<=", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependEquals);
        evaluateAndCheckResult(expr3, true, resolvedDependencies, ContainerState.ACTIVE);
    }

    @Test
    public void evaluateConditionExpressionEquals() throws Exception {
        final SExpression expr1 = buildExpression("==", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependFirstGtSecond);
        evaluateAndCheckResult(expr1, false, resolvedDependencies, ContainerState.ACTIVE);

        final SExpression expr2 = buildExpression("==", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependFirstLtSecond);
        evaluateAndCheckResult(expr2, false, resolvedDependencies, ContainerState.ACTIVE);

        final SExpression expr3 = buildExpression("==", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependEquals);
        evaluateAndCheckResult(expr3, true, resolvedDependencies, ContainerState.ACTIVE);
    }

    @Test
    public void evaluateConditionExpressionDifferent() throws Exception {
        final SExpression expr1 = buildExpression("!=", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependFirstGtSecond);
        evaluateAndCheckResult(expr1, true, resolvedDependencies, ContainerState.ACTIVE);

        final SExpression expr2 = buildExpression("!=", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependFirstLtSecond);
        evaluateAndCheckResult(expr2, true, resolvedDependencies, ContainerState.ACTIVE);

        final SExpression expr3 = buildExpression("!=", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependEquals);
        evaluateAndCheckResult(expr3, false, resolvedDependencies, ContainerState.ACTIVE);
    }

    @Test
    public void evaluateConditionExpressionBooleanOperator() throws Exception {

        final SExpression booleanDependTrueExpr = buildExpression("!=", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependFirstGtSecond);
        final SExpression boleanOperatorExpr1 = buildExpression("!", SExpression.TYPE_CONDITION, Boolean.class.getName(), null,
                Collections.singletonList(booleanDependTrueExpr));
        evaluateAndCheckResult(boleanOperatorExpr1, false, Collections.<Integer, Object> singletonMap(booleanDependTrueExpr.getDiscriminant(), true),
                ContainerState.ACTIVE);

        final SExpression booleanDependFalseExpr = buildExpression("!=", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependEquals);
        final SExpression boleanOperatorExpr2 = buildExpression("!", SExpression.TYPE_CONDITION, Boolean.class.getName(), null,
                Collections.singletonList(booleanDependFalseExpr));
        evaluateAndCheckResult(boleanOperatorExpr2, true, Collections.<Integer, Object> singletonMap(booleanDependFalseExpr.getDiscriminant(), false),
                ContainerState.ACTIVE);
    }

    @Test
    public void evaluateConditionListOfExpressions() throws Exception {
        final SExpression expr1 = buildExpression("!=", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependFirstGtSecond);
        final SExpression expr2 = buildExpression("!=", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependFirstLtSecond);
        final SExpression expr3 = buildExpression("!=", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, dependEquals);
        final List<SExpression> expressions = new ArrayList<SExpression>(3);
        expressions.add(expr1);
        expressions.add(expr2);
        expressions.add(expr3);

        final List<Object> resolvedExpressions = evaluate(expressions, resolvedDependencies, ContainerState.ACTIVE);
        assertEquals(3, resolvedExpressions.size());
        assertTrue((Boolean) resolvedExpressions.get(0));
        assertTrue((Boolean) resolvedExpressions.get(1));
        assertFalse((Boolean) resolvedExpressions.get(2));
    }

    @Test
    public void compareNumericEqualsLongInteger() throws Exception {
        final SExpression unLong = buildExpression("1l", SExpression.TYPE_CONSTANT, Long.class.getName(), null, null);
        final SExpression unInteger = buildExpression("1", SExpression.TYPE_CONSTANT, Integer.class.getName(), null, null);
        compare(true, "==", unLong, unInteger, Long.valueOf(1l), Integer.valueOf(1));
    }

    @Test
    public void compareNumericEqualsIntegerLong() throws Exception {
        final SExpression unLong = buildExpression("1l", SExpression.TYPE_CONSTANT, Long.class.getName(), null, null);
        final SExpression unInteger = buildExpression("1", SExpression.TYPE_CONSTANT, Integer.class.getName(), null, null);
        compare(true, "==", unInteger, unLong, Integer.valueOf(1), Long.valueOf(1l));
    }

    @Test
    public void compareNumericGreaterLongInteger() throws Exception {
        final SExpression unLong = buildExpression("2l", SExpression.TYPE_CONSTANT, Long.class.getName(), null, null);
        final SExpression unInteger = buildExpression("1", SExpression.TYPE_CONSTANT, Integer.class.getName(), null, null);
        compare(true, ">=", unLong, unInteger, Long.valueOf(2l), Integer.valueOf(1));
    }

    @Test
    public void compareNumericGreaterDoubleLong() throws Exception {
        final SExpression aDouble = buildExpression("2.00d", SExpression.TYPE_CONSTANT, Double.class.getName(), null, null);
        final SExpression aLong = buildExpression("1l", SExpression.TYPE_CONSTANT, Long.class.getName(), null, null);
        compare(true, ">=", aDouble, aLong, Double.valueOf(2.00d), Long.valueOf(1l));
        compare(true, ">=", aDouble, aLong, 2.00d, 1l);
    }

    @Test
    public void compareNumericGreaterDoubleInteger() throws Exception {
        final SExpression aDouble = buildExpression("2.00d", SExpression.TYPE_CONSTANT, Double.class.getName(), null, null);
        final SExpression anInteger = buildExpression("1", SExpression.TYPE_CONSTANT, Integer.class.getName(), null, null);
        compare(true, ">=", aDouble, anInteger, Double.valueOf(2.00d), Integer.valueOf(1));
        compare(true, ">=", aDouble, anInteger, 2.00d, 1);
        compare(false, "<", aDouble, anInteger, Double.valueOf(2.00d), Integer.valueOf(1));
        compare(false, "<", aDouble, anInteger, 2.00d, 1);
    }

    @Test
    public void compareNumericGreaterFloatByte() throws Exception {
        final SExpression aFloat = buildExpression("2.00d", SExpression.TYPE_CONSTANT, Float.class.getName(), null, null);
        final SExpression aByte = buildExpression("1", SExpression.TYPE_CONSTANT, Byte.class.getName(), null, null);
        compare(true, ">=", aFloat, aByte, Float.valueOf(2.00f), (byte) 1);
        compare(true, ">=", aFloat, aByte, 2.00f, Byte.valueOf((byte) 1));
    }

    @Test
    public void compareNumericGreaterIntegerLong() throws Exception {
        final SExpression unLong = buildExpression("1l", SExpression.TYPE_CONSTANT, Long.class.getName(), null, null);
        final SExpression unInteger = buildExpression("2", SExpression.TYPE_CONSTANT, Integer.class.getName(), null, null);
        compare(true, ">=", unInteger, unLong, Integer.valueOf(2), Long.valueOf(1l));
    }

    @Test
    public void compareNumericNotEqualsLongInteger() throws Exception {
        final SExpression unLong = buildExpression("2l", SExpression.TYPE_CONSTANT, Long.class.getName(), null, null);
        final SExpression unInteger = buildExpression("1", SExpression.TYPE_CONSTANT, Integer.class.getName(), null, null);
        compare(true, "!=", unLong, unInteger, Long.valueOf(2l), Integer.valueOf(1));
    }

    @Test
    public void compareNumericNotEqualsIntegerLong() throws Exception {
        final SExpression unLong = buildExpression("2l", SExpression.TYPE_CONSTANT, Long.class.getName(), null, null);
        final SExpression unInteger = buildExpression("2", SExpression.TYPE_CONSTANT, Integer.class.getName(), null, null);
        compare(false, "!=", unInteger, unLong, Integer.valueOf(2), Long.valueOf(2l));
    }

    @Test
    public void compareNumericEqualsDoubleFloat() throws Exception {
        final SExpression aDouble = buildExpression("1.1d", SExpression.TYPE_CONSTANT, Double.class.getName(), null, null);
        final SExpression aFloat = buildExpression("1.1f", SExpression.TYPE_CONSTANT, Float.class.getName(), null, null);
        compare(true, "==", aDouble, aFloat, Double.valueOf(1.1d), Float.valueOf(1.1f));
    }

    @Test
    public void compareNumericEqualsFloatDouble() throws Exception {
        final SExpression unFloat = buildExpression("1.1d", SExpression.TYPE_CONSTANT, Double.class.getName(), null, null);
        final SExpression unInteger = buildExpression("1.1f", SExpression.TYPE_CONSTANT, Float.class.getName(), null, null);
        compare(true, "==", unInteger, unFloat, Float.valueOf(1.1f), Double.valueOf(1.1d));
    }

    @Test
    public void compareNumericGreaterDoubleFloat() throws Exception {
        final SExpression unDouble = buildExpression("2.1d", SExpression.TYPE_CONSTANT, Double.class.getName(), null, null);
        final SExpression unFloat = buildExpression("1.1f", SExpression.TYPE_CONSTANT, Float.class.getName(), null, null);
        compare(true, ">=", unDouble, unFloat, Double.valueOf(2.1d), Float.valueOf(1.1f));
    }

    @Test
    public void compareNumericGreaterFloatDouble() throws Exception {
        final SExpression unDouble = buildExpression("1.1d", SExpression.TYPE_CONSTANT, Double.class.getName(), null, null);
        final SExpression unFloat = buildExpression("2.1f", SExpression.TYPE_CONSTANT, Float.class.getName(), null, null);
        compare(true, ">=", unFloat, unDouble, Float.valueOf(2.1f), Double.valueOf(1.1d));
    }

    @Test
    public void compareNumericNotEqualsDoubleFloat() throws Exception {
        final SExpression unFloat = buildExpression("1.1d", SExpression.TYPE_CONSTANT, Double.class.getName(), null, null);
        final SExpression unInteger = buildExpression("1.1f", SExpression.TYPE_CONSTANT, Float.class.getName(), null, null);
        compare(true, "!=", unFloat, unInteger, Double.valueOf(1.2d), Float.valueOf(1.1f));
    }

    @Test
    public void compareNumericNotEqualsFloatDouble() throws Exception {
        final SExpression unFloat = buildExpression("1.1d", SExpression.TYPE_CONSTANT, Double.class.getName(), null, null);
        final SExpression unInteger = buildExpression("1.1f", SExpression.TYPE_CONSTANT, Float.class.getName(), null, null);
        compare(false, "!=", unInteger, unFloat, Float.valueOf(1.1f), Double.valueOf(1.1d));
    }

    @Test
    public void compareNumericFloatFloat() throws Exception {
        final SExpression unLong = buildExpression("1.1f", SExpression.TYPE_CONSTANT, Float.class.getName(), null, null);
        final SExpression unInteger = buildExpression("1.1f", SExpression.TYPE_CONSTANT, Float.class.getName(), null, null);
        compare(true, "==", unLong, unInteger, Float.valueOf(1.1f), Float.valueOf(1.1f));
    }

    @Test
    public void compareNumericIntegerInteger() throws Exception {
        final SExpression unLong = buildExpression("1", SExpression.TYPE_CONSTANT, Integer.class.getName(), null, null);
        final SExpression unInteger = buildExpression("1", SExpression.TYPE_CONSTANT, Integer.class.getName(), null, null);
        compare(true, "==", unLong, unInteger, Integer.valueOf(1), Integer.valueOf(1));
    }

    private void compare(final boolean result, final String operator, final SExpression exp1, final SExpression exp2, final Object exp1Value,
            final Object exp2Value) throws Exception {
        final Map<Integer, Object> evaluatedDependencies = new HashMap<Integer, Object>(2);
        evaluatedDependencies.put(exp1.hashCode(), exp1Value);
        evaluatedDependencies.put(exp2.hashCode(), exp2Value);
        evaluateAndCheckResult(buildExpression(operator, SExpression.TYPE_CONDITION, Boolean.class.getName(), null, Arrays.asList(exp1, exp2)), result,
                evaluatedDependencies, ContainerState.ACTIVE);
    }

    @Test(expected = SExpressionEvaluationException.class)
    public void compareWithWrongNumberOfDependencies() throws Exception {
        final SExpression exp1 = buildExpression("1", SExpression.TYPE_CONSTANT, Long.class.getName(), null, null);
        final SExpression exp2 = buildExpression("1", SExpression.TYPE_CONSTANT, Long.class.getName(), null, null);
        final SExpression exp3 = buildExpression("1", SExpression.TYPE_CONSTANT, Long.class.getName(), null, null);
        final Map<Integer, Object> evaluatedDependencies = new HashMap<Integer, Object>(3);
        evaluatedDependencies.put(exp1.hashCode(), 1l);
        evaluatedDependencies.put(exp2.hashCode(), 1l);
        evaluatedDependencies.put(exp3.hashCode(), 1l);
        evaluateAndCheckResult(buildExpression("==", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, Arrays.asList(exp1, exp2, exp3)), true,
                evaluatedDependencies, ContainerState.ACTIVE);
    }

    @Test
    public void compareWithStringWithLong() throws Exception {
        final SExpression unLong = buildExpression("1", SExpression.TYPE_CONSTANT, Long.class.getName(), null, null);
        final SExpression aString = buildExpression("tada", SExpression.TYPE_CONSTANT, String.class.getName(), null, null);
        compare(false, "==", unLong, aString, Integer.valueOf(1), "tada");
    }

    @Test
    public void evaluateNotOnBoolean() throws Exception {
        final SExpression trueBoolean = buildExpression("false", SExpression.TYPE_CONSTANT, Boolean.class.getName(), null, null);
        final Map<Integer, Object> evaluatedDependencies = new HashMap<Integer, Object>(1);
        evaluatedDependencies.put(trueBoolean.hashCode(), Boolean.FALSE);
        evaluateAndCheckResult(buildExpression("!", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, Arrays.asList(trueBoolean)), true,
                evaluatedDependencies, ContainerState.ACTIVE);
    }

    @Test(expected = SExpressionEvaluationException.class)
    public void evaluateNotOnString() throws Exception {
        final SExpression stringExpression = buildExpression("false", SExpression.TYPE_CONSTANT, String.class.getName(), null, null);
        final Map<Integer, Object> evaluatedDependencies = new HashMap<Integer, Object>(1);
        evaluatedDependencies.put(stringExpression.hashCode(), "false");
        evaluateAndCheckResult(buildExpression("!", SExpression.TYPE_CONDITION, Boolean.class.getName(), null, Arrays.asList(stringExpression)), true,
                evaluatedDependencies, ContainerState.ACTIVE);
    }
}
