/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.STaskPriority;

/**
 * @author Zhang Bole
 */
public final class SetTaskPriority implements TransactionContent {

    private final long activityInstanceId;

    private final STaskPriority priority;

    private final ActivityInstanceService activityInstanceService;

    public SetTaskPriority(final ActivityInstanceService activityInstanceService, final long activityInstanceId,
            final STaskPriority sTaskPriority) {
        this.activityInstanceService = activityInstanceService;
        this.activityInstanceId = activityInstanceId;
        priority = sTaskPriority;
    }

    @Override
    public void execute() throws SBonitaException {
        final SActivityInstance activity = activityInstanceService.getActivityInstance(activityInstanceId);
        activityInstanceService.setTaskPriority(activity, priority);
    }

}
