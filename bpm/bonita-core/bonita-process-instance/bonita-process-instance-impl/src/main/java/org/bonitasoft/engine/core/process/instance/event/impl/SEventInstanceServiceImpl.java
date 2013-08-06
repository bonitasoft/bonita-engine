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
package org.bonitasoft.engine.core.process.instance.event.impl;

import java.util.Collections;
import java.util.List;

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
import org.bonitasoft.engine.core.process.instance.impl.FlowNodeInstanceServiceImpl;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceLogBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingEventLogBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.trigger.SEventTriggerInstanceLogBuilder;
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
import org.bonitasoft.engine.events.model.builders.SEventBuilder;
import org.bonitasoft.engine.events.model.builders.SEventBuilders;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Frederic Bouquet
 */
public class SEventInstanceServiceImpl extends FlowNodeInstanceServiceImpl implements EventInstanceService {

    private final SEventBuilders eventBuilders;

    private final QueriableLoggerService queriableLoggerService;

    private final EventService eventService;

    public SEventInstanceServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceRead, final EventService eventService,
            final BPMInstanceBuilders instanceBuilders, final SEventBuilders eventBuilders, final QueriableLoggerService queriableLoggerService,
            final TechnicalLoggerService logger) {
        super(recorder, persistenceRead, eventService, instanceBuilders, queriableLoggerService, logger);
        this.eventService = eventService;
        this.eventBuilders = eventBuilders;
        this.queriableLoggerService = queriableLoggerService;
    }

    @Override
    public void createEventInstance(final SEventInstance eventInstance) throws SEventInstanceCreationException {
        final SFlowNodeInstanceLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Creating a new event Instance", eventInstance);
        try {
            final InsertRecord insertRecord = new InsertRecord(eventInstance);
            SInsertEvent insertEvent = null;
            if (eventService.hasHandlers(EVENT_INSTANCE, EventActionType.CREATED)) {
                insertEvent = (SInsertEvent) eventBuilders.getEventBuilder().createInsertEvent(EVENT_INSTANCE).setObject(eventInstance).done();
            }
            getRecorder().recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(eventInstance.getId(), SQueriableLog.STATUS_OK, logBuilder, "createEventInstance");
        } catch (final SRecorderException e) {
            initiateLogBuilder(eventInstance.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createEventInstance");
            throw new SEventInstanceCreationException(e);
        }

        getLogger().log(this.getClass(), TechnicalLogSeverity.INFO,
                "Created " + eventInstance.getType().getValue() + " <" + eventInstance.getName() + "> with id <" + eventInstance.getId() + ">");
    }

    @Override
    public SEventInstance getEventInstance(final long eventInstanceId) throws SEventInstanceNotFoundException, SEventInstanceReadException {
        SEventInstance selectOne;
        try {
            selectOne = getPersistenceRead().selectById(SelectDescriptorBuilder.getElementById(SEventInstance.class, "SEventInstance", eventInstanceId));
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
            return getPersistenceRead().selectList(selectDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SEventInstanceReadException(e);
        }
    }

    @Override
    public List<SBoundaryEventInstance> getActivityBoundaryEventInstances(final long activityInstanceId) throws SEventInstanceReadException {
        final SelectListDescriptor<SBoundaryEventInstance> selectDescriptor = SelectDescriptorBuilder.getActivityBoundaryEvents(activityInstanceId);
        try {
            return getPersistenceRead().selectList(selectDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SEventInstanceReadException(e);
        }
    }

    protected SEventTriggerInstanceLogBuilder getQueriableLog(final ActionType actionType, final String message, final SEventTriggerInstance eventTrigger) {
        final SEventTriggerInstanceLogBuilder logBuilder = getInstanceBuilders().getSEventTriggerInstanceLogBuilder();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        logBuilder.eventInstanceId(eventTrigger.getEventInstanceId());
        return logBuilder;
    }

    protected SWaitingEventLogBuilder getQueriableLog(final ActionType actionType, final String message) {
        final SWaitingEventLogBuilder logBuilder = getInstanceBuilders().getSStartEventTriggerInstanceLogBuilder();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    @Override
    public void createEventTriggerInstance(final SEventTriggerInstance eventTriggerInstance) throws SEventTriggerInstanceCreationException {
        final SEventTriggerInstanceLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Creating a new event trigger Instance", eventTriggerInstance);
        try {
            final InsertRecord insertRecord = new InsertRecord(eventTriggerInstance);
            SInsertEvent insertEvent = null;
            if (eventService.hasHandlers(EVENT_TRIGGER_INSTANCE, EventActionType.CREATED)) {
                insertEvent = (SInsertEvent) eventBuilders.getEventBuilder().createInsertEvent(EVENT_TRIGGER_INSTANCE).setObject(eventTriggerInstance).done();
            }
            getRecorder().recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(eventTriggerInstance.getId(), SQueriableLog.STATUS_OK, logBuilder, "createEventTriggerInstance");
        } catch (final SRecorderException e) {
            initiateLogBuilder(eventTriggerInstance.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createEventTriggerInstance");
            throw new SEventTriggerInstanceCreationException(e);
        }
    }

    @Override
    public SEventTriggerInstance getEventTriggerInstance(final long eventTriggerInstanceId) throws SEventTriggerInstanceNotFoundException,
            SEventTriggerInstanceReadException {
        SEventTriggerInstance selectOne;
        try {
            selectOne = getPersistenceRead().selectById(
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
    public List<SEventTriggerInstance> getEventTriggerInstances(final long eventInstanceId, final int fromIndex, final int maxResults, final String fieldName,
            final OrderByType orderByType) throws SEventTriggerInstanceReadException {
        final SelectListDescriptor<SEventTriggerInstance> selectDescriptor = SelectDescriptorBuilder.getEventTriggers(eventInstanceId, fromIndex, maxResults,
                fieldName, orderByType);
        try {
            return getPersistenceRead().selectList(selectDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SEventTriggerInstanceReadException(e);
        }
    }

    @Override
    public List<SEventTriggerInstance> getEventTriggerInstances(final long eventInstanceId) throws SEventTriggerInstanceReadException {
        final SelectListDescriptor<SEventTriggerInstance> selectDescriptor = SelectDescriptorBuilder.getEventTriggers(eventInstanceId);
        try {
            return getPersistenceRead().selectList(selectDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SEventTriggerInstanceReadException(e);
        }
    }

    @Override
    public void createWaitingEvent(final SWaitingEvent waitingEvent) throws SWaitingEventCreationException {
        final SWaitingEventLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Creating a waiting");
        try {
            final InsertRecord insertRecord = new InsertRecord(waitingEvent);
            SInsertEvent insertEvent = null;
            if (eventService.hasHandlers(EVENT_TRIGGER_INSTANCE, EventActionType.CREATED)) {
                insertEvent = (SInsertEvent) eventBuilders.getEventBuilder().createInsertEvent(EVENT_TRIGGER_INSTANCE).setObject(waitingEvent).done();
            }
            getRecorder().recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(waitingEvent.getId(), SQueriableLog.STATUS_OK, logBuilder, "createWaitingEvent");
        } catch (final SRecorderException e) {
            initiateLogBuilder(waitingEvent.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "createWaitingEvent");
            throw new SWaitingEventCreationException(e);
        }

    }

    @Override
    public List<SMessageInstance> getThrownMessages(final String messageName, final String targetProcess, final String targetFlowNode)
            throws SEventTriggerInstanceReadException {
        final SelectListDescriptor<SMessageInstance> selectDescriptor = SelectDescriptorBuilder.getMessageInstancesByNameAndTarget(messageName, targetProcess,
                targetFlowNode);
        try {
            return getPersistenceRead().selectList(selectDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SEventTriggerInstanceReadException(e);
        }
    }

    @Override
    public List<SWaitingMessageEvent> getWaitingMessages(final String messageName, final String processName, final String flowNodeName)
            throws SEventTriggerInstanceReadException {
        final SelectListDescriptor<SWaitingMessageEvent> selectDescriptor = SelectDescriptorBuilder.getCaughtMessages(messageName, processName, flowNodeName);
        try {
            return getPersistenceRead().selectList(selectDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SEventTriggerInstanceReadException(e);
        }
    }

    @Override
    public void createMessageInstance(final SMessageInstance messageInstance) throws SMessageInstanceCreationException {
        try {
            final InsertRecord insertRecord = new InsertRecord(messageInstance);
            SInsertEvent insertEvent = null;
            if (eventService.hasHandlers(MESSAGE_INSTANCE, EventActionType.CREATED)) {
                insertEvent = (SInsertEvent) eventBuilders.getEventBuilder().createInsertEvent(MESSAGE_INSTANCE).setObject(messageInstance).done();
            }
            getRecorder().recordInsert(insertRecord, insertEvent);
        } catch (final SRecorderException e) {
            throw new SMessageInstanceCreationException(e);
        }
    }

    @Override
    public void deleteMessageInstance(final SMessageInstance messageInstance) throws SMessageModificationException {
        try {
            SDeleteEvent deleteEvent = null;
            if (eventService.hasHandlers(MESSAGE_INSTANCE, EventActionType.DELETED)) {
                deleteEvent = (SDeleteEvent) eventBuilders.getEventBuilder().createDeleteEvent(MESSAGE_INSTANCE).setObject(messageInstance).done();
            }
            getRecorder().recordDelete(new DeleteRecord(messageInstance), deleteEvent);
        } catch (final SRecorderException e) {
            throw new SMessageModificationException(e);
        }
    }

    @Override
    public void deleteWaitingEvent(final SWaitingEvent waitingEvent) throws SWaitingEventModificationException {
        final SWaitingEventLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "deleting wating event");
        try {
            SDeleteEvent deleteEvent = null;
            if (eventService.hasHandlers(EVENT_TRIGGER_INSTANCE, EventActionType.DELETED)) {
                deleteEvent = (SDeleteEvent) getEventService().getEventBuilder().createDeleteEvent(EVENT_TRIGGER_INSTANCE).setObject(waitingEvent).done();
            }
            final DeleteRecord deleteRecord = new DeleteRecord(waitingEvent);
            getRecorder().recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(waitingEvent.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteWaitingEvent");
        } catch (final SRecorderException e) {
            initiateLogBuilder(waitingEvent.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteWaitingEvent");
            throw new SWaitingEventModificationException(e);
        }
    }

    @Override
    public List<SMessageEventCouple> getMessageEventCouples() throws SEventTriggerInstanceReadException, SMessageModificationException,
            SWaitingEventModificationException {
        final SelectListDescriptor<SMessageEventCouple> selectDescriptor = SelectDescriptorBuilder.getMessageEventCouples();
        try {
            return getPersistenceRead().selectList(selectDescriptor);
        } catch (final SBonitaReadException e) {
            throw new SEventTriggerInstanceReadException(e);
        }
    }

    @Override
    public SWaitingMessageEvent getWaitingMessage(final long waitingMessageId) throws SWaitingEventNotFoundException, SWaitingEventReadException {
        SWaitingMessageEvent selectOne;
        try {
            selectOne = getPersistenceRead().selectById(
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
    public SMessageInstance getMessageInstance(final long messageInstanceId) throws SMessageInstanceNotFoundException, SMessageInstanceReadException {
        SMessageInstance selectOne;
        try {
            selectOne = getPersistenceRead().selectById(SelectDescriptorBuilder.getElementById(SMessageInstance.class, "MessageInstance", messageInstanceId));
        } catch (final SBonitaReadException e) {
            throw new SMessageInstanceReadException(e);
        }
        if (selectOne == null) {
            throw new SMessageInstanceNotFoundException(messageInstanceId);
        }
        return selectOne;
    }

    @Override
    public void updateWaitingMessage(final SWaitingMessageEvent waitingMessageEvent, final EntityUpdateDescriptor descriptor)
            throws SWaitingEventModificationException {
        final SWaitingEventLogBuilder logBuilder = getQueriableLog(ActionType.UPDATED, "updating waiting message instance");
        try {
            final SWaitingMessageEvent oldWaitingMessage = getInstanceBuilders().getSWaitingMessageEventBuilder().createNewInstance(waitingMessageEvent).done();
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(waitingMessageEvent, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(MESSAGE_INSTANCE, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) eventBuilders.getEventBuilder().createUpdateEvent(MESSAGE_INSTANCE).setObject(waitingMessageEvent).done();
                updateEvent.setOldObject(oldWaitingMessage);
            }
            getRecorder().recordUpdate(updateRecord, updateEvent);
            initiateLogBuilder(waitingMessageEvent.getId(), SQueriableLog.STATUS_OK, logBuilder, "updateWaitingMessage");
        } catch (final SRecorderException e) {
            initiateLogBuilder(waitingMessageEvent.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "updateWaitingMessage");
            throw new SWaitingEventModificationException(e);
        }
    }

    @Override
    public void updateMessageInstance(final SMessageInstance messageInstance, final EntityUpdateDescriptor descriptor) throws SMessageModificationException {
        try {
            final SMessageInstance oldMessage = getInstanceBuilders().getSMessageInstanceBuilder().createNewInstance(messageInstance).done();
            final UpdateRecord updateRecord = UpdateRecord.buildSetFields(messageInstance, descriptor);
            SUpdateEvent updateEvent = null;
            if (eventService.hasHandlers(MESSAGE_INSTANCE, EventActionType.UPDATED)) {
                updateEvent = (SUpdateEvent) eventBuilders.getEventBuilder().createUpdateEvent(MESSAGE_INSTANCE).setObject(oldMessage).done();
                updateEvent.setOldObject(oldMessage);
            }
            getRecorder().recordUpdate(updateRecord, updateEvent);
        } catch (final SRecorderException re) {
            throw new SMessageModificationException(re);
        }
    }

    @Override
    public List<SWaitingSignalEvent> getWaitingSignalEvents(final String signalName) throws SEventTriggerInstanceReadException {
        final SelectListDescriptor<SWaitingSignalEvent> descriptor = SelectDescriptorBuilder.getListeningSignals(signalName);
        try {
            return getPersistenceRead().selectList(descriptor);
        } catch (final SBonitaReadException e) {
            throw new SEventTriggerInstanceReadException(e);
        }
    }

    @Override
    public List<SMessageInstance> getInProgressMessageInstances() throws SMessageInstanceReadException {
        try {
            return getPersistenceRead()
                    .selectList(
                            new SelectListDescriptor<SMessageInstance>("getInProgressMessageInstances", Collections.<String, Object> emptyMap(),
                                    SMessageInstance.class));
        } catch (final SBonitaReadException e) {
            throw new SMessageInstanceReadException(e);
        }
    }

    @Override
    public List<SWaitingMessageEvent> getInProgressWaitingMessageEvents() throws SWaitingEventReadException {
        try {
            return getPersistenceRead().selectList(
                    new SelectListDescriptor<SWaitingMessageEvent>("getInProgressWaitingEvents", Collections.<String, Object> emptyMap(),
                            SWaitingMessageEvent.class));
        } catch (final SBonitaReadException e) {
            throw new SWaitingEventReadException(e);
        }
    }

    @Override
    public List<SWaitingEvent> getStartWaitingEvents(final long processDefinitionId) throws SEventTriggerInstanceReadException {
        final SelectListDescriptor<SWaitingEvent> descriptor = SelectDescriptorBuilder.getStartWaitingEvents(processDefinitionId);
        try {
            return getPersistenceRead().selectList(descriptor);
        } catch (final SBonitaReadException e) {
            throw new SEventTriggerInstanceReadException(e);
        }
    }

    @Override
    public <T extends SWaitingEvent> List<T> searchWaitingEvents(final Class<T> entityClass, final QueryOptions searchOptions) throws SBonitaSearchException {
        try {
            return getPersistenceRead().searchEntity(entityClass, searchOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public long getNumberOfWaitingEvents(final Class<? extends SWaitingEvent> entityClass, final QueryOptions countOptions) throws SBonitaSearchException {
        try {
            return getPersistenceRead().getNumberOfEntities(entityClass, countOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public <T extends SEventTriggerInstance> List<T> searchEventTriggerInstances(final Class<T> entityClass, final QueryOptions searchOptions)
            throws SBonitaSearchException {
        try {
            return getPersistenceRead().searchEntity(entityClass, searchOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public long getNumberOfEventTriggerInstances(final Class<? extends SEventTriggerInstance> entityClass, final QueryOptions countOptions)
            throws SBonitaSearchException {
        try {
            return getPersistenceRead().getNumberOfEntities(entityClass, countOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public SWaitingEvent getWaitingEvent(final Long waitingEvent) throws SWaitingEventNotFoundException, SWaitingEventReadException {
        SWaitingEvent selectOne;
        try {
            selectOne = getPersistenceRead().selectById(SelectDescriptorBuilder.getElementById(SWaitingEvent.class, "WaitingEvent", waitingEvent));
        } catch (final SBonitaReadException e) {
            throw new SWaitingEventReadException(e);
        }
        if (selectOne == null) {
            throw new SWaitingEventNotFoundException(waitingEvent);
        }
        return selectOne;
    }

    private void initiateLogBuilder(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String callerMethodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            queriableLoggerService.log(this.getClass().getName(), callerMethodName, log);
        }
    }

    @Override
    public SWaitingErrorEvent getBoundaryWaitingErrorEvent(final long relatedActivityInstanceId, final String errorCode) throws SWaitingEventReadException {
        SelectListDescriptor<SWaitingErrorEvent> selectDescriptor;
        if (errorCode == null) {
            selectDescriptor = SelectDescriptorBuilder.getCaughtError(relatedActivityInstanceId);
        } else {
            selectDescriptor = SelectDescriptorBuilder.getCaughtError(relatedActivityInstanceId, errorCode);
        }
        SWaitingErrorEvent waitingError = null;
        try {
            final List<SWaitingErrorEvent> selectList = getPersistenceRead().selectList(selectDescriptor);
            if (selectList != null && !selectList.isEmpty()) {
                if (selectList.size() == 1) {
                    waitingError = selectList.get(0);
                } else {
                    final StringBuilder stb = new StringBuilder();
                    stb.append("Only one catch error event was expected to handle the error code ");
                    stb.append(errorCode);
                    stb.append(" in the activity instance with id ");
                    stb.append(relatedActivityInstanceId);
                    stb.append(", but ");
                    stb.append(selectList.size());
                    stb.append(" was found.");
                    throw new SWaitingEventReadException(stb.toString());
                }
            }
        } catch (final SBonitaReadException e) {
            throw new SWaitingEventReadException(e);
        }
        return waitingError;
    }

    @Override
    public void deleteWaitingEvents(final SFlowNodeInstance flowNodeInstance) throws SWaitingEventModificationException, SFlowNodeReadException {
        final OrderByOption orderByOption = new OrderByOption(SWaitingEvent.class, getInstanceBuilders().getSWaitingMessageEventBuilder().getFlowNodeNameKey(),
                OrderByType.ASC);
        final FilterOption filterOption = new FilterOption(SWaitingEvent.class, getInstanceBuilders().getSWaitingMessageEventBuilder()
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
    public void deleteEventTriggerInstances(final long eventInstanceId) throws SEventTriggerInstanceReadException, SEventTriggerInstanceDeletionException {
        final List<SEventTriggerInstance> triggerInstances = getEventTriggerInstances(eventInstanceId);
        for (final SEventTriggerInstance eventTriggerInstance : triggerInstances) {
            deleteEventTriggerInstance(eventTriggerInstance);
        }
    }

    @Override
    public void deleteEventTriggerInstance(final SEventTriggerInstance eventTriggerInstance) throws SEventTriggerInstanceDeletionException {
        final SEventTriggerInstanceLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "deleting eventTrigger instance", eventTriggerInstance);
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(eventTriggerInstance);
            SDeleteEvent deleteEvent = null;
            if (eventService.hasHandlers(EVENT_TRIGGER_INSTANCE, EventActionType.DELETED)) {
                final SEventBuilder eventBuilder = eventService.getEventBuilder();
                deleteEvent = (SDeleteEvent) eventBuilder.createDeleteEvent(EVENT_TRIGGER_INSTANCE).setObject(eventTriggerInstance).done();
            }
            getRecorder().recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(eventTriggerInstance.getId(), SQueriableLog.STATUS_OK, logBuilder, "deleteEventTriggerInstance");
        } catch (final SRecorderException e) {
            initiateLogBuilder(eventTriggerInstance.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "deleteEventTriggerInstance");
            throw new SEventTriggerInstanceDeletionException(e);
        }
    }
}
