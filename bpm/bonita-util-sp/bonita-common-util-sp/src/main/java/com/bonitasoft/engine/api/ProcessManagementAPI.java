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

import org.bonitasoft.engine.bpm.model.ConnectorInstance;
import org.bonitasoft.engine.bpm.model.ConnectorStateReset;
import org.bonitasoft.engine.connector.ConnectorInstanceCriterion;
import org.bonitasoft.engine.exception.ActivityExecutionFailedException;
import org.bonitasoft.engine.exception.ActivityNotFoundException;
import org.bonitasoft.engine.exception.ConnectorException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.ObjectModificationException;
import org.bonitasoft.engine.exception.ObjectNotFoundException;
import org.bonitasoft.engine.exception.ObjectReadException;
import org.bonitasoft.engine.exception.PageOutOfRangeException;
import org.bonitasoft.engine.exception.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.exception.RetryTaskException;

import com.bonitasoft.engine.bpm.model.ParameterInstance;
import com.bonitasoft.engine.exception.InvalidParameterValueException;
import com.bonitasoft.engine.exception.ParameterNotFoundException;

/**
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 */
public interface ProcessManagementAPI extends org.bonitasoft.engine.api.ProcessManagementAPI {

    /**
     * Gets how many parameters the process definition contains.
     * 
     * @param processDefinitionUUID
     *            Identifier of the processDefinition
     * @return the number of parameters of a process definition
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     * @since 6.0
     */
    int getNumberOfParameterInstances(long processDefinitionUUID) throws InvalidSessionException, ProcessDefinitionNotFoundException;

    /**
     * Get a parameter instance by process definition UUID
     * 
     * @param processDefinitionUUID
     *            Identifier of the processDefinition
     * @param parameterName
     *            The parameter name for get ParameterInstance
     * @return the ParameterInstance of the process with processDefinitionUUID and name parameterName
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     * @throws ParameterNotFoundException
     *             Error thrown if the given parameter is not found.
     * @since 6.0
     */
    ParameterInstance getParameterInstance(long processDefinitionUUID, String parameterName) throws InvalidSessionException,
            ProcessDefinitionNotFoundException, ParameterNotFoundException;

    /**
     * Returns the parameters of a process definition or an empty map if the process does not contain any parameter.
     * 
     * @param processDefinitionUUID
     *            Identifier of the processDefinition
     * @param pageIndex
     *            Index of the page to be returned. First page has index 0.
     * @param numberPerPage
     *            Number of result per page. Maximum number of result returned.
     * @param sort
     *            The criterion to sort the result
     * @return The ordered list of parameter instances
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     * @throws PageOutOfRangeException
     *             Error thrown if page is out of the range.
     * @since 6.0
     */
    List<ParameterInstance> getParameterInstances(long processDefinitionUUID, int pageIndex, int numberPerPage, ParameterSorting sort)
            throws InvalidSessionException, ProcessDefinitionNotFoundException, PageOutOfRangeException;

    /**
     * Update an existing parameter of a process definition.
     * 
     * @param processDefinitionUUID
     *            Identifier of the processDefinition
     * @param parameterName
     *            the parameter name
     * @param parameterValue
     *            the new value of the parameter
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ProcessDefinitionNotFoundException
     *             Error thrown if no processDefinition have an id corresponding to the parameter.
     * @throws ParameterNotFoundException
     *             Error thrown if the given parameter is not found.
     * @throws InvalidParameterValueException
     *             Error thrown if the given parameter is invalid.
     * @since 6.0
     */
    void updateParameterInstanceValue(long processDefinitionUUID, String parameterName, String parameterValue) throws InvalidSessionException,
            ProcessDefinitionNotFoundException, ParameterNotFoundException, InvalidParameterValueException;

    /**
     * Import the parameters by a processDefinition id and an array byte of parametersXML
     * 
     * @param pDefinitionId
     *            Identifier of the processDefinition
     * @param parametersXML
     *            The parameter with XML format.
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws InvalidParameterValueException
     *             Error thrown if is value in the parameter is invalid
     * @since 6.0
     */
    void importParameters(long pDefinitionId, byte[] parametersXML) throws InvalidSessionException, InvalidParameterValueException;

    /**
     * Retrieve the list of connector instances on an activity instance
     * 
     * @param activityInstanceId
     *            the id of the element on which we want the connector instances
     * @param pageNumber
     * @param numberPerPage
     * @param order
     * @return
     *         the list of connector instance on this element
     * @throws PageOutOfRangeException
     * @since 6.0
     */
    List<ConnectorInstance> getConnectorInstancesOfActivity(long activityInstanceId, int pageNumber, int numberPerPage, ConnectorInstanceCriterion order)
            throws InvalidSessionException, ObjectReadException, PageOutOfRangeException;

    /**
     * Retrieve the list of connector instances on a process instance
     * 
     * @param processInstanceId
     *            the id of the element on which we want the connector instances
     * @param pageNumber
     * @param numberPerPage
     * @param order
     * @return
     *         the list of connector instance on this element
     * @throws PageOutOfRangeException
     * @since 6.0
     */
    List<ConnectorInstance> getConnectorInstancesOfProcess(long processInstanceId, int pageNumber, int numberPerPage, ConnectorInstanceCriterion order)
            throws InvalidSessionException, ObjectReadException, PageOutOfRangeException;

    /**
     * Allows to reset the state of an instance of connector
     * 
     * @param connectorInstanceId
     *            the id of the connector to change
     * @param state
     *            the state to set on the connector
     * @throws InvalidSessionException
     *             if no current valid engine session is found
     * @throws ConnectorException
     *             if a connector-related problem occurs
     * @since 6.0
     */
    void setConnectorInstanceState(long connectorInstanceId, ConnectorStateReset state) throws InvalidSessionException, ConnectorException;

    /**
     * Allows to reset connector instance states for a Collection of connector instances at once.
     * 
     * @param connectorsToReset
     *            a Map containing, as key, the connnector instance id, and as value, the <code>ConnectorStateReset</code> value to reset the connector instance
     *            to.
     * @throws InvalidSessionException
     *             if no current valid engine session is found
     * @throws ConnectorException
     *             if a connector-related problem occurs
     * @since 6.0
     */
    void setConnectorInstanceState(final Map<Long, ConnectorStateReset> connectorsToReset) throws InvalidSessionException, ConnectorException;

    /**
     * set state of activity to its previous state and then execute.
     * precondition: the activity is in state FAILED
     * 
     * @param activityInstanceId
     *            Identifier of the activity instance
     * @param connectorsToReset
     *            Map of connectors to reset before retrying the task
     * @throws InvalidSessionException
     *             Generic exception thrown if API Session is invalid, e.g session has expired.
     * @throws ActivityNotFoundException
     *             errors thrown if can't find corresponding activity
     * @throws RetryTaskException
     *             errors happened when one of the two step that re-set state of the task and execute it again failed.
     * @throws ActivityExecutionFailedException
     * @throws ObjectReadException
     * @throws ObjectNotFoundException
     * @throws ObjectModificationException
     * @since 6.0
     */
    void replayActivity(long activityInstanceId, Map<Long, ConnectorStateReset> connectorsToReset) throws InvalidSessionException, ActivityNotFoundException,
            RetryTaskException, ObjectNotFoundException, ObjectReadException, ActivityExecutionFailedException, ObjectModificationException;

    /**
     * Replay a task that was in failed state.
     * The task can be replayed if no connector is in state failed.
     * If that is the case change state of failed connectors first to SKIPPED of TO_BE_EXECUTED
     * 
     * @param activityInstanceId
     *            the activity to replay
     * @throws InvalidSessionException
     * @throws ObjectNotFoundException
     *             When the activity does not exists
     * @throws ObjectReadException
     *             When the activity or connectors couldn't be read
     * @throws ObjectModificationException
     *             When the activity can't be modified
     * @throws ActivityExecutionFailedException
     *             When the activity can't be replayed because it's not in a good state, i.e. connectors in fail are present
     * @since 6.0
     */
    void replayActivity(long activityInstanceId) throws InvalidSessionException, ObjectNotFoundException, ObjectReadException, ObjectModificationException,
            ActivityExecutionFailedException;

}
