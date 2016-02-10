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
package org.bonitasoft.engine.bdm.dao.client.resources.proxy;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.bonita.pojo.AddressForTesting;
import org.bonita.pojo.EmployeeForTesting;
import org.bonitasoft.engine.bdm.proxy.assertion.ProxyAssert;
import org.bonitasoft.engine.bdm.proxy.model.TestEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProxyfierTest {

    @Mock
    private LazyLoader lazyLoader;

    @InjectMocks
    private Proxyfier proxyfier;

    @Test
    public void should_return_null_when_entity_is_null() throws Exception {
        TestEntity entity = null;
        
        TestEntity proxy = proxyfier.proxify(entity);
        
        ProxyAssert.assertThat(proxy).isNull();
    }
    
    @Test
    public void should_return_null_when_list_of_entities_is_null() throws Exception {
        List<TestEntity> entities = null;
        
        List<TestEntity> proxies = proxyfier.proxify(entities);
        
        ProxyAssert.assertThat(proxies).isNull();
    }
    
    @Test
    public void should_proxify_an_entity() {
        final TestEntity entity = new TestEntity();

        final TestEntity proxy = proxyfier.proxify(entity);

        ProxyAssert.assertThat(proxy).isAProxy();
    }

    @Test
    public void should_proxify_a_list_of_entities() {
        final List<TestEntity> entities = Arrays.asList(new TestEntity(),
                new TestEntity());

        final List<TestEntity> proxies = proxyfier.proxify(entities);

        for (final TestEntity entity : proxies) {
            ProxyAssert.assertThat(entity).isAProxy();
        }
    }

    @Test
    public void shouldProxyfier_retrieve_list_setter_when_lazyLoader_returns_arraylist() throws Exception {

        //given
        final AddressForTesting address1 = new AddressForTesting();
        final AddressForTesting address2 = new AddressForTesting();
        final List<AddressForTesting> addresses = new ArrayList<AddressForTesting>();
        addresses.add(address1);
        addresses.add(address2);
        doReturn(addresses).when(lazyLoader).load(any(Method.class), anyLong());

        //when
        final long persistenceId = 1L;
        final EmployeeForTesting employee = new EmployeeForTesting();
        employee.setPersistenceId(persistenceId);
        final EmployeeForTesting proxify = proxyfier.proxify(employee);

        //then
        final List<?> lazyAddresses = (List<?>) proxify.getClass().getMethod("getAddresses", new Class[0]).invoke(proxify);
        ProxyAssert.assertThat(proxify).isAProxy();
        Assertions.assertThat(lazyAddresses).hasSize(2);

    }
}
