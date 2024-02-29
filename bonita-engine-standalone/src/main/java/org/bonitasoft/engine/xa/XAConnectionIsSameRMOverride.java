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

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.ConnectionEventListener;
import javax.sql.StatementEventListener;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

public class XAConnectionIsSameRMOverride implements XAConnection {

    private XAConnection xaConnection;

    private XAConnectionIsSameRMOverride(XAConnection xaConnection) {
        this.xaConnection = xaConnection;
    }

    static XAConnection overrideSameRM(XAConnection xaConnection) {
        return new XAConnectionIsSameRMOverride(xaConnection);
    }

    @Override
    public XAResource getXAResource() throws SQLException {
        return XAResourceIsSameRMOverride.overrideSameRM(xaConnection.getXAResource());
    }

    @Override
    public Connection getConnection() throws SQLException {
        return xaConnection.getConnection();
    }

    @Override
    public void close() throws SQLException {
        xaConnection.close();
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        xaConnection.addConnectionEventListener(listener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        xaConnection.removeConnectionEventListener(listener);
    }

    @Override
    public void addStatementEventListener(StatementEventListener listener) {
        xaConnection.addStatementEventListener(listener);

    }

    @Override
    public void removeStatementEventListener(StatementEventListener listener) {
        xaConnection.removeStatementEventListener(listener);

    }
}
