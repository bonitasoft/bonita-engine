/**
 * Copyright (C) 2015-2018 BonitaSoft S.A.
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.work.SWorkRegisterException;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public class ProcessInstanceInterruptor {

    private ProcessInstanceService processInstanceService;
    private FlowNodeInstanceService flowNodeInstanceService;
    private ContainerRegistry containerRegistry;
    private final TechnicalLoggerService logger;

    public ProcessInstanceInterruptor(ProcessInstanceService processInstanceService, FlowNodeInstanceService flowNodeInstanceService,
                                      ContainerRegistry containerRegistry, final TechnicalLoggerService technicalLoggerService) {
        this.processInstanceService = processInstanceService;
        this.flowNodeInstanceService = flowNodeInstanceService;
        this.containerRegistry = containerRegistry;
        logger = technicalLoggerService;
    }

    public void interruptProcessInstance(final long processInstanceId, final SStateCategory stateCategory)
            throws SBonitaException {
        processInstanceService.setStateCategory(processInstanceService.getProcessInstance(processInstanceId), stateCategory);
        final List<SFlowNodeInstance> stableChildrenIds = interruptChildrenFlowNodeInstances(processInstanceId, stateCategory, -1);
        for (final SFlowNodeInstance child : stableChildrenIds) {
            executeFlowNode(child);
        }
    }

    private void executeFlowNode(SFlowNodeInstance child) throws SWorkRegisterException {
        containerRegistry.executeFlowNode(child.getProcessDefinitionId(), child.getParentProcessInstanceId(), child.getId());
    }

    public void interruptProcessInstance(final long processInstanceId, final SStateCategory stateCategory, final long exceptionChildId)
            throws SBonitaException {
        processInstanceService.setStateCategory(processInstanceService.getProcessInstance(processInstanceId), stateCategory);
        final List<SFlowNodeInstance> stableChildrenIds = interruptChildrenFlowNodeInstances(processInstanceId, stateCategory, exceptionChildId);
        if (stableChildrenIds != null) {
            for (final SFlowNodeInstance child : stableChildrenIds) {
                executeFlowNode(child);
            }
        }
    }

    public void interruptChildrenOnly(final long processInstanceId, final SStateCategory stateCategory, final long interruptorChildId)
            throws SBonitaException {
        List<SFlowNodeInstance> stableChildrenIds = interruptChildrenFlowNodeInstances(processInstanceId, stateCategory, interruptorChildId);
        for (final SFlowNodeInstance child : stableChildrenIds) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Resume child in stateCategory " + stateCategory + " id = " + child.getId());
            }
            executeFlowNode(child);
        }
    }

    private List<SFlowNodeInstance> interruptChildrenFlowNodeInstances(long processInstanceId, SStateCategory stateCategory,
                                                                       long exceptionChildId) throws SBonitaException {
        List<SFlowNodeInstance> flowNodeInstances = flowNodeInstanceService.getFlowNodeInstances(processInstanceId, 0, Integer.MAX_VALUE)
                .stream()
                .filter(f -> f.getId() != exceptionChildId)
                .collect(Collectors.toList());
        return interruptFlowNodeInstances(flowNodeInstances, stateCategory);
    }

    private List<SFlowNodeInstance> interruptFlowNodeInstances(final List<SFlowNodeInstance> children, final SStateCategory stateCategory)
            throws SBonitaException {
        final List<SFlowNodeInstance> childrenToResume = new ArrayList<>();
        for (final SFlowNodeInstance child : children) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.DEBUG)) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG,
                        "Put element in " + stateCategory + ", id= " + child.getId() + " name = " + child.getName() + " state = " + child.getStateName());
            }
            flowNodeInstanceService.setStateCategory(child, stateCategory);
            if (child.mustExecuteOnAbortOrCancelProcess()) {
                childrenToResume.add(child);
            }
        }
        return childrenToResume;
    }

}
