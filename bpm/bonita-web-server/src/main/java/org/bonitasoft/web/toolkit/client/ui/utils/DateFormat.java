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

import java.util.Date;

import org.bonitasoft.web.toolkit.client.common.CommonDateFormater;
import org.bonitasoft.web.toolkit.client.common.i18n.AbstractI18n;

/**
 * Available formats are
 * <ul>
 * <li>long : a simple long value representing the date in milliseconds</li>
 * <li>SQL (JSON compatible) : yyyy-MM-dd HH:mm:ss.SSS</li>
 * <li>Form input : MM/dd/YY (localized format)</li>
 * <li>Display as a short date : MM/dd/YY (localized format)</li>
 * <li>Display as a full date with time : MM/dd/YYYY HH:mm (localized format)</li>
 * <li>Display as time relative to current time : "1 hour ago", "in 5 minutes", "2 years ago"</li>
 * </ul>
 *
 * @author Séverin Moussel
 */
// TODO :
// * pull out all kind of date formatter in different classes like RelativeStringDateFormatter
// * make an interface implemented by all date formatter
// * make a switch return a polimorph DateFormatter and just call dateFormatter.format(...)
public abstract class DateFormat {

    public enum UNIT {
        YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, MILLISECOND
    }

    public enum FORMAT {

        SQL("yyyy-MM-dd HH:mm:ss.SSS"), FORM(AbstractI18n.t_("MM/dd/yyyy")), DISPLAY(
                AbstractI18n.t_("MM/dd/yyyy h:mm a")), DISPLAY_SHORT(
                        AbstractI18n.t_("MMMM dd, yyyy")), LONG, DISPLAY_RELATIVE;

        private final String formatString;

        FORMAT() {
            this("");
        }

        FORMAT(final String formatString) {
            this.formatString = formatString;
        }

        public String getFormatString() {
            return this.formatString;
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GENERIC TO DATE conversion
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static Date formatToDate(final String date, final FORMAT format) throws IllegalArgumentException {
        if (date == null) {
            return null;
        }

        if (format.equals(FORMAT.LONG)) {
            new Date(Long.parseLong(date));
        }

        return formatToDate(date, format.getFormatString());
    }

    public static Date formatToDate(final String date, final String format) throws IllegalArgumentException {
        if (date == null) {
            return null;
        }

        return CommonDateFormater.parse(date, format);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GENERIC TO LONG CONVERSION
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static Long formatToLong(final String date, final FORMAT format) throws IllegalArgumentException {
        if (date == null || date.isEmpty()) {
            return null;
        }

        if (format.equals(FORMAT.LONG)) {
            return Long.parseLong(date);
        }

        return formatToLong(date, format.getFormatString());
    }

    public static Long formatToLong(final String date, final String format) throws IllegalArgumentException {
        if (date == null) {
            return null;
        }
        final Date _date = CommonDateFormater.parse(date, format);
        return _date.getTime();
    }

    // // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DATE OBJECT CONVERSIONS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static String dateToFormat(final Date date, final String format) {
        if (date == null) {
            return null;
        }
        return CommonDateFormater.toString(date, format);
    }

    public static String dateToSql(final Date date) {
        return dateToFormat(date, FORMAT.SQL.getFormatString());
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SQL TO ???
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static Long sqlToLong(final String date) {
        return formatToLong(date, FORMAT.SQL);
    }

    public static Date sqlToDate(final String date) {
        return formatToDate(date, FORMAT.SQL);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // FORM TO ???
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}
