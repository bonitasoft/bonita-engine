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
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.console.common.server.utils.FormsResourcesUtils;
import org.bonitasoft.console.common.server.utils.UnauthorizedFolderException;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.document.ArchivedDocument;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
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
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {

        final String filePath = request.getParameter(FILE_PATH_PARAM);
        String fileName = request.getParameter(FILE_NAME_PARAM);
        final String resourcePath = request.getParameter(RESOURCE_FILE_NAME_PARAM);
        final String documentId = request.getParameter(DOCUMENT_ID_PARAM);
        final APISession apiSession = (APISession) request.getSession().getAttribute(API_SESSION_PARAM_KEY);
        byte[] fileContent = null;
        if (filePath != null) {
            final BonitaHomeFolderAccessor tempFolderAccessor = new BonitaHomeFolderAccessor();
            try {
                final File file = tempFolderAccessor.getTempFile(FilenameUtils.separatorsToSystem(filePath));
                if (fileName == null) {
                    fileName = file.getName();
                }
                fileContent = getFileContent(file, filePath);
            } catch (final UnauthorizedFolderException e) {
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
                    fileContent = processAPI.getDocumentContent(contentStorageId);
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
                    final String errorMessage = "Error while retrieving the resource " + resourcePath
                            + " : Either a process, instance or task is required in the URL";
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error(errorMessage);
                    }
                    throw new ServletException(errorMessage);
                }
                Date processDeployementDate = getMigrationDate(apiSession, processDefinitionID);
                if (processDeployementDate == null) {
                    processDeployementDate = getProcessDefinitionDate(apiSession, processDefinitionID);
                }
                final File processDir = FormsResourcesUtils.getApplicationResourceDir(apiSession, processDefinitionID,
                        processDeployementDate);
                final File resource = new File(processDir,
                        BUSINESS_ARCHIVE_RESOURCES_DIRECTORY + File.separator + resourcePath);
                if (resource.exists()) {
                    fileName = resource.getName();
                    fileContent = getFileContent(resource, filePath);
                } else {
                    final String errorMessage = "The target resource does not exist " + resource.getAbsolutePath();
                    if (LOGGER.isErrorEnabled()) {
                        LOGGER.error(errorMessage);
                    }
                    throw new IOException(errorMessage);
                }
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
                if (fileContent != null) {
                    response.setContentLength(fileContent.length);
                    OutputStream out = response.getOutputStream();
                    out.write(fileContent);
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
