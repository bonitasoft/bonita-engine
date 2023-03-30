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
package org.bonitasoft.engine.service.impl;

import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * This is the main entry point to the engine services
 *
 * @author Baptiste Mesta.
 */
public interface ServiceAccessors {

    /**
     * @deprecated since 9.0.0, use {@link #getServiceAccessor()} instead
     */
    @Deprecated(forRemoval = true, since = "9.0.0")
    PlatformInitServiceAccessor getPlatformInitServiceAccessor();

    ServiceAccessor getServiceAccessor();

    /**
     * @deprecated since 9.0.0, use {@link #getServiceAccessor()} instead
     */
    @Deprecated(forRemoval = true, since = "9.0.0")
    PlatformServiceAccessor getPlatformServiceAccessor();

    /**
     * @deprecated since 9.0.0, use {@link #getServiceAccessor()} instead
     */
    @Deprecated(forRemoval = true, since = "9.0.0")
    TenantServiceAccessor getTenantServiceAccessor();

    void destroy();
}
