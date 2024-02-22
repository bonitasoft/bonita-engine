/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.parameter;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Matthieu Chaffotte
 * @since 6.0
 */
public interface ParameterService {

    /**
     * Update specific parameter value in a process
     *
     * @param processDefinitionId
     *        identifier of processDefinition
     * @param parameterName
     *        name of the parameter will be updated
     * @param parameterValue
     *        new value of the parameter
     * @throws SParameterNameNotFoundException
     *         error thrown if no parameter found for the specific parameterName
     */
    void update(final long processDefinitionId, final String parameterName, final String parameterValue)
            throws SParameterNameNotFoundException, SBonitaReadException, SObjectModificationException;

    /**
     * Merge given parameters with existing ones.
     * Unknown parameters are ignored.
     *
     * @param processDefinitionId
     *        identifier of processDefinition
     * @param parameters
     *        parameters to merge
     * @throws SBonitaReadException
     *         error thrown if an error occured while retrieving the process definition
     * @throws SObjectModificationException
     *         error thrown if an error occured while updating the parameter value
     */
    void merge(long processDefinitionId, Map<String, String> parameters)
            throws SBonitaReadException, SObjectModificationException;

    /**
     * Store all parameters provided to the specific process in file system
     *
     * @param processDefinitionId
     *        identifier of processDefinition
     * @param parameters
     *        parameters will be stored in file system
     * @throws SParameterProcessNotFoundException
     *         error thrown if no parameters configuration file found in file system
     */
    void addAll(final long processDefinitionId, final Map<String, String> parameters)
            throws SParameterProcessNotFoundException, SObjectCreationException, SBonitaReadException,
            SObjectModificationException;

    /**
     * return all parameters in a map
     *
     * @param processDefinitionId
     */
    Map<String, String> getAll(long processDefinitionId)
            throws SParameterProcessNotFoundException, SBonitaReadException;

    /**
     * Delete all parameters for a specific processDefinition
     *
     * @param processDefinitionId ID of processDefinition
     * @throws SParameterProcessNotFoundException
     *         error thrown if no parameters configuration file found in file system
     */
    void deleteAll(final long processDefinitionId)
            throws SParameterProcessNotFoundException, SBonitaReadException, SObjectModificationException;

    /**
     * Get parameters in a specific interval for specific process, this is used for pagination
     *
     * @param processDefinitionId
     *        identifier of processDefinition
     * @param fromIndex
     *        index of the record to be retrieved from. First record has index 0
     * @param numberOfResult
     *        number of result we want to get. Maximum number of result returned.
     * @param order
     *        OrderBy object, contains information to do order
     * @return a list of SParameter objects
     * @throws SOutOfBoundException
     *         error throw if fromIndex >= total size of parameters
     */
    List<SParameter> get(final long processDefinitionId, final int fromIndex, final int numberOfResult,
            final OrderBy order)
            throws SOutOfBoundException, SBonitaReadException;

    /**
     * Get parameter by name in specific process
     *
     * @param processDefinitionId
     *        identifier of processDefinition
     * @param parameterName
     *        name of parameter
     * @return the parameter or null if it does not exists
     */
    SParameter get(final long processDefinitionId, final String parameterName) throws SBonitaReadException;

    /**
     * Get a list of parameters will null values in order in specific process
     *
     * @param processDefinitionId
     *        identifier of processDefinition
     * @param fromIndex
     *        index of the record to be retrieved from. First record has index 0
     * @param numberOfResult
     *        number of result we want to get. Maximum number of result returned.
     * @param order
     *        OrderBy object, contains information to do order
     * @return a list of parameters
     * @throws SParameterProcessNotFoundException
     *         error thrown if no parameters configuration file found in file system
     * @throws SOutOfBoundException
     *         error throw if fromIndex >= total size of parameters
     */
    List<SParameter> getNullValues(final long processDefinitionId, final int fromIndex, final int numberOfResult,
            final OrderBy order)
            throws SParameterProcessNotFoundException, SOutOfBoundException, SBonitaReadException;

    /**
     * Check if the specific process contains null-valued parameter or not.
     *
     * @param processDefinitionId The ID of the process definition
     * @return true if at least one parameter contains a null value, false otherwise.
     * @throws SParameterProcessNotFoundException
     */
    boolean containsNullValues(final long processDefinitionId) throws SBonitaReadException;

}
