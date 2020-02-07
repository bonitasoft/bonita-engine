/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.WaitUntil;

/**
 * @author Elias Ricken de Medeiros
 */
public final class CheckNbAssignedTaskOf extends WaitUntil {

    private final int nbActivities;

    private final User user;

    private final ProcessAPI processAPI;

    private List<HumanTaskInstance> assignedHumanTaskInstances;

    public CheckNbAssignedTaskOf(final ProcessAPI processAPI, final int repeatEach, final int timeout,
            final boolean throwExceptions, final int nbActivities,
            final User user) {
        super(repeatEach, timeout, throwExceptions);
        this.nbActivities = nbActivities;
        this.user = user;
        this.processAPI = processAPI;
    }

    @Override
    protected boolean check() {
        assignedHumanTaskInstances = processAPI.getAssignedHumanTaskInstances(user.getId(), 0,
                Math.max(nbActivities, 20), ActivityInstanceCriterion.NAME_ASC);
        return assignedHumanTaskInstances.size() == nbActivities;
    }

    /**
     * @return the pendingHumanTaskInstances
     */
    public List<HumanTaskInstance> getAssingnedHumanTaskInstances() {
        return assignedHumanTaskInstances;
    }
}
