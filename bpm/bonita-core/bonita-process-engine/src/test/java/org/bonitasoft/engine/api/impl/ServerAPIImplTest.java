/**
 * Copyright (C) 2013-2014 Bonitasoft S.A.
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

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.exception.BonitaContextException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeConfigurationException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.APIAccessResolver;
import org.bonitasoft.engine.session.Session;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.runners.MockitoJUnitRunner;

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
		try{
			serverAPIImpl.invokeMethod(options , apiInterfaceName, methodName, classNameParameters, parametersValues);
		} finally {
			verify(technicalLogger, VerificationModeFactory.atLeastOnce()).log(any(Class.class), eq(TechnicalLogSeverity.DEBUG), Mockito.any(Throwable.class));
		}
	}
    
    @Test
    public void should_BonitaException_caught_by_invokeMethod_on_beforeInvokeMethod_set_context_information() throws Exception {

        ServerAPIImpl serverAPIImpl = spy(new ServerAPIImpl(true, accessResolver));

        final BonitaHomeNotSetException bonitaException = mock(BonitaHomeNotSetException.class);
        doThrow(bonitaException).when(serverAPIImpl).beforeInvokeMethod(any(Session.class), anyString());


        Map<String, Serializable> options = new HashMap<String, Serializable>();
        String apiInterfaceName = null;
        String methodName = null;
        List<String> classNameParameters = null;
        Object[] parametersValues = null;
        try {
            serverAPIImpl.invokeMethod(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
            fail("An exception should have been thrown.");
        } catch (ServerWrappedException e) {
            verify(bonitaException).setHostname(anyString());
        }
    }

    @Test
    public void testGlobalContextFilledForBonitaException() throws Throwable {
        testContextFilledFor(new BonitaException(""));
    }

    @Test
    public void testGlobalContextFilledForThrowable() throws BonitaHomeNotSetException, BonitaHomeConfigurationException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, NoSuchMethodException, InvocationTargetException, SBonitaException, Throwable{
    	testContextFilledFor(new Throwable());
    }
        
    @Test
    public void testGlobalContextFilledForBonitaRuntimeException() throws BonitaHomeNotSetException, BonitaHomeConfigurationException, InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, NoSuchMethodException, InvocationTargetException, SBonitaException, Throwable{
    	testContextFilledFor(new BonitaRuntimeException(""));
    }
    
	private void testContextFilledFor(Throwable throwable)
			throws BonitaHomeNotSetException, InstantiationException,
			IllegalAccessException, ClassNotFoundException,
			BonitaHomeConfigurationException, IOException,
			NoSuchMethodException, InvocationTargetException, SBonitaException,
			Throwable {
		ServerAPIImpl serverAPIImpl = spy(new ServerAPIImpl(true, accessResolver));

        doReturn(sessionAccessor).when(serverAPIImpl).beforeInvokeMethod(any(Session.class), anyString());
        doReturn(15L).when(sessionAccessor).getTenantId();

        Map<String, Serializable> options = new HashMap<String, Serializable>();
        final Session session = mock(Session.class);
        doReturn("userName").when(session).getUserName();
        options.put("session", session);

        doThrow(throwable).when(serverAPIImpl).invokeAPI(anyString(), anyString(), anyListOf(String.class), any(Class[].class), eq(session));
        
        String apiInterfaceName = null;
        String methodName = null;
        List<String> classNameParameters = null;
        Object[] parametersValues = null;
        try {
            serverAPIImpl.invokeMethod(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
            fail("An exception should have been thrown.");
        } catch (ServerWrappedException e) {
        	BonitaContextException bce = (BonitaContextException) e.getCause();
        	Assert.assertTrue("Tenant id was not set ", bce.getTenantId() > 0);
        	Assert.assertEquals("Username was not set correctly", "userName", bce.getUserName());
        	Assert.assertTrue("Hostname was not set", !bce.getHostname().isEmpty());
        	Assert.assertTrue("Thread Id was not set", bce.getThreadId() > 0);
        }
	}
	
	@Test
	public void testAPICallTrace() throws Throwable{
		final String apiInterfaceName = "apiInterfaceName";
		final String methodName = "methodName";
		final List<String> classNameParameters = new ArrayList<String>();
		final Object[] parametersValues = null;

		ServerAPIImpl serverAPIImpl = spy(new ServerAPIImpl(true, accessResolver));  	
		doReturn(sessionAccessor).when(serverAPIImpl).beforeInvokeMethod(session, apiInterfaceName);
		final TechnicalLoggerService technicalLogger = mock(TechnicalLoggerService.class);
		doReturn(true).when(technicalLogger).isLoggable(any(Class.class), eq(TechnicalLogSeverity.TRACE));
		doReturn(new Object()).when(serverAPIImpl).invokeAPI(apiInterfaceName, methodName, classNameParameters, parametersValues, session);
		
		
		serverAPIImpl.setTechnicalLogger(technicalLogger);
		final Map<String, Serializable> options = new HashMap<String, Serializable>();
		options.put("session", session);
		try{
			serverAPIImpl.invokeMethod(options , apiInterfaceName, methodName, classNameParameters, parametersValues);
		} finally {
			verify(technicalLogger, VerificationModeFactory.atLeast(2)).log(any(Class.class), eq(TechnicalLogSeverity.TRACE), Mockito.any(String.class));
		}
	}
	
}
