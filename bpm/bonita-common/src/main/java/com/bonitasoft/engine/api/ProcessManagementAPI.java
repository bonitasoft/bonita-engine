/*
 * Copyright (C) 2011-2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.api;

import java.util.List;

import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.PageOutOfRangeException;
import org.bonitasoft.engine.exception.ProcessDefinitionNotFoundException;

import com.bonitasoft.engine.bpm.model.ParameterInstance;
import com.bonitasoft.engine.exception.InvalidParameterValueException;
import com.bonitasoft.engine.exception.ParameterNotFoundException;

/**
 * @author Matthieu Chaffotte
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
     */
    void importParameters(long pDefinitionId, byte[] parametersXML) throws InvalidSessionException, InvalidParameterValueException;

}
