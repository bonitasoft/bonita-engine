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

import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationDefinition extends ItemDefinition<ApplicationItem> {

    public static final String TOKEN = "application";

    @Override
    protected String defineToken() {
        return TOKEN;
    }

    @Override
    protected String defineAPIUrl() {
        return "../API/living/application";
    }

    @Override
    protected void defineAttributes() {
        createAttribute(ApplicationItem.ATTRIBUTE_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(ApplicationItem.ATTRIBUTE_TOKEN, ItemAttribute.TYPE.STRING);
        createAttribute(ApplicationItem.ATTRIBUTE_DISPLAY_NAME, ItemAttribute.TYPE.STRING);
        createAttribute(ApplicationItem.ATTRIBUTE_PROFILE_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(ApplicationItem.ATTRIBUTE_HOME_PAGE_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(ApplicationItem.ATTRIBUTE_LAYOUT_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(ApplicationItem.ATTRIBUTE_THEME_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(ApplicationItem.ATTRIBUTE_VERSION, ItemAttribute.TYPE.STRING);
        createAttribute(ApplicationItem.ATTRIBUTE_DESCRIPTION, ItemAttribute.TYPE.TEXT);
        createAttribute(ApplicationItem.ATTRIBUTE_ICON, ItemAttribute.TYPE.IMAGE);
        createAttribute(ApplicationItem.ATTRIBUTE_CREATION_DATE, ItemAttribute.TYPE.STRING);
        createAttribute(ApplicationItem.ATTRIBUTE_CREATED_BY, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(ApplicationItem.ATTRIBUTE_LAST_UPDATE_DATE, ItemAttribute.TYPE.STRING);
        createAttribute(ApplicationItem.ATTRIBUTE_UPDATED_BY, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(ApplicationItem.ATTRIBUTE_STATE, ItemAttribute.TYPE.STRING);
        createAttribute(ApplicationItem.ATTRIBUTE_VISIBILITY, ItemAttribute.TYPE.STRING);
        createAttribute(ApplicationItem.ATTRIBUTE_EDITABLE, ItemAttribute.TYPE.BOOLEAN);
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(ApplicationItem.ATTRIBUTE_ID);
    }

    @Override
    protected ApplicationItem _createItem() {
        return new ApplicationItem();
    }

    public static ApplicationDefinition get() {
        return (ApplicationDefinition) Definitions.get(TOKEN);
    }

}
