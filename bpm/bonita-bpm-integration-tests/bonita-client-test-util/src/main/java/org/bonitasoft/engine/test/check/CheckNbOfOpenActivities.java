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
package org.bonitasoft.engine.test.check;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.test.WaitUntil;

/**
 * @author Emmanuel Duchastenier
 */
@Deprecated
public final class CheckNbOfOpenActivities extends WaitUntil {

    private final ProcessAPI processAPI;

    private final ProcessInstance processInstance;

    private final int nbActivities;

    @Deprecated
    public CheckNbOfOpenActivities(final int repeatEach, final int timeout, final boolean throwExceptions, final ProcessInstance processInstance,
            final int nbActivities, final ProcessAPI processAPI) {
        super(repeatEach, timeout, throwExceptions);
        this.processInstance = processInstance;
        this.nbActivities = nbActivities;
        this.processAPI = processAPI;
    }

    @Override
    protected boolean check() throws Exception {
        final int activityNumber = processAPI.getNumberOfOpenedActivityInstances(processInstance.getId());
        // The number of activities must be the one expected:
        return activityNumber == nbActivities;
    }

    public int getNumberOfOpenActivities() {
        return nbActivities;
    }
}
