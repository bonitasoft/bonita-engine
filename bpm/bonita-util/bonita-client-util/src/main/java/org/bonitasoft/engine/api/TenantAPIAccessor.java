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
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.util.APITypeManager;

/**
 * Accessor class that retrieve APIs in Bonita BPM <b>Community</b> Edition.
 * <ul>
 * <li>{@link ProcessAPI},</li>
 * <li>{@link CommandAPI},</li>
 * <li>{@link IdentityAPI},</li>
 * <li>{@link LoginAPI}</li>
 * </ul>
 *
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public final class TenantAPIAccessor {

    private static ServerAPI getServerAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        final ApiAccessType apiType;
        try {
            apiType = APITypeManager.getAPIType();
        } catch (IOException e) {
            throw new ServerAPIException(e);
        }
        Map<String, String> parameters = null;
        switch (apiType) {
            case LOCAL:
                return LocalServerAPIFactory.getServerAPI();
            case EJB3:
                try {
                    parameters = APITypeManager.getAPITypeParameters();
                } catch (IOException e) {
                    throw new ServerAPIException(e);
                }
                return new EJB3ServerAPI(parameters);
            case HTTP:
                try {
                    parameters = APITypeManager.getAPITypeParameters();
                } catch (IOException e) {
                    throw new ServerAPIException(e);
                }
                return new HTTPServerAPI(parameters);
            case TCP:
                try {
                    parameters = APITypeManager.getAPITypeParameters();
                } catch (IOException e) {
                    throw new ServerAPIException(e);
                }
                return new TCPServerAPI(parameters);
            default:
                throw new UnknownAPITypeException("Unsupported API Type: " + apiType);
        }
    }

    /**
     * Refreshes the way the engine client communicates to the engine server.
     *
     * @see APITypeManager
     * @see ApiAccessType
     */
    public static void refresh() {
        APITypeManager.refresh();
    }

    private static <T> T getAPI(final Class<T> clazz, final APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        final ServerAPI serverAPI = getServerAPI();
        final ClientInterceptor sessionInterceptor = new ClientInterceptor(clazz.getName(), serverAPI, session);
        return (T) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] { clazz }, sessionInterceptor);
    }

    private static <T> T getAPI(final Class<T> clazz) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        final ServerAPI serverAPI = getServerAPI();
        final ClientInterceptor sessionInterceptor = new ClientInterceptor(clazz.getName(), serverAPI);
        return (T) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] { clazz }, sessionInterceptor);
    }

    public static LoginAPI getLoginAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(LoginAPI.class);
    }

    public static IdentityAPI getIdentityAPI(final APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(IdentityAPI.class, session);
    }

    public static ProcessAPI getProcessAPI(final APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(ProcessAPI.class, session);
    }

    public static CommandAPI getCommandAPI(final APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(CommandAPI.class, session);
    }

    public static ProfileAPI getProfileAPI(final APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(ProfileAPI.class, session);
    }

    public static ThemeAPI getThemeAPI(final APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(ThemeAPI.class, session);
    }

    public static PermissionAPI getPermissionAPI(final APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(PermissionAPI.class, session);
    }

    public static PageAPI getCustomPageAPI(final APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(PageAPI.class, session);
    }

    public static ApplicationAPI getLivingApplicationAPI(final APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(ApplicationAPI.class, session);
    }

    public static ProcessConfigurationAPI getProcessConfigurationAPI(APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(ProcessConfigurationAPI.class, session);
    }

    public static TenantAdministrationAPI getTenantAdministrationAPI(final APISession session) throws BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        return getAPI(TenantAdministrationAPI.class, session);
    }

    public static BusinessDataAPI getBusinessDataAPI(APISession session) throws BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        return getAPI(BusinessDataAPI.class, session);
    }

}
