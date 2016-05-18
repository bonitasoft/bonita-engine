package org.bonitasoft.engine.home;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ClearSystemProperties;
import org.junit.rules.TemporaryFolder;

/**
 * @author Laurent Leseigneur
 */
public class TenantStorageTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public final ClearSystemProperties clearSystemProperties = new ClearSystemProperties("bonita.home");

    @Test
    public void should_return_profiles_md5_file() throws Exception {
        //given
        final File bonitaHome = temporaryFolder.newFolder("bonita-home");
        System.setProperty("bonita.home", bonitaHome.getAbsolutePath());
        TenantStorage tenantStorage = new TenantStorage(BonitaHomeServer.getInstance());

        //when
        Files.write(tenantStorage.getProfileMD5(5L).toPath(), "md5".getBytes());
        final File profileMD5 = tenantStorage.getProfileMD5(5L);

        //then
        assertThat(profileMD5).as("should retrieve file").exists().hasBinaryContent("md5".getBytes());

    }

}
