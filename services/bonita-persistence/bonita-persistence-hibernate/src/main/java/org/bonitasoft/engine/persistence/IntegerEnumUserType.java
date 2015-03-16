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
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.IntegerType;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;

/**
 * Use 'enumClass' to set the enum class type
 * use 'identifierMethod' param to set the method name returning the integer value of the enum. Defaults to 'order' method
 * 
 * @author Emmanuel Duchastenier
 */
public class IntegerEnumUserType implements UserType, ParameterizedType, Serializable {

    private static final long serialVersionUID = 8455087214832419499L;

    private static final String DEFAULT_IDENTIFIER_METHOD_NAME = "order";

    private static final String DEFAULT_VALUE_OF_METHOD_NAME = "fromInt";

    @SuppressWarnings("rawtypes")
    private Class<? extends Enum> enumClass;

    private Class<?> identifierType;

    private Method identifierMethod;

    private Method valueOfMethod;

    private final IntegerType type = new IntegerType();

    private int[] sqlTypes;

    @Override
    public void setParameterValues(final Properties parameters) {
        final String enumClassName = parameters.getProperty("enumClass");
        try {
            enumClass = Class.forName(enumClassName).asSubclass(Enum.class);
        } catch (final ClassNotFoundException cfne) {
            final String message = "Enum class not found";
            throw new HibernateException(message, cfne);
        }
        final String identifierMethodName = parameters.getProperty("identifierMethod", DEFAULT_IDENTIFIER_METHOD_NAME);
        try {
            identifierMethod = enumClass.getMethod(identifierMethodName, new Class[0]);
            identifierType = identifierMethod.getReturnType();
        } catch (final Exception e) {
            final String message = "Failed to obtain identifier method";
            throw new HibernateException(message, e);
        }
        sqlTypes = new int[] { type.sqlType() };
        final String valueOfMethodName = parameters.getProperty("valueOfMethod", DEFAULT_VALUE_OF_METHOD_NAME);
        try {
            valueOfMethod = enumClass.getMethod(valueOfMethodName, new Class[] { identifierType });
        } catch (final Exception e) {
            final String message = "Failed to obtain valueOf method";
            throw new HibernateException(message, e);
        }
    }

    @Override
    public Class<?> returnedClass() {
        return enumClass;
    }

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names, final SessionImplementor session, final Object owner) throws HibernateException,
            SQLException {
        final Object identifier = type.get(rs, names[0], session);
        if (identifier == null) {
            return null;
        }
        try {
            return valueOfMethod.invoke(null, new Object[] { identifier });
        } catch (final Exception e) {
            final StringBuilder stb = new StringBuilder("Exception while invoking valueOf method '");
            stb.append(valueOfMethod.getName());
            stb.append("' of enumeration class '");
            stb.append(enumClass);
            stb.append('\'');
            throw new HibernateException(stb.toString(), e);
        }
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index, final SessionImplementor session) throws HibernateException {
        try {
            if (value == null) {
                st.setNull(index, type.sqlType());
            } else {
                final Integer identifier = (Integer) identifierMethod.invoke(value, new Object[0]);
                type.set(st, identifier, index, session);
            }
        } catch (final Exception e) {
            final StringBuilder stb = new StringBuilder("Exception while invoking identifierMethod '");
            stb.append(valueOfMethod.getName());
            stb.append("' of enumeration class '");
            stb.append(enumClass);
            stb.append('\'');
            throw new HibernateException(stb.toString(), e);
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
