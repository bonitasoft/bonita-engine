package org.bonitasoft.engine.core.process.instance.model.builder;

import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.impl.SUserMembershipImpl;


public class UserMembershipBuilder extends PersistentObjectBuilder<SUserMembershipImpl> {

    private long groupId;
    private long userId;
    private long roleId;

    public static UserMembershipBuilder aUserMembership() {
        return new UserMembershipBuilder();
    }
    
    @Override
    SUserMembershipImpl _build() {
        SUserMembershipImpl membership = new SUserMembershipImpl();
        membership.setGroupId(groupId);
        membership.setUserId(userId);
        membership.setRoleId(roleId);
        return membership;
    }

    public UserMembershipBuilder forUser(SUser user) {
        this.userId = user.getId();
        return this;
    }
    
    public UserMembershipBuilder memberOf(long groupId, long roleId) {
        this.groupId = groupId;
        this.roleId = roleId;
        return this;
    }
}
