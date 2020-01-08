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
package org.bonitasoft.engine.search.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.identity.UserSearchDescriptor;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Matthieu Chaffotte
 */
public class SearchUserDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> userKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> userAllFields;

    SearchUserDescriptor() {
        userKeys = new HashMap<String, FieldDescriptor>(8);
        userKeys.put(UserSearchDescriptor.ID, new FieldDescriptor(SUser.class, SUser.ID));
        userKeys.put(UserSearchDescriptor.USER_NAME, new FieldDescriptor(SUser.class, SUser.USER_NAME));
        userKeys.put(UserSearchDescriptor.FIRST_NAME, new FieldDescriptor(SUser.class, SUser.FIRST_NAME));
        userKeys.put(UserSearchDescriptor.LAST_NAME, new FieldDescriptor(SUser.class, SUser.LAST_NAME));
        userKeys.put(UserSearchDescriptor.ENABLED, new FieldDescriptor(SUser.class, SUser.ENABLED));
        userKeys.put(UserSearchDescriptor.LAST_CONNECTION, new FieldDescriptor(SUser.class, SUser.LAST_CONNECTION));
        userKeys.put(UserSearchDescriptor.MANAGER_USER_ID, new FieldDescriptor(SUser.class, SUser.MANAGER_USER_ID));
        userKeys.put(UserSearchDescriptor.ROLE_ID, new FieldDescriptor(SUserMembership.class, SUserMembership.ROLE_ID));
        userKeys.put(UserSearchDescriptor.GROUP_ID,
                new FieldDescriptor(SUserMembership.class, SUserMembership.GROUP_ID));

        userAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> userFields = new HashSet<String>(4);
        userFields.add(SUser.USER_NAME);
        userFields.add(SUser.FIRST_NAME);
        userFields.add(SUser.LAST_NAME);
        userFields.add(SUser.JOB_TITLE);
        userAllFields.put(SUser.class, userFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return userKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return userAllFields;
    }

}
