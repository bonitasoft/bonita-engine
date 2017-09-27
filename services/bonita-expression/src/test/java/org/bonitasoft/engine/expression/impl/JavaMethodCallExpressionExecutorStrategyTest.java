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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.ExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class JavaMethodCallExpressionExecutorStrategyTest {

    private final JavaMethodCallExpressionExecutorStrategy methodCall = new JavaMethodCallExpressionExecutorStrategy();

    private Order order;

    private SExpression orderDep;

    private Map<Integer, Object> orderResolvedExp;

    private List<Integer> list;

    private SExpression listDep;

    private Map<Integer, Object> listResolvedExp;

    @Before
    public void setUp() {
        list = new ArrayList<Integer>(2);
        list.add(1);
        list.add(2);
        listDep = new SExpressionImpl("dep", "myValues", ExpressionExecutorStrategy.TYPE_VARIABLE, List.class.getName(), null, null);
        listResolvedExp = new HashMap<Integer, Object>(1);
        listResolvedExp.put(listDep.getDiscriminant(), list);

        order = new Order("32, rue Gustave Eiffel - 38500 - Grenoble", 123L);
        orderDep = new SExpressionImpl("dep", "order", ExpressionExecutorStrategy.TYPE_VARIABLE, Order.class.getName(), null, null);
        orderResolvedExp = new HashMap<Integer, Object>(1);
        orderResolvedExp.put(orderDep.getDiscriminant(), order);
    }

    @Test
    public void testJavaMethodCallOfSuperClass() throws Exception {
        final SExpression expression = new SExpressionImpl("exp1", "toString", ExpressionExecutorStrategy.TYPE_JAVA_METHOD_CALL, String.class.getName(), null,
                Collections.singletonList(listDep));

        final Object result = methodCall.evaluate(expression, Collections.<String, Object> emptyMap(), listResolvedExp, ContainerState.ACTIVE);
        assertEquals(list.toString(), result);
    }

    @Test
    public void testCallMethodOnClientObjectReturningString() throws Exception {
        final SExpression expression = new SExpressionImpl("exp1", "getShippingAddress", ExpressionExecutorStrategy.TYPE_JAVA_METHOD_CALL,
                String.class.getName(), null, Collections.singletonList(orderDep));

        final Object result = methodCall.evaluate(expression, Collections.<String, Object> emptyMap(), orderResolvedExp, ContainerState.ACTIVE);
        assertEquals(order.getShippingAddress(), result);
    }

    @Test
    public void testCallMethodOnClientObjectReturningLong() throws Exception {
        final SExpression expression = new SExpressionImpl("exp1", "getReferenceNumber", ExpressionExecutorStrategy.TYPE_JAVA_METHOD_CALL,
                Long.class.getName(), null, Collections.singletonList(orderDep));

        final Object result = methodCall.evaluate(expression, Collections.<String, Object> emptyMap(), orderResolvedExp, ContainerState.ACTIVE);
        assertEquals(order.getReferenceNumber(), result);
    }

    @Test
    public void testEvaluateListOfExpressions() throws Exception {
        final List<SExpression> expressions = new ArrayList<SExpression>(2);
        expressions.add(new SExpressionImpl("exp1", "getShippingAddress", ExpressionExecutorStrategy.TYPE_JAVA_METHOD_CALL, String.class.getName(), null,
                Collections.singletonList(orderDep)));
        expressions.add(new SExpressionImpl("exp1", "getReferenceNumber", ExpressionExecutorStrategy.TYPE_JAVA_METHOD_CALL, Long.class.getName(), null,
                Collections.singletonList(orderDep)));

        final List<Object> result = methodCall.evaluate(expressions, Collections.<String, Object> emptyMap(), orderResolvedExp, ContainerState.ACTIVE);
        assertEquals(order.getShippingAddress(), result.get(0));
        assertEquals(order.getReferenceNumber(), result.get(1));
    }

    @Test(expected = SInvalidExpressionException.class)
    public void testDepencenciesCannotBeNull() throws Exception {
        final SExpression expression = new SExpressionImpl("exp1", "getReferenceNumber", ExpressionExecutorStrategy.TYPE_JAVA_METHOD_CALL,
                Long.class.getName(), null, null);

        methodCall.validate(expression);
    }

    @Test(expected = SInvalidExpressionException.class)
    public void testDepencenciesCannotBeEmpty() throws Exception {
        final SExpression expression = new SExpressionImpl("exp1", "getReferenceNumber", ExpressionExecutorStrategy.TYPE_JAVA_METHOD_CALL,
                Long.class.getName(), null, Collections.<SExpression> emptyList());
        methodCall.validate(expression);
    }

    @Test(expected = SInvalidExpressionException.class)
    public void testDepencenciesCannotHasMoreThanOneElement() throws Exception {
        final List<SExpression> dependencies = new ArrayList<SExpression>(2);
        dependencies.add(orderDep);
        dependencies.add(listDep);

        final SExpression expression = new SExpressionImpl("exp1", "getReferenceNumber", ExpressionExecutorStrategy.TYPE_JAVA_METHOD_CALL,
                Long.class.getName(), null, dependencies);

        methodCall.validate(expression);
    }

    @Test(expected = SInvalidExpressionException.class)
    public void testExpressionCannotBeNull() throws Exception {
        methodCall.validate(null);
    }

}
