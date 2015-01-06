/**
 * Copyright (C) 2011, 2014 Bonitasoft S.A.
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
import org.bonitasoft.engine.core.operation.LeftOperandHandlerProvider;
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

/**
 * @author Zhang Bole
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @author Colin Puy
 * @author Matthieu Chaffotte
 */
public class OperationServiceImpl implements OperationService {

    private final Map<String, LeftOperandHandler> leftOperandHandlersMap;

    private final ExpressionResolverService expressionResolverService;

    private final TechnicalLoggerService logger;

    private final OperationExecutorStrategyProvider operationExecutorStrategyProvider;

    public OperationServiceImpl(final OperationExecutorStrategyProvider operationExecutorStrategyProvider,
            final LeftOperandHandlerProvider leftOperandHandlerProvider, final ExpressionResolverService expressionResolverService,
            final TechnicalLoggerService logger) {
        super();
        this.operationExecutorStrategyProvider = operationExecutorStrategyProvider;
        this.expressionResolverService = expressionResolverService;
        this.logger = logger;
        final List<LeftOperandHandler> leftOperandHandlers = leftOperandHandlerProvider.getLeftOperandHandlers();
        leftOperandHandlersMap = new HashMap<String, LeftOperandHandler>(leftOperandHandlers.size());
        for (final LeftOperandHandler leftOperandHandler : leftOperandHandlers) {
            leftOperandHandlersMap.put(leftOperandHandler.getType(), leftOperandHandler);
        }
    }

    @Override
    public void execute(final SOperation operation, final long containerId, final String containerType, final SExpressionContext expressionContext)
            throws SOperationExecutionException {
        execute(Arrays.asList(operation), containerId, containerType, expressionContext);
    }

    @Override
    public void execute(final List<SOperation> operations, final SExpressionContext expressionContext) throws SOperationExecutionException {
        execute(operations, expressionContext.getContainerId(), expressionContext.getContainerType(), expressionContext);
    }

    @Override
    public void execute(final List<SOperation> operations, final long leftOperandContainerId, final String leftOperandContainerType,
            final SExpressionContext expressionContext) throws SOperationExecutionException {
        if(operations.isEmpty()) {
            return;
        }
        // retrieve all left operand to set and put it in context
        retrieveLeftOperandsAndPutItInExpressionContextIfNotIn(operations, leftOperandContainerId, leftOperandContainerType, expressionContext);

        // execute operation and put it in context again
        final Map<SLeftOperand, Boolean> leftOperandUpdates = executeOperators(operations, expressionContext);
        // update data
        // TODO implement batch update in leftOperandHandlers
        updateLeftOperands(leftOperandUpdates, leftOperandContainerId, leftOperandContainerType, expressionContext);
    }

    Map<SLeftOperand, Boolean> executeOperators(final List<SOperation> operations, final SExpressionContext expressionContext)
            throws SOperationExecutionException {
        final Map<SLeftOperand, Boolean> updateLeftOperands = new HashMap<SLeftOperand, Boolean>();
        for (final SOperation operation : operations) {
            final SLeftOperand leftOperand = operation.getLeftOperand();
            final SOperatorType operatorType = operation.getType();
            final boolean update = operatorType != SOperatorType.DELETION;
            if (update) {
                final Object rightOperandValue = getOperationValue(operation, expressionContext, operation.getRightOperand());
                final OperationExecutorStrategy operationExecutorStrategy = operationExecutorStrategyProvider.getOperationExecutorStrategy(operation);
                final Object value = operationExecutorStrategy.computeNewValueForLeftOperand(operation, rightOperandValue, expressionContext);
                expressionContext.getInputValues().put(leftOperand.getName(), value);
                logOperation(TechnicalLogSeverity.DEBUG, operation, rightOperandValue, expressionContext);
            }

            final Boolean isAlreadyUpdated = updateLeftOperands.get(leftOperand);
            if (isAlreadyUpdated == null || isAlreadyUpdated && !update) {
                updateLeftOperands.put(leftOperand, update);
            }
        }
        return updateLeftOperands;
    }

    void updateLeftOperands(final Map<SLeftOperand, Boolean> leftOperandUpdates, final long leftOperandContainerId, final String leftOperandContainerType,
            final SExpressionContext expressionContext) throws SOperationExecutionException {
        for (final Entry<SLeftOperand, Boolean> update : leftOperandUpdates.entrySet()) {
            final SLeftOperand leftOperand = update.getKey();
            final LeftOperandHandler leftOperandHandler = getLeftOperandHandler(leftOperand.getType());
            if (update.getValue()) {
                leftOperandHandler.update(leftOperand, expressionContext.getInputValues(), expressionContext.getInputValues().get(leftOperand.getName()), leftOperandContainerId,
                        leftOperandContainerType);
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

    void retrieveLeftOperandsAndPutItInExpressionContextIfNotIn(final List<SOperation> operations, final long dataContainerId, final String dataContainerType,
            final SExpressionContext expressionContext) throws SOperationExecutionException {
        final Map<String, Object> inputValues = expressionContext.getInputValues();

        //if the container where we execute the operation is not the same than the container of the expression (call activity data mapping) we skip the loading of left operand
        String containerType = expressionContext.getContainerType();
        Long containerId = expressionContext.getContainerId();
        if(containerId == null || containerId != dataContainerId || containerType == null || !containerType.equals(dataContainerType)){
            return;
        }
        HashMap<String, List<SLeftOperand>> leftOperandHashMap = new HashMap<String, List<SLeftOperand>>();
        for (final SOperation operation : operations) {
            // this operation will set a data, we retrieve it and put it in context
            final SLeftOperand leftOperand = operation.getLeftOperand();
            if(!leftOperandHashMap.containsKey(leftOperand.getType())){
                leftOperandHashMap.put(leftOperand.getType(), new ArrayList<SLeftOperand>());
            }
            leftOperandHashMap.get(leftOperand.getType()).add(leftOperand);
        }
        for (Entry<String, List<SLeftOperand>> leftOperandByType : leftOperandHashMap.entrySet()) {
            try {
                getLeftOperandHandler(leftOperandByType.getKey()).loadLeftOperandInContext(leftOperandByType.getValue(),
                        expressionContext, inputValues);
            } catch (final SBonitaReadException e) {
                throw new SOperationExecutionException("Unable to retrieve value for operation "+leftOperandByType.getValue(), e);
            }
        }

    }

    protected Object getOperationValue(final SOperation operation, final SExpressionContext expressionContext, final SExpression sExpression)
            throws SOperationExecutionException {
        if (sExpression == null) {
            return null;
        }
        try {
            return expressionResolverService.evaluate(sExpression, expressionContext);
        } catch (final ClassCastException e) {
            throw new SOperationExecutionException("Unable to execute operation on " + operation.getLeftOperand().getName()
                    + " with a new value which is not Serializable", e);
        } catch (final SBonitaException e) {
            throw new SOperationExecutionException(e);
        }
    }

    private void logOperation(final TechnicalLogSeverity severity, final SOperation operation, final Object operationValue,
            final SExpressionContext expressionContext) {
        if (logger.isLoggable(this.getClass(), severity)) {
            final String message = buildLogMessage(operation, operationValue, expressionContext);
            logger.log(this.getClass(), severity, message);
        }
    }

    private String buildLogMessage(final SOperation operation, final Object operationValue, final SExpressionContext expressionContext) {
        final StringBuilder stb = new StringBuilder();
        stb.append("Executed operation on container [id: '");
        stb.append(expressionContext.getContainerId());
        stb.append("', type: '");
        stb.append(expressionContext.getContainerType());
        stb.append("']. Operation: [left operand: '");
        stb.append(operation.getLeftOperand().getName());
        stb.append("', operator: '");
        stb.append(operation.getOperator());
        stb.append("', operation value: '");
        stb.append(operationValue);
        stb.append("']");
        return stb.toString();
    }

}
