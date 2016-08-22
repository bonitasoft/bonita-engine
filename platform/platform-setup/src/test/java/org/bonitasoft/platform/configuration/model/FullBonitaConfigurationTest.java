package org.bonitasoft.platform.configuration.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.platform.configuration.type.ConfigurationType.*;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.platform.configuration.type.ConfigurationType;
import org.junit.Test;

/**
 * @author Laurent Leseigneur
 */
public class FullBonitaConfigurationTest {

    @Test
    public void should_have_readable_toString() throws Exception {
        //given
        FullBonitaConfiguration fullBonitaConfiguration = new FullBonitaConfiguration("resourceName", "content".getBytes(), "type", 147L);

        //then
        assertThat(fullBonitaConfiguration.toString())
                .isEqualTo("FullBonitaConfiguration{ resourceName='resourceName' , configurationType='type' , tenantId=147 }");
    }

    @Test
    public void should_be_a_licence_file() throws Exception {
        //given
        FullBonitaConfiguration fullBonitaConfiguration = new FullBonitaConfiguration("resourceName", "content".getBytes(), LICENSES.name(), 147L);

        //then
        assertThat(fullBonitaConfiguration.isLicenseFile()).isTrue();
    }

    @Test
    public void should_not_be_a_licence_file() throws Exception {
        //given
        final List<ConfigurationType> allExceptLicense = Arrays.asList(PLATFORM_PORTAL, PLATFORM_INIT_ENGINE, PLATFORM_ENGINE, TENANT_PORTAL, TENANT_ENGINE,
                TENANT_TEMPLATE_ENGINE, TENANT_SECURITY_SCRIPTS, TENANT_TEMPLATE_SECURITY_SCRIPTS, TENANT_TEMPLATE_PORTAL);

        for (ConfigurationType configurationType : allExceptLicense) {

            //when
            FullBonitaConfiguration fullBonitaConfiguration = new FullBonitaConfiguration("resourceName", "content".getBytes(), configurationType.name(), 147L);

            //then
            assertThat(fullBonitaConfiguration.isLicenseFile()).isFalse();

        }

    }

    @Test
    public void should_be_a_tenant_file() throws Exception {
        //given
        FullBonitaConfiguration fullBonitaConfiguration = new FullBonitaConfiguration("resourceName", "content".getBytes(), "type", 147L);

        //then
        assertThat(fullBonitaConfiguration.isTenantFile()).isTrue();
    }

    @Test
    public void should_not_be_a_tenant_file() throws Exception {
        //given
        FullBonitaConfiguration fullBonitaConfiguration = new FullBonitaConfiguration("resourceName", "content".getBytes(), "type", 0L);

        //then
        assertThat(fullBonitaConfiguration.isTenantFile()).isFalse();
    }
}
