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

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;
import org.bonitasoft.console.common.server.page.CustomPageService;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.InvalidPageTokenException;
import org.bonitasoft.engine.exception.InvalidPageZipContentException;
import org.bonitasoft.engine.session.APISession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @deprecated as of 9.0.0, Application page should be created/updated at startup.
 */
@Slf4j
@Deprecated(since = "9.0.0")
public class PageUploadServlet extends TenantFileUploadServlet {

    /**
     * UID
     */
    private static final long serialVersionUID = -5733037726905793432L;

    protected static final String ACTION_PARAM_NAME = "action";

    private static final String PROCESS_PARAM_NAME = "process";

    protected static final String ADD_ACTION = "add";

    protected static final Logger LOGGER = LoggerFactory.getLogger(PageUploadServlet.class.getName());

    protected static final String PERMISSIONS_RESPONSE_ATTRIBUTE = "permissions";

    /**
     * Request attribute holding the uploaded page until the response is generated. One servlet instance serves
     * concurrent requests: the page must not be kept in a field.
     */
    protected static final String UPLOADED_PAGE_ATTRIBUTE = PageUploadServlet.class.getName() + ".uploadedPage";

    /**
     * {@inheritDoc}
     *
     * @deprecated as of 9.0.0, Application page should be created/updated at startup.
     */
    @Override
    @Deprecated(since = "9.0.0")
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.warn("Application Page upload is deprecated! " +
                "An application page should be created or updated at startup instead.");
        super.doPost(request, response);
    }

    @Override
    protected String generateResponseString(final HttpServletRequest request, final String fileName,
            final String uploadedFileName) throws Exception {

        final String responseString = super.generateResponseString(request, fileName, uploadedFileName);
        String permissionString;
        try {
            final String[] permissions = getPermissions(request);
            permissionString = "[" + String.join(",", permissions) + "]";
        } catch (final Exception e) {
            permissionString = getPermissionsError(e);
        }
        return responseString + RESPONSE_SEPARATOR + permissionString;
    }

    @Override
    protected void fillJsonResponseMap(final HttpServletRequest request, final Map<String, Serializable> responseMap,
            final String fileName, final String contentType, final String uploadedFileKey) {
        super.fillJsonResponseMap(request, responseMap, fileName, contentType, uploadedFileKey);
        // also add the permissions to the map
        try {
            final String[] permissions = getPermissions(request);
            responseMap.put(PERMISSIONS_RESPONSE_ATTRIBUTE, permissions);
        } catch (final Exception e) {
            responseMap.put(PERMISSIONS_RESPONSE_ATTRIBUTE, getPermissionsError(e));
        }
    }

    @Override
    protected String storeTempFile(final HttpServletRequest request, final String fileName, final FileItem item)
            throws Exception {
        // the permissions of the page are read from the uploaded part itself, when the response is generated: the part
        // stays available until the upload request ends, so no copy of the page is needed
        request.setAttribute(UPLOADED_PAGE_ATTRIBUTE, item);
        return super.storeTempFile(request, fileName, item);
    }

    protected String[] getPermissions(final HttpServletRequest request)
            throws InvalidPageZipContentException, InvalidPageTokenException, AlreadyExistsException, BonitaException,
            IOException {
        final String action = request.getParameter(ACTION_PARAM_NAME);
        final boolean checkIfItAlreadyExists = ADD_ACTION.equals(action);
        final Set<String> permissionsSet = getPagePermissions(request, checkIfItAlreadyExists);
        return permissionsSet != null ? permissionsSet.toArray(new String[permissionsSet.size()]) : new String[0];
    }

    protected String getPermissionsError(final Exception e) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(e.getMessage());
        }
        return e.getClass().getSimpleName();
    }

    protected Set<String> getPagePermissions(final HttpServletRequest request, final boolean checkIfItAlreadyExists)
            throws BonitaException, IOException {
        final FileItem uploadedPage = (FileItem) request.getAttribute(UPLOADED_PAGE_ATTRIBUTE);
        if (uploadedPage == null) {
            throw new IllegalStateException("No page was uploaded with this request");
        }
        final APISession apiSession = getAPISession(request);
        final Long processDefinitionId = getProcessDefinitionId(request);
        final CustomPageService customPageService = getCustomPageService();
        final Properties properties = customPageService.getPageProperties(apiSession, uploadedPage.get(),
                checkIfItAlreadyExists, processDefinitionId);
        return customPageService.getCustomPagePermissions(properties, apiSession);
    }

    // protected for testing
    protected CustomPageService getCustomPageService() {
        return new CustomPageService();
    }

    private Long getProcessDefinitionId(final HttpServletRequest request) {
        final String processStr = request.getParameter(PROCESS_PARAM_NAME);
        Long processDefinitionId = null;
        if (processStr != null) {
            processDefinitionId = Long.parseLong(processStr);
        }
        return processDefinitionId;
    }

}
