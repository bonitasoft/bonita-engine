/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;

/**
 * @author Zhang Bole
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public interface OperationExecutorStrategy {

    /**
     * @param operation
     * @param value
     * @param containerId
     * @param containerType
     * @param expressionContext
     * @return
     * @throws SOperationExecutionException
     */
    Object getValue(SOperation operation, Object value, long containerId, String containerType, SExpressionContext expressionContext)
            throws SOperationExecutionException;

    /**
     * Update the object according to
     * 
     * @param sLeftOperand
     * @param newValue
     * @param containerId
     * @param containerType
     * @throws SOperationExecutionException
     */
    void update(SLeftOperand sLeftOperand, Object newValue, long containerId, String containerType) throws SOperationExecutionException;

    /**
     * Returns the type of the operation which identifies the strategy among all.
     * 
     * @return the operation type
     */
    String getOperationType();

    boolean shouldPerformUpdateAtEnd();

}
