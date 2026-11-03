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
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.bonitasoft.console.common.server.filter.MultiReadHttpServletRequest;
import org.bonitasoft.console.common.server.preferences.properties.ConsoleProperties;
import org.bonitasoft.console.common.server.utils.DocumentUtil;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.TemporaryContentAPI;
import org.bonitasoft.engine.io.FileContent;
import org.bonitasoft.engine.session.SessionNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet allowing to upload a File.
 *
 * @author Julien Mege
 */
public abstract class FileUploadServlet extends HttpServlet {

    /**
     * UID
     */
    protected static final long serialVersionUID = -948661031179067420L;

    protected static final Logger LOGGER = LoggerFactory.getLogger(FileUploadServlet.class.getName());

    protected String uploadDirectoryPath = null;

    public static final String RESPONSE_SEPARATOR = "::";

    protected static final String SUPPORTED_EXTENSIONS_PARAM = "SupportedExtensions";

    protected static final String SUPPORTED_EXTENSIONS_SEPARATOR = ",";

    /**
     * Allowed extension meaning that any extension is allowed, files without an extension included. The content of
     * the extensions {@link DocumentUtil} knows is still checked.
     */
    protected static final String ANY_EXTENSION = "*";

    protected static final String EXTENSION_NOT_SUPPORTED_MESSAGE = "Extension not supported.";

    protected static final String CONTENT_MISMATCH_MESSAGE = "File content does not match its extension.";

    protected static final String RETURN_ORIGINAL_FILENAME_PARAM = "ReturnOriginalFilename";

    protected static final String CHECK_UPLOADED_FILE_SIZE = "CheckUploadedFileSize";

    protected static final String CHECK_UPLOADED_IMAGE_SIZE = "CheckUploadedImageSize";

    protected static final String RESPONSE_CONTENT_TYPE_PARAM = "ContentType";

    protected static final String TEXT_CONTENT_TYPE = "text";

    protected static final String JSON_CONTENT_TYPE = "json";

    protected static final String TEMP_PATH_RESPONSE_ATTRIBUTE = "tempPath";

    protected static final String FILE_NAME_RESPONSE_ATTRIBUTE = "filename";

    protected static final String CONTENT_TYPE_ATTRIBUTE = "contentType";

    public static final int MEGABYTE = 1048576;

    public static final int KILOBYTE = 1024;

    protected String[] supportedExtensionsList = new String[0];

    protected boolean alsoReturnOriginalFilename = false;

    protected boolean checkUploadedFileSize = false;

    protected boolean checkUploadedImageSize = false;

    protected String responseContentType = TEXT_CONTENT_TYPE;

    private ObjectMapper objectMapper = new ObjectMapper();
    protected TemporaryContentAPI temporaryContentAPI;

    @Override
    public void init() throws ServletException {
        final String supportedExtensionsParam = getInitParameter(SUPPORTED_EXTENSIONS_PARAM);
        if (supportedExtensionsParam != null) {
            supportedExtensionsList = supportedExtensionsParam.split(SUPPORTED_EXTENSIONS_SEPARATOR);
        }
        alsoReturnOriginalFilename = Boolean.parseBoolean(getInitParameter(RETURN_ORIGINAL_FILENAME_PARAM));
        final String responseContentTypeParam = getInitParameter(RESPONSE_CONTENT_TYPE_PARAM);
        if (responseContentTypeParam != null) {
            responseContentType = responseContentTypeParam;
        }
        checkUploadedFileSize = Boolean.parseBoolean(getInitParameter(CHECK_UPLOADED_FILE_SIZE));
        checkUploadedImageSize = Boolean.parseBoolean(getInitParameter(CHECK_UPLOADED_IMAGE_SIZE));
        temporaryContentAPI = getTemporaryContentAPI();
    }

    protected TemporaryContentAPI getTemporaryContentAPI() {
        try {
            return PlatformAPIAccessor.getTemporaryContentAPI();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void defineUploadDirectoryPath(final HttpServletRequest request) throws SessionNotFoundException;

    protected abstract void setUploadMaxSize(ServletFileUpload serviceFileUpload, final HttpServletRequest request);

    protected void setUploadDirectoryPath(final String uploadDirectoryPath) {
        this.uploadDirectoryPath = uploadDirectoryPath;
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter responsePW = null;
        try {
            defineUploadDirectoryPath(request);
            if (!ServletFileUpload.isMultipartContent(request)) {
                return;
            }

            final File targetDirectory = new File(uploadDirectoryPath);
            if (!targetDirectory.exists()) {
                targetDirectory.mkdirs();
            }

            responsePW = response.getWriter();

            final FileItemFactory fileItemFactory = new DiskFileItemFactory();
            final ServletFileUpload serviceFileUpload = createServletFileUpload(fileItemFactory);
            setUploadMaxSize(serviceFileUpload, request);
            List<FileItem> items;
            try {
                items = serviceFileUpload.parseRequest(request);
            } catch (final OutOfMemoryError e) {
                throw new SizeLimitExceededException("The file exceeds its maximum permitted size.", 0L, 0);
            }

            for (final FileItem item : items) {
                if (item.isFormField()) {
                    continue;
                }

                final String fileName = DocumentUtil.sanitizeFilename(item.getName());

                // Check if extension is allowed
                if (!isSupportedExtension(fileName)) {
                    logRejectedExtension(request, fileName);
                    outputMediaTypeError(response, responsePW, EXTENSION_NOT_SUPPORTED_MESSAGE);
                    return;
                }

                // Check that the declared Content-Type and the actual content match the extension (CVE-68)
                try (InputStream itemContent = item.getInputStream()) {
                    if (!DocumentUtil.isContentConsistentWithExtension(fileName, item.getContentType(),
                            itemContent)) {
                        LOGGER.warn("Upload of file '{}' to {} rejected: its content or its declared Content-Type "
                                + "'{}' does not match its extension.", forLog(fileName), request.getRequestURI(),
                                forLog(item.getContentType()));
                        outputMediaTypeError(response, responsePW, CONTENT_MISMATCH_MESSAGE);
                        return;
                    }
                }

                // Make unique file name
                final String uploadedFileKey = storeTempFile(fileName, item);

                // Response
                final String responseString;
                if (JSON_CONTENT_TYPE.equals(responseContentType)) {
                    responseString = generateResponseJson(request, fileName,
                            DocumentUtil.resolveContentType(fileName, item.getContentType()), uploadedFileKey);
                } else if (TEXT_CONTENT_TYPE.equals(responseContentType)) {
                    responseString = generateResponseString(request, fileName, uploadedFileKey);
                } else {
                    throw new ServletException(
                            "Unsupported content type in servlet configuration : " + responseContentType);
                }
                responsePW.print(responseString);
                responsePW.flush();
                break;
            }
        } catch (SessionNotFoundException e) {
            final String message = "Session expired";
            LOGGER.debug(message);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
        } catch (final SizeLimitExceededException e) {
            LOGGER.error("File is Too Big", e);
            generateFileTooBigError(response, responsePW, "Uploaded file is too large, server is unable to process it");
        } catch (final FileSizeLimitExceededException e) {
            LOGGER.error("File is Too Big", e);
            generateFileTooBigError(response, responsePW,
                    e.getFileName() + " is " + e.getActualSize() + " large, limit is set to " + e.getPermittedSize()
                            / FileUploadServlet.MEGABYTE + "Mb");
        } catch (final Exception e) {
            final String theErrorMessage = "Exception while uploading file.";
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(theErrorMessage, e);
            }
            throw new ServletException(theErrorMessage, e);
        } finally {
            //Clean multiread wrapper temp file if it exists, whether the upload succeeded or not
            if (request instanceof MultiReadHttpServletRequest) {
                ((MultiReadHttpServletRequest) request).cleanMultipartTempContent();
            }
            if (responsePW != null) {
                responsePW.close();
            }
        }
    }

    protected String storeTempFile(final String fileName, final FileItem item)
            throws Exception {
        temporaryContentAPI = getTemporaryContentAPI();
        return temporaryContentAPI.storeTempFile(new FileContent(fileName, item.getInputStream(),
                DocumentUtil.resolveContentType(fileName, item.getContentType())));
    }

    private void generateFileTooBigError(final HttpServletResponse response, final PrintWriter responsePW,
            final String message) throws JsonProcessingException {
        response.setStatus(HttpURLConnection.HTTP_ENTITY_TOO_LARGE);
        if (JSON_CONTENT_TYPE.equals(responseContentType)) {
            printJsonError(responsePW, "EntityTooLarge", message, HttpURLConnection.HTTP_ENTITY_TOO_LARGE);
        }
    }

    private void printJsonError(final PrintWriter responsePW, final String type, final String message,
            final int statusCode) throws JsonProcessingException {
        final Map<String, Serializable> errorResponse = new HashMap<>();
        errorResponse.put("type", type);
        errorResponse.put("message", message);
        errorResponse.put("statusCode", statusCode);
        responsePW.print(objectMapper.writeValueAsString(errorResponse));
        responsePW.flush();
    }

    //for test purpose
    protected ServletFileUpload createServletFileUpload(final FileItemFactory fileItemFactory) {
        return new ServletFileUpload(fileItemFactory);
    }

    protected String generateResponseString(final HttpServletRequest request, final String fileName,
            final String uploadedFileName) throws Exception {
        String responseString = uploadedFileName;
        if (alsoReturnOriginalFilename) {
            responseString = responseString + RESPONSE_SEPARATOR + getFilenameLastSegment(fileName);
        }
        return responseString;
    }

    protected String generateResponseJson(final HttpServletRequest request, final String fileName, String contentType,
            final String uploadedFileName) throws Exception {
        final Map<String, Serializable> responseMap = new HashMap<>();
        fillJsonResponseMap(request, responseMap, fileName, contentType, uploadedFileName);
        return objectMapper.writeValueAsString(responseMap);
    }

    protected void fillJsonResponseMap(HttpServletRequest request, final Map<String, Serializable> responseMap,
            final String fileName,
            final String contentType, final String uploadedFileName) {
        if (alsoReturnOriginalFilename) {
            responseMap.put(FILE_NAME_RESPONSE_ATTRIBUTE, getFilenameLastSegment(fileName));
        }
        responseMap.put(TEMP_PATH_RESPONSE_ATTRIBUTE, uploadedFileName);
        responseMap.put(CONTENT_TYPE_ATTRIBUTE, contentType);
    }

    protected String getFilenameLastSegment(final String fileName) {
        int slashPos = fileName.lastIndexOf("/");
        if (slashPos == -1) {
            slashPos = fileName.lastIndexOf("\\");
        }
        return fileName.substring(slashPos + 1);
    }

    /**
     * Rejects the upload with a 415 status. The message is shown to end users by the upload widget, so it must
     * not contain configuration details: those go to the server log.
     */
    protected void outputMediaTypeError(final HttpServletResponse response, final PrintWriter responsePW,
            final String message) throws JsonProcessingException {
        response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        if (JSON_CONTENT_TYPE.equals(responseContentType)) {
            printJsonError(responsePW, "UnsupportedMediaType", message, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        } else {
            responsePW.print(message);
            responsePW.flush();
        }
    }

    private void logRejectedExtension(final HttpServletRequest request, final String fileName) {
        final String configurationHint = supportedExtensionsList.length > 0 ? ""
                : " (property " + ConsoleProperties.ALLOWED_ATTACHMENT_EXTENSIONS + " of console-config.properties)";
        LOGGER.warn("Upload of file '{}' to {} rejected: its extension is not one of {}{}.", forLog(fileName),
                request.getRequestURI(), String.join(",", getAllowedExtensions()), configurationHint);
    }

    /**
     * Client-provided values are logged on a single line, so that they cannot forge log entries.
     */
    private static String forLog(final String value) {
        return value == null ? null : value.replaceAll("[\\r\\n]", "_");
    }

    protected boolean isSupportedExtension(final String fileName) {
        if (fileName == null) {
            return false;
        }

        final String[] extensions = getAllowedExtensions();

        // if no extension whitelist applies at all, every extension is allowed
        if (extensions.length < 1) {
            return true;
        }

        for (final String extension : extensions) {
            if (ANY_EXTENSION.equals(extension.trim())
                    || fileName.toLowerCase(Locale.ROOT).endsWith("." + extension.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }

        return false;
    }

    private String[] getAllowedExtensions() {
        return supportedExtensionsList.length > 0 ? supportedExtensionsList : getDefaultAllowedExtensions();
    }

    private static final String[] DEFAULT_ALLOWED_EXTENSIONS = DocumentUtil.DEFAULT_ALLOWED_EXTENSIONS
            .toArray(new String[0]);

    /**
     * Extension whitelist used when this servlet declares no {@value #SUPPORTED_EXTENSIONS_PARAM} init-param,
     * i.e. generic business file upload widgets/forms (see CVE-68). Defaults to
     * {@link DocumentUtil#DEFAULT_ALLOWED_EXTENSIONS}
     * (same fallback as {@code ConsoleProperties}, so the base servlet -- used directly by
     * {@code PlatformFileUploadServlet}, which has no tenant {@code ConsoleProperties} -- never falls open
     * to "every extension allowed"), overridden by {@link TenantFileUploadServlet} with a tenant-configurable
     * whitelist.
     */
    protected String[] getDefaultAllowedExtensions() {
        return DEFAULT_ALLOWED_EXTENSIONS;
    }
}
