/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.business.data.JsonBusinessDataSerializer;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;


public class JsonBusinessDataSerializerImpl implements JsonBusinessDataSerializer {

    private final ObjectMapper mapper;

    private final EntitySerializer serializer;

    public JsonBusinessDataSerializerImpl() {
        mapper = new ObjectMapper();
        serializer = new EntitySerializer();
        final SimpleModule hbm = new SimpleModule();
        hbm.addSerializer(serializer);
        mapper.registerModule(hbm);
    }
    @Override
    public String serializeEntity(final Entity entity, final String businessDataURIPattern) throws JsonGenerationException, JsonMappingException, IOException {
        serializer.setPatternURI(businessDataURIPattern);
        final StringWriter writer = new StringWriter();
        mapper.writeValue(writer, entity);
        return writer.toString();

    }

    @Override
    public String serializeEntity(final List<Entity> entities, final String businessDataURIPattern) throws JsonGenerationException, JsonMappingException,
            IOException {
        serializer.setPatternURI(businessDataURIPattern);
        final StringWriter writer = new StringWriter();
        mapper.writeValue(writer, entities);
        return writer.toString();
    }

}
