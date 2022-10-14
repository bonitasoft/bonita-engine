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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// TODO Delete this class while no longer used
/**
 * @author Haojie Yuan
 * @deprecated Use DateFormat instead
 */
@Deprecated
public class DateUtil {

    /**
     * @deprecated Use DateFormat instead
     */
    @Deprecated
    public static String formatDate(final Date date) {
        String dateStr = null;
        if (date != null) {
            final DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.ENGLISH);
            dateStr = df.format(date);
        }
        return dateStr;
    }

    /**
     * @deprecated Use DateFormat instead
     */
    @Deprecated
    public static String parseDate(final Date date) {
        String dateStr = null;
        if (date != null) {
            final DateFormat time = DateFormat.getTimeInstance(DateFormat.MEDIUM, Locale.ENGLISH);
            final DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.ENGLISH);
            dateStr = time.format(date) + ", " + df.format(date);
        }
        return dateStr;
    }

    /**
     * @deprecated Use DateFormat instead
     */
    @Deprecated
    public static String convertLongToDate(final long time) {
        final Date date = new Date();
        date.setTime(time);
        return formatDate(date);
    }

    /**
     * @deprecated Use DateFormat instead
     */
    @Deprecated
    public static String getYear(final String date) throws ParseException {
        String year = null; // February 27, 2012
        if (date != null && !date.isEmpty()) {
            final DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.ENGLISH);
            final Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTime(df.parse(date));
            year = String.valueOf(cal.get(Calendar.YEAR));
        }
        return year;
    }

}
