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

import org.bonitasoft.engine.api.impl.resolver.BusinessArchiveArtifactsManager;
import org.bonitasoft.engine.business.data.BusinessDataModelRepository;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.tenant.TenantStateManager;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Emmanuel Duchastenier
 */
public class CustomApplicationInstallerTest {

    @RunWith(SpringRunner.class)
    @ContextConfiguration(classes = CustomApplicationInstallerConfigurationTest.TextConfiguration.class)
    abstract static class CustomApplicationInstallerConfigurationTest {

        @Autowired
        protected CustomOrDefaultApplicationInstaller listener;

        @Configuration
        static class TextConfiguration {

            @Bean
            public CustomOrDefaultApplicationInstaller listener() {
                return new CustomOrDefaultApplicationInstaller(new ApplicationInstaller(
                        mock(BusinessDataModelRepository.class),
                        mock(TransactionService.class), 1L, mock(SessionAccessor.class), mock(SessionService.class),
                        mock(TenantStateManager.class), mock(BusinessArchiveArtifactsManager.class),
                        mock(ApplicationArchiveReader.class)));
            }
        }
    }

    public static class DefaultCustomOrDefaultApplicationInstallerTest
            extends CustomApplicationInstallerConfigurationTest {

        @Test
        public void should_application_install_folder_have_default_value() {
            assertThat(listener.getApplicationInstallFolder()).isEqualTo("my-application");
        }
    }

    @TestPropertySource(properties = {
            "bonita.runtime.custom-application.install-folder=my-carpeta-personalizada",
    })
    public static class OverwrittenCustomOrDefaultApplicationInstallerTest
            extends CustomApplicationInstallerConfigurationTest {

        @Test
        public void should_support_application_install_folder_overwrite() {
            assertThat(listener.getApplicationInstallFolder()).isEqualTo("my-carpeta-personalizada");
        }
    }
}
