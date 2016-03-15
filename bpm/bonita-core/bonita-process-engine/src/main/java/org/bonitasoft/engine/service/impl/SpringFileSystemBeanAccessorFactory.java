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

import java.util.HashMap;
import java.util.Map;

/**
 * @author Charles Souillard
 */
public class SpringFileSystemBeanAccessorFactory {

    private static SpringPlatformInitFileSystemBeanAccessor platformInit;
    private static SpringPlatformFileSystemBeanAccessor platform;
    private static Map<Long, SpringTenantFileSystemBeanAccessor> tenants = new HashMap<>();

    public static SpringPlatformInitFileSystemBeanAccessor getPlatformInitAccessor() {
        if (platformInit == null) {
            platformInit = new SpringPlatformInitFileSystemBeanAccessor();
        }
        return platformInit;
    }

    public static SpringPlatformFileSystemBeanAccessor getPlatformAccessor() {
        if (platform == null) {
            platform = new SpringPlatformFileSystemBeanAccessor(getPlatformInitAccessor().getContext());
        }
        return platform;
    }

    public static SpringTenantFileSystemBeanAccessor getTenantAccessor(final long tenantId) {
        if (!tenants.containsKey(tenantId)) {
            tenants.put(tenantId, new SpringTenantFileSystemBeanAccessor(getPlatformAccessor().getContext(), tenantId));
        }
        return tenants.get(tenantId);
    }

}
