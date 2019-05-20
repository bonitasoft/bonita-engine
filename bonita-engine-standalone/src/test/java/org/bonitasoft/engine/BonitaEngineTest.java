package org.bonitasoft.engine;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Ignore;
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
        BonitaDatabaseConfiguration database = new BonitaDatabaseConfiguration();
        database.setDbVendor("postgres");
        bonitaEngine.setBonitaDatabaseConfiguration(database);

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

        assertThat(bonitaEngine.getBonitaDataSource().getUrl()).contains("h2");
        assertThat(bonitaEngine.getBonitaDataSource().getDriverClassName()).containsIgnoringCase("h2");

    }

    @Ignore("Should provide default values related to dbvendor")
    @Test
    public void should_configure_bonita_datasource_on_postgres() throws Exception {
        BonitaEngine bonitaEngine = newBonitaEngine();
        BonitaDatabaseConfiguration database = new BonitaDatabaseConfiguration();
        database.setDbVendor("postgres");
        bonitaEngine.setBonitaDatabaseConfiguration(database);

        bonitaEngine.initializeEnvironment();

        assertThat(bonitaEngine.getBonitaDataSource().getUrl()).contains("postgres");
        assertThat(bonitaEngine.getBonitaDataSource().getDriverClassName()).containsIgnoringCase("postgres");

    }

    private BonitaEngine newBonitaEngine() {
        return new BonitaEngine();
    }

}