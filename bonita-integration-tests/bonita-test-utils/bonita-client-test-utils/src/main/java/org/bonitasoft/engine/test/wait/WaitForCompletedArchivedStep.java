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

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.WaitUntil;

/**
 * @author Baptiste Mesta
 */
@Deprecated
public final class WaitForCompletedArchivedStep extends WaitUntil {

    private final String value;

    private final long id;

    private final ProcessAPI processApi;

    private ArchivedHumanTaskInstance archivedHumanTaskInstance;

    public WaitForCompletedArchivedStep(final int repeatEach, final int timeout, final String stepName, final long processDefinitionId,
            final ProcessAPI processApi) {
        super(repeatEach, timeout);
        this.processApi = processApi;
        value = stepName;
        id = processDefinitionId;
    }

    @Override
    protected boolean check() throws Exception {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ArchivedHumanTaskInstanceSearchDescriptor.PROCESS_DEFINITION_ID, id);
        builder.filter(ArchivedHumanTaskInstanceSearchDescriptor.NAME, value);
        builder.filter(ArchivedHumanTaskInstanceSearchDescriptor.STATE_NAME, ActivityStates.COMPLETED_STATE);
        final SearchResult<ArchivedHumanTaskInstance> searchArchivedTasks = processApi.searchArchivedHumanTasks(builder.done());
        final boolean ok = searchArchivedTasks.getCount() >= 1;
        if (ok) {
            archivedHumanTaskInstance = searchArchivedTasks.getResult().get(0);
        }
        return ok;
    }

    public ArchivedHumanTaskInstance getArchivedTask() {
        return archivedHumanTaskInstance;
    }
}
