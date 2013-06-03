/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.core.process.comment.api;

import java.util.List;

import org.bonitasoft.engine.core.process.comment.model.SComment;
import org.bonitasoft.engine.core.process.comment.model.archive.SAComment;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;

/**
 * @author Hongwen Zang
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 * @since 6.0
 */
public interface SCommentService {

    String COMMENT = "COMMENT";

    String COMMMENT_IS_DELETED = "deleting a comment";

    /**
     * List all comments related to the specified query options.
     * 
     * @param options
     *            a QueryOptions object, containing some query conditions
     * @return a list of SComment objects corresponding to the criteria
     * @throws SBonitaSearchException
     */
    List<SComment> searchComments(QueryOptions options) throws SBonitaSearchException;

    /**
     * Number of all comments related to the specified query options.
     * 
     * @param options
     *            a QueryOptions object, containing some query conditions
     * @return number of all comments corresponding to the criteria.
     * @throws SBonitaSearchException
     */
    long getNumberOfComments(QueryOptions options) throws SBonitaSearchException;

    /**
     * Add a comment on process instance
     * 
     * @param processInstanceId
     *            identifier of processInstance
     * @param comment
     *            the comment you want to add
     * @throws SCommentAddException
     */
    SComment addComment(long processInstanceId, String comment) throws SCommentAddException;

    /**
     * Add a system comment on process instance
     * 
     * @param processInstanceId
     *            identifier of processInstance
     * @param comment
     *            the comment you want to add
     * @throws SCommentAddException
     */
    SComment addSystemComment(long processInstanceId, String comment) throws SCommentAddException;

    /**
     * Get all comments for the given processInstance
     * 
     * @param processInstanceId
     *            identifier of processInstance
     * @return a list of SComment object
     * @throws SBonitaReadException
     */
    List<SComment> getComments(long processInstanceId) throws SBonitaReadException;

    /**
     * Search number of Comment for a specific supervisor
     * 
     * @param supervisorId
     *            the identifier of supervisor
     * @param queryOptions
     *            a map of specific parameters of a query
     * @return number of Comment for a specific supervisor
     * @throws SBonitaSearchException
     *             if a Read exception occurs
     */
    long getNumberOfCommentsSupervisedBy(long supervisorId, QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Search an Comment for a specific supervisor
     * 
     * @param supervisorId
     *            the identifier of supervisor
     * @param queryOptions
     *            a map of specific parameters of a query
     * @return an Comment for a specific supervisor
     * @throws SBonitaSearchException
     *             if a Read exception occurs
     */
    List<SComment> searchCommentsSupervisedBy(long supervisorId, QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Search number of all the comments on process instants that the user can access
     * 
     * @param userId
     *            identifier of user
     * @param searchOptions
     *            a QueryOptions object, containing some query conditions
     * @return number of all the comments on process instants that the user can access
     * @throws SBonitaSearchException
     */
    long getNumberOfCommentsInvolvingUser(long userId, QueryOptions searchOptions) throws SBonitaSearchException;

    /**
     * List all the comments on process instants that the user can access
     * 
     * @param userId
     *            identifier of user
     * @param queryOptions
     *            a QueryOptions object, containing some query conditions
     * @return a list of comments on process instants that the user can access
     * @throws SBonitaSearchException
     */
    List<SComment> searchCommentsInvolvingUser(long userId, QueryOptions queryOptions) throws SBonitaSearchException;

    /**
     * Search number of the comments visible by delegates of managerUserId
     * 
     * @param managerUserId
     *            identifier of a manager user
     * @param searchOptions
     *            a QueryOptions object, containing some query conditions
     * @return number of the comments visible by delegates of managerUserId
     * @throws SBonitaSearchException
     */
    long getNumberOfCommentsManagedBy(long managerUserId, QueryOptions searchOptions) throws SBonitaSearchException;

    /**
     * Search comments visible by delegates of managerUserId
     * 
     * @param managerUserId
     *            identifier of a manager user
     * @param searchOptions
     *            a QueryOptions object, containing some query conditions
     * @return a list of comments visible by delegates of managerUserId
     * @throws SBonitaSearchException
     */
    List<SComment> searchCommentsManagedBy(long managerUserId, QueryOptions searchOptions) throws SBonitaSearchException;

    /**
     * Search number of archived Comments
     * 
     * @param searchOptions
     * @param persistenceService
     * @return number of archived Comments
     * @throws SBonitaSearchException
     */
    long getNumberOfArchivedComments(QueryOptions searchOptions, ReadPersistenceService persistenceService) throws SBonitaSearchException;

    /**
     * Search archived Comments
     * 
     * @param searchOptions
     *            a QueryOptions object, containing some query conditions
     * @param persistenceService
     *            persistenceService object
     * @return a list with archived Comments
     * @throws SBonitaSearchException
     */
    List<SAComment> searchArchivedComments(QueryOptions searchOptions, ReadPersistenceService persistenceService) throws SBonitaSearchException;

    /**
     * Returning true if the system comments are enabled for the specific SystemCommentType.
     * 
     * @param sct
     *            A Enum for system comments
     * @return Returning true if the system comments are enabled for the specific SystemCommentType.
     * @since 6.0
     */
    boolean isCommentEnabled(SystemCommentType sct);

    /**
     * Delete the comment
     * 
     * @param comment
     * @throws SCommentDeletionException
     */
    void delete(SComment comment) throws SCommentDeletionException;

    /**
     * @param archivedCommentId
     * @throws SCommentNotFoundException
     * @throws SBonitaReadException
     */
    SAComment getArchivedComment(long archivedCommentId, ReadPersistenceService persistenceService) throws SCommentNotFoundException, SBonitaReadException;

}
