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
    package org.bonitasoft.engine.service.impl;

    import java.io.IOException;
    import java.util.HashMap;
    import java.util.Map;
    import java.util.Properties;

    import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
    import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
    import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
    import org.springframework.context.support.FileSystemXmlApplicationContext;

    /**
     * @author Charles Souillard
     */
    public class SpringFileSystemBeanAccessorFactory {

        private static SpringPlatformInitFileSystemBeanAcessor platformInit;
        private static SpringPlatformFileSystemBeanAccessor platform;
        private static Map<Long, SpringTenantFileSystemBeanAccessor> tenants = new HashMap<>();

        public static SpringPlatformInitFileSystemBeanAcessor getPlatformInitAccessor() {
            if (platformInit == null) {
                try {
                    platformInit = new SpringPlatformInitFileSystemBeanAcessor(null);
                } catch (IOException | BonitaHomeNotSetException e) {
                    throw new RuntimeException(e);
                }
            }
            return platformInit;
        }

        public static SpringPlatformFileSystemBeanAccessor getPlatformAccessor() {
            if (platform == null) {
                try {
                    platform = new SpringPlatformFileSystemBeanAccessor(getPlatformInitAccessor());
                } catch (IOException | BonitaHomeNotSetException e) {
                    throw new RuntimeException(e);
                }
            }
            return platform;
        }

        public static SpringTenantFileSystemBeanAccessor getTenantAccessor(final long tenantId) {
            if (!tenants.containsKey(tenantId)) {
                try {
                    tenants.put(tenantId, new SpringTenantFileSystemBeanAccessor(getPlatformAccessor(), tenantId));
                } catch (IOException | BonitaHomeNotSetException e) {
                    throw new RuntimeException(e);
                }
            }
            return tenants.get(tenantId);
        }

    }
