/**
 * Copyright (C) 2019 Bonitasoft S.A.
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

import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomLocalDateTimeSerializerTest {

    @Mock
    JsonGenerator jsonGenerator;

    @Test
    public void serialize_should_write_date_to_ISO_8601() throws Exception {
        // given:
        final CustomLocalDateTimeSerializer serializer = new CustomLocalDateTimeSerializer();

        // when:
        serializer.serialize(LocalDateTime.of(2018, 10, 23, 11, 21, 7), jsonGenerator, null);

        // then:
        verify(jsonGenerator).writeString("2018-10-23T11:21:07");
    }

    @Test
    public void serialize_should_write_null_for_a_null_date() throws Exception {
        // when:
        new CustomLocalDateTimeSerializer().serialize(null, jsonGenerator, null);

        // then:
        verify(jsonGenerator).writeString((String) null);
    }
}
