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

import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * @author Julien Mege
 */
public class ApplicationMenuDefinition extends ItemDefinition<ApplicationMenuItem> {

    public static final String TOKEN = "applicationmenu";

    @Override
    protected String defineToken() {
        return TOKEN;
    }

    @Override
    protected String defineAPIUrl() {
        return "../API/living/application-menu";
    }

    @Override
    protected void defineAttributes() {
        createAttribute(ApplicationMenuItem.ATTRIBUTE_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(ApplicationMenuItem.ATTRIBUTE_APPLICATION_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(ApplicationMenuItem.ATTRIBUTE_APPLICATION_PAGE_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(ApplicationMenuItem.ATTRIBUTE_PARENT_MENU_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(ApplicationMenuItem.ATTRIBUTE_MENU_INDEX, ItemAttribute.TYPE.INTEGER);
        createAttribute(ApplicationMenuItem.ATTRIBUTE_DISPLAY_NAME, ItemAttribute.TYPE.STRING);
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(ApplicationMenuItem.ATTRIBUTE_ID);
    }

    @Override
    protected ApplicationMenuItem _createItem() {
        return new ApplicationMenuItem();
    }

    public static ApplicationMenuDefinition get() {
        return (ApplicationMenuDefinition) Definitions.get(TOKEN);
    }

}
