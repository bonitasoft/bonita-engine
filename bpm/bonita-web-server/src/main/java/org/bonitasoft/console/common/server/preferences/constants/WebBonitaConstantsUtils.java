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
package org.bonitasoft.console.common.server.preferences.constants;

import static org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstants.rootTempDir;
import static org.bonitasoft.engine.io.IOUtil.createTempDirectory;

import java.io.File;

/**
 * @author Anthony Birembaut
 */
public class WebBonitaConstantsUtils {

    private static WebBonitaConstantsUtils tenantConstantsUtils = new WebBonitaConstantsUtils(
            new WebBonitaConstantsTenancyImpl());

    private static WebBonitaConstantsUtils platformConstantsUtils = new WebBonitaConstantsUtils(
            new WebBonitaConstantsImpl());

    private WebBonitaConstants webBonitaConstants;

    public WebBonitaConstantsUtils(WebBonitaConstants constants) {
        webBonitaConstants = constants;
    }

    public static WebBonitaConstantsUtils getTenantInstance() {
        return tenantConstantsUtils;
    }

    public static WebBonitaConstantsUtils getPlatformInstance() {
        return platformConstantsUtils;
    }

    /**
     * Get the folder where to write Tenant temporary files commons to all web
     * applications.
     */
    public File getTempFolder() {
        final File tempFolder = new File(rootTempDir);
        if (!tempFolder.exists()) {
            createTempDirectory(tempFolder.toURI());
        }
        return getFolder(webBonitaConstants.getTempFolderPath());
    }

    /**
     * Get the folder of Tenant pages files
     */
    public File getPagesFolder() {
        return getFolder(webBonitaConstants.getPagesTempFolderPath());
    }

    /**
     * Get the Tenant folder where to write Work files.
     */
    public File getFormsWorkFolder() {
        return getFolder(webBonitaConstants.getFormsTempFolderPath());
    }

    /**
     * Get the Tenant folder where to write BDM work files.
     */
    public File geBDMWorkFolder() {
        return getFolder(webBonitaConstants.getBDMTempFolderPath());
    }

    private File getFolder(final String folderPath) {
        final File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

}
