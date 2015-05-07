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

package org.bonitasoft.engine.business.data.proxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.bdm.lazy.LazyLoaded;

/**
 * @author Colin Puy
 * @author Laurent Leseigneur
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
        final List<Entity> proxies = new ArrayList<>();
        for (final Entity entity : entities) {
            proxies.add(proxifyEntity(entity));
        }
        return proxies;
    }

    /**
     * Handler that lazy load values for lazy loading methods that hasn't been loaded
     */
    public class LazyMethodHandler implements MethodHandler {

        private final ServerLazyLoader lazyLoader;
        private final Entity entity;

        public LazyMethodHandler(final Entity entity, final ServerLazyLoader lazyLoader) {
            this.entity = entity;
            this.lazyLoader = lazyLoader;
        }

        public Entity getEntity() {
            return entity;
        }

        @Override
        public Object invoke(final Object self, final Method thisMethod, final Method proceed, final Object[] args) throws Throwable {
            Object invocationResult;
            if (isMethodGetterOnLazyLoadedField(thisMethod)) {
                invocationResult = lazyLoader.load(thisMethod, entity.getPersistenceId());
            } else {
                invocationResult = thisMethod.invoke(entity, args);
            }
            return proxifyIfNeeded(invocationResult);
        }

        private boolean isMethodGetterOnLazyLoadedField(final Method thisMethod) {
            return isGetter(thisMethod) && thisMethod.isAnnotationPresent(LazyLoaded.class);
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

        private boolean isGetter(final Method method) {
            return method.getName().startsWith("get");
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
        if (entity != null && isLazyMethodProxyfied(entity)) {
            final LazyMethodHandler handler = (LazyMethodHandler) ProxyFactory.getHandler((Proxy) entity);
            return handler.getEntity();
        }
        return entity;
    }

    public static Entity unProxy(final Entity entity) throws IllegalArgumentException, IllegalAccessException {
        final Entity realEntity = unProxyfyIfNeeded(entity);
        if (entity != null) {
            final Field[] declaredFields = realEntity.getClass().getDeclaredFields();
            for (final Field field : declaredFields) {
                if (Entity.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    field.set(entity, unProxyfyIfNeeded((Entity) field.get(entity)));
                } else if (List.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    final List list = (List) field.get(entity);
                    if (list != null && !list.isEmpty() && Entity.class.isAssignableFrom(list.get(0).getClass())) {
                        final List<Entity> entities = list;
                        final List<Entity> realEntities = new ArrayList<>();
                        for (final Entity e : entities) {
                            realEntities.add(unProxyfyIfNeeded(e));
                        }
                        field.set(entity, realEntities);
                    }
                }
            }
        }
        return realEntity;
    }

    /**
     * Retrieves the real class for the given entity. This result will be same as {@code entity.getClass()} if the entity is not a proxy.
     * @param entity
     * @return
     */
    public static Class<? extends Entity> getRealClass(final Entity entity) {
        return unProxyfyIfNeeded(entity).getClass();
    }

}
