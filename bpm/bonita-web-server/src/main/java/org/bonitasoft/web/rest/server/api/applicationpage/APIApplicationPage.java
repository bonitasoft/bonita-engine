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
package org.bonitasoft.web.rest.server.api.applicationpage;

import java.util.List;

import org.bonitasoft.web.rest.model.applicationpage.ApplicationPageDefinition;
import org.bonitasoft.web.rest.model.applicationpage.ApplicationPageItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.api.deployer.ApplicationDeployer;
import org.bonitasoft.web.rest.server.api.deployer.PageDeployer;
import org.bonitasoft.web.rest.server.framework.api.APIHasAdd;
import org.bonitasoft.web.rest.server.framework.api.APIHasDelete;
import org.bonitasoft.web.rest.server.framework.api.APIHasGet;
import org.bonitasoft.web.rest.server.framework.api.APIHasSearch;
import org.bonitasoft.web.rest.server.framework.api.Datastore;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Julien Mege
 */
public class APIApplicationPage extends ConsoleAPI<ApplicationPageItem> implements APIHasAdd<ApplicationPageItem>,
        APIHasSearch<ApplicationPageItem>, APIHasGet<ApplicationPageItem>, APIHasDelete {

    private final APIApplicationDataStoreFactory factory;

    public APIApplicationPage(final APIApplicationDataStoreFactory factory) {
        this.factory = factory;
    }

    /**
     * @deprecated as of 9.0.0, Application page should be created at startup.
     */
    @Override
    @Deprecated(since = "9.0.0")
    public ApplicationPageItem add(final ApplicationPageItem item) {
        return factory.createApplicationPageDataStore(getEngineSession()).add(item);
    }

    @Override
    protected ItemDefinition<ApplicationPageItem> defineItemDefinition() {
        return ApplicationPageDefinition.get();
    }

    @Override
    protected Datastore defineDefaultDatastore() {
        return factory.createApplicationPageDataStore(getEngineSession());
    }

    @Override
    public String defineDefaultSearchOrder() {
        return ApplicationPageItem.ATTRIBUTE_TOKEN;
    }

    @Override
    protected void fillDeploys(final ApplicationPageItem item, final List<String> deploys) {
        addDeployer(new PageDeployer(
                factory.createPageDataStore(getEngineSession()), ApplicationPageItem.ATTRIBUTE_PAGE_ID));
        addDeployer(new ApplicationDeployer(
                factory.createApplicationDataStore(getEngineSession()), ApplicationPageItem.ATTRIBUTE_APPLICATION_ID));
        super.fillDeploys(item, deploys);
    }

}
