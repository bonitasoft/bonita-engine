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
package org.bonitasoft.engine.business.data.impl;

import java.io.IOException;
import java.util.List;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.bdm.serialization.BusinessDataObjectMapper;
import org.bonitasoft.engine.business.data.JsonBusinessDataSerializer;
import org.bonitasoft.engine.business.data.SBusinessDataRepositorySerializationException;
import org.bonitasoft.engine.business.data.impl.jackson.EntityBeanSerializerModifier;
import org.bonitasoft.engine.business.data.impl.jackson.EntityJacksonAnnotationIntrospector;
import org.bonitasoft.engine.business.data.impl.jackson.EntityMixin;
import org.bonitasoft.engine.business.data.impl.jackson.utils.LinkUtils;
import org.bonitasoft.engine.classloader.ClassLoaderListener;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class JsonBusinessDataSerializerImpl extends BusinessDataObjectMapper
        implements JsonBusinessDataSerializer, ClassLoaderListener {

    private static Logger LOG = LoggerFactory.getLogger(JsonBusinessDataSerializerImpl.class);

    public JsonBusinessDataSerializerImpl(ClassLoaderService classLoaderService) {
        classLoaderService.addListener(this);

        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new EntityBeanSerializerModifier());
        objectMapper.registerModule(module);

        objectMapper.addMixIn(Entity.class, EntityMixin.class);
        objectMapper.setAnnotationIntrospector(new EntityJacksonAnnotationIntrospector());

        // Ensure Jackson use only fields to get properties. Reasons:
        //  - entity can be wrapped in a javassist or hibernate proxy that have additional getters
        //  - generated getters may not exactly match the field name (getANumber for aNumber field)
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    private ObjectWriter newObjectWriter(String uriPattern) {
        return LinkUtils.putUriPatternIntoContext(objectMapper.writer(), uriPattern);
    }

    @Override
    public String serializeEntity(final Entity entity, final String businessDataURIPattern)
            throws SBusinessDataRepositorySerializationException {
        try {
            LOG.trace("Serializing entity");
            String json = newObjectWriter(businessDataURIPattern).writeValueAsString(entity);
            LOG.trace("Serialization result: {}", json);
            return json;
        } catch (IOException e) {
            throw new SBusinessDataRepositorySerializationException(
                    "Unable to serialize Entity of type " + entity.getClass().getSimpleName(), e);
        }
    }

    @Override
    public String serializeEntities(final List<? extends Entity> entities, final String businessDataURIPattern)
            throws SBusinessDataRepositorySerializationException {
        try {
            LOG.trace("Serializing a list of entities");
            String json = newObjectWriter(businessDataURIPattern).writeValueAsString(entities);
            LOG.trace("Serialization result: {}", json);
            return json;
        } catch (IOException e) {
            throw new SBusinessDataRepositorySerializationException("Unable to serialize list of Entity", e);
        }
    }

    @Override
    public void onUpdate(ClassLoader newClassLoader) {
    }

    @Override
    public void onDestroy(ClassLoader oldClassLoader) {
    }

}
