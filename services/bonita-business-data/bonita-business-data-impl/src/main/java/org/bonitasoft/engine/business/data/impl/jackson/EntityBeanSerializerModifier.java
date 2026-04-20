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
package org.bonitasoft.engine.business.data.impl.jackson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import org.apache.commons.lang3.ClassUtils;
import org.bonitasoft.engine.business.data.impl.jackson.utils.ExtraPropertyUtils;
import org.bonitasoft.engine.business.data.impl.jackson.writer.ExtraBeanPropertyWriter;
import org.bonitasoft.engine.business.data.impl.jackson.writer.IgnoredPropertyWriter;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityBeanSerializerModifier extends BeanSerializerModifier {

    private static Logger LOG = LoggerFactory.getLogger(EntityBeanSerializerModifier.class);

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc,
            List<BeanPropertyWriter> beanProperties) {
        LOG.trace("Changing list of property writers for {}", beanDesc.getClassInfo());
        List<BeanPropertyWriter> newProperties = new ArrayList<>();

        if (shouldBeIgnored(beanDesc)) {
            LOG.trace("Ignoring all properties of this bean");
            return newProperties;
        }

        for (BeanPropertyWriter beanPropertyWriter : beanProperties) {
            LOG.trace("{}", beanPropertyWriter);
            LOG.trace("Bean type {}", beanPropertyWriter.getType());

            if (shouldBeReplacedByLink(beanPropertyWriter)) {
                LOG.trace("Has to be replaced by link");
                BeanPropertyWriter ignoredPropertyWriter = new IgnoredPropertyWriter(beanPropertyWriter);
                LOG.trace("Adding only an ignored property writer {}", ignoredPropertyWriter);
                newProperties.add(ignoredPropertyWriter);
            } else {
                newProperties.add(beanPropertyWriter);
                if (ExtraPropertyUtils.shouldAddExtraProperty(beanPropertyWriter)) {
                    LOG.trace("Will have an additional property");
                    BeanPropertyWriter additionalPropertyWriter = ExtraBeanPropertyWriter.newWriter(beanPropertyWriter);
                    LOG.trace("Adding new property {}", additionalPropertyWriter);
                    newProperties.add(additionalPropertyWriter);
                }
            }
        }
        return newProperties;
    }

    private static boolean shouldBeReplacedByLink(BeanPropertyWriter propertyWriter) {
        return propertyWriter != null && propertyWriter.getAnnotation(JsonIgnore.class) != null;
    }

    private static boolean shouldBeIgnored(BeanDescription beanDescription) {
        JavaType type = beanDescription.getType();
        Class<?> rawClass = type.getRawClass();
        if (LOG.isTraceEnabled()) {
            LOG.trace("Checking if it has to be ignored - {} / {}", type, rawClass);
            LOG.trace("Interfaces: {}", getNames(ClassUtils.getAllInterfaces(rawClass)));
            LOG.trace("Superclasses: {}", getNames(ClassUtils.getAllSuperclasses(rawClass)));
        }
        return MethodHandler.class.isAssignableFrom(rawClass) || Proxy.class.isAssignableFrom(rawClass);
    }

    @Override
    public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc,
            JsonSerializer<?> serializer) {
        if (HibernateProxy.class.isAssignableFrom(beanDesc.getBeanClass())) {
            LOG.trace("Registering HibernateProxy unwrapping serializer for {}", beanDesc.getBeanClass().getName());
            return new HibernateProxyUnwrappingSerializer(serializer);
        }
        return serializer;
    }

    private static List<String> getNames(List<Class<?>> classes) {
        return classes.stream().map(Class::getName).collect(Collectors.toList());
    }

    private static class HibernateProxyUnwrappingSerializer extends JsonSerializer<HibernateProxy> {

        private final JsonSerializer<HibernateProxy> defaultSerializer;

        @SuppressWarnings("unchecked")
        HibernateProxyUnwrappingSerializer(JsonSerializer<?> defaultSerializer) {
            this.defaultSerializer = (JsonSerializer<HibernateProxy>) defaultSerializer;
        }

        @Override
        public void serialize(HibernateProxy value, JsonGenerator gen, SerializerProvider serializers)
                throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }
            LazyInitializer lazyInitializer = value.getHibernateLazyInitializer();
            if (!lazyInitializer.isUninitialized()) {
                Object implementation = lazyInitializer.getImplementation();
                if (implementation != null) {
                    LOG.debug("Unwrapping initialized HibernateProxy for entity {} (id={}) to actual type {}",
                            lazyInitializer.getEntityName(), lazyInitializer.getIdentifier(),
                            implementation.getClass().getName());
                    serializers.defaultSerializeValue(implementation, gen);
                } else {
                    LOG.debug(
                            "HibernateProxy for entity {} (id={}) getImplementation() returned null — serializing proxy as-is",
                            lazyInitializer.getEntityName(), lazyInitializer.getIdentifier());
                    defaultSerializer.serialize(value, gen, serializers);
                }
            } else {
                LOG.debug(
                        "Serializing uninitialized HibernateProxy for entity {} (id={}) without unwrapping — JSON may be empty",
                        lazyInitializer.getEntityName(), lazyInitializer.getIdentifier());
                defaultSerializer.serialize(value, gen, serializers);
            }
        }
    }

}
