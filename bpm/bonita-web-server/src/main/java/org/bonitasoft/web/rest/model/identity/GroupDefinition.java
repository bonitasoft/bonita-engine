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
 * @author Yongtao Guo
 */
public class GroupDefinition extends ItemDefinition<GroupItem> {

    /**
     * Singleton
     */
    public static GroupDefinition get() {
        return (GroupDefinition) Definitions.get(TOKEN);
    }

    public static final String TOKEN = "group";

    /**
     * the URL of user resource
     */
    private static final String API_URL = "../API/identity/group";

    @Override
    public String defineToken() {
        return TOKEN;
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(GroupItem.ATTRIBUTE_ID);
    }

    @Override
    protected String defineAPIUrl() {
        return API_URL;
    }

    @Override
    protected void defineAttributes() {
        createAttribute(GroupItem.ATTRIBUTE_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(GroupItem.ATTRIBUTE_NAME, ItemAttribute.TYPE.STRING);
        createAttribute(GroupItem.ATTRIBUTE_DISPLAY_NAME, ItemAttribute.TYPE.STRING);
        createAttribute(GroupItem.ATTRIBUTE_DESCRIPTION, ItemAttribute.TYPE.TEXT);
        createAttribute(GroupItem.ATTRIBUTE_CREATION_DATE, ItemAttribute.TYPE.DATETIME);
        createAttribute(GroupItem.ATTRIBUTE_CREATED_BY_USER_ID, ItemAttribute.TYPE.STRING);
        createAttribute(GroupItem.ATTRIBUTE_LAST_UPDATE_DATE, ItemAttribute.TYPE.DATETIME);
        createAttribute(GroupItem.ATTRIBUTE_ICON, ItemAttribute.TYPE.IMAGE);
        createAttribute(GroupItem.ATTRIBUTE_PARENT_PATH, ItemAttribute.TYPE.STRING);
        createAttribute(GroupItem.ATTRIBUTE_PARENT_GROUP_ID, ItemAttribute.TYPE.STRING);
    }

    @Override
    public GroupItem _createItem() {
        return new GroupItem();
    }
}
