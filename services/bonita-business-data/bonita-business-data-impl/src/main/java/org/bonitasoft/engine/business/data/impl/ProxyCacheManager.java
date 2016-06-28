/**
 * Copyright (C) 2016 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.business.data.impl;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.WeakHashMap;

import javassist.util.proxy.ProxyFactory;

public class ProxyCacheManager {

    static void setAccessible(final AccessibleObject ao,
            final boolean accessible) {
        if (System.getSecurityManager() == null)
            ao.setAccessible(accessible);
        else {
            AccessController.doPrivileged(new PrivilegedAction() {

                @Override
                public Object run() {
                    ao.setAccessible(accessible);
                    return null;
                }
            });
        }
    }

    static Field getDeclaredField(final Class clazz, final String name) throws NoSuchFieldException {
        if (System.getSecurityManager() == null)
            return clazz.getDeclaredField(name);
        else {
            try {
                return (Field) AccessController
                        .doPrivileged(new PrivilegedExceptionAction() {

                            @Override
                            public Object run() throws Exception {
                                return clazz.getDeclaredField(name);
                            }
                        });
            } catch (final PrivilegedActionException e) {
                if (e.getCause() instanceof NoSuchFieldException)
                    throw (NoSuchFieldException) e.getCause();

                throw new RuntimeException(e.getCause());
            }
        }
    }

    static Object get(final Field fld, final Object target)
            throws IllegalAccessException {
        if (System.getSecurityManager() == null)
            return fld.get(target);
        else {
            try {
                return AccessController.doPrivileged(new PrivilegedExceptionAction() {

                    @Override
                    public Object run() throws Exception {
                        return fld.get(target);
                    }
                });
            } catch (final PrivilegedActionException e) {
                if (e.getCause() instanceof NoSuchMethodException)
                    throw (IllegalAccessException) e.getCause();

                throw new RuntimeException(e.getCause());
            }
        }
    }

    public WeakHashMap/* <Classloader,HashMap<String,ProxyDetails>> */ get() throws NoSuchFieldException, IllegalAccessException {
        final Field proxyCacheField = getDeclaredField(ProxyFactory.class, "proxyCache");
        setAccessible(proxyCacheField, true);
        return (WeakHashMap) get(proxyCacheField, null);
    }

    public void clearCache() throws NoSuchFieldException, IllegalAccessException {
        final WeakHashMap cache = get();
        if (cache != null) {
            cache.clear();
        }
    }

}
