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
import org.bonitasoft.engine.core.migration.model.SMigrationPlan;
import org.bonitasoft.engine.core.migration.model.SOperationWithEnablement;

/**
 * @author Baptiste Mesta
 */
public class SMigrationPlanImpl implements SMigrationPlan {

    private static final long serialVersionUID = -2358593752459282824L;

    private final List<SMigrationMapping> mappings;

    private final List<SOperationWithEnablement> operations;

    private final List<SConnectorDefinitionWithEnablement> connectors;

    private final String targetVersion;

    private final String targetName;

    private final String sourceVersion;

    private final String sourceName;

    private final String description;

    public SMigrationPlanImpl(final String description, final String targetVersion, final String targetName, final String sourceVersion, final String sourceName) {
        super();
        this.description = description;
        this.targetVersion = targetVersion;
        this.targetName = targetName;
        this.sourceVersion = sourceVersion;
        this.sourceName = sourceName;
        operations = new ArrayList<SOperationWithEnablement>();
        mappings = new ArrayList<SMigrationMapping>();
        connectors = new ArrayList<SConnectorDefinitionWithEnablement>();
    }

    @Override
    public String getSourceName() {
        return sourceName;
    }

    @Override
    public String getSourceVersion() {
        return sourceVersion;
    }

    @Override
    public String getTargetName() {
        return targetName;
    }

    @Override
    public String getTargetVersion() {
        return targetVersion;
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
    public List<SMigrationMapping> getMappings() {
        return mappings;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (connectors == null ? 0 : connectors.hashCode());
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (mappings == null ? 0 : mappings.hashCode());
        result = prime * result + (operations == null ? 0 : operations.hashCode());
        result = prime * result + (sourceName == null ? 0 : sourceName.hashCode());
        result = prime * result + (sourceVersion == null ? 0 : sourceVersion.hashCode());
        result = prime * result + (targetName == null ? 0 : targetName.hashCode());
        result = prime * result + (targetVersion == null ? 0 : targetVersion.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SMigrationPlanImpl other = (SMigrationPlanImpl) obj;
        if (connectors == null) {
            if (other.connectors != null) {
                return false;
            }
        } else if (!connectors.equals(other.connectors)) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (mappings == null) {
            if (other.mappings != null) {
                return false;
            }
        } else if (!mappings.equals(other.mappings)) {
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
        if (sourceVersion == null) {
            if (other.sourceVersion != null) {
                return false;
            }
        } else if (!sourceVersion.equals(other.sourceVersion)) {
            return false;
        }
        if (targetName == null) {
            if (other.targetName != null) {
                return false;
            }
        } else if (!targetName.equals(other.targetName)) {
            return false;
        }
        if (targetVersion == null) {
            if (other.targetVersion != null) {
                return false;
            }
        } else if (!targetVersion.equals(other.targetVersion)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SMigrationPlanImpl [mappings=" + mappings + ", operations=" + operations + ", connectors=" + connectors + ", targetVersion=" + targetVersion
                + ", targetName=" + targetName + ", sourceVersion=" + sourceVersion + ", sourceName=" + sourceName + ", description=" + description + "]";
    }

}
