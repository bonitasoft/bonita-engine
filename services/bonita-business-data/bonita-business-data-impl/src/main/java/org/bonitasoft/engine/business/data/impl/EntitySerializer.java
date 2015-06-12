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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import javassist.util.proxy.MethodHandler;
import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.commons.ClassReflector;

public class EntitySerializer extends JsonSerializer<Entity> {

    public static final String STRING_SUFFIX = "_string";
    private String patternURI;

    public void setPatternURI(final String patternURI) {
        this.patternURI = patternURI;
    }

    private String buildURI(final Entity result, final String businessDataURIPattern, final Field entityField) {
        String uri = businessDataURIPattern.replace("{className}", result.getClass().getName());
        uri = uri.replace("{id}", result.getPersistenceId().toString());
        return uri.replace("{field}", entityField.getName());
    }

    private Set<String> numberTypes;

    public EntitySerializer() {
        numberTypes = new HashSet<>();
        numberTypes.add(Long.class.getCanonicalName());
        numberTypes.add(Float.class.getCanonicalName());
        numberTypes.add(Double.class.getCanonicalName());
    }

    @Override
    public void serialize(final Entity value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonProcessingException {
        final Class<? extends Entity> valueClass = value.getClass();
        jgen.writeStartObject();

        final List<Link> links = new ArrayList<Link>();
        for (final Field field : valueClass.getDeclaredFields()) {
            final Class<?> fieldType = field.getType();
            if (shouldSkipField(fieldType)) {
                continue;
            }
            if (shouldReplaceFieldByLink(field)) {
                links.add(new Link(field.getName(), buildURI(value, patternURI, field)));
                continue;
            }
            try {
                Method declaredMethod;
                final String getterName = ClassReflector.getGetterName(field.getName(), fieldType);
                declaredMethod = valueClass.getDeclaredMethod(getterName);
                final Object invoke = declaredMethod.invoke(value);
                jgen.writeObjectField(field.getName(), invoke);
                if (invoke != null && shouldAddStringRepresentationForNumber(field)) {
                    jgen.writeObjectField(field.getName().concat(STRING_SUFFIX), invoke.toString());
                }
                if (invoke != null && shouldAddStringRepresentationForNumberList(field)) {
                    jgen.writeObjectField(field.getName().concat(STRING_SUFFIX), convertToStringList((List) invoke));
                }
            } catch (final NoSuchMethodException e) {
                // nothing to do
            } catch (final Exception e) {
                throw new JsonGenerationException(e);
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

    private List<String> convertToStringList(List numberList) {
        ArrayList<String> strings = new ArrayList<>();
        for (Object item : numberList) {
            strings.add(item.toString());
        }
        return strings;
    }

    private boolean shouldAddStringRepresentationForNumberList(Field field) {
        if (field.getType().equals(List.class)) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
            Type type = parameterizedType.getActualTypeArguments()[0];
            return numberTypes.contains(((Class) type).getCanonicalName());
        }
        return false;
    }

    private boolean shouldAddStringRepresentationForNumber(Field field) {
        return numberTypes.contains(field.getType().getCanonicalName());
    }

    protected boolean shouldReplaceFieldByLink(Field field) {
        return field.isAnnotationPresent(JsonIgnore.class);
    }

    protected boolean shouldSkipField(Class<?> fieldType) {
        return fieldType.equals(MethodHandler.class);
    }

    @Override
    public Class<Entity> handledType() {
        return Entity.class;
    }
}
