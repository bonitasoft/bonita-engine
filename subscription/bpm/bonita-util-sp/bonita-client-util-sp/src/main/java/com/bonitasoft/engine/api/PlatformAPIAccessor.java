/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.api.EJB3ServerAPI;
import org.bonitasoft.engine.api.HTTPServerAPI;
import org.bonitasoft.engine.api.PlatformCommandAPI;
import org.bonitasoft.engine.api.PlatformLoginAPI;
import org.bonitasoft.engine.api.impl.ClientInterceptor;
import org.bonitasoft.engine.api.impl.LocalServerAPIFactory;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.util.APITypeManager;

/**
 * <b>Accessor class that retrieve Platform APIs</b>
 * <p>
 * All APIs given by this class are relevant to the platform only.
 * <ul>
 * <li>{@link PlatformAPI}</li>
 * <li>{@link PlatformCommandAPI}</li>
 * <li>{@link PlatformMonitoringAPI}</li>
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
            default:
                throw new UnknownAPITypeException("Unsupported API Type: " + apiType);
        }
        } catch (IOException e) {
            throw new ServerAPIException(e);
        }
    }

    public static void refresh() {
        APITypeManager.refresh();
    }

    private static <T> T getAPI(final Class<T> clazz, final PlatformSession session) throws BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        final ServerAPI serverAPI = getServerAPI();
        final ClientInterceptor clientInterceptor = new ClientInterceptor(clazz.getName(), serverAPI, session);
        return (T) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] { clazz }, clientInterceptor);
    }

    public static PlatformLoginAPI getPlatformLoginAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        final ServerAPI serverAPI = getServerAPI();
        final ClientInterceptor interceptor = new ClientInterceptor(PlatformLoginAPI.class.getName(), serverAPI);
        return (PlatformLoginAPI) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] { PlatformLoginAPI.class }, interceptor);
    }

    public static PlatformAPI getPlatformAPI(final PlatformSession session) throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return getAPI(PlatformAPI.class, session);
    }

    public static PlatformMonitoringAPI getPlatformMonitoringAPI(final PlatformSession session) throws BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        return getAPI(PlatformMonitoringAPI.class, session);
    }

    public static PlatformCommandAPI getPlatformCommandAPI(final PlatformSession session) throws BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        return getAPI(PlatformCommandAPI.class, session);
    }

    public static NodeAPI getNodeAPI() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        final ServerAPI serverAPI = getServerAPI();
        final ClientInterceptor interceptor = new ClientInterceptor(NodeAPI.class.getName(), serverAPI);
        return (NodeAPI) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] { NodeAPI.class }, interceptor);
    }

}
