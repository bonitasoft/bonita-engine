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
package org.bonitasoft.web.toolkit.client.common.i18n.model;

import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * @author Séverin Moussel
 */
public class I18nLocaleDefinition extends ItemDefinition<I18nLocaleItem> {

    /**
     * Singleton
     */
    public static I18nLocaleDefinition get() {
        return (I18nLocaleDefinition) Definitions.get(TOKEN);
    }

    public static final String TOKEN = "i18nlocale";

    /**
     * the URL of i18nlocale resource
     */
    private static final String API_URL = "../API/system/i18nlocale";

    @Override
    public String defineToken() {
        return TOKEN;
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(I18nLocaleItem.ATTRIBUTE_LOCALE);
    }

    @Override
    protected String defineAPIUrl() {
        return API_URL;
    }

    @Override
    protected void defineAttributes() {
        createAttribute(I18nLocaleItem.ATTRIBUTE_LOCALE, ItemAttribute.TYPE.STRING);
        createAttribute(I18nLocaleItem.ATTRIBUTE_NAME, ItemAttribute.TYPE.STRING);
    }

    @Override
    protected I18nLocaleItem _createItem() {
        return new I18nLocaleItem();
    }

}
