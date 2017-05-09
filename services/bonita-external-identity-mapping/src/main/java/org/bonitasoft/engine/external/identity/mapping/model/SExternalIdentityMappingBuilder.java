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
package org.bonitasoft.engine.external.identity.mapping.model;

/**
 * @author Emmanuel Duchastenier
 */
public interface SExternalIdentityMappingBuilder {

    SExternalIdentityMappingBuilder setKind(String kind);

    SExternalIdentityMappingBuilder setUserId(long userId);

    SExternalIdentityMappingBuilder setGroupId(long groupId);

    SExternalIdentityMappingBuilder setRoleId(long roleId);

    SExternalIdentityMappingBuilder setDisplayNamePart1(String displayNamePart1);

    SExternalIdentityMappingBuilder setDisplayNamePart2(String displayNamePart2);

    SExternalIdentityMappingBuilder setDisplayNamePart3(String displayNamePart3);

    SExternalIdentityMapping done();

}
