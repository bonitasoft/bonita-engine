/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *
 * @since 6.2
 *******************************************************************************/
package com.bonitasoft.engine.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.LeftOperandHandler;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceModificationException;
import com.bonitasoft.engine.core.process.instance.model.SMultiRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;
import com.bonitasoft.engine.core.process.instance.model.SSimpleRefBusinessDataInstance;

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
    public Object update(final SLeftOperand sLeftOperand, final Object newValue, final long containerId, final String containerType)
            throws SOperationExecutionException {
        try {
            final SRefBusinessDataInstance reference = getRefBusinessDataInstance(sLeftOperand.getName(), containerId, containerType);
            checkIsValidBusinessData(reference, newValue);
            if (newValue instanceof Entity) {
                final Entity newBusinessDataValue = (Entity) newValue;
                final SSimpleRefBusinessDataInstance simpleRef = (SSimpleRefBusinessDataInstance) reference;
                final Entity businessData = businessDataRepository.merge(newBusinessDataValue);
                if (!businessData.getPersistenceId().equals(simpleRef.getDataId())) {
                    refBusinessDataService.updateRefBusinessDataInstance(simpleRef, businessData.getPersistenceId());
                }
                return businessData;
            }
            final List<Entity> newBusinessDataValue = (List<Entity>) newValue;
            final SMultiRefBusinessDataInstance multiRef = (SMultiRefBusinessDataInstance) reference;
            final List<Long> businessDataIds = new ArrayList<Long>();
            final List<Entity> updated = new ArrayList<Entity>();
            for (final Entity entity : newBusinessDataValue) {
                final Entity businessData = businessDataRepository.merge(entity);
                businessDataIds.add(businessData.getPersistenceId());
                updated.add(businessData);
            }
            if (!multiRef.getDataIds().containsAll(businessDataIds) || multiRef.getDataIds().size() != businessDataIds.size()) {
                refBusinessDataService.updateRefBusinessDataInstance(multiRef, businessDataIds);
            }
            return updated;

        } catch (final SBonitaException e) {
            throw new SOperationExecutionException(e);
        }
    }

    private void checkIsValidBusinessData(final SRefBusinessDataInstance reference, final Object newValue) throws SOperationExecutionException {
        if (newValue == null) {
            throw new SOperationExecutionException("Unable to insert/update a null business data");
        }
        if (reference instanceof SSimpleRefBusinessDataInstance && !(newValue instanceof Entity)) {
            throw new SOperationExecutionException(new IllegalStateException(newValue.getClass().getName() + " must implements " + Entity.class.getName()));
        }
        if (reference instanceof SMultiRefBusinessDataInstance && !(newValue instanceof List)) {
            throw new SOperationExecutionException(new IllegalStateException(newValue.getClass().getName() + " must implements " + List.class.getName()));
        }
    }

    @SuppressWarnings("unchecked")
    protected Object getBusinessData(final String businessDataName, final long containerId, final String containerType) throws SBonitaReadException {
        try {
            final SRefBusinessDataInstance reference = getRefBusinessDataInstance(businessDataName, containerId, containerType);
            final Class<Entity> dataClass = (Class<Entity>) Thread.currentThread().getContextClassLoader().loadClass(reference.getDataClassName());
            if (reference instanceof SSimpleRefBusinessDataInstance) {
                final SSimpleRefBusinessDataInstance simpleRef = (SSimpleRefBusinessDataInstance) reference;
                final Long dataId = simpleRef.getDataId();
                if (dataId != null) {
                    return businessDataRepository.findById(dataClass, dataId);
                }
                return dataClass.newInstance();
            }
            final SMultiRefBusinessDataInstance multiRef = (SMultiRefBusinessDataInstance) reference;
            final List<Long> dataIds = multiRef.getDataIds();
            if (!dataIds.isEmpty()) {
                return businessDataRepository.findByIds(dataClass, dataIds);
            }
            return new ArrayList<Entity>();
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
            throws SBonitaException {
        if ("PROCESS_INSTANCE".equals(containerType)) {
            return refBusinessDataService.getRefBusinessDataInstance(businessDataName, containerId);
        }
        try {
            return refBusinessDataService.getFlowNodeRefBusinessDataInstance(businessDataName, containerId);
        } catch (final SBonitaException sbe) {
            final long processInstanceId = flowNodeInstanceService.getProcessInstanceId(containerId, containerType);
            return refBusinessDataService.getRefBusinessDataInstance(businessDataName, processInstanceId);
        }
    }

    @SuppressWarnings("unchecked")
    protected void removeBusinessData(final SRefBusinessDataInstance reference) throws ClassNotFoundException, SBusinessDataNotFoundException {
        final Class<Entity> dataClass = (Class<Entity>) Thread.currentThread().getContextClassLoader().loadClass(reference.getDataClassName());
        if (reference instanceof SSimpleRefBusinessDataInstance) {
            final SSimpleRefBusinessDataInstance simpleRef = (SSimpleRefBusinessDataInstance) reference;
            final Entity entity = businessDataRepository.findById(dataClass, simpleRef.getDataId());
            businessDataRepository.remove(entity);
        } else {
            final SMultiRefBusinessDataInstance multiRef = (SMultiRefBusinessDataInstance) reference;
            for (final Long dataId : multiRef.getDataIds()) {
                final Entity entity = businessDataRepository.findById(dataClass, dataId);
                businessDataRepository.remove(entity);
            }
        }
    }

    protected void dereferenceBusinessData(final SRefBusinessDataInstance reference) throws SRefBusinessDataInstanceModificationException {
        if (reference instanceof SSimpleRefBusinessDataInstance) {
            refBusinessDataService.updateRefBusinessDataInstance((SSimpleRefBusinessDataInstance) reference, null);
        } else {
            refBusinessDataService.updateRefBusinessDataInstance((SMultiRefBusinessDataInstance) reference, new ArrayList<Long>());
        }
    }

    @Override
    public Object retrieve(final SLeftOperand sLeftOperand, final SExpressionContext expressionContext) throws SBonitaReadException {
        final Map<String, Object> inputValues = expressionContext.getInputValues();
        final String businessDataName = sLeftOperand.getName();
        final Long containerId = expressionContext.getContainerId();
        final String containerType = expressionContext.getContainerType();
        if (inputValues.get(businessDataName) == null) {
            return getBusinessData(businessDataName, containerId, containerType);
        }
        return null;
    }

    @Override
    public boolean supportBatchUpdate() {
        return false;
    }

}
