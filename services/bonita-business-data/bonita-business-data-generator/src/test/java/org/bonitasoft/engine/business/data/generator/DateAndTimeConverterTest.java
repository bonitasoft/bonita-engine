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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Danila Mazour
 */
public class DateAndTimeConverterTest {

    @Test
    public void convertToDatabaseColumn_should_generate_a_string_ISO_compliant() {
        DateAndTimeConverter dateAndTimeConverter = new DateAndTimeConverter();
        LocalDateTime localDateTime = LocalDateTime.of(2017, 2, 22, 13, 00, 00);
        String dateAndTimeString = dateAndTimeConverter.convertToDatabaseColumn(localDateTime);
        assertThat(dateAndTimeString).isNotNull();
        assertThat(dateAndTimeString).isEqualTo("2017-02-22T13:00:00");
    }

    @Test
    public void convertToEntityAttribute_should_generate_the_correct_attribute() {
        DateAndTimeConverter dateAndTimeConverter = new DateAndTimeConverter();
        String dateAndTimeString = "2011-11-26T15:02:00.654";
        LocalDateTime localDateTime = dateAndTimeConverter.convertToEntityAttribute(dateAndTimeString);
        assertThat(localDateTime).isNotNull();
        assertThat(localDateTime.getYear()).isEqualTo(2011);
        assertThat(localDateTime.getMonthValue()).isEqualTo(11);
        assertThat(localDateTime.getDayOfMonth()).isEqualTo(26);
        assertThat(localDateTime.getHour()).isEqualTo(15);
        assertThat(localDateTime.getMinute()).isEqualTo(2);
        assertThat(localDateTime.getSecond()).isEqualTo(0);
        assertThat(localDateTime.getNano()).isEqualTo(654000000);
    }

    @Test
    public void dateAndTimeConverter_should_generate_the_same_object_in_and_out() {
        DateAndTimeConverter dateAndTimeConverter = new DateAndTimeConverter();
        LocalDateTime localDateTime = LocalDateTime.of(1961, 4, 12, 6, 7, 00);
        LocalDateTime resultLocalDateTime = dateAndTimeConverter.convertToEntityAttribute(dateAndTimeConverter.convertToDatabaseColumn(localDateTime));
        assertThat(resultLocalDateTime).isNotNull();
        assertThat(resultLocalDateTime).isEqualTo(localDateTime);
    }

    @Test
    public void convertToDatabaseColumn_should_return_null_when_given_a_null_string() {
        DateAndTimeConverter dateAndTimeConverter = new DateAndTimeConverter();
        String localDateinString = dateAndTimeConverter.convertToDatabaseColumn(null);
        assertThat(localDateinString).isNull();
    }

    @Test
    public void convertToEntityAttribute_should_return_null_when_given_a_null_object() {
        DateAndTimeConverter dateAndTimeConverter = new DateAndTimeConverter();
        LocalDateTime localDate = dateAndTimeConverter.convertToEntityAttribute(null);
        assertThat(localDate).isNull();
    }

    
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Test
    public void convertToEntityAttribute_should_throw_an_exception_when_given_a_faulty_string() {
        DateAndTimeConverter dateAndTimeConverter = new DateAndTimeConverter();
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Database date & time format must be ISO-8601 compliant ( yyyy-mm-ddThh:mm:ss )");
        LocalDateTime localDate = dateAndTimeConverter.convertToEntityAttribute("LaLaLand");
    }
    
    @Test
    public void convertToDatabaseColumn_should_account_up_to_nanoseconds_in_the_generated_string() throws Exception {
        DateAndTimeConverter dateAndTimeConverter = new DateAndTimeConverter();
        LocalDateTime localDateTime = LocalDateTime.of(2017, 2, 28, 17, 42, 12,9649);
        String dateAndTimeString = dateAndTimeConverter.convertToDatabaseColumn(localDateTime);
        assertThat(dateAndTimeString).isNotNull();
        assertThat(dateAndTimeString).isEqualTo("2017-02-28T17:42:12.000009649");
        
    }
}
