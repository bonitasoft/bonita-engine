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

import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.archive.SAHumanTaskInstance;

/**
 * @author Baptiste Mesta
 */
public abstract class SAHumanTaskInstanceImpl extends SAActivityInstanceImpl implements SAHumanTaskInstance {

    private static final long serialVersionUID = -2180651319743796704L;

    private long actorId;

    private long assigneeId;

    private long claimedDate;

    private long expectedEndDate;

    private STaskPriority priority;

    public SAHumanTaskInstanceImpl() {
    }

    public SAHumanTaskInstanceImpl(final SHumanTaskInstance sHumanTaskInstance) {
        super(sHumanTaskInstance);
        actorId = sHumanTaskInstance.getActorId();
        assigneeId = sHumanTaskInstance.getAssigneeId();
        priority = sHumanTaskInstance.getPriority();
        expectedEndDate = sHumanTaskInstance.getExpectedEndDate();
        claimedDate = sHumanTaskInstance.getClaimedDate();
    }

    @Override
    public long getActorId() {
        return actorId;
    }

    public void setActorId(final long actorId) {
        this.actorId = actorId;
    }

    @Override
    public long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(final long assigneeId) {
        this.assigneeId = assigneeId;
    }

    @Override
    public long getClaimedDate() {
        return claimedDate;
    }

    public void setClaimedDate(long claimedDate) {
        this.claimedDate = claimedDate;
    }

    @Override
    public STaskPriority getPriority() {
        return priority;
    }

    @Override
    public long getExpectedEndDate() {
        return expectedEndDate;
    }

    @Override
    public void setExpectedEndDate(final long expectedEndDate) {
        this.expectedEndDate = expectedEndDate;
    }

    public void setPriority(final STaskPriority priority) {
        this.priority = priority;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (actorId ^ actorId >>> 32);
        result = prime * result + (int) (assigneeId ^ assigneeId >>> 32);
        result = prime * result + (int) (expectedEndDate ^ expectedEndDate >>> 32);
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
        final SAHumanTaskInstanceImpl other = (SAHumanTaskInstanceImpl) obj;
        if (actorId != other.actorId) {
            return false;
        }
        if (assigneeId != other.assigneeId) {
            return false;
        }
        if (expectedEndDate != other.expectedEndDate) {
            return false;
        }
        if (priority == null) {
            if (other.priority != null) {
                return false;
            }
        } else if (!priority.equals(other.priority)) {
            return false;
        }
        return true;
    }

}
