/**
 * Copyright (C) 2012, 2014 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.instance.event.impl;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.SEventInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.SEventInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.SEventInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventReadException;
import org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstancesServiceImpl;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SMessageInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingMessageEventBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageEventCouple;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingErrorEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingSignalEvent;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
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
public class EventInstanceServiceImpl extends FlowNodeInstancesServiceImpl implements EventInstanceService {

    private static final String QUERY_RESET_PROGRESS_MESSAGE_INSTANCES = "resetProgressMessageInstances";

    public static final String QUERY_RESET_IN_PROGRESS_WAITING_EVENTS = "resetInProgressWaitingEvents";

    private final EventService eventService;

    public EventInstanceServiceImpl(final Recorder recorder, final PersistenceService persistenceService, final EventService eventService,
            final TechnicalLoggerService logger, final ArchiveService archiveService) {
        super(recorder, persistenceService, eventService, logger, archiveService);
        this.eventService = eventService;
    }

    @Override
    public void createEventInstance(final SEventInstance eventInstance) throws SEventInstanceCreationException {
        try {
            final InsertRecord insertRecord = new InsertRecord(eventInstance);
            SInsertEvent insertEvent = null;
            if (eventService.hasHandlers(EVENT_INSTANCE, EventActionType.CREATED)) {
                insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(EVENT_INSTANCE).setObject(eventInstance).done();
            }
            getRecorder().recordInsert(insertRecord, insertEvent);
        } catch (final SRecorderException e) {
            throw new SEventInstanceCreationException(e);
        }
        if (getLogger().isLoggable(getClass(), TechnicalLogSeverity.DEBUG)) {
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
            getLogger().log(this.getClass(), TechnicalLogSeverity.DEBUG, message);
        }
    }

    @Override
    public void createEventTriggerInstance(final SEventTriggerInstance eventTriggerInstance) throws SEventTriggerInstanceCreationException {
        try {
            final InsertRecord insertRecord = new InsertRecord(eventTriggerInstance);
            SInsertEvent insertEvent = null;
            if (eventService.hasHandlers(EVENT_TRIGGER_INSTANCE, EventActionType.CREATED)) {
                insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(EVENT_TRIGGER_INSTANCE)
                        .setObject(eventTriggerInstance).done();
            }
            getRecorder().recordInsert(insertRecord, insertEvent);
        } catch (final SRecorderException e) {
            throw new SEventTriggerInstanceCreationException(e);
        }
    }

    @Override
    public void createMessageInstance(final SMessageInstance messageInstance) throws SMessageInstanceCreationException {
        try {
            final InsertRecord insertRecord = new InsertRecord(messageInstance);
            SInsertEvent insertEvent = null;
            if (eventService.hasHandlers(MESSAGE_INSTANCE, EventActionType.CREATED)) {
                insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(MESSAGE_INSTANCE).setObject(messageInstance)
                        .done();
            }
            getRecorder().recordInsert(insertRecord, insertEvent);
        } catch (final SRecorderException e) {
            throw new SMessageInstanceCreationException(e);
        }
    }

    @Override
    public void createWaitingEvent(final SWaitingEvent waitingEvent) throws SWaitingEventCreationException {
        try {
            final InsertRecord insertRecord = new InsertRecord(waitingEvent);
            SInsertEvent insertEvent = null;
            if (eventService.hasHandlers(EVENT_TRIGGER_INSTANCE, EventActionType.CREATED)) {
                insertEvent = (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(EVENT_TRIGGER_INSTANCE).setObject(waitingEvent)
                        .done();
            }
            getRecorder().recordInsert(insertRecord, insertEvent);
        } catch (final SRecorderException e) {
            throw new SWaitingEventCreationException(e);
        }

    }

    @Override
    public void deleteEventTriggerInstance(final SEventTriggerInstance eventTriggerInstance) throws SEventTriggerInstanceDeletionException {
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(eventTriggerInstance);
            SDeleteEvent deleteEvent = null;
            if (eventService.hasHandlers(EVENT_TRIGGER_INSTANCE, EventActionType.DELETED)) {
                deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(EVENT_TRIGGER_INSTANCE)
                        .setObject(eventTriggerInstance).done();
            }
            getRecorder().recordDelete(deleteRecord, deleteEvent);
        } catch (final SRecorderException e) {
            throw new SEventTriggerInstanceDeletionException(e);
        }
    }

    @Override
    public void deleteEventTriggerInstances(final long eventInstanceId) throws SEventTriggerInstanceReadException, SEventTriggerInstanceDeletionException {
        final QueryOptions queryOptions = new QueryOptions(0, 100, SEventTriggerInstance.class, "id", OrderByType.ASC);
        List<SEventTriggerInstance> triggerInstances = getEventTriggerInstances(eventInstanceId, queryOptions);
        while (!triggerInstances.isEmpty()) {
            for (final SEventTriggerInstance eventTriggerInstance : triggerInstances) {
                deleteEventTriggerInstance(eventTriggerInstance);
            }
            triggerInstances = getEventTriggerInstances(eventInstanceId, queryOptions);
        }
    }

    @Override
    public void deleteMessageInstance(final SMessageInstance messageInstance) throws SMessageModificationException {
        try {
            SDeleteEvent deleteEvent = null;
            if (eventService.hasHandlers(MESSAGE_INSTANCE, EventActionType.DELETED)) {
                deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(MESSAGE_INSTANCE).setObject(messageInstance)
                        .done();
            }
            getRecorder().recordDelete(new DeleteRecord(messageInstance), deleteEvent);
        } catch (final SRecorderException e) {
            throw new SMessageModificationException(e);
        }
    }

    @Override
    public void deleteWaitingEvent(final SWaitingEvent waitingEvent) throws SWaitingEventModificationException {
        try {
            SDeleteEvent deleteEvent = null;
            if (eventService.hasHandlers(EVENT_TRIGGER_INSTANCE, EventActionType.DELETED)) {
                deleteEvent = (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(EVENT_TRIGGER_INSTANCE).setObject(waitingEvent)
                        .done();
            }
            final DeleteRecord deleteRecord = new DeleteRecord(waitingEvent);
            getRecorder().recordDelete(deleteRecord, deleteEvent);
        } catch (final SRecorderException e) {
            throw new SWaitingEventModificationException(e);
        }
    }

    @Override
    public void deleteWaitingEvents(final SFlowNodeInstance flowNodeInstance) throws SWaitingEventModificationException, SFlowNodeReadException {
        final OrderByOption orderByOption = new OrderByOption(SWaitingEvent.class, BuilderFactory.get(SWaitingMessageEventBuilderFactory.class)
                .getFlowNodeNameKey(), OrderByType.ASC);
        final FilterOption filterOption = new FilterOption(SWaitingEvent.class, BuilderFactory.get(SWaitingMessageEventBuilderFactory.class)
                .getFlowNodeInstanceIdKey(), flowNodeInstance.getId());
        final List<FilterOption> filters = Collections.singletonList(filterOption);
        try {
            QueryOptions queryOptions = new QueryOptions(0, 10, Collections.singletonList(orderByOption), filters, null);
            List<SWaitingEvent> waitingEvents = searchWaitingEvents(SWaitingEvent.class, queryOptions);

            do {
                for (final SWaitingEvent sWaitingEvent : waitingEvents) {
                    deleteWaitingEvent(sWaitingEvent);
                }
                queryOptions = new QueryOptions(0, 10, Collections.singletonList(orderByOption), filters, null);
                waitingEvents = searchWaitingEvents(SWaitingEvent.class, queryOptions);
            } while (waitingEvents.size() > 0);
        } catch (final SBonitaSearchException e) {
            throw new SFlowNodeReadException(e); // To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public List<SBoundaryEventInstance> getActivityBoundaryEventInstances(final long activityInstanceId, final int fromIndex, final int maxResults)
            throws SEventInstanceReadException {
        final SelectListDescriptor<SBoundaryEventInstance> selectDescriptor = SelectDescriptorBuilder.getActivityBoundaryEvents(activityInstanceId, fromIndex,
                maxResults);
        try {
            return getPersistenceService().selectList(selectDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SEventInstanceReadException(e);
        }
    }

    @Override
    public SWaitingErrorEvent getBoundaryWaitingErrorEvent(final long relatedActivityInstanceId, final String errorCode) throws SWaitingEventReadException {
        final QueryOptions queryOptions = new QueryOptions(0, 2, SWaitingErrorEvent.class, "id", OrderByType.ASC);
        SelectListDescriptor<SWaitingErrorEvent> selectDescriptor;
        if (errorCode == null) {
            selectDescriptor = SelectDescriptorBuilder.getCaughtError(relatedActivityInstanceId, queryOptions);
        } else {
            selectDescriptor = SelectDescriptorBuilder.getCaughtError(relatedActivityInstanceId, errorCode, queryOptions);
        }
        SWaitingErrorEvent waitingError = null;
        try {
            final List<SWaitingErrorEvent> selectList = getPersistenceService().selectList(selectDescriptor);
            if ((selectList != null) && !selectList.isEmpty()) {
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
    public SEventInstance getEventInstance(final long eventInstanceId) throws SEventInstanceNotFoundException, SEventInstanceReadException {
        SEventInstance selectOne;
        try {
            selectOne = getPersistenceService().selectById(SelectDescriptorBuilder.getElementById(SEventInstance.class, "SEventInstance", eventInstanceId));
        } catch (final SBonitaReadException e) {
            throw new SEventInstanceReadException(e);
        }
        if (selectOne == null) {
            throw new SEventInstanceNotFoundException(eventInstanceId);
        }
        return selectOne;
    }

    @Override
    public List<SEventInstance> getEventInstances(final long rootContainerId, final int fromIndex, final int maxResults, final String fieldName,
            final OrderByType orderByType) throws SEventInstanceReadException {
        final SelectListDescriptor<SEventInstance> selectDescriptor = SelectDescriptorBuilder.getEventsFromRootContainer(rootContainerId, fromIndex,
                maxResults, fieldName, orderByType);
        try {
            return getPersistenceService().selectList(selectDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SEventInstanceReadException(e);
        }
    }

    @Override
    public SEventTriggerInstance getEventTriggerInstance(final long eventTriggerInstanceId) throws SEventTriggerInstanceNotFoundException,
            SEventTriggerInstanceReadException {
        SEventTriggerInstance selectOne;
        try {
            selectOne = getPersistenceService().selectById(
                    SelectDescriptorBuilder.getElementById(SEventTriggerInstance.class, "EventTriggerInstance", eventTriggerInstanceId));
        } catch (final SBonitaReadException e) {
            throw new SEventTriggerInstanceReadException(e);
        }
        if (selectOne == null) {
            throw new SEventTriggerInstanceNotFoundException(eventTriggerInstanceId);
        }
        return selectOne;
    }

    @Override
    public List<SEventTriggerInstance> getEventTriggerInstances(final long eventInstanceId, final QueryOptions queryOptions)
            throws SEventTriggerInstanceReadException {
        final SelectListDescriptor<SEventTriggerInstance> selectDescriptor = SelectDescriptorBuilder.getEventTriggers(eventInstanceId, queryOptions);
        try {
            return getPersistenceService().selectList(selectDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SEventTriggerInstanceReadException(e);
        }
    }

    @Override
    public List<SEventTriggerInstance> getEventTriggerInstances(final long eventInstanceId, final int fromIndex, final int maxResults, final String fieldName,
            final OrderByType orderByType) throws SEventTriggerInstanceReadException {
        final SelectListDescriptor<SEventTriggerInstance> selectDescriptor = SelectDescriptorBuilder.getEventTriggers(eventInstanceId, fromIndex, maxResults,
                fieldName, orderByType);
        try {
            return getPersistenceService().selectList(selectDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SEventTriggerInstanceReadException(e);
        }
    }

    @Override
    public int resetProgressMessageInstances() throws SMessageModificationException {
        try {
            return getPersistenceService().update(QUERY_RESET_PROGRESS_MESSAGE_INSTANCES);
        } catch (SPersistenceException e) {
            throw new SMessageModificationException(e);
        }
    }

    @Override
    public int resetInProgressWaitingEvents() throws SWaitingEventModificationException {
        try {
            return getPersistenceService().update(QUERY_RESET_IN_PROGRESS_WAITING_EVENTS);
        } catch (SPersistenceException e) {
            throw new SWaitingEventModificationException(e);
        }
    }

    @Override
    public List<SMessageEventCouple> getMessageEventCouples(final int fromIndex, final int maxResults) throws SEventTriggerInstanceReadException {
        final SelectListDescriptor<SMessageEventCouple> selectDescriptor = SelectDescriptorBuilder.getMessageEventCouples(fromIndex, maxResults);
        try {
            return getPersistenceService().selectList(selectDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SEventTriggerInstanceReadException(e);
        }
    }

    @Override
    public SMessageInstance getMessageInstance(final long messageInstanceId) throws SMessageInstanceNotFoundException, SMessageInstanceReadException {
        SMessageInstance selectOne;
        try {
            selectOne = getPersistenceService()
                    .selectById(SelectDescriptorBuilder.getElementById(SMessageInstance.class, "MessageInstance", messageInstanceId));
        } catch (final SBonitaReadException e) {
            throw new SMessageInstanceReadException(e);
        }
        if (selectOne == null) {
            throw new SMessageInstanceNotFoundException(messageInstanceId);
        }
        return selectOne;
    }

    @Override
    public long getNumberOfEventTriggerInstances(final Class<? extends SEventTriggerInstance> entityClass, final QueryOptions countOptions)
            throws SBonitaSearchException {
        try {
            return getPersistenceService().getNumberOfEntities(entityClass, countOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public long getNumberOfWaitingEvents(final Class<? extends SWaitingEvent> entityClass, final QueryOptions countOptions) throws SBonitaSearchException {
        try {
            return getPersistenceService().getNumberOfEntities(entityClass, countOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public List<SWaitingEvent> searchStartWaitingEvents(final long processDefinitionId, final QueryOptions queryOptions) throws SBonitaSearchException {
        final SelectListDescriptor<SWaitingEvent> descriptor = SelectDescriptorBuilder.getStartWaitingEvents(processDefinitionId, queryOptions);
        try {
            return getPersistenceService().selectList(descriptor);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public SWaitingEvent getWaitingEvent(final Long waitingEvent) throws SWaitingEventNotFoundException, SWaitingEventReadException {
        SWaitingEvent selectOne;
        try {
            selectOne = getPersistenceService().selectById(SelectDescriptorBuilder.getElementById(SWaitingEvent.class, "WaitingEvent", waitingEvent));
        } catch (final SBonitaReadException e) {
            throw new SWaitingEventReadException(e);
        }
        if (selectOne == null) {
            throw new SWaitingEventNotFoundException(waitingEvent);
        }
        return selectOne;
    }

    @Override
    public SWaitingMessageEvent getWaitingMessage(final long waitingMessageId) throws SWaitingEventNotFoundException, SWaitingEventReadException {
        SWaitingMessageEvent selectOne;
        try {
            selectOne = getPersistenceService().selectById(
                    SelectDescriptorBuilder.getElementById(SWaitingMessageEvent.class, "WaitingMessageEvent", waitingMessageId));
        } catch (final SBonitaReadException e) {
            throw new SWaitingEventReadException(e);
        }
        if (selectOne == null) {
            throw new SWaitingEventNotFoundException(waitingMessageId);
        }
        return selectOne;
    }

    @Override
    public List<SWaitingSignalEvent> getWaitingSignalEvents(final String signalName, final int fromIndex, final int maxResults)
            throws SEventTriggerInstanceReadException {
        final SelectListDescriptor<SWaitingSignalEvent> descriptor = SelectDescriptorBuilder.getListeningSignals(signalName, fromIndex, maxResults);
        try {
            return getPersistenceService().selectList(descriptor);
        } catch (final SBonitaReadException e) {
            throw new SEventTriggerInstanceReadException(e);
        }
    }

    @Override
    public <T extends SEventTriggerInstance> List<T> searchEventTriggerInstances(final Class<T> entityClass, final QueryOptions searchOptions)
            throws SBonitaSearchException {
        try {
            return getPersistenceService().searchEntity(entityClass, searchOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public <T extends SWaitingEvent> List<T> searchWaitingEvents(final Class<T> entityClass, final QueryOptions searchOptions) throws SBonitaSearchException {
        try {
            return getPersistenceService().searchEntity(entityClass, searchOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public void updateMessageInstance(final SMessageInstance messageInstance, final EntityUpdateDescriptor descriptor) throws SMessageModificationException {
        try {
            final SMessageInstance oldMessage = BuilderFactory.get(SMessageInstanceBuilderFactory.class).createNewInstance(messageInstance).done();
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(messageInstance, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(MESSAGE_INSTANCE, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(MESSAGE_INSTANCE).setObject(oldMessage).done();
                updateEvent.setOldObject(oldMessage);
            }
            getRecorder().recordUpdate(updateRecord, updateEvent);
        } catch (final SRecorderException re) {
            throw new SMessageModificationException(re);
        }
    }

    @Override
    public void updateWaitingMessage(final SWaitingMessageEvent waitingMessageEvent, final EntityUpdateDescriptor descriptor)
            throws SWaitingEventModificationException {
        try {
            final SWaitingMessageEvent oldWaitingMessage = BuilderFactory.get(SWaitingMessageEventBuilderFactory.class).createNewInstance(waitingMessageEvent)
                    .done();
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(waitingMessageEvent, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(MESSAGE_INSTANCE, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) BuilderFactory.get(SEventBuilderFactory.class).createUpdateEvent(MESSAGE_INSTANCE).setObject(waitingMessageEvent)
                        .done();
                updateEvent.setOldObject(oldWaitingMessage);
            }
            getRecorder().recordUpdate(updateRecord, updateEvent);
        } catch (final SRecorderException e) {
            throw new SWaitingEventModificationException(e);
        }
    }
}
