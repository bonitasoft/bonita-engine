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
package org.bonitasoft.web.rest.server.datastore.bpm.process;

import static org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem.ATTRIBUTE_CONNECTOR_NAME;
import static org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem.ATTRIBUTE_CONNECTOR_VERSION;
import static org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem.ATTRIBUTE_PROCESS_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.console.common.server.utils.ListUtil;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.connector.ConnectorImplementationDescriptor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasSearch;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;

/**
 * @author Séverin Moussel
 */
public class ProcessConnectorDependencyDatastore
        extends CommonDatastore<ProcessConnectorDependencyItem, ConnectorImplementationDescriptor> implements
        DatastoreHasSearch<ProcessConnectorDependencyItem> {

    public ProcessConnectorDependencyDatastore(final APISession engineSession) {
        super(engineSession);
    }

    protected ProcessAPI getProcessAPI() {
        try {
            return TenantAPIAccessor.getProcessAPI(getEngineSession());
        } catch (BonitaException e) {
            throw new APIException(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ItemSearchResult<ProcessConnectorDependencyItem> search(final int page, final int resultsByPage,
            final String search, final String orders, final Map<String, String> filters) {

        try {
            final Long processId = MapUtil.getValueAsLong(filters, ATTRIBUTE_PROCESS_ID);
            final String connectorName = filters.get(ATTRIBUTE_CONNECTOR_NAME);
            final String connectorVersion = filters.get(ATTRIBUTE_CONNECTOR_VERSION);

            // Get connector definition
            final ConnectorImplementationDescriptor connectorImplementation = getProcessAPI()
                    .getConnectorImplementation(processId, connectorName, connectorVersion);

            // If connector definition doesn't exists returns an empty resultset
            if (connectorImplementation == null) {
                return new ItemSearchResult<>(page, 0, 0, new ArrayList<>());
            }

            // Get Jar from definition and Simulate pagination
            final List<String> jarDependencies = connectorImplementation.getJarDependencies();
            final List<String> dependencies = (List<String>) ListUtil.paginate(jarDependencies, page, resultsByPage);

            // Convert to consoleItem
            final List<ProcessConnectorDependencyItem> results = convertEngineToConsoleItems(processId, connectorName,
                    connectorVersion, dependencies);

            return new ItemSearchResult<>(page, results.size(), jarDependencies.size(), results);

        } catch (final BonitaException e) {
            throw new APIException(e);
        }

    }

    private List<ProcessConnectorDependencyItem> convertEngineToConsoleItems(final Long processId,
            final String connectorName, final String connectorVersion, final List<String> dependencies) {
        final List<ProcessConnectorDependencyItem> results = new ArrayList<>();

        for (final String filename : dependencies) {
            final ProcessConnectorDependencyItem dependencyItem = convertEngineToConsoleItem(processId, connectorName,
                    connectorVersion, filename);
            results.add(dependencyItem);
        }
        return results;
    }

    private ProcessConnectorDependencyItem convertEngineToConsoleItem(final Long processId, final String connectorName,
            final String connectorVersion, final String filename) {
        final ProcessConnectorDependencyItem dependencyItem = new ProcessConnectorDependencyItem();
        dependencyItem.setProcessId(processId);
        dependencyItem.setConnectorName(connectorName);
        dependencyItem.setConnectorVersion(connectorVersion);
        dependencyItem.setFilename(filename);
        return dependencyItem;
    }

    @Override
    protected ProcessConnectorDependencyItem convertEngineToConsoleItem(final ConnectorImplementationDescriptor item) {
        // Not used. Engine item is not fully populated
        return null;
    }

}
