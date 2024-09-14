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
package org.bonitasoft.web.rest.model.application;

import org.bonitasoft.web.rest.model.portal.page.PageItem;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasIcon;
import org.bonitasoft.web.toolkit.client.data.item.template.ItemHasUniqueId;

/**
 * Contains the meta information of a legacy Bonita Living Application for the REST API.
 */
public class ApplicationItem extends AbstractApplicationItem implements ItemHasUniqueId, ItemHasIcon {

    /**
     * @deprecated since 7.13.0, see {@link #getIconPath()} & {@link #setIconPath(String)}
     */
    @Deprecated(since = "7.13.0")
    public static final String ATTRIBUTE_ICON_PATH = "iconPath";

    public static final String ATTRIBUTE_HOME_PAGE_ID = "homePageId";

    public static final String ATTRIBUTE_LAYOUT_ID = "layoutId";

    public static final String ATTRIBUTE_THEME_ID = "themeId";

    public ApplicationItem() {
        super();
    }

    public ApplicationItem(final IItem item) {
        super(item);
    }

    @Override
    public boolean isLink() {
        return false;
    }

    /**
     * @deprecated since 7.13, use {@link #getIcon()} instead
     */
    @Deprecated(since = "7.13.0")
    public String getIconPath() {
        return getAttributeValue(ATTRIBUTE_ICON_PATH);
    }

    /**
     * @deprecated since 7.13, use {@link #setIcon(String)} instead
     */
    @Deprecated(since = "7.13.0")
    public void setIconPath(final String iconPath) {
        setAttribute(ATTRIBUTE_ICON_PATH, iconPath);
    }

    public APIID getHomePageId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_HOME_PAGE_ID);
    }

    public void setHomePageId(final Long homePageId) {
        setAttribute(ATTRIBUTE_HOME_PAGE_ID, homePageId);
    }

    public APIID getLayoutId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_LAYOUT_ID);
    }

    public void setLayoutId(final Long layoutId) {
        setAttribute(ATTRIBUTE_LAYOUT_ID, layoutId);
    }

    public APIID getThemeId() {
        return getAttributeValueAsAPIID(ATTRIBUTE_THEME_ID);
    }

    public void setThemeId(final Long themeId) {
        setAttribute(ATTRIBUTE_THEME_ID, themeId);
    }

    public PageItem getLayout() {
        return (PageItem) getDeploy(ATTRIBUTE_LAYOUT_ID);
    }

    public PageItem getTheme() {
        return (PageItem) getDeploy(ATTRIBUTE_THEME_ID);
    }

}
