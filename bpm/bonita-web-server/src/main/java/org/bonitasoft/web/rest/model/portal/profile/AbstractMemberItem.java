/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.model.portal.profile;

import org.bonitasoft.web.rest.model.identity.GroupItem;
import org.bonitasoft.web.rest.model.identity.RoleItem;
import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.Item;

/**
 * @author Séverin Moussel
 */
public abstract class AbstractMemberItem extends Item {

    public AbstractMemberItem() {
        super();
    }

    public AbstractMemberItem(final IItem item) {
        super(item);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String ATTRIBUTE_USER_ID = "user_id";

    public static final String ATTRIBUTE_ROLE_ID = "role_id";

    public static final String ATTRIBUTE_GROUP_ID = "group_id";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FILTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static final String FILTER_MEMBER_TYPE = "member_type";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FILTERS VALUES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String VALUE_MEMBER_TYPE_USER = "user";

    public static final String VALUE_MEMBER_TYPE_GROUP = "group";

    public static final String VALUE_MEMBER_TYPE_ROLE = "role";

    public static final String VALUE_MEMBER_TYPE_MEMBERSHIP = "roleAndGroup";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SETTERS / GETTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public APIID getUserId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_USER_ID);
    }

    public void setUserId(final String userId) {
        setAttribute(ATTRIBUTE_USER_ID, userId);
    }

    public void setUserId(final APIID userId) {
        setAttribute(ATTRIBUTE_USER_ID, userId);
    }

    public void setUserId(final Long userId) {
        setAttribute(ATTRIBUTE_USER_ID, userId);
    }

    public APIID getRoleId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_ROLE_ID);
    }

    public void setRoleId(final String roleId) {
        setAttribute(ATTRIBUTE_ROLE_ID, roleId);
    }

    public void setRoleId(final APIID roleId) {
        setAttribute(ATTRIBUTE_ROLE_ID, roleId);
    }

    public void setRoleId(final Long roleId) {
        setAttribute(ATTRIBUTE_ROLE_ID, roleId);
    }

    public APIID getGroupId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_GROUP_ID);
    }

    public void setGroupId(final String groupId) {
        setAttribute(ATTRIBUTE_GROUP_ID, groupId);
    }

    public void setGroupId(final APIID groupId) {
        setAttribute(ATTRIBUTE_GROUP_ID, groupId);
    }

    public void setGroupId(final Long groupId) {
        setAttribute(ATTRIBUTE_GROUP_ID, groupId);
    }

    // Deploys

    public UserItem getUser() {
        return new UserItem(getDeploy(ATTRIBUTE_USER_ID));
    }

    public RoleItem getRole() {
        return new RoleItem(getDeploy(ATTRIBUTE_ROLE_ID));
    }

    public GroupItem getGroup() {
        return new GroupItem(getDeploy(ATTRIBUTE_GROUP_ID));
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void setMembershipId(final APIID roleId, final APIID groupId) {
        setRoleId(roleId);
        setGroupId(groupId);
    }

    public boolean isGroup() {
        return getGroupId() != null && getUserId() == null && getRoleId() == null;
    }

    public boolean isRole() {
        return getGroupId() == null && getUserId() == null && getRoleId() != null;
    }

    public boolean isUser() {
        return getGroupId() == null && getUserId() != null && getRoleId() == null;
    }

    public boolean isMembership() {
        return getGroupId() != null && getUserId() == null && getRoleId() != null;
    }
}
