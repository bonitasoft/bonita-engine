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
package org.bonitasoft.engine;

import java.util.Properties;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.home.BonitaHomeServer;

/**
 * @author Baptiste Mesta
 */
public class EngineInitializerProperties {

    protected static final String PROPERTIES_FILENAME = "platform-tenant-config.properties";

    /**
     * Configurations of platform
     */
    public static final String PLATFORM_CREATE = "platform.create";

    protected static final String NODE_START = "node.start";

    protected static final String NODE_STOP = "node.stop";

    protected static final String PLATFORM_ADMIN_USERNAME = "platformAdminUsername";

    protected static final String TENANT_ADMIN_USERNAME = "userName";

    // /**
    // * Configurations of tenant
    // */
    // public static final String PLATFORM_DEFAULT_TENANT_ID = "platform.tenant.default.id";

    /**
     * properties of the platform
     */
    private Properties platformProperties;

    /**
     * Private contructor to prevent instantiation
     * 
     * @throws BonitaHomeNotSetException
     */
    public EngineInitializerProperties() {
        try {
            platformProperties = BonitaHomeServer.getInstance().getPlatformProperties();
        } catch (Exception e) {
            throw new BonitaRuntimeException(e.getMessage(), e);
        }
    }

    public boolean shouldCreatePlatform() {
        final String needCreate = getProperty(PLATFORM_CREATE, platformProperties);
        return Boolean.valueOf(needCreate);
    }

    public boolean shouldStartPlatform() {
        final String start = getProperty(NODE_START, platformProperties);
        return Boolean.valueOf(start);
    }

    public boolean shouldStopPlatform() {
        final String stop = getProperty(NODE_STOP, platformProperties);
        return Boolean.valueOf(stop);
    }

    public String getPlatformAdminUsername() {
        String platformAdminUsername = PLATFORM_ADMIN_USERNAME;
        return getProperty(platformAdminUsername, platformProperties);
    }

    private String getProperty(final String propertyName, final Properties properties) {
        String property = properties.getProperty(propertyName);
        if (property == null) {
            throw new IllegalStateException("Mandatory property not set in bonita-platform.properties: " + propertyName);
        }
        return property;
    }

    public String getTenantAdminUsername(final long tenantId) {
        try {
            return getProperty(TENANT_ADMIN_USERNAME, BonitaHomeServer.getInstance().getTenantProperties(tenantId));
        } catch (Exception e) {
            throw new BonitaRuntimeException(e.getMessage(), e);
        }
    }
}
