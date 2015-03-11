package org.bonitasoft.engine.service.impl;

import java.util.Properties;

import org.springframework.core.env.PropertySource;

/**
 * @author Charles Souillard
 */
public class CustomPropertySource extends PropertySource<String> {

    private final Properties properties;

    public CustomPropertySource(final String name, final Properties properties) {
        super(name);
        this.properties = properties;
    }

    @Override
    public Object getProperty(String key) {
        return properties.get(key);
    }
}
