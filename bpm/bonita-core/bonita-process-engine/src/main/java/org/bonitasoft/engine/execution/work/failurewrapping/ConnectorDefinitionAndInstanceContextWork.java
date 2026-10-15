/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.execution.work.failurewrapping;

import java.util.Map;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.commons.exceptions.ExceptionContext;
import org.bonitasoft.engine.work.BonitaWork;

/**
 * Adding context information about Connector definition and instance to exception for better logging
 *
 * @author Celine Souchet
 */
public class ConnectorDefinitionAndInstanceContextWork extends TxInHandleFailureWrappingWork {

    private static final long serialVersionUID = 6958842321501639910L;

    private final String connectorDefinitionId;

    private final String connectorName;

    private final ConnectorEvent activationEvent;

    private final long connectorInstanceId;

    /**
     * @param wrappedWork
     *        The work to wrap
     * @param connectorDefinitionId
     *        The name of the connector definition
     * @param connectorName
     *        The name of the connector
     * @param connectorInstanceId
     *        The identifier of the connector instance
     * @param activationEvent
     *        The event to activate the connector
     */
    public ConnectorDefinitionAndInstanceContextWork(final BonitaWork wrappedWork,
            final String connectorDefinitionId,
            final String connectorName,
            long connectorInstanceId, final ConnectorEvent activationEvent) {
        super(wrappedWork);
        this.connectorDefinitionId = connectorDefinitionId;
        this.connectorName = connectorName;
        this.activationEvent = activationEvent;
        this.connectorInstanceId = connectorInstanceId;
    }

    @Override
    protected void setExceptionContext(final ExceptionContext e, final Map<String, Object> context) {
        e.setConnectorImplementationClassNameOnContext(connectorName);
        e.setConnectorNameOnContext(connectorName);
        e.setConnectorDefinitionIdOnContext(connectorDefinitionId);
        e.setConnectorInstanceIdOnContext(connectorInstanceId);
        if (activationEvent != null) {
            e.setConnectorActivationEventOnContext(activationEvent.name());
        }
    }
}
