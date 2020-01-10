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

import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.profile.ProfileMemberSearchDescriptor;
import org.bonitasoft.engine.profile.model.SProfileMember;

/**
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SearchProfileMemberRoleDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> profileMemberAllFields;

    public SearchProfileMemberRoleDescriptor() {
        searchEntityKeys = new HashMap<String, FieldDescriptor>(6);
        searchEntityKeys.put(ProfileMemberSearchDescriptor.ID,
                new FieldDescriptor(SProfileMember.class, SProfileMember.ID));
        searchEntityKeys.put(ProfileMemberSearchDescriptor.PROFILE_ID,
                new FieldDescriptor(SProfileMember.class, SProfileMember.PROFILE_ID));
        searchEntityKeys.put(ProfileMemberSearchDescriptor.ROLE_ID,
                new FieldDescriptor(SProfileMember.class, SProfileMember.ROLE_ID));
        searchEntityKeys.put(ProfileMemberSearchDescriptor.USER_ID,
                new FieldDescriptor(SProfileMember.class, SProfileMember.USER_ID));
        searchEntityKeys.put(ProfileMemberSearchDescriptor.GROUP_ID,
                new FieldDescriptor(SProfileMember.class, SProfileMember.GROUP_ID));
        searchEntityKeys.put(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1,
                new FieldDescriptor(SRole.class, SRole.NAME));

        profileMemberAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> roleFields = new HashSet<String>(1);
        roleFields.add(SRole.NAME);
        profileMemberAllFields.put(SRole.class, roleFields);
    }

    @Override
    protected Map<String, FieldDescriptor> getEntityKeys() {
        return searchEntityKeys;
    }

    @Override
    protected Map<Class<? extends PersistentObject>, Set<String>> getAllFields() {
        return profileMemberAllFields;
    }

}
