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

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Conditions;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;

public class RequestBuilder {

    private Request request;

    public RequestBuilder(final String uri) {
        request = new Request();
        request.setResourceRef(uri);
    }

    public RequestBuilder setConditions(Conditions conditions) {
        request.setConditions(conditions);
        return this;
    }

    public Response get() {
        final Client client = new Client(Protocol.HTTP);
        request.setMethod(Method.GET);
        return client.handle(request);
    }

    public Response post(final String value) {
        final Client client = new Client(Protocol.HTTP);
        request.setMethod(Method.POST);
        request.setEntity(value, MediaType.APPLICATION_JSON);
        return client.handle(request);
    }

    public Response put(final String value) {
        final Client client = new Client(Protocol.HTTP);
        request.setMethod(Method.PUT);
        request.setEntity(value, MediaType.APPLICATION_JSON);
        return client.handle(request);
    }

    public Response delete() {
        final Client client = new Client(Protocol.HTTP);
        request.setMethod(Method.DELETE);
        return client.handle(request);
    }

}
