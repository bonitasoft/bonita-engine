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

import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public abstract class HumanTaskInstanceImpl extends TaskInstanceImpl implements HumanTaskInstance {

    private static final long serialVersionUID = 5988594900787242204L;

    private final long actorId;

    private long assigneeId;

    private TaskPriority priority;

    private Date expectedEndDate;

    private Date claimedDate;

    public HumanTaskInstanceImpl(final String name, final long flownodeDefinitionId, final long actorId) {
        super(name, flownodeDefinitionId);
        this.actorId = actorId;
    }

    public void setAssigneeId(final long assigneeId) {
        this.assigneeId = assigneeId;
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
    public TaskPriority getPriority() {
        return priority;
    }

    @Override
    public Date getExpectedEndDate() {
        return expectedEndDate;
    }

    @Override
    public Date getClaimedDate() {
        return claimedDate;
    }

    public void setPriority(final TaskPriority priority) {
        this.priority = priority;
    }

    public void setExpectedEndDate(final Date expectedEndDate) {
        this.expectedEndDate = expectedEndDate;
    }

    public void setClaimedDate(final Date claimedDate) {
        this.claimedDate = claimedDate;
    }

}
