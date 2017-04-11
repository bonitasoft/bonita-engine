/**
 * Copyright (C) 2017 Bonitasoft S.A.
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

package org.bonitasoft.engine.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Baptiste Mesta.
 */
public class SpringServiceAccessors implements ServiceAccessors {

    private SpringBeanAccessor platformInit;
    private SpringBeanAccessor platform;
    private Map<Long, SpringBeanAccessor> tenants = new HashMap<>();

    //----  Initialize spring contexts
    private synchronized SpringBeanAccessor getPlatformInitBeanAccessor() {
        if (platformInit == null) {
            platformInit = new PlatformInitBeanAccessor();
        }
        return platformInit;
    }

    protected synchronized SpringBeanAccessor getPlatformBeanAccessor() {
        if (platform == null) {
            platform = new PlatformBeanAccessor(getPlatformInitBeanAccessor().getContext());
        }
        return platform;
    }

    protected synchronized SpringBeanAccessor getTenantBeanAccessor(final long tenantId) {
        if (!tenants.containsKey(tenantId)) {
            tenants.put(tenantId, createTenantBeanAccessor(tenantId));
        }
        return tenants.get(tenantId);
    }

    protected TenantBeanAccessor createTenantBeanAccessor(long tenantId) {
        return new TenantBeanAccessor(getPlatformBeanAccessor().getContext(), tenantId);
    }

    //---- Wrap context with service accessors

    @Override
    public PlatformInitServiceAccessor getPlatformInitServiceAccessor() {
        return new SpringPlatformInitServiceAccessor(getPlatformInitBeanAccessor());
    }

    @Override
    public PlatformServiceAccessor getPlatformServiceAccessor() {
        return new SpringPlatformServiceAccessor(getPlatformBeanAccessor());
    }

    @Override
    public TenantServiceAccessor getTenantServiceAccessor(Long tenantId) {
        return new SpringTenantServiceAccessor(getTenantBeanAccessor(tenantId), tenantId);
    }

    @Override
    public void destroy() {
        Set<Map.Entry<Long, SpringBeanAccessor>> tenantAccessors = tenants.entrySet();
        for (Map.Entry<Long, SpringBeanAccessor> tenantAccessor : tenantAccessors) {
            tenantAccessor.getValue().destroy();
        }
        tenants.clear();
        if (platform != null) {
            platform.destroy();
            platform = null;
        }
        if (platformInit != null) {
            platformInit.destroy();
            platformInit = null;
        }
    }
}
