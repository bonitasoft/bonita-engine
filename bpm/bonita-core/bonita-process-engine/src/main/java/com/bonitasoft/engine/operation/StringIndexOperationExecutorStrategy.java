/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
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
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;

import com.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import com.bonitasoft.engine.core.process.instance.model.builder.SProcessInstanceUpdateBuilder;

/**
 * @author Baptiste Mesta
 */
public class StringIndexOperationExecutorStrategy implements OperationExecutorStrategy {

    public static final String TYPE_STRING_INDEX = "STRING_INDEX";

    private final ProcessInstanceService processInstanceService;

    private final BPMInstanceBuilders bpmInstanceBuilders;

    private final ActivityInstanceService activityInstanceService;

    public StringIndexOperationExecutorStrategy(final ProcessInstanceService processInstanceService, final BPMInstanceBuilders bpmInstanceBuilders,
            final ActivityInstanceService activityInstanceService) {
        this.processInstanceService = processInstanceService;
        this.bpmInstanceBuilders = bpmInstanceBuilders;
        this.activityInstanceService = activityInstanceService;
    }

    @Override
    public void execute(final SOperation operation, final Object value, final long containerId, final String containerType,
            final SExpressionContext expressionContext) throws SOperationExecutionException {
        final SProcessInstanceUpdateBuilder updateBuilder = bpmInstanceBuilders.getProcessInstanceUpdateBuilder();
        final SLeftOperand leftOperand = operation.getLeftOperand();
        final String name = leftOperand.getName();
        Integer index;
        try {
            index = Integer.valueOf(name);
        } catch (final NumberFormatException e) {
            throw new SOperationExecutionException("name of left operand for string index operation must be 1,2,3,4 or 5 but was " + name);
        }
        if (value != null && !(value instanceof String)) {
            throw new SOperationExecutionException("expression of string index operation must return a string, was:" + value.getClass().getName());
        }
        try {
            SProcessInstance processInstance;
            if (DataInstanceContainer.PROCESS_INSTANCE.name().equals(containerType)) {
                processInstance = processInstanceService.getProcessInstance(containerId);
            } else {
                final SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(containerId);
                processInstance = processInstanceService.getProcessInstance(flowNodeInstance.getRootContainerId());
            }
            final String stringValue = (String) value;
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
        } catch (final SBonitaException e) {
            throw new SOperationExecutionException(e);
        }
    }

    @Override
    public String getOperationType() {
        return TYPE_STRING_INDEX;
    }
}
