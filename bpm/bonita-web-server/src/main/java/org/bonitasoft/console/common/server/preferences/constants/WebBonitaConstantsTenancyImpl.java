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

/**
 * @author Ruiheng.Fan
 */
public class WebBonitaConstantsTenancyImpl implements WebBonitaConstants {

    private static final String PAGES_WORK_FOLDER_NAME = "pages";
    private String tenantsFolderPath = null;
    private final String tempFolderPath;
    private String formsWorkFolderPath = null;
    private String pagesWorkFolderPath = null;
    private String bdmWorkFolderPath;

    /**
     * Default constructor.
     */
    WebBonitaConstantsTenancyImpl() {
        tempFolderPath = rootTempDir + File.separator + tenantsFolderName + File.separator;
    }

    @Override
    public String getTenantsFolderPath() {
        if (tenantsFolderPath == null) {
            tenantsFolderPath = rootTempDir + File.separator + tenantsFolderName + File.separator;
        }
        return tenantsFolderPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTempFolderPath() {
        return tempFolderPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPagesTempFolderPath() {
        if (pagesWorkFolderPath == null) {
            pagesWorkFolderPath = getTempFolderPath() + PAGES_WORK_FOLDER_NAME + File.separator;
        }
        return pagesWorkFolderPath;
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
        if (bdmWorkFolderPath == null) {
            bdmWorkFolderPath = getTempFolderPath() + File.separator + bdmFolderName + File.separator;
        }
        return bdmWorkFolderPath;
    }
}
