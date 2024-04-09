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

import static org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n.t_;

import java.util.List;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.business.application.ApplicationImportPolicy;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.common.model.ImportStatusMessages;
import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n;
import org.bonitasoft.web.toolkit.client.common.texttemplate.Arg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Julien Mege
 */
public class ApplicationsImportService extends BonitaImportService {

    public final static String TOKEN = "/application/import";

    /**
     * organization data file
     */
    public static final String FILE_UPLOAD = "applicationsDataUpload";

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationsImportService.class.getName());

    protected ApplicationAPI getApplicationAPI()
            throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getLivingApplicationAPI(getSession());
    }

    @Override
    public String getToken() {
        return TOKEN;
    }

    @Override
    public String getFileUploadParamName() {
        return FILE_UPLOAD;
    }

    @Override
    public ImportStatusMessages importFileContent(final byte[] fileContent, final String importPolicyAsString)
            throws ExecutionException, ImportException, AlreadyExistsException, InvalidSessionException,
            BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        final ApplicationImportPolicy importPolicy = ApplicationImportPolicy.valueOf(importPolicyAsString);
        final List<ImportStatus> ImportStatusList = getApplicationAPI().importApplications(fileContent, importPolicy);
        return new ImportStatusMessages(ImportStatusList);
    }

    @Override
    protected String getFileFormatExceptionMessage() {
        return AbstractI18n.t_("Can't import Applications.", getLocale());
    }

    @Override
    protected String getAlreadyExistsExceptionMessage(final AlreadyExistsException e) {
        return t_("Can't import applications. An application '%token%' already exists", getLocale(),
                new Arg("token", e.getName()));
    }

    @Override
    protected String getFileReadingError() {
        return AbstractI18n.t_("Error during Application import file reading.");
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

}
