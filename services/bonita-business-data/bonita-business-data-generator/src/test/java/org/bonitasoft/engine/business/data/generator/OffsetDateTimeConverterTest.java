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

package org.bonitasoft.engine.business.data.generator;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Danila Mazour
 */
public class OffsetDateTimeConverterTest {
    
    @Test
    public void convertToDatabaseColumn_should_generate_a_string_ISO_compliant() {
        OffsetDateTimeConverter offsetDateAndTimeConverter = new OffsetDateTimeConverter();
        OffsetDateTime offsetDateTime = OffsetDateTime.of(LocalDateTime.of(2017, 2, 22, 13, 00, 00), ZoneOffset.ofHours(-5));
        String offsetDateAndTimeString = offsetDateAndTimeConverter.convertToDatabaseColumn(offsetDateTime);
        assertThat(offsetDateAndTimeString).isNotNull();
        assertThat(offsetDateAndTimeString).isEqualTo("2017-02-22T18:00:00Z");
    }

    @Test
    public void convertToEntityAttribute_should_generate_the_correct_attribute() {
        OffsetDateTimeConverter offsetDateAndTimeConverter = new OffsetDateTimeConverter();
        String offsetDateAndTimeString = "2011-11-26T15:02:00.654Z";
        OffsetDateTime offsetDateTime = offsetDateAndTimeConverter.convertToEntityAttribute(offsetDateAndTimeString);
        assertThat(offsetDateTime).isNotNull();
        assertThat(offsetDateTime.getYear()).isEqualTo(2011);
        assertThat(offsetDateTime.getMonthValue()).isEqualTo(11);
        assertThat(offsetDateTime.getDayOfMonth()).isEqualTo(26);
        assertThat(offsetDateTime.getHour()).isEqualTo(15);
        assertThat(offsetDateTime.getMinute()).isEqualTo(2);
        assertThat(offsetDateTime.getSecond()).isEqualTo(0);
        assertThat(offsetDateTime.getNano()).isEqualTo(654000000);
    }

    @Test
    public void offsetDateTimeConverter_should_generate_the_same_object_in_and_out() {
        OffsetDateTimeConverter offsetDateAndTimeConverter = new OffsetDateTimeConverter();
        OffsetDateTime offsetDateTime = OffsetDateTime.of(LocalDateTime.of(1961, 4, 12, 6, 7, 00),ZoneOffset.ofHours(5));
        OffsetDateTime resultOffsetDateTime = offsetDateAndTimeConverter.convertToEntityAttribute(offsetDateAndTimeConverter.convertToDatabaseColumn(offsetDateTime)).withOffsetSameInstant(ZoneOffset.ofHours(5));
        assertThat(resultOffsetDateTime).isNotNull();
        assertThat(resultOffsetDateTime).isEqualTo(offsetDateTime);
    }
    
    @Test
    public void offsetDateTimeConverter_should_generate_the_same_time_for_OffsetDateTimes_in_and_out() {
        OffsetDateTimeConverter offsetDateAndTimeConverter = new OffsetDateTimeConverter();
        OffsetDateTime offsetDateTime = OffsetDateTime.of(LocalDateTime.of(1961, 4, 12, 6, 7, 00),ZoneOffset.ofHours(5));
        OffsetDateTime resultOffsetDateTime = offsetDateAndTimeConverter.convertToEntityAttribute(offsetDateAndTimeConverter.convertToDatabaseColumn(offsetDateTime));
        assertThat(offsetDateTime.isEqual(resultOffsetDateTime)).isTrue();
        
    }

    @Test
    public void convertToDatabaseColumn_should_return_null_when_given_a_null_string() {
        OffsetDateTimeConverter offsetDateAndTimeConverter = new OffsetDateTimeConverter();
        String nullOffsetDateTime = offsetDateAndTimeConverter.convertToDatabaseColumn(null);
        assertThat(nullOffsetDateTime).isNull();
    }

    @Test
    public void convertToEntityAttribute_should_return_null_when_given_a_null_object() {
        OffsetDateTimeConverter offsetDateAndTimeConverter = new OffsetDateTimeConverter();
        OffsetDateTime nullOffsetDateTime = offsetDateAndTimeConverter.convertToEntityAttribute(null);
        assertThat(nullOffsetDateTime).isNull();
    }
    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Test
    public void convertToEntityAttribute_should_throw_an_exception_when_given_a_faulty_string() {
        OffsetDateTimeConverter offsetDateAndTimeConverter = new OffsetDateTimeConverter();
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Database OffsetDate&Time format must be ISO-8601 compliant yyyy-MM-dd'T'HH:mm:ss(.SSS)Z ");
        OffsetDateTime offsetDateTime = offsetDateAndTimeConverter.convertToEntityAttribute("Django");
    }
    
    @Test
    public void convertToDatabaseColumn_should_modify_the_day_month_and_year_when_switching_to_UTC_in_the_generated_string() throws Exception {
        OffsetDateTimeConverter offsetDateAndTimeConverter = new OffsetDateTimeConverter();
        OffsetDateTime offsetDateTime = OffsetDateTime.of(LocalDateTime.of(2017, 1, 1, 7, 42, 12,9649), ZoneOffset.ofHours(10));
        String offsetDateAndTimeString = offsetDateAndTimeConverter.convertToDatabaseColumn(offsetDateTime);
        assertThat(offsetDateAndTimeString).isNotNull();
        assertThat(offsetDateAndTimeString).isEqualTo("2016-12-31T21:42:12.000009649Z");
        
    }
}