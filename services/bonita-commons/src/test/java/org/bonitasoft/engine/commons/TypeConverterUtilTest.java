/**
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
 **/

package org.bonitasoft.engine.commons;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.junit.jupiter.api.Test;

/**
 * @author Emmanuel Duchastenier
 */
class TypeConverterUtilTest {

    private final TypeConverterUtil typeConverterUtil = new TypeConverterUtil(null);

    @Test
    void convertToType_should_convert_to_LocalDate() {
        final Object date = typeConverterUtil.convertToType(LocalDate.class, "2014-07-14");

        // then:
        assertThat(date).isEqualTo(LocalDate.of(2014, 7, 14));
    }

    @Test
    void getDefaultType_should_truncate_extra_LocalDate_string_characters() {
        final Object date = typeConverterUtil.convertToType(LocalDate.class, "2015-07-14TOTO12:00");

        // then:
        assertThat(date).isEqualTo(LocalDate.of(2015, 7, 14));
    }

    @Test
    void getDefaultType_should_gracefully_handle_null_values() {
        assertThrows(IllegalArgumentException.class, () -> typeConverterUtil.convertToType(Date.class, null),
                "unable to parse");
    }

    @Test
    void convertToType_should_convert_to_LocalDateTime() {
        final Object date = typeConverterUtil.convertToType(LocalDateTime.class, "2014-07-14T17:42:01");

        // then:
        assertThat(date).isEqualTo(LocalDateTime.of(2014, 7, 14, 17, 42, 1));
    }

    @Test
    void getDefaultType_should_truncate_extra_LocalDateTime_string_characters() {
        final Object date = typeConverterUtil.convertToType(LocalDateTime.class, "2015-07-14T12:00:07+03:00");

        // then:
        assertThat(date).isEqualTo(LocalDateTime.of(2015, 7, 14, 12, 0, 7));
    }

    @Test
    void convertToType_should_convert_to_OffsetDateTime() {
        final Object date = typeConverterUtil.convertToType(OffsetDateTime.class, "2014-07-14T17:42:01Z");

        // then:
        assertThat(date).isEqualTo(OffsetDateTime.of(2014, 7, 14, 17, 42, 1, 0, ZoneOffset.UTC));
    }

    @Test
    void getDefaultType_should_throw_exception_for_wrong_date_format() {
        assertThrows(IllegalArgumentException.class,
                () -> typeConverterUtil.convertToType(OffsetDateTime.class, "2015-07-14T12:00:07+03:00:17:15:123.000"),
                "unable to parse");
    }

    @Test
    void getDefaultType_should_throw_exception_for_wrong_LocalDate_type() {
        assertThrows(IllegalArgumentException.class,
                () -> typeConverterUtil.convertToType(LocalDate.class, 225L),
                "unable to parse");
    }

    @Test
    void getDefaultType_should_throw_exception_for_wrong_LocalDateTime_type() {
        assertThrows(IllegalArgumentException.class,
                () -> typeConverterUtil.convertToType(LocalDateTime.class, new Date()),
                "unable to parse");
    }

    @Test
    void getDefaultType_should_throw_exception_for_wrong_OffsetDateTime_type() {
        assertThrows(IllegalArgumentException.class,
                () -> typeConverterUtil.convertToType(OffsetDateTime.class, (byte) 0),
                "unable to parse");
    }
}
