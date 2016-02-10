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

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.test.WaitUntil;

/**
 * @author Baptiste Mesta
 */
@Deprecated
public final class CheckNbOfArchivedActivityInstances extends WaitUntil {

    private final ProcessAPI processAPI;

    private final ProcessInstance processInstance1;

    private final int expected;

    @Deprecated
    public CheckNbOfArchivedActivityInstances(final int repeatEach, final int timeout, final ProcessInstance processInstance, final int expected,
            final ProcessAPI processAPI) {
        super(repeatEach, timeout, false);
        this.processInstance1 = processInstance;
        this.expected = expected;
        this.processAPI = processAPI;
    }

    @Override
    protected boolean check() {
        return processAPI.getArchivedActivityInstances(processInstance1.getId(), 0, 100, ActivityInstanceCriterion.DEFAULT).size() == expected;
    }
}
