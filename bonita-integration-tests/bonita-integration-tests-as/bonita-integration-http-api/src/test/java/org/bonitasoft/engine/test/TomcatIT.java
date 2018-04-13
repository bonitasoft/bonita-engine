/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine.test;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.BPMRemoteTestsForServers;
import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.api.internal.servlet.HttpAPIServlet;
import org.bonitasoft.engine.util.APITypeManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Baptiste Mesta
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ BPMRemoteTestsForServers.class })
public class TomcatIT {

    private static TestEngine testEngine;
    private static Server jettyServer;

    @BeforeClass
    public static void setUpServerAndHttpApi() throws Exception {
        // Start engine first:
        testEngine = TestEngineImpl.getInstance();
        testEngine.start();

        // Running our HttpAPIServlet in a Jetty server:
        jettyServer = new Server(0);// 0=random port
        ServletHandler handler = new ServletHandler();
        jettyServer.setHandler(handler);
        handler.addServletWithMapping(HttpAPIServlet.class, "/bonita/serverAPI/*");
        jettyServer.start();

        setClientApiToHTTP(((ServerConnector) jettyServer.getConnectors()[0]).getLocalPort());
    }

    public static void setClientApiToHTTP(int localPort) {
        Map<String, String> params = new HashMap<>();
        params.put("server.url", "http://localhost:" + localPort);
        params.put("application.name", "bonita");
        APITypeManager.setAPITypeAndParams(ApiAccessType.HTTP, params);
    }

    @AfterClass
    public static void shutdownServer() throws Exception {
        try {
            testEngine.stop();
        } catch (Exception e) {
            // no need to fail the shutdown for that:
            e.printStackTrace();
        }

        jettyServer.stop();
        jettyServer.join();
    }

}
