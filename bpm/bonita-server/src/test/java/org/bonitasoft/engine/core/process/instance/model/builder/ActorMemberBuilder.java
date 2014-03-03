package org.bonitasoft.engine.core.process.instance.model.builder;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.impl.SActorMemberImpl;


public class ActorMemberBuilder extends Builder<SActorMemberImpl> {

    private long actorId;
    private long userId;
    private long groupId;
    private long roleId;

    public static ActorMemberBuilder anActorMember() {
        return new ActorMemberBuilder();
    }
    
    @Override
    SActorMemberImpl _build() {
        SActorMemberImpl actorMember = new SActorMemberImpl();
        actorMember.setUserId(userId);
        actorMember.setActorId(actorId);
        actorMember.setGroupId(groupId);
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
