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
package org.bonitasoft.engine.search.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.identity.UserSearchDescriptor;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.model.builder.SUserBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SUserMembershipBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;

/**
 * @author Matthieu Chaffotte
 */
public class SearchUserDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> userKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> userAllFields;

    SearchUserDescriptor() {
        userKeys = new HashMap<String, FieldDescriptor>(8);
        userKeys.put(UserSearchDescriptor.USER_NAME, new FieldDescriptor(SUser.class, BuilderFactory.get(SUserBuilderFactory.class).getUserNameKey()));
        userKeys.put(UserSearchDescriptor.FIRST_NAME, new FieldDescriptor(SUser.class, BuilderFactory.get(SUserBuilderFactory.class).getFirstNameKey()));
        userKeys.put(UserSearchDescriptor.LAST_NAME, new FieldDescriptor(SUser.class, BuilderFactory.get(SUserBuilderFactory.class).getLastNameKey()));
        userKeys.put(UserSearchDescriptor.ENABLED, new FieldDescriptor(SUser.class, BuilderFactory.get(SUserBuilderFactory.class).getEnabledKey()));
        userKeys.put(UserSearchDescriptor.LAST_CONNECTION, new FieldDescriptor(SUser.class, BuilderFactory.get(SUserBuilderFactory.class).getLastConnectionKey()));
        userKeys.put(UserSearchDescriptor.MANAGER_USER_ID, new FieldDescriptor(SUser.class, BuilderFactory.get(SUserBuilderFactory.class).getManagerUserIdKey()));
        userKeys.put(UserSearchDescriptor.ROLE_ID, new FieldDescriptor(SUserMembership.class, BuilderFactory.get(SUserMembershipBuilderFactory.class).getRoleIdKey()));
        userKeys.put(UserSearchDescriptor.GROUP_ID, new FieldDescriptor(SUserMembership.class, BuilderFactory.get(SUserMembershipBuilderFactory.class).getGroupIdKey()));

        userAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> userFields = new HashSet<String>(4);
        userFields.add(BuilderFactory.get(SUserBuilderFactory.class).getUserNameKey());
        userFields.add(BuilderFactory.get(SUserBuilderFactory.class).getFirstNameKey());
        userFields.add(BuilderFactory.get(SUserBuilderFactory.class).getLastNameKey());
        userFields.add(BuilderFactory.get(SUserBuilderFactory.class).getJobTitleKey());
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
