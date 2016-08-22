/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.platform.setup;

import static org.bonitasoft.platform.setup.PlatformSetup.BONITA_SETUP_FOLDER;
import static org.mockito.Mockito.doReturn;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import javax.sql.DataSource;

import org.bonitasoft.platform.configuration.ConfigurationService;
import org.bonitasoft.platform.exception.PlatformException;
import org.bonitasoft.platform.util.ConfigurationFolderUtil;
import org.bonitasoft.platform.version.VersionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class PlatformSetupTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private DatabaseMetaData metaData;
    @Mock
    private ScriptExecutor scriptExecutor;
    @Mock
    private ConfigurationService configurationService;
    @Mock
    private VersionService versionService;
    @InjectMocks
    public PlatformSetup platformSetup;

    private ConfigurationFolderUtil configurationFolderUtil = new ConfigurationFolderUtil();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Rule
    public final TestRule restoreSystemProperties = new RestoreSystemProperties();

    @Before
    public void before() throws Exception {
        doReturn(connection).when(dataSource).getConnection();
        doReturn(metaData).when(connection).getMetaData();
    }

    @Test
    public void should_not_check_license_if_platform_already_init() throws Exception {
        final Path setupFolder = temporaryFolder.newFolder().toPath();
        System.setProperty(BONITA_SETUP_FOLDER, setupFolder.toString());
        final Path platform_conf = configurationFolderUtil.buildPlatformConfFolder(setupFolder);
        final Path licenseFolder = platform_conf.resolve("licenses");
        Files.createDirectories(licenseFolder);
        doReturn(true).when(scriptExecutor).isPlatformAlreadyCreated();

        //no exception
        platformSetup.init();
    }

    @Test
    public void should_fail_if_init_with_no_license() throws Exception {
        final Path setupFolder = temporaryFolder.newFolder().toPath();
        System.setProperty(BONITA_SETUP_FOLDER, setupFolder.toString());
        final Path platform_conf = configurationFolderUtil.buildPlatformConfFolder(setupFolder);
        final Path licenseFolder = platform_conf.resolve("licenses");
        Files.createDirectories(licenseFolder);

        expectedException.expect(PlatformException.class);
        expectedException.expectMessage("No license (.lic file) found");
        platformSetup.init();
    }
}
