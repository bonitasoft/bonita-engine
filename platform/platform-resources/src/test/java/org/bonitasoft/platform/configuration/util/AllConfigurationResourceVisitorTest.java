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
package org.bonitasoft.platform.configuration.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.platform.configuration.model.FullBonitaConfiguration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Laurent Leseigneur
 */
public class AllConfigurationResourceVisitorTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ConfigurationResourceVisitor.class);

    @Test
    public void should_store_files_from_configuration_folder() throws Exception {
        //given
        Path rootFolder = Paths.get(getClass().getResource("/allConfiguration").toURI());
        LOGGER.info("folder:" + rootFolder);
        final List<FullBonitaConfiguration> bonitaConfigurations = new ArrayList<>();

        //when
        final AllConfigurationResourceVisitor resourceVisitor = new AllConfigurationResourceVisitor(
                bonitaConfigurations);
        Files.walkFileTree(rootFolder, resourceVisitor);

        //then
        assertThat(bonitaConfigurations).hasSize(8);
        assertThat(bonitaConfigurations).as("should contains tenant level configuration files")
                .extracting("tenantId")
                .contains(0L, 456L);
        assertThat(bonitaConfigurations).as("should visit all configuration folders")
                .extracting("configurationType")
                .containsOnly("TENANT_ENGINE", "TENANT_PORTAL", "TENANT_SECURITY_SCRIPTS",
                        "TENANT_TEMPLATE_SECURITY_SCRIPTS", "PLATFORM_PORTAL", "TENANT_TEMPLATE_ENGINE",
                        "PLATFORM_INIT_ENGINE", "PLATFORM_ENGINE");
        assertThat(bonitaConfigurations).as("should add all configuration files and skip licenses")
                .extracting("resourceName")
                .containsOnly("bonita-platform-init-custom.xml", "cache-config.xml",
                        "compound-permissions-mapping.properties",
                        "SamplePermissionRule.groovy.sample",
                        "bonita-tenant-community.properties",
                        "bonita-platform-community.properties");

    }
}
