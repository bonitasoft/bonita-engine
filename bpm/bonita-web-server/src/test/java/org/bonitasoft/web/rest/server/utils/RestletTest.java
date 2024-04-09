/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.web.rest.server.BonitaRestletApplication;
import org.bonitasoft.web.rest.server.FinderFactory;
import org.junit.After;
import org.junit.Before;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

/**
 * Restlet test base class.
 * <ul>
 * <li>Allow to configure tested resource via {@link #configureResource()}</li>
 * <li>Allow to configure tested app via {@link #configureApplication()}</li>
 * <li>Start Restlet server bound on a random port before each tests (and stop it after)</li>
 * <li>Provide {@link #request(String)} method to perform HTTP requests</li>
 * </ul>
 *
 * @author Colin Puy
 */
public abstract class RestletTest {

    private Component component;
    private String baseUri;

    /**
     * Configure the {@link ServerResource} under test.
     * Override this method in your test to return an instance of the tested resource.
     * If not overridden, production RestletApplication will be launched
     */
    protected ServerResource configureResource() {
        return null;
    }

    /**
     * Configure the {@link Application} under test.
     * Override this method to fully configure app under test launched by server
     */
    protected Application configureApplication() {
        final ServerResource resource = configureResource();
        final FinderFactory finderFactory = getFinderFactory(resource);
        return new BonitaRestletApplication(finderFactory, new BonitaJacksonConverter());
    }

    /**
     * Create a request builder with server baseUri as base path
     *
     * @param path absolute path to map request on
     */
    protected RequestBuilder request(final String path) {
        return new RequestBuilder(baseUri + path);
    }

    /**
     * Read a file in current package
     */
    protected String readFile(final String fileName) throws IOException {
        try (InputStream resourceAsStream = this.getClass().getResourceAsStream(fileName)) {
            return IOUtils.toString(resourceAsStream);
        }
    }

    @Before
    public void startServer() throws Exception {
        baseUri = start();
    }

    private String start() throws Exception {
        component = new Component();
        final Server server = component.getServers().add(Protocol.HTTP, 0);
        // server.getContext().getParameters().add("tracing", "true");
        final Application application = configureApplication();
        component.getDefaultHost().attach(application);
        component.start();
        return "http://localhost:" + server.getEphemeralPort();
    }

    @After
    public void stopServer() throws Exception {
        stop();
    }

    private void stop() throws Exception {
        if (component != null && component.isStarted()) {
            component.stop();
        }
        component = null;
    }

    protected FinderFactory getFinderFactory(final ServerResource resource) {
        if (resource != null) {
            return new MockFinderFactory(resource);
        } else {
            return new FinderFactory();
        }
    }

    protected String getJson(String jsonFile) throws Exception {
        return new String(IOUtils.toByteArray(this.getClass().getResourceAsStream(jsonFile)));
    }

    protected class MockFinderFactory extends FinderFactory {

        private final ServerResource serverResource;

        public MockFinderFactory(final ServerResource serverResource) {
            this.serverResource = serverResource;
        }

        @Override
        public Finder create(final Class<? extends ServerResource> clazz) {
            if (clazz.equals(serverResource.getClass())) {
                return new Finder() {

                    @Override
                    public ServerResource create(final Request request, final Response response) {
                        return serverResource;
                    }
                };
            }
            return super.create(clazz);
        }
    }

}
