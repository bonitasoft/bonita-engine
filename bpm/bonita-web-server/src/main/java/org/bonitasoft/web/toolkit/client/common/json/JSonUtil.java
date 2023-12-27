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
package org.bonitasoft.web.toolkit.client.common.json;

import static java.lang.Integer.toHexString;

/**
 * @author Séverin Moussel
 */
public class JSonUtil {

    public static String quote(final String value) {
        return quoteInternal(value).toString();
    }

    public static StringBuilder quoteInternal(final String value) {
        return new StringBuilder("\"").append(escape(value)).append("\"");
    }

    public static String escape(final String string) {
        return escapeInternal(string).toString();
    }

    private static StringBuilder escapeInternal(String string) {
        if (string == null || string.length() == 0) {
            return new StringBuilder();
        }

        char b;
        char c = 0;
        int i;
        final int len = string.length();
        final StringBuilder sb = new StringBuilder(len + 4);

        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
                case '<':
                case '>':
                case '\'':
                case '\\':
                case '"':
                    sb.append(convertToUnicodeInternal(c));
                    break;
                case '/':
                    if (b == '<') {
                        sb.append('\\');
                    }
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ' || c >= '\u0080' && c < '\u00a0' || c >= '\u2000' && c < '\u2100') {
                        sb.append(convertToUnicodeInternal(c));
                    } else {
                        sb.append(c);
                    }
            }
        }

        return new StringBuilder(sb);
    }

    private static StringBuffer convertToUnicodeInternal(char character) {
        StringBuilder hexString = new StringBuilder("000").append(toHexString(character));
        return new StringBuffer("\\u").append(hexString.substring(hexString.length() - 4));
    }

}
