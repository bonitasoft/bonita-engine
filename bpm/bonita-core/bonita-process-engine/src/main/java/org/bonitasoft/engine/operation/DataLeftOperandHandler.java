/**
 * Copyright (C) 2019 Bonitasoft S.A.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.LeftOperandHandler;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.api.ParentContainerResolver;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.springframework.stereotype.Component;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
@Component
public class DataLeftOperandHandler implements LeftOperandHandler {

    private static final String DATA_INSTANCE = "%DATA_INSTANCE%_";
    private final DataInstanceService dataInstanceService;

    private final ParentContainerResolver parentContainerResolver;

    public DataLeftOperandHandler(final DataInstanceService dataInstanceService,
            final ParentContainerResolver parentContainerResolver) {
        this.dataInstanceService = dataInstanceService;
        this.parentContainerResolver = parentContainerResolver;
    }

    @Override
    public String getType() {
        return "DATA";
    }

    @Override
    public Object update(final SLeftOperand leftOperand, Map<String, Object> inputValues, final Object newValue,
            final long containerId, final String containerType)
            throws SOperationExecutionException {
        updateDataInstance(leftOperand, containerId, containerType, inputValues, newValue);
        return newValue;
    }

    protected void update(final SDataInstance sDataInstance, final Object content) throws SDataInstanceException {
        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        updateDescriptor.addField(SDataInstance.VALUE, content);

        dataInstanceService.updateDataInstance(sDataInstance, updateDescriptor);
    }

    private void checkReturnType(final Object value, final SDataInstance sDataInstance)
            throws SOperationExecutionException {
        if (value != null) {
            final Object dataValue = sDataInstance.getValue();
            /*
             * if the object is null (data is not initialized) the return type is not checked
             * but the data instance service should throw an exception
             */
            if (dataValue != null) {
                final Class<?> dataEffectiveType = dataValue.getClass();
                final Class<?> evaluatedReturnedType = value.getClass();
                if (!(dataEffectiveType.isAssignableFrom(evaluatedReturnedType)
                        || dataEffectiveType.equals(evaluatedReturnedType))) {
                    throw new SOperationExecutionException(
                            "Incompatible assignment operation type: Left operand " + dataEffectiveType
                                    + " is not compatible with right operand " + evaluatedReturnedType
                                    + " for expression with name '" + sDataInstance.getName() + "'");
                }
            }
        }
    }

    private void updateDataInstance(final SLeftOperand leftOperand, final long containerId, final String containerType,
            Map<String, Object> inputValues, final Object expressionResult)
            throws SOperationExecutionException {
        final String dataInstanceName = leftOperand.getName();
        SDataInstance dataInstance;
        try {
            dataInstance = (SDataInstance) inputValues.get(DATA_INSTANCE + dataInstanceName);
            if (dataInstance == null) {
                dataInstance = getDataInstance(dataInstanceName, containerId, containerType);
            }
            // Specific return type check for Data:
            checkReturnType(expressionResult, dataInstance);
            update(dataInstance, expressionResult);
        } catch (final SDataInstanceException e) {
            throw new SOperationExecutionException(e);
        }
    }

    @Override
    public void delete(final SLeftOperand leftOperand, final long containerId, final String containerType)
            throws SOperationExecutionException {
        throw new SOperationExecutionException("Deleting a data is not supported");
    }

    @Override
    public void loadLeftOperandInContext(final SLeftOperand sLeftOperand, final long leftOperandContainerId,
            final String leftOperandContainerType, final SExpressionContext expressionContext)
            throws SBonitaReadException {
        try {
            putInContext(sLeftOperand, leftOperandContainerId, leftOperandContainerType, expressionContext);
        } catch (final SDataInstanceException e) {
            throw new SBonitaReadException("Unable to retrieve the data", e);
        }
    }

    private void putInContext(SLeftOperand sLeftOperand, final long leftOperandContainerId,
            final String leftOperandContainerType, SExpressionContext expressionContext) throws SDataInstanceException {
        SDataInstance dataInstance = getDataInstance(sLeftOperand.getName(), leftOperandContainerId,
                leftOperandContainerType);
        putDataInContext(expressionContext.getInputValues(), dataInstance);
    }

    private void putDataInContext(Map<String, Object> contextToSet, SDataInstance dataInstance) {
        String name = dataInstance.getName();
        contextToSet.put(DATA_INSTANCE + name, dataInstance);
        if (!contextToSet.containsKey(name)) {
            contextToSet.put(name, dataInstance.getValue());
        }
    }

    @Override
    public void loadLeftOperandInContext(final List<SLeftOperand> sLeftOperand, final long leftOperandContainerId,
            final String leftOperandContainerType, final SExpressionContext expressionContext)
            throws SBonitaReadException {
        try {
            ArrayList<String> names = new ArrayList<>(sLeftOperand.size());
            for (SLeftOperand leftOperand : sLeftOperand) {
                names.add(leftOperand.getName());
            }
            List<SDataInstance> dataInstances = dataInstanceService.getDataInstances(names, leftOperandContainerId,
                    leftOperandContainerType, parentContainerResolver);
            for (SDataInstance dataInstance : dataInstances) {
                putDataInContext(expressionContext.getInputValues(), dataInstance);
            }
        } catch (final SDataInstanceException e) {
            throw new SBonitaReadException("Unable to retrieve the data", e);
        }
    }

    protected SDataInstance getDataInstance(final String dataInstanceName, final long containerId,
            final String containerType) throws SDataInstanceException {
        return dataInstanceService.getDataInstance(dataInstanceName, containerId, containerType,
                parentContainerResolver);
    }

}
