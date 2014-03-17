package org.bonitasoft.engine.local;

import java.util.Map;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jndi.JndiTemplate;

public class MemoryJNDISetup {

    private final JndiTemplate jndiTemplate;

    private final Map<String, Object> jndiMapping;

    private final Logger logger = LoggerFactory.getLogger(MemoryJNDISetup.class.getSimpleName());

    public MemoryJNDISetup(final JndiTemplate jndiTemplate, final Map<String, Object> jndiMapping) {
        super();
        this.jndiTemplate = jndiTemplate;
        this.jndiMapping = jndiMapping;
    }

    public void init() throws NamingException {
        for (final Map.Entry<String, Object> addToJndi : jndiMapping.entrySet()) {
            logger.info("Binding " + addToJndi.getKey() + " @ " + addToJndi.getValue());
            jndiTemplate.bind(addToJndi.getKey(), addToJndi.getValue());
        }
    }

    public void clean() throws NamingException {
        for (final Map.Entry<String, Object> removeFromJndi : jndiMapping.entrySet()) {
            logger.info("Unbinding " + removeFromJndi.getKey());
            jndiTemplate.unbind(removeFromJndi.getKey());
        }
    }

}
