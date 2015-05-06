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
package org.bonitasoft.engine.core.operation;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SOperation;

/**
 * @author Zhang Bole
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public interface OperationExecutorStrategy {

    /**
     * Calculate the new value of the left operand base of right operand expression value
     *
     * @param operation the operation in progress
     * @param rightOperandValue
     *        result of the evaluation of right operand expression
     * @param expressionContext the expression context
     * @param shouldPersistValue true if the right operand must be persisted (Business Data)
     * @return
     *         the new value to set the left operand with
     * @throws SOperationExecutionException
     */
    Object computeNewValueForLeftOperand(SOperation operation, Object rightOperandValue, SExpressionContext expressionContext, final boolean shouldPersistValue)
            throws SOperationExecutionException;

    String getOperationType();

}
