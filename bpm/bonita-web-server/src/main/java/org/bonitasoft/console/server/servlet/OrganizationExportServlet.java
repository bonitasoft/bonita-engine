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
package org.bonitasoft.console.server.servlet;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chong Zhao
 */
public class OrganizationExportServlet extends HttpServlet {

    private static final String EXPORT_FILE_NAME = "Organization_Data.xml";

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationExportServlet.class.getName());

    /**
     * UID
     */
    private static final long serialVersionUID = 7203686892997001991L;

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {

        final APISession apiSession = (APISession) request.getSession().getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        OutputStream out = null;
        try {
            final String organizationContent = getIdentityAPI(apiSession).exportOrganization();

            // Set response headers
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/octet-stream");
            final String encodedfileName = URLEncoder.encode(EXPORT_FILE_NAME, StandardCharsets.UTF_8);
            final String userAgent = request.getHeader("User-Agent");
            if (userAgent != null && userAgent.contains("Firefox")) {
                response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedfileName);
            } else {
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + encodedfileName.replaceAll("\\_", " ") + "\"; filename*=UTF-8''"
                                + encodedfileName);
            }

            out = response.getOutputStream();

            if (organizationContent == null) {
                response.setContentLength(0);
            } else {
                response.setContentLength(organizationContent.getBytes().length);
            }
            out.write(organizationContent.getBytes());
            out.flush();

        } catch (final InvalidSessionException e) {
            final String message = "Session expired. Please log in again.";
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(message, e);
            }
            throw new ServletException(e.getMessage(), e);
        } catch (final Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(e.getMessage(), e);
            }
            throw new ServletException(e.getMessage(), e);
        } finally {
            try {
                out.close();
            } catch (final Exception e) {
                out = null;
            }
        }

    }

    private IdentityAPI getIdentityAPI(final APISession apiSession)
            throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException,
            UnknownAPITypeException {
        return TenantAPIAccessor.getIdentityAPI(apiSession);
    }

}
