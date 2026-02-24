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
package org.bonitasoft.web.rest.server;

import java.util.List;
import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.engine.Engine;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.routing.Router;

/**
 * @author Matthieu Chaffotte
 */
public class BonitaRestletApplication extends Application {

    private final FinderFactory factory;

    public BonitaRestletApplication(final FinderFactory finderFactory, ConverterHelper converterHelper) {
        super();
        factory = finderFactory;
        getMetadataService().setDefaultMediaType(MediaType.APPLICATION_JSON);
        getMetadataService().setDefaultCharacterSet(CharacterSet.UTF_8);
        replaceJacksonConverter(converterHelper);
    }

    private void replaceJacksonConverter(ConverterHelper converterHelper) {
        final List<ConverterHelper> registeredConverters = Engine.getInstance().getRegisteredConverters();
        registeredConverters.add(converterHelper);
        for (ConverterHelper registeredConverter : registeredConverters) {
            if (registeredConverter.getClass().equals(JacksonConverter.class)) {
                registeredConverters.remove(registeredConverter);
                registeredConverters.add(converterHelper);
            }
        }
    }

    /**
     * Creates a root Restlet that will receive all incoming calls.
     */
    @Override
    public synchronized Restlet createInboundRoot() {
        return buildRouter();
    }

    protected Router buildRouter() {
        final Context context = getContext();
        final Router router = new Router(context);
        // No more routes - all API endpoints have been migrated to Spring MVC controllers.
        // This Restlet application will be removed entirely in a follow-up PR.

        return router;
    }

    @Override
    public void handle(final Request request, final Response response) {
        request.setLoggable(false);
        Engine.setLogLevel(Level.OFF);
        Engine.setRestletLogLevel(Level.OFF);
        super.handle(request, response);
    }
}
