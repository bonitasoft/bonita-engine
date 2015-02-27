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
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.WaitUntil;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public final class CheckNbPendingTasksForUserUsingSearch extends WaitUntil {

    private final int nbTasks;

    private final long userId;

    private final ProcessAPI processAPI;

    private List<HumanTaskInstance> pendingHumanTasks;

    private final SearchOptions searchOptions;

    public CheckNbPendingTasksForUserUsingSearch(final ProcessAPI processAPI, final int repeatEach, final int timeout, final boolean throwExceptions,
            final int nbTasks, final long userId, final SearchOptions searchOptions) {
        super(repeatEach, timeout, throwExceptions);
        this.nbTasks = nbTasks;
        this.userId = userId;
        this.processAPI = processAPI;
        this.searchOptions = searchOptions;
    }

    @Override
    protected boolean check() throws Exception {
        final SearchResult<HumanTaskInstance> searchResult = processAPI.searchPendingTasksForUser(userId, searchOptions);
        pendingHumanTasks = searchResult.getResult();
        return searchResult.getCount() == nbTasks;
    }

    public List<HumanTaskInstance> getPendingHumanTasks() {
        return pendingHumanTasks;
    }

}
