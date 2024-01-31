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
package org.bonitasoft.console.server.servlet;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.ExportException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.APISession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Julien MEGE
 */
public class ApplicationsExportServlet extends ExportByIdsServlet {

    /**
     * export file name
     */
    private static final String EXPORT_FILE_NAME = "applicationDescriptorFile.xml";

    /**
     * UID
     */
    private static final long serialVersionUID = 1800666571090128789L;

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationsExportServlet.class.getName());

    @Override
    protected String getFileExportName() {
        return EXPORT_FILE_NAME;
    }

    @Override
    protected byte[] exportResources(final long[] ids, final APISession apiSession) throws BonitaHomeNotSetException,
            ServerAPIException, UnknownAPITypeException, ExecutionException, ExportException {
        final ApplicationAPI applicationAPI = getApplicationAPI(apiSession);
        return applicationAPI.exportApplications(ids);
    }

    protected ApplicationAPI getApplicationAPI(final APISession apiSession)
            throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getLivingApplicationAPI(apiSession);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
