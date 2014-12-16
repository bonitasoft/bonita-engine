/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.command;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;
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
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * @author Matthieu Chaffotte
 */
public class GetBusinessDataByIdCommand extends TenantCommand {

    private static final String EMPTY_OBJECT = "{}";

    public static final String ENTITY_CLASS_NAME = "entityClassName";
    public static final String BUSINESS_DATA_ID = "businessDataId";
    public static final String BUSINESS_DATA_URI_PATTERN = "businessDataURIPattern";
    public static final String BUSINESS_DATA_CHILD_NAME = "businessDataChildName";

    private final ObjectMapper mapper;

    private final EntitySerializer serializer;

    public GetBusinessDataByIdCommand() {
        this(new ObjectMapper());
    }

    protected GetBusinessDataByIdCommand(final ObjectMapper mapper) {
        this.mapper = mapper;
        serializer = new EntitySerializer();
        final SimpleModule hbm = new SimpleModule();
        hbm.addSerializer(serializer);
        mapper.registerModule(hbm);
    }

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        final BusinessDataRepository businessDataRepository = serviceAccessor.getBusinessDataRepository();
        final Long identifier = getLongMandadoryParameter(parameters, BUSINESS_DATA_ID);
        final String entityClassName = getStringMandadoryParameter(parameters, ENTITY_CLASS_NAME);
        final String businessDataURIPattern = getStringMandadoryParameter(parameters, BUSINESS_DATA_URI_PATTERN);
        serializer.setPatternURI(businessDataURIPattern);
        final Class<? extends Entity> entityClass = loadClass(entityClassName);
        try {
            final Entity entity = businessDataRepository.findById(entityClass, identifier);
            final String childName = getParameter(parameters, BUSINESS_DATA_CHILD_NAME);
            if (childName != null && !childName.isEmpty()) {
                return serializeChildEntity(entity, childName, businessDataURIPattern, businessDataRepository);
            }
            return serializeEntity(entity);
        } catch (final SBusinessDataNotFoundException sbdnfe) {
            throw new SCommandExecutionException(new DataNotFoundException(sbdnfe));
        } catch (final ClassNotFoundException e) {
            throw new SCommandExecutionException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends Entity> loadClass(final String returnType) throws SCommandParameterizationException {
        try {
            return (Class<? extends Entity>) Thread.currentThread().getContextClassLoader().loadClass(returnType);
        } catch (final ClassNotFoundException e) {
            throw new SCommandParameterizationException(e);
        }
    }

    private String serializeEntity(final Entity entity) throws SCommandExecutionException {
        try {
            final StringWriter writer = new StringWriter();
            mapper.writeValue(writer, entity);
            return writer.toString();
        } catch (final JsonProcessingException jpe) {
            throw new SCommandExecutionException(jpe);
        } catch (final IOException ioe) {
            throw new SCommandExecutionException(ioe);
        }
    }

    private String serializeEntityList(final List<Entity> entities) throws SCommandExecutionException {
        try {
            final StringWriter writer = new StringWriter();
            mapper.writeValue(writer, entities);
            return writer.toString();
        } catch (final JsonProcessingException jpe) {
            throw new SCommandExecutionException(jpe);
        } catch (final IOException ioe) {
            throw new SCommandExecutionException(ioe);
        }
    }

    @SuppressWarnings("unchecked")
    private Serializable serializeChildEntity(final Entity entity, final String fieldName, final String businessDataURIPattern,
            final BusinessDataRepository businessDataRepository) throws SCommandExecutionException, ClassNotFoundException {
        final Method method;
        try {
            final String getterName = buildGetterMethodName(fieldName);
            method = entity.getClass().getMethod(getterName);
            final Object child = method.invoke(entity);
            if (child == null) {
                return EMPTY_OBJECT;
            }
            if (child instanceof Entity) {
                return serializeEntity((Entity) child);
            } else if (child instanceof List) {
                final Class<?> type = (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
                if (Entity.class.isAssignableFrom(type)) {
                    return serializeEntityList((List<Entity>) child);
                }
            }
            throw new SCommandExecutionException(fieldName + " is not a valid attribute of entity " + entity.getClass());
        } catch (final Exception e) {
            throw new SCommandExecutionException(fieldName + " is not a valid attribute of entity " + entity.getClass(), e);
        }
    }

    private String buildGetterMethodName(final String fieldName) {
        final StringBuilder builder = new StringBuilder("get");
        builder.append(fieldName.substring(0, 1).toUpperCase());
        builder.append(fieldName.substring(1));
        return builder.toString();
    }

}
