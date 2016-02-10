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

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.WaitUntil;

@Deprecated
public class WaitForArchivedActivity extends WaitUntil {

    private final long activityId;

    private ArchivedActivityInstance archivedActivityInstance;

    private final String stateName;

    private final ProcessAPI processAPI;

    public WaitForArchivedActivity(final int repeatEach, final int timeout, final long activityId, final TestStates state, final ProcessAPI processAPI) {
        super(repeatEach, timeout, false);
        this.activityId = activityId;
        this.stateName = state.getStateName();
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
