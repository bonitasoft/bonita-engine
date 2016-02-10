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

import java.util.Date;
import java.util.Map;

import org.bonitasoft.engine.identity.impl.UserMembershipImpl;
import org.bonitasoft.engine.xml.ElementBinding;

/**
 * @author Yanyan Liu
 */
public class MembershipBinding extends ElementBinding {

    private final UserMembershipImpl membership;

    public MembershipBinding() {
        super();
        membership = new UserMembershipImpl();
    }

    @Override
    public void setAttributes(final Map<String, String> attributes) {
    }

    @Override
    public void setChildElement(final String name, final String value, final Map<String, String> attributes) {
        if (OrganizationMappingConstants.USER_NAME.equals(name)) {
            membership.setUsername(value);
        } else if (OrganizationMappingConstants.ROLE_NAME.equals(name)) {
            membership.setRoleName(value);
        } else if (OrganizationMappingConstants.GROUP_NAME.equals(name)) {
            membership.setGroupName(value);
        } else if (OrganizationMappingConstants.GROUP_PARENT_PATH.equals(name)) {
            membership.setGroupParentPath(value);
        } else if (OrganizationMappingConstants.ASSIGNED_BY.equals(name)) {
            membership.setAssignedByName(value);
        } else if (OrganizationMappingConstants.ASSIGNED_DATE.equals(name)) {
            membership.setAssignedDate(new Date(Long.valueOf(value)));
        }
    }

    @Override
    public void setChildObject(final String name, final Object value) {
    }

    @Override
    public Object getObject() {
        return membership;
    }

    @Override
    public String getElementTag() {
        return OrganizationMappingConstants.MEMBERSHIP;
    }

}
