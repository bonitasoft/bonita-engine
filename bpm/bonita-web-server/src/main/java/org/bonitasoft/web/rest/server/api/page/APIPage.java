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
package org.bonitasoft.web.rest.server.api.page;

import java.util.List;
import java.util.Map;

import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.web.rest.model.portal.page.PageDefinition;
import org.bonitasoft.web.rest.model.portal.page.PageItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.api.deployer.DeployerFactory;
import org.bonitasoft.web.rest.server.datastore.page.PageDatastore;
import org.bonitasoft.web.rest.server.datastore.page.PageDatastoreFactory;
import org.bonitasoft.web.rest.server.framework.api.APIHasAdd;
import org.bonitasoft.web.rest.server.framework.api.APIHasDelete;
import org.bonitasoft.web.rest.server.framework.api.APIHasGet;
import org.bonitasoft.web.rest.server.framework.api.APIHasSearch;
import org.bonitasoft.web.rest.server.framework.api.APIHasUpdate;
import org.bonitasoft.web.rest.server.framework.api.Datastore;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Fabio Lombardi
 */
public class APIPage extends ConsoleAPI<PageItem>
        implements APIHasGet<PageItem>, APIHasSearch<PageItem>, APIHasAdd<PageItem>, APIHasUpdate<PageItem>,
        APIHasDelete {

    @Override
    protected ItemDefinition<PageItem> defineItemDefinition() {
        return PageDefinition.get();
    }

    @Override
    protected Datastore defineDefaultDatastore() {
        return getPageDatastore();
    }

    @Override
    public PageItem get(final APIID id) {
        return getPageDatastore().get(id);
    }

    /**
     * @deprecated as of 9.0.0, a page should be created at startup.
     */
    @Override
    @Deprecated(since = "9.0.0")
    public PageItem add(final PageItem item) {
        return getPageDatastore().add(item);
    }

    /**
     * @deprecated as of 9.0.0, a page should be updated at startup.
     */
    @Override
    @Deprecated(since = "9.0.0")
    public PageItem update(final APIID id, final Map<String, String> attributes) {
        return getPageDatastore().update(id, attributes);
    }

    @Override
    public void delete(final List<APIID> ids) {
        getPageDatastore().delete(ids);
    }

    @Override
    public ItemSearchResult<PageItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        return getPageDatastore().search(page, resultsByPage, search, orders, filters);
    }

    @Override
    protected void fillDeploys(final PageItem item, final List<String> deploys) {
        /*
         * Need to be done there and not in constructor
         * because need the engine session which is set
         * by setter instead of being injected in API constructor...
         */
        addDeployer(getDeployerFactory().createUserDeployer(PageItem.ATTRIBUTE_CREATED_BY_USER_ID));
        addDeployer(getDeployerFactory().createUserDeployer(PageItem.ATTRIBUTE_UPDATED_BY_USER_ID));
        super.fillDeploys(item, deploys);
    }

    protected DeployerFactory getDeployerFactory() {
        return new DeployerFactory(getEngineSession());
    }

    @Override
    public String defineDefaultSearchOrder() {
        return PageItem.ATTRIBUTE_URL_TOKEN;
    }

    private PageDatastore getPageDatastore() {
        PageAPI pageAPI;
        try {
            pageAPI = TenantAPIAccessor.getCustomPageAPI(getEngineSession());
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
        final WebBonitaConstantsUtils constants = WebBonitaConstantsUtils.getTenantInstance();
        final PageDatastoreFactory pageDatastoreFactory = new PageDatastoreFactory();
        return pageDatastoreFactory.create(getEngineSession(), constants, pageAPI);
    }

}
