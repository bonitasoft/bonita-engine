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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TypeConvertUtilTest {

    private TypeConverterUtil typeConverterUtil;

    @BeforeEach
    void before() {
        String[] datePatterns = new String[] { "yyyy-MM-dd HH:mm:ss","yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd", "HH:mm:ss","yyyy-MM-dd'T'HH:mm:ss.SSS" };
        typeConverterUtil = new TypeConverterUtil(datePatterns);
    }

    @Test
    void should_convert_primitives() {

        assertThat((Long) typeConverterUtil.convertToType(Long.class, "15")).isEqualTo(15L);

        assertThat((Integer) typeConverterUtil.convertToType(Integer.class, "16")).isEqualTo(16);

        assertThat((Float) typeConverterUtil.convertToType(Float.class, "-17.596")).isEqualTo(-17.596F);

        final BigDecimal bigDecimal = new BigDecimal(12.3650000000000002131628207280300557613372802734375);
        assertThat((BigDecimal) typeConverterUtil.convertToType(BigDecimal.class, "12.3650000000000002131628207280300557613372802734375")).isEqualTo(bigDecimal);


        assertThat((Boolean) typeConverterUtil.convertToType(Boolean.class, "true")).isTrue();
        assertThat((Boolean) typeConverterUtil.convertToType(Boolean.class, "false")).isFalse();

        checkDateConvert("2015-08-06T22:00:00.000", 1438898400000L);
        checkDateConvert("2015-01-31", 1422662400000L);
        checkDateConvert("2015-01-31 23:15:59", 1422746159000L);
        checkDateConvert("2015-01-31T23:15:59", 1422746159000L);
        checkDateConvert("2015-01-31T23:15:59.001", 1422746159001L);
        checkDateConvert("12:00:00", 43200000L);

    }

    @Test
    void dateConvert_should_fail() {
        assertThrows(IllegalArgumentException.class, () -> typeConverterUtil.convertToType(Date.class, "not a date"),
                "unable to parse 'not a date' to type java.util.Date");
    }

    private void checkDateConvert(String dateToConvert, long expectedDateAsLong) {
        final Date expectedDate = new Date(expectedDateAsLong);
        final Date returnedDate = (Date) typeConverterUtil.convertToType(Date.class, dateToConvert);
        assertThat(returnedDate).as("error while converting date:" + dateToConvert + " (assuming provided date is GMT)").hasTime(expectedDateAsLong).isEqualTo(expectedDate);
    }



}
