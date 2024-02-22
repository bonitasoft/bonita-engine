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

import org.bonitasoft.engine.profile.model.SProfileMember;

/**
 * @author Emmanuel Duchastenier
 */
public class ProfileMemberBuilder extends PersistentObjectBuilder<SProfileMember, ProfileMemberBuilder> {

    private long userId = -1;

    private long groupId = -1;

    private long roleId = -1;

    private long profileId;

    public static ProfileMemberBuilder aProfileMember() {
        return new ProfileMemberBuilder();
    }

    @Override
    ProfileMemberBuilder getThisBuilder() {
        return this;
    }

    @Override
    SProfileMember _build() {
        return SProfileMember.builder()
                .profileId(profileId)
                .groupId(groupId)
                .roleId(roleId)
                .userId(userId).build();
    }

    public ProfileMemberBuilder withProfileId(long profileId) {
        this.profileId = profileId;
        return this;
    }

    public ProfileMemberBuilder withUserId(long userId) {
        this.userId = userId;
        return this;
    }

    public ProfileMemberBuilder withGroupId(long groupId) {
        this.groupId = groupId;
        return this;
    }

    public ProfileMemberBuilder withRoleId(long roleId) {
        this.roleId = roleId;
        return this;
    }
}
