package com.bonitasoft.engine.bdm.proxy;

import javassist.util.proxy.ProxyFactory;

import com.bonitasoft.engine.bdm.Entity;

/**
 * @author Colin Puy
 */
public class Proxyfier {

    @SuppressWarnings("unchecked")
    public static <T extends Entity> T proxify(T entity) {
        final ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(entity.getClass());
        factory.setFilter(new EntityGetterFilter());
        try {
            return (T) factory.create(new Class<?>[0], new Object[0], new LazyMethodHandler());
        } catch (Exception e) {
            throw new RuntimeException("Error when proxifying object", e);
        }
    }
}
