/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.impl;

import static java.util.logging.Level.FINEST;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.session.Session;

/**
 * API classes given to the client are proxies. That class is the {@link InvocationHandler} given when proxying the
 * client api classes
 * Its purpose is to keep the client session and give it to the {@link ServerAPI} call
 */
public class ClientInterceptor implements InvocationHandler, Serializable {

    private static final long serialVersionUID = -6284726148297940515L;

    private final ServerAPI api;

    private final String interfaceName;

    private final Session session;

    private static final Logger LOGGER = Logger.getLogger(ClientInterceptor.class.getName());

    public ClientInterceptor(final String interfaceName, final ServerAPI api, final Session session) {
        this.api = api;
        this.interfaceName = interfaceName;
        this.session = session;
    }

    public ClientInterceptor(final String interfaceName, final ServerAPI api) {
        this(interfaceName, api, null);
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        try {
            final Class<?>[] parameterTypes = method.getParameterTypes();
            final List<String> classNameParameters = new ArrayList<>();
            for (final Class<?> parameterType : parameterTypes) {
                classNameParameters.add(parameterType.getName());
            }
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.log(FINEST, "Calling method " + method.getName() + " on API " + this.api.getClass().getName());
            }
            Map<String, Serializable> options;
            options = new HashMap<>();
            options.put("session", this.session);
            // invoke ServerAPI unique method
            final Object object = this.api.invokeMethod(options, this.interfaceName, method.getName(),
                    classNameParameters, args);
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.log(FINEST, "Quitting method " + method.getName() + " on API " + this.api.getClass().getName());
            }
            return object;
        } catch (final ServerWrappedException | RemoteException e) {
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.log(FINEST, "Quitting method " + method.getName() + " on API " + this.api.getClass().getName()
                        + " with exception " + e.getMessage());
            }
            throw e.getCause();
        }
    }
}
