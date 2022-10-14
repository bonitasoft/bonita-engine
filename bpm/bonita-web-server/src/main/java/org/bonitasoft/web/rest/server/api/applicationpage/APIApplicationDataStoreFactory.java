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
package org.bonitasoft.web.rest.server.api.applicationpage;

import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.server.datastore.application.ApplicationDataStore;
import org.bonitasoft.web.rest.server.datastore.application.ApplicationDataStoreCreator;
import org.bonitasoft.web.rest.server.datastore.applicationpage.ApplicationPageDataStore;
import org.bonitasoft.web.rest.server.datastore.applicationpage.ApplicationPageDataStoreCreator;
import org.bonitasoft.web.rest.server.datastore.page.PageDatastore;
import org.bonitasoft.web.rest.server.datastore.page.PageDatastoreFactory;
import org.bonitasoft.web.rest.server.engineclient.EngineAPIAccessor;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;

public class APIApplicationDataStoreFactory {

    public ApplicationPageDataStore createApplicationPageDataStore(final APISession session) {
        return new ApplicationPageDataStoreCreator().create(session);
    }

    public PageDatastore createPageDataStore(final APISession session) {
        try {
            final PageDatastoreFactory pageDatastoreFactory = new PageDatastoreFactory();
            return pageDatastoreFactory.create(session,
                    WebBonitaConstantsUtils.getTenantInstance(),
                    new EngineAPIAccessor(session).getPageAPI());
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    public ApplicationDataStore createApplicationDataStore(final APISession session) {
        return new ApplicationDataStoreCreator().create(session);
    }
}
