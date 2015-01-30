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
package org.bonitasoft.engine.api.impl.transaction.flownode;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.STaskVisibilityException;

/**
 * @author Emmanuel Duchastenier
 */
public class HideTasks implements TransactionContent {

    private final ActivityInstanceService activityInstanceService;

    private final long userId;

    private final Long[] activityInstanceIds;

    public HideTasks(final ActivityInstanceService activityInstanceService, final long userId, final Long... activityInstanceIds) {
        this.activityInstanceService = activityInstanceService;
        this.userId = userId;
        this.activityInstanceIds = activityInstanceIds;

    }

    @Override
    public void execute() throws SBonitaException {
        // First, let's check if no task is already hidden:
        boolean found = false;
        long foundActivityInstanceId = 0;
        for (int i = 0; i < activityInstanceIds.length && !found; i++) {
            long activityInstanceId = activityInstanceIds[i];
            try {
                activityInstanceService.getHiddenTask(userId, activityInstanceId);
                found = true;
                foundActivityInstanceId = activityInstanceId;
            } catch (STaskVisibilityException vis) {
                // Ok, is not hidden already.
            }
        }
        if (found) {
            throw new STaskVisibilityException("Task with id " + foundActivityInstanceId + " is already hidden");
        }

        // If not, we can hide the tasks:
        activityInstanceService.hideTasks(userId, activityInstanceIds);
    }
}
