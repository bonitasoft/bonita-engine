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
package org.bonitasoft.engine.identity.impl;

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
        final int prime = 31;
        int result = 1;
        result = prime * result + (contactData == null ? 0 : contactData.hashCode());
        result = prime * result + (user == null ? 0 : user.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserWithContactDataImpl other = (UserWithContactDataImpl) obj;
        if (contactData == null) {
            if (other.contactData != null) {
                return false;
            }
        } else if (!contactData.equals(other.contactData)) {
            return false;
        }
        if (user == null) {
            if (other.user != null) {
                return false;
            }
        } else if (!user.equals(other.user)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UserWithContactDataImpl [user=" + user + ", contactData=" + contactData + "]";
    }

}
