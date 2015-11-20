package com.bonitasoft.engine.api;

import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.platform.LogoutException;
import org.bonitasoft.engine.session.APISession;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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
    public static final Map<String, Serializable> CREDENTIALS = new HashMap<>();

    @BeforeClass
    public static void initConstants() {
        CREDENTIALS.put("username", VALID_USERNAME);
        CREDENTIALS.put("password", VALID_PASSWORD);
    }

    @Spy
    APIClient client;

    @Mock
    APISession session;

    @Mock
    LoginAPI loginAPI;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void before() throws LoginException {
        doReturn(loginAPI).when(client).getLoginAPI();
        doReturn(session).when(loginAPI).login(TENANT_ID, VALID_USERNAME, VALID_PASSWORD);
        doReturn(session).when(loginAPI).login(TENANT_ID, CREDENTIALS);
    }

    @After
    public void after() throws IOException {

    }

    @Test
    public void should_throw_exception_when_accessing_api_without_being_loggedIn() {
        expectedEx.expect(IllegalStateException.class);
        expectedEx.expectMessage("You must call login prior to access any API.");

        client.getLogAPI();
    }

    @Test
    public void should_login_create_a_session() throws LoginException {
        client.login(TENANT_ID, VALID_USERNAME, VALID_PASSWORD);
        assertThat(client.getSession()).isNotNull();
    }

    @Test
    public void should_login_with_credentials_create_a_session() throws LoginException {
        client.login(TENANT_ID, CREDENTIALS);
        assertThat(client.getSession()).isNotNull();
    }

    @Test
    public void should_logout_destroy_session() throws LoginException, LogoutException {
        client.login(TENANT_ID, VALID_USERNAME, VALID_PASSWORD);
        assertThat(client.getSession()).isNotNull();
        client.logout();
        assertThat(client.getSession()).isNull();

    }

    @Test
    public void should_logout_destroy_session_created_by_credentials() throws LoginException, LogoutException {

        client.login(TENANT_ID, CREDENTIALS);
        assertThat(client.getSession()).isNotNull();
        client.logout();
        assertThat(client.getSession()).isNull();

    }

}
