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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.SCommandParameterizationException;

import com.bonitasoft.engine.api.rest.Link;
import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.BusinessDataRepository;
import com.bonitasoft.engine.business.data.SBusinessDataNotFoundException;
import com.bonitasoft.engine.service.TenantServiceAccessor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

/**
 * @author Matthieu Chaffotte
 */
public class GetBusinessDataByIdCommand extends TenantCommand {

    public static final String ENTITY_CLASS_NAME = "entityClassName";

    public static final String BUSINESS_DATA_ID = "businessDataId";

    public static final String BUSINESS_DATA_URI_PATTERN = "businessDataURIPattern";

    public static final String BUSINESS_DATA_CHILD_NAME = "businessDataChildName";

    private final ObjectMapper mapper;

    public GetBusinessDataByIdCommand() {
        mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        final Hibernate4Module hbm = new Hibernate4Module();
        hbm.enable(Hibernate4Module.Feature.FORCE_LAZY_LOADING);
        mapper.registerModule(hbm);
    }

    protected GetBusinessDataByIdCommand(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandParameterizationException, SCommandExecutionException {
        final BusinessDataRepository businessDataRepository = serviceAccessor.getBusinessDataRepository();
        final Long identifier = getLongMandadoryParameter(parameters, BUSINESS_DATA_ID);
        final String entityClassName = getStringMandadoryParameter(parameters, ENTITY_CLASS_NAME);
        final String businessDataURIPattern = getStringMandadoryParameter(parameters, BUSINESS_DATA_URI_PATTERN);
        final Class<? extends Entity> entityClass = loadClass(entityClassName);
        try {
            final Entity entity = businessDataRepository.findById(entityClass, identifier);
            final String childName = getParameter(parameters, BUSINESS_DATA_CHILD_NAME);
            if (childName != null && !childName.isEmpty()) {
                return serializeChildResult(entity, childName, businessDataURIPattern);
            } else {
                return serializeResult(entity, businessDataURIPattern);
            }
        } catch (final SBusinessDataNotFoundException sbdnfe) {
            throw new SCommandExecutionException(new DataNotFoundException(sbdnfe));
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

    private String serializeResult(final Entity entity, final String businessDataURIPattern) throws SCommandExecutionException {
        final List<Link> links = buildLinks(entity, businessDataURIPattern);
        final JsonNode jsonNode = buildJsonNode(entity, links);
        return serializeResult(jsonNode);
    }

    private JsonNode buildJsonNode(final Entity result, final List<Link> links) {
        final JsonNode jsonNode = mapper.valueToTree(result);
        if (!links.isEmpty()) {
            final ArrayNode linksNode = ((ObjectNode) jsonNode).putArray("links");
            for (final Link link : links) {
                linksNode.addPOJO(link);
            }
        }
        return jsonNode;
    }

    private List<Link> buildLinks(final Entity result, final String businessDataURIPattern) {
        final List<Link> links = new ArrayList<Link>();
        for (final Field entityField : result.getClass().getDeclaredFields()) {
            if (entityField.getAnnotation(JsonIgnore.class) != null) {
                final String uri = buildURI(result, businessDataURIPattern, entityField);
                final Link link = new Link(entityField.getName(), uri);
                links.add(link);
            }
        }
        return links;
    }

    private String buildURI(final Entity result, final String businessDataURIPattern, final Field entityField) {
        String uri = businessDataURIPattern.replace("{className}", result.getClass().getName());
        uri = uri.replace("{id}", result.getPersistenceId().toString());
        return uri.replace("{field}", entityField.getName());
    }

    private String serializeResult(final JsonNode jsonNode) throws SCommandExecutionException {
        try {
            final StringWriter writer = new StringWriter();
            mapper.writeValue(writer, jsonNode);
            return writer.toString();
        } catch (final JsonProcessingException jpe) {
            throw new SCommandExecutionException(jpe);
        } catch (final IOException ioe) {
            throw new SCommandExecutionException(ioe);
        }
    }

    private String serializeChildResult(final Entity entity, final String fieldName, final String businessDataURIPattern) throws SCommandExecutionException {
        final Method method;
        final Object invoke;
        try {
            final String getterName = buildGetterMethodName(fieldName);
            method = entity.getClass().getMethod(getterName);
            invoke = method.invoke(entity);
        } catch (final Exception e) {
            throw new SCommandExecutionException(fieldName + " is not a valid attribute of entity " + entity.getClass(), e);
        }

        if (invoke instanceof Entity) {
            return serializeResult((Entity) invoke, businessDataURIPattern);
        } else {
            if (invoke instanceof List) {
            final Class<?> type = (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
            if (Entity.class.isAssignableFrom(type)) {
                final ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
                final List<Entity> entities = (List<Entity>) invoke;
                for (final Entity entity2 : entities) {
                    final List<Link> links = buildLinks(entity2, businessDataURIPattern);
                    final JsonNode jsonNode = buildJsonNode(entity2, links);
                    arrayNode.add(jsonNode);
                }
                return serializeResult(arrayNode);
                }
            }
        }
        throw new SCommandExecutionException("the type of " + fieldName + " must be an instance of either " + Entity.class.getName() + " or "
                + List.class.getName() + " of " + Entity.class.getName());
    }

    private String buildGetterMethodName(final String fieldName) {
        final StringBuilder builder = new StringBuilder("get");
        builder.append(fieldName.substring(0, 1).toUpperCase());
        builder.append(fieldName.substring(1));
        return builder.toString();
    }

}
