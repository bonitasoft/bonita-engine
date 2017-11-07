/*
 * Copyright (C) 2017 Bonitasoft S.A.
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
 */
package org.bonitasoft.engine.data.instance.model.impl;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.thoughtworks.xstream.converters.basic.AbstractSingleValueConverter;

/**
 * @author Emmanuel Duchastenier
 */
public class OffsetDateTimeXStreamConverter extends AbstractSingleValueConverter {

    public boolean canConvert(Class type) {
        return OffsetDateTime.class.equals(type);
    }

    public String toString(Object source) {
        return source == null ? null : ((OffsetDateTime) source).withOffsetSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public Object fromString(String str) {
        try {
            return OffsetDateTime.parse(str).withOffsetSameInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("OffsetDateTime failed to parse the incoming string", e);
        }
    }
}
