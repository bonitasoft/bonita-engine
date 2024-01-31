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

import static org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem.ATTRIBUTE_CONNECTOR_NAME;
import static org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem.ATTRIBUTE_CONNECTOR_VERSION;
import static org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem.ATTRIBUTE_PROCESS_ID;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.connector.ConnectorCriterion;
import org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyDefinition;
import org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ProcessConnectorDependencyDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ProcessDatastore;
import org.bonitasoft.web.rest.server.framework.api.APIHasSearch;
import org.bonitasoft.web.rest.server.framework.exception.APIFilterMandatoryException;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;

/**
 * @author Séverin Moussel
 */
public class APIProcessConnectorDependency extends ConsoleAPI<ProcessConnectorDependencyItem> implements
        APIHasSearch<ProcessConnectorDependencyItem> {

    @Override
    protected ProcessConnectorDependencyDefinition defineItemDefinition() {
        return ProcessConnectorDependencyDefinition.get();
    }

    @Override
    public String defineDefaultSearchOrder() {
        return ConnectorCriterion.DEFINITION_ID_ASC.name();
    }

    @Override
    public ItemSearchResult<ProcessConnectorDependencyItem> search(final int page, final int resultsByPage,
            final String search, final String orders,
            final Map<String, String> filters) {

        checkMandatoryAttributes(filters);
        return super.search(page, resultsByPage, search, orders, filters);
    }

    protected void checkMandatoryAttributes(final Map<String, String> filters) {
        if (MapUtil.isBlank(filters, ATTRIBUTE_PROCESS_ID)) {
            throw new APIFilterMandatoryException(ATTRIBUTE_PROCESS_ID);
        }
        if (MapUtil.isBlank(filters, ATTRIBUTE_CONNECTOR_NAME)) {
            throw new APIFilterMandatoryException(ATTRIBUTE_CONNECTOR_NAME);
        }
        if (MapUtil.isBlank(filters, ATTRIBUTE_CONNECTOR_VERSION)) {
            throw new APIFilterMandatoryException(ATTRIBUTE_CONNECTOR_VERSION);
        }
    }

    @Override
    protected ProcessConnectorDependencyDatastore defineDefaultDatastore() {
        return new ProcessConnectorDependencyDatastore(getEngineSession());
    }

    @Override
    protected void fillDeploys(final ProcessConnectorDependencyItem item, final List<String> deploys) {
        if (isDeployable(ATTRIBUTE_PROCESS_ID, deploys, item)) {
            item.setDeploy(ATTRIBUTE_PROCESS_ID, new ProcessDatastore(getEngineSession()).get(item.getProcessId()));
        }
    }

}
