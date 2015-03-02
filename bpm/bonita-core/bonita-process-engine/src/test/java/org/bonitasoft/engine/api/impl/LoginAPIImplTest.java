package org.bonitasoft.engine.api.impl;

import static org.mockito.Mockito.doReturn;

import java.util.Properties;

import org.bonitasoft.engine.exception.TenantStatusException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.platform.LoginException;
import org.bonitasoft.engine.platform.model.STenant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class LoginAPIImplTest {

    @Mock
    BonitaHomeServer bonitaHomeServer;

    @Mock
    STenant sTenant;

    Properties properties;

    @Spy
    LoginAPIImpl loginAPI;

    @Before
    public void before() throws Exception {
        doReturn(bonitaHomeServer).when(loginAPI).getBonitaHomeServerInstance();

        doReturn(1L).when(sTenant).getId();

        properties = new Properties();
        properties.put("userName", "install");
        doReturn(properties).when(bonitaHomeServer).getTenantProperties(1L);

    }

    @Test
    public void checkThatWeCanLogin_should_allow_technical_user() throws Exception {
        //given
        doReturn(true).when(sTenant).isActivated();
        doReturn(true).when(sTenant).isPaused();

        //when then no exception
        loginAPI.checkThatWeCanLogin("install", sTenant, bonitaHomeServer);

    }

    @Test(expected = LoginException.class)
    public void checkThatWeCanLogin_should_throw_exception_when_tenant_is_not_activated() throws Exception {
        //given
        doReturn(false).when(sTenant).isActivated();

        //when then exception
        loginAPI.checkThatWeCanLogin("joe", sTenant, bonitaHomeServer);

    }

    @Test(expected = TenantStatusException.class)
    public void checkThatWeCanLogin_should_refuse_non_technical_user() throws Exception {
        //given
        doReturn(true).when(sTenant).isActivated();
        doReturn(true).when(sTenant).isPaused();

        LoginAPIImpl loginAPI = new LoginAPIImpl();

        //when then no exception
        loginAPI.checkThatWeCanLogin("joe", sTenant, bonitaHomeServer);

    }

}
