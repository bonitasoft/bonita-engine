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

import org.bonitasoft.web.rest.server.datastore.organization.Avatars;
import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;
import org.bonitasoft.web.toolkit.client.data.item.attribute.validator.FileIsImageOrServletPathValidator;

/**
 * @author Yongtao Guo
 */
public class RoleDefinition extends ItemDefinition<RoleItem> {

    /**
     * Singleton
     */
    public static RoleDefinition get() {
        return (RoleDefinition) Definitions.get(TOKEN);
    }

    public static final String TOKEN = "role";

    /**
     * the URL of user resource
     */
    private static final String API_URL = "../API/identity/role";

    @Override
    public String defineToken() {
        return TOKEN;
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(RoleItem.ATTRIBUTE_ID);
    }

    @Override
    protected String defineAPIUrl() {
        return API_URL;
    }

    @Override
    protected void defineAttributes() {
        createAttribute(RoleItem.ATTRIBUTE_ID, ItemAttribute.TYPE.ITEM_ID);

        createAttribute(RoleItem.ATTRIBUTE_NAME, ItemAttribute.TYPE.STRING);

        createAttribute(GroupItem.ATTRIBUTE_DISPLAY_NAME, ItemAttribute.TYPE.STRING);

        createAttribute(RoleItem.ATTRIBUTE_DESCRIPTION, ItemAttribute.TYPE.TEXT);

        createAttribute(RoleItem.ATTRIBUTE_CREATION_DATE, ItemAttribute.TYPE.DATETIME);

        createAttribute(RoleItem.ATTRIBUTE_CREATED_BY_USER_ID, ItemAttribute.TYPE.ITEM_ID);

        createAttribute(RoleItem.ATTRIBUTE_LAST_UPDATE_DATE, ItemAttribute.TYPE.DATETIME);

        createAttribute(RoleItem.ATTRIBUTE_ICON, ItemAttribute.TYPE.STRING)
                .addValidator(new FileIsImageOrServletPathValidator(Avatars.PATH));
    }

    @Override
    public RoleItem _createItem() {
        return new RoleItem();
    }
}
