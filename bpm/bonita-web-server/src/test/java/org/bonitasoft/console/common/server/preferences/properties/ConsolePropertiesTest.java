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
package org.bonitasoft.console.common.server.preferences.properties;

import static org.bonitasoft.console.common.server.preferences.properties.ConfigurationFilesManager.getProperties;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Rohart Bastien
 */
public class ConsolePropertiesTest {

    private ConsoleProperties properties;

    @Before
    public void setUp() throws IOException {
        properties = spy(new ConsoleProperties());
        doReturn(getProperties("aProperty=aValue".getBytes())).when(properties).getProperties();
    }

    @Test
    public void testGetNotExistingProperty() {
        assertNull("Cannot get a not existing property", properties.getProperty("test"));
    }

    @Test
    public void testWeCanRetrieveAProperty() {
        String value = properties.getProperty("aProperty");

        assertEquals("Cannot retrieve a property", value, "aValue");
    }

}
