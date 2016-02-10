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
package org.bonitasoft.engine.core.process.instance.model.impl;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;

/**
 * @author Baptiste Mesta
 */
public class SConnectorInstanceImpl extends SNamedElementImpl implements SConnectorInstance {

    private static final long serialVersionUID = 6815931350326178293L;

    private long containerId;

    private String connectorId;

    private String version;

    private ConnectorEvent activationEvent;

    private String state;

    private String containerType;

    private int executionOrder;

    public SConnectorInstanceImpl() {
        super();
    }

    public SConnectorInstanceImpl(final String name, final long containerId, final String containerType, final String connectorId, final String version,
            final ConnectorEvent activationEvent) {
        super(name);
        this.containerId = containerId;
        this.containerType = containerType;
        this.connectorId = connectorId;
        this.version = version;
        this.activationEvent = activationEvent;
    }

    @Override
    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(final String connectorId) {
        this.connectorId = connectorId;
    }

    @Override
    public ConnectorEvent getActivationEvent() {
        return activationEvent;
    }

    @Override
    public String getDiscriminator() {
        return this.getClass().getName();
    }

    @Override
    public String getState() {
        return state;
    }

    public void setActivationEvent(final ConnectorEvent activationEvent) {
        this.activationEvent = activationEvent;
    }

    public void setState(final String state) {
        this.state = state;
    }

    @Override
    public long getContainerId() {
        return containerId;
    }

    public void setContainerId(final long containerId) {
        this.containerId = containerId;
    }

    @Override
    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(final String containerType) {
        this.containerType = containerType;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    @Override
    public int getExecutionOrder() {
        return executionOrder;
    }

    public void setExecutionOrder(final int executionOrder) {
        this.executionOrder = executionOrder;
    }

    @Override
    public String toString() {
        return "SConnectorInstanceImpl#" + getId();
    }

}
