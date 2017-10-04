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

package org.bonitasoft.engine.business.data.impl.jackson;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ClassUtils;
import org.bonitasoft.engine.business.data.impl.jackson.utils.ExtraPropertyUtils;
import org.bonitasoft.engine.business.data.impl.jackson.writer.ExtraBeanPropertyWriter;
import org.bonitasoft.engine.business.data.impl.jackson.writer.IgnoredPropertyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;

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
            }
            else {
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
        if (MethodHandler.class.isAssignableFrom(rawClass) || Proxy.class.isAssignableFrom(rawClass)) {
            return true;
        }
        return false;
    }

    private static List<String> getNames(List<Class<?>> classes) {
        return classes.stream().map(Class::getName).collect(Collectors.toList());
    }

}
