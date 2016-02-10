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
package org.bonitasoft.engine.bpm.connector.impl;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.ConnectorInstanceWithFailureInfo;
import org.bonitasoft.engine.bpm.connector.ConnectorState;


/**
 * @author Elias Ricken de Medeiros
 * 
 */
public class ConnectorInstanceWithFailureInfoImpl extends ConnectorInstanceImpl implements ConnectorInstanceWithFailureInfo {

    private static final long serialVersionUID = 7158777025106286625L;

    public ConnectorInstanceWithFailureInfoImpl(final String name, final long containerId, final String containerType, final String connectorId,
            String version, ConnectorState state,
            final ConnectorEvent activationEvent, final String exceptionMessage, final String stackTrace) {
        super(name, containerId, containerType, connectorId, version, state, activationEvent);
        this.exceptionMessage = exceptionMessage;
        this.stackTrace = stackTrace;
    }

    private final String exceptionMessage;

    private final String stackTrace;

    @Override
    public String getExceptionMessage() {
        return exceptionMessage;
    }

    @Override
    public String getStackTrace() {
        return stackTrace;
    }

}
