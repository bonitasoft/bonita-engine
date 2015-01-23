package com.bonitasoft.engine.business.data.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javassist.util.proxy.MethodHandler;

import org.apache.commons.lang3.StringUtils;

import com.bonitasoft.engine.bdm.Entity;
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
            if (field.getType().equals(MethodHandler.class)){
                continue;
            }
            if (field.isAnnotationPresent(JsonIgnore.class)) {
                final String uri = buildURI(value, patternURI, field);
                final Link link = new Link(field.getName(), uri);
                links.add(link);
            }
            else {
                try {
                    Method declaredMethod;
                    declaredMethod = valueClass.getDeclaredMethod("get" + StringUtils.capitalize(field.getName()));
                    final Object invoke = declaredMethod.invoke(value);
                    jgen.writeObjectField(field.getName(), invoke);
                } catch (final NoSuchMethodException e) {
                    // nothing to do
                } catch (final Exception e) {
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
