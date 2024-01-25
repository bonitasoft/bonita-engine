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

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.core.data.instance.TransientDataService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.LeftOperandHandler;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.springframework.stereotype.Component;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */

@Slf4j
@Component
public class TransientDataLeftOperandHandler implements LeftOperandHandler {

    private static final String TRANSIENT_DATA = "%TRANSIENT_DATA%_";

    private final TransientDataService transientDataService;

    public TransientDataLeftOperandHandler(TransientDataService transientDataService) {
        this.transientDataService = transientDataService;
    }

    @Override
    public String getType() {
        return LeftOperand.TYPE_TRANSIENT_DATA;
    }

    @Override
    public Object update(final SLeftOperand sLeftOperand, Map<String, Object> inputValues, final Object newValue,
            final long containerId, final String containerType)
            throws SOperationExecutionException {
        SDataInstance dataInstance;
        try {

            dataInstance = (SDataInstance) inputValues.get(TRANSIENT_DATA + sLeftOperand.getName());
            if (dataInstance == null) {
                dataInstance = retrieve(sLeftOperand, containerId, containerType);
            }
            final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
            descriptor.addField("value", newValue);
            log.warn(
                    "The value of the transient data "
                            + sLeftOperand.getName()
                            + " of "
                            + containerId
                            + " "
                            + containerType
                            + " is being updated, be carefull if the application server is restarted this new value will be lost and the data will be reset to its initial value. "
                            + "We advise you to change the design of your process. If you understand the risks and want to hide this warning, change the logging level of this class to error.");
            transientDataService.updateDataInstance(dataInstance, descriptor);
            return newValue;
        } catch (final SDataInstanceException | SBonitaReadException e) {
            throw new SOperationExecutionException("Unable to update the transient data", e);
        }
    }

    @Override
    public void delete(final SLeftOperand leftOperand, final long containerId, final String containerType)
            throws SOperationExecutionException {
        throw new SOperationExecutionException("Deleting a transient data is not supported");
    }

    @Override
    public void loadLeftOperandInContext(final SLeftOperand sLeftOperand, final long leftOperandContainerId,
            final String leftOperandContainerType, final SExpressionContext expressionContext)
            throws SBonitaReadException {
        String name = sLeftOperand.getName();
        SDataInstance dataInstance = retrieve(sLeftOperand, leftOperandContainerId, leftOperandContainerType);
        expressionContext.getInputValues().put(TRANSIENT_DATA + name, dataInstance);
        if (!expressionContext.getInputValues().containsKey(name)) {
            expressionContext.getInputValues().put(name, dataInstance.getValue());
        }
    }

    private SDataInstance retrieve(final SLeftOperand sLeftOperand, final Long containerId, final String containerType)
            throws SBonitaReadException {
        try {
            return transientDataService.getDataInstance(sLeftOperand.getName(), containerId, containerType);
        } catch (final SDataInstanceException e) {
            throw new SBonitaReadException("Unable to read the transient data", e);
        }
    }

    @Override
    public void loadLeftOperandInContext(final List<SLeftOperand> sLeftOperand, final long leftOperandContainerId,
            final String leftOperandContainerType, final SExpressionContext expressionContext)
            throws SBonitaReadException {
        for (SLeftOperand leftOperand : sLeftOperand) {
            loadLeftOperandInContext(leftOperand, leftOperandContainerId, leftOperandContainerType, expressionContext);
        }
    }

}
