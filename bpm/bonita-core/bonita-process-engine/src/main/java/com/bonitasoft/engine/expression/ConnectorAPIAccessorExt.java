/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.expression;

import java.lang.reflect.Proxy;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.impl.ClientInterceptor;
import org.bonitasoft.engine.api.impl.ServerAPIFactory;
import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.connector.ConnectorAPIAccessorImpl;
import org.bonitasoft.engine.session.APISession;

import com.bonitasoft.engine.api.APIAccessor;
import com.bonitasoft.engine.api.ApplicationAPI;
import com.bonitasoft.engine.api.IdentityAPI;
import com.bonitasoft.engine.api.LogAPI;
import com.bonitasoft.engine.api.MonitoringAPI;
import com.bonitasoft.engine.api.NodeAPI;
import com.bonitasoft.engine.api.PageAPI;
import com.bonitasoft.engine.api.PlatformMonitoringAPI;
import com.bonitasoft.engine.api.ProcessAPI;
import com.bonitasoft.engine.api.ProfileAPI;
import com.bonitasoft.engine.api.ReportingAPI;
import com.bonitasoft.engine.api.ThemeAPI;

/**
 * @author Baptiste Mesta
 */
public class ConnectorAPIAccessorExt extends ConnectorAPIAccessorImpl implements APIAccessor {

    private static final long serialVersionUID = 1L;

    public ConnectorAPIAccessorExt(final long tenantId) {
        super(tenantId);
    }

    @Override
    public IdentityAPI getIdentityAPI() {
        return getAPI(IdentityAPI.class, getAPISession());
    }

    @Override
    public ProcessAPI getProcessAPI() {
        return getAPI(ProcessAPI.class, getAPISession());
    }

    @Override
    public CommandAPI getCommandAPI() {
        return getAPI(CommandAPI.class, getAPISession());
    }

    @Override
    public ProfileAPI getProfileAPI() {
        return getAPI(ProfileAPI.class, getAPISession());
    }

    private static ServerAPI getServerAPI() {
        return ServerAPIFactory.getServerAPI(false);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getAPI(final Class<T> clazz, final APISession session) {
        final ServerAPI serverAPI = getServerAPI();
        final ClientInterceptor sessionInterceptor = new ClientInterceptor(clazz.getName(), serverAPI, session);
        return (T) Proxy.newProxyInstance(APIAccessor.class.getClassLoader(), new Class[] { clazz }, sessionInterceptor);
    }

    @Override
    public MonitoringAPI getMonitoringAPI() {
        return getAPI(MonitoringAPI.class, getAPISession());
    }

    @Override
    public PlatformMonitoringAPI getPlatformMonitoringAPI() {
        return getAPI(PlatformMonitoringAPI.class, getAPISession());
    }

    @Override
    public LogAPI getLogAPI() {
        return getAPI(LogAPI.class, getAPISession());
    }

    @Override
    public NodeAPI getNodeAPI() {
        return getAPI(NodeAPI.class, getAPISession());
    }

    @Override
    public ReportingAPI getReportingAPI() {
        return getAPI(ReportingAPI.class, getAPISession());
    }

    @Override
    public ThemeAPI getThemeAPI() {
        return getAPI(ThemeAPI.class, getAPISession());
    }

    /**
     * @deprecated from version 7.0 on, use {@link #getCustomPageAPI()} instead.
     */
    @Deprecated
    @Override
    public PageAPI getPageAPI() {
        return getAPI(PageAPI.class, getAPISession());
    }

    /**
     * @deprecated from version 7.0 on, use {@link #getLivingApplicationAPI()} instead.
     */
    @Deprecated
    @Override
    public ApplicationAPI getApplicationAPI() {
        return getAPI(ApplicationAPI.class, getAPISession());
    }

}
