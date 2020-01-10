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
package org.bonitasoft.engine.search.activity;

import static org.mockito.Mockito.doReturn;

import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.FlowNodeType;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.descriptor.SearchActivityInstanceDescriptor;
import org.bonitasoft.engine.search.descriptor.SearchEntitiesDescriptor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Julien Mege
 */
@RunWith(MockitoJUnitRunner.class)
public class SearchActivityInstancesTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    TenantServiceAccessor tenantAccessor;

    @Mock
    SearchEntitiesDescriptor searchEntitiesDescriptor;

    @Mock
    ActivityInstanceService activityInstanceService;

    @Mock
    FlowNodeStateManager flowNodeStateManager;

    @Mock
    SearchActivityInstanceDescriptor searchActivityInstanceDescriptor;

    @Before
    public void setUp() throws Exception {
        doReturn(searchActivityInstanceDescriptor).when(searchEntitiesDescriptor).getSearchActivityInstanceDescriptor();

    }

    @Test
    public void execute_search_with_two_activityType_filter_should_throw_exception() throws Exception {
        //given
        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(ActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.HUMAN_TASK);
        searchOptionsBuilder.filter(ActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.AUTOMATIC_TASK);

        final SearchActivityInstances searchActivityInstancesTransaction = new SearchActivityInstances(
                activityInstanceService, flowNodeStateManager,
                searchEntitiesDescriptor.getSearchActivityInstanceDescriptor(), searchOptionsBuilder.done());

        //then
        expectedException.expect(SBonitaReadException.class);
        expectedException.expectMessage(
                "Invalid query, filtering several times on 'ActivityInstanceSearchDescriptor.ACTIVITY_TYPE' is not supported.");

        //when
        searchActivityInstancesTransaction.execute();

    }

    @Test
    public void execute_search_with_one_activityType_filter_should_not_throw_exception() throws Exception {
        //given
        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(ActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.HUMAN_TASK);

        final SearchActivityInstances searchActivityInstancesTransaction = new SearchActivityInstances(
                activityInstanceService, flowNodeStateManager,
                searchEntitiesDescriptor.getSearchActivityInstanceDescriptor(), searchOptionsBuilder.done());

        //when
        searchActivityInstancesTransaction.execute();

    }

}
