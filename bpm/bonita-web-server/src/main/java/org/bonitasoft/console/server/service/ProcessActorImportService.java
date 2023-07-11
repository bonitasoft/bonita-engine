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

import org.apache.commons.io.IOUtils;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.io.FileContent;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APISessionInvalidException;
import org.bonitasoft.web.toolkit.server.ServiceException;

/**
 * @author Paul AMAR
 */
public class ProcessActorImportService extends ConsoleService {

    public final static String TOKEN = "/bpm/process/importActors";

    @Override
    public Object run() {
        final BonitaHomeFolderAccessor tenantFolder = new BonitaHomeFolderAccessor();
        try {
            final FileContent xmlFile = tenantFolder.retrieveUploadedTempContent(getFileUploadParameter());

            final APISession apiSession = getSession();
            final ProcessAPI processAPI = TenantAPIAccessor.getProcessAPI(apiSession);
            try (InputStream xmlStream = xmlFile.getInputStream()) {
                final byte[] actorsXmlContent = IOUtils.toByteArray(xmlStream);
                processAPI.importActorMapping(Long.valueOf(getParameter("process_id")), actorsXmlContent);
            }

        } catch (final InvalidSessionException e) {
            throw new APISessionInvalidException(e);
        } catch (final BonitaException | IOException e) {
            throw new ServiceException(TOKEN, e.getMessage());
        } finally {
            tenantFolder.removeUploadedTempContent(getFileUploadParameter());
        }
        return "";
    }

    protected String getFileUploadParameter() {
        return getParameter("file");
    }
}
