package org.bonitasoft.engine.local;

import javax.naming.CompositeName;
import javax.naming.Name;
import javax.naming.NameParser;
import javax.naming.NamingException;

public class SimpleNameParser implements NameParser {

    public SimpleNameParser(final String name) {
        // TODO Auto-generated constructor stub
    }

    @Override
    public Name parse(final String name) throws NamingException {
        return new CompositeName(name);
    }

}
