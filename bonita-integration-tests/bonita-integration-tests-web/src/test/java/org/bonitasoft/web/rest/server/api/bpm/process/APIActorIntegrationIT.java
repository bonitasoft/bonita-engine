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
package org.bonitasoft.web.rest.server.api.bpm.process;

import static java.util.Arrays.asList;
import static org.bonitasoft.web.rest.model.bpm.process.ActorItem.ATTRIBUTE_DESCRIPTION;
import static org.bonitasoft.web.rest.model.bpm.process.ActorItem.ATTRIBUTE_PROCESS_ID;
import static org.bonitasoft.web.rest.model.bpm.process.ActorItem.COUNTER_GROUPS;
import static org.bonitasoft.web.rest.model.bpm.process.ActorItem.COUNTER_MEMBERSHIPS;
import static org.bonitasoft.web.rest.model.bpm.process.ActorItem.COUNTER_ROLES;
import static org.bonitasoft.web.rest.model.bpm.process.ActorItem.COUNTER_USERS;
import static org.bonitasoft.web.rest.model.builder.bpm.process.ActorItemBuilder.anActorItem;
import static org.bonitasoft.web.toolkit.client.data.APIID.makeAPIID;
import static org.bonitasoft.web.toolkit.client.data.item.template.ItemHasDualName.ATTRIBUTE_DISPLAY_NAME;
import static org.bonitasoft.web.toolkit.client.data.item.template.ItemHasDualName.ATTRIBUTE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.test.toolkit.bpm.TestProcess;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.bonitasoft.test.toolkit.bpm.process.TestActorMemberFactory;
import org.bonitasoft.test.toolkit.organization.TestGroupFactory;
import org.bonitasoft.test.toolkit.organization.TestRoleFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.bpm.process.ActorItem;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.junit.Test;

/**
 * @author Colin PUY
 */
public class APIActorIntegrationIT extends AbstractConsoleTest {

    private APIActor apiActor;

    @Override
    public void consoleTestSetUp() throws Exception {
        apiActor = new APIActor();
        apiActor.setCaller(getAPICaller(getInitiator().getSession(), "API/bpm/actor"));
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    private ActorItem getFromEngine(final long actorId) throws Exception {
        return anActorItem().fromActorInstance(getProcessAPI().getActor(actorId)).build();
    }

    private ProcessAPI getProcessAPI() throws Exception {
        return TenantAPIAccessor.getProcessAPI(getInitiator().getSession());
    }

    @Test
    public void testGet() throws Exception {
        final ActorInstance actor = TestProcessFactory.getDefaultHumanTaskProcess()
                .addActor(TestUserFactory.getJohnCarpenter()).getActors().get(0);

        final ActorItem fetchedActorItem = apiActor.runGet(makeAPIID(actor.getId()), null, null);

        assertItemEquals(getFromEngine(actor.getId()), fetchedActorItem);
    }

    @Test
    public void testGetWithDeploys() {
        final TestProcess process = TestProcessFactory.getDefaultHumanTaskProcess()
                .addActor(TestUserFactory.getJohnCarpenter());
        final long actorId = process.getActors().get(0).getId();

        final ActorItem fetchedActorItem = apiActor.runGet(makeAPIID(actorId), List.of(ATTRIBUTE_PROCESS_ID), null);

        assertNotNull(fetchedActorItem.getProcess());
        assertEquals(fetchedActorItem.getProcess().getId(), process.getId());
    }

    @Test
    public void testGetWithCounters() {
        //given
        final TestUser johnCarpenter = TestUserFactory.getJohnCarpenter();
        final TestProcess process = TestProcessFactory.getDefaultHumanTaskProcess().addActor(johnCarpenter);
        final long actorId = process.getActors().get(0).getId();

        //when
        final List<String> counters = asList(COUNTER_USERS, COUNTER_GROUPS, COUNTER_ROLES, COUNTER_MEMBERSHIPS);
        final ActorItem fetchedActorItem = apiActor.runGet(makeAPIID(actorId), null, counters);

        //then
        assertEquals(1L, (long) fetchedActorItem.getNbSelectedUsers());
        assertEquals(0L, (long) fetchedActorItem.getNbSelectedGroups());
        assertEquals(0L, (long) fetchedActorItem.getNbSelectedRoles());
        assertEquals(0L, (long) fetchedActorItem.getNbSelectedMembershipss());
    }

    @Test
    public void getCanCountNumberOfMembershipForActor() throws Exception {
        final ActorInstance actor = TestProcessFactory.getDefaultHumanTaskProcess()
                .addActor(TestUserFactory.getJohnCarpenter()).getActors().get(0);
        TestActorMemberFactory.createMembershipActorMember(actor.getId(), TestGroupFactory.getRAndD(),
                TestRoleFactory.getDeveloper());
        final List<String> counters = List.of(COUNTER_MEMBERSHIPS);

        final ActorItem fetchedActorItem = apiActor.runGet(makeAPIID(actor.getId()), null, counters);

        assertEquals(1L, (long) fetchedActorItem.getNbSelectedMembershipss());
    }

    @Test
    public void testUpdate() throws Exception {
        final TestProcess process = TestProcessFactory.getDefaultHumanTaskProcess()
                .addActor(TestUserFactory.getJohnCarpenter());
        final Map<String, String> attributes = buildUpdateAttributes("newDescription", "newDisplayName");
        final long actorId = process.getActors().get(0).getId();

        final ActorItem updatedItem = apiActor.runUpdate(makeAPIID(actorId), attributes);

        assertItemEquals(getFromEngine(actorId), updatedItem);
        assertEquals("newDisplayName", updatedItem.getDisplayName());
        assertEquals("newDescription", updatedItem.getDescription());
    }

    private Map<String, String> buildUpdateAttributes(final String description, final String displayName) {
        return Map.of(ATTRIBUTE_DESCRIPTION, description, ATTRIBUTE_DISPLAY_NAME, displayName);
    }

    @Test
    public void testSearchCanBePaginatedAndOrdered() {
        final TestProcess process = TestProcessFactory.createProcessWith3Actors()
                .addActor(TestUserFactory.getJohnCarpenter())
                .addActor(TestGroupFactory.getRAndD())
                .addActor(TestRoleFactory.getDeveloper());
        final HashMap<String, String> filters = new HashMap<>();
        filters.put(ATTRIBUTE_PROCESS_ID, String.valueOf(process.getId()));
        final String order = ATTRIBUTE_NAME + " ASC";

        final ItemSearchResult<ActorItem> searchResult = apiActor.search(0, 2, null, order, filters);

        assertEquals(3L, searchResult.getTotal());
        assertEquals(2, searchResult.getResults().size());
        final String result1Name = searchResult.getResults().get(0).getName();
        final String result2Name = searchResult.getResults().get(1).getName();
        assertTrue(isAscendantOrder(result1Name, result2Name));
    }

    private boolean isAscendantOrder(final String name1, final String name2) {
        return name1.compareTo(name2) < 0;
    }
}
