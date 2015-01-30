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
package org.bonitasoft.engine.test.check;

import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.test.WaitUntil;

@Deprecated
public final class CheckNbOfProcessInstances extends WaitUntil {

    private final ProcessAPI processAPI;

    private final int nbOfProcInst;

    private List<ProcessInstance> result;

    private final ProcessInstanceCriterion orderBy;

    @Deprecated
    public CheckNbOfProcessInstances(final int repeatEach, final int timeout, final int nbOfProcInst, final ProcessInstanceCriterion orderBy,
            final ProcessAPI processAPI) {
        super(repeatEach, timeout);
        this.nbOfProcInst = nbOfProcInst;
        this.orderBy = orderBy;
        this.processAPI = processAPI;
    }

    @Deprecated
    public CheckNbOfProcessInstances(final int repeatEach, final int timeout, final int nbOfProcInst, final ProcessAPI processAPI) {
        this(repeatEach, timeout, nbOfProcInst, ProcessInstanceCriterion.NAME_ASC, processAPI);
    }

    @Override
    protected boolean check() {
        result = processAPI.getProcessInstances(0, nbOfProcInst + 1, orderBy);
        return nbOfProcInst == result.size();
    }

    public List<ProcessInstance> getResult() {
        return result;
    }
}
