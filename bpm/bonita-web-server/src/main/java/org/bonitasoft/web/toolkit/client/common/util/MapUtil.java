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
package org.bonitasoft.web.toolkit.client.common.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.web.toolkit.client.common.texttemplate.Arg;

public class MapUtil {

    /**
     * Get a value in a Map<String, String>.
     *
     * @param map
     *        The map to search in
     * @param key
     *        The key of the element to get.
     * @param defaultValue
     *        The value to return if the element at the defined key is not set.
     * @return This method returns the value corresponding or the defaultValue if the element doesn't exist.
     */
    public static String getValue(final Map<String, String> map, final String key, final String defaultValue) {
        if (!map.containsKey(key)) {
            return defaultValue;
        }
        return map.get(key);
    }

    /**
     * Get a value in a Map<String, String> and convert it to a long.
     *
     * @param map
     *        The map to search in
     * @param key
     *        The key of the element to get.
     * @return This method returns the Long value corresponding or NULL if the element doesn't exist or is empty.
     */
    public static Long getValueAsLong(final Map<String, String> map, final String key) throws NumberFormatException {
        final String value = map.get(key);
        if (StringUtil.isBlank(value)) {
            return null;
        }

        return Long.valueOf(value);
    }

    /**
     * Get a value in a Map<String, String> and convert it to an integer.
     *
     * @param map
     *        The map to search in
     * @param key
     *        The key of the element to get.
     * @return This method returns the long value corresponding or NULL if the element doesn't exist or is empty.
     */
    public static Boolean getValueAsBoolean(final Map<String, String> map, final String key)
            throws IllegalArgumentException {
        final String value = map.get(key);
        if (StringUtil.isBlank(value)) {
            return null;
        }

        return StringUtil.toBoolean(value);
    }

    /**
     * Check if a map entry is blank (not set, NULL or empty String).
     *
     * @param map
     *        The map to check
     * @param key
     *        The key to check
     * @return This method returns TRUE if the key is not set OR null OR an empty String, otherwise FALSE.
     */
    public static boolean isBlank(final Map<String, String> map, final String key) {
        return !map.containsKey(key) || StringUtil.isBlank(map.get(key));
    }

    /**
     * Remove a map entry if its value is blank (NULL or empty String)
     *
     * @param map
     *        The map to check
     * @param key
     *        The key to check
     * @return This method returns TRUE if the key is not set OR has been removed, otherwise FALSE.
     */
    public static boolean removeIfBlank(final Map<String, String> map, final String key) {
        if (isBlank(map, key)) {
            map.remove(key);
            return true;
        }
        return false;
    }

    public static Map<String, String> asMap(final Arg... args) {
        final Map<String, String> results = new HashMap<>();
        for (final Arg arg : args) {
            results.put(arg.getName(), arg.getValue());
        }

        return results;
    }

    public static abstract class ForEach<K, V> {

        protected abstract void apply(K key, V value);
    }

    public static <K, V> void iterate(Map<K, V> map, ForEach<K, V> modifier) {
        Iterator<Entry<K, V>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Entry<K, V> entry = it.next();
            modifier.apply(entry.getKey(), entry.getValue());
        }
    }

    public static String getMandatory(Map<String, String> map, String key) {
        String value = map.get(key);
        if (value == null) {
            throw new RuntimeException("Can't find value corresponding to " + key);
        }
        return value;
    }
}
