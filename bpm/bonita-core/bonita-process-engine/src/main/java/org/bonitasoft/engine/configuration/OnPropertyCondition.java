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
        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(ConditionalOnProperty.class.getName());

        String propertyName = ((String) annotationAttributes.get("value"));
        Boolean defaultValue = ((Boolean) annotationAttributes.get("enableIfMissing"));
        defaultValue = (defaultValue == null)?false:defaultValue;
        if (propertyName == null) {
            return false;
        }
        Boolean property = context.getEnvironment().getProperty(propertyName, Boolean.class);
        if (property == null) {
            return defaultValue;
        }
        return property;
    }
}
