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

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;

/**
 * Servlet allowing to upload a file in the platform common temp folder
 *
 * @author Anthony Birembaut
 */
public class PlatformFileUploadServlet extends FileUploadServlet {

    /**
     * UID
     */
    private static final long serialVersionUID = 58370675776169565L;

    @Override
    protected void defineUploadDirectoryPath(final HttpServletRequest request) {
        setUploadDirectoryPath(WebBonitaConstantsUtils.getPlatformInstance().getTempFolder().getPath());
    }

    @Override
    protected void setUploadMaxSize(final JakartaServletFileUpload<DiskFileItem, DiskFileItemFactory> serviceFileUpload,
            final HttpServletRequest request) {
        //currently no limit check on the platform
    }

}
