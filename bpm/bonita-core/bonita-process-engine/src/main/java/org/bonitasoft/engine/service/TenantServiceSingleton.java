/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.service;

import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;

/**
 * @deprecated use {@link ServiceAccessorSingleton} instead
 * @author Matthieu Chaffotte
 */
@Deprecated(since = "9.0.0", forRemoval = true)
public final class TenantServiceSingleton {

    private static TenantServiceAccessor instance = null;

    private TenantServiceSingleton() {
        super();
    }

    private static TenantServiceAccessor getTenant() {
        try {
            return ServiceAccessorFactory.getInstance().createTenantServiceAccessor();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static TenantServiceAccessor getInstance() {
        if (instance == null) {
            instance = getTenant();
        }
        return instance;
    }

}
