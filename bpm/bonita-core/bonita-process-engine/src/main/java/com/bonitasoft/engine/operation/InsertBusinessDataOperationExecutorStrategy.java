/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.operation;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationExecutorStrategy;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;

import com.bonitasoft.engine.business.data.BusinessDataRespository;

/**
 * @author Matthieu Chaffotte
 */
public class InsertBusinessDataOperationExecutorStrategy implements OperationExecutorStrategy {

    private final BusinessDataRespository respository;

    public InsertBusinessDataOperationExecutorStrategy(final BusinessDataRespository respository) {
        super();
        this.respository = respository;
    }

    @Override
    public Object getValue(final SOperation operation, final Object value, final long containerId, final String containerType,
            final SExpressionContext expressionContext) throws SOperationExecutionException {
        if (value == null) {
            throw new SOperationExecutionException("Unable to insert a null business data");
        }
        return value;
    }

    @Override
    public void update(final SLeftOperand sLeftOperand, final Object newValue, final long containerId, final String containerType)
            throws SOperationExecutionException {
        respository.persist(newValue);
    }

    @Override
    public String getOperationType() {
        return "CREATE_BUSINESS_DATA";
    }

    @Override
    public boolean doUpdateData() {
        return false;
    }

}
