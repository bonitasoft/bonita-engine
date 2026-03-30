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
import java.io.OutputStream;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.ArchivedDocument;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessResourceNotFoundException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.io.FileContent;
import org.bonitasoft.engine.session.APISession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet allowing to view process instances attachments as images
 * TODO refactor to remove duplicate code with {@link DocumentDownloadServlet}
 *
 * @author Anthony Birembaut
 */
public class DocumentImageServlet extends DocumentDownloadServlet {

    /**
     * UID
     */
    private static final long serialVersionUID = -2397573068771431608L;

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentImageServlet.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        final String filePath = request.getParameter(FILE_PATH_PARAM);
        String fileName = request.getParameter(FILE_NAME_PARAM);
        final String resourcePath = request.getParameter(RESOURCE_FILE_NAME_PARAM);
        final String documentId = request.getParameter(DOCUMENT_ID_PARAM);
        final APISession apiSession = (APISession) request.getSession().getAttribute(API_SESSION_PARAM_KEY);
        byte[] content = null;
        if (filePath != null) {
            try {
                final FileContent fileContent = bonitaHomeFolderAccessor
                        .retrieveUploadedTempContent(FilenameUtils.separatorsToSystem(filePath));
                if (fileName == null) {
                    fileName = fileContent.getFileName();
                }
                try (InputStream inputStream = fileContent.getInputStream()) {
                    content = getFileContent(inputStream, filePath, fileContent.getSize());
                }
            } catch (final BonitaException e) {
                throw new ServletException(e.getMessage());
            } catch (final IOException e) {
                throw new ServletException(e);
            }
        } else if (documentId != null) {
            try {
                final ProcessAPI processAPI = bpmEngineAPIUtil.getProcessAPI(apiSession);
                String contentStorageId;
                try {
                    final Document document = processAPI.getDocument(Long.valueOf(documentId));
                    fileName = document.getContentFileName();
                    contentStorageId = document.getContentStorageId();
                } catch (final DocumentNotFoundException dnfe) {
                    final ArchivedDocument archivedDocument = processAPI
                            .getArchivedVersionOfProcessDocument(Long.valueOf(documentId));
                    fileName = archivedDocument.getContentFileName();
                    contentStorageId = archivedDocument.getContentStorageId();
                }
                if (contentStorageId != null && !contentStorageId.isEmpty()) {
                    content = processAPI.getDocumentContent(contentStorageId);
                }
            } catch (final Exception e) {
                final String errorMessage = "Error while retrieving the document  with ID " + documentId
                        + " from the engine.";
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(errorMessage, e);
                }
                throw new ServletException(errorMessage, e);
            }
        } else if (resourcePath != null) {
            final String processIDStr = request.getParameter(PROCESS_ID_PARAM);
            final String instanceIDStr = request.getParameter(INSTANCE_ID_PARAM);
            final String taskIdStr = request.getParameter(TASK_ID_PARAM);
            long processDefinitionID = -1;
            try {
                if (processIDStr != null) {
                    processDefinitionID = Long.parseLong(processIDStr);
                } else if (taskIdStr != null) {
                    processDefinitionID = getProcessDefinitionIDFromActivityInstanceID(apiSession,
                            Long.parseLong(taskIdStr));
                } else if (instanceIDStr != null) {
                    processDefinitionID = getProcessDefinitionIDFromProcessInstanceID(apiSession,
                            Long.parseLong(instanceIDStr));
                } else {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Either a process, instance or task parameter is required in the URL");
                    return;
                }
                final ProcessAPI processAPI = bpmEngineAPIUtil.getProcessAPI(apiSession);
                content = processAPI.getDocumentProcessResource(processDefinitionID, resourcePath);
                fileName = resourcePath.contains("/")
                        ? resourcePath.substring(resourcePath.lastIndexOf('/') + 1)
                        : resourcePath;
            } catch (final ProcessResourceNotFoundException e) {
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn(e.getMessage());
                }
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            } catch (final Exception e) {
                final String errorMessage = "Error while retrieving the resource " + resourcePath;
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(errorMessage, e);
                }
                throw new ServletException(errorMessage, e);
            }
        } else {
            final String errorMessage = "Error while getting the file. either a document, a filePath or a resourcePath parameter is required.";
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(errorMessage);
            }
            throw new ServletException(errorMessage);
        }
        final String contentType = URLConnection.guessContentTypeFromName(fileName);
        if (contentType != null) {
            response.setContentType(contentType);
        }
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        if (fileName != null) {
            try {
                final String encodedfileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+",
                        "%20");
                final String userAgent = request.getHeader("User-Agent");
                if (userAgent != null && userAgent.contains("Firefox")) {
                    response.setHeader("Content-Disposition", "inline; filename*=UTF-8''" + encodedfileName);
                } else {
                    response.setHeader("Content-Disposition",
                            "inline; filename=\"" + encodedfileName + "\"; filename*=UTF-8''"
                                    + encodedfileName);
                }
                if (content != null) {
                    response.setContentLength(content.length);
                    OutputStream out = response.getOutputStream();
                    out.write(content);
                }
            } catch (final IOException e) {
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error("Error while generating the response.", e);
                }
                throw new ServletException(e.getMessage(), e);
            }
        }
    }
}
