/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.test.wait;

import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.WaitUntil;

@Deprecated
public final class WaitProcessToFinishAndBeArchived extends WaitUntil {

    private final ProcessInstance processInstance;

    private final ProcessAPI processAPI;

    private final String state;

    @Deprecated
    public WaitProcessToFinishAndBeArchived(final int repeatEach, final int timeout, final boolean throwExceptions, final ProcessInstance processInstance,
            final ProcessAPI processAPI, final String state) {
        super(repeatEach, timeout, throwExceptions);
        this.processInstance = processInstance;
        this.processAPI = processAPI;
        this.state = state;
    }

    @Deprecated
    public WaitProcessToFinishAndBeArchived(final int repeatEach, final int timeout, final boolean throwExceptions, final ProcessInstance processInstance,
            final ProcessAPI processAPI) {
        this(repeatEach, timeout, throwExceptions, processInstance, processAPI, TestStates.getNormalFinalState());
    }

    @Deprecated
    public WaitProcessToFinishAndBeArchived(final int repeatEach, final int timeout, final ProcessInstance processInstance, final ProcessAPI processAPI) {
        this(repeatEach, timeout, false, processInstance, processAPI);
    }

    @Override
    protected boolean check() {
        final List<ArchivedProcessInstance> archivedProcessInstances = processAPI.getArchivedProcessInstances(processInstance.getId(), 0, 200);
        return APITestUtil.containsState(archivedProcessInstances, state);
    }

}
