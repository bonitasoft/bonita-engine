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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Anthony Birembaut
 */
public abstract class ResourceServlet extends HttpServlet {

    /**
     * UID
     */
    private static final long serialVersionUID = -2103038794535737881L;

    /**
     * Logger
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ResourceServlet.class.getName());

    protected abstract String getResourceParameterName();

    protected abstract String getDefaultResourceName();

    protected abstract File getResourcesParentFolder();

    /**
     * Return null if there is no subfolder
     */
    protected abstract String getSubFolderName();

    protected ResourceLocationReader resourceLocationReader = new ResourceLocationReader();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final String fileName = resourceLocationReader.getResourceLocationFromRequest(request);
        String resourceName = request.getParameter(getResourceParameterName());
        if (resourceName == null) {
            resourceName = getDefaultResourceName();
        }
        try {
            getResourceFile(response, resourceName, fileName);
        } catch (final UnsupportedEncodingException e) {
            final String errorMessage = "UnsupportedEncodingException :" + e;
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(errorMessage);
            }
            throw new ServletException(errorMessage);
        }
    }

    /**
     * Get resource file.
     *
     * @throws ServletException
     * @throws UnsupportedEncodingException
     */
    protected void getResourceFile(final HttpServletResponse response, String resourceName, String fileName)
            throws ServletException, IOException {
        if (resourceName == null) {
            final String errorMessage = "Error while using the servlet to get a resource: the parameter "
                    + getResourceParameterName() + " is null.";
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(errorMessage);
            }
            throw new ServletException(errorMessage);
        }
        if (fileName == null) {
            final String errorMessage = "Error while using the servlet to get a resource: the parameter "
                    + ResourceLocationReader.LOCATION_PARAM + " is null.";
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(errorMessage);
            }
            throw new ServletException(errorMessage);
        }
        resourceName = URLDecoder.decode(resourceName, "UTF-8");
        fileName = URLDecoder.decode(fileName, "UTF-8");
        response.setCharacterEncoding("UTF-8");

        final File resourcesParentFolder = getResourcesFolder();
        final String subFolderName = getSubFolderName();
        String subFolderSuffix;
        if (subFolderName != null) {
            subFolderSuffix = File.separator + subFolderName;
        } else {
            subFolderSuffix = "";
        }
        try {
            final File resourceFolder = new File(resourcesParentFolder, resourceName + subFolderSuffix);
            final File file = new File(resourceFolder, fileName);
            final BonitaHomeFolderAccessor tenantFolder = new BonitaHomeFolderAccessor();
            if (!tenantFolder.isInFolder(resourceFolder, resourcesParentFolder)) {
                throw new ServletException("For security reasons, access to this file paths "
                        + resourceFolder.getAbsolutePath() + " is restricted.");
            }
            if (!tenantFolder.isInFolder(file, resourceFolder)) {
                throw new ServletException("For security reasons, access to this file paths " + file.getAbsolutePath()
                        + " is restricted.");
            }

            byte[] content;
            String contentType;

            final String lowerCaseFileName = fileName.toLowerCase();
            if (lowerCaseFileName.endsWith(".jpg")) {
                contentType = "image/jpeg";
            } else if (lowerCaseFileName.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (lowerCaseFileName.endsWith(".gif")) {
                contentType = "image/gif";
            } else if (lowerCaseFileName.endsWith(".png")) {
                contentType = "image/png";
            } else if (lowerCaseFileName.endsWith(".css") || lowerCaseFileName.endsWith(".less")) {
                contentType = "text/css";
            } else if (lowerCaseFileName.endsWith(".js")) {
                contentType = "application/x-javascript";
            } else if (lowerCaseFileName.endsWith(".html")) {
                contentType = "text/html; charset=UTF-8";
            } else if (lowerCaseFileName.endsWith(".htc")) {
                contentType = "text/x-component";
            } else if (lowerCaseFileName.endsWith(".svg")) {
                contentType = "image/svg+xml";
            } else if (lowerCaseFileName.endsWith(".eot")) {
                contentType = "application/vnd.ms-fontobject";
            } else if (lowerCaseFileName.endsWith(".woff")) {
                contentType = "application/x-font-woff";
            } else if (lowerCaseFileName.endsWith(".ttf")) {
                contentType = "application/x-font-ttf";
            } else if (lowerCaseFileName.endsWith(".otf")) {
                contentType = "application/x-font-opentype";
            } else {
                final FileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
                contentType = mimetypesFileTypeMap.getContentType(file);
            }
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            content = FileUtils.readFileToByteArray(file);
            response.setContentType(contentType);
            response.setContentLength(content.length);
            response.setBufferSize(content.length);
            final OutputStream out = response.getOutputStream();
            out.write(content, 0, content.length);
            response.flushBuffer();
            out.close();
        } catch (FileNotFoundException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(e.getMessage());
            }
            response.sendError(404);
        } catch (final IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while generating the response.", e);
            }
            throw new ServletException(e.getMessage(), e);
        }
    }

    protected File getResourcesFolder() throws ServletException {
        try {
            return getResourcesParentFolder();
        } catch (final RuntimeException e) {
            final String errorMessage = "Error while using the servlet to get parent folder.";
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn(errorMessage);
            }
            throw new ServletException(errorMessage);
        }
    }

}
