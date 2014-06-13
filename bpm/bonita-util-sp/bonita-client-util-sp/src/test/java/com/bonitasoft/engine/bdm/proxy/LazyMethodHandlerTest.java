package com.bonitasoft.engine.bdm.proxy;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.lang.reflect.Method;

import javassist.util.proxy.ProxyFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LazyMethodHandlerTest {

    @Mock
    private LazyLoader lazyLoader;

    @InjectMocks
    private LazyMethodHandler methodHandler;

    @Test
    public void should_lazy_load_object_when_method_is_lazy_and_object_is_not_loaded() throws Exception {
        TestEntity entity = proxyfy(new TestEntity());

        entity.getNotLoadedEntity();

        verify(lazyLoader).load(any(Method.class), any(Long.class));
    }

    @Test
    public void should_not_load_object_when_already_loaded_before_proxyfication() throws Exception {
        TestEntity entity = proxyfy(new TestEntity());

        entity.getAlreadyLoadedEntity();

        verifyZeroInteractions(lazyLoader);
    }

    @Test
    public void should_not_load_object_which_has_been_already_lazy_loaded() throws Exception {
        TestEntity entity = proxyfy(new TestEntity());

        entity.getNotLoadedEntity();
        entity.getNotLoadedEntity();

        verify(lazyLoader, times(1)).load(any(Method.class), any(Long.class));
    }

    @Test
    public void should_not_load_object_for_a_non_lazy_loading_method() throws Exception {
        TestEntity entity = proxyfy(new TestEntity());

        entity.getEagerEntity();

        verifyZeroInteractions(lazyLoader);
    }

    @Test
    public void should_not_load_object_that_has_been_set_by_a_setter() throws Exception {
        TestEntity entity = proxyfy(new TestEntity());

        entity.setEntity(null);
        entity.getEntity();

        verifyZeroInteractions(lazyLoader);
    }

    private TestEntity proxyfy(TestEntity entity) {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(TestEntity.class);
        factory.setFilter(new EntityGetterAndSetterFilter());
        try {
            return (TestEntity) factory.create(new Class<?>[0], new Object[0], methodHandler);
        } catch (Exception e) {
            throw new RuntimeException("Error when proxifying object", e);
        }
    }
}
