/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
