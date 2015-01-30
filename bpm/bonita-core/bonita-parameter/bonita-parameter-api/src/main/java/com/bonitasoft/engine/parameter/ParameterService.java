/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.parameter;

import java.util.List;
import java.util.Map;

/**
 * @author Matthieu Chaffotte
 * @since 6.0
 */
public interface ParameterService {

    /**
     * Update specific parameter value in a process
     * 
     * @param processDefinitionId
     *            identifier of processDefinition
     * @param parameterName
     *            name of the parameter will be updated
     * @param parameterValue
     *            new value of the parameter
     * @throws SParameterProcessNotFoundException
     *             error thrown if no parameters configuration file found in file system
     * @throws SParameterNameNotFoundException
     *             error thrown if no parameter found for the specific parameterName
     */
    void update(final long processDefinitionId, final String parameterName, final String parameterValue) throws SParameterProcessNotFoundException,
            SParameterNameNotFoundException;

    /**
     * Store all parameters provided to the specific process in file system
     * 
     * @param processDefinitionId
     *            identifier of processDefinition
     * @param parameters
     *            parameters will be stored in file system
     * @throws SParameterProcessNotFoundException
     *             error thrown if no parameters configuration file found in file system
     */
    void addAll(final long processDefinitionId, final Map<String, String> parameters) throws SParameterProcessNotFoundException;

    /**
     * Delete all parameters for specific processDefinition
     * 
     * @param processDefinitionId
     *            identifier of processDefinition
     * @throws SParameterProcessNotFoundException
     *             error thrown if no parameters configuration file found in file system
     */
    void deleteAll(final long processDefinitionId) throws SParameterProcessNotFoundException;

    /**
     * Get parameters in a specific interval for specific process, this is used for pagination
     * 
     * @param processDefinitionId
     *            identifier of processDefinition
     * @param fromIndex
     *            index of the record to be retrieved from. First record has index 0
     * @param numberOfResult
     *            number of result we want to get. Maximum number of result returned.
     * @param order
     *            OrderBy object, contains information to do order
     * @return a list of SParameter objects
     * @throws SParameterProcessNotFoundException
     *             error thrown if no parameters configuration file found in file system
     * @throws SOutOfBoundException
     *             error throw if fromIndex >= total size of parameters
     */
    List<SParameter> get(final long processDefinitionId, final int fromIndex, final int numberOfResult, final OrderBy order)
            throws SParameterProcessNotFoundException, SOutOfBoundException;

    /**
     * Get parameter by name in specific process
     * 
     * @param processDefinitionId
     *            identifier of processDefinition
     * @param parameterName
     *            name of parameter
     * @return the parameter
     * @throws SParameterProcessNotFoundException
     *             error thrown if no parameters configuration file found in file system
     */
    SParameter get(final long processDefinitionId, final String parameterName) throws SParameterProcessNotFoundException;

    /**
     * Get a list of parameters will null values in order in specific process
     * 
     * @param processDefinitionId
     *            identifier of processDefinition
     * @param fromIndex
     *            index of the record to be retrieved from. First record has index 0
     * @param numberOfResult
     *            number of result we want to get. Maximum number of result returned.
     * @param order
     *            OrderBy object, contains information to do order
     * @return a list of parameters
     * @throws SParameterProcessNotFoundException
     *             error thrown if no parameters configuration file found in file system
     * @throws SOutOfBoundException
     *             error throw if fromIndex >= total size of parameters
     */
    List<SParameter> getNullValues(final long processDefinitionId, final int fromIndex, final int numberOfResult, final OrderBy order)
            throws SParameterProcessNotFoundException, SOutOfBoundException;

    /**
     * Check the specific process contains null-valued parameter or not.
     * 
     * @param processDefinitionId
     * @return
     * @throws SParameterProcessNotFoundException
     */
    boolean containsNullValues(final long processDefinitionId) throws SParameterProcessNotFoundException;

}
