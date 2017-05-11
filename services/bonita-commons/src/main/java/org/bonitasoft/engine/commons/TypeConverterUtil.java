/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.commons;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.converters.AbstractConverter;
import org.apache.commons.beanutils.converters.DateConverter;

/**
 * @author Laurent Leseigneur
 */

public class TypeConverterUtil {

    private ConvertUtilsBean convertUtilsBean;

    public TypeConverterUtil(String[] datePatterns) {
        convertUtilsBean = new ConvertUtilsBean();
        final DateConverter dateConverter = new DateConverter();
        dateConverter.setPatterns(datePatterns);
        dateConverter.setTimeZone(TimeZone.getTimeZone("GMT"));
        convertUtilsBean.register(dateConverter, Date.class);
        convertUtilsBean.register(new LocalDateTimeConverter(), LocalDateTime.class);
        convertUtilsBean.register(new LocalDateConverter(), LocalDate.class);
        convertUtilsBean.register(new OffsetDateTimeConverter(), OffsetDateTime.class);
    }

    public Object convertToType(Class<? extends Serializable> clazz, Serializable parameterValue) {
        try {
            return convertUtilsBean.convert(parameterValue, clazz);
        } catch (ConversionException e) {
            throw new IllegalArgumentException("unable to parse '" + parameterValue + "' to type " + clazz.getName());
        }
    }

    private class LocalDateConverter extends AbstractConverter {

        static final int MAX_LOCAL_DATE_LENGTH = 10;

        @Override
        protected <T> T convertToType(Class<T> type, Object value) throws Throwable {
            if (!(value instanceof String)) {
                throw conversionException(type, value);
            }
            String valueAsString = (String) value;
            if (valueAsString.length() > MAX_LOCAL_DATE_LENGTH) {
                valueAsString = valueAsString.substring(0, MAX_LOCAL_DATE_LENGTH);
            }
            return type.cast(LocalDate.parse(valueAsString));
        }

        @Override
        protected Class<?> getDefaultType() {
            return LocalDate.class;
        }
    }

    private class LocalDateTimeConverter extends AbstractConverter {

        @Override
        protected <T> T convertToType(Class<T> type, Object value) throws Throwable {
            if (!(value instanceof String)) {
                throw conversionException(type, value);
            }
            String paramValueString = (String) value;
            try {
                return type.cast(LocalDateTime.parse(paramValueString));
            } catch (DateTimeParseException e) {
                //We drop the timezone info from the String:
                return type.cast(ZonedDateTime.parse(paramValueString).toLocalDateTime());
            }
        }

        @Override
        protected Class<?> getDefaultType() {
            return LocalDateTime.class;
        }
    }

    private class OffsetDateTimeConverter extends AbstractConverter {

        @Override
        protected <T> T convertToType(Class<T> type, Object value) throws Throwable {
            if (!(value instanceof String)) {
                throw conversionException(type, value);
            }
            return type.cast(OffsetDateTime.parse((CharSequence) value));
        }

        @Override
        protected Class<?> getDefaultType() {
            return OffsetDateTime.class;
        }
    }
}
