/**
 * Copyright (C) 2021 Bonitasoft S.A.
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
package org.bonitasoft.engine.authorization.properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.authorization.properties.ConfigurationFile.CONFIGURATION_FILES_CACHE;
import static org.bonitasoft.engine.authorization.properties.ConfigurationFilesManager.getProperties;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Properties;
import java.util.Set;

import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationFileTest {

    public static final long TENANT_ID = 12L;
    private static Properties compoundProperties;
    private static Properties resourcesProperties;
    private static Properties customProperties;

    private ConfigurationFile resourcesPermissionsMapping;
    private ConfigurationFile customPermissionsMapping;
    private ConfigurationFile compoundPermissionsMapping;

    @Mock
    private CacheService cacheService;

    @Mock
    private ConfigurationFilesManager configurationFilesManager;

    @BeforeClass
    public static void init() throws Exception {
        compoundProperties = getProperties(IOUtil.getAllContentFrom(
                ConfigurationFileTest.class.getResourceAsStream("/compound-permissions-mapping.properties")));
        resourcesProperties = getProperties(IOUtil.getAllContentFrom(
                ConfigurationFileTest.class.getResourceAsStream("/resources-permissions-mapping.properties")));
        customProperties = getProperties(IOUtil.getAllContentFrom(
                ConfigurationFileTest.class.getResourceAsStream("/custom-permissions-mapping.properties")));
    }

    @Before
    public void setupMocksAndSpies() {
        resourcesPermissionsMapping = spy(
                new ResourcesPermissionsMapping(TENANT_ID, cacheService, configurationFilesManager));
        doReturn(resourcesProperties).when(resourcesPermissionsMapping).getTenantProperties();

        customPermissionsMapping = spy(
                new CustomPermissionsMapping(TENANT_ID, cacheService, configurationFilesManager));
        doReturn(customProperties).when(customPermissionsMapping).getTenantProperties();

        compoundPermissionsMapping = spy(
                new CompoundPermissionsMapping(TENANT_ID, cacheService, configurationFilesManager));
        doReturn(compoundProperties).when(compoundPermissionsMapping).getTenantProperties();
    }

    @Test
    public void should_getProperty_return_the_right_custom_permissions_with_special_characters() {
        final String customValue = customPermissionsMapping.getProperty("profile|HR manager");
        assertEquals("[ManageProfiles]", customValue);
    }

    @Test
    public void should_getPropertyAsSet_return_the_right_permissions_with_trailing_spaces() {
        final String value = compoundPermissionsMapping.getProperty("caseListingPage");
        final Set<String> valueAsList = compoundPermissionsMapping.getPropertyAsSet("caseListingPage");

        assertEquals("caseVisualizationWithTrailingSpace", value);
        assertThat(valueAsList).containsOnly("caseVisualizationWithTrailingSpace");
    }

    @Test
    public void should_getProperty_return_null_with_unknown_permissions() {
        final String value = compoundPermissionsMapping.getProperty("unknownListingPage");
        assertThat(value).isNull();
    }

    @Test
    public void should_getProperty_return_the_right_compound_permissions() {
        final String compoundValue = compoundPermissionsMapping.getProperty("taskListingPage");
        assertEquals("[TaskVisualization, CaseVisualization]", compoundValue);
    }

    @Test
    public void should_getProperty_return_the_right_resource_permissions() {
        final String resourcesValue = resourcesPermissionsMapping.getProperty("GET|bpm/identity");
        assertEquals("[UserVisualization, groupVisualization]", resourcesValue);
    }

    @Test
    public void should_getProperty_return_the_right_custom_permissions() {
        final String customValue = customPermissionsMapping.getProperty("profile|User");
        assertEquals("[ManageLooknFeel, ManageProfiles]", customValue);
    }

    @Test
    public void should_getPropertyAsSet_return_the_right_permissions_list() {
        final Set<String> compoundPermissionsList = compoundPermissionsMapping.getPropertyAsSet("taskListingPage");
        assertThat(compoundPermissionsList).containsOnly("TaskVisualization", "CaseVisualization");
    }

    @Test
    public void should_getPropertyAsSet_return_the_right_permissions_list_with_single_value() {
        final Set<String> compoundPermissionsList = compoundPermissionsMapping.getPropertyAsSet("processListingPage");
        assertThat(compoundPermissionsList).containsOnly("processVisualization");
    }

    @Test
    public void getTenantProperties_should_get_from_cache_and_store_to_cache_if_not_already_in() throws Exception {
        // given:
        final ResourcesPermissionsMapping configFile = spy(
                new ResourcesPermissionsMapping(TENANT_ID, cacheService, configurationFilesManager));
        final Properties props = new Properties();
        doReturn(props).when(configurationFilesManager).getTenantProperties("resources-permissions-mapping.properties",
                TENANT_ID);

        // when:
        configFile.getTenantProperties();

        // then:
        verify(cacheService).get(CONFIGURATION_FILES_CACHE, TENANT_ID + "_resources-permissions-mapping.properties");
        verify(cacheService).store(CONFIGURATION_FILES_CACHE, TENANT_ID + "_resources-permissions-mapping.properties",
                props);
    }
}
