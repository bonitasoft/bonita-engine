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

import org.bonitasoft.engine.bpm.model.ConnectorStateReset;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;

/**
 * @author Emmanuel Duchastenier
 */
public class SetConnectorInstancesState implements TransactionContent {

    private final Map<Long, ConnectorStateReset> connectorsToReset;

    private final ConnectorService connectorService;

    public SetConnectorInstancesState(final Map<Long, ConnectorStateReset> connectorsToReset, final ConnectorService connectorService) {
        this.connectorsToReset = connectorsToReset;
        this.connectorService = connectorService;
    }

    @Override
    public void execute() throws SBonitaException {
        for (Entry<Long, ConnectorStateReset> connEntry : connectorsToReset.entrySet()) {
            final SConnectorInstance connectorInstance = connectorService.getConnectorInstance(connEntry.getKey());
            final ConnectorStateReset state = connEntry.getValue();
            connectorService.setState(connectorInstance, state.name());
        }
    }

}
