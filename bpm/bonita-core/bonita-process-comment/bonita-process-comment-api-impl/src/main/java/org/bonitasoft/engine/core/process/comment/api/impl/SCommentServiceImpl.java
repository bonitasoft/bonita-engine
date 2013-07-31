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
package org.bonitasoft.engine.core.process.comment.api.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.comment.api.SCommentAddException;
import org.bonitasoft.engine.core.process.comment.api.SCommentDeletionException;
import org.bonitasoft.engine.core.process.comment.api.SCommentNotFoundException;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.api.SystemCommentType;
import org.bonitasoft.engine.core.process.comment.model.SComment;
import org.bonitasoft.engine.core.process.comment.model.archive.SAComment;
import org.bonitasoft.engine.core.process.comment.model.archive.builder.SACommentBuilder;
import org.bonitasoft.engine.core.process.comment.model.builder.SCommentBuilder;
import org.bonitasoft.engine.core.process.comment.model.builder.SCommentBuilders;
import org.bonitasoft.engine.core.process.comment.model.builder.SCommmentLogBuilder;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilders;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
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
import org.bonitasoft.engine.session.SSessionNotFoundException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;

/**
 * @author Hongwen Zang
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SCommentServiceImpl implements SCommentService {

    private static final String SUPERVISED_BY = "SupervisedBy";

    private static final String INVOLVING_USER = "InvolvingUser";

    private static final String MANAGED_BY = "ManagedBy";

    private final SCommentBuilders commentBuilders;

    private final Recorder recorder;

    private final SEventBuilders eventBuilders;

    private final ReadPersistenceService persistenceService;

    private final SessionService sessionService;

    private final ReadSessionAccessor sessionAccessor;

    private final Map<SystemCommentType, Boolean> systemCommentType;

    private final QueriableLoggerService queriableLoggerService;

    private final EventService eventService;

    private final ArchiveService archiveService;

    public SCommentServiceImpl(final SCommentBuilders commentBuilders, final Recorder recorder, final SEventBuilders eventBuilders,
            final ReadPersistenceService persistenceService, final ArchiveService archiveService, final SessionService sessionService,
            final ReadSessionAccessor sessionAccessor, final Map<SystemCommentType, Boolean> systemCommentType,
            final QueriableLoggerService queriableLoggerService, final EventService eventService) {
        super();
        this.commentBuilders = commentBuilders;
        this.recorder = recorder;
        this.eventBuilders = eventBuilders;
        this.persistenceService = persistenceService;
        this.sessionService = sessionService;
        this.sessionAccessor = sessionAccessor;
        this.systemCommentType = systemCommentType;
        this.queriableLoggerService = queriableLoggerService;
        this.eventService = eventService;
        this.archiveService = archiveService;
    }

    private SInsertEvent getInsertEvent(final Object obj) {
        return (SInsertEvent) eventBuilders.getEventBuilder().createInsertEvent(COMMENT).setObject(obj).done();
    }

    private <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.createNewInstance().actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    private <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    private SCommmentLogBuilder getCommmentLogBuilder(final ActionType actionType, final String message) {
        final SCommmentLogBuilder logBuilder = commentBuilders.getCommentLogBuilder();
        initializeLogBuilder(logBuilder, message);
        updateLog(actionType, logBuilder);
        return logBuilder;
    }

    private SDeleteEvent getDeleteEvent(final Object obj) {
        return (SDeleteEvent) eventBuilders.getEventBuilder().createDeleteEvent(COMMENT).setObject(obj).done();
    }

    @Override
    public List<SComment> searchComments(final QueryOptions options) throws SBonitaSearchException {
        try {
            return persistenceService.searchEntity(SComment.class, options, null);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public long getNumberOfComments(final QueryOptions options) throws SBonitaSearchException {
        try {
            return persistenceService.getNumberOfEntities(SComment.class, options, null);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public List<SComment> getComments(final long processInstanceId) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("processInstanceId", (Object) processInstanceId);
        final SelectListDescriptor<SComment> selectDescriptor = new SelectListDescriptor<SComment>("getSComments", parameters, SComment.class);
        return persistenceService.selectList(selectDescriptor);
    }

    @Override
    public SComment addComment(final long processInstanceId, final String comment) throws SCommentAddException {
        NullCheckingUtil.checkArgsNotNull(processInstanceId);
        NullCheckingUtil.checkArgsNotNull(comment);
        final SCommmentLogBuilder logBuilder = getCommmentLogBuilder(ActionType.CREATED, "creating a comment");
        try {
            final SCommentBuilder sCommentBuilder = commentBuilders.getSHumanCommentBuilder();
            final long userId = getUserId();
            final SComment sComment = sCommentBuilder.createNewInstance(processInstanceId, comment, userId).done();
            final InsertRecord insertRecord = new InsertRecord(sComment);
            SInsertEvent insertEvent = null;
            if (eventService.hasHandlers(COMMENT, EventActionType.CREATED)) {
                insertEvent = getInsertEvent(sComment);
            }
            recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(processInstanceId, SQueriableLog.STATUS_OK, logBuilder, "addComment");
            return sComment;
        } catch (final SRecorderException e) {
            initiateLogBuilder(processInstanceId, SQueriableLog.STATUS_FAIL, logBuilder, "addComment");
            throw new SCommentAddException("Imposible to create comment.", e);
        } catch (final SSessionNotFoundException e) {
            throw new SCommentAddException("Session is not found.", e);
        } catch (final SessionIdNotSetException e) {
            throw new SCommentAddException("Session id is not set.", e);
        }
    }

    @Override
    public void delete(final SComment comment) throws SCommentDeletionException {
        NullCheckingUtil.checkArgsNotNull(comment);
        final SCommmentLogBuilder logBuilder = getCommmentLogBuilder(ActionType.DELETED, "creating a comment");
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(comment);
            SDeleteEvent deleteEvent = null;
            if (eventService.hasHandlers(COMMENT, EventActionType.DELETED)) {
                deleteEvent = getDeleteEvent(comment);
            }
            recorder.recordDelete(deleteRecord, deleteEvent);
            initiateLogBuilder(comment.getId(), SQueriableLog.STATUS_OK, logBuilder, "delete");

        } catch (final SRecorderException e) {
            initiateLogBuilder(comment.getId(), SQueriableLog.STATUS_FAIL, logBuilder, "delete");

            throw new SCommentDeletionException("Imposible to delete comment.", e);
        }
    }

    @Override
    public void deleteComments(final long processInstanceId) throws SBonitaException {
        final List<SComment> sComments = getComments(processInstanceId);
        for (final SComment sComment : sComments) {
            delete(sComment);
        }
    }

    private long getUserId() throws SSessionNotFoundException, SessionIdNotSetException {
        final SSession session = sessionService.getSession(sessionAccessor.getSessionId());
        return session.getUserId();
    }

    @Override
    public long getNumberOfCommentsSupervisedBy(final long supervisorId, final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap("supervisorId", (Object) supervisorId);
            return persistenceService.getNumberOfEntities(SComment.class, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public List<SComment> searchCommentsSupervisedBy(final long supervisorId, final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap("supervisorId", (Object) supervisorId);
            return persistenceService.searchEntity(SComment.class, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public long getNumberOfCommentsInvolvingUser(final long userId, final QueryOptions searchOptions) throws SBonitaSearchException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
            return persistenceService.getNumberOfEntities(SComment.class, INVOLVING_USER, searchOptions, parameters);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public List<SComment> searchCommentsInvolvingUser(final long userId, final QueryOptions queryOptions) throws SBonitaSearchException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
            return persistenceService.searchEntity(SComment.class, INVOLVING_USER, queryOptions, parameters);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public long getNumberOfCommentsManagedBy(final long managerUserId, final QueryOptions searchOptions) throws SBonitaSearchException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap("managerUserId", (Object) managerUserId);
            return persistenceService.getNumberOfEntities(SComment.class, MANAGED_BY, searchOptions, parameters);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public List<SComment> searchCommentsManagedBy(final long managerUserId, final QueryOptions searchOptions) throws SBonitaSearchException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap("managerUserId", (Object) managerUserId);
            return persistenceService.searchEntity(SComment.class, MANAGED_BY, searchOptions, parameters);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaSearchException(bre);
        }
    }

    @Override
    public long getNumberOfArchivedComments(final QueryOptions searchOptions) throws SBonitaSearchException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            return persistenceService.getNumberOfEntities(SAComment.class, searchOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public List<SAComment> searchArchivedComments(final QueryOptions searchOptions) throws SBonitaSearchException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        try {
            return persistenceService.searchEntity(SAComment.class, searchOptions, null);
        } catch (final SBonitaReadException e) {
            throw new SBonitaSearchException(e);
        }
    }

    @Override
    public SComment addSystemComment(final long processInstanceId, final String comment) throws SCommentAddException {
        NullCheckingUtil.checkArgsNotNull(processInstanceId);
        NullCheckingUtil.checkArgsNotNull(comment);
        final SCommmentLogBuilder logBuilder = getCommmentLogBuilder(ActionType.CREATED, "creating a system comment");
        try {
            final SCommentBuilder sCommentBuilder = commentBuilders.getSSystemCommentBuilder();
            final SComment sComment = sCommentBuilder.createNewInstance(processInstanceId, comment, null).done();
            final InsertRecord insertRecord = new InsertRecord(sComment);
            SInsertEvent insertEvent = null;
            if (eventService.hasHandlers(COMMENT, EventActionType.CREATED)) {
                insertEvent = getInsertEvent(sComment);
            }
            recorder.recordInsert(insertRecord, insertEvent);
            initiateLogBuilder(processInstanceId, SQueriableLog.STATUS_OK, logBuilder, "addSystemComment");
            return sComment;
        } catch (final SRecorderException e) {
            initiateLogBuilder(processInstanceId, SQueriableLog.STATUS_FAIL, logBuilder, "addSystemComment");
            throw new SCommentAddException("Imposible to create system comment.", e);
        }
    }

    @Override
    public boolean isCommentEnabled(final SystemCommentType sct) {
        if (systemCommentType.containsKey(sct)) {
            return systemCommentType.get(sct);
        }
        return false;
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
    public SAComment getArchivedComment(final long archivedCommentId) throws SCommentNotFoundException, SBonitaReadException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SAComment selectById = persistenceService.selectById(new SelectByIdDescriptor<SAComment>("getArchivedCommentById", SAComment.class,
                archivedCommentId));
        if (selectById == null) {
            throw new SCommentNotFoundException("Archived comment not found with id=" + archivedCommentId);
        }
        return selectById;
    }

    @Override
    public void deleteArchivedComments(final long processInstanceId) throws SBonitaException {
        final SACommentBuilder archCommentKeyProvider = commentBuilders.getSACommentBuilder();
        final List<FilterOption> filters = Collections.singletonList(new FilterOption(SAComment.class, archCommentKeyProvider.getProcessInstanceIdKey(),
                processInstanceId));
        final List<OrderByOption> orderByOptions = Collections.singletonList(new OrderByOption(SAComment.class, archCommentKeyProvider.getIdKey(),
                OrderByType.ASC));
        List<SAComment> searchArchivedComments = null;
        // fromIndex always will be zero because the elements will be deleted
        final QueryOptions queryOptions = new QueryOptions(0, 100, orderByOptions, filters, null);
        do {
            searchArchivedComments = searchArchivedComments(queryOptions);
            for (final SAComment saComment : searchArchivedComments) {
                archiveService.recordDelete(new DeleteRecord(saComment), null);
            }
        } while (!searchArchivedComments.isEmpty());
    }
}
