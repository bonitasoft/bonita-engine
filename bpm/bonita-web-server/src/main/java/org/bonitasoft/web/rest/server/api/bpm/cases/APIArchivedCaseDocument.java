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

import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCaseDocumentDefinition;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCaseDocumentItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.api.deployer.DeployerFactory;
import org.bonitasoft.web.rest.server.datastore.bpm.cases.ArchivedCaseDocumentDatastore;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Fabio Lombardi
 */
public class APIArchivedCaseDocument extends ConsoleAPI<ArchivedCaseDocumentItem> {

    @Override
    protected ItemDefinition defineItemDefinition() {
        return Definitions.get(ArchivedCaseDocumentDefinition.TOKEN);
    }

    @Override
    public ArchivedCaseDocumentItem get(final APIID id) {
        return getArchivedCaseDocumentDatastore().get(id);
    }

    @Override
    public String defineDefaultSearchOrder() {
        return "";
    }

    @Override
    protected void fillDeploys(final ArchivedCaseDocumentItem item, final List<String> deploys) {
        addDeployer(getDeployerFactory().createUserDeployer(ArchivedCaseDocumentItem.ATTRIBUTE_SUBMITTED_BY_USER_ID));
        addDeployer(getDeployerFactory().createUserDeployer(ArchivedCaseDocumentItem.ATTRIBUTE_AUTHOR));
        super.fillDeploys(item, deploys);
    }

    protected DeployerFactory getDeployerFactory() {
        return new DeployerFactory(getEngineSession());
    }

    @Override
    protected void fillCounters(final ArchivedCaseDocumentItem item, final List<String> counters) {
    }

    @Override
    public ItemSearchResult<ArchivedCaseDocumentItem> search(final int page, final int resultsByPage,
            final String search, final String orders,
            final Map<String, String> filters) {

        return getArchivedCaseDocumentDatastore().search(page, resultsByPage, search, filters, orders);
    }

    @Override
    public void delete(final List<APIID> ids) {
        getArchivedCaseDocumentDatastore().delete(ids);
    }

    protected ArchivedCaseDocumentDatastore getArchivedCaseDocumentDatastore() {
        ProcessAPI processAPI;
        try {
            processAPI = TenantAPIAccessor.getProcessAPI(getEngineSession());
        } catch (final Exception e) {
            throw new APIException(e);
        }
        final WebBonitaConstantsUtils constants = WebBonitaConstantsUtils.getTenantInstance();

        return new ArchivedCaseDocumentDatastore(getEngineSession(), processAPI);
    }
}
