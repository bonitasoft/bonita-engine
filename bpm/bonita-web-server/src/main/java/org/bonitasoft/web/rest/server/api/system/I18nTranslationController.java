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

import static org.bonitasoft.web.rest.server.QueryParameterUtils.parseFilters;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.web.rest.server.api.AbstractRESTController;
import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Julien Mege
 */
@RestController
@RequestMapping("/API/system/i18ntranslation")
public class I18nTranslationController extends AbstractRESTController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Translation> getI18nTranslation(@RequestParam(value = "f", required = false) List<String> filters) {
        Map<String, String> filterMap = parseFilters(filters);
        String locale = filterMap != null ? filterMap.get("locale") : null;

        if (locale == null) {
            throw new IllegalArgumentException("Request should contain 'locale' parameter.");
        }

        return getI18n().getLocale(AbstractI18n.stringToLocale(locale))
                .entrySet().stream()
                .map(entry -> new Translation(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    protected I18n getI18n() {
        return I18n.getInstance();
    }

}
