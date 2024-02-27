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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.authorization.properties.ConfigurationFilesManager.getProperties;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.cache.CacheService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResourcesPermissionsMappingTest {

    @Mock
    private CacheService cacheService;

    @Mock
    private ConfigurationFilesManager configurationFilesManager;

    @Test
    public void testGetResourcePermission() {
        //given
        final String fileContent = "GET|bpm/process [Process visualization, Process categories, Process actor mapping visualization, Connector visualization]\n"
                + "POST|bpm/process [Process Deploy]\n" +
                "POST|bpm/process/6 [Custom permission]\n" +
                "PUT|bpm/process []";
        final ResourcesPermissionsMapping resourcesPermissionsMapping = getResourcesPermissionsMapping(fileContent);

        //when
        final Set<String> getPermissions = resourcesPermissionsMapping.getResourcePermissions("GET", "bpm", "process");
        final Set<String> postPermission = resourcesPermissionsMapping.getResourcePermissions("POST", "bpm", "process");
        final Set<String> postOnSinglePermission = resourcesPermissionsMapping.getResourcePermissions("POST", "bpm",
                "process", singletonList("6"));
        final Set<String> putPermissions = resourcesPermissionsMapping.getResourcePermissions("PUT", "bpm", "process");
        final Set<String> unknown = resourcesPermissionsMapping.getResourcePermissions("unknown", "unknown", "unknown",
                singletonList("unknown"));

        //then
        assertThat(getPermissions).containsOnly("Process visualization", "Process categories",
                "Process actor mapping visualization",
                "Connector visualization");
        assertThat(postPermission).containsOnly("Process Deploy");
        assertThat(postOnSinglePermission).containsOnly("Custom permission");
        assertThat(putPermissions).isEmpty();
        assertThat(unknown).isEmpty();
    }

    @Test
    public void testGetResourcePermissionWithWildCard() {
        //given
        final String fileContent = "POST|bpm/process/* [Process Deploy]\n" +
                "POST|bpm/process/*/instantiation [Custom permission]\n" +
                "PUT|bpm/process/*/expression [Expression update]";

        final ResourcesPermissionsMapping resourcesPermissionsMapping = getResourcesPermissionsMapping(fileContent);

        //when
        final Set<String> getWithResourcesQualifier = resourcesPermissionsMapping.getResourcePermissionsWithWildCard(
                "GET", "bpm", "process", List.of("6"));
        final Set<String> postWithResourcesQualifier = resourcesPermissionsMapping.getResourcePermissionsWithWildCard(
                "POST", "bpm", "process", List.of("6"));
        final Set<String> postWithResourcesQualifiers = resourcesPermissionsMapping.getResourcePermissionsWithWildCard(
                "POST", "bpm", "process", List.of("6", "instantiation"));
        final Set<String> putWithResourcesQualifiers = resourcesPermissionsMapping.getResourcePermissionsWithWildCard(
                "PUT", "bpm", "process", List.of("6", "expression", "10"));

        //then
        assertThat(getWithResourcesQualifier).isEmpty();
        assertThat(postWithResourcesQualifier).containsOnly("Process Deploy");
        assertThat(postWithResourcesQualifiers).containsOnly("Custom permission");
        assertThat(putWithResourcesQualifiers).containsOnly("Expression update");
    }

    public ResourcesPermissionsMapping getResourcesPermissionsMapping(final String fileContent) {
        final ResourcesPermissionsMapping resourcesPermissionsMapping = spy(
                new ResourcesPermissionsMapping(423L, cacheService, configurationFilesManager));
        doReturn(getProperties(fileContent.getBytes())).when(resourcesPermissionsMapping).getProperties();
        return resourcesPermissionsMapping;
    }
}
