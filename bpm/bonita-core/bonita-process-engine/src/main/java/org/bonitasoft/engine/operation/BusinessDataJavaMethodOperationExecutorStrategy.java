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
package org.bonitasoft.engine.operation;


import org.bonitasoft.engine.business.data.BusinessDataService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.impl.JavaMethodOperationExecutorStrategy;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;

/**
 * @author Laurent Leseigneur
 * @author Matthieu Chaffotte
 */
public class BusinessDataJavaMethodOperationExecutorStrategy extends JavaMethodOperationExecutorStrategy {

    private final BusinessDataService businessDataService;
    private final EntitiesActionsExecutor entitiesActionsExecutor;
    private final MergeEntityAction mergeEntityAction;

    public BusinessDataJavaMethodOperationExecutorStrategy(final BusinessDataService businessDataService, EntitiesActionsExecutor entitiesActionsExecutor,
            MergeEntityAction mergeEntityAction) {
        this.businessDataService = businessDataService;
        this.entitiesActionsExecutor = entitiesActionsExecutor;
        this.mergeEntityAction = mergeEntityAction;
    }

    @Override
    public Object computeNewValueForLeftOperand(final SOperation operation, final Object valueToSetObjectWith, final SExpressionContext expressionContext,
            final boolean shouldPersistValue)
            throws SOperationExecutionException {
        if (isBusinessData(operation)) {
            return delegateBusinessValueForLeftOperand(operation, valueToSetObjectWith, expressionContext, shouldPersistValue);
        }
        return computeJavaOperation(operation, valueToSetObjectWith, expressionContext, shouldPersistValue);
    }

    protected Object computeJavaOperation(final SOperation operation, final Object valueToSetObjectWith, final SExpressionContext expressionContext,
            final boolean shouldPersistValue)
            throws SOperationExecutionException {
        return super.computeNewValueForLeftOperand(operation, valueToSetObjectWith, expressionContext, shouldPersistValue);
    }

    private Object delegateBusinessValueForLeftOperand(final SOperation operation, final Object valueToSetObjectWith,
            final SExpressionContext expressionContext, final boolean shouldPersistValue)
            throws SOperationExecutionException {
        final Object businessObject = extractObjectToInvokeFromContext(operation, expressionContext);
        final String methodName = extractMethodName(operation);
        final String parameterType = extractParameterType(operation);
        try {
            Object newValue = businessDataService.callJavaOperation(businessObject, valueToSetObjectWith, methodName, parameterType);
            if (shouldPersistValue) {
                newValue = entitiesActionsExecutor.executeAction(newValue, null, mergeEntityAction);
            }
            return newValue;
        } catch (final SBonitaException e) {
            throw new SOperationExecutionException(e);
        }
    }

    private boolean isBusinessData(final SOperation operation) {
        return SLeftOperand.TYPE_BUSINESS_DATA.equals(operation.getLeftOperand().getType());
    }
}
