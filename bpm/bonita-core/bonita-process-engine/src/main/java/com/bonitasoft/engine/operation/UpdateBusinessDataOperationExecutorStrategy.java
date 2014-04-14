/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.operation;

import java.util.Map;

import org.bonitasoft.engine.commons.JavaMethodInvoker;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationExecutorStrategy;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.persistence.SBonitaReadException;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;

/**
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public class UpdateBusinessDataOperationExecutorStrategy extends BusinessDataOperation implements OperationExecutorStrategy {

    private final BusinessDataRepository businessDataRepository;

    public UpdateBusinessDataOperationExecutorStrategy(final BusinessDataRepository businessDataRepository,
            final RefBusinessDataService refBusinessDataService, final FlowNodeInstanceService flowNodeInstanceService) {
        super(refBusinessDataService, flowNodeInstanceService);
        this.businessDataRepository = businessDataRepository;
    }

    @Override
    public Object getValue(final SOperation operation, final Object value, final long containerId, final String containerType,
            final SExpressionContext expressionContext) throws SOperationExecutionException {
        final String operator = getOperator(operation);
        final String operatorParameterClassName = getOperatorParameterClassName(operation);
        try {
            final Object objectToInvokeJavaMethodOn = getBusinessDataObjectAndPutInContextIfNotAlready(containerId, containerType, expressionContext, operation
                    .getLeftOperand().getName());
            return new JavaMethodInvoker().invokeJavaMethod(operation.getRightOperand().getReturnType(), value, objectToInvokeJavaMethodOn, operator,
                    operatorParameterClassName);
        } catch (final Exception e) {
            throw new SOperationExecutionException("Unable to evaluate operation " + operation, e);
        }
    }

    protected String getOperatorParameterClassName(final SOperation operation) {
        final String[] split = operation.getOperator().split(":", 2);
        if (split.length > 1) {
            return split[1];
        }
        return null;
    }

    protected String getOperator(final SOperation operation) {
        return operation.getOperator().split(":", 2)[0];
    }

    protected Object getBusinessDataObjectAndPutInContextIfNotAlready(final long containerId, final String containerType,
            final SExpressionContext expressionContext, final String businessDataName) throws SOperationExecutionException, SFlowNodeNotFoundException,
            SFlowNodeReadException {
        final Map<String, Object> inputValues = expressionContext.getInputValues();
        Object objectToInvokeJavaMethodOn = inputValues.get(businessDataName);
        if (objectToInvokeJavaMethodOn == null) {
            final long processInstanceId = flowNodeInstanceService.getProcessInstanceId(containerId, containerType);
            objectToInvokeJavaMethodOn = getBusinessData(businessDataName, processInstanceId);
            // put it in context for further reuse:
            inputValues.put(businessDataName, objectToInvokeJavaMethodOn);
        }
        return objectToInvokeJavaMethodOn;
    }

    protected Object getBusinessData(final String bizDataName, final long processInstanceId) throws SOperationExecutionException {
        try {
            final SRefBusinessDataInstance refBusinessDataInstance = refBusinessDataService.getRefBusinessDataInstance(bizDataName, processInstanceId);
            final Class<Entity> dataClass = (Class<Entity>) Thread.currentThread().getContextClassLoader()
                    .loadClass(refBusinessDataInstance.getDataClassName());
            final Long dataId = refBusinessDataInstance.getDataId();
            if (dataId != null) {
                return businessDataRepository.findById(dataClass, dataId);
            } else {
                return dataClass.newInstance();
            }
        } catch (final SBonitaReadException e) {
            throw new SOperationExecutionException("Unable to retrieve business data instance with name " + bizDataName);
        } catch (final Exception e) {
            throw new SOperationExecutionException(e);
        }
    }

    @Override
    public void update(final SLeftOperand sLeftOperand, final Object newValue, final long containerId, final String containerType)
            throws SOperationExecutionException {
        checkIsBusinessData(newValue);
        try {
            Entity businessData = businessDataRepository.merge((Entity) newValue);
            SRefBusinessDataInstance refBusinessDataInstance = getRefBusinessDataInstance(sLeftOperand.getName(), containerId, containerType);
            if (refBusinessDataInstance != null) {
                refBusinessDataService.updateRefBusinessDataInstance(refBusinessDataInstance, businessData.getPersistenceId());
            }
        } catch (final SBonitaException e) {
            throw new SOperationExecutionException(e);
        }
    }

    private void checkIsBusinessData(final Object newValue) throws SOperationExecutionException {
        if (!(newValue instanceof Entity)) {
            throw new SOperationExecutionException(new IllegalStateException(newValue.getClass().getName() + " must implements " + Entity.class.getName()));
        }
    }

    @Override
    public String getOperationType() {
        return OperatorType.BUSINESS_DATA_JAVA_SETTER.name();
    }

    @Override
    public boolean shouldPerformUpdateAtEnd() {
        return true;
    }
}
