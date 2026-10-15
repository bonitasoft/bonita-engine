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

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.console.common.server.preferences.properties.ConfigurationFilesManager.getProperties;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Rohart Bastien
 */
public class ConsolePropertiesTest {

    private ConsoleProperties properties;

    @Before
    public void setUp() throws Exception {
        resetStaticPropertiesCache();
        properties = spy(new ConsoleProperties());
        doReturn(getProperties("aProperty=aValue".getBytes())).when(properties).getProperties();
    }

    // ConsoleProperties.getProperty() caches values in a static map for the JVM lifetime;
    // reset it so one test's mocked properties don't leak into the next.
    private static void resetStaticPropertiesCache() throws Exception {
        Field cacheField = ConsoleProperties.class.getDeclaredField("consoleProperties");
        cacheField.setAccessible(true);
        cacheField.set(null, null);
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

    @Test
    public void getAllowedAttachmentExtensions_should_return_configured_whitelist() throws IOException {
        doReturn(getProperties("form.attachment.allowed.extensions=pdf, docx ".getBytes())).when(properties)
                .getProperties();

        assertThat(properties.getAllowedAttachmentExtensions()).containsExactly("pdf", "docx");
    }

    @Test
    public void getAllowedAttachmentExtensions_should_fall_back_to_default_when_configured_value_is_malformed()
            throws IOException {
        doReturn(getProperties("form.attachment.allowed.extensions=, ,".getBytes())).when(properties)
                .getProperties();

        assertThat(properties.getAllowedAttachmentExtensions())
                .containsExactly("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "json", "jpg",
                        "jpeg", "png", "gif", "bmp");
    }

}
