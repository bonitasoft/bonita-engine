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

import org.bonitasoft.engine.core.process.instance.model.SFlowElementInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 */
public abstract class SFlowElementInstanceImpl extends SNamedElementImpl implements SFlowElementInstance {

    private static final long serialVersionUID = 4771702652977437892L;

    private long rootContainerId;

    private long parentContainerId;

    private SStateCategory stateCategory = SStateCategory.NORMAL;

    private long logicalGroup1;

    private long logicalGroup2;

    private long logicalGroup3;

    private long logicalGroup4;

    private String description;

    private boolean terminal;

    private boolean stable;

    public SFlowElementInstanceImpl() {
    }

    public SFlowElementInstanceImpl(final String name) {
        super(name);
    }

    public SFlowElementInstanceImpl(final String name, final long rootContainerId, final long parentContainerId, final long logicalGroup1,
            final long logicalGroup2) {
        super(name);
        this.rootContainerId = rootContainerId;
        this.parentContainerId = parentContainerId;
        this.logicalGroup1 = logicalGroup1;
        this.logicalGroup2 = logicalGroup2;
    }

    @Override
    public long getParentContainerId() {
        return parentContainerId;
    }

    @Override
    public long getRootContainerId() {
        return rootContainerId;
    }

    @Override
    public long getProcessDefinitionId() {
        return logicalGroup1;
    }

    @Override
    public long getRootProcessInstanceId() {
        return logicalGroup2;
    }

    @Override
    public long getParentActivityInstanceId() {
        return logicalGroup3;
    }

    @Override
    public long getParentProcessInstanceId() {
        return logicalGroup4;
    }

    @Override
    public SFlowElementsContainerType getParentContainerType() {
        return getParentActivityInstanceId() <= 0 ? SFlowElementsContainerType.PROCESS : SFlowElementsContainerType.FLOWNODE;
    }

    @Override
    public long getLogicalGroup(final int index) {
        switch (index) {
            case 0:
                return logicalGroup1;
            case 1:
                return logicalGroup2;
            case 2:
                return logicalGroup3;
            case 3:
                return logicalGroup4;
            default:
                throw new IllegalArgumentException("Invalid index: the index must be 0, 1, 2 or 3");
        }
    }

    public void setParentContainerId(final long parentContainerId) {
        this.parentContainerId = parentContainerId;
    }

    public void setRootContainerId(final long rootContainerId) {
        this.rootContainerId = rootContainerId;
    }

    @Override
    public boolean isAborting() {
        return SStateCategory.ABORTING.equals(stateCategory);
    }

    @Override
    public boolean isCanceling() {
        return SStateCategory.CANCELLING.equals(stateCategory);
    }

    @Override
    public SStateCategory getStateCategory() {
        return stateCategory;
    }

    public void setStateCategory(final SStateCategory stateCategory) {
        this.stateCategory = stateCategory;
    }

    public void setLogicalGroup(final int index, final long value) {
        switch (index) {
            case 0:
                logicalGroup1 = value;
                break;
            case 1:
                logicalGroup2 = value;
                break;
            case 2:
                logicalGroup3 = value;
                break;
            case 3:
                logicalGroup4 = value;
                break;
            default:
                throw new IllegalArgumentException("Invalid index: the index must be 0, 1, 2 or 3");
        }
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public boolean isStable() {
        return stable;
    }

    public void setStable(final boolean stable) {
        this.stable = stable;
    }

    @Override
    public boolean isTerminal() {
        return terminal;
    }

    public void setTerminal(final boolean terminal) {
        this.terminal = terminal;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (int) (logicalGroup1 ^ logicalGroup1 >>> 32);
        result = prime * result + (int) (logicalGroup2 ^ logicalGroup2 >>> 32);
        result = prime * result + (int) (logicalGroup3 ^ logicalGroup3 >>> 32);
        result = prime * result + (int) (logicalGroup4 ^ logicalGroup4 >>> 32);
        result = prime * result + (int) (parentContainerId ^ parentContainerId >>> 32);
        result = prime * result + (int) (rootContainerId ^ rootContainerId >>> 32);
        result = prime * result + (stable ? 1231 : 1237);
        result = prime * result + (stateCategory == null ? 0 : stateCategory.hashCode());
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
        final SFlowElementInstanceImpl other = (SFlowElementInstanceImpl) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (logicalGroup1 != other.logicalGroup1) {
            return false;
        }
        if (logicalGroup2 != other.logicalGroup2) {
            return false;
        }
        if (logicalGroup3 != other.logicalGroup3) {
            return false;
        }
        if (logicalGroup4 != other.logicalGroup4) {
            return false;
        }
        if (parentContainerId != other.parentContainerId) {
            return false;
        }
        if (rootContainerId != other.rootContainerId) {
            return false;
        }
        if (stable != other.stable) {
            return false;
        }
        if (stateCategory != other.stateCategory) {
            return false;
        }
        if (terminal != other.terminal) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SFlowElementInstanceImpl [rootContainerId=" + rootContainerId + ", parentContainerId=" + parentContainerId + ", stateCategory=" + stateCategory
                + ", logicalGroup1=" + logicalGroup1 + ", logicalGroup2=" + logicalGroup2 + ", logicalGroup3=" + logicalGroup3 + ", logicalGroup4="
                + logicalGroup4 + ", description=" + description + ", terminal=" + terminal + ", stable=" + stable + "]";
    }

}
