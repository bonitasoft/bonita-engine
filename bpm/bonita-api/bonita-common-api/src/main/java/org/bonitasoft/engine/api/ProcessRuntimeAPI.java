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
import org.bonitasoft.engine.exception.CreationException;
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
     * Search for pending hidden tasks that are available to the specified user.
     * Only searches for pending tasks for the current user: if a hidden task has been assigned
     * or executed, it will not be retrieved.
     * 
     * @param userId
     *            the ID of the user for whom to list the hidden tasks.
     * @param searchOptions
     *            the search criterion.
     * @return the list of hidden tasks for the specified user.
     * @throws SearchException
     *             if an exception occurs when getting the list of tasks.
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchPendingHiddenTasks(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * List all open process instances.
     * 
     * @param searchOptions
     *            the search criterion.
     * @return a processInstance object.
     * @throws SearchException
     *             if an exception occurs when getting the list of tasks.
     * @since 6.0
     */
    SearchResult<ProcessInstance> searchOpenProcessInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * List all open process instances supervised by a user.
     * If the specified userId does not correspond to a user, an empty SearchResult is returned.
     * 
     * @param userId
     *            the identifier of the user.
     * @param searchOptions
     *            the search criterion.
     * @return the list of process instances supervised by the specified user.
     * @throws SearchException
     *             if an exception occurs when getting the list of process instances.
     * @since 6.0
     */
    SearchResult<ProcessInstance> searchOpenProcessInstancesSupervisedBy(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Get the number of process data instances by process id.
     * 
     * @param processInstanceId
     *            the identifier of the process instance.
     * @return the number of process data instances.
     * @throws ProcessInstanceNotFoundException
     *             if the specified ProcessInstance does not refer to a process instance.
     * @since 6.0
     */
    long getNumberOfProcessDataInstances(long processInstanceId) throws ProcessInstanceNotFoundException;

    /**
     * Get the number of activity data instances by activity id.
     * 
     * @param activityInstanceId
     *            identifier of the activity instance.
     * @return the number of activity data instances.
     * @throws ActivityInstanceNotFoundException
     *             if the specified activity instance does not refer to an activity instance.
     * @since 6.0
     */
    long getNumberOfActivityDataInstances(long activityInstanceId) throws ActivityInstanceNotFoundException;

    /**
     * Get a paged list of all process instances.
     * 
     * @param startIndex
     *            the index of the first result (starting from 0).
     * @param maxResults
     *            the maximum number of results per page.
     * @param criterion
     *            the sort criterion.
     * @return the list of process instances.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    List<ProcessInstance> getProcessInstances(int startIndex, int maxResults, ProcessInstanceCriterion criterion);

    /**
     * Get a paged list of archived process instances.
     * 
     * @param startIndex
     *            the index of the first result (starting from 0).
     * @param maxResults
     *            the maximum number of results per page.
     * @param criterion
     *            the sort criterion.
     * @return the list of archived process instances.
     * @since 6.0
     */
    List<ArchivedProcessInstance> getArchivedProcessInstances(int startIndex, int maxResults, ProcessInstanceCriterion criterion);

    /**
     * Get a paged list of archived activity instances for a process instance.
     * 
     * @param processInstanceId
     *            identifier of the process instance.
     * @param startIndex
     *            the index of the first result (starting from 0).
     * @param maxResults
     *            the maximum number of result per page.
     * @param criterion
     *            the sort criterion.
     * @return the list of archived activity instances.
     * @since 6.0
     */
    List<ArchivedActivityInstance> getArchivedActivityInstances(long processInstanceId, int startIndex, int maxResults, ActivityInstanceCriterion criterion);

    /**
     * Retrieve a paged list of open activities for a given process instance.
     * 
     * @param processInstanceId
     *            the identifier of the process instance.
     * @param startIndex
     *            the index of the first result (starting from 0).
     * @param maxResults
     *            the maximum number of results per page.
     * @param criterion
     *            the sort criterion.
     * @return the list of activity instances.
     * @since 6.0
     */
    List<ActivityInstance> getOpenActivityInstances(long processInstanceId, int startIndex, int maxResults, ActivityInstanceCriterion criterion);

    /**
     * Get the total number of open activity instances by process instance id.
     * 
     * @param processInstanceId
     *            the identifier of the process instance.
     * @return the number of open activity instances.
     *         #throws ProcessInstanceNotFoundException
     *         if the specified process instacne id does not refer to a process instance.
     * @since 6.0
     */
    int getNumberOfOpenedActivityInstances(long processInstanceId) throws ProcessInstanceNotFoundException;

    /**
     * Get the number of open process instances.
     * An open process instance is a process instance that has not been archived.
     * 
     * @return the total number of open process instances.
     * @since 6.0
     */
    long getNumberOfProcessInstances();

    /**
     * Get the number of archived process instances.
     * Process instances in state COMPLETED are counted.
     * 
     * @return the number of archived process instances.
     * @since 6.0
     */
    long getNumberOfArchivedProcessInstances();

    /**
     * Delete the specified process instance.
     * 
     * @param processInstanceId
     *            the identifier of the process instance to delete.
     * @throws ProcessInstanceHierarchicalDeletionException
     *             if a process instance cannot be deleted because of a parent that is still active.
     * @throws DeletionException
     *             if an error occurs during deletion.
     * @since 6.0
     */
    void deleteProcessInstance(long processInstanceId) throws DeletionException;

    /**
     * Delete all instances of a specified process definition.
     * If the process definition id does not match anything, no exception is thrown, but nothing is deleted.
     * 
     * @param processDefinitionId
     *            the identifier of the processDefinition.
     * @throws ProcessInstanceHierarchicalDeletionException
     *             if a process instance cannot be deleted because of a parent that still exists.
     * @throws DeletionException
     *             if other deletion problem occurs.
     * @since 6.0
     * @deprecated As of release 6.1, replaced by {@link #deleteProcessInstances(long, int, int, ProcessInstanceCriterion)} and
     *             {@link #deleteArchivedProcessInstances(long, int, int)}
     */
    @Deprecated
    void deleteProcessInstances(long processDefinitionId) throws DeletionException;

    /**
     * Delete active process instances, and their elements, of process definition given as input parameter respecting the pagination parameters
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
    long deleteArchivedProcessInstances(long processDefinitionId, int startIndex, int maxResults) throws DeletionException;

    /**
     * Start an instance of the process with the specified process definition, using the current session user.
     * 
     * @param processDefinitionId
     *            the identifier of the process definition for which an instance will be started.
     * @return an instance of the process.
     * @throws ProcessDefinitionNotFoundException
     *             if no matching process definition is found.
     * @throws ProcessActivationException
     *             if an exception occurs during activation.
     * @throws ProcessExecutionException
     *             if a problem occurs when starting the process.
     * @since 6.0
     */
    ProcessInstance startProcess(long processDefinitionId) throws ProcessDefinitionNotFoundException, ProcessActivationException, ProcessExecutionException;

    /**
     * Instantiates a process.
     * <b>
     * The process variables will be initialized by the initialVariables.
     * 
     * @param processDefinitionId
     *            the identifier of the processDefinition
     * @param initialVariables
     *            the couples of initial variable/value
     * @return a ProcessInstance object
     * @throws ProcessDefinitionNotFoundException
     *             if the identifier of process definition does not refer to any existing process definition
     * @throws ProcessExecutionException
     *             if the process fails to start
     * @throws ProcessActivationException
     *             if the process is disable
     * @since 6.1
     */
    ProcessInstance startProcess(long processDefinitionId, Map<String, Serializable> initialVariables) throws ProcessDefinitionNotFoundException,
            ProcessActivationException, ProcessExecutionException;

    /**
     * Start an instance of the process with the specified process definition id, and set the initial values of the data with the given operations.
     * 
     * @param processDefinitionId
     *            the identifier of the process definition for which an instance will be started.
     * @param operations
     *            the operations to execute to set the initial values of the data.
     * @param context
     *            the context in which operations are executed.
     * @return an instance of the process.
     * @throws ProcessDefinitionNotFoundException
     *             if no matching process definition is found.
     * @throws ProcessActivationException
     *             if an exception occurs during activation.
     * @throws ProcessExecutionException
     *             if a problem occurs when starting the process.
     * @since 6.0
     */
    ProcessInstance startProcess(long processDefinitionId, List<Operation> operations, Map<String, Serializable> context)
            throws ProcessDefinitionNotFoundException, ProcessActivationException, ProcessExecutionException;

    /**
     * Start an instance of the process with the specified process definition id on behalf of a given user.
     * 
     * @param userId
     *            the user id of the user.
     * @param processDefinitionId
     *            the identifier of the process definition for which an instance will be started.
     * @return an instance of the process.
     * @throws ProcessDefinitionNotFoundException
     *             if no matching process definition is found.
     * @throws ProcessActivationException
     *             if a problem occurs when starting the process.
     * @since 6.0
     */
    ProcessInstance startProcess(long userId, long processDefinitionId) throws UserNotFoundException, ProcessDefinitionNotFoundException,
            ProcessActivationException, ProcessExecutionException;

    /**
     * Start an instance of the process with the specified process definition id on behalf of a given user, and set the initial values of the data with the
     * given operations.
     * 
     * @param userId
     *            the id of the user.
     * @param processDefinitionId
     *            the identifier of the process definition for which an instance will be started.
     * @param operations
     *            the operations to execute to set the initial values of the data.
     * @param context
     *            the context in which the operations are executed.
     * @return an instance of the process.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             if no matching process definition is found.
     * @throws ProcessActivationException
     *             if an exception occurs during activation.
     * @throws UserNotFoundException
     *             if there is no user with the specified userId.
     * @throws ProcessExecutionException
     *             if a problem occurs when starting the process.
     * @since 6.0
     */
    ProcessInstance startProcess(long userId, long processDefinitionId, List<Operation> operations, Map<String, Serializable> context)
            throws UserNotFoundException, ProcessDefinitionNotFoundException, ProcessActivationException, ProcessExecutionException;

    /**
     * Execute an activity that is in an unstable state.
     * Will move the activity to the next stable state and then continue the execution of the process.
     * 
     * @param flownodeInstanceId
     *            the identifier of the flow node to execute.
     * @throws FlowNodeExecutionException
     *             if an execution exception occurs.
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
     *            the identifier of the process instance,
     * @param startIndex
     *            the index of the first result (starting from 0).
     * @param maxResults
     *            the maximum number of results to get.
     * @return the matching set of activity instances.
     * @since 6.0
     */
    List<ActivityInstance> getActivities(long processInstanceId, int startIndex, int maxResults);

    /**
     * Get the specified process instance.
     * 
     * @param processInstanceId
     *            the identifier of the process instance.
     * @return the matching instance of process.
     * @throws ProcessInstanceNotFoundException
     *             if there is no process instance with the specified identifier.
     * @since 6.0
     */
    ProcessInstance getProcessInstance(long processInstanceId) throws ProcessInstanceNotFoundException;

    /**
     * Get the specified activity instance.
     * 
     * @param activityInstanceId
     *            the identifier of the activity instance.
     * @return the matching activity instance.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ActivityInstanceNotFoundException
     *             if the activity cannot be found.
     * @throws RetrieveException
     *             if the activity instance cannot be retrieved.
     * @since 6.0
     */
    ActivityInstance getActivityInstance(long activityInstanceId) throws ActivityInstanceNotFoundException;

    /**
     * Get a specified flow node instance.
     * 
     * @param flowNodeInstanceId
     *            the identifier of the flow node instance.
     * @return the matching flow node instance.
     * @since 6.0
     */
    FlowNodeInstance getFlowNodeInstance(final long flowNodeInstanceId) throws FlowNodeInstanceNotFoundException;

    /**
     * Get an activity instance that is archived.
     * 
     * @param activityInstanceId
     *            the identifier of the activity instance.
     * @return the matching archived activity instance.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ActivityInstanceNotFoundException
     *             if the archived activity instance cannot be found.
     * @throws RetrieveException
     *             if the archived activity instance cannot be retrieved.
     * @since 6.0
     */
    ArchivedActivityInstance getArchivedActivityInstance(long activityInstanceId) throws ActivityInstanceNotFoundException;

    /**
     * Get the list of human task instances assigned to the specified user.
     * 
     * @param userId
     *            the identifier of the user.
     * @param startIndex
     *            the index of the first result (starting from 0).
     * @param maxResults
     *            the maximum number of elements to get per page.
     * @param criterion
     *            the sort criterion.
     * @return the matching list of task instances.
     * @throws InvalidSessionException
     *             occurs when the session is invalid.
     * @throws RetrieveException
     *             if a task instance cannot be retrieved.
     * @since 6.0
     */
    List<HumanTaskInstance> getAssignedHumanTaskInstances(long userId, int startIndex, int maxResults, ActivityInstanceCriterion criterion);

    /**
     * Get the list of pending human task instances available to the specified user.
     * A human task is pending for a given user if it is not yet assigned and if the
     * user is a candidate either through an {@link ActorMember} or through a {@link UserFilter}. Hidden tasks for this user are not retrieved (see
     * {@link #hideTasks(long, Long...)}).
     * 
     * @param userId
     *            the identifier of the user.
     * @param startIndex
     *            the index of the first result (starting from 0).
     * @param maxResults
     *            the maximum number of elements to get per page.
     * @param pagingCriterion
     *            the criterion for sorting the items over pages.
     * @return the list of matching task instances.
     * @throws InvalidSessionException
     *             occurs when the session is invalid.
     * @since 6.0
     */
    List<HumanTaskInstance> getPendingHumanTaskInstances(long userId, int startIndex, int maxResults, ActivityInstanceCriterion pagingCriterion);

    /**
     * Count the total number of human task instances assigned to the specified user.
     * 
     * @param userId
     *            the identifier of a user.
     * @return a number of human task instances assigned.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws RetrieveException
     *             if an error occurs while retrieving an instance of an activity.
     * @since 6.0
     */
    long getNumberOfAssignedHumanTaskInstances(long userId);

    /**
     * For a specified list of users, get the number of pending tasks.
     * 
     * @param userIds
     *            a list of user identifiers.
     * @return a map with userId as key and number of tasks as value.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws RetrieveException
     *             can't retrieve an instance of activity
     * @since 6.0
     */
    Map<Long, Long> getNumberOfOpenTasks(List<Long> userIds);

    /**
     * Count the number of pending human task instances available to a specified user.
     * 
     * @param userId
     *            the identifier of a user.
     * @return a number of pending human task instances.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws RetrieveException
     *             if an error occurs while retrieving an instance of an activity.
     * @since 6.0
     */
    long getNumberOfPendingHumanTaskInstances(long userId);

    /**
     * Retrieve a human task instance by the corresponding activity instance id.
     * 
     * @param activityInstanceId
     *            the identifier of the activity instance.
     * @return the matching instance of human task.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ActivityInstanceNotFoundException
     *             if the human task cannot be found.
     * @throws RetrieveException
     *             if an error occurs while retrieving the instance of the activity.
     * @since 6.0
     */
    HumanTaskInstance getHumanTaskInstance(long activityInstanceId) throws ActivityInstanceNotFoundException;

    /**
     * Get a list of event instances related to a process instance that match the specified conditions.
     * 
     * @param rootContainerId
     *            the id of the containing root process instance.
     * @param startIndex
     *            the index of the first result (starting from 0).
     * @param maxResults
     *            the maximum number of results to get.
     * @param sortingType
     *            the criterion for sorting event instances.
     * @return the matching list of event instances.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    List<EventInstance> getEventInstances(long rootContainerId, int startIndex, int maxResults, EventCriterion sortingType);

    /**
     * Assign a task to a user with given user identifier.
     * 
     * @param userTaskId
     *            the identifier of the user task.
     * @param userId
     *            the identifier of the user.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws UpdateException
     *             if an error occurs while updating the activity instance.
     * @since 6.0
     */
    void assignUserTask(long userTaskId, long userId) throws UpdateException;

    /**
     * Updates the actors of the user task. It evaluates again the eligible users for that task.
     * 
     * @param userTaskId
     *            the identifier of the user task
     * @throws UpdateException
     *             If an exception occurs during the evaluation of actors.
     * @since 6.1
     */
    void updateActorsOfUserTask(long userTaskId) throws UpdateException;

    /**
     * Returns all data of a process instance.
     * 
     * @param processInstanceId
     *            the identifier of the process instance.
     * @param startIndex
     *            the index of the page of results to get (starting from 0).
     * @param maxResults
     *            the maximum number of results to get.
     * @return the matching list of dataInstances.
     * @since 6.0
     */
    List<DataInstance> getProcessDataInstances(long processInstanceId, int startIndex, int maxResults);

    /**
     * Get the value of named data item from a specified process instance.
     * The value is returned in a DataInstance object.
     * 
     * @param dataName
     *            the name of the data item.
     * @param processInstanceId
     *            the identifier of the process instance.
     * @return an instance of the data
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws DataNotFoundException
     *             if the specified data value cannot be found.
     * @since 6.0
     */
    DataInstance getProcessDataInstance(String dataName, long processInstanceId) throws DataNotFoundException;

    /**
     * Update the value of a named data item in a specified process instance.
     * 
     * @param dataName
     *            the name of the data item.
     * @param processInstanceId
     *            the identifier of the process instance.
     * @param dataValue
     *            the new value for the data item.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws UpdateException
     *             if a problem occurs while updating the data value.
     * @since 6.0
     */
    void updateProcessDataInstance(String dataName, long processInstanceId, Serializable dataValue) throws UpdateException;

    /**
     * Get a list of the data instances from a specified activity instance.
     * 
     * @param activityInstanceId
     *            the identifier of the activity instance.
     * @param startIndex
     *            the index of the first result (starting at 0).
     * @param maxResults
     *            the maximum number of results to get.
     * @return the list of matching DataInstances.
     * @since 6.0
     */
    List<DataInstance> getActivityDataInstances(long activityInstanceId, int startIndex, int maxResults);

    /**
     * Get a named data instance from a specified activity instance.
     * 
     * @param dataName
     *            the name of the data item.
     * @param activityInstanceId
     *            the identifier of the activity instance.
     * @return an instance of data.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws DataNotFoundException
     *             if the specified data value cannot be found.
     * @since 6.0
     */
    DataInstance getActivityDataInstance(String dataName, long activityInstanceId) throws DataNotFoundException;

    /**
     * Update the value of a named data instance in a specified activity instance.
     * 
     * @param dataName
     *            the name of the data instance.
     * @param activityInstanceId
     *            the identifier of the activity instance.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws UpdateException
     *             if an error occurs during the update.
     * @since 6.0
     */
    void updateActivityDataInstance(String dataName, long activityInstanceId, Serializable dataValue) throws UpdateException;

    /**
     * Get the date when the specified activity instance reached the given state.
     * 
     * @param activityInstanceId
     *            the identifier of the activity instance.
     * @param state
     *            the state of interest.
     * @return the date at which the activity instance reached the state.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws RetrieveException
     *             if an error occurs while retrieving the activity instance.
     * @since 6.0
     */
    Date getActivityReachedStateDate(long activityInstanceId, String state);

    /**
     * Update the given variables of an activity instance.
     * The updates are treated as a single transaction, so if any variable update fails, none of the values is changed.
     * 
     * @param activityInstanceId
     *            the activity identifier.
     * @param variables
     *            a map which contains several pairs of variable name and value.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    void updateActivityInstanceVariables(long activityInstanceId, Map<String, Serializable> variables) throws UpdateException;

    /**
     * Update the values of variables in an activity instance using expressions.
     * 
     * @param operations
     *            a sequence of operations on expressions that update the values variables.
     * @param activityInstanceId
     *            the identifier of the activity instance.
     * @param expressionContexts
     *            store all information identifying the container that the data belongs to.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws UpdateException
     *             if an error occurs during the update.
     * @since 6.0
     */
    void updateActivityInstanceVariables(List<Operation> operations, long activityInstanceId, Map<String, Serializable> expressionContexts)
            throws UpdateException;

    /**
     * Update the due date of a task.
     * 
     * @param userTaskId
     *            teh identifier of the task to update.
     * @param dueDate
     *            the new due date for the task.
     * @throws InvalidSessionException
     *             if the activity does not exist.
     * @since 6.0
     */
    void updateDueDateOfTask(long userTaskId, Date dueDate) throws UpdateException;

    /**
     * Get an instance of a task asssigned to a given user for the specified process instance.
     * 
     * @param processInstanceId
     *            the identifier of the process instance.
     * @param userId
     *            the identifier of the user.
     * @return the id of a user task from the process instance that is assigned to the user.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws UserNotFoundException
     *             if there is no user with the specified id.
     * @throws RetrieveException
     *             if an error occurs happen while retrieving the activity instance.
     * @since 6.0
     */
    long getOneAssignedUserTaskInstanceOfProcessInstance(long processInstanceId, long userId) throws ProcessInstanceNotFoundException, UserNotFoundException;

    /**
     * Get an instance of a task asssigned to a given user for the specified process definition.
     * 
     * @param processDefinitionId
     *            the identifier of the process definition.
     * @param userId
     *            the identifier of a user.
     * @return the id of a user task from the process definition that is assigned to the user.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws RetrieveException
     *             if an error occurs happen while retrieving the activity instance.
     * @since 6.0
     */
    long getOneAssignedUserTaskInstanceOfProcessDefinition(long processDefinitionId, long userId) throws ProcessDefinitionNotFoundException,
            UserNotFoundException;

    /**
     * Get the state of a specified activity instance.
     * 
     * @param activityInstanceId
     *            the identifier of the activity instance.
     * @return the state of the activity instance.
     * @throws ActivityInstanceNotFoundException
     *             if the activity cannot be found.
     * @since 6.0
     */
    String getActivityInstanceState(long activityInstanceId) throws ActivityInstanceNotFoundException;

    /**
     * Check whether a specified task can be executed by a given user.
     * 
     * @param activityInstanceId
     *            the identifier of the activity instance.
     * @param userId
     *            the identifier of a user.
     * @return a flag that indicates whether task can be executed by the user.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ActivityInstanceNotFoundException
     *             if the activity cannot be found.
     * @throws UserNotFoundException
     *             if there is no user with the specified userId.
     * @throws RetrieveException
     *             if an error occurs happen while retrieving the activity instance.
     * @since 6.0
     */
    boolean canExecuteTask(long activityInstanceId, long userId) throws ActivityInstanceNotFoundException, UserNotFoundException;

    /**
     * Release a task (unclaim or unassign). After the operation, the task is in the pending task list.
     * 
     * @param userTaskId
     *            the identifier of the user task.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ActivityInstanceNotFoundException
     *             if the activity cannot be found.
     * @since 6.0
     */
    void releaseUserTask(long userTaskId) throws ActivityInstanceNotFoundException, UpdateException;

    /**
     * List the archived process instances for the specified process instance.
     * A process instance is archived when it changes state, so there are several archived process instances for each process instance.
     * 
     * @param processInstanceId
     *            the identifier of the process instance.
     * @param startIndex
     *            the index of the page of results to get.
     * @param maxResults
     *            the maximum number of results to get.
     * @return the list of archived process instances.
     * @throws InvalidSessionException
     *             if no current valid session is found.
     * @throws RetrieveException
     *             if the search fails because an archived process instance cannot be read.
     * @since 6.0
     */
    List<ArchivedProcessInstance> getArchivedProcessInstances(long processInstanceId, int startIndex, int maxResults);

    /**
     * Get the last archived instance of a process instance.
     * A process instance is archived when it changes state, so there are several archived process instances for each process instance.
     * The last archived instance is returned.
     * 
     * @param processInstanceId
     *            the identifier of the process instance.
     * @return an archived process instance.
     * @throws InvalidSessionException
     *             if no current valid session is found.
     * @throws RetrieveException
     *             if the search fails because an archived process instance cannot be read.
     * @since 6.0
     */
    ArchivedProcessInstance getFinalArchivedProcessInstance(long processInstanceId) throws ArchivedProcessInstanceNotFoundException;

    /**
     * Set the state of an activity instance.
     * 
     * @param activityInstanceId
     *            the identifier of the activity instance.
     * @param stateId
     *            the identifier of the required state.
     * @throws InvalidSessionException
     *             if no current valid session is found.
     * @throws UpdateException
     *             if an error occurs during the update.
     * @since 6.0
     */
    void setActivityStateById(long activityInstanceId, int stateId) throws UpdateException;

    /**
     * Set the state of an activity instance.
     * 
     * @param activityInstanceId
     *            the identifier of the activity instance.
     * @param state
     *            the name of the required state.
     * @throws InvalidSessionException
     *             if no current valid session is found.
     * @throws UpdateException
     *             if an error occurs during the update.
     * @since 6.0
     */
    void setActivityStateByName(long activityInstanceId, String state) throws UpdateException;

    /**
     * Set a state of a process instance.
     * 
     * @param processInstance
     *            a process instance.
     * @param state
     *            the name of the required state.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws UpdateException
     *             if an error occurs during the update.
     * @since 6.0
     */
    void setProcessInstanceState(ProcessInstance processInstance, String state) throws UpdateException;

    /**
     * Set the priority of a user task.
     * 
     * @param userTaskInstanceId
     *            the identifier of user task instance.
     * @param priority
     *            the new priority of this task.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws UpdateException
     *             if an error occurs during the update.
     * @since 6.0
     */
    void setTaskPriority(long userTaskInstanceId, TaskPriority priority) throws UpdateException;

    /**
     * Execute a connector in a specified processDefinition.
     * 
     * @param connectorDefinitionId
     *            the identifier of connector definition.
     * @param connectorDefinitionVersion
     *            the version of the connector definition.
     * @param connectorInputParameters
     *            the expressions related to the connector input paramters.
     * @param inputValues
     *            the parameters values for expression needed when evaluating the connector.
     * @param processDefinitionId
     *            the identifier of the process definition.
     * @return a map with connector parameter names and parameter value objects.
     * @throws ConnectorExecutionException
     *             if an error occurs during connector execution.
     * @throws ConnectorNotFoundException
     *             if there is no connector definition with the specified identifier or version.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnProcessDefinition(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long processDefinitionId)
            throws ConnectorExecutionException, ConnectorNotFoundException;

    /**
     * Execute a connector in a specified processDefinition with operations.
     * 
     * @param connectorDefinitionId
     *            the identifier of connector definition.
     * @param connectorDefinitionVersion
     *            the version of the connector definition.
     * @param connectorInputParameters
     *            the expressions related to the connector input parameters.
     * @param inputValues
     *            the parameters values for expression needed when evaluating the connector.
     * @param operations
     *            the operations used when executing the connector.
     * @param operationInputValues
     *            the input values for the operations.
     * @param processDefinitionId
     *            the identifier of the process definition.
     * @return a map with connector parameter names and parameter value objects after operations and connector execution.
     * @throws ConnectorExecutionException
     *             if an error occurs during connector execution.
     * @throws ConnectorNotFoundException
     *             if there is no connector definition with the specified identifier or version.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnProcessDefinition(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, List<Operation> operations,
            Map<String, Serializable> operationInputValues, long processDefinitionId) throws ConnectorExecutionException, ConnectorNotFoundException;

    /**
     * Search the archived human tasks for tasks that match the search options.
     * 
     * @param searchOptions
     *            the search conditions and the options for sorting and paging the results.
     * @return the archived human tasks that match the search conditions.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    SearchResult<ArchivedHumanTaskInstance> searchArchivedHumanTasks(SearchOptions searchOptions) throws SearchException;

    /**
     * Search the assigned human tasks for tasks that match the search options and are administered by the specified user.
     * 
     * @param managerUserId
     *            the identifier of the user.
     * @param searchOptions
     *            the search conditions and the options for sorting and paging the results.
     * @return the assigned human tasks that match the search conditions and are supervised by the user.
     * @throws SearchException
     *             if there is an error in the search conditions.
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchAssignedTasksManagedBy(long managerUserId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search the pending human tasks for tasks that match the search options and are supervised by the specified user.
     * 
     * @param userId
     *            the identifier of the user.
     * @param searchOptions
     *            the search conditions and the options for sorting and paging the results.
     * @return the pending human tasks that match the search conditions and are supervised by the user.
     * @throws SearchException
     *             if there is an error in the search conditions.
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchPendingTasksSupervisedBy(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search the pending human tasks for tasks available to the specified user.
     * 
     * @param userId
     *            the identifier of the user.
     * @param searchOptions
     *            the search conditions and the options for sorting and paging the results.
     * @return the pending human tasks that match the search conditions and are available to the user.
     * @throws SearchException
     *             if there is an error in the search conditions.
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchPendingTasksForUser(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search the pending human tasks for tasks that match the search options and are managed by the specified user.
     * 
     * @param managerUserId
     *            the identifier of the user.
     * @param searchOptions
     *            the search conditions and the options for sorting and paging the results.
     * @return the pending human tasks that match the search conditions and are managed by the user.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if there is an error in the search conditions.
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchPendingTasksManagedBy(long managerUserId, SearchOptions searchOptions) throws SearchException;

    /**
     * Get the number of assigned and pending overdue tasks for the specified users.
     * 
     * @param userIds
     *            a list of user identifiers.
     * @return a map of user identifiers and numbers of overdue tasks.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Map<Long, Long> getNumberOfOverdueOpenTasks(List<Long> userIds);

    /**
     * Cancel a specified process instance.
     * 
     * @param processInstanceId
     *            the identifier of the process instance.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ProcessInstanceNotFoundException
     *             if there is no process instance with the specified identifier.
     * @throws RetrieveException
     *             if an error occurs while retreiving the process instance.
     * @since 6.0
     */
    void cancelProcessInstance(long processInstanceId) throws ProcessInstanceNotFoundException, UpdateException;

    /**
     * Reset the state of a failed activity instance to its previous state and then execute it.
     * The activity must be in state FAILED.
     * 
     * @param activityInstanceId
     *            the identifier of the activity instance.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ActivityInstanceNotFoundException
     *             if there is no activity instance with the specified identifier.
     * @throws ActivityExecutionException
     *             if an error occurs either while resetting the state of while executing the activity instance.
     * @since 6.0
     */
    void retryTask(long activityInstanceId) throws ActivityInstanceNotFoundException, ActivityExecutionException;

    /**
     * Hides a list of tasks from a specified user. A task that is "hidden" for a user is not pending for that user,
     * so is not retrieved by #searchPendingTasksForUser.
     * As soon as a task is claimed by or assigned to a user, it is no longer hidden from any users.
     * 
     * @param userId
     *            the identifier of the user.
     * @param activityInstanceId
     *            the list of identifiers of the tasks to be hidden.
     * @throws InvalidSessionException
     *             if there is no current valid session.
     * @throws UpdateException
     *             if a problem occurs when hiding one of the tasks.
     * @see #unhideTasks(long, Long...)
     * @since 6.0
     */
    void hideTasks(long userId, Long... activityInstanceId) throws UpdateException;

    /**
     * Un-hides a list of tasks for a specified user. Un-hiding a task makes it available for a user if the task is pending for that user.
     * 
     * @param userId
     *            the identifier of the user.
     * @param activityInstanceId
     *            the list of identifiers of the tasks to be hidden.
     * @throws InvalidSessionException
     *             if there is no current valid session.
     * @throws UpdateException
     *             if a problem occurs when un-hiding one of the tasks.
     * @see #hideTasks(long, Long...)
     * @since 6.0
     */
    void unhideTasks(long userId, Long... activityInstanceId) throws UpdateException;

    /**
     * Evaluate an expression in the context of the specified process.
     * Some context values can also be provided
     * 
     * @param expression
     *            the expression to evaluate.
     * @param context
     *            context values that are provided for evaluating the expression.
     * @param processDefinitionId
     *            the id of the process definition in which the expression is evaluated.
     * @return
     *         the result of the evaluation.
     * @throws InvalidSessionException
     *             if there is no current valid session.
     * @throws ExpressionEvaluationException
     *             if an error occurs while evaluating the expression.
     * @since 6.0
     */
    Serializable evaluateExpressionOnProcessDefinition(Expression expression, Map<String, Serializable> context, long processDefinitionId)
            throws ExpressionEvaluationException;

    /**
     * Checks whether a specified task is hidden from a given user.
     * 
     * @param userTaskId
     *            the identifier of the task to check.
     * @param userId
     *            the identifier of the user.
     * @return
     *         true if the task is hidden from the user.
     * @throws InvalidSessionException
     *             if there is no current valid session
     * @throws RetrieveException
     *             if an error occurs while retreiving the task.
     * @since 6.0
     */
    boolean isTaskHidden(long userTaskId, long userId);

    /**
     * Get the number of comments matching the search conditions.
     * 
     * @param searchOptions
     *            the search conditions and the options for sorting and paging the results.
     * @return
     *         the number of comments matching the search conditions.
     * @throws SearchException
     *             if the search could not be completed correctly.
     * @since 6.0
     */
    long countComments(SearchOptions searchOptions) throws SearchException;

    /**
     * Get the number of attachments matching the search conditions.
     * 
     * @param searchOptions
     *            the search conditions and the options for sorting and paging the results.
     * @return
     *         the number of attachments matching the search conditions.
     * @throws SearchException
     *             if the search could not be completed correctly.
     * @since 6.0
     */

    long countAttachments(SearchOptions searchOptions) throws SearchException;

    /**
     * Send a BPMN signal event. Invoking this method acts as executing a Throw Signal Event.
     * 
     * @param signalName
     *            the signal name.
     * @throws InvalidSessionException
     *             if there is no current valid session.
     * @throws SendEventException
     *             if an exception occurs while sending signal.
     * @since 6.0
     */
    void sendSignal(String signalName) throws SendEventException;

    /**
     * Send a BPMN message event. Invoking this method acts as executing a Throw Message Event.
     * 
     * @param messageName
     *            the message name.
     * @param targetProcess
     *            an expression representing the target process name.
     * @param targetFlowNode
     *            an expression representing the target flow node name.
     * @param messageContent
     *            a key->value map containing the message data, with the data name as key.
     * @throws InvalidSessionException
     *             if there is no current valid session.
     * @throws SendEventException
     *             if an exception occurs while sending message.
     * @since 6.0
     */
    void sendMessage(String messageName, Expression targetProcess, Expression targetFlowNode, Map<Expression, Expression> messageContent)
            throws SendEventException;

    /**
     * Send a BPMN message event, with message correlation. Invoking this method acts as executing a Throw Message Event.
     * 
     * @param messageName
     *            the message name.
     * @param targetProcess
     *            an expression representing the target process name.
     * @param targetFlowNode
     *            an expression representing the target flow node name.
     * @param messageContent
     *            a key->value map containing the message data, with the data name as key.
     * @param correlations
     *            the message correlations (five maximum).
     * @throws InvalidSessionException
     *             if there is no current valid session.
     * @throws SendEventException
     *             if there are too many correlations (more than 5) or an exception occurs while sending message.
     * @since 6.0
     */
    void sendMessage(String messageName, Expression targetProcess, Expression targetFlowNode, Map<Expression, Expression> messageContent,
            Map<Expression, Expression> correlations) throws SendEventException;

    /**
     * Retrieve an <code>ArchivedProcessInstance</code> specified by its identifier.
     * 
     * @param archivedProcessInstanceId
     *            the identifier of the <code>ArchivedProcessInstance</code> to be retrieved.
     * @return the <code>ArchivedProcessInstance</code> instance.
     * @throws ArchivedProcessInstanceNotFoundException
     *             if the <code>ArchivedProcessInstance</code> was not found.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws RetrieveException
     *             if an error occurs while trying to retrieve the <code>ArchivedProcessInstance</code>.
     * @since 6.0
     */
    ArchivedProcessInstance getArchivedProcessInstance(long archivedProcessInstanceId) throws ArchivedProcessInstanceNotFoundException;

    /**
     * Retrieve an <code>ArchivedFlowNodeInstance</code> specified by its identifier.
     * 
     * @param archivedFlowNodeInstanceId
     *            the identifier of the <code>ArchivedFlowNodeInstance</code> to be retrieved.
     * @return the <code>ArchivedFlowNodeInstance</code> instance.
     * @throws ArchivedFlowNodeInstanceNotFoundException
     *             if the <code>ArchivedFlowNodeInstance</code> was not found.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws RetrieveException
     *             if an error occurs while trying to retrieve the <code>ArchivedFlowNodeInstance</code>.
     * @since 6.0
     */
    ArchivedFlowNodeInstance getArchivedFlowNodeInstance(long archivedFlowNodeInstanceId) throws ArchivedFlowNodeInstanceNotFoundException;

    /**
     * Retrieve an <code>ArchivedComment</code> specified by its identifier.
     * 
     * @param archivedCommentId
     *            the identifier of the <code>ArchivedComment</code> to be retrieved.
     * @return the <code>ArchivedComment</code> instance.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws RetrieveException
     *             if an error occurs while trying to retrieve the <code>ArchivedComment</code>.
     * @throws NotFoundException
     *             if no <code>ArchivedComment</code> was found with the specified archivedCommentId.
     * @since 6.0
     */
    ArchivedComment getArchivedComment(long archivedCommentId) throws NotFoundException;
    
    /**
     * Search for connector instances.
     * 
     * @param searchOptions
     *            the search conditions and the options for sorting and paging the results.
     * @return the {@link SearchResult} containing the <code>ConnectorInstance</code>s matching the search options.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<ConnectorInstance> searchConnectorInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for archived connector instances.
     * 
     * @param searchOptions
     *            the search options parameters
     * @return the {@link SearchResult} containing the <code>ArchivedConnectorInstance</code>s matching the search options.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<ArchivedConnectorInstance> searchArchivedConnectorInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * List the named human tasks belonging to the specified process instance.
     * 
     * @param processInstanceId
     *            the identifier of the process instance.
     * @param taskName
     *            the name of the required human tasks.
     * @param startIndex
     *            the result start index (strating from 0).
     * @param maxResults
     *            the maximum number of results to retrieve.
     * @return the list of matching human task instances.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    List<HumanTaskInstance> getHumanTaskInstances(long processInstanceId, String taskName, int startIndex, int maxResults);

    /**
     * Return the last created human task instance with the specified name for the given process instance.
     * 
     * @param processInstanceId
     *            the identifier of the process instance.
     * @param taskName
     *            the name of the required human task.
     * @return a HumanTaskInstance, in its latest state.
     * @throws NotFoundException
     *             if no current task with provided name is found.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    HumanTaskInstance getLastStateHumanTaskInstance(long processInstanceId, String taskName) throws NotFoundException;

    /**
     * Search for archived activity instances in terminal states. Archived activity instances in intermediate states are not considered.
     * 
     * @param searchOptions
     *            the criterion used to search for archived activity instances.
     * @return a {@link SearchResult} containing the search result.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if an exception occurs during the search.
     * @since 6.0
     */
    SearchResult<ArchivedActivityInstance> searchArchivedActivities(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for activity instances.
     * 
     * @param searchOptions
     *            the criterion used to search for activity instances.
     * @return a {@link SearchResult} containing the search result.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<ActivityInstance> searchActivities(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for flow node instances (activities, gateways and events).
     * 
     * @param searchOptions
     *            the criterion used to search for flow node instances.
     * @return a {@link SearchResult} containing the search result
     * @throws InvalidSessionException
     *             if the ession is invalid, e.g session has expired.
     * @throws SearchException
     *             if an exception occurs during the search.
     * @since 6.0
     */
    SearchResult<FlowNodeInstance> searchFlowNodeInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for archived flow node instances (activities, gateways and events)
     * 
     * @param searchOptions
     *            the options used to search for flow node instances.
     * @return a {@link SearchResult} containing the search result.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g session has expired.
     * @throws SearchException
     *             if an exception occurs during the search.
     * @see {@link ArchivedFlowNodeInstance}
     * @since 6.0
     */
    SearchResult<ArchivedFlowNodeInstance> searchArchivedFlowNodeInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for all tasks available to a specified user.
     * A task is available to a user if is assigned to the user or it is pending for that user.
     * Hidden tasks are not retrieved.
     * 
     * @param userId
     *            the identifier of the user for whom the tasks are available.
     * @param searchOptions
     *            the options used to search for tasks.
     * @return the list of tasks matching the search options.
     * @throws InvalidSessionException
     *             if the current session is invalid.
     * @throws SearchException
     *             if an exception occurs during the search.
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchMyAvailableHumanTasks(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search for comments related to the specified process instance.
     * 
     * @param searchOptions
     *            the options used to search for comments.
     * @return the matching comments.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if an exception occurs during the search.
     * @since 6.0
     */
    SearchResult<Comment> searchComments(SearchOptions searchOptions) throws SearchException;

    /**
     * Add a comment on a process instance.
     * 
     * @param processInstanceId
     *            the identifier of the process instance.
     * @param comment
     *            the content of the comment.
     * @return the newly created comment.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @deprecated use {@link #addProcessComment(long, String)} instead, that can throw CreationException is case of inexistant Process Instance
     * @since 6.0
     */
    @Deprecated
    Comment addComment(long processInstanceId, String comment);

    /**
     * Add a comment on a process instance.
     * 
     * @param processInstanceId
     *            the identifier of the process instance.
     * @param comment
     *            the content of the comment.
     * @return the newly created comment.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws CreationException
     *             if the parameter processInstanceId does not refer to any active process instance (existing and non-archived).
     * @since 6.1
     */
    Comment addProcessComment(final long processInstanceId, final String comment) throws CreationException;

    /**
     * Get the first 20 comments of the specified process instance.
     * 
     * @param processInstanceId
     *            the identifier of the process instance.
     * @return the list of comments found
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @deprecated use paginated version {@link #searchComments(SearchOptions)} instead, passing a filter on processInstanceId field.
     * @since 6.0
     */
    @Deprecated
    List<Comment> getComments(long processInstanceId);

    /**
     * Get all the comments managed by the specified user.
     * A comment is considered to be managed by user A if one or more of the following conditions is true:
     * - the author of the comment is a subordinate of user A (A the author's manager).
     * - the comment belongs to a process started by a subordinate of user A.
     * - the comment belongs to a process where at least one human task is assigned to a subordinate of user A.
     * 
     * @param managerUserId
     *            the identifier of the user.
     * @param searchOptions
     *            the options used to search for comments.
     * @return the comments managed by the user that match the search options.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<Comment> searchCommentsManagedBy(long managerUserId, SearchOptions searchOptions) throws SearchException;

    /**
     * Get the comments on process instances that the specified user can access.
     * 
     * @param userId
     *            the identifier of the user.
     * @param searchOptions
     *            the options used to search for comments.
     * @return the comments on process instances that the user can access.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<Comment> searchCommentsInvolvingUser(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Get the children instances (sub process or call activity) of a process instance. The returned list is paginated.
     * 
     * @param processInstanceId
     *            the identifier of the process definition.
     * @param startIndex
     *            the index of the page to be returned (starting at 0).
     * @param maxResults
     *            the maximum number of results per page.
     * @param criterion
     *            the criterion used to sort the result.
     * @return the list of children instance identifiers.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    List<Long> getChildrenInstanceIdsOfProcessInstance(long processInstanceId, int startIndex, int maxResults, ProcessInstanceCriterion criterion);

    /**
     * Check whether a specified user is involved in a process instance.
     * User A is involved with a process instance if any of the following is true:
     * - a task in the process instance is assigned to user A
     * - a task in the process instance is pending for user A
     * - a task in the process instance is assigned to a user managed by user A
     * - a task in the process instance is pending for a user managed by user A
     * 
     * @param userId
     *            the identifier of the user.
     * @param processInstanceId
     *            the identifier of the process instance.
     * @return true if the user is involved with the process instance.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ProcessInstanceNotFoundException
     *             if there is no processInstance with the specified identifier.
     * @throws UserNotFoundException
     *             if there is no user with the specified identifier.
     * @since 6.0
     */
    boolean isInvolvedInProcessInstance(long userId, long processInstanceId) throws ProcessInstanceNotFoundException, UserNotFoundException;

    /**
     * Get the process instance id from an activity instance id.
     * 
     * @param activityInstanceId
     *            the identifier of the activity instance.
     * @return the corresponding process instance id.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ProcessInstanceNotFoundException
     *             if there is no process instance with the specified identifier.
     * @since 6.0
     */
    long getProcessInstanceIdFromActivityInstanceId(long activityInstanceId) throws ProcessInstanceNotFoundException;

    /**
     * Get the process definition id from an process instance id.
     * 
     * @param processInstanceId
     *            the identifier of the process instance.
     * @return the corresponding process definition id.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             if there is no process definition with the specified identifier.
     * @since 6.0
     */
    long getProcessDefinitionIdFromProcessInstanceId(long processInstanceId) throws ProcessDefinitionNotFoundException;

    /**
     * Get the process definition id from an activity instance id.
     * 
     * @param activityInstanceId
     *            the identifier of the activity instance.
     * @return the corresponding process definition id.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             if no ProcessDefinition have an id corresponding to the parameter.
     * @since 6.0
     */
    long getProcessDefinitionIdFromActivityInstanceId(long activityInstanceId) throws ProcessDefinitionNotFoundException;

    /**
     * Search for archived comments.
     * 
     * @param searchOptions
     *            the options used to search for comments.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @return the <code>ArchivedComment</code> items that match the search options.
     * @throws SearchException
     *             if the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<ArchivedComment> searchArchivedComments(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for archived human tasks managed by the specified user.
     * 
     * @param managerUserId
     *            the identifier of the user manager,
     * @param searchOptions
     *            the options used to search for tasks.
     * @return archived humanTask instances managed by the specified user that match the search options.
     * @throws SearchException
     *             if the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<ArchivedHumanTaskInstance> searchArchivedHumanTasksManagedBy(long managerUserId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search for open process instances that the specified user can access.
     * 
     * @param userId
     *            the identifier of the user.
     * @param searchOptions
     *            the options used to search for process instance.
     * @return the <code>ProcessInstance</code>s that match the search options.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<ProcessInstance> searchOpenProcessInstancesInvolvingUser(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search for open process instances that all subordinates of the specified user can access.
     * 
     * @param managerUserId
     *            the identifier of the user manager.
     * @param searchOptions
     *            the search options (pagination, filter, order sort).
     * @return the <code>ProcessInstance</code>s that match the search options.
     * @throws SearchException
     *             if the search could not be completed correctly.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    SearchResult<ProcessInstance> searchOpenProcessInstancesInvolvingUsersManagedBy(long managerUserId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search for archived process instances.
     * 
     * @param searchOptions
     *            the search options (pagination, filter, order sort).
     * @return the archived process instances that match the search options.
     * @throws SearchException
     *             if the search could not be fullfilled correctly
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    SearchResult<ArchivedProcessInstance> searchArchivedProcessInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for archived process instances supervised by the specified user.
     * 
     * @param userId
     *            the identifier of the user.
     * @param searchOptions
     *            the search options (pagination, filter, order sort).
     * @return the archived process instances supervised by the user that match the search options.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<ArchivedProcessInstance> searchArchivedProcessInstancesSupervisedBy(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search for archived process instances that the specified user can access.
     * 
     * @param userId
     *            the identifier of the user.
     * @param searchOptions
     *            the search options (pagination, filter, order sort).
     * @return the archived process instances that the user can access that match the search options.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<ArchivedProcessInstance> searchArchivedProcessInstancesInvolvingUser(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search for human task instances.
     * 
     * @param searchOptions
     *            the search options (pagination, filter, order sort).
     * @return the human task instances that match the search options.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchHumanTaskInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for tasks assigned to users supervised by the specified user.
     * 
     * @param supervisorId
     *            the identifier of supervising user.
     * @param searchOptions
     *            the search options (pagination, filter, order sort).
     * @return the human task instances that match the search options.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchAssignedTasksSupervisedBy(long supervisorId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search for archived tasks assigned to users supervised by the specified user.
     * 
     * @param supervisorId
     *            the identifier of the supervising user.
     * @param searchOptions
     *            the search options (pagination, filter, order sort).
     * @return the archived human task instances that match the search options.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             if the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<ArchivedHumanTaskInstance> searchArchivedHumanTasksSupervisedBy(long supervisorId, SearchOptions searchOptions) throws SearchException;

    /**
     * Evaluate expressions with values valid at process instantiation scope.
     * 
     * @param processInstanceId
     *            the identifier of the process instance.
     * @param expressions
     *            a map of expressions to evaluate.
     * @return the result of the expression execution. Content of the resulting map depends on the incoming expression map. The returned key is the name of the
     *         expression (or its content if name is empty), the returned value is the evaluated expression result.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ExpressionEvaluationException
     *             occurs when an exception is thrown during expression evaluation.
     * @since 6.0
     */
    Map<String, Serializable> evaluateExpressionsAtProcessInstanciation(long processInstanceId, Map<Expression, Map<String, Serializable>> expressions)
            throws ExpressionEvaluationException;

    /**
     * Evaluate expressions with values valid on a completed process instance scope.
     * 
     * @param processInstanceId
     *            the identifier of the process instance.
     * @param expressions
     *            a map of expressions to evaluate.
     * @return the result of the expression execution. Content of the resulting map depends on the incoming expression map. The returned key is the name of the
     *         expression (or its content if name is empty), the returned value is the evaluated expression result.
     * @throws InvalidSessionException
     *             if the API session is invalid, e.g session has expired.
     * @throws ExpressionEvaluationException
     *             occurs when an exception is thrown during expression evaluation.
     * @since 6.0
     */
    Map<String, Serializable> evaluateExpressionOnCompletedProcessInstance(long processInstanceId, Map<Expression, Map<String, Serializable>> expressions)
            throws ExpressionEvaluationException;

    /**
     * Evaluate expressions with values valid on a process instance scope.
     * 
     * @param processInstanceId
     *            the identifier of the process instance.
     * @param expressions
     *            a map of expressions to evaluate.
     * @return the result of the expression execution. Content of the resulting map depends on the incoming expression map. The returned key is the name of the
     *         expression (or its content if name is empty), the returned value is the evaluated expression result.
     * @throws InvalidSessionException
     *             if the API session is invalid, e.g session has expired.
     * @throws ExpressionEvaluationException
     *             occurs when an exception is thrown during expression evaluation.
     * @since 6.0
     */
    Map<String, Serializable> evaluateExpressionsOnProcessInstance(long processInstanceId, Map<Expression, Map<String, Serializable>> expressions)
            throws ExpressionEvaluationException;

    /**
     * Evaluate expressions with values valid on a process definition scope.
     * 
     * @param processDefinitionId
     *            the identifier of the process definition.
     * @param expressions
     *            a map of expressions to evaluate.
     * @return the result of the expression execution. Content of the resulting map depends on the incoming expression map. The returned key is the name of the
     *         expression (or its content if name is empty), the returned value is the evaluated expression result.
     * @throws InvalidSessionException
     *             if the API session is invalid, e.g session has expired.
     * @throws ExpressionEvaluationException
     *             occurs when an exception is thrown during expression evaluation.
     * @since 6.0
     */
    Map<String, Serializable> evaluateExpressionsOnProcessDefinition(long processDefinitionId, Map<Expression, Map<String, Serializable>> expressions)
            throws ExpressionEvaluationException;

    /**
     * Evaluate expressions with values valid on an activity instance scope.
     * 
     * @param activityInstanceId
     *            the identifier of the activity instance.
     * @param expressions
     *            a map of expressions to evaluate.
     * @return the result of the expression execution. Content of the resulting map depends on the incoming expression map. The returned key is the name of the
     *         expression (or its content if name is empty), the returned value is the evaluated expression result.
     * @throws InvalidSessionException
     *             if the API session is invalid, e.g session has expired.
     * @throws ExpressionEvaluationException
     *             occurs when an exception is thrown during expression evaluation.
     * @since 6.0
     */
    Map<String, Serializable> evaluateExpressionsOnActivityInstance(long activityInstanceId, Map<Expression, Map<String, Serializable>> expressions)
            throws ExpressionEvaluationException;

    /**
     * Evaluate expressions with values valid on a completed activity instance scope.
     * 
     * @param activityInstanceId
     *            the identifier of the activity instance.
     * @param expressions
     *            a map of expressions to evaluate.
     * @return the result of the expression execution. Content of the resulting map depends on the incoming expression map. The returned key is the name of the
     *         expression (or its content if name is empty), the returned value is the evaluated expression result.
     * @throws InvalidSessionException
     *             if the API session is invalid, e.g session has expired.
     * @throws ExpressionEvaluationException
     *             occurs when an exception is thrown during expression evaluation.
     * @since 6.0
     */
    Map<String, Serializable> evaluateExpressionsOnCompletedActivityInstance(long activityInstanceId, Map<Expression, Map<String, Serializable>> expressions)
            throws ExpressionEvaluationException;

}
