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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.platform.configuration.ConfigurationService;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class BonitaHomeServerTest {

    public static final long TENANT_ID = 16543L;
    @InjectMocks
    BonitaHomeServer bonitaHomeServer;

    @Mock
    ConfigurationService configurationService;

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

    private List<BonitaConfiguration> confs(BonitaConfiguration... bonitaConfiguration) {
        return Arrays.asList(bonitaConfiguration);
    }

    private BonitaConfiguration conf(String file1, byte[] bytes) {
        return new BonitaConfiguration(file1, bytes);
    }

}
