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
import static java.util.Collections.singletonList;
import static org.bonitasoft.web.rest.model.bpm.process.ActorMemberItem.ATTRIBUTE_ACTOR_ID;
import static org.bonitasoft.web.rest.model.builder.bpm.process.ActorMemberItemBuilder.anActorMemberItem;
import static org.bonitasoft.web.rest.model.identity.MemberType.GROUP;
import static org.bonitasoft.web.rest.model.portal.profile.AbstractMemberItem.FILTER_MEMBER_TYPE;
import static org.bonitasoft.web.toolkit.client.data.APIID.makeAPIID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.actor.ActorMember;
import org.bonitasoft.engine.bpm.actor.ActorNotFoundException;
import org.bonitasoft.test.toolkit.bpm.TestProcess;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.bonitasoft.test.toolkit.bpm.process.TestActorMemberFactory;
import org.bonitasoft.test.toolkit.organization.TestGroupFactory;
import org.bonitasoft.test.toolkit.organization.TestRoleFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.bpm.process.ActorMemberItem;
import org.bonitasoft.web.rest.model.identity.MemberType;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.junit.Test;

/**
 * @author Colin PUY
 */
public class APIActorMemberIT extends AbstractConsoleTest {

    private APIActorMember apiActorMember;

    @Override
    public void consoleTestSetUp() throws Exception {
        apiActorMember = new APIActorMember();
        apiActorMember.setCaller(getAPICaller(getInitiator().getSession(), "API/bpm/actorMember"));
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    private ActorMemberItem fetchUserActorMember(final long actorId, final long userId) throws Exception {
        try {
            final List<ActorMember> actorMembers = getProcessAPI().getActorMembers(actorId, 0, Integer.MAX_VALUE);
            for (final ActorMember actorMember : actorMembers) {
                if (actorMember.getUserId() == userId) {
                    return anActorMemberItem().fromActorMember(actorMember, actorId).build();
                }
            }
        } catch (final ActorNotFoundException e) {
            return null;
        }
        return null;
    }

    private ProcessAPI getProcessAPI() throws Exception {
        return TenantAPIAccessor.getProcessAPI(getInitiator().getSession());
    }

    private HashMap<String, String> buildActorIdFilter(final long actorId) {
        final HashMap<String, String> filters = new HashMap<>();
        filters.put(ATTRIBUTE_ACTOR_ID, String.valueOf(actorId));
        return filters;
    }

    private HashMap<String, String> buildMemberTypeFilter(final long actorId, final MemberType memberType) {
        final HashMap<String, String> filters = buildActorIdFilter(actorId);
        filters.put(FILTER_MEMBER_TYPE, memberType.name());
        return filters;
    }

    @Test
    public void testAdd() throws Exception {
        final TestProcess process = TestProcessFactory.getRandomHumanTaskProcess()
                .addActor(TestUserFactory.getMrSpechar());
        final long actorId = process.getActors().get(0).getId();
        final long userId = getInitiator().getId();

        final ActorMemberItem addedItem = apiActorMember
                .runAdd(anActorMemberItem().withActorId(actorId).withuserId(userId).build());

        final ActorMemberItem expectedItem = fetchUserActorMember(actorId, userId);
        assertItemEquals(expectedItem, addedItem);
    }

    @Test
    public void testDelete() throws Exception {
        final TestUser aUser = getInitiator();
        final ActorInstance actor = TestProcessFactory.getRandomHumanTaskProcess()
                .addActor(TestUserFactory.getMrSpechar()).getActors().get(0);
        final ActorMember addedMember = TestActorMemberFactory.createUserActorMember(actor.getId(), aUser);

        apiActorMember.runDelete(asList(makeAPIID(addedMember.getId())));

        assertNull(fetchUserActorMember(actor.getId(), aUser.getId()));
    }

    @Test
    public void searchCanBeFilteredByActorIdAndMemberType() throws Exception {
        final ActorInstance actor = TestProcessFactory.getRandomHumanTaskProcess()
                .addActor(TestUserFactory.getMrSpechar()).getActors().get(0);
        TestActorMemberFactory.createUserActorMember(actor.getId(), getInitiator());
        final ActorMember addedMember = TestActorMemberFactory.createGroupActorMember(actor.getId(),
                TestGroupFactory.getRAndD());
        final HashMap<String, String> filter = buildMemberTypeFilter(actor.getId(), GROUP);

        final ItemSearchResult<ActorMemberItem> searchResult = apiActorMember.runSearch(0, 10, null, null, filter, null,
                null);

        assertEquals(1L, searchResult.getTotal());
        assertItemEquals(anActorMemberItem().fromActorMember(addedMember, actor.getId()).build(),
                searchResult.getResults().get(0));
    }

    @Test
    public void searchCanBeFilteredByActorIdAndMemberTypeMemberShip() throws Exception {
        final ActorInstance actor = TestProcessFactory.getRandomHumanTaskProcess()
                .addActor(TestUserFactory.getMrSpechar()).getActors().get(0);
        final ActorMember addedMember = TestActorMemberFactory.createMembershipActorMember(actor.getId(),
                TestGroupFactory.getWeb(),
                TestRoleFactory.getManager());
        final HashMap<String, String> filters = buildMemberTypeFilter(actor.getId(), MemberType.MEMBERSHIP);

        final ItemSearchResult<ActorMemberItem> searchResult = apiActorMember.runSearch(0, 10, null, null, filters,
                null, null);

        assertEquals(1L, searchResult.getTotal());
        assertItemEquals(anActorMemberItem().fromActorMember(addedMember, actor.getId()).build(),
                searchResult.getResults().get(0));
    }

    @Test
    public void testSearchCanBePaginated() throws Exception {
        final TestProcess process = TestProcessFactory.getRandomHumanTaskProcess()
                .addActor(TestUserFactory.getMrSpechar());
        final long actorId = process.getActors().get(0).getId();
        TestActorMemberFactory.createUserActorMember(actorId, getInitiator());
        TestActorMemberFactory.createGroupActorMember(actorId, TestGroupFactory.getWeb());

        final ItemSearchResult<ActorMemberItem> searchResult = apiActorMember.runSearch(1, 1, null, null,
                buildActorIdFilter(actorId), null, null);

        // when adding actor to process, it seems to create automaticaly an actorMember for this user
        // so total is 3 even we add only 2 ActorMember
        assertEquals(3L, searchResult.getTotal());
        assertEquals(1, searchResult.getResults().size());
    }

    @Test
    public void testSearchWithDeploy() throws Exception {
        final ActorInstance actor = TestProcessFactory.getRandomHumanTaskProcess()
                .addActor(TestUserFactory.getMrSpechar()).getActors().get(0);
        TestActorMemberFactory.createUserActorMember(actor.getId(), getInitiator());
        final HashMap<String, String> filters = buildActorIdFilter(actor.getId());
        final List<String> deploy = singletonList(ATTRIBUTE_ACTOR_ID);

        final ItemSearchResult<ActorMemberItem> searchResult = apiActorMember.runSearch(0, 1, null, null, filters,
                deploy, null);

        assertNotNull(searchResult.getResults().get(0).getActor());
    }
}
