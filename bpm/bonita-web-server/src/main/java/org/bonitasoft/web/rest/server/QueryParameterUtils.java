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

import org.springframework.lang.Nullable;

/**
 * Utilities to handle query parameters.
 * Shared across Spring MVC controllers.
 */
public class QueryParameterUtils {

    /**
     * Builds a map where keys are Engine constants defining filter keys, and values are values corresponding to those
     * keys.
     *
     * @param parameters the filters passed as string according to the form
     *        {@code ["key1=value1", "key2=value2"]}
     * @return a map of the form {@code {key1: value1, key2: value2}}, or {@code null} if
     *         {@code parameters} is {@code null}
     */
    public static Map<String, String> parseFilters(final List<String> parameters) {
        if (parameters == null) {
            return null;
        }
        final Map<String, String> results = new HashMap<>();
        for (final String parameter : parameters) {
            final String[] split = parameter.split("=");
            if (split.length == 0) {
                // "=".split("=") returns an empty array — skip filters with no key
                continue;
            }
            if (split.length == 1) {
                // filter with no value (e.g. "key1" or "key1="), put null as value in the map
                results.put(split[0], null);
            } else {
                // valid filter, put key and value in the map (e.g. "key1=value1")
                results.put(split[0], parameter.substring(split[0].length() + 1));
            }
        }
        return results;
    }

    /**
     * Extracts a string filter value from the filter list.
     *
     * @param filters the list of filter strings in format {@code ["key1=value1", "key2=value2"]}
     * @param filterName the name of the filter to extract
     * @return the filter value, or {@code null} if the filter is not found, {@code filters} is {@code null},
     *         or the value is empty
     */
    public static @Nullable String extractStringFilter(List<String> filters, String filterName) {
        if (filters == null) {
            return null;
        }
        for (String filter : filters) {
            String filterKey = filterName + "=";
            if (filter.startsWith(filterKey)) {
                String value = filter.substring(filterKey.length());
                return value.isEmpty() ? null : value;
            }
        }
        return null;
    }

    /**
     * Extracts a mandatory string filter value from the filter list.
     *
     * @param filters the list of filter strings in format {@code ["key1=value1", "key2=value2"]}
     * @param filterName the name of the filter to extract
     * @return the filter value
     * @throws IllegalArgumentException if the filter is not found or its value is empty
     */
    public static String extractMandatoryStringFilter(List<String> filters, String filterName) {
        String value = extractStringFilter(filters, filterName);
        if (value == null) {
            throw new IllegalArgumentException("filter " + filterName + " is mandatory");
        }
        return value;
    }

    /**
     * Extracts an optional numeric {@link Long} filter value from the filter list.
     *
     * @param filters the list of filter strings in format {@code ["key1=value1", "key2=value2"]}
     * @param filterName the name of the filter to extract
     * @return the filter value as a {@link Long}, or {@code null} if the filter is not found or {@code filters}
     *         is {@code null}
     * @throws IllegalArgumentException if the filter value is not a valid number
     */
    public static @Nullable Long extractLongFilter(List<String> filters, String filterName) {
        String value = extractStringFilter(filters, filterName);
        try {
            return value == null ? null : Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("filter " + filterName + " must be a number");
        }
    }

    /**
     * Extracts a mandatory numeric {@code long} filter value from the filter list.
     *
     * @param filters the list of filter strings in format {@code ["key1=value1", "key2=value2"]}
     * @param filterName the name of the filter to extract
     * @return the filter value as a {@code long}
     * @throws IllegalArgumentException if the filter is not found, its value is empty, or not a valid number
     */
    public static long extractMandatoryLongFilter(List<String> filters, String filterName) {
        Long value = extractLongFilter(filters, filterName);
        if (value == null) {
            throw new IllegalArgumentException("filter " + filterName + " is mandatory");
        }
        return value;
    }

}
