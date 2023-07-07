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
package org.bonitasoft.web.rest.server.api.system;

import java.util.LinkedList;
import java.util.Map;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.web.rest.server.framework.API;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.i18n.model.I18nLocaleDefinition;
import org.bonitasoft.web.toolkit.client.common.i18n.model.I18nLocaleItem;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Julien Mege
 */
// TODO add url pathParameter to load available locales by application (portal, platform, ...)
public class APII18nLocale extends API<I18nLocaleItem> {

    private static final String DEFAULT_APPLICATION = "portal";

    @Override
    protected ItemDefinition<I18nLocaleItem> defineItemDefinition() {
        return I18nLocaleDefinition.get();
    }

    @Override
    public ItemSearchResult<I18nLocaleItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {

        final Map<String, String> availableLocales = I18n.getInstance().getAvailableLocalesFor(DEFAULT_APPLICATION);
        final LinkedList<I18nLocaleItem> items = new LinkedList<>();

        for (final String locale : availableLocales.keySet()) {
            final String name = availableLocales.get(locale);

            if (search == null || search.length() == 0 || locale.contains(search) || name.contains(search)) {
                items.add(new I18nLocaleItem(locale, availableLocales.get(locale)));
            }
        }

        return new ItemSearchResult<>(page * resultsByPage, resultsByPage, items.size(), items);

    }

    @Override
    public String defineDefaultSearchOrder() {
        return "";
    }

}
