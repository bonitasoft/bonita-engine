/**
 * Copyright (C) 2016 Bonitasoft S.A.
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
package org.bonitasoft.platform.configuration.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;

/**
 * @author Laurent Leseigneur
 */
public class BonitaConfigurationTenantCleaner implements BatchPreparedStatementSetter {

    public static final String DELETE_TENANT_CONFIGURATION = "DELETE FROM configuration WHERE tenant_id = ?";

    private final long tenantId;

    public BonitaConfigurationTenantCleaner(long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
        preparedStatement.setLong(1, tenantId);
    }

    @Override
    public int getBatchSize() {
        return 1;
    }
}
