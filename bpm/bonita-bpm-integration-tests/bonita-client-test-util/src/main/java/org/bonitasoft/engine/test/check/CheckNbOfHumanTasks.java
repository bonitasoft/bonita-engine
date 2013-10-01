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
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.WaitUntil;

/**
 * @author Emmanuel Duchastenier
 */
@Deprecated
public final class CheckNbOfHumanTasks extends WaitUntil {

    private final ProcessAPI processAPI;

    private final long nbOfHumanTasks;

    private final SearchOptions searchOptions;

    private SearchResult<HumanTaskInstance> humanTaskInstances;

    @Deprecated
    public CheckNbOfHumanTasks(final int repeatEach, final int timeout, final boolean throwExceptions, final long nbOfHumanTasks,
            final SearchOptions searchOptions, final ProcessAPI processAPI) {
        super(repeatEach, timeout, throwExceptions);
        this.nbOfHumanTasks = nbOfHumanTasks;
        this.searchOptions = searchOptions;
        this.processAPI = processAPI;
    }

    @Override
    protected boolean check() throws Exception {
        humanTaskInstances = processAPI.searchHumanTaskInstances(searchOptions);
        return nbOfHumanTasks == humanTaskInstances.getCount();
    }

    public SearchResult<HumanTaskInstance> getHumanTaskInstances() {
        return humanTaskInstances;
    }
}
