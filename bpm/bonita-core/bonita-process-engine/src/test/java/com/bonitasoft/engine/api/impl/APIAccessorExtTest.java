/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.ProfileAPI;
import org.bonitasoft.engine.api.ThemeAPI;
import org.bonitasoft.engine.api.impl.CommandAPIImpl;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.session.SSessionException;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.session.model.SSession;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.api.ApplicationAPI;
import com.bonitasoft.engine.api.LogAPI;
import com.bonitasoft.engine.api.MonitoringAPI;
import com.bonitasoft.engine.api.NodeAPI;
import com.bonitasoft.engine.api.PageAPI;
import com.bonitasoft.engine.api.PlatformMonitoringAPI;
import com.bonitasoft.engine.api.ReportingAPI;

@RunWith(MockitoJUnitRunner.class)
public class APIAccessorExtTest {

    private static final long TENANT_ID = 45L;

    @Mock
    private SSession session;

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private SessionService sessionService;

    private APIAccessorExt apiAccessor;

    private void initAPIAccessor() throws SBonitaException {
        when(sessionAccessor.getTenantId()).thenReturn(TENANT_ID);
        when(sessionService.createSession(TENANT_ID, APIAccessorExt.class.getSimpleName())).thenReturn(session);
        when(session.getId()).thenReturn(78L);

        apiAccessor = new APIAccessorExt(sessionAccessor, sessionService);
    }

    @Test
    public void constructorAPIAccessorExt_should_create_a_new_session() throws Exception {
        initAPIAccessor();

        verify(sessionService).createSession(TENANT_ID, APIAccessorExt.class.getSimpleName());
        verify(sessionAccessor).setSessionInfo(78L, TENANT_ID);
    }

    @Test(expected = SBonitaRuntimeException.class)
    public void constructorAPIAccessorExt_should_throw_an_exception_if_one_occurs_during_session_creation() throws Exception {
        when(sessionAccessor.getTenantId()).thenReturn(TENANT_ID);
        when(sessionService.createSession(TENANT_ID, APIAccessorExt.class.getSimpleName())).thenThrow(new SSessionException("error"));

        new APIAccessorExt(sessionAccessor, sessionService);
    }

    @Test(expected = SBonitaRuntimeException.class)
    public void constructorAPIAccessorExt_should_throw_an_exception_if_a_runtime_one_occurs_during_session_creation() throws Exception {
        when(sessionAccessor.getTenantId()).thenReturn(TENANT_ID);
        when(sessionService.createSession(TENANT_ID, APIAccessorExt.class.getSimpleName())).thenThrow(new SBonitaRuntimeException("error"));

        try {
            new APIAccessorExt(sessionAccessor, sessionService);
        } catch (final SBonitaRuntimeException bre) {
            assertThat(bre.getCause()).isNull();
            throw bre;
        }
    }

    @Test(expected = SBonitaRuntimeException.class)
    public void constructorAPIAccessorExt_should_throw_an_exception_if_one_occurs_during_tenant_retrieving() throws Exception {
        when(sessionAccessor.getTenantId()).thenThrow(new STenantIdNotSetException("error"));

        new APIAccessorExt(sessionAccessor, sessionService);
    }

    @Test
    public void getIdentityAPI_should_return_the_default_implementation() throws SBonitaException {
        initAPIAccessor();
        final IdentityAPI identityAPI = apiAccessor.getIdentityAPI();

        assertThat(identityAPI).isNotNull().isExactlyInstanceOf(IdentityAPIExt.class);
    }

    @Test
    public void getProcessAPI_should_return_the_default_implementation() throws SBonitaException {
        initAPIAccessor();
        final ProcessAPI processAPI = apiAccessor.getProcessAPI();

        assertThat(processAPI).isNotNull().isExactlyInstanceOf(ProcessAPIExt.class);
    }

    @Test
    public void getCommandAPI_should_return_the_default_implementation() throws SBonitaException {
        initAPIAccessor();
        final CommandAPI commandAPI = apiAccessor.getCommandAPI();

        assertThat(commandAPI).isNotNull().isExactlyInstanceOf(CommandAPIImpl.class);
    }

    @Test
    public void getProfileAPI_should_return_the_default_implementation() throws SBonitaException {
        initAPIAccessor();
        final ProfileAPI profileAPI = apiAccessor.getProfileAPI();

        assertThat(profileAPI).isNotNull().isExactlyInstanceOf(ProfileAPIExt.class);
    }

    @Test
    public void getThemeAPI_should_return_the_default_implementation() throws SBonitaException {
        initAPIAccessor();
        final ThemeAPI themeAPI = apiAccessor.getThemeAPI();

        assertThat(themeAPI).isNotNull().isExactlyInstanceOf(ThemeAPIExt.class);
    }

    @Test
    public void getMonitoringAPI_should_return_the_default_implementation() throws SBonitaException {
        initAPIAccessor();
        final MonitoringAPI monitoringAPI = apiAccessor.getMonitoringAPI();

        assertThat(monitoringAPI).isNotNull().isExactlyInstanceOf(MonitoringAPIImpl.class);
    }

    @Test
    public void getPlatformMonitoringAPI_should_return_the_default_implementation() throws SBonitaException {
        initAPIAccessor();
        final PlatformMonitoringAPI platformMonitoringAPI = apiAccessor.getPlatformMonitoringAPI();

        assertThat(platformMonitoringAPI).isNotNull().isExactlyInstanceOf(PlatformMonitoringAPIImpl.class);
    }

    @Test
    public void getNodeAPI_should_return_the_default_implementation() throws SBonitaException {
        initAPIAccessor();
        final NodeAPI nodeAPI = apiAccessor.getNodeAPI();

        assertThat(nodeAPI).isNotNull().isExactlyInstanceOf(NodeAPIImpl.class);
    }

    @Test
    public void getApplicationAPI_should_return_the_default_implementation() throws SBonitaException {
        initAPIAccessor();
        final ApplicationAPI applicationAPI = apiAccessor.getApplicationAPI();

        assertThat(applicationAPI).isNotNull().isExactlyInstanceOf(ApplicationAPIImpl.class);
    }

    @Test
    public void getLogAPI_should_return_the_default_implementation() throws SBonitaException {
        initAPIAccessor();
        final LogAPI logAPI = apiAccessor.getLogAPI();

        assertThat(logAPI).isNotNull().isExactlyInstanceOf(LogAPIExt.class);
    }

    @Test
    public void getReportingAPI_should_return_the_default_implementation() throws SBonitaException {
        initAPIAccessor();
        final ReportingAPI reportingAPI = apiAccessor.getReportingAPI();

        assertThat(reportingAPI).isNotNull().isExactlyInstanceOf(ReportingAPIExt.class);
    }

    @Test
    public void getPageAPI_should_return_the_default_implementation() throws SBonitaException {
        initAPIAccessor();
        final PageAPI pageAPI = apiAccessor.getPageAPI();

        assertThat(pageAPI).isNotNull().isExactlyInstanceOf(PageAPIExt.class);
    }

}
