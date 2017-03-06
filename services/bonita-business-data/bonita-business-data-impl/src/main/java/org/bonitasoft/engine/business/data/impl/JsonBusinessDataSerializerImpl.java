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
package org.bonitasoft.engine.business.data.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.business.data.JsonBusinessDataSerializer;
import org.bonitasoft.engine.business.data.impl.utils.JsonNumberSerializerHelper;
import org.bonitasoft.engine.classloader.ClassLoaderListener;
import org.bonitasoft.engine.classloader.ClassLoaderService;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JsonBusinessDataSerializerImpl implements JsonBusinessDataSerializer, ClassLoaderListener {

    private ObjectMapper mapper;

    private EntitySerializer serializer;

    public JsonBusinessDataSerializerImpl(ClassLoaderService classLoaderService) {
        init();
        classLoaderService.addListener(this);
    }

    private void init() {
        serializer = new EntitySerializer(new JsonNumberSerializerHelper());
        mapper = new ObjectMapper();
        final SimpleModule module = new SimpleModule();
        module.addSerializer(serializer);
        module.addSerializer(LocalDate.class, new CustomLocalDateSerializer());
        module.addSerializer(LocalDateTime.class, new CustomLocalDateTimeSerializer());
        mapper.registerModule(module);
    }

    @Override
    public String serializeEntity(final Entity entity, final String businessDataURIPattern) throws JsonGenerationException, JsonMappingException, IOException {
        serializer.setPatternURI(businessDataURIPattern);
        final StringWriter writer = new StringWriter();
        mapper.writeValue(writer, entity);
        return writer.toString();

    }

    @Override
    public String serializeEntity(final List<? extends Entity> entities, final String businessDataURIPattern) throws JsonGenerationException,
            JsonMappingException, IOException {
        serializer.setPatternURI(businessDataURIPattern);
        final StringWriter writer = new StringWriter();
        mapper.writeValue(writer, entities);
        return writer.toString();
    }

    @Override
    public void onUpdate(ClassLoader newClassLoader) {
        init();
    }

    @Override
    public void onDestroy(ClassLoader oldClassLoader) {
        init();
    }
}
