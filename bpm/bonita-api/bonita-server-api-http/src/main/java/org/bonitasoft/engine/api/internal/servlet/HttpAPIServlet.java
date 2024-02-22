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
import org.bonitasoft.engine.properties.BooleanProperty;

/**
 * @author Julien Mege
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 */
public class HttpAPIServlet extends HttpServlet {

    public static final String PROPERTY_TO_ENABLE_HTTP_API = "http.api";

    private static final long serialVersionUID = 4936475894513095747L;
    private BooleanProperty httpApi;

    @Override
    public void init() throws ServletException {
        httpApi = new BooleanProperty("Http API", PROPERTY_TO_ENABLE_HTTP_API, true);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp)
            throws ServletException, IOException {
        if (!httpApi.isEnabled()) {
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
