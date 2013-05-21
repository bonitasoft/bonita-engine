/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.ActivityExecutionException;
import org.bonitasoft.engine.bpm.flownode.ActivityNotFoundException;
import org.bonitasoft.engine.bpm.instance.ConnectorInstance;
import org.bonitasoft.engine.bpm.model.ConnectorStateReset;
import org.bonitasoft.engine.connector.ConnectorInstanceCriterion;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.exception.connector.ConnectorInstanceNotFoundException;
import org.bonitasoft.engine.exception.connector.InvalidConnectorImplementationException;
import org.bonitasoft.engine.exception.platform.InvalidSessionException;

import com.bonitasoft.engine.bpm.model.ParameterInstance;
import com.bonitasoft.engine.exception.ImportParameterException;
import com.bonitasoft.engine.exception.ParameterNotFoundException;

/**
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public interface ProcessManagementAPI extends org.bonitasoft.engine.api.ProcessManagementAPI {

    /**
     * Gets how many parameters the process definition contains.
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @return the number of parameters of a process definition
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 6.0
     */
    int getNumberOfParameterInstances(long processDefinitionId);

    /**
     * Get a parameter instance by process definition UUID
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @param parameterName
     *            The parameter name for get ParameterInstance
     * @return the ParameterInstance of the process with processDefinitionUUID and name parameterName
     * @throws ParameterNotFoundException
     *             Error thrown if the given parameter is not found.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 6.0
     */
    ParameterInstance getParameterInstance(long processDefinitionId, String parameterName) throws ParameterNotFoundException;

    /**
     * Returns the parameters of a process definition or an empty map if the process does not contain any parameter.
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @param startIndex
     *            Index of the page to be returned. First page has index 0.
     * @param maxResults
     *            Number of result per page. Maximum number of result returned.
     * @param sort
     *            The criterion to sort the result
     * @return The ordered list of parameter instances
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 6.0
     */
    List<ParameterInstance> getParameterInstances(long processDefinitionId, int startIndex, int maxResults, ParameterSorting sort);

    /**
     * Update an existing parameter of a process definition.
     * 
     * @param processDefinitionId
     *            Identifier of the processDefinition
     * @param parameterName
     *            the parameter name
     * @param parameterValue
     *            the new value of the parameter
     * @throws ParameterNotFoundException
     *             Error thrown if the given parameter is not found.
     * @throws UpdateException
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 6.0
     */
    void updateParameterInstanceValue(long processDefinitionId, String parameterName, String parameterValue) throws ParameterNotFoundException, UpdateException;

    /**
     * Import the parameters by a processDefinition id and an array byte of parametersXML
     * 
     * @param pDefinitionId
     *            Identifier of the processDefinition
     * @param parametersXML
     *            The parameter with XML format.
     * @throws ImportParameterException
     * @throws InvalidSessionException
     *             if API Session is invalid, e.g session has expired.
     * @since 6.0
     */
    void importParameters(long pDefinitionId, byte[] parametersXML) throws ImportParameterException;

    /**
     * Retrieve the list of connector instances on an activity instance
     * 
     * @param activityInstanceId
     *            the id of the element on which we want the connector instances
     * @param startIndex
     * @param maxResults
     * @param sortingCriterion
     * @return
     *         the list of connector instance on this element
     * @since 6.0
     */
    List<ConnectorInstance> getConnectorInstancesOfActivity(long activityInstanceId, int startIndex, int maxResults, ConnectorInstanceCriterion sortingCriterion);

    /**
     * Retrieve the list of connector instances on a process instance
     * 
     * @param processInstanceId
     *            the id of the element on which we want the connector instances
     * @param startIndex
     * @param maxResults
     * @param sortingCriterion
     * @return
     *         the list of connector instance on this element
     * @since 6.0
     */
    List<ConnectorInstance> getConnectorInstancesOfProcess(long processInstanceId, int startIndex, int maxResults, ConnectorInstanceCriterion sortingCriterion);

    /**
     * Allows to reset the state of an instance of connector
     * 
     * @param connectorInstanceId
     *            the id of the connector to change
     * @param state
     *            the state to set on the connector
     * @throws UpdateException
     * @throws ConnectorInstanceNotFoundException
     *             TODO
     * @throws InvalidSessionException
     *             if no current valid engine session is found
     * @since 6.0
     */
    void setConnectorInstanceState(long connectorInstanceId, ConnectorStateReset state) throws UpdateException, ConnectorInstanceNotFoundException;

    /**
     * Allows to reset connector instance states for a Collection of connector instances at once.
     * 
     * @param connectorsToReset
     *            a Map containing, as key, the connector instance id, and as value, the <code>ConnectorStateReset</code> value to reset the connector instance
     *            to.
     * @throws ConnectorInstanceNotFoundException
     *             TODO
     * @throws UpdateException
     *             TODO
     * @throws InvalidSessionException
     *             if no current valid engine session is found
     * @since 6.0
     */
    void setConnectorInstanceState(final Map<Long, ConnectorStateReset> connectorsToReset) throws ConnectorInstanceNotFoundException, UpdateException;

    /**
     * Updates the implementation version of the connector of the process definition.
     * Removes the old the old .impl file, puts the new .impl file in the connector directory and reloads the cache.
     * 
     * @param processDefinitionId
     *            the identifier of the process definition.
     * @param connectorName
     *            the name of the connector.
     * @param connectorVersion
     *            the version of the connector.
     * @param connectorImplementationArchive
     *            the zipped .impl file contented as a byte array.
     * @throws InvalidConnectorImplementationException
     *             if the implementation is not valid. (e.g. wrong format)
     * @throws UpdateException
     * @throws InvalidSessionException
     *             if the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    void setConnectorImplementation(long processDefinitionId, String connectorName, String connectorVersion, byte[] connectorImplementationArchive)
            throws InvalidConnectorImplementationException, UpdateException;

    /**
     * set state of activity to its previous state and then execute.
     * precondition: the activity is in state FAILED
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @param connectorsToReset
     *            Map of connectors to reset before retrying the task
     * @throws ActivityNotFoundException
     *             errors thrown if can't find corresponding activity
     * @throws ActivityExecutionException
     *             TODO
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 6.0
     */
    void replayActivity(long activityInstanceId, Map<Long, ConnectorStateReset> connectorsToReset) throws ActivityNotFoundException, ActivityExecutionException;

    /**
     * Replay a task that was in failed state.
     * The task can be replayed if no connector is in state failed.
     * If that is the case change state of failed connectors first to SKIPPED of TO_BE_EXECUTED
     * 
     * @param activityInstanceId
     *            the activity to replay
     * @throws ActivityExecutionException
     *             TODO
     * @throws ActivityNotFoundException
     *             TODO
     * @throws InvalidSessionException
     *             When the activity can't be modified
     * @since 6.0
     */
    void replayActivity(long activityInstanceId) throws ActivityExecutionException, ActivityNotFoundException;

}
