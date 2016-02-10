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

import java.util.Date;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;

/**
 * @author Baptiste Mesta
 */
public class SetExpectedEndDate implements TransactionContent {

    private final ActivityInstanceService activityInstanceService;

    private final long userTaskId;

    private final Date dueDate;

    public SetExpectedEndDate(final ActivityInstanceService activityInstanceService, final long userTaskId, final Date dueDate) {
        this.activityInstanceService = activityInstanceService;
        this.userTaskId = userTaskId;
        this.dueDate = dueDate;

    }

    @Override
    public void execute() throws SBonitaException {
        final SFlowNodeInstance flowNodeInstance = activityInstanceService.getFlowNodeInstance(userTaskId);
        activityInstanceService.setExpectedEndDate(flowNodeInstance, dueDate.getTime());
    }

}
