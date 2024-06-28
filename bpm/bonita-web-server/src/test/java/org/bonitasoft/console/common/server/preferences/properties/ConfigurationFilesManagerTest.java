/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.preferences.properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.util.Properties;

import org.bonitasoft.console.common.server.utils.PlatformManagementUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationFilesManagerTest {

    @Mock
    private PlatformManagementUtils platformManagementUtils;
    @Spy
    private ConfigurationFilesManager configurationFilesManager;

    @Before
    public void before() throws Exception {
        doReturn(platformManagementUtils).when(configurationFilesManager).getPlatformManagementUtils();
    }

    @Test
    public void getAlsoCustomAndInternalPropertiesFromFilename_should_merge_custom_properties_if_exist() {
        // given:
        final Properties defaultProps = new Properties();
        defaultProps.put("defaultKey", "defaultValue");
        doReturn(defaultProps).when(configurationFilesManager).getTenantConfiguration("toto.properties");
        final Properties customProps = new Properties();
        customProps.put("customKey", "customValue");
        doReturn(customProps).when(configurationFilesManager).getTenantConfiguration("toto-custom.properties");

        // when:
        final Properties properties = configurationFilesManager
                .getAlsoCustomAndInternalPropertiesFromFilename("toto.properties");

        // then:
        assertThat(properties)
                .containsEntry("defaultKey", "defaultValue")
                .containsEntry("customKey", "customValue");
    }

    @Test
    public void getAlsoCustomAndInternalPropertiesFromFilename_should_merge_internal_properties_if_exist() {
        // given:
        final Properties defaultProps = new Properties();
        defaultProps.put("defaultKey", "defaultValue");
        doReturn(defaultProps).when(configurationFilesManager).getTenantConfiguration("toto.properties");
        final Properties internalProps = new Properties();
        internalProps.put("internalKey", "internalValue");
        doReturn(internalProps).when(configurationFilesManager).getTenantConfiguration("toto-internal.properties");

        // when:
        final Properties properties = configurationFilesManager
                .getAlsoCustomAndInternalPropertiesFromFilename("toto.properties");

        // then:
        assertThat(properties)
                .containsEntry("defaultKey", "defaultValue")
                .containsEntry("internalKey", "internalValue");
    }

    @Test
    public void getAlsoCustomAndInternalPropertiesFromFilename_should_not_fail_if_base_file_does_not_exist() {
        // when:
        final Properties properties = configurationFilesManager
                .getAlsoCustomAndInternalPropertiesFromFilename("non-existing.properties");

        // then:
        assertThat(properties).isEmpty();
    }

    @Test
    public void custom_properties_should_overwrite_internal_properties() {
        // given:
        final Properties defaultProps = new Properties();
        defaultProps.put("defaultKey", "defaultValue");
        doReturn(defaultProps).when(configurationFilesManager).getTenantConfiguration("overwrite.properties");

        final Properties internalProps = new Properties();
        internalProps.put("otherKey", "someInternallyManagedValue");
        doReturn(internalProps).when(configurationFilesManager).getTenantConfiguration("overwrite-internal.properties");

        final Properties customProps = new Properties();
        final String expectedOverwrittenValue = "custom_changed_value";
        customProps.put("otherKey", expectedOverwrittenValue);
        doReturn(customProps).when(configurationFilesManager).getTenantConfiguration("overwrite-custom.properties");

        // when:
        final Properties properties = configurationFilesManager
                .getAlsoCustomAndInternalPropertiesFromFilename("overwrite.properties");

        // then:
        assertThat(properties).containsEntry("defaultKey", "defaultValue").containsEntry("otherKey",
                expectedOverwrittenValue);
    }
}
