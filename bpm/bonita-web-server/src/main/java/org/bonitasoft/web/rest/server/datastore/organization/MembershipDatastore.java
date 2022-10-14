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

import static org.bonitasoft.web.rest.server.framework.utils.SearchOptionsBuilderUtil.computeIndex;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.identity.UserMembershipCriterion;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.rest.model.identity.MembershipItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasAdd;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasDelete;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasSearch;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIForbiddenException;
import org.bonitasoft.web.toolkit.client.common.i18n.T_;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Séverin Moussel
 */
public class MembershipDatastore extends CommonDatastore<MembershipItem, UserMembership> implements
        DatastoreHasAdd<MembershipItem>,
        DatastoreHasSearch<MembershipItem>,
        DatastoreHasDelete {

    public MembershipDatastore(final APISession engineSession) {
        super(engineSession);
    }

    /**
     * @return
     * @throws InvalidSessionException
     * @throws BonitaHomeNotSetException
     * @throws ServerAPIException
     * @throws UnknownAPITypeException
     */
    private IdentityAPI getIdentityAPI()
            throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getIdentityAPI(getEngineSession());
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CONVERT
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected MembershipItem convertEngineToConsoleItem(final UserMembership item) {
        final MembershipItem result = new MembershipItem();

        result.setUserId(item.getUserId());
        result.setRoleId(item.getRoleId());
        result.setGroupId(item.getGroupId());
        result.setAssignedByUserId(item.getAssignedBy());
        result.setAssignedDate(item.getAssignedDate());

        return result;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CRUDS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void delete(final List<APIID> ids) {
        try {
            for (final APIID id : ids) {
                getIdentityAPI().deleteUserMembership(id.getPartAsLong(0), id.getPartAsLong(1), id.getPartAsLong(2));
            }
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    @Override
    public ItemSearchResult<MembershipItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        try {

            Long userId = MapUtil.getValueAsLong(filters, MembershipItem.ATTRIBUTE_USER_ID);
            // Get results
            final List<UserMembership> engineItems = getIdentityAPI().getUserMemberships(
                    userId, computeIndex(page, resultsByPage), resultsByPage, UserMembershipCriterion.valueOf(orders));

            // Convert results
            final List<MembershipItem> consoleSearchResults = convertEngineToConsoleItemsList(
                    engineItems);

            // Get total results
            final long total = getIdentityAPI()
                    .getNumberOfUserMemberships(MapUtil.getValueAsLong(filters, MembershipItem.ATTRIBUTE_USER_ID));

            // Return search object
            return new ItemSearchResult<>(
                    page,
                    resultsByPage,
                    total,
                    consoleSearchResults);

        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    @Override
    public MembershipItem add(final MembershipItem item) {
        try {
            return convertEngineToConsoleItem(getIdentityAPI()
                    .addUserMembership(item.getUserId().toLong(), item.getGroupId().toLong(),
                            item.getRoleId().toLong()));
        } catch (AlreadyExistsException e) {
            throw new APIForbiddenException(new T_("This membership is already added to user"), e);
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }
}
