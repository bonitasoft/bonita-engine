/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.platform.configuration.datasource;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.quartz.utils.ConnectionProvider;

/**
 * This is a hack to let Quartz access datasource beans from SprigContext
 * Quartz support custom connection providers but not non primitive parameters for them
 */
public class QuartzConnectionProvider implements ConnectionProvider {

    private Boolean isXaDataSource;
    private DataSource dataSource;

    /**
     * this is called by quartz (given by the configuration)
     *
     * @param isXaDataSource true if the datasource returned by this configuration provider should be the xa datasource
     */
    public void setXaDataSource(boolean isXaDataSource) {
        this.isXaDataSource = isXaDataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void shutdown() throws SQLException {

    }

    @Override
    public void initialize() throws SQLException {
        if (isXaDataSource == null) {
            throw new IllegalStateException("Quartz datasource is not set");
        }
        if (isXaDataSource) {
            dataSource = QuartzDataSourceAccessorProvider.getInstance().getBonitaDataSource();
        } else {
            dataSource = QuartzDataSourceAccessorProvider.getInstance().getBonitaNonXaDataSource();
        }
    }
}
