/**
 * Copyright (C) 2017 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

package org.bonitasoft.engine.persistence;

import org.bonitasoft.engine.data.instance.model.impl.XStreamFactory;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.CharacterStream;
import org.hibernate.engine.jdbc.internal.CharacterStreamImpl;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.DataHelper;
import org.hibernate.type.descriptor.java.StringTypeDescriptor;

import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.SQLException;

public class XMLTypeDescriptor
        extends AbstractTypeDescriptor<Serializable> {

    public XMLTypeDescriptor() {
        super(Serializable.class);
    }


    @Override
    public boolean areEqual(Serializable one, Serializable another) {
        if (one == another) {
            return true;
        }
        if (one == null || another == null) {
            return false;
        }
        return one.equals(another);
    }

    @Override
    public String toString(Serializable value) {
        return XStreamFactory.getXStream().toXML(value);
    }

    @Override
    public Serializable fromString(String string) {
        return ((Serializable) XStreamFactory.getXStream().fromXML((string)));
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public <X> X unwrap(Serializable value, Class<X> type, WrapperOptions options) {

        if (value == null) {
            return null;
        }
        if (String.class.isAssignableFrom(type)) {
            return (X) toString(value);
        }
        if (Reader.class.isAssignableFrom(type)) {
            return (X) new StringReader(toString(value));
        }
        if (CharacterStream.class.isAssignableFrom(type)) {
            return (X) new CharacterStreamImpl(toString(value));
        }
        if (Clob.class.isAssignableFrom(type)) {
            return (X) options.getLobCreator().createClob(toString(value));
        }
        if (DataHelper.isNClob(type)) {
            return (X) options.getLobCreator().createNClob(toString(value));
        }
        throw unknownUnwrap(type);
    }

    @Override
    public <X> Serializable wrap(X value, WrapperOptions options) {
        if (value == null) {
            return null;
        }
        if (String.class.isAssignableFrom(value.getClass())) {
            return fromString((String) value);
        }
        if (Reader.class.isInstance(value)) {
            return fromReader((Reader) value);
        }
        if (Clob.class.isAssignableFrom(value.getClass())) {
            return fromReader(extractReader((Clob) value));
        }
        throw unknownWrap(value.getClass());
    }

    private Reader extractReader(Clob value) {
        Reader reader;
        try {
            reader = value.getCharacterStream();
        } catch (SQLException e) {
            throw new HibernateException("Unable to get the Clob value", e);
        }
        return reader;
    }

    private Serializable fromReader(Reader characterStream) {
        return (Serializable) XStreamFactory.getXStream().fromXML(characterStream);
    }

}
