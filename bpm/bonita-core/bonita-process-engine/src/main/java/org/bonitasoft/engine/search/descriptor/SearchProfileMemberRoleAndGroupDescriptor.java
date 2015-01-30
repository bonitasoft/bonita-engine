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
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.builder.SGroupBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SRoleBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.profile.ProfileMemberSearchDescriptor;
import org.bonitasoft.engine.profile.builder.SProfileMemberBuilderFactory;
import org.bonitasoft.engine.profile.model.SProfileMember;

/**
 * @author Zhang Bole
 * @author Celine Souchet
 */
public class SearchProfileMemberRoleAndGroupDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> profileMemberAllFields;

    public SearchProfileMemberRoleAndGroupDescriptor() {
        searchEntityKeys = new HashMap<String, FieldDescriptor>(7);
        searchEntityKeys.put(ProfileMemberSearchDescriptor.ID, new FieldDescriptor(SProfileMember.class, SProfileMemberBuilderFactory.ID));
        searchEntityKeys.put(ProfileMemberSearchDescriptor.PROFILE_ID, new FieldDescriptor(SProfileMember.class, SProfileMemberBuilderFactory.PROFILE_ID));
        searchEntityKeys.put(ProfileMemberSearchDescriptor.ROLE_ID, new FieldDescriptor(SProfileMember.class, SProfileMemberBuilderFactory.ROLE_ID));
        searchEntityKeys.put(ProfileMemberSearchDescriptor.USER_ID, new FieldDescriptor(SProfileMember.class, SProfileMemberBuilderFactory.USER_ID));
        searchEntityKeys.put(ProfileMemberSearchDescriptor.GROUP_ID, new FieldDescriptor(SProfileMember.class, SProfileMemberBuilderFactory.GROUP_ID));
        searchEntityKeys.put(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, new FieldDescriptor(SRole.class, BuilderFactory.get(SRoleBuilderFactory.class).getNameKey()));
        searchEntityKeys.put(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART2, new FieldDescriptor(SGroup.class, BuilderFactory.get(SGroupBuilderFactory.class).getNameKey()));
        searchEntityKeys.put(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART3, new FieldDescriptor(SGroup.class, BuilderFactory.get(SGroupBuilderFactory.class).getParentPathKey()));

        profileMemberAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(2);
        final Set<String> roleFields = new HashSet<String>(1);
        roleFields.add(BuilderFactory.get(SRoleBuilderFactory.class).getNameKey());
        profileMemberAllFields.put(SRole.class, roleFields);
        final Set<String> groupFields = new HashSet<String>(2);
        groupFields.add(BuilderFactory.get(SGroupBuilderFactory.class).getNameKey());
        groupFields.add(BuilderFactory.get(SGroupBuilderFactory.class).getParentPathKey());
        profileMemberAllFields.put(SGroup.class, groupFields);
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
