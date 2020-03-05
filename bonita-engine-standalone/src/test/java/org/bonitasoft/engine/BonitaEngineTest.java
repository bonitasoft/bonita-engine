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
package org.bonitasoft.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;

public class BonitaEngineTest {

    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Test
    public void should_set_db_vendor_system_property_according_to_database_configuration() throws Exception {
        BonitaEngine bonitaEngine = newBonitaEngine();
        bonitaEngine.setBonitaDatabaseConfiguration(BonitaDatabaseConfiguration.builder()
                .dbVendor("postgres")
                .url("jdbc:postgresql://someServer:5433/bonitadb")
                .user("user")
                .build());

        bonitaEngine.initializeEnvironment();

        assertThat(System.getProperty(BonitaEngine.BONITA_DB_VENDOR)).isEqualTo("postgres");
    }

    @Test
    public void should_set_db_vendor_system_property_to_h2_by_default() throws Exception {
        BonitaEngine bonitaEngine = newBonitaEngine();

        bonitaEngine.initializeEnvironment();

        assertThat(System.getProperty(BonitaEngine.BONITA_DB_VENDOR)).isEqualTo("h2");
    }

    @Test
    public void should_configure_bonita_datasource_to_h2_by_default() throws Exception {
        BonitaEngine bonitaEngine = newBonitaEngine();

        bonitaEngine.initializeEnvironment();

        JdbcDataSource xaDataSourceInstance = (JdbcDataSource) bonitaEngine.getBonitaDataSource()
                .getXaDataSourceInstance();
        assertThat(xaDataSourceInstance.getURL()).contains("h2");
        assertThat(xaDataSourceInstance.getDescription()).contains("RawDataSource of h2");

    }

    private BonitaEngine newBonitaEngine() {
        return new BonitaEngine();
    }

}
