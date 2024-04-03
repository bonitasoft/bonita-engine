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
public class I18nTranslationItem extends Item {

    public I18nTranslationItem() {
        super();
    }

    public I18nTranslationItem(final IItem item) {
        super(item);
    }

    public static final String ATTRIBUTE_KEY = "key";

    public static final String ATTRIBUTE_VALUE = "value";

    public I18nTranslationItem(final String key, final String value) {
        this();
        this.setAttribute(ATTRIBUTE_KEY, key);
        this.setAttribute(ATTRIBUTE_VALUE, value);
    }

    @Override
    public ItemDefinition getItemDefinition() {
        return new I18nTranslationDefinition();
    }

}
