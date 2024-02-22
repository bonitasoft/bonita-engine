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

import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * @author Vincent Elcrin
 */
public class CustomUserInfoDefinitionDefinition extends ItemDefinition<CustomUserInfoDefinitionItem> {

    public static final String TOKEN = "customuserinfo/definitions";

    @Override
    protected String defineToken() {
        return TOKEN;
    }

    @Override
    protected String defineAPIUrl() {
        return "../API/customuserinfo/definition";
    }

    @Override
    protected void defineAttributes() {
        createAttribute(CustomUserInfoDefinitionItem.ATTRIBUTE_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(CustomUserInfoDefinitionItem.ATTRIBUTE_NAME, ItemAttribute.TYPE.STRING);
        createAttribute(CustomUserInfoDefinitionItem.ATTRIBUTE_DESCRIPTION, ItemAttribute.TYPE.TEXT);
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(CustomUserInfoDefinitionItem.ATTRIBUTE_ID);
    }

    @Override
    protected CustomUserInfoDefinitionItem _createItem() {
        return new CustomUserInfoDefinitionItem();
    }

    public static CustomUserInfoDefinitionDefinition get() {
        return (CustomUserInfoDefinitionDefinition) Definitions.get(TOKEN);
    }
}
