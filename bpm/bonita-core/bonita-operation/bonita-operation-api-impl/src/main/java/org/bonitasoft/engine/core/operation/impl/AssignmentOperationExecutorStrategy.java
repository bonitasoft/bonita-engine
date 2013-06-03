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
import org.bonitasoft.engine.core.operation.OperationExecutorStrategy;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilder;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilders;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * AssignmentOperationExecutorStrategy is the default Bonita strategy to execute data assignment operations
 * 
 * @author Zhang Bole
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 */
public class AssignmentOperationExecutorStrategy implements OperationExecutorStrategy {

    /**
     * The Operation type of this strategy, as a String
     */
    public static final String TYPE_ASSIGNMENT = "ASSIGNMENT";

    private final DataInstanceService dataInstanceService;

    private final SDataInstanceBuilders sDataInstanceBuilders;

    /**
     * Builds a new AssignmentOperationExecutorStrategy, which is the strategy to execute data assignment operations
     * 
     * @param dataInstanceService
     *            how to access to the data
     * @param sDataInstanceBuilders
     *            <code>SDataInstanceBuilders</code> to build the updateDescriptor when updating the data
     */
    public AssignmentOperationExecutorStrategy(final DataInstanceService dataInstanceService, final SDataInstanceBuilders sDataInstanceBuilders) {
        this.dataInstanceService = dataInstanceService;
        this.sDataInstanceBuilders = sDataInstanceBuilders;
    }

    private void updateDataInstance(final SOperation operation, final long containerId, final String containerType, final Object expressionResult)
            throws SOperationExecutionException {
        final String dataInstanceName = operation.getLeftOperand().getName();
        SDataInstance sDataInstance;
        try {
            sDataInstance = dataInstanceService.getDataInstance(dataInstanceName, containerId, containerType);
            checkReturnType(sDataInstance.getClassName(), expressionResult, operation.getRightOperand().getName());

            final EntityUpdateDescriptor updateDescriptor = getUpdateDescriptor(expressionResult);
            dataInstanceService.updateDataInstance(sDataInstance, updateDescriptor);
        } catch (final SDataInstanceException e) {
            throw new SOperationExecutionException(e);
        }
    }

    private void checkReturnType(final String className, final Object result, final String rightOperandExpressionName) throws SOperationExecutionException {
        if (result != null) {
            try {
                final Class<?> dataDeclaredType = Thread.currentThread().getContextClassLoader().loadClass(className);
                final Class<?> evaluatedReturnedType = result.getClass();
                if (!dataDeclaredType.isAssignableFrom(evaluatedReturnedType)) {
                    throw new SOperationExecutionException("Incompatible assignment operation type: Left operand " + dataDeclaredType
                            + " is not compatible with right operand " + evaluatedReturnedType + " for expression with name '" + rightOperandExpressionName
                            + "'");
                }
            } catch (final ClassNotFoundException e) {
                throw new SOperationExecutionException("Declared return type unknown: " + className + " for expression " + rightOperandExpressionName, e);
            }
        }
    }

    @Override
    public void execute(final SOperation operation, final Object value, final long containerId, final String containerType,
            final SExpressionContext expressionContext) throws SOperationExecutionException {
        // Let's update the value if the variable to set is not external:
        if (!operation.getLeftOperand().isExternal()) {
            updateDataInstance(operation, containerId, containerType, value);
        } else {
            // set the new value of the external data in the list of input values:
            expressionContext.getInputValues().put(operation.getLeftOperand().getName(), value);
        }
    }

    private EntityUpdateDescriptor getUpdateDescriptor(final Object newValue) {
        // update data instance value
        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        final SDataInstanceBuilder sDataInstanceBuilder = sDataInstanceBuilders.getDataInstanceBuilder();
        updateDescriptor.addField(sDataInstanceBuilder.getValueKey(), newValue);
        return updateDescriptor;
    }

    @Override
    public String getOperationType() {
        return TYPE_ASSIGNMENT;
    }
}
