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
        //System.err.println("----- CustomPropertySource(" + name + ") Thread: " + Thread.currentThread().getId() + "-----");
        //Thread.dumpStack();
        //System.err.println("Loading properties: " + properties);
        //System.err.println("----- END CustomPropertySource(" + name + ") Thread: " + Thread.currentThread().getId() + "-----");
    }

    @Override
    public Object getProperty(String key) {
        final Object value = properties.get(key);
        System.err.println("--- (" + name + " --- Retrieving " + key + "=" + value);
        return value;
    }
}
