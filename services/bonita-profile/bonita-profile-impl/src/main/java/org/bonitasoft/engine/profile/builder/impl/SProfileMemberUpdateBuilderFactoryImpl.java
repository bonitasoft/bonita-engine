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

import org.bonitasoft.engine.profile.builder.SProfileMemberBuilderFactory;
import org.bonitasoft.engine.profile.builder.SProfileMemberUpdateBuilder;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Celine Souchet
 */
public class SProfileMemberUpdateBuilderFactoryImpl implements SProfileMemberUpdateBuilder {

    protected final EntityUpdateDescriptor descriptor;

    public SProfileMemberUpdateBuilderFactoryImpl() {
        descriptor = new EntityUpdateDescriptor();
    }

    @Override
    public EntityUpdateDescriptor done() {
        return descriptor;
    }

    @Override
    public SProfileMemberUpdateBuilder setGroupId(final long groupId) {
        descriptor.addField(SProfileMemberBuilderFactory.GROUP_ID, groupId);
        return this;
    }

    @Override
    public SProfileMemberUpdateBuilder setRoleId(final long roleId) {
        descriptor.addField(SProfileMemberBuilderFactory.ROLE_ID, roleId);
        return this;
    }

    @Override
    public SProfileMemberUpdateBuilder setUserId(final long userId) {
        descriptor.addField(SProfileMemberBuilderFactory.USER_ID, userId);
        return this;
    }

    @Override
    public SProfileMemberUpdateBuilder setDisplayNamePart1(final String displayNamePart1) {
        descriptor.addField(SProfileMemberBuilderFactory.DISPLAY_NAME_PART1, displayNamePart1);
        return this;
    }

    @Override
    public SProfileMemberUpdateBuilder setDisplayNamePart2(final String displayNamePart2) {
        descriptor.addField(SProfileMemberBuilderFactory.DISPLAY_NAME_PART2, displayNamePart2);
        return this;
    }

    @Override
    public SProfileMemberUpdateBuilder setDisplayNamePart3(final String displayNamePart3) {
        descriptor.addField(SProfileMemberBuilderFactory.DISPLAY_NAME_PART3, displayNamePart3);
        return this;
    }

}
