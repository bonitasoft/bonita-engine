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

import static org.bonitasoft.web.rest.model.bpm.flownode.IHumanTaskItem.FILTER_SHOW_ASSIGNED_TO_OTHERS;
import static org.bonitasoft.web.rest.model.bpm.flownode.IHumanTaskItem.FILTER_USER_ID;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.session.impl.APISessionImpl;
import org.bonitasoft.web.rest.model.bpm.flownode.ActivityItem;
import org.bonitasoft.web.rest.model.bpm.flownode.HumanTaskItem;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractHumanTaskDatastoreTest {

    @Mock
    private ProcessAPI processAPI;
    private APISession session = new APISessionImpl(55L, new Date(), 5000, "john", 44L, "default", 1L);
    private AbstractHumanTaskDatastore<HumanTaskItem, HumanTaskInstance> datastore = new AbstractHumanTaskDatastore<>(
            session) {

        @Override
        protected ProcessAPI getProcessAPI() {
            return processAPI;
        }
    };

    @Test
    public void should_use_searchAssignedAndPendingHumanTasks_when_filtering_ready_state() throws SearchException {
        datastore.runSearch(new SearchOptionsBuilder(1, 100), Collections.singletonMap("state", "ready"));

        verify(processAPI).searchAssignedAndPendingHumanTasks(any());
    }

    @Test
    public void should_use_searchPendingOrAssignedToUserOrAssignedToOthersTasks_when_filtering_show_assigned_to_others_tasks()
            throws SearchException {
        HashMap<String, String> filters = new HashMap<>();
        filters.put(FILTER_USER_ID, "44");
        filters.put(FILTER_SHOW_ASSIGNED_TO_OTHERS, "true");
        datastore.runSearch(new SearchOptionsBuilder(1, 100), filters);

        verify(processAPI).searchPendingOrAssignedToUserOrAssignedToOthersTasks(eq(44L), any());
    }

    @Test
    public void should_use_searchHumanTaskInstances_when_there_is_no_particular_filter() throws SearchException {
        datastore.runSearch(new SearchOptionsBuilder(1, 100), Collections.singletonMap("tpye", "toto"));

        verify(processAPI).searchHumanTaskInstances(any());
    }

    @Test
    public void should_SearchOptionBuilderConvertSortParameter() throws SearchException {
        SearchOptionsBuilder builder = datastore.makeSearchOptionBuilder(0, 10, null,
                ActivityItem.ATTRIBUTE_ROOT_CASE_ID + " " + Order.DESC, new HashMap<>());

        Assert.assertEquals(builder.done().getSorts().size(), 1);
        Assert.assertEquals(builder.done().getSorts().get(0).getField(),
                ActivityInstanceSearchDescriptor.PROCESS_INSTANCE_ID);
        Assert.assertEquals(builder.done().getSorts().get(0).getOrder(), Order.DESC);
    }

}
