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
package org.bonitasoft.engine.core.operation.impl;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;

/**
 * AssignmentOperationExecutorStrategy is the default Bonita strategy to execute data assignment operations
 * 
 * @author Zhang Bole
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class AssignmentOperationExecutorStrategy extends UpdateDataOperationExecutorStrategy {

    /**
     * The Operation type of this strategy, as a String
     */
    public static final String TYPE_ASSIGNMENT = "ASSIGNMENT";

    /**
     * Builds a new AssignmentOperationExecutorStrategy, which is the strategy to execute data assignment operations
     * 
     * @param dataInstanceService
     *            how to access to the data
     */
    public AssignmentOperationExecutorStrategy(final DataInstanceService dataInstanceService) {
        super(dataInstanceService);
    }

    @Override
    public Object getValue(final SOperation operation, final Object value, final long containerId, final String containerType,
            final SExpressionContext expressionContext) throws SOperationExecutionException {
        // do not check if value is external, see ENGINE-1739
        if (!operation.getLeftOperand().isExternal()) {
            checkReturnType(value, operation, expressionContext);
        }
        // no processing on the value, just return it
        return value;
    }

    private void checkReturnType(final Object value, final SOperation operation, final SExpressionContext expressionContext)
            throws SOperationExecutionException {
        if (value != null) {
            final String name = operation.getLeftOperand().getName();
            final Object object = expressionContext.getInputValues().get(name);
            /*
             * if the object is null (data is not initialized) the return type is not checked
             * but the data instance service should throw an exception
             */
            if (object != null) {
                final Class<?> dataEffectiveType = object.getClass();
                final Class<?> evaluatedReturnedType = value.getClass();
                if (!(dataEffectiveType.isAssignableFrom(evaluatedReturnedType) || dataEffectiveType.equals(evaluatedReturnedType))) {
                    throw new SOperationExecutionException("Incompatible assignment operation type: Left operand " + dataEffectiveType
                            + " is not compatible with right operand " + evaluatedReturnedType + " for expression with name '" + expressionContext + "'");
                }
            }
        }
    }

    @Override
    public String getOperationType() {
        return TYPE_ASSIGNMENT;
    }
}
