package com.bonitasoft.engine.bdm.proxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javassist.util.proxy.MethodFilter;
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
        return (T) proxifyEntity(entity);
    }

    private Entity proxifyEntity(Entity entity) {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(entity.getClass());
        factory.setFilter(new AllMethodFilter());
        try {
            return (Entity) factory.create(new Class<?>[0], new Object[0], new LazyMethodHandler(entity, lazyLoader));
        } catch (Exception e) {
            throw new RuntimeException("Error when proxifying object", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> List<T> proxify(List<T> entities) {
        return (List<T>) proxifyEntities((List<Entity>) entities);
    }

    private List<Entity> proxifyEntities(List<Entity> entities) {
        List<Entity> proxies = new ArrayList<Entity>();
        for (Entity entity : entities) {
            proxies.add(proxifyEntity(entity));
        }
        return proxies;
    }

    /**
     * Handler that lazy load values for lazy loading methods that hasn't been loaded
     */
    private class LazyMethodHandler implements MethodHandler {

        private LazyLoader lazyloader;
        private List<String> alreadyLoaded = new ArrayList<String>();
        private Entity entity;

        public LazyMethodHandler(Entity entity, LazyLoader lazyloader) {
            this.entity = entity;
            this.lazyloader = lazyloader;
        }

        @Override
        public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
            Object invocationResult = thisMethod.invoke(entity, args);

            if (isGetterOrSetter(thisMethod)) {
                if (shouldBeLoaded(thisMethod, invocationResult)) {
                    invocationResult = lazyloader.load(thisMethod, entity.getPersistenceId());
                }
                alreadyLoaded.add(toFieldName(thisMethod.getName()));
            }

            return proxifyIfNeeded(invocationResult);
        }

        @SuppressWarnings("unchecked")
        private Object proxifyIfNeeded(Object invocationResult) {
            if (isAnEntity(invocationResult)) {
                return proxifyEntity((Entity) invocationResult);
            }

            if (isAListOfEntities(invocationResult)) {
                return proxifyEntities((List<Entity>) invocationResult);
            }
            return invocationResult;
        }

        private boolean isAListOfEntities(Object invocationResult) {
            if (invocationResult instanceof List) {
                List<?> list = (List<?>) invocationResult;
                if (!list.isEmpty() && list.get(0) instanceof Entity) {
                    return true;
                }
            }
            return false;
        }

        private boolean isAnEntity(Object invocationResult) {
            return invocationResult instanceof Entity;
        }

        private boolean shouldBeLoaded(Method thisMethod, Object notLazyLoaded) {
            return notLazyLoaded == null && !alreadyLoaded.contains(toFieldName(thisMethod.getName())) && thisMethod.getAnnotation(LazyLoaded.class) != null;
        }

        private boolean isGetterOrSetter(Method method) {
            return method.getName().startsWith("get") || method.getName().startsWith("set") && method.getName().length() > 3;
        }

        private String toFieldName(final String methodName) {
            if (methodName.startsWith("get") || methodName.startsWith("set") && methodName.length() > 3) {
                return methodName.substring(3).toLowerCase();
            }
            throw new IllegalArgumentException(methodName + " is not a valid getter or setter name.");
        }
    }

    /**
     * Filter all methods
     */
    private class AllMethodFilter implements MethodFilter {

        @Override
        public boolean isHandled(Method m) {
            return true;
        }

    }
}
