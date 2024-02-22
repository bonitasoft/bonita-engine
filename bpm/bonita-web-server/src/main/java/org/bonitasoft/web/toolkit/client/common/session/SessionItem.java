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

import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Julien Mege
 */
public class SessionItem extends Item {

    public SessionItem() {
        super();
    }

    public static final String ATTRIBUTE_SESSIONID = "session_id";

    public static final String ATTRIBUTE_USERID = "user_id";

    public static final String ATTRIBUTE_FIRSTNAME = "first_name";

    public static final String ATTRIBUTE_LASTNAME = "last_name";

    public static final String ATTRIBUTE_USERNAME = "user_name";

    public static final String ATTRIBUTE_ICON = "icon";

    public static final String ATTRIBUTE_IS_TECHNICAL_USER = "is_technical_user";

    public static final String ATTRIBUTE_VERSION = "version";

    public static final String ATTRIBUTE_BRANDING_VERSION = "branding_version";

    public static final String ATTRIBUTE_BRANDING_VERSION_WITH_DATE = "branding_version_with_date";

    public static final String ATTRIBUTE_CONF = "conf";

    public static final String ATTRIBUTE_COPYRIGHT = "copyright";

    public static final String ATTRIBUTE_IS_GUEST_USER = "is_guest_user";

    @Override
    public ItemDefinition getItemDefinition() {
        return new SessionDefinition();
    }

}
