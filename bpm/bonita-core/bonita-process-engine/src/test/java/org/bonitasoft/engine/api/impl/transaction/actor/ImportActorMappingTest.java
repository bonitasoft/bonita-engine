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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author mazourd
 */
@RunWith(MockitoJUnitRunner.class)
public class ImportActorMappingTest {

    @Mock
    private ActorMappingService actorMappingService;
    @Mock
    private IdentityService identityService;
    @Mock
    private ActorMapping actorMapping;
    @InjectMocks
    private ImportActorMapping importActorMapping;
    private long ACTOR_ID = 12;

    @Test
    public void Execute_method_should_create_SActors_for_all_actors_and_correctly_add_users_roles_and_groups() throws SBonitaException {
        //given
        ArrayList<String> mocklist = new ArrayList<String>();
        mocklist.add("mock");
        Actor actor1 = new Actor("Lulu");
        Actor actor2 = new Actor("Lala");
        Actor actor3 = new Actor("Sisi");
        List<Actor> actors = new ArrayList<>();
        SActorImpl sActor = new SActorImpl("Lulu", ACTOR_ID, true);
        SUserImpl sUser = new SUserImpl();
        sUser.setId(ACTOR_ID);
        sActor.setId(ACTOR_ID);
        SRoleImpl sRole = new SRoleImpl();
        sRole.setId(ACTOR_ID);
        SGroupImpl sGroup = new SGroupImpl();
        sGroup.setId(ACTOR_ID);
        actors.add(actor1);
        actors.add(actor2);
        actors.add(actor3);
        for (final Actor actor : actors) {
            actor.addUser("mockUser");
            actor.addRole("mockRole");
            actor.addGroup("mockGroup");
            actor.addMembership("mockRole", "mockGroup");
        }
        doReturn(actors).when(actorMapping).getActors();
        when(actorMappingService.getActor(anyString(), anyLong())).thenReturn(sActor);
        when(identityService.getUserByUserName(anyString())).thenReturn(sUser);
        when(identityService.getRoleByName(anyString())).thenReturn(sRole);
        when(identityService.getGroupByPath(anyString())).thenReturn(sGroup);

        //when
        importActorMapping.execute(actorMapping, ACTOR_ID);
        //then
        verify(identityService, times(3)).getUserByUserName(anyString());
        verify(actorMappingService, times(3)).addRoleToActor(anyLong(), anyLong());
        verify(actorMappingService, times(3)).addGroupToActor(anyLong(), anyLong());
        verify(actorMappingService, times(3)).addRoleAndGroupToActor(anyLong(), anyLong(), anyLong());
    }


}
