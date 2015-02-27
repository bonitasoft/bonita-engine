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
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.WaitUntil;

@Deprecated
public final class WaitProcessToFinishAndBeArchived extends WaitUntil {

    private final ProcessInstance processInstance;

    private final ProcessAPI processAPI;

    private final TestStates state;

    @Deprecated
    public WaitProcessToFinishAndBeArchived(final int repeatEach, final int timeout, final boolean throwExceptions, final ProcessInstance processInstance,
            final ProcessAPI processAPI, final TestStates state) {
        super(repeatEach, timeout, throwExceptions);
        this.processInstance = processInstance;
        this.processAPI = processAPI;
        this.state = state;
    }

    @Deprecated
    public WaitProcessToFinishAndBeArchived(final int repeatEach, final int timeout, final boolean throwExceptions, final ProcessInstance processInstance,
            final ProcessAPI processAPI) {
        this(repeatEach, timeout, throwExceptions, processInstance, processAPI, TestStates.NORMAL_FINAL);
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
