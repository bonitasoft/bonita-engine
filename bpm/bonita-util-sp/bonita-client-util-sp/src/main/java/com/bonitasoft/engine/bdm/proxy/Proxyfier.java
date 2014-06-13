package com.bonitasoft.engine.bdm.proxy;

import javassist.util.proxy.ProxyFactory;

import com.bonitasoft.engine.bdm.Entity;

/**
 * @author Colin Puy
 */
public class Proxyfier {

    private LazyLoader lazyLoader;

    public Proxyfier(LazyLoader lazyLoader) {
        this.lazyLoader = lazyLoader;
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> T proxify(T entity) {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(TestEntity.class);
        factory.setFilter(new EntityGetterAndSetterFilter());
        try {
            return (T) factory.create(new Class<?>[0], new Object[0], new LazyMethodHandler(lazyLoader));
        } catch (Exception e) {
            throw new RuntimeException("Error when proxifying object", e);
        }
    }
}
