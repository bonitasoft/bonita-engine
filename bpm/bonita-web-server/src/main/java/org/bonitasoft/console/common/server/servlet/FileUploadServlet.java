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
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
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
import org.bonitasoft.console.common.server.utils.DocumentUtil;
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

    protected static final String RETURN_FULL_SERVER_PATH_PARAM = "ReturnFullPath";

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

    protected boolean returnFullPathInResponse = false;

    protected boolean alsoReturnOriginalFilename = false;

    protected boolean checkUploadedFileSize = false;

    protected boolean checkUploadedImageSize = false;

    protected String responseContentType = TEXT_CONTENT_TYPE;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void init() throws ServletException {

        final String supportedExtensionsParam = getInitParameter(SUPPORTED_EXTENSIONS_PARAM);
        if (supportedExtensionsParam != null) {
            supportedExtensionsList = supportedExtensionsParam.split(SUPPORTED_EXTENSIONS_SEPARATOR);
        }
        alsoReturnOriginalFilename = Boolean.parseBoolean(getInitParameter(RETURN_ORIGINAL_FILENAME_PARAM));
        returnFullPathInResponse = Boolean.parseBoolean(getInitParameter(RETURN_FULL_SERVER_PATH_PARAM));
        final String responseContentTypeParam = getInitParameter(RESPONSE_CONTENT_TYPE_PARAM);
        if (responseContentTypeParam != null) {
            responseContentType = responseContentTypeParam;
        }
        checkUploadedFileSize = Boolean.parseBoolean(getInitParameter(CHECK_UPLOADED_FILE_SIZE));
        checkUploadedImageSize = Boolean.parseBoolean(getInitParameter(CHECK_UPLOADED_IMAGE_SIZE));
    }

    protected abstract void defineUploadDirectoryPath(final HttpServletRequest request) throws SessionNotFoundException;

    protected abstract void setUploadMaxSize(ServletFileUpload serviceFileUpload, final HttpServletRequest request);

    protected void setUploadDirectoryPath(final String uploadDirectoryPath) {
        this.uploadDirectoryPath = uploadDirectoryPath;
    }

    @Override
    @SuppressWarnings("unchecked")
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
                    outputMediaTypeError(response, responsePW);
                    return;
                }

                // Make unique file name
                final File uploadedFile = makeUniqueFilename(targetDirectory, fileName);

                // Upload file
                item.write(uploadedFile);
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("File uploaded : " + uploadedFile.getPath());
                }
                uploadedFile.deleteOnExit();

                // Response
                final String responseString;
                if (JSON_CONTENT_TYPE.equals(responseContentType)) {
                    responseString = generateResponseJson(request, fileName, item.getContentType(), uploadedFile);
                } else if (TEXT_CONTENT_TYPE.equals(responseContentType)) {
                    responseString = generateResponseString(request, fileName, uploadedFile);
                } else {
                    throw new ServletException(
                            "Unsupported content type in servlet configuration : " + responseContentType);
                }
                responsePW.print(responseString);
                responsePW.flush();
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
            if (responsePW != null) {
                responsePW.close();
            }
        }
    }

    private void generateFileTooBigError(final HttpServletResponse response, final PrintWriter responsePW,
            final String message) throws JsonProcessingException {
        response.setStatus(HttpURLConnection.HTTP_ENTITY_TOO_LARGE);
        if (JSON_CONTENT_TYPE.equals(responseContentType)) {
            final Map<String, Serializable> errorResponse = new HashMap<>();
            errorResponse.put("type", "EntityTooLarge");
            errorResponse.put("message", message);
            errorResponse.put("statusCode", HttpURLConnection.HTTP_ENTITY_TOO_LARGE);
            responsePW.print(objectMapper.writeValueAsString(errorResponse));
            responsePW.flush();
        }
    }

    //for test purpose
    protected ServletFileUpload createServletFileUpload(final FileItemFactory fileItemFactory) {
        return new ServletFileUpload(fileItemFactory);
    }

    protected String generateResponseString(final HttpServletRequest request, final String fileName,
            final File uploadedFile) throws Exception {
        String responseString;
        if (returnFullPathInResponse) {
            responseString = uploadedFile.getPath();
        } else {
            responseString = uploadedFile.getName();
        }
        if (alsoReturnOriginalFilename) {
            responseString = responseString + RESPONSE_SEPARATOR + getFilenameLastSegment(fileName);
        }
        return responseString;
    }

    protected String generateResponseJson(final HttpServletRequest request, final String fileName, String contentType,
            final File uploadedFile) throws Exception {
        final Map<String, Serializable> responseMap = new HashMap<>();
        fillJsonResponseMap(request, responseMap, fileName, contentType, uploadedFile);
        return objectMapper.writeValueAsString(responseMap);
    }

    protected void fillJsonResponseMap(HttpServletRequest request, final Map<String, Serializable> responseMap,
            final String fileName,
            final String contentType, final File uploadedFile) {
        if (alsoReturnOriginalFilename) {
            responseMap.put(FILE_NAME_RESPONSE_ATTRIBUTE, getFilenameLastSegment(fileName));
        }
        if (returnFullPathInResponse) {
            responseMap.put(TEMP_PATH_RESPONSE_ATTRIBUTE, uploadedFile.getPath());
        } else {
            responseMap.put(TEMP_PATH_RESPONSE_ATTRIBUTE, uploadedFile.getName());
        }
        responseMap.put(CONTENT_TYPE_ATTRIBUTE, contentType);
    }

    protected File makeUniqueFilename(final File targetDirectory, final String fileName) throws IOException {
        final File uploadedFile = File.createTempFile("tmp_", getExtension(fileName), targetDirectory);
        uploadedFile.deleteOnExit();
        return uploadedFile;
    }

    protected String getExtension(final String fileName) {
        String extension = "";
        final String filenameLastSegment = getFilenameLastSegment(fileName);
        final int dotPos = filenameLastSegment.lastIndexOf('.');
        if (dotPos > -1) {
            extension = filenameLastSegment.substring(dotPos);
        }
        return extension;
    }

    protected String getFilenameLastSegment(final String fileName) {
        int slashPos = fileName.lastIndexOf("/");
        if (slashPos == -1) {
            slashPos = fileName.lastIndexOf("\\");
        }
        return fileName.substring(slashPos + 1);
    }

    protected void outputMediaTypeError(final HttpServletResponse response, final PrintWriter responsePW) {
        response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
        responsePW.print("Extension not supported.");
        responsePW.flush();
    }

    protected boolean isSupportedExtension(final String fileName) {
        if (fileName == null) {
            return false;
        }

        // if no extension specified, all extensions are allowed
        if (supportedExtensionsList.length < 1) {
            return true;
        }

        for (final String extension : supportedExtensionsList) {
            if (fileName.toLowerCase().endsWith("." + extension.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}
