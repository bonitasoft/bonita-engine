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
package org.bonitasoft.platform.setup.command.configure;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;

/**
 * @author Emmanuel Duchastenier
 */
public class DatabaseConfigurationTest {

    @Rule
    public TestRule clean = new RestoreSystemProperties();

    @Test
    public void bonita_database_values_can_be_overridden_by_system_properties() throws Exception {
        // given:
        final Properties properties = new PropertyLoader().loadProperties();

        System.setProperty("db.vendor", "mysql");
        System.setProperty("db.server.name", "postgresServer");
        System.setProperty("db.server.port", "3333");
        System.setProperty("db.database.name", "bonita_database");
        System.setProperty("db.user", "root");
        System.setProperty("db.password", "secret");

        // when:
        final DatabaseConfiguration bonitaConfig = new DatabaseConfiguration("", properties, null);

        // then:
        assertThat(bonitaConfig.getDbVendor()).isEqualTo("mysql");
        assertThat(bonitaConfig.getServerName()).isEqualTo("postgresServer");
        assertThat(bonitaConfig.getServerPort()).isEqualTo("3333");
        assertThat(bonitaConfig.getDatabaseName()).isEqualTo("bonita_database");
        assertThat(bonitaConfig.getDatabaseUser()).isEqualTo("root");
        assertThat(bonitaConfig.getDatabasePassword()).isEqualTo("secret");
    }

    @Test
    public void bdm_database_values_can_be_overridden_by_system_properties() throws Exception {
        // given:
        final Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("/internal.properties"));

        System.setProperty("bdm.db.vendor", "postgres");
        System.setProperty("bdm.db.server.name", "myServer");
        System.setProperty("bdm.db.server.port", "1111");
        System.setProperty("bdm.db.database.name", "internal_database");
        System.setProperty("bdm.db.user", "_user_");
        System.setProperty("bdm.db.password", "_pwd_");

        // when:
        final DatabaseConfiguration bdmConfig = new DatabaseConfiguration("bdm.", properties, null);

        // then:
        assertThat(bdmConfig.getDbVendor()).isEqualTo("postgres");
        assertThat(bdmConfig.getServerName()).isEqualTo("myServer");
        assertThat(bdmConfig.getServerPort()).isEqualTo("1111");
        assertThat(bdmConfig.getDatabaseName()).isEqualTo("internal_database");
        assertThat(bdmConfig.getDatabaseUser()).isEqualTo("_user_");
        assertThat(bdmConfig.getDatabasePassword()).isEqualTo("_pwd_");
    }

    @Test
    public void database_values_should_be_trimmed() throws Exception {
        // given:
        final Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("/database_with_space_values.properties"));
        properties.load(this.getClass().getResourceAsStream("/internal.properties"));

        System.setProperty("db.server.name", "  localhost   ");
        System.setProperty("db.server.port", "   5135   ");

        // when:
        final DatabaseConfiguration dbConfig = new DatabaseConfiguration("", properties, null);
        final DatabaseConfiguration bdmDbConfig = new DatabaseConfiguration("bdm.", properties, null);

        // then:
        assertThat(dbConfig.getUrl()).isEqualTo("jdbc:postgresql://localhost:5135/bonita");
        assertThat(bdmDbConfig.getUrl()).isEqualTo("jdbc:oracle:thin:@//ora1.rd.lan:1521/ORCL_DATABASE");
    }

    @Test
    public void support_absolute_path_in_h2_database_dir() throws Exception {
        // given:
        Path rootPath = new File(".").toPath();
        String h2DatabaseDir = "/h2Database";
        Properties properties = new PropertyLoader().loadProperties();

        if (org.apache.commons.lang3.SystemUtils.IS_OS_WINDOWS) {
            h2DatabaseDir = "C:/h2Database";
        }

        System.setProperty("db.vendor", "h2");
        System.setProperty("h2.database.dir", h2DatabaseDir);

        // when:
        DatabaseConfiguration bonitaConfig = new DatabaseConfiguration("", properties, rootPath);

        // then:
        assertThat(bonitaConfig.getUrl())
                .isEqualTo("jdbc:h2:file:"
                        + h2DatabaseDir
                        + "/bonita;DB_CLOSE_ON_EXIT=FALSE;IGNORECASE=TRUE;AUTO_SERVER=TRUE;");
    }

    @Test
    public void support_properties_in_h2_database_dir() throws Exception {
        // given:
        Path rootPath = new File(".").toPath();
        String h2DatabaseDir = "${org.bonitasoft.h2.database.dir}";
        Properties properties = new PropertyLoader().loadProperties();

        System.setProperty("db.vendor", "h2");
        System.setProperty("h2.database.dir", h2DatabaseDir);

        // when:
        DatabaseConfiguration dbConfig = new DatabaseConfiguration("", properties, rootPath);

        // then:
        assertThat(dbConfig.getUrl())
                .isEqualTo("jdbc:h2:file:"
                        + h2DatabaseDir
                        + "/bonita;DB_CLOSE_ON_EXIT=FALSE;IGNORECASE=TRUE;AUTO_SERVER=TRUE;");
    }

    @Test
    public void convert_relative_path_to_absolute_path_in_h2_database_dir() throws Exception {
        // given:
        Path rootPath = new File(".").toPath();
        String h2DatabaseDir = "../h2Database";
        Properties properties = new PropertyLoader().loadProperties();

        System.setProperty("db.vendor", "h2");
        System.setProperty("h2.database.dir", h2DatabaseDir);

        // when:
        DatabaseConfiguration dbConfig = new DatabaseConfiguration("", properties, rootPath);

        // then:
        assertThat(dbConfig.getUrl())
                .isEqualTo("jdbc:h2:file:"
                        + rootPath.resolve("setup").resolve(h2DatabaseDir).toAbsolutePath().normalize().toString()
                                .replace("\\", "/")
                        + "/bonita;DB_CLOSE_ON_EXIT=FALSE;IGNORECASE=TRUE;AUTO_SERVER=TRUE;");
    }

}
