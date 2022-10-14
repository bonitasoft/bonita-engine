/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.datastore.bpm.flownode;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.search.Order;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedActivityItem;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedTaskDefinition;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedTaskItem;
import org.bonitasoft.web.rest.model.bpm.flownode.TaskItem;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasGet;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasSearch;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;

/**
 * @author Vincent Elcrin
 */
public class TaskFinder {

    private final DatastoreHasGet<TaskItem> journal;

    private final DatastoreHasSearch<ArchivedTaskItem> archives;

    public TaskFinder(final DatastoreHasGet<TaskItem> journal, final DatastoreHasSearch<ArchivedTaskItem> archives) {
        this.journal = journal;
        this.archives = archives;
    }

    public IItem find(final APIID taskId) {
        try {
            return journal.get(taskId);
        } catch (final APIItemNotFoundException e) {
            final Map<String, String> filters = new HashMap<>();
            filters.put(ArchivedActivityItem.ATTRIBUTE_SOURCE_OBJECT_ID, taskId.toString());
            final ItemSearchResult<ArchivedTaskItem> result = archives.search(0, 1, null,
                    ArchivedActivityItem.ATTRIBUTE_ARCHIVED_DATE + " "
                            + Order.DESC,
                    filters);
            if (result.getResults().isEmpty()) {
                throw new APIItemNotFoundException(ArchivedTaskDefinition.TOKEN, taskId);
            }
            return result.getResults().get(0);
        }
    }
}
