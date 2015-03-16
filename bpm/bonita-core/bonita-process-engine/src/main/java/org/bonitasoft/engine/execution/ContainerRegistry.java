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
package org.bonitasoft.engine.execution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.execution.work.WorkFactory;
import org.bonitasoft.engine.work.SWorkRegisterException;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class ContainerRegistry {

    private final Map<String, ContainerExecutor> executors = new HashMap<String, ContainerExecutor>(2);

    private final WorkService workService;

    public ContainerRegistry(final WorkService workService) {
        super();
        this.workService = workService;
    }

    public void addContainerExecutor(final ContainerExecutor containerExecutor) {
        executors.put(containerExecutor.getHandledType(), containerExecutor);
    }

    public void nodeReachedState(final long processDefinitionId, final long flowNodeInstanceId, final long parentId, final String parentType)
            throws SBonitaException {
        final ContainerExecutor containerExecutor = executors.get(parentType);
        if (containerExecutor != null) {
            containerExecutor.childFinished(processDefinitionId, flowNodeInstanceId, parentId);
        } else {
            throw new SActivityExecutionException("There is no container executor for the container " + parentId + " having the type " + parentType);
        }
    }

    private ContainerExecutor getContainerExecutor(final String containerType) {
        return executors.get(containerType);
    }

    public void executeFlowNode(final long processDefinitionId, final long processInstanceId, final long flowNodeInstanceId,
            final SExpressionContext contextDependency, final List<SOperation> operations) throws SWorkRegisterException {
        workService.registerWork(WorkFactory.createExecuteFlowNodeWork(processDefinitionId, processInstanceId, flowNodeInstanceId, operations,
                contextDependency));
    }

    public void executeFlowNodeInSameThread(final Long processInstanceId, final long flowNodeInstanceId, final SExpressionContext contextDependency,
            final List<SOperation> operations, final String containerType) throws SFlowNodeReadException, SFlowNodeExecutionException {
        final ContainerExecutor containerExecutor = getContainerExecutor(containerType);
        containerExecutor.executeFlowNode(flowNodeInstanceId, contextDependency, operations, processInstanceId, null, null);
    }

}
