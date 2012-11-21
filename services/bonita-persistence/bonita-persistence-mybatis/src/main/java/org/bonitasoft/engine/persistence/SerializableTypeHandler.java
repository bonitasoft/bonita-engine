/**
 * Copyright (C) 2011 BonitaSoft S.A.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

/**
 * @author Baptiste Mesta
 */
public class SerializableTypeHandler implements TypeHandler<Serializable> {

    @Override
    public void setParameter(final PreparedStatement ps, final int i, final Serializable parameter, final JdbcType jdbcType) throws SQLException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(parameter);
            final byte[] yourBytes = bos.toByteArray();

            ps.setBytes(i, yourBytes);
        } catch (final IOException e) {
            throw new SQLException(e);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            try {
                bos.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Serializable getResult(final ResultSet rs, final String columnName) throws SQLException {
        return toObject(rs.getBytes(columnName));
    }

    private Serializable toObject(final byte[] bytes) throws SQLException {
        if (bytes == null) {
            return null;
        }
        final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            final Object o = in.readObject();

            return (Serializable) o;
        } catch (final IOException e) {
            throw new SQLException(e);
        } catch (final ClassNotFoundException e) {
            throw new SQLException(e);
        } finally {
            try {
                bis.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Serializable getResult(final CallableStatement cs, final int columnIndex) throws SQLException {
        return toObject(cs.getBytes(columnIndex));
    }

    @Override
    public Serializable getResult(final ResultSet rs, final int columnIndex) throws SQLException {
        return toObject(rs.getBytes(columnIndex));
    }

}
