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

import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public abstract class ArchivedHumanTaskInstanceImpl extends ArchivedActivityInstanceImpl implements ArchivedHumanTaskInstance {

    private static final long serialVersionUID = -5566250139796838252L;

    private long actorId;

    private long assigneeId;

    private Date claimedDate;

    private TaskPriority priority;

    private Date expectedEndDate;

    public ArchivedHumanTaskInstanceImpl(final String name) {
        super(name);
    }

    @Override
    public long getActorId() {
        return actorId;
    }

    @Override
    public long getAssigneeId() {
        return assigneeId;
    }

    @Override
    public Date getClaimedDate() {
        return claimedDate;
    }

    public void setClaimedDate(Date claimedDate) {
        this.claimedDate = claimedDate;
    }

    @Override
    public Date getExpectedEndDate() {
        return expectedEndDate;
    }

    @Override
    public TaskPriority getPriority() {
        return priority;
    }

    public void setActorId(final long actorId) {
        this.actorId = actorId;
    }

    public void setAssigneeId(final long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public void setPriority(final TaskPriority priority) {
        this.priority = priority;
    }

    public void setExpectedEndDate(final Date expectedEndDate) {
        this.expectedEndDate = expectedEndDate;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("ArchivedHumanTaskInstanceImpl [actorId=");
        builder.append(actorId);
        builder.append(", assigneeId=");
        builder.append(assigneeId);
        builder.append(", priority=");
        builder.append(priority);
        builder.append(", expectedEndDate=");
        builder.append(expectedEndDate);
        builder.append(", getReachedStateDate()=");
        builder.append(getReachedStateDate());
        builder.append(", getLastUpdateDate()=");
        builder.append(getLastUpdateDate());
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
        builder.append(", getId()=");
        builder.append(getId());
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (actorId ^ actorId >>> 32);
        result = prime * result + (int) (assigneeId ^ assigneeId >>> 32);
        result = prime * result + (expectedEndDate == null ? 0 : expectedEndDate.hashCode());
        result = prime * result + (priority == null ? 0 : priority.hashCode());
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
        final ArchivedHumanTaskInstanceImpl other = (ArchivedHumanTaskInstanceImpl) obj;
        if (actorId != other.actorId) {
            return false;
        }
        if (assigneeId != other.assigneeId) {
            return false;
        }
        if (expectedEndDate == null) {
            if (other.expectedEndDate != null) {
                return false;
            }
        } else if (!expectedEndDate.equals(other.expectedEndDate)) {
            return false;
        }
        if (priority != other.priority) {
            return false;
        }
        return true;
    }

}
