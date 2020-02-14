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

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.api.ApiAccessType;
import org.bonitasoft.engine.util.APITypeManager;
import org.eclipse.jetty.server.ServerConnector;

/**
 * Handles a jetty HTTP server and registers Bonita APIs in HTTP mode,
 * if tests are requested to be configured that way.
 * To request that, start Engine with system property '-Dorg.bonitasoft.engine.access.mode=http'.
 *
 * @author Emmanuel Duchastenier
 */
@Slf4j
public class BonitaHttpServer {

    private JettyServer jettyServer;

    public void startIfNeeded() throws Exception {
        if ("http".equals(System.getProperty("org.bonitasoft.engine.access.mode", null))) {
            log.info("Starting Http Server for Bonita Engine...");
            jettyServer = new JettyServer();
            jettyServer.start();

            setClientApiToHTTP(((ServerConnector) jettyServer.getServer().getConnectors()[0]).getLocalPort());
        }
    }

    public static void setClientApiToHTTP(int localPort) {
        Map<String, String> params = new HashMap<>();
        params.put("server.url", "http://localhost:" + localPort);
        params.put("application.name", "bonita");
        APITypeManager.setAPITypeAndParams(ApiAccessType.HTTP, params);
    }

    public void stopIfNeeded() throws Exception {
        if (jettyServer != null) {
            jettyServer.stop();
        }
    }

}
