/*
 * Copyright (C) 2011-2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package org.bonitasoft.engine.api;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.model.ActivityInstance;
import org.bonitasoft.engine.bpm.model.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.model.HumanTaskInstance;
import org.bonitasoft.engine.bpm.model.Index;
import org.bonitasoft.engine.bpm.model.ManualTaskInstance;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.bpm.model.ProcessInstanceUpdateDescriptor;
import org.bonitasoft.engine.bpm.model.TaskPriority;
import org.bonitasoft.engine.bpm.model.archive.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.model.archive.ArchivedComment;
import org.bonitasoft.engine.bpm.model.archive.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.model.archive.ArchivedHumanTaskInstance;
import org.bonitasoft.engine.bpm.model.archive.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.model.data.DataInstance;
import org.bonitasoft.engine.bpm.model.event.EventInstance;
import org.bonitasoft.engine.core.operation.Operation;
import org.bonitasoft.engine.exception.ActivityCreationException;
import org.bonitasoft.engine.exception.ActivityExecutionErrorException;
import org.bonitasoft.engine.exception.ActivityExecutionFailedException;
import org.bonitasoft.engine.exception.ActivityInstanceModificationException;
import org.bonitasoft.engine.exception.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.exception.ActivityInstanceReadException;
import org.bonitasoft.engine.exception.ActivityInterruptedException;
import org.bonitasoft.engine.exception.ActivityNotFoundException;
import org.bonitasoft.engine.exception.ArchivedActivityInstanceNotFoundException;
import org.bonitasoft.engine.exception.ArchivedProcessInstanceNotFoundException;
import org.bonitasoft.engine.exception.ClassLoaderException;
import org.bonitasoft.engine.exception.CommentReadException;
import org.bonitasoft.engine.exception.ConnectorException;
import org.bonitasoft.engine.exception.DataNotFoundException;
import org.bonitasoft.engine.exception.DataUpdateException;
import org.bonitasoft.engine.exception.EventInstanceReadException;
import org.bonitasoft.engine.exception.ExpressionEvaluationException;
import org.bonitasoft.engine.exception.InvalidEvaluationConnectorCondition;
import org.bonitasoft.engine.exception.InvalidProcessDefinitionException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.ObjectNotFoundException;
import org.bonitasoft.engine.exception.OperationExecutionException;
import org.bonitasoft.engine.exception.PageOutOfRangeException;
import org.bonitasoft.engine.exception.ProcessDefinitionNotEnabledException;
import org.bonitasoft.engine.exception.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.exception.ProcessDefinitionReadException;
import org.bonitasoft.engine.exception.ProcessInstanceCreationException;
import org.bonitasoft.engine.exception.ProcessInstanceModificationException;
import org.bonitasoft.engine.exception.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.exception.ProcessInstanceReadException;
import org.bonitasoft.engine.exception.RetryTaskException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.SendEventException;
import org.bonitasoft.engine.exception.TaskReleaseException;
import org.bonitasoft.engine.exception.UnreleasableTaskException;
import org.bonitasoft.engine.exception.UserNotFoundException;
import org.bonitasoft.engine.exception.UserTaskNotFoundException;
import org.bonitasoft.engine.exception.UserTaskSetPriorityException;
import org.bonitasoft.engine.exception.flownode.TaskHidingException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Yanyan Liu
 * @author Zhao Na
 * @author Frederic Bouquet
 * @author Elias Ricken de Medeiros
 */
public interface ProcessRuntimeAPI {

    /**
     * Start an instance of the process definition having processDefinitionId, and using the current session user
     * 
     * @param processDefinitionId
     *            Identifier of the process definition will be started
     * @return an instance of the process
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             No matching process definition found
     * @throws ProcessInstanceCreationException
     *             the exception maybe happened when the instance of process is inserted into DataBase
     * @throws ProcessDefinitionReadException
     *             some errors are occurred when retrieve the matching process definition by given processDefinitionId
     * @throws ProcessDefinitionNotEnabledException
     *             a primary precondition of starting a process is the status of matching process definition should be enabled, if not,the exception occur.
     */
    ProcessInstance startProcess(long processDefinitionId) throws InvalidSessionException, ProcessDefinitionNotFoundException, ProcessDefinitionReadException,
            ProcessDefinitionNotEnabledException, ProcessInstanceCreationException;

    /**
     * Execute an activity that is is a non stable state
     * Will make the activity go in the next stable state and then continue the execution of the process
     * 
     * @param flowNodeInstanceId
     *            id of the flow node to execute
     * @throws InvalidSessionException
     *             The session is not valid
     * @throws ActivityInterruptedException
     *             The activity was interrupted
     * @throws ActivityExecutionFailedException
     *             The execution of the activity failed
     * @throws ActivityExecutionErrorException
     *             An unexpected error occurred while executing the activity
     * @throws ActivityInstanceNotFoundException
     *             if the activity cannot be found
     */
    void executeActivity(long flowNodeInstanceId) throws InvalidSessionException, ActivityInterruptedException, ActivityExecutionFailedException,
            ActivityExecutionErrorException, ActivityInstanceNotFoundException;

    /**
     * Execute an activity that is in a non state step by step.
     * Will make the activity go in the next normal state
     * 
     * @param activityInstanceUUID
     *            id of the activity to execute
     * @return
     *         The activityInstance after the execution
     * @throws InvalidSessionException
     *             The session is not valid
     * @throws ActivityExecutionFailedException
     *             The execution of the activity failed
     * @throws ActivityExecutionErrorException
     *             An unexpected error occurred while executing the activity
     */
    ActivityInstance executeActivityStepByStep(long activityInstanceUUID) throws InvalidSessionException, ActivityExecutionFailedException,
            ActivityExecutionErrorException;

    /**
     * Returns all activities (active and finished) of a process instance.
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @param pageIndex
     *            the index of the page of results to get
     * @param numberPerPage
     *            the number of results to get
     * @return the matching set of activity instances
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActivityInstanceReadException
     *             can't retrieve an instance of activity
     */
    Set<ActivityInstance> getActivities(long processInstanceId, int pageIndex, int numberPerPage) throws InvalidSessionException, ActivityInstanceReadException;

    /**
     * Get an instance of process with its processInstance id.
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @return the matching instance of process
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws ProcessInstanceReadException
     *             Error thrown if can't retrieve corresponding instance of process.
     */
    ProcessInstance getProcessInstance(long processInstanceId) throws InvalidSessionException, ProcessInstanceNotFoundException, ProcessInstanceReadException;

    /**
     * Get an instance of activity using its activity instance id.
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return the matching instance of activity
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActivityInstanceNotFoundException
     *             if the activity cannot be found
     * @throws ActivityInstanceReadException
     *             can't retrieve an instance of activity
     */
    ActivityInstance getActivityInstance(long activityInstanceId) throws InvalidSessionException, ActivityInstanceNotFoundException,
            ActivityInstanceReadException;

    /**
     * Get an activity instance that already was archived.
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return the matching activity instance that already was archived
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActivityInstanceNotFoundException
     *             if the archived activity cannot be found
     * @throws ActivityInstanceReadException
     *             can't retrieve an instance of activity
     */
    ArchivedActivityInstance getArchivedActivityInstance(long activityInstanceId) throws InvalidSessionException, ActivityInstanceNotFoundException,
            ActivityInstanceReadException;

    /**
     * Retrieve a list of assigned human task instances related to given userId.
     * 
     * @param userId
     *            the user identifier
     * @param pageIndex
     *            the index of the page to retrieve
     * @param numberPerPage
     *            the max number of elements to retrieve per page
     * @param criterion
     *            the sort criterion
     * @return the matching list
     * @throws InvalidSessionException
     *             occurs when the session is invalid.
     * @throws UserNotFoundException
     *             occurs when the userId does not refer to any user.
     * @throws ActivityInstanceReadException
     *             can't retrieve an instance of activity
     */
    List<HumanTaskInstance> getAssignedHumanTaskInstances(long userId, int pageIndex, int numberPerPage, ActivityInstanceCriterion criterion)
            throws InvalidSessionException, UserNotFoundException, ActivityInstanceReadException;

    /**
     * Retrieve a list of pending human task instances related to given userId.
     * 
     * @param userId
     *            the user identifier
     * @param pageIndex
     *            the index of the page to retrieve
     * @param numberPerPage
     *            the max number of elements to retrieve per page
     * @param pagingCriterion
     *            the Criterion is for how to seperate all items gotten to many pages.
     * @return the matching list
     * @throws InvalidSessionException
     *             occurs when the session is invalid.
     * @throws UserNotFoundException
     *             occurs when the userId does not refer to any user.
     */
    List<HumanTaskInstance> getPendingHumanTaskInstances(long userId, final int pageIndex, final int numberPerPage, ActivityInstanceCriterion pagingCriterion)
            throws InvalidSessionException, UserNotFoundException;

    /**
     * Count total number of human task instances assigned that is related to the given userId.
     * 
     * @param userId
     *            Identifier of a user
     * @return a number of human task instances assigned
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActivityInstanceReadException
     *             errors happen in while retrieving the instance of activity
     */
    long getNumberOfAssignedHumanTaskInstances(long userId) throws InvalidSessionException, ActivityInstanceReadException;

    /**
     * Return userIds and corresponding open task's id.
     * 
     * @param userIds
     *            a list of Identifiers for users
     * @return a map with userId as key and task id as value
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActivityInstanceReadException
     *             can't retrieve an instance of activity
     */
    Map<Long, Long> getNumberOfOpenTasks(List<Long> userIds) throws InvalidSessionException, ActivityInstanceReadException;

    /**
     * Count number of pending human task instances.
     * 
     * @param userId
     *            Identifier of a user
     * @return a number of pending human task instances
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActivityInstanceReadException
     *             errors happen in while retrieving the instance of activity
     */
    long getNumberOfPendingHumanTaskInstances(long userId) throws InvalidSessionException, ActivityInstanceReadException;

    /**
     * Retrieve a human task instance using corresponding activity instance id.
     * 
     * @param activityInstanceID
     *            Identifier of the activity instance
     * @return the matching instance of human task
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActivityInstanceNotFoundException
     *             if the human task cannot be found
     * @throws ActivityInstanceReadException
     *             errors happen in while retrieving the instance of activity
     */
    HumanTaskInstance getHumanTaskInstance(long activityInstanceID) throws InvalidSessionException, ActivityInstanceNotFoundException,
            ActivityInstanceReadException;

    /**
     * Get a event instance list according to all given conditions
     * 
     * @param rootContainerId
     *            id of the most original container
     * @param pageIndex
     *            the index of the page of results to get
     * @param numberPerPage
     *            the number of results to get
     * @param sortingType
     * @return the matching list of event instances
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws EventInstanceReadException
     *             Errors thrown if can't retrieve the corresponding instance of event.
     */
    List<EventInstance> getEventInstances(long rootContainerId, int pageIndex, int numberPerPage, EventSorting sortingType) throws InvalidSessionException,
            EventInstanceReadException;

    /**
     * Count assigned tasks that was already supervised by a manager with given userId.
     * 
     * @param managerUserId
     *            Identifier of a user
     * @return the matching number of assigned tasks
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws UserNotFoundException
     *             Error thrown if no user have an id corresponding to the value of managerUserId parameter.
     */
    long getNumberOfAssignedTasksSupervisedBy(long managerUserId) throws InvalidSessionException, UserNotFoundException;

    /**
     * Add a manual task with given human task id.
     * 
     * @param humanTaskId
     *            Identifier of the human task
     * @param taskName
     *            name of the task
     * @param assignTo
     *            a name of user that the task assigned to
     * @param description
     *            what's the task for
     * @param dueDate
     *            expected date
     * @param priority
     *            the task priority to set
     * @return the matching an instance of manual task
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActivityExecutionErrorException
     *             An unexpected error occurred while executing the user task
     * @throws ActivityExecutionFailedException
     *             The execution of the activity failed
     * @throws ActivityInterruptedException
     *             The activity was interrupted
     * @throws ActivityCreationException
     * @throws ActivityNotFoundException
     *             errors thrown if can't find corresponding activity
     */
    ManualTaskInstance addManualUserTask(long humanTaskId, String taskName, String displayName, long assignTo, String description, Date dueDate,
            TaskPriority priority) throws InvalidSessionException, ActivityInterruptedException, ActivityExecutionErrorException, ActivityCreationException,
            ActivityNotFoundException;

    /**
     * Assign a task to a user with given user name.
     * 
     * @param userTaskId
     *            Identifier of user task
     * @param userId
     *            id of user
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActivityInstanceNotFoundException
     *             if the activity cannot be found
     * @throws ActivityInstanceReadException
     *             errors happen in while retrieving the instance of activity
     * @throws UserNotFoundException
     *             Error thrown if no user have an id corresponding to the value of userName parameter
     */
    void assignUserTask(long userTaskId, long userId) throws InvalidSessionException, ActivityInstanceNotFoundException, ActivityInstanceReadException,
            UserNotFoundException;

    /**
     * Returns all datas of a process instance
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @param pageIndex
     *            the index of the page of results to get
     * @param numberPerPage
     *            the number of results to get
     * @return the matching list of dataInstances
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DataNotFoundException
     *             Error thrown if can't get the corresponding data.
     */
    List<DataInstance> getProcessDataInstances(long processInstanceId, int pageIndex, int numberPerPage) throws InvalidSessionException, DataNotFoundException;

    /**
     * Get a DataInstance by dataName, containerId and containerType.
     * 
     * @param dataName
     *            name of data
     * @param processInstanceId
     *            Identifier of the process instance
     * @return an instance of the data
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DataNotFoundException
     *             Error thrown if can't get the corresponding data.
     */
    public DataInstance getProcessDataInstance(String dataName, long processInstanceId) throws InvalidSessionException, DataNotFoundException;

    /**
     * Update data instance with given data name,value and container id that the data belongs to.
     * 
     * @param dataName
     *            name of data
     * @param containerId
     *            Identifier of container like processInstanceId
     * @param dataValue
     *            value of data
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DataUpdateException
     */
    public void updateProcessDataInstance(String dataName, long containerId, Serializable dataValue) throws InvalidSessionException, DataUpdateException;

    /**
     * Get data instances under the activity with given activity id.
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @param pageIndex
     *            the index of the page of results to get
     * @param numberPerPage
     *            the number of results to get
     * @return the matching list of dataInstances
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DataNotFoundException
     *             Error thrown if can't get the corresponding data.
     */
    List<DataInstance> getActivityDataInstances(long activityInstanceId, int pageIndex, int numberPerPage) throws InvalidSessionException,
            DataNotFoundException;

    /**
     * Get a data instance in activity with data name and activity instance id.
     * 
     * @param dataName
     *            name of data
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return an instance of data
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DataNotFoundException
     *             Error thrown if can't get the corresponding data.
     */
    DataInstance getActivityDataInstance(String dataName, long activityInstanceId) throws InvalidSessionException, DataNotFoundException;

    /**
     * Update a data instance's value which in activity using data name and activity instance id.
     * 
     * @param dataName
     *            name of data
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @param dataValue
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DataUpdateException
     */
    void updateActivityDataInstance(String dataName, long activityInstanceId, Serializable dataValue) throws InvalidSessionException, DataUpdateException;

    /**
     * Get the date when the activity with given activityInstanceId reaches the given state.
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @param state
     *            representing state of the activity existed
     * @return the matching date
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActivityInstanceReadException
     *             errors happen in while retrieving the instance of activity
     */
    Date getActivityReachedStateDate(long activityInstanceId, String state) throws InvalidSessionException, ActivityInstanceReadException;

    /**
     * Update the given variables of an activity instance.
     * 
     * @param activityInstanceId
     *            the activity identifier
     * @param variables
     *            a map which contains several couple the variable name/value
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DataNotFoundException
     *             Error thrown if can't get the corresponding data.
     */
    void updateActivityInstanceVariables(long activityInstanceId, Map<String, Serializable> variables) throws InvalidSessionException, DataNotFoundException;

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
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws DataNotFoundException
     *             Error thrown if can't get the corresponding data.
     * @throws OperationExecutionException
     *             in case of exception during the process of executing one of the operations
     * @throws ActivityInstanceNotFoundException
     *             if the activity cannot be found
     */
    void updateActivityInstanceVariables(List<Operation> operations, long activityInstanceId, Map<String, Serializable> expressionContexts)
            throws InvalidSessionException, DataNotFoundException, OperationExecutionException, ActivityInstanceNotFoundException;

    /**
     * Update the due date of a task
     * 
     * @param userTaskId
     *            identifier of the task to update
     * @param dueDate
     *            new due date for the task
     * @throws InvalidSessionException
     * @throws ActivityInstanceModificationException
     *             when it's not possible to update it
     * @throws ActivityInstanceNotFoundException
     *             if the activity does not exists
     */
    void updateDueDateOfTask(long userTaskId, Date dueDate) throws InvalidSessionException, ActivityInstanceModificationException,
            ActivityInstanceNotFoundException;

    /**
     * Get an instance of userTask assigned in an instance of process.
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @param userId
     *            Identifier of a user
     * @return id of assigned userTask instance in process instance
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws UserNotFoundException
     *             Error thrown if no user have an id corresponding to the value of userId parameter
     * @throws ActivityInstanceReadException
     *             errors happen in while retrieving the instance of activity
     */
    long getOneAssignedUserTaskInstanceOfProcessInstance(long processInstanceId, long userId) throws InvalidSessionException, UserNotFoundException,
            ActivityInstanceReadException;

    /**
     * Get an instance of userTask assigned in a processDefinition.
     * 
     * @param processDefinitionId
     *            Identifier of the process definition
     * @param userId
     *            Identifier of a user
     * @return the matching userTask id
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws UserNotFoundException
     *             Error thrown if no user have an id corresponding to the value of userId parameter
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws ProcessInstanceReadException
     *             Error thrown if can't retrieve corresponding instance of process.
     * @throws ActivityInstanceReadException
     *             errors happened in while retrieving the instance of activity
     */
    long getOneAssignedUserTaskInstanceOfProcessDefinition(long processDefinitionId, long userId) throws InvalidSessionException, UserNotFoundException,
            ProcessInstanceNotFoundException, ProcessInstanceReadException, ActivityInstanceReadException;

    /**
     * Get an activity instance's state according to its activityInstanceId.
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return the matching activity's state
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActivityInstanceNotFoundException
     *             if the activity cannot be found
     */
    String getActivityInstanceState(long activityInstanceId) throws InvalidSessionException, ActivityInstanceNotFoundException;

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
     * @throws ActivityInstanceReadException
     *             errors happen in while retrieving the instance of activity
     */
    boolean canExecuteTask(long activityInstanceId, long userId) throws InvalidSessionException, ActivityInstanceNotFoundException, UserNotFoundException,
            ActivityInstanceReadException;

    /**
     * release a task (un_claim or un_assign). After the operation, the task should be in the pending task list
     * 
     * @param userTaskId
     *            Identifier of user task
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActivityNotFoundException
     *             errors thrown if can't find corresponding activity
     * @throws TaskReleaseException
     */
    void releaseUserTask(long userTaskId) throws InvalidSessionException, ActivityNotFoundException, TaskReleaseException, UnreleasableTaskException;

    /**
     * @param processInstanceId
     *            the process instance ID to retrieve the list of archives from
     * @param pageIndex
     *            the index of the page of results to get
     * @param numberPerPage
     *            the number of results to get
     * @return the list found
     * @throws InvalidSessionException
     *             if no current valid session is found
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws ProcessInstanceReadException
     *             if search failed for a read reason
     */
    List<ArchivedProcessInstance> getArchivedProcessInstanceList(long processInstanceId, int pageIndex, int numberPerPage) throws InvalidSessionException,
            ProcessInstanceNotFoundException, ProcessInstanceReadException;

    /**
     * Get the last archived instance of process.
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @return an instance of process archived
     * @throws InvalidSessionException
     *             if no current valid session is found
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws ProcessInstanceReadException
     *             if search failed for a read reason
     */
    ArchivedProcessInstance getFinalArchivedProcessInstance(long processInstanceId) throws InvalidSessionException, ProcessInstanceNotFoundException,
            ProcessInstanceReadException;

    /**
     * Set a activity's state with state id.
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @param stateId
     *            Identifier of state
     * @throws InvalidSessionException
     *             if no current valid session is found
     * @throws UserTaskNotFoundException
     *             errors thrown if can't get corresponding user task.
     * @throws ActivityExecutionFailedException
     *             The execution of the activity failed
     */
    void setStateByStateId(long activityInstanceId, int stateId) throws InvalidSessionException, UserTaskNotFoundException, ActivityExecutionFailedException;

    /**
     * Set a activity's state with state name.
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @param state
     *            representing new state of the activity
     * @throws InvalidSessionException
     *             if no current valid session is found
     * @throws UserTaskNotFoundException
     *             errors thrown if can't get corresponding user task.
     * @throws ActivityExecutionFailedException
     *             The execution of the activity failed
     */
    void setStateByStateName(long activityInstanceId, String state) throws InvalidSessionException, UserTaskNotFoundException, ActivityExecutionFailedException;

    /**
     * Set a process instance's state.
     * 
     * @param processInstance
     *            an instance of process
     * @param state
     *            representing new state of the activity
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessInstanceModificationException
     *             error thrown while updating the instance of process failed.
     */
    void setProcessInstanceState(final ProcessInstance processInstance, final String state) throws InvalidSessionException,
            ProcessInstanceModificationException;

    /**
     * Set this userTask instance's priority.
     * 
     * @param userTaskInstanceId
     *            Identifier of user task instance
     * @param priority
     *            new priority of this task
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws UserTaskSetPriorityException
     */
    void setTaskPriority(long userTaskInstanceId, TaskPriority priority) throws InvalidSessionException, UserTaskSetPriorityException;

    /**
     * Execute connector in given activity instance.
     * 
     * @param connectorDefinitionId
     *            Identifier of connector definition
     * @param connectorDefinitionVersion
     *            version of the connector definition
     * @param connectorInputParameters
     *            all expressions related with the connector
     * @param inputValues
     *            all parameters values for expression need when evalute the connector
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return a map with connector parameter name and parameter value object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActivityInstanceNotFoundException
     *             if the activity cannot be found
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws ClassLoaderException
     *             errors thrown while loading class failed.
     * @throws ConnectorException
     *             error thrown when connect external application failed.
     * @throws InvalidEvaluationConnectorCondition
     */
    Map<String, Serializable> executeConnectorOnActivityInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long activityInstanceId)
            throws InvalidSessionException, ActivityInstanceNotFoundException, ProcessInstanceNotFoundException, ClassLoaderException, ConnectorException,
            InvalidEvaluationConnectorCondition;

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
     *            all parameters values for expression need when evalute the connector
     * @param processDefinitionId
     *            Identifier of the process definition
     * @return a map with connector parameter name and parameter value object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ClassLoaderException
     *             errors thrown while loading class failed.
     * @throws ConnectorException
     *             error thrown when connect external application failed.
     * @throws InvalidEvaluationConnectorCondition
     * @throws InvalidProcessDefinitionException
     */
    Map<String, Serializable> executeConnectorOnProcessDefinition(final String connectorDefinitionId, final String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long processDefinitionId)
            throws InvalidSessionException, ClassLoaderException, ConnectorException, InvalidEvaluationConnectorCondition, InvalidProcessDefinitionException;

    /**
     * Execute connector in given process instance.
     * 
     * @param connectorDefinitionId
     *            Identifier of connector definition
     * @param connectorDefinitionVersion
     *            version of the connector definition
     * @param connectorInputParameters
     *            all expressions related with the connector
     * @param inputValues
     *            all parameters values for expression need when evalute the connector
     * @param processInstanceId
     *            Identifier of the process instance
     * @return a map with connector parameter name and parameter value object
     * @throws ClassLoaderException
     *             errors thrown while loading class failed.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws ConnectorException
     *             error thrown when connect external application failed.
     * @throws InvalidEvaluationConnectorCondition
     */
    Map<String, Serializable> executeConnectorOnProcessInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long processInstanceId)
            throws ClassLoaderException, InvalidSessionException, ProcessInstanceNotFoundException, ConnectorException, InvalidEvaluationConnectorCondition;

    /**
     * Execute connector in given activity instance finished.
     * 
     * @param connectorDefinitionId
     *            Identifier of connector definition
     * @param connectorDefinitionVersion
     *            version of the connector definition
     * @param connectorInputParameters
     *            all expressions related with the connector
     * @param inputValues
     *            all parameters values for expression need when evalute the connector
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return a map with connector parameter name and parameter value object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ArchivedActivityInstanceNotFoundException
     *             if the activity cannot be found
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws ClassLoaderException
     *             errors thrown while loading class failed.
     * @throws ConnectorException
     *             error thrown when connect external application failed.
     * @throws InvalidEvaluationConnectorCondition
     */
    Map<String, Serializable> executeConnectorOnCompletedActivityInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long activityInstanceId)
            throws InvalidSessionException, ArchivedActivityInstanceNotFoundException, ProcessInstanceNotFoundException, ClassLoaderException,
            ConnectorException, InvalidEvaluationConnectorCondition;

    /**
     * Execute connector in given process instance finished.
     * 
     * @param connectorDefinitionId
     *            Identifier of connector definition
     * @param connectorDefinitionVersion
     *            version of the connector definition
     * @param connectorInputParameters
     *            all expressions related with the connector
     * @param inputValues
     *            all parameters values for expression need when evalute the connector
     * @param processInstanceId
     *            Identifier of the process instance
     * @return a map with connector parameter name and parameter value object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ArchivedProcessInstanceNotFoundException
     * @throws ClassLoaderException
     *             errors thrown while loading class failed.
     * @throws ConnectorException
     *             error thrown when connect external application failed.
     * @throws InvalidEvaluationConnectorCondition
     */
    Map<String, Serializable> executeConnectorOnCompletedProcessInstance(final String connectorDefinitionId, final String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long processInstanceId)
            throws InvalidSessionException, ArchivedProcessInstanceNotFoundException, ClassLoaderException, ConnectorException,
            InvalidEvaluationConnectorCondition;

    /**
     * Execute connector in given process instance initialized.
     * 
     * @param connectorDefinitionId
     *            Identifier of connector definition
     * @param connectorDefinitionVersion
     *            version of the connector definition
     * @param connectorInputParameters
     *            all expressions related with the connector
     * @param inputValues
     *            all parameters values for expression need when evaluate the connector
     * @param processInstanceId
     *            Identifier of the process instance
     * @return a map with connector parameter name and parameter value object
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ArchivedProcessInstanceNotFoundException
     * @throws ClassLoaderException
     *             errors thrown while loading class failed.
     * @throws ConnectorException
     *             error thrown when connect external application failed.
     * @throws InvalidEvaluationConnectorCondition
     */
    Map<String, Serializable> executeConnectorAtProcessInstantiation(final String connectorDefinitionId, final String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long processInstanceId)
            throws InvalidSessionException, ArchivedProcessInstanceNotFoundException, ClassLoaderException, ConnectorException,
            InvalidEvaluationConnectorCondition;

    /**
     * Search task archived.
     * 
     * @param searchOptions
     *            search conditions and set sort,paging properties.
     * @return the number of human tasks found and the matching human tasks
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     */
    SearchResult<ArchivedHumanTaskInstance> searchArchivedTasks(final SearchOptions searchOptions) throws InvalidSessionException;

    /**
     * Search human tasks administered by the given user.
     * 
     * @param managerUserId
     *            Identifier of a user
     * @param searchOptions
     *            search conditions and set sort,paging properties.
     * @return the number of human tasks found and the matching human tasks
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws UserNotFoundException
     *             Error thrown if no user have an id corresponding to the value of managerUserId parameter
     * @throws SearchException
     *             if there's wrong search condition, error happened.e.g,set a String value to long attribute.
     */
    SearchResult<HumanTaskInstance> searchAssignedTasksManagedBy(long managerUserId, final SearchOptions searchOptions) throws InvalidSessionException,
            UserNotFoundException, SearchException;

    /**
     * search pending human tasks supervised by the given user.
     * 
     * @param userId
     *            Identifier of a user
     * @param searchOptions
     *            search conditions and set sort,paging properties.
     * @return the number of human tasks found and the matching human tasks
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws UserNotFoundException
     *             Error thrown if no user have an id corresponding to the value of userId parameter
     */
    SearchResult<HumanTaskInstance> searchPendingTasksSupervisedBy(long userId, final SearchOptions searchOptions) throws InvalidSessionException,
            UserNotFoundException;

    SearchResult<HumanTaskInstance> searchPendingTasksForUser(long userId, final SearchOptions searchOptions) throws InvalidSessionException,
            UserNotFoundException;

    /**
     * Search pending human tasks administered by the given user.
     * 
     * @param managerUserId
     *            same to user id
     * @param searchOptions
     *            search conditions and set sort,paging properties.
     * @return the number of human tasks found and the matching human tasks
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws UserNotFoundException
     *             Error thrown if no user have an id corresponding to the value of managerUserId parameter
     * @throws SearchException
     *             if there's wrong search condition, error happened.e.g,set a String value to long attribute.
     */
    SearchResult<HumanTaskInstance> searchPendingTasksManagedBy(long managerUserId, final SearchOptions searchOptions) throws InvalidSessionException,
            UserNotFoundException, SearchException;

    /**
     * Get the number of both assigned and pending overdue tasks for each user
     * 
     * @param userIds
     *            a list of user identifiers
     * @return a map with user id and relative number of overdue tasks
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActivityInstanceReadException
     *             errors happen in while retrieving the instance of activity
     */
    Map<Long, Long> getNumberOfOverdueOpenTasks(List<Long> userIds) throws InvalidSessionException, ActivityInstanceReadException;

    /**
     * Update an instance of process with the given processInstanceId.
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @param updateDescriptor
     *            including new value of all attributes adaptable
     * @return the process instance updated
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws ProcessInstanceModificationException
     *             error thrown while updating the instance of process failed.
     */
    ProcessInstance updateProcessInstance(long processInstanceId, ProcessInstanceUpdateDescriptor updateDescriptor) throws InvalidSessionException,
            ProcessInstanceNotFoundException, ProcessInstanceModificationException;

    /**
     * Update an index of a process instance.
     * 
     * @param processInstanceId
     *            identifier of the process instance
     * @param index
     *            which index to update
     * @param value
     *            the new value for the index
     * @return the updated process instance
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws ProcessInstanceModificationException
     *             Error thrown while updating the instance of process failed.
     */
    ProcessInstance updateProcessInstanceIndex(long processInstanceId, Index index, String value) throws InvalidSessionException,
            ProcessInstanceNotFoundException, ProcessInstanceModificationException;

    /**
     * Cancel an instance of process with the given processInstanceId.
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws ProcessInstanceReadException
     *             Error happened when can't retrieve the instance of process.
     * @throws ProcessInstanceModificationException
     *             Error thrown while updating the instance of process failed.
     */
    void cancelProcessInstance(long processInstanceId) throws InvalidSessionException, ProcessInstanceNotFoundException, ProcessInstanceReadException,
            ProcessInstanceModificationException;

    /**
     * set state of activity to its previous state and then execute.
     * precondition: the activity is in state FAILED
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActivityNotFoundException
     *             errors thrown if can't find corresponding activity
     * @throws RetryTaskException
     *             errors happened when one of the two step that re-set state of the task and execute it again failed.
     */
    void retryTask(long activityInstanceId) throws InvalidSessionException, ActivityNotFoundException, RetryTaskException;

    /**
     * Start an instance of the process definition on behalf of a given user, and set the initial values of the data with the given operations.
     * 
     * @param userId
     *            the id of the user to start the process on behalf of
     * @param processDefinitionId
     *            Identifier of the process definition will be started
     * @param operations
     *            the operations to execute to set the initial values of the data
     * @return an instance of the process
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             No matching process definition found
     * @throws ProcessInstanceCreationException
     *             the exception maybe happened when the instance of process is inserted into DataBase
     * @throws ProcessDefinitionReadException
     *             some errors are occurred when retrieve the matching process definition by given processDefinitionId
     * @throws ProcessDefinitionNotEnabledException
     *             a primary precondition of starting a process is the status of matching process definition should be enabled, if not,the exception occur.
     * @throws OperationExecutionException
     *             in case of exception during the process of executing one of the operations
     * @throws UserNotFoundException
     *             in case this userId is not found.
     * @see {@link #startProcess(String, long, Map)}
     */
    ProcessInstance startProcess(long userId, long processDefinitionId, Map<Operation, Map<String, Serializable>> operations) throws InvalidSessionException,
            ProcessDefinitionNotFoundException, ProcessInstanceCreationException, ProcessDefinitionReadException, ProcessDefinitionNotEnabledException,
            OperationExecutionException, UserNotFoundException;

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
     * @throws TaskHidingException
     *             in case a problem occurs when hiding one of the tasks.
     * @see #unhideTasks(long, Long...)
     * @since 6.0
     */
    void hideTasks(long userId, Long... activityInstanceId) throws InvalidSessionException, TaskHidingException;

    /**
     * Un-hides a list of tasks from a specified user. Un-hiding a task makes it available for a user if the task is pending for that user.
     * 
     * @param userId
     *            the ID of the user to un-hide the tasks for.
     * @param activityInstanceId
     *            the ID of the task to un-hide
     * @throws InvalidSessionException
     *             in there is no current valid session.
     * @throws TaskHidingException
     *             in case a problem occurs when un-hiding one of the tasks.
     * @see #hideTasks(long, Long...)
     * @since 6.0
     */
    void unhideTasks(long userId, Long... activityInstanceId) throws InvalidSessionException, TaskHidingException;

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
     */
    Serializable evaluateExpressionOnProcessDefinition(Expression expression, Map<String, Serializable> context, long processDefinitionId)
            throws InvalidSessionException, ExpressionEvaluationException;

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
     * @throws ActivityInstanceReadException
     */
    boolean isTaskHidden(long userTaskId, long userId) throws InvalidSessionException, ActivityInstanceReadException;

    /**
     * Query the engine to get the number of comments depending on the search options
     * 
     * @param searchOptions
     *            Search conditions and set sort,paging properties
     * @return
     *         The number of comments
     */
    long countComments(final SearchOptions searchOptions) throws PageOutOfRangeException, InvalidSessionException, ProcessInstanceNotFoundException;

    /**
     * Query the engine to get the number of attachments depending on the search options
     * 
     * @param searchOptions
     *            Search conditions and set sorts, paging properties
     * @return
     *         The number of attachments
     */

    long countAttachments(final SearchOptions searchOptions) throws InvalidSessionException, SearchException;

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
    void sendSignal(String signalName) throws InvalidSessionException, SendEventException;

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
            throws InvalidSessionException, SendEventException;

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
            Map<Expression, Expression> correlations) throws InvalidSessionException, SendEventException;

    /**
     * @param archivedProcessInstanceId
     *            the id of the archived process instance
     * @return
     * @throws InvalidSessionException
     * @throws ProcessInstanceNotFoundException
     * @throws ProcessInstanceReadException
     */
    ArchivedProcessInstance getArchivedProcessInstance(long archivedProcessInstanceId) throws InvalidSessionException, ProcessInstanceNotFoundException,
            ProcessInstanceReadException;

    /**
     * @param archivedFlowNodeInstanceId
     * @return
     * @throws InvalidSessionException
     * @throws ActivityInstanceNotFoundException
     * @throws ActivityInstanceReadException
     */
    ArchivedFlowNodeInstance getArchivedFlowNodeInstance(long archivedFlowNodeInstanceId) throws InvalidSessionException, ActivityInstanceNotFoundException,
            ActivityInstanceReadException;

    /**
     * @param archivedCommentId
     * @return
     * @throws InvalidSessionException
     * @throws CommentReadException
     * @throws ObjectNotFoundException
     */
    ArchivedComment getArchivedComment(long archivedCommentId) throws InvalidSessionException, CommentReadException, ObjectNotFoundException;

}
