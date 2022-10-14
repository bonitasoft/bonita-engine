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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedActivityItem;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedTaskItem;
import org.bonitasoft.web.rest.model.bpm.flownode.TaskItem;
import org.bonitasoft.web.rest.server.datastore.bpm.flownode.archive.ArchivedTaskDatastore;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TaskFinderTest {

    @Mock
    private TaskDatastore journal;

    @Mock
    private ArchivedTaskDatastore archives;

    private TaskFinder taskFinder;

    private final APIID id = APIID.makeAPIID(658L);

    @Before
    public void setUp() throws Exception {
        I18n.getInstance();
        ItemDefinitionFactory.setDefaultFactory(new ItemDefinitionFactory() {

            @Override
            public ItemDefinition<?> defineItemDefinitions(final String token) {
                return null;
            }
        });
        taskFinder = new TaskFinder(journal, archives);
    }

    @Test
    public void should_return_task_from_the_journal_when_it_belong_to_the_journal() throws Exception {
        final TaskItem task = new TaskItem();
        task.setId(id);
        when(journal.get(id)).thenReturn(task);

        final IItem item = taskFinder.find(id);

        assertThat(item.getId()).isEqualTo(task.getId());
    }

    @Test
    public void should_return_task_from_the_archives_when_not_found_in_the_journal() throws Exception {
        final ArchivedTaskItem task = new ArchivedTaskItem();
        task.setId(id);
        when(journal.get(id)).thenThrow(new APIItemNotFoundException("type", id));
        final Map<String, String> filters = new HashMap<>();
        filters.put(ArchivedActivityInstanceSearchDescriptor.SOURCE_OBJECT_ID, id.toString());
        final ItemSearchResult<ArchivedTaskItem> result = mock(ItemSearchResult.class);
        when(result.getResults()).thenReturn(Arrays.asList(task));
        when(archives.search(0, 1, null, ArchivedActivityItem.ATTRIBUTE_ARCHIVED_DATE + " "
                + Order.DESC, filters)).thenReturn(result);

        final IItem item = taskFinder.find(id);

        assertThat(item.getId()).isEqualTo(task.getId());
    }

    @Test(expected = APIItemNotFoundException.class)
    public void should_throw_an_exception_when_the_task_does_not_exist() throws Exception {
        when(journal.get(id)).thenThrow(new APIItemNotFoundException("type", id));
        final Map<String, String> filters = new HashMap<>();
        filters.put(ArchivedActivityInstanceSearchDescriptor.SOURCE_OBJECT_ID, id.toString());
        when(archives.search(0, 1, null, ArchivedActivityItem.ATTRIBUTE_ARCHIVED_DATE + " "
                + Order.DESC, filters)).thenThrow(new APIItemNotFoundException("type", id));

        taskFinder.find(id);
    }
}
