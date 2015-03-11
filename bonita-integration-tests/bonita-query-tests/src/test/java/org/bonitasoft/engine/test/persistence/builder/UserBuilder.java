package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.identity.model.impl.SUserImpl;

public class UserBuilder extends PersistentObjectBuilder<SUserImpl, UserBuilder> {

    private String userName = "userName" + id;

    private long managerUserId = 0;

    public static UserBuilder aUser() {
        return new UserBuilder();
    }

    @Override
    UserBuilder getThisBuilder() {
        return this;
    }

    @Override
    public SUserImpl _build() {
        SUserImpl user = new SUserImpl();
        user.setFirstName("aFirstName" + id);
        user.setLastName("aLastName" + id);
        user.setUserName(userName);
        user.setManagerUserId(managerUserId);
        return user;
    }

    public UserBuilder withId(long id) {
        this.id = id;
        return this;
    }

    public UserBuilder withUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public UserBuilder withManager(long managerUserId) {
        this.managerUserId = managerUserId;
        return this;
    }
}
