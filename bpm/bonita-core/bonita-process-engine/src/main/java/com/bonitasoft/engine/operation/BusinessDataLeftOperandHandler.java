/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.operation;

import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.LeftOperandHandler;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.persistence.SBonitaReadException;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceModificationException;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceNotFoundException;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class BusinessDataLeftOperandHandler implements LeftOperandHandler {

    protected final RefBusinessDataService refBusinessDataService;

    protected final FlowNodeInstanceService flowNodeInstanceService;

    private final BusinessDataRepository businessDataRepository;

    protected BusinessDataLeftOperandHandler(final BusinessDataRepository businessDataRepository, final RefBusinessDataService refBusinessDataService,
            final FlowNodeInstanceService flowNodeInstanceService) {
        super();
        this.businessDataRepository = businessDataRepository;
        this.refBusinessDataService = refBusinessDataService;
        this.flowNodeInstanceService = flowNodeInstanceService;
    }

    @Override
    public String getType() {
        return SLeftOperand.TYPE_BUSINESS_DATA;
    }

    @Override
    public void update(final SLeftOperand sLeftOperand, final Object newValue, final long containerId, final String containerType)
            throws SOperationExecutionException {
        checkIsValidBusinessData(newValue);
        final Entity newBusinessDataValue = (Entity) newValue;
        try {
            final SRefBusinessDataInstance refBusinessDataInstance = getRefBusinessDataInstance(sLeftOperand.getName(), containerId, containerType);
            final Long businessDataId = newBusinessDataValue.getPersistenceId();
            if (businessDataId != null || refBusinessDataInstance.getDataId() == null) {
                final Entity businessData = businessDataRepository.merge(newBusinessDataValue);
                if (!businessData.getPersistenceId().equals(refBusinessDataInstance.getDataId())) {
                    refBusinessDataService.updateRefBusinessDataInstance(refBusinessDataInstance, businessData.getPersistenceId());
                }
            }
        } catch (final SBonitaException e) {
            throw new SOperationExecutionException(e);
        }
    }

    private void checkIsValidBusinessData(final Object newValue) throws SOperationExecutionException {
        if (newValue == null) {
            throw new SOperationExecutionException("Unable to insert/update a null business data");
        }
        if (!(newValue instanceof Entity)) {
            throw new SOperationExecutionException(new IllegalStateException(newValue.getClass().getName() + " must implements " + Entity.class.getName()));
        }
    }

    @SuppressWarnings("unchecked")
    protected Object getBusinessData(final String bizDataName, final long processInstanceId) throws SBonitaReadException {
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
        } catch (final Exception e) {
            throw new SBonitaReadException(e);
        }
    }

    @Override
    public void delete(final SLeftOperand sLeftOperand, final long containerId, final String containerType) throws SOperationExecutionException {
        try {
            final SRefBusinessDataInstance refBusinessDataInstance = getRefBusinessDataInstance(sLeftOperand.getName(), containerId, containerType);
            removeBusinessData(refBusinessDataInstance);
            dereferenceBusinessData(refBusinessDataInstance);
        } catch (final Exception e) {
            throw new SOperationExecutionException(e);
        }
    }

    protected SRefBusinessDataInstance getRefBusinessDataInstance(final String businessDataName, final long containerId, final String containerType)
            throws SFlowNodeNotFoundException, SFlowNodeReadException, SRefBusinessDataInstanceNotFoundException, SBonitaReadException {
        final long processInstanceId = flowNodeInstanceService.getProcessInstanceId(containerId, containerType);
        return refBusinessDataService.getRefBusinessDataInstance(businessDataName, processInstanceId);
    }

    @SuppressWarnings("unchecked")
    protected void removeBusinessData(final SRefBusinessDataInstance reference) throws ClassNotFoundException, SBusinessDataNotFoundException {
        final Class<Entity> dataClass = (Class<Entity>) Thread.currentThread().getContextClassLoader().loadClass(reference.getDataClassName());
        final Entity entity = businessDataRepository.findById(dataClass, reference.getDataId());
        businessDataRepository.remove(entity);
    }

    protected void dereferenceBusinessData(final SRefBusinessDataInstance reference) throws SRefBusinessDataInstanceModificationException {
        refBusinessDataService.updateRefBusinessDataInstance(reference, null);
    }

    @Override
    public Object retrieve(final SLeftOperand sLeftOperand, final SExpressionContext expressionContext) throws SBonitaReadException {
        final Map<String, Object> inputValues = expressionContext.getInputValues();
        final String businessDataName = sLeftOperand.getName();
        final Long containerId = expressionContext.getContainerId();
        final String containerType = expressionContext.getContainerType();
        try {
            if (inputValues.get(businessDataName) == null) {
                long processInstanceId;
                processInstanceId = flowNodeInstanceService.getProcessInstanceId(containerId, containerType);

                return getBusinessData(businessDataName, processInstanceId);

            }
        } catch (final SFlowNodeNotFoundException e) {
            throwBonitaReadException(businessDataName, e);
        } catch (final SFlowNodeReadException e) {
            throwBonitaReadException(businessDataName, e);
        }
        return null;
    }

    private void throwBonitaReadException(final String businessDataName, final Exception e) throws SBonitaReadException {
        throw new SBonitaReadException("Unable to retrieve the context for business data " + businessDataName, e);
    }

}
