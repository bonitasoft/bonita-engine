package com.bonitasoft.engine.bdm.dao.client.resources.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.bdm.dao.client.resources.proxy.LazyLoader;
import com.bonitasoft.engine.bdm.dao.client.resources.proxy.Proxyfier;
import com.bonitasoft.engine.bdm.proxy.assertion.ProxyAssert;
import com.bonitasoft.engine.bdm.proxy.model.TestEntity;

@RunWith(MockitoJUnitRunner.class)
public class ProxyTest {

    @Mock
    private LazyLoader lazyLoader;

    @InjectMocks
    private Proxyfier proxyfier;

    @Test
    public void should_load_object_when_method_is_lazy_and_object_is_not_loaded() throws Exception {
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        entity.getLazyEntity();

        verify(lazyLoader).load(any(Method.class), any(Long.class));
    }

    @Test
    public void should_load_object_when_method_is_lazy_and_object_is_an_empty_list() throws Exception {
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        entity.getLazyEntityList();

        verify(lazyLoader).load(any(Method.class), any(Long.class));
    }

    @Test
    public void should_return_value_when_it_has_been_already_loaded_before_proxyfication() throws Exception {
        final String name = "this is a preloaded value";
        TestEntity entity = new TestEntity();
        entity.setName(name);
        entity = proxyfier.proxify(entity);

        final String proxyName = entity.getName();

        verifyZeroInteractions(lazyLoader);
        assertThat(proxyName).isEqualTo(name);
    }

    @Test
    public void should_not_load_entity_when_it_has_been_already_loaded_before_proxyfication() throws Exception {
        final TestEntity alreadySetEntity = new TestEntity();
        alreadySetEntity.setName("aDeepName");
        TestEntity entity = new TestEntity();
        entity.setLazyEntity(alreadySetEntity);
        entity = proxyfier.proxify(entity);

        final TestEntity loadedEntity = entity.getLazyEntity();

        verifyZeroInteractions(lazyLoader);
        assertThat(loadedEntity.getName()).isEqualTo(alreadySetEntity.getName());
    }

    @Test
    public void should_not_load_object_which_has_been_already_lazy_loaded() throws Exception {
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        entity.getLazyEntity();
        entity.getLazyEntity();

        verify(lazyLoader, times(1)).load(any(Method.class), any(Long.class));
    }

    @Test
    public void should_not_load_object_for_a_non_lazy_loading_method() throws Exception {
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        entity.getEagerEntity();

        verifyZeroInteractions(lazyLoader);
    }

    @Test
    public void should_not_load_object_that_has_been_set_by_a_setter() throws Exception {
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        entity.setLazyEntity(null);
        entity.getLazyEntity();

        verifyZeroInteractions(lazyLoader);
    }

    @Test
    public void should_return_a_proxy_when_calling_a_getter_returning_an_entity() throws Exception {
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        final TestEntity eagerEntity = entity.getEagerEntity();

        ProxyAssert.assertThat(eagerEntity).isAProxy();
    }

    @Test
    public void should_not_return_a_proxy_when_calling_a_getter_not_returning_an_entity() throws Exception {
        final TestEntity entity = proxyfier.proxify(new TestEntity());
        entity.setName("aName");

        final String name = entity.getName();

        ProxyAssert.assertThat(name).isNotAProxy();
    }

    @Test
    public void should_return_a_list_of_proxies_when_calling_a_getter_returning_a_list_of_entities() throws Exception {
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        final List<TestEntity> entities = entity.getEagerEntities();

        for (final TestEntity e : entities) {
            ProxyAssert.assertThat(e).isAProxy();
        }
    }

    @Test
    public void should_not_return_a_list_of_proxies_when_calling_a_getter_not_returning_a_list_of_entities() throws Exception {
        final TestEntity entity = proxyfier.proxify(new TestEntity());

        final List<String> strings = entity.getStrings();

        for (final String string : strings) {
            ProxyAssert.assertThat(string).isNotAProxy();
        }
    }
}
