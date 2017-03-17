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
package org.bonitasoft.engine.data.instance.model.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.WeakHashMap;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class XStreamFactory {

    private static final Map<ClassLoader, XStream> XSTREAM_MAP = new WeakHashMap<>();

    public static XStream getXStream() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        XStream xStream = XSTREAM_MAP.get(classLoader);
        if (xStream == null) {
            xStream = new XStream(new StaxDriver());
            XSTREAM_MAP.put(classLoader, xStream);
            xStream.registerConverter(new LocalDateXStreamConverter());
            xStream.registerConverter(new LocalDateTimeXStreamConverter());
            xStream.registerConverter(new OffsetDateTimeXStreamConverter());
        }

        return xStream;
    }

    /**
     * Removes the XStream object related from given ClassLoader from the cache
     * 
     * @param classLoader classLoader related to the XStreamObject to be removed.
     */
    public static void remove(ClassLoader classLoader) {
        XSTREAM_MAP.remove(classLoader);
    }

    private static class LocalDateXStreamConverter extends AbstractSingleValueConverter {

        public boolean canConvert(Class type) {
            return LocalDate.class.equals(type);
        }

        public String toString(Object source) {
            return source.toString();
        }

        public Object fromString(String str) {
            try {
                return LocalDate.parse(str);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("LocalDate failed to parse the incoming string", e);
            }
        }
    }

    private static class LocalDateTimeXStreamConverter extends AbstractSingleValueConverter {

        public boolean canConvert(Class type) {
            return LocalDateTime.class.equals(type);
        }

        public String toString(Object source) {
            return source.toString();
        }

        public Object fromString(String str) {
            try {
                return LocalDateTime.parse(str);
            } catch (DateTimeParseException e) {
                throw new RuntimeException("LocalDateTime failed to parse the incoming string", e);
            }
        }
    }

}
