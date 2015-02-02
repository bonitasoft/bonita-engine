/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import java.lang.reflect.Proxy;
import java.util.Map;

import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.EJB3ServerAPI;
import org.bonitasoft.engine.api.HTTPServerAPI;
import org.bonitasoft.engine.api.impl.ClientInterceptor;
import org.bonitasoft.engine.api.impl.LocalServerAPIFactory;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.util.APITypeManager;

/**
 * Accessor class that retrieve APIs in Bonita BPM <b>Subscription</b> Edition.<br/>
 * Be aware that if you accidently use {@link org.bonitasoft.engine.api.TenantAPIAccessor} instead, you will have only access to <b>Community</b> Edition
 * APIs, which give access to less functionalities.
 * <ul>
 * <li>{@link ProcessAPI},</li>
 * <li>{@link CommandAPI},</li>
 * <li>{@link IdentityAPI},</li>
 * <li>{@link MonitoringAPI},</li>
 * <li>{@link LoginAPI},</li>
 * <li>{@link ProfileAPI},</li>
 * <li>{@link ReportingAPI},</li>
 * <li>{@link LogAPI},</li>
 * <li>{@link ThemeAPI}</li>
 * <li>{@link PageAPI}</li>
 * <li>{@link ApplicationAPI}</li>
 * </ul>
 *
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public final class TenantAPIAccessor {

    private static ServerAPI getServerAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
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
        final ClientInterceptor clientInterceptor = new ClientInterceptor(clazz.getName(), serverAPI, session);
        return (T) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] { clazz }, clientInterceptor);
    }

    public static LoginAPI getLoginAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        final ServerAPI serverAPI = getServerAPI();
        final ClientInterceptor interceptor = new ClientInterceptor(LoginAPI.class.getName(), serverAPI);
        return (LoginAPI) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] { LoginAPI.class }, interceptor);
    }

    public static IdentityAPI getIdentityAPI(final APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(IdentityAPI.class, session);
    }

    public static ProcessAPI getProcessAPI(final APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(ProcessAPI.class, session);
    }

    public static MonitoringAPI getMonitoringAPI(final APISession session) throws BonitaHomeNotSetException, UnknownAPITypeException, ServerAPIException {
        return getAPI(MonitoringAPI.class, session);
    }

    public static PlatformMonitoringAPI getPlatformMonitoringAPI(final APISession session) throws BonitaHomeNotSetException, UnknownAPITypeException,
            ServerAPIException {
        return getAPI(PlatformMonitoringAPI.class, session);
    }

    public static LogAPI getLogAPI(final APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(LogAPI.class, session);
    }

    public static ThemeAPI getThemeAPI(final APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(ThemeAPI.class, session);
    }

    public static CommandAPI getCommandAPI(final APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(CommandAPI.class, session);
    }

    public static ProfileAPI getProfileAPI(final APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(ProfileAPI.class, session);
    }

    public static ReportingAPI getReportingAPI(final APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(ReportingAPI.class, session);
    }

    public static TenantManagementAPI getTenantManagementAPI(final APISession session) throws BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        return getAPI(TenantManagementAPI.class, session);
    }

    public static org.bonitasoft.engine.api.PageAPI getCustomPageAPI(final APISession session) throws BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        return getAPI(org.bonitasoft.engine.api.PageAPI.class, session);
    }

    /**
     * @deprecated from version 7.0 on, use {@link #getCustomPageAPI(APISession)} instead.
     */
    @Deprecated
    public static PageAPI getPageAPI(final APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(PageAPI.class, session);
    }

    public static org.bonitasoft.engine.api.ApplicationAPI getLivingApplicationAPI(final APISession session) throws BonitaHomeNotSetException,
            ServerAPIException, UnknownAPITypeException {
        return getAPI(org.bonitasoft.engine.api.ApplicationAPI.class, session);
    }

    /**
     * @deprecated from version 7.0 on, use {@link #getLivingApplicationAPI(APISession)} instead.
     */
    @Deprecated
    public static ApplicationAPI getApplicationAPI(final APISession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(ApplicationAPI.class, session);
    }
}
