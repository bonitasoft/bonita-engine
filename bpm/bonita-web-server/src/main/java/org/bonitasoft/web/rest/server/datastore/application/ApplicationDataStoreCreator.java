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
package org.bonitasoft.web.rest.server.datastore.application;

import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationDataStoreCreator {

    public ApplicationDataStore create(final APISession session) {
        ApplicationAPI applicationAPI;
        PageAPI pageAPI;
        try {
            applicationAPI = TenantAPIAccessor.getLivingApplicationAPI(session);
            pageAPI = TenantAPIAccessor.getCustomPageAPI(session);
            return new ApplicationDataStore(session, applicationAPI, pageAPI, getApplicationConverter());
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    protected ApplicationItemConverter getApplicationConverter() {
        return new ApplicationItemConverter(new BonitaHomeFolderAccessor());
    }
}
