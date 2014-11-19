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



    private APIAccessorExt apiAccessor;

    private void initAPIAccessor() throws SBonitaException {
        apiAccessor = new APIAccessorExt();
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
