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

import org.apache.commons.io.FileUtils;
import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BonitaHomeFolderAccessor {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BonitaHomeFolderAccessor.class.getName());

    public BonitaHomeFolderAccessor() {
    }

    public File getTempFile(final String filePath) throws IOException {
        return new File(getCompleteTenantTempFilePath(filePath));
    }

    public String getCompleteTenantTempFilePath(final String filePath) throws IOException {
        String tempFilePath = filePath;
        final File tempFolder = getBonitaTenantConstantUtil().getTempFolder();

        if (!tempFilePath.contains(File.separator)) {
            tempFilePath = tempFolder.getAbsolutePath() + File.separator + tempFilePath;
        } else {
            verifyFolderAuthorization(new File(filePath), tempFolder);
        }

        return tempFilePath;
    }

    public String getCompletePlatformTempFilePath(final String filePath) throws IOException {
        String tempFilePath = filePath;
        final File tempFolder = getBonitaPlatformConstantUtil().getTempFolder();

        if (!tempFilePath.contains(File.separator)) {
            tempFilePath = tempFolder.getAbsolutePath() + File.separator + tempFilePath;
        } else {
            verifyFolderAuthorization(new File(filePath), tempFolder);
        }

        return tempFilePath;
    }

    public WebBonitaConstantsUtils getBonitaTenantConstantUtil() {
        return WebBonitaConstantsUtils.getTenantInstance();
    }

    public WebBonitaConstantsUtils getBonitaPlatformConstantUtil() {
        return WebBonitaConstantsUtils.getPlatformInstance();
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

    public IconDescriptor getIconFromFileSystem(String iconPath) {
        try {
            String completeTempFilePath = getCompleteTenantTempFilePath(iconPath);
            File tempFile = new File(completeTempFilePath);
            return new IconDescriptor(tempFile.getName(), FileUtils.readFileToByteArray(tempFile));
        } catch (IOException e) {
            throw new APIForbiddenException("Forbidden access to " + iconPath, e);
        }
    }
}
