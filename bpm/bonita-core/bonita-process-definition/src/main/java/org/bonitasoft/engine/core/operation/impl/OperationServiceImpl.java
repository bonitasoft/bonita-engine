/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
 **/
package org.bonitasoft.engine.core.operation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.LeftOperandHandler;
import org.bonitasoft.engine.core.operation.OperationExecutorStrategy;
import org.bonitasoft.engine.core.operation.OperationExecutorStrategyProvider;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.SOperatorType;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.springframework.stereotype.Service;

/**
 * @author Zhang Bole
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @author Colin Puy
 * @author Matthieu Chaffotte
 */
@Service("operationService")
public class OperationServiceImpl implements OperationService {

    private final Map<String, LeftOperandHandler> leftOperandHandlersMap;
    private final ExpressionResolverService expressionResolverService;
    private final PersistRightOperandResolver persistRightOperandResolver;
    private final TechnicalLoggerService logger;
    private final OperationExecutorStrategyProvider operationExecutorStrategyProvider;

    public OperationServiceImpl(OperationExecutorStrategyProvider operationExecutorStrategyProvider,
            List<LeftOperandHandler> leftOperandHandlers,
            ExpressionResolverService expressionResolverService,
            PersistRightOperandResolver persistRightOperandResolver,
            TechnicalLoggerService logger) {
        super();
        this.operationExecutorStrategyProvider = operationExecutorStrategyProvider;
        this.expressionResolverService = expressionResolverService;
        this.persistRightOperandResolver = persistRightOperandResolver;
        this.logger = logger;
        leftOperandHandlersMap = new HashMap<>(leftOperandHandlers.size());
        for (final LeftOperandHandler leftOperandHandler : leftOperandHandlers) {
            leftOperandHandlersMap.put(leftOperandHandler.getType(), leftOperandHandler);
        }
    }

    @Override
    public void execute(final SOperation operation, final long containerId, final String containerType,
            final SExpressionContext expressionContext)
            throws SOperationExecutionException {
        execute(Arrays.asList(operation), containerId, containerType, expressionContext);
    }

    @Override
    public void execute(final List<SOperation> operations, final SExpressionContext expressionContext)
            throws SOperationExecutionException {
        execute(operations, expressionContext.getContainerId(), expressionContext.getContainerType(),
                expressionContext);
    }

    @Override
    public void execute(final List<SOperation> operations, final long leftOperandContainerId,
            final String leftOperandContainerType,
            final SExpressionContext expressionContext) throws SOperationExecutionException {
        if (operations.isEmpty()) {
            return;
        }
        // retrieve all left operand to set and put it in context
        retrieveLeftOperandsAndPutItInExpressionContextIfNotIn(operations, leftOperandContainerId,
                leftOperandContainerType, expressionContext);

        // execute operation and put it in context again
        final Map<SLeftOperand, LeftOperandUpdateStatus> leftOperandUpdates = executeOperators(operations,
                expressionContext);
        // update data
        updateLeftOperands(leftOperandUpdates, leftOperandContainerId, leftOperandContainerType, expressionContext);
    }

    Map<SLeftOperand, LeftOperandUpdateStatus> executeOperators(final List<SOperation> operations,
            final SExpressionContext expressionContext)
            throws SOperationExecutionException {
        final Map<SLeftOperand, LeftOperandUpdateStatus> leftOperandsToUpdate = new HashMap<>();
        for (int i = 0; i < operations.size(); i++) {
            final SOperation currentOperation = operations.get(i);
            final boolean shouldPersistValue = persistRightOperandResolver.shouldPersistByPosition(i, operations);
            final LeftOperandUpdateStatus currentUpdateStatus = calculateRightOperandValue(currentOperation,
                    expressionContext, shouldPersistValue);
            if (shouldUpdateLeftOperandContext(leftOperandsToUpdate, currentOperation.getLeftOperand(),
                    currentUpdateStatus)) {
                leftOperandsToUpdate.put(currentOperation.getLeftOperand(), currentUpdateStatus);
            }
        }
        return leftOperandsToUpdate;
    }

    private LeftOperandUpdateStatus calculateRightOperandValue(final SOperation operation,
            final SExpressionContext expressionContext,
            boolean shouldPersistValue)
            throws SOperationExecutionException {
        final SLeftOperand leftOperand = operation.getLeftOperand();
        final LeftOperandUpdateStatus currentUpdateStatus = new LeftOperandUpdateStatus(operation.getType());
        if (currentUpdateStatus.shouldUpdate()) {
            final OperationExecutorStrategy operationExecutorStrategy = operationExecutorStrategyProvider
                    .getOperationExecutorStrategy(operation);
            final Object rightOperandValue = evaluateRightOperandExpression(operation, expressionContext,
                    operation.getRightOperand());
            shouldPersistValue = persistRightOperandResolver
                    .shouldPersistByValue(rightOperandValue, shouldPersistValue,
                            operationExecutorStrategy.shouldPersistOnNullValue());
            final Object value = operationExecutorStrategy.computeNewValueForLeftOperand(operation, rightOperandValue,
                    expressionContext,
                    shouldPersistValue);
            expressionContext.getInputValues().put(leftOperand.getName(), value);
            logOperation(TechnicalLogSeverity.DEBUG, operation, rightOperandValue, expressionContext);
        }
        return currentUpdateStatus;
    }

    boolean shouldUpdateLeftOperandContext(final Map<SLeftOperand, LeftOperandUpdateStatus> updateLeftOperands,
            final SLeftOperand leftOperand,
            final LeftOperandUpdateStatus currentUpdateStatus) {
        final LeftOperandUpdateStatus previousStatus = updateLeftOperands.get(leftOperand);
        return previousStatus == null || !previousStatus.shouldDelete() && currentUpdateStatus.shouldDelete();
    }

    void updateLeftOperands(final Map<SLeftOperand, LeftOperandUpdateStatus> leftOperandUpdates,
            final long leftOperandContainerId,
            final String leftOperandContainerType, final SExpressionContext expressionContext)
            throws SOperationExecutionException {
        for (final Entry<SLeftOperand, LeftOperandUpdateStatus> update : leftOperandUpdates.entrySet()) {
            final SLeftOperand leftOperand = update.getKey();
            final LeftOperandHandler leftOperandHandler = getLeftOperandHandler(leftOperand.getType());
            if (update.getValue().shouldUpdate()) {
                leftOperandHandler.update(leftOperand, expressionContext.getInputValues(),
                        expressionContext.getInputValues().get(leftOperand.getName()),
                        leftOperandContainerId, leftOperandContainerType);
            } else {
                leftOperandHandler.delete(leftOperand, leftOperandContainerId, leftOperandContainerType);
            }
        }
    }

    private LeftOperandHandler getLeftOperandHandler(final String type) throws SOperationExecutionException {
        final LeftOperandHandler leftOperandHandler = leftOperandHandlersMap.get(type);
        if (leftOperandHandler == null) {
            throw new SOperationExecutionException("Left operand type not found: " + type);
        }
        return leftOperandHandler;
    }

    void retrieveLeftOperandsAndPutItInExpressionContextIfNotIn(final List<SOperation> operations,
            final long leftOperandContainerId,
            final String leftOperandContainerType,
            final SExpressionContext expressionContext) throws SOperationExecutionException {

        //if the container where we execute the operation is not the same than the container of the expression (call activity data mapping) we skip the loading of left operand
        final String containerType = expressionContext.getContainerType();
        final Long containerId = expressionContext.getContainerId();
        if (containerId == null || containerType == null) {
            return;
        }
        final HashMap<String, List<SLeftOperand>> leftOperandHashMap = new HashMap<>();
        for (final SOperation operation : operations) {
            if (!operation.getType().equals(SOperatorType.ASSIGNMENT)) {
                // this operation will set a data, we retrieve it and put it in context
                final SLeftOperand leftOperand = operation.getLeftOperand();
                if (!leftOperandHashMap.containsKey(leftOperand.getType())) {
                    leftOperandHashMap.put(leftOperand.getType(), new ArrayList<>());
                }
                leftOperandHashMap.get(leftOperand.getType()).add(leftOperand);
            }
        }
        for (final Entry<String, List<SLeftOperand>> leftOperandByType : leftOperandHashMap.entrySet()) {
            try {
                getLeftOperandHandler(leftOperandByType.getKey())
                        .loadLeftOperandInContext(leftOperandByType.getValue(), leftOperandContainerId,
                                leftOperandContainerType, expressionContext);
            } catch (final SBonitaReadException e) {
                throw new SOperationExecutionException(
                        "Unable to retrieve value for operation " + leftOperandByType.getValue(), e);
            }
        }

    }

    protected Object evaluateRightOperandExpression(final SOperation operation,
            final SExpressionContext expressionContext, final SExpression sExpression)
            throws SOperationExecutionException {
        if (sExpression == null) {
            return null;
        }
        try {
            return expressionResolverService.evaluate(sExpression, expressionContext);
        } catch (final ClassCastException e) {
            throw new SOperationExecutionException(
                    "Unable to execute operation on " + operation.getLeftOperand().getName()
                            + " with a new value which is not Serializable",
                    e);
        } catch (final SBonitaException e) {
            throw new SOperationExecutionException(e);
        }
    }

    private void logOperation(final TechnicalLogSeverity severity, final SOperation operation,
            final Object operationValue,
            final SExpressionContext expressionContext) {
        if (logger.isLoggable(this.getClass(), severity)) {
            final String message = buildLogMessage(operation, operationValue, expressionContext);
            logger.log(this.getClass(), severity, message);
        }
    }

    private String buildLogMessage(final SOperation operation, final Object operationValue,
            final SExpressionContext expressionContext) {
        String stb = "Executed operation on container [id: '" +
                expressionContext.getContainerId() +
                "', type: '" +
                expressionContext.getContainerType() +
                "']. Operation: [left operand: '" +
                operation.getLeftOperand().getName() +
                "', operator: '" +
                operation.getOperator() +
                "', operation value: '" +
                operationValue +
                "']";
        return stb;
    }

}
