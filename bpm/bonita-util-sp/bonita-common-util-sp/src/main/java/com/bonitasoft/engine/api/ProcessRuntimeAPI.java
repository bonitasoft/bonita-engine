/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.model.Index;
import org.bonitasoft.engine.bpm.model.ManualTaskInstance;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.bpm.model.TaskPriority;
import org.bonitasoft.engine.core.operation.Operation;
import org.bonitasoft.engine.exception.ClassLoaderException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.NotSerializableException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.exception.activity.ActivityExecutionErrorException;
import org.bonitasoft.engine.exception.activity.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.exception.activity.ActivityInterruptedException;
import org.bonitasoft.engine.exception.activity.ActivityNotFoundException;
import org.bonitasoft.engine.exception.activity.ArchivedActivityInstanceNotFoundException;
import org.bonitasoft.engine.exception.connector.ConnectorException;
import org.bonitasoft.engine.exception.connector.ConnectorExecutionException;
import org.bonitasoft.engine.exception.connector.ConnectorNotFoundException;
import org.bonitasoft.engine.exception.connector.InvalidEvaluationConnectorConditionException;
import org.bonitasoft.engine.exception.platform.InvalidSessionException;
import org.bonitasoft.engine.exception.process.ArchivedProcessInstanceNotFoundException;
import org.bonitasoft.engine.exception.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.exception.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.expression.Expression;

import com.bonitasoft.engine.bpm.model.ProcessInstanceUpdateDescriptor;

/**
 * @author Matthieu Chaffotte
 */
public interface ProcessRuntimeAPI extends org.bonitasoft.engine.api.ProcessRuntimeAPI {

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
     *             if the session is invalid, e.g. the session has expired.
     * @throws ActivityExecutionErrorException
     *             An unexpected error occurred while executing the user task
     * @throws ActivityInterruptedException
     *             The activity was interrupted
     * @throws ActivityCreationException
     * @throws ActivityNotFoundException
     *             errors thrown if can't find corresponding activity
     *             since 6.0
     */
    ManualTaskInstance addManualUserTask(long humanTaskId, String taskName, String displayName, long assignTo, String description, Date dueDate,
            TaskPriority priority) throws ActivityInterruptedException, ActivityExecutionErrorException, CreationException, ActivityNotFoundException;

    /**
     * Delete a manual task. Only manual tasks can be deleted at runtime.
     * 
     * @param manualTaskId
     *            the id of the task to delete
     * @throws DeletionException
     *             if the manual task could not be deleted.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     */
    void deleteManualUserTask(final long manualTaskId) throws DeletionException;

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
     * @throws NotSerializableException
     *             error thrown when connector outputs are not serializable
     * @throws ConnectorExecutionException
     *             TODO
     * @throws ConnectorNotFoundException TODO
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorAtProcessInstantiation(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long processInstanceId)
            throws NotSerializableException, ConnectorExecutionException, ConnectorNotFoundException;

    /**
     * Execute connector in given process instance initialized with operations.
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
     * @param operationsInputValues
     *            TODO
     * @param processInstanceId
     *            Identifier of the process instance
     * @return a map with new values of elements set by the operations
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ArchivedProcessInstanceNotFoundException
     * @throws ClassLoaderException
     *             errors thrown while loading class failed.
     * @throws ConnectorException
     *             error thrown when connect external application failed.
     * @throws InvalidEvaluationConnectorConditionException
     * @throws NotSerializableException
     *             error thrown when connector outputs are not serializable
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorAtProcessInstantiation(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, List<Operation> operations,
            Map<String, Serializable> operationsInputValues, long processInstanceId) throws ArchivedProcessInstanceNotFoundException, ClassLoaderException,
            ConnectorException, InvalidEvaluationConnectorConditionException, NotSerializableException;

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
     *             if the session is invalid, e.g. the session has expired.
     * @throws ActivityInstanceNotFoundException
     *             if the activity cannot be found
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws ClassLoaderException
     *             errors thrown while loading class failed.
     * @throws ConnectorException
     *             error thrown when connect external application failed.
     * @throws InvalidEvaluationConnectorConditionException
     * @throws NotSerializableException
     *             error thrown when connector outputs are not serializable
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnActivityInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long activityInstanceId)
            throws ActivityInstanceNotFoundException, ProcessInstanceNotFoundException, ClassLoaderException, ConnectorException,
            InvalidEvaluationConnectorConditionException, NotSerializableException;

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
     *            all parameters values for expression need when evaluate the connector
     * @param operations
     *            map of operations having each a special context (input values)
     * @param operationsInputValues
     *            TODO
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return a map with new values of elements set by the operations
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ActivityInstanceNotFoundException
     *             if the activity cannot be found
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws ClassLoaderException
     *             errors thrown while loading class failed.
     * @throws ConnectorException
     *             error thrown when connect external application failed.
     * @throws InvalidEvaluationConnectorConditionException
     * @throws NotSerializableException
     *             error thrown when connector outputs are not serializable
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnActivityInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, List<Operation> operations,
            Map<String, Serializable> operationsInputValues, long activityInstanceId) throws ActivityInstanceNotFoundException,
            ProcessInstanceNotFoundException, ClassLoaderException, ConnectorException, InvalidEvaluationConnectorConditionException, NotSerializableException;

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
     *             if the session is invalid, e.g. the session has expired.
     * @throws ArchivedActivityInstanceNotFoundException
     *             if the activity cannot be found
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws ClassLoaderException
     *             errors thrown while loading class failed.
     * @throws ConnectorException
     *             error thrown when connect external application failed.
     * @throws InvalidEvaluationConnectorConditionException
     * @throws NotSerializableException
     *             error thrown when connector outputs are not serializable
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnCompletedActivityInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long activityInstanceId)
            throws ArchivedActivityInstanceNotFoundException, ProcessInstanceNotFoundException, ClassLoaderException, ConnectorException,
            InvalidEvaluationConnectorConditionException, NotSerializableException;

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
     * @param operations
     *            map of operations having each a special context (input values)
     * @param operationsInputValues
     *            TODO
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return a map with new values of elements set by the operations
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ArchivedActivityInstanceNotFoundException
     *             if the activity cannot be found
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws ClassLoaderException
     *             errors thrown while loading class failed.
     * @throws ConnectorException
     *             error thrown when connect external application failed.
     * @throws InvalidEvaluationConnectorConditionException
     * @throws NotSerializableException
     *             error thrown when connector outputs are not serializable
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnCompletedActivityInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, List<Operation> operations,
            Map<String, Serializable> operationsInputValues, long activityInstanceId) throws ArchivedActivityInstanceNotFoundException,
            ProcessInstanceNotFoundException, ClassLoaderException, ConnectorException, InvalidEvaluationConnectorConditionException, NotSerializableException;

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
     *             if the session is invalid, e.g. the session has expired.
     * @throws ArchivedProcessInstanceNotFoundException
     * @throws ClassLoaderException
     *             errors thrown while loading class failed.
     * @throws ConnectorException
     *             error thrown when connect external application failed.
     * @throws InvalidEvaluationConnectorConditionException
     * @throws NotSerializableException
     *             error thrown when connector outputs are not serializable
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnCompletedProcessInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long processInstanceId)
            throws ArchivedProcessInstanceNotFoundException, ClassLoaderException, ConnectorException, InvalidEvaluationConnectorConditionException,
            NotSerializableException;

    /**
     * Execute connector in given process instance finished with operations.
     * 
     * @param connectorDefinitionId
     *            Identifier of connector definition
     * @param connectorDefinitionVersion
     *            version of the connector definition
     * @param connectorInputParameters
     *            all expressions related with the connector
     * @param inputValues
     *            all parameters values for expression need when evalute the connector
     * @param operations
     *            map of operations having each a special context (input values)
     * @param operationsInputValues
     *            TODO
     * @param processInstanceId
     *            Identifier of the process instance
     * @return a map with new values of elements set by the operations
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ArchivedProcessInstanceNotFoundException
     * @throws ClassLoaderException
     *             errors thrown while loading class failed.
     * @throws ConnectorException
     *             error thrown when connect external application failed.
     * @throws InvalidEvaluationConnectorConditionException
     * @throws NotSerializableException
     *             error thrown when connector outputs are not serializable
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnCompletedProcessInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, List<Operation> operations,
            Map<String, Serializable> operationsInputValues, long processInstanceId) throws ArchivedProcessInstanceNotFoundException, ClassLoaderException,
            ConnectorException, InvalidEvaluationConnectorConditionException, NotSerializableException;

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
     *             if the session is invalid, e.g. the session has expired.
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws ConnectorException
     *             error thrown when connect external application failed.
     * @throws InvalidEvaluationConnectorConditionException
     * @throws NotSerializableException
     *             error thrown when connector outputs are not serializable
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnProcessInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long processInstanceId)
            throws ClassLoaderException, ProcessInstanceNotFoundException, ConnectorException, InvalidEvaluationConnectorConditionException,
            NotSerializableException;

    /**
     * Execute connector in given process instance with operations
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
     * @param operations
     *            map of operations having each a special context (input values)
     * @return a map with new values of elements set by the operations
     * @throws ClassLoaderException
     *             errors thrown while loading class failed.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws ConnectorException
     *             error thrown when connect external application failed.
     * @throws InvalidEvaluationConnectorConditionException
     * @throws NotSerializableException
     *             error thrown when connector outputs are not serializable
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnProcessInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, List<Operation> operations,
            Map<String, Serializable> operationsInputValues, long processInstanceId) throws ClassLoaderException, ProcessInstanceNotFoundException,
            ConnectorException, InvalidEvaluationConnectorConditionException, NotSerializableException;

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
     *             if the session is invalid, e.g. the session has expired.
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws ProcessInstanceModificationException
     *             Error thrown while updating the instance of process failed.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no process definition have an id corresponding to the value of processDefinitionId of process instance with processInstanceId
     *             parameter.
     * @since 6.0
     */
    ProcessInstance updateProcessInstanceIndex(long processInstanceId, Index index, String value) throws ProcessInstanceNotFoundException, UpdateException,
            ProcessDefinitionNotFoundException;

    /**
     * Update an instance of process with the given processInstanceId.
     * 
     * @param processInstanceId
     *            Identifier of the process instance
     * @param updateDescriptor
     *            including new value of all attributes adaptable
     * @return the process instance updated
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws ProcessInstanceModificationException
     *             error thrown while updating the instance of process failed.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no process definition have an id corresponding to the value of processDefinitionId of process instance with processInstanceId
     *             parameter.
     * @since 6.0
     */
    ProcessInstance updateProcessInstance(long processInstanceId, ProcessInstanceUpdateDescriptor updateDescriptor) throws ProcessInstanceNotFoundException,
            UpdateException, ProcessDefinitionNotFoundException;

}
