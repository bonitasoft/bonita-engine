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
package org.bonitasoft.engine.expression;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class ExpressionBuilderTest {

    // FIXME : Split the following tests in unit tests

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
    public void invalidOperator() throws Exception {
        new ExpressionBuilder().createComparisonExpression("comp1", intExpr, "||", intExpr);
    }

    @Test
    public void validReturnTypesForBinaryOperator() throws Exception {
        new ExpressionBuilder().createComparisonExpression("comp1", intExpr, ComparisonOperator.GREATER_THAN, longExpr);
    }

    @Test(expected = InvalidExpressionException.class)
    public void invalidReturnTypesForBinaryOperator() throws Exception {
        new ExpressionBuilder().createComparisonExpression("comp1", intExpr, ComparisonOperator.GREATER_THAN, string);
    }

    @Test(expected = InvalidExpressionException.class)
    public void invalidReturnTypeForUnaryOperator() throws Exception {
        new ExpressionBuilder().createLogicalComplementExpression("complement", intExpr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setReturnTypeShouldForbidPrimitiveCharType() throws Exception {
        new ExpressionBuilder().setReturnType(char.class.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setReturnTypeShouldForbidPrimitiveByteType() throws Exception {
        new ExpressionBuilder().setReturnType(byte.class.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setReturnTypeShouldForbidPrimitiveLongType() throws Exception {
        new ExpressionBuilder().setReturnType(long.class.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setReturnTypeShouldForbidPrimitiveIntType() throws Exception {
        new ExpressionBuilder().setReturnType(int.class.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setReturnTypeShouldForbidPrimitiveFloatType() throws Exception {
        new ExpressionBuilder().setReturnType(float.class.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setReturnTypeShouldForbidPrimitiveDoubleType() throws Exception {
        new ExpressionBuilder().setReturnType(double.class.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setReturnTypeShouldForbidPrimitiveShortType() throws Exception {
        new ExpressionBuilder().setReturnType(short.class.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setReturnTypeShouldForbidPrimitiveBooleanType() throws Exception {
        new ExpressionBuilder().setReturnType(boolean.class.getName());
    }

    @Test
    public void setReturnTypeShouldAllowNonPrimitiveBooleanType() throws Exception {
        new ExpressionBuilder().createNewInstance("someName").setReturnType(Boolean.class.getName());
    }

    @Test
    public void createContractInputExpressionShouldConstructARightExpression() throws Exception {
        final Expression expression = new ExpressionBuilder().createContractInputExpression("comment", String.class.getName());
        assertThat(expression.getName()).isEqualTo("comment");
        assertThat(expression.getContent()).isEqualTo("comment");
        assertThat(expression.getReturnType()).isEqualTo(String.class.getName());
        assertThat(expression.getDependencies()).isEmpty();
        assertThat(expression.getExpressionType()).isEqualTo(ExpressionType.TYPE_CONTRACT_INPUT.toString());
        assertThat(expression.getInterpreter()).isNull();
    }

}
