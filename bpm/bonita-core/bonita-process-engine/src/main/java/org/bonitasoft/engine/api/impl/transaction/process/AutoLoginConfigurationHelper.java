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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.bonitasoft.engine.core.process.definition.exception.SProcessDisablementException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessEnablementException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.platform.configuration.ConfigurationService;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Laurent Leseigneur
 */
public class AutoLoginConfigurationHelper {

    public static final String AUTOLOGIN_V6_JSON = "autologin-v6.json";
    public static final String USER_NAME_KEY = "forms.application.login.auto.username";
    public static final String PASSWORD_KEY = "forms.application.login.auto.password";
    private final long tenantId;
    private final SProcessDefinition sProcessDefinition;
    private final ConfigurationService configurationService;
    private ObjectMapper mapper;

    public AutoLoginConfigurationHelper(ConfigurationService configurationService, long tenantId, SProcessDefinition sProcessDefinition) {
        this.configurationService = configurationService;
        this.tenantId = tenantId;
        this.sProcessDefinition = sProcessDefinition;
        mapper = new ObjectMapper();
    }

    public void enableAutoLogin(Properties properties) throws SProcessEnablementException {
        try {
            final String userName = properties.getProperty(USER_NAME_KEY);
            final String password = properties.getProperty(PASSWORD_KEY);
            final List<AutoLoginConfiguration> tenantConfigurations = getTenantConfiguration();
            final AutoLoginConfiguration autoLoginConfiguration = getProcessConfiguration(tenantConfigurations);
            if (autoLoginConfiguration == null) {
                tenantConfigurations.add(new AutoLoginConfiguration(sProcessDefinition.getName(), sProcessDefinition.getVersion(), userName, password));
            } else {
                autoLoginConfiguration.setUserName(userName);
                autoLoginConfiguration.setPassword(password);
            }
            storeAutoLoginConfiguration(tenantConfigurations);
        } catch (IOException e) {
            throw new SProcessEnablementException(
                    "unable to activate auto login for process with name:" + sProcessDefinition.getName() + " and version:" + sProcessDefinition.getVersion(),
                    e);
        }

    }

    public void disableAutoLogin() throws SProcessDisablementException {
        final List<AutoLoginConfiguration> tenantConfigurations;
        try {
            tenantConfigurations = getTenantConfiguration();
            final AutoLoginConfiguration configuration = getProcessConfiguration(tenantConfigurations);
            if (configuration != null) {
                tenantConfigurations.remove(configuration);
            }
            storeAutoLoginConfiguration(tenantConfigurations);
        } catch (IOException e) {
            throw new SProcessDisablementException(
                    "unable to disable auto login for process with name:" + sProcessDefinition.getName() + " and version:" + sProcessDefinition.getVersion(),
                    e);
        }
    }

    private AutoLoginConfiguration getProcessConfiguration(List<AutoLoginConfiguration> autoLoginConfigurations) {
        for (AutoLoginConfiguration configuration : autoLoginConfigurations) {
            if (matchWithProcess(configuration)) {
                return configuration;
            }
        }
        return null;
    }

    private boolean matchWithProcess(AutoLoginConfiguration configuration) {
        return configuration.getProcessName().equals(sProcessDefinition.getName())
                && configuration.getProcessVersion().equals(sProcessDefinition.getVersion());
    }

    private void storeAutoLoginConfiguration(List<AutoLoginConfiguration> autoLoginConfigurations) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            mapper.writeValue(byteArrayOutputStream, autoLoginConfigurations);
            final byte[] bytes = byteArrayOutputStream.toByteArray();
            configurationService.storeTenantPortalConf(Collections.singletonList(new BonitaConfiguration(AUTOLOGIN_V6_JSON, bytes)), tenantId);
        }
    }

    private List<AutoLoginConfiguration> getTenantConfiguration() throws IOException {
        final BonitaConfiguration configuration = configurationService.getTenantPortalConfiguration(tenantId, AUTOLOGIN_V6_JSON);
        List<AutoLoginConfiguration> autoLoginConfigurations = mapper.readValue(configuration.getResourceContent(),
                new TypeReference<List<AutoLoginConfiguration>>() {
                });
        return autoLoginConfigurations;
    }
}
