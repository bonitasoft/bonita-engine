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
package org.bonitasoft.web.rest.server.framework.utils.converter.typed;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.bonitasoft.web.rest.server.framework.utils.converter.ConversionException;
import org.bonitasoft.web.rest.server.framework.utils.converter.Converter;

/**
 * @author Nicolas Tith
 */
public class DateConverter implements Converter<Date> {

    @Override
    public Date convert(String toBeConverted) throws ConversionException {
        if (toBeConverted == null || toBeConverted.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy",
                    DateFormatSymbols.getInstance(Locale.ENGLISH));
            return formatter.parse(toBeConverted);
        } catch (ParseException e) {
            throw new ConversionException(toBeConverted + " cannot be converted to Date", e);
        }
    }
}
