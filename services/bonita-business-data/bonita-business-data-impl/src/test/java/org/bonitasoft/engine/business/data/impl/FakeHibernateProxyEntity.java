/*
 * Copyright (C) 2016 Bonitasoft S.A.
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
 */
package org.bonitasoft.engine.business.data.impl;

import org.bonitasoft.engine.bdm.Entity;
import org.hibernate.proxy.AbstractLazyInitializer;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

/**
 * author Emmanuel Duchastenier
 */
public class FakeHibernateProxyEntity implements HibernateProxy, Entity {

    @Override
    public Object writeReplace() {
        return null;
    }

    @Override
    public LazyInitializer getHibernateLazyInitializer() {
        return new AbstractLazyInitializer() {

            @Override
            public Class getPersistentClass() {
                return EntityPojo.class;
            }

        };
    }

    @Override
    public Long getPersistenceId() {
        return null;
    }

    @Override
    public Long getPersistenceVersion() {
        return null;
    }
}
