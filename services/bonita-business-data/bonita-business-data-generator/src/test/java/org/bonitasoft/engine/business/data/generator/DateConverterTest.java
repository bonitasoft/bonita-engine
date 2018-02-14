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

import java.time.LocalDate;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Danila Mazour
 */
public class DateConverterTest {

    @Test
    public void convertToDatabaseColumn_should_generate_a_string_ISO_compliant() {
        DateConverter dateConverter = new DateConverter();
        LocalDate localDate = LocalDate.of(2001, 9, 11);
        String localDateinString = dateConverter.convertToDatabaseColumn(localDate);
        assertThat(localDateinString).isNotNull();
        assertThat(localDateinString).isEqualTo("2001-09-11");
    }

    @Test
    public void convertToEntityAttribute_should_generate_the_correct_attribute() {
        DateConverter dateConverter = new DateConverter();
        String localDateinString = "1993-01-19";
        LocalDate localDate = dateConverter.convertToEntityAttribute(localDateinString);
        assertThat(localDate).isNotNull();
        assertThat(localDate.getMonthValue()).isEqualTo(01);
        assertThat(localDate.getYear()).isEqualTo(1993);
        assertThat(localDate.getDayOfMonth()).isEqualTo(19);

    }

    @Test
    public void dateConverter_should_generate_the_same_object_in_and_out() {
        DateConverter dateConverter = new DateConverter();
        LocalDate localDate = LocalDate.of(2017, 02, 24);
        LocalDate localDateResult = dateConverter.convertToEntityAttribute(dateConverter.convertToDatabaseColumn(localDate));
        assertThat(localDateResult).isNotNull();
        assertThat(localDateResult).isEqualTo(localDate);
    }

    @Test
    public void convertToDatabaseColumn_should_return_null_when_given_a_null_string() {
        DateConverter dateConverter = new DateConverter();
        String localDateinString = dateConverter.convertToDatabaseColumn(null);
        assertThat(localDateinString).isNull();
    }

    @Test
    public void convertToEntityAttribute_should_return_null_when_given_a_null_object() {
        DateConverter dateConverter = new DateConverter();
        LocalDate localDate = dateConverter.convertToEntityAttribute(null);
        assertThat(localDate).isNull();
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Test
    public void convertToEntityAttribute_should_throw_an_exception_when_given_a_faulty_string() {
        DateConverter dateConverter = new DateConverter();
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Database date format must be ISO-8601 compliant ( yyyy-mm-dd )");
        LocalDate localDate = dateConverter.convertToEntityAttribute("Logan");
    }
}
