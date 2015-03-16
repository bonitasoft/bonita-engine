/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.persistence;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.LongType;
import org.hibernate.usertype.UserType;

/**
 * @author Emmanuel Duchastenier
 */
public class DateStoredAsLongUserType implements UserType, Serializable {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    private Class<? extends Enum> enumClass;

    private static final LongType type = new LongType();

    private static int[] sqlTypes = new int[] { type.sqlType() };

    @Override
    public Class<?> returnedClass() {
        return Date.class;
    }

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names, final SessionImplementor session, final Object owner) throws HibernateException,
            SQLException {
        final Object identifier = type.get(rs, names[0], session);
        if (identifier == null) {
            return null;
        }
        return new Date((Long) identifier);
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index, final SessionImplementor session) throws HibernateException {
        try {
            if (value == null) {
                st.setNull(index, type.sqlType());
            } else {
                type.set(st, ((Date) value).getTime(), index, session);
            }
        } catch (final Exception e) {
        }

    }

    @Override
    public int[] sqlTypes() {
        return sqlTypes;
    }

    @Override
    public Object assemble(final Serializable cached, final Object owner) {
        return cached;
    }

    @Override
    public Object deepCopy(final Object value) {
        return value;
    }

    @Override
    public Serializable disassemble(final Object value) {
        return (Serializable) value;
    }

    @Override
    public boolean equals(final Object x, final Object y) {
        return x == y;
    }

    @Override
    public int hashCode(final Object x) {
        return x.hashCode();
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Object replace(final Object original, final Object target, final Object owner) {
        return original;
    }

}
