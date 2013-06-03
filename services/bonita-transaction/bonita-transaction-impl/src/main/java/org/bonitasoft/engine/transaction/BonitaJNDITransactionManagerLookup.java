/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.transaction;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

public class BonitaJNDITransactionManagerLookup implements BonitaTransactionManagerLookup {

    private final String initialContextFactory;

    private final String jndiLookupPath;

    public BonitaJNDITransactionManagerLookup(final String initialContextFactory, final String jndiLookupPath) {
        this.initialContextFactory = initialContextFactory;
        this.jndiLookupPath = jndiLookupPath;
    }

    @Override
    public TransactionManager getTransactionManager() {
        try {
            final Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, initialContextFactory);

            final Context ctx = new InitialContext(env);
            return (TransactionManager) ctx.lookup(jndiLookupPath);
        } catch (final NamingException e) {
            throw new RuntimeException(e);
        }
    }

}
