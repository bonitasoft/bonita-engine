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

/**
 * @author Julien Mege
 */
public abstract class StringUtil {

    /**
     * Convert a String to a boolean using the smart way<br />
     * <ul>
     * <li>if the String is NULL, the Boolean will be NULL.</li>
     * <li>if the String can be cast to a long, the boolean will be TRUE if the long value is > 0, otherwise FALSE.</li>
     * <li>if the String is equal to "true", "yes" or "ok" the boolean will be TRUE.</li>
     * <li>if the String is equal to "false", "no" or "ko" the boolean will be FALSE.</li>
     * <li>all other cases will throw an IllegalArgumentException</li>
     *
     * @param value
     *        The value to convert
     * @return This method will return the Boolean value of the value passed.
     * @throw IllegalArgumentException
     */
    public static Boolean toBoolean(final String value) throws IllegalArgumentException {
        if (value == null) {
            return null;
        }

        // FIXME Manage integer values (<=0 false, >=1 true)

        if ("true".equals(value) || "yes".equals(value) || "ok".equals(value)) {
            return true;
        } else if ("false".equals(value) || "no".equals(value) || "ko".equals(value)) {
            return false;
        }

        try {
            return Integer.parseInt(value) > 0;
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException(value + " is not a valid boolean value");
        }
    }

    /**
     * Converts a String to an integer plus if the String is NULL, the returned value will be NULL.
     */
    public static Integer toInteger(final String value) throws NumberFormatException {
        if (value == null) {
            return null;
        }
        return Integer.valueOf(value);
    }

    /**
     * Check if a value is blank (NULL or empty String).
     *
     * @param value
     *        The value to check
     * @return This method returns TRUE if the value is null OR an empty String, otherwise FALSE.
     */
    public static boolean isBlank(final String value) {
        return value == null || value.isEmpty() || value.trim().isEmpty();
    }

}
