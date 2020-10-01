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
package org.bonitasoft.engine.core.operation.impl;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.LeftOperandHandler;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.springframework.stereotype.Component;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
@Component
public class ExternalDataLeftOperandHandler implements LeftOperandHandler {

    @Override
    public String getType() {
        return "EXTERNAL_DATA";
    }

    @Override
    public Object update(final SLeftOperand leftOperand, Map<String, Object> inputValues, final Object newValue,
            final long containerId, final String containerType) {
        // nothing to do, the value is already changed in the context
        return newValue;
    }

    @Override
    public void delete(final SLeftOperand leftOperand, final long containerId, final String containerType)
            throws SOperationExecutionException {
        throw new SOperationExecutionException("Deleting an external data is not supported");
    }

    @Override
    public void loadLeftOperandInContext(final SLeftOperand sLeftOperand, final long leftOperandContainerId,
            final String leftOperandContainerType, final SExpressionContext expressionContext) {
        String name = sLeftOperand.getName();
        if (!expressionContext.getInputValues().containsKey(name)) {
            expressionContext.getInputValues().put(name, expressionContext.getInputValues().get(name));
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
