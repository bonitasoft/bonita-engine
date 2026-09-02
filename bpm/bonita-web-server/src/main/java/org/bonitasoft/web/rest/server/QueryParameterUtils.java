/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities to handle query parameters.
 * Shared between Restlet and SpringMVC controllers.
 */
public class QueryParameterUtils {

    /**
     * Builds a map where keys are Engine constants defining filter keys, and values are values corresponding to those
     * keys.
     *
     * @param parameters The filters passed as string according to the form ["key1=value1", "key2=value2"].
     * @return a map of the form: [key1: value1, key2: value2].
     */
    public static Map<String, String> parseFilters(final List<String> parameters) {
        if (parameters == null) {
            return null;
        }
        final Map<String, String> results = new HashMap<>();
        for (final String parameter : parameters) {
            final String[] split = parameter.split("=");
            if (split.length < 1) {
                continue;
            }
            if (split.length < 2) {
                results.put(split[0], null);
            } else {
                results.put(split[0], parameter.substring(split[0].length() + 1));
            }
        }
        return results;
    }

}
