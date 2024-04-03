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
package org.bonitasoft.engine.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.bonitasoft.engine.authorization.properties.CompoundPermissionsMapping;
import org.bonitasoft.engine.authorization.properties.CustomPermissionsMapping;
import org.bonitasoft.engine.authorization.properties.DynamicPermissionsChecks;
import org.bonitasoft.engine.authorization.properties.ResourcesPermissionsMapping;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { PermissionServiceConfigurationTest.TestConfiguration.class })
@TestPropertySource(properties = {
        "tenantId=1",
        "bonita.runtime.authorization.dynamic-check.enabled=false" })
public class PermissionServiceConfigurationTest {

    @Autowired
    protected PermissionServiceImpl permissionService;

    @Test
    public void should_dynamic_check_property_use_specific_property_value_if_overwritten() {
        assertThat(permissionService.dynamicPermissionCheck.isEnabled()).isFalse();
    }

    @ComponentScan({
            "org.bonitasoft.engine.authorization" // for Spring to find PermissionService
    })
    @Configuration
    public static class TestConfiguration {

        @Bean
        ClassLoaderService classLoaderService() {
            return mock(ClassLoaderService.class);
        }

        @Bean
        SessionAccessor sessionAccessor() {
            return mock(SessionAccessor.class);
        }

        @Bean
        SessionService sessionService() {
            return mock(SessionService.class);
        }

        @Bean
        PermissionsBuilder permissionsBuilder() {
            return mock(PermissionsBuilder.class);
        }

        @Bean
        CompoundPermissionsMapping compoundPermissionsMapping() {
            return mock(CompoundPermissionsMapping.class);
        }

        @Bean
        ResourcesPermissionsMapping resourcesPermissionsMapping() {
            return mock(ResourcesPermissionsMapping.class);
        }

        @Bean
        CustomPermissionsMapping customPermissionsMapping() {
            return mock(CustomPermissionsMapping.class);
        }

        @Bean
        DynamicPermissionsChecks dynamicPermissionsChecks() {
            return mock(DynamicPermissionsChecks.class);
        }
    }
}
