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

import java.util.Date;

import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasIcon;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasUniqueId;

/**
 * @author Julien Mege
 * @author Zhiheng Yang
 * @author Séverin Moussel
 * @autor Paul Amar
 */
public class ProfileItem extends Item implements ItemHasUniqueId, ItemHasIcon {

    public static final String ATTRIBUTE_NAME = "name";

    public static final String ATTRIBUTE_DESCRIPTION = "description";

    public static final String FILTER_USER_ID = "user_id";

    public static final String ATTRIBUTE_IS_DEFAULT = "is_default";

    public static final String ATTRIBUTE_LAST_UPDATE_DATE = "lastUpdateDate";

    public static final String ATTRIBUTE_CREATION_DATE = "creationDate";

    public static final String ATTRIBUTE_CREATED_BY_USER_ID = "createdBy";

    public static final String ATTRIBUTE_UPDATED_BY_USER_ID = "updatedBy";

    public ProfileItem() {
        super();
    }

    public ProfileItem(final IItem item) {
        super(item);
    }

    @Override
    public void setId(final String id) {
        setAttribute(ATTRIBUTE_ID, id);
    }

    @Override
    public void setId(final Long id) {
        setAttribute(ATTRIBUTE_ID, id);
    }

    @Override
    public void setIcon(final String icon) {
        setAttribute(ATTRIBUTE_ICON, icon);
    }

    public void setName(final String name) {
        setAttribute(ATTRIBUTE_NAME, name);
    }

    public void setDescription(final String description) {
        setAttribute(ATTRIBUTE_DESCRIPTION, description);
    }

    public void setCreationDate(String date) {
        setAttribute(ATTRIBUTE_CREATION_DATE, date);
    }

    public void setCreationDate(Date date) {
        setAttribute(ATTRIBUTE_CREATION_DATE, date);
    }

    public void setCreatedByUserId(String id) {
        setAttribute(ATTRIBUTE_CREATED_BY_USER_ID, id);
    }

    public void setCreatedByUserId(Long id) {
        setAttribute(ATTRIBUTE_CREATED_BY_USER_ID, id);
    }

    public void setCreatedByUserId(APIID id) {
        setAttribute(ATTRIBUTE_CREATED_BY_USER_ID, id);
    }

    public UserItem getCreatedByUser() {
        return (UserItem) getDeploy(ATTRIBUTE_CREATED_BY_USER_ID);
    }

    public void setUpdatedByUserId(String id) {
        setAttribute(ATTRIBUTE_UPDATED_BY_USER_ID, id);
    }

    public void setUpdatedByUserId(Long id) {
        setAttribute(ATTRIBUTE_UPDATED_BY_USER_ID, id);
    }

    public void setUpdatedByUserId(APIID id) {
        setAttribute(ATTRIBUTE_UPDATED_BY_USER_ID, id);
    }

    public void setLastUpdateDate(final String date) {
        setAttribute(ATTRIBUTE_LAST_UPDATE_DATE, date);
    }

    public void setLastUpdateDate(final Date date) {
        setAttribute(ATTRIBUTE_LAST_UPDATE_DATE, date);
    }

    public UserItem getUpdatedByUser() {
        return (UserItem) getDeploy(ATTRIBUTE_UPDATED_BY_USER_ID);
    }

    public void setIsDefault(final Boolean isDefault) {
        setAttribute(ATTRIBUTE_IS_DEFAULT, isDefault);
    }

    @Override
    public String getIcon() {
        return getAttributeValue(ATTRIBUTE_ICON);
    }

    public String getName() {
        return getAttributeValue(ATTRIBUTE_NAME);
    }

    public String getDescription() {
        return getAttributeValue(ATTRIBUTE_DESCRIPTION);
    }

    public String isDefault() {
        return getAttributeValue(ATTRIBUTE_IS_DEFAULT);
    }

    @Override
    public ItemDefinition<ProfileItem> getItemDefinition() {
        return ProfileDefinition.get();
    }
}
