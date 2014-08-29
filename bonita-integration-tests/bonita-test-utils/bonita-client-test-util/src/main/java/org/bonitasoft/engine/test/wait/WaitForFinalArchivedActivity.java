/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.test.wait;

import java.util.Iterator;
import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.WaitUntil;

@Deprecated
public class WaitForFinalArchivedActivity extends WaitUntil {

    private final String activityName;

    private final long processInstanceId;

    private ArchivedActivityInstance result;

    private final ProcessAPI processAPI;

    @Deprecated
    public WaitForFinalArchivedActivity(final int repeatEach, final int timeout, final String activityName, final long processInstanceId,
            final ProcessAPI processAPI) {
        super(repeatEach, timeout);
        this.activityName = activityName;
        this.processInstanceId = processInstanceId;
        this.processAPI = processAPI;
    }

    @Override
    protected boolean check() {
        final List<ArchivedActivityInstance> activityInstances = processAPI.getArchivedActivityInstances(processInstanceId, 0, 100,
                ActivityInstanceCriterion.NAME_ASC);
        final Iterator<ArchivedActivityInstance> iterator = activityInstances.iterator();
        boolean found = false;
        while (iterator.hasNext() && !found) {
            final ArchivedActivityInstance activityInstance = iterator.next();
            if (activityInstance.getName().equals(activityName) && activityInstance.getState().equals(TestStates.getNormalFinalState())) {
                result = activityInstance;
                found = true;
            }
        }
        return found;
    }

    public ArchivedActivityInstance getResult() {
        return result;
    }

}
