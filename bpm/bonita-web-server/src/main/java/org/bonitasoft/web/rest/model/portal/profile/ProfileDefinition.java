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
package org.bonitasoft.web.rest.model.portal.profile;

import static org.bonitasoft.web.toolkit.client.data.item.template.ItemHasIcon.ATTRIBUTE_ICON;
import static org.bonitasoft.web.toolkit.client.data.item.template.ItemHasUniqueId.ATTRIBUTE_ID;

import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * @author Julien Mege
 * @author Séverin Moussel
 */
public class ProfileDefinition extends ItemDefinition<ProfileItem> {

    /**
     * Singleton
     */
    public static ProfileDefinition get() {
        return (ProfileDefinition) Definitions.get(TOKEN);
    }

    public static final String TOKEN = "profile";

    /**
     * the URL of profile resource
     */
    protected static final String API_URL = "../API/portal/profile";

    @Override
    public String defineToken() {
        return TOKEN;
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(ATTRIBUTE_ID);
    }

    @Override
    protected String defineAPIUrl() {
        return API_URL;
    }

    @Override
    protected void defineAttributes() {
        createAttribute(ATTRIBUTE_ID, ItemAttribute.TYPE.ITEM_ID);

        createAttribute(ProfileItem.ATTRIBUTE_NAME, ItemAttribute.TYPE.STRING)
                .isMandatory(true);

        createAttribute(ProfileItem.ATTRIBUTE_DESCRIPTION, ItemAttribute.TYPE.TEXT);

        createAttribute(ATTRIBUTE_ICON, ItemAttribute.TYPE.IMAGE);

        createAttribute(ProfileItem.ATTRIBUTE_UPDATED_BY_USER_ID, ItemAttribute.TYPE.ITEM_ID);

        createAttribute(ProfileItem.ATTRIBUTE_LAST_UPDATE_DATE, ItemAttribute.TYPE.DATETIME);

        createAttribute(ProfileItem.ATTRIBUTE_CREATED_BY_USER_ID, ItemAttribute.TYPE.ITEM_ID);

        createAttribute(ProfileItem.ATTRIBUTE_CREATION_DATE, ItemAttribute.TYPE.DATETIME);
    }

    @Override
    public ProfileItem _createItem() {
        return new ProfileItem();
    }
}
