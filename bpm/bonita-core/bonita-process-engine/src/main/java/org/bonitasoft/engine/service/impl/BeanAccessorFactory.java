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
 * Factory that build bean accessor ( == spring contexts )
 * It is the entry point on the configuration of the engine
 *
 * @author Charles Souillard
 */
public class BeanAccessorFactory {

    private static PlatformInitBeanAccessor platformInit;
    private static PlatformBeanAccessor platform;
    private static Map<Long, TenantBeanAccessor> tenants = new HashMap<>();

    public static PlatformInitBeanAccessor getPlatformInitBeanAccessor() {
        if (platformInit == null) {
            platformInit = new PlatformInitBeanAccessor();
        }
        return platformInit;
    }

    public static PlatformBeanAccessor getPlatformBeanAccessor() {
        if (platform == null) {
            platform = new PlatformBeanAccessor(getPlatformInitBeanAccessor().getContext());
        }
        return platform;
    }

    public static TenantBeanAccessor getTenantBeanAccessor(final long tenantId) {
        if (!tenants.containsKey(tenantId)) {
            tenants.put(tenantId, new TenantBeanAccessor(getPlatformBeanAccessor().getContext(), tenantId));
        }
        return tenants.get(tenantId);
    }

}
