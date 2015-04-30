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
