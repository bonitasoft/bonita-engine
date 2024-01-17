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
package org.bonitasoft.web.toolkit.client.ui.utils;

import java.util.List;

/**
 * @author Séverin Moussel
 */
public class ListUtils {

    public static void removeFromListByClass(final List<?> list, final String className) {
        removeFromListByClass(list, className, false);
    }

    public static void removeFromListByClass(final List<?> list, final String className, final boolean firstOnly) {
        for (int i = 0; i < list.size(); i++) {
            final Object o = list.get(i);
            if (o.getClass().getName().equals(className)) {
                list.remove(i);
                if (firstOnly) {
                    return;
                }
                i--;
            }
        }
    }

    public static Object getFromListByClass(final List<?> list, final String className) {
        for (final Object o : list) {
            if (o.getClass().getName().equals(className)) {
                return o;
            }
        }

        return null;
    }

    public static String join(final List<?> list, final String separator) {
        return join(list, separator, separator, "", "");
    }

    public static String join(final List<?> list, final String separator, final String lastSeparator,
            final String itemPrefix, final String itemSuffix) {
        if (list == null || list.isEmpty()) {
            return "";
        }

        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            final Object item = list.get(i);
            if (item != null) {
                if (i == list.size() - 1 && list.size() > 1) {
                    result.append(lastSeparator);
                } else if (i > 0) {
                    result.append(separator);
                }
                if (itemPrefix != null) {
                    result.append(itemPrefix);
                }
                result.append(item);
                if (itemSuffix != null) {
                    result.append(itemSuffix);
                }
            }
        }

        return result.toString();
    }

}
