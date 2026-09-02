/**
 * Copyright (C) 2025 Bonitasoft S.A.
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
package org.bonitasoft.engine.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.bonitasoft.engine.properties.BonitaProperty;
import org.bonitasoft.engine.properties.BonitaPropertyAnnotationProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
class BonitaPropertyAnnotationProcessorTest {

    @Mock
    private Environment environment;

    @InjectMocks
    private BonitaPropertyAnnotationProcessor processor;

    @Test
    void should_PostProcessBeforeInitialization_set_BonitaProperty_value() throws Exception {
        // Create a test bean with a field annotated with @BonitaProperty
        TestBean testBean = new TestBean();

        // Mock the environment to return a value for the property
        when(environment.getProperty("test.property")).thenReturn("testValue");

        // Process the bean
        processor.postProcessBeforeInitialization(testBean, "testBean");

        // Verify that the field value was set correctly
        Field field = TestBean.class.getDeclaredField("property");
        field.setAccessible(true);
        assertEquals("testValue", field.get(testBean));
    }

    static class TestBean {

        @BonitaProperty("test.property")
        private String property;
    }
}
