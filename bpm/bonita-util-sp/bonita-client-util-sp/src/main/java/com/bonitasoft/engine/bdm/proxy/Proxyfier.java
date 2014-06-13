package com.bonitasoft.engine.bdm.proxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.bdm.lazy.LazyLoaded;

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

    private class LazyMethodHandler implements MethodHandler {

        private LazyLoader lazyloader;
        private List<String> alreadyLoaded = new ArrayList<String>();

        public LazyMethodHandler(LazyLoader lazyloader) {
            this.lazyloader = lazyloader;
        }

        @Override
        public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
            Object notLazyLoaded = proceed.invoke(self, args);
            if (shouldBeLoaded(thisMethod, notLazyLoaded)) {
                notLazyLoaded = lazyloader.load(thisMethod, ((Entity) self).getPersistenceId());
            }
            alreadyLoaded.add(toFieldName(thisMethod.getName()));
            return notLazyLoaded;
        }

        private boolean shouldBeLoaded(Method thisMethod, Object notLazyLoaded) {
            return notLazyLoaded == null && !alreadyLoaded.contains(toFieldName(thisMethod.getName())) && thisMethod.getAnnotation(LazyLoaded.class) != null;
        }

        private String toFieldName(final String methodName) {
            if (methodName.startsWith("get") || methodName.startsWith("set") && methodName.length() > 3) {
                return methodName.substring(3).toLowerCase();
            }
            throw new IllegalArgumentException(methodName + " is not a valid getter or setter name.");
        }
    }
}
