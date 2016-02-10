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
package org.bonitasoft.engine.identity.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator;
import org.bonitasoft.engine.identity.ExportedUser;
import org.bonitasoft.engine.identity.GroupCreator;
import org.bonitasoft.engine.identity.RoleCreator;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.xml.ElementBinding;

/**
 * @author Yanyan Liu
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class OrganizationBinding extends ElementBinding {
    
    private final List<CustomUserInfoDefinitionCreator> customUserInfoDefinitionCreators = new ArrayList<CustomUserInfoDefinitionCreator>();

    private final List<ExportedUser> users = new ArrayList<ExportedUser>();

    private final List<RoleCreator> roleCreators = new ArrayList<RoleCreator>();

    private final List<GroupCreator> groupCreators = new ArrayList<GroupCreator>();

    private final List<UserMembership> memberships = new ArrayList<UserMembership>();

    @Override
    public void setAttributes(final Map<String, String> attributes) {
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
    }

    @Override
    public void setChildObject(final String name, final Object value) {
        if (OrganizationMappingConstants.CUSTOM_USER_INFO_DEFINITION.equals(name)) {
            customUserInfoDefinitionCreators.add((CustomUserInfoDefinitionCreator) value);
        } else if (OrganizationMappingConstants.USER.equals(name)) {
            users.add((ExportedUser) value);
        } else if (OrganizationMappingConstants.ROLE.equals(name)) {
            roleCreators.add((RoleCreator) value);
        } else if (OrganizationMappingConstants.GROUP.equals(name)) {
            groupCreators.add((GroupCreator) value);
        } else if (OrganizationMappingConstants.MEMBERSHIP.equals(name)) {
            memberships.add((UserMembership) value);
        }
    }

    @Override
    public Object getObject() {
        return new OrganizationCreator(users, roleCreators, groupCreators, memberships, customUserInfoDefinitionCreators);
    }

    @Override
    public String getElementTag() {
        return OrganizationMappingConstants.IDENTITY_ORGANIZATION;
    }

}
