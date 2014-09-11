/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.core.process.instance.model.STaskPriority;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;

/**
 * @author Julien Reboul
 *
 */
public class UserTaskInstanceBuilder extends ActivityInstanceBuilder<SUserTaskInstanceImpl, UserTaskInstanceBuilder> {

    public static UserTaskInstanceBuilder aUserTask() {
        return new UserTaskInstanceBuilder();
    }

    private long assigneeId;

    private long actorId = 10;

    @Override
    UserTaskInstanceBuilder getThisBuilder() {
        return this;
    }

    public static UserTaskInstanceBuilder aCallActivityInstanceBuilder() {
        return new UserTaskInstanceBuilder();
    }

    @Override
    SUserTaskInstanceImpl _build() {
        final SUserTaskInstanceImpl userTaskInstanceImpl = new SUserTaskInstanceImpl(name, flowNodeDefinitionId, rootContainerId, parentContainerId, actorId,
                STaskPriority.NORMAL, logicalGroup1, logicalGroup2);
        userTaskInstanceImpl.setAssigneeId(assigneeId);
        return userTaskInstanceImpl;
    }

    public UserTaskInstanceBuilder withRootProcessInstanceId(final long rootProcessInstanceId) {
        this.logicalGroup2 = rootProcessInstanceId;
        return this;
    }

    public UserTaskInstanceBuilder withAssigneeId(final long assigneeId) {
        this.assigneeId = assigneeId;
        return this;
    }

    public UserTaskInstanceBuilder withActorId(final long actorId) {
        this.actorId = actorId;
        return this;
    }

}
