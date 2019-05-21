package org.bonitasoft.engine;


import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.dbcp2.BasicDataSource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BonitaDataSourceInitializerTest {

    @Rule
    public ExpectedException expectedExceptions = ExpectedException.none();
    private BonitaDataSourceInitializer bonitaDataSourceInitializer = new BonitaDataSourceInitializer();

    @Test
    public void should_fail_to_create_datasource_if_some_field_are_not_set(){
        BonitaDatabaseConfiguration configuration = BonitaDatabaseConfiguration.builder()
                .dbVendor("postgres")
                .url("jdbc::postgres")
                .build();

        expectedExceptions.expect(IllegalArgumentException.class);
        expectedExceptions.expectMessage("Database user not se");

        bonitaDataSourceInitializer.createDataSource(configuration);
    }

    @Test
    public void should_fail_to_create_datasource_configuration_is_empty(){
        expectedExceptions.expect(IllegalArgumentException.class);
        expectedExceptions.expectMessage("Database dbVendor not set");

        bonitaDataSourceInitializer.createDataSource(new BonitaDatabaseConfiguration());
    }



    @Test
    public void should_fail_if_db_vendor_is_not_supported(){
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


}