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
package org.bonitasoft.engine.identity.impl;

import java.util.Objects;
import java.util.StringJoiner;

import org.bonitasoft.engine.identity.ContactData;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserWithContactData;

/**
 * @author Matthieu Chaffotte
 */
public class UserWithContactDataImpl implements UserWithContactData {

    private static final long serialVersionUID = -6496385394314633962L;

    private final User user;

    private final ContactData contactData;

    public UserWithContactDataImpl(final User user, final ContactData contactData) {
        this.user = new UserImpl(user);
        if (contactData != null) {
            this.contactData = new ContactDataImpl(contactData);
        } else {
            this.contactData = null;
        }
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public ContactData getContactData() {
        return contactData;
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, contactData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserWithContactDataImpl that = (UserWithContactDataImpl) o;
        return Objects.equals(user, that.user) && Objects.equals(contactData, that.contactData);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UserWithContactDataImpl.class.getSimpleName() + "[", "]")
                .add("user=" + user)
                .add("contactData=" + contactData)
                .toString();
    }

}
