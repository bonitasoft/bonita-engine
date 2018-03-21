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
package org.bonitasoft.engine.api;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Emmanuel Duchastenier
 * @author Ludovic Bertin
 */
public class HTTPServerAPIIT {

    private static final String APPLICATION_NAME = "HTTPServerAPITest";

    private static String baseResourceUrl;

    private static Server server;

    private final Map<String, Serializable> options = new HashMap<String, Serializable>();

    private final String apiInterfaceName = "someInterface";

    private final String methodName = "someMethod";

    final List<String> classNameParameters = new ArrayList<String>();

    final Object[] parametersValues = null;

    @BeforeClass
    public static void startJetty() throws Exception {
        // Launch server on random port
        server = new Server(0);

        // Simple login service
        URL realmPropertyURL = HTTPServerAPIIT.class.getClassLoader().getResource("myRealm.properties");
        LoginService loginService = new HashLoginService("MyRealm", realmPropertyURL.toString());
        server.addBean(loginService);

        // Security handler.
        ConstraintSecurityHandler security = new ConstraintSecurityHandler();
        server.setHandler(security);

        // Add security constraint
        Constraint constraint = new Constraint();
        constraint.setName("auth");
        constraint.setAuthenticate(true);
        constraint.setRoles(new String[] { "user", "admin" });

        // Bind contraint
        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setPathSpec("/*");
        mapping.setConstraint(constraint);

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
        int actualPort = server.getConnectors()[0].getLocalPort();
        baseResourceUrl = "http://localhost:" + actualPort;
    }

    @AfterClass
    public static void stopJetty() throws Exception {
        server.stop();
    }

    @Test
    public void invokeMethodWithBasicAuthentication() throws Exception {
        Map<String, String> configuration = new HashMap<String, String>();
        configuration.put("server.url", baseResourceUrl);
        configuration.put("application.name", APPLICATION_NAME);
        configuration.put("basicAuthentication.active", "true");
        configuration.put("basicAuthentication.username", "john");
        configuration.put("basicAuthentication.password", "doe");

        final HTTPServerAPI httpServerAPI = new HTTPServerAPI(configuration);
        httpServerAPI.invokeMethod(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
    }

    @Test(expected = ServerWrappedException.class)
    public void invokeBasicAuthenticationWithWrongCredentialsShouldFail() throws Exception {
        Map<String, String> configuration = new HashMap<String, String>();
        configuration.put("server.url", baseResourceUrl);
        configuration.put("application.name", APPLICATION_NAME);
        configuration.put("basicAuthentication.active", "true");
        configuration.put("basicAuthentication.username", "john");
        configuration.put("basicAuthentication.password", "__LENNON__");

        final HTTPServerAPI httpServerAPI = new HTTPServerAPI(configuration);
        httpServerAPI.invokeMethod(options, apiInterfaceName, methodName, classNameParameters, parametersValues);
    }

    private static final class BonitaHandler extends AbstractHandler {

        @Override
        public void handle(final String s, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) {
            assertEquals("john", request.getUserPrincipal().getName());
            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);
        }
    }

}
