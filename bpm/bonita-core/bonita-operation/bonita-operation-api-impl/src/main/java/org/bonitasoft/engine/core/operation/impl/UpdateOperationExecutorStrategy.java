/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.operation.impl;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.operation.OperationExecutorStrategy;
import org.bonitasoft.engine.core.operation.exception.SOperationExecutionException;
import org.bonitasoft.engine.core.operation.model.SLeftOperand;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.data.instance.model.SDataInstance;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilderFactory;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Matthieu Chaffotte
 */
public abstract class UpdateOperationExecutorStrategy implements OperationExecutorStrategy {

    private final DataInstanceService dataInstanceService;

    public UpdateOperationExecutorStrategy(final DataInstanceService dataInstanceService) {
        this.dataInstanceService = dataInstanceService;
    }

    protected void update(final SDataInstance sDataInstance, final Object content) throws SDataInstanceException {
        final EntityUpdateDescriptor updateDescriptor = new EntityUpdateDescriptor();
        final SDataInstanceBuilderFactory fact = BuilderFactory.get(SDataInstanceBuilderFactory.class);
        updateDescriptor.addField(fact.getValueKey(), content);

        dataInstanceService.updateDataInstance(sDataInstance, updateDescriptor);
    }

    protected SDataInstance getDataInstance(final String dataInstanceName, final long containerId, final String containerType)
            throws SDataInstanceException {
        return dataInstanceService.getDataInstance(dataInstanceName, containerId, containerType);
    }

    @Override
    public void update(final SLeftOperand leftOperand, final Object newValue, final long containerId, final String containerType)
            throws SOperationExecutionException {
        // external data are already updated in the input values map
        if (!leftOperand.isExternal()) {
            updateDataInstance(leftOperand, containerId, containerType, newValue);
        }
    }

    @Override
    public boolean shouldPerformUpdateAtEnd() {
        return true;
    }

    private void updateDataInstance(final SLeftOperand leftOperand, final long containerId, final String containerType, final Object expressionResult)
            throws SOperationExecutionException {
        final String dataInstanceName = leftOperand.getName();
        SDataInstance sDataInstance;
        try {
            sDataInstance = getDataInstance(dataInstanceName, containerId, containerType);
            update(sDataInstance, expressionResult);
        } catch (final SDataInstanceException e) {
            throw new SOperationExecutionException(e);
        }
    }

}
