/**
 * Copyright (C) 2012-2014 BonitaSoft S.A.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.comment.api.SCommentAddException;
import org.bonitasoft.engine.core.process.comment.api.SCommentDeletionException;
import org.bonitasoft.engine.core.process.comment.api.SCommentNotFoundException;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.api.SystemCommentType;
import org.bonitasoft.engine.core.process.comment.model.SComment;
import org.bonitasoft.engine.core.process.comment.model.archive.SAComment;
import org.bonitasoft.engine.core.process.comment.model.archive.builder.SACommentBuilderFactory;
import org.bonitasoft.engine.core.process.comment.model.builder.SHumanCommentBuilderFactory;
import org.bonitasoft.engine.core.process.comment.model.builder.SSystemCommentBuilderFactory;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
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

    private final Recorder recorder;

    private final ReadPersistenceService persistenceService;

    private final SessionService sessionService;

    private final ReadSessionAccessor sessionAccessor;

    private final Map<SystemCommentType, Boolean> systemCommentType;

    private final EventService eventService;

    private final ArchiveService archiveService;

    public SCommentServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceService, final ArchiveService archiveService,
            final SessionService sessionService, final ReadSessionAccessor sessionAccessor, final Map<SystemCommentType, Boolean> systemCommentType,
            final EventService eventService) {
        super();
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.sessionService = sessionService;
        this.sessionAccessor = sessionAccessor;
        this.systemCommentType = systemCommentType;
        this.eventService = eventService;
        this.archiveService = archiveService;
    }

    private SInsertEvent getInsertEvent(final Object obj) {
        return (SInsertEvent) BuilderFactory.get(SEventBuilderFactory.class).createInsertEvent(COMMENT).setObject(obj).done();
    }

    private SDeleteEvent getDeleteEvent(final Object obj) {
        return (SDeleteEvent) BuilderFactory.get(SEventBuilderFactory.class).createDeleteEvent(COMMENT).setObject(obj).done();
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

    @Deprecated
    @Override
    public List<SComment> getComments(final long processInstanceId) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("processInstanceId", (Object) processInstanceId);
        OrderByOption orderByOption = new OrderByOption(SComment.class, "id", OrderByType.ASC);
        final QueryOptions queryOptions = new QueryOptions(Arrays.asList(orderByOption));
        final SelectListDescriptor<SComment> selectDescriptor = new SelectListDescriptor<SComment>("getSComments", parameters, SComment.class, queryOptions);
        return persistenceService.selectList(selectDescriptor);
    }

    @Override
    public List<SComment> getComments(final long processInstanceId, final QueryOptions queryOptions) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("processInstanceId", (Object) processInstanceId);
        final SelectListDescriptor<SComment> selectDescriptor = new SelectListDescriptor<SComment>("getSComments", parameters, SComment.class, queryOptions);
        return persistenceService.selectList(selectDescriptor);
    }

    @Override
    public SComment addComment(final long processInstanceId, final String comment) throws SCommentAddException {
        NullCheckingUtil.checkArgsNotNull(processInstanceId);
        NullCheckingUtil.checkArgsNotNull(comment);
        try {
            final long userId = getUserId();
            final SComment sComment = BuilderFactory.get(SHumanCommentBuilderFactory.class).createNewInstance(processInstanceId, comment, userId).done();
            final InsertRecord insertRecord = new InsertRecord(sComment);
            SInsertEvent insertEvent = null;
            if (eventService.hasHandlers(COMMENT, EventActionType.CREATED)) {
                insertEvent = getInsertEvent(sComment);
            }
            recorder.recordInsert(insertRecord, insertEvent);
            return sComment;
        } catch (final SRecorderException e) {
            throw new SCommentAddException(processInstanceId, "human", e);
        } catch (final SSessionNotFoundException e) {
            throw new SCommentAddException("Session is not found.", e);
        }
    }

    @Override
    public SComment addSystemComment(final long processInstanceId, final String comment) throws SCommentAddException {
        NullCheckingUtil.checkArgsNotNull(processInstanceId);
        NullCheckingUtil.checkArgsNotNull(comment);
        try {
            final SComment sComment = BuilderFactory.get(SSystemCommentBuilderFactory.class).createNewInstance(processInstanceId, comment, null).done();
            final InsertRecord insertRecord = new InsertRecord(sComment);
            SInsertEvent insertEvent = null;
            if (eventService.hasHandlers(COMMENT, EventActionType.CREATED)) {
                insertEvent = getInsertEvent(sComment);
            }
            recorder.recordInsert(insertRecord, insertEvent);
            return sComment;
        } catch (final SRecorderException e) {
            throw new SCommentAddException(processInstanceId, "system", e);
        }
    }

    @Override
    public void delete(final SComment comment) throws SCommentDeletionException {
        NullCheckingUtil.checkArgsNotNull(comment);
        try {
            final DeleteRecord deleteRecord = new DeleteRecord(comment);
            SDeleteEvent deleteEvent = null;
            if (eventService.hasHandlers(COMMENT, EventActionType.DELETED)) {
                deleteEvent = getDeleteEvent(comment);
            }
            recorder.recordDelete(deleteRecord, deleteEvent);
        } catch (final SRecorderException e) {
            throw new SCommentDeletionException("Can't delete the comment " + comment, e);
        }
    }

    @Override
    public void deleteComments(final long processInstanceId) throws SBonitaException {
        List<SComment> sComments = null;
        do {
            sComments = getComments(processInstanceId, new QueryOptions(0, 100));
            if (sComments != null) {
                for (final SComment sComment : sComments) {
                    delete(sComment);
                }
            }
        } while (sComments != null && sComments.size() > 0);
    }

    private long getUserId() throws SSessionNotFoundException {
        long sessionId;
        try {
            sessionId = sessionAccessor.getSessionId();
        } catch (SessionIdNotSetException e) {
            // system
            return -1;
        }
        final SSession session = sessionService.getSession(sessionId);
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
    public boolean isCommentEnabled(final SystemCommentType sct) {
        if (systemCommentType.containsKey(sct)) {
            return systemCommentType.get(sct);
        }
        return false;
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
        final List<FilterOption> filters = Collections.singletonList(new FilterOption(SAComment.class, BuilderFactory.get(SACommentBuilderFactory.class)
                .getProcessInstanceIdKey(), processInstanceId));
        final List<OrderByOption> orderByOptions = Collections.singletonList(new OrderByOption(SAComment.class, BuilderFactory.get(
                SACommentBuilderFactory.class).getIdKey(), OrderByType.ASC));
        List<SAComment> searchArchivedComments = null;
        // fromIndex always will be zero because the elements will be deleted
        final QueryOptions queryOptions = new QueryOptions(0, 100, orderByOptions, filters, null);
        do {
            searchArchivedComments = searchArchivedComments(queryOptions);
            for (final SAComment saComment : searchArchivedComments) {
                archiveService.recordDelete(new DeleteRecord(saComment));
            }
        } while (!searchArchivedComments.isEmpty());
    }
}
