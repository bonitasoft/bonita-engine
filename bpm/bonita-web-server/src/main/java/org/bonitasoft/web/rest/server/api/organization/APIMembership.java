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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.identity.UserMembershipCriterion;
import org.bonitasoft.web.rest.model.identity.MembershipDefinition;
import org.bonitasoft.web.rest.model.identity.MembershipItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.datastore.organization.GroupDatastore;
import org.bonitasoft.web.rest.server.datastore.organization.MembershipDatastore;
import org.bonitasoft.web.rest.server.datastore.organization.RoleDatastore;
import org.bonitasoft.web.rest.server.datastore.organization.UserDatastore;
import org.bonitasoft.web.rest.server.framework.api.APIHasAdd;
import org.bonitasoft.web.rest.server.framework.api.APIHasDelete;
import org.bonitasoft.web.rest.server.framework.api.APIHasSearch;
import org.bonitasoft.web.rest.server.framework.api.Datastore;
import org.bonitasoft.web.rest.server.framework.exception.APIFilterException;
import org.bonitasoft.web.rest.server.framework.exception.APIFilterMandatoryException;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Séverin Moussel
 */
public class APIMembership extends ConsoleAPI<MembershipItem> implements
        APIHasAdd<MembershipItem>,
        APIHasSearch<MembershipItem>,
        APIHasDelete {

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CONFIGURATION
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected ItemDefinition defineItemDefinition() {
        return Definitions.get(MembershipDefinition.TOKEN);
    }

    @Override
    protected List<String> defineReadOnlyAttributes() {
        return Arrays.asList(
                MembershipItem.ATTRIBUTE_ASSIGNED_BY_USER_ID,
                MembershipItem.ATTRIBUTE_ASSIGNED_DATE);
    }

    @Override
    protected void fillDeploys(final MembershipItem item, final List<String> deploys) {
        if (isDeployable(MembershipItem.ATTRIBUTE_USER_ID, deploys, item)) {
            item.setDeploy(MembershipItem.ATTRIBUTE_USER_ID,
                    new UserDatastore(getEngineSession()).get(item.getUserId()));
        }

        if (isDeployable(MembershipItem.ATTRIBUTE_ROLE_ID, deploys, item)) {
            item.setDeploy(MembershipItem.ATTRIBUTE_ROLE_ID,
                    new RoleDatastore(getEngineSession()).get(item.getRoleId()));
        }

        if (isDeployable(MembershipItem.ATTRIBUTE_GROUP_ID, deploys, item)) {
            item.setDeploy(MembershipItem.ATTRIBUTE_GROUP_ID,
                    new GroupDatastore(getEngineSession()).get(item.getGroupId()));
        }

        if (isDeployable(MembershipItem.ATTRIBUTE_ASSIGNED_BY_USER_ID, deploys, item)) {
            item.setDeploy(MembershipItem.ATTRIBUTE_ASSIGNED_BY_USER_ID,
                    new UserDatastore(getEngineSession()).get(item.getAssignedByUserId()));
        }
    }

    @Override
    protected Datastore defineDefaultDatastore() {
        return new MembershipDatastore(getEngineSession());
    }

    @Override
    public String defineDefaultSearchOrder() {
        return UserMembershipCriterion.ROLE_NAME_ASC.name();
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CRUDS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ItemSearchResult<MembershipItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {

        // user_id filter mandatory
        if (filters.size() == 0) {
            throw new APIFilterMandatoryException(MembershipItem.ATTRIBUTE_USER_ID);
        }
        // only user_id filter allowed
        else if (filters.size() > 1 || !filters.containsKey(MembershipItem.ATTRIBUTE_USER_ID)) {
            throw new APIFilterException("Cant search on filter other than " + MembershipItem.ATTRIBUTE_USER_ID);
        }

        return super.search(page, resultsByPage, search, orders, filters);
    }

}
