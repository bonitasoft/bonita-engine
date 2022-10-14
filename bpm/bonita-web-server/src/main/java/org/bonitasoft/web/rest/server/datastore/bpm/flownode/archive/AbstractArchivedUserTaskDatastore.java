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
package org.bonitasoft.web.rest.server.datastore.bpm.flownode.archive;

import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.ArchivedUserTaskInstance;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedUserTaskItem;
import org.bonitasoft.web.rest.server.datastore.utils.SearchOptionsCreator;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Séverin Moussel
 */
public abstract class AbstractArchivedUserTaskDatastore<CONSOLE_ITEM extends ArchivedUserTaskItem, ENGINE_ITEM extends ArchivedUserTaskInstance>
        extends AbstractArchivedHumanTaskDatastore<CONSOLE_ITEM, ENGINE_ITEM> {

    public AbstractArchivedUserTaskDatastore(final APISession engineSession, String token) {
        super(engineSession, token);
    }

    @Override
    protected SearchResult<ENGINE_ITEM> runSearch(final SearchOptionsCreator creator,
            final Map<String, String> filters) {
        try {
            if (!filters.containsKey(ArchivedUserTaskItem.ATTRIBUTE_TYPE)) {
                filters.put(ArchivedUserTaskItem.ATTRIBUTE_TYPE, ArchivedUserTaskItem.VALUE_TYPE_USER_TASK);
            }

            return super.runSearch(creator, filters);
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    @Override
    protected ENGINE_ITEM runGet(final APIID id) {
        try {
            return super.runGet(id);
        } catch (ClassCastException e) {
            throw new APIItemNotFoundException(this.token, id);
        }
    }
}
