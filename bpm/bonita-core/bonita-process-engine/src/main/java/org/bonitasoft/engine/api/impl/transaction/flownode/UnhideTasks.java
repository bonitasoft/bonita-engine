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

/**
 * @author Emmanuel Duchastenier
 */
public class UnhideTasks implements TransactionContent {

    private final ActivityInstanceService activityInstanceService;

    private final long userId;

    private final Long[] activityInstanceId;

    public UnhideTasks(final ActivityInstanceService activityInstanceService, final long userId, final Long... activityInstanceId) {
        this.activityInstanceService = activityInstanceService;
        this.userId = userId;
        this.activityInstanceId = activityInstanceId;

    }

    @Override
    public void execute() throws SBonitaException {
        activityInstanceService.unhideTasks(userId, activityInstanceId);
    }

}
