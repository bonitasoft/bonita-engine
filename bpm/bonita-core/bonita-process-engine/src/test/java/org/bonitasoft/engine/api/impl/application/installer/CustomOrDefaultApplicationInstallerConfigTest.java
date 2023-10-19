/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl.application.installer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.business.application.importer.DefaultLivingApplicationImporter;
import org.bonitasoft.engine.business.application.importer.MandatoryLivingApplicationImporter;
import org.bonitasoft.engine.platform.PlatformService;
import org.bonitasoft.engine.tenant.TenantServicesManager;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(Enclosed.class)
public class CustomOrDefaultApplicationInstallerConfigTest {

    @RunWith(SpringRunner.class)
    @ContextConfiguration(classes = AbstractConfigTest.TextConfiguration.class)
    abstract static class AbstractConfigTest {

        @Autowired
        protected CustomOrDefaultApplicationInstaller installer;

        @Configuration
        static class TextConfiguration {

            @Bean
            public CustomOrDefaultApplicationInstaller installer() {
                return new CustomOrDefaultApplicationInstaller(mock(ApplicationInstaller.class),
                        mock(DefaultLivingApplicationImporter.class), mock(MandatoryLivingApplicationImporter.class),
                        mock(TenantServicesManager.class), mock(ApplicationArchiveReader.class),
                        mock(PlatformService.class));
            }
        }
    }

    public static class CustomOrDefaultApplicationInstallerDefaultConfigTest extends AbstractConfigTest {

        @Test
        public void should_application_install_folder_have_default_value() {
            assertThat(installer.getApplicationInstallFolder()).isEqualTo("my-application");
        }
    }

    @TestPropertySource(properties = {
            "bonita.runtime.custom-application.install-folder=my-carpeta-personalizada",
    })
    public static class CustomOrDefaultApplicationInstallerOverwrittenConfigTest extends AbstractConfigTest {

        @Test
        public void should_support_application_install_folder_overwrite() {
            assertThat(installer.getApplicationInstallFolder()).isEqualTo("my-carpeta-personalizada");
        }
    }
}
