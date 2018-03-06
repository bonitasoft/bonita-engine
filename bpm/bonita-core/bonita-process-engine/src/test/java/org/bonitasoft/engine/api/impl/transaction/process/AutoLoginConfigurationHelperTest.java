/**
 * Copyright (C) 2016 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.api.impl.transaction.process.AutoLoginConfigurationHelper.AUTOLOGIN_V6_JSON;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Properties;

import org.bonitasoft.engine.core.process.definition.exception.SProcessDisablementException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessEnablementException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.platform.configuration.ConfigurationService;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class AutoLoginConfigurationHelperTest {

    public static final long TENANT_ID = 12L;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private ConfigurationService configurationService;

    private SProcessDefinition sProcessDefinition;

    @Captor
    private ArgumentCaptor<List<BonitaConfiguration>> argumentCaptor;

    @Test
    public void enableAutoLogin_should_init_autoLogin_configuration() throws Exception {
        //given
        sProcessDefinition = new SProcessDefinitionImpl("first Pool", "1.0");
        doReturn(new BonitaConfiguration(AUTOLOGIN_V6_JSON, "[]".getBytes())).when(configurationService).getTenantPortalConfiguration(TENANT_ID,
                AUTOLOGIN_V6_JSON);
        AutoLoginConfigurationHelper autoLoginConfigurationHelper = new AutoLoginConfigurationHelper(configurationService, TENANT_ID, sProcessDefinition);
        Properties properties = getProperties();

        //when
        autoLoginConfigurationHelper.enableAutoLogin(properties);

        //then
        String expectedJson = "[{\"processname\":\"first Pool\",\"processversion\":\"1.0\",\"username\":\"john\",\"password\":\"secret\"}]";
        verifyExpectedJsonConfigurationIsStored(expectedJson);
    }

    @Test
    public void enableAutoLogin_should_add_autoLogin_configuration() throws Exception {
        //given
        sProcessDefinition = new SProcessDefinitionImpl("new Pool", "1.0");
        String existingConfig = "[{\"processname\":\"Pool\",\"processversion\":\"1.0\",\"username\":\"autologin-user\",\"password\":\"secret\"}]";
        when(configurationService.getTenantPortalConfiguration(TENANT_ID, AUTOLOGIN_V6_JSON))
                .thenReturn(new BonitaConfiguration(AUTOLOGIN_V6_JSON, existingConfig.getBytes()));
        AutoLoginConfigurationHelper autoLoginConfigurationHelper = new AutoLoginConfigurationHelper(configurationService, TENANT_ID, sProcessDefinition);

        //when
        autoLoginConfigurationHelper.enableAutoLogin(getProperties());

        //then
        String expectedJson = "[{\"processname\":\"Pool\",\"processversion\":\"1.0\",\"username\":\"autologin-user\",\"password\":\"secret\"}," +
                "{\"processname\":\"new Pool\",\"processversion\":\"1.0\",\"username\":\"john\",\"password\":\"secret\"}]";
        verifyExpectedJsonConfigurationIsStored(expectedJson);
    }

    @Test
    public void enableAutoLogin_should_update_autoLogin_configuration() throws Exception {
        //given
        sProcessDefinition = new SProcessDefinitionImpl("Pool", "1.0");
        String existingConfig = "[{\"processname\":\"Pool\",\"processversion\":\"1.0\",\"username\":\"jack\",\"password\":\"bpm\"}]";
        when(configurationService.getTenantPortalConfiguration(TENANT_ID, AUTOLOGIN_V6_JSON))
                .thenReturn(new BonitaConfiguration(AUTOLOGIN_V6_JSON, existingConfig.getBytes()));
        AutoLoginConfigurationHelper autoLoginConfigurationHelper = new AutoLoginConfigurationHelper(configurationService, TENANT_ID, sProcessDefinition);

        //when
        autoLoginConfigurationHelper.enableAutoLogin(getProperties());

        //then
        String expectedJson = "[{\"processname\":\"Pool\",\"processversion\":\"1.0\",\"username\":\"john\",\"password\":\"secret\"}]";
        verifyExpectedJsonConfigurationIsStored(expectedJson);
    }

    @Test
    public void enableAutoLogin_should_throw_exception_when_enable_fails() throws Exception {

        //given
        sProcessDefinition = new SProcessDefinitionImpl("Pool", "1.0");
        when(configurationService.getTenantPortalConfiguration(TENANT_ID, AUTOLOGIN_V6_JSON))
                .thenReturn(new BonitaConfiguration(AUTOLOGIN_V6_JSON, "not json".getBytes()));
        AutoLoginConfigurationHelper autoLoginConfigurationHelper = new AutoLoginConfigurationHelper(configurationService, TENANT_ID, sProcessDefinition);

        //expect
        expectedException.expect(SProcessEnablementException.class);
        expectedException.expectMessage(
                "unable to activate auto login for process with name:Pool and version:1.0");

        //when
        autoLoginConfigurationHelper.enableAutoLogin(getProperties());
    }

    @Test
    public void disableAutoLogin_should_throw_exception_when_enable_fails() throws Exception {

        //given
        sProcessDefinition = new SProcessDefinitionImpl("Pool", "1.0");
        when(configurationService.getTenantPortalConfiguration(TENANT_ID, AUTOLOGIN_V6_JSON))
                .thenReturn(new BonitaConfiguration(AUTOLOGIN_V6_JSON, "not json".getBytes()));
        AutoLoginConfigurationHelper autoLoginConfigurationHelper = new AutoLoginConfigurationHelper(configurationService, TENANT_ID, sProcessDefinition);

        //expect
        expectedException.expect(SProcessDisablementException.class);
        expectedException.expectMessage(
                "unable to disable auto login for process with name:Pool and version:1.0");

        //when
        autoLoginConfigurationHelper.disableAutoLogin();

    }

    @Test
    public void disableAutoLogin_should_remove_configuration() throws Exception {
        //given
        sProcessDefinition = new SProcessDefinitionImpl("Pool", "1.0");
        String existingConfig = "[{\"processname\":\"Pool\",\"processversion\":\"1.0\",\"username\":\"jack\",\"password\":\"bpm\"}]";
        when(configurationService.getTenantPortalConfiguration(TENANT_ID, AUTOLOGIN_V6_JSON))
                .thenReturn(new BonitaConfiguration(AUTOLOGIN_V6_JSON, existingConfig.getBytes()));
        AutoLoginConfigurationHelper autoLoginConfigurationHelper = new AutoLoginConfigurationHelper(configurationService, TENANT_ID, sProcessDefinition);

        //when
        autoLoginConfigurationHelper.disableAutoLogin();

        //then
        verifyExpectedJsonConfigurationIsStored("[]");
    }

    private void verifyExpectedJsonConfigurationIsStored(String expectedJson) {
        verify(configurationService).storeTenantPortalConf(argumentCaptor.capture(), eq(TENANT_ID));
        final List<BonitaConfiguration> configurations = argumentCaptor.getValue();
        assertThat(configurations).as("should have stored configuration").hasSize(1);
        final byte[] returnedContent = configurations.get(0).getResourceContent();
        assertThat(new String(returnedContent)).as("should store expected json content").isEqualTo(expectedJson);
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.put("security.password.validator", "org.bonitasoft.web.rest.server.api.organization.password.validator.DefaultPasswordValidator");
        properties.put("forms.application.login.auto.password", "secret");
        properties.put("security.rest.api.authorizations.check.enabled", "true");
        properties.put("forms.application.login.auto", "true");
        properties.put("forms.application.login.auto.username", "john");
        properties.put("security.rest.api.authorizations.check.debug", "false");
        return properties;
    }
}
