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
package org.bonitasoft.engine.core.operation;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * 
 * Tells the engine how to retrieve and update a left operand having the specified type
 * 
 * e.g. a data left operand handler will get data from database and set the data back in database
 * 
 * @author Baptiste Mesta
 * 
 */
public interface LeftOperandHandler {

    String getType();

    /**
     * 
     * @param sLeftOperand
     * @param inputValues
     *            contains value(s) given by the strategy to udpdate the left operand with
     * @param containerId
     * @param containerType
     * @throws SOperationExecutionException
     */
    // TODO batch method
    void update(SLeftOperand sLeftOperand, Object newValue, long containerId, String containerType) throws SOperationExecutionException;

    /**
     * 
     * @param sLeftOperand
     * @param expressionContext
     * @return
     *         objects retrieved by this handler to be put in the context for further updates
     * @throws SBonitaReadException
     */
    // TODO batch method
    Object retrieve(SLeftOperand sLeftOperand, SExpressionContext expressionContext) throws SBonitaReadException;
}
