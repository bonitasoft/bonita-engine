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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.APIAccessResolver;
import org.bonitasoft.engine.session.Session;
import org.bonitasoft.engine.session.impl.APISessionImpl;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;

/**
 * @author Celine Souchet
 * @author Aurelien Pupier
 * 
 */
@RunWith(MockitoJUnitRunner.class)
public class ServerAPIImplTest {

    @Mock
    private APIAccessResolver accessResolver;

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private Session session;

    @Mock
    private ServerWrappedException serverWrappedException;

    /**
     * Test method for
     * {@link org.bonitasoft.engine.api.impl.ServerAPIImpl#invokeMethod(java.util.Map, java.lang.String, java.lang.String, java.util.List, java.lang.Object[])}.
     * 
     * @throws Throwable
     */
    @Test(expected = ServerWrappedException.class)
    public void invokeMethodCatchUndeclaredThrowableException() throws Throwable {
        testCatchAndLogged((new UndeclaredThrowableException(null, "")));
    }

    @Test(expected = ServerWrappedException.class)
    public void invokeMethodCatchThrowable() throws Throwable {
        testCatchAndLogged(new Throwable(""));
    }

    private void testCatchAndLogged(final Throwable toBeThrown)
            throws BonitaHomeNotSetException, InstantiationException,
            IllegalAccessException, ClassNotFoundException,
            BonitaHomeConfigurationException, IOException,
            NoSuchMethodException, InvocationTargetException, SBonitaException,
            Throwable, ServerWrappedException {
        final String apiInterfaceName = "apiInterfaceName";
        final String methodName = "methodName";
        final List<String> classNameParameters = new ArrayList<String>();
        final Object[] parametersValues = null;

        ServerAPIImpl serverAPIImpl = spy(new ServerAPIImpl(true, accessResolver));
        doReturn(sessionAccessor).when(serverAPIImpl).beforeInvokeMethod(session, apiInterfaceName);
        final TechnicalLoggerService technicalLogger = mock(TechnicalLoggerService.class);
        doReturn(true).when(technicalLogger).isLoggable(any(Class.class), eq(TechnicalLogSeverity.DEBUG));
        doThrow(toBeThrown).when(serverAPIImpl).invokeAPI(apiInterfaceName, apiInterfaceName, classNameParameters, parametersValues, session);

        serverAPIImpl.setTechnicalLogger(technicalLogger);
        final Map<String, Serializable> options = new HashMap<String, Serializable>();
        options.put("session", session);
        try {
            serverAPIImpl.invokeMethod(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
        } finally {
            verify(technicalLogger, VerificationModeFactory.atLeastOnce()).log(any(Class.class), eq(TechnicalLogSeverity.DEBUG), Mockito.any(Throwable.class));
        }
    }

    @Test(expected = ServerAPIRuntimeException.class)
    public void invokeAPIWithInvalidChecksShouldNotInvokeAnything() throws Throwable {
        // given:
        final String apiInterfaceName = "apiInterfaceName";
        // must be an existing method name (on Object):
        final String methodName = "toString";
        final List<String> classNameParameters = new ArrayList<String>();
        final Object[] parametersValues = null;
        Session session = new APISessionImpl(1L, new Date(), 120L, "userName", 5487L, "mon_tenant", 25L);

        APIAccessResolver accessResolver = mock(APIAccessResolver.class);
        when(accessResolver.getAPIImplementation(apiInterfaceName)).thenReturn(new Object());
        final ServerAPIImpl mockedServerAPIImpl = PowerMockito.spy(new ServerAPIImpl(true, accessResolver));
        doThrow(BonitaRuntimeException.class).when(mockedServerAPIImpl).checkMethodAccessibility(any(), eq(apiInterfaceName), any(Method.class), eq(session), eq(false));
        doReturn(new UserTransactionService(){
            @Override
            public <T> T executeInTransaction(Callable<T> callable) throws Exception {
                return callable.call();
            }
            @Override
            public void registerBonitaSynchronization(BonitaTransactionSynchronization txSync) throws STransactionNotFoundException {
            }
            @Override
            public void registerBeforeCommitCallable(Callable<Void> callable) throws STransactionNotFoundException {
            }
        }).when(mockedServerAPIImpl).selectUserTransactionService(any(Session.class),any(ServerAPIImpl.SessionType.class));
        try {
            // when:
            mockedServerAPIImpl.invokeAPI(apiInterfaceName, methodName, classNameParameters, parametersValues, session);
        } finally {
            // then:
            verify(mockedServerAPIImpl, never()).invokeAPI(any(Object[].class), anyString(), any(Method.class));
        }
    }

    @Test
    public void invokeAPIWithValidChecksAndCustomTransactionsShouldCallDirectInvokeAPI() throws Throwable {
        // given:
        final String apiInterfaceName = "apiInterfaceName";
        final String methodName = "customTxAPIMethod";
        final List<String> classNameParameters = new ArrayList<String>();
        final Object[] parametersValues = null;
        Session session = new APISessionImpl(1L, new Date(), 120L, "userName", 5487L, "mon_tenant", 25L);

        APIAccessResolver accessResolver = mock(APIAccessResolver.class);
        FakeAPI apiImpl = new FakeAPI();
        when(accessResolver.getAPIImplementation(apiInterfaceName)).thenReturn(apiImpl);
        final ServerAPIImpl mockedServerAPIImpl = PowerMockito.spy(new ServerAPIImpl(true, accessResolver));
        doNothing().when(mockedServerAPIImpl).checkMethodAccessibility(any(), eq(apiInterfaceName), any(Method.class), eq(session), eq(false));

        try {
            // when:
            mockedServerAPIImpl.invokeAPI(apiInterfaceName, methodName, classNameParameters, parametersValues, session);
        } finally {
            // then:
            verify(mockedServerAPIImpl).invokeAPI(parametersValues, apiImpl, FakeAPI.class.getMethod(methodName));
            verify(mockedServerAPIImpl, never()).invokeAPIInTransaction(any(Object[].class), anyString(), any(Method.class), any(Session.class), anyString());
        }
    }

    @Test
    public void invokeAPIWithValidChecksAndNoSessionRequiredShouldCallDirectInvokeAPI() throws Throwable {
        // given:
        final String apiInterfaceName = "apiInterfaceName";
        final String methodName = "noSessionRequiredMethod";
        final List<String> classNameParameters = new ArrayList<String>();
        final Object[] parametersValues = null;
        Session session = new APISessionImpl(1L, new Date(), 120L, "userName", 5487L, "mon_tenant", 25L);

        APIAccessResolver accessResolver = mock(APIAccessResolver.class);
        FakeAPI apiImpl = new FakeAPI();
        when(accessResolver.getAPIImplementation(apiInterfaceName)).thenReturn(apiImpl);
        final ServerAPIImpl mockedServerAPIImpl = PowerMockito.spy(new ServerAPIImpl(true, accessResolver));
        doNothing().when(mockedServerAPIImpl).checkMethodAccessibility(any(), eq(apiInterfaceName), any(Method.class), eq(session), eq(false));
        doReturn(null).when(mockedServerAPIImpl).invokeAPI(parametersValues, apiImpl, FakeAPI.class.getMethod(methodName));

        try {
            // when:
            mockedServerAPIImpl.invokeAPI(apiInterfaceName, methodName, classNameParameters, parametersValues, session);
        } finally {
            // then:
            verify(mockedServerAPIImpl).invokeAPI(parametersValues, apiImpl, FakeAPI.class.getMethod(methodName));
            verify(mockedServerAPIImpl, never()).invokeAPIInTransaction(any(Object[].class), anyString(), any(Method.class), any(Session.class), anyString());
        }
    }

    @Test
    public void invokeAPIWithValidChecksAndNoAnnotationshouldCallTransactionalInvokeAPI() throws Throwable {
        // given:
        final String apiInterfaceName = "apiInterfaceName";
        final String methodName = "notAnnotatedMethod";
        final List<String> classNameParameters = new ArrayList<String>();
        final Object[] parametersValues = null;
        Session session = new APISessionImpl(1L, new Date(), 120L, "userName", 5487L, "mon_tenant", 25L);

        APIAccessResolver accessResolver = mock(APIAccessResolver.class);
        FakeAPI apiImpl = new FakeAPI();
        when(accessResolver.getAPIImplementation(apiInterfaceName)).thenReturn(apiImpl);
        final ServerAPIImpl mockedServerAPIImpl = PowerMockito.spy(new ServerAPIImpl(true, accessResolver));
        doNothing().when(mockedServerAPIImpl).checkMethodAccessibility(any(), eq(apiInterfaceName), any(Method.class), eq(session), eq(false));
        doReturn(null).when(mockedServerAPIImpl).invokeAPIInTransaction(parametersValues, apiImpl, FakeAPI.class.getMethod(methodName), session, apiInterfaceName);

        try {
            // when:
            mockedServerAPIImpl.invokeAPI(apiInterfaceName, methodName, classNameParameters, parametersValues, session);
        } finally {
            // then:
            verify(mockedServerAPIImpl).invokeAPIInTransaction(parametersValues, apiImpl, FakeAPI.class.getMethod(methodName), session, apiInterfaceName);
            verify(mockedServerAPIImpl, never()).invokeAPI(any(Object[].class), anyString(), any(Method.class));
        }
    }

}
