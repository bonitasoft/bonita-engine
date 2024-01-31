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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIMalformedUrlException;
import org.bonitasoft.web.toolkit.client.common.exception.http.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class IconServlet extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(IconServlet.class.getName());

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
        String iconIdPath = request.getPathInfo();
        if (iconIdPath == null || iconIdPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Long iconId = parseLong(iconIdPath);
        if (iconId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Optional<IconContent> iconContent = retrieveIcon(iconId,
                (APISession) request.getSession().getAttribute("apiSession"));
        if (!iconContent.isPresent()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType(iconContent.get().getMimeType());
        response.setCharacterEncoding("UTF-8");
        try {
            setHeaders(request, response, iconId);
        } catch (UnsupportedEncodingException e) {
            logAndThrowException(e, "Error while generating the headers.");
        }
        try {
            OutputStream out = response.getOutputStream();
            response.setContentLength(iconContent.get().getContent().length);
            out.write(iconContent.get().getContent());
        } catch (final IOException e) {
            logAndThrowException(e, "Error while generating the response.");
        }

    }

    @Override
    protected void doDelete(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        Long entityId = parseLong(pathInfo);
        if (entityId == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            deleteIcon(entityId, (APISession) request.getSession().getAttribute("apiSession"), request);
        } catch (APIItemNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        } catch (APIMalformedUrlException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (ServerException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }
        try {
            setHeaders(request, response, entityId);
        } catch (UnsupportedEncodingException e) {
            logAndThrowException(e, "Error while generating the headers.");
        }
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    protected abstract Optional<IconContent> retrieveIcon(Long iconId, APISession apiSession);

    protected abstract void deleteIcon(Long entityId, APISession apiSession, HttpServletRequest request)
            throws ServerException;

    private Long parseLong(String iconIdPath) {
        try {
            return Long.valueOf(iconIdPath.substring(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void logAndThrowException(IOException e, String msg) throws ServletException {
        LOGGER.error(msg, e);
        throw new ServletException(e.getMessage(), e);
    }

    private void setHeaders(HttpServletRequest request, HttpServletResponse response, Long iconId)
            throws UnsupportedEncodingException {
        final String encodedFileName = URLEncoder.encode(String.valueOf(iconId), StandardCharsets.UTF_8);
        final String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.contains("Firefox")) {
            response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + encodedFileName);
        } else {
            response.setHeader("Content-Disposition",
                    "inline; filename=\"" + encodedFileName.replaceAll("\\+", " ") + "\"; filename*=UTF-8''"
                            + encodedFileName);
        }
    }
}
