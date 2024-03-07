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

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BonitaDataSourceInitializerTest {

    @Rule
    public ExpectedException expectedExceptions = ExpectedException.none();
    private BonitaDataSourceInitializer bonitaDataSourceInitializer = new BonitaDataSourceInitializer();

    @Test
    public void should_fail_to_create_datasource_if_some_field_are_not_set() {
        BonitaDatabaseConfiguration configuration = BonitaDatabaseConfiguration.builder()
                .dbVendor("postgres")
                .url("jdbc::postgres")
                .build();

        expectedExceptions.expect(IllegalArgumentException.class);
        expectedExceptions.expectMessage("Database user not se");

        bonitaDataSourceInitializer.createDataSource(configuration);
    }

    @Test
    public void should_fail_to_create_datasource_configuration_is_empty() {
        expectedExceptions.expect(IllegalArgumentException.class);
        expectedExceptions.expectMessage("Database dbVendor not set");

        bonitaDataSourceInitializer.createDataSource(new BonitaDatabaseConfiguration());
    }

    @Test
    public void should_fail_if_db_vendor_is_not_supported() {
        BonitaDatabaseConfiguration configuration = BonitaDatabaseConfiguration.builder()
                .dbVendor("toto")
                .url("jdbc::postgres")
                .build();

        expectedExceptions.expect(IllegalArgumentException.class);
        expectedExceptions.expectMessage("Database db vendor toto is invalid");

        bonitaDataSourceInitializer.createDataSource(configuration);
    }

    @Test
    public void should_create_datasource_with_configuration() {
        BonitaDatabaseConfiguration configuration = BonitaDatabaseConfiguration.builder()
                .dbVendor("postgres")
                .driverClassName("org.postgres.MyCustomDriver")
                .url("jdbc::postgres")
                .user("myUser")
                .password("secret")
                .build();

        BasicDataSource dataSource = bonitaDataSourceInitializer.createDataSource(configuration);

        assertThat(dataSource.getUrl()).isEqualTo("jdbc::postgres");
        assertThat(dataSource.getUsername()).isEqualTo("myUser");
        assertThat(dataSource.getDriverClassName()).isEqualTo("org.postgres.MyCustomDriver");
        assertThat(dataSource.getPassword()).isEqualTo("secret");
    }

    @Test
    public void should_provide_default_driver_according_to_dbVendor() {
        BonitaDatabaseConfiguration configuration = BonitaDatabaseConfiguration.builder()
                .dbVendor("mysql")
                .url("jdbc::localhost")
                .user("myUser")
                .build();

        BasicDataSource dataSource = bonitaDataSourceInitializer.createDataSource(configuration);

        assertThat(dataSource.getUrl()).isEqualTo("jdbc::localhost");
        assertThat(dataSource.getDriverClassName()).isEqualTo("com.mysql.cj.jdbc.Driver");
    }

    @Test
    public void should_set_pool_size_according_to_configuration() throws Exception {
        BonitaDatabaseConfiguration configuration = BonitaDatabaseConfiguration.builder()
                .dbVendor("mysql")
                .url("jdbc::localhost")
                .user("myUser")
                .xaDatasource(DatasourceConfiguration.builder().maxPoolSize(10).build())
                .datasource(DatasourceConfiguration.builder().maxPoolSize(5).build())
                .build();

        BasicDataSource dataSource = bonitaDataSourceInitializer.createDataSource(configuration);
        BasicDataSource managedDataSource = bonitaDataSourceInitializer.createManagedDataSource(configuration, null);

        assertThat(dataSource.getMaxTotal()).isEqualTo(5);
        assertThat(managedDataSource.getMaxTotal()).isEqualTo(10);
    }

}
