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
package org.bonitasoft.web.toolkit.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.web.toolkit.client.common.exception.api.APIMalformedUrlException;
import org.bonitasoft.web.toolkit.client.common.json.JSonItemReader;
import org.bonitasoft.web.toolkit.client.common.json.JSonSerializer;
import org.bonitasoft.web.toolkit.client.common.json.JsonSerializable;

/**
 * This class represent a call to a service.
 *
 * @author Séverin Moussel
 */
public class ServiceServletCall extends ServletCall {

    private String calledToolToken;
    private final ServiceFactory serviceFactory;

    public ServiceServletCall(ServiceFactory serviceFactory, final HttpServletRequest request,
            final HttpServletResponse response) {
        super(request, response);
        this.serviceFactory = serviceFactory;
    }

    @Override
    protected void parseRequest(final HttpServletRequest request, HttpServletResponse response) {
        super.parseRequest(request, response);

        // Gather all the POST parameters
        final Map<String, String> postParams = JSonItemReader.parseMap(getInputStream());
        for (final Map.Entry<String, String> entry : postParams.entrySet()) {
            this.parameters.put(entry.getKey(), new String[] { entry.getValue() });
        }

        // Read TOOL tokens
        this.calledToolToken = request.getPathInfo();
        if (this.calledToolToken.length() == 0) {
            throw new APIMalformedUrlException(getRequestURL(), "Missing tool name");
        }
    }

    @Override
    public final void doGet() throws IOException {
        run();
    }

    @Override
    public final void doPost() throws IOException {
        run();
    }

    @Override
    public final void doPut() throws IOException {
        run();
    }

    @Override
    public final void doDelete() throws IOException {
        run();
    }

    /**
     * Instantiate and run the service.
     *
     * @throws IOException
     */
    private void run() throws IOException {
        final Service service = serviceFactory.getService(this.calledToolToken);
        service.setCaller(this);

        final Object response = service.run();

        if (response instanceof File) {
            output((File) response);
        } else if (response instanceof InputStream) {
            output((InputStream) response);
        } else if (response instanceof JsonSerializable || response instanceof Map<?, ?>
                || response instanceof List<?>) {
            output(JSonSerializer.serialize(response));
        } else if (response != null) {
            output(response.toString());
        }
    }

}
