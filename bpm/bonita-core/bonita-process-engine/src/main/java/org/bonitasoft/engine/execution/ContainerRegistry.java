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
package org.bonitasoft.engine.execution;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
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
    private final BPMWorkFactory workFactory;

    public ContainerRegistry(final WorkService workService, BPMWorkFactory workFactory) {
        super();
        this.workService = workService;
        this.workFactory = workFactory;
    }

    public void addContainerExecutor(final ContainerExecutor containerExecutor) {
        executors.put(containerExecutor.getHandledType(), containerExecutor);
    }

    public void notifyChildFinished(SFlowNodeInstance flowNodeInstance) throws SWorkRegisterException {
        workService.registerWork(workFactory.createNotifyChildFinishedWorkDescriptor(flowNodeInstance));
    }

    public void nodeReachedState(SFlowNodeInstance flowNodeInstance)
            throws SBonitaException {
        final ContainerExecutor containerExecutor = executors.get(flowNodeInstance.getParentContainerType().name());
        if (containerExecutor != null) {
            containerExecutor.childFinished(flowNodeInstance.getProcessDefinitionId(),
                    flowNodeInstance.getParentContainerId(), flowNodeInstance);
        } else {
            throw new SActivityExecutionException(
                    "There is no container executor for the container " + flowNodeInstance.getParentContainerId()
                            + " having the type " + flowNodeInstance.getParentContainerType());
        }
    }

    private ContainerExecutor getContainerExecutor(final String containerType) {
        return executors.get(containerType);
    }

    public void executeFlowNode(SFlowNodeInstance flowNodeInstance) throws SWorkRegisterException {
        workService.registerWork(workFactory.createExecuteFlowNodeWorkDescriptor(flowNodeInstance));
    }

    // FIXME, we should never execute a flow node directly, all call to this method should be replaced by a work
    @Deprecated
    public void executeFlowNodeInSameThread(final SFlowNodeInstance flowNodeInstance,
            final String containerType) throws SFlowNodeReadException, SFlowNodeExecutionException {
        final ContainerExecutor containerExecutor = getContainerExecutor(containerType);
        containerExecutor.executeFlowNode(flowNodeInstance, null, null);
    }
}
