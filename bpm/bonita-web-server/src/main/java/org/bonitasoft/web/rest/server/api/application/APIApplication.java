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
package org.bonitasoft.web.rest.server.api.application;

import java.util.List;
import java.util.Map;

import org.bonitasoft.web.rest.model.application.ApplicationDefinition;
import org.bonitasoft.web.rest.model.application.ApplicationItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.api.applicationpage.APIApplicationDataStoreFactory;
import org.bonitasoft.web.rest.server.api.deployer.DeployerFactory;
import org.bonitasoft.web.rest.server.api.deployer.PageDeployer;
import org.bonitasoft.web.rest.server.api.deployer.UserDeployer;
import org.bonitasoft.web.rest.server.datastore.application.ApplicationDataStoreCreator;
import org.bonitasoft.web.rest.server.datastore.organization.UserDatastore;
import org.bonitasoft.web.rest.server.framework.api.APIHasAdd;
import org.bonitasoft.web.rest.server.framework.api.APIHasDelete;
import org.bonitasoft.web.rest.server.framework.api.APIHasGet;
import org.bonitasoft.web.rest.server.framework.api.APIHasSearch;
import org.bonitasoft.web.rest.server.framework.api.APIHasUpdate;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Julien Mege
 */
public class APIApplication extends ConsoleAPI<ApplicationItem>
        implements APIHasAdd<ApplicationItem>, APIHasSearch<ApplicationItem>,
        APIHasGet<ApplicationItem>, APIHasUpdate<ApplicationItem>, APIHasDelete {

    private final ApplicationDataStoreCreator creator;

    private final APIApplicationDataStoreFactory applicationDataStoreFactory;

    public APIApplication(final ApplicationDataStoreCreator creator,
            final APIApplicationDataStoreFactory applicationDataStoreFactory) {
        this.creator = creator;
        this.applicationDataStoreFactory = applicationDataStoreFactory;
    }

    /**
     * @deprecated as of 9.0.0, Applications should be created at startup.
     */
    @Override
    @Deprecated(since = "9.0.0")
    public ApplicationItem add(final ApplicationItem item) {
        return creator.create(getEngineSession()).add(item);
    }

    /**
     * @deprecated as of 9.0.0, Applications should be updated at startup.
     */
    @Override
    @Deprecated(since = "9.0.0")
    public ApplicationItem update(final APIID id, final Map<String, String> attributes) {
        return creator.create(getEngineSession()).update(id, attributes);
    }

    @Override
    public ApplicationItem get(final APIID id) {
        return creator.create(getEngineSession()).get(id);
    }

    @Override
    public void delete(final List<APIID> ids) {
        creator.create(getEngineSession()).delete(ids);
    }

    @Override
    public ItemSearchResult<ApplicationItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        return creator.create(getEngineSession()).search(page, resultsByPage, search, orders, filters);
    }

    @Override
    public String defineDefaultSearchOrder() {
        return ApplicationItem.ATTRIBUTE_DISPLAY_NAME;
    }

    @Override
    protected ItemDefinition<ApplicationItem> defineItemDefinition() {
        return ApplicationDefinition.get();
    }

    @Override
    protected void fillDeploys(final ApplicationItem item, final List<String> deploys) {
        addDeployer(new UserDeployer(
                new UserDatastore(getEngineSession()), ApplicationItem.ATTRIBUTE_CREATED_BY));
        addDeployer(new UserDeployer(
                new UserDatastore(getEngineSession()), ApplicationItem.ATTRIBUTE_UPDATED_BY));
        addDeployer(getDeployerFactory().createProfileDeployer(ApplicationItem.ATTRIBUTE_PROFILE_ID));
        addDeployer(new PageDeployer(
                applicationDataStoreFactory.createPageDataStore(getEngineSession()),
                ApplicationItem.ATTRIBUTE_LAYOUT_ID));
        addDeployer(new PageDeployer(
                applicationDataStoreFactory.createPageDataStore(getEngineSession()),
                ApplicationItem.ATTRIBUTE_THEME_ID));

        super.fillDeploys(item, deploys);
    }

    protected DeployerFactory getDeployerFactory() {
        return new DeployerFactory(getEngineSession());
    }

}
