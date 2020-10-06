/**
 * Copyright (C) 2020 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.LeftOperandHandler;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.springframework.stereotype.Component;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
@Component
public class StringIndexLeftOperandHandler implements LeftOperandHandler {

    private final ProcessInstanceService processInstanceService;

    private final ActivityInstanceService activityInstanceService;

    public StringIndexLeftOperandHandler(final ProcessInstanceService processInstanceService,
            final ActivityInstanceService activityInstanceService) {
        this.processInstanceService = processInstanceService;
        this.activityInstanceService = activityInstanceService;
    }

    @Override
    public Object update(final SLeftOperand sLeftOperand, Map<String, Object> inputValues, final Object newValue,
            final long containerId, final String containerType)
            throws SOperationExecutionException {
        final String name = sLeftOperand.getName();
        int index;
        try {
            index = Integer.parseInt(name);
        } catch (final NumberFormatException e) {
            throw new SOperationExecutionException(
                    "name of left operand for string index operation must be 1,2,3,4 or 5 but was " + name);
        }
        if (index < 1 || index > 5) {
            throw new SOperationExecutionException(
                    "name of left operand for string index operation must be 1,2,3,4 or 5 but was " + name);
        }
        if (newValue != null && !(newValue instanceof String)) {
            throw new SOperationExecutionException(
                    "expression of string index operation must return a string, was:" + newValue.getClass().getName());
        }
        try {
            SProcessInstance processInstance;
            if (DataInstanceContainer.PROCESS_INSTANCE.name().equals(containerType)) {
                processInstance = processInstanceService.getProcessInstance(containerId);
            } else {
                final SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(containerId);
                processInstance = processInstanceService
                        .getProcessInstance(flowNodeInstance.getParentProcessInstanceId());
            }
            if (processInstance.getCallerType() == SFlowNodeType.SUB_PROCESS) {
                SFlowNodeInstance subProcessActivity = activityInstanceService
                        .getFlowNodeInstance(processInstance.getCallerId());
                processInstance = processInstanceService
                        .getProcessInstance(subProcessActivity.getParentProcessInstanceId());
            }
            processInstanceService.updateProcess(processInstance,
                    new EntityUpdateDescriptor().addField(SProcessInstance.STRING_INDEX_KEY + index, newValue));
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
    public void delete(final SLeftOperand leftOperand, final long containerId, final String containerType)
            throws SOperationExecutionException {
        throw new SOperationExecutionException("Deleting a string index is not supported");
    }

    @Override
    public void loadLeftOperandInContext(SLeftOperand sLeftOperand, long leftOperandContainerId,
            String leftOperandContainerType, SExpressionContext contextToSet) throws SBonitaReadException {
        //do nothing
    }

    @Override
    public void loadLeftOperandInContext(List<SLeftOperand> sLeftOperandList, long leftOperandContainerId,
            String leftOperandContainerType, SExpressionContext contextToSet) throws SBonitaReadException {
        //do nothing
    }

}
