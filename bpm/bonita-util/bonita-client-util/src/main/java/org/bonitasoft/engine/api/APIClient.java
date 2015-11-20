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
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.platform.LogoutException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.SessionNotFoundException;
import org.bonitasoft.engine.util.APITypeManager;

/**
 * Bonita BPM <b>Community</b> Edition API's client.<br>
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
 * <li>{@link BusinessDataAPI},</li>
 * </ul>
 *
 * @author Nicolas Chabanoles
 */
public class APIClient {

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

    protected ServerAPI getServerAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        final ApiAccessType apiType = getApiAccessType();
        Map<String, String> parameters = null;
        switch (apiType) {
            case LOCAL:
                return LocalServerAPIFactory.getServerAPI();
            case EJB3:
                parameters = getAPITypeParameters();
                return new EJB3ServerAPI(parameters);
            case HTTP:
                parameters = getAPITypeParameters();
                return new HTTPServerAPI(parameters);
            default:
                throw new UnknownAPITypeException("Unsupported API Type: " + apiType);
        }
    }

    Map<String, String> getAPITypeParameters() throws BonitaHomeNotSetException, ServerAPIException {
        Map<String, String> parameters;
        try {
            parameters = APITypeManager.getAPITypeParameters();
        } catch (IOException e) {
            throw new ServerAPIException(e);
        }
        return parameters;
    }

    ApiAccessType getApiAccessType() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        ApiAccessType apiType;
        try {
            apiType = APITypeManager.getAPIType();
        } catch (IOException e) {
            throw new ServerAPIException(e);
        }
        return apiType;
    }

    protected <T> T getAPI(final Class<T> clazz) {
        if (session == null) {
            throw new IllegalStateException("You must call login prior to access any API.");
        }
        final ServerAPI serverAPI;
        try {
            serverAPI = getServerAPI();
            final ClientInterceptor clientInterceptor = new ClientInterceptor(clazz.getName(), serverAPI, session);
            return (T) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[]{clazz}, clientInterceptor);
        } catch (BonitaHomeNotSetException | ServerAPIException | UnknownAPITypeException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    protected LoginAPI getLoginAPI() {
        return getLoginAPI(LoginAPI.class);
    }

    protected <T extends LoginAPI> T getLoginAPI(Class<T> apiClass) {
        try {
            final ServerAPI serverAPI = getServerAPI();
            final ClientInterceptor interceptor = new ClientInterceptor(LoginAPI.class.getName(), serverAPI);
            T result = (T) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[]{apiClass}, interceptor);
            return result;
        } catch (BonitaHomeNotSetException | ServerAPIException | UnknownAPITypeException e) {
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
        } catch (SessionNotFoundException e) {
            // If the session is not found on server, then the client is already logged out.
            // Do nothing
        }
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
     */
    public BusinessDataAPI getBusinessDataAPI() {
        return getAPI(BusinessDataAPI.class);
    }

}
