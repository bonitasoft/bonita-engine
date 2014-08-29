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

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.test.WaitUntil;

@Deprecated
public class WaitForArchivedActivity extends WaitUntil {

    private final long activityId;

    private ArchivedActivityInstance archivedActivityInstance;

    private final String stateName;

    private final ProcessAPI processAPI;

    public WaitForArchivedActivity(final int repeatEach, final int timeout, final long activityId, final String stateName, final ProcessAPI processAPI) {
        super(repeatEach, timeout, false);
        this.activityId = activityId;
        this.stateName = stateName;
        this.processAPI = processAPI;
    }

    @Override
    protected boolean check() throws BonitaException {
        archivedActivityInstance = processAPI.getArchivedActivityInstance(activityId);
        return stateName.equals(archivedActivityInstance.getState());
    }

    public ArchivedActivityInstance getArchivedActivityInstance() {
        return archivedActivityInstance;
    }

}
