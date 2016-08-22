package org.bonitasoft.platform.configuration.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;

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

        FullBonitaConfiguration fullBonitaConfiguration = new FullBonitaConfiguration("licence1.lic", "license content".getBytes(),
                ConfigurationType.LICENSES.name(), 0L);

        //then
        assertThat(folderResolver.getFolder(fullBonitaConfiguration)).isEqualTo(LicFolder.toFile());

    }

    @Test
    public void should_resolve_non_tenant_folder() throws Exception {
        //given
        Path confFolder = temporaryFolder.newFolder("configuration").toPath();
        Path LicFolder = temporaryFolder.newFolder("licenses").toPath();
        FolderResolver folderResolver = new FolderResolver(confFolder, LicFolder);

        FullBonitaConfiguration fullBonitaConfiguration = new FullBonitaConfiguration("conf.properties", "key=value".getBytes(),
                ConfigurationType.PLATFORM_ENGINE.name(), 0L);

        //then
        assertThat(folderResolver.getFolder(fullBonitaConfiguration)).isEqualTo(confFolder.resolve("platform_engine").toFile());

    }

    @Test
    public void should_resolve_tenant_folder() throws Exception {
        //given
        Path confFolder = temporaryFolder.newFolder("configuration").toPath();
        Path LicFolder = temporaryFolder.newFolder("licenses").toPath();
        FolderResolver folderResolver = new FolderResolver(confFolder, LicFolder);

        FullBonitaConfiguration fullBonitaConfiguration = new FullBonitaConfiguration("conf.properties", "key=value".getBytes(),
                ConfigurationType.TENANT_PORTAL.name(), 55L);

        //then
        assertThat(folderResolver.getFolder(fullBonitaConfiguration)).isEqualTo(confFolder.resolve("tenants").resolve("55").resolve("tenant_portal").toFile());

    }

}
