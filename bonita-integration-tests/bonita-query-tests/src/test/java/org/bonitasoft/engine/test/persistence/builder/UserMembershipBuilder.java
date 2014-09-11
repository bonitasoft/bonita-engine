package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.impl.SUserMembershipImpl;

public class UserMembershipBuilder extends PersistentObjectBuilder<SUserMembershipImpl, UserMembershipBuilder> {

    private long groupId;

    private long userId;

    private long roleId;

    public static UserMembershipBuilder aUserMembership() {
        return new UserMembershipBuilder();
    }

    @Override
    UserMembershipBuilder getThisBuilder() {
        return this;
    }

    @Override
    SUserMembershipImpl _build() {
        SUserMembershipImpl membership = new SUserMembershipImpl();
        membership.setGroupId(groupId);
        membership.setUserId(userId);
        membership.setRoleId(roleId);
        return membership;
    }

    public UserMembershipBuilder forUser(final SUser user) {
        this.userId = user.getId();
        return this;
    }

    public UserMembershipBuilder forUser(final long userId) {
        this.userId = userId;
        return this;
    }

    public UserMembershipBuilder memberOf(final long groupId, final long roleId) {
        this.groupId = groupId;
        this.roleId = roleId;
        return this;
    }
}
