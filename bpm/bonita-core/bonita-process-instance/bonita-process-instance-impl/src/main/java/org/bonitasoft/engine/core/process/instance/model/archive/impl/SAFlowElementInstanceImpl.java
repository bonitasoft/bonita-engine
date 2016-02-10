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

import org.bonitasoft.engine.core.process.instance.model.SFlowElementInstance;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowElementInstance;

/**
 * @author Elias Ricken de Medeiros
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public abstract class SAFlowElementInstanceImpl extends SANamedElementImpl implements SAFlowElementInstance {

    private static final long serialVersionUID = 4941905230983394324L;

    private long rootContainerId;

    private long parentContainerId;

    private boolean aborting;

    private long logicalGroup1;

    private long logicalGroup2;

    private long logicalGroup3;

    private long logicalGroup4;

    public SAFlowElementInstanceImpl() {
        super();
    }

    public SAFlowElementInstanceImpl(final SFlowElementInstance flowElementInstance) {
        super(flowElementInstance.getName(), flowElementInstance.getId());
        rootContainerId = flowElementInstance.getRootContainerId();
        parentContainerId = flowElementInstance.getParentContainerId();
        logicalGroup1 = flowElementInstance.getLogicalGroup(0);
        logicalGroup2 = flowElementInstance.getLogicalGroup(1);
        logicalGroup3 = flowElementInstance.getLogicalGroup(2);
        logicalGroup4 = flowElementInstance.getLogicalGroup(3);
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
    public long getRootContainerId() {
        return rootContainerId;
    }

    public void setRootContainerId(final long rootContainerId) {
        this.rootContainerId = rootContainerId;
    }

    @Override
    public long getParentContainerId() {
        return parentContainerId;
    }

    public void setParentContainerId(final long parentContainerId) {
        this.parentContainerId = parentContainerId;
    }

    public void setLogicalGroup(final int index, final long id) {
        switch (index) {
            case 0:
                logicalGroup1 = id;
                break;
            case 1:
                logicalGroup2 = id;
                break;
            case 2:
                logicalGroup3 = id;
                break;
            case 3:
                logicalGroup4 = id;
                break;
            default:
                throw new IndexOutOfBoundsException("Index out of range for setLogicalGroup: " + index);
        }
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
                throw new IllegalArgumentException("Invalid index: must be 0, 1, 2 or 3");
        }
    }

    public boolean isAborting() {
        return aborting;
    }

    public void setAborting(final boolean aborting) {
        this.aborting = aborting;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (aborting ? 1231 : 1237);
        result = prime * result + (int) (logicalGroup1 ^ logicalGroup1 >>> 32);
        result = prime * result + (int) (logicalGroup2 ^ logicalGroup2 >>> 32);
        result = prime * result + (int) (logicalGroup3 ^ logicalGroup3 >>> 32);
        result = prime * result + (int) (logicalGroup4 ^ logicalGroup4 >>> 32);
        result = prime * result + (int) (parentContainerId ^ parentContainerId >>> 32);
        result = prime * result + (int) (rootContainerId ^ rootContainerId >>> 32);
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
        final SAFlowElementInstanceImpl other = (SAFlowElementInstanceImpl) obj;
        if (aborting != other.aborting) {
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
        return true;
    }

    @Override
    public String toString() {
        return "SAFlowElementInstanceImpl [rootContainerId=" + rootContainerId + ", parentContainerId=" + parentContainerId + ", aborting=" + aborting
                + ", logicalGroup1=" + logicalGroup1 + ", logicalGroup2=" + logicalGroup2 + ", logicalGroup3=" + logicalGroup3 + ", logicalGroup4="
                + logicalGroup4 + ", getName()=" + getName() + ", getId()=" + getId() + ", getSourceObjectId()=" + getSourceObjectId() + "]";
    }

}
