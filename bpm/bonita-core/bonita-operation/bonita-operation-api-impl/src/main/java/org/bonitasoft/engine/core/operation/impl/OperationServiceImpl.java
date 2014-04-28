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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Zhang Bole
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @author Colin PUY
 */
public class OperationServiceImpl implements OperationService {

    private final Map<String, LeftOperandHandler> leftOperandHandlersMap;

    private final ExpressionResolverService expressionResolverService;

    private final TechnicalLoggerService logger;

    private final OperationExecutorStrategyProvider operationExecutorStrategyProvider;

    public OperationServiceImpl(final OperationExecutorStrategyProvider operationExecutorStrategyProvider,
            final LeftOperandHandlerProvider leftOperandHandlerProvider,
            final ExpressionResolverService expressionResolverService, final TechnicalLoggerService logger) {
        super();
        this.operationExecutorStrategyProvider = operationExecutorStrategyProvider;
        this.expressionResolverService = expressionResolverService;
        this.logger = logger;
        List<LeftOperandHandler> leftOperandHandlers = leftOperandHandlerProvider.getLeftOperandHandlers();
        leftOperandHandlersMap = new HashMap<String, LeftOperandHandler>(leftOperandHandlers.size());
        for (LeftOperandHandler leftOperandHandler : leftOperandHandlers) {
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
        // retrieve all left operand to set and put it in context
        // TODO implement batch retrieve in leftOperandHandlers
        retrieveLeftOperandsAndPutItInExpressionContextIfNotIn(operations, leftOperandContainerId, leftOperandContainerType, expressionContext);

        // execute operation and put it in context again
        executeOperators(operations, expressionContext);

        // update data
        // TODO implement batch update in leftOperandHandlers
        updateLeftOperands(operations, leftOperandContainerId, leftOperandContainerType, expressionContext);
    }

    void updateLeftOperands(final List<SOperation> operations, final long leftOperandContainerId, final String leftOperandContainerType,
            final SExpressionContext expressionContext) throws SOperationExecutionException {
        for (SOperation operation : operations) {
            SLeftOperand leftOperand = operation.getLeftOperand();
            getLeftOperandHandler(leftOperand).update(leftOperand, expressionContext.getInputValues().get(leftOperand.getName()), leftOperandContainerId,
                    leftOperandContainerType);
        }
    }

    void executeOperators(final List<SOperation> operations,
            final SExpressionContext expressionContext) throws SOperationExecutionException {
        for (final SOperation operation : operations) {
            final Object rightOperandValue = getOperationValue(operation, expressionContext, operation.getRightOperand());
            final OperationExecutorStrategy operationExecutorStrategy = operationExecutorStrategyProvider.getOperationExecutorStrategy(operation);
            final Object value = operationExecutorStrategy.computeNewValueForLeftOperand(operation, rightOperandValue, expressionContext);
            expressionContext.getInputValues().put(operation.getLeftOperand().getName(), value);
            logOperation(TechnicalLogSeverity.DEBUG, operation, rightOperandValue, expressionContext);
        }
    }

    private LeftOperandHandler getLeftOperandHandler(final SLeftOperand leftOperand) throws SOperationExecutionException {
        LeftOperandHandler leftOperandHandler = leftOperandHandlersMap.get(leftOperand.getType());
        if (leftOperandHandler == null) {
            throw new SOperationExecutionException("Left operand type not found: " + leftOperand.getType());
        }
        return leftOperandHandler;
    }

    void retrieveLeftOperandsAndPutItInExpressionContextIfNotIn(final List<SOperation> operations, final long dataContainerId,
            final String dataContainerType, final SExpressionContext expressionContext)
            throws SOperationExecutionException {
        final Map<String, Object> inputValues = expressionContext.getInputValues();

        for (final SOperation operation : operations) {
            // this operation will set a data, we retrieve it and put it in context
            SLeftOperand leftOperand = operation.getLeftOperand();
            Object retrieve;
            try {
                retrieve = getLeftOperandHandler(leftOperand).retrieve(leftOperand,
                        new SExpressionContext(dataContainerId, dataContainerType, expressionContext.getInputValues()));
            } catch (SBonitaReadException e) {
                throw new SOperationExecutionException("Unable to retrieve value for operation " + operation, e);
            }
            if (retrieve != null /* some left operand don't retrieve it, e.g. document, it's heavy */&& !inputValues.containsKey(leftOperand.getName())) {
                inputValues.put(leftOperand.getName(), retrieve);
            }

        }
    }

    protected Object getOperationValue(final SOperation operation, final SExpressionContext expressionContext, final SExpression sExpression)
            throws SOperationExecutionException {
        try {
            return expressionResolverService.evaluate(sExpression, expressionContext);
        } catch (final ClassCastException e) {
            throw new SOperationExecutionException("Unable to execute operation on " + operation.getLeftOperand().getName()
                    + " with a new value which is not Serializable", e);
        } catch (final SBonitaException e) {
            throw new SOperationExecutionException(e);
        }
    }

    private void logOperation(final TechnicalLogSeverity severity, final SOperation operation,
            final Object operationValue, final SExpressionContext expressionContext) {
        if (logger.isLoggable(this.getClass(), severity)) {
            String message = buildLogMessage(operation, operationValue, expressionContext);
            logger.log(this.getClass(), severity, message);
        }
    }

    private String buildLogMessage(final SOperation operation, final Object operationValue, final SExpressionContext expressionContext) {
        StringBuilder stb = new StringBuilder();
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
