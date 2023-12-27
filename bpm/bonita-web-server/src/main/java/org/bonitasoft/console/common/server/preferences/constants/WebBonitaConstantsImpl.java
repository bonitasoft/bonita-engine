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
import java.nio.file.Paths;

/**
 * @author Ruiheng.Fan
 */
public class WebBonitaConstantsImpl implements WebBonitaConstants {

    /**
     * platform folder
     */
    private static final String platformFolderName = "platform";

    private String platformFolderPath = null;

    /**
     * tenants folder
     */
    private String tenantsFolderPath = null;

    /**
     * tmp
     */
    private String tempFolderPath = null;

    /**
     * conf
     */
    private String confFolderPath = null;

    private String formsWorkFolderPath = null;

    /**
     * Default constructor.
     */
    public WebBonitaConstantsImpl() {
    }

    private String getPlatformFolderPath() {
        if (platformFolderPath == null) {
            platformFolderPath = clientFolderPath + platformFolderName + File.separator;
        }
        return platformFolderPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTenantsFolderPath() {
        if (tenantsFolderPath == null) {
            tenantsFolderPath = Paths.get(getTempFolderPath()).resolveSibling(tenantsFolderName) + File.separator;
        }
        return tenantsFolderPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTempFolderPath() {
        if (tempFolderPath == null) {
            tempFolderPath = rootTempDir + File.separator + platformFolderName + File.separator;
        }
        return tempFolderPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPagesTempFolderPath() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getFormsTempFolderPath() {
        if (formsWorkFolderPath == null) {
            formsWorkFolderPath = getTempFolderPath() + formsFolderName + File.separator;
        }
        return formsWorkFolderPath;
    }

    @Override
    public String getBDMTempFolderPath() {
        return null; // does not means anything at platform level
    }

}
