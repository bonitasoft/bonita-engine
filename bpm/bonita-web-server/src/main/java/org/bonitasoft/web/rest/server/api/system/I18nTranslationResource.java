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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.web.rest.server.api.resource.CommonResource;
import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n;
import org.restlet.data.Status;
import org.restlet.resource.Get;

/**
 * @author Julien Mege
 */
public class I18nTranslationResource extends CommonResource {

    private final I18n i18n;

    public I18nTranslationResource(I18n i18n) {
        this.i18n = i18n;
    }

    @Get("json")
    public List<Translation> getI18nTranslation() {
        List<Translation> items = new ArrayList<>();

        String locale = getLocale();
        if (locale == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Request should contains 'locale' parameter.");
            return null;
        }
        Map<String, String> translations = i18n.getLocale(AbstractI18n.stringToLocale(locale));

        for (final Map.Entry<String, String> entry : translations.entrySet()) {
            items.add(new Translation(entry.getKey(), entry.getValue()));
        }

        return items;
    }

    private String getLocale() {
        return getSearchFilters() != null ? getSearchFilters().get("locale") : null;
    }

}
