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

import static org.bonitasoft.web.rest.model.bpm.process.ActorMemberItem.ATTRIBUTE_ACTOR_ID;
import static org.bonitasoft.web.rest.model.portal.profile.AbstractMemberItem.FILTER_MEMBER_TYPE;

import java.util.List;
import java.util.Map;

import org.bonitasoft.web.rest.model.bpm.process.ActorMemberDefinition;
import org.bonitasoft.web.rest.model.bpm.process.ActorMemberItem;
import org.bonitasoft.web.rest.server.api.profile.AbstractAPIMember;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ActorDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ActorMemberDatastore;
import org.bonitasoft.web.rest.server.framework.api.Datastore;
import org.bonitasoft.web.rest.server.framework.exception.APIFilterEmptyException;
import org.bonitasoft.web.rest.server.framework.exception.APIFilterMandatoryException;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Julien Mege
 * @author Séverin Moussel
 */
public class APIActorMember extends AbstractAPIMember<ActorMemberItem> {

    @Override
    protected ItemDefinition defineItemDefinition() {
        return ActorMemberDefinition.get();
    }

    @Override
    public String defineDefaultSearchOrder() {
        return "";
    }

    @Override
    protected Datastore defineDefaultDatastore() {
        return new ActorMemberDatastore(getEngineSession());
    }

    @Override
    public ItemSearchResult<ActorMemberItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        if (MapUtil.isBlank(filters, ATTRIBUTE_ACTOR_ID)) {
            throw new APIFilterMandatoryException(ATTRIBUTE_ACTOR_ID);
        }

        if (filters.containsKey(FILTER_MEMBER_TYPE) && MapUtil.isBlank(filters, FILTER_MEMBER_TYPE)) {
            throw new APIFilterEmptyException(FILTER_MEMBER_TYPE);
        }

        return super.search(page, resultsByPage, search, orders, filters);
    }

    @Override
    protected void fillDeploys(final ActorMemberItem item, final List<String> deploys) {
        if (isDeployable(ActorMemberItem.ATTRIBUTE_ACTOR_ID, deploys, item)) {
            item.setDeploy(ActorMemberItem.ATTRIBUTE_ACTOR_ID,
                    new ActorDatastore(getEngineSession()).get(item.getActorId()));
        }

        super.fillDeploys(item, deploys);
    }

}
