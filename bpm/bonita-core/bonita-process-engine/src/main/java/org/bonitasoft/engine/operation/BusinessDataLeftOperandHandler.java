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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import org.bonitasoft.engine.commons.Container;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.LeftOperandHandler;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.business.data.SRefBusinessDataInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.model.business.data.SMultiRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SRefBusinessDataInstance;
import org.bonitasoft.engine.core.process.instance.model.business.data.SSimpleRefBusinessDataInstance;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class BusinessDataLeftOperandHandler implements LeftOperandHandler {

    protected final RefBusinessDataService refBusinessDataService;

    protected final FlowNodeInstanceService flowNodeInstanceService;
    private final EntitiesActionsExecutor entitiesActionsExecutor;
    private final UpdateDataRefAction updateDataRefAction;

    private final BusinessDataRepository businessDataRepository;

    protected BusinessDataLeftOperandHandler(final BusinessDataRepository businessDataRepository, final RefBusinessDataService refBusinessDataService,
            final FlowNodeInstanceService flowNodeInstanceService, EntitiesActionsExecutor entitiesActionsExecutor, UpdateDataRefAction updateDataRefAction) {
        super();
        this.businessDataRepository = businessDataRepository;
        this.refBusinessDataService = refBusinessDataService;
        this.flowNodeInstanceService = flowNodeInstanceService;
        this.entitiesActionsExecutor = entitiesActionsExecutor;
        this.updateDataRefAction = updateDataRefAction;
    }

    @Override
    public String getType() {
        return SLeftOperand.TYPE_BUSINESS_DATA;
    }

    @Override
    public Object update(final SLeftOperand sLeftOperand, Map<String, Object> inputValues, final Object newValue, final long containerId,
            final String containerType)
            throws SOperationExecutionException {
        try {
            return entitiesActionsExecutor.executeAction(newValue, new BusinessDataContext(sLeftOperand.getName(), new Container(containerId, containerType)),
                    updateDataRefAction);
        } catch (SEntityActionExecutionException e) {
            throw new SOperationExecutionException(e);
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
    public void loadLeftOperandInContext(final SLeftOperand sLeftOperand, final SExpressionContext expressionContext, Map<String, Object> contextToSet)
            throws SBonitaReadException {
        final Map<String, Object> inputValues = expressionContext.getInputValues();
        final String businessDataName = sLeftOperand.getName();
        final Long containerId = expressionContext.getContainerId();
        final String containerType = expressionContext.getContainerType();
        if (inputValues.get(businessDataName) == null) {
            if (!contextToSet.containsKey(businessDataName)) {
                contextToSet.put(businessDataName, getBusinessData(businessDataName, containerId, containerType));
            }
        }
    }

    @Override
    public void loadLeftOperandInContext(final List<SLeftOperand> sLeftOperand, final SExpressionContext expressionContext, Map<String, Object> contextToSet)
            throws SBonitaReadException {
        for (SLeftOperand leftOperand : sLeftOperand) {
            loadLeftOperandInContext(leftOperand, expressionContext, contextToSet);
        }
    }


}
