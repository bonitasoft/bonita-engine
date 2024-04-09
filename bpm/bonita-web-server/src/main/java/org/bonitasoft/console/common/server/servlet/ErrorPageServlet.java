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
package org.bonitasoft.console.common.server.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.console.common.server.utils.PlatformManagementUtils;
import org.bonitasoft.console.common.server.utils.SessionUtil;
import org.bonitasoft.console.common.server.utils.TenantsManagementUtils;
import org.bonitasoft.engine.session.APISession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorPageServlet extends HttpServlet {

    /**
     * UID
     */
    private static final long serialVersionUID = -6981838056314293935L;

    /**
     * Path of the error page template
     */
    protected static final String ERROR_TEMPLATE_PATH = "/WEB-INF/errors.html";

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorPageServlet.class.getName());

    /**
     * Static variable to avoid reading the error HTML page every time the error page is displayed
     */
    private static String errorPageString = null;

    /**
     * {@inheritDoc}
     *
     * @throws IOException
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        response.setContentType("text/html");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try (PrintWriter output = response.getWriter()) {
            if (!StringUtils.isEmpty(pathInfo)) {
                String errorCode = pathInfo.substring(1);
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Displaying error page with code " + errorCode);
                }
                final APISession apiSession = (APISession) request.getSession()
                        .getAttribute(SessionUtil.API_SESSION_PARAM_KEY);
                if (apiSession != null && isPlatformHealthy()) {
                    String contextPath = request.getContextPath();
                    if (contextPath.equals("/")) {
                        //avoid double / in URL if Bonita is deployed at the root
                        contextPath = "";
                    }
                    if (errorPageString == null) {
                        ServletContext sc = getServletContext();
                        try (InputStream errorPageInputStream = sc.getResourceAsStream(ERROR_TEMPLATE_PATH)) {
                            errorPageString = new String(errorPageInputStream.readAllBytes(), StandardCharsets.UTF_8);
                        } catch (Exception e) {
                            if (LOGGER.isErrorEnabled()) {
                                LOGGER.error("Error while trying to get the error page.", e);
                            }
                            output.println("An Error occured.");
                        }
                    }
                    writeFormatedResponse(output, errorCode, contextPath);
                } else {
                    //if there is no session or if the platform is not available, fallback on generic error pages
                    getServletContext().getRequestDispatcher("/" + errorCode + ".jsp").forward(request, response);
                }
            } else {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Status code missing from request.");
                }
                output.println("Status code missing from request.");
            }
            output.flush();
        }
    }

    protected boolean isPlatformHealthy() {
        try {
            PlatformManagementUtils platformManagementUtils = new PlatformManagementUtils();
            return platformManagementUtils.isPlatformAvailable() && !TenantsManagementUtils.isDefaultTenantPaused();
        } catch (Exception e) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Platform is not healthy.");
            }
            return false;
        }
    }

    protected void writeFormatedResponse(PrintWriter output, String errorCode, String contextPath) throws IOException {
        try {
            output.format(errorPageString, errorCode, contextPath, errorCode);
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while trying to display the error page.", e);
            }
            output.println("An Error occured.");
        }

    }

}
