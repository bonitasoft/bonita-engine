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
package org.bonitasoft.platform.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.platform.setup.PlatformSetup.PLATFORM_CONF_FOLDER_NAME;
import static org.bonitasoft.platform.util.ConfigurationFolderUtil.ALL_SQL_FILES;

import java.io.File;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Laurent Leseigneur
 */
public class ConfigurationFolderUtilTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_copy_all_sql_files() throws Exception {
        //given
        ConfigurationFolderUtil configurationFolderUtil = new ConfigurationFolderUtil();
        Path setupFolder = temporaryFolder.newFolder().toPath();

        //when
        configurationFolderUtil.buildSqlFolder(setupFolder, "h2");

        //then
        final File expectedFolder = setupFolder.resolve(PLATFORM_CONF_FOLDER_NAME).resolve("sql").resolve("h2")
                .toFile();
        assertThat(expectedFolder).exists().isDirectory();
        assertThat(expectedFolder.listFiles()).extracting("name").hasSize(8).containsOnly(ALL_SQL_FILES);
    }

    @Test
    public void should_build_initial_folder() throws Exception {
        //given
        ConfigurationFolderUtil configurationFolderUtil = new ConfigurationFolderUtil();
        Path setupFolder = temporaryFolder.newFolder().toPath();

        //when
        configurationFolderUtil.buildPlatformEngineFolder(setupFolder);

        //then
        final File expectedFolder = setupFolder.resolve(PLATFORM_CONF_FOLDER_NAME).resolve("initial")
                .resolve("platform_engine").toFile();
        assertThat(expectedFolder).exists().isDirectory();
        assertThat(expectedFolder.listFiles()).extracting("name").hasSize(1).containsOnly("initialConfig.properties");
    }

    @Test
    public void should_build_current_folder() throws Exception {
        //given
        ConfigurationFolderUtil configurationFolderUtil = new ConfigurationFolderUtil();
        Path setupFolder = temporaryFolder.newFolder().toPath();

        //when
        configurationFolderUtil.buildCurrentFolder(setupFolder);

        //then
        final File expectedFolder = setupFolder.resolve(PLATFORM_CONF_FOLDER_NAME).resolve("current")
                .resolve("platform_engine").toFile();
        assertThat(expectedFolder).exists().isDirectory();
        assertThat(expectedFolder.listFiles()).extracting("name").hasSize(1).containsOnly("currentConfig.properties");
    }
}
