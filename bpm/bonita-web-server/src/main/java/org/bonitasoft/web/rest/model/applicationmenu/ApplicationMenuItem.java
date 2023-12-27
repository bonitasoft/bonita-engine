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
package org.bonitasoft.web.rest.model.applicationmenu;

import org.bonitasoft.web.toolkit.client.common.util.StringUtil;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasUniqueId;

/**
 * @author Julien Mege
 */
public class ApplicationMenuItem extends Item implements ItemHasUniqueId {

    public static final String ATTRIBUTE_DISPLAY_NAME = "displayName";

    public static final String ATTRIBUTE_APPLICATION_ID = "applicationId";

    public static final String ATTRIBUTE_APPLICATION_PAGE_ID = "applicationPageId";

    public static final String ATTRIBUTE_PARENT_MENU_ID = "parentMenuId";

    public static final String ATTRIBUTE_MENU_INDEX = "menuIndex";

    @Override
    public void setId(final String id) {
        this.setAttribute(ATTRIBUTE_ID, id);
    }

    @Override
    public void setId(final Long id) {
        this.setId(id.toString());
    }

    public void setDisplayName(final String displayName) {
        setAttribute(ATTRIBUTE_DISPLAY_NAME, displayName);
    }

    public String getDisplayName() {
        return getAttributeValue(ATTRIBUTE_DISPLAY_NAME);
    }

    public void setApplicationPageId(final String id) {
        this.setAttribute(ATTRIBUTE_APPLICATION_PAGE_ID, id);
    }

    public void setApplicationPageId(final Long id) {
        this.setAttribute(ATTRIBUTE_APPLICATION_PAGE_ID, id.toString());
    }

    public APIID getApplicationPageId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_APPLICATION_PAGE_ID);
    }

    public void setApplicationId(final String id) {
        this.setAttribute(ATTRIBUTE_APPLICATION_ID, id);
    }

    public void setApplicationId(final Long id) {
        this.setAttribute(ATTRIBUTE_APPLICATION_ID, id.toString());
    }

    public APIID getApplicationId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_APPLICATION_ID);
    }

    public void setParentMenuId(final String id) {
        this.setAttribute(ATTRIBUTE_PARENT_MENU_ID, id);
    }

    public void setParentMenuId(final Long id) {
        this.setAttribute(ATTRIBUTE_PARENT_MENU_ID, id.toString());
    }

    public APIID getParentMenuId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_PARENT_MENU_ID);
    }

    public void setMenuIndex(final int id) {
        this.setAttribute(ATTRIBUTE_MENU_INDEX, Integer.toString(id));
    }

    public Integer getMenuIndex() {
        return StringUtil.toInteger(getAttributeValue(ATTRIBUTE_MENU_INDEX));
    }

    @Override
    public ApplicationMenuDefinition getItemDefinition() {
        return ApplicationMenuDefinition.get();
    }
}
