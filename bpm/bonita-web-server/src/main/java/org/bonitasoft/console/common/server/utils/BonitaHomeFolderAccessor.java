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
package org.bonitasoft.console.common.server.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.api.TemporaryContentAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.io.FileContent;
import org.bonitasoft.engine.io.TemporaryFileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BonitaHomeFolderAccessor {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BonitaHomeFolderAccessor.class.getName());

    public BonitaHomeFolderAccessor() {
    }

    /**
     * @param tempFileKey
     * @return
     * @throws IOException
     * @deprecated use retrieveTempFileContent instead to avoid creating additional temp file
     */
    @Deprecated
    public File getTempFile(final String tempFileKey) throws IOException {
        try {
            FileContent fileContent = retrieveTempFileContent(tempFileKey);
            File file = makeUniqueFilename(fileContent.getFileName());
            try (InputStream inputStream = fileContent.getInputStream()) {
                Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return file;
        } catch (BonitaException e) {
            throw new IOException(e);
        }
    }

    public FileContent retrieveTempFileContent(final String tempFileKey) throws BonitaException {
        TemporaryContentAPI temporaryContentAPI = PlatformAPIAccessor.getTemporaryContentAPI();
        try {
            return temporaryContentAPI.retrieveTempFile(tempFileKey);
        } catch (TemporaryFileNotFoundException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Unable to find temporary file with key " + tempFileKey);
            }
            throw e;
        } catch (BonitaRuntimeException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Unable to retrieve temporary file with key " + tempFileKey);
            }
            throw new BonitaException(e);
        }
    }

    public void removeTempFileContent(final String tempFileKey) {
        try {
            TemporaryContentAPI temporaryContentAPI = PlatformAPIAccessor.getTemporaryContentAPI();
            temporaryContentAPI.removeTempFile(tempFileKey);
        } catch (BonitaException | BonitaRuntimeException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Unable to remove temporary file with key " + tempFileKey
                        + " If still present, it will be cleaned by the scheduler.");
            }
        }
    }

    protected File makeUniqueFilename(final String fileName) throws IOException {
        final File uploadedFile = File.createTempFile("tmp_", getExtension(fileName),
                getBonitaTenantConstantUtil().getTempFolder());
        uploadedFile.deleteOnExit();
        return uploadedFile;
    }

    protected String getExtension(final String fileName) {
        return FilenameUtils.getExtension(fileName);
    }

    public WebBonitaConstantsUtils getBonitaTenantConstantUtil() {
        return WebBonitaConstantsUtils.getTenantInstance();
    }

    public boolean isInTempFolder(final File file, final WebBonitaConstantsUtils webBonitaConstantsUtils)
            throws IOException {
        return isInFolder(file, webBonitaConstantsUtils.getTempFolder());
    }

    public boolean isInFolder(final File file, final File parentFolder) throws IOException {
        try {
            verifyFolderAuthorization(file, parentFolder);
        } catch (final UnauthorizedFolderException e) {
            return false;
        }
        return true;
    }

    private void verifyFolderAuthorization(final File file, final File parentFolder) throws IOException {
        try {
            if (!file.getCanonicalPath().startsWith(parentFolder.getCanonicalPath())) {
                throw new UnauthorizedFolderException("Unauthorized access to the file " + file.getPath());
            }
        } catch (final UnauthorizedFolderException e) {
            final String errorMessage = "Unauthorized access to the file " + file.getAbsolutePath()
                    + ". For security reasons, access to paths other than "
                    + parentFolder.getAbsolutePath()
                    + " is restricted.";
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(errorMessage, e);
            }
            throw e;
        }
    }

    public IconDescriptor getIconFromFileSystem(String iconKey) {
        try {
            TemporaryContentAPI temporaryContentAPI = PlatformAPIAccessor.getTemporaryContentAPI();
            FileContent fileContent = temporaryContentAPI.retrieveTempFile(iconKey);
            return new IconDescriptor(fileContent.getFileName(), IOUtils.toByteArray(fileContent.getInputStream()));
        } catch (TemporaryFileNotFoundException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Unable to find temporary file with key " + iconKey);
            }
            throw new RuntimeException(e);
        } catch (BonitaException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
