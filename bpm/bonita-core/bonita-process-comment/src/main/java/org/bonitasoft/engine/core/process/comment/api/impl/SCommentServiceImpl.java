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
package org.bonitasoft.engine.core.process.comment.api.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveInsertRecord;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.commons.NullCheckingUtil;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.core.process.comment.api.SCommentAddException;
import org.bonitasoft.engine.core.process.comment.api.SCommentDeletionException;
import org.bonitasoft.engine.core.process.comment.api.SCommentNotFoundException;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.api.SystemCommentType;
import org.bonitasoft.engine.core.process.comment.model.SComment;
import org.bonitasoft.engine.core.process.comment.model.SHumanComment;
import org.bonitasoft.engine.core.process.comment.model.SSystemComment;
import org.bonitasoft.engine.core.process.comment.model.archive.SAComment;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;

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

    private final ArchiveService archiveService;

    public SCommentServiceImpl(final Recorder recorder, final ReadPersistenceService persistenceService,
            final ArchiveService archiveService,
            final SessionService sessionService, final ReadSessionAccessor sessionAccessor,
            final Map<SystemCommentType, Boolean> systemCommentType) {
        super();
        this.recorder = recorder;
        this.persistenceService = persistenceService;
        this.sessionService = sessionService;
        this.sessionAccessor = sessionAccessor;
        this.systemCommentType = systemCommentType;
        this.archiveService = archiveService;
    }

    @Override
    public List<SComment> searchComments(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.searchEntity(SComment.class, options, null);
    }

    @Override
    public long getNumberOfComments(final QueryOptions options) throws SBonitaReadException {
        return persistenceService.getNumberOfEntities(SComment.class, options, null);
    }

    @Deprecated
    @Override
    public List<SComment> getComments(final long processInstanceId) throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("processInstanceId",
                (Object) processInstanceId);
        final OrderByOption orderByOption = new OrderByOption(SComment.class, "id", OrderByType.ASC);
        final QueryOptions queryOptions = new QueryOptions(Arrays.asList(orderByOption));
        final SelectListDescriptor<SComment> selectDescriptor = new SelectListDescriptor<SComment>("getSComments",
                parameters, SComment.class, queryOptions);
        return persistenceService.selectList(selectDescriptor);
    }

    @Override
    public List<SComment> getComments(final long processInstanceId, final QueryOptions queryOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("processInstanceId",
                (Object) processInstanceId);
        final SelectListDescriptor<SComment> selectDescriptor = new SelectListDescriptor<SComment>("getSComments",
                parameters, SComment.class, queryOptions);
        return persistenceService.selectList(selectDescriptor);
    }

    @Override
    public SComment addComment(long processInstanceId, String comment, long userId) throws SCommentAddException {
        NullCheckingUtil.checkArgsNotNull(processInstanceId);
        NullCheckingUtil.checkArgsNotNull(comment);
        try {
            final SComment sComment = new SHumanComment(processInstanceId, comment, userId);
            recorder.recordInsert(new InsertRecord(sComment), COMMENT);
            return sComment;
        } catch (final SRecorderException e) {
            throw new SCommentAddException(processInstanceId, "human", e);
        }
    }

    @Override
    public SComment addSystemComment(final long processInstanceId, final String comment) throws SCommentAddException {
        NullCheckingUtil.checkArgsNotNull(processInstanceId);
        NullCheckingUtil.checkArgsNotNull(comment);
        try {
            final SComment sComment = new SSystemComment(processInstanceId, comment);
            recorder.recordInsert(new InsertRecord(sComment), COMMENT);
            return sComment;
        } catch (final SRecorderException e) {
            throw new SCommentAddException(processInstanceId, "system", e);
        }
    }

    @Override
    public void delete(final SComment comment) throws SCommentDeletionException {
        NullCheckingUtil.checkArgsNotNull(comment);
        try {
            recorder.recordDelete(new DeleteRecord(comment), COMMENT);
        } catch (final SRecorderException e) {
            throw new SCommentDeletionException("Can't delete the comment " + comment, e);
        }
    }

    @Override
    public void deleteComments(final long processInstanceId) throws SBonitaException {
        final QueryOptions queryOptions = new QueryOptions(0, 100, SComment.class, "id", OrderByType.ASC);

        List<SComment> sComments = null;
        do {
            sComments = getComments(processInstanceId, queryOptions);
            if (sComments != null) {
                for (final SComment sComment : sComments) {
                    delete(sComment);
                }
            }
        } while (sComments != null && sComments.size() > 0);
    }

    private long getUserId() {
        return sessionService.getLoggedUserFromSession(sessionAccessor);
    }

    @Override
    public long getNumberOfCommentsSupervisedBy(final long supervisorId, final QueryOptions queryOptions)
            throws SBonitaReadException {
        try {
            final Map<String, Object> parameters = Collections.singletonMap("supervisorId", (Object) supervisorId);
            return persistenceService.getNumberOfEntities(SComment.class, SUPERVISED_BY, queryOptions, parameters);
        } catch (final SBonitaReadException bre) {
            throw new SBonitaReadException(bre);
        }
    }

    @Override
    public List<SComment> searchCommentsSupervisedBy(final long supervisorId, final QueryOptions queryOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("supervisorId", (Object) supervisorId);
        return persistenceService.searchEntity(SComment.class, SUPERVISED_BY, queryOptions, parameters);
    }

    @Override
    public long getNumberOfCommentsInvolvingUser(final long userId, final QueryOptions searchOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
        return persistenceService.getNumberOfEntities(SComment.class, INVOLVING_USER, searchOptions, parameters);
    }

    @Override
    public List<SComment> searchCommentsInvolvingUser(final long userId, final QueryOptions queryOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("userId", (Object) userId);
        return persistenceService.searchEntity(SComment.class, INVOLVING_USER, queryOptions, parameters);
    }

    @Override
    public long getNumberOfCommentsManagedBy(final long managerUserId, final QueryOptions searchOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("managerUserId", (Object) managerUserId);
        return persistenceService.getNumberOfEntities(SComment.class, MANAGED_BY, searchOptions, parameters);
    }

    @Override
    public List<SComment> searchCommentsManagedBy(final long managerUserId, final QueryOptions searchOptions)
            throws SBonitaReadException {
        final Map<String, Object> parameters = Collections.singletonMap("managerUserId", (Object) managerUserId);
        return persistenceService.searchEntity(SComment.class, MANAGED_BY, searchOptions, parameters);
    }

    @Override
    public long getNumberOfArchivedComments(final QueryOptions searchOptions) throws SBonitaReadException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        return persistenceService.getNumberOfEntities(SAComment.class, searchOptions, null);
    }

    @Override
    public List<SAComment> searchArchivedComments(final QueryOptions searchOptions) throws SBonitaReadException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        return persistenceService.searchEntity(SAComment.class, searchOptions, null);
    }

    @Override
    public boolean isCommentEnabled(final SystemCommentType sct) {
        if (systemCommentType.containsKey(sct)) {
            return systemCommentType.get(sct);
        }
        return false;
    }

    @Override
    public SAComment getArchivedComment(final long archivedCommentId)
            throws SCommentNotFoundException, SBonitaReadException {
        final ReadPersistenceService persistenceService = archiveService.getDefinitiveArchiveReadPersistenceService();
        final SAComment selectById = persistenceService.selectById(new SelectByIdDescriptor<SAComment>(SAComment.class,
                archivedCommentId));
        if (selectById == null) {
            throw new SCommentNotFoundException("Archived comment not found with id=" + archivedCommentId);
        }
        return selectById;
    }

    @Override
    public void deleteArchivedComments(List<Long> processInstanceIds) throws SBonitaException {
        archiveService.deleteFromQuery("deleteArchiveCommentsOfProcessInstances",
                Collections.singletonMap("processInstanceIds", processInstanceIds));
    }

    @Override
    public void archive(final long archiveDate, final SComment sComment) throws SObjectModificationException {
        final ArchiveInsertRecord insertRecord = new ArchiveInsertRecord(new SAComment(sComment));
        try {
            archiveService.recordInsert(archiveDate, insertRecord);
        } catch (final SRecorderException e) {
            throw new SObjectModificationException("Unable to archive the comment with id = <" + sComment.getId() + ">",
                    e);
        }
    }

}
