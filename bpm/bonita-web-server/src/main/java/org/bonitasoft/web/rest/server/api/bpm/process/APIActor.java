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

import static org.bonitasoft.web.rest.model.bpm.process.ActorItem.ATTRIBUTE_PROCESS_ID;
import static org.bonitasoft.web.toolkit.client.data.item.template.ItemHasDualName.ATTRIBUTE_NAME;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.web.rest.model.bpm.process.ActorDefinition;
import org.bonitasoft.web.rest.model.bpm.process.ActorItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ActorDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ProcessDatastore;
import org.bonitasoft.web.rest.server.framework.api.APIHasGet;
import org.bonitasoft.web.rest.server.framework.api.APIHasSearch;
import org.bonitasoft.web.rest.server.framework.api.APIHasUpdate;
import org.bonitasoft.web.rest.server.framework.api.Datastore;
import org.bonitasoft.web.rest.server.framework.exception.APIFilterMandatoryException;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Julien Mege
 * @author Séverin Moussel
 */
public class APIActor extends ConsoleAPI<ActorItem> implements
        APIHasGet<ActorItem>,
        APIHasSearch<ActorItem>,
        APIHasUpdate<ActorItem> {

    @Override
    protected ItemDefinition defineItemDefinition() {
        return ActorDefinition.get();
    }

    @Override
    protected Datastore defineDefaultDatastore() {
        return new ActorDatastore(getEngineSession());
    }

    @Override
    protected List<String> defineReadOnlyAttributes() {
        return Arrays.asList(ATTRIBUTE_PROCESS_ID, ATTRIBUTE_NAME);
    }

    @Override
    public String defineDefaultSearchOrder() {
        return ActorCriterion.NAME_ASC.name();
    }

    @Override
    public ItemSearchResult<ActorItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {

        // Process id is a mandatory filter
        if (MapUtil.isBlank(filters, ActorItem.ATTRIBUTE_PROCESS_ID)) {
            throw new APIFilterMandatoryException(ActorItem.ATTRIBUTE_PROCESS_ID);
        }

        return super.search(page, resultsByPage, search, orders, filters);
    }

    @Override
    protected void fillDeploys(final ActorItem item, final List<String> deploys) {
        if (isDeployable(ATTRIBUTE_PROCESS_ID, deploys, item)) {
            item.setDeploy(ATTRIBUTE_PROCESS_ID, new ProcessDatastore(getEngineSession()).get(item.getProcessId()));
        }
    }

    @Override
    protected void fillCounters(final ActorItem item, final List<String> counters) {
        final ActorDatastore actorDatastore = (ActorDatastore) getDefaultDatastore();

        if (counters.contains(ActorItem.COUNTER_USERS)) {
            item.setAttribute(ActorItem.COUNTER_USERS, actorDatastore.countUsers(item.getId()));
        }
        if (counters.contains(ActorItem.COUNTER_GROUPS)) {
            item.setAttribute(ActorItem.COUNTER_GROUPS, actorDatastore.countGroups(item.getId()));
        }
        if (counters.contains(ActorItem.COUNTER_ROLES)) {
            item.setAttribute(ActorItem.COUNTER_ROLES, actorDatastore.countRoles(item.getId()));
        }
        if (counters.contains(ActorItem.COUNTER_MEMBERSHIPS)) {
            item.setAttribute(ActorItem.COUNTER_MEMBERSHIPS, actorDatastore.countMemberships(item.getId()));
        }

    }

}
