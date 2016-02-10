/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

package org.bonitasoft.engine.api.impl.connector;

import java.util.List;

import org.bonitasoft.engine.bpm.connector.ConnectorState;
import org.bonitasoft.engine.bpm.connector.ConnectorStateReset;
import org.bonitasoft.engine.bpm.flownode.ActivityExecutionException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstanceWithFailureInfo;

/**
 * @author Elias Ricken de Medeiros
 */
public class ResetAllFailedConnectorStrategy implements ConnectorResetStrategy {

    private final ConnectorInstanceService connectorInstanceService;
    private final ConnectorReseter connectorReseter;
    private final int maxResults;

    public ResetAllFailedConnectorStrategy(ConnectorInstanceService connectorInstanceService, ConnectorReseter connectorReseter, int maxResults) {
        this.connectorInstanceService = connectorInstanceService;
        this.connectorReseter = connectorReseter;
        this.maxResults = maxResults;
    }

    /**
     * Reset all {@link org.bonitasoft.engine.core.process.instance.model.SConnectorInstance}s related to the given activity that are in the failed state
     * to the state {@link org.bonitasoft.engine.bpm.connector.ConnectorStateReset#TO_RE_EXECUTE}
     *
     * @param flowNodeInstanceId the identifier of the {@link org.bonitasoft.engine.core.process.instance.model.SActivityInstance} where the connectors are
     *        attached to.
     */
    @Override
    public void resetConnectorsOf(final long flowNodeInstanceId) throws ActivityExecutionException {
        try {
            int startIndex = 0;
            List<SConnectorInstanceWithFailureInfo> failedConnectors;
            do {
                failedConnectors = connectorInstanceService.getConnectorInstancesWithFailureInfo(flowNodeInstanceId, SConnectorInstance.FLOWNODE_TYPE,
                        ConnectorState.FAILED.name(), startIndex, maxResults);
                resetCurrentPage(failedConnectors);
                startIndex += maxResults;
            } while (failedConnectors.size() == maxResults);
        } catch (SBonitaException e) {
            throw new ActivityExecutionException(e);
        }
    }

    private void resetCurrentPage(final List<SConnectorInstanceWithFailureInfo> failedConnectors) throws ActivityExecutionException {
        for (SConnectorInstanceWithFailureInfo failedConnector : failedConnectors) {
            connectorReseter.resetState(failedConnector, ConnectorStateReset.TO_RE_EXECUTE);
        }
    }

}
