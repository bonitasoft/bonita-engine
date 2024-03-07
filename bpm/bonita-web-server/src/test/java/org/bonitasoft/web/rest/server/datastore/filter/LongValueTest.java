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

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Vincent Elcrin
 */
public class LongValueTest {

    @Test
    public void testLongValue() {
        LongValue value = new LongValue("8");

        assertEquals(Long.valueOf(8), value.cast());
    }

    @Test(expected = NumberFormatException.class)
    public void testNoneLongValueThrowException() {
        LongValue value = new LongValue("abc");

        value.cast();
    }
}
