/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.data.impl.hibernatepatch;

import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.hibernate.internal.CoreLogging;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.resource.transaction.spi.DdlTransactionIsolator;
import org.hibernate.tool.schema.internal.exec.JdbcConnectionAccessProvidedConnectionImpl;
import org.hibernate.tool.schema.internal.exec.JdbcContext;
import org.hibernate.tool.schema.spi.SchemaManagementException;

/**
 * Specialized DdlTransactionIsolator for cases where we have a user provided Connection
 *
 * @author Steve Ebersole
 */
class DdlTransactionIsolatorProvidedConnectionImpl implements DdlTransactionIsolator {

    private static final CoreMessageLogger LOG = CoreLogging
            .messageLogger(DdlTransactionIsolatorProvidedConnectionImpl.class);

    private final JdbcContext jdbcContext;

    public DdlTransactionIsolatorProvidedConnectionImpl(JdbcContext jdbcContext) {
        assert jdbcContext.getJdbcConnectionAccess() instanceof JdbcConnectionAccessProvidedConnectionImpl;
        this.jdbcContext = jdbcContext;
    }

    @Override
    public JdbcContext getJdbcContext() {
        return jdbcContext;
    }

    @Override
    public Connection getIsolatedConnection() {
        try {
            return jdbcContext.getJdbcConnectionAccess().obtainConnection();
        } catch (SQLException e) {
            // should never happen
            throw new SchemaManagementException(
                    "Error accessing user-provided Connection via JdbcConnectionAccessProvidedConnectionImpl", e);
        }
    }

    @Override
    public void prepare() {
    }

    @Override
    public void release() {
        JdbcConnectionAccess connectionAccess = jdbcContext.getJdbcConnectionAccess();
        if (!(connectionAccess instanceof JdbcConnectionAccessProvidedConnectionImpl)) {
            throw new IllegalStateException(
                    "DdlTransactionIsolatorProvidedConnectionImpl should always use a JdbcConnectionAccessProvidedConnectionImpl");
        }
        try {
            // While passing the connection to the releaseConnection method might be suitable for other `JdbcConnectionAccess` implementations,
            // it has no meaning for JdbcConnectionAccessProvidedConnectionImpl because, in this case, the connection is wrapped
            // and we don't have access to it upon releasing via the DdlTransactionIsolatorProvidedConnectionImpl.
            connectionAccess.releaseConnection(null);
        } catch (SQLException ignore) {
            LOG.unableToReleaseIsolatedConnection(ignore);
        }
    }
}
