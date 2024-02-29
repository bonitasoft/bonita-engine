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
package org.bonitasoft.web.rest.server.api.bpm.process;

import java.util.Map;

import org.bonitasoft.web.rest.model.bpm.process.ProcessResolutionProblemDefinition;
import org.bonitasoft.web.rest.model.bpm.process.ProcessResolutionProblemItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ProcessResolutionProblemDatastore;
import org.bonitasoft.web.rest.server.framework.api.APIHasSearch;
import org.bonitasoft.web.rest.server.framework.api.Datastore;
import org.bonitasoft.web.rest.server.framework.exception.APIFilterMandatoryException;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Séverin Moussel
 */
public class APIProcessResolutionProblem extends ConsoleAPI<ProcessResolutionProblemItem>
        implements APIHasSearch<ProcessResolutionProblemItem> {

    @Override
    protected ItemDefinition defineItemDefinition() {
        return Definitions.get(ProcessResolutionProblemDefinition.TOKEN);
    }

    @Override
    protected Datastore defineDefaultDatastore() {
        return new ProcessResolutionProblemDatastore(getEngineSession());
    }

    @Override
    public String defineDefaultSearchOrder() {
        return ProcessResolutionProblemItem.ATTRIBUTE_TARGET_TYPE;
    }

    @Override
    public ItemSearchResult<ProcessResolutionProblemItem> search(final int page, final int resultsByPage,
            final String search, final String orders,
            final Map<String, String> filters) {

        if (!filters.containsKey(ProcessResolutionProblemItem.FILTER_PROCESS_ID)) {
            throw new APIFilterMandatoryException(ProcessResolutionProblemItem.FILTER_PROCESS_ID);
        }

        return super.search(page, resultsByPage, search, orders, filters);
    }

}
