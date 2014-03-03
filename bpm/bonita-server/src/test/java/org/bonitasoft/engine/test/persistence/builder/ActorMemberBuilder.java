package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.actor.mapping.model.impl.SActorMemberImpl;


public class ActorMemberBuilder extends PersistentObjectBuilder<SActorMemberImpl> {

    private long actorId;
    private Long userId;
    private Long groupId;
    private Long roleId;

    public static ActorMemberBuilder anActorMember() {
        return new ActorMemberBuilder();
    }
    
    @Override
    SActorMemberImpl _build() {
        SActorMemberImpl actorMember = new SActorMemberImpl();
        actorMember.setActorId(actorId);
        if (userId != null) actorMember.setUserId(userId);
        if (groupId != null) actorMember.setGroupId(groupId);
        if (roleId != null) actorMember.setRoleId(roleId);
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
