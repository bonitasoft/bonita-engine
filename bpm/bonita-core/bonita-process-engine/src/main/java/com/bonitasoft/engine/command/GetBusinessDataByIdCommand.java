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

import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author Matthieu Chaffotte
 */
public class GetBusinessDataByIdCommand extends TenantCommand {

    public static final String ENTITY_CLASS_NAME = "entityClassName";

    public static final String BUSINESS_DATA_ID = "businessDataId";

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        final BusinessDataRepository businessDataRepository = getBusinessDataRepository(serviceAccessor);
        final Long identifier = getLongMandadoryParameter(parameters, BUSINESS_DATA_ID);
        final String entityClassName = getStringMandadoryParameter(parameters, ENTITY_CLASS_NAME);
        final Class<? extends Entity> entityClass = loadClass(entityClassName);
        try {
            final Entity entity = businessDataRepository.findById(entityClass, identifier);
            return serializeResult(entity);
        } catch (final SBusinessDataNotFoundException sbdnfe) {
            throw new SCommandExecutionException(new DataNotFoundException(sbdnfe));
        }
    }

    protected BusinessDataRepository getBusinessDataRepository(final TenantServiceAccessor serviceAccessor) {
        return serviceAccessor.getBusinessDataRepository();
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends Entity> loadClass(final String returnType) throws SCommandParameterizationException {
        try {
            return (Class<? extends Entity>) Thread.currentThread().getContextClassLoader().loadClass(returnType);
        } catch (final ClassNotFoundException e) {
            throw new SCommandParameterizationException(e);
        }
    }

    private String serializeResult(final Serializable result) throws SCommandExecutionException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        try {
            return mapper.writeValueAsString(result);
        } catch (final JsonProcessingException jpe) {
            throw new SCommandExecutionException(jpe);
        }
    }

}
