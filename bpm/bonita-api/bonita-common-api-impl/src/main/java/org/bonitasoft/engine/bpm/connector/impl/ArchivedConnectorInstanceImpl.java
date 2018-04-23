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

import java.util.Date;

import org.bonitasoft.engine.bpm.connector.ArchivedConnectorInstance;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.ConnectorState;
import org.bonitasoft.engine.bpm.internal.NamedElementImpl;

/**
 * @author Baptiste Mesta
 */
public class ArchivedConnectorInstanceImpl extends NamedElementImpl implements ArchivedConnectorInstance {

    private static final long serialVersionUID = 1740487116886845229L;

    private final Date archiveDate;

    private final long containerId;

    private final String containerType;

    private final String connectorId;

    private final String version;

    private final ConnectorEvent activationEvent;

    private final ConnectorState state;

    private final long sourceObjectId;

    public ArchivedConnectorInstanceImpl(final String name, final Date archiveDate, final long containerId, final String containerType,
            final String connectorId, final String version, final ConnectorEvent activationEvent, final ConnectorState state, final long sourceObjectId) {
        super(name);
        this.archiveDate = archiveDate;
        this.containerId = containerId;
        this.containerType = containerType;
        this.connectorId = connectorId;
        this.version = version;
        this.activationEvent = activationEvent;
        this.state = state;
        this.sourceObjectId = sourceObjectId;
    }

    /**
     * @return the archiveDate
     */
    @Override
    public Date getArchiveDate() {
        return archiveDate;
    }

    /**
     * @return the containerId
     */
    @Override
    public long getContainerId() {
        return containerId;
    }

    /**
     * @return the containerType
     */
    @Override
    public String getContainerType() {
        return containerType;
    }

    /**
     * @return the connectorId
     */
    @Override
    public String getConnectorId() {
        return connectorId;
    }

    /**
     * @return the version
     */
    @Override
    public String getVersion() {
        return version;
    }

    /**
     * @return the activationEvent
     */
    @Override
    public ConnectorEvent getActivationEvent() {
        return activationEvent;
    }

    /**
     * @return the state
     */
    @Override
    public ConnectorState getState() {
        return state;
    }

    @Override
    public long getSourceObjectId() {
        return sourceObjectId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (activationEvent == null ? 0 : activationEvent.hashCode());
        result = prime * result + (archiveDate == null ? 0 : archiveDate.hashCode());
        result = prime * result + (connectorId == null ? 0 : connectorId.hashCode());
        result = prime * result + (int) (containerId ^ containerId >>> 32);
        result = prime * result + (containerType == null ? 0 : containerType.hashCode());
        result = prime * result + (int) (sourceObjectId ^ sourceObjectId >>> 32);
        result = prime * result + (state == null ? 0 : state.hashCode());
        result = prime * result + (version == null ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ArchivedConnectorInstanceImpl other = (ArchivedConnectorInstanceImpl) obj;
        if (activationEvent != other.activationEvent) {
            return false;
        }
        if (archiveDate == null) {
            if (other.archiveDate != null) {
                return false;
            }
        } else if (!archiveDate.equals(other.archiveDate)) {
            return false;
        }
        if (connectorId == null) {
            if (other.connectorId != null) {
                return false;
            }
        } else if (!connectorId.equals(other.connectorId)) {
            return false;
        }
        if (containerId != other.containerId) {
            return false;
        }
        if (containerType == null) {
            if (other.containerType != null) {
                return false;
            }
        } else if (!containerType.equals(other.containerType)) {
            return false;
        }
        if (sourceObjectId != other.sourceObjectId) {
            return false;
        }
        if (state != other.state) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ArchivedConnectorInstance [archiveDate=" + archiveDate + ", containerId=" + containerId + ", containerType=" + containerType + ", connectorId="
                + connectorId + ", version=" + version + ", activationEvent=" + activationEvent + ", state=" + state + ", sourceObjectId=" + sourceObjectId
                + ", name=" + getName() + "]";
    }

}
