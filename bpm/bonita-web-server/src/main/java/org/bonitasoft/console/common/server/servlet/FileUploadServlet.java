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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileUploadByteCountLimitException;
import org.apache.commons.fileupload2.core.FileUploadSizeException;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.bonitasoft.console.common.server.filter.MultiReadHttpServletRequest;
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

    protected static final String RETURN_ORIGINAL_FILENAME_PARAM = "ReturnOriginalFilename";

    protected static final String CHECK_UPLOADED_FILE_SIZE = "CheckUploadedFileSize";

    protected static final String CHECK_UPLOADED_IMAGE_SIZE = "CheckUploadedImageSize";

    protected static final String RESPONSE_CONTENT_TYPE_PARAM = "ContentType";

    protected static final String TEXT_CONTENT_TYPE = "text";

    protected static final String JSON_CONTENT_TYPE = "json";

    protected static final String TEMP_PATH_RESPONSE_ATTRIBUTE = "tempPath";

    protected static final String FILE_NAME_RESPONSE_ATTRIBUTE = "filename";

    protected static final String CONTENT_TYPE_ATTRIBUTE = "contentType";

    protected static final String JARLESS_BAR_ATTRIBUTE = "jarlessBar";

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

    protected abstract void setUploadMaxSize(
            JakartaServletFileUpload<DiskFileItem, DiskFileItemFactory> serviceFileUpload,
            final HttpServletRequest request);

    protected void setUploadDirectoryPath(final String uploadDirectoryPath) {
        this.uploadDirectoryPath = uploadDirectoryPath;
    }

    /**
     * Test whether file is a jar less bar (only when check is relevant).
     *
     * @param fileName the file name
     * @param item the file item
     * @return true when file is a bar file without jar dependency files and check is relevant, false otherwise
     */
    private boolean isJarLessBar(String fileName, DiskFileItem item) {
        if (Arrays.equals(supportedExtensionsList, new String[] { "bar" }) && fileName.endsWith(".bar")) {
            ZipEntry zipEntry = null;
            try (ZipInputStream zipInputstream = new ZipInputStream(item.getInputStream());) {
                while ((zipEntry = zipInputstream.getNextEntry()) != null) {
                    if (".jarless".equals(zipEntry.getName())) {
                        return true;
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Error while checking if file is a jar less bar", e);
            }
        }
        return false;
    }

    @Override
    public void doPost(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain;charset=UTF-8");
        PrintWriter responsePW = null;
        try {
            defineUploadDirectoryPath(request);
            if (!JakartaServletFileUpload.isMultipartContent(request)) {
                return;
            }

            final File targetDirectory = new File(uploadDirectoryPath);
            if (!targetDirectory.exists()) {
                targetDirectory.mkdirs();
            }

            responsePW = response.getWriter();

            final DiskFileItemFactory fileItemFactory = DiskFileItemFactory.builder().get();
            final JakartaServletFileUpload<DiskFileItem, DiskFileItemFactory> serviceFileUpload = createServletFileUpload(
                    fileItemFactory);
            setUploadMaxSize(serviceFileUpload, request);
            List<DiskFileItem> items;
            try {
                // on any parse failure (including this OutOfMemoryError), parseRequest deletes the
                // items it already spilled to disk itself (internal finally on !successful)
                items = serviceFileUpload.parseRequest(request);
            } catch (final OutOfMemoryError e) {
                throw new FileUploadSizeException("The file exceeds its maximum permitted size.", 0L, 0L);
            }

            try {
                for (final DiskFileItem item : items) {
                    if (item.isFormField()) {
                        continue;
                    }

                    final String fileName = DocumentUtil.sanitizeFilename(item.getName());

                    // Check if extension is allowed
                    if (!isSupportedExtension(fileName)) {
                        outputMediaTypeError(response, responsePW);
                        return;
                    }

                    // Check that the declared Content-Type and the actual content match the extension (CVE-68)
                    try (InputStream itemContent = item.getInputStream()) {
                        if (!DocumentUtil.isContentConsistentWithExtension(fileName, item.getContentType(),
                                itemContent)) {
                            outputMediaTypeError(response, responsePW);
                            return;
                        }
                    }

                    // Make unique file name
                    final String uploadedFileKey = storeTempFile(fileName, item);

                    //Clean multiread wrapper temp file if it exists
                    if (request instanceof MultiReadHttpServletRequest) {
                        ((MultiReadHttpServletRequest) request).cleanMultipartTempContent();
                    }

                    // Response
                    final String responseString;
                    if (JSON_CONTENT_TYPE.equals(responseContentType)) {
                        // just check whether it's a jarless bar to display a warning
                        boolean isJarlessBar = isJarLessBar(fileName, item);
                        responseString = generateResponseJson(request, fileName, item.getContentType(),
                                uploadedFileKey,
                                isJarlessBar);
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
            } finally {
                deleteTempFiles(items);
            }
        } catch (SessionNotFoundException e) {
            final String message = "Session expired";
            LOGGER.debug(message);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
        } catch (final FileUploadByteCountLimitException e) {
            LOGGER.error("File is Too Big", e);
            generateFileTooBigError(response, responsePW,
                    e.getFileName() + " is " + e.getActualSize() + " large, limit is set to " + e.getPermitted()
                            / FileUploadServlet.MEGABYTE + "Mb");
        } catch (final FileUploadSizeException e) {
            LOGGER.error("File is Too Big", e);
            generateFileTooBigError(response, responsePW, "Uploaded file is too large, server is unable to process it");
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

    protected String storeTempFile(final String fileName, final DiskFileItem item)
            throws Exception {
        temporaryContentAPI = PlatformAPIAccessor.getTemporaryContentAPI();
        return temporaryContentAPI
                .storeTempFile(new FileContent(fileName, item.getInputStream(), item.getContentType()));
    }

    /**
     * fileupload2 has no finalizer deleting the disk-buffered content of a DiskFileItem on GC
     * (commons-fileupload dropped it in the 1.5 line already) and no FileCleaningTracker is
     * configured: items spilled beyond the in-memory threshold must be deleted explicitly, or
     * their backing files accumulate in the temp directory until JVM exit.
     */
    private void deleteTempFiles(final List<DiskFileItem> items) {
        for (final DiskFileItem item : items) {
            try {
                item.delete();
            } catch (final IOException e) {
                // warn: nothing else reclaims these files (no finalizer, no deleteOnExit), a failed
                // delete grows the temp directory until JVM exit
                LOGGER.warn("Could not delete the temporary content of the multipart item {}", item.getName(), e);
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
    protected JakartaServletFileUpload<DiskFileItem, DiskFileItemFactory> createServletFileUpload(
            final DiskFileItemFactory fileItemFactory) {
        return new JakartaServletFileUpload<>(fileItemFactory);
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
            final String uploadedFileName, final boolean isJarlessBar) throws Exception {
        final Map<String, Serializable> responseMap = new HashMap<>();
        fillJsonResponseMap(request, responseMap, fileName, contentType, uploadedFileName, isJarlessBar);
        return objectMapper.writeValueAsString(responseMap);
    }

    protected void fillJsonResponseMap(HttpServletRequest request, final Map<String, Serializable> responseMap,
            final String fileName,
            final String contentType, final String uploadedFileName, final boolean isJarlessBar) {
        if (alsoReturnOriginalFilename) {
            responseMap.put(FILE_NAME_RESPONSE_ATTRIBUTE, getFilenameLastSegment(fileName));
        }
        responseMap.put(TEMP_PATH_RESPONSE_ATTRIBUTE, uploadedFileName);
        responseMap.put(CONTENT_TYPE_ATTRIBUTE, contentType);
        if (isJarlessBar) {
            responseMap.put(JARLESS_BAR_ATTRIBUTE, Boolean.TRUE);
        }
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

        final String[] extensions = supportedExtensionsList.length > 0 ? supportedExtensionsList
                : getDefaultAllowedExtensions();

        // if no extension whitelist applies at all, every extension is allowed
        if (extensions.length < 1) {
            return true;
        }

        for (final String extension : extensions) {
            if (fileName.toLowerCase().endsWith("." + extension.toLowerCase())) {
                return true;
            }
        }

        return false;
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
