/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.instance.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.SLoopActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SMultiInstanceActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SPendingActivityMapping;
import org.bonitasoft.engine.core.process.instance.model.SUserTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAHumanTaskInstance;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Hongwen Zang
 * @author Yanyan Liu
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @since 6.0
 */
public interface ActivityInstanceService extends FlowNodeInstanceService {

    String ACTIVITYINSTANCE = "ACTIVITYINSTANCE";

    String ARCHIVED_ACTIVITYINSTANCE = "ARCHIVED_ACTIVITYINSTANCE";

    String PENDINGACTIVITYMAPPING = "PENDINGACTIVITYMAPPING";

    /**
     * Create activityInstance in DB according to the given activityInstance object
     *
     * @param activityInstance
     *        an SActivityInstance object
     * @throws SActivityCreationException
     */
    void createActivityInstance(SActivityInstance activityInstance) throws SActivityCreationException;

    /**
     * Create a new pending activity mapping in DB
     *
     * @param mapping
     *        pending activity mapping object
     * @throws SActivityCreationException
     */
    void addPendingActivityMappings(SPendingActivityMapping mapping) throws SActivityCreationException;

    /**
     * deletePendingMappings
     *
     * @param mapping
     *        pending activity mapping object
     * @throws SActivityModificationException
     */
    void deletePendingMappings(long humanTaskInstanceId) throws SActivityModificationException;

    /**
     * Delete all pending mappings for the connected tenant
     *
     * @throws SActivityModificationException
     * @since 6.1
     */
    void deleteAllPendingMappings() throws SActivityModificationException;

    /**
     * Get activityInstance by its id
     *
     * @param activityInstanceId
     *        identifier of activityInstance
     * @return an SActivityInstance object with id corresponding to the parameter
     * @throws SActivityInstanceNotFoundException
     *         if no activityInstance found
     * @throws SActivityReadException
     */
    SActivityInstance getActivityInstance(long activityInstanceId) throws SActivityInstanceNotFoundException, SActivityReadException;

    /**
     * Get humanTaskInstance by its id
     *
     * @param activityInstanceId
     *        identifier of humanTaskInstance
     * @return an SHumanTaskInstance object with id corresponding to the parameter
     * @throws SActivityInstanceNotFoundException
     * @throws SActivityReadException
     */
    SHumanTaskInstance getHumanTaskInstance(long activityInstanceId) throws SActivityInstanceNotFoundException, SActivityReadException;

    /**
     * Get activities with specific states in the root container in specific order, this is used for pagination
     *
     * @param rootContainerId
     *        identifier of root container, it always is process definition id
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param maxResults
     *        Number of result we want to get. Maximum number of result returned
     * @param sortingField
     *        the field used to do order
     * @param sortingOrder
     *        ASC or DESC
     * @param stateIds
     *        Identifiers of states
     * @return a list of SActivityInstance objects
     * @throws SActivityReadException
     */
    List<SActivityInstance> getActivitiesWithStates(long rootContainerId, Set<Integer> stateIds, int fromIndex, int maxResults, String sortingField,
            OrderByType sortingOrder) throws SActivityReadException;

    /**
     * Get the most recent archived version of a specified activity instance
     *
     * @param activityInstanceId
     *        identifier of activity instance
     * @return an SAActivityInstance object
     * @throws SActivityReadException
     *         if a Read error occurs
     * @throws SActivityInstanceNotFoundException
     *         it the provided activityInstanceId does not refer to an existing Activity Instance
     */
    SAActivityInstance getMostRecentArchivedActivityInstance(long activityInstanceId) throws SActivityReadException, SActivityInstanceNotFoundException;

    /**
     * Get pending tasks for the user in specific actors. This is used for pagination
     *
     * @param userId
     *        identifier of user
     * @param actorIds
     *        identifiers of actor
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param maxResults
     *        Number of result we want to get. Maximum number of result returned
     * @param sortFieldName
     *        the field used to do order
     * @param order
     *        ASC or DESC
     * @return a list of SActivityInstance objects
     * @throws SActivityReadException
     */
    List<SHumanTaskInstance> getPendingTasks(long userId, Set<Long> actorIds, int fromIndex, int maxResults, String sortFieldName, OrderByType order)
            throws SActivityReadException;

    /**
     * Get tasks assigned to the user. This is used for pagination
     *
     * @param userId
     *        identifier of user
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param maxResults
     *        Number of result we want to get. Maximum number of result returned
     * @param sortFieldName
     *        the field used to do order
     * @param order
     *        ASC or DESC
     * @return a list of SHumanTaskInstance objects
     * @throws SActivityReadException
     */
    List<SHumanTaskInstance> getAssignedUserTasks(long userId, int fromIndex, int maxResults, String sortFieldName, OrderByType order)
            throws SActivityReadException;

    /**
     * Get archived activity instances in the specific root container.
     *
     * @param rootContainerId
     *        identifier of root container, the root container can be process instance
     * @param queryOptions
     *        a map of specific parameters of a query
     * @return a list of SAActivityInstance objects
     * @throws SActivityReadException
     */
    List<SAActivityInstance> getArchivedActivityInstances(long rootContainerId, QueryOptions queryOptions) throws SActivityReadException;

    /**
     * Get total number of open activity instances for the specific process instance
     *
     * @param processInstanceId
     *        identifier of process instance
     * @return the number of opened activity instances in the specific process instance
     * @throws SActivityReadException
     */
    int getNumberOfOpenActivityInstances(long processInstanceId) throws SActivityReadException;

    /**
     * Get all open activity instances in the specific process instance. This is used for pagination
     *
     * @param rootContainerId
     *        identifier of root container, the root container can be process instance
     * @param pageIndex
     *        the page index to indicate which page will be retrieved. First page has index 0
     * @param maxResults
     *        Number of result we want to get. Maximum number of result returned
     * @param sortingField
     *        the field used to do order
     * @param orderbyType
     *        ASC or DESC
     * @return a list of SActivityInstance objects
     * @throws SActivityReadException
     */
    List<SActivityInstance> getOpenActivityInstances(long rootContainerId, int pageIndex, int maxResults, String sortingField, OrderByType orderbyType)
            throws SActivityReadException;

    /**
     * Get all activity instances for the specific process instance
     *
     * @param rootContainerId
     *        identifier of root container, the root container can be process instance
     * @return a list of SActivityInstance objects
     * @throws SActivityReadException
     */
    List<SActivityInstance> getActivityInstances(long rootContainerId, int fromIndex, int numberOfResults)
            throws SActivityReadException;

    /**
     * Get all child instances for the specific parent activity instance, order by id ascending.
     *
     * @param parentActivityInstanceId
     *        identifier of parent activity instance
     * @param fromIndex
     *        Index of the record to be retrieved from. First record has index 0
     * @param numberOfResults
     *        TODO
     * @return a list of SActivityInstance objects
     * @throws SActivityReadException
     */
    List<SActivityInstance> getChildrenOfAnActivity(long parentActivityInstanceId, int fromIndex, int numberOfResults) throws SActivityReadException;

    /**
     * Assign the specific human task to the user
     *
     * @param userTaskId
     *        identifier of human task instance
     * @param userId
     *        identifier of user
     * @throws SFlowNodeNotFoundException
     * @throws SFlowNodeReadException
     * @throws SActivityModificationException
     */
    void assignHumanTask(long userTaskId, long userId) throws SFlowNodeNotFoundException, SFlowNodeReadException, SActivityModificationException;

    /**
    /**
     * Get the number of UserTask instances assigned to a specific user
     *
     * @param userId
     *        the id of the user concerned
     * @return the number of UserTask instances assigned to this specific user
     * @throws SActivityReadException
     *         if a Read exception occurs
     */
    long getNumberOfAssignedHumanTaskInstances(long userId) throws SActivityReadException;

    /**
     * Search UserTask instances assigned for a specific supervisor
     *
     * @param parameters
     *        a map of specific parameters of a query
     * @param parameters
     *        a map of specific parameters of a query
     * @return the number of UserTask assigned to this specific supervisor
     * @throws SActivityReadException
     *         if a Read exception occurs
     */
    long getNumberOfAssignedTasksSupervisedBy(final long supervisorId, final QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Search AUserTask instances archived for a specific supervisor
     *
     * @param queryOptions
     *        the object used to manage all the search parameters of a query
     * @param parameters
     *        a map of specific parameters of a query
     * @return the number of UserTask archived to this specific supervisor
     * @throws SActivityReadException
     *         if a Read exception occurs
     */
    long getNumberOfArchivedHumanTasksSupervisedBy(final long supervisorId, final QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Search UserTask instances assigned for a specific supervisor
     *
     * @param queryOptions
     *        the object used to manage all the search parameters of a query
     * @param parameters
     *        a map of specific parameters of a query
     * @return the UserTask instances list assigned to this specific supervisor
     * @throws SActivityReadException
     *         if a Read exception occurs
     */
    List<SHumanTaskInstance> searchAssignedTasksSupervisedBy(final long supervisorId, final QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Search AUserTask instances archived for a specific supervisor
     *
     * @param queryOptions
     *        the object used to manage all the search parameters of a query
     * @param parameters
     *        a map of specific parameters of a query
     * @return the UserTask instances list archived to this specific supervisor
     * @throws SActivityReadException
     *         if a Read exception occurs
     */
    List<SAHumanTaskInstance> searchArchivedHumanTasksSupervisedBy(final long supervisorId, final QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Gets the archive instance of the activity according to its identifier at a given state.
     *
     * @param activityId
     *        the activity identifier
     * @param stateId
     *        the state identifier
     * @param persistenceService
     * @return
     * @throws SActivityReadException
     *         if a Read exception occurs
     * @throws SActivityInstanceNotFoundException
     */

    SAActivityInstance getArchivedActivityInstance(long activityInstanceId, int stateId) throws SActivityReadException, SActivityInstanceNotFoundException;

    /**
     * Search archived human tasks according to specific search criteria
     *
     * @param searchOptions
     *        the object used to manage all the search parameters of a query
     * @param persistenceService
     *        used to retrieve the archived tasks
     * @return a list of SAHumanTaskInstance objects
     * @throws SBonitaReadException
     */
    List<SAHumanTaskInstance> searchArchivedTasks(QueryOptions searchOptions) throws SBonitaReadException;

    /**
     * Get total number of archived tasks according to specific search criteria
     *
     * @param searchOptions
     *        the object used to manage all the search parameters of a query
     * @param persistenceService
     *        used to retrieve the archived tasks
     * @return
     * @throws SBonitaReadException
     */
    long getNumberOfArchivedTasks(QueryOptions searchOptions) throws SBonitaReadException;

    /**
     * Get total number of assigned tasks managed by the specific manager
     *
     * @param managerUserId
     *        identifier of manager user
     * @param searchOptions
     *        the object used to manage all the search parameters of a query
     * @return number of assigned tasks managed by the specific manager
     * @throws SBonitaReadException
     */
    long getNumberOfAssignedTasksManagedBy(long managerUserId, QueryOptions searchOptions) throws SBonitaReadException;

    /**
     * Get all assigned tasks managed by the specific manager
     *
     * @param managerUserId
     *        identifier of manager user
     * @param searchOptions
     *        the object used to manage all the search parameters of a query
     * @return a list of SHumanTaskInstance objects
     */
    List<SHumanTaskInstance> searchAssignedTasksManagedBy(long managerUserId, QueryOptions searchOptions) throws SBonitaReadException;

    /**
     * get the total number of archived tasks assigned to subordinates of specified manager.
     *
     * @param managerUserId
     *        the userId of the manager
     * @param searchOptions
     *        the search options to paginate, filter, ...
     * @return the number of elements encountered
     * @throws SBonitaReadException
     *         in case a search error occurs
     */
    long getNumberOfArchivedTasksManagedBy(long managerUserId, QueryOptions searchOptions) throws SBonitaReadException;

    /**
     * get the archived tasks assigned to subordinates of specified manager, limited to, sorted, paginated with the specifies QueryOptions
     *
     * @param managerUserId
     *        the userId of the manager
     * @param searchOptions
     *        the search options to paginate, filter, sort ...
     * @return the elements encountered matching the specified options
     * @throws SBonitaReadException
     *         in case a search error occurs
     */
    List<SAHumanTaskInstance> searchArchivedTasksManagedBy(long managerUserId, QueryOptions searchOptions) throws SBonitaReadException;

    /**
     * Search all pending human task instances for the specific supervisor
     *
     * @param userId
     *        identifier of supervisor user
     * @param searchOptions
     *        the search options to paginate, filter, sort ...
     * @return a list of SHumanTaskInstance objects
     * @throws SBonitaReadException
     */
    List<SHumanTaskInstance> searchPendingTasksSupervisedBy(long userId, QueryOptions searchOptions) throws SBonitaReadException;

    /**
     * Get total number of pending human task instances for the specific supervisor
     *
     * @param userId
     *        identifier of supervisor user
     * @param queryOptions
     *        the search options to paginate, filter, sort ...
     * @return number of pending human task instances for the specific supervisor
     * @throws SBonitaReadException
     */
    long getNumberOfPendingTasksSupervisedBy(long userId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Get number of human task instances according to the criteria
     *
     * @param queryOptions
     *        the search options to paginate, filter, sort ...
     * @return number of human task instances satisfied to the criteria
     * @throws SBonitaReadException
     */
    long getNumberOfHumanTasks(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Search all human task instances according to the criteria
     *
     * @param queryOptions
     *        the search options to paginate, filter, sort ...
     * @return a list of SHumanTaskInstance objects
     * @throws SBonitaReadException
     */
    List<SHumanTaskInstance> searchHumanTasks(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Get number of open tasks for each user
     *
     * @param userIds
     *        identifiers of users
     * @return a map containing user id and corresponding task number
     * @throws SBonitaReadException
     */
    Map<Long, Long> getNumberOfOpenTasksForUsers(List<Long> userIds) throws SBonitaReadException;

    /**
     * Search total number of pending tasks for the specific manager
     *
     * @param managerUserId
     *        identifier of manager user
     * @param searchOptions
     *        the search options to paginate, filter, sort ...
     * @return number of pending tasks
     * @throws SBonitaReadException
     */
    long searchNumberOfPendingTasksManagedBy(long managerUserId, QueryOptions searchOptions) throws SBonitaReadException;

    /**
     * Search all pending tasks for the specific manager
     *
     * @param managerUserId
     *        identifier of manager user
     * @param searchOptions
     *        the search options to paginate, filter, sort ...
     * @return a list of SHumanTaskInstance objects
     * @throws SBonitaReadException
     */
    List<SHumanTaskInstance> searchPendingTasksManagedBy(long managerUserId, QueryOptions searchOptions) throws SBonitaReadException;

    /**
     * Increase loopCounter(loopCount+1) for the specific loop instance
     *
     * @param loopInstance
     *        the loopCounter in which will be increased
     * @throws SActivityModificationException
     */
    void incrementLoopCounter(final SLoopActivityInstance loopInstance) throws SActivityModificationException;

    /**
     * Get number of overdue open tasks for each user
     *
     * @param userIds
     *        identifiers of users
     * @return a map containing userId and corresponding number of tasks
     * @throws SBonitaReadException
     */
    Map<Long, Long> getNumberOfOverdueOpenTasksForUsers(List<Long> userIds) throws SBonitaReadException;

    /**
     * Set max loop for the specific loopActvity
     *
     * @param loopActivity
     *        the loopActivity
     * @param result
     *        value for max loop
     * @throws SActivityModificationException
     */
    void setLoopMax(SLoopActivityInstance loopActivity, Integer result) throws SActivityModificationException;

    /**
     * Set LoopCardinality for the specific loopActvity
     *
     * @param flowNodeInstance
     *        the loopActvity
     * @param intLoopCardinality
     *        value of loop cardinality
     * @throws SActivityModificationException
     */
    void setLoopCardinality(SFlowNodeInstance flowNodeInstance, int intLoopCardinality) throws SActivityModificationException;

    /**
     * Add number of activeInstances for the specific SMultiInstanceActivityInstance object
     *
     * @param flowNodeInstance
     *        an SMultiInstanceActivityInstance object
     * @param number
     *        the number will be added
     * @throws SActivityModificationException
     */
    void addMultiInstanceNumberOfActiveActivities(SMultiInstanceActivityInstance flowNodeInstance, int number) throws SActivityModificationException;

    /**
     * Add number of terminated activeInstances for the specific SMultiInstanceActivityInstance object
     *
     * @param flowNodeInstance
     *        an SMultiInstanceActivityInstance object
     * @param number
     *        will be added to terminated instances of flowNodeInstance
     *        the number will be added
     * @throws SActivityModificationException
     */
    void addMultiInstanceNumberOfTerminatedActivities(SMultiInstanceActivityInstance flowNodeInstance, int number) throws SActivityModificationException;

    /**
     * Add number of completed activeInstances for the specific SMultiInstanceActivityInstance object
     *
     * @param flowNodeInstance
     *        an SMultiInstanceActivityInstance object whose completed activity number will be updated
     * @param number
     *        the number will be added
     * @throws SActivityModificationException
     */
    void addMultiInstanceNumberOfCompletedActivities(SMultiInstanceActivityInstance flowNodeInstance, int number) throws SActivityModificationException;

    /**
     * Get total number of activity instances for the specific entity class
     *
     * @param entityClass
     *        to indicate which type of class will be retrieved
     * @param searchOptions
     *        the search options to paginate, filter, sort ...
     * @return number of activity instances for the specific entity class
     * @throws SBonitaReadException
     */
    long getNumberOfActivityInstances(Class<? extends PersistentObject> entityClass, QueryOptions searchOptions) throws SBonitaReadException;

    /**
     * Search all activity instances for the specific entity class
     *
     * @param entityClass
     *        to indicate which type of class will be retrieved
     * @param searchOptions
     *        the search options to paginate, filter, sort ...
     * @return a list of SActivityInstance objects
     * @throws SBonitaReadException
     */
    List<SActivityInstance> searchActivityInstances(Class<? extends PersistentObject> entityClass, QueryOptions searchOptions) throws SBonitaReadException;

    /**
     * Get total number of archived activity instances for the specific entity class
     *
     * @param entityClass
     *        to indicate which type of class will be retrieved
     * @param searchOptions
     *        the search options to paginate, filter, sort ...
     * @return number of archived activity instances for the specific entity class
     * @throws SBonitaReadException
     */
    long getNumberOfArchivedActivityInstances(Class<? extends PersistentObject> entityClass, QueryOptions searchOptions) throws SBonitaReadException;

    /***
     * Search all archived activity instances for the specific entity class
     *
     * @param entityClass
     *        to indicate which type of class will be retrieved
     * @param searchOptions
     *        the search options to paginate, filter, sort ...
     * @return a list of SAActivityInstance objects
     * @throws SBonitaReadException
     */
    List<SAActivityInstance> searchArchivedActivityInstances(Class<? extends PersistentObject> entityClass, QueryOptions searchOptions)
            throws SBonitaReadException;

    /**
     * Set tokenCount for the specific activity instance
     *
     * @param activityInstance
     *        the activityInstance will be updated
     * @param tokenCount
     *        value of tokenCount will be set to the activity
     * @throws SFlowNodeModificationException
     */
    void setTokenCount(final SActivityInstance activityInstance, int tokenCount) throws SFlowNodeModificationException;

    /**
     * @param userId
     * @param searchOptions
     * @return
     * @since 6.0
     */
    long getNumberOfPendingTasksForUser(long userId, QueryOptions searchOptions) throws SBonitaReadException;

    /**
     * @param userId
     * @param searchOptions
     * @return
     * @since 6.0
     */
    List<SHumanTaskInstance> searchPendingTasksForUser(long userId, QueryOptions searchOptions) throws SBonitaReadException;

    /**
     * @param humanTaskInstanceId
     * @param queryOptions
     * @return
     * @throws SBonitaReadException
     */
    List<SPendingActivityMapping> getPendingMappings(long humanTaskInstanceId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * @param userId
     * @param searchOptions
     * @return
     * @throws SBonitaReadException
     * @since 6.0
     */
    List<SHumanTaskInstance> searchPendingOrAssignedTasks(long userId, QueryOptions searchOptions) throws SBonitaReadException;

    /**
     * @param userId
     * @param searchOptions
     * @return
     * @throws SBonitaReadException
     * @since 6.0
     */
    long getNumberOfPendingOrAssignedTasks(long userId, QueryOptions searchOptions) throws SBonitaReadException;

    /**
     * @param flowNodeInstanceId
     *     delete pending mapping of this flow node
     */
    void deleteArchivedPendingMappings(long flowNodeInstanceId) throws SActivityModificationException;

    /**
     * @param activityInstance
     * @param boundaryEventId
     * @throws SActivityModificationException
     * @since 6.0
     */
    void setAbortedByBoundaryEvent(SActivityInstance activityInstance, long boundaryEventId) throws SActivityModificationException;

    /**
     * @param processInstanceId
     * @return
     * @throws SBonitaReadException
     * @since 6.0
     */
    int getNumberOfActivityInstances(long processInstanceId) throws SActivityReadException;

    List<Long> getPossibleUserIdsOfPendingTasks(long humanTaskInstanceId, int startIndex, int maxResults) throws SActivityReadException;

    /**
     * Retrieve the total number of the archived Activities matching the given search criteria, for a specific supervisor.
     *
     * @param supervisorId
     *        The identifier of the supervisor
     * @param entityClass
     *        The type of the archived flow node to search for
     * @param queryOptions
     *        The search options to filter the results
     * @return The number found, 0 if no matching search criteria
     * @since 6.3
     */
    long getNumberOfArchivedActivityInstancesSupervisedBy(long supervisorId, Class<? extends SAActivityInstance> entityClass, QueryOptions queryOptions)
            throws SBonitaReadException;

    boolean isTaskPendingForUser(long humanTaskInstanceId, long userId) throws SBonitaReadException;
    /**
     * Retrieve the total number of the archived Activities matching the given search criteria, for a specific supervisor.
     *
     * @param supervisorId
     *        The identifier of the supervisor
     * @param entityClass
     *        The type of the archived flow node to search for
     * @param queryOptions
     *        The search options to filter the results
     * @return The list of paginated results, according to the QueryOptions search criteria
     * @since 6.3
     */
    List<SAActivityInstance> searchArchivedActivityInstancesSupervisedBy(long supervisorId, Class<? extends SAActivityInstance> entityClass,
            QueryOptions queryOptions)
            throws SBonitaReadException;

    /**
     * Get total number of users according to specific query options, and who can start the task filtered with the search option
     * of the given process definition
     *
     * @param searchOptions
     *        The QueryOptions object containing some query conditions
     * @return
     */
    long getNumberOfUsersWhoCanExecutePendingHumanTaskDeploymentInfo(long humanTaskInstanceId, QueryOptions searchOptions) throws SBonitaReadException;

    /**
     * Search the users according to specific query options, and who can start the task filtered with the search option
     * of the given process definition
     *
     * @param searchOptions
     *        The QueryOptions object containing some query conditions
     * @return
     */
    List<SUser> searchUsersWhoCanExecutePendingHumanTaskDeploymentInfo(long humanTaskInstanceId, QueryOptions searchOptions) throws SBonitaReadException;

    /**
     * Get the total number of the assigned and pending human tasks for the specified user, on the specified root process definition, corresponding to the
     * options.
     *
     * @param rootProcessDefinitionId
     *        The identifier of the root process definition
     * @param userId
     *        The identifier of the user
     * @param queryOptions
     *        The search conditions and the options for sorting and paging the results.
     * @return The assigned and pending human tasks
     * @since 6.3.3
     */
    long getNumberOfAssignedAndPendingHumanTasksFor(long rootProcessDefinitionId, long userId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Search the assigned and pending human tasks for the specified user, on the specified root process definition, corresponding to the options.
     *
     * @param rootProcessDefinitionId
     *        The identifier of the root process definition
     * @param userId
     *        The identifier of the user
     * @param queryOptions
     *        The search conditions and the options for sorting and paging the results.
     * @return The assigned and pending human tasks
     * @since 6.3.3
     */
    List<SHumanTaskInstance> searchAssignedAndPendingHumanTasksFor(long rootProcessDefinitionId, long userId, QueryOptions queryOptions)
            throws SBonitaReadException;

    /**
     * Get the total number of the assigned and pending human tasks for any user, on the specified root process definition, corresponding to the
     * options.
     *
     * @param rootProcessDefinitionId
     *        The identifier of the root process definition
     * @param queryOptions
     *        The search conditions and the options for sorting and paging the results.
     * @return The assigned and pending human tasks
     * @since 6.3.3
     */
    long getNumberOfAssignedAndPendingHumanTasks(long rootProcessDefinitionId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Search the assigned and pending human tasks for any user, on the specified root process definition, corresponding to the options.
     *
     * @param rootProcessDefinitionId
     *        The identifier of the root process definition
     * @param queryOptions
     *        The search conditions and the options for sorting and paging the results.
     * @return The assigned and pending human tasks
     * @since 6.3.3
     */
    List<SHumanTaskInstance> searchAssignedAndPendingHumanTasks(long rootProcessDefinitionId, QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Delete archived flow node instances and their elements
     *
     * @param processInstanceId
     * @throws SFlowNodeDeletionException
     * @since 6.4.0
     */
    void deleteArchivedFlowNodeInstances(long processInstanceId) throws SFlowNodeDeletionException;

    /**
     * @param parentActivityInstanceId
     * @param maxNumberOfResults
     * @return
     * @since 6.4.0
     */
    QueryOptions buildQueryOptionsForSubActivitiesInNormalStateAndNotTerminal(long parentActivityInstanceId, int numberOfResults);




    /**
     * Returns the instance of the user task.
     *
     * @param userTaskInstanceId the identifier of the instance of the user task
     * @return the instance of the user task
     * @throws SActivityInstanceNotFoundException
     *         if the identifier does not refer to an existing user task
     * @throws SActivityReadException
     *         if an exception occurs when retrieving the instance
     */
    SUserTaskInstance getUserTaskInstance(long userTaskInstanceId) throws SActivityInstanceNotFoundException, SActivityReadException;

}
