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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.web.rest.model.bpm.cases.CaseItem;
import org.bonitasoft.web.rest.model.bpm.process.ProcessDefinition;
import org.bonitasoft.web.rest.model.bpm.process.ProcessItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.api.deployer.UserDeployer;
import org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ProcessDatastore;
import org.bonitasoft.web.rest.server.datastore.organization.UserDatastore;
import org.bonitasoft.web.rest.server.framework.api.APIHasAdd;
import org.bonitasoft.web.rest.server.framework.api.APIHasDelete;
import org.bonitasoft.web.rest.server.framework.api.APIHasGet;
import org.bonitasoft.web.rest.server.framework.api.APIHasSearch;
import org.bonitasoft.web.rest.server.framework.api.APIHasUpdate;
import org.bonitasoft.web.rest.server.framework.api.Datastore;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Nicolas Tith
 * @author Celine Souchet
 */
public class APIProcess extends ConsoleAPI<ProcessItem> implements
        APIHasAdd<ProcessItem>,
        APIHasUpdate<ProcessItem>,
        APIHasGet<ProcessItem>,
        APIHasSearch<ProcessItem>,
        APIHasDelete {

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CONFIGURE
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected ItemDefinition<ProcessItem> defineItemDefinition() {
        return ProcessDefinition.get();
    }

    @Override
    public String defineDefaultSearchOrder() {
        return ProcessItem.ATTRIBUTE_NAME + " ASC";
    }

    @Override
    protected Datastore defineDefaultDatastore() {
        return new ProcessDatastore(getEngineSession());
    }

    /**
     * @deprecated as of 9.0.0, Process should be created at startup.
     */
    @Override
    @Deprecated(since = "9.0.0")
    public ProcessItem add(final ProcessItem item) {
        return getProcessDatastore().add(item);
    }

    /**
     * @deprecated as of 9.0.0, Process should be updated at startup.
     */
    @Override
    @Deprecated(since = "9.0.0")
    public ProcessItem update(final APIID id, final Map<String, String> attributes) {
        return getProcessDatastore().update(id, attributes);
    }

    @Override
    public ProcessItem get(final APIID id) {
        return getProcessDatastore().get(id);
    }

    @Override
    public ItemSearchResult<ProcessItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {

        if (filters.containsKey(ProcessItem.FILTER_TEAM_MANAGER_ID)
                && filters.containsKey(ProcessItem.FILTER_SUPERVISOR_ID)) {
            throw new APIException(
                    "Can't set those filters at the same time : " + ProcessItem.FILTER_TEAM_MANAGER_ID + " and "
                            + ProcessItem.FILTER_SUPERVISOR_ID);
        }

        return getProcessDatastore().search(page, resultsByPage, search, orders, filters);
    }

    @Override
    public void delete(final List<APIID> ids) {
        getProcessDatastore().delete(ids);
    }

    @Override
    protected void fillDeploys(final ProcessItem item, final List<String> deploys) {
        addDeployer(new UserDeployer(
                new UserDatastore(getEngineSession()), ProcessItem.ATTRIBUTE_DEPLOYED_BY_USER_ID));
        super.fillDeploys(item, deploys);
    }

    @Override
    protected void fillCounters(final ProcessItem item, final List<String> counters) {
        fillNumberOfFailedCasesIfFailedCounterExists(item, counters);
        fillNumberOfOpenCasesIfOpenCounterExists(item, counters);
    }

    private void fillNumberOfFailedCasesIfFailedCounterExists(final ProcessItem item, final List<String> counters) {
        if (counters.contains(ProcessItem.COUNTER_FAILED_CASES)) {
            final Map<String, String> filters = new HashMap<>();
            filters.put(CaseItem.FILTER_CALLER, "any");
            filters.put(CaseItem.ATTRIBUTE_PROCESS_ID, item.getId().toString());
            filters.put(CaseItem.FILTER_STATE, ProcessInstanceState.ERROR.name());
            item.setAttribute(ProcessItem.COUNTER_FAILED_CASES, getCaseDatastore().count(null, null, filters));
        }
    }

    private void fillNumberOfOpenCasesIfOpenCounterExists(final ProcessItem item, final List<String> counters) {
        if (counters.contains(ProcessItem.COUNTER_OPEN_CASES)) {
            // Open is all states without the terminal states
            final Map<String, String> filters = new HashMap<>();
            filters.put(CaseItem.FILTER_CALLER, "any");
            filters.put(CaseItem.ATTRIBUTE_PROCESS_ID, item.getId().toString());
            item.setAttribute(ProcessItem.COUNTER_OPEN_CASES, getCaseDatastore().count(null, null, filters));
        }
    }

    protected ProcessDatastore getProcessDatastore() {
        return new ProcessDatastore(getEngineSession());
    }

    protected CaseDatastore getCaseDatastore() {
        return new CaseDatastore(getEngineSession());
    }

}
