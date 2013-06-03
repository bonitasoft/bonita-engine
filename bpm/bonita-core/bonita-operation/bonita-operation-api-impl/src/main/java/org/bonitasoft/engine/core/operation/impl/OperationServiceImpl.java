/**
 * Copyright (C) 2011 BonitaSoft S.A.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationExecutorStrategy;
import org.bonitasoft.engine.core.operation.OperationExecutorStrategyProvider;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.expression.model.SExpression;

/**
 * @author Zhang Bole
 * @author Elias Ricken de Medeiros
 */
public class OperationServiceImpl implements OperationService {

    private final Map<String, OperationExecutorStrategy> operationExecutorsMap;

    private final ExpressionResolverService expressionResolverService;

    public OperationServiceImpl(final OperationExecutorStrategyProvider operationExecutorStrategyProvider,
            final ExpressionResolverService expressionResolverService) {
        super();
        this.expressionResolverService = expressionResolverService;
        final List<OperationExecutorStrategy> expressionExecutors = operationExecutorStrategyProvider.getOperationExecutors();
        operationExecutorsMap = new HashMap<String, OperationExecutorStrategy>(expressionExecutors.size());
        for (final OperationExecutorStrategy operationExecutorStrategy : expressionExecutors) {
            operationExecutorsMap.put(operationExecutorStrategy.getOperationType(), operationExecutorStrategy);
        }
    }

    private OperationExecutorStrategy getOperationExecutorStrategy(final SOperation operation) throws SOperationExecutionException {
        final String operatorTypeName = operation.getType().name();
        final OperationExecutorStrategy operationExecutorStrategy = operationExecutorsMap.get(operatorTypeName);
        if (operationExecutorStrategy == null) {
            throw new SOperationExecutionException("Unable to find an executor for operation type " + operatorTypeName);
        }
        return operationExecutorStrategy;
    }

    @Override
    public void execute(final SOperation operation, final long dataContainerId, final String dataContainerType, final SExpressionContext expressionContext)
            throws SOperationExecutionException {
        final SExpression rightOperand = operation.getRightOperand();
        if (rightOperand != null) {// ignore operation if there is no right operand
            final Object operationValue = getOperationValue(operation, expressionContext, rightOperand);
            final OperationExecutorStrategy operationExecutorStrategy = getOperationExecutorStrategy(operation);
            operationExecutorStrategy.execute(operation, operationValue, dataContainerId, dataContainerType, expressionContext);
        }
    }

    @Override
    public void execute(final SOperation operation, final SExpressionContext expressionContext) throws SOperationExecutionException {
        execute(operation, expressionContext.getContainerId(), expressionContext.getContainerType(), expressionContext);
    }

    protected Object getOperationValue(final SOperation operation, final SExpressionContext expressionContext, final SExpression sExpression)
            throws SOperationExecutionException {
        Object expressionResult;
        try {
            expressionResult = expressionResolverService.evaluate(sExpression, expressionContext);
        } catch (final ClassCastException e) {
            throw new SOperationExecutionException("Trying to set variable " + operation.getLeftOperand().getName() + " a value which is not Serializable", e);
        } catch (final SBonitaException e) {
            throw new SOperationExecutionException(e);
        }
        return expressionResult;
    }
}
