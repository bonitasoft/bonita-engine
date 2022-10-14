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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.common.model.ImportStatusMessages;
import org.bonitasoft.web.rest.server.framework.json.JacksonSerializer;
import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n;
import org.bonitasoft.web.toolkit.server.ServiceException;
import org.slf4j.Logger;

/**
 * Import REST Resources and return the Import Status Messages
 *
 * @author Julien Mege
 */
abstract class BonitaImportService extends ConsoleService {

    /**
     * import policy
     */
    public static final String IMPORT_POLICY_PARAM_NAME = "importPolicy";

    @Override
    public Object run() {
        File xmlFile;
        try {
            xmlFile = getTenantFolder().getTempFile(getFileUploadParamValue());
        } catch (final IOException e) {
            throw new ServiceException(getToken(), e.getMessage(), e);
        }

        final String importPolicyAsString = getParameter(IMPORT_POLICY_PARAM_NAME);

        try {
            final JacksonSerializer serializer = new JacksonSerializer();
            final ImportStatusMessages importStatusMessages = importFileContent(readImportFile(xmlFile),
                    importPolicyAsString);
            return serializer.serialize(importStatusMessages);
        } catch (final InvalidSessionException e) {
            if (getLogger().isInfoEnabled()) {
                getLogger().info(AbstractI18n.t_("Session expired. Please log in again."), e);
            }
            throw e;
        } catch (final ExecutionException e) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn(e.getMessage(), e);
            }
            throw new ServiceException(getToken(), getFileFormatExceptionMessage(), e);
        } catch (final ImportException e) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn(e.getMessage(), e);
            }
            throw new ServiceException(getToken(), getFileFormatExceptionMessage(), e);
        } catch (final AlreadyExistsException e) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn(e.getMessage(), e);
            }
            throw new ServiceException(getToken(), getAlreadyExistsExceptionMessage(e), e);
        } catch (final Exception e) {
            if (getLogger().isErrorEnabled()) {
                getLogger().error(e.getMessage(), e);
            }
            throw new ServiceException(getToken(), e);
        }
    }

    protected byte[] readImportFile(final File xmlFile) {
        InputStream xmlStream = null;
        byte[] content;
        try {
            xmlStream = new FileInputStream(xmlFile);
            content = IOUtils.toByteArray(xmlStream);
        } catch (final Exception e) {
            throw new ServiceException(getFileReadingError(), e);
        } finally {
            if (xmlStream != null) {
                try {
                    xmlStream.close();
                } catch (final IOException e) {
                    xmlStream = null;
                }
            }
        }

        return content;
    }

    protected BonitaHomeFolderAccessor getTenantFolder() {
        return new BonitaHomeFolderAccessor();
    }

    protected String getFileUploadParamValue() {
        return getParameter(getFileUploadParamName());
    }

    protected abstract String getFileReadingError();

    protected abstract String getToken();

    protected abstract String getFileUploadParamName();

    protected abstract ImportStatusMessages importFileContent(final byte[] fileContent, final String importPolicy)
            throws ExecutionException, ImportException, AlreadyExistsException, InvalidSessionException,
            BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException;

    protected abstract String getFileFormatExceptionMessage();

    protected abstract String getAlreadyExistsExceptionMessage(AlreadyExistsException e);

    protected abstract Logger getLogger();

}
