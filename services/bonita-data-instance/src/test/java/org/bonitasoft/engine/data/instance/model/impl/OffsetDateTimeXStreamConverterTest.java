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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.Test;

/**
 * @author Emmanuel Duchastenier
 */
public class OffsetDateTimeXStreamConverterTest {

    @Test
    public void toString_should_OffsetDateTime_converted_to_UTC() throws Exception {
        // given:
        final OffsetDateTimeXStreamConverter offsetDateTimeConverter = new OffsetDateTimeXStreamConverter();
        final OffsetDateTime offsetDateTime = OffsetDateTime.of(LocalDateTime.of(1973, 10, 17, 13, 42, 0), ZoneOffset.ofHours(-4));

        // when:
        final String toString = offsetDateTimeConverter.toString(offsetDateTime);

        // then:
        assertThat(toString).isEqualTo("1973-10-17T17:42:00Z");
    }

    @Test
    public void fromString_should_reset_offset_to_UTC() throws Exception {
        // given:
        String dateAsString = "1973-10-17T11:50:00-02:00";
        final OffsetDateTimeXStreamConverter offsetDateTimeConverter = new OffsetDateTimeXStreamConverter();

        // when:
        final Object offsetDateTime = offsetDateTimeConverter.fromString(dateAsString);

        // then:
        assertThat(offsetDateTime).isEqualTo(OffsetDateTime.of(LocalDateTime.of(1973, 10, 17, 13, 50, 0), ZoneOffset.UTC));
    }

    @Test
    public void toString_should_return_null_for_null_input() throws Exception {
        // given:
        final OffsetDateTimeXStreamConverter offsetDateTimeConverter = new OffsetDateTimeXStreamConverter();

        // when:
        final String toString = offsetDateTimeConverter.toString(null);

        // then:
        assertThat(toString).isNull();
    }
}
