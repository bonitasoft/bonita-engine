/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
 **
 * @since 6.2
 */
package com.bonitasoft.engine.operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceNotFoundException;
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

    protected SRefBusinessDataInstance getRefBusinessDataInstance(final String businessDataName, final long containerId, final String containerType)
            throws SFlowNodeNotFoundException, SFlowNodeReadException, SRefBusinessDataInstanceNotFoundException, SBonitaReadException {
        final long processInstanceId = flowNodeInstanceService.getProcessInstanceId(containerId, containerType);
        return refBusinessDataService.getRefBusinessDataInstance(businessDataName, processInstanceId);
    }

    @Override
    public String getType() {
        return SLeftOperand.TYPE_BUSINESS_DATA;
    }

    @Override
    public void update(final SLeftOperand sLeftOperand, final Object newValue, final long containerId, final String containerType)
            throws SOperationExecutionException {
        try {
            final SRefBusinessDataInstance reference = getRefBusinessDataInstance(sLeftOperand.getName(),
                    containerId, containerType);
            checkIsValidBusinessData(reference, newValue);
            if (newValue instanceof Entity) {
                final Entity newBusinessDataValue = (Entity) newValue;
                final SSimpleRefBusinessDataInstance simpleRef = (SSimpleRefBusinessDataInstance) reference;
                final Entity businessData = businessDataRepository.merge(newBusinessDataValue);
                if (!businessData.getPersistenceId().equals(simpleRef.getDataId())) {
                    refBusinessDataService.updateRefBusinessDataInstance(simpleRef, businessData.getPersistenceId());
                }
            } else {
                final List<Entity> newBusinessDataValue = (List<Entity>) newValue;
                final SMultiRefBusinessDataInstance multiRef = (SMultiRefBusinessDataInstance) reference;
                final List<Long> businessDataIds = new ArrayList<Long>();
                for (final Entity entity : newBusinessDataValue) {
                    final Entity businessData = businessDataRepository.merge(entity);
                    businessDataIds.add(businessData.getPersistenceId());
                }
                if (!multiRef.getDataIds().containsAll(businessDataIds) || multiRef.getDataIds().size() != businessDataIds.size()) {
                    refBusinessDataService.updateRefBusinessDataInstance(multiRef, businessDataIds);
                }
            }
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

    protected Object getBusinessData(final String bizDataName, final long processInstanceId) throws SBonitaReadException {
        try {
            final SRefBusinessDataInstance reference = refBusinessDataService.getRefBusinessDataInstance(bizDataName, processInstanceId);
            final Class<Entity> dataClass = (Class<Entity>) Thread.currentThread().getContextClassLoader().loadClass(reference.getDataClassName());
            if (reference instanceof SSimpleRefBusinessDataInstance) {
                final SSimpleRefBusinessDataInstance simpleRef = (SSimpleRefBusinessDataInstance) reference;
                final Long dataId = simpleRef.getDataId();
                if (dataId != null) {
                    return businessDataRepository.findById(dataClass, dataId);
                } else {
                    return dataClass.newInstance();
                }
            } else {
                final SMultiRefBusinessDataInstance multiRef = (SMultiRefBusinessDataInstance) reference;
                final List<Long> dataIds = multiRef.getDataIds();
                if (!dataIds.isEmpty()) {
                    return businessDataRepository.findByIds(dataClass, dataIds);
                } else {
                    return Arrays.asList(dataClass.newInstance());
                }
            }
        } catch (final Exception e) {
            throw new SBonitaReadException(e);
        }
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
