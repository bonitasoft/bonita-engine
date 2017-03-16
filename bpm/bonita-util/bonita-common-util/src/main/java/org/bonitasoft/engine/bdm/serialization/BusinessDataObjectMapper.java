/**
 * Copyright (C) 2017 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.bdm.serialization;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import org.bonitasoft.engine.bdm.Entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;

public class BusinessDataObjectMapper {

    private ObjectMapper objectMapper;
    private SimpleModule module;

    public BusinessDataObjectMapper() {
        init();
    }

    public void init() {
        objectMapper = new ObjectMapper();
        module = new SimpleModule();
        module.addSerializer(LocalDate.class, new CustomLocalDateSerializer());
        module.addSerializer(LocalDateTime.class, new CustomLocalDateTimeSerializer());
        module.addSerializer(OffsetDateTime.class, new CustomOffsetDateTimeSerializer());
        module.addDeserializer(LocalDate.class, new CustomLocalDateDeserializer());
        module.addDeserializer(LocalDateTime.class, new CustomLocalDateTimeDeserializer());
        module.addDeserializer(OffsetDateTime.class, new CustomOffsetDateTimeDeserializer());
        objectMapper.registerModule(module);
    }

    public void addSerializer(JsonSerializer serializer) {
        module.addSerializer(serializer);
    }

    public void writeValue(StringWriter writer, Entity entity) throws IOException {
        objectMapper.writeValue(writer, entity);
    }

    public void writeValue(StringWriter writer, List<? extends Entity> entities) throws IOException {
        objectMapper.writeValue(writer, entities);
    }

    public <T extends Serializable> List<T> readListValue(byte[] result, Class<T> loadClass) throws IOException {
        return objectMapper.readValue(result, objectMapper.getTypeFactory().constructCollectionType(List.class, loadClass));
    }

    public <T extends Serializable> T readValue(byte[] result, Class<T> loadClass) throws IOException {
        return objectMapper.readValue(result, loadClass);
    }

    public void enableSerializationFeature(SerializationFeature serializationFeature) {
        objectMapper.enable(serializationFeature);
        ((DefaultSerializerProvider) objectMapper.getSerializerProvider()).flushCachedSerializers();
    }

    public void disableSerializationFeature(SerializationFeature serializationFeature) {
        objectMapper.disable(serializationFeature);
        ((DefaultSerializerProvider) objectMapper.getSerializerProvider()).flushCachedSerializers();
    }

    public byte[] writeValueAsBytes(Serializable result) throws JsonProcessingException {
        return objectMapper.writeValueAsBytes(result);
    }
}
