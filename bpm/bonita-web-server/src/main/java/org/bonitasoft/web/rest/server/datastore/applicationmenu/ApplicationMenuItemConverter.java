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
package org.bonitasoft.web.rest.server.datastore.applicationmenu;

import java.util.Map;

import org.bonitasoft.engine.business.application.ApplicationMenu;
import org.bonitasoft.engine.business.application.ApplicationMenuCreator;
import org.bonitasoft.engine.business.application.ApplicationMenuUpdater;
import org.bonitasoft.web.rest.model.applicationmenu.ApplicationMenuItem;

/**
 * @author Julien Mege
 */
public class ApplicationMenuItemConverter {

    public ApplicationMenuItem toApplicationMenuItem(final ApplicationMenu applicationMenu) {
        final ApplicationMenuItem item = new ApplicationMenuItem();
        item.setId(applicationMenu.getId());
        item.setApplicationId(applicationMenu.getApplicationId());
        item.setDisplayName(applicationMenu.getDisplayName());
        if (applicationMenu.getApplicationPageId() != null) {
            item.setApplicationPageId(applicationMenu.getApplicationPageId());
        } else {
            item.setApplicationPageId("-1");
        }
        if (applicationMenu.getParentId() != null) {
            item.setParentMenuId(applicationMenu.getParentId());
        } else {
            item.setParentMenuId("-1");
        }
        item.setMenuIndex(applicationMenu.getIndex());
        return item;
    }

    public ApplicationMenuCreator toApplicationMenuCreator(final ApplicationMenuItem item) {

        Long applicationId = null;
        if (item.getApplicationId() != null && item.getApplicationId().isValidLongID()) {
            applicationId = item.getApplicationId().toLong();
        }
        Long applicationPageId = null;
        if (item.getApplicationPageId() != null && item.getApplicationPageId().isValidLongID()) {
            applicationPageId = item.getApplicationPageId().toLong();
        }

        final ApplicationMenuCreator menuCreator = new ApplicationMenuCreator(applicationId, item.getDisplayName(),
                applicationPageId);

        if (item.getParentMenuId() != null && item.getParentMenuId().isValidLongID()) {
            menuCreator.setParentId(item.getParentMenuId().toLong());
        }

        return menuCreator;
    }

    public ApplicationMenuUpdater toApplicationMenuUpdater(final Map<String, String> attributes) {
        final ApplicationMenuUpdater applicationMenuUpdater = new ApplicationMenuUpdater();

        if (attributes.containsKey(ApplicationMenuItem.ATTRIBUTE_APPLICATION_PAGE_ID)) {
            Long appPageId = Long.parseLong(attributes.get(ApplicationMenuItem.ATTRIBUTE_APPLICATION_PAGE_ID));
            if (appPageId == -1) {
                appPageId = null;
            }
            applicationMenuUpdater.setApplicationPageId(appPageId);
        }
        if (attributes.containsKey(ApplicationMenuItem.ATTRIBUTE_DISPLAY_NAME)) {
            applicationMenuUpdater.setDisplayName(attributes.get(ApplicationMenuItem.ATTRIBUTE_DISPLAY_NAME));
        }
        if (attributes.containsKey(ApplicationMenuItem.ATTRIBUTE_MENU_INDEX)) {
            applicationMenuUpdater.setIndex(Integer.parseInt(attributes.get(ApplicationMenuItem.ATTRIBUTE_MENU_INDEX)));
        }
        if (attributes.containsKey(ApplicationMenuItem.ATTRIBUTE_PARENT_MENU_ID)) {
            Long parentMenuId = Long.parseLong(attributes.get(ApplicationMenuItem.ATTRIBUTE_PARENT_MENU_ID));
            if (parentMenuId == -1) {
                parentMenuId = null;
            }
            applicationMenuUpdater.setParentId(parentMenuId);
        }

        return applicationMenuUpdater;
    }

}
