/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.connector.ConnectorExecutionException;
import org.bonitasoft.engine.bpm.connector.ConnectorInstanceNotFoundException;
import org.bonitasoft.engine.bpm.connector.ConnectorInstanceWithFailureInfo;
import org.bonitasoft.engine.bpm.connector.ConnectorNotFoundException;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ManualTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.InvalidSessionException;

import com.bonitasoft.engine.bpm.flownode.ManualTaskCreator;
import com.bonitasoft.engine.bpm.process.Index;
import com.bonitasoft.engine.bpm.process.impl.ProcessInstanceUpdater;
import com.bonitasoft.engine.businessdata.BusinessDataReference;

/**
 * {@link ProcessRuntimeAPI} extends {@link org.bonitasoft.engine.api.ProcessRuntimeAPI} and adds capabilities on Manual tasks, connector execution (directly
 * available at API level), search index updating.
 *
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public interface ProcessRuntimeAPI extends org.bonitasoft.engine.api.ProcessRuntimeAPI {

    /**
     * Add a manual task with given human task id.
     *
     * @param creator
     *            the manual task creator
     * @return the matching an instance of manual task
     * @throws CreationException
     *             if the manual task cannot be created.
     * @throws AlreadyExistsException
     *             if the provided manual task already exists.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    ManualTaskInstance addManualUserTask(ManualTaskCreator creator) throws CreationException, AlreadyExistsException;

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
     * @throws ConnectorExecutionException
     *             if an error occurs when trying to execute the connector
     * @throws ConnectorNotFoundException
     *             if the specified connector is not found
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorAtProcessInstantiation(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long processInstanceId)
                    throws ConnectorExecutionException, ConnectorNotFoundException;

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
     *            all parameters values for operations
     * @param processInstanceId
     *            Identifier of the process instance
     * @return a map with new values of elements set by the operations
     * @throws ConnectorExecutionException
     *             if the connector failed to execute properly.
     * @throws ConnectorNotFoundException
     *             if the connector cannot be found with the provided connectorDefinitionId + connectorDefinitionVersion
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorAtProcessInstantiation(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, List<Operation> operations,
            Map<String, Serializable> operationsInputValues, long processInstanceId) throws ConnectorExecutionException, ConnectorNotFoundException;

    /**
     * Execute connector in given activity instance.
     *
     * @param connectorDefinitionId
     *        Identifier of connector definition
     * @param connectorDefinitionVersion
     *        version of the connector definition
     * @param connectorInputParameters
     *        all expressions related with the connector
     * @param inputValues
     *        all parameters values for expression need when evaluate the connector
     * @param activityInstanceId
     *        Identifier of the activity instance
     * @return a map with connector parameter name and parameter value object
     * @throws ConnectorExecutionException
     *         if the connector failed to execute properly.
     * @throws ConnectorNotFoundException
     *         if the connector cannot be found with the provided connectorDefinitionId + connectorDefinitionVersion
     * @throws InvalidSessionException
     *         if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnActivityInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long activityInstanceId)
                    throws ConnectorExecutionException, ConnectorNotFoundException;

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
     *            the map of input name-value pairs used by operation executions.
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @return a map with new values of elements set by the operations
     * @throws ConnectorExecutionException
     *             if the connector failed to execute properly.
     * @throws ConnectorNotFoundException
     *             if the connector cannot be found with the provided connectorDefinitionId + connectorDefinitionVersion
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnActivityInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, List<Operation> operations,
            Map<String, Serializable> operationsInputValues, long activityInstanceId) throws ConnectorExecutionException, ConnectorNotFoundException;

    /**
     * Execute connector in given activity instance finished.
     *
     * @param connectorDefinitionId
     *        Identifier of connector definition
     * @param connectorDefinitionVersion
     *        version of the connector definition
     * @param connectorInputParameters
     *        all expressions related with the connector
     * @param inputValues
     *        all parameters values for expression need when evaluate the connector
     * @param activityInstanceId
     *        Identifier of the activity instance
     * @return a map with connector parameter name and parameter value object
     * @throws ConnectorExecutionException
     *         if the connector failed to execute properly.
     * @throws ConnectorNotFoundException
     *         if the connector cannot be found with the provided connectorDefinitionId + connectorDefinitionVersion
     * @throws InvalidSessionException
     *         if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnCompletedActivityInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long activityInstanceId)
                    throws ConnectorExecutionException, ConnectorNotFoundException;

    /**
     * Execute connector in given activity instance finished.
     *
     * @param connectorDefinitionId
     *        Identifier of connector definition
     * @param connectorDefinitionVersion
     *        version of the connector definition
     * @param connectorInputParameters
     *        all expressions related with the connector
     * @param inputValues
     *        all parameters values for expression need when evaluate the connector
     * @param operations
     *        map of operations having each a special context (input values)
     * @param operationsInputValues
     *        the map of input name-value pairs used by operation executions.
     * @param activityInstanceId
     *        Identifier of the activity instance
     * @return a map with new values of elements set by the operations
     * @throws ConnectorExecutionException
     *         if the connector failed to execute properly.
     * @throws ConnectorNotFoundException
     *         if the connector cannot be found with the provided connectorDefinitionId + connectorDefinitionVersion
     * @throws InvalidSessionException
     *         if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnCompletedActivityInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, List<Operation> operations,
            Map<String, Serializable> operationsInputValues, long activityInstanceId) throws ConnectorExecutionException, ConnectorNotFoundException;

    /**
     * Execute connector in given process instance finished.
     *
     * @param connectorDefinitionId
     *        Identifier of connector definition
     * @param connectorDefinitionVersion
     *        version of the connector definition
     * @param connectorInputParameters
     *        all expressions related with the connector
     * @param inputValues
     *        all parameters values for expression need when evaluate the connector
     * @param processInstanceId
     *        Identifier of the process instance
     * @return a map with connector parameter name and parameter value object
     * @throws ConnectorExecutionException
     *         if the connector failed to execute properly.
     * @throws ConnectorNotFoundException
     *         if the connector cannot be found with the provided connectorDefinitionId + connectorDefinitionVersion
     * @throws InvalidSessionException
     *         if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnCompletedProcessInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long processInstanceId)
                    throws ConnectorExecutionException, ConnectorNotFoundException;

    /**
     * Execute connector in given process instance finished with operations.
     *
     * @param connectorDefinitionId
     *        Identifier of connector definition
     * @param connectorDefinitionVersion
     *        version of the connector definition
     * @param connectorInputParameters
     *        all expressions related with the connector
     * @param inputValues
     *        all parameters values for expression need when evaluate the connector
     * @param operations
     *        map of operations having each a special context (input values)
     * @param operationsInputValues
     *        the map of input name-value pairs used by operation executions.
     * @param processInstanceId
     *        Identifier of the process instance
     * @return a map with new values of elements set by the operations
     * @throws ConnectorExecutionException
     *         if the connector failed to execute properly.
     * @throws ConnectorNotFoundException
     *         if the connector cannot be found with the provided connectorDefinitionId + connectorDefinitionVersion
     * @throws InvalidSessionException
     *         if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnCompletedProcessInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, List<Operation> operations,
            Map<String, Serializable> operationsInputValues, long processInstanceId) throws ConnectorExecutionException, ConnectorNotFoundException;

    /**
     * Execute connector in given process instance.
     *
     * @param connectorDefinitionId
     *        Identifier of connector definition
     * @param connectorDefinitionVersion
     *        version of the connector definition
     * @param connectorInputParameters
     *        all expressions related with the connector
     * @param inputValues
     *        all parameters values for expression need when evaluate the connector
     * @param processInstanceId
     *        Identifier of the process instance
     * @return a map with connector parameter name and parameter value object
     * @throws ConnectorExecutionException
     *         if the connector failed to execute properly.
     * @throws ConnectorNotFoundException
     *         if the connector cannot be found with the provided connectorDefinitionId + connectorDefinitionVersion
     * @throws InvalidSessionException
     *         if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnProcessInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, long processInstanceId)
                    throws ConnectorExecutionException, ConnectorNotFoundException;

    /**
     * Execute connector in given process instance with operations
     *
     * @param connectorDefinitionId
     *        Identifier of connector definition
     * @param connectorDefinitionVersion
     *        version of the connector definition
     * @param connectorInputParameters
     *        all expressions related with the connector
     * @param inputValues
     *        all parameters values for expression need when evaluate the connector
     * @param processInstanceId
     *        Identifier of the process instance
     * @param operations
     *        map of operations having each a special context (input values)
     * @param operationsInputValues
     *        the map of input name-value pairs used by operation executions.
     * @return a map with new values of elements set by the operations
     * @throws ConnectorExecutionException
     *         if the connector failed to execute properly.
     * @throws ConnectorNotFoundException
     *         if the connector cannot be found with the provided connectorDefinitionId + connectorDefinitionVersion
     * @throws InvalidSessionException
     *         if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    Map<String, Serializable> executeConnectorOnProcessInstance(String connectorDefinitionId, String connectorDefinitionVersion,
            Map<String, Expression> connectorInputParameters, Map<String, Map<String, Serializable>> inputValues, List<Operation> operations,
            Map<String, Serializable> operationsInputValues, long processInstanceId) throws ConnectorExecutionException, ConnectorNotFoundException;

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
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws UpdateException
     *             if an error is thrown while updating the process instance.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    ProcessInstance updateProcessInstanceIndex(long processInstanceId, Index index, String value) throws ProcessInstanceNotFoundException, UpdateException;

    /**
     * Update an instance of process with the given processInstanceId.
     *
     * @param processInstanceId
     *            Identifier of the process instance
     * @param updater
     *            including new value of all attributes adaptable
     * @return the process instance updated
     * @throws ProcessInstanceNotFoundException
     *             Error thrown if no process instance have an id corresponding to the value of processInstanceId parameter.
     * @throws UpdateException
     *             if an error is thrown while updating the process instance.
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    ProcessInstance updateProcessInstance(long processInstanceId, ProcessInstanceUpdater updater) throws ProcessInstanceNotFoundException, UpdateException;

    /**
     * Retrieves a <code>ConnectorInstanceWithFailureInfo</code> specified by its identifier.
     *
     * @param connectorInstanceId
     *            the identifier of the <code>ConnectorInstanceWithFailureInfo</code> to be retrieved.
     * @return the <code>ConnectorInstanceWithFailureInfo</code> instance.
     * @throws ConnectorInstanceNotFoundException
     *             if no <code>ConnectorInstanceWithFailureInfo</code> is found with the specified connectorInstanceId.
     * @since 6.1
     */
    ConnectorInstanceWithFailureInfo getConnectorInstanceWithFailureInformation(long connectorInstanceId) throws ConnectorInstanceNotFoundException;

    /**
     * Search for archived flow node instances (activities, gateways and events) supervised by the specified user.
     *
     * @param supervisorId
     *            The identifier of the supervising user.
     * @param searchOptions
     *            The search options (pagination, filter, order sort).
     * @return A {@link SearchResult} containing the search result.
     * @throws InvalidSessionException
     *             If the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             If the search could not be completed correctly.
     * @since 6.3
     */
    SearchResult<ArchivedFlowNodeInstance> searchArchivedFlowNodeInstancesSupervisedBy(long supervisorId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search for archived flow node instances (activities, gateways and events) supervised by the specified user.
     *
     * @param supervisorId
     *            The identifier of the supervising user.
     * @param searchOptions
     *            The search options (pagination, filter, order sort).
     * @return A {@link SearchResult} containing the search result.
     * @throws InvalidSessionException
     *             If the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             If the search could not be completed correctly.
     * @since 6.3
     */
    SearchResult<ArchivedActivityInstance> searchArchivedActivityInstancesSupervisedBy(long supervisorId, SearchOptions searchOptions) throws SearchException;

    /**
     * Search for flow node instances (activities, gateways and events) supervised by the specified user.
     *
     * @param supervisorId
     *            The identifier of the supervising user.
     * @param searchOptions
     *            The search options (pagination, filter, order sort).
     * @return A {@link SearchResult} containing the search result.
     * @throws InvalidSessionException
     *             If the session is invalid, e.g. the session has expired.
     * @throws SearchException
     *             If the search could not be completed correctly.
     * @since 6.3
     */
    SearchResult<FlowNodeInstance> searchFlowNodeInstancesSupervisedBy(long supervisorId, SearchOptions searchOptions) throws SearchException;

    /**
     * Returns the {@link BusinessDataReference} of the named business data of the process instance.
     * The value is returned in a DataInstance object.
     *
     * @param businessDataName
     *        The name of the business data
     * @param processInstanceId
     *        The identifier of the process instance
     * @return the reference of the business data
     * @throws InvalidSessionException
     *         If the session is invalid, e.g. the session has expired.
     * @throws DataNotFoundException
     *         If the specified business data value cannot be found.
     * @since 6.4
     */
    BusinessDataReference getProcessBusinessDataReference(String businessDataName, long processInstanceId) throws DataNotFoundException;

    /**
     * Lists the paginated @link BusinessDataReference}s of the process instance order by identifier.
     *
     * @param processInstanceId
     *        The identifier of the process instance
     * @param startIndex
     *        the index of the first result (starting from 0).
     * @param maxResults
     *        the maximum number of result per page
     * @return the paginated references of the business data
     * @since 6.4
     */
    List<BusinessDataReference> getProcessBusinessDataReferences(long processInstanceId, int startIndex, int maxResults);

}
