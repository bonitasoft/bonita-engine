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
package org.bonitasoft.web.toolkit.client.common.session;

import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * User definition
 *
 * @author Julien Mege
 */
public class SessionDefinition extends ItemDefinition<SessionItem> {

    /**
     * Singleton
     */
    public static SessionDefinition get() {
        return (SessionDefinition) Definitions.get(TOKEN);
    }

    public static final String TOKEN = "session";

    /**
     * the URL of user resource
     */
    private static final String API_URL = "../API/system/session";

    @Override
    public String defineToken() {
        return TOKEN;
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(SessionItem.ATTRIBUTE_USERID);
    }

    @Override
    protected String defineAPIUrl() {
        return API_URL;
    }

    @Override
    protected void defineAttributes() {
        createAttribute(SessionItem.ATTRIBUTE_FIRSTNAME, ItemAttribute.TYPE.STRING);
        createAttribute(SessionItem.ATTRIBUTE_LASTNAME, ItemAttribute.TYPE.STRING);
        createAttribute(SessionItem.ATTRIBUTE_ICON, ItemAttribute.TYPE.IMAGE);
        createAttribute(SessionItem.ATTRIBUTE_USERID, ItemAttribute.TYPE.STRING);
        createAttribute(SessionItem.ATTRIBUTE_USERNAME, ItemAttribute.TYPE.STRING);
        createAttribute(SessionItem.ATTRIBUTE_CONF, ItemAttribute.TYPE.STRING);
    }

    @Override
    public SessionItem _createItem() {
        return new SessionItem();
    }
}
