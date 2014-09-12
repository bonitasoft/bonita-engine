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

import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.test.WaitUntil;

/**
 * @author Baptiste Mesta
 */
@Deprecated
public final class WaitForPendingTasks extends WaitUntil {

    private final int nbPendingTasks;

    private final long userId;

    private List<HumanTaskInstance> results;

    private final ProcessAPI processAPI;

    public WaitForPendingTasks(final int repeatEach, final int timeout, final int nbPendingTasks, final long userId, final ProcessAPI processAPI) {
        super(repeatEach, timeout);
        this.nbPendingTasks = nbPendingTasks;
        this.userId = userId;
        this.processAPI = processAPI;
    }

    @Override
    protected boolean check() {
        results = processAPI.getPendingHumanTaskInstances(userId, 0, nbPendingTasks, null);
        return results.size() == nbPendingTasks;
    }

    public List<HumanTaskInstance> getResults() {
        return results;
    }
}
