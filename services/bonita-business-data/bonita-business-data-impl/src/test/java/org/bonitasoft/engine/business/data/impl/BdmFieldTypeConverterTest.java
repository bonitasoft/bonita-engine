/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.data.impl;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class BdmFieldTypeConverterTest {

    @Test
    void should_convert_float_to_double() {
        assertThat(BdmFieldTypeConverter.convert(125f, Double.class)).isEqualTo(125d);
    }

    @Test
    void should_convert_double_float() {
        assertThat(BdmFieldTypeConverter.convert(125d, Float.class)).isEqualTo(125f);
    }

    @Test
    void should_convert_integer_to_double() {
        assertThat(BdmFieldTypeConverter.convert(1255598556, Double.class)).isEqualTo(1255598556d);
    }

    @Test
    void should_convert_integer_to_long() {
        assertThat(BdmFieldTypeConverter.convert(9988877, Long.class)).isEqualTo(9988877L);
    }

    @Test
    void should_convert_long_to_integer() {
        assertThat(BdmFieldTypeConverter.convert(9988877L, Integer.class)).isEqualTo(9988877);
    }

    @Test
    void should_convert_integer_to_short() {
        assertThat(BdmFieldTypeConverter.convert(55000, Short.class)).isEqualTo((short) 55000);
    }

    @Test
    void should_convert_integer_to_byte() {
        assertThat(BdmFieldTypeConverter.convert(124, Byte.class)).isEqualTo((byte) 124);
    }

    @Test
    void converting_null_should_return_null() {
        assertThat(BdmFieldTypeConverter.convert(null, Long.class)).isNull();
    }

    @Test
    void converting_supported_type_should_return_it_directly_String() {
        final String someString = "someString";
        assertThat(BdmFieldTypeConverter.convert(someString, String.class)).isSameAs(someString);
    }

    @Test
    void converting_supported_type_should_return_it_directly_Float() {
        Float someFloat = 1.0f;
        assertThat(BdmFieldTypeConverter.convert(someFloat, Float.class)).isSameAs(someFloat);
    }

    @Test
    void should_convert_string_to_localdate() {
        assertThat(BdmFieldTypeConverter.convert("2025-07-17", LocalDate.class)).isEqualTo(LocalDate.of(2025, 7, 17));
    }

    @Test
    void should_convert_string_to_localdatetime() {
        assertThat(BdmFieldTypeConverter.convert("2025-01-12T10:15:30", LocalDateTime.class))
                .isEqualTo(LocalDateTime.of(2025, 1, 12, 10, 15, 30));
    }

    @Test
    void should_convert_string_to_offsetdatetime() {
        OffsetDateTime result = BdmFieldTypeConverter.convert("2025-11-29T10:15:30+01:00", OffsetDateTime.class);
        assertThat(result.toLocalDateTime()).isEqualTo(LocalDateTime.of(2025, 11, 29, 10, 15, 30));
        assertThat(result.getOffset()).isEqualTo(ZoneOffset.of("+01:00"));
    }

    @Test
    void should_throw_exception_for_incompatible_conversion() {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> BdmFieldTypeConverter.convert("invalid", Integer.class))
                .withMessageContaining("Cannot convert");
    }

}
