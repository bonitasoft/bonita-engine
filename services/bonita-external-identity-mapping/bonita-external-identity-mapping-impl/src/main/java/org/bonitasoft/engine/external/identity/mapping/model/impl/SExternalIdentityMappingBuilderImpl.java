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
package org.bonitasoft.engine.external.identity.mapping.model.impl;

import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMapping;
import org.bonitasoft.engine.external.identity.mapping.model.SExternalIdentityMappingBuilder;
import org.bonitasoft.engine.queriablelogger.model.builder.impl.MissingMandatoryFieldsException;

/**
 * @author Emmanuel Duchastenier
 */
public class SExternalIdentityMappingBuilderImpl implements SExternalIdentityMappingBuilder {

    private final SExternalIdentityMappingImpl mapping;

    public SExternalIdentityMappingBuilderImpl(final SExternalIdentityMappingImpl mapping) {
        super();
        this.mapping = mapping;
    }

    @Override
    public SExternalIdentityMappingBuilder setKind(final String kind) {
        mapping.setKind(kind);
        return this;
    }

    @Override
    public SExternalIdentityMappingBuilder setUserId(final long userId) {
        mapping.setUserId(userId);
        return this;
    }

    @Override
    public SExternalIdentityMappingBuilder setGroupId(final long groupId) {
        mapping.setGroupId(groupId);
        return this;
    }

    @Override
    public SExternalIdentityMappingBuilder setRoleId(final long roleId) {
        mapping.setRoleId(roleId);
        return this;
    }

    @Override
    public SExternalIdentityMappingBuilder setDisplayNamePart1(final String displayNamePart1) {
        mapping.setDisplayNamePart1(displayNamePart1);
        return this;
    }

    @Override
    public SExternalIdentityMappingBuilder setDisplayNamePart2(final String displayNamePart2) {
        mapping.setDisplayNamePart2(displayNamePart2);
        return this;
    }

    @Override
    public SExternalIdentityMappingBuilder setDisplayNamePart3(final String displayNamePart3) {
        mapping.setDisplayNamePart3(displayNamePart3);
        return this;
    }

    @Override
    public SExternalIdentityMapping done() {
        if (mapping.getUserId() == -1 && mapping.getGroupId() == -1 && mapping.getRoleId() == -1) {
            throw new MissingMandatoryFieldsException("you must set at least one of the following fields: userId, groupId, roleId");
        }
        return mapping;
    }

}
