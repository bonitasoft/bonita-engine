package com.bonitasoft.engine.api;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.platform.LoginException;

/**
 * Bonita BPM <b>Subscription</b> Edition API's client.<br>
 * Be aware that if you use {@link org.bonitasoft.engine.api.APIClient} instead, you will have only access to <b>Community</b> Edition
 * APIs, which give access to less features.
 * <ul>
 * <li>{@link IdentityAPI},</li>
 * <li>{@link ProcessAPI},</li>
 * <li>{@link MonitoringAPI},</li>
 * <li>{@link LogAPI},</li>
 * <li>{@link ThemeAPI}</li>
 * <li>{@link ProfileAPI},</li>
 * <li>{@link ReportingAPI},</li>
 * </ul>
 *
 * @author Nicolas Chabanoles
 */
public class APIClient extends org.bonitasoft.engine.api.APIClient {

    protected LoginAPI getLoginAPI() {
        return getLoginAPI(LoginAPI.class);
    }

    /**
     * Connects a user, identified by his (her) username and password, in order to use API methods of a tenant.
     *
     * @param tenantId
     *        the tenant identifier
     * @param username
     *        the user name
     * @param password
     *        the password
     * @throws LoginException
     *         occurs when an exception is thrown during the login (userName does not exist, or couple (userName, password) is incorrect)
     * @since 7.2
     */
    public void login(long tenantId, String username, String password) throws LoginException {
        session = getLoginAPI().login(tenantId, username, password);
    }

    /**
     * Connects a user, identified by credentials, in order to use API methods of a tenant.
     *
     * @param tenantId
     *        the tenant identifier
     * @param credentials
     *        the credentials to login with. Can be username / password couple, SSO ticket, ... depending on the implementation.<br>
     *            By default possible map keys can be:
     *            <ul>
     *                <li>Basic Authentication: authentication.username and authentication.password</li>
     *                <li>CAS Authentication: ticket and service</li>
     *                <li>Please refer to specific documentation regarding the Authentication Service in use, to know what the credentials must contain.</li>
     *            </ul>
     * @throws LoginException
     *         occurs when an exception is thrown during the login (userName does not exist, or couple (userName, password) is incorrect)
     * @since 7.2
     */
    public void login(long tenantId, Map<String, Serializable> credentials) throws LoginException {
        session = getLoginAPI().login(tenantId, credentials);
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
     * Get API to monitor tenant resources consumption.
     *
     * @since 7.2
     */
    public MonitoringAPI getMonitoringAPI() {
        return getAPI(MonitoringAPI.class);
    }

    /**
     * Get API to read logs stored in database to track actions made by users.
     *
     * @since 7.2
     */
    public LogAPI getLogAPI() {
        return getAPI(LogAPI.class);
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
     * Get API to manage portal user profiles.
     *
     * @since 7.2
     */
    public ProfileAPI getProfileAPI() {
        return getAPI(ProfileAPI.class);
    }

    /**
     * Get API to manage reporting capabilities.
     *
     * @since 7.2
     */
    public ReportingAPI getReportingAPI() {
        return getAPI(ReportingAPI.class);
    }

}
