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

import java.util.Properties;

/**
 * @author Zhiheng Yang
 */
public class PlatformTenantConfigProperties {

    private static final String PLATFORM_TENANT_DEFAULT_USERNAME = "platform.tenant.default.username";

    private static final String PLATFORM_TENANT_DEFAULT_PASSWORD = "platform.tenant.default.password";

    private static final String PROPERTIES_FILE = "platform-tenant-config.properties";

    private Properties getProperties() {
        return ConfigurationFilesManager.getInstance().getPlatformProperties(PROPERTIES_FILE);
    }

    public String defaultTenantUserName() {
        return getProperties().getProperty(PLATFORM_TENANT_DEFAULT_USERNAME);
    }

    public String defaultTenantPassword() {
        return getProperties().getProperty(PLATFORM_TENANT_DEFAULT_PASSWORD);
    }

}
