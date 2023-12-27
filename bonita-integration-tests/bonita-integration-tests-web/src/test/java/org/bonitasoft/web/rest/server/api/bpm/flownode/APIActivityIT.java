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
package org.bonitasoft.web.rest.server.api.bpm.flownode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.toolkit.client.data.APIID.makeAPIID;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.test.toolkit.bpm.TestHumanTask;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.bpm.flownode.ActivityItem;
import org.bonitasoft.web.rest.server.WaitUntil;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.junit.Test;

public class APIActivityIT extends AbstractConsoleTest {

    private static final String JSON_UPDATE_VARIABLES = "[" +
            "{\"name\": \"variable1\", \"value\": \"newValue\"}," +
            "{\"name\": \"variable2\", \"value\": 9}," +
            "{\"name\": \"variable3\", \"value\": 349246800000}" +
            "]";

    private APIActivity apiActivity;

    @Override
    public void consoleTestSetUp() throws Exception {
        apiActivity = new APIActivity();
        apiActivity.setCaller(getAPICaller(TestUserFactory.getJohnCarpenter().getSession(), "API/bpm/activity"));
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    @Test
    public void api_can_update_activity_variables() throws Exception {
        final TestHumanTask activity = TestProcessFactory.createActivityWithVariables(getInitiator());
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(ActivityItem.ATTRIBUTE_VARIABLES, JSON_UPDATE_VARIABLES);

        apiActivity.runUpdate(makeAPIID(activity.getId()), attributes);

        assertThat(activity.getDataInstance("variable1").getValue(), is((Serializable) "newValue"));
        assertThat(activity.getDataInstance("variable2").getValue(), is((Serializable) 9L));
        assertThat(activity.getDataInstance("variable3").getValue(), is((Serializable) new Date(349246800000L)));
    }

    @Test
    public void api_can_update_variables_and_terminate_activity() throws Exception {
        final TestHumanTask activity = TestProcessFactory.createActivityWithVariables(getInitiator());
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(ActivityItem.ATTRIBUTE_VARIABLES, JSON_UPDATE_VARIABLES);
        attributes.put(ActivityItem.ATTRIBUTE_STATE, ActivityItem.VALUE_STATE_COMPLETED);

        apiActivity.runUpdate(makeAPIID(activity.getId()), attributes);

        final ArchivedActivityInstance archivedActivityInstance = getArchivedDataInstance(activity);
        assertThat(archivedActivityInstance.getState(), is(ActivityItem.VALUE_STATE_COMPLETED));

        // Can't manage to do variable verification because of asynchronous engine update ...
        //        assertThat(getArchivedDataInstanceValue("variable1", archivedActivityInstance), is((Serializable) "newValue"));
        //        assertThat(getArchivedDataInstanceValue("variable2", archivedActivityInstance), is((Serializable) 9L));
        //        assertThat(getArchivedDataInstanceValue("variable3", archivedActivityInstance), is((Serializable) new Date(349246800000L)));
    }

    //    private Serializable getArchivedDataInstanceValue(String dataName, ArchivedActivityInstance archivedActivityInstance) throws Exception {
    //        return getProcessAPI().getArchivedActivityDataInstance(dataName, archivedActivityInstance.getSourceObjectId()).getValue();
    //    }

    /**
     * Activity state is updated asynchronously - need to wait... :-(
     */
    private ArchivedActivityInstance getArchivedDataInstance(final TestHumanTask activity) throws Exception {
        if (new WaitUntil(50, 3000) {

            @Override
            protected boolean check() throws Exception {
                try {
                    final ArchivedActivityInstance instance = getProcessAPI()
                            .getArchivedActivityInstance(activity.getId());
                    return ActivityItem.VALUE_STATE_COMPLETED.equals(instance.getState());
                } catch (final ActivityInstanceNotFoundException e) {
                    return false;
                }
            }
        }.waitUntil()) {
            return getProcessAPI().getArchivedActivityInstance(activity.getId());
        } else {
            throw new Exception("can't get archived task");
        }
    }

    private ProcessAPI getProcessAPI() throws Exception {
        return TenantAPIAccessor.getProcessAPI(getInitiator().getSession());
    }

    @Test
    public void api_can_search_with_default_search_order() throws Exception {
        //given
        TestProcessFactory.createActivityWithVariables(getInitiator());

        //when
        final ItemSearchResult<ActivityItem> searchResult = apiActivity.runSearch(0, 1, null,
                apiActivity.defineDefaultSearchOrder(), null, null, null);

        //then
        assertThat(searchResult.getResults()).as("should be able to search with default search order").isNotEmpty();
    }

}
