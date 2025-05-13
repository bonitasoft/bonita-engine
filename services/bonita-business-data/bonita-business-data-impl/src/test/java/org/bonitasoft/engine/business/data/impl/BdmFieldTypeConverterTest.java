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

import static org.assertj.core.api.Assertions.assertThat;

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

}
