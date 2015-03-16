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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;

import org.bonitasoft.engine.bdm.proxy.assertion.ProxyAssert;
import org.bonitasoft.engine.bdm.proxy.model.TestEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ProxyTest {

    @Mock
    private LazyLoader lazyLoader;

    @InjectMocks
    private Proxyfier proxyfier;

    private TestEntity mockLazyLoaderToReturn(final TestEntity entity) {
        when(lazyLoader.load(any(Method.class), any(Long.class))).thenReturn(entity);
        return entity;
    }

    @Test
    public void should_load_object_when_method_is_lazy_and_object_is_not_loaded() {
        final TestEntity expectedEntity = mockLazyLoaderToReturn(new TestEntity());
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        final TestEntity lazyEntity = entity.getLazyEntity();

        verify(lazyLoader).load(any(Method.class), any(Long.class));
        assertThat(lazyEntity).isEqualTo(expectedEntity);
    }

    @Test
    public void should_load_object_when_method_is_lazy_and_object_is_an_empty_list() {
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        entity.getLazyEntityList();

        verify(lazyLoader).load(any(Method.class), any(Long.class));
    }

    @Test
    public void should_return_value_when_it_has_been_already_loaded_before_proxyfication() {
        final String name = "this is a preloaded value";
        TestEntity entity = new TestEntity();
        entity.setName(name);
        entity = proxyfier.proxify(entity);

        final String proxyName = entity.getName();

        verifyZeroInteractions(lazyLoader);
        assertThat(proxyName).isEqualTo(name);
    }

    @Test
    public void should_not_load_object_which_has_been_already_lazy_loaded() {
        final TestEntity expectedEntity = mockLazyLoaderToReturn(new TestEntity());
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        entity.getLazyEntity();
        final TestEntity lazyEntity = entity.getLazyEntity();

        verify(lazyLoader, times(1)).load(any(Method.class), any(Long.class));
        assertThat(lazyEntity).isEqualTo(expectedEntity);
    }

    @Test
    public void should_not_load_object_for_a_non_lazy_loading_method() {
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        entity.getEagerEntity();

        verifyZeroInteractions(lazyLoader);
    }

    @Test
    public void should_not_load_object_that_has_been_set_by_a_setter() {
        final TestEntity expectedEntity = new TestEntity();
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        entity.setLazyEntity(expectedEntity);
        final TestEntity lazyEntity = entity.getLazyEntity();

        verifyZeroInteractions(lazyLoader);
        assertThat(lazyEntity).isEqualTo(expectedEntity);
    }

    @Test
    public void should_return_a_proxy_when_calling_a_getter_returning_an_entity() {
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        final TestEntity eagerEntity = entity.getEagerEntity();

        ProxyAssert.assertThat(eagerEntity).isAProxy();
    }

    @Test
    public void should_not_return_a_proxy_when_calling_a_getter_not_returning_an_entity() {
        final TestEntity entity = proxyfier.proxify(new TestEntity());
        entity.setName("aName");

        final String name = entity.getName();

        ProxyAssert.assertThat(name).isNotAProxy();
    }

    @Test
    public void should_return_a_list_of_proxies_when_calling_a_getter_returning_a_list_of_entities() {
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        final List<TestEntity> entities = entity.getEagerEntities();

        for (final TestEntity e : entities) {
            ProxyAssert.assertThat(e).isAProxy();
        }
    }

    @Test
    public void should_not_return_a_list_of_proxies_when_calling_a_getter_not_returning_a_list_of_entities() {
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        final List<String> strings = entity.getStrings();

        for (final String string : strings) {
            ProxyAssert.assertThat(string).isNotAProxy();
        }
    }
}
