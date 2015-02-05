/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.command;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;

import com.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 * @author Laurent Leseigneur
 */
public class GetBusinessDataByQueryCommand extends TenantCommand {

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
