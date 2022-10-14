/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.datastore.filter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.bonitasoft.web.rest.server.datastore.converter.ValueConverter;
import org.junit.Test;

/**
 * @author Vincent Elcrin
 */
public class ValueTest {

    @Test
    public void testName() throws Exception {
        @SuppressWarnings("unchecked")
        ValueConverter<Long> converter = mock(ValueConverter.class);
        doReturn(5L).when(converter).convert("12");

        Value<Long> value = new Value<>("12", converter);

        assertEquals(Long.valueOf(5), value.cast());
    }
}
