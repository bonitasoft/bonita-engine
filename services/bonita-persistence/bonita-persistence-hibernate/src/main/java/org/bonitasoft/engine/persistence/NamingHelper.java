package org.bonitasoft.engine.persistence;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.hibernate.cfg.AvailableSettings;

public final class NamingHelper {

    public static InitialContext getInitialContext(final Properties props) throws NamingException {
        final Hashtable<?, ?> hash = getJndiProperties(props);
        try {
            return hash.size() == 0 ? new InitialContext() : new InitialContext(hash);
        } catch (final NamingException e) {
            throw e;
        }
    }

    public static void bind(Context ctx, final String name, final Object val) throws NamingException {
        try {
            ctx.rebind(name, val);
        } catch (final Exception e) {
            Name n = ctx.getNameParser("").parse(name);
            while (n.size() > 1) {
                final String ctxName = n.get(0);
                Context subctx = null;
                try {
                    subctx = (Context) ctx.lookup(ctxName);
                } catch (final NameNotFoundException nfe) {
                }
                if (subctx != null) {
                    ctx = subctx;
                } else {
                    ctx = ctx.createSubcontext(ctxName);
                }
                n = n.getSuffix(1);
            }
            ctx.rebind(n, val);
        }
    }

    public static Properties getJndiProperties(final Properties properties) {
        final HashSet<String> specialProps = new HashSet<String>();
        specialProps.add(AvailableSettings.JNDI_CLASS);
        specialProps.add(AvailableSettings.JNDI_URL);
        final Iterator<?> iter = properties.keySet().iterator();
        final Properties result = new Properties();
        while (iter.hasNext()) {
            final String prop = (String) iter.next();
            if (prop.indexOf(AvailableSettings.JNDI_PREFIX) > -1 && !specialProps.contains(prop)) {
                result.setProperty(prop.substring(AvailableSettings.JNDI_PREFIX.length() + 1), properties.getProperty(prop));
            }
        }

        final String jndiClass = properties.getProperty(AvailableSettings.JNDI_CLASS);
        final String jndiURL = properties.getProperty(AvailableSettings.JNDI_URL);
        // we want to be able to just use the defaults,
        // if JNDI environment properties are not supplied
        // so don't put null in anywhere
        if (jndiClass != null) {
            result.put(Context.INITIAL_CONTEXT_FACTORY, jndiClass);
        }
        if (jndiURL != null) {
            result.put(Context.PROVIDER_URL, jndiURL);
        }
        return result;
    }

    private NamingHelper() {
    }

}
