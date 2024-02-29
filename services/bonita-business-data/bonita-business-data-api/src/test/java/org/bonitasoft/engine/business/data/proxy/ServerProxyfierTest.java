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
package org.bonitasoft.engine.business.data.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.bonitasoft.engine.bdm.Entity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Romain Bioteau
 * @author Laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerProxyfierTest {

    @Mock
    private ServerLazyLoader lazyLoader;
    private ServerProxyfier serverProxyfier;

    @Before
    public void setUp() throws Exception {
        serverProxyfier = new ServerProxyfier(lazyLoader);
    }

    @Test
    public void should_proxify_an_entity() {
        final PersonEntity proxy = serverProxyfier.proxify(new PersonEntity());

        assertThat(proxy).isInstanceOf(ProxyObject.class);
        assertThat(proxy.getClass().getSuperclass()).isEqualTo(PersonEntity.class);
    }

    @Test
    public void should_not_reproxify_a_server_proxy() {
        final PersonEntity originalProxy = serverProxyfier.proxify(new PersonEntity());
        final PersonEntity proxy = serverProxyfier.proxify(originalProxy);

        assertThat(proxy).isSameAs(originalProxy);
    }

    @Test
    public void should_reproxify_an_hibernate_proxy() throws Exception {
        final ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(PersonEntity.class);
        final Entity aProxy = (Entity) factory.create(new Class[0], new Object[0], new MethodHandler() {

            @Override
            public Object invoke(final Object self, final Method thisMethod, final Method proceed,
                    final Object[] args) {
                return null;
            }
        });
        final PersonEntity proxy = (PersonEntity) serverProxyfier.proxify(aProxy);

        assertThat(proxy).isNotSameAs(aProxy);
    }

    @Test
    public void should_call_on_lazy_loaded_getter_use_lazyLoader() throws Exception {
        //given
        final PersonEntity personEntity = new PersonEntity();

        final Method method = PersonEntity.class.getMethod("getWithLazyLoadedAnnotation");
        doReturn("lazyResult").when(lazyLoader).load(any(Method.class), anyLong());

        //when
        final PersonEntity proxy = serverProxyfier.proxify(personEntity);
        final String withLazyLoadedAnnotation = proxy.getWithLazyLoadedAnnotation();

        //
        verify(lazyLoader).load(method, personEntity.getPersistenceId());
        assertThat(withLazyLoadedAnnotation).isEqualTo("lazyResult");
    }

    @Test
    public void should_not_call_lazyLoader() throws Exception {
        //given
        final PersonEntity personEntity = new PersonEntity();
        final Method method = PersonEntity.class.getMethod("getWithoutLazyLoadedAnnotation");

        //when
        final PersonEntity proxy = serverProxyfier.proxify(personEntity);
        final String withLazyLoadedAnnotation = proxy.getWithoutLazyLoadedAnnotation();

        //
        verify(lazyLoader, never()).load(method, personEntity.getPersistenceId());
        assertThat(withLazyLoadedAnnotation).isEqualTo("getWithoutLazyLoadedAnnotation");
    }

    @Test
    public void unProxy_should_not_remove_a_proxy_on_a_null_entity() {
        final Entity entity = ServerProxyfier.unProxifyIfNeeded(null);

        assertThat(entity).isNull();
    }

    @Test
    public void unProxy_should_not_remove_a_proxy_on_an_entity() {
        final PersonEntity proxy = new PersonEntity();

        final Entity entity = ServerProxyfier.unProxifyIfNeeded(proxy);

        assertThat(ServerProxyfier.isLazyMethodProxyfied(entity)).isFalse();
    }

    @Test
    public void unProxy_should_remove_a_proxy_on_an_entity() {
        final PersonEntity proxy = serverProxyfier.proxify(new PersonEntity());

        final Entity entity = ServerProxyfier.unProxifyIfNeeded(proxy);

        assertThat(ServerProxyfier.isLazyMethodProxyfied(entity)).isFalse();
    }

    @Test
    public void should_unproxify_object_with_cycle() {
        A a = new A();
        B b = new B();
        A aProxy = serverProxyfier.proxify(a);
        B bProxy = serverProxyfier.proxify(b);
        b.setA(aProxy);
        a.setB(bProxy);

        A unproxyfied = (A) ServerProxyfier.unProxifyIfNeeded(aProxy);

        assertThat(ServerProxyfier.isLazyMethodProxyfied(unproxyfied)).isFalse();
        assertThat(ServerProxyfier.isLazyMethodProxyfied(unproxyfied.getB())).isFalse();
    }

    @Test
    public void should_unproxify_object_directReferenceToItself() {
        A a = new A();
        A aProxy = serverProxyfier.proxify(a);
        a.setA(aProxy);

        A unproxyfied = (A) ServerProxyfier.unProxifyIfNeeded(aProxy);

        assertThat(ServerProxyfier.isLazyMethodProxyfied(unproxyfied)).isFalse();
        assertThat(ServerProxyfier.isLazyMethodProxyfied(unproxyfied.getA())).isFalse();
    }

    @Test
    public void unProxy_should_remove_a_proxy_on_an_ref_attribute_of_an_entity() {
        final Address proxy = serverProxyfier.proxify(new Address());
        final Employee employee = new Employee(10L, 45L, "John", "Doe");
        employee.setAddress(proxy);

        final Employee entity = (Employee) ServerProxyfier.unProxifyIfNeeded(employee);

        assertThat(ServerProxyfier.isLazyMethodProxyfied(entity.getAddress())).isFalse();
    }

    @Test
    public void unProxy_should_remove_a_proxy_on_a_list_attribute_of_an_entity() {
        final List<Address> addresses = new ArrayList<>();
        addresses.add(new Address());
        addresses.add(serverProxyfier.proxify(new Address()));
        final Employee employee = new Employee(10L, 45L, "John", "Doe");
        employee.setAddresses(addresses);

        final Employee entity = (Employee) ServerProxyfier.unProxifyIfNeeded(employee);

        for (final Address address : entity.getAddresses()) {
            assertThat(ServerProxyfier.isLazyMethodProxyfied(address)).isFalse();
        }
    }

    @Test
    public void unproxify_should_not_fail_on_a_first_null_object_in_a_list_attribute_of_an_entity() {
        final List<Address> addresses = new ArrayList<>();
        addresses.add(null);
        final Address address = new Address();
        addresses.add(serverProxyfier.proxify(address));
        final Employee employee = new Employee(11L, 42L, "Ol", "Delaf");
        employee.setAddresses(addresses);

        final Employee entity = (Employee) ServerProxyfier.unProxifyIfNeeded(employee);

        assertThat(entity.getAddresses()).hasSize(2).containsExactly(null, address);
    }

    @Test
    public void unproxify_should_not_fail_on_a_later_null_object_in_a_list_attribute_of_an_entity() {
        final List<Address> addresses = new ArrayList<>();
        final Address address = new Address();
        addresses.add(serverProxyfier.proxify(address));
        addresses.add(null);
        final Employee employee = new Employee(11L, 42L, "Ol", "Delaf");
        employee.setAddresses(addresses);

        final Employee entity = (Employee) ServerProxyfier.unProxifyIfNeeded(employee);

        assertThat(entity.getAddresses()).hasSize(2).containsExactly(address, null);
    }

    @Test
    public void unproxify_should_not_fail_when_all_objects_in_a_list_attribute_of_an_entity_are_null() {
        final List<Address> addresses = new ArrayList<>();
        addresses.add(null);
        addresses.add(null);
        final Employee employee = new Employee(11L, 42L, "Ol", "Delaf");
        employee.setAddresses(addresses);

        final Employee entity = (Employee) ServerProxyfier.unProxifyIfNeeded(employee);

        assertThat(entity.getAddresses()).hasSize(2).containsExactly(null, null);
    }

    @Test
    public void should_return_null_when_proxifying_a_null_entity() {
        final Entity proxy = serverProxyfier.proxify((Entity) null);

        assertThat(proxy).isNull();
    }
}
