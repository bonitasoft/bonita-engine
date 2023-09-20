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

import static org.bonitasoft.web.toolkit.client.data.APIID.toLongList;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.GroupCreator;
import org.bonitasoft.engine.identity.GroupSearchDescriptor;
import org.bonitasoft.engine.identity.GroupUpdater;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.identity.GroupItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.engineclient.EngineAPIAccessor;
import org.bonitasoft.web.rest.server.engineclient.EngineClientFactory;
import org.bonitasoft.web.rest.server.engineclient.GroupEngineClient;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasAdd;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasDelete;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasGet;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasSearch;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasUpdate;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.rest.server.framework.utils.SearchOptionsBuilderUtil;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Nicolas Tith
 */
public class GroupDatastore extends CommonDatastore<GroupItem, Group> implements
        DatastoreHasAdd<GroupItem>,
        DatastoreHasUpdate<GroupItem>,
        DatastoreHasGet<GroupItem>,
        DatastoreHasSearch<GroupItem>, DatastoreHasDelete {

    public GroupDatastore(final APISession engineSession) {
        super(engineSession);
    }

    public GroupEngineClient getGroupEngineClient() {
        return new EngineClientFactory(new EngineAPIAccessor(getEngineSession()))
                .createGroupEngineClient();
    }

    @Override
    public void delete(final List<APIID> ids) {
        getGroupEngineClient().delete(toLongList(ids));
    }

    @Override
    public ItemSearchResult<GroupItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        try {
            final SearchOptionsBuilder builder = SearchOptionsBuilderUtil.buildSearchOptions(page, resultsByPage,
                    orders, search);

            addStringFilterToSearchBuilder(filters, builder, GroupItem.ATTRIBUTE_NAME, GroupSearchDescriptor.NAME);
            addStringFilterToSearchBuilder(filters, builder, GroupItem.ATTRIBUTE_DISPLAY_NAME,
                    GroupSearchDescriptor.DISPLAY_NAME);
            addStringFilterToSearchBuilder(filters, builder, GroupItem.ATTRIBUTE_PARENT_PATH,
                    GroupSearchDescriptor.PARENT_PATH);

            SearchResult<Group> engineSearchResults;
            engineSearchResults = TenantAPIAccessor.getIdentityAPI(getEngineSession()).searchGroups(builder.done());

            return new ItemSearchResult<>(page, resultsByPage, engineSearchResults.getCount(),
                    new GroupItemConverter().convert(engineSearchResults.getResult()));

        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    @Override
    public GroupItem get(final APIID id) {
        Group result = getGroupEngineClient().get(id.toLong());
        return new GroupItemConverter().convert(result);
    }

    @Override
    public GroupItem update(final APIID id, final Map<String, String> attributes) {
        GroupUpdater updater = new GroupUpdaterConverter(getGroupEngineClient()).convert(attributes);
        Group group = getGroupEngineClient().update(id.toLong(), updater);
        return new GroupItemConverter().convert(group);
    }

    @Override
    public GroupItem add(final GroupItem group) {
        GroupCreator creator = new GroupCreatorConverter(getGroupEngineClient()).convert(group);
        Group result = getGroupEngineClient().create(creator);
        return new GroupItemConverter().convert(result);
    }

    public Long getNumberOfUsers(final APIID groupId) {
        try {
            return TenantAPIAccessor.getIdentityAPI(getEngineSession()).getNumberOfUsersInGroup(groupId.toLong());
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    @Override
    protected GroupItem convertEngineToConsoleItem(final Group group) {
        throw new RuntimeException("Unimplemented method");
    }
}
