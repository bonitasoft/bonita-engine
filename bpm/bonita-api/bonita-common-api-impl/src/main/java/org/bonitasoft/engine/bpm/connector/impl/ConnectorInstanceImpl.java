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
import org.bonitasoft.engine.bpm.connector.ConnectorInstance;
import org.bonitasoft.engine.bpm.connector.ConnectorState;
import org.bonitasoft.engine.bpm.internal.NamedElementImpl;

/**
 * @author Baptiste Mesta
 */
public class ConnectorInstanceImpl extends NamedElementImpl implements ConnectorInstance {

    private static final long serialVersionUID = 2148709030350403891L;

    private final long containerId;

    private final String containerType;

    private final String connectorId;

    private final String version;

    private final ConnectorState state;

    private final ConnectorEvent activationEvent;

    public ConnectorInstanceImpl(final String name, final long containerId, final String containerType, final String connectorId, final String version,
            final ConnectorState state, final ConnectorEvent activationEvent) {
        super(name);
        this.containerId = containerId;
        this.containerType = containerType;
        this.connectorId = connectorId;
        this.version = version;
        this.state = state;
        this.activationEvent = activationEvent;
    }

    @Override
    public long getContainerId() {
        return containerId;
    }

    @Override
    public String getContainerType() {
        return containerType;
    }

    @Override
    public String getConnectorId() {
        return connectorId;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public ConnectorEvent getActivationEvent() {
        return activationEvent;
    }

    @Override
    public ConnectorState getState() {
        return state;
    }

}
