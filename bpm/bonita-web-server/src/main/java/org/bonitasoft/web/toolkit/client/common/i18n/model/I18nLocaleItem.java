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

import org.bonitasoft.web.toolkit.client.data.item.IItem;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Séverin Moussel
 */
public class I18nLocaleItem extends Item {

    public static final String ATTRIBUTE_NAME = "name";
    public static final String ATTRIBUTE_LOCALE = "locale";

    public I18nLocaleItem() {
        super();
    }

    public I18nLocaleItem(final IItem item) {
        super(item);
    }

    public I18nLocaleItem(final String locale, final String name) {
        this();
        this.setAttribute(ATTRIBUTE_LOCALE, locale);
        this.setAttribute(ATTRIBUTE_NAME, name);
    }

    @Override
    public ItemDefinition<I18nLocaleItem> getItemDefinition() {
        return new I18nLocaleDefinition();
    }

    public String getName() {
        return getAttributeValue(ATTRIBUTE_NAME);
    }

    public String getLocale() {
        return getAttributeValue(ATTRIBUTE_LOCALE);
    }
}
