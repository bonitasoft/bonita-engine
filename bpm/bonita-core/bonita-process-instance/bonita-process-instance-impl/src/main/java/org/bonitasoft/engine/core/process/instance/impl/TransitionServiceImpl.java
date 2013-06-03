/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.instance.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.archive.SDefinitiveArchiveNotFound;
import org.bonitasoft.engine.core.process.definition.model.STransitionDefinition;
import org.bonitasoft.engine.core.process.definition.model.TransitionState;
import org.bonitasoft.engine.core.process.instance.api.TransitionService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STransitionCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STransitionDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STransitionInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STransitionReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.STransitionInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SATransitionInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.STransitionInstanceLogBuilder;
import org.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.SPersistenceLogBuilder;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;

/**
 * @author Zhao Na
 * @author Baptiste Mesta
 */
public class TransitionServiceImpl implements TransitionService {

    private final Recorder recorder;

    private final EventService eventService;

    private final ReadPersistenceService persistenceRead;

    private final BPMInstanceBuilders instanceBuilders;

    private final ArchiveService archiveService;

    private final QueriableLoggerService queriableLoggerService;

    public TransitionServiceImpl(final Recorder recorder, final EventService eventService, final ReadPersistenceService persistenceRead,
            final BPMInstanceBuilders instanceBuilders, final ArchiveService archiveService, final QueriableLoggerService queriableLoggerService) {
        this.recorder = recorder;
        this.eventService = eventService;
        this.persistenceRead = persistenceRead;
        this.instanceBuilders = instanceBuilders;
        this.archiveService = archiveService;
        this.queriableLoggerService = queriableLoggerService;
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.createNewInstance().actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    private STransitionInstanceLogBuilder getQueriableLog(final ActionType actionType, final String message, final STransitionInstance transitionInstance) {
        final STransitionInstanceLogBuilder logBuilder = this.instanceBuilders.getSTransitionInstanceLogBuilder();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        logBuilder.processInstanceId(transitionInstance.getParentContainerId());
        return logBuilder;
    }

    private STransitionInstanceLogBuilder getQueriableLog(final ActionType actionType, final String message, final SATransitionInstance transitionInstance) {
        final STransitionInstanceLogBuilder logBuilder = this.instanceBuilders.getSTransitionInstanceLogBuilder();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        logBuilder.processInstanceId(transitionInstance.getParentContainerId());
        return logBuilder;
    }

    @Override
    public void create(final STransitionInstance transitionInstance) throws STransitionCreationException {
        final STransitionInstanceLogBuilder logBuilder = getQueriableLog(ActionType.CREATED, "Creating a new Transition Instance", transitionInstance);
        final InsertRecord insertRecord = new InsertRecord(transitionInstance);
        SInsertEvent insertEvent = null;
        if (this.eventService.hasHandlers(TRANSITIONINSTANCE, EventActionType.CREATED)) {
            insertEvent = (SInsertEvent) this.eventService.getEventBuilder().createInsertEvent(TRANSITIONINSTANCE).setObject(transitionInstance).done();
        }
        try {
            this.recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(transitionInstance.getId(), SQueriableLog.STATUS_OK, logBuilder, "create");
        } catch (final SRecorderException e) {
            initiateLogBuilder(transitionInstance.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "create");
            throw new STransitionCreationException(e);
        }

    }

    @Override
    public void delete(final STransitionInstance transitionInstance) throws STransitionDeletionException {
        final STransitionInstanceLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "Deleting a Transition Instance", transitionInstance);
        final DeleteRecord deleteRecord = new DeleteRecord(transitionInstance);
        SDeleteEvent deleteEvent = null;
        if (this.eventService.hasHandlers(TRANSITIONINSTANCE, EventActionType.DELETED)) {
            deleteEvent = (SDeleteEvent) this.eventService.getEventBuilder().createDeleteEvent(TRANSITIONINSTANCE).setObject(transitionInstance).done();
        }
        try {
            this.recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(transitionInstance.getId(), SQueriableLog.STATUS_OK, logBuilder, "delete");
        } catch (final SRecorderException e) {
            initiateLogBuilder(transitionInstance.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "delete");
            throw new STransitionDeletionException(e);
        }

    }

    @Override
    public STransitionInstance get(final long transitionId) throws STransitionReadException, STransitionInstanceNotFoundException {
        STransitionInstance selectOne;
        try {
            selectOne = this.persistenceRead.selectById(SelectDescriptorBuilder.getElementById(STransitionInstance.class, "STransitionInstance", transitionId));
        } catch (final SBonitaReadException e) {
            throw new STransitionReadException(e);
        }
        if (selectOne == null) {
            throw new STransitionInstanceNotFoundException(transitionId);
        }
        return selectOne;
    }

    @Override
    public boolean containsActiveTransition(final long rootContainerId) throws STransitionReadException {
        final HashMap<String, Object> hashMap = new HashMap<String, Object>(2);
        hashMap.put("rootContainerId", rootContainerId);
        Long selectOne;
        try {
            selectOne = this.persistenceRead.selectOne(new SelectOneDescriptor<Long>("getNumberOfActiveTransitionOnRootContainer", hashMap,
                    STransitionInstance.class));
        } catch (final SBonitaReadException e) {
            throw new STransitionReadException(e);
        }
        return selectOne != null && selectOne > 0;
    }

    @Override
    public long getNumberOfTransitionInstances(final QueryOptions countOptions) throws SBonitaSearchException {
        try {
            return this.persistenceRead.getNumberOfEntities(STransitionInstance.class, countOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public List<STransitionInstance> search(final QueryOptions searchOptions) throws SBonitaSearchException {
        try {
            return this.persistenceRead.searchEntity(STransitionInstance.class, searchOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public long getNumberOfArchivedTransitionInstances(final QueryOptions countOptions) throws SBonitaSearchException {
        try {
            return this.persistenceRead.getNumberOfEntities(SATransitionInstance.class, countOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public List<SATransitionInstance> searchArchived(final QueryOptions searchOptions) throws SBonitaSearchException {
        try {
            return this.persistenceRead.searchEntity(SATransitionInstance.class, searchOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public void archive(final STransitionInstance sTransitionInstance, final long sFlowNodeInstanceId, final TransitionState transitionState)
            throws STransitionCreationException {
        final SATransitionInstance saTransitionInstance = this.instanceBuilders.getSATransitionInstanceBuilder()
                .createNewTransitionInstance(sTransitionInstance, sFlowNodeInstanceId, transitionState).done();
        if (saTransitionInstance != null) {
            final long archiveDate = System.currentTimeMillis();
            try {
                archiveTransitionInstanceInsertRecord(saTransitionInstance, archiveDate);
            } catch (final SRecorderException e) {
                throw new STransitionCreationException(e);
            } catch (final SDefinitiveArchiveNotFound e) {
                throw new STransitionCreationException(e);
            }
        }
    }

    @Override
    public void archive(final STransitionDefinition sTransitionDefinition, final SFlowNodeInstance sFlowNodeInstance, final TransitionState transitionState)
            throws STransitionCreationException {
        final SATransitionInstance saTransitionInstance = this.instanceBuilders.getSATransitionInstanceBuilder()
                .createNewTransitionInstance(sTransitionDefinition, sFlowNodeInstance, transitionState).done();
        final long archiveDate = System.currentTimeMillis();
        try {
            archiveTransitionInstanceInsertRecord(saTransitionInstance, archiveDate);
        } catch (final SRecorderException e) {
            throw new STransitionCreationException(e);
        } catch (final SDefinitiveArchiveNotFound e) {
            throw new STransitionCreationException(e);
        }

    }

    private void archiveTransitionInstanceInsertRecord(final SATransitionInstance saTransitionInstance, final long archiveDate) throws SRecorderException,
            SDefinitiveArchiveNotFound {
        final ArchiveInsertRecord insertRecord = new ArchiveInsertRecord(saTransitionInstance);
        this.archiveService.recordInsert(archiveDate, insertRecord, getQueriableLog(ActionType.CREATED, "archive the transition instance").done());
    }

    protected STransitionInstanceLogBuilder getQueriableLog(final ActionType actionType, final String message) {
        final STransitionInstanceLogBuilder logBuilder = this.instanceBuilders.getSTransitionInstanceLogBuilder();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    private void initiateLogBuilder(final long objectId, final int sQueriableLogStatus, final SPersistenceLogBuilder logBuilder, final String callerMethodName) {
        logBuilder.actionScope(String.valueOf(objectId));
        logBuilder.actionStatus(sQueriableLogStatus);
        logBuilder.objectId(objectId);
        final SQueriableLog log = logBuilder.done();
        if (this.queriableLoggerService.isLoggable(log.getActionType(), log.getSeverity())) {
            this.queriableLoggerService.log(this.getClass().getName(), callerMethodName, log);
        }
    }

    @Override
    public List<SATransitionInstance> getArchivedTransitionOfProcessInstance(final long processInstanceId, final int from, final int numberOfResult)
            throws STransitionReadException {
        try {
            final Map<String, Object> singletonMap = Collections.singletonMap("processInstanceId", (Object) processInstanceId);
            return this.persistenceRead.selectList(new SelectListDescriptor<SATransitionInstance>("getArchivedTransitionOfProcessInstance", singletonMap,
                    SATransitionInstance.class));
        } catch (final SBonitaReadException e) {
            throw new STransitionReadException(e);
        }
    }

    @Override
    public void delete(final SATransitionInstance saTransitionInstance) throws STransitionDeletionException {
        final STransitionInstanceLogBuilder logBuilder = getQueriableLog(ActionType.DELETED, "Deleting a Transition Instance", saTransitionInstance);
        final DeleteRecord deleteRecord = new DeleteRecord(saTransitionInstance);
        try {
            this.archiveService.recordDelete(deleteRecord, null);
            initiateLogBuilder(saTransitionInstance.getId(), SQueriableLog.STATUS_OK, logBuilder, "delete");
        } catch (final SRecorderException e) {
            initiateLogBuilder(saTransitionInstance.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "delete");
            throw new STransitionDeletionException(e);
        }
    }
}
