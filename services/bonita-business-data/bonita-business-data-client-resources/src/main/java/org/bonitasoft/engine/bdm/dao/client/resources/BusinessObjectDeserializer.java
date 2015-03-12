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
package org.bonitasoft.engine.bdm.dao.client.resources;

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
