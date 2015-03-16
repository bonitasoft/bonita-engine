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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javassist.util.proxy.MethodHandler;

import org.bonitasoft.engine.commons.ClassReflector;

import org.bonitasoft.engine.bdm.Entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class EntitySerializer extends JsonSerializer<Entity> {

    private String patternURI;

    public void setPatternURI(final String patternURI) {
        this.patternURI = patternURI;
    }

    private String buildURI(final Entity result, final String businessDataURIPattern, final Field entityField) {
        String uri = businessDataURIPattern.replace("{className}", result.getClass().getName());
        uri = uri.replace("{id}", result.getPersistenceId().toString());
        return uri.replace("{field}", entityField.getName());
    }

    @Override
    public void serialize(final Entity value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonProcessingException {
        final Class<? extends Entity> valueClass = value.getClass();
        jgen.writeStartObject();

        final List<Link> links = new ArrayList<Link>();
        for (final Field field : valueClass.getDeclaredFields()) {
            final Class<?> fieldType = field.getType();
            if (fieldType.equals(MethodHandler.class)) {
                continue;
            }
            if (field.isAnnotationPresent(JsonIgnore.class)) {
                links.add(new Link(field.getName(), buildURI(value, patternURI, field)));
            }
            else {
                try {
                    Method declaredMethod;
                    final String getterName = ClassReflector.getGetterName(field.getName(), fieldType);
                    declaredMethod = valueClass.getDeclaredMethod(getterName);
                    final Object invoke = declaredMethod.invoke(value);
                    jgen.writeObjectField(field.getName(), invoke);
                } catch (final NoSuchMethodException e) {
                    // nothing to do
                }
                catch (final Exception e) {
                    throw new JsonGenerationException(e);
                }
            }
        }
        if (!links.isEmpty()) {
            jgen.writeArrayFieldStart("links");
            for (final Link link : links) {
                jgen.writeObject(link);
            }
            jgen.writeEndArray();
        }
        jgen.writeEndObject();
    }

    @Override
    public Class<Entity> handledType() {
        return Entity.class;
    }
}
