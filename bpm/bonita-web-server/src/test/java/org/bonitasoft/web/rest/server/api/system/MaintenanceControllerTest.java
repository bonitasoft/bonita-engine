/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.system;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import javax.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.api.MaintenanceAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.maintenance.MaintenanceDetails;
import org.bonitasoft.engine.maintenance.impl.MaintenanceDetailsImpl;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.system.MaintenanceDetailsClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@RunWith(MockitoJUnitRunner.class)
public class MaintenanceControllerTest {

    @Mock
    private HttpSession session;

    @Mock
    private MaintenanceAPI maintenanceAPI;

    @Spy
    @InjectMocks
    private MaintenanceController maintenanceController;

    @Test
    public void should_get_maintenance_info_from_java_api() throws BonitaException {
        //given
        APISession apiSession = mock(APISession.class);
        doReturn(apiSession).when(maintenanceController).getApiSession(session);

        MaintenanceDetails maintenanceDetails = mock(MaintenanceDetails.class);
        doReturn(maintenanceAPI).when(maintenanceController).getMaintenanceAPI(apiSession);
        doReturn(maintenanceDetails).when(maintenanceAPI).getMaintenanceDetails();

        //when
        MaintenanceDetails result = maintenanceController.getMaintenanceDetails(session);

        //then
        verify(maintenanceAPI).getMaintenanceDetails();
        assertSame(maintenanceDetails, result);
    }

    @Test
    public void should_enable_maintenance_and_change_and_enable_maintenance_msg() throws BonitaException {
        //given
        APISession apiSession = mock(APISession.class);
        doReturn(apiSession).when(maintenanceController).getApiSession(session);

        MaintenanceDetails maintenanceDetails = MaintenanceDetailsImpl.builder()
                .maintenanceState(MaintenanceDetails.State.DISABLED)
                .maintenanceMessage(null)
                .maintenanceMessageActive(false)
                .build();
        doReturn(maintenanceAPI).when(maintenanceController).getMaintenanceAPI(apiSession);
        doReturn(maintenanceDetails).when(maintenanceAPI).getMaintenanceDetails();

        MaintenanceDetailsClient maintenanceInfoClient = new MaintenanceDetailsClient(
                MaintenanceDetails.State.ENABLED, "new Scheduled Maintenance msg", true);

        //when
        maintenanceController.changeMaintenanceState(maintenanceInfoClient, session);

        //then
        verify(maintenanceAPI).updateMaintenanceMessage("new Scheduled Maintenance msg");
        verify(maintenanceAPI).enableMaintenanceMode();
        verify(maintenanceAPI, never()).disableMaintenanceMode();
        verify(maintenanceAPI).enableMaintenanceMessage();
        verify(maintenanceAPI, never()).disableMaintenanceMessage();
    }

    @Test
    public void should_disable_maintenance_and_change_and_disable_maintenance_msg() throws BonitaException {
        //given
        APISession apiSession = mock(APISession.class);
        doReturn(apiSession).when(maintenanceController).getApiSession(session);

        MaintenanceDetails maintenanceDetails = MaintenanceDetailsImpl.builder()
                .maintenanceState(MaintenanceDetails.State.ENABLED)
                .maintenanceMessage("msg")
                .maintenanceMessageActive(true)
                .build();
        doReturn(maintenanceAPI).when(maintenanceController).getMaintenanceAPI(apiSession);
        doReturn(maintenanceDetails).when(maintenanceAPI).getMaintenanceDetails();

        MaintenanceDetailsClient maintenanceInfoClient = new MaintenanceDetailsClient(
                MaintenanceDetails.State.DISABLED, "new Scheduled Maintenance msg", false);

        //when
        maintenanceController.changeMaintenanceState(maintenanceInfoClient, session);

        //then
        verify(maintenanceAPI).updateMaintenanceMessage("new Scheduled Maintenance msg");
        verify(maintenanceAPI, never()).enableMaintenanceMode();
        verify(maintenanceAPI).disableMaintenanceMode();
        verify(maintenanceAPI, never()).enableMaintenanceMessage();
        verify(maintenanceAPI).disableMaintenanceMessage();
    }

    @Test
    public void should_disable_maintenance_only() throws BonitaException {
        //given
        APISession apiSession = mock(APISession.class);
        doReturn(apiSession).when(maintenanceController).getApiSession(session);

        MaintenanceDetails maintenanceDetails = MaintenanceDetailsImpl.builder()
                .maintenanceState(MaintenanceDetails.State.ENABLED)
                .maintenanceMessage("msg")
                .maintenanceMessageActive(true)
                .build();
        doReturn(maintenanceAPI).when(maintenanceController).getMaintenanceAPI(apiSession);
        doReturn(maintenanceDetails).when(maintenanceAPI).getMaintenanceDetails();

        MaintenanceDetailsClient maintenanceInfoClient = new MaintenanceDetailsClient(
                MaintenanceDetails.State.DISABLED, "msg", true);

        //when
        maintenanceController.changeMaintenanceState(maintenanceInfoClient, session);

        //then
        verify(maintenanceAPI, never()).updateMaintenanceMessage(any());
        verify(maintenanceAPI, never()).enableMaintenanceMode();
        verify(maintenanceAPI).disableMaintenanceMode();
        verify(maintenanceAPI, never()).enableMaintenanceMessage();
        verify(maintenanceAPI, never()).disableMaintenanceMessage();
    }

    @Test
    public void should_disable_scheduled_maintenance_msg_only() throws BonitaException {
        //given
        APISession apiSession = mock(APISession.class);
        doReturn(apiSession).when(maintenanceController).getApiSession(session);

        MaintenanceDetails maintenanceDetails = MaintenanceDetailsImpl.builder()
                .maintenanceState(MaintenanceDetails.State.DISABLED)
                .maintenanceMessage("msg")
                .maintenanceMessageActive(true)
                .build();
        doReturn(maintenanceAPI).when(maintenanceController).getMaintenanceAPI(apiSession);
        doReturn(maintenanceDetails).when(maintenanceAPI).getMaintenanceDetails();

        MaintenanceDetailsClient maintenanceInfoClient = new MaintenanceDetailsClient(
                MaintenanceDetails.State.DISABLED, "msg", false);

        //when
        maintenanceController.changeMaintenanceState(maintenanceInfoClient, session);

        //then
        verify(maintenanceAPI, never()).updateMaintenanceMessage(any());
        verify(maintenanceAPI, never()).enableMaintenanceMode();
        verify(maintenanceAPI, never()).disableMaintenanceMode();
        verify(maintenanceAPI, never()).enableMaintenanceMessage();
        verify(maintenanceAPI).disableMaintenanceMessage();
    }

    @Test
    public void should_respond_unauthorized_if_no_session() {
        //given
        when(session.getAttribute(SessionUtil.API_SESSION_PARAM_KEY)).thenReturn(null);

        //when
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> maintenanceController.getApiSession(session));
        //then
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());

        //when
        exception = assertThrows(ResponseStatusException.class,
                () -> maintenanceController.getMaintenanceDetails(session));
        //then
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());

        //when
        exception = assertThrows(ResponseStatusException.class,
                () -> maintenanceController.changeMaintenanceState(null, session));
        //then
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    public void should_getApiSession_return_session_from_HttpRequestSession() {
        // Create a mock APISession
        APISession apiSession = mock(APISession.class);
        when(session.getAttribute(SessionUtil.API_SESSION_PARAM_KEY)).thenReturn(apiSession);

        // Call the method and expect the same APISession to be returned
        APISession result = maintenanceController.getApiSession(session);
        assertSame(apiSession, result);
    }
}
