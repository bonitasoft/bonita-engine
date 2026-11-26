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

import org.bonitasoft.engine.BonitaDatabaseConfiguration;
import org.bonitasoft.engine.BonitaEngine;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class BonitaEngineAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BonitaEngineCommonAutoConfiguration.class,
                    BonitaEngineServerAutoConfiguration.class));

    @Test
    public void should_configure_database_using_properties() {
        this.contextRunner
                .withPropertyValues(
                        "org.bonitasoft.engine.database.bonita.db-vendor=postgres",
                        "org.bonitasoft.engine.database.bonita.url=myServerUrl",
                        "org.bonitasoft.engine.database.bonita.driver-class-name=my.Driver",
                        "org.bonitasoft.engine.database.bonita.user=myUser",
                        "org.bonitasoft.engine.database.bonita.password=secret",
                        "org.bonitasoft.engine.database.bonita.datasource.max-pool-size=3",
                        "org.bonitasoft.engine.database.bonita.xa-datasource.max-pool-size=4",
                        "org.bonitasoft.engine.database.business-data.datasource.max-pool-size=5",
                        "org.bonitasoft.engine.database.business-data.xa-datasource.max-pool-size=6",
                        "org.bonitasoft.engine.database.business-data.db-vendor=mysql")
                .run((context) -> {
                    BonitaEngine engine = context.getBean(BonitaEngine.class);
                    BonitaDatabaseConfiguration bonitaDatabaseConfiguration = engine.getBonitaDatabaseConfiguration();
                    assertThat(bonitaDatabaseConfiguration.getDbVendor()).isEqualTo("postgres");
                    assertThat(bonitaDatabaseConfiguration.getUrl()).isEqualTo("myServerUrl");
                    assertThat(bonitaDatabaseConfiguration.getDriverClassName()).isEqualTo("my.Driver");
                    assertThat(bonitaDatabaseConfiguration.getUser()).isEqualTo("myUser");
                    assertThat(bonitaDatabaseConfiguration.getPassword()).isEqualTo("secret");
                    assertThat(bonitaDatabaseConfiguration.getDatasource().getMaxPoolSize()).isEqualTo(3);
                    assertThat(bonitaDatabaseConfiguration.getXaDatasource().getMaxPoolSize()).isEqualTo(4);
                    BonitaDatabaseConfiguration businessDataDatabaseConfiguration = engine
                            .getBusinessDataDatabaseConfiguration();
                    assertThat(businessDataDatabaseConfiguration.getDatasource().getMaxPoolSize()).isEqualTo(5);
                    assertThat(businessDataDatabaseConfiguration.getXaDatasource().getMaxPoolSize()).isEqualTo(6);
                    assertThat(
                            businessDataDatabaseConfiguration.getDbVendor())
                            .isEqualTo("mysql");
                });
    }

}
