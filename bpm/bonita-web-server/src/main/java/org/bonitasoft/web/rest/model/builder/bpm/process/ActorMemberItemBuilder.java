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
package org.bonitasoft.web.rest.model.builder.bpm.process;

import org.bonitasoft.engine.bpm.actor.ActorMember;
import org.bonitasoft.web.rest.model.bpm.process.ActorMemberItem;

/**
 * @author Colin PUY
 */
public class ActorMemberItemBuilder {

    private Long id;
    private Long actorId;
    private Long userId;
    private Long roleId;
    private Long groupId;

    private ActorMemberItemBuilder() {
    }

    public static ActorMemberItemBuilder anActorMemberItem() {
        return new ActorMemberItemBuilder();
    }

    public ActorMemberItem build() {
        ActorMemberItem item = new ActorMemberItem();
        item.setId(id);
        item.setActorId(actorId);
        item.setUserId(userId);
        item.setRoleId(roleId);
        item.setGroupId(groupId);
        return item;
    }

    public ActorMemberItemBuilder fromActorMember(ActorMember actorMember, long actorId) {
        id = actorMember.getId();
        this.actorId = actorId;
        userId = actorMember.getUserId();
        roleId = actorMember.getRoleId();
        groupId = actorMember.getGroupId();
        return this;
    }

    public ActorMemberItemBuilder withActorId(long actorId) {
        this.actorId = actorId;
        return this;
    }

    public ActorMemberItemBuilder withuserId(long userId) {
        this.userId = userId;
        return this;
    }
}
