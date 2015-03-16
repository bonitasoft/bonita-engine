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
package org.bonitasoft.engine.service;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;

/**
 * @author Matthieu Chaffotte
 */
public final class TenantServiceSingleton {

    private static final Map<Long, TenantServiceAccessor> instances = new HashMap<Long, TenantServiceAccessor>();

    private TenantServiceSingleton() {
        super();
    }

    private static TenantServiceAccessor getTenant(final long tenantId) {
        try {
            return ServiceAccessorFactory.getInstance().createTenantServiceAccessor(tenantId);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static TenantServiceAccessor getInstance() {
        long tenantId;
        try {
            tenantId = ServiceAccessorFactory.getInstance().createSessionAccessor().getTenantId();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
        return getInstance(tenantId);
    }

    public static TenantServiceAccessor getInstance(final long tenantId) {
        if (!instances.containsKey(tenantId)) {
            instances.put(tenantId, getTenant(tenantId));
        }
        return instances.get(tenantId);
    }

}
