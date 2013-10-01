package org.bonitasoft.engine.local;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * A JNDI context implementation that uses the memory as a dictionary of objects.
 */
public class SimpleMemoryContext implements Context {

    private final Map<String, Object> dictionary = new ConcurrentHashMap<String, Object>();

    public void clear() {
        dictionary.clear();
    }

    @Override
    public Object lookup(Name name) throws NamingException {
        return lookup(name.toString());
    }

    @Override
    public Object lookup(String name) throws NamingException {
        // System.out.println(toString() + " ~~~~ lookup " + name + " contains ? " + dictionary.containsKey(name));
        if (dictionary.containsKey(name)) {
            return dictionary.get(name);
        }
        throw new NameNotFoundException("Name " + name + " is not bound !");
    }

    @Override
    public void bind(Name name, Object o) throws NamingException {
        bind(name.toString(), o);
    }

    @Override
    public void bind(String name, Object o) throws NamingException {
        // System.out.println(toString() + " ~~~~ binding " + name + " with " + o + " already bound ? " + dictionary.containsKey(name));
        if (dictionary.containsKey(name)) {
            throw new NameAlreadyBoundException("Name " + name + " already bound!");
        }
        rebind(name, o);
    }

    @Override
    public void rebind(Name name, Object o) {
        rebind(name.toString(), o);
    }

    @Override
    public void rebind(String name, Object o) {
        dictionary.put(name, o);
    }

    @Override
    public void unbind(Name name) throws NamingException {
        unbind(name.toString());
    }

    @Override
    public void unbind(String name) throws NamingException {
        if (!dictionary.containsKey(name)) {
            throw new NameNotFoundException("No such name " + name + " is bound!");
        }
        dictionary.remove(name);
    }

    @Override
    public void rename(Name oldName, Name newName) throws NamingException {
        rename(oldName.toString(), newName.toString());
    }

    @Override
    public void rename(String oldName, String newName) throws NamingException {
        Object object = lookup(oldName);
        bind(newName, object);
        unbind(oldName);
    }

    @Override
    public NamingEnumeration<NameClassPair> list(Name name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NamingEnumeration<NameClassPair> list(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NamingEnumeration<Binding> listBindings(Name name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NamingEnumeration<Binding> listBindings(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void destroySubcontext(Name name) {
        destroySubcontext(name.toString());
    }

    @Override
    public void destroySubcontext(String name) {
        dictionary.remove(name);
    }

    @Override
    public Context createSubcontext(Name name) throws NamingException {
        return createSubcontext(name.toString());
    }

    @Override
    public Context createSubcontext(String name) throws NamingException {
        Context subContext = new SimpleMemoryContext();
        bind(name, subContext);
        return subContext;
    }

    @Override
    public Object lookupLink(Name name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object lookupLink(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NameParser getNameParser(Name name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public NameParser getNameParser(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Name composeName(Name name, Name name1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String composeName(String string, String string1) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object addToEnvironment(String string, Object o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object removeFromEnvironment(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Hashtable<?, ?> getEnvironment() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() {
        // System.out.println("Closing SimpleMemoryContext");
        // Thread.dumpStack();
        // clear();
    }

    @Override
    public String getNameInNamespace() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
