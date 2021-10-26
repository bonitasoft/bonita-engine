/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.api.internal.servlet;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Julien Mege
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 */
public class HttpAPIServlet extends HttpServlet {

    public static final String PROPERTY_TO_ENABLE_HTTP_API = "http.api";
    private static final Logger logger = LoggerFactory.getLogger(HttpAPIServlet.class);

    private static final long serialVersionUID = 4936475894513095747L;
    private boolean enabled;

    @Override
    public void init() throws ServletException {
        enabled = Boolean.parseBoolean(
                System.getProperty(PROPERTY_TO_ENABLE_HTTP_API, System.getenv().getOrDefault(envProperty(), "true")));
        logger.info("Http API is {}, you may {} it using env property {} or System property {} [=true/false]",
                enabled ? "enabled" : "disabled", enabled ? "disable" : "enable", envProperty(),
                PROPERTY_TO_ENABLE_HTTP_API);
    }

    private String envProperty() {
        return PROPERTY_TO_ENABLE_HTTP_API.toUpperCase().replace(".", "_");
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        if (!enabled) {
            resp.sendError(SC_FORBIDDEN);
            return;
        }
        callHttpApi(req, resp);
    }

    void callHttpApi(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            new HttpAPIServletCall(req, resp).doPost();
        } catch (final FileUploadException e) {
            throw new ServletException(e);
        }
    }

}
