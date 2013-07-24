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
 */
public class OperationServiceImpl implements OperationService {

    private final Map<String, OperationExecutorStrategy> operationExecutorsMap;

    private final ExpressionResolverService expressionResolverService;

    private final DataInstanceService dataInstanceService;

    private final TechnicalLoggerService logger;

    public OperationServiceImpl(final OperationExecutorStrategyProvider operationExecutorStrategyProvider,
            final ExpressionResolverService expressionResolverService, final TechnicalLoggerService logger, final DataInstanceService dataInstanceService) {
        super();
        this.expressionResolverService = expressionResolverService;
        this.dataInstanceService = dataInstanceService;
        this.logger = logger;
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
        executeOperation(operation, dataContainerId, dataContainerType, expressionContext);
    }

    @Override
    public void execute(final SOperation operation, final SExpressionContext expressionContext) throws SOperationExecutionException {
        execute(operation, expressionContext.getContainerId(), expressionContext.getContainerType(), expressionContext);
    }

    private void executeOperation(final SOperation operation, final long dataContainerId, final String dataContainerType,
            final SExpressionContext expressionContext) throws SOperationExecutionException {
        execute(Arrays.asList(operation), dataContainerId, dataContainerType, expressionContext);
    }

    protected Object getOperationValue(final SOperation operation, final SExpressionContext expressionContext, final SExpression sExpression)
            throws SOperationExecutionException {
        try {
            return expressionResolverService.evaluate(sExpression, expressionContext);
        } catch (final ClassCastException e) {
            throw new SOperationExecutionException("Trying to set variable " + operation.getLeftOperand().getName() + " a value which is not Serializable", e);
        } catch (final SBonitaException e) {
            throw new SOperationExecutionException(e);
        }
    }

    @Override
    public void execute(final List<SOperation> operations, final SExpressionContext expressionContext) throws SOperationExecutionException {
        execute(operations, expressionContext.getContainerId(), expressionContext.getContainerType(), expressionContext);
    }

    @Override
    public void execute(final List<SOperation> operations, final long dataContainerId, final String dataContainerType,
            final SExpressionContext expressionContext) throws SOperationExecutionException {
        retrieveDataInstancesToSetAndPutItInExpressionContextIfNotIn(operations, expressionContext);
        final Map<SLeftOperand, OperationExecutorStrategy> updates = new HashMap<SLeftOperand, OperationExecutorStrategy>();
        for (final SOperation operation : operations) {
            final Object operationValue = getOperationValue(operation, expressionContext, operation.getRightOperand());
            final OperationExecutorStrategy operationExecutorStrategy = getOperationExecutorStrategy(operation);
            final Object value = operationExecutorStrategy.getValue(operation, operationValue, dataContainerId, dataContainerType, expressionContext);
            if (!operationExecutorStrategy.doUpdateData()) {
                operationExecutorStrategy.update(operation.getLeftOperand(), value, dataContainerId, dataContainerType);
            } else {
                expressionContext.getInputValues().put(operation.getLeftOperand().getName(), value);
                updates.put(operation.getLeftOperand(), operationExecutorStrategy);
            }
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.INFO)) {
                if (dataContainerType != null) {
                    logger.log(this.getClass(), TechnicalLogSeverity.INFO, "Executed operation on " + dataContainerType + " <" + dataContainerId + "> : "
                            + operation.getLeftOperand().getName() + " " + operation.getOperator() + " " + operationValue);
                } else {
                    logger.log(this.getClass(), TechnicalLogSeverity.INFO, "Executed operation " + " <" + dataContainerId + "> : "
                            + operation.getLeftOperand().getName() + " " + operation.getOperator() + " " + operationValue);
                }
            }
        }
        for (final Entry<SLeftOperand, OperationExecutorStrategy> update : updates.entrySet()) {
            update.getValue().update(update.getKey(), expressionContext.getInputValues().get(update.getKey().getName()), dataContainerId, dataContainerType);
        }
    }

    private void retrieveDataInstancesToSetAndPutItInExpressionContextIfNotIn(final List<SOperation> operations, final SExpressionContext expressionContext)
            throws SOperationExecutionException {
        if (expressionContext.getContainerId() != null) {
            final HashSet<String> names = new HashSet<String>(operations.size());
            for (final SOperation operation : operations) {
                // this operation will set a data, we retrieve it and put it in context
                if (getOperationExecutorStrategy(operation).doUpdateData()) {
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

}
