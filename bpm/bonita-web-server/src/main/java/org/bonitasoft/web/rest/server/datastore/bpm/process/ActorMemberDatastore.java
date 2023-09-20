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
package org.bonitasoft.web.rest.server.datastore.bpm.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.console.common.server.utils.ListUtil;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.actor.ActorMember;
import org.bonitasoft.engine.exception.*;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.rest.model.bpm.process.ActorMemberItem;
import org.bonitasoft.web.rest.model.identity.MemberType;
import org.bonitasoft.web.rest.model.portal.profile.AbstractMemberItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasAdd;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasDelete;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasSearch;
import org.bonitasoft.web.rest.server.framework.exception.APIAttributesException;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIForbiddenException;
import org.bonitasoft.web.toolkit.client.common.i18n.T_;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.bonitasoft.web.toolkit.client.common.util.StringUtil;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Séverin Moussel
 */
public class ActorMemberDatastore extends CommonDatastore<ActorMemberItem, ActorMember> implements
        DatastoreHasAdd<ActorMemberItem>,
        DatastoreHasSearch<ActorMemberItem>,
        DatastoreHasDelete {

    public ActorMemberDatastore(final APISession engineSession) {
        super(engineSession);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @throws InvalidSessionException
     * @throws BonitaHomeNotSetException
     * @throws ServerAPIException
     * @throws UnknownAPITypeException
     */
    private ProcessAPI getProcessAPI()
            throws InvalidSessionException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getProcessAPI(getEngineSession());
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CRUDS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void delete(final List<APIID> ids) {
        try {
            for (final APIID id : ids) {
                getProcessAPI().removeActorMember(id.toLong());
            }
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    @Override
    public ItemSearchResult<ActorMemberItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {

        try {
            final Long actorId = MapUtil.getValueAsLong(filters, ActorMemberItem.ATTRIBUTE_ACTOR_ID);

            final List<ActorMember> unfilteredResults = getProcessAPI().getActorMembers(
                    actorId,
                    0, Integer.MAX_VALUE);

            final List<ActorMember> filteredResults = applyTypeFilter(filters.get(ActorMemberItem.FILTER_MEMBER_TYPE),
                    unfilteredResults);

            @SuppressWarnings("unchecked")
            final List<ActorMember> paginatedResults = (List<ActorMember>) ListUtil.paginate(filteredResults, page,
                    resultsByPage);

            final List<ActorMemberItem> finalResults = convertEngineToConsoleItemsList(paginatedResults);
            for (final ActorMemberItem actorMemberItem : finalResults) {
                actorMemberItem.setActorId(actorId);
            }

            return new ItemSearchResult<>(
                    page, resultsByPage,
                    filteredResults.size(),
                    finalResults);

        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    /**
     * @param filterType
     *        We accept filter value or {@link MemberType} enum value. Better use MemberType enum value
     */
    private List<ActorMember> applyTypeFilter(final String filterType, final List<ActorMember> unfilteredResults) {
        final List<ActorMember> filteredResults = new ArrayList<>();
        if (StringUtil.isBlank(filterType)) {
            filteredResults.addAll(unfilteredResults);
        } else {
            if (isMemberTypeUser(filterType)) {
                filterToUser(unfilteredResults, filteredResults);
            } else if (isMemberTypeRole(filterType)) {
                filterToRole(unfilteredResults, filteredResults);
            } else if (isMemberTypeGroup(filterType)) {
                filterToGroup(unfilteredResults, filteredResults);
            } else if (isMemberTypeMembership(filterType)) {
                filterToMembership(unfilteredResults, filteredResults);
            }
        }

        return filteredResults;
    }

    private boolean isMemberTypeUser(final String filterType) {
        return AbstractMemberItem.VALUE_MEMBER_TYPE_USER.equalsIgnoreCase(filterType)
                || MemberType.USER.name().equals(filterType);
    }

    private boolean isMemberTypeRole(final String filterType) {
        return AbstractMemberItem.VALUE_MEMBER_TYPE_ROLE.equalsIgnoreCase(filterType)
                || MemberType.ROLE.name().equals(filterType);
    }

    private boolean isMemberTypeGroup(final String filterType) {
        return AbstractMemberItem.VALUE_MEMBER_TYPE_GROUP.equalsIgnoreCase(filterType)
                || MemberType.GROUP.name().equals(filterType);
    }

    private boolean isMemberTypeMembership(final String filterType) {
        return AbstractMemberItem.VALUE_MEMBER_TYPE_MEMBERSHIP.equalsIgnoreCase(filterType)
                || MemberType.MEMBERSHIP.name().equals(filterType);
    }

    private void filterToUser(final List<ActorMember> unfilteredResults, final List<ActorMember> filteredResults) {
        for (final ActorMember result : unfilteredResults) {
            if (APIID.makeAPIID(result.getUserId()) != null) {
                filteredResults.add(result);
            }
        }
    }

    private void filterToRole(final List<ActorMember> unfilteredResults, final List<ActorMember> filteredResults) {
        for (final ActorMember result : unfilteredResults) {
            if (APIID.makeAPIID(result.getRoleId()) != null && APIID.makeAPIID(result.getGroupId()) == null) {
                filteredResults.add(result);
            }
        }
    }

    private void filterToGroup(final List<ActorMember> unfilteredResults, final List<ActorMember> filteredResults) {
        for (final ActorMember result : unfilteredResults) {
            if (APIID.makeAPIID(result.getGroupId()) != null && APIID.makeAPIID(result.getRoleId()) == null) {
                filteredResults.add(result);
            }
        }
    }

    private void filterToMembership(final List<ActorMember> unfilteredResults,
            final List<ActorMember> filteredResults) {
        for (final ActorMember result : unfilteredResults) {
            if (APIID.makeAPIID(result.getGroupId()) != null && APIID.makeAPIID(result.getRoleId()) != null) {
                filteredResults.add(result);
            }
        }
    }

    @Override
    public ActorMemberItem add(final ActorMemberItem item) {
        try {
            ActorMember addedActorMember = null;
            if (isUserActorMember(item)) {
                addedActorMember = addUserActorMember(item);
            } else if (isMembershipActorMember(item)) {
                addedActorMember = addMembershipActorMember(item);
            } else if (isRoleActorMember(item)) {
                addedActorMember = addRoleActorMember(item);
            } else if (isGroupActorMember(item)) {
                addedActorMember = addGroupActorMember(item);
            } else {
                throw new APIAttributesException(ActorMemberItem.ATTRIBUTE_USER_ID, ActorMemberItem.ATTRIBUTE_ROLE_ID,
                        ActorMemberItem.ATTRIBUTE_GROUP_ID);
            }
            final ActorMemberItem addedItem = convertEngineToConsoleItem(addedActorMember);
            addedItem.setActorId(item.getActorId());
            return addedItem;
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    private ActorMember addGroupActorMember(final ActorMemberItem item)
            throws InvalidSessionException, NotFoundException,
            BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        try {
            return getProcessAPI().addGroupToActor(item.getActorId().toLong(), item.getGroupId().toLong());
        } catch (final AlreadyExistsException e) {
            throw new APIForbiddenException(new T_("This group has already been mapped to actor"), e);
        } catch (CreationException e) {
            throw new APIException(new T_("Error when adding group to actor member"), e);
        }
    }

    private ActorMember addRoleActorMember(final ActorMemberItem item)
            throws InvalidSessionException, NotFoundException,
            BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        try {
            return getProcessAPI().addRoleToActor(item.getActorId().toLong(), item.getRoleId().toLong());
        } catch (final CreationException e) {
            throw new APIForbiddenException(new T_("This role has already been mapped to actor"), e);
        }
    }

    private ActorMember addMembershipActorMember(final ActorMemberItem item) throws InvalidSessionException,
            NotFoundException, BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        try {
            return getProcessAPI()
                    .addRoleAndGroupToActor(item.getActorId().toLong(), item.getRoleId().toLong(),
                            item.getGroupId().toLong());
        } catch (final CreationException e) {
            throw new APIForbiddenException(new T_("This membership has already been mapped to actor"), e);
        }
    }

    private ActorMember addUserActorMember(final ActorMemberItem item)
            throws InvalidSessionException, NotFoundException,
            BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        try {
            return getProcessAPI().addUserToActor(item.getActorId().toLong(), item.getUserId().toLong());
        } catch (final CreationException e) {
            throw new APIForbiddenException(new T_("This user has already been mapped to actor"), e);
        }
    }

    private boolean isGroupActorMember(final ActorMemberItem item) {
        return item.getGroupId() != null;
    }

    private boolean isRoleActorMember(final ActorMemberItem item) {
        return item.getRoleId() != null;
    }

    private boolean isMembershipActorMember(final ActorMemberItem item) {
        return isRoleActorMember(item) && isGroupActorMember(item);
    }

    private boolean isUserActorMember(final ActorMemberItem item) {
        return item.getUserId() != null;
    }

    @Override
    protected ActorMemberItem convertEngineToConsoleItem(final ActorMember item) {
        final ActorMemberItem actorMemberItem = new ActorMemberItem();

        actorMemberItem.setId(item.getId());
        actorMemberItem.setUserId(item.getUserId());
        actorMemberItem.setRoleId(item.getRoleId());
        actorMemberItem.setGroupId(item.getGroupId());

        return actorMemberItem;
    }
}
