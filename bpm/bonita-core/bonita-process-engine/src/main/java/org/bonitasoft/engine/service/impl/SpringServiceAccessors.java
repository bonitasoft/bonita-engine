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
 * @author Baptiste Mesta.
 */
public class SpringServiceAccessors implements ServiceAccessors {

    private SpringBeanAccessor platform;

    //----  Initialize spring contexts
    protected synchronized SpringBeanAccessor getBeanAccessor() {
        if (platform == null) {
            platform = createBeanAccessor();
        }
        return platform;
    }

    protected SpringBeanAccessor createBeanAccessor() {
        return new SpringBeanAccessor();
    }

    //---- Wrap context with service accessors

    @Override
    public PlatformInitServiceAccessor getPlatformInitServiceAccessor() {
        return new SpringPlatformInitServiceAccessor(getBeanAccessor());
    }

    @Override
    public ServiceAccessor getServiceAccessor() {
        return new SpringServiceAccessor(getBeanAccessor());
    }

    @Override
    public PlatformServiceAccessor getPlatformServiceAccessor() {
        return new SpringPlatformServiceAccessor(getBeanAccessor());
    }

    @Override
    public TenantServiceAccessor getTenantServiceAccessor() {
        return new SpringTenantServiceAccessor(getBeanAccessor());
    }

    @Override
    public void destroy() {
        if (platform != null) {
            platform.destroy();
            platform = null;
        }
    }
}
