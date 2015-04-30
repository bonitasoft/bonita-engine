/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package org.bonitasoft.engine.operation;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationExecutorStrategy;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.operation.model.SOperatorType;

/**
 * @author Elias Ricken de Medeiros
 */
public class BusinessDataAssignmentStrategy implements OperationExecutorStrategy {

    private final EntitiesActionsExecutor actionsExecutor;
    private final MergeEntityAction mergeEntityAction;

    public BusinessDataAssignmentStrategy(EntitiesActionsExecutor actionsExecutor, MergeEntityAction mergeEntityAction) {
        this.actionsExecutor = actionsExecutor;
        this.mergeEntityAction = mergeEntityAction;
    }

    @Override
    public Object computeNewValueForLeftOperand(final SOperation operation, final Object value, final SExpressionContext expressionContext,
            final boolean shouldPersistValue)
            throws SOperationExecutionException {
        if (!shouldPersistValue) {
            return value;
        }
        try {
            return actionsExecutor.executeAction(value, null, mergeEntityAction);
        } catch (SEntityActionExecutionException e) {
            throw new SOperationExecutionException(e);
        }
    }

    @Override
    public String getOperationType() {
        return SOperatorType.ASSIGNMENT.name() + "_" + SLeftOperand.TYPE_BUSINESS_DATA;
    }

}
