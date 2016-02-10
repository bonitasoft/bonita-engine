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

import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public abstract class SAFlowNodeInstanceImpl extends SAFlowElementInstanceImpl implements SAFlowNodeInstance {

    private static final long serialVersionUID = -6505193253859149099L;

    private int stateId;

    private String stateName;

    private boolean terminal;

    private boolean stable;

    private long reachedStateDate;

    private long lastUpdateDate;

    private long expectedEndDate;

    private String displayDescription;

    private String displayName;

    private String description;

    private long executedBy;

    private long executedBySubstitute;

    private String kind;

    private long flowNodeDefinitionId;

    public SAFlowNodeInstanceImpl() {
        super();
    }

    public SAFlowNodeInstanceImpl(final SFlowNodeInstance flowNodeInstance) {
        super(flowNodeInstance);
        stateId = flowNodeInstance.getStateId();
        stateName = flowNodeInstance.getStateName();
        reachedStateDate = flowNodeInstance.getReachedStateDate();
        lastUpdateDate = flowNodeInstance.getLastUpdateDate();
        terminal = flowNodeInstance.isTerminal();
        stable = flowNodeInstance.isStable();
        displayName = flowNodeInstance.getDisplayName();
        displayDescription = flowNodeInstance.getDisplayDescription();
        description = flowNodeInstance.getDescription();
        executedBy = flowNodeInstance.getExecutedBy();
        executedBySubstitute = flowNodeInstance.getExecutedBySubstitute();
        flowNodeDefinitionId = flowNodeInstance.getFlowNodeDefinitionId();
    }

    @Override
    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    @Override
    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    @Override
    public boolean isTerminal() {
        return terminal;
    }

    public void setTerminal(boolean terminal) {
        this.terminal = terminal;
    }

    public boolean isStable() {
        return stable;
    }

    public void setStable(boolean stable) {
        this.stable = stable;
    }

    @Override
    public long getReachedStateDate() {
        return reachedStateDate;
    }

    public void setReachedStateDate(long reachedStateDate) {
        this.reachedStateDate = reachedStateDate;
    }

    @Override
    public long getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(long lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public long getExpectedEndDate() {
        return expectedEndDate;
    }

    public void setExpectedEndDate(long expectedEndDate) {
        this.expectedEndDate = expectedEndDate;
    }

    @Override
    public String getDisplayDescription() {
        return displayDescription;
    }

    public void setDisplayDescription(String displayDescription) {
        this.displayDescription = displayDescription;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    @Override
    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    @Override
    public long getFlowNodeDefinitionId() {
        return flowNodeDefinitionId;
    }

    public void setFlowNodeDefinitionId(long flownodeDefinitionId) {
        this.flowNodeDefinitionId = flownodeDefinitionId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (displayDescription == null ? 0 : displayDescription.hashCode());
        result = prime * result + (displayName == null ? 0 : displayName.hashCode());
        result = prime * result + (int) (executedBy ^ executedBy >>> 32);
        result = prime * result + (int) (executedBySubstitute ^ executedBySubstitute >>> 32);
        result = prime * result + (int) (expectedEndDate ^ expectedEndDate >>> 32);
        result = prime * result + (kind == null ? 0 : kind.hashCode());
        result = prime * result + (int) (lastUpdateDate ^ lastUpdateDate >>> 32);
        result = prime * result + (int) (reachedStateDate ^ reachedStateDate >>> 32);
        result = prime * result + (stable ? 1231 : 1237);
        result = prime * result + stateId;
        result = prime * result + (stateName == null ? 0 : stateName.hashCode());
        result = prime * result + (terminal ? 1231 : 1237);
        result = prime * result + (int) (flowNodeDefinitionId ^ flowNodeDefinitionId >>> 32);
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
        final SAFlowNodeInstanceImpl other = (SAFlowNodeInstanceImpl) obj;
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
        if (expectedEndDate != other.expectedEndDate) {
            return false;
        }
        if (kind == null) {
            if (other.kind != null) {
                return false;
            }
        } else if (!kind.equals(other.kind)) {
            return false;
        }
        if (lastUpdateDate != other.lastUpdateDate) {
            return false;
        }
        if (reachedStateDate != other.reachedStateDate) {
            return false;
        }
        if (stable != other.stable) {
            return false;
        }
        if (stateId != other.stateId) {
            return false;
        }
        if (stateName == null) {
            if (other.stateName != null) {
                return false;
            }
        } else if (!stateName.equals(other.stateName)) {
            return false;
        }
        if (terminal != other.terminal) {
            return false;
        }
        if (flowNodeDefinitionId != other.flowNodeDefinitionId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SAFlowNodeInstanceImpl [stateId=" + stateId + ", stateName=" + stateName + ", terminal=" + terminal + ", stable=" + stable
                + ", reachedStateDate=" + reachedStateDate + ", lastUpdateDate=" + lastUpdateDate + ", expectedEndDate=" + expectedEndDate
                + ", displayDescription=" + displayDescription + ", displayName=" + displayName + ", description=" + description + ", executedBy=" + executedBy
                + ", kind=" + kind + ", flownodeDefinitionId=" + flowNodeDefinitionId + "]";
    }

}
