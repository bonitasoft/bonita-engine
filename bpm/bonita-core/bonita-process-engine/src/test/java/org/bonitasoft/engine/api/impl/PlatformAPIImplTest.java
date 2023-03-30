/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
 **/
package org.bonitasoft.engine.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.platform.PlatformManager;
import org.bonitasoft.engine.platform.StartNodeException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PlatformAPIImplTest {

    public static final long TENANT_ID = 56423L;
    @Mock
    private ServiceAccessor serviceAccessor;
    @Mock
    private SchedulerService schedulerService;
    @Mock
    private BonitaHomeServer bonitaHomeServer;
    @Mock
    private PlatformManager platformManager;
    @Spy
    @InjectMocks
    private PlatformAPIImpl platformAPI;

    @Before
    public void setup() throws Exception {
        doReturn(schedulerService).when(serviceAccessor).getSchedulerService();
        doReturn(platformManager).when(serviceAccessor).getPlatformManager();
        doReturn(serviceAccessor).when(platformAPI).getServiceAccessor();
        doReturn(bonitaHomeServer).when(platformAPI).getBonitaHomeServer();
    }

    @Test
    public void rescheduleErroneousTriggers_should_call_rescheduleErroneousTriggers() throws Exception {
        platformAPI.rescheduleErroneousTriggers();

        verify(schedulerService).rescheduleErroneousTriggers();
    }

    @Test(expected = UpdateException.class)
    public void rescheduleErroneousTriggers_should_throw_exception_when_rescheduleErroneousTriggers_failed()
            throws Exception {
        doThrow(new SSchedulerException("failed")).when(schedulerService).rescheduleErroneousTriggers();

        platformAPI.rescheduleErroneousTriggers();
    }

    @Test(expected = UpdateException.class)
    public void rescheduleErroneousTriggers_should_throw_exception_when_cant_getPlatformAccessor() throws Exception {
        doThrow(new IOException()).when(platformAPI).getServiceAccessor();

        platformAPI.rescheduleErroneousTriggers();
    }

    @Test
    public void should_updateTenantPortalConfigurationFile_call_bonitaHomeServer() throws Exception {
        //when
        platformAPI.updateClientTenantConfigurationFile(TENANT_ID, "myProps.properties", "updated content".getBytes());
        //then
        verify(bonitaHomeServer).updateTenantPortalConfigurationFile(TENANT_ID, "myProps.properties",
                "updated content".getBytes());
    }

    @Test
    public void should_getTenantPortalConfigurationFile_call_bonitaHomeServer() {
        //given
        final String configurationFile = "a file";
        doReturn("content".getBytes()).when(bonitaHomeServer).getTenantPortalConfiguration(TENANT_ID,
                configurationFile);

        //when
        final byte[] configuration = platformAPI.getClientTenantConfiguration(TENANT_ID, configurationFile);

        //then
        assertThat(configuration).as("should return file content").isEqualTo("content".getBytes());
        verify(bonitaHomeServer).getTenantPortalConfiguration(TENANT_ID, configurationFile);
    }

    @Test(expected = StartNodeException.class)
    public void startNode_should_throw_exception_when_platform_is_not_in_state_stopped() throws Exception {
        doReturn(false).when(platformManager).start();

        platformAPI.startNode();
    }

    @Test
    public void startNode_should_call_start_on_platformManager() throws Exception {
        doReturn(true).when(platformManager).start();

        platformAPI.startNode();

        verify(platformManager).start();
    }

}
