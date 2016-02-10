/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.authentication.impl.cas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;

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
    public void setUp() {
        credentials = new HashMap<String, Serializable>();
        callbackHandlerDelegate.casUtils = casUtils;
    }

    @Test
    public void handleServiceWithoutLicenseShouldThrowException() {
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
    public void handleServiceShouldReturnTicket() {
        String service = "http://www.bonitasoft.com/bonita/portal/homepage";
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) {
                return null;
            }
        }).when(casUtils).checkLicense();
        credentials.put(AuthenticationConstants.CAS_SERVICE, service);
        assertThat(callbackHandlerDelegate.getCASService(credentials)).isEqualTo(service);
    }

    @Test
    public void handleServiceWithNullMapShouldReturnNull() {
        assertThat(callbackHandlerDelegate.getCASService(null)).isNull();
    }

    @Test
    public void handleServiceWithEmptyMapShouldReturnNull() {
        assertThat(callbackHandlerDelegate.getCASService(credentials)).isNull();
    }

    @Test
    public void handleTicketWithoutLicenseShouldThrowException() {
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
    public void handleTicketShouldReturnTicket() {
        String ticket = "ST-zlfkjsldkf";
        doAnswer(new Answer<Object>() {

            @Override
            public Object answer(InvocationOnMock invocation) {
                return null;
            }
        }).when(casUtils).checkLicense();
        credentials.put(AuthenticationConstants.CAS_TICKET, ticket);
        assertThat(callbackHandlerDelegate.getCASTicket(credentials)).isEqualTo(ticket);
    }

    @Test
    public void handleTicketWithNullMapShouldReturnNull() {
        assertThat(callbackHandlerDelegate.getCASTicket(null)).isNull();
    }

    @Test
    public void handleTicketWithEmptyMapShouldReturnNull() {
        assertThat(callbackHandlerDelegate.getCASTicket(credentials)).isNull();
    }

}
