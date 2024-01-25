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
package org.bonitasoft.engine.service.impl.installation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;

public class ConfigurationArchiveTest {

    @Test
    public void should_load_process_configurations_from_archive() throws Exception {
        try (InputStream bConfStream = ConfigurationArchiveTest.class.getResourceAsStream("/test.bconf");
                ConfigurationArchive configurationArchive = new ConfigurationArchive(bConfStream.readAllBytes())) {
            assertThat(configurationArchive.getBuilderVersion()).isEqualTo("7.8.0-SNAPSHOT");
            assertThat(configurationArchive.getTargetEnvironment()).isEqualTo("Production");
            List<ProcessConfiguration> processConfigurations = configurationArchive.getProcessConfigurations();
            assertThat(processConfigurations)
                    .contains(new ProcessConfiguration("RequestLoan", "1.3.1"),
                            new ProcessConfiguration("LoanRequestBot", "1.3.1"));

            ProcessConfiguration processConfiguration = configurationArchive.getProcessConfigurations().stream()
                    .filter(new ProcessConfiguration("RequestLoan", "1.3.1")::equals).findFirst().orElse(null);
            assertThat(processConfiguration).isNotNull();
            assertThat(processConfiguration.getParameters()).contains(entry("botActivated", "true"),
                    entry("smtpPassword", "dfghj65789)IU"));
        }
    }

}
