/**
 * Copyright (C) 2011 BonitaSoft S.A.
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
package org.bonitasoft.engine.identity.model.builder.impl;

import org.bonitasoft.engine.identity.model.builder.GroupBuilder;
import org.bonitasoft.engine.identity.model.builder.GroupUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.identity.model.builder.ProfileMetadataDefinitionBuilder;
import org.bonitasoft.engine.identity.model.builder.ProfileMetadataDefinitionUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.ProfileMetadataValueBuilder;
import org.bonitasoft.engine.identity.model.builder.ProfileMetadataValueUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.RoleBuilder;
import org.bonitasoft.engine.identity.model.builder.RoleUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.SContactInfoBuilder;
import org.bonitasoft.engine.identity.model.builder.SContactInfoLogBuilder;
import org.bonitasoft.engine.identity.model.builder.SContactInfoUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.SGroupLogBuilder;
import org.bonitasoft.engine.identity.model.builder.SProfileMetadataDefinitionLogBuilder;
import org.bonitasoft.engine.identity.model.builder.SRoleLogBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserLogBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserMembershipLogBuilder;
import org.bonitasoft.engine.identity.model.builder.UserMembershipBuilder;
import org.bonitasoft.engine.identity.model.builder.UserMembershipUpdateBuilder;
import org.bonitasoft.engine.identity.model.builder.UserUpdateBuilder;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 */
public class IdentityModelBuilderImpl implements IdentityModelBuilder {

    @Override
    public SUserBuilder getUserBuilder() {
        return new SUserBuilderImpl();
    }

    @Override
    public SContactInfoBuilder getUserContactInfoBuilder() {
        return new SContactInfoBuilderImpl();
    }

    @Override
    public GroupBuilder getGroupBuilder() {
        return new GroupBuilderImpl();
    }

    @Override
    public UserMembershipBuilder getUserMembershipBuilder() {
        return new UserMembershipBuilderImpl();
    }

    @Override
    public ProfileMetadataDefinitionBuilder getProfileMetadataDefinitionBuilder() {
        return new ProfileMetadataDefinitionBuilderImpl();
    }

    @Override
    public ProfileMetadataValueBuilder getProfileMetadataValueBuilder() {
        return new ProfileMetadataValueBuilderImpl();
    }

    @Override
    public RoleBuilder getRoleBuilder() {
        return new RoleBuilderImpl();
    }

    @Override
    public UserUpdateBuilder getUserUpdateBuilder() {
        return new UserUpdateBuilderImpl();
    }

    @Override
    public SContactInfoUpdateBuilder getUserContactInfoUpdateBuilder() {
        return new ContactInfoUpdateBuilderImpl();
    }

    @Override
    public GroupUpdateBuilder getGroupUpdateBuilder() {
        return new GroupUpdateBuilderImpl();
    }

    @Override
    public UserMembershipUpdateBuilder getUserMembershipUpdateBuilder() {
        return new UserMembershipUpdateBuilderImpl();
    }

    @Override
    public ProfileMetadataDefinitionUpdateBuilder getProfileMetadataDefinitionUpdateBuilder() {
        return new ProfileMetadataDefinitionUpdateBuilderImpl();
    }

    @Override
    public ProfileMetadataValueUpdateBuilder getProfileMetadataValueUpdateBuilder() {
        return new ProfileMetadataValueUpdateBuilderImpl();
    }

    @Override
    public RoleUpdateBuilder getRoleUpdateBuilder() {
        return new RoleUpdateBuilderImpl();
    }

    @Override
    public SRoleLogBuilder getSIdentityRoleLogBuilder() {
        return new SRoleLogBuilderImpl();
    }

    @Override
    public SUserLogBuilder getSIdentityUserLogBuilder() {
        return new SUserLogBuilderImpl();
    }

    @Override
    public SContactInfoLogBuilder getSIdentityUserContactInfoLogBuilder() {
        return new SContactInfoLogBuilderImpl();
    }

    @Override
    public SGroupLogBuilder getSIdentityGroupLogBuilder() {
        return new SGroupLogBuilderImpl();
    }

    @Override
    public SUserMembershipLogBuilder getSIdentityUserMembershipLogBuilder() {
        return new SUserMembershipLogBuilderImpl();
    }

    @Override
    public SProfileMetadataDefinitionLogBuilder getSIdentitySProfileMetadataDefinitionLogBuilder() {
        return new SProfileMetadataDefinitionLogBuilderImpl();
    }

}
