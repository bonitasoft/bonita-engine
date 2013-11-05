/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.api;

import com.thoughtworks.xstream.XStream;
import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.exception.BonitaException;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.*;

/**
 * @author Celine Souchet
 * 
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.net.ssl.*")
@PrepareForTest({ HTTPServerAPI.class })
public class HTTPServerAPITest {

    private static final String APPLICATION_NAME = "HTTPServerAPITest";
    private static String baseResourceUrl;

    @BeforeClass
    public static void startJetty() throws Exception {
        // Launch server on random port
        Server server = new Server(0);

        // Simple login service
        URL realmPropertyURL = HTTPServerAPITest.class.getClassLoader().getResource("myRealm.properties");
        LoginService loginService = new HashLoginService("MyRealm",realmPropertyURL.toString());
        server.addBean(loginService);

        // Security handler.
        ConstraintSecurityHandler security = new ConstraintSecurityHandler();
        server.setHandler(security);

        // Add security constraint
        Constraint constraint = new Constraint();
        constraint.setName("auth");
        constraint.setAuthenticate( true );
        constraint.setRoles(new String[]{"user", "admin"});

        // Bind contraint
        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setPathSpec( "/*" );
        mapping.setConstraint( constraint );

        // Configure the security handler
        security.setConstraintMappings(Collections.singletonList(mapping));
        security.setAuthenticator(new BasicAuthenticator());
        security.setLoginService(loginService);
        security.setStrict(false);

        // Simulate Bonita engine part
        BonitaHandler bonitaHandler = new BonitaHandler();
        security.setHandler(bonitaHandler);

        // start server
        server.start();

        // retrieve port
        int actualPort = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
        baseResourceUrl = "http://localhost:" + actualPort;

    }

    @Mock
    private Map<String, String> parameters;

    @Before
    public void initialize() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public final void invokeMethod() throws Exception {
        // TODO : Not yet implemented
    }

    @Test(expected = ServerWrappedException.class)
    public void invokeMethodCatchUndeclaredThrowableException() throws Exception {
        final PrintStream printStream = System.err;
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setErr(new PrintStream(myOut));
        final Logger logger = Logger.getLogger(HTTPServerAPI.class.getName());
        logger.setLevel(Level.FINE);
        final ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.FINE);
        logger.addHandler(ch);

        try {
            final Map<String, Serializable> options = new HashMap<String, Serializable>();
            final String apiInterfaceName = "apiInterfaceName";
            final String methodName = "methodName";
            final List<String> classNameParameters = new ArrayList<String>();
            final Object[] parametersValues = null;

            final HTTPServerAPI httpServerAPI = mock(HTTPServerAPI.class);
            final String response = "response";
            doReturn(response).when(httpServerAPI, "executeHttpPost", eq(options), eq(apiInterfaceName), eq(methodName), eq(classNameParameters),
                    eq(parametersValues), Mockito.any(XStream.class));
            doThrow(new UndeclaredThrowableException(new BonitaException("Bonita exception"), "Exception plop")).when(httpServerAPI, "checkInvokeMethodReturn",
                    eq(response), Mockito.any(XStream.class));

            // Let's call it for real:
            doCallRealMethod().when(httpServerAPI).invokeMethod(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
            httpServerAPI.invokeMethod(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
        } finally {
            System.setErr(printStream);
            final String logs = myOut.toString();
            System.out.println(logs);
            assertTrue("should have written in logs an exception", logs.contains("java.lang.reflect.UndeclaredThrowableException"));
            assertTrue("should have written in logs an exception", logs.contains("BonitaException"));
            assertTrue("should have written in logs an exception", logs.contains("Exception plop"));
        }
    }

    @Test
    public void invokeMethodWithBasicAuthentication() throws Exception {
        final Map<String, Serializable> options = new HashMap<String, Serializable>();
        final String apiInterfaceName = "someInterface";
        final String methodName = "someMethode";
        final List<String> classNameParameters = new ArrayList<String>();
        final Object[] parametersValues = null;

        Map<String, String> configuration = new HashMap<String, String>();

        configuration.put("server.url",baseResourceUrl);
        configuration.put("application.name", APPLICATION_NAME);
        configuration.put("basicAuthentication.active","true");
        configuration.put("basicAuthentication.username","john");
        configuration.put("basicAuthentication.password","doe");

        final HTTPServerAPI httpServerAPI = new HTTPServerAPI(configuration);
        httpServerAPI.invokeMethod(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
    }

    @Test
    public final void serialize() throws Exception {
        // TODO : Not yet implemented
    }

    @Test
    public final void deserialize() throws Exception {
        // TODO : Not yet implemented
    }

    private static final class BonitaHandler extends AbstractHandler
    {
        public void handle(String s, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
        {
            assertEquals("john", request.getUserPrincipal().getName() );
            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
        }
    }

}
