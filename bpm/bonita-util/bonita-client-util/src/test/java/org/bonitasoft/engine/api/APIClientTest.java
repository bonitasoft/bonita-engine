package org.bonitasoft.engine.api;

import org.bonitasoft.engine.api.internal.ServerAPI;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.platform.LogoutException;
import org.bonitasoft.engine.session.APISession;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;


/**
 * Created by Nicolas Chabanoles on 18/11/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class APIClientTest {

    public static final long TENANT_ID = 1L;
    public static final String VALID_USERNAME = "username";
    public static final String VALID_PASSWORD = "password";

    @Spy
    APIClient client;

    @Mock
    ServerAPI server;

    @Mock
    APISession session;

    @Mock
    LoginAPI loginAPI;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void before() throws IOException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException, ServerWrappedException {
        doReturn(server).when(client).getServerAPI();
        doReturn(session).when(server).invokeMethod(anyMap(), eq("org.bonitasoft.engine.api.LoginAPI"), eq("login"), anyList(), eq(new Object[]{VALID_USERNAME, VALID_PASSWORD}));
    }

    @After
    public void after() throws IOException {

    }

    @Test
    public void should_throw_exception_when_accessing_api_without_being_loggedIn() {
        expectedEx.expect(IllegalStateException.class);
        expectedEx.expectMessage("You must call login prior to access any API.");

        client.getProcessAPI();
    }

    @Test
    public void should_throw_exception_when_bonitaHome_not_set() throws LoginException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        doThrow(new BonitaHomeNotSetException("not set")).when(client).getServerAPI();
        expectedEx.expect(IllegalStateException.class);
        expectedEx.expectMessage("not set");

        client.login(VALID_USERNAME, VALID_PASSWORD);
    }

    @Test
    public void should_login_create_a_session() throws LoginException {
        client.login(VALID_USERNAME, VALID_PASSWORD);
        assertThat(client.getSession()).isNotNull();
    }

    @Test
    public void should_logout_destroy_session() throws LoginException, LogoutException {
        client.login(VALID_USERNAME, VALID_PASSWORD);
        assertThat(client.getSession()).isNotNull();
        client.logout();
        assertThat(client.getSession()).isNull();
    }

    @Test
    public void should_get_ProcessAPI_from_server() throws LoginException {
        client.login(VALID_USERNAME, VALID_PASSWORD);
        ProcessAPI processAPI = client.getProcessAPI();
        assertThat(processAPI).isNotNull();
    }

    @Test
    public void should_get_IdentityAPI_from_server() throws LoginException {
        client.login(VALID_USERNAME, VALID_PASSWORD);
        IdentityAPI api = client.getIdentityAPI();
        assertThat(api).isNotNull();
    }

    @Test
    public void should_get_ThemeAPI_from_server() throws LoginException {
        client.login(VALID_USERNAME, VALID_PASSWORD);
        ThemeAPI api = client.getThemeAPI();
        assertThat(api).isNotNull();
    }

    @Test
    public void should_get_CommandAPI_from_server() throws LoginException {
        client.login(VALID_USERNAME, VALID_PASSWORD);
        CommandAPI api = client.getCommandAPI();
        assertThat(api).isNotNull();
    }

    @Test
    public void should_get_ProfileAPI_from_server() throws LoginException {
        client.login(VALID_USERNAME, VALID_PASSWORD);
        ProfileAPI api = client.getProfileAPI();
        assertThat(api).isNotNull();
    }

    @Test
    public void should_get_TenantAdministrationAPI_from_server() throws LoginException {
        client.login(VALID_USERNAME, VALID_PASSWORD);
        TenantAdministrationAPI api = client.getTenantAdministrationAPI();
        assertThat(api).isNotNull();
    }

    @Test
    public void should_get_PageAPI_from_server() throws LoginException {
        client.login(VALID_USERNAME, VALID_PASSWORD);
        PageAPI api = client.getCustomPageAPI();
        assertThat(api).isNotNull();
    }

    @Test
    public void should_get_ApplicationAPI_from_server() throws LoginException {
        client.login(VALID_USERNAME, VALID_PASSWORD);
        ApplicationAPI api = client.getLivingApplicationAPI();
        assertThat(api).isNotNull();
    }

    @Test
    public void should_get_PermissionAPI_from_server() throws LoginException {
        client.login(VALID_USERNAME, VALID_PASSWORD);
        PermissionAPI api = client.getPermissionAPI();
        assertThat(api).isNotNull();
    }

    @Test
    public void should_get_BusinessDataAPI_from_server() throws LoginException {
        client.login(VALID_USERNAME, VALID_PASSWORD);
        BusinessDataAPI api = client.getBusinessDataAPI();
        assertThat(api).isNotNull();
    }

    @Test
    public void should_throw_exception_when_accessing_api_after_logout() throws LoginException, LogoutException {
        expectedEx.expect(IllegalStateException.class);
        expectedEx.expectMessage("You must call login prior to access any API.");

        client.login(VALID_USERNAME, VALID_PASSWORD);
        client.logout();
        client.getProcessAPI();
    }

    @Test
    public void should_return_session_used_at_creation() throws LoginException, LogoutException {
        APIClient clientToTest = new APIClient(session);
        assertThat(clientToTest.getSession()).isEqualTo(session);
    }

    @Test
    public void should_return_session_created_at_login() throws LoginException, LogoutException {
        client.login(VALID_USERNAME, VALID_PASSWORD);
        assertThat(client.getSession()).isEqualTo(session);
    }

    @Test
    public void should_newly_created_client_has_no_session() throws LoginException, LogoutException {
        assertThat(client.getSession()).isNull();
    }

}
