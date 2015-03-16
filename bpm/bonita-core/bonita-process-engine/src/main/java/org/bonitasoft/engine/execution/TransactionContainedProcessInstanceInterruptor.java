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

import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class TransactionContainedProcessInstanceInterruptor extends AbstractProcessInstanceInterruptor {

    private final ProcessInstanceService processInstanceService;

    private final FlowNodeInstanceService flowNodeInstanceService;

    private final ContainerRegistry containerRegistry;

    public TransactionContainedProcessInstanceInterruptor(final ProcessInstanceService processInstanceService,
            final FlowNodeInstanceService flowNodeInstanceService, final ContainerRegistry containerRegistry, final TechnicalLoggerService logger) {
        super(logger);
        this.processInstanceService = processInstanceService;
        this.flowNodeInstanceService = flowNodeInstanceService;
        this.containerRegistry = containerRegistry;
    }

    @Override
    protected void setProcessStateCategory(final long processInstanceId, final SStateCategory stateCategory) throws SProcessInstanceNotFoundException,
            SProcessInstanceReadException, SProcessInstanceModificationException {
        final SProcessInstance processInstance = processInstanceService.getProcessInstance(processInstanceId);
        processInstanceService.setStateCategory(processInstance, stateCategory);
    }

    @Override
    protected void resumeChildExecution(final long childId, final long processInstanceId, final long userId) throws SBonitaException {
        final SFlowNodeInstance flowNodeInstance = flowNodeInstanceService.getFlowNodeInstance(childId);
        final SFlowNodeInstanceBuilderFactory flowNodeKeyProvider = BuilderFactory.get(SUserTaskInstanceBuilderFactory.class);

        containerRegistry.executeFlowNode(flowNodeInstance.getProcessDefinitionId(), flowNodeInstance.getLogicalGroup(flowNodeKeyProvider.getParentProcessInstanceIndex()), flowNodeInstance.getId(), null,
                null);
    }

    @Override
    protected List<SFlowNodeInstance> getChildren(final long processInstanceId) throws SBonitaException {
        final List<SFlowNodeInstance> flowNodeInstances = flowNodeInstanceService.searchFlowNodeInstances(SFlowNodeInstance.class,
                getQueryOptions(processInstanceId));
        return flowNodeInstances;
    }

    @Override
    protected List<SFlowNodeInstance> getChildrenExcept(final long processInstanceId, final long childExceptionId) throws SBonitaException {
        final List<SFlowNodeInstance> flowNodeInstances = flowNodeInstanceService.searchFlowNodeInstances(SFlowNodeInstance.class,
                getQueryOptions(processInstanceId, childExceptionId));
        return flowNodeInstances;
    }

    @Override
    protected long getNumberOfChildren(final long processInstanceId) throws SBonitaReadException {
        final QueryOptions countOptions = new QueryOptions(0, 1, null, getFilterOptions(processInstanceId), null);
        return flowNodeInstanceService.getNumberOfFlowNodeInstances(SFlowNodeInstance.class, countOptions);
    }

    @Override
    protected long getNumberOfChildrenExcept(final long processInstanceId, final long childExceptionId) throws SBonitaReadException {
        final QueryOptions countOptions = new QueryOptions(0, 1, null, getFilterOptions(processInstanceId, childExceptionId), null);
        return flowNodeInstanceService.getNumberOfFlowNodeInstances(SFlowNodeInstance.class, countOptions);
    }

    @Override
    protected void setChildStateCategory(final long flowNodeInstanceId, final SStateCategory stateCategory) throws SBonitaException {
        final SFlowNodeInstance flowNodeInstance = flowNodeInstanceService.getFlowNodeInstance(flowNodeInstanceId);
        flowNodeInstanceService.setStateCategory(flowNodeInstance, stateCategory);
    }

}
