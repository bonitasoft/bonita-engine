/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package org.bonitasoft.engine.api.impl.transaction.expression.bdm;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;
import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.bdm.lazy.LazyLoaded;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Colin Puy
 */
public class ServerProxyfier {

    private final ServerLazyLoader lazyLoader;

    public ServerProxyfier(final ServerLazyLoader lazyLoader) {
        this.lazyLoader = lazyLoader;
    }

    public static boolean isLazyMethodProxyfied(final Entity e) {
        return ProxyFactory.isProxyClass(e.getClass()) && ProxyFactory.getHandler((Proxy) e) instanceof LazyMethodHandler;
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> T proxify(final T entity) {
        if (isLazyMethodProxyfied(entity)) {
            return entity;
        }
        return (T) proxifyEntity(entity);
    }

    private Entity proxifyEntity(final Entity entity) {
        if (entity == null) {
            return null;
        }
        final ProxyFactory factory = new ProxyFactory();
        Class<?> classForProxy = entity.getClass();

        //It's not possible to create a Proxy on a Proxy
        //Here Entity can already be an Hibernate Proxy
        if (ProxyFactory.isProxyClass(classForProxy)) {
            classForProxy = classForProxy.getSuperclass();
        }
        factory.setSuperclass(classForProxy);
        factory.setFilter(new AllMethodFilter());
        try {
            return (Entity) factory.create(new Class<?>[0], new Object[0], new LazyMethodHandler(entity, lazyLoader));
        } catch (final Exception e) {
            throw new RuntimeException("Error when proxifying object", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Entity> List<T> proxify(final List<T> entities) {
        if (entities == null) {
            return null;
        }
        return (List<T>) proxifyEntities((List<Entity>) entities);
    }

    private List<Entity> proxifyEntities(final List<Entity> entities) {
        final List<Entity> proxies = new ArrayList<Entity>();
        for (final Entity entity : entities) {
            proxies.add(proxifyEntity(entity));
        }
        return proxies;
    }

    /**
     * Handler that lazy load values for lazy loading methods that hasn't been loaded
     */
    public class LazyMethodHandler implements MethodHandler {

        private final ServerLazyLoader lazyloader;
        private final List<String> alreadyLoaded = new ArrayList<String>();
        private final Entity entity;

        public LazyMethodHandler(final Entity entity, final ServerLazyLoader lazyloader) {
            this.entity = entity;
            this.lazyloader = lazyloader;
        }

        public Entity getEntity() {
            return entity;
        }

        @Override
        public Object invoke(final Object self, final Method thisMethod, final Method proceed, final Object[] args) throws Throwable {
            Object invocationResult = thisMethod.invoke(entity, args);

            if (isGetterOrSetter(thisMethod)) {
                if (isGetter(thisMethod) && shouldBeLoaded(thisMethod, invocationResult)) {
                    invocationResult = lazyloader.load(thisMethod, entity.getPersistenceId());
                    callSetterOnEntity(invocationResult, thisMethod);
                }
                alreadyLoaded.add(toFieldName(thisMethod.getName()));
            }

            return proxifyIfNeeded(invocationResult);
        }

        private void callSetterOnEntity(final Object invocationResult, final Method getter) throws NoSuchMethodException, SecurityException,
                IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            if (invocationResult != null) {
                final Method setter = getAssociatedSetter(invocationResult, getter);
                setter.invoke(entity, invocationResult);
            }
        }

        private Method getAssociatedSetter(final Object invocationResult, final Method getter) throws NoSuchMethodException, SecurityException {
            return entity.getClass().getMethod(getter.getName().replaceFirst("^get", "set"), getter.getReturnType());
        }

        @SuppressWarnings("unchecked")
        private Object proxifyIfNeeded(final Object invocationResult) {
            if (isAnEntity(invocationResult)) {
                return proxifyEntity((Entity) invocationResult);
            }

            if (isAListOfEntities(invocationResult)) {
                return proxifyEntities((List<Entity>) invocationResult);
            }
            return invocationResult;
        }

        private boolean isAListOfEntities(final Object invocationResult) {
            if (invocationResult instanceof List) {
                final List<?> list = (List<?>) invocationResult;
                if (!list.isEmpty() && list.get(0) instanceof Entity) {
                    return true;
                }
            }
            return false;
        }

        private boolean isAnEntity(final Object invocationResult) {
            return invocationResult instanceof Entity;
        }

        private boolean shouldBeLoaded(final Method thisMethod, final Object notLazyLoaded) {
            return thisMethod.isAnnotationPresent(LazyLoaded.class) && !alreadyLoaded.contains(toFieldName(thisMethod.getName()));
        }

        private boolean isGetterOrSetter(final Method method) {
            return isGetter(method) || method.getName().startsWith("set") && method.getName().length() > 3;
        }

        private boolean isGetter(final Method method) {
            return method.getName().startsWith("get");
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
        public boolean isHandled(final Method m) {
            return true;
        }

    }

    public static Entity unProxyfyIfNeeded(final Entity entity) {
        if (isLazyMethodProxyfied(entity)) {
            final LazyMethodHandler handler = (LazyMethodHandler) ProxyFactory.getHandler((Proxy) entity);
            return handler.getEntity();
        }
        return entity;
    }
}
