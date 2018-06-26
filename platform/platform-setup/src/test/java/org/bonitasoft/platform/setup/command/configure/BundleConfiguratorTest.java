/**
 * Copyright (C) 2017 Bonitasoft S.A.
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.filefilter.RegexFileFilter;
import org.assertj.core.api.SoftAssertions;
import org.bonitasoft.platform.exception.PlatformException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BundleConfiguratorTest {

    private BundleConfigurator configurator = Mockito.spy(_BundleConfigurator.class);

    private abstract static class _BundleConfigurator extends BundleConfigurator {

        public _BundleConfigurator() throws PlatformException {
            super(Paths.get(""));
        }
    }

    @Test
    public void escapeWindowsBackslashesIfAny_should_double_backslashes() {
        // when:
        final String windowsValue = BundleConfigurator.convertWindowsBackslashes("C:\\Windows");

        // then:
        assertThat(windowsValue).isEqualTo("C:/Windows");
    }

    @Test
    public void xml_chars_in_URLs_should_be_escaped_before_replacing() {
        // given:
        String url = "jdbc:mysql://${bdm.db.server.name}:${bdm.db.server.port}/${bdm.db.database.name}" +
                "?dontTrackOpenResources=true&useUnicode=true&characterEncoding=UTF-8";

        // when:
        final String escapedXmlCharacters = BundleConfigurator.escapeXmlCharacters(url);

        // then:
        assertThat(escapedXmlCharacters)
                .isEqualTo("jdbc:mysql://${bdm.db.server.name}:${bdm.db.server.port}/${bdm.db.database.name}" +
                        "?dontTrackOpenResources=true&amp;useUnicode=true&amp;characterEncoding=UTF-8");
    }

    @Test
    public void getDriverFilter_should_detect_Oracle_drivers() {
        // when:
        final RegexFileFilter driverFilter = configurator.getDriverFilter("oracle");

        // then:
        assertThat(driverFilter.accept(new File("myFavoriteOjdbc1.4drivers.JAR"))).isTrue();
        assertThat(driverFilter.accept(new File("OraCLE-4.jar"))).isTrue();
        assertThat(driverFilter.accept(new File("OraCLE.zip"))).isTrue();
        assertThat(driverFilter.accept(new File("OJdbc-1.4.2.ZIP"))).isTrue();
    }

    @Test
    public void getDriverFilter_should_detect_Postgres_drivers() {
        // when:
        final RegexFileFilter driverFilter = configurator.getDriverFilter("postgres");

        // then:
        assertThat(driverFilter.accept(new File("postgres.JAR"))).isTrue();
        assertThat(driverFilter.accept(new File("POSTGRESsql-5.jar"))).isTrue();
        assertThat(driverFilter.accept(new File("drivers_postgres.LAST.zip"))).isTrue();
    }

    @Test
    public void getDriverFilter_should_detect_SQLSERVER_drivers() {
        // when:
        final RegexFileFilter driverFilter = configurator.getDriverFilter("sqlserver");

        // then:
        assertThat(driverFilter.accept(new File("sqlserver.JAR"))).isTrue();
        assertThat(driverFilter.accept(new File("SQLSERVER-5.jar"))).isTrue();
        assertThat(driverFilter.accept(new File("drivers_SQLServer.zip"))).isTrue();
        assertThat(driverFilter.accept(new File("old-sqljdbc.jar"))).isTrue();
        assertThat(driverFilter.accept(new File("sqljdbc.jar"))).isTrue();
        assertThat(driverFilter.accept(new File("sqljdbc4.jar"))).isTrue();
        assertThat(driverFilter.accept(new File("sqljdbc41.jar"))).isTrue();
        assertThat(driverFilter.accept(new File("sqljdbc42.jar"))).isTrue();
        assertThat(driverFilter.accept(new File("mssql-jdbc-6.2.1.jre8.jar"))).isTrue();
    }

    @Test
    public void getDriverFilter_should_detect_MySQL_drivers() {
        // when:
        final RegexFileFilter driverFilter = configurator.getDriverFilter("mysql");

        // then:
        assertThat(driverFilter.accept(new File("MySQL.JAR"))).isTrue();
        assertThat(driverFilter.accept(new File("mySQL-5.jar"))).isTrue();
        assertThat(driverFilter.accept(new File("drivers_mysql.zIp"))).isTrue();
    }

    @Test
    public void getDriverFilter_should_detect_H2_drivers() {
        // when:
        final RegexFileFilter driverFilter = configurator.getDriverFilter("h2");

        // then:
        assertThat(driverFilter.accept(new File("h2-1.4.JAR"))).isTrue();
        assertThat(driverFilter.accept(new File("drivers-H2.ZIP"))).isTrue();
        assertThat(driverFilter.accept(new File("my-custom-h2_package.jar"))).isTrue();
    }

    @Test
    public void should_escape_db_url_in_properties_for_H2() throws Exception {
        // given:
        DatabaseConfiguration dbConfig = mock(DatabaseConfiguration.class);
        when(dbConfig.getUrl()).thenReturn("/opt/bonità");
        when(dbConfig.getDbVendor()).thenReturn("h2");

        // when:
        String escapedURL = BundleConfigurator.getDatabaseConnectionUrlForPropertiesFile(dbConfig);

        // then:
        assertThat(escapedURL).isEqualTo("/opt/bonit\\\\u00E0");
    }

    // =================================================================================================================
    // UTILS
    // =================================================================================================================

    public static void checkFileContains(Path file, String... expectedTexts) throws IOException {
        final String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        assertThat(content).contains(expectedTexts);
    }

    public static void checkFileDoesNotContain(Path file, String... expectedTexts) throws IOException {
        final String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        SoftAssertions softly = new SoftAssertions();
        for (String text : expectedTexts) {
            softly.assertThat(content).doesNotContain(text);
        }
        softly.assertAll();
    }

}
