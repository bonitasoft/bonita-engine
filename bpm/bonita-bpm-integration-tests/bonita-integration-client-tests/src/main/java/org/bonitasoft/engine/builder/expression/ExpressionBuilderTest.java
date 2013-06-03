/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.builder.expression;

import org.bonitasoft.engine.expression.ComparisonOperator;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class ExpressionBuilderTest {

    private Expression longExpr;

    private Expression intExpr;

    private Expression string;

    @Before
    public void initializeExpressions() throws Exception {
        longExpr = new ExpressionBuilder().createConstantLongExpression(1);
        intExpr = new ExpressionBuilder().createConstantIntegerExpression(1);
        string = new ExpressionBuilder().createConstantStringExpression("string");
    }

    @Test(expected = InvalidExpressionException.class)
    public void testInvalidOperator() throws Exception {
        new ExpressionBuilder().createComparisonExpression("comp1", intExpr, "||", intExpr);
    }

    @Test
    public void testValidReturnTypesForBinaryOperator() throws Exception {
        new ExpressionBuilder().createComparisonExpression("comp1", intExpr, ComparisonOperator.GREATER_THAN, longExpr);
    }

    @Test(expected = InvalidExpressionException.class)
    public void testInvalidReturnTypesForBinaryOperator() throws Exception {
        new ExpressionBuilder().createComparisonExpression("comp1", intExpr, ComparisonOperator.GREATER_THAN, string);
    }

    @Test(expected = InvalidExpressionException.class)
    public void testInvalidReturnTypeForUnaryOperator() throws Exception {
        new ExpressionBuilder().createLogicalComplementExpression("complement", intExpr);
    }

}
