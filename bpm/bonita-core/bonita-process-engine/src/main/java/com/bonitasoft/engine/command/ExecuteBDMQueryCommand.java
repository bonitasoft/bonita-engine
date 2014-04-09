/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bonitasoft.engine.command;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;

import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.NonUniqueResultException;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Romain Bioteau
 * 
 */
public class ExecuteBDMQueryCommand extends TenantCommand {

    public static final String RETURNS_LIST = "returnsList";

    public static final String QUERY_PARAMETERS = "queryParameters";

    public static final String RETURN_TYPE = "returnType";

    public static final String QUERY_NAME = "queryName";

    @Override
    public Serializable execute(Map<String, Serializable> parameters, TenantServiceAccessor serviceAccessor) throws SCommandParameterizationException,
            SCommandExecutionException {
        BusinessDataRepository businessDataRepository = getBusinessDataRepository(serviceAccessor);
        String queryName = getStringMandadoryParameter(parameters, QUERY_NAME);
        @SuppressWarnings("unchecked")
        Map<String, Serializable> queryParameters = (Map<String, Serializable>) parameters.get(QUERY_PARAMETERS);
        String returnType = getStringMandadoryParameter(parameters, RETURN_TYPE);
        Class<? extends Serializable> resultClass = null;
        try {
            resultClass = loadClass(returnType);
        } catch (ClassNotFoundException e) {
            throw new SCommandParameterizationException(e);
        }
        Boolean returnsList = (Boolean) parameters.get(RETURNS_LIST);
        if (returnsList != null && returnsList) {
            return (Serializable) businessDataRepository.findListByNamedQuery(queryName, resultClass, queryParameters);
        } else {
            try {
                return businessDataRepository.findByNamedQuery(queryName, resultClass, queryParameters);
            } catch (NonUniqueResultException e) {
                throw new SCommandExecutionException(e);
            }
        }
    }

    protected BusinessDataRepository getBusinessDataRepository(TenantServiceAccessor serviceAccessor) {
        return serviceAccessor.getBusinessDataRepository();
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends Serializable> loadClass(String returnType) throws ClassNotFoundException {
        return (Class<? extends Serializable>) Thread.currentThread().getContextClassLoader().loadClass(returnType);
    }

}
