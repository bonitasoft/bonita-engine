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
        final File expectedFolder = setupFolder.resolve(PLATFORM_CONF_FOLDER_NAME).resolve("sql").resolve("h2").toFile();
        assertThat(expectedFolder).exists().isDirectory();
        assertThat(expectedFolder.listFiles()).extracting("name").hasSize(10).containsOnly(ALL_SQL_FILES);
    }

    @Test
    public void should_build_initial_folder() throws Exception {
        //given
        ConfigurationFolderUtil configurationFolderUtil = new ConfigurationFolderUtil();
        Path setupFolder = temporaryFolder.newFolder().toPath();

        //when
        configurationFolderUtil.buildInitialFolder(setupFolder);

        //then
        final File expectedFolder = setupFolder.resolve(PLATFORM_CONF_FOLDER_NAME).resolve("initial").resolve("platform_init_engine").toFile();
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
        final File expectedFolder = setupFolder.resolve(PLATFORM_CONF_FOLDER_NAME).resolve("current").resolve("platform_init_engine").toFile();
        assertThat(expectedFolder).exists().isDirectory();
        assertThat(expectedFolder.listFiles()).extracting("name").hasSize(1).containsOnly("currentConfig.properties");
    }
}
