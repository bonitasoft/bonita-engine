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
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;

/**
 * @author Matthieu Chaffotte
 */
public class InsertBusinessDataOperationExecutorStrategy implements OperationExecutorStrategy {

    private final BusinessDataRepository repository;

    private final RefBusinessDataService refBusinessDataService;

    private final FlowNodeInstanceService flowNodeInstanceService;

    public InsertBusinessDataOperationExecutorStrategy(final BusinessDataRepository repository, final RefBusinessDataService refBusinessDataService,
            final FlowNodeInstanceService flowNodeInstanceService) {
        super();
        this.repository = repository;
        this.refBusinessDataService = refBusinessDataService;
        this.flowNodeInstanceService = flowNodeInstanceService;
    }

    @Override
    public Object getValue(final SOperation operation, final Object value, final long containerId, final String containerType,
            final SExpressionContext expressionContext) throws SOperationExecutionException {
        if (value == null) {
            throw new SOperationExecutionException("Unable to insert/update a null business data");
        }
        return value;
    }

    @Override
    public void update(final SLeftOperand sLeftOperand, Object newValue, final long containerId, final String containerType)
            throws SOperationExecutionException {
        try {
            long processInstanceId = flowNodeInstanceService.getProcessInstanceId(containerId, containerType);
            final SRefBusinessDataInstance refBusinessDataInstance =
                    refBusinessDataService.getRefBusinessDataInstance(sLeftOperand.getName(), processInstanceId);
            final Long dataId = refBusinessDataInstance.getDataId();
            if (dataId == null) {
                Entity businessData = repository.merge((Entity) newValue);
                refBusinessDataService.updateRefBusinessDataInstance(refBusinessDataInstance, businessData.getPersistenceId());
            }
        } catch (final SBonitaException e) {
            throw new SOperationExecutionException(e);
        }
    }

    @Override
    public String getOperationType() {
        return OperatorType.CREATE_BUSINESS_DATA.name();
    }

    @Override
    public boolean shouldPerformUpdateAtEnd() {
        return false;
    }

}
