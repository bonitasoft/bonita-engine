/**
 * Copyright (C) 2020 Bonitasoft S.A.
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
package org.bonitasoft.engine.test.http;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.internal.servlet.HttpAPIServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * Handles a jetty HTTP server and registers Bonita APIs in HTTP mode,
 * if tests are requested to be configured that way.
 * To request that, start Engine with system property '-Dorg.bonitasoft.engine.access.mode=http'.
 *
 * @author Emmanuel Duchastenier
 */
@Slf4j
public class JettyServer {

    private Server server;

    public void start() throws Exception {
        server = startJettyServer();
    }

    public Server startJettyServer() throws Exception {
        // Running our HttpAPIServlet in a Jetty server:
        Server jettyServer = new Server(0);// 0=random port
        ServletHandler handler = new ServletHandler();
        jettyServer.setHandler(handler);
        handler.addServletWithMapping(HttpAPIServlet.class, "/bonita/serverAPI/*");
        jettyServer.start();
        return jettyServer;
    }

    public void stop() throws Exception {
        if (server != null) {
            server.stop();
            server.join();
        }
    }

    Server getServer() {
        return server;
    }
}
