/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package com.bonitasoft.engine.api.impl.transaction.task;

import java.util.Date;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SManualTaskInstance;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public final class CreateManualUserTask implements TransactionContentWithResult<SManualTaskInstance> {

    private final ActivityInstanceService activityInstanceService;

    private final String name;

    private final long userTaskId;

    private final long userId;

    private final String description;

    private final Date dueDate;

    private SManualTaskInstance createManualUserTask;

    private final String displayName;

    private final STaskPriority priority;

    private final long flowNodeDefinitionId;

    public CreateManualUserTask(final ActivityInstanceService activityInstanceService, final String name, final long flowNodeDefinitionId,
            final String displayName, final Long userTaskId, final Long userId, final String description, final Date dueDate, final STaskPriority priority) {
        this.activityInstanceService = activityInstanceService;
        this.name = name;
        this.displayName = displayName;
        this.userTaskId = userTaskId;
        this.userId = userId;
        this.description = description;
        this.dueDate = dueDate;
        this.priority = priority;
        this.flowNodeDefinitionId = flowNodeDefinitionId;
    }

    @Override
    public void execute() throws SBonitaException {
        long dueTime = 0L;
        if (dueDate != null) {
            dueTime = dueDate.getTime();
        }
        createManualUserTask = activityInstanceService.createManualUserTask(userTaskId, name, flowNodeDefinitionId, displayName, userId, description, dueTime,
                priority);
    }

    @Override
    public SManualTaskInstance getResult() {
        return createManualUserTask;
    }
}
