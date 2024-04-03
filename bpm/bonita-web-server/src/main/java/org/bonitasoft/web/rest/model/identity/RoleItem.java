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
import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasCreator;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasDualName;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasIcon;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasLastUpdateDate;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasUniqueId;

/**
 * @author Séverin Moussel
 */
public class RoleItem extends Item
        implements ItemHasDualName, ItemHasUniqueId, ItemHasCreator, ItemHasLastUpdateDate, ItemHasIcon {

    public RoleItem() {
        super();
    }

    public RoleItem(final IItem item) {
        super(item);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String ATTRIBUTE_DESCRIPTION = "description";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES VALUES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FILTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COUNTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static final String COUNTER_NUMBER_OF_USERS = "number_of_users";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DEPLOYS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public UserItem getCreatedByUser() {
        return new UserItem(getDeploy(ATTRIBUTE_CREATED_BY_USER_ID));
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTERS AND SETTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // GETTERS

    @Override
    public String getName() {
        return getAttributeValue(ATTRIBUTE_NAME);
    }

    @Override
    public String getDisplayName() {
        return getAttributeValue(ATTRIBUTE_DISPLAY_NAME);
    }

    public String getDescription() {
        return getAttributeValue(ATTRIBUTE_DESCRIPTION);
    }

    @Override
    public String getIcon() {
        return getAttributeValue(ATTRIBUTE_ICON);
    }

    @Override
    public Date getLastUpdateDate() {
        return getAttributeValueAsDate(ATTRIBUTE_LAST_UPDATE_DATE);
    }

    @Override
    public Date getCreationDate() {
        return getAttributeValueAsDate(ATTRIBUTE_CREATION_DATE);
    }

    @Override
    public APIID getCreatedByUserId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_CREATED_BY_USER_ID);
    }

    public Long getNumberOfUsers() {
        return getAttributeValueAsLong(COUNTER_NUMBER_OF_USERS);
    }

    // SETTERS

    public void setDescription(final String description) {
        setAttribute(ATTRIBUTE_DESCRIPTION, description);
    }

    @Override
    public void setIcon(final String icon) {
        setAttribute(ATTRIBUTE_ICON, icon);
    }

    @Override
    public void setLastUpdateDate(final String date) {
        setAttribute(ATTRIBUTE_LAST_UPDATE_DATE, date);
    }

    @Override
    public void setLastUpdateDate(final Date date) {
        setAttribute(ATTRIBUTE_LAST_UPDATE_DATE, date);
    }

    @Override
    public void setCreationDate(final String date) {
        setAttribute(ATTRIBUTE_LAST_UPDATE_DATE, date);
    }

    @Override
    public void setCreationDate(final Date date) {
        setAttribute(ATTRIBUTE_CREATION_DATE, date);
    }

    @Override
    public void setCreatedByUserId(final String id) {
        setAttribute(ATTRIBUTE_CREATED_BY_USER_ID, id);
    }

    @Override
    public void setCreatedByUserId(final Long id) {
        setAttribute(ATTRIBUTE_CREATED_BY_USER_ID, id);
    }

    @Override
    public void setCreatedByUserId(final APIID id) {
        setAttribute(ATTRIBUTE_CREATED_BY_USER_ID, id);
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
    public void setName(final String name) {
        setAttribute(ATTRIBUTE_NAME, name);
    }

    @Override
    public void setDisplayName(final String displayName) {
        setAttribute(ATTRIBUTE_DISPLAY_NAME, displayName);
    }
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ItemDefinition getItemDefinition() {
        return Definitions.get(RoleDefinition.TOKEN);
    }

}
