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
package org.bonitasoft.engine.command;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import org.bonitasoft.engine.command.system.CommandWithParameters;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 * @author Laurent Leseigneur
 */
public class GetBusinessDataByQueryCommand extends CommandWithParameters {

    public static final String QUERY_PARAMETERS = "queryParameters";
    public static final String ENTITY_CLASS_NAME = "entityClassName";
    public static final String QUERY_NAME = "queryName";

    public static final String START_INDEX = "startIndex";
    public static final String MAX_RESULTS = "maxResults";

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {

        final String queryName = getStringMandadoryParameter(parameters, QUERY_NAME);
        @SuppressWarnings("unchecked")
        final Map<String, Serializable> queryParameters = (Map<String, Serializable>) parameters.get(QUERY_PARAMETERS);
        final String entityClassName = getStringMandadoryParameter(parameters, ENTITY_CLASS_NAME);
        final Integer startIndex = getIntegerMandadoryParameter(parameters, START_INDEX);
        final Integer maxResults = getIntegerMandadoryParameter(parameters, MAX_RESULTS);
        String businessDataURIPattern = getStringMandadoryParameter(parameters, BusinessDataCommandField.BUSINESS_DATA_URI_PATTERN);
        try {
            return serviceAccessor.getBusinessDataService().getJsonQueryEntities(entityClassName, queryName, queryParameters, startIndex, maxResults,
                    businessDataURIPattern);
        } catch (SBusinessDataRepositoryException e) {
            throw new SCommandExecutionException(e);
        }
    }
}
