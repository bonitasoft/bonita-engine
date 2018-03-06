/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.engine.api.impl.transaction.actor;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.model.impl.SActorImpl;
import org.bonitasoft.engine.bpm.bar.actorMapping.Actor;
import org.bonitasoft.engine.bpm.bar.actorMapping.ActorMapping;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.impl.SGroupImpl;
import org.bonitasoft.engine.identity.model.impl.SRoleImpl;
import org.bonitasoft.engine.identity.model.impl.SUserImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author mazourd
 */
@RunWith(MockitoJUnitRunner.class)
public class ImportActorMappingTest {

    @Mock
    private ActorMappingService actorMappingService;

    @Mock
    private IdentityService identityService;

    @InjectMocks
    private ImportActorMapping importActorMapping;

    @Test
    public void execute_method_should_create_SActors_for_all_actors_and_correctly_add_users_roles_and_groups() throws SBonitaException {
        //given
        ArrayList<String> mocklist = new ArrayList<>();
        mocklist.add("mock");
        Actor actor1 = new Actor("Lulu");
        Actor actor2 = new Actor("Lala");
        Actor actor3 = new Actor("Sisi");
        List<Actor> actors = new ArrayList<>();
        actors.add(actor1);
        actors.add(actor2);
        actors.add(actor3);
        ActorMapping actorMapping = new ActorMapping();
        actorMapping.setActors(actors);
        long ACTOR_ID = 12;
        SActorImpl sActor = new SActorImpl("Lulu", 1458714L, true);
        sActor.setId(ACTOR_ID);

        SUserImpl sUser = new SUserImpl();
        final long userId = 111L;
        sUser.setId(userId);

        SRoleImpl sRole = new SRoleImpl();
        final long roleId = 222L;
        sRole.setId(roleId);

        SGroupImpl sGroup = new SGroupImpl();
        final long groupId = 333L;
        sGroup.setId(groupId);

        int cpt = 0;
        for (final Actor actor : actors) {
            actor.addUser("mockUser" + cpt++);
            actor.addRole("mockRole");
            actor.addGroup("mockGroup");
            actor.addMembership("mockRole", "mockGroup");
        }
        when(actorMappingService.getActor(anyString(), anyLong())).thenReturn(sActor);
        when(identityService.getUserByUserName(anyString())).thenReturn(sUser);
        when(identityService.getRoleByName(anyString())).thenReturn(sRole);
        when(identityService.getGroupByPath(anyString())).thenReturn(sGroup);

        //when
        importActorMapping.execute(actorMapping, ACTOR_ID);

        //then
        verify(identityService).getUserByUserName("mockUser0");
        verify(identityService).getUserByUserName("mockUser1");
        verify(identityService).getUserByUserName("mockUser2");
        verify(actorMappingService, times(3)).addRoleToActor(ACTOR_ID, roleId);
        verify(actorMappingService, times(3)).addGroupToActor(ACTOR_ID, groupId);
        verify(actorMappingService, times(3)).addRoleAndGroupToActor(ACTOR_ID, roleId, groupId);
    }

}
