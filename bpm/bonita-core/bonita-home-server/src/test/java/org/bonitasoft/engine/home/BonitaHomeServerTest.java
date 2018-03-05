/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.platform.configuration.ConfigurationService;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
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
    public void should_createTenant_copy_tenant_template_files() throws Exception {
        //given
        List<BonitaConfiguration> tenantTemplateConf = confs(conf("file1", "file1Content".getBytes()),
                conf("file2", "file2Content".getBytes()));
        doReturn(tenantTemplateConf)
                .when(configurationService).getTenantTemplateEngineConf();
        List<BonitaConfiguration> tenantTemplateScripts = confs(conf("org/bonitasoft/package/TrueScript.groovy", "return true".getBytes()),
                conf("org/bonitasoft/package/FalseScript.groovy", "return false".getBytes()));
        doReturn(tenantTemplateScripts)
                .when(configurationService).getTenantTemplateSecurityScripts();

        //when
        bonitaHomeServer.createTenant(12L);
        //then
        verify(configurationService).storeTenantEngineConf(tenantTemplateConf, 12L);
        verify(configurationService).storeTenantSecurityScripts(tenantTemplateScripts, 12L);

    }

    @Test
    public void should_updateTenantPortalConfigurationFile_update_the_files() throws Exception {
        //given
        List<BonitaConfiguration> tenantTemplateConf = confs(
                conf("myFile.properties", "previous content".getBytes()),
                conf("file2", "file2Content".getBytes()));
        doReturn(tenantTemplateConf)
                .when(configurationService).getTenantPortalConf(TENANT_ID);

        //when
        bonitaHomeServer.updateTenantPortalConfigurationFile(TENANT_ID, "myFile.properties", "the updated content".getBytes());
        //then
        verify(configurationService).storeTenantPortalConf(Collections.singletonList(conf("myFile.properties", "the updated content".getBytes())), TENANT_ID);
    }

    @Test(expected = UpdateException.class)
    public void should_updateTenantPortalConfigurationFile_throws_UpdateException_if_not_found() throws Exception {
        //given
        List<BonitaConfiguration> tenantTemplateConf = confs(
                conf("file2", "file2Content".getBytes()));
        doReturn(tenantTemplateConf)
                .when(configurationService).getTenantPortalConf(TENANT_ID);

        //when
        bonitaHomeServer.updateTenantPortalConfigurationFile(TENANT_ID, "myFile.properties", "the updated content".getBytes());
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
        doReturn(Collections.singletonList(new BonitaConfiguration("myProp.properties", out.toByteArray()))).when(configurationService).getPlatformEngineConf();
        Properties classPathProperties = new Properties();
        classPathProperties.setProperty("overriddenProperty", "classPathValue");
        classPathProperties.setProperty("classPathProperty", "aValueInClassPath");
        doReturn(classPathProperties).when(bonitaHomeServer).getPropertiesFromClassPath(Matchers.<String> anyVararg());
        //when
        Properties allProperties = bonitaHomeServer.getPlatformProperties();
        //then
        assertThat(allProperties).containsOnly(entry("overriddenProperty", "databaseValue"), entry("databaseProperty", "aValueInDb"),
                entry("classPathProperty", "aValueInClassPath"));
    }

    @Test
    public void should_getPropertiesFromClassPath_get_properties_of_all_files_from_classpath() throws Exception {
        //given
        File jar1 = temporaryFolder.newFile("myJar1.jar");
        FileUtils.writeByteArrayToFile(jar1, IOUtil.generateJar(Collections.singletonMap("myPropertiesFile1.properties", "prop1=value1".getBytes())));
        File jar2 = temporaryFolder.newFile("myJar2.jar");
        FileUtils.writeByteArrayToFile(jar2, IOUtil.generateJar(Collections.singletonMap("myPropertiesFile2.properties", "prop2=value2".getBytes())));
        File jar3 = temporaryFolder.newFile("myJar3.jar");
        FileUtils.writeByteArrayToFile(jar3, IOUtil.generateJar(Collections.singletonMap("myPropertiesFile3.properties", "prop3=value3".getBytes())));
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { jar1.toURI().toURL(), jar2.toURI().toURL(), jar3.toURI().toURL() }, contextClassLoader);
        try {
            Thread.currentThread().setContextClassLoader(urlClassLoader);
            //when
            Properties propertiesFromClassPath = bonitaHomeServer.getPropertiesFromClassPath("myPropertiesFile1.properties",
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
