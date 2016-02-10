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
package org.bonitasoft.engine.api.impl.transaction.actor;

import org.bonitasoft.engine.actor.mapping.ActorMappingService;
import org.bonitasoft.engine.actor.mapping.model.SActorMember;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.identity.MemberType;

/**
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class AddActorMember implements TransactionContent {

    private final ActorMappingService actorMappingService;

    private final long actorId;

    private final long userId;

    private final long groupId;

    private final long roleId;

    private SActorMember actorMember;

    private final MemberType memberType;

    public AddActorMember(final ActorMappingService actorMappingService, final long actorId, final long userId, final long groupId, final long roleId,
            final MemberType memberType) {
        super();
        this.actorMappingService = actorMappingService;
        this.actorId = actorId;
        this.userId = userId;
        this.groupId = groupId;
        this.roleId = roleId;
        this.memberType = memberType;
    }

    @Override
    public void execute() throws SBonitaException {
        switch (memberType) {
            case USER:
                actorMember = actorMappingService.addUserToActor(actorId, userId);
                break;
            case GROUP:
                actorMember = actorMappingService.addGroupToActor(actorId, groupId);
                break;
            case ROLE:
                actorMember = actorMappingService.addRoleToActor(actorId, roleId);
                break;
            case MEMBERSHIP:
                actorMember = actorMappingService.addRoleAndGroupToActor(actorId, roleId, groupId);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    public SActorMember getActorMember() {
        return actorMember;
    }

}
