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
package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.impl.SActorMemberImpl;

public class ActorMemberBuilder extends PersistentObjectBuilder<SActorMemberImpl, ActorMemberBuilder> {

    private long actorId;
    private Long userId;
    private Long groupId;
    private Long roleId;

    @Override
    ActorMemberBuilder getThisBuilder() {
        return this;
    }

    public static ActorMemberBuilder anActorMember() {
        return new ActorMemberBuilder();
    }

    @Override
    SActorMemberImpl _build() {
        SActorMemberImpl actorMember = new SActorMemberImpl();
        actorMember.setActorId(actorId);
        if (userId != null)
            actorMember.setUserId(userId);
        if (groupId != null)
            actorMember.setGroupId(groupId);
        if (roleId != null)
            actorMember.setRoleId(roleId);
        return actorMember;
    }

    public ActorMemberBuilder forActor(SActor actor) {
        this.actorId = actor.getId();
        return this;
    }

    public ActorMemberBuilder withUserId(long userId) {
        this.userId = userId;
        return this;
    }

    public ActorMemberBuilder withGroupId(long groupId) {
        this.groupId = groupId;
        return this;
    }

    public ActorMemberBuilder withRoleId(long roleId) {
        this.roleId = roleId;
        return this;
    }
}
