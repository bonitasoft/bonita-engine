/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl.transaction.connector;

import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.bpm.connector.ConnectorStateReset;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.exception.SConnectorException;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;

/**
 * @author Emmanuel Duchastenier
 */
public class SetConnectorInstancesState implements TransactionContent {

    private final Map<Long, ConnectorStateReset> connectorsToReset;

    private final ConnectorInstanceService connectorInstanceService;

    public SetConnectorInstancesState(final Map<Long, ConnectorStateReset> connectorsToReset, final ConnectorInstanceService connectorInstanceService) {
        this.connectorsToReset = connectorsToReset;
        this.connectorInstanceService = connectorInstanceService;
    }

    @Override
    public void execute() throws SBonitaException {
        for (final Entry<Long, ConnectorStateReset> connEntry : connectorsToReset.entrySet()) {
            final Long connectorInstanceId = connEntry.getKey();
            final SConnectorInstance connectorInstance = connectorInstanceService.getConnectorInstance(connectorInstanceId);
            if (connectorInstance == null) {
                throw new SConnectorException("Connector instance not found with id " + connectorInstanceId);
            }
            final ConnectorStateReset state = connEntry.getValue();
            connectorInstanceService.setState(connectorInstance, state.name());
        }
    }

}
