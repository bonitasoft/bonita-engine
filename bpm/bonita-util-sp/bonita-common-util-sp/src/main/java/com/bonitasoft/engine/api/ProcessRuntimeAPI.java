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
import java.util.Map;

import org.bonitasoft.engine.bpm.model.ManualTaskInstance;
import org.bonitasoft.engine.bpm.model.TaskPriority;
import org.bonitasoft.engine.core.operation.Operation;
import org.bonitasoft.engine.exception.ActivityCreationException;
import org.bonitasoft.engine.exception.ActivityExecutionErrorException;
import org.bonitasoft.engine.exception.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.exception.ActivityInterruptedException;
import org.bonitasoft.engine.exception.ActivityNotFoundException;
import org.bonitasoft.engine.exception.ArchivedActivityInstanceNotFoundException;
import org.bonitasoft.engine.exception.ArchivedProcessInstanceNotFoundException;
import org.bonitasoft.engine.exception.ClassLoaderException;
import org.bonitasoft.engine.exception.ConnectorException;
import org.bonitasoft.engine.exception.InvalidEvaluationConnectorConditionException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.NotSerializableException;
import org.bonitasoft.engine.exception.ObjectDeletionException;
import org.bonitasoft.engine.exception.ObjectNotFoundException;
import org.bonitasoft.engine.exception.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.expression.Expression;

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
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActivityExecutionErrorException
     *             An unexpected error occurred while executing the user task
     * @throws ActivityInterruptedException
     *             The activity was interrupted
     * @throws ActivityCreationException
     * @throws ActivityNotFoundException
     *             errors thrown if can't find corresponding activity
     * since 6.0
     */
    ManualTaskInstance addManualUserTask(long humanTaskId, String taskName, String displayName, long assignTo, String description, Date dueDate,
            TaskPriority priority) throws InvalidSessionException, ActivityInterruptedException, ActivityExecutionErrorException, ActivityCreationException,
            ActivityNotFoundException;

    void deleteManualUserTask(final long manualTaskId) throws InvalidSessionException, ObjectDeletionException, ObjectNotFoundException;

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
     * @throws InvalidEvaluationConnectorConditionException
     * @throws NotSerializableException
     *             error thrown when connector outputs are not serializable
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorAtProcessInstantiation(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long processInstanceId)
            throws InvalidSessionException, ArchivedProcessInstanceNotFoundException, ClassLoaderException, ConnectorException,
            InvalidEvaluationConnectorConditionException, NotSerializableException;

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
     * @param processInstanceId
     *            Identifier of the process instance
     * @param processInstanceId
     *            Identifier of the process instance
     * @return a map with new values of elements set by the operations
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
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
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues,
            Map<Operation, Map<String, Serializable>> operations, long processInstanceId) throws InvalidSessionException,
            ArchivedProcessInstanceNotFoundException, ClassLoaderException, ConnectorException, InvalidEvaluationConnectorConditionException, NotSerializableException;

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
     * @throws InvalidEvaluationConnectorConditionException
     * @throws NotSerializableException
     *             error thrown when connector outputs are not serializable
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnActivityInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long activityInstanceId)
            throws InvalidSessionException, ActivityInstanceNotFoundException, ProcessInstanceNotFoundException, ClassLoaderException, ConnectorException,
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
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return a map with new values of elements set by the operations
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
     * @throws InvalidEvaluationConnectorConditionException
     * @throws NotSerializableException
     *             error thrown when connector outputs are not serializable
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnActivityInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues,
            Map<Operation, Map<String, Serializable>> operations, long activityInstanceId) throws InvalidSessionException, ActivityInstanceNotFoundException,
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
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
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
            throws InvalidSessionException, ArchivedActivityInstanceNotFoundException, ProcessInstanceNotFoundException, ClassLoaderException,
            ConnectorException, InvalidEvaluationConnectorConditionException, NotSerializableException;

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
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return a map with new values of elements set by the operations
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
     * @throws InvalidEvaluationConnectorConditionException
     * @throws NotSerializableException
     *             error thrown when connector outputs are not serializable
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnCompletedActivityInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues,
            Map<Operation, Map<String, Serializable>> operations, long activityInstanceId) throws InvalidSessionException,
            ArchivedActivityInstanceNotFoundException, ProcessInstanceNotFoundException, ClassLoaderException, ConnectorException,
            InvalidEvaluationConnectorConditionException, NotSerializableException;

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
     * @throws InvalidEvaluationConnectorConditionException
     * @throws NotSerializableException
     *             error thrown when connector outputs are not serializable
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnCompletedProcessInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long processInstanceId)
            throws InvalidSessionException, ArchivedProcessInstanceNotFoundException, ClassLoaderException, ConnectorException,
            InvalidEvaluationConnectorConditionException, NotSerializableException;

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
     * @param processInstanceId
     *            Identifier of the process instance
     * @return a map with new values of elements set by the operations
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
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
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues,
            Map<Operation, Map<String, Serializable>> operations, long processInstanceId) throws InvalidSessionException,
            ArchivedProcessInstanceNotFoundException, ClassLoaderException, ConnectorException, InvalidEvaluationConnectorConditionException, NotSerializableException;

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
     * @throws InvalidEvaluationConnectorConditionException
     * @throws NotSerializableException
     *             error thrown when connector outputs are not serializable
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnProcessInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long processInstanceId)
            throws ClassLoaderException, InvalidSessionException, ProcessInstanceNotFoundException, ConnectorException, InvalidEvaluationConnectorConditionException,
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
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
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
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues,
            Map<Operation, Map<String, Serializable>> operations, long processInstanceId) throws ClassLoaderException, InvalidSessionException,
            ProcessInstanceNotFoundException, ConnectorException, InvalidEvaluationConnectorConditionException, NotSerializableException;
}
