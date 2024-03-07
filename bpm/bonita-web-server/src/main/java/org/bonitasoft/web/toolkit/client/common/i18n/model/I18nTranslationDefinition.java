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
import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;
import org.bonitasoft.web.toolkit.client.data.item.attribute.ItemAttribute;

/**
 * @author Séverin Moussel
 */
public class I18nTranslationDefinition extends ItemDefinition {

    /**
     * Singleton
     */
    public static I18nTranslationDefinition get() {
        return (I18nTranslationDefinition) Definitions.get(TOKEN);
    }

    public static final String TOKEN = "i18ntranslation";

    /**
     * the URL of i18nlocale resource
     */
    private static final String API_URL = "../API/system/i18ntranslation";

    private static final String PLURAL_RESOURCES_URL = "../API/system/i18ntranslation";

    @Override
    public String defineToken() {
        return TOKEN;
    }

    @Override
    protected void definePrimaryKeys() {
        setPrimaryKeys(I18nTranslationItem.ATTRIBUTE_KEY);
    }

    @Override
    protected String defineAPIUrl() {
        return API_URL;
    }

    @Override
    protected void defineAttributes() {
        createAttribute(I18nTranslationItem.ATTRIBUTE_KEY, ItemAttribute.TYPE.TEXT);
        createAttribute(I18nTranslationItem.ATTRIBUTE_VALUE, ItemAttribute.TYPE.TEXT);
    }

    @Override
    protected IItem _createItem() {
        return new I18nTranslationItem();
    }
}
