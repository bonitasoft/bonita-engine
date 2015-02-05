/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
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

import com.bonitasoft.engine.business.data.BusinessDataService;
import com.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import com.bonitasoft.engine.business.data.SBusinessDataRepositoryException;
import com.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Matthieu Chaffotte
 * @author Laurent Leseigneur
 */
public class GetBusinessDataByIdCommand extends TenantCommand {

    public static final String ENTITY_CLASS_NAME = "entityClassName";
    public static final String BUSINESS_DATA_ID = "businessDataId";
    public static final String BUSINESS_DATA_URI_PATTERN = "businessDataURIPattern";
    public static final String BUSINESS_DATA_CHILD_NAME = "businessDataChildName";

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {

        final BusinessDataService businessDataService = serviceAccessor.getBusinessDataService();

        final Long identifier = getLongMandadoryParameter(parameters, BUSINESS_DATA_ID);
        final String entityClassName = getStringMandadoryParameter(parameters, ENTITY_CLASS_NAME);
        final String businessDataURIPattern = getStringMandadoryParameter(parameters, BUSINESS_DATA_URI_PATTERN);
        final String childName = getParameter(parameters, BUSINESS_DATA_CHILD_NAME);
        try {
            if (childName != null && !childName.isEmpty()) {
                return businessDataService.getJsonChildEntity(entityClassName, identifier, childName, businessDataURIPattern);
            }
            else {
                return businessDataService.getJsonEntity(entityClassName, identifier, businessDataURIPattern);
            }
        } catch (final SBusinessDataNotFoundException e) {
            throw new SCommandExecutionException(e);
        } catch (final SBusinessDataRepositoryException e) {
            throw new SCommandExecutionException(e);
        }
    }

}
