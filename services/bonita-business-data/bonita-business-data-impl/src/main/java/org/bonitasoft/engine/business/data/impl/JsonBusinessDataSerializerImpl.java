/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.data.impl;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.bdm.serialization.BusinessDataObjectMapper;
import org.bonitasoft.engine.business.data.JsonBusinessDataSerializer;
import org.bonitasoft.engine.business.data.SBusinessDataRepositorySerializationException;
import org.bonitasoft.engine.business.data.impl.jackson.EntityBeanSerializerModifier;
import org.bonitasoft.engine.business.data.impl.jackson.EntityJacksonAnnotationIntrospector;
import org.bonitasoft.engine.business.data.impl.jackson.EntityMixin;
import org.bonitasoft.engine.business.data.impl.jackson.utils.LinkUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;

/**
 * Jackson-based serializer for Business Data Model (BDM) entities to JSON.
 * <p>
 * This service is responsible for converting BDM entities and query results into JSON format
 * for REST API responses. It handles two JSON output shapes:
 * <ul>
 * <li><b>Standard shape</b>: Single-entity queries return objects {@code {...}}, scalar queries return
 * {@code { "value": X }}</li>
 * <li><b>Legacy shape</b>: All queries return arrays {@code [...]}</li>
 * </ul>
 * <p>
 * The serializer configures Jackson to:
 * <ul>
 * <li>Use field-based introspection (avoiding proxy getters)</li>
 * <li>Generate links for entity relationships</li>
 * <li>Apply custom entity serialization logic via {@link EntityBeanSerializerModifier}</li>
 * </ul>
 * <p>
 * This is a stateless service - the decision of which shape to use is made by the calling service
 * ({@link BusinessDataServiceImpl}) and passed as method parameters.
 *
 * @see JsonBusinessDataSerializer
 * @see BusinessDataServiceImpl for the orchestration layer that determines which shape to use
 */
@Component
@ConditionalOnSingleCandidate(JsonBusinessDataSerializer.class)
@Slf4j
public class JsonBusinessDataSerializerImpl extends BusinessDataObjectMapper
        implements JsonBusinessDataSerializer {

    /**
     * Property name for the standard shape configuration.
     */
    public static final String STANDARD_SHAPE_PROPERTY = "bonita.runtime.business-data.serialization.standard-shape.enabled";

    @Value("${" + STANDARD_SHAPE_PROPERTY + ":true}")
    private boolean standardShapeEnabled;

    public JsonBusinessDataSerializerImpl() {
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
    public boolean isStandardShapeEnabled() {
        return standardShapeEnabled;
    }

    @Override
    public String serializeEntity(final Entity entity, final String businessDataURIPattern)
            throws SBusinessDataRepositorySerializationException {
        try {
            if (log.isTraceEnabled()) {
                log.trace("Serializing entity");
            }
            String json = newObjectWriter(businessDataURIPattern).writeValueAsString(entity);
            if (log.isTraceEnabled()) {
                log.trace("Entity serialization result: {}", json);
            }
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
            if (log.isTraceEnabled()) {
                log.trace("Serializing a list of entities");
            }
            String json = newObjectWriter(businessDataURIPattern).writeValueAsString(entities);
            if (log.isTraceEnabled()) {
                log.trace("List of entities serialization result: {}", json);
            }
            return json;
        } catch (IOException e) {
            throw new SBusinessDataRepositorySerializationException("Unable to serialize list of Entity", e);
        }
    }

    @Override
    @Deprecated
    public String serializeCountResult(List<Long> list, String entityClassName) {
        String json = "[" + list.get(0).toString() + "]";
        if (log.isTraceEnabled()) {
            log.trace("Count serialization result: {}", json);
        }
        return json;
    }

    @Override
    public String serializeScalarResult(List<?> list, String entityClassName, boolean useStandardShape) {
        Object value = (list == null || list.isEmpty()) ? null : list.get(0);
        String json;
        if (useStandardShape) {
            // Standard shape: { "value": 10 }
            json = "{ \"value\": " + (value == null ? "null" : value.toString()) + " }";
        } else {
            // Legacy shape: [ 10 ]
            json = "[" + (value == null ? "null" : value.toString()) + "]";
        }
        if (log.isTraceEnabled()) {
            log.trace("Scalar serialization result: {}", json);
        }
        return json;
    }

    @Override
    public String serializeEntityQueryResult(List<? extends Entity> entities, String businessDataURIPattern,
            boolean useStandardShape, boolean queryReturnsMultipleResults)
            throws SBusinessDataRepositorySerializationException {
        if (!useStandardShape || queryReturnsMultipleResults) {
            // Legacy shape (always array) OR query designed to return List: return array [{...}]
            return serializeEntities(entities, businessDataURIPattern);
        }
        // Standard shape with query designed to return single entity
        if (entities != null && entities.size() == 1) {
            // Return object {...}
            return serializeEntity(entities.get(0), businessDataURIPattern);
        } else if (entities == null || entities.isEmpty()) {
            // Return empty object {}
            return EMPTY_OBJECT;
        } else {
            // Multiple results from single-entity query (shouldn't happen normally): return array
            return serializeEntities(entities, businessDataURIPattern);
        }
    }

}
