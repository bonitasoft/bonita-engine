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
package org.bonitasoft.web.rest.server.framework.utils.converter.typed;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;

import org.bonitasoft.web.rest.server.framework.utils.converter.ConversionException;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Colin PUY
 */
public class IntegerConverterTest {

    private IntegerConverter converter;

    @Before
    public void initConverter() {
        converter = new IntegerConverter();
    }

    @Test
    public void nullIsConvertedToNull() throws Exception {

        Integer converted = converter.convert(null);

        assertNull(converted);
    }

    @Test
    public void emptyIsConvertedToNull() throws Exception {

        Integer converted = converter.convert("");

        assertNull(converted);
    }

    @Test
    public void intNumberIsParsedToInteger() throws Exception {

        int converted = converter.convert("456789");

        assertEquals(456789, converted);
    }

    @Test(expected = ConversionException.class)
    public void nonParsableStringThrowAConversionException() throws Exception {
        converter.convert("nonParsable");
    }
}
