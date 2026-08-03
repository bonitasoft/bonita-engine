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
import jakarta.servlet.http.HttpSession;

import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload;
import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.bonitasoft.console.common.server.preferences.properties.ConsoleProperties;
import org.bonitasoft.console.common.server.preferences.properties.PropertiesFactory;
import org.bonitasoft.engine.session.APISession;

/**
 * Servlet allowing to upload a file in the tenant common temp folder
 *
 * @author Anthony Birembaut
 */
public class TenantFileUploadServlet extends FileUploadServlet {

    /**
     * UID
     */
    private static final long serialVersionUID = 58370675886169565L;

    @Override
    protected void defineUploadDirectoryPath(final HttpServletRequest request) {
        setUploadDirectoryPath(WebBonitaConstantsUtils.getTenantInstance().getTempFolder().getPath());
    }

    protected APISession getAPISession(final HttpServletRequest request) {
        final HttpSession session = request.getSession();
        return (APISession) session.getAttribute("apiSession");
    }

    @Override
    protected void setUploadMaxSize(final JakartaServletFileUpload<DiskFileItem, DiskFileItemFactory> serviceFileUpload,
            final HttpServletRequest request) {
        if (checkUploadedImageSize) {
            serviceFileUpload.setMaxFileSize(getConsoleProperties().getImageMaxSizeInKB() * KILOBYTE);
        } else if (checkUploadedFileSize) {
            serviceFileUpload.setMaxFileSize(getConsoleProperties().getMaxSize() * MEGABYTE);
        } else {
            //Some db vendor do not support more than 2GB Blobs
            serviceFileUpload.setMaxFileSize(Integer.MAX_VALUE);
        }
    }

    protected ConsoleProperties getConsoleProperties() {
        return PropertiesFactory.getConsoleProperties();
    }

}
