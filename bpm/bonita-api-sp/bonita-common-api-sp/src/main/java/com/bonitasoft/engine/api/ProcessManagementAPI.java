/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.connector.ConnectorInstance;
import org.bonitasoft.engine.bpm.connector.ConnectorInstanceCriterion;
import org.bonitasoft.engine.bpm.connector.ConnectorInstanceNotFoundException;
import org.bonitasoft.engine.bpm.connector.ConnectorStateReset;
import org.bonitasoft.engine.bpm.connector.InvalidConnectorImplementationException;
import org.bonitasoft.engine.bpm.flownode.ActivityExecutionException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.exception.UpdateException;

import com.bonitasoft.engine.bpm.parameter.ImportParameterException;
import com.bonitasoft.engine.bpm.parameter.ParameterCriterion;
import com.bonitasoft.engine.bpm.parameter.ParameterInstance;
import com.bonitasoft.engine.bpm.parameter.ParameterNotFoundException;

/**
 * * {@link ProcessManagementAPI} extends {@link org.bonitasoft.engine.api.ProcessManagementAPI} and adds capabilities on <code>Parameter</code>s, activity
 * replay, connector implementation hot-replace.
 * 
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public interface ProcessManagementAPI extends org.bonitasoft.engine.api.ProcessManagementAPI {

    /**
     * Get a parameter instance by process definition UUID
     * 
     * @param processDefinitionId
     *            The identifier of the processDefinition
     * @param parameterName
     *            The parameter name for get ParameterInstance
     * @return The ParameterInstance of the process with processDefinitionUUID and name parameterName
     * @throws ParameterNotFoundException
     *             Error thrown if the given parameter is not found.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 6.0
     * @deprecated use {@link org.bonitasoft.engine.api.ProcessManagementAPI#getParameterInstance(long, String)}
     */
    @Deprecated
    ParameterInstance getParameterInstance(long processDefinitionId, String parameterName) throws ParameterNotFoundException;

    /**
     * Returns the parameters of a process definition or an empty map if the process does not contain any parameter.
     * 
     * @param processDefinitionId
     *            The identifier of the processDefinition
     * @param startIndex
     *            The index of the page to be returned. First page has index 0.
     * @param maxResults
     *            The number of result per page. Maximum number of result returned.
     * @param sort
     *            The criterion to sort the result
     * @return The ordered list of parameter instances
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 6.0
     * @deprecated use {@link org.bonitasoft.engine.api.ProcessManagementAPI#getParameterInstances(long, int, int, org.bonitasoft.engine.bpm.parameter.ParameterCriterion)}
     */
    @Deprecated
    List<ParameterInstance> getParameterInstances(long processDefinitionId, int startIndex, int maxResults, ParameterCriterion sort);

    /**
     * Update an existing parameter of a process definition.
     * 
     * @param processDefinitionId
     *            The identifier of the processDefinition
     * @param parameterName
     *            The parameter name
     * @param parameterValue
     *            The new value of the parameter
     * @throws ParameterNotFoundException
     *             Error thrown if the given parameter is not found.
     * @throws UpdateException
     *             If the update cannot be fullfilled.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 6.0
     */
    void updateParameterInstanceValue(long processDefinitionId, String parameterName, String parameterValue) throws ParameterNotFoundException, UpdateException;

    /**
     * Imports the parameters of the process definition.
     * The parameters are stored in a properties file.
     * 
     * @param processDefinitionId
     *            The identifier of the process definition
     * @param parameters
     *            The content of the properties file as a byte array. If null or empty byte[], nothing is done, and the process does not reset to unresolved but
     *            stays the same.
     * @throws ImportParameterException
     *             If an exception occurs while importing the parameters
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid (expired, unknown, ...)
     * @since 6.0
     */
    void importParameters(long processDefinitionId, byte[] parameters) throws ImportParameterException;

    /**
     * Retrieve the list of connector instances on an activity instance
     * 
     * @param activityInstanceId
     *            The identifier of the element on which we want the connector instances
     * @param startIndex
     *            The index of the page to be returned. First page has index 0.
     * @param maxResults
     *            The number of result per page. Maximum number of result returned.
     * @param sortingCriterion
     *            The criterion to sort the result
     * @return The list of connector instance on this element
     * @since 6.0
     */
    List<ConnectorInstance> getConnectorInstancesOfActivity(long activityInstanceId, int startIndex, int maxResults, ConnectorInstanceCriterion sortingCriterion);

    /**
     * Retrieve the list of connector instances on a process instance
     * 
     * @param processInstanceId
     *            The identifier of the element on which we want the connector instances
     * @param startIndex
     *            The index of the page to be returned. First page has index 0.
     * @param maxResults
     *            The number of result per page. Maximum number of result returned.
     * @param sortingCriterion
     *            The criterion to sort the result
     * @return The list of connector instance on this element
     * @since 6.0
     */
    List<ConnectorInstance> getConnectorInstancesOfProcess(long processInstanceId, int startIndex, int maxResults, ConnectorInstanceCriterion sortingCriterion);

    /**
     * Allows to reset the state of an instance of connector
     * 
     * @param connectorInstanceId
     *            The identifier of the connector to change
     * @param state
     *            The state to set on the connector
     * @throws UpdateException
     *             If the set operation cannot be fullfilled.
     * @throws ConnectorInstanceNotFoundException
     *             If the connector instance cannot be found with the provided connectorInstanceId
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If no current valid engine session is found
     * @since 6.0
     */
    void setConnectorInstanceState(long connectorInstanceId, ConnectorStateReset state) throws UpdateException, ConnectorInstanceNotFoundException;

    /**
     * Allows to reset connector instance states for a Collection of connector instances at once.
     * 
     * @param connectorsToReset
     *            A Map containing, as key, the connector instance id, and as value, the <code>ConnectorStateReset</code> value to reset the connector instance
     *            to.
     * @throws ConnectorInstanceNotFoundException
     *             If the connector instance cannot be found with the provided connectorInstanceId
     * @throws UpdateException
     *             If the set operation cannot be fullfilled.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If no current valid engine session is found
     * @since 6.0
     */
    void setConnectorInstanceState(final Map<Long, ConnectorStateReset> connectorsToReset) throws ConnectorInstanceNotFoundException, UpdateException;

    /**
     * Updates the implementation version of the connector of the process definition.
     * Removes the old the old .impl file, puts the new .impl file in the connector directory and reloads the cache.
     * Warning filesystem operation are not rolledback
     * 
     * @param processDefinitionId
     *            The identifier of the process definition.
     * @param connectorName
     *            The name of the connector.
     * @param connectorVersion
     *            The version of the connector.
     * @param connectorImplementationArchive
     *            The zipped .impl file contented as a byte array. The file is exported from studio.
     * @throws InvalidConnectorImplementationException
     *             If the implementation is not valid. (e.g. wrong format)
     * @throws UpdateException
     *             If the set operation cannot be fullfilled.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             If the session is invalid, e.g. the session has expired.
     * @since 6.0
     */
    void setConnectorImplementation(long processDefinitionId, String connectorName, String connectorVersion, byte[] connectorImplementationArchive)
            throws InvalidConnectorImplementationException, UpdateException;

    /**
     * Set state of activity to its previous state and then execute.
     * precondition: the activity is in state FAILED
     * 
     * @param activityInstanceId
     *            The identifier of the activity instance
     * @param connectorsToReset
     *            The map of connectors to reset before retrying the task
     * @throws ActivityInstanceNotFoundException
     *             If no activity instance can be found with the provided activityInstanceId
     * @throws ActivityExecutionException
     *             If the activity failed to replay.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @since 6.0
     */
    void replayActivity(long activityInstanceId, Map<Long, ConnectorStateReset> connectorsToReset) throws ActivityInstanceNotFoundException,
            ActivityExecutionException;

    /**
     * Replay a task that was in failed state.
     * The task can be replayed if no connector is in state failed.
     * If that is the case change state of failed connectors first to SKIPPED of TO_BE_EXECUTED
     * 
     * @param activityInstanceId
     *            The activity to replay
     * @throws ActivityInstanceNotFoundException
     *             If no activity instance can be found with the provided activityInstanceId
     * @throws ActivityExecutionException
     *             If the activity failed to replay.
     * @throws org.bonitasoft.engine.session.InvalidSessionException
     *             When the activity can't be modified
     * @since 6.0
     */
    void replayActivity(long activityInstanceId) throws ActivityInstanceNotFoundException, ActivityExecutionException;

    /**
     * Get the number of processes supervisors mapped to a user on a specific process definition.
     * 
     * @param processDefinitionId
     *            the id of the process definition
     * @return The number of processes supervisors mapped to a user for the process definition
     * @since 6.3
     */
    long getNumberOfProcessSupervisorsForUser(long processDefinitionId);

    /**
     * Get the number of processes supervisors mapped to a group on a specific process definition.
     * 
     * @param processDefinitionId
     *            the id of the process definition
     * @return The number of processes supervisors mapped to a group for the process definition
     * @since 6.3
     */
    long getNumberOfProcessSupervisorsForGroup(long processDefinitionId);

    /**
     * Get the number of processes supervisors mapped to a role on a specific process definition.
     * 
     * @param processDefinitionId
     *            the id of the process definition
     * @return The number of processes supervisors mapped to a role for the process definition
     * @since 6.3
     */
    long getNumberOfProcessSupervisorsForRole(long processDefinitionId);

    /**
     * Get the number of processes supervisors mapped to a membership on a specific process definition.
     * 
     * @param processDefinitionId
     *            the id of the process definition
     * @return The number of processes supervisors mapped to a membership for the process definition
     * @since 6.3
     */
    long getNumberOfProcessSupervisorsForMembership(long processDefinitionId);

}
