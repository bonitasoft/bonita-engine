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

import java.util.Iterator;
import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.WaitUntil;

@Deprecated
public class WaitForStep extends WaitUntil {

    private final String stepName;

    private final long processInstanceId;

    private long activityInstanceId;

    private ActivityInstance result;

    private String state = null;

    private final ProcessAPI processAPI;

    @Deprecated
    public WaitForStep(final int repeatEach, final int timeout, final String stepName, final long processInstanceId, final ProcessAPI processAPI) {
        super(repeatEach, timeout);
        this.stepName = stepName;
        this.processInstanceId = processInstanceId;
        this.processAPI = processAPI;
    }

    @Deprecated
    public WaitForStep(final int repeatEach, final int timeout, final String stepName, final long processInstanceId, final TestStates state,
            final ProcessAPI processAPI) {
        super(repeatEach, timeout);
        this.stepName = stepName;
        this.processInstanceId = processInstanceId;
        this.state = state.getStateName();
        this.processAPI = processAPI;
    }

    @Override
    protected boolean check() {
        final List<ActivityInstance> openedActivityInstances = processAPI.getOpenActivityInstances(processInstanceId, 0, 10,
                ActivityInstanceCriterion.DEFAULT);
        final Iterator<ActivityInstance> iterator = openedActivityInstances.iterator();
        boolean found = false;
        while (iterator.hasNext() && !found) {
            final ActivityInstance activityInstance = iterator.next();
            if (activityInstance.getName().equals(stepName)) {
                if (state == null || state.equals(activityInstance.getState())) {
                    activityInstanceId = activityInstance.getId();
                    result = activityInstance;
                    found = true;
                }
            }
        }
        return found;
    }

    public long getStepId() {
        return activityInstanceId;
    }

    public ActivityInstance getResult() {
        return result;
    }
}
