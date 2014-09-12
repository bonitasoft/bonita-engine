package org.bonitasoft.engine.local;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.spi.InitialContextFactory;

/**
 * A factory of a naming context that uses the memory as dictionary of objects. Useful to tests
 * objects using JNDI to get dependencies.
 */
public class SimpleMemoryContextFactory implements InitialContextFactory {

    private static final SimpleMemoryContext context = new SimpleMemoryContext();

    @Override
    public Context getInitialContext(final Hashtable<?, ?> environment) {
        return context;
    }
}
