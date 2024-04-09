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

import static java.util.Collections.singletonMap;

import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceRepository;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.SEventInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.SEventInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.*;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.*;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.persistence.*;
import org.bonitasoft.engine.persistence.search.FilterOperationType;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Frederic Bouquet
 * @author Celine Souchet
 */
@Slf4j
public class EventInstanceRepositoryImpl implements EventInstanceRepository {

    public static final String QUERY_RESET_IN_PROGRESS_WAITING_EVENTS = "resetInProgressWaitingEvents";

    private static final String QUERY_RESET_PROGRESS_MESSAGE_INSTANCES = "resetProgressMessageInstances";

    private final EventService eventService;

    private final Recorder recorder;

    private final PersistenceService persistenceService;

    public EventInstanceRepositoryImpl(final Recorder recorder, final PersistenceService persistenceService,
            final EventService eventService, final ArchiveService archiveService) {

        this.recorder = recorder;
        this.eventService = eventService;
        this.persistenceService = persistenceService;
    }

    @Override
    public void createEventInstance(final SEventInstance eventInstance) throws SEventInstanceCreationException {
        try {
            recorder.recordInsert(new InsertRecord(eventInstance), EVENT_INSTANCE);
        } catch (final SRecorderException e) {
            throw new SEventInstanceCreationException(e);
        }
        if (log.isDebugEnabled()) {
            final StringBuilder stb = new StringBuilder();
            stb.append("Created ");
            stb.append(eventInstance.getType().getValue());
            stb.append(" <");
            stb.append(eventInstance.getName());
            stb.append("> with id = <");
            stb.append(eventInstance.getId());
            stb.append(">, parent process instance id = <");
            stb.append(eventInstance.getParentProcessInstanceId());
            stb.append(">, root process instance id = <");
            stb.append(eventInstance.getRootProcessInstanceId());
            stb.append(">, process definition id = <");
            stb.append(eventInstance.getProcessDefinitionId());
            stb.append(">");
            final String message = stb.toString();
            log.debug(message);
        }
    }

    @Override
    public void createTimerEventTriggerInstance(final STimerEventTriggerInstance eventTriggerInstance)
            throws SEventTriggerInstanceCreationException {
        try {
            recorder.recordInsert(new InsertRecord(eventTriggerInstance), EVENT_TRIGGER_INSTANCE);
        } catch (final SRecorderException e) {
            throw new SEventTriggerInstanceCreationException(e);
        }
    }

    @Override
    public void createMessageInstance(SMessageInstance messageInstance) throws SMessageInstanceCreationException {
        try {
            recorder.recordInsert(new InsertRecord(messageInstance), MESSAGE_INSTANCE);
        } catch (final SRecorderException e) {
            throw new SMessageInstanceCreationException(e);
        }
    }

    @Override
    public void createWaitingEvent(final SWaitingEvent waitingEvent) throws SWaitingEventCreationException {
        try {
            recorder.recordInsert(new InsertRecord(waitingEvent), EVENT_TRIGGER_INSTANCE);
        } catch (final SRecorderException e) {
            throw new SWaitingEventCreationException(e);
        }

    }

    @Override
    public void deleteEventTriggerInstance(final STimerEventTriggerInstance eventTriggerInstance)
            throws SEventTriggerInstanceDeletionException {
        try {
            recorder.recordDelete(new DeleteRecord(eventTriggerInstance), EVENT_TRIGGER_INSTANCE);
        } catch (final SRecorderException e) {
            throw new SEventTriggerInstanceDeletionException(e);
        }
    }

    @Override
    public void deleteMessageInstance(final SMessageInstance messageInstance) throws SMessageModificationException {
        try {
            recorder.recordDelete(new DeleteRecord(messageInstance), MESSAGE_INSTANCE);
        } catch (final SRecorderException e) {
            throw new SMessageModificationException(e);
        }
    }

    @Override
    public List<SBoundaryEventInstance> getActivityBoundaryEventInstances(final long activityInstanceId,
            final int fromIndex, final int maxResults)
            throws SEventInstanceReadException {
        final SelectListDescriptor<SBoundaryEventInstance> selectDescriptor = SelectDescriptorBuilder
                .getActivityBoundaryEvents(activityInstanceId, fromIndex,
                        maxResults);
        try {
            return persistenceService.selectList(selectDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SEventInstanceReadException(e);
        }
    }

    @Override
    public SWaitingErrorEvent getBoundaryWaitingErrorEvent(final long relatedActivityInstanceId, final String errorCode)
            throws SWaitingEventReadException {
        final QueryOptions queryOptions = new QueryOptions(0, 2, SWaitingErrorEvent.class, "id", OrderByType.ASC);
        SelectListDescriptor<SWaitingErrorEvent> selectDescriptor;
        if (errorCode == null) {
            selectDescriptor = SelectDescriptorBuilder.getCaughtError(relatedActivityInstanceId, queryOptions);
        } else {
            selectDescriptor = SelectDescriptorBuilder.getCaughtError(relatedActivityInstanceId, errorCode,
                    queryOptions);
        }
        SWaitingErrorEvent waitingError = null;
        try {
            final List<SWaitingErrorEvent> selectList = persistenceService.selectList(selectDescriptor);
            if (selectList != null && !selectList.isEmpty()) {
                if (selectList.size() == 1) {
                    waitingError = selectList.get(0);
                } else {
                    final StringBuilder stb = new StringBuilder();
                    stb.append("Only one catch error event was expected to handle the error code ");
                    stb.append(errorCode);
                    stb.append(" in the activity instance with id ");
                    stb.append(relatedActivityInstanceId + ".");
                    throw new SWaitingEventReadException(stb.toString());
                }
            }
        } catch (final SBonitaReadException e) {
            throw new SWaitingEventReadException(e);
        }
        return waitingError;
    }

    @Override
    public List<SEventInstance> getEventInstances(final long rootContainerId, final int fromIndex, final int maxResults,
            final String fieldName,
            final OrderByType orderByType) throws SEventInstanceReadException {
        final SelectListDescriptor<SEventInstance> selectDescriptor = SelectDescriptorBuilder
                .getEventsFromRootContainer(rootContainerId, fromIndex,
                        maxResults, fieldName, orderByType);
        try {
            return persistenceService.selectList(selectDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SEventInstanceReadException(e);
        }
    }

    @Override
    public <T extends STimerEventTriggerInstance> T getEventTriggerInstance(final Class<T> entityClass,
            final long eventTriggerInstanceId)
            throws SEventTriggerInstanceReadException {
        try {
            return persistenceService.selectById(
                    SelectDescriptorBuilder.getElementById(entityClass, entityClass.getSimpleName(),
                            eventTriggerInstanceId));
        } catch (final SBonitaReadException e) {
            throw new SEventTriggerInstanceReadException(e);
        }
    }

    @Override
    public int resetProgressMessageInstances() throws SMessageModificationException {
        try {
            return persistenceService.update(QUERY_RESET_PROGRESS_MESSAGE_INSTANCES);
        } catch (final SPersistenceException e) {
            throw new SMessageModificationException(e);
        }
    }

    @Override
    public int resetInProgressWaitingEvents() throws SWaitingEventModificationException {
        try {
            return persistenceService.update(QUERY_RESET_IN_PROGRESS_WAITING_EVENTS);
        } catch (final SPersistenceException e) {
            throw new SWaitingEventModificationException(e);
        }
    }

    @Override
    public List<SMessageEventCouple> getMessageEventCouples(final int fromIndex, final int maxResults)
            throws SEventTriggerInstanceReadException {
        final SelectListDescriptor<SMessageEventCouple> selectDescriptor = SelectDescriptorBuilder
                .getMessageEventCouples(fromIndex, maxResults);
        try {
            return persistenceService.selectList(selectDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SEventTriggerInstanceReadException(e);
        }
    }

    @Override
    public SMessageInstance getMessageInstance(final long messageInstanceId) throws SMessageInstanceReadException {
        try {
            return persistenceService
                    .selectById(SelectDescriptorBuilder.getElementById(SMessageInstance.class, "MessageInstance",
                            messageInstanceId));
        } catch (final SBonitaReadException e) {
            throw new SMessageInstanceReadException(e);
        }
    }

    @Override
    public List<Long> getMessageInstanceIdOlderThanCreationDate(final long creationDate,
            QueryOptions queryOptions) throws SMessageInstanceReadException {
        try {
            if (queryOptions != null) {
                validateFiltersForGetMessage(queryOptions);
            } else {
                queryOptions = QueryOptions.ALL_RESULTS;
            }
            final SelectListDescriptor<Long> selectDescriptor = SelectDescriptorBuilder
                    .getMessageInstanceIdOlderThanCreationDate(creationDate, queryOptions);
            return persistenceService.selectList(selectDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SMessageInstanceReadException(e);
        }
    }

    private void validateFiltersForGetMessage(QueryOptions queryOptions) {
        List<FilterOption> notOkField = queryOptions.getFilters().stream()
                .filter(filterOption -> filterOption.getFilterOperationType() != FilterOperationType.EQUALS
                        || !filterOption.getFieldName().equals("messageName")
                        || filterOption.getPersistentClass() != SMessageInstance.class)
                .collect(Collectors.toList());
        if (!notOkField.isEmpty()) {
            throw new IllegalArgumentException(
                    "Unsupported filters  " + notOkField + ", can only filter on messageName");
        }

    }

    @Override
    public void deleteMessageInstanceByIds(List<Long> ids) throws SMessageModificationException {
        List<List<Long>> listAsFragment = ListUtils.partition(ids, IN_REQUEST_SIZE);
        for (List<Long> fragmentIds : listAsFragment) {
            try {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("ids", fragmentIds);
                persistenceService.update("deleteMessageInstanceByIds", parameters);
            } catch (SPersistenceException e) {
                throw new SMessageModificationException(e);
            }
        }

    }

    @Override
    public long getNumberOfWaitingEvents(final Class<? extends SWaitingEvent> entityClass,
            final QueryOptions countOptions) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(entityClass, countOptions, null);
    }

    @Override
    public List<SWaitingEvent> getStartWaitingEventsOfProcessDefinition(final long processDefinitionId)
            throws SBonitaReadException {
        // event sub-processes are not returned:
        return persistenceService.selectList(new SelectListDescriptor<>("getStartWaitingEvents",
                singletonMap("processDefinitionId", processDefinitionId), SWaitingEvent.class,
                QueryOptions.ALL_RESULTS));
    }

    @Override
    public SWaitingMessageEvent getWaitingMessage(final long waitingMessageId) throws SWaitingEventReadException {
        try {
            return persistenceService.selectById(
                    SelectDescriptorBuilder.getElementById(SWaitingMessageEvent.class, "WaitingMessageEvent",
                            waitingMessageId));
        } catch (final SBonitaReadException e) {
            throw new SWaitingEventReadException(e);
        }
    }

    @Override
    public void deleteWaitingEvent(final SWaitingEvent waitingEvent) throws SWaitingEventModificationException {
        try {
            recorder.recordDelete(new DeleteRecord(waitingEvent), EVENT_TRIGGER_INSTANCE);
        } catch (final SRecorderException e) {
            throw new SWaitingEventModificationException(e);
        }
    }

    @Override
    public List<SWaitingEvent> getWaitingEventsForFlowNodeId(long flowNodeInstanceId)
            throws SEventTriggerInstanceReadException {
        try {
            return persistenceService.selectList(new SelectListDescriptor<>("getWaitingEventsOfFlowNode",
                    Collections.singletonMap("flowNodeInstanceId", flowNodeInstanceId),
                    SWaitingEvent.class, new QueryOptions(0, 100)));
        } catch (final SBonitaReadException e) {
            throw new SEventTriggerInstanceReadException(e);
        }
    }

    @Override
    public List<SWaitingEvent> getAllWaitingEventsForProcessInstance(long processInstanceId, final int fromIndex,
            final int maxResults) throws SEventTriggerInstanceReadException {
        try {
            return persistenceService
                    .selectList(new SelectListDescriptor<>("getWaitingEventsForProcessInstance",
                            Collections.singletonMap("processInstanceId", processInstanceId),
                            SWaitingEvent.class, new QueryOptions(fromIndex, maxResults)));
        } catch (final SBonitaReadException e) {
            throw new SEventTriggerInstanceReadException(e);
        }
    }

    @Override
    public List<SWaitingSignalEvent> getWaitingSignalEvents(final String signalName, final int fromIndex,
            final int maxResults)
            throws SEventTriggerInstanceReadException {
        final SelectListDescriptor<SWaitingSignalEvent> descriptor = SelectDescriptorBuilder
                .getListeningSignals(signalName, fromIndex, maxResults);
        try {
            return persistenceService.selectList(descriptor);
        } catch (final SBonitaReadException e) {
            throw new SEventTriggerInstanceReadException(e);
        }
    }

    @Override
    public SWaitingSignalEvent getWaitingSignalEvent(final long id)
            throws SEventTriggerInstanceReadException, SEventTriggerInstanceNotFoundException {
        final SelectByIdDescriptor<SWaitingSignalEvent> descriptor = new SelectByIdDescriptor<>(
                SWaitingSignalEvent.class, id);
        try {
            SWaitingSignalEvent sWaitingSignalEvent = persistenceService.selectById(descriptor);
            if (sWaitingSignalEvent == null) {
                throw new SEventTriggerInstanceNotFoundException(id);
            }
            return sWaitingSignalEvent;
        } catch (final SBonitaReadException e) {
            throw new SEventTriggerInstanceReadException(e);
        }
    }

    @Override
    public Optional<STimerEventTriggerInstance> getTimerEventTriggerInstanceOfFlowNode(long flowNodeInstanceId)
            throws SBonitaReadException {
        return Optional.ofNullable(persistenceService.selectOne(new SelectOneDescriptor<>(
                "getEventTriggerInstances",
                singletonMap("eventInstanceId", flowNodeInstanceId),
                STimerEventTriggerInstance.class)));
    }

    @Override
    public long getNumberOfTimerEventTriggerInstances(final long processInstanceId, final QueryOptions queryOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = singletonMap("processInstanceId", processInstanceId);
        return persistenceService.getNumberOfEntities(STimerEventTriggerInstance.class, "ByProcessInstance",
                queryOptions, parameters);
    }

    @Override
    public List<STimerEventTriggerInstance> searchTimerEventTriggerInstances(final long processInstanceId,
            final QueryOptions queryOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = singletonMap("processInstanceId", processInstanceId);
        return persistenceService.searchEntity(STimerEventTriggerInstance.class, "ByProcessInstance", queryOptions,
                parameters);
    }

    @Override
    public <T extends SWaitingEvent> List<T> searchWaitingEvents(final Class<T> entityClass,
            final QueryOptions searchOptions) throws SBonitaReadException {
        return persistenceService.searchEntity(entityClass, searchOptions, null);
    }

    @Override
    public void updateMessageInstance(final SMessageInstance messageInstance, final EntityUpdateDescriptor descriptor)
            throws SMessageModificationException {
        try {
            recorder.recordUpdate(UpdateRecord.buildSetFields(messageInstance, descriptor), MESSAGE_INSTANCE);
        } catch (final SRecorderException re) {
            throw new SMessageModificationException(re);
        }
    }

    @Override
    public void updateWaitingMessage(final SWaitingMessageEvent waitingMessageEvent,
            final EntityUpdateDescriptor descriptor)
            throws SWaitingEventModificationException {
        try {
            recorder.recordUpdate(UpdateRecord.buildSetFields(waitingMessageEvent, descriptor), MESSAGE_INSTANCE);
        } catch (final SRecorderException e) {
            throw new SWaitingEventModificationException(e);
        }
    }

    @Override
    public void updateEventTriggerInstance(final STimerEventTriggerInstance sTimerEventTriggerInstance,
            final EntityUpdateDescriptor descriptor)
            throws SEventTriggerInstanceModificationException {
        try {
            recorder.recordUpdate(UpdateRecord.buildSetFields(sTimerEventTriggerInstance, descriptor),
                    EVENT_TRIGGER_INSTANCE);
        } catch (final SRecorderException e) {
            throw new SEventTriggerInstanceModificationException(e);
        }
    }

}
