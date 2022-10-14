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

import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCommentDefinition;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCommentItem;
import org.bonitasoft.web.rest.model.bpm.cases.CommentItem;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.datastore.bpm.cases.ArchivedCommentDatastore;
import org.bonitasoft.web.rest.server.datastore.organization.UserDatastore;
import org.bonitasoft.web.rest.server.framework.api.APIHasSearch;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Nicolas Tith
 */
public class APIArchivedComment extends ConsoleAPI<ArchivedCommentItem> implements APIHasSearch<ArchivedCommentItem> {

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CONFIGURE
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected ItemDefinition defineItemDefinition() {
        return Definitions.get(ArchivedCommentDefinition.TOKEN);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // C.R.U.D.S
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ItemSearchResult<ArchivedCommentItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {

        return getDatastore().search(page, resultsByPage, search, orders, filters);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public ArchivedCommentDatastore getDatastore() {
        return new ArchivedCommentDatastore(getEngineSession());
    }

    @Override
    public String defineDefaultSearchOrder() {
        return "";
    }

    @Override
    protected void fillDeploys(final ArchivedCommentItem item, final List<String> deploys) {
        if (isDeployable(ArchivedCommentItem.ATTRIBUTE_USER_ID, deploys, item)) {
            item.setDeploy(ArchivedCommentItem.ATTRIBUTE_USER_ID,
                    new UserDatastore(getEngineSession()).get(item.getUserId()));
        } else {
            item.setDeploy(CommentItem.ATTRIBUTE_USER_ID, getSystemUser());
        }

        // TODO: Deploy process instance
    }

    private UserItem getSystemUser() {
        final UserItem systemUser = new UserItem();
        systemUser.setUserName("System");
        systemUser.setIcon(UserItem.DEFAULT_USER_ICON);
        return systemUser;
    }

    @Override
    protected void fillCounters(final ArchivedCommentItem item, final List<String> counters) {
    }

}
