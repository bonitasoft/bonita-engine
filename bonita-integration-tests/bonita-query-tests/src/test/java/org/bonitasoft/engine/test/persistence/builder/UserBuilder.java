/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.test.persistence.builder;

import org.bonitasoft.engine.identity.model.SUser;

public class UserBuilder extends PersistentObjectBuilder<SUser, UserBuilder> {

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
    public SUser _build() {
        SUser user = new SUser();
        user.setFirstName("aFirstName" + id);
        user.setLastName("aLastName" + id);
        user.setUserName(userName);
        user.setManagerUserId(managerUserId);
        user.setEnabled(true);
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
