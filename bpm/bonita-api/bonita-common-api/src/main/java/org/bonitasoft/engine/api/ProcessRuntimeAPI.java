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
package org.bonitasoft.engine.api;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.comment.ArchivedComment;
import org.bonitasoft.engine.bpm.comment.Comment;
import org.bonitasoft.engine.bpm.connector.ArchivedConnectorInstance;
import org.bonitasoft.engine.bpm.connector.ConnectorExecutionException;
import org.bonitasoft.engine.bpm.connector.ConnectorInstance;
import org.bonitasoft.engine.bpm.connector.ConnectorNotFoundException;
import org.bonitasoft.engine.bpm.contract.ContractDefinition;
import org.bonitasoft.engine.bpm.contract.ContractViolationException;
import org.bonitasoft.engine.bpm.data.ArchivedDataInstance;
import org.bonitasoft.engine.bpm.data.ArchivedDataNotFoundException;
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
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstance;
import org.bonitasoft.engine.bpm.flownode.TimerEventTriggerInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.UserTaskNotFoundException;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessActivationException;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessExecutionException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.ContractDataNotFoundException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.engine.job.FailedJob;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.InvalidSessionException;

/**
 * <code>ProcessRuntimeAPI</code> deals with Process runtime notions such as starting a new instance of a process, retrieving and executing tasks, accessing to
 * all types of tasks, assigning a user to a task, retrieving archived versions of a task, accessing / updating data / variable values, adding / retrieving
 * process comments ...
 * It generally allows all BPM runtime actions, that is, once process instances are running of finished executing.
 *
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Zhao Na
 * @author Frederic Bouquet
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 */
public interface ProcessRuntimeAPI {

    /**
     * List all open root process instances.
     *
     * @param searchOptions
     *        The search criterion. See {@link org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor} for valid fields for searching and sorting.
     * @return A {@link ProcessInstance} object.
     * @throws SearchException
     *         If an exception occurs when getting the list of tasks.
     * @since 6.0
     */
    SearchResult<ProcessInstance> searchOpenProcessInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * List all process instances.
     *
     * @param searchOptions
     *        The search criterion. See {@link org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor} for valid fields for searching and sorting.
     * @return A {@link ProcessInstance} object.
     * @throws SearchException
     *         If an exception occurs when getting the list of {@link ProcessInstance}.
     * @since 6.2
     */
    SearchResult<ProcessInstance> searchProcessInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * List all process instances with at least one failed task or the {@link org.bonitasoft.engine.bpm.process.ProcessInstanceState#ERROR} state.
     *
     * @param searchOptions
     *        The search criterion. See {@link org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor} for valid fields for searching and sorting.
     * @return A {@link ProcessInstance} object.
     * @throws SearchException
     *         If an exception occurs when getting the list of {@link ProcessInstance}.
     * @since 6.4.0
     */
    SearchResult<ProcessInstance> searchFailedProcessInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * List all process instances with at least one failed task or the {@link org.bonitasoft.engine.bpm.process.ProcessInstanceState#ERROR} state that
     * are supervised by the given user.
     * If the specified userId does not correspond to a user, an empty SearchResult is returned.
     *
     * @param userId
     *        The identifier of the user.
     * @param searchOptions
     *        The search criterion. See {@link org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor} for valid fields for searching and sorting.
     * @return The list of failed process instances supervised by the specified user.
     * @throws SearchException
     *         If an exception occurs when getting the list of process instances.
     * @since 7.0
     */
    SearchResult<ProcessInstance> searchFailedProcessInstancesSupervisedBy(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * List all open process instances supervised by a user.
     * If the specified userId does not correspond to a user, an empty SearchResult is returned.
     *
     * @param userId
     *        The identifier of the user.
     * @param searchOptions
     *        The search criterion. See {@link org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor} for valid fields for searching and sorting.
     * @return The list of process instances supervised by the specified user.
     * @throws SearchException
     *         If an exception occurs when getting the list of process instances.
     * @since 6.0
     */
    SearchResult<ProcessInstance> searchOpenProcessInstancesSupervisedBy(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Get the number of process data instances by process id.
     *
     * @param processInstanceId
     *        The identifier of the process instance.
     * @return The number of process data instances.
     * @throws ProcessInstanceNotFoundException
     *         If the specified ProcessInstance does not refer to a process instance.
     * @since 6.0
     */
    long getNumberOfProcessDataInstances(long processInstanceId) throws ProcessInstanceNotFoundException;

    /**
     * Get the number of activity data instances by activity id.
     *
     * @param activityInstanceId
     *        The identifier of the activity instance.
     * @return The number of activity data instances.
     * @throws ActivityInstanceNotFoundException
     *         If the specified activity instance does not refer to an activity instance.
     * @since 6.0
     */
    long getNumberOfActivityDataInstances(long activityInstanceId) throws ActivityInstanceNotFoundException;

    /**
     * Get a paged list of all process instances.
     *
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of results per page.
     * @param criterion
     *        The sort criterion.
     * @return The list of process instances.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    List<ProcessInstance> getProcessInstances(int startIndex, int maxResults, ProcessInstanceCriterion criterion);

    /**
     * Get a paged list of archived process instances.
     *
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of results per page.
     * @param criterion
     *        The sort criterion.
     * @return The list of archived process instances.
     * @since 6.0
     */
    List<ArchivedProcessInstance> getArchivedProcessInstances(int startIndex, int maxResults, ProcessInstanceCriterion criterion);

    /**
     * Get a paged list of archived activity instances for a process instance.
     *
     * @param processInstanceId
     *        The identifier of the process instance.
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of result per page.
     * @param criterion
     *        The sort criterion.
     * @return The list of archived activity instances.
     * @since 6.0
     */
    List<ArchivedActivityInstance> getArchivedActivityInstances(long processInstanceId, int startIndex, int maxResults, ActivityInstanceCriterion criterion);

    /**
     * Retrieve a paged list of open activities for a given process instance.
     *
     * @param processInstanceId
     *        The identifier of the process instance.
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of results per page.
     * @param criterion
     *        The sort criterion.
     * @return The list of activity instances.
     * @since 6.0
     */
    List<ActivityInstance> getOpenActivityInstances(long processInstanceId, int startIndex, int maxResults, ActivityInstanceCriterion criterion);

    /**
     * Get the total number of open activity instances by process instance id.
     *
     * @param processInstanceId
     *        The identifier of the process instance.
     * @return The number of open activity instances.
     * @throws ProcessInstanceNotFoundException
     *         If no matching process instance is found for parameter processInstanceId
     * @since 6.0
     */
    int getNumberOfOpenedActivityInstances(long processInstanceId) throws ProcessInstanceNotFoundException;

    /**
     * Get the number of open process instances.
     * An open process instance is a process instance that has not been archived.
     *
     * @return The total number of open process instances.
     * @since 6.0
     */
    long getNumberOfProcessInstances();

    /**
     * Get the number of archived process instances.
     * Process instances in state COMPLETED are counted.
     *
     * @return The number of archived process instances.
     * @since 6.0
     */
    long getNumberOfArchivedProcessInstances();

    /**
     * Delete the specified process instance.
     *
     * @param processInstanceId
     *        The identifier of the process instance to delete.
     * @throws org.bonitasoft.engine.exception.ProcessInstanceHierarchicalDeletionException
     *         If a process instance cannot be deleted because of a parent that is still active.
     * @throws DeletionException
     *         If an error occurs during deletion.
     * @since 6.0
     */
    void deleteProcessInstance(long processInstanceId) throws DeletionException;

    /**
     * Delete all instances of a specified process definition.
     * If the process definition id does not match anything, no exception is thrown, but nothing is deleted.
     *
     * @param processDefinitionId
     *        The identifier of the processDefinition.
     * @throws org.bonitasoft.engine.exception.ProcessInstanceHierarchicalDeletionException
     *         If a process instance cannot be deleted because of a parent that still exists.
     * @throws DeletionException
     *         If other deletion problem occurs.
     * @since 6.0
     * @deprecated As of release 6.1, replaced by {@link #deleteProcessInstances(long, int, int)} and {@link #deleteArchivedProcessInstances(long, int, int)}.
     *             As these new methods are paginated, to delete ALL archived and non-archived process instances, use some code like:
     *             <pre>
     *             <code>
     *             long nbDeleted = 0;
     *             processAPI.disableProcess(processDefinitionId);
     *             do {
     *             nbDeleted = processAPI.deleteProcessInstances(processDefinitionId, 0, 100);
     *             } while (nbDeleted &gt; 0);
     *             do {
     *             nbDeleted = processAPI.deleteArchivedProcessInstances(processDefinitionId, 0, 100);
     *             } while (nbDeleted &gt; 0);
     *             </code>
     *             </pre>
     */
    @Deprecated
    void deleteProcessInstances(long processDefinitionId) throws DeletionException;

    /**
     * Delete active process instances, and their elements, of process definition given as input parameter respecting the pagination parameters.
     * Passing {@link Integer#MAX_VALUE} as maxResults is discouraged as the amount of operations may be large and may thus result in timeout operation.
     * Instead, to delete all Process instances of a specific process definition, should you should use a loop and delete instances in bulk.
     *
     * @param processDefinitionId
     *        Identifier of the processDefinition
     * @param startIndex
     *        The index
     * @param maxResults
     *        The max number of elements to retrieve per page
     * @return The number of elements that have been deleted
     * @throws DeletionException
     *         If a process instance can't be deleted because of a parent that is still active
     * @since 6.1
     */
    long deleteProcessInstances(long processDefinitionId, int startIndex, int maxResults) throws DeletionException;

    /**
     * Delete archived process instances of process definition given as input parameter respecting the pagination parameters.
     * Passing {@link Integer#MAX_VALUE} as maxResults is discouraged as the amount of operations may be large and may thus result in timeout operation.
     * Instead, to delete all archived process instances of a specific process definition, you should use a loop and delete archived instances in bulk.
     *
     * @param processDefinitionId
     *        Identifier of the processDefinition
     * @param startIndex
     *        The index
     * @param maxResults
     *        The max number of elements to retrieve per page
     * @return The number of elements that have been deleted in any state. For example, process instance can be archived is several states: Cancelled,
     *         Aborted, Completed, Failed
     * @throws DeletionException
     *         If a process instance can't be deleted because of a parent that is still active
     * @since 6.1
     */
    long deleteArchivedProcessInstances(long processDefinitionId, int startIndex, int maxResults) throws DeletionException;

    /**
     * Delete all archived process instance (different states) of the source identifier list.
     * Passing {@link Integer#MAX_VALUE} identifiers is discouraged as the amount of operations may be large and may thus result in timeout operation.
     *
     * @param sourceProcessInstanceIds
     *        Identifiers corresponding to {@link ArchivedProcessInstance#getSourceObjectId()}.
     * @return The number of {@link ArchivedProcessInstance}s that have been deleted in any state. For example, process instance can be archived is several
     *         states: Cancelled, Aborted, Completed, Failed
     * @throws DeletionException
     *         If a process instance can't be deleted because of a parent that is still active
     * @since 6.4.0
     */
    long deleteArchivedProcessInstancesInAllStates(List<Long> sourceProcessInstanceIds) throws DeletionException;

    /**
     * Delete all archived process instance (different states) corresponding to the source identifier.
     * Passing {@link Integer#MAX_VALUE} identifiers is discouraged as the amount of operations may be large and may thus result in timeout operation.
     *
     * @param sourceProcessInstanceId
     *        Identifier corresponding to {@link ArchivedProcessInstance#getSourceObjectId()}.
     * @return The number of {@link ArchivedProcessInstance}s that have been deleted in any state. For example, process instance can be archived is several
     *         states: Cancelled, Aborted, Completed, Failed
     * @throws DeletionException
     *         If a process instance can't be deleted because of a parent that is still active
     * @since 6.4.0
     */
    long deleteArchivedProcessInstancesInAllStates(long sourceProcessInstanceId) throws DeletionException;

    /**
     * Start an instance of the process with the specified process definition, using the current session user.
     *
     * @param processDefinitionId
     *        The identifier of the process definition for which an instance will be started.
     * @return An instance of the process.
     * @throws ProcessDefinitionNotFoundException
     *         If no matching process definition is found.
     * @throws ProcessActivationException
     *         If an exception occurs during activation.
     * @throws ProcessExecutionException
     *         If a problem occurs when starting the process.
     * @since 6.0
     */
    ProcessInstance startProcess(long processDefinitionId) throws ProcessDefinitionNotFoundException, ProcessActivationException, ProcessExecutionException;

    /**
     * Instantiates a process.
     * <br>
     * The process variables will be initialized by the initialVariables.
     *
     * @param processDefinitionId
     *        The identifier of the processDefinition
     * @param initialVariables
     *        The couples of initial variable/value
     * @return A ProcessInstance object
     * @throws ProcessDefinitionNotFoundException
     *         If The identifier of process definition does not refer to any existing process definition
     * @throws ProcessExecutionException
     *         If the process fails to start
     * @throws ProcessActivationException
     *         If the process is disable
     * @since 6.1
     */
    ProcessInstance startProcess(long processDefinitionId, Map<String, Serializable> initialVariables) throws ProcessDefinitionNotFoundException,
            ProcessActivationException, ProcessExecutionException;

    /**
     * Start an instance of the process with the specified process definition id, and set the initial values of the data with the given operations.
     *
     * @param processDefinitionId
     *        The identifier of the process definition for which an instance will be started.
     * @param operations
     *        The operations to execute to set the initial values of the data.
     * @param context
     *        The context in which operations are executed.
     * @return An instance of the process.
     * @throws ProcessDefinitionNotFoundException
     *         If no matching process definition is found.
     * @throws ProcessActivationException
     *         If an exception occurs during activation.
     * @throws ProcessExecutionException
     *         If a problem occurs when starting the process.
     * @since 6.0
     */
    ProcessInstance startProcess(long processDefinitionId, List<Operation> operations, Map<String, Serializable> context)
            throws ProcessDefinitionNotFoundException, ProcessActivationException, ProcessExecutionException;

    /**
     * Start an instance of the process with the specified process definition id on behalf of a given user.
     *
     * @param userId
     *        The user id of the user.
     * @param processDefinitionId
     *        The identifier of the process definition for which an instance will be started.
     * @return An instance of the process.
     * @throws UserNotFoundException
     *         If the given user does not exist.
     * @throws ProcessDefinitionNotFoundException
     *         If no matching process definition is found.
     * @throws ProcessActivationException
     *         If a problem occurs when starting the process.
     * @throws ProcessExecutionException
     *         If an execution problem occurs when starting the process.
     * @since 6.0
     */
    ProcessInstance startProcess(long userId, long processDefinitionId) throws UserNotFoundException, ProcessDefinitionNotFoundException,
            ProcessActivationException, ProcessExecutionException;

    /**
     * Start an instance of the process with the specified process definition id on behalf of a given user, and set the initial values of the data with the
     * given operations.
     *
     * @param userId
     *        The identifier of the user.
     * @param processDefinitionId
     *        The identifier of the process definition for which an instance will be started.
     * @param operations
     *        The operations to execute to set the initial values of the data.
     * @param context
     *        The context in which the operations are executed.
     * @return An instance of the process.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws ProcessDefinitionNotFoundException
     *         If no matching process definition is found.
     * @throws ProcessActivationException
     *         If an exception occurs during activation.
     * @throws UserNotFoundException
     *         If there is no user with the specified userId.
     * @throws ProcessExecutionException
     *         If a problem occurs when starting the process.
     * @since 6.0
     */
    ProcessInstance startProcess(long userId, long processDefinitionId, List<Operation> operations, Map<String, Serializable> context)
            throws UserNotFoundException, ProcessDefinitionNotFoundException, ProcessActivationException, ProcessExecutionException;

    /**
     * Start an instance of the process with the specified process definition id on behalf of a given user, and set the initial values of the data with the
     * given initialVariables.
     *
     * @param userId
     *        The identifier of the user.
     * @param processDefinitionId
     *        The identifier of the process definition for which an instance will be started.
     * @param initialVariables
     *        The couples of initial variable/value
     * @return An instance of the process.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws ProcessDefinitionNotFoundException
     *         If no matching process definition is found.
     * @throws ProcessActivationException
     *         If an exception occurs during activation.
     * @throws ProcessExecutionException
     *         If a problem occurs when starting the process.
     * @since 6.0
     */
    ProcessInstance startProcess(final long userId, final long processDefinitionId, final Map<String, Serializable> initialVariables)
            throws ProcessDefinitionNotFoundException, ProcessActivationException, ProcessExecutionException;

    /**
     * Start an instance of the process with the specified process definition id, and provides inputs to fullfill Process Contract.
     * See {@link org.bonitasoft.engine.bpm.contract.ContractDefinition} for details on contracts.
     *
     * @param processDefinitionId
     *        The identifier of the process definition for which an instance will be started.
     * @param instantiationInputs
     *        The couples of input name/value that allows to start a process with an instantiation contract.
     * @return An instance of the process.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws ProcessDefinitionNotFoundException
     *         If no matching process definition is found.
     * @throws ProcessActivationException
     *         If an exception occurs during activation.
     * @throws ProcessExecutionException
     *         If a problem occurs when starting the process.
     * @throws ContractViolationException
     *         If inputs don't fit with task contract
     * @since 7.0.0
     */
    ProcessInstance startProcessWithInputs(final long processDefinitionId, final Map<String, Serializable> instantiationInputs)
            throws ProcessDefinitionNotFoundException, ProcessActivationException, ProcessExecutionException, ContractViolationException;

    /**
     * Start an instance of the process with the specified process definition id on behalf of a given user, and provides inputs to fullfill Process Contract.
     * See {@link org.bonitasoft.engine.bpm.contract.ContractDefinition} for details on contracts.
     *
     * @param userId The identifier of the user in the name of whom the process is started.
     * @param processDefinitionId
     *        The identifier of the process definition for which an instance will be started.
     * @param instantiationInputs
     *        The couples of input name/value that allows to start a process with an instantiation contract.
     * @return An instance of the process.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws ProcessDefinitionNotFoundException
     *         If no matching process definition is found.
     * @throws ProcessActivationException
     *         If an exception occurs during activation.
     * @throws ProcessExecutionException
     *         If a problem occurs when starting the process.
     * @throws ContractViolationException
     *         If inputs don't fit with process contract
     * @since 7.0.0
     */
    ProcessInstance startProcessWithInputs(final long userId, final long processDefinitionId, final Map<String, Serializable> instantiationInputs)
            throws ProcessDefinitionNotFoundException, ProcessActivationException, ProcessExecutionException, ContractViolationException;

    /**
     * Executes a flow node that is in a stable state.
     * Will move the activity to the next stable state and then continue the execution of the process.
     *
     * @param flownodeInstanceId
     *        The identifier of the flow node to execute.
     * @throws FlowNodeExecutionException
     *         If an execution exception occurs.
     * @since 6.0
     */
    void executeFlowNode(long flownodeInstanceId) throws FlowNodeExecutionException;

    /**
     * Executes a flow node that is in a stable state on behalf of a given user
     * Will make the flow node go in the next stable state and then continue the execution of the process
     * If userId equals 0, the logged-in user is declared as the executer of the flow node.
     * The user, who executed the flow node on behalf of a given user, is declared as a executer delegate.
     *
     * @param userId
     *        The identifier of the user for which you want to execute the flow node
     * @param flownodeInstanceId
     *        The identifier of the flow node to execute
     * @throws FlowNodeExecutionException
     *         If an execution exception occurs
     * @since 6.0.1
     */
    void executeFlowNode(long userId, long flownodeInstanceId) throws FlowNodeExecutionException;

    /**
     * Returns all activities (active and finished) of a process instance.
     *
     * @param processInstanceId
     *        The identifier of the process instance,
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of results to get.
     * @return The matching set of activity instances.
     * @since 6.0
     */
    List<ActivityInstance> getActivities(long processInstanceId, int startIndex, int maxResults);

    /**
     * Get the specified process instance.
     *
     * @param processInstanceId
     *        The identifier of the process instance.
     * @return The matching instance of process.
     * @throws ProcessInstanceNotFoundException
     *         If there is no process instance with the specified identifier.
     * @since 6.0
     */
    ProcessInstance getProcessInstance(long processInstanceId) throws ProcessInstanceNotFoundException;

    /**
     * Get the specified activity instance.
     *
     * @param activityInstanceId
     *        The identifier of the activity instance.
     * @return The matching activity instance.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws ActivityInstanceNotFoundException
     *         If the activity cannot be found.
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If the activity instance cannot be retrieved.
     * @since 6.0
     */
    ActivityInstance getActivityInstance(long activityInstanceId) throws ActivityInstanceNotFoundException;

    /**
     * Get a specified flow node instance.
     *
     * @param flowNodeInstanceId
     *        The identifier of the flow node instance.
     * @return The matching flow node instance.
     * @throws FlowNodeInstanceNotFoundException
     *         If the given flow node instance does not exist.
     * @since 6.0
     */
    FlowNodeInstance getFlowNodeInstance(final long flowNodeInstanceId) throws FlowNodeInstanceNotFoundException;

    /**
     * Get an activity instance that is archived.
     *
     * @param activityInstanceId
     *        The identifier of the activity instance.
     * @return The matching archived activity instance.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws ActivityInstanceNotFoundException
     *         If the archived activity instance cannot be found.
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If the archived activity instance cannot be retrieved.
     * @since 6.0
     */
    ArchivedActivityInstance getArchivedActivityInstance(long activityInstanceId) throws ActivityInstanceNotFoundException;

    /**
     * Get the list of human task instances assigned to the specified user.
     *
     * @param userId
     *        The identifier of the user.
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of elements to get per page.
     * @param criterion
     *        The sort criterion.
     * @return The matching list of task instances.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Occurs when the session is invalid.
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If a task instance cannot be retrieved.
     * @since 6.0
     */
    List<HumanTaskInstance> getAssignedHumanTaskInstances(long userId, int startIndex, int maxResults, ActivityInstanceCriterion criterion);

    /**
     * Get the list of pending human task instances available to the specified user.
     * A human task is pending for a given user if it is not yet assigned and if the
     * user is a candidate either through an {@link org.bonitasoft.engine.bpm.actor.ActorMember} or through a {@link org.bonitasoft.engine.filter.UserFilter}.
     *
     * @param userId
     *        The identifier of the user.
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of elements to get per page.
     * @param pagingCriterion
     *        The criterion for sorting the items over pages.
     * @return The list of matching task instances.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Occurs when the session is invalid.
     * @since 6.0
     */
    List<HumanTaskInstance> getPendingHumanTaskInstances(long userId, int startIndex, int maxResults, ActivityInstanceCriterion pagingCriterion);

    /**
     * Count the total number of human task instances assigned to the specified user.
     *
     * @param userId
     *        The identifier of a user.
     * @return A number of human task instances assigned.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an error occurs while retrieving an instance of an activity.
     * @since 6.0
     */
    long getNumberOfAssignedHumanTaskInstances(long userId);

    /**
     * For a specified list of users, get the number of pending tasks.
     *
     * @param userIds
     *        A list of user identifiers.
     * @return A map with userId as key and number of tasks as value.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         can't retrieve an instance of activity
     * @since 6.0
     */
    Map<Long, Long> getNumberOfOpenTasks(List<Long> userIds);

    /**
     * Count the number of pending human task instances available to a specified user.
     *
     * @param userId
     *        The identifier of a user.
     * @return A number of pending human task instances.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an error occurs while retrieving an instance of an activity.
     * @since 6.0
     */
    long getNumberOfPendingHumanTaskInstances(long userId);

    /**
     * Retrieve a human task instance by the corresponding activity instance id.
     *
     * @param activityInstanceId
     *        The identifier of the activity instance.
     * @return The matching instance of human task.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws ActivityInstanceNotFoundException
     *         If the human task cannot be found.
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an error occurs while retrieving the instance of the activity.
     * @since 6.0
     */
    HumanTaskInstance getHumanTaskInstance(long activityInstanceId) throws ActivityInstanceNotFoundException;

    /**
     * Get a list of event instances related to a process instance that match the specified conditions.
     *
     * @param rootContainerId
     *        The identifier of the containing root process instance.
     * @param startIndex
     *        The index of the first result (starting from 0).
     * @param maxResults
     *        The maximum number of results to get.
     * @param sortingType
     *        The criterion for sorting event instances.
     * @return The matching list of event instances.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    List<EventInstance> getEventInstances(long rootContainerId, int startIndex, int maxResults, EventCriterion sortingType);

    /**
     * Assign a task to a user with given user identifier.
     *
     * @param userTaskId
     *        The identifier of the user task.
     * @param userId
     *        The identifier of the user.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws UpdateException
     *         If an error occurs while updating the activity instance.
     * @since 6.0
     */
    void assignUserTask(long userTaskId, long userId) throws UpdateException;

    /**
     * Updates the actors of the user task. It evaluates again the eligible users for that task.
     *
     * @param userTaskId
     *        The identifier of the user task
     * @throws UpdateException
     *         If an exception occurs during the evaluation of actors.
     * @since 6.1
     */
    void updateActorsOfUserTask(long userTaskId) throws UpdateException;

    /**
     * Returns all data of a process instance.
     *
     * @param processInstanceId
     *        The identifier of the process instance.
     * @param startIndex
     *        The index of the page of results to get (starting from 0).
     * @param maxResults
     *        The maximum number of results to get.
     * @return The matching list of dataInstances.
     * @since 6.0
     */
    List<DataInstance> getProcessDataInstances(long processInstanceId, int startIndex, int maxResults);

    /**
     * Get the value of named data item from a specified process instance.
     * The value is returned in a DataInstance object.
     *
     * @param dataName
     *        The name of the data item.
     * @param processInstanceId
     *        The identifier of the process instance.
     * @return An instance of the data
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws DataNotFoundException
     *         If the specified data value cannot be found.
     * @since 6.0
     */
    DataInstance getProcessDataInstance(String dataName, long processInstanceId) throws DataNotFoundException;

    /**
     * Update the value of a named data item in a specified process instance.
     *
     * @param dataName
     *        The name of the data item.
     * @param processInstanceId
     *        The identifier of the process instance.
     * @param dataValue
     *        The new value for the data item.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws UpdateException
     *         If a problem occurs while updating the data value.
     * @since 6.0
     */
    void updateProcessDataInstance(String dataName, long processInstanceId, Serializable dataValue) throws UpdateException;

    /**
     * Update the value of a named data item in a specified process instance.
     *
     * @param processInstanceId
     *        The identifier of the process instance.
     * @param dataNameValues
     *        The mapping between the data name and its value to update to.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws UpdateException
     *         If a problem occurs while updating the data value.
     * @since 6.2.3
     */
    void updateProcessDataInstances(final long processInstanceId, final Map<String, Serializable> dataNameValues) throws UpdateException;

    /**
     * Get a list of the data instances from a specified activity instance.
     *
     * @param activityInstanceId
     *        The identifier of the activity instance.
     * @param startIndex
     *        The index of the first result (starting at 0).
     * @param maxResults
     *        The maximum number of results to get.
     * @return The list of matching DataInstances.
     * @since 6.0
     */
    List<DataInstance> getActivityDataInstances(long activityInstanceId, int startIndex, int maxResults);

    /**
     * Get a named data instance from a specified activity instance.
     *
     * @param dataName
     *        The name of the data item.
     * @param activityInstanceId
     *        The identifier of the activity instance.
     * @return An instance of data.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws DataNotFoundException
     *         If the specified data value cannot be found.
     * @since 6.0
     */
    DataInstance getActivityDataInstance(String dataName, long activityInstanceId) throws DataNotFoundException;

    /**
     * Update the value of a named data instance in a specified activity instance.<br>
     * <br>
     * <b>WARNING</b>: this method is not supported for updating a Custom Data Instance variable with a remote Engine API connection,
     * because the custom data type is not present in the remote classloader that deserializes the API call parameters. <br>
     * use {@link ProcessRuntimeAPI#updateActivityInstanceVariables(List, long, Map)} instead
     *
     * @param dataName
     *        The name of the data instance.
     * @param activityInstanceId
     *        The identifier of the activity instance.
     * @param dataValue
     *        The new value of the data to set.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws UpdateException
     *         If an error occurs during the update.
     * @since 6.0
     */
    void updateActivityDataInstance(String dataName, long activityInstanceId, Serializable dataValue) throws UpdateException;

    /**
     * Update the value of a named transient data instance in a specified activity instance.
     *
     * @param dataName
     *        The name of the data instance.
     * @param activityInstanceId
     *        The identifier of the activity instance.
     * @param dataValue
     *        The new value of the data to set.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws UpdateException
     *         If an error occurs during the update.
     * @since 6.0
     */
    void updateActivityTransientDataInstance(String dataName, long activityInstanceId, Serializable dataValue) throws UpdateException;

    /**
     * Get a list of the transient data instances from a specified activity instance.
     *
     * @param activityInstanceId
     *        The identifier of the activity instance.
     * @param startIndex
     *        The index of the first result (starting at 0).
     * @param maxResults
     *        The maximum number of results to get.
     * @return The list of matching DataInstances.
     * @since 6.0
     */
    List<DataInstance> getActivityTransientDataInstances(long activityInstanceId, int startIndex, int maxResults);

    /**
     * Get a named transient data instance from a specified activity instance.
     *
     * @param dataName
     *        The name of the data item.
     * @param activityInstanceId
     *        The identifier of the activity instance.
     * @return An instance of data.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws DataNotFoundException
     *         If the specified data value cannot be found.
     * @since 6.0
     */
    DataInstance getActivityTransientDataInstance(String dataName, long activityInstanceId) throws DataNotFoundException;

    /**
     * Get the date when the specified activity instance reached the given state.
     *
     * @param activityInstanceId
     *        The identifier of the activity instance.
     * @param state
     *        The state of interest.
     * @return The date at which the activity instance reached the state.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an error occurs while retrieving the activity instance.
     * @since 6.0
     */
    Date getActivityReachedStateDate(long activityInstanceId, String state);

    /**
     * Update the given variables of an activity instance.
     * The updates are treated as a single transaction, so if any variable update fails, none of the values is changed.
     *
     * @param activityInstanceId
     *        The activity identifier.
     * @param variables
     *        A map which contains several pairs of variable name and value.
     * @throws UpdateException
     *         If a problem occurs while updating one of the data instance value.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    void updateActivityInstanceVariables(long activityInstanceId, Map<String, Serializable> variables) throws UpdateException;

    /**
     * Update the values of variables in an activity instance using expressions.
     *
     * @param operations
     *        A sequence of operations on expressions that update the values variables.
     * @param activityInstanceId
     *        The identifier of the activity instance.
     * @param expressionContexts
     *        Store all information identifying the container that the data belongs to.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws UpdateException
     *         If an error occurs during the update.
     * @since 6.0
     */
    void updateActivityInstanceVariables(List<Operation> operations, long activityInstanceId, Map<String, Serializable> expressionContexts)
            throws UpdateException;

    /**
     * Update the due date of a task.
     *
     * @param userTaskId
     *        The identifier of the task to update.
     * @param dueDate
     *        The new due date for the task.
     * @throws UpdateException
     *         If the activity does not exist or the update cannot be fulfilled.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    void updateDueDateOfTask(long userTaskId, Date dueDate) throws UpdateException;

    /**
     * Get an instance of a task asssigned to a given user for the specified process instance.
     *
     * @param processInstanceId
     *        The identifier of the process instance.
     * @param userId
     *        The identifier of the user.
     * @return The identifier of a user task from the process instance that is assigned to the user.
     * @throws ProcessInstanceNotFoundException
     *         If the given process instance does not exist.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws UserNotFoundException
     *         If there is no user with the specified id.
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an error occurs happen while retrieving the activity instance.
     * @since 6.0
     */
    long getOneAssignedUserTaskInstanceOfProcessInstance(long processInstanceId, long userId) throws ProcessInstanceNotFoundException, UserNotFoundException;

    /**
     * Get an instance of a task asssigned to a given user for the specified process definition.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @param userId
     *        The identifier of a user.
     * @return The identifier of a user task from the process definition that is assigned to the user.
     * @throws ProcessDefinitionNotFoundException
     *         If the given process definition does not exist.
     * @throws UserNotFoundException
     *         If the given user does not exist.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an error occurs happen while retrieving the activity instance.
     * @since 6.0
     */
    long getOneAssignedUserTaskInstanceOfProcessDefinition(long processDefinitionId, long userId) throws ProcessDefinitionNotFoundException,
            UserNotFoundException;

    /**
     * Get the state of a specified activity instance.
     *
     * @param activityInstanceId
     *        The identifier of the activity instance.
     * @return The state of the activity instance.
     * @throws ActivityInstanceNotFoundException
     *         If the activity cannot be found.
     * @since 6.0
     */
    String getActivityInstanceState(long activityInstanceId) throws ActivityInstanceNotFoundException;

    /**
     * Check whether a specified task can be executed by a given user.
     *
     * @param activityInstanceId
     *        The identifier of the activity instance.
     * @param userId
     *        The identifier of a user.
     * @return A flag that indicates whether task can be executed by the user.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws ActivityInstanceNotFoundException
     *         If the activity cannot be found.
     * @throws UserNotFoundException
     *         If there is no user with the specified userId.
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an error occurs happen while retrieving the activity instance.
     * @since 6.0
     */
    boolean canExecuteTask(long activityInstanceId, long userId) throws ActivityInstanceNotFoundException, UserNotFoundException;

    /**
     * Release a task (unclaim or unassign). After the operation, the task is in the pending task list.
     *
     * @param userTaskId
     *        The identifier of the user task.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws ActivityInstanceNotFoundException
     *         If the activity cannot be found.
     * @throws UpdateException
     *         If a problem occurs while release (un-assigning) the user task.
     * @since 6.0
     */
    void releaseUserTask(long userTaskId) throws ActivityInstanceNotFoundException, UpdateException;

    /**
     * List the archived process instances for the specified process instance.
     * A process instance is archived when it changes state, so there are several archived process instances for each process instance.
     *
     * @param processInstanceId
     *        The identifier of the process instance.
     * @param startIndex
     *        The index of the page of results to get.
     * @param maxResults
     *        The maximum number of results to get.
     * @return The list of archived process instances.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If no current valid session is found.
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If the search fails because an archived process instance cannot be read.
     * @since 6.0
     */
    List<ArchivedProcessInstance> getArchivedProcessInstances(long processInstanceId, int startIndex, int maxResults);

    /**
     * Get the last archived instance of a process instance.
     * A process instance is archived when it changes state, so there are several archived process instances for each process instance.
     * The last archived instance is returned.
     *
     * @param processInstanceId
     *        The identifier of the source process instance, i.e. not an archived version, the original process instance id.
     * @return The archived process instance.
     * @throws ArchivedProcessInstanceNotFoundException
     *         If no archived process instance can be found with the provided Id.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If no current valid session is found.
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If the search fails because an archived process instance cannot be read.
     * @since 6.0
     */
    ArchivedProcessInstance getFinalArchivedProcessInstance(long processInstanceId) throws ArchivedProcessInstanceNotFoundException;

    /**
     * Set the state of an activity instance.
     *
     * @param activityInstanceId
     *        The identifier of the activity instance.
     * @param stateId
     *        The identifier of the required state.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If no current valid session is found.
     * @throws UpdateException
     *         If an error occurs during the update.
     * @since 6.0
     */
    void setActivityStateById(long activityInstanceId, int stateId) throws UpdateException;

    /**
     * Set the state of an activity instance.
     *
     * @param activityInstanceId
     *        The identifier of the activity instance.
     * @param state
     *        The name of the required state.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If no current valid session is found.
     * @throws UpdateException
     *         If an error occurs during the update.
     * @since 6.0
     */
    void setActivityStateByName(long activityInstanceId, String state) throws UpdateException;

    /**
     * Set a state of a process instance.
     *
     * @param processInstance
     *        The process instance.
     * @param state
     *        The name of the required state.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws UpdateException
     *         If an error occurs during the update.
     * @since 6.0
     */
    void setProcessInstanceState(ProcessInstance processInstance, String state) throws UpdateException;

    /**
     * Set the priority of a user task.
     *
     * @param userTaskInstanceId
     *        The identifier of user task instance.
     * @param priority
     *        The new priority of this task.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws UpdateException
     *         If an error occurs during the update.
     * @since 6.0
     */
    void setTaskPriority(long userTaskInstanceId, TaskPriority priority) throws UpdateException;

    /**
     * Execute a connector in a specified processDefinition.
     *
     * @param connectorDefinitionId
     *        The identifier of connector definition.
     * @param connectorDefinitionVersion
     *        The version of the connector definition.
     * @param connectorInputParameters
     *        The expressions related to the connector input paramters.
     * @param inputValues
     *        The parameters values for expression needed when evaluating the connector.
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @return A map with connector parameter names and parameter value objects.
     * @throws ConnectorExecutionException
     *         If an error occurs during connector execution.
     * @throws ConnectorNotFoundException
     *         If there is no connector definition with the specified identifier or version.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnProcessDefinition(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long processDefinitionId)
            throws ConnectorExecutionException, ConnectorNotFoundException;

    /**
     * Execute a connector in a specified processDefinition with operations.
     *
     * @param connectorDefinitionId
     *        The identifier of connector definition.
     * @param connectorDefinitionVersion
     *        The version of the connector definition.
     * @param connectorInputParameters
     *        The expressions related to the connector input parameters.
     * @param inputValues
     *        The parameters values for expression needed when evaluating the connector.
     * @param operations
     *        The operations used when executing the connector.
     * @param operationInputValues
     *        The input values for the operations.
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @return A map with connector parameter names and parameter value objects after operations and connector execution.
     * @throws ConnectorExecutionException
     *         If an error occurs during connector execution.
     * @throws ConnectorNotFoundException
     *         If there is no connector definition with the specified identifier or version.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnProcessDefinition(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, List<Operation> operations,
            Map<String, Serializable> operationInputValues, long processDefinitionId) throws ConnectorExecutionException, ConnectorNotFoundException;

    /**
     * Search the archived human tasks for tasks that match the search options.
     *
     * @param searchOptions
     *        The search conditions and the options for sorting and paging the results. See
     *        {@link org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor} for valid
     *        fields
     *        for searching and sorting.
     * @return The archived human tasks that match the search conditions.
     * @throws SearchException
     *         If the search could not be completed correctly.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    SearchResult<ArchivedHumanTaskInstance> searchArchivedHumanTasks(SearchOptions searchOptions) throws SearchException;

    /**
     * Search the assigned human tasks for tasks that match the search options and are administered by the specified user.
     *
     * @param managerUserId
     *        The identifier of the user.
     * @param searchOptions
     *        The search conditions and the options for sorting and paging the results. See
     *        {@link org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor} for valid fields for searching and sorting.
     * @return The assigned human tasks that match the search conditions and are supervised by the user.
     * @throws SearchException
     *         If there is an error in the search conditions.
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchAssignedTasksManagedBy(long managerUserId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search the pending human tasks for tasks that match the search options and are supervised by the specified user.
     *
     * @param userId
     *        The identifier of the user.
     * @param searchOptions
     *        The search conditions and the options for sorting and paging the results. See
     *        {@link org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor} for valid fields for searching and sorting.
     * @return The pending human tasks that match the search conditions and are supervised by the user.
     * @throws SearchException
     *         If there is an error in the search conditions.
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchPendingTasksSupervisedBy(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search the pending human tasks for tasks available to the specified user.
     *
     * @param userId
     *        The identifier of the user.
     * @param searchOptions
     *        The search conditions and the options for sorting and paging the results. See
     *        {@link org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor} for valid fields for searching and sorting.
     * @return The pending human tasks that match the search conditions and are available to the user.
     * @throws SearchException
     *         If there is an error in the search conditions.
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchPendingTasksForUser(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search the pending human tasks for tasks that match the search options and are managed by the specified user.
     *
     * @param managerUserId
     *        The identifier of the user.
     * @param searchOptions
     *        The search conditions and the options for sorting and paging the results. See
     *        {@link org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor} for valid fields for searching and sorting.
     * @return The pending human tasks that match the search conditions and are managed by the user.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *         If there is an error in the search conditions.
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchPendingTasksManagedBy(long managerUserId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search the assigned and pending human tasks for the specified user, on the specified root process definition, corresponding to the options.
     *
     * @param rootProcessDefinitionId
     *        The identifier of the root process definition
     * @param userId
     *        The identifier of the user
     * @param searchOptions
     *        The search conditions and the options for sorting and paging the results. See
     *        {@link org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor} for valid fields for searching and sorting.
     * @return The assigned and pending human tasks
     * @throws SearchException
     *         If there is an error in the search conditions.
     * @since 6.3.3
     */
    SearchResult<HumanTaskInstance> searchAssignedAndPendingHumanTasksFor(final long rootProcessDefinitionId, final long userId,
            final SearchOptions searchOptions) throws SearchException;

    /**
     * Search the assigned and pending human tasks for any user, on the specified root process definition, corresponding to the options.
     *
     * @param rootProcessDefinitionId
     *        The identifier of the root process definition
     * @param searchOptions
     *        The search conditions and the options for sorting and paging the results. See
     *        {@link org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor} for valid fields for searching and sorting.
     * @return The assigned and pending human tasks
     * @throws SearchException
     *         If there is an error in the search conditions.
     * @since 6.3.3
     */
    SearchResult<HumanTaskInstance> searchAssignedAndPendingHumanTasks(final long rootProcessDefinitionId, final SearchOptions searchOptions)
            throws SearchException;

    /**
     * Get the number of assigned and pending overdue tasks for the specified users.
     *
     * @param userIds
     *        A list of user identifiers.
     * @return A map of user identifiers and numbers of overdue tasks.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Map<Long, Long> getNumberOfOverdueOpenTasks(List<Long> userIds);

    /**
     * Cancels the process instance and all of its active flow nodes.
     *
     * @param processInstanceId
     *        The identifier of the process instance.
     * @throws ProcessInstanceNotFoundException
     *         If the process instance identifier does not refer to a process instance.
     * @throws UpdateException
     *         If an exception occurs during the process instance canceling.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    void cancelProcessInstance(long processInstanceId) throws ProcessInstanceNotFoundException, UpdateException;

    /**
     * Reset the state of a failed {@link org.bonitasoft.engine.bpm.flownode.FlowNodeInstance} to its previous state and then execute it. Pre-condition: the
     * {@code FlowNodeInstance} must be in state FAILED. If this condition is not respected, a ActivityExecutionException is thrown.
     * <p>If the {@code FlowNodeInstance} contains failed {@link org.bonitasoft.engine.bpm.connector.ConnectorInstance}s, they will be re-executed. In the case
     * where the connector execution fails again, the {@code FlowNodeInstance} will remain in failed state. There is not counter on the number of
     * re-executions</p>
     *
     * @param activityInstanceId
     *        The identifier of the {@code FlowNodeInstance} to be re-retried.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         when session is invalid, e.g. the session has expired.
     * @throws ActivityInstanceNotFoundException
     *         when no {@code FlowNodeInstance} is found with the specified identifier.
     * @throws ActivityExecutionException
     *         occurs if the current Flownode is not in FAILED state, or while resetting the state, or while executing the {@code FlowNodeInstance}.
     * @since 6.0
     * @see org.bonitasoft.engine.bpm.flownode.FlowNodeInstance
     * @see org.bonitasoft.engine.bpm.connector.ConnectorInstance
     */
    void retryTask(long activityInstanceId) throws ActivityInstanceNotFoundException, ActivityExecutionException;

    /**
     * Evaluate an expression in the context of the specified process.
     * Some context values can also be provided
     *
     * @param expression
     *        The expression to evaluate.
     * @param context
     *        The context values that are provided for evaluating the expression.
     * @param processDefinitionId
     *        The identifier of the process definition in which the expression is evaluated.
     * @return The result of the evaluation.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If there is no current valid session.
     * @throws ExpressionEvaluationException
     *         If an error occurs while evaluating the expression.
     * @since 6.0
     */
    Serializable evaluateExpressionOnProcessDefinition(Expression expression, Map<String, Serializable> context, long processDefinitionId)
            throws ExpressionEvaluationException;

    /**
     * Get the number of comments matching the search conditions.
     *
     * @param searchOptions
     *        The search conditions and the options for sorting and paging the results.
     * @return The number of comments matching the search conditions.
     * @throws SearchException
     *         If the search could not be completed correctly.
     * @since 6.0
     */
    long countComments(SearchOptions searchOptions) throws SearchException;

    /**
     * Get the number of attachments matching the search conditions.
     *
     * @param searchOptions
     *        The search conditions and the options for sorting and paging the results.
     * @return The number of attachments matching the search conditions.
     * @throws SearchException
     *         If the search could not be completed correctly.
     * @since 6.0
     */

    long countAttachments(SearchOptions searchOptions) throws SearchException;

    /**
     * Send a BPMN signal event. Invoking this method acts as executing a Throw Signal Event.
     *
     * @param signalName
     *        The signal name.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If there is no current valid session.
     * @throws SendEventException
     *         If an exception occurs while sending signal.
     * @since 6.0
     */
    void sendSignal(String signalName) throws SendEventException;

    /**
     * Send a BPMN message event. Invoking this method acts as executing a Throw Message Event.
     *
     * @param messageName
     *        The message name.
     * @param targetProcess
     *        An expression representing the target process name.
     * @param targetFlowNode
     *        An expression representing the target flow node name.
     * @param messageContent
     *        A key-&gt;value map containing the message data, with the data name as key.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If there is no current valid session.
     * @throws SendEventException
     *         If an exception occurs while sending message.
     * @since 6.0
     */
    void sendMessage(String messageName, Expression targetProcess, Expression targetFlowNode, Map<Expression, Expression> messageContent)
            throws SendEventException;

    /**
     * Send a BPMN message event, with message correlation. Invoking this method acts as executing a Throw Message Event.
     *
     * @param messageName
     *        The message name.
     * @param targetProcess
     *        An expression representing the target process name.
     * @param targetFlowNode
     *        An expression representing the target flow node name.
     * @param messageContent
     *        A key-&gt;value map containing the message data, with the data name as key.
     * @param correlations
     *        The message correlations (five maximum).
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If there is no current valid session.
     * @throws SendEventException
     *         If there are too many correlations (more than 5) or an exception occurs while sending message.
     * @since 6.0
     */
    void sendMessage(String messageName, Expression targetProcess, Expression targetFlowNode, Map<Expression, Expression> messageContent,
            Map<Expression, Expression> correlations) throws SendEventException;

    /**
     * Retrieve an <code>ArchivedProcessInstance</code> specified by its identifier.
     *
     * @param archivedProcessInstanceId
     *        The identifier of the <code>ArchivedProcessInstance</code> to be retrieved.
     * @return The <code>ArchivedProcessInstance</code> instance.
     * @throws ArchivedProcessInstanceNotFoundException
     *         If the <code>ArchivedProcessInstance</code> was not found.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an error occurs while trying to retrieve the <code>ArchivedProcessInstance</code>.
     * @since 6.0
     */
    ArchivedProcessInstance getArchivedProcessInstance(long archivedProcessInstanceId) throws ArchivedProcessInstanceNotFoundException;

    /**
     * Retrieve an <code>ArchivedFlowNodeInstance</code> specified by its identifier.
     *
     * @param archivedFlowNodeInstanceId
     *        The identifier of the <code>ArchivedFlowNodeInstance</code> to be retrieved.
     * @return The <code>ArchivedFlowNodeInstance</code> instance.
     * @throws ArchivedFlowNodeInstanceNotFoundException
     *         If the <code>ArchivedFlowNodeInstance</code> was not found.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an error occurs while trying to retrieve the <code>ArchivedFlowNodeInstance</code>.
     * @since 6.0
     */
    ArchivedFlowNodeInstance getArchivedFlowNodeInstance(long archivedFlowNodeInstanceId) throws ArchivedFlowNodeInstanceNotFoundException;

    /**
     * Retrieve an <code>ArchivedComment</code> specified by its identifier.
     *
     * @param archivedCommentId
     *        The identifier of the <code>ArchivedComment</code> to be retrieved.
     * @return The <code>ArchivedComment</code> instance.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an error occurs while trying to retrieve the <code>ArchivedComment</code>.
     * @throws NotFoundException
     *         If no <code>ArchivedComment</code> was found with the specified archivedCommentId.
     * @since 6.0
     */
    ArchivedComment getArchivedComment(long archivedCommentId) throws NotFoundException;

    /**
     * Search for connector instances.
     *
     * @param searchOptions
     *        The search conditions and the options for sorting and paging the results. See
     *        {@link org.bonitasoft.engine.bpm.connector.ConnectorInstancesSearchDescriptor} for valid fields for searching and sorting.
     * @return The {@link SearchResult} containing the <code>ConnectorInstance</code>s matching the search options.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *         If the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<ConnectorInstance> searchConnectorInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for archived connector instances.
     *
     * @param searchOptions
     *        The search options parameters. See {@link org.bonitasoft.engine.bpm.connector.ArchiveConnectorInstancesSearchDescriptor} for valid fields for
     *        searching and sorting.
     * @return The {@link SearchResult} containing the <code>ArchivedConnectorInstance</code>s matching the search options.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *         If the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<ArchivedConnectorInstance> searchArchivedConnectorInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * List the named human tasks belonging to the specified process instance.
     *
     * @param processInstanceId
     *        The identifier of the process instance.
     * @param taskName
     *        The name of the required human tasks.
     * @param startIndex
     *        The result start index (strating from 0).
     * @param maxResults
     *        The maximum number of results to retrieve.
     * @return The list of matching human task instances.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    List<HumanTaskInstance> getHumanTaskInstances(long processInstanceId, String taskName, int startIndex, int maxResults);

    /**
     * Return the last created human task instance with the specified name for the given process instance.
     *
     * @param processInstanceId
     *        The identifier of the process instance.
     * @param taskName
     *        The name of the required human task.
     * @return A HumanTaskInstance, in its latest state.
     * @throws NotFoundException
     *         If no current task with provided name is found.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    HumanTaskInstance getLastStateHumanTaskInstance(long processInstanceId, String taskName) throws NotFoundException;

    /**
     * Search for archived activity instances in terminal states. Archived activity instances in intermediate states are not considered.
     *
     * @param searchOptions
     *        The criterion used to search for archived activity instances. See
     *        {@link org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor} for valid fields for searching and sorting.
     * @return A {@link SearchResult} containing the search result.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *         If an exception occurs during the search.
     * @since 6.0
     */
    SearchResult<ArchivedActivityInstance> searchArchivedActivities(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for activity instances.
     *
     * @param searchOptions
     *        The criterion used to search for activity instances. See {@link org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor} for valid
     *        fields for searching and sorting.
     * @return A {@link SearchResult} containing the search result.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *         If the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<ActivityInstance> searchActivities(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for flow node instances (activities, gateways and events).
     *
     * @param searchOptions
     *        The criterion used to search for flow node instances. See {@link org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceSearchDescriptor} for valid
     *        fields for searching and sorting.
     * @return A {@link SearchResult} containing the search result
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the ession is invalid, e.g session has expired.
     * @throws SearchException
     *         If an exception occurs during the search.
     * @since 6.0
     */
    SearchResult<FlowNodeInstance> searchFlowNodeInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for archived flow node instances (activities, gateways and events)
     *
     * @param searchOptions
     *        The options used to search for flow node instances. See {@link org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceSearchDescriptor} for
     *        valid fields for searching and sorting.
     * @return A {@link SearchResult} containing the search result.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g session has expired.
     * @throws SearchException
     *         If an exception occurs during the search.
     * @see ArchivedFlowNodeInstance
     * @since 6.0
     */
    SearchResult<ArchivedFlowNodeInstance> searchArchivedFlowNodeInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for all tasks available to a specified user.
     * A task is available to a user if is assigned to the user or it is pending for that user.
     *
     * @param userId
     *        The identifier of the user for whom the tasks are available.
     * @param searchOptions
     *        The options used to search for tasks. See {@link org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor} for valid fields for
     *        searching and sorting.
     * @return The list of tasks matching the search options.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the current session is invalid.
     * @throws SearchException
     *         If an exception occurs during the search.
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchMyAvailableHumanTasks(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search for comments related to the specified process instance.
     *
     * @param searchOptions
     *        The options used to search for comments. See {@link org.bonitasoft.engine.bpm.comment.SearchCommentsDescriptor} for valid fields for searching
     *        and sorting.
     * @return The matching comments.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *         If an exception occurs during the search.
     * @since 6.0
     */
    SearchResult<Comment> searchComments(SearchOptions searchOptions) throws SearchException;

    /**
     * Add a comment on a process instance.
     *
     * @param processInstanceId
     *        The identifier of the process instance.
     * @param comment
     *        The content of the comment.
     * @return The newly created comment.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @deprecated use {@link #addProcessComment(long, String)} instead, that can throw CreationException is case of inexistant Process Instance
     * @since 6.0
     */
    @Deprecated
    Comment addComment(long processInstanceId, String comment);

    /**
     * Add a comment on a process instance.
     *
     * @param processInstanceId
     *        The identifier of the process instance.
     * @param comment
     *        The content of the comment.
     * @return The newly created comment.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws CreationException
     *         If the parameter processInstanceId does not refer to any active process instance (existing and non-archived).
     * @since 6.1
     */
    Comment addProcessComment(final long processInstanceId, final String comment) throws CreationException;

    /**
     * Get the first 20 comments of the specified process instance.
     *
     * @param processInstanceId
     *        The identifier of the process instance.
     * @return The list of comments found
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
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
     *        The identifier of the user.
     * @param searchOptions
     *        The options used to search for comments. See {@link org.bonitasoft.engine.bpm.comment.SearchCommentsDescriptor} for valid fields for searching and
     *        sorting.
     * @return The comments managed by the user that match the search options.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *         If the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<Comment> searchCommentsManagedBy(long managerUserId, SearchOptions searchOptions) throws SearchException;

    /**
     * Get the comments on process instances that the specified user can access.
     *
     * @param userId
     *        The identifier of the user.
     * @param searchOptions
     *        The options used to search for comments. See {@link org.bonitasoft.engine.bpm.comment.SearchCommentsDescriptor} for valid fields for searching and
     *        sorting.
     * @return The comments on process instances that the user can access.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *         If the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<Comment> searchCommentsInvolvingUser(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Get the children instances (sub process or call activity) of a process instance. The returned list is paginated.
     * It does not return the process instance of the given id (itself).
     *
     * @param processInstanceId
     *        The identifier of the process definition.
     * @param startIndex
     *        The index of the page to be returned (starting at 0).
     * @param maxResults
     *        The maximum number of results per page.
     * @param criterion
     *        The criterion used to sort the result.
     * @return The list of children instance identifiers.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    List<Long> getChildrenInstanceIdsOfProcessInstance(long processInstanceId, int startIndex, int maxResults, ProcessInstanceCriterion criterion);

    /**
     * Check whether a specific user is involved in a given process instance.<br/>
     * User A is involved with a process instance if any of the following is true:
     * <ul>
     * <li>user A has started the process instance</li>
     * <li>a task in the process instance is assigned to user A</li>
     * <li>a task in the process instance is pending for user A</li>
     * <li>a task in the process instance has been performed by user A</li>
     * </ul>
     * This method also applies to completed instances of process.
     *
     * @param userId
     *        The identifier of the user.
     * @param processInstanceId
     *        The identifier of the process instance.
     * @return True if the user is involved with the process instance.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws ProcessInstanceNotFoundException
     *         If there is no processInstance with the specified identifier.
     * @throws UserNotFoundException
     *         If there is no user with the specified identifier.
     * @since 6.0
     * @see #isManagerOfUserInvolvedInProcessInstance(long, long)
     */
    boolean isInvolvedInProcessInstance(long userId, long processInstanceId) throws ProcessInstanceNotFoundException, UserNotFoundException;

    /**
     * Check whether a specific user is involved in a given human task instance.<br/>
     * User A is involved with a human task instance if any of the following is true:
     * <ul>
     * <li>the human task instance is assigned to user A</li>
     * <li>the human task instance is pending for user A</li>
     * </ul>
     *
     * @param userId
     *        The identifier of the user.
     * @param humanTaskInstanceId
     *        The identifier of the human task instance.
     * @return True if the user is involved with the human task instance.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws ActivityInstanceNotFoundException
     *         If there is no the human task instance with the specified identifier.
     * @since 6.5.1
     * @see #isManagerOfUserInvolvedInProcessInstance(long, long)
     */
    boolean isInvolvedInHumanTaskInstance(long userId, long humanTaskInstanceId) throws ActivityInstanceNotFoundException, UserNotFoundException;

    /**
     * Check whether a specific user has at least one subordinate (person he / she is the manager of) involved in a given process instance.<br/>
     * User A is involved with a process instance if any of the following is true:
     * <ul>
     * <li>user A has started the process instance</li>
     * <li>a task in the process instance is assigned to user A</li>
     * <li>a task in the process instance is pending for user A</li>
     * <li>a task in the process instance has been performed by user A</li>
     * </ul>
     * This method also applies to completed instances of process.
     *
     * @param managerUserId the ID of the manager of the user involved.
     * @param processInstanceId the ID of the process instance we are interested in.
     * @return true if the specified manager has subordinates involved in the given process instance.
     * @throws ProcessInstanceNotFoundException if the process instance does not exist.
     * @throws BonitaException if an error occured while searching for users involved.
     * @since 6.4.2
     * @see #isInvolvedInProcessInstance(long, long)
     */
    boolean isManagerOfUserInvolvedInProcessInstance(long managerUserId, long processInstanceId) throws ProcessInstanceNotFoundException, BonitaException;

    /**
     * Get the process instance id from an activity instance id.
     *
     * @param activityInstanceId
     *        The identifier of the activity instance.
     * @return The corresponding process instance id.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws ProcessInstanceNotFoundException
     *         If there is no process instance with the specified identifier.
     * @since 6.0
     */
    long getProcessInstanceIdFromActivityInstanceId(long activityInstanceId) throws ProcessInstanceNotFoundException;

    /**
     * Get the process definition id from an process instance id.
     *
     * @param processInstanceId
     *        The identifier of the process instance.
     * @return The corresponding process definition id.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws ProcessDefinitionNotFoundException
     *         If there is no process definition with the specified identifier.
     * @since 6.0
     */
    long getProcessDefinitionIdFromProcessInstanceId(long processInstanceId) throws ProcessDefinitionNotFoundException;

    /**
     * Get the process definition id from an activity instance id.
     *
     * @param activityInstanceId
     *        The identifier of the activity instance.
     * @return The corresponding process definition id.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws ProcessDefinitionNotFoundException
     *         If no ProcessDefinition have an id corresponding to the parameter.
     * @since 6.0
     */
    long getProcessDefinitionIdFromActivityInstanceId(long activityInstanceId) throws ProcessDefinitionNotFoundException;

    /**
     * Search for archived comments.
     *
     * @param searchOptions
     *        The options used to search for comments. See {@link org.bonitasoft.engine.bpm.comment.ArchivedCommentsSearchDescriptor} for valid fields for
     *        searching and sorting.
     * @throws InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @return The <code>ArchivedComment</code> items that match the search options.
     * @throws SearchException
     *         If the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<ArchivedComment> searchArchivedComments(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for archived human tasks managed by the specified user.
     *
     * @param managerUserId
     *        The identifier of the user manager,
     * @param searchOptions
     *        The options used to search for tasks. See {@link org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstanceSearchDescriptor} for valid fields
     *        for searching and sorting.
     * @return The archived humanTask instances managed by the specified user that match the search options.
     * @throws SearchException
     *         If the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<ArchivedHumanTaskInstance> searchArchivedHumanTasksManagedBy(long managerUserId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search for open process instances that the specified user can access.
     *
     * @param userId
     *        The identifier of the user.
     * @param searchOptions
     *        The options used to search for process instance. See {@link org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor} for valid fields
     *        for searching and sorting.
     * @return The <code>ProcessInstance</code>s that match the search options.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *         If the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<ProcessInstance> searchOpenProcessInstancesInvolvingUser(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search for open process instances that all subordinates of the specified user can access.
     *
     * @param managerUserId
     *        The identifier of the user manager.
     * @param searchOptions
     *        The search options (pagination, filter, order sort). See {@link org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor} for valid
     *        fields for searching and sorting.
     * @return The <code>ProcessInstance</code>s that match the search options.
     * @throws SearchException
     *         If the search could not be completed correctly.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    SearchResult<ProcessInstance> searchOpenProcessInstancesInvolvingUsersManagedBy(long managerUserId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search for archived root process instances. Only archived process instances in states COMPLETED, ABORTED, CANCELED and FAILED will be retrieved.
     *
     * @param searchOptions
     *        The search options (pagination, filter, order sort).
     * @return The archived process instances that match the search options. See
     *         {@link org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor} for valid fields for searching and sorting.
     * @throws SearchException
     *         If the search could not be full filled correctly
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    SearchResult<ArchivedProcessInstance> searchArchivedProcessInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for archived process instances (root and intermediate levels) in all states (even intermediate states). Depending on used filters several
     * ArchivedProcessInstance will be
     * retrieved for a single ProcessInstance (one for each reached state).
     *
     * @param searchOptions
     *        The search options (pagination, filter, order sort). See {@link org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor} for
     *        valid fields for searching and sorting.
     * @return The archived process instances in all states that match the search options.
     * @throws SearchException
     *         If the search could not be completed correctly.
     * @since 6.2
     */
    SearchResult<ArchivedProcessInstance> searchArchivedProcessInstancesInAllStates(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for archived process instances supervised by the specified user.
     *
     * @param userId
     *        The identifier of the user.
     * @param searchOptions
     *        The search options (pagination, filter, order sort). See {@link org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor} for
     *        valid fields for searching and sorting.
     * @return The archived process instances supervised by the user that match the search options.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *         If the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<ArchivedProcessInstance> searchArchivedProcessInstancesSupervisedBy(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search for archived process instances that the specified user can access.
     *
     * @param userId
     *        The identifier of the user.
     * @param searchOptions
     *        The search options (pagination, filter, order sort). See {@link org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor} for
     *        valid fields for searching and sorting.
     * @return The archived process instances that the user can access that match the search options.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *         If the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<ArchivedProcessInstance> searchArchivedProcessInstancesInvolvingUser(long userId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search for human task instances.
     *
     * @param searchOptions
     *        The search options (pagination, filter, order sort). See {@link org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor} for valid
     *        fields for searching and sorting.
     * @return The human task instances that match the search options.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *         If the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchHumanTaskInstances(SearchOptions searchOptions) throws SearchException;

    /**
     * Search for tasks assigned to users supervised by the specified user.
     *
     * @param supervisorId
     *        The identifier of supervising user.
     * @param searchOptions
     *        The search options (pagination, filter, order sort). See {@link org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor} for valid
     *        fields for searching and sorting.
     * @return The human task instances that match the search options.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *         If the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<HumanTaskInstance> searchAssignedTasksSupervisedBy(long supervisorId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search for archived tasks assigned to users supervised by the specified user.
     *
     * @param supervisorId
     *        The identifier of the supervising user.
     * @param searchOptions
     *        The search options (pagination, filter, order sort). See {@link org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstanceSearchDescriptor} for
     *        valid fields for searching and sorting.
     * @return The archived human task instances that match the search options.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *         If the search could not be completed correctly.
     * @since 6.0
     */
    SearchResult<ArchivedHumanTaskInstance> searchArchivedHumanTasksSupervisedBy(long supervisorId, SearchOptions searchOptions) throws SearchException;

    /**
     * Evaluate expressions with values valid at process instantiation scope.
     *
     * @param processInstanceId
     *        The identifier of the process instance.
     * @param expressions
     *        The map of expressions to evaluate.
     * @return The result of the expression execution. Content of the resulting map depends on the incoming expression map. The returned key is The name of the
     *         expression (or its content if name is empty), the returned value is the evaluated expression result.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ExpressionEvaluationException
     *         Occurs when an exception is thrown during expression evaluation.
     * @since 6.0
     */
    Map<String, Serializable> evaluateExpressionsAtProcessInstanciation(long processInstanceId, Map<Expression, Map<String, Serializable>> expressions)
            throws ExpressionEvaluationException;

    /**
     * Evaluate expressions with values valid on a completed process instance scope.
     *
     * @param processInstanceId
     *        The identifier of the process instance.
     * @param expressions
     *        The map of expressions to evaluate.
     * @return The result of the expression execution. Content of the resulting map depends on the incoming expression map. The returned key is The name of the
     *         expression (or its content if name is empty), the returned value is the evaluated expression result.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the API session is invalid, e.g session has expired.
     * @throws ExpressionEvaluationException
     *         Occurs when an exception is thrown during expression evaluation.
     * @since 6.0
     */
    Map<String, Serializable> evaluateExpressionOnCompletedProcessInstance(long processInstanceId, Map<Expression, Map<String, Serializable>> expressions)
            throws ExpressionEvaluationException;

    /**
     * Evaluate expressions with values valid on a process instance scope.
     *
     * @param processInstanceId
     *        The identifier of the process instance.
     * @param expressions
     *        The map of expressions to evaluate.
     * @return The result of the expression execution. Content of the resulting map depends on the incoming expression map. The returned key is The name of the
     *         expression (or its content if name is empty), the returned value is the evaluated expression result.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the API session is invalid, e.g session has expired.
     * @throws ExpressionEvaluationException
     *         Occurs when an exception is thrown during expression evaluation.
     * @since 6.0
     */
    Map<String, Serializable> evaluateExpressionsOnProcessInstance(long processInstanceId, Map<Expression, Map<String, Serializable>> expressions)
            throws ExpressionEvaluationException;

    /**
     * Evaluate expressions with values valid on a process definition scope.
     *
     * @param processDefinitionId
     *        The identifier of the process definition.
     * @param expressions
     *        The map of expressions to evaluate.
     * @return The result of the expression execution. Content of the resulting map depends on the incoming expression map. The returned key is The name of the
     *         expression (or its content if name is empty), the returned value is the evaluated expression result.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the API session is invalid, e.g session has expired.
     * @throws ExpressionEvaluationException
     *         Occurs when an exception is thrown during expression evaluation.
     * @since 6.0
     */
    Map<String, Serializable> evaluateExpressionsOnProcessDefinition(long processDefinitionId, Map<Expression, Map<String, Serializable>> expressions)
            throws ExpressionEvaluationException;

    /**
     * Evaluate expressions with values valid on an activity instance scope.
     *
     * @param activityInstanceId
     *        The identifier of the activity instance.
     * @param expressions
     *        The map of expressions to evaluate.
     * @return The result of the expression execution. Content of the resulting map depends on the incoming expression map. The returned key is The name of the
     *         expression (or its content if name is empty), the returned value is the evaluated expression result.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the API session is invalid, e.g session has expired.
     * @throws ExpressionEvaluationException
     *         Occurs when an exception is thrown during expression evaluation.
     * @since 6.0
     */
    Map<String, Serializable> evaluateExpressionsOnActivityInstance(long activityInstanceId, Map<Expression, Map<String, Serializable>> expressions)
            throws ExpressionEvaluationException;

    /**
     * Evaluate expressions with values valid on a completed activity instance scope.
     *
     * @param activityInstanceId
     *        The identifier of the activity instance.
     * @param expressions
     *        The map of expressions to evaluate.
     * @return The result of the expression execution. Content of the resulting map depends on the incoming expression map. The returned key is The name of the
     *         expression (or its content if name is empty), the returned value is the evaluated expression result.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the API session is invalid, e.g session has expired.
     * @throws ExpressionEvaluationException
     *         Occurs when an exception is thrown during expression evaluation.
     * @since 6.0
     */
    Map<String, Serializable> evaluateExpressionsOnCompletedActivityInstance(long activityInstanceId, Map<Expression, Map<String, Serializable>> expressions)
            throws ExpressionEvaluationException;

    /**
     * Returns the list of jobs that failed.
     *
     * @param startIndex
     *        The result start index (starting from 0).
     * @param maxResults
     *        The maximum number of results to retrieve.
     * @return The list of failed jobs.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.1
     */
    List<FailedJob> getFailedJobs(int startIndex, int maxResults);

    /**
     * Replays the failed job in order to unlock it. The replay will use the stored parameters of the job.
     *
     * @param jobDescriptorId
     *        The identifier of the job descriptor.
     * @throws ExecutionException
     *         Occurs when an exception is thrown during the job replay
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.1
     */
    void replayFailedJob(final long jobDescriptorId) throws ExecutionException;

    /**
     * Replays the failed job in order to unlock it. The specified parameters replace the stored parameters. If the job is launched from CRON, all job
     * executions use the specified parameters.
     *
     * @param jobDescriptorId
     *        The identifier of the job descriptor.
     * @param parameters
     *        The job parameters.
     * @throws ExecutionException
     *         Occurs when an exception is thrown during the job replay
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @since 6.1
     */
    void replayFailedJob(final long jobDescriptorId, Map<String, Serializable> parameters) throws ExecutionException;

    /**
     * Gets the last archived data instance of the named data of the specified process instance.
     *
     * @param dataName
     *        The name of the data
     * @param processInstanceId
     *        The identifier of the process instance
     * @return An archived instance of data.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @throws ArchivedDataNotFoundException
     *         If the specified data cannot be found.
     * @since 6.1
     */
    ArchivedDataInstance getArchivedProcessDataInstance(String dataName, long processInstanceId) throws ArchivedDataNotFoundException;

    /**
     * Gets the last archived data instance of the named data of the specified activity instance.
     *
     * @param dataName
     *        The name of the data
     * @param activityInstanceId
     *        The identifier of the activity instance
     * @return An archived instance of data.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @throws ArchivedDataNotFoundException
     *         If the specified data cannot be found
     * @since 6.1
     */
    ArchivedDataInstance getArchivedActivityDataInstance(String dataName, long activityInstanceId) throws ArchivedDataNotFoundException;

    /**
     * Lists the last archived instances of data of the specified process instance.
     *
     * @param processInstanceId
     *        The identifier of the process instance
     * @param startIndex
     *        The start index
     * @param maxResults
     *        The max number of archived data instances
     * @return The list of archived data instances.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs while retrieving the archived instances of data
     * @since 6.1
     */
    List<ArchivedDataInstance> getArchivedProcessDataInstances(long processInstanceId, int startIndex, int maxResults);

    /**
     * Lists the last archived instances of data of the specified activity instance.
     *
     * @param activityInstanceId
     *        The identifier of the activity instance
     * @param startIndex
     *        The start index
     * @param maxResults
     *        The max number of archived data instances
     * @return The list of archived data instances.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs while retrieving the archived instances of data
     * @since 6.1
     */
    List<ArchivedDataInstance> getArchivedActivityDataInstances(long activityInstanceId, int startIndex, int maxResults);

    /**
     * Lists the possible users (candidates) of the specified human task instance.
     * Users are ordered by user name.
     *
     * @param humanTaskInstanceId
     *        The identifier of the human task instance
     * @param startIndex
     *        The start index
     * @param maxResults
     *        The max number of users
     * @return The list of users.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs while retrieving the users
     * @since 6.1
     */
    List<User> getPossibleUsersOfPendingHumanTask(long humanTaskInstanceId, int startIndex, int maxResults);

    /**
     * Lists the possible users (candidates) that can execute the specified human task instance.
     * Users are ordered by user name.
     *
     * @param humanTaskInstanceId
     *        The identifier of the human task instance
     * @param searchOptions
     *        the search options. See {@link org.bonitasoft.engine.identity.UserSearchDescriptor} for valid fields for searching and sorting.
     * @return The list of users.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *         If the session is invalid (expired, unknown, ...)
     * @throws org.bonitasoft.engine.exception.RetrieveException
     *         If an exception occurs while retrieving the users
     * @since 6.3
     */
    SearchResult<User> searchUsersWhoCanExecutePendingHumanTask(final long humanTaskInstanceId, SearchOptions searchOptions);

    /**
     * Search process definitions that have instances with assigned or pending human tasks for a specific user.
     * The tasks are in stable state, not in terminal/executing state.
     *
     * @param userId
     *        The identifier of the user.
     * @param searchOptions
     *        The search criterion. See {@link org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoSearchDescriptor} for valid fields for searching and
     *        sorting.
     * @return The list of process definitions
     * @throws SearchException
     *         if an exception occurs when getting the process deployment information.
     * @since 6.3.3
     */
    SearchResult<ProcessDeploymentInfo> searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksFor(long userId, SearchOptions searchOptions)
            throws SearchException;

    /**
     * Search process definitions supervised by a specific user, that have instances with assigned or pending human tasks.
     * The tasks are in stable state, not in terminal/executing state.
     *
     * @param supervisorId
     *        The identifier of the user.
     * @param searchOptions
     *        The search criterion. See {@link org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoSearchDescriptor} for valid fields for searching and
     *        sorting.
     * @return The list of process definitions
     * @throws SearchException
     *         if an exception occurs when getting the process deployment information.
     * @since 6.3.3
     */
    SearchResult<ProcessDeploymentInfo> searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(long supervisorId, SearchOptions searchOptions)
            throws SearchException;

    /**
     * Search process definitions that have instances with assigned or pending human tasks.
     * The tasks are in stable state, not in terminal/executing state.
     *
     * @param searchOptions
     *        The search criterion. See {@link org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoSearchDescriptor} for valid fields for searching and
     *        sorting.
     * @return The list of process definitions
     * @throws SearchException
     *         If an exception occurs when getting the process deployment information.
     * @since 6.3.3
     */
    SearchResult<ProcessDeploymentInfo> searchProcessDeploymentInfosWithAssignedOrPendingHumanTasks(SearchOptions searchOptions) throws SearchException;

    /**
     * Retrieve, for a given process instance, the current counters on flownodes. Please note: this method does not count the flownodes of sub-process instances
     * of the given process instance.
     *
     * @param processInstanceId ID of the process instance of which to retrieve the current indicators.
     * @return A map of counters: the key is the name of the flownode, as defined at design-time. the value is the current counters for this flownode, that is,
     *         a map of &lt;state name, number of current flownode in that state&gt;
     *         If no results, returns an empty Map.
     * @since 6.5.0
     */
    Map<String, Map<String, Long>> getFlownodeStateCounters(long processInstanceId);

    /**
     * Search the {@link TimerEventTriggerInstance} on the specific {@link ProcessInstance}.
     *
     * @param searchOptions
     *        The search criterion.
     * @return The list of the timer event triggers
     * @throws SearchException
     *         If an exception occurs when getting the timer event triggers.
     * @since 6.4.0
     */
    SearchResult<TimerEventTriggerInstance> searchTimerEventTriggerInstances(long processInstanceId, SearchOptions searchOptions) throws SearchException;

    /**
     * Change the date of the execution of a specific {@link TimerEventTriggerInstance}.
     *
     * @param timerEventTriggerInstanceId
     *        The identifier of the {@link TimerEventTriggerInstance} to update
     * @param executionDate
     *        The new date of the execution of the {@link TimerEventTriggerInstance}
     * @return The first fire time of the newly scheduled trigger is returned
     * @throws TimerEventTriggerInstanceNotFoundException
     *         If the {@link TimerEventTriggerInstance} doesn't exist
     * @throws UpdateException
     *         If an exception occurs when updating the {@link TimerEventTriggerInstance}
     * @since 6.4.0
     */
    Date updateExecutionDateOfTimerEventTriggerInstance(long timerEventTriggerInstanceId, Date executionDate)
            throws TimerEventTriggerInstanceNotFoundException, UpdateException;

    /**
     * Gets the contract of the user task.
     *
     * @param userTaskId the identifier of the user task.
     * @return the contract of the user task
     * @throws UserTaskNotFoundException
     *         if identifier does not refer to a real user task.
     */
    ContractDefinition getUserTaskContract(long userTaskId) throws UserTaskNotFoundException;

    /**
     * Gets the process instantiation contract for a given process definition.
     *
     * @param processDefinitionId the identifier of the process definition.
     * @return the contract of the given process
     * @throws ProcessDefinitionNotFoundException
     *         if identifier does not refer to an existing process definition.
     */
    ContractDefinition getProcessContract(long processDefinitionId) throws ProcessDefinitionNotFoundException;

    /**
     * Executes a user task that is in a stable state.
     * Will move the activity to the next stable state and then continue the execution of the process.
     *
     * @param userTaskInstanceId
     *        The identifier of the user task to execute.
     * @param inputs
     *        the inputs used for user task execution
     * @throws UserTaskNotFoundException
     *         If user task to execute is not found
     * @throws ContractViolationException
     *         If inputs don't fit with task contract
     * @throws FlowNodeExecutionException
     *         If an execution exception occurs
     * @since 7.0
     */
    void executeUserTask(long userTaskInstanceId, Map<String, Serializable> inputs) throws UserTaskNotFoundException, ContractViolationException,
            FlowNodeExecutionException;

    /**
     * Executes a user task that is in a stable state on behalf of a given user
     * Will make the task go in the next stable state and then continue the execution of the process
     * If userId equals 0, the logged-in user is declared as the executer of the task.
     * The user, who executed the task on behalf of a given user, is declared as a executer delegate.
     *
     * @param userId
     *        The identifier of the user for which you want to execute the flow node
     * @param userTaskInstanceId
     *        The identifier of the user task to execute
     * @param inputs
     *        the input used for user task execution
     * @throws UserTaskNotFoundException
     *         If user task to execute is not found
     * @throws ContractViolationException
     *         If inputs don't fit with task contract
     * @throws FlowNodeExecutionException
     *         If an execution exception occurs
     * @since 7.0
     */
    void executeUserTask(long userId, long userTaskInstanceId, Map<String, Serializable> inputs) throws UserTaskNotFoundException, ContractViolationException,
            FlowNodeExecutionException;

    /**
     * Gets the value of the variable of the user task contract.
     *
     * @param userTaskInstanceId
     *        The identifier of the user task
     * @param name
     *        The name of the variable
     * @return The identifier of the user task
     * @throws ContractDataNotFoundException if no data found for the given user task instance and name.
     */
    Serializable getUserTaskContractVariableValue(long userTaskInstanceId, String name) throws ContractDataNotFoundException;

    /**
     * Gets the value of a process instantiation input, during the phase of initializing. For instance, if a connector on_enter fails, this method can be called
     * to check the current value.
     *
     * @param processInstanceId The identifier of the process instance
     * @param name The name of the process input to retrieve
     * @return The identifier of the user task
     * @throws ContractDataNotFoundException if no data found for the given process instance and name.
     */
    Serializable getProcessInputValueDuringInitialization(long processInstanceId, String name) throws ContractDataNotFoundException;

    /**
     * Gets the value of a process instantiation input, after initialization has finished. Requires Archiving feature to be enabled (default behaviour).
     *
     * @param processInstanceId The identifier of the process instance
     * @param name The name of the process input to retrieve
     * @return The identifier of the user task
     * @throws ContractDataNotFoundException if identifier does not refer to an existing process instance.
     */
    Serializable getProcessInputValueAfterInitialization(long processInstanceId, String name) throws ContractDataNotFoundException;

    /**
     * return the context defined in the process definition for this user task instance
     *
     * @param userTaskInstanceId the id of the user task instance
     * @return a map containing the evaluated context
     * @throws UserTaskNotFoundException if <code>userTaskInstanceId</code> does not reference any existing task.
     */
    Map<String, Serializable> getUserTaskExecutionContext(long userTaskInstanceId) throws UserTaskNotFoundException, ExpressionEvaluationException;

    /**
     * return the context defined in the process definition for this user task instance
     *
     * @param archivedUserTaskInstanceId the id of the archived version of the user task instance
     * @return a map containing the evaluated context
     * @throws UserTaskNotFoundException if <code>archivedUserTaskInstanceId</code> does not reference any existing archived task.
     */
    Map<String, Serializable> getArchivedUserTaskExecutionContext(long archivedUserTaskInstanceId) throws UserTaskNotFoundException, ExpressionEvaluationException;

    /**
     * return the context defined in the process definition for this process instance
     *
     * @param processInstanceId the id of the process instance
     * @return a map containing the evaluated context
     * @throws ProcessInstanceNotFoundException if <code>processInstanceId</code> does not reference any existing process.
     */
    Map<String, Serializable> getProcessInstanceExecutionContext(long processInstanceId) throws ProcessInstanceNotFoundException, ExpressionEvaluationException;

    /**
     * return the context defined in the process definition for this process instance
     *
     * @param archivedProcessInstanceId the id of the archived version of a process instance
     * @return a map containing the evaluated context
     * @throws ProcessInstanceNotFoundException if <code>archivedProcessInstanceId</code> does not reference any existing process.
     */
    Map<String, Serializable> getArchivedProcessInstanceExecutionContext(long archivedProcessInstanceId) throws ProcessInstanceNotFoundException, ExpressionEvaluationException;
}
