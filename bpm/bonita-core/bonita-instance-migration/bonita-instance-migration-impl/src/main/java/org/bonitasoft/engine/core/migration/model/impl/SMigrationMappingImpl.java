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
package org.bonitasoft.engine.core.migration.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.core.migration.model.SConnectorDefinitionWithEnablement;
import org.bonitasoft.engine.core.migration.model.SMigrationMapping;
import org.bonitasoft.engine.core.migration.model.SOperationWithEnablement;

/**
 * @author Baptiste Mesta
 */
public class SMigrationMappingImpl implements SMigrationMapping {

    private static final long serialVersionUID = 7882278173711512209L;

    private final String sourceName;

    private final String targetName;

    private final int sourceState;

    private final int targetState;

    private final List<SConnectorDefinitionWithEnablement> connectors;

    private final List<SOperationWithEnablement> operations;

    public SMigrationMappingImpl(final String sourceName, final String targetName, final int sourceState, final int targetState) {
        super();
        this.sourceName = sourceName;
        this.targetName = targetName;
        this.sourceState = sourceState;
        this.targetState = targetState;
        operations = new ArrayList<SOperationWithEnablement>();
        connectors = new ArrayList<SConnectorDefinitionWithEnablement>();
    }

    @Override
    public List<SConnectorDefinitionWithEnablement> getConnectors() {
        return connectors;
    }

    @Override
    public List<SOperationWithEnablement> getOperations() {
        return operations;
    }

    @Override
    public String getSourceName() {
        return sourceName;
    }

    @Override
    public String getTargetName() {
        return targetName;
    }

    @Override
    public int getSourceState() {
        return sourceState;
    }

    @Override
    public int getTargetState() {
        return targetState;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (connectors == null ? 0 : connectors.hashCode());
        result = prime * result + (operations == null ? 0 : operations.hashCode());
        result = prime * result + (sourceName == null ? 0 : sourceName.hashCode());
        result = prime * result + sourceState;
        result = prime * result + (targetName == null ? 0 : targetName.hashCode());
        result = prime * result + targetState;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final SMigrationMappingImpl other = (SMigrationMappingImpl) obj;
        if (connectors == null) {
            if (other.connectors != null) {
                return false;
            }
        } else if (!connectors.equals(other.connectors)) {
            return false;
        }
        if (operations == null) {
            if (other.operations != null) {
                return false;
            }
        } else if (!operations.equals(other.operations)) {
            return false;
        }
        if (sourceName == null) {
            if (other.sourceName != null) {
                return false;
            }
        } else if (!sourceName.equals(other.sourceName)) {
            return false;
        }
        if (sourceState != other.sourceState) {
            return false;
        }
        if (targetName == null) {
            if (other.targetName != null) {
                return false;
            }
        } else if (!targetName.equals(other.targetName)) {
            return false;
        }
        if (targetState != other.targetState) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MigrationMappingImpl [sourceName=" + sourceName + ", targetName=" + targetName + ", sourceState=" + sourceState + ", targetState="
                + targetState + ", connectors=" + connectors + ", operations=" + operations + "]";
    }

}
