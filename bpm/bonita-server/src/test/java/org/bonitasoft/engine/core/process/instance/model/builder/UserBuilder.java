package org.bonitasoft.engine.core.process.instance.model.builder;

import org.bonitasoft.engine.identity.model.impl.SUserImpl;


public class UserBuilder extends Builder<SUserImpl> {

    public static UserBuilder aUser() {
        return new UserBuilder();
    }
    
    @Override
    public SUserImpl _build() {
        SUserImpl user = new SUserImpl();
        user.setFirstName("aFirstName" + id);
        user.setLastName("aLastName" + id);
        return user;
    }
    
    public UserBuilder withId(long id) {
        this.id = id;
        return this;
    }
}
