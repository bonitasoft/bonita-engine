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
package org.bonitasoft.engine.api.impl.transaction.task;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.execution.SUnreleasableTaskException;
import org.bonitasoft.engine.execution.StateBehaviors;

/**
 * @author Baptiste Mesta
 */
public final class AssignOrUnassignUserTask implements TransactionContent {

    private final long userId;

    private final long userTaskId;

    private final ActivityInstanceService activityInstanceService;

    private final StateBehaviors stateBehaviors;

    public AssignOrUnassignUserTask(final long userId, final long userTaskId, final ActivityInstanceService activityInstanceService,
            StateBehaviors stateBehaviors) {
        this.userId = userId;
        this.userTaskId = userTaskId;
        this.activityInstanceService = activityInstanceService;
        this.stateBehaviors = stateBehaviors;
    }

    @Override
    public void execute() throws SBonitaException {
        final SActivityInstance activityInstance = activityInstanceService.getActivityInstance(userTaskId);
        if (userId == 0 && SFlowNodeType.MANUAL_TASK.equals(activityInstance.getType())) {
            throw new SUnreleasableTaskException("The activity with id " + activityInstance.getId() + " can't be assigned because it is a manual sub task");
        }
        activityInstanceService.assignHumanTask(userTaskId, userId);
        if (userId > 0) {
            stateBehaviors.addAssignmentSystemComment(activityInstance, userId);
        }

    }
}
