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

import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public abstract class ArchivedActivityInstanceImpl extends ArchivedFlowNodeInstanceImpl implements ArchivedActivityInstance {

    private static final long serialVersionUID = 2457027970594869050L;

    private Date reachedStateDate;

    private Date lastUpdateDate;

    public ArchivedActivityInstanceImpl(final String name) {
        super(name);
    }

    @Override
    public Date getReachedStateDate() {
        return reachedStateDate;
    }

    public void setReachedStateDate(final Date reachedStateDate) {
        this.reachedStateDate = reachedStateDate;
    }

    @Override
    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(final Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("ArchivedActivityInstanceImpl [reacedStateDate=");
        builder.append(reachedStateDate);
        builder.append(", lastUpdateDate=");
        builder.append(lastUpdateDate);
        builder.append(", getId()=");
        builder.append(getId());
        builder.append(", getArchiveDate()=");
        builder.append(getArchiveDate());
        builder.append(", getState()=");
        builder.append(getState());
        builder.append(", getParentContainerId()=");
        builder.append(getParentContainerId());
        builder.append(", getRootContainerId()=");
        builder.append(getRootContainerId());
        builder.append(", getProcessDefinitionId()=");
        builder.append(getProcessDefinitionId());
        builder.append(", getProcessInstanceId()=");
        builder.append(getProcessInstanceId());
        builder.append(", getName()=");
        builder.append(getName());
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (lastUpdateDate == null ? 0 : lastUpdateDate.hashCode());
        result = prime * result + (reachedStateDate == null ? 0 : reachedStateDate.hashCode());
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
        final ArchivedActivityInstanceImpl other = (ArchivedActivityInstanceImpl) obj;
        if (lastUpdateDate == null) {
            if (other.lastUpdateDate != null) {
                return false;
            }
        } else if (!lastUpdateDate.equals(other.lastUpdateDate)) {
            return false;
        }
        if (reachedStateDate == null) {
            if (other.reachedStateDate != null) {
                return false;
            }
        } else if (!reachedStateDate.equals(other.reachedStateDate)) {
            return false;
        }
        return true;
    }

}
