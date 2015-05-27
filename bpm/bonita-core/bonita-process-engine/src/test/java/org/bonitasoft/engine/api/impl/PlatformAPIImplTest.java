/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.platform.model.STenant;
import org.bonitasoft.engine.scheduler.AbstractBonitaPlatformJobListener;
import org.bonitasoft.engine.scheduler.AbstractBonitaTenantJobListener;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PlatformAPIImplTest {

    @Mock
    private PlatformServiceAccessor platformServiceAccessor;

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private NodeConfiguration platformConfiguration;

    @Mock
    private TenantServiceAccessor tenantServiceAccessor;

    @Mock
    private TenantConfiguration tenantConfiguration;

    private final List<AbstractBonitaTenantJobListener> tenantJobListeners = Collections.singletonList(mock(AbstractBonitaTenantJobListener.class));

    private final List<AbstractBonitaPlatformJobListener> platformJobListeners = Collections.singletonList(mock(AbstractBonitaPlatformJobListener.class));

    @Spy
    @InjectMocks
    private PlatformAPIImpl platformAPI;

    @Before
    public void setup() throws Exception {
        doReturn(platformConfiguration).when(platformServiceAccessor).getPlatformConfiguration();
        doReturn(platformJobListeners).when(platformConfiguration).getJobListeners();
        doReturn(schedulerService).when(platformServiceAccessor).getSchedulerService();
        doReturn(platformServiceAccessor).when(platformAPI).getPlatformAccessor();
        doReturn(sessionAccessor).when(platformAPI).createSessionAccessor();
        doReturn(tenantServiceAccessor).when(platformAPI).getTenantServiceAccessor(anyLong());
        doReturn(tenantConfiguration).when(tenantServiceAccessor).getTenantConfiguration();
        doReturn(tenantJobListeners).when(tenantConfiguration).getJobListeners();
    }

    @Test
    public void rescheduleErroneousTriggers_should_call_rescheduleErroneousTriggers() throws Exception {
        platformAPI.rescheduleErroneousTriggers();

        verify(schedulerService).rescheduleErroneousTriggers();
    }

    @Test(expected = UpdateException.class)
    public void rescheduleErroneousTriggers_should_throw_exception_when_rescheduleErroneousTriggers_failed() throws Exception {
        doThrow(new SSchedulerException("failed")).when(schedulerService).rescheduleErroneousTriggers();

        platformAPI.rescheduleErroneousTriggers();
    }

    @Test(expected = UpdateException.class)
    public void rescheduleErroneousTriggers_should_throw_exception_when_cant_getPlatformAccessor() throws Exception {
        doThrow(new IOException()).when(platformAPI).getPlatformAccessor();

        platformAPI.rescheduleErroneousTriggers();
    }

    @Test
    public void startNode_should_call_addPlatformAndTenantJobListener() throws Exception {
        // Given
        doNothing().when(platformAPI).checkPlatformVersion(platformServiceAccessor);
        doNothing().when(platformAPI).startPlatformServices(platformServiceAccessor);
        final List<STenant> tenants = Collections.singletonList(mock(STenant.class));
        doReturn(tenants).when(platformAPI).getTenants(platformServiceAccessor);
        doReturn(false).when(platformAPI).isNodeStarted();
        doNothing().when(platformAPI).beforeServicesStartOfRestartHandlersOfTenant(platformServiceAccessor, sessionAccessor, tenants);
        doNothing().when(platformAPI).startServicesOfTenants(platformServiceAccessor, sessionAccessor, tenants);
        doNothing().when(platformAPI).restartHandlersOfPlatform(platformServiceAccessor);
        doNothing().when(platformAPI).afterServicesStartOfRestartHandlersOfTenant(platformServiceAccessor, sessionAccessor, tenants);
        doReturn(true).when(platformConfiguration).shouldStartScheduler();

        // When
        platformAPI.startNode();

        // Then
        verify(schedulerService).initializeScheduler();
        verify(schedulerService).addJobListener(anyListOf(AbstractBonitaPlatformJobListener.class));
        verify(schedulerService).addJobListener(anyListOf(AbstractBonitaTenantJobListener.class), anyString());
    }

    @Test
    public void startNode_should_do_nothing_when_no_TenantJobListener() throws Exception {
        // Given
        doNothing().when(platformAPI).checkPlatformVersion(platformServiceAccessor);
        doNothing().when(platformAPI).startPlatformServices(platformServiceAccessor);
        final List<STenant> tenants = Collections.singletonList(mock(STenant.class));
        doReturn(tenants).when(platformAPI).getTenants(platformServiceAccessor);
        doReturn(false).when(platformAPI).isNodeStarted();
        doNothing().when(platformAPI).beforeServicesStartOfRestartHandlersOfTenant(platformServiceAccessor, sessionAccessor, tenants);
        doNothing().when(platformAPI).startServicesOfTenants(platformServiceAccessor, sessionAccessor, tenants);
        doNothing().when(platformAPI).restartHandlersOfPlatform(platformServiceAccessor);
        doNothing().when(platformAPI).afterServicesStartOfRestartHandlersOfTenant(platformServiceAccessor, sessionAccessor, tenants);
        doReturn(true).when(platformConfiguration).shouldStartScheduler();
        doReturn(Collections.EMPTY_LIST).when(tenantConfiguration).getJobListeners();

        // When
        platformAPI.startNode();

        // Then
        verify(schedulerService).initializeScheduler();
        verify(schedulerService, never()).addJobListener(anyListOf(AbstractBonitaTenantJobListener.class), anyString());
    }

    @Test
    public void startNode_should_do_nothing_when_no_PlatformJobListener() throws Exception {
        // Given
        doNothing().when(platformAPI).checkPlatformVersion(platformServiceAccessor);
        doNothing().when(platformAPI).startPlatformServices(platformServiceAccessor);
        final List<STenant> tenants = Collections.singletonList(mock(STenant.class));
        doReturn(tenants).when(platformAPI).getTenants(platformServiceAccessor);
        doReturn(false).when(platformAPI).isNodeStarted();
        doNothing().when(platformAPI).beforeServicesStartOfRestartHandlersOfTenant(platformServiceAccessor, sessionAccessor, tenants);
        doNothing().when(platformAPI).startServicesOfTenants(platformServiceAccessor, sessionAccessor, tenants);
        doNothing().when(platformAPI).restartHandlersOfPlatform(platformServiceAccessor);
        doNothing().when(platformAPI).afterServicesStartOfRestartHandlersOfTenant(platformServiceAccessor, sessionAccessor, tenants);
        doReturn(true).when(platformConfiguration).shouldStartScheduler();
        doReturn(Collections.EMPTY_LIST).when(platformConfiguration).getJobListeners();

        // When
        platformAPI.startNode();

        // Then
        verify(schedulerService).initializeScheduler();
        verify(schedulerService, never()).addJobListener(anyListOf(AbstractBonitaPlatformJobListener.class));
    }
}
