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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.SOperatorType;

/**
 * @author Zhang Bole
 */
public class OperationExecutorStrategyProvider {

    private final Map<String, OperationExecutorStrategy> operationStrategies;
    
    public OperationExecutorStrategyProvider(final List<OperationExecutorStrategy> operationExecutors) {
        operationStrategies = new HashMap<>(operationExecutors.size());
        for (final OperationExecutorStrategy operationExecutorStrategy : operationExecutors) {
            operationStrategies.put(operationExecutorStrategy.getOperationType(), operationExecutorStrategy);
        }
    }
    
    public OperationExecutorStrategy getOperationExecutorStrategy(final SOperation operation) throws SOperationExecutionException {
        final String operatorTypeName = getStrategyKey(operation);
        final OperationExecutorStrategy operationExecutorStrategy = operationStrategies.get(operatorTypeName);
        if (operationExecutorStrategy == null) {
            throw new SOperationExecutionException("Unable to find an executor for operation type " + operatorTypeName);
        }
        return operationExecutorStrategy;
    }

    protected String getStrategyKey(final SOperation operation) {
        String key = operation.getType().name();
        String leftOperandType = operation.getLeftOperand().getType();
        if (leftOperandType.equals(SLeftOperand.TYPE_BUSINESS_DATA) && key.equals(SOperatorType.ASSIGNMENT.name())) {
            key += "_" + leftOperandType;
        }
        return key;
    }

}
