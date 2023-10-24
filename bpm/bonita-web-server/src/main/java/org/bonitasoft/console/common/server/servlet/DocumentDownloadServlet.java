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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.bonitasoft.console.common.server.utils.BPMEngineAPIUtil;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.console.common.server.utils.FormsResourcesUtils;
import org.bonitasoft.console.common.server.utils.UnauthorizedFolderException;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.document.ArchivedDocument;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.APISession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet allowing to download process instances attachments
 *
 * @author Anthony Birembaut
 */
public class DocumentDownloadServlet extends HttpServlet {

    /**
     * UID
     */
    private static final long serialVersionUID = 5209516978177786895L;

    /**
     * process ID
     */
    protected static final String PROCESS_ID_PARAM = "process";

    /**
     * instance ID
     */
    protected static final String INSTANCE_ID_PARAM = "instance";

    /**
     * task ID
     */
    protected static final String TASK_ID_PARAM = "task";

    /**
     * document id of the document to download
     */
    protected static final String DOCUMENT_ID_PARAM = "document";

    /**
     * attachment : indicate the path of the process attachment
     */
    protected static final String FILE_PATH_PARAM = "filePath";

    /**
     * attachment : indicate the file name of the process attachment
     */
    protected static final String FILE_NAME_PARAM = "fileName";

    /**
     * resource : indicate the file name of the process resource
     */
    protected static final String RESOURCE_FILE_NAME_PARAM = "resourceFileName";

    /**
     * The engine API session param key name
     */
    protected static final String API_SESSION_PARAM_KEY = "apiSession";

    /**
     * the name of the directory in which the resources are stored in the business archive (in /resources/forms)
     */
    protected static final String BUSINESS_ARCHIVE_RESOURCES_DIRECTORY = "documents";

    /**
     * content storage id of the document downloaded
     */
    protected static final String CONTENT_STORAGE_ID_PARAM = "contentStorageId";

    /**
     * Util class allowing to work with the BPM engine API
     */
    protected final BPMEngineAPIUtil bpmEngineAPIUtil = new BPMEngineAPIUtil();

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentDownloadServlet.class.getName());

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {

        final String filePath = request.getParameter(FILE_PATH_PARAM);
        String fileName = request.getParameter(FILE_NAME_PARAM);
        final String resourcePath = request.getParameter(RESOURCE_FILE_NAME_PARAM);
        final String documentId = request.getParameter(DOCUMENT_ID_PARAM);
        String contentStorageId = request.getParameter(CONTENT_STORAGE_ID_PARAM);
        final APISession apiSession = (APISession) request.getSession().getAttribute(API_SESSION_PARAM_KEY);
        byte[] fileContent = null;
        if (filePath != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("attachmentPath: " + filePath);
            }
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
        } else if (fileName != null && contentStorageId != null) {
            try {
                fileContent = bpmEngineAPIUtil.getProcessAPI(apiSession).getDocumentContent(contentStorageId);
            } catch (final Exception e) {
                final String errorMessage = "Error while retrieving the document  with content storage ID "
                        + contentStorageId + " from the engine.";
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(errorMessage, e);
                }
                throw new ServletException(errorMessage, e);
            }
        } else if (documentId != null) {
            try {
                final ProcessAPI processAPI = bpmEngineAPIUtil.getProcessAPI(apiSession);
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
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        try {
            final String encodedfileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
            final String userAgent = request.getHeader("User-Agent");
            if (userAgent != null && userAgent.contains("Firefox")) {
                response.setHeader("Content-Disposition",
                        "attachment; filename*=UTF-8''" + encodedfileName.replace("+", "%20"));
            } else {
                response.setHeader("Content-Disposition",
                        "attachment; filename=\"" + encodedfileName.replaceAll("\\+", " ") + "\"; filename*=UTF-8''"
                                + encodedfileName.replace("+", "%20"));
            }
            final OutputStream out = response.getOutputStream();
            if (fileContent == null) {
                response.setContentLength(0);
            } else {
                response.setContentLength(fileContent.length);
                out.write(fileContent);
            }
        } catch (final IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error while generating the response.", e);
            }
            throw new ServletException(e);
        }
    }

    protected byte[] getFileContent(final File file, final String filePath) throws ServletException {

        int fileLength = 0;
        if (file.length() > Integer.MAX_VALUE) {
            throw new ServletException("file " + filePath + " too big !");
        } else {
            fileLength = (int) file.length();
        }

        byte[] content;
        try {
            final InputStream fileInput = new FileInputStream(file);
            final byte[] fileContent = new byte[fileLength];
            try {
                int offset = 0;
                int length = fileLength;
                while (length > 0) {
                    final int read = fileInput.read(fileContent, offset, length);
                    if (read <= 0) {
                        break;
                    }
                    length -= read;
                    offset += read;
                }
                content = fileContent;
            } catch (final FileNotFoundException e) {
                final String errorMessage = "Error while getting the attachment. The file " + filePath
                        + " does not exist.";
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(errorMessage, e);
                }
                throw new ServletException(errorMessage, e);
            } finally {
                fileInput.close();
            }
        } catch (final IOException e) {
            final String errorMessage = "Error while reading attachment (file  : " + filePath;
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(errorMessage, e);
            }
            throw new ServletException(errorMessage, e);
        }
        return content;
    }

    protected long getProcessDefinitionIDFromActivityInstanceID(final APISession session, final long activityInstanceID)
            throws BonitaException {
        final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
        return processAPI.getProcessDefinitionIdFromActivityInstanceId(activityInstanceID);
    }

    protected long getProcessDefinitionIDFromProcessInstanceID(final APISession session, final long processInstanceID)
            throws BonitaException {
        final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
        return processAPI.getProcessDefinitionIdFromProcessInstanceId(processInstanceID);
    }

    protected Date getMigrationDate(final APISession session, final long processDefinitionID) throws BonitaException {
        final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
        final ProcessDeploymentInfo processDeploymentInfo = processAPI.getProcessDeploymentInfo(processDefinitionID);
        Date migrationDate = null;
        if (!processDeploymentInfo.getDeploymentDate().equals(processDeploymentInfo.getLastUpdateDate())) {
            migrationDate = processDeploymentInfo.getLastUpdateDate();
        }
        return migrationDate;
    }

    protected Date getProcessDefinitionDate(final APISession session, final long processDefinitionID)
            throws BonitaException {
        final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(session);
        return processAPI.getProcessDeploymentInfo(processDefinitionID).getDeploymentDate();
    }
}
