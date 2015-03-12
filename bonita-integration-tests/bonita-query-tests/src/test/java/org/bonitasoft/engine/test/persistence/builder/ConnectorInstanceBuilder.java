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
package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.core.process.instance.model.impl.SConnectorInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SConnectorInstanceWithFailureInfoImpl;

/**
 * @author Julien Reboul
 */
public class ConnectorInstanceBuilder extends PersistentObjectBuilder<SConnectorInstanceImpl, ConnectorInstanceBuilder> {

    private boolean withFailureInfo = false;

    private String state;

    private String connectorId;

    private ConnectorEvent activationEvent;

    private long containerId;

    private String containerType;

    private String version;

    private int executionOrder;

    private String exceptionMessage;

    private String stackTrace;

    private String name;

    private long tenantId;

    public static ConnectorInstanceBuilder aConnectorInstance() {
        return new ConnectorInstanceBuilder();
    }

    @Override
    ConnectorInstanceBuilder getThisBuilder() {
        return this;
    }


    @Override
    SConnectorInstanceImpl _build() {
        SConnectorInstanceImpl connectorInstance;
        if (withFailureInfo) {
            connectorInstance = new SConnectorInstanceWithFailureInfoImpl();
            ((SConnectorInstanceWithFailureInfoImpl) connectorInstance).setExceptionMessage(exceptionMessage);
            ((SConnectorInstanceWithFailureInfoImpl) connectorInstance).setStackTrace(stackTrace);
        }
        else {
            connectorInstance = new SConnectorInstanceImpl();
        }
        connectorInstance.setState(state);
        connectorInstance.setConnectorId(connectorId);
        connectorInstance.setActivationEvent(activationEvent);
        connectorInstance.setContainerId(containerId);
        connectorInstance.setContainerType(containerType);
        connectorInstance.setVersion(version);
        connectorInstance.setExecutionOrder(executionOrder);
        connectorInstance.setName(name);
        connectorInstance.setTenantId(tenantId);
        return connectorInstance;
    }

    public ConnectorInstanceBuilder setName(final String name) {
        this.name = name;
        return this;
    }

    public ConnectorInstanceBuilder setState(final String state) {
        this.state = state;
        return this;
    }

    public ConnectorInstanceBuilder withFailureInfo(final boolean withFailureInfo) {
        this.withFailureInfo = withFailureInfo;
        return this;
    }

    public ConnectorInstanceBuilder setConnectorId(final String connectorId) {
        this.connectorId = connectorId;
        return this;
    }

    public ConnectorInstanceBuilder setActivationEvent(final ConnectorEvent activationEvent) {
        this.activationEvent = activationEvent;
        return this;
    }

    public ConnectorInstanceBuilder setTenantId(final long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public ConnectorInstanceBuilder setContainerId(final long containerId) {
        this.containerId = containerId;
        return this;
    }

    public ConnectorInstanceBuilder setContainerType(final String containerType) {
        this.containerType = containerType;
        return this;
    }

    public ConnectorInstanceBuilder setVersion(final String version) {
        this.version = version;
        return this;
    }

    public ConnectorInstanceBuilder setExecutionOrder(final int executionOrder) {
        this.executionOrder = executionOrder;
        return this;
    }

    public ConnectorInstanceBuilder setExceptionMessage(String message) {
        this.exceptionMessage = message;
        return this;
    }

    public ConnectorInstanceBuilder setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
        return this;
    }
}
