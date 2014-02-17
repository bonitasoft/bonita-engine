/*******************************************************************************
 * Copyright (C) 2013-2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.operation;

import org.bonitasoft.engine.commons.ClassReflector;
import org.bonitasoft.engine.commons.ReflectException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationExecutorStrategy;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.persistence.SBonitaReadException;

import com.bonitasoft.engine.business.data.BusinessDataRespository;
import com.bonitasoft.engine.core.process.instance.api.RefBusinessDataService;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceModificationException;
import com.bonitasoft.engine.core.process.instance.api.exceptions.SRefBusinessDataInstanceNotFoundException;
import com.bonitasoft.engine.core.process.instance.model.SRefBusinessDataInstance;

/**
 * @author Matthieu Chaffotte
 */
public class InsertBusinessDataOperationExecutorStrategy implements OperationExecutorStrategy {

    private static final String PERSISTENCE_ID_GETTER = "getPersistenceId";

    private final BusinessDataRespository repository;

    private final RefBusinessDataService refBusinessDataService;

    private final FlowNodeInstanceService flowNodeInstanceService;

    public InsertBusinessDataOperationExecutorStrategy(final BusinessDataRespository repository, final RefBusinessDataService refBusinessDataService,
            final FlowNodeInstanceService flowNodeInstanceService) {
        super();
        this.repository = repository;
        this.refBusinessDataService = refBusinessDataService;
        this.flowNodeInstanceService = flowNodeInstanceService;
    }

    protected long getProcessInstanceId(final long containerId, final String containerType) throws SFlowNodeNotFoundException, SFlowNodeReadException {
        if (DataInstanceContainer.PROCESS_INSTANCE.name().equals(containerType)) {
            return containerId;
        } else if (DataInstanceContainer.ACTIVITY_INSTANCE.name().equals(containerType)) {
            SFlowNodeInstance flowNodeInstance;
            flowNodeInstance = flowNodeInstanceService.getFlowNodeInstance(containerId);
            return flowNodeInstance.getParentProcessInstanceId();
        }
        throw new IllegalArgumentException("Invalid container type: " + containerType);
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
            final SRefBusinessDataInstance refBusinessDataInstance = refBusinessDataService.getRefBusinessDataInstance(sLeftOperand.getName(),
                    getProcessInstanceId(containerId, containerType));
            final Long dataId = refBusinessDataInstance.getDataId();
            if (dataId == null) {
                newValue = repository.merge(newValue);
                final Long id = ClassReflector.invokeGetter(newValue, PERSISTENCE_ID_GETTER);
                refBusinessDataService.updateRefBusinessDataInstance(refBusinessDataInstance, id);
            }
        } catch (final SRefBusinessDataInstanceNotFoundException srbdinfe) {
            throw new SOperationExecutionException(srbdinfe);
        } catch (final SBonitaReadException sbre) {
            throw new SOperationExecutionException(sbre);
        } catch (final ReflectException re) {
            throw new SOperationExecutionException(re);
        } catch (final SRefBusinessDataInstanceModificationException srbsme) {
            throw new SOperationExecutionException(srbsme);
        } catch (SFlowNodeNotFoundException e) {
            throw new SOperationExecutionException(e);
        } catch (SFlowNodeReadException e) {
            throw new SOperationExecutionException(e);
        }
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
