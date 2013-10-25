/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.engine.search.descriptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.builder.GroupBuilder;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.profile.ProfileMemberSearchDescriptor;
import org.bonitasoft.engine.profile.builder.SProfileMemberBuilder;
import org.bonitasoft.engine.profile.model.SProfileMember;

/**
 * @author Zhang Bole
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SearchProfileMemberGroupDescriptor extends SearchEntityDescriptor {

    private final Map<String, FieldDescriptor> searchEntityKeys;

    private final Map<Class<? extends PersistentObject>, Set<String>> profileMemberAllFields;

    public SearchProfileMemberGroupDescriptor(final IdentityModelBuilder identityModelBuilder) {
        final GroupBuilder groupBuilder = identityModelBuilder.getGroupBuilder();
        searchEntityKeys = new HashMap<String, FieldDescriptor>(4);
        searchEntityKeys.put(ProfileMemberSearchDescriptor.ID, new FieldDescriptor(SProfileMember.class, SProfileMemberBuilder.ID));
        searchEntityKeys.put(ProfileMemberSearchDescriptor.PROFILE_ID, new FieldDescriptor(SProfileMember.class, SProfileMemberBuilder.PROFILE_ID));
        searchEntityKeys.put(ProfileMemberSearchDescriptor.ROLE_ID, new FieldDescriptor(SProfileMember.class, SProfileMemberBuilder.ROLE_ID));
        searchEntityKeys.put(ProfileMemberSearchDescriptor.USER_ID, new FieldDescriptor(SProfileMember.class, SProfileMemberBuilder.USER_ID));
        searchEntityKeys.put(ProfileMemberSearchDescriptor.GROUP_ID, new FieldDescriptor(SProfileMember.class, SProfileMemberBuilder.GROUP_ID));
        searchEntityKeys.put(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART1, new FieldDescriptor(SGroup.class, groupBuilder.getNameKey()));
        searchEntityKeys.put(ProfileMemberSearchDescriptor.DISPLAY_NAME_PART2, new FieldDescriptor(SGroup.class, groupBuilder.getParentPathKey()));

        profileMemberAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>(1);
        final Set<String> groupFields = new HashSet<String>(2);
        groupFields.add(groupBuilder.getNameKey());
        groupFields.add(groupBuilder.getParentPathKey());
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
