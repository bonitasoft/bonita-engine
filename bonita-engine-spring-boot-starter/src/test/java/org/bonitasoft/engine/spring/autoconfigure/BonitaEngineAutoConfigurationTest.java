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
package org.bonitasoft.engine.spring.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.test.BonitaDatabaseConfiguration;
import org.bonitasoft.engine.test.TestEngineImpl;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class BonitaEngineAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BonitaEngineCommonAutoConfiguration.class,BonitaEngineServerAutoConfiguration.class));

    @Test
    public void should_configure_database_using_properties() {
        this.contextRunner
                .withPropertyValues(
                        "org.bonitasoft.engine.database.bonita.db-vendor=postgres",
                        "org.bonitasoft.engine.database.bonita.server=myServer",
                        "org.bonitasoft.engine.database.bonita.port=1289",
                        "org.bonitasoft.engine.database.bonita.database-name=mySchema",
                        "org.bonitasoft.engine.database.bonita.user=myUser",
                        "org.bonitasoft.engine.database.bonita.password=secret",
                        "org.bonitasoft.engine.database.business-data.db-vendor=mysql")
                .run((context) -> {
                    TestEngineImpl testEngine = context.getBean(TestEngineImpl.class);
                    BonitaDatabaseConfiguration bonitaDatabaseConfiguration = testEngine.getBonitaDatabaseConfiguration();
                    assertThat(bonitaDatabaseConfiguration.getDbVendor()).isEqualTo("postgres");
                    assertThat(bonitaDatabaseConfiguration.getServer()).isEqualTo("myServer");
                    assertThat(bonitaDatabaseConfiguration.getPort()).isEqualTo("1289");
                    assertThat(bonitaDatabaseConfiguration.getDatabaseName()).isEqualTo("mySchema");
                    assertThat(bonitaDatabaseConfiguration.getUser()).isEqualTo("myUser");
                    assertThat(bonitaDatabaseConfiguration.getPassword()).isEqualTo("secret");
                    assertThat(
                            testEngine.getBusinessDataDatabase().getDbVendor())
                            .isEqualTo("mysql");
                });
    }

}