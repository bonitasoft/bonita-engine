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
package org.bonitasoft.engine.api.impl.transaction.activity;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.archive.SAActivityInstance;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class GetArchivedActivityInstance implements TransactionContentWithResult<SAActivityInstance> {

    private final ActivityInstanceService activityInstanceService;

    private final int stateId;

    private final long activityInstanceId;

    private SAActivityInstance activity;

    public GetArchivedActivityInstance(final ActivityInstanceService activityInstanceService, final long activityInstanceId) {
        this.activityInstanceService = activityInstanceService;
        this.activityInstanceId = activityInstanceId;
        stateId = -1;
    }

    public GetArchivedActivityInstance(final long activityInstanceId, final int stateId, final ActivityInstanceService activityInstanceService) {
        this.activityInstanceId = activityInstanceId;
        this.stateId = stateId;
        this.activityInstanceService = activityInstanceService;
    }

    @Override
    public void execute() throws SBonitaException {
        if (stateId > -1) {
            activity = activityInstanceService.getArchivedActivityInstance(activityInstanceId, stateId);
        } else {
            activity = activityInstanceService.getMostRecentArchivedActivityInstance(activityInstanceId);
        }
    }

    @Override
    public SAActivityInstance getResult() {
        return activity;
    }

}
