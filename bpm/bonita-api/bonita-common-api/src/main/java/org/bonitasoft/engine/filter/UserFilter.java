/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.filter;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.connector.ConnectorValidationException;

/**
 * The execution of this filter returns a list of userId.
 * It is used for the filtering of who can claim a UserTask based on its actorId
 * 
 * @author Baptiste Mesta
 */
public interface UserFilter {

    /**
     * Set the input parameter for the filter.
     * 
     * @param parameters
     *            parameters is a map with parameter names and their value.
     */
    void setInputParameters(Map<String, Object> parameters);

    /**
     * Validate the input parameters. Check the parameters types and boundaries.
     * 
     * @throws ConnectorValidationException
     */
    void validateInputParameters() throws ConnectorValidationException;

    /**
     * Execute the filter.
     * 
     * @param actorName
     *            the actor name of the task
     * @return the connector outputs map corresponding to the output definition.
     * @throws UserFilterException
     */
    List<Long> filter(String actorName) throws UserFilterException;

    /**
     * This method make the engine assign automatically the task if the result of {@link #filter(String)} is only one element.
     * i.e. when the task is filtered only for a single user
     * 
     * @return true if we should assign task when there is only one result.
     */
    boolean shouldAutoAssignTaskIfSingleResult();

}
