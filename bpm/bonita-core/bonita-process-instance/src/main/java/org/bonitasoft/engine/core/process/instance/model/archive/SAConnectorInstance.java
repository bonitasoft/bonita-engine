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
package org.bonitasoft.engine.core.process.instance.model.archive;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.persistence.ArchivedPersistentObject;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;

@Data
@NoArgsConstructor
@IdClass(PersistentObjectId.class)
@Entity
@Table(name = "arch_connector_instance")
public class SAConnectorInstance implements ArchivedPersistentObject {

    private static final String FLOWNODE_TYPE = "flowNode";
    private static final String PROCESS_TYPE = "process";
    @Id
    private long id;
    @Id
    private long tenantId;
    private long archiveDate;
    private long sourceObjectId;
    private String name;
    private long containerId;
    private String connectorId;
    private String version;
    @Enumerated(EnumType.STRING)
    private ConnectorEvent activationEvent;
    private String state;
    private String containerType;

    public SAConnectorInstance(final SConnectorInstance connectorInstance) {
        sourceObjectId = connectorInstance.getId();
        name = connectorInstance.getName();
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

}
