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
package org.bonitasoft.web.rest.server.framework.utils.converter;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author Colin PUY
 */
public class TypeConverterTest {

    private TypeConverter converter;

    @Mock
    private ConverterFactory factory;

    @Before
    public void initConverter() {
        MockitoAnnotations.initMocks(this);

        converter = new TypeConverter(factory);
    }

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void convertCreateAFactoryAndConvertStringValueToSerializableObject() throws Exception {
        Converter typedConverter = mock(Converter.class);
        when(factory.createConverter(anyString())).thenReturn(typedConverter);

        converter.convert("aClassName", "somethingToconvert");

        verify(typedConverter).convert("somethingToconvert");
    }

}
