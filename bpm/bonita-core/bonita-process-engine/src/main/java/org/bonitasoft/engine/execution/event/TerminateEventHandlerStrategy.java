/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution.event;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerDefinition;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.event.SCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.TransactionContainedProcessInstanceInterruptor;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class TerminateEventHandlerStrategy extends EventHandlerStrategy {

    private static final OperationsWithContext EMPTY = new OperationsWithContext(null, null);

    private final BPMInstanceBuilders bpmInstanceBuilders;

    private final ProcessInstanceService processInstanceService;

    private final FlowNodeInstanceService activityInstanceService;

    private final ContainerRegistry containerRegistry;

    private final LockService lockService;

    private final TechnicalLoggerService logger;

    public TerminateEventHandlerStrategy(final BPMInstanceBuilders bpmInstanceBuilders, final ProcessInstanceService processInstanceService,
            final FlowNodeInstanceService activityInstanceService, final ContainerRegistry containerRegistry, final LockService lockService,
            final TechnicalLoggerService logger) {
        super();
        this.bpmInstanceBuilders = bpmInstanceBuilders;
        this.processInstanceService = processInstanceService;
        this.activityInstanceService = activityInstanceService;
        this.containerRegistry = containerRegistry;
        this.lockService = lockService;
        this.logger = logger;
    }

    @Override
    public void handleThrowEvent(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition, final SThrowEventInstance eventInstance,
            final SEventTriggerDefinition sEventTriggerDefinition) throws SBonitaException {
        final TransactionContainedProcessInstanceInterruptor processInstanceInterruptor = new TransactionContainedProcessInstanceInterruptor(
                bpmInstanceBuilders, processInstanceService, activityInstanceService, containerRegistry, lockService, logger);
        processInstanceInterruptor.interruptChildrenOnly(eventInstance.getParentContainerId(), SStateCategory.ABORTING, -1, eventInstance.getId());
        // Parent should always be process for event (but must change that id it's not the case anymore
    }

    @Override
    public void handleCatchEvent(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition, final SCatchEventInstance eventInstance,
            final SEventTriggerDefinition sEventTriggerDefinition) throws SBonitaException {
        // No catch of terminate

    }

    @Override
    public OperationsWithContext getOperations(final SWaitingEvent waitingEvent, final Long triggeringElementID) throws SBonitaException {
        return EMPTY;
    }

    @Override
    public void handleThrowEvent(final SEventTriggerDefinition sEventTriggerDefinition) throws SBonitaException {

    }

    @Override
    public void handleEventSubProcess(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition,
            final SEventTriggerDefinition sEventTriggerDefinition, final long subProcessId, final SProcessInstance parentProcessInstance)
            throws SBonitaException {
        // TODO Auto-generated method stub

    }

    @Override
    public void unregisterCatchEvent(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition,
            final SEventTriggerDefinition sEventTriggerDefinition, final long subProcessId, final SProcessInstance parentProcessIsnstance)
            throws SBonitaException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean handlePostThrowEvent(final SProcessDefinition processDefinition, final SEndEventDefinition eventDefinition,
            final SThrowEventInstance eventInstance, final SEventTriggerDefinition sEventTriggerDefinition, final SFlowNodeInstance flowNodeInstance)
            throws SBonitaException {
        // TODO Auto-generated method stub
        return false;
    }

}
