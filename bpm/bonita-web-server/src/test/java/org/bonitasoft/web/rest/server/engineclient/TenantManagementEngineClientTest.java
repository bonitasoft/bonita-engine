/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.engineclient;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.engine.api.TenantAdministrationAPI;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TenantManagementEngineClientTest {

    @Mock
    private TenantAdministrationAPI tenantAdministrationAPI;

    @InjectMocks
    private TenantManagementEngineClient tenantManagementEngineClient;

    private void pauseTenant() {
        when(tenantAdministrationAPI.isPaused()).thenReturn(true);
    }

    private void resumeTenant() {
        when(tenantAdministrationAPI.isPaused()).thenReturn(false);
    }

    @Before
    public void startTenant() {
        resumeTenant();
    }

    @Test
    public void pauseTenant_pause_the_tenant() throws Exception {

        tenantManagementEngineClient.pauseTenant();

        verify(tenantAdministrationAPI).pause();
    }

    @Test
    public void pauseTenant_dont_pause_tenant_if_it_is_already_paused() throws Exception {
        pauseTenant();

        tenantManagementEngineClient.pauseTenant();

        verify(tenantAdministrationAPI, never()).pause();
    }

    @Test(expected = APIException.class)
    public void pauseTenant_throw_APIException_if_error_occurs_when_pausing_tenant() throws Exception {
        doThrow(new UpdateException(new NullPointerException())).when(tenantAdministrationAPI).pause();

        tenantManagementEngineClient.pauseTenant();
    }

    @Test
    public void resumeTenant_resume_the_tenant() throws Exception {
        pauseTenant();

        tenantManagementEngineClient.resumeTenant();

        verify(tenantAdministrationAPI).resume();
    }

    @Test
    public void resumeTenant_dont_resume_tenant_if_it_not_paused() throws Exception {

        tenantManagementEngineClient.resumeTenant();

        verify(tenantAdministrationAPI, never()).resume();
    }

    @Test(expected = APIException.class)
    public void resumeTenant_throw_APIException_if_error_occurs_when_resuming_tenant() throws Exception {
        pauseTenant();
        doThrow(new UpdateException(new NullPointerException())).when(tenantAdministrationAPI).resume();

        tenantManagementEngineClient.resumeTenant();
    }
}
