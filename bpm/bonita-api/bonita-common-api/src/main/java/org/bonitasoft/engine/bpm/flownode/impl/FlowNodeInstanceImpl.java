/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.bpm.flownode.impl;

import org.bonitasoft.engine.bpm.NamedElementImpl;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.StateCategory;

/**
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public abstract class FlowNodeInstanceImpl extends NamedElementImpl implements FlowNodeInstance {

    private static final long serialVersionUID = -6573747806944970703L;

    private long parentContainerId;

    private String state;

    private StateCategory stateCategory;

    private long rootContainerId;

    private long processDefinitionId;

    private long parentProcessInstanceId;

    private String displayDescription;

    private String displayName;

    private String description;

    private long executedBy;

    private long flownodeDefinitionId;

    public FlowNodeInstanceImpl(final String name, final long flownodeDefinitionId) {
        super(name);

        this.flownodeDefinitionId = flownodeDefinitionId;
    }

    @Override
    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    @Override
    public StateCategory getStateCategory() {
        return stateCategory;
    }

    public void setStateCategory(final StateCategory stateCategory) {
        this.stateCategory = stateCategory;
    }

    @Override
    public long getParentContainerId() {
        return parentContainerId;
    }

    public void setParentContainerId(final long parentContainerId) {
        this.parentContainerId = parentContainerId;
    }

    @Override
    public long getRootContainerId() {
        return rootContainerId;
    }

    public void setRootContainerId(final long rootContainerId) {
        this.rootContainerId = rootContainerId;
    }

    @Override
    public long getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(final long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public long getParentProcessInstanceId() {
        return parentProcessInstanceId;
    }

    public void setParentProcessInstanceId(final long parentProcessInstanceId) {
        this.parentProcessInstanceId = parentProcessInstanceId;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public void setDisplayDescription(final String displayDescription) {
        this.displayDescription = displayDescription;
    }

    @Override
    public String getDisplayDescription() {
        return displayDescription;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public long getExecutedBy() {
        return executedBy;
    }

    public void setExecutedBy(final long executedBy) {
        this.executedBy = executedBy;
    }

    @Override
    public long getFlownodeDefinitionId() {
        return flownodeDefinitionId;
    }

    public void setFlownodeDefinitionId(final long flownodeDefinitionId) {
        this.flownodeDefinitionId = flownodeDefinitionId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (displayDescription == null ? 0 : displayDescription.hashCode());
        result = prime * result + (displayName == null ? 0 : displayName.hashCode());
        result = prime * result + (int) (executedBy ^ executedBy >>> 32);
        result = prime * result + (int) (parentContainerId ^ parentContainerId >>> 32);
        result = prime * result + (int) (parentProcessInstanceId ^ parentProcessInstanceId >>> 32);
        result = prime * result + (int) (processDefinitionId ^ processDefinitionId >>> 32);
        result = prime * result + (int) (rootContainerId ^ rootContainerId >>> 32);
        result = prime * result + (state == null ? 0 : state.hashCode());
        result = prime * result + (stateCategory == null ? 0 : stateCategory.hashCode());
        result = prime * result + (int) (flownodeDefinitionId ^ flownodeDefinitionId >>> 32);
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
        final FlowNodeInstanceImpl other = (FlowNodeInstanceImpl) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (displayDescription == null) {
            if (other.displayDescription != null) {
                return false;
            }
        } else if (!displayDescription.equals(other.displayDescription)) {
            return false;
        }
        if (displayName == null) {
            if (other.displayName != null) {
                return false;
            }
        } else if (!displayName.equals(other.displayName)) {
            return false;
        }
        if (executedBy != other.executedBy) {
            return false;
        }
        if (parentContainerId != other.parentContainerId) {
            return false;
        }
        if (parentProcessInstanceId != other.parentProcessInstanceId) {
            return false;
        }
        if (processDefinitionId != other.processDefinitionId) {
            return false;
        }
        if (rootContainerId != other.rootContainerId) {
            return false;
        }
        if (state == null) {
            if (other.state != null) {
                return false;
            }
        } else if (!state.equals(other.state)) {
            return false;
        }
        if (stateCategory != other.stateCategory) {
            return false;
        }
        if (flownodeDefinitionId != other.flownodeDefinitionId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FlowNodeInstanceImpl [parentContainerId=" + parentContainerId + ", state=" + state + ", stateCategory=" + stateCategory + ", rootContainerId="
                + rootContainerId + ", processDefinitionId=" + processDefinitionId + ", parentProcessInstanceId=" + parentProcessInstanceId
                + ", displayDescription=" + displayDescription + ", displayName=" + displayName + ", description=" + description + ", executedBy=" + executedBy
                + ", flownodeDefinitionId=" + flownodeDefinitionId + "]";
    }

}
