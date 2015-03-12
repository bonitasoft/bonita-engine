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
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.business.data.BusinessDataRepository;
import org.bonitasoft.engine.business.data.NonUniqueResultException;
import org.bonitasoft.engine.command.system.CommandWithParameters;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

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

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        final BusinessDataRepository businessDataRepository = getBusinessDataRepository(serviceAccessor);
        final String queryName = getStringMandadoryParameter(parameters, QUERY_NAME);
        @SuppressWarnings("unchecked")
        final Map<String, Serializable> queryParameters = (Map<String, Serializable>) parameters.get(QUERY_PARAMETERS);
        final String returnType = getStringMandadoryParameter(parameters, RETURN_TYPE);
        Class<? extends Serializable> resultClass = null;
        try {
            resultClass = loadClass(returnType);
        } catch (final ClassNotFoundException e) {
            throw new SCommandParameterizationException(e);
        }
        final Boolean returnsList = (Boolean) parameters.get(RETURNS_LIST);
        if (returnsList != null && returnsList) {
            final Integer startIndex = getIntegerMandadoryParameter(parameters, START_INDEX);
            final Integer maxResults = getIntegerMandadoryParameter(parameters, MAX_RESULTS);
            final List<? extends Serializable> list = businessDataRepository.findListByNamedQuery(queryName, resultClass, queryParameters, startIndex,
                    maxResults);
            return serializeResult((Serializable) list);
        }
        try {
            final Serializable result = businessDataRepository.findByNamedQuery(queryName, resultClass, queryParameters);
            return serializeResult(result);
        } catch (final NonUniqueResultException e) {
            throw new SCommandExecutionException(e);
        }
    }

    private byte[] serializeResult(final Serializable result) throws SCommandExecutionException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        try {
            return mapper.writeValueAsBytes(result);
        } catch (final JsonProcessingException jpe) {
            throw new SCommandExecutionException(jpe);
        }
    }

    protected BusinessDataRepository getBusinessDataRepository(final TenantServiceAccessor serviceAccessor) {
        return serviceAccessor.getBusinessDataRepository();
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends Serializable> loadClass(final String returnType) throws ClassNotFoundException {
        return (Class<? extends Serializable>) Thread.currentThread().getContextClassLoader().loadClass(returnType);
    }

}
