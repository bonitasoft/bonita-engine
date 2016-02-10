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

import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public abstract class SHumanTaskInstanceImpl extends SActivityInstanceImpl implements SHumanTaskInstance {

    private static final long serialVersionUID = 6424178454905635309L;

    private long actorId;

    private long assigneeId;

    private long expectedEndDate;

    private STaskPriority priority;

    private long claimedDate;

    protected SHumanTaskInstanceImpl() {
        super();
    }

    public SHumanTaskInstanceImpl(final String name, final long flowNodeDefinitionId, final long rootContainerId, final long parenteContainerId,
            final long actorId, final STaskPriority priority, final long logicalGroup1, final long logicalGroup2) {
        super(name, flowNodeDefinitionId, rootContainerId, parenteContainerId, logicalGroup1, logicalGroup2);
        this.actorId = actorId;
        this.priority = priority;
    }

    public void setExpectedEndDate(final long expectedEndDate) {
        this.expectedEndDate = expectedEndDate;
    }

    public void setClaimedDate(final long claimedDate) {
        this.claimedDate = claimedDate;
    }

    public void setPriority(final STaskPriority priority) {
        this.priority = priority;
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
    public long getClaimedDate() {
        return claimedDate;
    }

    @Override
    public long getActorId() {
        return actorId;
    }

    @Override
    public long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(final long userId) {
        assigneeId = userId;
    }

}
