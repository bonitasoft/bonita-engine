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

package org.bonitasoft.engine.bdm;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.persistence.AttributeConverter;

/**
 * @author Danila Mazour
 * @deprecated {@link org.bonitasoft.engine.business.data.generator.OffsetDateTimeConverter} is now used. Keep this class for backward runtime compatibility
 */
@Deprecated
public class OffsetDateTimeConverter implements AttributeConverter<OffsetDateTime, String> {

    @Override
    public String convertToDatabaseColumn(OffsetDateTime offsetDateTime) {
        if (offsetDateTime != null) {
            return offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } else {
            return null;
        }
    }

    @Override
    public OffsetDateTime convertToEntityAttribute(String dbData) {
        if (dbData != null) {
            try {
                return OffsetDateTime.parse(dbData, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new RuntimeException(
                        "Database OffsetDate&Time format must be ISO-8601 compliant yyyy-MM-dd'T'HH:mm:ss(.SSS)Z ", e);
            }
        } else {
            return null;
        }
    }
}
