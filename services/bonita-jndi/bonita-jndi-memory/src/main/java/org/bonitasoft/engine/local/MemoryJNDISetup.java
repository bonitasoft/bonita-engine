package org.bonitasoft.engine.local;

import java.util.Map;

import javax.naming.NamingException;

import org.springframework.jndi.JndiTemplate;

public class MemoryJNDISetup {

    private final JndiTemplate jndiTemplate;

    private final Map<String, Object> jndiMapping;

    public MemoryJNDISetup(final JndiTemplate jndiTemplate, final Map<String, Object> jndiMapping) {
        super();
        this.jndiTemplate = jndiTemplate;
        this.jndiMapping = jndiMapping;
    }

    public void init() throws NamingException {
        for (final Map.Entry<String, Object> addToJndi : jndiMapping.entrySet()) {
            System.out.println("Binding " + addToJndi.getKey() + " @ " + addToJndi.getValue());
            jndiTemplate.bind(addToJndi.getKey(), addToJndi.getValue());
        }
    }

    public void clean() throws NamingException {
        for (final Map.Entry<String, Object> removeFromJndi : jndiMapping.entrySet()) {
            System.out.println("Unbinding " + removeFromJndi.getKey());
            jndiTemplate.unbind(removeFromJndi.getKey());
        }
    }

}
