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
package org.bonitasoft.web.rest.server.datastore.bpm.process.helper;

import java.util.Map;

import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.web.rest.model.bpm.process.ProcessItem;
import org.bonitasoft.web.rest.server.datastore.converter.ItemSearchResultConverter;
import org.bonitasoft.web.rest.server.datastore.filter.Filters;
import org.bonitasoft.web.rest.server.datastore.utils.SearchOptionsCreator;
import org.bonitasoft.web.rest.server.datastore.utils.Sorts;
import org.bonitasoft.web.rest.server.engineclient.ProcessEngineClient;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasSearch;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Vincent Elcrin
 */
public class SearchProcessHelper implements DatastoreHasSearch<ProcessItem> {

    private final ProcessEngineClient engineClient;

    public SearchProcessHelper(ProcessEngineClient engineClient) {
        this.engineClient = engineClient;
    }

    @Override
    public ItemSearchResult<ProcessItem> search(int page, int resultsByPage, String search, String orders,
            Map<String, String> filters) {
        SearchOptionsCreator searchOptions = new SearchOptionsCreator(page, resultsByPage, search,
                new Sorts(orders, new ProcessSearchDescriptorConverter()),
                new Filters(filters, new SearchProcessFilterCreator(new ProcessSearchDescriptorConverter())));
        final SearchResult<ProcessDeploymentInfo> result = runSearch(filters, searchOptions.create());
        return convertResult(page, resultsByPage, result);
    }

    private SearchResult<ProcessDeploymentInfo> runSearch(Map<String, String> filters, SearchOptions searchOptions) {

        if (isFilteringOn(filters, ProcessItem.FILTER_USER_ID, ProcessItem.FILTER_RECENT_PROCESSES)) {
            return engineClient.searchRecentlyStartedProcessDefinitions(getApiId(filters, ProcessItem.FILTER_USER_ID),
                    searchOptions);
        } else if (isFilteringOn(filters, ProcessItem.FILTER_USER_ID, ProcessItem.FILTER_CATEGORY_ID)
                && filters.get(ProcessItem.FILTER_CATEGORY_ID) == null) {
            return engineClient.searchUncategorizedProcessDefinitionsUserCanStart(
                    getApiId(filters, ProcessItem.FILTER_USER_ID), searchOptions);
        } else if (isFilteringOn(filters, ProcessItem.FILTER_USER_ID,
                ProcessItem.FILTER_FOR_PENDING_OR_ASSIGNED_TASKS)) {
            return engineClient.searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksFor(
                    getApiId(filters, ProcessItem.FILTER_USER_ID), searchOptions);
        } else if (isFilteringOn(filters, ProcessItem.FILTER_USER_ID)) {
            return engineClient.searchProcessDeploymentInfos(getApiId(filters, ProcessItem.FILTER_USER_ID),
                    searchOptions);
        } else if (isFilteringOn(filters, ProcessItem.FILTER_SUPERVISOR_ID,
                ProcessItem.FILTER_FOR_PENDING_OR_ASSIGNED_TASKS)) {
            return engineClient.searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(
                    getApiId(filters, ProcessItem.FILTER_USER_ID),
                    searchOptions);
        } else if (isFilteringOn(filters, ProcessItem.FILTER_SUPERVISOR_ID)) {
            return engineClient.searchProcessDefinitionsSupervisedBy(
                    getApiId(filters, ProcessItem.FILTER_SUPERVISOR_ID), searchOptions);
        } else if (isFilteringOn(filters, ProcessItem.FILTER_FOR_PENDING_OR_ASSIGNED_TASKS)) {
            return engineClient.searchProcessDeploymentInfosWithAssignedOrPendingHumanTasks(searchOptions);
        } else {
            return engineClient.searchProcessDefinitions(searchOptions);
        }
    }

    private boolean isFilteringOn(Map<String, String> filters, String... attributes) {
        for (String attribute : attributes) {
            if (!filters.containsKey(attribute)) {
                return false;
            }
        }
        return true;
    }

    private Long getApiId(Map<String, String> filters, String attribute) {
        return APIID.makeAPIID(filters.get(attribute)).toLong();
    }

    private ItemSearchResult<ProcessItem> convertResult(int page, int nbResultsByPage,
            final SearchResult<ProcessDeploymentInfo> result) {
        return new ItemSearchResultConverter<>(page, nbResultsByPage, result, new ProcessItemConverter(
                engineClient.getProcessApi())).toItemSearchResult();
    }
}
