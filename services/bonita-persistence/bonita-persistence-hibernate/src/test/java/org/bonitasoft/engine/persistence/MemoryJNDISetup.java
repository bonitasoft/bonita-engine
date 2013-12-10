package org.bonitasoft.engine.persistence;

import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class MemoryJNDISetup {

    private final InitialContext ctx;

    private final Map<String, Object> jndiMapping;

    public MemoryJNDISetup(final InitialContext ctx, final Map<String, Object> jndiMapping) {
        super();
        this.ctx = ctx;
        this.jndiMapping = jndiMapping;
    }

    public void init() throws NamingException {
        for (final Map.Entry<String, Object> addToJndi : jndiMapping.entrySet()) {
            System.out.println("Binding " + addToJndi.getKey() + " @ " + addToJndi.getValue());
            ctx.bind(addToJndi.getKey(), addToJndi.getValue());
        }
    }

    public void clean() throws NamingException {
        for (final Map.Entry<String, Object> removeFromJndi : jndiMapping.entrySet()) {
            System.out.println("Unbinding " + removeFromJndi.getKey());
            ctx.unbind(removeFromJndi.getKey());
        }
    }

}
