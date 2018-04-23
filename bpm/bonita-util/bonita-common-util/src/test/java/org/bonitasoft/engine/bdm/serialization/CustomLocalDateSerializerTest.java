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

import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * @author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomLocalDateSerializerTest {

    @Mock
    JsonGenerator jsonGenerator;

    @Test
    public void serialize_should_write_date_to_ISO_8601() throws Exception {
        // given:
        final CustomLocalDateSerializer serializer = new CustomLocalDateSerializer();

        // when:
        serializer.serialize(LocalDate.of(2018, 8, 15), jsonGenerator, null);

        // then:
        verify(jsonGenerator).writeString("2018-08-15");
    }

    @Test
    public void serialize_should_write_null_for_a_null_date() throws Exception {
        // when:
        new CustomLocalDateSerializer().serialize(null, jsonGenerator, null);

        // then:
        verify(jsonGenerator).writeString((String) null);
    }
}
