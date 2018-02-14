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
package org.bonitasoft.engine.core.process.instance.model.archive.impl;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAConnectorInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Elias Ricken de Medeiros
 */
public class SAConnectorInstanceImpl extends SANamedElementImpl implements SAConnectorInstance {

    private static final long serialVersionUID = -151513267181136782L;

    private long containerId;

    private String connectorId;

    private String version;

    private ConnectorEvent activationEvent;

    private String state;

    private String containerType;

    public SAConnectorInstanceImpl() {
        super();
    }

    public SAConnectorInstanceImpl(final SConnectorInstance connectorInstance) {
        super(connectorInstance.getName(), connectorInstance.getId());
        containerId = connectorInstance.getContainerId();
        connectorId = connectorInstance.getConnectorId();
        version = connectorInstance.getVersion();
        activationEvent = connectorInstance.getActivationEvent();
        state = connectorInstance.getState();
        containerType = connectorInstance.getContainerType();
    }

    @Override
    public Class<? extends PersistentObject> getPersistentObjectInterface() {
        return SConnectorInstance.class;
    }

    @Override
    public String getDiscriminator() {
        return SAConnectorInstanceImpl.class.getName();
    }

    @Override
    public long getContainerId() {
        return containerId;
    }

    public void setContainerId(final long containerId) {
        this.containerId = containerId;
    }

    @Override
    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(final String connectorId) {
        this.connectorId = connectorId;
    }

    @Override
    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    @Override
    public ConnectorEvent getActivationEvent() {
        return activationEvent;
    }

    public void setActivationEvent(final ConnectorEvent activationEvent) {
        this.activationEvent = activationEvent;
    }

    @Override
    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    @Override
    public String getContainerType() {
        return containerType;
    }

    public void setContainerType(final String containerType) {
        this.containerType = containerType;
    }

}
