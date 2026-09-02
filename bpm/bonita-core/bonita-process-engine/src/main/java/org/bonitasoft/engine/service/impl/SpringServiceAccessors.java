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

import org.bonitasoft.engine.service.ServiceAccessor;

/**
 * @author Baptiste Mesta.
 */
public class SpringServiceAccessors implements ServiceAccessors {

    private SpringBeanAccessor springBeanAccessor;

    //----  Initialize spring contexts
    protected synchronized SpringBeanAccessor getBeanAccessor() {
        if (springBeanAccessor == null) {
            springBeanAccessor = createBeanAccessor();
        }
        return springBeanAccessor;
    }

    protected SpringBeanAccessor createBeanAccessor() {
        return new SpringBeanAccessor();
    }

    @Override
    public ServiceAccessor getServiceAccessor() {
        return new SpringServiceAccessor(getBeanAccessor());
    }

    @Override
    public void destroy() {
        if (springBeanAccessor != null) {
            springBeanAccessor.destroy();
            springBeanAccessor = null;
        }
    }
}
