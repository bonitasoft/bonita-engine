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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationExecutorStrategy;
import org.bonitasoft.engine.core.operation.OperationExecutorStrategyProvider;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Zhang Bole
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @author Colin PUY
 */
public class OperationServiceImpl implements OperationService {

    private final ExpressionResolverService expressionResolverService;

    private final DataInstanceService dataInstanceService;

    private final TechnicalLoggerService logger;

    private final OperationExecutorStrategyProvider operationExecutorStrategyProvider;

    public OperationServiceImpl(final OperationExecutorStrategyProvider operationExecutorStrategyProvider,
            final ExpressionResolverService expressionResolverService, final TechnicalLoggerService logger, final DataInstanceService dataInstanceService) {
        super();
        this.operationExecutorStrategyProvider = operationExecutorStrategyProvider;
        this.expressionResolverService = expressionResolverService;
        this.dataInstanceService = dataInstanceService;
        this.logger = logger;
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
    public void execute(final List<SOperation> operations, final long containerId, final String containerType, final SExpressionContext expressionContext)
            throws SOperationExecutionException {

        retrieveDataInstancesToSetAndPutItInExpressionContextIfNotIn(operations, expressionContext);

        Map<SLeftOperand, OperationExecutorStrategy> operationsToUpdateAtEnd = new HashMap<SLeftOperand, OperationExecutorStrategy>(operations.size());
        for (SOperation operation : operations) {
            Object operationValue = getOperationValue(operation, expressionContext, operation.getRightOperand());
            OperationExecutorStrategy operationExecutorStrategy = operationExecutorStrategyProvider.getOperationExecutorStrategy(operation);
            Object value = operationExecutorStrategy.getValue(operation, operationValue, containerId, containerType, expressionContext);
            
            if (operationExecutorStrategy.shouldPerformUpdateAtEnd()) {
                expressionContext.getInputValues().put(operation.getLeftOperand().getName(), value);
                operationsToUpdateAtEnd.put(operation.getLeftOperand(), operationExecutorStrategy);
            } else {
                operationExecutorStrategy.update(operation.getLeftOperand(), value, containerId, containerType);
            }
            
            logOperation(TechnicalLogSeverity.DEBUG, containerId, containerType, operation, operationValue);
        }
        
        for (Entry<SLeftOperand, OperationExecutorStrategy> operationToUpdate : operationsToUpdateAtEnd.entrySet()) {
            OperationExecutorStrategy strategy = operationToUpdate.getValue();
            SLeftOperand leftOperand = operationToUpdate.getKey();
            Object newValue = expressionContext.getInputValues().get(leftOperand.getName());

            strategy.update(leftOperand, newValue, containerId, containerType);
        }
    }

    private void retrieveDataInstancesToSetAndPutItInExpressionContextIfNotIn(final List<SOperation> operations, final SExpressionContext expressionContext)
            throws SOperationExecutionException {
        if (expressionContext.getContainerId() != null) {
            final HashSet<String> names = new HashSet<String>(operations.size());
            for (final SOperation operation : operations) {
                // this operation will set a data, we retrieve it and put it in context
                if (operationExecutorStrategyProvider.getOperationExecutorStrategy(operation).shouldPerformUpdateAtEnd()) {
                    names.add(operation.getLeftOperand().getName());
                }
            }
            List<SDataInstance> dataInstances;
            try {
                dataInstances = dataInstanceService.getDataInstances(new ArrayList<String>(names), expressionContext.getContainerId(),
                        expressionContext.getContainerType());
            } catch (final SDataInstanceException e) {
                throw new SOperationExecutionException(e);
            }
            final Map<String, Object> inputValues = expressionContext.getInputValues();
            for (final SDataInstance dataInstance : dataInstances) {
                if (!inputValues.containsKey(dataInstance.getName())) {
                    inputValues.put(dataInstance.getName(), dataInstance.getValue());
                }
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

    private void logOperation(TechnicalLogSeverity severity, final long containerId, final String containerType, SOperation operation, Object operationValue) {
        if (logger.isLoggable(this.getClass(), severity)) {
            String message = buildLogMessage(containerId, containerType, operation, operationValue);
            logger.log(this.getClass(), severity, message);
        }
    }

    private String buildLogMessage(final long containerId, final String containerType, final SOperation operation, final Object operationValue) {
        StringBuilder stb = new StringBuilder();
        stb.append("Executed operation on container [id: '");
        stb.append(containerId);
        stb.append("', type: '");
        stb.append(containerType);
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
