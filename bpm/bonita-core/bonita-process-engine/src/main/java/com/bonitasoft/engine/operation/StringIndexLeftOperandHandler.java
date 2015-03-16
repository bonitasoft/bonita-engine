/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.operation;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.LeftOperandHandler;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.persistence.SBonitaReadException;

import com.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceUpdateBuilder;
import com.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceUpdateBuilderFactory;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class StringIndexLeftOperandHandler implements LeftOperandHandler {

    private final ProcessInstanceService processInstanceService;

    private final ActivityInstanceService activityInstanceService;

    public StringIndexLeftOperandHandler(final ProcessInstanceService processInstanceService, final ActivityInstanceService activityInstanceService) {
        this.processInstanceService = processInstanceService;
        this.activityInstanceService = activityInstanceService;
    }

    @Override
    public Object update(final SLeftOperand sLeftOperand, Map<String, Object> inputValues, final Object newValue, final long containerId, final String containerType)
            throws SOperationExecutionException {
        final String name = sLeftOperand.getName();
        Integer index;
        try {
            index = Integer.valueOf(name);
        } catch (final NumberFormatException e) {
            throw new SOperationExecutionException("name of left operand for string index operation must be 1,2,3,4 or 5 but was " + name);
        }
        if (newValue != null && !(newValue instanceof String)) {
            throw new SOperationExecutionException("expression of string index operation must return a string, was:" + newValue.getClass().getName());
        }
        try {
            SProcessInstance processInstance;
            if (DataInstanceContainer.PROCESS_INSTANCE.name().equals(containerType)) {
                processInstance = processInstanceService.getProcessInstance(containerId);
            } else {
                final SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(containerId);
                processInstance = processInstanceService.getProcessInstance(flowNodeInstance.getRootContainerId());
            }
            final String stringValue = (String) newValue;
            final SProcessInstanceUpdateBuilder updateBuilder = BuilderFactory.get(SProcessInstanceUpdateBuilderFactory.class).createNewInstance();
            switch (index) {
                case 1:
                    updateBuilder.updateStringIndex1(stringValue);
                    break;
                case 2:
                    updateBuilder.updateStringIndex2(stringValue);
                    break;
                case 3:
                    updateBuilder.updateStringIndex3(stringValue);
                    break;
                case 4:
                    updateBuilder.updateStringIndex4(stringValue);
                    break;
                case 5:
                    updateBuilder.updateStringIndex5(stringValue);
                    break;
                default:
                    throw new SOperationExecutionException("name of left operand for string index operation must be 1,2,3,4 or 5");
            }
            processInstanceService.updateProcess(processInstance, updateBuilder.done());
            return newValue;
        } catch (final SBonitaException e) {
            throw new SOperationExecutionException(e);
        }
    }

    @Override
    public String getType() {
        return SLeftOperand.TYPE_SEARCH_INDEX;
    }

    @Override
    public void delete(final SLeftOperand leftOperand, final long containerId, final String containerType) throws SOperationExecutionException {
        throw new SOperationExecutionException("Deleting a string index is not supported");
    }

    @Override
    public void loadLeftOperandInContext(final SLeftOperand sLeftOperand, final SExpressionContext expressionContext, Map<String, Object> contextToSet) {
        // don't retrieve it, not useful
    }


    @Override
    public void loadLeftOperandInContext(final List<SLeftOperand> sLeftOperand, final SExpressionContext expressionContext, Map<String, Object> contextToSet) throws SBonitaReadException {
        for (SLeftOperand leftOperand : sLeftOperand) {
            loadLeftOperandInContext(leftOperand, expressionContext, contextToSet);
        }
    }

}
