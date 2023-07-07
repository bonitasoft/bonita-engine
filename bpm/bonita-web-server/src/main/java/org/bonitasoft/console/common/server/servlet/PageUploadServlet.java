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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.bonitasoft.console.common.server.page.CustomPageService;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.InvalidPageTokenException;
import org.bonitasoft.engine.exception.InvalidPageZipContentException;
import org.bonitasoft.engine.io.FileContent;
import org.bonitasoft.engine.session.APISession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    protected File pageTmp;

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
    protected String storeTempFile(final String fileName, final FileItem item)
            throws Exception {
        pageTmp = File.createTempFile("tmp_page", ".tmp");
        FileUtils.copyToFile(item.getInputStream(), pageTmp);
        pageTmp.deleteOnExit();
        final FileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
        String mimeType = mimetypesFileTypeMap.getContentType(pageTmp);
        return temporaryContentAPI.storeTempFile(new FileContent(fileName, new FileInputStream(pageTmp), mimeType));
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
        final APISession apiSession = getAPISession(request);
        final Long processDefinitionId = getProcessDefinitionId(request);
        final CustomPageService customPageService = new CustomPageService();
        final Properties properties = customPageService.getPageProperties(apiSession,
                FileUtils.readFileToByteArray(pageTmp),
                checkIfItAlreadyExists, processDefinitionId);
        Set<String> customPagePermissions = customPageService.getCustomPagePermissions(properties, apiSession);
        pageTmp.delete();
        return customPagePermissions;
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
