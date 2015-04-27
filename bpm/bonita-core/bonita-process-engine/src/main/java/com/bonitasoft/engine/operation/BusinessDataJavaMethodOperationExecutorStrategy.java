/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.operation;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.impl.JavaMethodOperationExecutorStrategy;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;

import com.bonitasoft.engine.business.data.BusinessDataService;

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
