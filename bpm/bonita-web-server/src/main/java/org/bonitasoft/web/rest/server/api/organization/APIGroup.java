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
package org.bonitasoft.web.rest.server.api.organization;

import java.util.List;

import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.GroupCriterion;
import org.bonitasoft.web.rest.model.identity.GroupDefinition;
import org.bonitasoft.web.rest.model.identity.GroupItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.datastore.organization.GroupDatastore;
import org.bonitasoft.web.rest.server.framework.api.APIHasAdd;
import org.bonitasoft.web.rest.server.framework.api.APIHasDelete;
import org.bonitasoft.web.rest.server.framework.api.APIHasGet;
import org.bonitasoft.web.rest.server.framework.api.APIHasSearch;
import org.bonitasoft.web.rest.server.framework.api.APIHasUpdate;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Nicolas Tith
 */
public class APIGroup extends ConsoleAPI<GroupItem> implements
        APIHasAdd<GroupItem>,
        APIHasGet<GroupItem>,
        APIHasDelete,
        APIHasSearch<GroupItem>,
        APIHasUpdate<GroupItem> {

    @Override
    protected ItemDefinition<GroupItem> defineItemDefinition() {
        return GroupDefinition.get();
    }

    @Override
    protected GroupDatastore defineDefaultDatastore() {
        return new GroupDatastore(getEngineSession());
    }

    @Override
    public String defineDefaultSearchOrder() {
        return GroupCriterion.NAME_ASC.toString();
    }

    @Override
    protected void fillDeploys(final GroupItem item, final List<String> deploys) {
        if (deploys.contains(GroupItem.ATTRIBUTE_PARENT_GROUP_ID) && item.getParentPath() != null
                && !item.getParentPath().isEmpty()) {
            Group parentGroup = ((GroupDatastore) getDefaultDatastore()).getGroupEngineClient()
                    .getGroupByPath(item.getParentPath());
            item.setParentGroupId(String.valueOf(parentGroup.getId()));
        }
    }

    @Override
    protected void fillCounters(final GroupItem item, final List<String> counters) {
        if (counters.contains(GroupItem.COUNTER_NUMBER_OF_USERS)) {
            item.setAttribute(GroupItem.COUNTER_NUMBER_OF_USERS,
                    ((GroupDatastore) getDefaultDatastore()).getNumberOfUsers(item.getId()));
        }
    }

}
