/*******************************************************************************
 * Copyright (C) 2013-2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.operation;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationExecutorStrategy;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.operation.OperatorType;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;

/**
 * @author Emmanuel Duchastenier
 */
public class AttachExistingBusinessDataOperationStrategy extends BusinessDataOperation implements OperationExecutorStrategy {

    public AttachExistingBusinessDataOperationStrategy(final RefBusinessDataService refBusinessDataService, final FlowNodeInstanceService flowNodeInstanceService) {
        super(refBusinessDataService, flowNodeInstanceService);
    }

    @Override
    // Returns the persistenceId of the business data to set the ref with:
    public Object getValue(final SOperation operation, final Object rightOperandValue, final long containerId, final String containerType,
            final SExpressionContext expressionContext) throws SOperationExecutionException {
        if (rightOperandValue == null) {
            throw new SOperationExecutionException("Unable to set a business data with a not existing reference");
        }
        if (!(rightOperandValue instanceof Entity)) {
            throw new SOperationExecutionException("Wrong usage of " + this.getClass().getSimpleName()
                    + ": right operand must evaluate to a Business Data (subclass of " + Entity.class.getName() + ")");
        }
        return ((Entity) rightOperandValue).getPersistenceId();
    }

    @Override
    public void update(final SLeftOperand sLeftOperand, final Object newValue, final long containerId, final String containerType)
            throws SOperationExecutionException {
        try {
            final SRefBusinessDataInstance refBusinessDataInstance = getRefBusinessDataInstance(sLeftOperand.getName(), containerId, containerType);
            if (newValue != null) {
                refBusinessDataService.updateRefBusinessDataInstance(refBusinessDataInstance, (Long) newValue);
            }
        } catch (final SBonitaException e) {
            throw new SOperationExecutionException(e);
        }
    }

    @Override
    public String getOperationType() {
        return OperatorType.ATTACH_EXISTING_BUSINESS_DATA.name();
    }

    @Override
    public boolean shouldPerformUpdateAtEnd() {
        return false;
    }

}
