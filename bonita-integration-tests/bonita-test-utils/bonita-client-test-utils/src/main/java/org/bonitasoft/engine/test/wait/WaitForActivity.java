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
import org.bonitasoft.engine.test.WaitUntil;

@Deprecated
public class WaitForActivity extends WaitUntil {

    private final String activityName;

    private final long processInstanceId;

    private String state = null;

    private ActivityInstance result;

    private final ProcessAPI processAPI;

    @Deprecated
    public WaitForActivity(final int repeatEach, final int timeout, final String activityName, final long processInstanceId, final ProcessAPI processAPI) {
        super(repeatEach, timeout);
        this.activityName = activityName;
        this.processInstanceId = processInstanceId;
        this.processAPI = processAPI;
    }

    @Deprecated
    public WaitForActivity(final int repeatEach, final int timeout, final String activityName, final long processInstanceId, final String state,
            final ProcessAPI processAPI) {
        super(repeatEach, timeout);
        this.activityName = activityName;
        this.processInstanceId = processInstanceId;
        this.state = state;
        this.processAPI = processAPI;
    }

    @Override
    protected boolean check() {
        final List<ActivityInstance> activityInstances = processAPI.getActivities(processInstanceId, 0, 10);
        final Iterator<ActivityInstance> iterator = activityInstances.iterator();
        boolean found = false;
        while (iterator.hasNext() && !found) {
            final ActivityInstance activityInstance = iterator.next();
            if (activityInstance.getName().equals(activityName)) {
                if (state == null || state.equals(activityInstance.getState())) {
                    result = activityInstance;
                    found = true;
                }
            }
        }
        return found;
    }

    public ActivityInstance getResult() {
        return result;
    }

}
