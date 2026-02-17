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

import org.bonitasoft.web.rest.server.api.bpm.cases.ArchivedCaseVariableResource;
import org.bonitasoft.web.rest.server.api.bpm.cases.ArchivedCaseVariablesResource;
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
import org.restlet.routing.Template;

/**
 * @author Matthieu Chaffotte
 */
public class BonitaRestletApplication extends Application {

    public static final String ROUTER_EXTENSION_PREFIX = "/extension/";

    public static final String BPM_ARCHIVED_CASE_VARIABLE_URL = "/bpm/archivedCaseVariable";

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
        // WARNING: if you add a route you need to declare it in org.bonitasoft.web.rest.server.FinderFactory

        // api extension
        router.attach(ROUTER_EXTENSION_PREFIX, factory.createExtensionResource(), Template.MODE_STARTS_WITH);

        router.attach(BPM_ARCHIVED_CASE_VARIABLE_URL + "/{caseId}/{variableName}",
                factory.create(ArchivedCaseVariableResource.class));
        router.attach(BPM_ARCHIVED_CASE_VARIABLE_URL, factory.create(ArchivedCaseVariablesResource.class));

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
