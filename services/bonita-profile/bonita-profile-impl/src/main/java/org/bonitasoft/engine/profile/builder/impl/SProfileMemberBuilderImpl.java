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
package org.bonitasoft.engine.profile.builder.impl;

import org.bonitasoft.engine.profile.builder.SProfileMemberBuilder;
import org.bonitasoft.engine.profile.model.SProfileMember;
import org.bonitasoft.engine.profile.model.impl.SProfileMemberImpl;

/**
 * @author Elias Ricken de Medeiros
 */
public class SProfileMemberBuilderImpl implements SProfileMemberBuilder {

    private final SProfileMemberImpl profileMember;
    
    public SProfileMemberBuilderImpl(final SProfileMemberImpl profileMember) {
        super();
        this.profileMember = profileMember;
    }

    @Override
    public SProfileMemberBuilder setId(final long id) {
        profileMember.setId(id);
        return this;
    }

    @Override
    public SProfileMemberBuilder setGroupId(final long groupId) {
        profileMember.setGroupId(groupId);
        return this;
    }

    @Override
    public SProfileMemberBuilder setRoleId(final long roleId) {
        profileMember.setRoleId(roleId);
        return this;
    }

    @Override
    public SProfileMemberBuilder setUserId(final long userId) {
        profileMember.setUserId(userId);
        return this;
    }

    @Override
    public SProfileMemberBuilder setDisplayNamePart1(final String displayNamePart1) {
        profileMember.setDisplayNamePart1(displayNamePart1);
        return this;
    }

    @Override
    public SProfileMemberBuilder setDisplayNamePart2(final String displayNamePart2) {
        profileMember.setDisplayNamePart2(displayNamePart2);
        return this;
    }

    @Override
    public SProfileMemberBuilder setDisplayNamePart3(final String displayNamePart3) {
        profileMember.setDisplayNamePart3(displayNamePart3);
        return this;
    }

    @Override
    public SProfileMember done() {
        return profileMember;
    }

}
