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
package org.bonitasoft.web.rest.server.api.bpm.cases;

import java.util.List;
import java.util.Map;

import org.bonitasoft.web.rest.model.bpm.cases.CommentDefinition;
import org.bonitasoft.web.rest.model.bpm.cases.CommentItem;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.datastore.bpm.cases.CommentDatastore;
import org.bonitasoft.web.rest.server.datastore.organization.UserDatastore;
import org.bonitasoft.web.rest.server.framework.api.APIHasAdd;
import org.bonitasoft.web.rest.server.framework.api.APIHasSearch;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Vincent Elcrin
 */
public class APIComment extends ConsoleAPI<CommentItem> implements APIHasAdd<CommentItem>, APIHasSearch<CommentItem> {

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CONFIGURE
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected ItemDefinition defineItemDefinition() {
        return Definitions.get(CommentDefinition.TOKEN);
    }

    @Override
    public String defineDefaultSearchOrder() {
        // FIXME Define criterion
        return "";
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // C.R.U.D.S
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ItemSearchResult<CommentItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        return new CommentDatastore(getEngineSession()).search(page, resultsByPage, search, orders, filters);
    }

    @Override
    public CommentItem add(final CommentItem comment) {
        return new CommentDatastore(getEngineSession()).add(comment);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.web.toolkit.server.API#fillDeploys(org.bonitasoft.web.toolkit.client.data.item.Item,
     * java.util.List)
     */
    @Override
    protected void fillDeploys(final CommentItem item, final List<String> deploys) {
        if (isDeployable(CommentItem.ATTRIBUTE_USER_ID, deploys, item)) {
            item.setDeploy(CommentItem.ATTRIBUTE_USER_ID,
                    new UserDatastore(getEngineSession()).get(item.getUserId()));
        } else {
            item.setDeploy(CommentItem.ATTRIBUTE_USER_ID, getSystemUser());
        }

        // FIXME Deploy process instance
    }

    private UserItem getSystemUser() {
        final UserItem systemUser = new UserItem();
        systemUser.setUserName("System");
        systemUser.setIcon(UserItem.DEFAULT_USER_ICON);
        return systemUser;
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.web.toolkit.server.API#fillCounters(org.bonitasoft.web.toolkit.client.data.item.Item,
     * java.util.List)
     */
    @Override
    protected void fillCounters(final CommentItem item, final List<String> counters) {
    }

}
