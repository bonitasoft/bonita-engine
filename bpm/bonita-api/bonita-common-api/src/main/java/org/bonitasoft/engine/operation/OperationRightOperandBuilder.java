/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
 ** 
 * @since 6.2
 */
package org.bonitasoft.engine.operation;

import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;

/**
 * @author Baptiste Mesta
 * 
 */
public class OperationRightOperandBuilder {

    private final OperationBuilder operationBuilder;

    /**
     * @param done
     */
    public OperationRightOperandBuilder(final OperationBuilder operationBuilder) {
        this.operationBuilder = operationBuilder;
    }

    /**
     * 
     */
    public OperationOperatorBuilder with(final Expression expression) {
        return new OperationOperatorBuilder(operationBuilder.setRightOperand(expression));
    }

    public OperationOperatorBuilder with(final String constantString) {
        try {
            return with(new ExpressionBuilder().createConstantStringExpression(constantString));
        } catch (InvalidExpressionException e) {
            throw new IllegalStateException(e);
        }
    }
}
