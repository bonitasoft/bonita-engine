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
package org.bonitasoft.web.toolkit.client.common;

import java.util.Date;

/**
 * @author Paul AMAR
 */
public abstract class CommonDateFormater {

    private static CommonDateFormater formater = null;

    abstract public Date _parse(final String value, final String format);

    abstract public String _toString(final Date value, final String format);

    public static void setDateFormater(final CommonDateFormater formater) {
        CommonDateFormater.formater = formater;
    }

    public static Date parse(final String value, final String format) {
        return formater._parse(value, format);
    }

    public static String toString(final Date value, final String format) {
        return formater._toString(value, format);
    }

}
