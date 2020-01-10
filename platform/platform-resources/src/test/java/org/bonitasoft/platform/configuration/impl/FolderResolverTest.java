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
package org.bonitasoft.platform.configuration.impl;

import java.nio.file.Path;

import org.assertj.core.api.Assertions;
import org.bonitasoft.platform.configuration.model.FullBonitaConfiguration;
import org.bonitasoft.platform.configuration.type.ConfigurationType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Laurent Leseigneur
 */
public class FolderResolverTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_resolve_license_folder() throws Exception {
        //given
        Path confFolder = temporaryFolder.newFolder("configuration").toPath();
        Path LicFolder = temporaryFolder.newFolder("licenses").toPath();
        FolderResolver folderResolver = new FolderResolver(confFolder, LicFolder);

        FullBonitaConfiguration fullBonitaConfiguration = new FullBonitaConfiguration("licence1.lic",
                "license content".getBytes(),
                ConfigurationType.LICENSES.name(), 0L);

        //then
        Assertions.assertThat(folderResolver.getFolder(fullBonitaConfiguration)).isEqualTo(LicFolder.toFile());

    }

    @Test
    public void should_resolve_non_tenant_folder() throws Exception {
        //given
        Path confFolder = temporaryFolder.newFolder("configuration").toPath();
        Path LicFolder = temporaryFolder.newFolder("licenses").toPath();
        FolderResolver folderResolver = new FolderResolver(confFolder, LicFolder);

        FullBonitaConfiguration fullBonitaConfiguration = new FullBonitaConfiguration("conf.properties",
                "key=value".getBytes(),
                ConfigurationType.PLATFORM_ENGINE.name(), 0L);

        //then
        Assertions.assertThat(folderResolver.getFolder(fullBonitaConfiguration))
                .isEqualTo(confFolder.resolve("platform_engine").toFile());

    }

    @Test
    public void should_resolve_tenant_folder() throws Exception {
        //given
        Path confFolder = temporaryFolder.newFolder("configuration").toPath();
        Path LicFolder = temporaryFolder.newFolder("licenses").toPath();
        FolderResolver folderResolver = new FolderResolver(confFolder, LicFolder);

        FullBonitaConfiguration fullBonitaConfiguration = new FullBonitaConfiguration("conf.properties",
                "key=value".getBytes(),
                ConfigurationType.TENANT_PORTAL.name(), 55L);

        //then
        Assertions.assertThat(folderResolver.getFolder(fullBonitaConfiguration))
                .isEqualTo(confFolder.resolve("tenants").resolve("55").resolve("tenant_portal").toFile());

    }

}
