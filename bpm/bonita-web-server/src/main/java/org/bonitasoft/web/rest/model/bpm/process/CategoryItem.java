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
package org.bonitasoft.web.rest.model.bpm.process;

import java.util.Date;

import org.bonitasoft.web.rest.model.identity.UserItem;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasCreator;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasDualName;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasLastUpdateDate;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasUniqueId;

/**
 * category item
 *
 * @author Qixiang Zhang
 */
public class CategoryItem extends Item
        implements ItemHasUniqueId, ItemHasDualName, ItemHasCreator, ItemHasLastUpdateDate {

    public CategoryItem() {
        super();
    }

    public CategoryItem(final IItem item) {
        super(item);
    }

    /**
     * category of id
     */
    public static final String ATTRIBUTE_ID = "id";

    /**
     * created by
     */
    public static final String ATTRIBUTE_CREATED_BY_USER_ID = "createdBy";

    /**
     * category name
     */
    public static final String ATTRIBUTE_NAME = "name";

    /**
     * description about category
     */
    public static final String ATTRIBUTE_DESCRIPTION = "description";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ATTRIBUTES VALUES
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FILTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static final String FILTER_PROCESS_ID = "processId";

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // COUNTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GETTERS AND SETTERS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // GETTERS

    @Override
    public String getName() {
        return this.getAttributeValue(ATTRIBUTE_NAME);
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
    public String getDisplayName() {
        return this.getAttributeValue(ATTRIBUTE_DISPLAY_NAME);
    }

    // SETTERS

    public void setCreatedby(final String attributeCreatedby) {
        this.setAttribute(ATTRIBUTE_CREATED_BY_USER_ID, attributeCreatedby);
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
    public void setCreatedByUserId(final APIID id) {
        this.setAttribute(ATTRIBUTE_CREATED_BY_USER_ID, id);
    }

    @Override
    public void setCreatedByUserId(final String id) {
        this.setAttribute(ATTRIBUTE_CREATED_BY_USER_ID, id);

    }

    @Override
    public void setId(final String id) {
        this.setAttribute("id", id);
    }

    @Override
    public void setId(final Long id) {
        this.setAttribute("id", id.toString());
    }

    @Override
    public void setName(final String name) {
        this.setAttribute(ATTRIBUTE_NAME, name);

    }

    @Override
    public void setDisplayName(final String displayName) {
        this.setAttribute(ATTRIBUTE_DISPLAY_NAME, displayName);

    }

    public void setDescription(final String description) {
        this.setAttribute(ATTRIBUTE_DESCRIPTION, description);

    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ItemDefinition getItemDefinition() {
        return new CategoryDefinition();
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
    public Date getLastUpdateDate() {
        return this.getAttributeValueAsDate(ATTRIBUTE_LAST_UPDATE_DATE);
    }
}
