package com.bonitasoft.engine.bdm.proxy;

import static com.bonitasoft.engine.bdm.proxy.ProxyAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

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
    public void proxy_should_load_object_when_method_is_lazy_and_object_is_not_loaded() throws Exception {
        TestEntity entity = proxyfier.proxify(new TestEntity());

        entity.getNotLoadedEntity();

        verify(lazyLoader).load(any(Method.class), any(Long.class));
    }

    @Test
    public void proxy_should_not_load_object_when_it_has_been_already_loaded_before_proxyfication() throws Exception {
        TestEntity entity = proxyfier.proxify(new TestEntity());

        entity.getAlreadyLoadedEntity();

        verifyZeroInteractions(lazyLoader);
    }

    @Test
    public void proxy_should_not_load_object_which_has_been_already_lazy_loaded() throws Exception {
        TestEntity entity = proxyfier.proxify(new TestEntity());

        entity.getNotLoadedEntity();
        entity.getNotLoadedEntity();

        verify(lazyLoader, times(1)).load(any(Method.class), any(Long.class));
    }

    @Test
    public void proxy_should_not_load_object_for_a_non_lazy_loading_method() throws Exception {
        TestEntity entity = proxyfier.proxify(new TestEntity());

        entity.getEagerEntity();

        verifyZeroInteractions(lazyLoader);
    }

    @Test
    public void proxy_should_not_load_object_that_has_been_set_by_a_setter() throws Exception {
        TestEntity entity = proxyfier.proxify(new TestEntity());

        entity.setEntity(null);
        entity.getEntity();

        verifyZeroInteractions(lazyLoader);
    }

    @Test
    public void can_proxify_a_list_of_entities() throws Exception {
        List<TestEntity> entities = Arrays.asList(new TestEntity(), new TestEntity());

        List<TestEntity> proxies = proxyfier.proxify(entities);

        for (TestEntity entity : proxies) {
            assertThat(entity).isAProxy();
        }
    }
    // TODO, proxify returned object
    // TODO, proxify lists ?

}
