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
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * User definition
 *
 * @author Séverin Moussel
 */
public class MembershipDefinition extends ItemDefinition {

    /**
     * Singleton
     */
    public static MembershipDefinition get() {
        return (MembershipDefinition) Definitions.get(TOKEN);
    }

    public static final String TOKEN = "membership";

    /**
     * the URL of user resource
     */
    private static final String API_URL = "../API/identity/membership";

    @Override
    public String defineToken() {
        return TOKEN;
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(MembershipItem.ATTRIBUTE_USER_ID, MembershipItem.ATTRIBUTE_GROUP_ID,
                MembershipItem.ATTRIBUTE_ROLE_ID);
    }

    @Override
    protected String defineAPIUrl() {
        return API_URL;
    }

    @Override
    protected void defineAttributes() {
        createAttribute(MembershipItem.ATTRIBUTE_USER_ID, ItemAttribute.TYPE.ITEM_ID)
                .isMandatory();

        createAttribute(MembershipItem.ATTRIBUTE_GROUP_ID, ItemAttribute.TYPE.ITEM_ID)
                .isMandatory();

        createAttribute(MembershipItem.ATTRIBUTE_ROLE_ID, ItemAttribute.TYPE.ITEM_ID)
                .isMandatory();

        createAttribute(MembershipItem.ATTRIBUTE_ASSIGNED_BY_USER_ID, ItemAttribute.TYPE.ITEM_ID);

        createAttribute(MembershipItem.ATTRIBUTE_ASSIGNED_DATE, ItemAttribute.TYPE.DATETIME);
    }

    @Override
    public IItem _createItem() {
        return new MembershipItem();
    }

}
