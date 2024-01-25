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
package org.bonitasoft.engine.core.process.instance.event.impl;

import java.util.List;
import java.util.Optional;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceRepository;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.SEventInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.SEventInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageEventCouple;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingErrorEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingSignalEvent;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.exception.SDataInstanceException;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

public class EventInstanceServiceImpl implements EventInstanceService {

    private final DataInstanceService dataInstanceService;
    public static final String BONITA_BPMENGINE_MESSAGE_SENT = "bonita.bpmengine.message.sent";
    private final Counter messageSentCounter;

    private final EventInstanceRepository eventInstanceRepository;

    public EventInstanceServiceImpl(EventInstanceRepository eventInstanceRepository,
            DataInstanceService dataInstanceService, MeterRegistry meterRegistry, Long tenantId) {
        this.eventInstanceRepository = eventInstanceRepository;
        this.dataInstanceService = dataInstanceService;
        messageSentCounter = meterRegistry.counter(BONITA_BPMENGINE_MESSAGE_SENT, "tenant", tenantId.toString());
    }

    @Override
    public void createEventInstance(SEventInstance eventInstance)
            throws SEventInstanceCreationException {
        this.eventInstanceRepository.createEventInstance(eventInstance);
    }

    @Override
    public void createTimerEventTriggerInstance(STimerEventTriggerInstance sEventTriggerInstance)
            throws SEventTriggerInstanceCreationException {
        this.eventInstanceRepository.createTimerEventTriggerInstance(sEventTriggerInstance);
    }

    @Override
    public void createMessageInstance(SMessageInstance messageInstance) throws SMessageInstanceCreationException {
        messageSentCounter.increment();
        this.eventInstanceRepository.createMessageInstance(messageInstance);
    }

    @Override
    public void createWaitingEvent(SWaitingEvent sWaitingEvent) throws SWaitingEventCreationException {
        this.eventInstanceRepository.createWaitingEvent(sWaitingEvent);
    }

    @Override
    public void deleteEventTriggerInstance(STimerEventTriggerInstance sTimerEventTriggerInstance)
            throws SEventTriggerInstanceDeletionException {
        this.eventInstanceRepository.deleteEventTriggerInstance(sTimerEventTriggerInstance);
    }

    @Override
    public void deleteEventTriggerInstanceOfFlowNode(long flowNodeInstanceId)
            throws SBonitaReadException, SEventTriggerInstanceDeletionException {
        Optional<STimerEventTriggerInstance> timerEventTriggerInstanceOfFlowNode = getTimerEventTriggerInstanceOfFlowNode(
                flowNodeInstanceId);
        if (!timerEventTriggerInstanceOfFlowNode.isPresent()) {
            return;
        }
        deleteEventTriggerInstance(timerEventTriggerInstanceOfFlowNode.get());
    }

    @Override
    public void deleteMessageInstance(SMessageInstance messageInstance) throws SMessageModificationException {
        this.eventInstanceRepository.deleteMessageInstance(messageInstance);
    }

    @Override
    public void deleteWaitingEvent(SWaitingEvent sWaitingEvent) throws SWaitingEventModificationException {
        this.eventInstanceRepository.deleteWaitingEvent(sWaitingEvent);
    }

    @Override
    public void deleteWaitingEvents(SFlowNodeInstance flowNodeInstance)
            throws SWaitingEventModificationException, SEventTriggerInstanceReadException {
        List<SWaitingEvent> waitingEvents;
        do {
            waitingEvents = this.eventInstanceRepository.getWaitingEventsForFlowNodeId(flowNodeInstance.getId());
            for (final SWaitingEvent sWaitingEvent : waitingEvents) {
                deleteWaitingEvent(sWaitingEvent);
            }
        } while (waitingEvents.size() == 100);

    }

    @Override
    public void deleteWaitingEvents(SProcessInstance processInstance)
            throws SWaitingEventModificationException, SEventTriggerInstanceReadException {
        List<SWaitingEvent> waitingEvents;
        final int pageCount = 100;
        do {
            waitingEvents = this.eventInstanceRepository.getAllWaitingEventsForProcessInstance(processInstance.getId(),
                    0, pageCount);
            for (final SWaitingEvent sWaitingEvent : waitingEvents) {
                deleteWaitingEvent(sWaitingEvent);
            }
        } while (waitingEvents.size() == pageCount);

    }

    @Override
    public List<SBoundaryEventInstance> getActivityBoundaryEventInstances(long id, int fromIndex,
            int maxResults) throws SEventInstanceReadException {
        return this.eventInstanceRepository.getActivityBoundaryEventInstances(id, fromIndex, maxResults);
    }

    @Override
    public SWaitingErrorEvent getBoundaryWaitingErrorEvent(long id, String catchingErrorCode)
            throws SWaitingEventReadException {
        return this.eventInstanceRepository.getBoundaryWaitingErrorEvent(id, catchingErrorCode);
    }

    @Override
    public List<SEventInstance> getEventInstances(long rootContainerId, int fromIndex, int maxResults, String fieldName,
            OrderByType orderByType) throws SEventInstanceReadException {
        return this.eventInstanceRepository.getEventInstances(rootContainerId, fromIndex, maxResults, fieldName,
                orderByType);
    }

    @Override
    public <T extends STimerEventTriggerInstance> T getEventTriggerInstance(Class<T> entityClass,
            long eventTriggerInstanceId) throws SEventTriggerInstanceReadException {
        return this.eventInstanceRepository.getEventTriggerInstance(entityClass, eventTriggerInstanceId);
    }

    @Override
    public int resetProgressMessageInstances() throws SMessageModificationException {
        return this.eventInstanceRepository.resetProgressMessageInstances();
    }

    @Override
    public int resetInProgressWaitingEvents() throws SWaitingEventModificationException {
        return this.eventInstanceRepository.resetInProgressWaitingEvents();
    }

    @Override
    public List<SMessageEventCouple> getMessageEventCouples(int i, int maxCouples)
            throws SEventTriggerInstanceReadException {
        return this.eventInstanceRepository.getMessageEventCouples(i, maxCouples);
    }

    @Override
    public SMessageInstance getMessageInstance(long messageInstanceId) throws SMessageInstanceReadException {
        return this.eventInstanceRepository.getMessageInstance(messageInstanceId);
    }

    @Override
    public long getNumberOfWaitingEvents(final Class<? extends SWaitingEvent> sWaitingEventClass,
            QueryOptions searchOptions) throws SBonitaReadException {
        return this.eventInstanceRepository.getNumberOfWaitingEvents(sWaitingEventClass, searchOptions);
    }

    @Override
    public List<SWaitingEvent> getStartWaitingEventsOfProcessDefinition(long processDefinitionId)
            throws SBonitaReadException {
        return this.eventInstanceRepository.getStartWaitingEventsOfProcessDefinition(processDefinitionId);
    }

    @Override
    public SWaitingMessageEvent getWaitingMessage(long waitingMessageId) throws SWaitingEventReadException {
        return this.eventInstanceRepository.getWaitingMessage(waitingMessageId);
    }

    @Override
    public List<SWaitingSignalEvent> getWaitingSignalEvents(String signalName, int fromIndex, int maxResults)
            throws SEventTriggerInstanceReadException {
        return this.eventInstanceRepository.getWaitingSignalEvents(signalName, fromIndex, maxResults);
    }

    @Override
    public SWaitingSignalEvent getWaitingSignalEvent(long signalId)
            throws SEventTriggerInstanceReadException, SEventTriggerInstanceNotFoundException {
        return this.eventInstanceRepository.getWaitingSignalEvent(signalId);
    }

    @Override
    public Optional<STimerEventTriggerInstance> getTimerEventTriggerInstanceOfFlowNode(long flowNodeInstanceId)
            throws SBonitaReadException {
        return this.eventInstanceRepository.getTimerEventTriggerInstanceOfFlowNode(flowNodeInstanceId);
    }

    @Override
    public long getNumberOfTimerEventTriggerInstances(long processInstanceId, QueryOptions searchOptions)
            throws SBonitaReadException {
        return this.eventInstanceRepository.getNumberOfTimerEventTriggerInstances(processInstanceId, searchOptions);
    }

    @Override
    public List<STimerEventTriggerInstance> searchTimerEventTriggerInstances(long processInstanceId,
            QueryOptions searchOptions) throws SBonitaReadException {
        return this.eventInstanceRepository.searchTimerEventTriggerInstances(processInstanceId, searchOptions);
    }

    @Override
    public <T extends SWaitingEvent> List<T> searchWaitingEvents(Class<T> waitingEventClass,
            QueryOptions queryOptions) throws SBonitaReadException {
        return this.eventInstanceRepository.searchWaitingEvents(waitingEventClass, queryOptions);
    }

    @Override
    public void updateMessageInstance(SMessageInstance messageInstance, EntityUpdateDescriptor descriptor)
            throws SMessageModificationException {
        this.eventInstanceRepository.updateMessageInstance(messageInstance, descriptor);
    }

    @Override
    public void updateWaitingMessage(SWaitingMessageEvent waitingMsg, EntityUpdateDescriptor descriptor)
            throws SWaitingEventModificationException {
        this.eventInstanceRepository.updateWaitingMessage(waitingMsg, descriptor);
    }

    @Override
    public Integer deleteMessageAndDataInstanceOlderThanCreationDate(long creationDate,
            QueryOptions queryOptions)
            throws SMessageModificationException {

        try {
            List<Long> messageInstanceIdOlderThanCreationDate = eventInstanceRepository
                    .getMessageInstanceIdOlderThanCreationDate(creationDate, queryOptions);
            if (messageInstanceIdOlderThanCreationDate.size() > 0) {
                eventInstanceRepository.deleteMessageInstanceByIds(messageInstanceIdOlderThanCreationDate);
                for (Long messageId : messageInstanceIdOlderThanCreationDate) {
                    dataInstanceService.deleteLocalDataInstances(messageId,
                            DataInstanceContainer.MESSAGE_INSTANCE.name(),
                            true);
                    dataInstanceService.deleteLocalArchivedDataInstances(messageId,
                            DataInstanceContainer.MESSAGE_INSTANCE.name());
                }
            }
            return messageInstanceIdOlderThanCreationDate.size();
        } catch (SDataInstanceException | SEventTriggerInstanceReadException | SMessageInstanceReadException e) {
            throw new SMessageModificationException(e);
        }
    }

    @Override
    public List<Long> getMessageInstanceIdOlderThanCreationDate(long creationDate, QueryOptions queryOptions)
            throws SEventTriggerInstanceReadException, SMessageInstanceReadException {
        return this.eventInstanceRepository.getMessageInstanceIdOlderThanCreationDate(creationDate, queryOptions);
    }

    @Override
    public void updateEventTriggerInstance(STimerEventTriggerInstance sTimerEventTriggerInstance,
            EntityUpdateDescriptor descriptor) throws SEventTriggerInstanceModificationException {
        this.eventInstanceRepository.updateEventTriggerInstance(sTimerEventTriggerInstance, descriptor);
    }
}
