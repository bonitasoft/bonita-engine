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
package org.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.home.BonitaHomeServer;

/**
 * @author Emmanuel Duchastenier
 */
public class ServerAPIFactory {

    private static final String SERVER_API_CLASS_NOT_FOUND = "Cannot load class %s. Platform property 'serverApi' may not be set.";

    private static ServerAPIFactory INSTANCE = new ServerAPIFactory(BonitaHomeServer.getInstance());

    private final BonitaHomeServer bonitaHomeServer;
    private Class<?> serverApiClass;

    ServerAPIFactory(final BonitaHomeServer bonitaHomeServer) {
        this.bonitaHomeServer = bonitaHomeServer;
    }

    public static ServerAPI getServerAPI() {
        return ServerAPIFactory.getInstance().getServerAPIImplementation();
    }

    public static ServerAPI getServerAPI(final boolean cleanSession) {
        return ServerAPIFactory.getInstance().getServerAPIImplementation(cleanSession);
    }

    ServerAPI getServerAPIImplementation() {
        Class<?> aClass = getServerAPIClass();
        try {
            return (ServerAPI) aClass.newInstance();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(String.format(SERVER_API_CLASS_NOT_FOUND, aClass.getName()));
        }
    }

    private Class<?> getServerAPIClass() {
        if (serverApiClass == null) {
            String serverAPIClassName = bonitaHomeServer.getServerAPIImplementation();
            try {
                serverApiClass = Class.forName(serverAPIClassName);
            } catch (Exception e) {
                throw new ExceptionInInitializerError(String.format(SERVER_API_CLASS_NOT_FOUND, serverAPIClassName));
            }
        }
        return serverApiClass;
    }

    private ServerAPI getServerAPIImplementation(final boolean cleanSession) {
        Class<?> serverApiClass = getServerAPIClass();
        try {
            return (ServerAPI) serverApiClass.getConstructor(boolean.class).newInstance(cleanSession);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(String.format(SERVER_API_CLASS_NOT_FOUND, serverApiClass.getName()));
        }
    }

    public static ServerAPIFactory getInstance() {
        return INSTANCE;
    }
}
