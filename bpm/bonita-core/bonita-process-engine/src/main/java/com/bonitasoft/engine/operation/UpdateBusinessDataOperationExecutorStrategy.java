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
import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceModificationException;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceNotFoundException;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;

/**
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public class UpdateBusinessDataOperationExecutorStrategy implements OperationExecutorStrategy {

    private final BusinessDataRepository businessDataRepository;

    private final RefBusinessDataService refBusinessDataService;

    private final FlowNodeInstanceService flowNodeInstanceService;

    public UpdateBusinessDataOperationExecutorStrategy(final BusinessDataRepository businessDataRepository,
            final RefBusinessDataService refBusinessDataService, final FlowNodeInstanceService flowNodeInstanceService) {
        super();
        this.businessDataRepository = businessDataRepository;
        this.refBusinessDataService = refBusinessDataService;
        this.flowNodeInstanceService = flowNodeInstanceService;
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
    public void update(final SLeftOperand sLeftOperand, Object newValue, final long containerId, final String containerType)
            throws SOperationExecutionException {
        try {
            final long processInstanceId = flowNodeInstanceService.getProcessInstanceId(containerId, containerType);
            final SRefBusinessDataInstance refBusinessDataInstance = refBusinessDataService.getRefBusinessDataInstance(sLeftOperand.getName(),
                    processInstanceId);
            if (!(newValue instanceof Entity)) {
                throw new SOperationExecutionException(new IllegalStateException(newValue.getClass().getName() + " must implements " + Entity.class.getName()));
            }
            newValue = businessDataRepository.merge((Entity) newValue);
            if (refBusinessDataInstance != null) {
                refBusinessDataService.updateRefBusinessDataInstance(refBusinessDataInstance, ((Entity) newValue).getPersistenceId());
            }
        } catch (final SRefBusinessDataInstanceNotFoundException srbdinfe) {
            throw new SOperationExecutionException(srbdinfe);
        } catch (final SBonitaReadException sbre) {
            throw new SOperationExecutionException(sbre);
        } catch (final SRefBusinessDataInstanceModificationException srbsme) {
            throw new SOperationExecutionException(srbsme);
        } catch (final SFlowNodeNotFoundException e) {
            throw new SOperationExecutionException(e);
        } catch (final SFlowNodeReadException e) {
            throw new SOperationExecutionException(e);
        }
    }

    @Override
    public String getOperationType() {
        return OperatorType.BUSINESS_DATA_JAVA_SETTER.name();
    }

    @Override
    public boolean doUpdateData() {
        return true;
    }

}
