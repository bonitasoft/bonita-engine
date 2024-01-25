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
package org.bonitasoft.web.rest.server.datastore.organization;

import java.util.List;
import java.util.Map;

import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCreator;
import org.bonitasoft.engine.identity.UserUpdater;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.datastore.filter.Filters;
import org.bonitasoft.web.rest.server.datastore.utils.SearchOptionsCreator;
import org.bonitasoft.web.rest.server.datastore.utils.Sorts;
import org.bonitasoft.web.rest.server.engineclient.EngineAPIAccessor;
import org.bonitasoft.web.rest.server.engineclient.EngineClientFactory;
import org.bonitasoft.web.rest.server.engineclient.ProcessEngineClient;
import org.bonitasoft.web.rest.server.engineclient.UserEngineClient;
import org.bonitasoft.web.rest.server.framework.api.*;
import org.bonitasoft.web.rest.server.framework.exception.APIAttributeException;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Séverin Moussel
 */
public class UserDatastore extends CommonDatastore<UserItem, User>
        implements DatastoreHasAdd<UserItem>,
        DatastoreHasGet<UserItem>,
        DatastoreHasSearch<UserItem>,
        DatastoreHasUpdate<UserItem>,
        DatastoreHasDelete {

    protected EngineClientFactory engineClientFactory;

    protected UserItemConverter userItemConverter;

    public UserDatastore(final APISession engineSession) {
        super(engineSession);
        userItemConverter = new UserItemConverter();
        engineClientFactory = new EngineClientFactory(new EngineAPIAccessor(engineSession));
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // C.R.U.D.
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public UserItem add(final UserItem user) {
        UserCreator userCreator = new UserCreatorConverter().convert(user);
        User createdUser = getUserEngineClient().create(userCreator);
        return userItemConverter.convert(createdUser);
    }

    public UserItem update(final APIID id, final Map<String, String> attributes) {
        UserUpdater userUpdater = new UserUpdaterConverter().convert(attributes, getBonitaHomeFolderAccessor());
        User user = getUserEngineClient().update(id.toLong(), userUpdater);
        return userItemConverter.convert(user);
    }

    BonitaHomeFolderAccessor getBonitaHomeFolderAccessor() {
        return new BonitaHomeFolderAccessor();
    }

    @Override
    public UserItem get(final APIID id) {
        User user = getUserEngineClient().get(id.toLong());
        return userItemConverter.convert(user);
    }

    /**
     * Search for users
     *
     * @param page
     *        The page to display
     * @param resultsByPage
     *        The number of results by page
     * @param search
     *        Search terms
     * @param filters
     *        The filters to doAuthorize. There will be an AND operand between filters.
     * @param orders
     *        The order to doAuthorize to the search
     * @return This method returns an ItemSearch result containing the returned data and information about the total
     *         possible results.
     */
    @Override
    public ItemSearchResult<UserItem> search(final int page, final int resultsByPage, final String search,
            final String orders, Map<String, String> filters) {

        if (filters.containsKey(UserItem.FILTER_PROCESS_ID)) {
            String processId = filters.get(UserItem.FILTER_PROCESS_ID);
            filters.remove(UserItem.FILTER_PROCESS_ID);
            return searchUsersWhoCanStartProcess(processId, page, resultsByPage, search, filters, orders);
        } else if (filters.containsKey(UserItem.FILTER_HUMAN_TASK_ID)) {
            String taskId = filters.get(UserItem.FILTER_HUMAN_TASK_ID);
            filters.remove(UserItem.FILTER_HUMAN_TASK_ID);
            return searchUsersWhoCanPerformTask(taskId, page, resultsByPage, search, filters, orders);
        } else {
            return searchUsers(page, resultsByPage, search, filters, orders);
        }

    }

    private ItemSearchResult<UserItem> searchUsers(final int page, final int resultsByPage, final String search,
            final Map<String, String> filters, final String orders) {

        SearchOptionsCreator searchOptionsCreator = buildSearchOptionCreator(page,
                resultsByPage, search, filters, orders);

        SearchResult<User> engineSearchResults = getUserEngineClient().search(searchOptionsCreator.create());

        return new ItemSearchResult<>(page, resultsByPage, engineSearchResults.getCount(),
                userItemConverter.convert(engineSearchResults.getResult()));

    }

    protected ItemSearchResult<UserItem> searchUsersWhoCanStartProcess(final String processId, final int page,
            final int resultsByPage, final String search,
            final Map<String, String> filters, final String orders) {

        SearchOptionsCreator searchOptionsCreator = buildSearchOptionCreator(page,
                resultsByPage, search, filters, orders);

        SearchResult<User> engineSearchResults;
        try {
            engineSearchResults = getProcessEngineClient().getProcessApi().searchUsersWhoCanStartProcessDefinition(
                    Long.valueOf(processId),
                    searchOptionsCreator.create());
        } catch (NumberFormatException e) {
            throw new APIAttributeException(UserItem.FILTER_PROCESS_ID,
                    "Cannot convert process id: " + processId + " into long.");
        } catch (SearchException e) {
            throw new APIException(e);
        }

        return new ItemSearchResult<>(page, resultsByPage, engineSearchResults.getCount(),
                userItemConverter.convert(engineSearchResults.getResult()));

    }

    protected ItemSearchResult<UserItem> searchUsersWhoCanPerformTask(final String taskId, final int page,
            final int resultsByPage, final String search,
            final Map<String, String> filters, final String orders) {

        SearchResult<User> engineSearchResults;
        try {
            SearchOptionsCreator searchOptionsCreator = buildSearchOptionCreator(page,
                    resultsByPage, search, filters, orders);
            engineSearchResults = getProcessEngineClient().getProcessApi().searchUsersWhoCanExecutePendingHumanTask(
                    Long.valueOf(taskId),
                    searchOptionsCreator.create());

        } catch (NumberFormatException e) {
            throw new APIAttributeException(UserItem.FILTER_HUMAN_TASK_ID,
                    "Cannot convert human task id: " + taskId + " into long.");
        }

        return new ItemSearchResult<>(page, resultsByPage, engineSearchResults.getCount(),
                userItemConverter.convert(engineSearchResults.getResult()));

    }

    protected SearchOptionsCreator buildSearchOptionCreator(final int page,
            final int resultsByPage, final String search,
            final Map<String, String> filters, final String orders) {
        return new SearchOptionsCreator(page, resultsByPage, search,
                new Sorts(orders, new UserSearchAttributeConverter()),
                new Filters(filters, new UserFilterCreator(new UserSearchAttributeConverter())));
    }

    /**
     * Delete users
     *
     * @param ids
     */
    public void delete(final List<APIID> ids) {
        getUserEngineClient().delete(APIID.toLongList(ids));
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CONVERTS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // protected for tests
    UserEngineClient getUserEngineClient() {
        return engineClientFactory.createUserEngineClient();
    }

    ProcessEngineClient getProcessEngineClient() {
        return engineClientFactory.createProcessEngineClient();
    }

    @Override
    protected UserItem convertEngineToConsoleItem(final User user) {
        throw new RuntimeException("Unimplemented method");
    }
}
