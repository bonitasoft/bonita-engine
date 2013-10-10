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
