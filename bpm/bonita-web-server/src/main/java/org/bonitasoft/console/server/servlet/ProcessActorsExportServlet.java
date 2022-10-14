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
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Chong Zhao
 */
public class ProcessActorsExportServlet extends HttpServlet {

    private static final String EXPORT_FILE_SUFFIX = "_Process_Actors.xml";

    private static final String PROCESS_ID = "processId";

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessActorsExportServlet.class.getName());

    /**
     * UID
     */
    private static final long serialVersionUID = -1463938254928196006L;

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
        final String processId = request.getParameter(PROCESS_ID);
        final APISession apiSession = (APISession) request.getSession().getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
        OutputStream out = null;
        try {
            final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(apiSession);
            final String actorContent = processAPI.exportActorMapping(Long.parseLong(processId));

            // Set response headers
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/octet-stream");
            final String encodedfileName = URLEncoder.encode(processId + EXPORT_FILE_SUFFIX, StandardCharsets.UTF_8);
            final String userAgent = request.getHeader("User-Agent");
            if (userAgent != null && userAgent.contains("Firefox")) {
                response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedfileName);
            } else {
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + encodedfileName.replaceAll("\\_", " ") + "\"; filename*=UTF-8''"
                                + encodedfileName);
            }

            out = response.getOutputStream();

            if (actorContent == null) {
                response.setContentLength(0);
            } else {
                response.setContentLength(actorContent.length());
            }
            out.write(actorContent.getBytes());
            out.flush();

        } catch (final InvalidSessionException e) {
            final String message = "Session expires. Please login again.";
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(message, e);
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

}
