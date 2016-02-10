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
package org.bonitasoft.engine.test.wait;

import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.test.WaitUntil;

public class WaitForAssignedStep extends WaitUntil {

    private final ProcessAPI processAPI;

    private final String userTaskName;

    private final long processInstanceId;

    private final long userId;

    private HumanTaskInstance result;

    public WaitForAssignedStep(final ProcessAPI processAPI, final String userTaskName, final long processInstanceId, final long userId) {
        super(100, 1500);
        this.processAPI = processAPI;
        this.userTaskName = userTaskName;
        this.processInstanceId = processInstanceId;
        this.userId = userId;
    }

    @Override
    protected boolean check() {
        final List<HumanTaskInstance> taskInstances = processAPI.getAssignedHumanTaskInstances(userId, 0, 20, ActivityInstanceCriterion.DEFAULT);
        for (final HumanTaskInstance taskInstance : taskInstances) {
            if (taskInstance.getName().equals(userTaskName) && taskInstance.getParentProcessInstanceId() == processInstanceId) {
                result = taskInstance;
                return true;
            }
        }
        return false;
    }

    public HumanTaskInstance getResult() {
        return result;
    }

}
