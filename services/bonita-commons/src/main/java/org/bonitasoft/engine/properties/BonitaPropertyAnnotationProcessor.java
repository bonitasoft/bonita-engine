/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
package org.bonitasoft.engine.properties;

import java.lang.reflect.Field;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.InvalidPropertyException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Spring {@link BeanPostProcessor} that parses {@link BonitaProperty} and sets the value of the annotated field.
 * If a deprecated property name is detected, then a warning message is issued.
 *
 * @see BonitaProperty
 */
@Component
@Slf4j
public class BonitaPropertyAnnotationProcessor implements BeanPostProcessor {

    @Autowired
    private Environment environment;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        for (Field field : bean.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(BonitaProperty.class)) {
                BonitaProperty bonitaProperty = field.getAnnotation(BonitaProperty.class);
                String propertyName = bonitaProperty.value();
                if (propertyName.isEmpty()) {
                    propertyName = bonitaProperty.name();
                }
                if (propertyName.isEmpty()) {
                    throw new InvalidPropertyException(bean.getClass(), "unset property name",
                            "@BonitaProperty 'name' or 'value' attribute is mandatory");
                }
                String[] deprecated = bonitaProperty.deprecated();

                String resolvedValue = null;
                for (String deprecatedPropertyName : deprecated) {
                    resolvedValue = environment.getProperty(deprecatedPropertyName);
                    if (resolvedValue != null) {
                        log.warn("Warning: property '{}' is deprecated. Please use '{}' instead",
                                deprecatedPropertyName, propertyName);
                        break;
                    }
                }
                if (resolvedValue == null) {
                    resolvedValue = environment.getProperty(propertyName);
                }

                // Set the field value if necessary
                field.setAccessible(true);
                try {
                    field.set(bean, resolvedValue);
                } catch (IllegalAccessException e) {
                    throw new BeanInitializationException(
                            "Failed to set BonitaProperty-annotated field value for bean " + beanName, e);
                }
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
