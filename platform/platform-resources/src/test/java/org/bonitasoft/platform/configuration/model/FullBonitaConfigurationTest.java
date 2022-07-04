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
package org.bonitasoft.platform.configuration.model;

import static org.bonitasoft.platform.configuration.type.ConfigurationType.*;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.bonitasoft.platform.configuration.type.ConfigurationType;
import org.junit.Test;

/**
 * @author Laurent Leseigneur
 */
public class FullBonitaConfigurationTest {

    @Test
    public void should_have_readable_toString() {
        //given
        FullBonitaConfiguration fullBonitaConfiguration = new FullBonitaConfiguration("resourceName",
                "content".getBytes(), "type", 147L);

        //then
        Assertions
                .assertThat(fullBonitaConfiguration.toString())
                .isEqualTo(
                        "FullBonitaConfiguration{ resourceName='resourceName' , configurationType='type' , tenantId=147 }");
    }

    @Test
    public void should_be_a_licence_file() {
        //given
        FullBonitaConfiguration fullBonitaConfiguration = new FullBonitaConfiguration("resourceName",
                "content".getBytes(), LICENSES.name(), 147L);

        //then
        Assertions.assertThat(fullBonitaConfiguration.isLicenseFile()).isTrue();
    }

    @Test
    public void should_not_be_a_licence_file() {
        //given
        final List<ConfigurationType> allExceptLicense = Arrays.asList(PLATFORM_PORTAL, PLATFORM_ENGINE, TENANT_PORTAL,
                TENANT_ENGINE, TENANT_TEMPLATE_ENGINE, TENANT_SECURITY_SCRIPTS, TENANT_TEMPLATE_SECURITY_SCRIPTS,
                TENANT_TEMPLATE_PORTAL);

        for (ConfigurationType configurationType : allExceptLicense) {

            //when
            FullBonitaConfiguration fullBonitaConfiguration = new FullBonitaConfiguration("resourceName",
                    "content".getBytes(), configurationType.name(), 147L);

            //then
            Assertions.assertThat(fullBonitaConfiguration.isLicenseFile()).isFalse();

        }

    }

    @Test
    public void should_be_a_tenant_file() {
        //given
        FullBonitaConfiguration fullBonitaConfiguration = new FullBonitaConfiguration("resourceName",
                "content".getBytes(), "type", 147L);

        //then
        Assertions.assertThat(fullBonitaConfiguration.isTenantFile()).isTrue();
    }

    @Test
    public void should_not_be_a_tenant_file() {
        //given
        FullBonitaConfiguration fullBonitaConfiguration = new FullBonitaConfiguration("resourceName",
                "content".getBytes(), "type", 0L);

        //then
        Assertions.assertThat(fullBonitaConfiguration.isTenantFile()).isFalse();
    }
}
