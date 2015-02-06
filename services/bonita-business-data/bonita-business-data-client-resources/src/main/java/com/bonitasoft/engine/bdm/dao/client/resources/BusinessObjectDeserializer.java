/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.dao.client.resources;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * @author Romain Bioteau
 *
 */
public class BusinessObjectDeserializer {

    private final ObjectMapper mapper;
    private final TypeFactory typeFactory;

    public BusinessObjectDeserializer() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        typeFactory = mapper.getTypeFactory();
    }

    @SuppressWarnings("unchecked")
	public <T> T deserialize(final byte[] serializedResult, final Class<T> targetType) throws JsonParseException, JsonMappingException, IOException {
        return (T) mapper.readValue(serializedResult, createJavaType(targetType));
    }
    
    @SuppressWarnings("unchecked")
	public <T> List<T> deserializeList(final byte[] serializedResult, final Class<T> targetType) throws JsonParseException, JsonMappingException, IOException {
        return (List<T>) mapper.readValue(serializedResult, createListJavaType(targetType));
    }

    private JavaType createListJavaType(final Type elementType) {
        return typeFactory.constructCollectionType(List.class, createJavaType(elementType));
    }

    private JavaType createJavaType(final Type elementType) {
        return typeFactory.constructType(elementType);
    }

}
