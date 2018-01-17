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

import java.util.List;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SOperation;

/**
 * @author Zhang Bole
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @since 6.0
 */
public interface OperationService {

    /**
     * Execute the given operation in the given context and update data that are in the given data container
     * 
     * @param operation
     *            the operation to execute
     * @param dataContainerId
     *            the id of the data container (used for left operand)
     * @param dataContainerType
     *            the type of the data container (used for left operand)
     * @param expressionContext
     *            the context in which execute the operation
     * @throws SOperationExecutionException
     */
    void execute(SOperation operation, long dataContainerId, String dataContainerType, SExpressionContext expressionContext)
            throws SOperationExecutionException;

    /**
     * Execute the given operation in the given context and update data that are in the given data container
     * 
     * @param operations
     *            the operations to execute
     * @param leftOperandContainerId
     *            the id of the container (used for left operand)
     * @param leftOperandContainerType
     *            the type of the container (used for left operand)
     * @param expressionContext
     *            the context in which execute the operation
     * @throws SOperationExecutionException
     */
    void execute(List<SOperation> operations, long leftOperandContainerId, final String leftOperandContainerType, SExpressionContext expressionContext)
            throws SOperationExecutionException;

    /**
     * Execute the given operation in the given context and update data that are in the same context
     * 
     * @param operations
     * @param expressionContext
     * @throws SOperationExecutionException
     */
    void execute(List<SOperation> operations, SExpressionContext expressionContext) throws SOperationExecutionException;

}
