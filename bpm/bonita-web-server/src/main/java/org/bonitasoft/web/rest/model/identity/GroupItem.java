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

import org.bonitasoft.web.toolkit.client.common.util.StringUtil;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasCreator;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasDualName;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasIcon;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasLastUpdateDate;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasUniqueId;

/**
 * @author Yongtao Guo
 */
public class GroupItem extends Item
        implements ItemHasDualName, ItemHasUniqueId, ItemHasCreator, ItemHasLastUpdateDate, ItemHasIcon {

    public static final String ATTRIBUTE_DESCRIPTION = "description";
    public static final String ATTRIBUTE_PATH = "path";
    public static final String ATTRIBUTE_PARENT_PATH = "parent_path";
    public static final String ATTRIBUTE_PARENT_GROUP_ID = "parent_group_id";

    public GroupItem() {
        super();
    }

    public GroupItem(final IItem item) {
        super(item);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COUNTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String COUNTER_NUMBER_OF_USERS = "number_of_users";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTERS AND SETTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // GETTERS

    @Override
    public ItemDefinition<GroupItem> getItemDefinition() {
        return new GroupDefinition();
    }

    @Override
    public String getName() {
        return this.getAttributeValue(ATTRIBUTE_NAME);
    }

    @Override
    public String getDisplayName() {
        if (!StringUtil.isBlank(this.getAttributeValue(ATTRIBUTE_DISPLAY_NAME))) {
            return this.getAttributeValue(ATTRIBUTE_DISPLAY_NAME);
        } else {
            return this.getAttributeValue(ATTRIBUTE_NAME);
        }
    }

    public String getDescription() {
        return this.getAttributeValue(ATTRIBUTE_DESCRIPTION);
    }

    @Override
    public APIID getCreatedByUserId() {
        return this.getAttributeValueAsAPIID(ATTRIBUTE_CREATED_BY_USER_ID);
    }

    @Override
    public UserItem getCreatedByUser() {
        return new UserItem(getDeploy(ATTRIBUTE_CREATED_BY_USER_ID));
    }

    @Override
    public Date getCreationDate() {
        return this.getAttributeValueAsDate(ATTRIBUTE_CREATION_DATE);
    }

    @Override
    public Date getLastUpdateDate() {
        return this.getAttributeValueAsDate(ATTRIBUTE_LAST_UPDATE_DATE);
    }

    @Override
    public String getIcon() {
        return this.getAttributeValue(ATTRIBUTE_ICON);
    }

    public String getParentPath() {
        return getAttributeValue(ATTRIBUTE_PARENT_PATH);
    }

    public String getPath() {
        return getAttributeValue(ATTRIBUTE_PATH);
    }

    public String getParentGroupId() {
        return getAttributeValue(ATTRIBUTE_PARENT_GROUP_ID);
    }

    // SETTERS

    @Override
    public void setId(final String id) {
        this.setAttribute(ATTRIBUTE_ID, id);
    }

    @Override
    public void setId(final Long id) {
        this.setAttribute(ATTRIBUTE_ID, id.toString());// TODO delete the tostring call when severin will commit
    }

    @Override
    public void setName(final String name) {
        this.setAttribute(ATTRIBUTE_NAME, name);
    }

    @Override
    public void setCreatedByUserId(final APIID id) {
        this.setAttribute(ATTRIBUTE_CREATED_BY_USER_ID, id);
    }

    @Override
    public void setCreatedByUserId(final String id) {
        this.setAttribute(ATTRIBUTE_CREATED_BY_USER_ID, id);

    }

    @Override
    public void setCreatedByUserId(final Long id) {
        this.setAttribute(ATTRIBUTE_CREATED_BY_USER_ID, id.toString());

    }

    @Override
    public void setCreationDate(final String date) {
        this.setAttribute(ATTRIBUTE_CREATION_DATE, date);

    }

    @Override
    public void setCreationDate(final Date date) {
        this.setAttribute(ATTRIBUTE_CREATION_DATE, date);

    }

    @Override
    public void setDisplayName(final String displayName) {
        this.setAttribute(ATTRIBUTE_DISPLAY_NAME, displayName);
    }

    public void setDescription(final String description) {
        this.setAttribute(ATTRIBUTE_DESCRIPTION, description);
    }

    @Override
    public void setLastUpdateDate(final String date) {
        this.setAttribute(ATTRIBUTE_LAST_UPDATE_DATE, date);
    }

    @Override
    public void setLastUpdateDate(final Date date) {
        this.setAttribute(ATTRIBUTE_LAST_UPDATE_DATE, date);
    }

    @Override
    public void setIcon(final String icon) {
        this.setAttribute(ATTRIBUTE_ICON, icon);
    }

    public void setParentPath(final String parentPath) {
        this.setAttribute(ATTRIBUTE_PARENT_PATH, parentPath);
    }

    public void setPath(final String parentPath) {
        this.setAttribute(ATTRIBUTE_PATH, parentPath);
    }

    public void setParentGroupId(final String parentGroupId) {
        this.setAttribute(ATTRIBUTE_PARENT_GROUP_ID, parentGroupId);
    }

}
