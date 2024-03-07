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
package org.bonitasoft.console.server.service;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.identity.ImportPolicy;
import org.bonitasoft.engine.identity.InvalidOrganizationFileFormatException;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n;
import org.bonitasoft.web.toolkit.server.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Séverin Moussel
 */
public class OrganizationImportService extends ConsoleService {

    public static final String TOKEN = "/organization/import";

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(OrganizationImportService.class.getName());

    /**
     * organization data file
     */
    private static final String FILE_UPLOAD = "organizationDataUpload";

    /**
     * import policy
     */
    private static final String IMPORT_POLICY_PARAM_NAME = "importPolicy";

    @Override
    public Object run() {
        final BonitaHomeFolderAccessor tenantFolder = new BonitaHomeFolderAccessor();
        try {
            final byte[] organizationContent = getOrganizationContent(tenantFolder);
            getIdentityAPI().importOrganizationWithWarnings(new String(organizationContent), getImportPolicy());
        } catch (final InvalidSessionException e) {
            getHttpResponse().setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            String message = AbstractI18n.t_("Session expired. Please log in again.");
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(message, e.getMessage());
            }
            throw new ServiceException(TOKEN, message, e);
        } catch (InvalidOrganizationFileFormatException | IllegalArgumentException e) {
            getHttpResponse().setStatus(HttpServletResponse.SC_BAD_REQUEST);
            String message = AbstractI18n.t_("Can't import organization. Please check that your file is well-formed.");
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info(message, e.getMessage());
            }
            throw new ServiceException(TOKEN, message, e);
        } catch (final Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(e.getMessage(), e);
            }
            throw new ServiceException(TOKEN, AbstractI18n.t_("Can't import organization"), e);
        } finally {
            cleanTempContent(tenantFolder);
        }
        return "";
    }

    private ImportPolicy getImportPolicy() {
        final String importPolicyAsString = getParameter(IMPORT_POLICY_PARAM_NAME);
        ImportPolicy importPolicy = ImportPolicy.MERGE_DUPLICATES;
        if (importPolicyAsString != null) {
            importPolicy = ImportPolicy.valueOf(importPolicyAsString);
        }
        return importPolicy;
    }

    public byte[] getOrganizationContent(final BonitaHomeFolderAccessor tenantFolder)
            throws IOException, BonitaException {
        try (InputStream xmlStream = tenantFolder.retrieveUploadedTempContent(getFileUploadParameter())
                .getInputStream()) {
            return IOUtils.toByteArray(xmlStream);
        }
    }

    public void cleanTempContent(final BonitaHomeFolderAccessor tenantFolder) {
        tenantFolder.removeUploadedTempContent(getFileUploadParameter());
    }

    protected IdentityAPI getIdentityAPI()
            throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getIdentityAPI(getSession());
    }

    protected String getFileUploadParameter() {
        return getParameter(FILE_UPLOAD);
    }

}
