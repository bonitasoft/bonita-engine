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
package org.bonitasoft.web.rest.server.api.bpm.cases;

import java.util.List;
import java.util.Map;

import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCaseDefinition;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCaseItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.datastore.bpm.cases.ArchivedCaseDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ProcessDatastore;
import org.bonitasoft.web.rest.server.datastore.organization.UserDatastore;
import org.bonitasoft.web.rest.server.framework.api.APIHasDelete;
import org.bonitasoft.web.rest.server.framework.api.APIHasGet;
import org.bonitasoft.web.rest.server.framework.api.APIHasSearch;
import org.bonitasoft.web.rest.server.framework.api.Datastore;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Séverin Moussel
 */
public class APIArchivedCase extends ConsoleAPI<ArchivedCaseItem>
        implements APIHasGet<ArchivedCaseItem>, APIHasSearch<ArchivedCaseItem>, APIHasDelete {

    @Override
    public ItemDefinition defineItemDefinition() {
        return Definitions.get(ArchivedCaseDefinition.TOKEN);
    }

    @Override
    protected Datastore defineDefaultDatastore() {
        return new ArchivedCaseDatastore(getEngineSession());
    }

    @Override
    public ArchivedCaseItem get(final APIID id) {
        return getArchivedCaseDatastore().get(id);
    }

    @Override
    public String defineDefaultSearchOrder() {
        return "";
    }

    @Override
    public ItemSearchResult<ArchivedCaseItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {

        // Check that team manager and supervisor filters are not used together
        if (filters.containsKey(ArchivedCaseItem.FILTER_TEAM_MANAGER_ID)
                && filters.containsKey(ArchivedCaseItem.FILTER_SUPERVISOR_ID)) {
            throw new APIException(
                    "Can't set those filters at the same time : " + ArchivedCaseItem.FILTER_TEAM_MANAGER_ID + " and "
                            + ArchivedCaseItem.FILTER_SUPERVISOR_ID);
        }

        return getArchivedCaseDatastore().search(page, resultsByPage, search, orders, filters);
    }

    @Override
    protected void fillDeploys(final ArchivedCaseItem item, final List<String> deploys) {
        if (isDeployable(ArchivedCaseItem.ATTRIBUTE_STARTED_BY_USER_ID, deploys, item)) {
            item.setDeploy(
                    ArchivedCaseItem.ATTRIBUTE_STARTED_BY_USER_ID,
                    getUserDatastore().get(item.getStartedByUserId()));
        }

        if (isDeployable(ArchivedCaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID, deploys, item)) {
            item.setDeploy(
                    ArchivedCaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID,
                    getUserDatastore().get(item.getStartedBySubstituteUserId()));
        }

        if (isDeployable(ArchivedCaseItem.ATTRIBUTE_PROCESS_ID, deploys, item)) {
            item.setDeploy(
                    ArchivedCaseItem.ATTRIBUTE_PROCESS_ID,
                    getProcessDatastore().get(item.getProcessId()));
        }
    }

    /**
     * @return
     */
    protected ProcessDatastore getProcessDatastore() {
        return new ProcessDatastore(getEngineSession());
    }

    /**
     * @return
     */
    protected UserDatastore getUserDatastore() {
        return new UserDatastore(getEngineSession());
    }

    @Override
    protected void fillCounters(final ArchivedCaseItem item, final List<String> counters) {
    }

    protected ArchivedCaseDatastore getArchivedCaseDatastore() {
        return new ArchivedCaseDatastore(getEngineSession());
    }
}
