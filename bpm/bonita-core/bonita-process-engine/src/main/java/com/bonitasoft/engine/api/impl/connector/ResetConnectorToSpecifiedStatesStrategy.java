/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.api.impl.connector;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.impl.connector.ConnectorResetStrategy;
import org.bonitasoft.engine.api.impl.connector.ConnectorReseter;
import org.bonitasoft.engine.bpm.connector.ConnectorState;
import org.bonitasoft.engine.bpm.connector.ConnectorStateReset;
import org.bonitasoft.engine.bpm.flownode.ActivityExecutionException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.exception.SConnectorInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstanceWithFailureInfo;

/**
 * @author Elias Ricken de Medeiros
 */
public class ResetConnectorToSpecifiedStatesStrategy implements ConnectorResetStrategy {

    private final ConnectorInstanceService connectorInstanceService;
    private final ConnectorReseter connectorReseter;
    private final Map<Long, ConnectorStateReset> connectorStates;

    public ResetConnectorToSpecifiedStatesStrategy(final ConnectorInstanceService connectorInstanceService, final ConnectorReseter connectorReseter,
            final Map<Long, ConnectorStateReset> connectorStates) {
        this.connectorInstanceService = connectorInstanceService;
        this.connectorReseter = connectorReseter;
        this.connectorStates = connectorStates;
    }

    /**
     * Reset the {@link org.bonitasoft.engine.core.process.instance.model.SConnectorInstance}s to the given states and verify that there are no more connectors
     * in failed state.
     *
     * @param flowNodeInstanceId the flow node instance identifier
     * @throws org.bonitasoft.engine.bpm.flownode.ActivityExecutionException
     */
    @Override
    public void resetConnectorsOf(final long flowNodeInstanceId) throws ActivityExecutionException {
        try {
            for (Map.Entry<Long, ConnectorStateReset> entry : connectorStates.entrySet()) {
                SConnectorInstanceWithFailureInfo connectorInstance = connectorInstanceService.getConnectorInstanceWithFailureInfo(entry.getKey());
                connectorReseter.resetState(connectorInstance, entry.getValue());
            }
            verifyNoMoreFailedConnectors(flowNodeInstanceId);
        } catch (SBonitaException e) {
            throw new ActivityExecutionException(e);
        }

    }

    private void verifyNoMoreFailedConnectors(final long flowNodeInstanceId) throws SConnectorInstanceReadException, ActivityExecutionException {
        List<SConnectorInstanceWithFailureInfo> failedConnectors = connectorInstanceService.getConnectorInstancesWithFailureInfo(flowNodeInstanceId,
                SConnectorInstanceWithFailureInfo.FLOWNODE_TYPE, ConnectorState.FAILED.name(), 0, 1);
        if (!failedConnectors.isEmpty()) {
            throw new ActivityExecutionException("The flow node instance with id '" + flowNodeInstanceId + "' has still failed connectors.");
        }
    }

}
