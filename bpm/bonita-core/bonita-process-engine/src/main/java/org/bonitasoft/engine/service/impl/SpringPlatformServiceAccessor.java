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

import org.bonitasoft.engine.dependency.DependencyService;
import org.bonitasoft.engine.service.ServiceAccessor;

/**
 * Uses spring to access platform services
 *
 * @deprecated since 9.0.0, use {@link SpringServiceAccessor} instead
 */
@Deprecated(forRemoval = true, since = "9.0.0")
public class SpringPlatformServiceAccessor extends SpringServiceAccessor {

    public SpringPlatformServiceAccessor(final SpringBeanAccessor beanAccessor) {
        super(beanAccessor);
    }

    /**
     * @deprecated use {@link ServiceAccessor#getPlatformDependencyService()} instead
     */
    @Override
    @Deprecated(since = "9.0.0")
    public DependencyService getDependencyService() {
        return beanAccessor.getService("platformDependencyService", DependencyService.class);
    }

}
