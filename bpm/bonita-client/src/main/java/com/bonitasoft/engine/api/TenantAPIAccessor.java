/*
 * Copyright (C) 2011-2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 */
package com.bonitasoft.engine.api;

import java.lang.reflect.Proxy;
import java.util.Map;

import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.EJB2ServerAPI;
import org.bonitasoft.engine.api.EJB3ServerAPI;
import org.bonitasoft.engine.api.EngineAPI;
import org.bonitasoft.engine.api.HTTPServerAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.MigrationAPI;
import org.bonitasoft.engine.api.MonitoringAPI;
import org.bonitasoft.engine.api.impl.ClientInterceptor;
import org.bonitasoft.engine.api.impl.ClientSessionInterceptor;
import org.bonitasoft.engine.api.impl.LocalServerAPIFactory;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnableToReadBonitaClientConfiguration;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.util.APITypeManager;

/**
 * @author Matthieu Chaffotte
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
            case EJB2:
                parameters = APITypeManager.getAPITypeParameters();
                return new EJB2ServerAPI(parameters);
            case HTTP:
                parameters = APITypeManager.getAPITypeParameters();
                return new HTTPServerAPI(parameters);
            default:
                throw new UnknownAPITypeException("Unsupported API Type: " + apiType);
        }
    }

    public static void refresh() {
        APITypeManager.refresh();
    }

    private static <T> T getAPI(final Class<T> clazz, final APISession session) throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        final ServerAPI serverAPI = getServerAPI();
        final ClientSessionInterceptor sessionInterceptor = new ClientSessionInterceptor(clazz.getName(), serverAPI, session);
        return (T) Proxy.newProxyInstance(EngineAPI.class.getClassLoader(), new Class[] { clazz }, sessionInterceptor);
    }

    public static LoginAPI getLoginAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        final ServerAPI serverAPI = getServerAPI();
        final ClientInterceptor interceptor = new ClientInterceptor(LoginAPI.class.getName(), serverAPI);
        return (LoginAPI) Proxy.newProxyInstance(EngineAPI.class.getClassLoader(), new Class[] { LoginAPI.class }, interceptor);
    }

    public static IdentityAPI getIdentityAPI(final APISession session) throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        return getAPI(IdentityAPI.class, session);
    }

    public static ProcessAPI getProcessAPI(final APISession session) throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        return getAPI(ProcessAPI.class, session);
    }

    public static MigrationAPI getMigrationAPI(final APISession session) throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        return getAPI(MigrationAPI.class, session);
    }

    public static MonitoringAPI getMonitoringAPI(final APISession session) throws BonitaHomeNotSetException, UnableToReadBonitaClientConfiguration,
            UnknownAPITypeException, ServerAPIException, InvalidSessionException {
        return getAPI(MonitoringAPI.class, session);
    }

    public static LogAPI getLogAPI(final APISession session) throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        return getAPI(LogAPI.class, session);
    }

    public static CommandAPI getCommandAPI(final APISession session) throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        return getAPI(CommandAPI.class, session);
    }

}
