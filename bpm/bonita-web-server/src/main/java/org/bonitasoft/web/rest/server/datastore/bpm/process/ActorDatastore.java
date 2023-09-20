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

import static org.bonitasoft.web.rest.model.bpm.process.ActorItem.ATTRIBUTE_DESCRIPTION;
import static org.bonitasoft.web.toolkit.client.data.item.template.ItemHasDualName.ATTRIBUTE_DISPLAY_NAME;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.actor.ActorUpdater;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.InvalidSessionException;
import org.bonitasoft.web.rest.model.bpm.process.ActorItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasGet;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasSearch;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasUpdate;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.rest.server.framework.utils.SearchOptionsBuilderUtil;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Vincent Elcrin
 * @author Séverin Moussel
 */
public class ActorDatastore extends CommonDatastore<ActorItem, ActorInstance> implements
        DatastoreHasGet<ActorItem>,
        DatastoreHasSearch<ActorItem>,
        DatastoreHasUpdate<ActorItem> {

    public ActorDatastore(final APISession engineSession) {
        super(engineSession);
    }

    @Override
    protected ActorItem convertEngineToConsoleItem(final ActorInstance engineItem) {
        final ActorItem result = new ActorItem();

        result.setId(engineItem.getId());
        result.setName(engineItem.getName());
        result.setDisplayName(engineItem.getDisplayName());
        result.setDescription(engineItem.getDescription());
        result.setProcessId(engineItem.getProcessDefinitionId());

        return result;
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
    public ActorItem get(final APIID id) {
        try {
            return convertEngineToConsoleItem(getProcessAPI().getActor(id.toLong()));
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    @Override
    public ItemSearchResult<ActorItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        try {

            final Long processId = MapUtil.getValueAsLong(filters, ActorItem.ATTRIBUTE_PROCESS_ID);

            final List<ActorInstance> actors = getProcessAPI().getActors(processId,
                    SearchOptionsBuilderUtil.computeIndex(page, resultsByPage), resultsByPage,
                    ActorCriterion.valueOf(orders.toUpperCase().replace(" ", "_")));

            return new ItemSearchResult<>(
                    page,
                    resultsByPage,
                    getProcessAPI().getNumberOfActors(processId),
                    convertEngineToConsoleItemsList(actors));

        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    @Override
    public ActorItem update(final APIID id, final Map<String, String> attributes) {
        try {
            final ActorUpdater updater = new ActorUpdater();

            if (attributes.containsKey(ATTRIBUTE_DISPLAY_NAME)) {
                updater.setDisplayName(attributes.get(ATTRIBUTE_DISPLAY_NAME));
            }
            if (attributes.containsKey(ATTRIBUTE_DESCRIPTION)) {
                updater.setDescription(attributes.get(ATTRIBUTE_DESCRIPTION));
            }

            return convertEngineToConsoleItem(getProcessAPI().updateActor(id.toLong(), updater));

        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COUNTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Long countUsers(final APIID actorId) {
        try {
            return getProcessAPI().getNumberOfUsersOfActor(actorId.toLong());
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    public Long countGroups(final APIID actorId) {
        try {
            return getProcessAPI().getNumberOfGroupsOfActor(actorId.toLong());
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    public Long countRoles(final APIID actorId) {
        try {
            return getProcessAPI().getNumberOfRolesOfActor(actorId.toLong());
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    public Long countMemberships(final APIID actorId) {
        try {
            return getProcessAPI().getNumberOfMembershipsOfActor(actorId.toLong());
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }
}
