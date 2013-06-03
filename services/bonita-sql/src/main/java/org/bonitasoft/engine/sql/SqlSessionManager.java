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
package org.bonitasoft.engine.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlSessionManager {

    public static SqlSession getSession(final String url, final String driver, final String user, final String password) throws SqlSessionException {
        return getSession(url, driver, user, password, true);
    }

    public static SqlSession getSession(final String url, final String driver, final String user, final String password, final boolean autoCommit)
            throws SqlSessionException {
        try {
            Class.forName(driver, true, Thread.currentThread().getContextClassLoader()).newInstance();
            final Connection connection = DriverManager.getConnection(url, user, password);
            connection.setAutoCommit(autoCommit);
            return new SqlSession(connection);
        } catch (final InstantiationException e) {
            throw new SqlSessionException(e);
        } catch (final IllegalAccessException e) {
            throw new SqlSessionException(e);
        } catch (final ClassNotFoundException e) {
            throw new SqlSessionException(e);
        } catch (final SQLException e) {
            throw new SqlSessionException(e);
        }
    }

}
