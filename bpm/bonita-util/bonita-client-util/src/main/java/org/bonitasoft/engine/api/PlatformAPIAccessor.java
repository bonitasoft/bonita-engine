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
package org.bonitasoft.engine.api;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.bonitasoft.engine.api.impl.ClientInterceptor;
import org.bonitasoft.engine.api.impl.LocalServerAPIFactory;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.util.APITypeManager;

/**
 * <b>Accessor class that retrieve Platform APIs</b>
 * <p>
 * All APIs given by this class are relevant to the platform only.
 * <ul>
 * <li>{@link PlatformAPI}</li>
 * <li>{@link PlatformCommandAPI}</li>
 * <li>{@link PlatformLoginAPI}</li>
 * </ul>
 *
 * @author Matthieu Chaffotte
 */
public class PlatformAPIAccessor {

    private static ServerAPI getServerAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        try {
            final ApiAccessType apiType = APITypeManager.getAPIType();
            Map<String, String> parameters = null;
            switch (apiType) {
                case LOCAL:
                    return LocalServerAPIFactory.getServerAPI();
                case EJB3:
                    parameters = APITypeManager.getAPITypeParameters();
                    return new EJB3ServerAPI(parameters);
                case HTTP:
                    parameters = APITypeManager.getAPITypeParameters();
                    return new HTTPServerAPI(parameters);
                case TCP:
                    parameters = APITypeManager.getAPITypeParameters();
                    return new TCPServerAPI(parameters);
                default:
                    throw new UnknownAPITypeException("Unsupported API Type: " + apiType);
            }
        } catch (IOException e) {
            throw new ServerAPIException(e);
        }
    }

    /**
     * Reload the configuration of the Bonita home from the file system
     * It allows to change in runtime the Bonita engine your client application uses
     */
    public static void refresh() {
        APITypeManager.refresh();
    }

    /**
     * @return the {@link PlatformLoginAPI}
     * @throws BonitaHomeNotSetException
     * @throws ServerAPIException
     * @throws UnknownAPITypeException
     */
    public static PlatformLoginAPI getPlatformLoginAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(PlatformLoginAPI.class);
    }

    private static <T> T getAPI(final Class<T> clazz, final PlatformSession session) throws BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        final ServerAPI serverAPI = getServerAPI();
        final ClientInterceptor sessionInterceptor = new ClientInterceptor(clazz.getName(), serverAPI, session);
        return (T) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] { clazz }, sessionInterceptor);
    }

    private static <T> T getAPI(final Class<T> clazz) throws BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        final ServerAPI serverAPI = getServerAPI();
        final ClientInterceptor sessionInterceptor = new ClientInterceptor(clazz.getName(), serverAPI);
        return (T) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] { clazz }, sessionInterceptor);
    }

    /**
     * @param session
     *        a {@link PlatformSession} created using the {@link PlatformLoginAPI}
     * @return
     *         the {@link PlatformAPI}
     * @throws InvalidSessionException
     * @throws BonitaHomeNotSetException
     * @throws ServerAPIException
     * @throws UnknownAPITypeException
     */
    public static PlatformAPI getPlatformAPI(final PlatformSession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(PlatformAPI.class, session);
    }

    /**
     * @param session
     *        a {@link PlatformSession} created using the {@link PlatformLoginAPI}
     * @return
     *         the {@link PlatformCommandAPI}
     * @throws BonitaHomeNotSetException
     * @throws ServerAPIException
     * @throws UnknownAPITypeException
     * @throws InvalidSessionException
     */
    public static PlatformCommandAPI getPlatformCommandAPI(final PlatformSession session) throws BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        return getAPI(PlatformCommandAPI.class, session);
    }

}
