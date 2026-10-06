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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpSession;

import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.api.MaintenanceAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.maintenance.MaintenanceDetails;
import org.bonitasoft.engine.maintenance.impl.MaintenanceDetailsImpl;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.system.MaintenanceDetailsClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class MaintenanceControllerTest {

    @Mock
    private HttpSession session;

    @Mock
    private MaintenanceAPI maintenanceAPI;

    @Spy
    @InjectMocks
    private MaintenanceController maintenanceController;

    /**
     * Jackson hands the controller a fresh String, never the interned literal: only a non-interned equal message
     * tells a value comparison apart from a reference one.
     */
    @Test
    public void should_not_update_maintenance_message_when_an_equal_but_not_interned_message_is_submitted()
            throws BonitaException {
        //given
        APISession apiSession = mock(APISession.class);
        doReturn(apiSession).when(maintenanceController).getApiSession(session);
        doReturn(maintenanceAPI).when(maintenanceController).getMaintenanceAPI(apiSession);
        MaintenanceDetails currentDetails = MaintenanceDetailsImpl.builder()
                .maintenanceState(MaintenanceDetails.State.DISABLED)
                .maintenanceMessage("msg")
                .maintenanceMessageActive(false)
                .build();
        doReturn(currentDetails).when(maintenanceAPI).getMaintenanceDetails();

        //when the submitted message is equal but a different instance
        maintenanceController.changeMaintenanceState(new MaintenanceDetailsClient(
                MaintenanceDetails.State.DISABLED, new StringBuilder("msg").toString(), false), session);

        //then
        verify(maintenanceAPI, never()).updateMaintenanceMessage(any());
    }

    /**
     * {@code MaintenanceAPIImpl.disableMaintenanceMode()} also clears the message flag, so the message guards must
     * compare against details re-read after the state change, not the snapshot taken before it.
     */
    @Test
    public void should_keep_the_message_active_when_maintenance_is_disabled_in_the_same_request()
            throws BonitaException {
        //given maintenance is on with the banner showing
        APISession apiSession = mock(APISession.class);
        doReturn(apiSession).when(maintenanceController).getApiSession(session);
        doReturn(maintenanceAPI).when(maintenanceController).getMaintenanceAPI(apiSession);
        MaintenanceDetails whileEnabled = MaintenanceDetailsImpl.builder()
                .maintenanceState(MaintenanceDetails.State.ENABLED)
                .maintenanceMessage("msg")
                .maintenanceMessageActive(true)
                .build();
        //and disabling maintenance clears the message flag as a side effect, as the engine does
        MaintenanceDetails afterDisabling = MaintenanceDetailsImpl.builder()
                .maintenanceState(MaintenanceDetails.State.DISABLED)
                .maintenanceMessage("msg")
                .maintenanceMessageActive(false)
                .build();
        doReturn(whileEnabled, afterDisabling).when(maintenanceAPI).getMaintenanceDetails();

        //when the client exits maintenance but asks to keep the banner up
        maintenanceController.changeMaintenanceState(new MaintenanceDetailsClient(
                MaintenanceDetails.State.DISABLED, "msg", true), session);

        //then the banner is re-enabled rather than left off
        verify(maintenanceAPI).disableMaintenanceMode();
        verify(maintenanceAPI).enableMaintenanceMessage();
    }

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

        MaintenanceDetails whileEnabled = MaintenanceDetailsImpl.builder()
                .maintenanceState(MaintenanceDetails.State.ENABLED)
                .maintenanceMessage("msg")
                .maintenanceMessageActive(true)
                .build();
        //and disabling maintenance clears the message flag as a side effect, as the engine does
        MaintenanceDetails afterDisabling = MaintenanceDetailsImpl.builder()
                .maintenanceState(MaintenanceDetails.State.DISABLED)
                .maintenanceMessage("msg")
                .maintenanceMessageActive(false)
                .build();
        doReturn(maintenanceAPI).when(maintenanceController).getMaintenanceAPI(apiSession);
        doReturn(whileEnabled, afterDisabling).when(maintenanceAPI).getMaintenanceDetails();

        MaintenanceDetailsClient maintenanceInfoClient = new MaintenanceDetailsClient(
                MaintenanceDetails.State.DISABLED, "new Scheduled Maintenance msg", false);

        //when
        maintenanceController.changeMaintenanceState(maintenanceInfoClient, session);

        //then the message is updated, and not disabled a second time
        verify(maintenanceAPI).updateMaintenanceMessage("new Scheduled Maintenance msg");
        verify(maintenanceAPI, never()).enableMaintenanceMode();
        verify(maintenanceAPI).disableMaintenanceMode();
        verify(maintenanceAPI, never()).enableMaintenanceMessage();
        verify(maintenanceAPI, never()).disableMaintenanceMessage();
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

        //only the state is sent, as the platform maintenance page does
        MaintenanceDetailsClient maintenanceInfoClient = new MaintenanceDetailsClient(
                MaintenanceDetails.State.DISABLED, null, null);

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
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        //when
        exception = assertThrows(ResponseStatusException.class,
                () -> maintenanceController.getMaintenanceDetails(session));
        //then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        //when
        exception = assertThrows(ResponseStatusException.class,
                () -> maintenanceController.changeMaintenanceState(null, session));
        //then
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
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
