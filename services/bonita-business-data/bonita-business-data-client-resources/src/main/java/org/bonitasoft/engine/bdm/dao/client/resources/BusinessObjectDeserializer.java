/**
 * Copyright (C) 2015-2017 BonitaSoft S.A.
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import org.bonitasoft.engine.bdm.serialization.CustomLocalDateDeserializer;
import org.bonitasoft.engine.bdm.serialization.CustomLocalDateTimeDeserializer;
import org.bonitasoft.engine.bdm.serialization.CustomOffsetDateTimeDeserializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * @author Romain Bioteau
 */
public class BusinessObjectDeserializer {

    private final ObjectMapper mapper;
    private final TypeFactory typeFactory;

    public BusinessObjectDeserializer() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        final SimpleModule customModule = new SimpleModule();
        customModule.addDeserializer(LocalDate.class, new CustomLocalDateDeserializer());
        customModule.addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeserializer());
        customModule.addDeserializer(OffsetDateTime.class, new CustomOffsetDateTimeDeserializer());
        mapper.registerModule(customModule);
        typeFactory = mapper.getTypeFactory();
    }

    @SuppressWarnings("unchecked")
    public <T> T deserialize(final byte[] serializedResult, final Class<T> targetType) throws IOException {
        return (T) mapper.readValue(serializedResult, createJavaType(targetType));
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> deserializeList(final byte[] serializedResult, final Class<T> targetType) throws IOException {
        return (List<T>) mapper.readValue(serializedResult, createListJavaType(targetType));
    }

    private JavaType createListJavaType(final Type elementType) {
        return typeFactory.constructCollectionType(List.class, createJavaType(elementType));
    }

    private JavaType createJavaType(final Type elementType) {
        return typeFactory.constructType(elementType);
    }

}
