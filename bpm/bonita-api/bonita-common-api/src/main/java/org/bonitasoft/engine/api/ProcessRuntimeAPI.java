/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
 ** 
 * @since 6.0
 */
package org.bonitasoft.engine.api;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.actor.ActorMember;
import org.bonitasoft.engine.bpm.comment.ArchivedComment;
import org.bonitasoft.engine.bpm.comment.Comment;
import org.bonitasoft.engine.bpm.connector.ArchivedConnectorInstance;
import org.bonitasoft.engine.bpm.connector.ConnectorExecutionException;
import org.bonitasoft.engine.bpm.connector.ConnectorInstance;
import org.bonitasoft.engine.bpm.connector.ConnectorNotFoundException;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityExecutionException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.EventCriterion;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeExecutionException;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.SendEventException;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessActivationException;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessExecutionException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.ProcessInstanceHierarchicalDeletionException;
import org.bonitasoft.engine.exception.RetrieveException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.engine.filter.UserFilter;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.InvalidSessionException;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Zhao Na
 * @author Frederic Bouquet
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public interface ProcessRuntimeAPI {

    /**
     * Search for the hidden tasks for the specified user. Only searches for pending tasks for the current user: if a hidden task has been assigned,
     * executed, ... it will not be retrieved.
     * 
     * @param userId
     *            the ID of the user for whom to retrieve the hidden tasks
     * @param searchOptions
     *            the search options parameters
     * @return the list of hidden tasks for the specified user
     * @throws SearchException
     *             in case a search problem occurs
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchPendingHiddenTasks(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * List all open process instances
     * 
     * @param searchOptions
     *            The criterion used to search process instance
     * @return A processInstance object
     * @throws SearchException
     *             The search failed
     * @since 6.0
     */
    SearchResult<ProcessInstance> searchOpenProcessInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * List all open process instances supervised by a user.
     * If userId is not bound to any user, returns an empty SearchResult
     * 
     * @param userId
     *            Identifier of the user
     * @param searchOptions
     *            The criterion used to search process instance
     * @return The list of process instances supervised by the user bound to userId
     * @throws SearchException
     *             The search failed
     * @since 6.0
     */
    SearchResult<ProcessInstance> searchOpenProcessInstancesSupervisedBy(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Get number of all process data instances by id
     * 
     * @param processInstanceId
     *            Identifier of the activity instance
     * @return The number of process data instances
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no ProcessInstance have an id corresponding to the parameter.
     * @since 6.0
     */
    long getNumberOfProcessDataInstances(long processInstanceId) throws ProcessInstanceNotFoundException;

    /**
     * Get number of all activity data instances by id
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return Number of all activity data instances
     * @throws ActivityInstanceNotFoundException
     *             Error thrown if no activity instance have an id corresponding to the parameter.
     * @since 6.0
     */
    long getNumberOfActivityDataInstances(long activityInstanceId) throws ActivityInstanceNotFoundException;

    /**
     * Get all process instances, the returned list is paginated
     * 
     * @param startIndex
     *            Index of the page to be returned. First page has index 0.
     * @param maxResults
     *            Number of documents per page. Maximum number of documents returned.
     * @param criterion
     *            the criterion for sort result.
     * @return The list of process instance
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    List<ProcessInstance> getProcessInstances(int startIndex, int maxResults, ProcessInstanceCriterion criterion);

    /**
     * Get all archived process instances, the returned list is paginated
     * 
     * @param startIndex
     *            Index of the page to be returned. First page has index 0.
     * @param maxResults
     *            Number of result per page. Maximum number of result returned.
     * @param criterion
     *            the criterion for sort result.
     * @return The list of archived process instances
     * @since 6.0
     */
    List<ArchivedProcessInstance> getArchivedProcessInstances(int startIndex, int maxResults, ProcessInstanceCriterion criterion);

    /**
     * Get a list of archived activity instances, the returned list is paginated
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @param startIndex
     *            Index of the page to be returned. First page has index 0.
     * @param maxResults
     *            Number of result per page. Maximum number of result returned.
     * @param criterion
     *            aging criterion to sort the results
     * @return this list of activities from the first definitive archive
     * @since 6.0
     */
    List<ArchivedActivityInstance> getArchivedActivityInstances(long processInstanceId, int startIndex, int maxResults, ActivityInstanceCriterion criterion);

    /**
     * Retrieve a list of open activities for a given process instance, the returned list is paginated
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @param startIndex
     *            Index of the page to be returned. First page has index 0.
     * @param maxResults
     *            Number of result per page. Maximum number of result returned.
     * @param criterion
     *            paging criterion to sort the results
     * @return The list of activity instances
     * @since 6.0
     */
    List<ActivityInstance> getOpenActivityInstances(long processInstanceId, int startIndex, int maxResults, ActivityInstanceCriterion criterion);

    /**
     * Get total number of open activity instances by its id
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @return Number of open activity instances
     * @since 6.0
     */
    int getNumberOfOpenedActivityInstances(long processInstanceId) throws ProcessInstanceNotFoundException;

    /**
     * Get the number of running process instances.
     * 
     * @return The total number of process instances, whatever the process definition.
     * @since 6.0
     */
    long getNumberOfProcessInstances();

    /**
     * Get the number of distinct archived process instances. "Archived" means in the definitive archive.
     * Only state Process Instances are retrieved.
     * 
     * @return The number of archived process instances
     * @since 6.0
     */
    long getNumberOfArchivedProcessInstances();

    /**
     * Delete the process instance having the identifier in parameter
     * 
     * @param processInstanceId
     *            identifier of the process instance to delete
     * @throws ProcessInstanceHierarchicalDeletionException
     *             if a process instance can't be deleted because of a parent that is still existing.
     * @since 6.0
     */
    void deleteProcessInstance(long processInstanceId) throws DeletionException;

    /**
     * Delete process instances by its process definition id
     * If process having the id is not found, it will thrown ProcessDefinitionNotFoundException
     * If process having the id is enabled, it will thrown DeletingEnabledProcessException
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @throws ProcessInstanceHierarchicalDeletionException
     *             if a process instance can't be deleted because of a parent that is still active
     * 
     * @since 6.0
     * 
     * @deprecated As of release 6.1, replaced by {@link #deleteProcessInstances(long, int, int, ProcessInstanceCriterion)} and
     *             {@link #deleteArchivedProcessInstances(long, int, int, ProcessInstanceCriterion)}
     */
    @Deprecated
    void deleteProcessInstances(long processDefinitionId) throws DeletionException;

    /**
     * Delete active process instances of process definition given as input parameter respecting the pagination parameters
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @param startIndex
     *            the index
     * @param maxResults
     *            the max number of elements to retrieve per page
     * @return the number of elements that have been deleted
     * @throws DeletionException
     *             if a process instance can't be deleted because of a parent that is still active
     * @since 6.1
     */
    long deleteProcessInstances(long processDefinitionId, int startIndex, int maxResults) throws DeletionException;

    /**
     * Delete archived process instances of process definition given as input parameter respecting the pagination parameters
     * If process having the id is not found, it will thrown ProcessDefinitionNotFoundException
     * If process having the id is enabled, it will thrown DeletingEnabledProcessException
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @param startIndex
     *            the index
     * @param maxResults
     *            the max number of elements to retrieve per page
     * @param criterion
     *            the sort criterion
     * @return the number of elements that have been deleted
     * @throws DeletionException
     *             if a process instance can't be deleted because of a parent that is still active
     * @since 6.1
     */
    long deleteArchivedProcessInstances(long processDefinitionId, int startIndex, int maxResults, ProcessInstanceCriterion criterion) throws DeletionException;

    /**
     * Start an instance of the process definition having processDefinitionId, and using the current session user
     * 
     * @param processDefinitionId
     *            Identifier of the process definition will be started
     * @return an instance of the process
     * @throws ProcessDefinitionNotFoundException
     *             No matching process definition found
     * @throws ProcessActivationException
     *             if the process is disabled
     * @throws ProcessExecutionException
     *             The process failed to execute
     * @since 6.0
     */
    ProcessInstance startProcess(long processDefinitionId) throws ProcessDefinitionNotFoundException, ProcessActivationException, ProcessExecutionException;

    /**
     * Start a process by process definition id
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @param operations
     *            The operations to start process
     * @param context
     *            The context in which operations are executed
     * @throws ExecutionException
     *             The process failed to start
     * @throws ProcessDefinitionNotFoundException
     *             The process definition corresponding to processDefinitionId is not found
     * @return a ProcessInstance object
     * @throws ProcessExecutionException
     * @throws ProcessDefinitionNotFoundException
     * @throws ProcessActivationException
     *             if the process is disabled
     * @since 6.0
     */
    ProcessInstance startProcess(long processDefinitionId, List<Operation> operations, Map<String, Serializable> context)
            throws ProcessDefinitionNotFoundException, ProcessActivationException, ProcessExecutionException, ProcessDefinitionNotFoundException;

    /**
     * Start an instance of the process definition on behalf of a given user
     * If userId equals 0, the logged-in user is declared as the starter of the process.
     * The user, who started the process on behalf of a given user, is declared as a starter delegate.
     * 
     * @param userId
     *            the identifier of the user for which you want to start the process
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @return The ProcessInstance Objects
     * @throws ProcessActivationException
     *             if the process is disabled
     * @since 6.0
     */
    ProcessInstance startProcess(long userId, long processDefinitionId) throws UserNotFoundException, ProcessDefinitionNotFoundException,
            ProcessActivationException, ProcessExecutionException;

    /**
     * Start an instance of the process definition on behalf of a given user, and set the initial values of the data with the given operations.
     * If userId equals 0, the logged-in user is declared as the starter of the process.
     * The user, who started the process on behalf of a given user, is declared as a starter delegate.
     * 
     * @param userId
     *            the identifier of the user for which you want to start the process
     * @param processDefinitionId
     *            Identifier of the process definition will be started
     * @param operations
     *            the operations to execute to set the initial values of the data
     * @param context
     *            TODO
     * @return an instance of the process
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             No matching process definition found
     * @throws ProcessActivationException
     *             a primary precondition of starting a process is the status of matching process definition should be enabled, if not,the exception occur.
     * @throws UserNotFoundException
     *             in case this userId is not found.
     * @throws ProcessExecutionException
     *             if a problem occurs when starting the process
     * @since 6.0
     */
    ProcessInstance startProcess(long userId, long processDefinitionId, List<Operation> operations, Map<String, Serializable> context)
            throws UserNotFoundException, ProcessDefinitionNotFoundException, ProcessActivationException, ProcessExecutionException;

    /**
     * Execute an flowNode that is is a non stable state
     * Will make the flow node go in the next stable state and then continue the execution of the process
     * 
     * @param flownodeInstanceId
     *            the identifier of the flow node to execute
     * @throws FlowNodeExecutionException
     *             if an execution exception occurs
     * @since 6.0
     */
    void executeFlowNode(long flownodeInstanceId) throws FlowNodeExecutionException;

    /**
     * Start an flow node that is is a non stable state on behalf of a given user
     * Will make the flow node go in the next stable state and then continue the execution of the process
     * If userId equals 0, the logged-in user is declared as the executer of the flow node.
     * The user, who executed the flow node on behalf of a given user, is declared as a executer delegate.
     * 
     * @param userId
     *            the identifier of the user for which you want to execute the flow node
     * @param flownodeInstanceId
     *            the identifier of the flow node to execute
     * @throws FlowNodeExecutionException
     *             if an execution exception occurs
     * @since 6.0.1
     */
    void executeFlowNode(long userId, long flownodeInstanceId) throws FlowNodeExecutionException;

    /**
     * Returns all activities (active and finished) of a process instance.
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @param startIndex
     *            the index
     * @param maxResults
     *            the number of results to get
     * @return the matching set of activity instances
     * @since 6.0
     */
    List<ActivityInstance> getActivities(long processInstanceId, int startIndex, int maxResults);

    /**
     * Get an instance of process with its processInstance id.
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @return the matching instance of process
     * @since 6.0
     */
    ProcessInstance getProcessInstance(long processInstanceId) throws ProcessInstanceNotFoundException;

    /**
     * Get an instance of activity using its activity instance id.
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return the matching instance of activity
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ActivityInstanceNotFoundException
     *             if the activity cannot be found
     * @throws RetrieveException
     *             can't retrieve an instance of activity
     * @since 6.0
     */
    ActivityInstance getActivityInstance(long activityInstanceId) throws ActivityInstanceNotFoundException;

    /**
     * Get an instance of flow node using its flow node instance id.
     * 
     * @param flowNodeInstanceId
     *            Identifier of the flow node instance
     * @return the matching instance of activity
     * @since 6.0
     */
    FlowNodeInstance getFlowNodeInstance(final long flowNodeInstanceId) throws FlowNodeInstanceNotFoundException;

    /**
     * Get an activity instance that already was archived.
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return the matching activity instance that already was archived
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ActivityInstanceNotFoundException
     *             if the archived activity cannot be found
     * @throws RetrieveException
     *             can't retrieve an instance of activity
     * @since 6.0
     */
    ArchivedActivityInstance getArchivedActivityInstance(long activityInstanceId) throws ActivityInstanceNotFoundException;

    /**
     * Retrieve a list of assigned human task instances related to given userId.
     * 
     * @param userId
     *            the user identifier
     * @param startIndex
     *            the index
     * @param maxResults
     *            the max number of elements to retrieve per page
     * @param criterion
     *            the sort criterion
     * @return the matching list
     * @throws InvalidSessionException
     *             occurs when the session is invalid.
     * @throws RetrieveException
     *             can't retrieve an instance of activity
     * @since 6.0
     */
    List<HumanTaskInstance> getAssignedHumanTaskInstances(long userId, int startIndex, int maxResults, ActivityInstanceCriterion criterion);

    /**
     * Retrieve a list of pending human task instances related to a given userId. A human task is pending for a given user if it is not yet assigned and if the
     * user is a candidate either through an {@link ActorMember} or through a {@link UserFilter}. In addition, hidden tasks for this user are not retrieved (see
     * {@link #hideTasks(long, Long...)})
     * 
     * @param userId
     *            the user identifier
     * @param startIndex
     *            the index
     * @param maxResults
     *            the max number of elements to retrieve per page
     * @param pagingCriterion
     *            the Criterion is for how to separate all items gotten to many pages.
     * @return the matching list
     * @throws InvalidSessionException
     *             occurs when the session is invalid.
     * @since 6.0
     */
    List<HumanTaskInstance> getPendingHumanTaskInstances(long userId, int startIndex, int maxResults, ActivityInstanceCriterion pagingCriterion);

    /**
     * Count total number of human task instances assigned that is related to the given userId.
     * 
     * @param userId
     *            Identifier of a user
     * @return a number of human task instances assigned
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws RetrieveException
     *             errors happen in while retrieving the instance of activity
     * @since 6.0
     */
    long getNumberOfAssignedHumanTaskInstances(long userId);

    /**
     * Return userIds and corresponding open task's id.
     * 
     * @param userIds
     *            a list of Identifiers for users
     * @return a map with userId as key and task id as value
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws RetrieveException
     *             can't retrieve an instance of activity
     * @since 6.0
     */
    Map<Long, Long> getNumberOfOpenTasks(List<Long> userIds);

    /**
     * Count number of pending human task instances.
     * 
     * @param userId
     *            Identifier of a user
     * @return a number of pending human task instances
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws RetrieveException
     *             errors happen in while retrieving the instance of activity
     * @since 6.0
     */
    long getNumberOfPendingHumanTaskInstances(long userId);

    /**
     * Retrieve a human task instance using corresponding activity instance id.
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return the matching instance of human task
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ActivityInstanceNotFoundException
     *             if the human task cannot be found
     * @throws RetrieveException
     *             errors happen in while retrieving the instance of activity
     * @since 6.0
     */
    HumanTaskInstance getHumanTaskInstance(long activityInstanceId) throws ActivityInstanceNotFoundException;

    /**
     * Get a event instance list according to all given conditions
     * 
     * @param rootContainerId
     *            id of the top-level container
     * @param startIndex
     *            the index
     * @param maxResults
     *            the number of results to get
     * @return the matching list of event instances
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    List<EventInstance> getEventInstances(long rootContainerId, int startIndex, int maxResults, EventCriterion sortingType);

    /**
     * Assign a task to a user with given user name.
     * 
     * @param userTaskId
     *            Identifier of user task
     * @param userId
     *            id of user
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    void assignUserTask(long userTaskId, long userId) throws UpdateException;

    /**
     * Returns all data of a process instance
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @param startIndex
     *            the index of the page of results to get
     * @param maxResults
     *            the number of results to get
     * @return the matching list of dataInstances
     * @since 6.0
     */
    List<DataInstance> getProcessDataInstances(long processInstanceId, int startIndex, int maxResults);

    /**
     * Get a DataInstance by dataName, processInstanceId.
     * 
     * @param dataName
     *            name of data
     * @param processInstanceId
     *            Identifier of the process instance
     * @return an instance of the data
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws DataNotFoundException
     *             Error thrown if can't get the corresponding data.
     * @since 6.0
     */
    DataInstance getProcessDataInstance(String dataName, long processInstanceId) throws DataNotFoundException;

    /**
     * Update data instance with given data name, value and process instance id that the data belongs to.
     * 
     * @param dataName
     *            name of data
     * @param processInstanceId
     *            Identifier of process instance the data belongs to
     * @param dataValue
     *            the new value of data to set
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws UpdateException
     *             if an data update problem occurs
     * @since 6.0
     */
    void updateProcessDataInstance(String dataName, long processInstanceId, Serializable dataValue) throws UpdateException;

    /**
     * Get data instances under the activity with given activity id.
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @param startIndex
     *            the index
     * @param maxResults
     *            the number of results to get
     * @return the matching list of dataInstances
     * @since 6.0
     */
    List<DataInstance> getActivityDataInstances(long activityInstanceId, int startIndex, int maxResults);

    /**
     * Get a data instance in activity with data name and activity instance id.
     * 
     * @param dataName
     *            name of data
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return an instance of data
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws DataNotFoundException
     *             Error thrown if can't get the corresponding data.
     * @since 6.0
     */
    DataInstance getActivityDataInstance(String dataName, long activityInstanceId) throws DataNotFoundException;

    /**
     * Update a data instance's value which in activity using data name and activity instance id.
     * 
     * @param dataName
     *            name of data
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws UpdateException
     * @since 6.0
     */
    void updateActivityDataInstance(String dataName, long activityInstanceId, Serializable dataValue) throws UpdateException;

    /**
     * Get the date when the activity with given activityInstanceId reaches the given state.
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @param state
     *            representing state of the activity existed
     * @return the matching date
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws RetrieveException
     *             errors happen in while retrieving the instance of activity
     * @since 6.0
     */
    Date getActivityReachedStateDate(long activityInstanceId, String state);

    /**
     * Update the given variables of an activity instance.
     * 
     * @param activityInstanceId
     *            the activity identifier
     * @param variables
     *            a map which contains several couple the variable name/value
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    void updateActivityInstanceVariables(long activityInstanceId, Map<String, Serializable> variables) throws UpdateException;

    /**
     * Update a variable data's expression in an activity instance which has left and right sides.
     * 
     * @param operations
     *            using this parameter to update expression's left and right side for different operation type.
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @param expressionContexts
     *            store all parameters that related to container which the data belongs to.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    void updateActivityInstanceVariables(List<Operation> operations, long activityInstanceId, Map<String, Serializable> expressionContexts)
            throws UpdateException;

    /**
     * Update the due date of a task
     * 
     * @param userTaskId
     *            identifier of the task to update
     * @param dueDate
     *            new due date for the task
     * @throws InvalidSessionException
     *             if the activity does not exists
     * @since 6.0
     */
    void updateDueDateOfTask(long userTaskId, Date dueDate) throws UpdateException;

    /**
     * Get an instance of userTask assigned in an instance of process.
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @param userId
     *            Identifier of a user
     * @return id of assigned userTask instance in process instance
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws UserNotFoundException
     *             Error thrown if no user have an id corresponding to the value of userId parameter
     * @throws RetrieveException
     *             errors happen in while retrieving the instance of activity
     * @since 6.0
     */
    long getOneAssignedUserTaskInstanceOfProcessInstance(long processInstanceId, long userId) throws ProcessInstanceNotFoundException, UserNotFoundException;

    /**
     * Get an instance of userTask assigned in a processDefinition.
     * 
     * @param processDefinitionId
     *            Identifier of the process definition
     * @param userId
     *            Identifier of a user
     * @return the matching userTask id
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws RetrieveException
     *             Error thrown if can't retrieve corresponding instance of process.
     * @since 6.0
     */
    long getOneAssignedUserTaskInstanceOfProcessDefinition(long processDefinitionId, long userId) throws ProcessDefinitionNotFoundException,
            UserNotFoundException;

    /**
     * Get an activity instance's state according to its activityInstanceId.
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return the matching activity's state
     * @throws ActivityInstanceNotFoundException
     *             if the activity cannot be found
     * @since 6.0
     */
    String getActivityInstanceState(long activityInstanceId) throws ActivityInstanceNotFoundException;

    /**
     * Check if the task can be executed
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @param userId
     *            Identifier of a user
     * @return a flag that indicates the task can be executed
     * @throws InvalidSessionException
     * @throws ActivityInstanceNotFoundException
     *             if the activity cannot be found
     * @throws UserNotFoundException
     *             Error thrown if no user have an id corresponding to the value of userId parameter
     * @throws RetrieveException
     *             errors happen in while retrieving the instance of activity
     * @since 6.0
     */
    boolean canExecuteTask(long activityInstanceId, long userId) throws ActivityInstanceNotFoundException, UserNotFoundException;

    /**
     * release a task (un_claim or un_assign). After the operation, the task should be in the pending task list
     * 
     * @param userTaskId
     *            Identifier of user task
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ActivityInstanceNotFoundException
     *             errors thrown if can't find corresponding activity
     * @since 6.0
     */
    void releaseUserTask(long userTaskId) throws ActivityInstanceNotFoundException, UpdateException;

    /**
     * @param processInstanceId
     *            the process instance ID to retrieve the list of archives from
     * @param startIndex
     *            the index of the page of results to get
     * @param maxResults
     *            the number of results to get
     * @return the list found
     * @throws InvalidSessionException
     *             if no current valid session is found
     * @throws RetrieveException
     *             if search failed for a read reason
     * @since 6.0
     */
    List<ArchivedProcessInstance> getArchivedProcessInstances(long processInstanceId, int startIndex, int maxResults);

    /**
     * Get the last archived instance of process.
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @return an instance of process archived
     * @throws InvalidSessionException
     *             if no current valid session is found
     * @throws RetrieveException
     *             if search failed for a read reason
     * @since 6.0
     */
    ArchivedProcessInstance getFinalArchivedProcessInstance(long processInstanceId) throws ArchivedProcessInstanceNotFoundException;

    /**
     * Set a activity's state with state id.
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @param stateId
     *            Identifier of state
     * @throws InvalidSessionException
     *             if no current valid session is found
     * @since 6.0
     */
    void setActivityStateById(long activityInstanceId, int stateId) throws UpdateException;

    /**
     * Set a activity's state with state name.
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @param state
     *            representing new state of the activity
     * @throws InvalidSessionException
     *             if no current valid session is found
     * @since 6.0
     */
    void setActivityStateByName(long activityInstanceId, String state) throws UpdateException;

    /**
     * Set a process instance's state.
     * 
     * @param processInstance
     *            an instance of process
     * @param state
     *            representing new state of the activity
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    void setProcessInstanceState(ProcessInstance processInstance, String state) throws UpdateException;

    /**
     * Set this userTask instance's priority.
     * 
     * @param userTaskInstanceId
     *            Identifier of user task instance
     * @param priority
     *            new priority of this task
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    void setTaskPriority(long userTaskInstanceId, TaskPriority priority) throws UpdateException;

    /**
     * Execute connector in given processDefinition.
     * 
     * @param connectorDefinitionId
     *            Identifier of connector definition
     * @param connectorDefinitionVersion
     *            version of the connector definition
     * @param connectorInputParameters
     *            all expressions related with the connector
     * @param inputValues
     *            all parameters values for expression need when evaluate the connector
     * @param processDefinitionId
     *            Identifier of the process definition
     * @return a map with connector parameter name and parameter value object
     * @throws ConnectorExecutionException
     *             TODO
     * @throws ConnectorNotFoundException
     *             TODO
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnProcessDefinition(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long processDefinitionId)
            throws ConnectorExecutionException, ConnectorNotFoundException;

    /**
     * Execute connector in given processDefinition with operations.
     * 
     * @param connectorDefinitionId
     *            Identifier of connector definition
     * @param connectorDefinitionVersion
     *            version of the connector definition
     * @param connectorInputParameters
     *            all expressions related with the connector
     * @param inputValues
     *            all parameters values for expression need when evaluate the connector
     * @param operations
     *            map of operations having each a special context (input values)
     * @param operationInputValues
     *            TODO
     * @param processDefinitionId
     *            Identifier of the process definition
     * @return a map with new values of elements set by the operations
     * @throws ConnectorExecutionException
     *             TODO
     * @throws ConnectorNotFoundException
     *             TODO
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnProcessDefinition(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, List<Operation> operations,
            Map<String, Serializable> operationInputValues, long processDefinitionId) throws ConnectorExecutionException, ConnectorNotFoundException;

    /**
     * Search task archived.
     * 
     * @param searchOptions
     *            search conditions and set sort,paging properties.
     * @return the number of human tasks found and the matching human tasks
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    SearchResult<ArchivedHumanTaskInstance> searchArchivedHumanTasks(SearchOptions searchOptions) throws SearchException;

    /**
     * Search human tasks administered by the given user.
     * 
     * @param managerUserId
     *            Identifier of a user
     * @param searchOptions
     *            search conditions and set sort,paging properties.
     * @return the number of human tasks found and the matching human tasks
     * @throws SearchException
     *             if there's wrong search condition, error happened.e.g,set a String value to long attribute.
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchAssignedTasksManagedBy(long managerUserId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search pending human tasks supervised by the given user.
     * 
     * @param userId
     *            Identifier of a user
     * @param searchOptions
     *            search conditions and set sort,paging properties.
     * @return the number of human tasks found and the matching human tasks
     * @throws SearchException
     *             if there's wrong search condition, error happened.e.g,set a String value to long attribute.
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchPendingTasksSupervisedBy(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search pending human tasks for the given user.
     * 
     * @param userId
     *            Identifier of a user
     * @param searchOptions
     *            search conditions and set sort,paging properties.
     * @return the number of human tasks found and the matching human tasks
     * @throws SearchException
     *             if there's wrong search condition, error happened.e.g,set a String value to long attribute.
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchPendingTasksForUser(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search pending human tasks administered by the given user.
     * 
     * @param managerUserId
     *            same to user id
     * @param searchOptions
     *            search conditions and set sort,paging properties.
     * @return the number of human tasks found and the matching human tasks
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if there's wrong search condition, error happened.e.g,set a String value to long attribute.
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchPendingTasksManagedBy(long managerUserId, SearchOptions searchOptions) throws SearchException;

    /**
     * Get the number of both assigned and pending overdue tasks for each user
     * 
     * @param userIds
     *            a list of user identifiers
     * @return a map with user id and relative number of overdue tasks
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Map<Long, Long> getNumberOfOverdueOpenTasks(List<Long> userIds);

    /**
     * Cancel an instance of process with the given processInstanceId.
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ProcessInstanceNotFoundException
     *             if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws RetrieveException
     *             Error happened when can't retrieve the instance of process.
     * @since 6.0
     */
    void cancelProcessInstance(long processInstanceId) throws ProcessInstanceNotFoundException, UpdateException;

    /**
     * set state of activity to its previous state and then execute.
     * precondition: the activity is in state FAILED
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ActivityInstanceNotFoundException
     *             errors thrown if can't find corresponding activity
     * @throws ActivityExecutionException
     *             errors happened when one of the two step that re-set state of the task and execute it again failed.
     * @since 6.0
     */
    void retryTask(long activityInstanceId) throws ActivityInstanceNotFoundException, ActivityExecutionException;

    /**
     * Hides a list of tasks from a specified user. A task "hidden" is then not retrieved anymore by method "searchPendingTasksForUser".
     * As soon as a task is claimed by / assigned to a user, it is not "hidden" anymore for anyone.
     * 
     * @param userId
     *            the ID of the user to hide the tasks for.
     * @param activityInstanceId
     *            the ID of the task to hide
     * @throws InvalidSessionException
     *             in there is no current valid session.
     * @throws UpdateException
     *             in case a problem occurs when hiding one of the tasks.
     * @see #unhideTasks(long, Long...)
     * @since 6.0
     */
    void hideTasks(long userId, Long... activityInstanceId) throws UpdateException;

    /**
     * Un-hides a list of tasks from a specified user. Un-hiding a task makes it available for a user if the task is pending for that user.
     * 
     * @param userId
     *            the ID of the user to un-hide the tasks for.
     * @param activityInstanceId
     *            the ID of the task to un-hide
     * @throws InvalidSessionException
     *             in there is no current valid session.
     * @throws UpdateException
     *             in case a problem occurs when un-hiding one of the tasks.
     * @see #hideTasks(long, Long...)
     * @since 6.0
     */
    void unhideTasks(long userId, Long... activityInstanceId) throws UpdateException;

    /**
     * Evaluate the expression in the context of the process having the processDefinitionId.
     * Some context values can also be provided
     * 
     * @param expression
     *            the expression to evaluate
     * @param context
     *            context values that is provided to evaluate the expression
     * @param processDefinitionId
     *            the id of the process of which the context will be used to evaluate the expression
     * @return
     *         the result of the evaluation
     * @throws InvalidSessionException
     * @throws ExpressionEvaluationException
     * @since 6.0
     */
    Serializable evaluateExpressionOnProcessDefinition(Expression expression, Map<String, Serializable> context, long processDefinitionId)
            throws ExpressionEvaluationException;

    /**
     * return true if the task have the identifier userTaskId is hidden for the logged user
     * 
     * @param userTaskId
     *            id of the task to check
     * @param userId
     *            id of the user
     * @return
     *         true if it is hidden for the logged user
     * @throws InvalidSessionException
     * @throws RetrieveException
     * @since 6.0
     */
    boolean isTaskHidden(long userTaskId, long userId);

    /**
     * Query the engine to get the number of comments depending on the search options
     * 
     * @param searchOptions
     *            Search conditions and set sort, paging properties
     * @return
     *         The number of comments matching the search options
     * @throws SearchException
     *             if search could not be fullfilled correctly
     * @since 6.0
     */
    long countComments(SearchOptions searchOptions) throws SearchException;

    /**
     * Query the engine to get the number of attachments depending on the search options
     * 
     * @param searchOptions
     *            Search conditions and set sorts, paging properties
     * @return
     *         The number of attachments
     * @throws SearchException
     *             if search could not be fullfilled correctly
     * @since 6.0
     */

    long countAttachments(SearchOptions searchOptions) throws SearchException;

    /**
     * Send a BPMN signal event. Invoking this method act as executing a Throw Signal Event
     * 
     * @param signalName
     *            the signal name
     * @throws InvalidSessionException
     * @throws SendEventException
     *             if an Exception occurs while sending signal
     * @since 6.0
     */
    void sendSignal(String signalName) throws SendEventException;

    /**
     * Send a BPMN message event. Invoking this method act as executing a Throw Message Event
     * 
     * @param messageName
     *            the message name
     * @param targetProcess
     *            expression representing the target process name
     * @param targetFlowNode
     *            expression representing the target flow node name
     * @param messageContent
     *            a map representing the message data. The key is used for the data name and the value is used for the data value
     * @throws InvalidSessionException
     * @throws SendEventException
     *             if an Exception occurs while sending message
     * @since 6.0
     */
    void sendMessage(String messageName, Expression targetProcess, Expression targetFlowNode, Map<Expression, Expression> messageContent)
            throws SendEventException;

    /**
     * Send a BPMN message event. Invoking this method act as executing a Throw Message Event
     * 
     * @param messageName
     *            the message name
     * @param targetProcess
     *            expression representing the target process name
     * @param targetFlowNode
     *            expression representing the target flow node name
     * @param messageContent
     *            The message data. The key is used for the data name and the value is used for the data value
     * @param correlations
     *            the message correlations (five maximum).
     * @throws InvalidSessionException
     * @throws SendEventException
     *             if there are too many correlations (more than 5) or an Exception occurs while sending message
     * @since 6.0
     */
    void sendMessage(String messageName, Expression targetProcess, Expression targetFlowNode, Map<Expression, Expression> messageContent,
            Map<Expression, Expression> correlations) throws SendEventException;

    /**
     * Retrieves an <code>ArchivedProcessInstance</code> from its id.
     * 
     * @param archivedProcessInstanceId
     *            the ID of the <code>ArchivedProcessInstance</code> to retrieve.
     * @return the found <code>ArchivedProcessInstance</code> instance.
     * @throws ArchivedProcessInstanceNotFoundException
     *             if the <code>ArchivedProcessInstance</code> was not found.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws RetrieveException
     *             if a problem occurs while trying to retrieve the <code>ArchivedFlowNodeInstance</code>.
     * @since 6.0
     */
    ArchivedProcessInstance getArchivedProcessInstance(long archivedProcessInstanceId) throws ArchivedProcessInstanceNotFoundException;

    /**
     * Retrieves an <code>ArchivedFlowNodeInstance</code> from its id.
     * 
     * @param archivedFlowNodeInstanceId
     *            the ID of the <code>ArchivedFlowNodeInstance</code> to retrieve.
     * @return the found <code>ArchivedFlowNodeInstance</code> instance.
     * @throws ArchivedFlowNodeInstanceNotFoundException
     *             if the <code>ArchivedFlowNodeInstance</code> was not found.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws RetrieveException
     *             if a problem occurs while trying to retrieve the <code>ArchivedFlowNodeInstance</code>.
     * @since 6.0
     */
    ArchivedFlowNodeInstance getArchivedFlowNodeInstance(long archivedFlowNodeInstanceId) throws ArchivedFlowNodeInstanceNotFoundException;

    /**
     * Retrieves an <code>ArchivedComment</code> from its id.
     * 
     * @param archivedCommentId
     *            the ID of the <code>ArchivedComment</code> to retrieve.
     * @return the found <code>ArchivedComment</code> instance.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws RetrieveException
     *             if a problem occurs while trying to retrieve the <code>ArchivedComment</code>
     * @throws NotFoundException
     *             if not <code>ArchivedComment</code> was found with id archivedCommentId
     * @since 6.0
     */
    ArchivedComment getArchivedComment(long archivedCommentId) throws NotFoundException;

    /**
     * Searches for connector instances.
     * 
     * @param searchOptions
     *            the search options parameters
     * @return the {@link SearchResult} containing the <code>ConnectorInstance</code>s matching the search options
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if search could not be fullfilled correctly
     * @since 6.0
     */
    SearchResult<ConnectorInstance> searchConnectorInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * Searches for archived connector instances.
     * 
     * @param searchOptions
     *            the search options parameters
     * @return the {@link SearchResult} containing the <code>ArchivedConnectorInstance</code>s matching the search options
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if search could not be fullfilled correctly
     * @since 6.0
     */
    SearchResult<ArchivedConnectorInstance> searchArchivedConnectorInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * Get all tasks having given name and belonging to the process instance having given id
     * 
     * @param processInstanceId
     *            the ID of the process instance to look the <code>HumanTaskInstance</code>s for.
     * @param taskName
     *            the name of the tasks to search
     * @param startIndex
     *            the result start index
     * @param maxResults
     *            the max number of results to retrieve
     * @return The list of matching <code>HumanTaskInstance</code>s
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    List<HumanTaskInstance> getHumanTaskInstances(long processInstanceId, String taskName, int startIndex, int maxResults);

    /**
     * Returns the last created HumanTask, in the given process instance, named taskName
     * 
     * @param processInstanceId
     *            the Identifier of the process instance for which to search the <code>HumanTaskInstance</code>
     * @param taskName
     *            the task
     * @return The HumanTaskInstance, in its last state, in the given process instance, named taskName
     * @throws NotFoundException
     *             if no current task with provided name could be found
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    HumanTaskInstance getLastStateHumanTaskInstance(long processInstanceId, String taskName) throws NotFoundException;

    /**
     * Searches for archived activity instances (only terminal states, intermediate states are not considered)
     * 
     * @param searchOptions
     *            The criterion used to search for archived activity instances
     * @return A {@link SearchResult} containing the search result
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if has exceptions during the process to search archived activity instance
     * @since 6.0
     */
    SearchResult<ArchivedActivityInstance> searchArchivedActivities(SearchOptions searchOptions) throws SearchException;

    /**
     * Searches for activity instances
     * 
     * @param searchOptions
     *            The criterion used to activity instance
     * @return A {@link SearchResult} containing the search result
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if search could not be fullfilled correctly
     * @since 6.0
     */
    SearchResult<ActivityInstance> searchActivities(SearchOptions searchOptions) throws SearchException;

    /**
     * Searches for flow node instances (activities, gateways and events)
     * 
     * @param searchOptions
     *            the search options parameters
     * @return A {@link SearchResult} containing the search result
     * @throws InvalidSessionException
     *             If API Session is invalid, e.g session has expired.
     * @throws SearchException
     *             if an exception occurs while performing the search
     * @since 6.0
     */
    SearchResult<FlowNodeInstance> searchFlowNodeInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * Searches for archived flow node instances (activities, gateways and events)
     * 
     * @param searchOptions
     *            the search options parameters
     * @return A {@link SearchResult} containing the found archived flown node instances.
     * @throws InvalidSessionException
     *             If API Session is invalid, e.g session has expired.
     * @throws SearchException
     *             if an exception occurs while performing the search
     * @see {@link ArchivedFlowNodeInstance}
     * @since 6.0
     */
    SearchResult<ArchivedFlowNodeInstance> searchArchivedFlowNodeInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * Searches, for a specific user, for all tasks pending for that user, or already assigned to that user. (=Available)
     * Hidden tasks are not retrieved.
     * 
     * @param userId
     *            the user for whom to retrieve the tasks
     * @param searchOptions
     *            the search options parameters
     * @return the list of tasks matching the provided criteria
     * @throws InvalidSessionException
     *             if the current session is invalid
     * @throws SearchException
     *             in case a search problem occurs
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchMyAvailableHumanTasks(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Searches for comments related to the specified Process Instance.
     * 
     * @param searchOptions
     *            The options used to search for Comments
     * @return The matching comments
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             in case a search problem occurs
     * @since 6.0
     */
    SearchResult<Comment> searchComments(SearchOptions searchOptions) throws SearchException;

    /**
     * Add a comment on a process instance
     * 
     * @param processInstanceId
     *            Identifier of the processInstance
     * @param comment
     *            The content of comment
     * @return The newly created Comment
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Comment addComment(long processInstanceId, String comment);

    /**
     * Get all comments by its process instance id
     * 
     * @param processInstanceId
     *            Identifier of the processInstance
     * @return The list of comment
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    List<Comment> getComments(long processInstanceId);

    /**
     * Lists all comments managed by user.
     * A comment is said to be managed by a user A if :
     * - the author of the comment is a subordinate of user A ( or A the author's manager ).
     * - the comment belongs to a process started by a subordinate of user A.
     * - the comment belongs to a process where at least one human task is assigned to a subordinate of user A.
     * 
     * @param managerUserId
     *            Identifier of the user
     * @param searchOptions
     *            The criterion used to search Comment
     * @return comments managed by a user, and matching the provided search options
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if search could not be fullfilled correctly
     * @since 6.0
     */
    SearchResult<Comment> searchCommentsManagedBy(long managerUserId, SearchOptions searchOptions) throws SearchException;

    /**
     * Searches the comments on process instances that the user can access
     * 
     * @param userId
     *            Identifier of the user
     * @param searchOptions
     *            The criterion used to search Comment
     * @return the comments on process instances that the user can access
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if search could not be fullfilled correctly
     * @since 6.0
     */
    SearchResult<Comment> searchCommentsInvolvingUser(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Get the children instances (sub process or call activity) of a process instance. The returned list is paginated.
     * 
     * @param processInstanceId
     *            Identifier of the processDefinition
     * @param startIndex
     *            Index of the page to be returned. First page has index 0.
     * @param maxResults
     *            Number of result per page. Maximum number of result returned.
     * @param criterion
     *            The criterion used to sort the result
     * @return The list of children instance id
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    List<Long> getChildrenInstanceIdsOfProcessInstance(long processInstanceId, int startIndex, int maxResults, ProcessInstanceCriterion criterion);

    /**
     * Whether or not a user is involved in an instance of process (has tasks pending?)
     * 
     * @param userId
     *            Identifier of the user
     * @param processInstanceId
     *            Identifier of the processDefinition
     * @return An boolean type to get if a process instance is involved with given user
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ProcessInstanceNotFoundException
     *             if no processInstance have an id corresponding to the parameter.
     * @throws UserNotFoundException
     *             if no user have an id corresponding to the parameter.
     * @since 6.0
     */
    boolean isInvolvedInProcessInstance(long userId, long processInstanceId) throws ProcessInstanceNotFoundException, UserNotFoundException;

    /**
     * Get process instance id from its activity instance id
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return the found process instance id
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ProcessInstanceNotFoundException
     *             if no processInstance have an id corresponding to the parameter.
     * @since 6.0
     */
    long getProcessInstanceIdFromActivityInstanceId(long activityInstanceId) throws ProcessInstanceNotFoundException;

    /**
     * Get process definition id from its process instance id
     * 
     * @param processInstanceId
     *            Identifier of the activity instance
     * @return the found process definition id
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             if no ProcessDefinition have an id corresponding to the parameter.
     * @since 6.0
     */
    long getProcessDefinitionIdFromProcessInstanceId(long processInstanceId) throws ProcessDefinitionNotFoundException;

    /**
     * Get process definition id from its activity instance id
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return the found process definition id
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             if no ProcessDefinition have an id corresponding to the parameter.
     * @since 6.0
     */
    long getProcessDefinitionIdFromActivityInstanceId(long activityInstanceId) throws ProcessDefinitionNotFoundException;

    /**
     * Searches for archived comments
     * 
     * @param searchOptions
     *            The ArchivedComment search options
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @return the <code>ArchivedComment</code> matching the search options
     * @throws SearchException
     *             if search could not be fullfilled correctly
     * @since 6.0
     */
    SearchResult<ArchivedComment> searchArchivedComments(SearchOptions searchOptions) throws SearchException;

    /**
     * Searches for archived human tasks performed by users managed by a specific user
     * 
     * @param managerUserId
     *            Identifier of the user manager
     * @param searchOptions
     *            The ArchivedHumanTaskInstance search options
     * @return archived humanTask instances managed by a user, and matching the search options
     * @throws SearchException
     *             if search could not be fullfilled correctly
     * @since 6.0
     */
    SearchResult<ArchivedHumanTaskInstance> searchArchivedHumanTasksManagedBy(long managerUserId, SearchOptions searchOptions) throws SearchException;

    /**
     * Searches for open process instances that the user can access
     * 
     * @param userId
     *            Identifier of the user
     * @param searchOptions
     *            The criterion used to search ProcessInstance
     * @return the found <code>ProcessInstance</code>s matching the search options
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if search could not be fullfilled correctly
     * @since 6.0
     */
    SearchResult<ProcessInstance> searchOpenProcessInstancesInvolvingUser(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Searches for open process instances that all subordinates of the specific manager can access
     * 
     * @param managerUserId
     *            the ID of the user manager
     * @param searchOptions
     *            the ProcessInstance search options (pagination, filter, order sort)
     * @return the found <code>ProcessInstance</code>s matching the search options
     * @throws SearchException
     *             if search could not be fullfilled correctly
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    SearchResult<ProcessInstance> searchOpenProcessInstancesInvolvingUsersManagedBy(long managerUserId, SearchOptions searchOptions) throws SearchException;

    /**
     * Searches for archived process instances
     * 
     * @param searchOptions
     *            the ArchivedProcessInstance search options (pagination, filter, order sort)
     * @return the matching archived process instances
     * @throws SearchException
     *             if search could not be fullfilled correctly
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    SearchResult<ArchivedProcessInstance> searchArchivedProcessInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * Searches for archived process instances supervised by a user
     * 
     * @param userId
     *            Identifier of the user
     * @param searchOptions
     *            the ArchivedProcessInstance search options (pagination, filter, order sort)
     * @return matching archived process instances supervised by a user
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if search could not be fullfilled correctly
     * @since 6.0
     */
    SearchResult<ArchivedProcessInstance> searchArchivedProcessInstancesSupervisedBy(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Searches for the archived process instances that the user can access
     * 
     * @param userId
     *            Identifier of the user
     * @param searchOptions
     *            the ArchivedProcessInstance search options (pagination, filter, order sort)
     * @return the archived process instances that the user can access
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if search could not be fullfilled correctly
     * @since 6.0
     */
    SearchResult<ArchivedProcessInstance> searchArchivedProcessInstancesInvolvingUser(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Searches for humanTask instances
     * 
     * @param searchOptions
     *            the HumanTaskInstance search options (pagination, filter, order sort)
     * @return the resulting <code>HumanTaskInstance</code>s matching the provided search options
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if search could not be fullfilled correctly
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchHumanTaskInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * Searches tasks assigned to users supervised by a specific user.
     * 
     * @param supervisorId
     *            the id of the process Supervisor user
     * @param searchOptions
     *            the HumanTaskInstance search options (pagination, filter, order sort)
     * @return the resulting <code>HumanTaskInstance</code>s matching the provided search options
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if search could not be fullfilled correctly
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchAssignedTasksSupervisedBy(long supervisorId, SearchOptions searchOptions) throws SearchException;

    /**
     * Searches archived tasks assigned to users supervised by a specific user.
     * 
     * @param supervisorId
     *            the id of the process Supervisor user
     * @param searchOptions
     *            the ArchivedHumanTaskInstance search options (pagination, filter, order sort)
     * @return the resulting <code>ArchivedHumanTaskInstance</code>s matching the provided search options
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if search could not be fullfilled correctly
     * @since 6.0
     */
    SearchResult<ArchivedHumanTaskInstance> searchArchivedHumanTasksSupervisedBy(long supervisorId, SearchOptions searchOptions) throws SearchException;

    /**
     * Evaluate expressions with values valid at process instantiation scope.
     * 
     * @param processInstanceId
     *            The identifier of process instance
     * @param expressions
     *            Map of expressions to evaluate
     * @return the result of the expression execution. Content of the resulting map depends on the incoming expression map. The returned key is the name of the
     *         expression (or its content if name is empty), the returned value is the evaluated expression result.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ExpressionEvaluationException
     *             occurs when an exception is thrown during expression evaluation
     * @since 6.0
     */
    Map<String, Serializable> evaluateExpressionsAtProcessInstanciation(long processInstanceId, Map<Expression, Map<String, Serializable>> expressions)
            throws ExpressionEvaluationException;

    /**
     * Evaluate expressions with values valid on a completed process instance scope.
     * 
     * @param processInstanceId
     *            The identifier of process instance
     * @param expressions
     *            Map of expressions to evaluate
     * @return the result of the expression execution. Content of the resulting map depends on the incoming expression map. The returned key is the name of the
     *         expression (or its content if name is empty), the returned value is the evaluated expression result.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ExpressionEvaluationException
     *             occurs when an exception is thrown during expression evaluation
     * @since 6.0
     */
    Map<String, Serializable> evaluateExpressionOnCompletedProcessInstance(long processInstanceId, Map<Expression, Map<String, Serializable>> expressions)
            throws ExpressionEvaluationException;

    /**
     * Evaluate expressions with values valid on a process instance scope.
     * 
     * @param processInstanceId
     *            The identifier of process instance
     * @param expressions
     *            Map of expressions to evaluate
     * @return the result of the expression execution. Content of the resulting map depends on the incoming expression map. The returned key is the name of the
     *         expression (or its content if name is empty), the returned value is the evaluated expression result.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ExpressionEvaluationException
     *             occurs when an exception is thrown during expression evaluation
     * @since 6.0
     */
    Map<String, Serializable> evaluateExpressionsOnProcessInstance(long processInstanceId, Map<Expression, Map<String, Serializable>> expressions)
            throws ExpressionEvaluationException;

    /**
     * Evaluate expressions with values valid on a process definition scope.
     * 
     * @param processDefinitionId
     *            The identifier of process definition
     * @param expressions
     *            Map of expressions to evaluate
     * @return the result of the expression execution. Content of the resulting map depends on the incoming expression map. The returned key is the name of the
     *         expression (or its content if name is empty), the returned value is the evaluated expression result.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ExpressionEvaluationException
     *             occurs when an exception is thrown during expression evaluation
     * @since 6.0
     */
    Map<String, Serializable> evaluateExpressionsOnProcessDefinition(long processDefinitionId, Map<Expression, Map<String, Serializable>> expressions)
            throws ExpressionEvaluationException;

    /**
     * Evaluate expressions with values valid on an activity instance scope.
     * 
     * @param activityInstanceId
     *            The identifier of activity instance
     * @param expressions
     *            Map of expressions to evaluate
     * @return the result of the expression execution. Content of the resulting map depends on the incoming expression map. The returned key is the name of the
     *         expression (or its content if name is empty), the returned value is the evaluated expression result.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ExpressionEvaluationException
     *             occurs when an exception is thrown during expression evaluation
     * @since 6.0
     */
    Map<String, Serializable> evaluateExpressionsOnActivityInstance(long activityInstanceId, Map<Expression, Map<String, Serializable>> expressions)
            throws ExpressionEvaluationException;

    /**
     * Evaluate expressions with values valid on a completed activity instance scope.
     * 
     * @param activityInstanceId
     *            The identifier of activity instance
     * @param expressions
     *            Map of expressions to evaluate
     * @return the result of the expression execution. Content of the resulting map depends on the incoming expression map. The returned key is the name of the
     *         expression (or its content if name is empty), the returned value is the evaluated expression result.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ExpressionEvaluationException
     *             occurs when an exception is thrown during expression evaluation
     * @since 6.0
     */
    Map<String, Serializable> evaluateExpressionsOnCompletedActivityInstance(long activityInstanceId, Map<Expression, Map<String, Serializable>> expressions)
            throws ExpressionEvaluationException;

}
