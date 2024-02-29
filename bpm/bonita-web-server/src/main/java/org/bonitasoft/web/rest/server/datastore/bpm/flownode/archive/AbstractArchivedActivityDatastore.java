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

import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedActivityItem;
import org.bonitasoft.web.rest.server.datastore.bpm.flownode.archive.converter.ArchivedActivitySearchDescriptorConverter;
import org.bonitasoft.web.rest.server.datastore.utils.SearchOptionsCreator;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Séverin Moussel
 */
public abstract class AbstractArchivedActivityDatastore<CONSOLE_ITEM extends ArchivedActivityItem, ENGINE_ITEM extends ArchivedActivityInstance>
        extends AbstractArchivedFlowNodeDatastore<CONSOLE_ITEM, ENGINE_ITEM> {

    public AbstractArchivedActivityDatastore(final APISession engineSession, String token) {
        super(engineSession, token);
    }

    /**
     * Fill a console item using the engine item passed.
     *
     * @param result
     *        The console item to fill
     * @param item
     *        The engine item to use for filling
     * @return This method returns the result parameter passed.
     */
    public static ArchivedActivityItem fillConsoleItem(final ArchivedActivityItem result,
            final ArchivedActivityInstance item) {
        ArchivedFlowNodeDatastore.fillConsoleItem(result, item);

        result.setReachStateDate(item.getReachedStateDate());
        result.setLastUpdateDate(item.getLastUpdateDate());

        return result;
    }

    @Override
    protected ArchivedActivitySearchDescriptorConverter getSearchDescriptorConverter() {
        return new ArchivedActivitySearchDescriptorConverter();
    }

    @Override
    protected SearchResult<ENGINE_ITEM> runSearch(final SearchOptionsCreator creator,
            final Map<String, String> filters) {
        try {
            @SuppressWarnings("unchecked")
            final SearchResult<ENGINE_ITEM> result = (SearchResult<ENGINE_ITEM>) getProcessAPI()
                    .searchArchivedActivities(
                            creator.create());

            return result;
        } catch (final BonitaException e) {
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
