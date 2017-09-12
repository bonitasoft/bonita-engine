/**
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
 **/
package org.bonitasoft.engine.business.data.impl;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.WeakHashMap;

import org.bonitasoft.engine.bdm.Entity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import com.company.pojo.Employee;

import javassist.util.proxy.ProxyFactory;

@RunWith(MockitoJUnitRunner.class)
public class ProxyCacheManagerTest {

    @Test
    public void should_retrieve_the_proxyCache_map() throws Exception {
        final ProxyCacheManager proxyCacheManager = new ProxyCacheManager();

        createProxy(new EntityPojo());
        createProxy(new EntityPojo());
        createProxy(new Employee("John", "Doe"));
        createProxy(new Employee("Jane", "Doe"));

        WeakHashMap cache = proxyCacheManager.get();
        assertThat(cache).hasSize(1);

        proxyCacheManager.clearCache();
        cache = proxyCacheManager.get();
        assertThat(cache).isEmpty();
    }

    private Entity createProxy(Entity entity) {
        final ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(entity.getClass());
        try {
            return (Entity) factory.create(new Class<?>[0], new Object[0], null);
        } catch (final Exception e) {
            throw new RuntimeException("Error when proxifying object", e);
        }
    }

}