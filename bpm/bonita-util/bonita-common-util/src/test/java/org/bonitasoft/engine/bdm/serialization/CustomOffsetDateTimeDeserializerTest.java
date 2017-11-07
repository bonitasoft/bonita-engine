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

package org.bonitasoft.engine.bdm.serialization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonParser;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomOffsetDateTimeDeserializerTest {

    @Mock
    private JsonParser jsonParser;

    @Test
    public void deserialize_should_convert_to_UTC() throws Exception {
        // given:
        doReturn("2018-09-01T09:21:08+07:00").when(jsonParser).readValueAs(String.class);
        final CustomOffsetDateTimeDeserializer serializer = new CustomOffsetDateTimeDeserializer();

        // when:
        final OffsetDateTime deserialized = serializer.deserialize(jsonParser, null);

        // then:
        assertThat(deserialized).isEqualTo(OffsetDateTime.of(LocalDateTime.of(2018, 9, 1, 2, 21, 8), ZoneOffset.ofHours(0)));
    }

    @Test
    public void deserialize_should_return_null_for_a_null_date() throws Exception {
        // given:
        doReturn(null).when(jsonParser).readValueAs(String.class);

        // when:
        final OffsetDateTime offsetDateTime = new CustomOffsetDateTimeDeserializer().deserialize(jsonParser, null);

        // then:
        assertThat(offsetDateTime).isNull();
    }
}
