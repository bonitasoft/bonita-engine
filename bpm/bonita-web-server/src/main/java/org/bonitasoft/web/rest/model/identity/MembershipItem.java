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
package org.bonitasoft.web.rest.model.identity;

import java.util.Date;

import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Séverin Moussel
 */
public class MembershipItem extends Item {

    public MembershipItem() {
        super();
    }

    public MembershipItem(final IItem item) {
        super(item);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String ATTRIBUTE_USER_ID = "user_id";

    public static final String ATTRIBUTE_GROUP_ID = "group_id";

    public static final String ATTRIBUTE_ROLE_ID = "role_id";

    public static final String ATTRIBUTE_ASSIGNED_BY_USER_ID = "assigned_by_user_id";

    public static final String ATTRIBUTE_ASSIGNED_DATE = "assigned_date";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES VALUES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FILTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COUNTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DEPLOYS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public UserItem getUser() {
        return new UserItem(getDeploy(ATTRIBUTE_USER_ID));
    }

    public RoleItem getRole() {
        return new RoleItem(getDeploy(ATTRIBUTE_ROLE_ID));
    }

    public GroupItem getGroup() {
        return new GroupItem(getDeploy(ATTRIBUTE_GROUP_ID));
    }

    public UserItem getAssignedByUser() {
        return new UserItem(getDeploy(ATTRIBUTE_ASSIGNED_BY_USER_ID));
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTERS AND SETTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // GETTERS

    public APIID getUserId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_USER_ID);
    }

    public APIID getGroupId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_GROUP_ID);
    }

    public APIID getRoleId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_ROLE_ID);
    }

    public APIID getAssignedByUserId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_ASSIGNED_BY_USER_ID);
    }

    public Date getAssignedDate() {
        return getAttributeValueAsDate(ATTRIBUTE_ASSIGNED_DATE);
    }

    // SETTERS

    public void setUserId(final String id) {
        setAttribute(ATTRIBUTE_USER_ID, id);
    }

    public void setUserId(final Long id) {
        setAttribute(ATTRIBUTE_USER_ID, id);
    }

    public void setUserId(final APIID id) {
        setAttribute(ATTRIBUTE_USER_ID, id);
    }

    public void setRoleId(final String id) {
        setAttribute(ATTRIBUTE_ROLE_ID, id);
    }

    public void setRoleId(final Long id) {
        setAttribute(ATTRIBUTE_ROLE_ID, id);
    }

    public void setRoleId(final APIID id) {
        setAttribute(ATTRIBUTE_ROLE_ID, id);
    }

    public void setGroupId(final String id) {
        setAttribute(ATTRIBUTE_GROUP_ID, id);
    }

    public void setGroupId(final Long id) {
        setAttribute(ATTRIBUTE_GROUP_ID, id);
    }

    public void setGroupId(final APIID id) {
        setAttribute(ATTRIBUTE_GROUP_ID, id);
    }

    public void setAssignedByUserId(final String id) {
        setAttribute(ATTRIBUTE_ASSIGNED_BY_USER_ID, id);
    }

    public void setAssignedByUserId(final Long id) {
        setAttribute(ATTRIBUTE_ASSIGNED_BY_USER_ID, id);
    }

    public void setAssignedByUserId(final APIID id) {
        setAttribute(ATTRIBUTE_ASSIGNED_BY_USER_ID, id);
    }

    public void setAssignedDate(final Date date) {
        setAttribute(ATTRIBUTE_ASSIGNED_DATE, date);
    }

    public void setAssignedDate(final String date) {
        setAttribute(ATTRIBUTE_ASSIGNED_DATE, date);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ItemDefinition getItemDefinition() {
        return new MembershipDefinition();
    }

}
