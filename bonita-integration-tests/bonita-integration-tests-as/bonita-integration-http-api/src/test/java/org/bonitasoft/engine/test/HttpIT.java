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

import static org.bonitasoft.engine.test.HttpServerEnvSetup.setClientApiToHTTP;
import static org.bonitasoft.engine.test.HttpServerEnvSetup.startJettyServer;

import org.bonitasoft.engine.BPMRemoteTestsForServers;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Baptiste Mesta
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ BPMRemoteTestsForServers.class })
public class HttpIT {

    private static TestEngine testEngine;
    private static Server jettyServer;

    @BeforeClass
    public static void setUpServerAndHttpApi() throws Exception {
        // Start engine first:
        testEngine = TestEngineImpl.getInstance();
        testEngine.start();

        jettyServer = startJettyServer();

        setClientApiToHTTP(((ServerConnector) HttpIT.jettyServer.getConnectors()[0]).getLocalPort());
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
