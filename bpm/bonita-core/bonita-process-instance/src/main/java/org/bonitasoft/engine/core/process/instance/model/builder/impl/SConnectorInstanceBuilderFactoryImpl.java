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
package org.bonitasoft.engine.core.process.instance.model.builder.impl;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.ConnectorState;
import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.impl.SConnectorInstanceImpl;

/**
 * @author Baptiste Mesta
 */
public class SConnectorInstanceBuilderFactoryImpl implements SConnectorInstanceBuilderFactory {

    private static final String ID_KEY = "id";

    private static final String NAME_KEY = "name";

    private static final String CONTAINER_ID_KEY = "containerId";

    private static final String CONTAINER_TYPE_KEY = "containerType";

    private static final String CONNECTOR_ID_KEY = "connectorId";

    private static final String VERSION_KEY = "version";

    private static final String ACTIVATION_EVENT_KEY = "activationEvent";

    private static final String STATE_KEY = "state";

    @Override
    public SConnectorInstanceBuilder createNewInstance(final String name, final long containerId, final String containerType, final String connectorId,
            final String version, final ConnectorEvent activationEvent, final int executionOrder) {
        final SConnectorInstanceImpl entity = new SConnectorInstanceImpl(name, containerId, containerType, connectorId, version, activationEvent);
        entity.setState(ConnectorState.TO_BE_EXECUTED.name());
        entity.setExecutionOrder(executionOrder);
        return new SConnectorInstanceBuilderImpl(entity);
    }

    @Override
    public String getIdKey() {
        return ID_KEY;
    }

    @Override
    public String getNameKey() {
        return NAME_KEY;
    }

    @Override
    public String getContainerIdKey() {
        return CONTAINER_ID_KEY;
    }

    @Override
    public String getContainerTypeKey() {
        return CONTAINER_TYPE_KEY;
    }

    @Override
    public String getConnectorIdKey() {
        return CONNECTOR_ID_KEY;
    }

    @Override
    public String getVersionKey() {
        return VERSION_KEY;
    }

    @Override
    public String getActivationEventKey() {
        return ACTIVATION_EVENT_KEY;
    }

    @Override
    public String getStateKey() {
        return STATE_KEY;
    }

    @Override
    public String getExecutionOrderKey() {
        return "executionOrder";
    }

}
