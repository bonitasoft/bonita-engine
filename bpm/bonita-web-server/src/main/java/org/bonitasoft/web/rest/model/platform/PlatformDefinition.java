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
package org.bonitasoft.web.rest.model.platform;

import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * @author Zhiheng Yang
 */
public class PlatformDefinition extends ItemDefinition<PlatformItem> {

    /**
     * Singleton
     */
    public static PlatformDefinition get() {
        return (PlatformDefinition) Definitions.get(TOKEN);
    }

    public final static String TOKEN = "platformInfo";

    private static final String API_URL = "../API/platform/platform";

    @Override
    public String defineToken() {
        return TOKEN;
    }

    @Override
    protected String defineAPIUrl() {
        return API_URL;
    }

    @Override
    protected void defineAttributes() {
        createAttribute(PlatformItem.ATTRIBUTE_CREATED_DATE, ItemAttribute.TYPE.STRING);
        createAttribute(PlatformItem.ATTRIBUTE_INIT_VERSION, ItemAttribute.TYPE.STRING);
        createAttribute(PlatformItem.ATTRIBUTE_PRE_VERSION, ItemAttribute.TYPE.STRING);
        createAttribute(PlatformItem.ATTRIBUTE_VERSION, ItemAttribute.TYPE.STRING);
        createAttribute(PlatformItem.ATTRIBUTE_CREATEDBY, ItemAttribute.TYPE.STRING);
        createAttribute(PlatformItem.ATTRIBUTE_STATE, ItemAttribute.TYPE.STRING);
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(PlatformItem.ATTRIBUTE_CREATED_DATE);
    }

    @Override
    protected PlatformItem _createItem() {
        return new PlatformItem();
    }
}
