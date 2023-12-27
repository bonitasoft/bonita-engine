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
package org.bonitasoft.console.common.server.utils;

/**
 * @author Vincent Elcrin
 */
public class UrlValue {

    private final String value;

    public UrlValue(String value) {
        this.value = value;
    }

    public UrlValue(String... values) {
        this(merge(values));
    }

    private static String merge(String... values) {
        final StringBuilder value = new StringBuilder();
        for (final String parameterValue : values) {
            if (value.length() > 0) {
                value.append(",");
            }
            value.append(parameterValue);
        }
        return value.toString();
    }

    @Override
    public String toString() {
        return value;
    }

}
