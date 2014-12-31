/**
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.util.Map;
import java.util.List;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * Tells the engine how to retrieve and update a left operand having the specified type
 * e.g. a data left operand handler will get data from database and set the data back in database
 *
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public interface LeftOperandHandler {

    String getType();

    /**
     * Checks whether the handler supports batch update after executing all operations
     *
     * @return true if the handler supports batch update after executing all operations; false otherwise
     */
    boolean supportBatchUpdate();

    /**
     * @param inputValues   contains value(s) given by the strategy to update the left operand with
     * @param sLeftOperand  the left operand
     * @param newValue      the value to set the element with
     * @param containerId   the container id
     * @param containerType the container type    @throws SOperationExecutionException
     */
    // TODO batch method
    Object update(SLeftOperand sLeftOperand, Map<String, Object> inputValues, Object newValue, long containerId, String containerType) throws SOperationExecutionException;

    void delete(SLeftOperand sLeftOperand, long containerId, String containerType) throws SOperationExecutionException;

    /**
     * retrieve the left operand and put it in context as needed by the left operand
     *
     * @param sLeftOperand      the left operand
     * @param expressionContext the expression context
     * @param contextToSet      the context to add the value in
     * @throws SBonitaReadException
     */
    // TODO batch method
    void loadLeftOperandInContext(SLeftOperand sLeftOperand, SExpressionContext expressionContext, Map<String, Object> contextToSet) throws SBonitaReadException;

    void loadLeftOperandInContext(List<SLeftOperand> sLeftOperandList, SExpressionContext expressionContext, Map<String, Object> contextToSet) throws SBonitaReadException;
}
