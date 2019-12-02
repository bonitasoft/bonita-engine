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
package org.bonitasoft.engine.configuration;

import java.util.Map;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author Danila Mazour
 */
public class OnPropertyCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> annotationAttributes = metadata
                .getAnnotationAttributes(ConditionalOnProperty.class.getName());

        String propertyName = ((String) annotationAttributes.get("value"));
        boolean enableIfMissing = (boolean) annotationAttributes.get("enableIfMissing");
        boolean havingValue = (boolean) annotationAttributes.get("havingValue");
        if (propertyName == null) {
            return false;
        }
        Boolean property = context.getEnvironment().getProperty(propertyName, Boolean.class);
        if (property == null) {
            return enableIfMissing;
        }
        return property.equals(havingValue);
    }

}
