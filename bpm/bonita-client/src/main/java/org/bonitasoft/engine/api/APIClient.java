/*
 * Copyright (C) 2015 Bonitasoft S.A.
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
 */
package org.bonitasoft.engine.api;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.bonitasoft.engine.api.impl.ClientInterceptor;
import org.bonitasoft.engine.api.impl.LocalServerAPIFactory;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.bdm.BusinessObjectDaoCreationException;
import org.bonitasoft.engine.bdm.dao.BusinessObjectDAO;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.platform.LogoutException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.SessionNotFoundException;
import org.bonitasoft.engine.util.APITypeManager;

/**
 * Bonita <b>Community</b> Edition APIs client.<br>
 * <p/>
 * <ul>
 * <li>{@link IdentityAPI},</li>
 * <li>{@link ProcessAPI},</li>
 * <li>{@link ThemeAPI}</li>
 * <li>{@link CommandAPI},</li>
 * <li>{@link ProfileAPI},</li>
 * <li>{@link TenantAdministrationAPI},</li>
 * <li>{@link PageAPI},</li>
 * <li>{@link ApplicationAPI},</li>
 * <li>{@link PermissionAPI},</li>
 * <li>{@link BusinessDataAPI} (deprecated as of 7.3),</li>
 * </ul>
 *
 * @author Nicolas Chabanoles
 */
public class APIClient {

    private static final String IMPL_SUFFIX = "Impl";

    protected APISession session;

    public APIClient() {
        session = null;
    }

    public APIClient(APISession session) {
        this.session = session;
    }

    public APISession getSession() {
        return session;
    }

    ServerAPI getServerAPI() throws ServerAPIException, UnknownAPITypeException {
        try {
            final ApiAccessType apiType = APITypeManager.getAPIType();
            Map<String, String> parameters;
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

    protected <T> T getAPI(final Class<T> apiClass) {
        ensureSessionExists();
        try {
            final ClientInterceptor clientInterceptor = new ClientInterceptor(apiClass.getName(), getServerAPI(), session);
            @SuppressWarnings("unchecked")
            final T api = (T) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] { apiClass }, clientInterceptor);
            return api;
        } catch (ServerAPIException | UnknownAPITypeException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private void ensureSessionExists() {
        if (session == null) {
            throw new IllegalStateException("You must call login() prior to accessing any API.");
        }
    }

    protected LoginAPI getLoginAPI() {
        return getLoginAPI(LoginAPI.class);
    }

    /**
     * This methods serves the purpose to remove confusion between getAPI() when a session is mandatory, and this one, where no session is needed to access the
     * API class.
     * 
     * @param apiClass the API to retrieve
     * @param <T> The type of the API, extending {@link org.bonitasoft.engine.api.LoginAPI}
     * @return the retrieved API
     * @throws IllegalStateException if the API cannot be retrieved.
     */
    protected <T extends LoginAPI> T getLoginAPI(Class<T> apiClass) {
        try {
            final ClientInterceptor interceptor = new ClientInterceptor(apiClass.getName(), getServerAPI());
            @SuppressWarnings("unchecked")
            final T api = (T) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] { apiClass }, interceptor);
            return api;
        } catch (ServerAPIException | UnknownAPITypeException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Connects a user, identified by his (her) username and password, in order to use API methods of a tenant.
     *
     * @param username
     *        the user name
     * @param password
     *        the password
     * @throws LoginException
     *         occurs when an exception is thrown during the login (userName does not exist, or couple (userName, password) is incorrect)
     * @since 7.2
     */
    public void login(String username, String password) throws LoginException {
        session = getLoginAPI(LoginAPI.class).login(username, password);
    }

    /**
     * Disconnect user from tenant APIs.
     *
     * @since 7.2
     */
    public void logout() throws LogoutException {
        try {
            if (session != null) {
                getLoginAPI().logout(session);
                session = null;
            }
        } catch (SessionNotFoundException ignored) {
            // If the session is not found on server, then the client is already logged out.
            // Do nothing
        }
    }

    /**
     * Get an implementation instance of the DAO Interface.
     *
     * @param daoInterface the interface of the DAO
     * @return the implementation of the DAO
     * @throws BusinessObjectDaoCreationException if the factory is not able to instantiate the DAO
     */
    public <T extends BusinessObjectDAO> T getDAO(final Class<T> daoInterface) throws BusinessObjectDaoCreationException {
        ensureSessionExists();
        if (daoInterface == null) {
            throw new IllegalArgumentException("daoInterface is null");
        }
        if (!daoInterface.isInterface()) {
            throw new IllegalArgumentException(daoInterface.getName() + " is not an interface");
        }

        Class<T> daoImplClass;
        try {
            daoImplClass = loadClass(daoInterface);

            if (daoImplClass != null) {
                final Constructor<T> constructor = daoImplClass.getConstructor(APISession.class);
                return constructor.newInstance(session);
            }
        } catch (final ClassNotFoundException | SecurityException | NoSuchMethodException | IllegalArgumentException | InstantiationException
                | IllegalAccessException | InvocationTargetException e) {
            throw new BusinessObjectDaoCreationException(e);
        }
        throw new BusinessObjectDaoCreationException("No Implementation of the DAO available.");
    }

    /**
     * Loads the class of the {@link BusinessObjectDAO} according to its class name.
     * <p>
     * The loading is done in the current Thread ClassLoader.
     *
     * @param daoInterface the DAO's interface
     * @return the Implementation class of the BusinessObjectDAO
     * @throws ClassNotFoundException if the implementation class name is unknown by the current Thread ClassLoader
     */
    @SuppressWarnings("unchecked")
    protected <T extends BusinessObjectDAO> Class<T> loadClass(final Class<T> daoInterface) throws ClassNotFoundException {
        final String implementationClassName = daoInterface.getName() + IMPL_SUFFIX;
        return (Class<T>) Class.forName(implementationClassName, true, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Get API to manage the organization, i.e., users, groups and roles.
     *
     * @since 7.2
     */
    public IdentityAPI getIdentityAPI() {
        return getAPI(IdentityAPI.class);
    }

    /**
     * Get API to manage the business processes.
     *
     * @since 7.2
     */
    public ProcessAPI getProcessAPI() {
        return getAPI(ProcessAPI.class);
    }

    /**
     * Get API to manage themes (Portal and mobile).
     *
     * @since 7.2
     */
    public ThemeAPI getThemeAPI() {
        return getAPI(ThemeAPI.class);
    }

    /**
     * Get API to manage commands and Tenant level dependencies.
     *
     * @since 7.2
     */
    public CommandAPI getCommandAPI() {
        return getAPI(CommandAPI.class);
    }

    /**
     * Get API to manage portal user profiles.
     *
     * @since 7.2
     */
    public ProfileAPI getProfileAPI() {
        return getAPI(ProfileAPI.class);
    }

    /**
     * Get API to manage the tenant your are logged on.
     *
     * @since 7.2
     */
    public TenantAdministrationAPI getTenantAdministrationAPI() {
        return getAPI(TenantAdministrationAPI.class);
    }

    /**
     * Get API to manage portal pages.
     *
     * @since 7.2
     */
    public PageAPI getCustomPageAPI() {
        return getAPI(PageAPI.class);
    }

    /**
     * Get API to manage Living Applications.
     *
     * @since 7.2
     */
    public ApplicationAPI getLivingApplicationAPI() {
        return getAPI(ApplicationAPI.class);
    }

    /**
     * Get API to dynamically check REST API call access right.
     *
     * @since 7.2
     */
    public PermissionAPI getPermissionAPI() {
        return getAPI(PermissionAPI.class);
    }

    /**
     * Get API to access Business Data related to processes.
     *
     * @since 7.2
     * @deprecated as of 7.3, see {@link BusinessDataAPI} for replacements
     */
    @Deprecated
    public BusinessDataAPI getBusinessDataAPI() {
        return getAPI(BusinessDataAPI.class);
    }

}
