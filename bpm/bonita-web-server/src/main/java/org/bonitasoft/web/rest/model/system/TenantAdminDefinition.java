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
package org.bonitasoft.web.rest.model.system;

import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * @author Julien Reboul
 */
public class TenantAdminDefinition extends ItemDefinition<TenantAdminItem> {

    public static final String TOKEN = "tenantAdmin";

    public static final String UNUSED_ID = "1";

    protected static final String API_URL = "../API/system/tenant";

    public static TenantAdminDefinition get() {
        return (TenantAdminDefinition) Definitions.get(TOKEN);
    }

    @Override
    protected String defineToken() {
        return TOKEN;
    }

    @Override
    protected String defineAPIUrl() {
        return API_URL;
    }

    @Override
    protected void defineAttributes() {
        createAttribute(TenantAdminItem.ATTRIBUTE_ID, ItemAttribute.TYPE.ITEM_ID);
        createAttribute(TenantAdminItem.ATTRIBUTE_IS_PAUSED, ItemAttribute.TYPE.BOOLEAN);
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(TenantAdminItem.ATTRIBUTE_ID);
    }

    @Override
    protected TenantAdminItem _createItem() {
        return new TenantAdminItem();
    }
}
