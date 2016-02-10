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
