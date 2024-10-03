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
package org.bonitasoft.engine.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.platform.configuration.ConfigurationService;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class BonitaHomeServerTest {

    public static final long TENANT_ID = 16543L;
    @InjectMocks
    @Spy
    BonitaHomeServer bonitaHomeServer;
    @Mock
    ConfigurationService configurationService;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_updateTenantPortalConfigurationFile_update_the_files() throws Exception {
        //given
        doReturn(conf("myFile.properties", "previous content".getBytes()))
                .when(configurationService).getTenantPortalConfiguration(TENANT_ID, "myFile.properties");

        //when
        bonitaHomeServer.updateTenantPortalConfigurationFile(TENANT_ID, "myFile.properties",
                "the updated content".getBytes());
        //then
        verify(configurationService).storeTenantPortalConf(
                Collections.singletonList(conf("myFile.properties", "the updated content".getBytes())), TENANT_ID);
    }

    @Test(expected = UpdateException.class)
    public void should_updateTenantPortalConfigurationFile_throws_UpdateException_if_not_found() throws Exception {
        //given
        doReturn(null).when(configurationService).getTenantPortalConfiguration(TENANT_ID, "myFile.properties");

        //when
        bonitaHomeServer.updateTenantPortalConfigurationFile(TENANT_ID, "myFile.properties",
                "the updated content".getBytes());
    }

    @Test
    public void should_delete_tenant_delete_configuration_files() throws Exception {
        //when
        bonitaHomeServer.deleteTenant(TENANT_ID);

        //then
        verify(configurationService).deleteTenantConfiguration(TENANT_ID);
    }

    private List<BonitaConfiguration> confs(BonitaConfiguration... bonitaConfiguration) {
        return Arrays.asList(bonitaConfiguration);
    }

    private BonitaConfiguration conf(String file1, byte[] bytes) {
        return new BonitaConfiguration(file1, bytes);
    }

    @Test
    public void should_context_have_properties_overriden_with_database_properties() throws Exception {
        //given
        Properties databaseProperties = new Properties();
        databaseProperties.setProperty("overriddenProperty", "databaseValue");
        databaseProperties.setProperty("databaseProperty", "aValueInDb");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        databaseProperties.store(out, "");
        doReturn(Collections.singletonList(new BonitaConfiguration("myProp.properties", out.toByteArray())))
                .when(configurationService).getPlatformEngineConf();
        Properties classPathProperties = new Properties();
        classPathProperties.setProperty("overriddenProperty", "classPathValue");
        classPathProperties.setProperty("classPathProperty", "aValueInClassPath");
        doReturn(classPathProperties).when(bonitaHomeServer)
                .getPropertiesFromClassPath(ArgumentMatchers.<String> any());
        //when
        Properties allProperties = bonitaHomeServer.getPlatformProperties();
        //then
        assertThat(allProperties).containsOnly(entry("overriddenProperty", "databaseValue"),
                entry("databaseProperty", "aValueInDb"),
                entry("classPathProperty", "aValueInClassPath"));
    }

    @Test
    public void tenant_properties_should_inherit_from_platform_properties() throws Exception {
        //given
        Thread.currentThread().setContextClassLoader(getClassLoaderWithProperties(
                new BonitaConfiguration("bonita-platform-community.properties",
                        getPropertiesAsByteArray("prop1=prop1PlatformCP", "prop2=prop2PlatformCP",
                                "prop3=prop3PlatformCP", "prop4=prop4PlatformCP")),
                new BonitaConfiguration("bonita-tenant-community.properties",
                        getPropertiesAsByteArray("prop3=prop3TenantCP", "prop4=prop4TenantCP"))));

        doReturn(Collections.singletonList(new BonitaConfiguration("platform.properties",
                getPropertiesAsByteArray("prop2=prop2PlatformDB", "prop3=prop3PlatformDB", "prop4=prop4PlatformDB"))))
                .when(configurationService).getPlatformEngineConf();

        doReturn(Collections.singletonList(new BonitaConfiguration("tenant.properties",
                getPropertiesAsByteArray("prop4=prop4TenantDB"))))
                .when(configurationService).getTenantEngineConf(1);
        //when
        Properties allProperties = bonitaHomeServer.getTenantProperties(1);
        //then
        assertThat(allProperties).containsOnly(
                entry("prop1", "prop1PlatformCP"),
                entry("prop2", "prop2PlatformDB"),
                entry("prop3", "prop3TenantCP"),
                entry("prop4", "prop4TenantDB"),
                entry("tenantId", "1"));
    }

    private byte[] getPropertiesAsByteArray(String... propertiesV) {
        return String.join("\n", propertiesV).getBytes(StandardCharsets.UTF_8);
    }

    private Properties getPropertiesAsProp(String... propertiesV) throws IOException {
        Properties properties = new Properties();
        properties.load(new StringReader(String.join("\n", propertiesV)));
        return properties;
    }

    private ClassLoader getClassLoaderWithProperties(BonitaConfiguration... bonitaConfigurations) throws IOException {
        File jar1 = temporaryFolder.newFile("myJar1.jar");
        FileUtils.writeByteArrayToFile(jar1, IOUtil.generateJar(
                Arrays.stream(bonitaConfigurations).collect(Collectors.toMap(BonitaConfiguration::getResourceName,
                        BonitaConfiguration::getResourceContent))));
        return new URLClassLoader(new URL[] { jar1.toURI().toURL() }, Thread.currentThread().getContextClassLoader());
    }

    @Test
    public void should_getPropertiesFromClassPath_get_properties_of_all_files_from_classpath() throws Exception {
        //given
        File jar1 = temporaryFolder.newFile("myJar1.jar");
        FileUtils.writeByteArrayToFile(jar1, IOUtil
                .generateJar(Collections.singletonMap("myPropertiesFile1.properties", "prop1=value1".getBytes())));
        File jar2 = temporaryFolder.newFile("myJar2.jar");
        FileUtils.writeByteArrayToFile(jar2, IOUtil
                .generateJar(Collections.singletonMap("myPropertiesFile2.properties", "prop2=value2".getBytes())));
        File jar3 = temporaryFolder.newFile("myJar3.jar");
        FileUtils.writeByteArrayToFile(jar3, IOUtil
                .generateJar(Collections.singletonMap("myPropertiesFile3.properties", "prop3=value3".getBytes())));
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        URLClassLoader urlClassLoader = new URLClassLoader(
                new URL[] { jar1.toURI().toURL(), jar2.toURI().toURL(), jar3.toURI().toURL() }, contextClassLoader);
        try {
            Thread.currentThread().setContextClassLoader(urlClassLoader);
            //when
            Properties propertiesFromClassPath = bonitaHomeServer.getPropertiesFromClassPath(
                    "myPropertiesFile1.properties",
                    "myPropertiesFile2.properties", "anUnexistingProperty.properties");
            //then
            assertThat(propertiesFromClassPath).containsOnly(entry("prop1", "value1"), entry("prop2", "value2"));
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Test
    public void should_getTenantPortal_Configuration() throws Exception {
        //given
        final String configFile = "a portal config file";
        BonitaConfiguration tenantTemplateConf = conf(configFile, "{}".getBytes());
        doReturn(tenantTemplateConf).when(configurationService).getTenantPortalConfiguration(TENANT_ID, configFile);

        //when
        byte[] content = bonitaHomeServer.getTenantPortalConfiguration(TENANT_ID, configFile);

        //then
        verify(configurationService).getTenantPortalConfiguration(TENANT_ID, configFile);
        assertThat(content).isEqualTo("{}".getBytes());

    }
}
