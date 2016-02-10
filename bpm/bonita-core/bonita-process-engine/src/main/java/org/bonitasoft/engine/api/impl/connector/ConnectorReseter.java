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

import org.bonitasoft.engine.bpm.connector.ConnectorStateReset;
import org.bonitasoft.engine.bpm.flownode.ActivityExecutionException;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstanceWithFailureInfo;

/**
 * @author Elias Ricken de Medeiros
 */
public class ConnectorReseter {

    private ConnectorInstanceService connectorInstanceService;

    public ConnectorReseter(final ConnectorInstanceService connectorInstanceService) {
        this.connectorInstanceService = connectorInstanceService;
    }

    /**
     * Reset the {@link org.bonitasoft.engine.core.process.instance.model.SConnectorInstanceWithFailureInfo} to the given state
     * 
     * @param connectorInstanceWithFailure {@code SConnectorInstance} identifier
     * @param state the new {@code SConnectorInstance} state
     * @throws ActivityExecutionException
     */
    public void resetState(SConnectorInstanceWithFailureInfo connectorInstanceWithFailure, ConnectorStateReset state) throws ActivityExecutionException {
        try {
            // set state
            connectorInstanceService.setState(connectorInstanceWithFailure, state.name());
            // clean stack trace if necessary
            if (connectorInstanceWithFailure.getStackTrace() != null) {
                connectorInstanceService.setConnectorInstanceFailureException(connectorInstanceWithFailure, null);
            }
        } catch (SBonitaException e) {
            throw new ActivityExecutionException(e);
        }

    }

}
