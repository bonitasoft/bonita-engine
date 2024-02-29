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
package org.bonitasoft.engine.core.process.instance.model;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@IdClass(PersistentObjectId.class)
@MappedSuperclass
public abstract class SAbstractConnectorInstance implements PersistentObject {

    public static final String ID_KEY = "id";
    public static final String NAME_KEY = "name";
    public static final String CONTAINER_ID_KEY = "containerId";
    public static final String CONTAINER_TYPE_KEY = "containerType";
    public static final String CONNECTOR_ID_KEY = "connectorId";
    public static final String VERSION_KEY = "version";
    public static final String ACTIVATION_EVENT_KEY = "activationEvent";
    public static final String STATE_KEY = "state";
    public static final String EXECUTION_ORDER = "executionOrder";
    public static final String FLOWNODE_TYPE = "flowNode";
    public static final String PROCESS_TYPE = "process";

    @Id
    private long id;
    @Id
    private long tenantId;
    private String name;
    private long containerId;
    private String connectorId;
    private String version;
    @Enumerated(EnumType.STRING)
    private ConnectorEvent activationEvent;
    private String state;
    private String containerType;
    private int executionOrder;

    public SAbstractConnectorInstance(final String name, final long containerId, final String containerType,
            final String connectorId, final String version,
            final ConnectorEvent activationEvent) {

        this.name = name;
        this.containerId = containerId;
        this.containerType = containerType;
        this.connectorId = connectorId;
        this.version = version;
        this.activationEvent = activationEvent;
    }
}
