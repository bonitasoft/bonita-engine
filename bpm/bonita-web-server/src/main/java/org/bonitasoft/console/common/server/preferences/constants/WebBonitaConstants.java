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

import java.io.File;
import java.lang.management.ManagementFactory;

/**
 * @author Nicolas Chabanoles
 */
public interface WebBonitaConstants {

    /**
     * tenants folder
     */
    String tenantsFolderName = "tenant";

    /**
     * Generics folders
     */

    String clientFolderName = "client";

    String tmpFolderName = "bonita_portal_";

    // We use a tempFolder specific to the running JVM, so that 2 JVMs running on the same machine are isolated:
    String rootTempDir = System.getProperty("java.io.tmpdir") + File.separator + tmpFolderName
            + ManagementFactory.getRuntimeMXBean().getName();

    String formsFolderName = "forms";

    String bdmFolderName = "bdm";

    /**
     * Client
     */
    String clientFolderPath = clientFolderName + File.separator;

    /**
     * Get Tenants Folder Path
     *
     * @return path
     */
    String getTenantsFolderPath();

    /**
     * Get Tenant TempFolder Path
     *
     * @return path
     */
    String getTempFolderPath();

    /**
     * Get Tenant FormsTempFolder Path
     *
     * @return path
     */
    String getFormsTempFolderPath();

    /**
     * Get pagesConsoleTempFolder Path
     *
     * @return path
     */
    String getPagesTempFolderPath();

    /**
     * Get BDMTempFolderPath Path
     *
     * @return path
     */
    String getBDMTempFolderPath();

}
