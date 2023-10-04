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
package org.bonitasoft.web.rest.server.api.applicationmenu;

import java.util.List;
import java.util.Map;

import org.bonitasoft.web.rest.model.applicationmenu.ApplicationMenuDefinition;
import org.bonitasoft.web.rest.model.applicationmenu.ApplicationMenuItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.api.deployer.ApplicationPageDeployer;
import org.bonitasoft.web.rest.server.datastore.applicationmenu.ApplicationMenuDataStoreCreator;
import org.bonitasoft.web.rest.server.datastore.applicationpage.ApplicationPageDataStoreCreator;
import org.bonitasoft.web.rest.server.framework.api.APIHasAdd;
import org.bonitasoft.web.rest.server.framework.api.APIHasDelete;
import org.bonitasoft.web.rest.server.framework.api.APIHasGet;
import org.bonitasoft.web.rest.server.framework.api.APIHasSearch;
import org.bonitasoft.web.rest.server.framework.api.Datastore;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Julien Mege
 */
public class APIApplicationMenu extends ConsoleAPI<ApplicationMenuItem>
        implements APIHasAdd<ApplicationMenuItem>, APIHasSearch<ApplicationMenuItem>,
        APIHasGet<ApplicationMenuItem>, APIHasDelete {

    private final ApplicationMenuDataStoreCreator creator;

    public APIApplicationMenu(final ApplicationMenuDataStoreCreator creator) {
        this.creator = creator;
    }

    /**
     * @deprecated as of 9.0.0, Application menu should be created at startup.
     */
    @Override
    @Deprecated(since = "9.0.0")
    public ApplicationMenuItem add(final ApplicationMenuItem item) {
        return creator.create(getEngineSession()).add(item);
    }

    @Override
    public ApplicationMenuItem get(final APIID id) {
        return creator.create(getEngineSession()).get(id);
    }

    /**
     * @deprecated as of 9.0.0, Application menu should be updated at startup.
     */
    @Override
    @Deprecated(since = "9.0.0")
    public ApplicationMenuItem update(final APIID id, final Map<String, String> attributes) {
        return creator.create(getEngineSession()).update(id, attributes);
    }

    @Override
    public void delete(final List<APIID> ids) {
        creator.create(getEngineSession()).delete(ids);
    }

    @Override
    protected ItemDefinition<ApplicationMenuItem> defineItemDefinition() {
        return ApplicationMenuDefinition.get();
    }

    @Override
    protected Datastore defineDefaultDatastore() {
        return creator.create(getEngineSession());
    }

    @Override
    public String defineDefaultSearchOrder() {
        return ApplicationMenuItem.ATTRIBUTE_DISPLAY_NAME;
    }

    @Override
    protected void fillDeploys(final ApplicationMenuItem item, final List<String> deploys) {
        addDeployer(new ApplicationPageDeployer(
                new ApplicationPageDataStoreCreator().create(getEngineSession()),
                ApplicationMenuItem.ATTRIBUTE_APPLICATION_PAGE_ID));

        super.fillDeploys(item, deploys);
    }

}
