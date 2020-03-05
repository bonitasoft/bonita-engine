/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine.xa;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.XAConnection;
import javax.sql.XADataSource;

public class XADataSourceIsSameRMOverride implements XADataSource {

    private XADataSource xaDataSource;

    private XADataSourceIsSameRMOverride(XADataSource xaDataSource) {
        this.xaDataSource = xaDataSource;
    }

    public static XADataSource overrideSameRM(XADataSource xaDataSource) {
        return new XADataSourceIsSameRMOverride(xaDataSource);
    }

    @Override
    public XAConnection getXAConnection() throws SQLException {
        return XAConnectionIsSameRMOverride.overrideSameRM(xaDataSource.getXAConnection());
    }

    @Override
    public XAConnection getXAConnection(String user, String password) throws SQLException {
        return XAConnectionIsSameRMOverride.overrideSameRM(xaDataSource.getXAConnection(user, password));
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return xaDataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        xaDataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        xaDataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return xaDataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return xaDataSource.getParentLogger();
    }
}
