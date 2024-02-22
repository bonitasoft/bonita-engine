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

import junit.framework.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * @author Vincent Elcrin
 */
public class FilterTest {

    @Mock
    Field field = Mockito.mock(Field.class);

    @Mock
    @SuppressWarnings("unchecked")
    Value<String> value = Mockito.mock(Value.class);

    @Test
    public void testFilterField() throws Exception {
        Mockito.doReturn("field").when(field).toString();
        Filter<String> filter = new Filter<>(field, value);

        Assert.assertEquals("field", filter.getField());
    }

    @Test
    public void testFilterValue() throws Exception {
        Mockito.doReturn("value").when(value).cast();
        Filter<String> filter = new Filter<>(field, value);

        Assert.assertEquals("value", filter.getValue());
    }
}
