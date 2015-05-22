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
package org.bonitasoft.engine.core.process.instance.model.impl;

import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainer;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Feng Hui
 * @author Zhao Na
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public abstract class SFlowNodeInstanceImpl extends SFlowElementInstanceImpl implements SFlowNodeInstance, SFlowElementsContainer, PersistentObject {

    private static final long serialVersionUID = -699008235249779931L;

    private int stateId;

    private String stateName;

    private int previousStateId;

    private long reachedStateDate;

    private long lastUpdateDate;

    private String displayName;

    private String displayDescription;

    private int tokenCount = 0;

    private int loopCounter;

    private long executedBy;

    private long executedBySubstitute;

    private boolean stateExecuting;

    private long flowNodeDefinitionId;

    public SFlowNodeInstanceImpl() {
        super();
    }

    public SFlowNodeInstanceImpl(final String name, final long flowNodeDefinitionId, final long rootContainerId, final long parentContainerId,
            final long logicalGroup1, final long logicalGroup2) {
        super(name, rootContainerId, parentContainerId, logicalGroup1, logicalGroup2);
        this.flowNodeDefinitionId = flowNodeDefinitionId;
    }

    @Override
    public String getDiscriminator() {
        return SActivityInstance.class.getName();
    }

    @Override
    public SFlowElementsContainerType getContainerType() {
        return SFlowElementsContainerType.FLOWNODE;
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
    public int getPreviousStateId() {
        return previousStateId;
    }

    public void setPreviousStateId(int previousStateId) {
        this.previousStateId = previousStateId;
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
    public int getTokenCount() {
        return tokenCount;
    }

    public void setTokenCount(int tokenCount) {
        this.tokenCount = tokenCount;
    }

    @Override
    public int getLoopCounter() {
        return loopCounter;
    }

    public void setLoopCounter(int loopCounter) {
        this.loopCounter = loopCounter;
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
    public boolean isStateExecuting() {
        return stateExecuting;
    }

    public void setStateExecuting(boolean stateExecuting) {
        this.stateExecuting = stateExecuting;
    }

    @Override
    public long getFlowNodeDefinitionId() {
        return flowNodeDefinitionId;
    }

    public void setFlowNodeDefinitionId(long flowNodeDefinitionId) {
        this.flowNodeDefinitionId = flowNodeDefinitionId;
    }

    @Override
    public boolean mustExecuteOnAbortOrCancelProcess() {
        return isStable();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (displayDescription == null ? 0 : displayDescription.hashCode());
        result = prime * result + (displayName == null ? 0 : displayName.hashCode());
        result = prime * result + (int) (executedBy ^ executedBy >>> 32);
        result = prime * result + (int) (executedBySubstitute ^ executedBySubstitute >>> 32);
        result = prime * result + (int) (lastUpdateDate ^ lastUpdateDate >>> 32);
        result = prime * result + loopCounter;
        result = prime * result + previousStateId;
        result = prime * result + (int) (reachedStateDate ^ reachedStateDate >>> 32);
        result = prime * result + (stateExecuting ? 1231 : 1237);
        result = prime * result + stateId;
        result = prime * result + (stateName == null ? 0 : stateName.hashCode());
        result = prime * result + tokenCount;
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
        final SFlowNodeInstanceImpl other = (SFlowNodeInstanceImpl) obj;
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
        if (lastUpdateDate != other.lastUpdateDate) {
            return false;
        }
        if (loopCounter != other.loopCounter) {
            return false;
        }
        if (previousStateId != other.previousStateId) {
            return false;
        }
        if (reachedStateDate != other.reachedStateDate) {
            return false;
        }
        if (stateExecuting != other.stateExecuting) {
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
        if (tokenCount != other.tokenCount) {
            return false;
        }
        if (flowNodeDefinitionId != other.flowNodeDefinitionId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " [name=" + getName() + ", stateId=" + stateId + ", stateName=" + stateName + ", previousStateId=" + previousStateId
                + ", reachedStateDate=" + reachedStateDate + ", lastUpdateDate=" + lastUpdateDate + ", displayName=" + displayName + ", displayDescription="
                + displayDescription + ", tokenCount=" + tokenCount + ", loopCounter=" + loopCounter + ", executedBy=" + executedBy
                + ", stateExecuting=" + stateExecuting + ", flownodeDefinitionId=" + flowNodeDefinitionId + "]";
    }

}
