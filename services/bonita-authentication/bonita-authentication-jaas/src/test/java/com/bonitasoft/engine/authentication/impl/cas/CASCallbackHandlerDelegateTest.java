package com.bonitasoft.engine.authentication.impl.cas;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.authentication.AuthenticationConstants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;



@RunWith(MockitoJUnitRunner.class)
public class CASCallbackHandlerDelegateTest {

    @Spy
    CASCallbackHandlerDelegate callbackHandlerDelegate = new CASCallbackHandlerDelegate();
    
    @Mock
    CASUtils casUtils;

    Map<String, Serializable> credentials;

    @Before
    public void setUp() throws Exception{
        credentials = new HashMap<String, Serializable>();
        callbackHandlerDelegate.casUtils = casUtils;
    }

    @Test
    public void testHandleServiceWithoutLicenseShouldThrowException() {
        String service = "http://www.bonitasoft.com/bonita/portal/homepage";
        callbackHandlerDelegate.casUtils = casUtils;
        doThrow(new IllegalStateException(CASUtils.THE_CAS_AUTHENTICATOR_IS_NOT_AN_ACTIVE_FEATURE)).when(casUtils).checkLicense();
        credentials.put(AuthenticationConstants.CAS_SERVICE, service);
        try {
            callbackHandlerDelegate.getCASService(credentials);
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage(CASUtils.THE_CAS_AUTHENTICATOR_IS_NOT_AN_ACTIVE_FEATURE);
            return;
        }
        fail();
    }

    @Test
    public void testHandleServiceShouldReturnTicket() {
        String service = "http://www.bonitasoft.com/bonita/portal/homepage";
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(casUtils).checkLicense();
        credentials.put(AuthenticationConstants.CAS_SERVICE, service);
        assertThat(callbackHandlerDelegate.getCASService(credentials)).isEqualTo(service);
    }

    @Test
    public void testHandleServiceWithNullMapShouldReturnNull() {
        assertThat(callbackHandlerDelegate.getCASService(null)).isNull();
    }

    @Test
    public void testHandleServiceWithEmptyMapShouldReturnNull() {
        assertThat(callbackHandlerDelegate.getCASService(credentials)).isNull();
    }

    @Test
    public void testHandleTicketWithoutLicenseShouldThrowException() {
        String ticket = "ST-zlfkjsldkf";
        callbackHandlerDelegate.casUtils = casUtils;
        doThrow(new IllegalStateException(CASUtils.THE_CAS_AUTHENTICATOR_IS_NOT_AN_ACTIVE_FEATURE)).when(casUtils).checkLicense();
        credentials.put(AuthenticationConstants.CAS_TICKET, ticket);
        try {
            callbackHandlerDelegate.getCASTicket(credentials);
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage(CASUtils.THE_CAS_AUTHENTICATOR_IS_NOT_AN_ACTIVE_FEATURE);
            return;
        }
        fail();
    }

    @Test
    public void testHandleTicketShouldReturnTicket() {
        String ticket = "ST-zlfkjsldkf";
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }
        }).when(casUtils).checkLicense();
        credentials.put(AuthenticationConstants.CAS_TICKET, ticket);
        assertThat(callbackHandlerDelegate.getCASTicket(credentials)).isEqualTo(ticket);
    }

    @Test
    public void testHandleTicketWithNullMapShouldReturnNull() {
        assertThat(callbackHandlerDelegate.getCASTicket(null)).isNull();
    }

    @Test
    public void testHandleTicketWithEmptyMapShouldReturnNull() {
        assertThat(callbackHandlerDelegate.getCASTicket(credentials)).isNull();
    }
    
}
