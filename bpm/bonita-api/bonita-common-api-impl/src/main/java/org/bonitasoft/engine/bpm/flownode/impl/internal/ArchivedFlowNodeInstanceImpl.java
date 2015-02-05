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
package org.bonitasoft.engine.bpm.flownode.impl.internal;

import java.util.Date;

import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.internal.NamedElementImpl;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
public abstract class ArchivedFlowNodeInstanceImpl extends NamedElementImpl implements ArchivedFlowNodeInstance {

    private static final long serialVersionUID = -6573747806944970703L;

    private long parentContainerId;

    private Date archiveDate;

    private String state;

    private long rootContainerId;

    private long processDefinitionId;

    private long processInstanceId;

    private long parentActivityInstanceId;

    private String displayName;

    private String displayDescription;

    private long sourceObjectId;

    private String description;

    private long executedBy;

    private long executedBySubstitute;

    private long flownodeDefinitionId;

    private boolean terminal;

    public ArchivedFlowNodeInstanceImpl(final String name) {
        super(name);
    }

    @Override
    public long getParentContainerId() {
        return parentContainerId;
    }

    public void setParentContainerId(long parentContainerId) {
        this.parentContainerId = parentContainerId;
    }

    @Override
    public Date getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(Date archiveDate) {
        this.archiveDate = archiveDate;
    }

    @Override
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public long getRootContainerId() {
        return rootContainerId;
    }

    public void setRootContainerId(long rootContainerId) {
        this.rootContainerId = rootContainerId;
    }

    @Override
    public long getProcessDefinitionId() {
        return processDefinitionId;
    }

    public void setProcessDefinitionId(long processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    @Override
    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public long getParentActivityInstanceId() {
        return parentActivityInstanceId;
    }

    public void setParentActivityInstanceId(long parentActivityInstanceId) {
        this.parentActivityInstanceId = parentActivityInstanceId;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getDisplayDescription() {
        return displayDescription;
    }

    public void setDisplayDescription(String displayDescription) {
        this.displayDescription = displayDescription;
    }

    @Override
    public long getSourceObjectId() {
        return sourceObjectId;
    }

    public void setSourceObjectId(long sourceObjectId) {
        this.sourceObjectId = sourceObjectId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public long getExecutedBy() {
        return executedBy;
    }

    public void setExecutedBy(long executedBy) {
        this.executedBy = executedBy;
    }

    @Override
    public long getExecutedBySubstitute() {
        return executedBySubstitute;
    }

    public void setExecutedBySubstitute(long executedBySubstitute) {
        this.executedBySubstitute = executedBySubstitute;
    }

    @Deprecated
    @Override
    public long getExecutedByDelegate() {
        return getExecutedBySubstitute();
    }

    @Deprecated
    public void setExecutedByDelegate(long executedBySubstitute) {
        setExecutedBySubstitute(executedBySubstitute);
    }

    @Override
    public long getFlownodeDefinitionId() {
        return flownodeDefinitionId;
    }

    public void setFlownodeDefinitionId(long flownodeDefinitionId) {
        this.flownodeDefinitionId = flownodeDefinitionId;
    }

    @Override
    public boolean isTerminal() {
        return terminal;
    }

    public void setTerminal(boolean terminal) {
        this.terminal = terminal;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (archiveDate == null ? 0 : archiveDate.hashCode());
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (displayDescription == null ? 0 : displayDescription.hashCode());
        result = prime * result + (displayName == null ? 0 : displayName.hashCode());
        result = prime * result + (int) (executedBy ^ executedBy >>> 32);
        result = prime * result + (int) (executedBySubstitute ^ executedBySubstitute >>> 32);
        result = prime * result + (int) (flownodeDefinitionId ^ flownodeDefinitionId >>> 32);
        result = prime * result + (int) (parentContainerId ^ parentContainerId >>> 32);
        result = prime * result + (int) (processDefinitionId ^ processDefinitionId >>> 32);
        result = prime * result + (int) (processInstanceId ^ processInstanceId >>> 32);
        result = prime * result + (int) (rootContainerId ^ rootContainerId >>> 32);
        result = prime * result + (int) (sourceObjectId ^ sourceObjectId >>> 32);
        result = prime * result + (state == null ? 0 : state.hashCode());
        result = prime * result + (terminal ? 1231 : 1237);
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
        final ArchivedFlowNodeInstanceImpl other = (ArchivedFlowNodeInstanceImpl) obj;
        if (archiveDate == null) {
            if (other.archiveDate != null) {
                return false;
            }
        } else if (!archiveDate.equals(other.archiveDate)) {
            return false;
        }
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
        if (executedBySubstitute != other.executedBySubstitute) {
            return false;
        }
        if (flownodeDefinitionId != other.flownodeDefinitionId) {
            return false;
        }
        if (parentContainerId != other.parentContainerId) {
            return false;
        }
        if (processDefinitionId != other.processDefinitionId) {
            return false;
        }
        if (processInstanceId != other.processInstanceId) {
            return false;
        }
        if (rootContainerId != other.rootContainerId) {
            return false;
        }
        if (sourceObjectId != other.sourceObjectId) {
            return false;
        }
        if (state == null) {
            if (other.state != null) {
                return false;
            }
        } else if (!state.equals(other.state)) {
            return false;
        }
        if (terminal != other.terminal) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ArchivedFlowNodeInstanceImpl [parentContainerId=" + parentContainerId + ", archiveDate=" + archiveDate + ", state=" + state
                + ", rootContainerId=" + rootContainerId + ", processDefinitionId=" + processDefinitionId + ", processInstanceId=" + processInstanceId
                + ", displayName=" + displayName + ", displayDescription=" + displayDescription + ", sourceObjectId=" + sourceObjectId + ", description="
                + description + ", executedBy=" + executedBy + ", flownodeDefinitionId=" + flownodeDefinitionId + ", terminal=" + terminal + ", getName()="
                + getName() + ", getId()=" + getId() + "]";
    }

}
