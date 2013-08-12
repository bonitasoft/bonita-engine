/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.SEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SSignalEventTriggerDefinition;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventCreationException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingSignalEventBuilder;
import org.bonitasoft.engine.core.process.instance.model.event.SCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingSignalEvent;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowSignalEventTriggerInstance;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public class SignalEventHandlerStrategy extends CoupleEventHandlerStrategy {

    private static final OperationsWithContext EMPTY = new OperationsWithContext(null, null);

    private final EventsHandler eventsHandler;

    public SignalEventHandlerStrategy(final EventsHandler eventsHandler, final BPMInstanceBuilders instanceBuilders,
            final EventInstanceService eventInstanceService) {
        super(instanceBuilders, eventInstanceService);
        this.eventsHandler = eventsHandler;
    }

    @Override
    public void handleCatchEvent(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition, final SCatchEventInstance eventInstance,
            final SEventTriggerDefinition sEventTriggerDefinition) throws SBonitaException {
        final SWaitingSignalEventBuilder builder = getInstanceBuilders().getSWaitingSignalEventBuilder();
        final SSignalEventTriggerDefinition sSignalEventTriggerDefinition = (SSignalEventTriggerDefinition) sEventTriggerDefinition;
        switch (eventDefinition.getType()) {
            case BOUNDARY_EVENT:
                builder.createNewWaitingSignalBoundaryEventInstance(processDefinition.getId(), eventInstance.getParentContainerId(), eventInstance.getId(),
                        sSignalEventTriggerDefinition.getSignalName(), processDefinition.getName(), eventDefinition.getId(), eventInstance.getName());
                break;
            case INTERMEDIATE_CATCH_EVENT:
                builder.createNewWaitingSignalIntermediateEventInstance(processDefinition.getId(), eventInstance.getParentContainerId(), eventInstance.getId(),
                        sSignalEventTriggerDefinition.getSignalName(), processDefinition.getName(), eventDefinition.getId(), eventInstance.getName());
                break;
            case START_EVENT:
                builder.createNewWaitingSignalStartEventInstance(processDefinition.getId(), sSignalEventTriggerDefinition.getSignalName(),
                        processDefinition.getName(), eventDefinition.getId(), eventDefinition.getName());
                break;
            default:
                throw new SWaitingEventCreationException(eventDefinition.getType() + " is not a catch event.");
        }

        final SWaitingSignalEvent signalEvent = builder.done();
        getEventInstanceService().createWaitingEvent(signalEvent);
    }

    @Override
    public void handleThrowEvent(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition, final SThrowEventInstance eventInstance,
            final SEventTriggerDefinition sEventTriggerDefinition) throws SBonitaException {
        final long eventInstanceId = eventInstance.getId();
        handleThrowSignal(sEventTriggerDefinition, eventInstanceId);
    }

    private void handleThrowSignal(final SEventTriggerDefinition sEventTriggerDefinition, final long eventInstanceId) throws SBonitaException {
        final SSignalEventTriggerDefinition signalTrigger = (SSignalEventTriggerDefinition) sEventTriggerDefinition;
        final SThrowSignalEventTriggerInstance signalEventTriggerInstance = getInstanceBuilders().getSThrowSignalEventTriggerInstanceBuilder()
                .createNewInstance(eventInstanceId, signalTrigger.getSignalName()).done();
        getEventInstanceService().createEventTriggerInstance(signalEventTriggerInstance);
        final List<SWaitingSignalEvent> listeningSignals = getEventInstanceService().getWaitingSignalEvents(signalTrigger.getSignalName());
        for (final SWaitingSignalEvent listeningSignal : listeningSignals) {
            eventsHandler.triggerCatchEvent(listeningSignal, null);
        }
    }

    @Override
    public OperationsWithContext getOperations(final SWaitingEvent waitingEvent, final Long triggeringElementID) {
        return EMPTY;
    }

    @Override
    public void handleThrowEvent(final SEventTriggerDefinition sEventTriggerDefinition) throws SBonitaException {
        handleThrowSignal(sEventTriggerDefinition, -1);
    }

    @Override
    public void handleEventSubProcess(final SProcessDefinition processDefinition, final SEventDefinition eventDefinition,
            final SEventTriggerDefinition sEventTriggerDefinition, final long subProcessId, final SProcessInstance parentProcessInstance)
            throws SBonitaException {
        final SWaitingSignalEventBuilder builder = getInstanceBuilders().getSWaitingSignalEventBuilder();
        final SSignalEventTriggerDefinition sSignalEventTriggerDefinition = (SSignalEventTriggerDefinition) sEventTriggerDefinition;
        builder.createNewWaitingSignalEventSubProcInstance(processDefinition.getId(), parentProcessInstance.getId(),
                parentProcessInstance.getRootProcessInstanceId(), sSignalEventTriggerDefinition.getSignalName(), processDefinition.getName(),
                eventDefinition.getId(), eventDefinition.getName(), subProcessId);

        final SWaitingSignalEvent signalEvent = builder.done();
        getEventInstanceService().createWaitingEvent(signalEvent);
    }

    @Override
    public boolean handlePostThrowEvent(final SProcessDefinition processDefinition, final SEndEventDefinition sEventDefinition,
            final SThrowEventInstance sThrowEventInstance,
            final SEventTriggerDefinition sEventTriggerDefinition, final SFlowNodeInstance sFlowNodeInstance) {
        // nothing to do
        return false;
    }
}
