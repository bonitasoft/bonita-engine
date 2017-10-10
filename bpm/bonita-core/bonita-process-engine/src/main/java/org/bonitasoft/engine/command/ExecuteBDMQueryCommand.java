/**
 * Copyright (C) 2015-2017 BonitaSoft S.A.
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

import static org.apache.commons.lang3.BooleanUtils.toBoolean;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.bdm.serialization.BusinessDataObjectMapper;
import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.NonUniqueResultException;
import org.bonitasoft.engine.command.system.CommandWithParameters;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Romain Bioteau
 * @author Matthieu Chaffotte
 */
public class ExecuteBDMQueryCommand extends CommandWithParameters {

    public static final String RETURNS_LIST = "returnsList";

    public static final String QUERY_PARAMETERS = "queryParameters";

    public static final String RETURN_TYPE = "returnType";

    public static final String QUERY_NAME = "queryName";

    public static final String START_INDEX = "startIndex";

    public static final String MAX_RESULTS = "maxResults";

    // Avoid multiple instantiations for better performance, see BS-16794
    private static final BusinessDataObjectMapper businessDataObjectMapper = new BusinessDataObjectMapper();

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        final String queryName = getStringMandadoryParameter(parameters, QUERY_NAME);
        @SuppressWarnings("unchecked")
        final Map<String, Serializable> queryParameters = (Map<String, Serializable>) parameters.get(QUERY_PARAMETERS);
        final String returnType = getStringMandadoryParameter(parameters, RETURN_TYPE);
        Class<? extends Serializable> resultClass = loadClass(returnType);
        final BusinessDataRepository businessDataRepository = serviceAccessor.getBusinessDataRepository();
        final Boolean returnsList = (Boolean) parameters.get(RETURNS_LIST);

        final Serializable result;
        if (toBoolean(returnsList)) {
            final Integer startIndex = getIntegerMandadoryParameter(parameters, START_INDEX);
            final Integer maxResults = getIntegerMandadoryParameter(parameters, MAX_RESULTS);
            result = (Serializable) businessDataRepository.findListByNamedQuery(queryName, resultClass, queryParameters,
                    startIndex, maxResults);
        } else {
            try {
                result = businessDataRepository.findByNamedQuery(queryName, resultClass, queryParameters);
            } catch (final NonUniqueResultException e) {
                throw new SCommandExecutionException(e);
            }
        }
        return serializeResult(result);
    }

    private static byte[] serializeResult(final Serializable result) throws SCommandExecutionException {
        try {
            return businessDataObjectMapper.writeValueAsBytes(result);
        } catch (final IOException jpe) {
            throw new SCommandExecutionException(jpe);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Serializable> loadClass(final String returnType) throws SCommandParameterizationException {
        try {
            return (Class<? extends Serializable>) Thread.currentThread().getContextClassLoader().loadClass(returnType);
        } catch (final ClassNotFoundException e) {
            throw new SCommandParameterizationException("Unable to load class for type " + returnType, e);
        }
    }

}
