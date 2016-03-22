/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Properties;

import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class SpringPlatformFileSystemBeanAccessorTest {
    @Test
    public void should_getPropertyWithPlaceholder_with_placeholder_return_system_property() throws Exception {
        //given
        SpringPlatformFileSystemBeanAccessor springPlatformFileSystemBeanAccessor = new SpringPlatformFileSystemBeanAccessor(null);
        Properties properties = new Properties();
        properties.setProperty("myTestProperty", "${system.test.property:default value if not set in syst prop}");
        System.setProperty("system.test.property", "the value set in syst properties");
        //when
        String value = springPlatformFileSystemBeanAccessor.getPropertyWithPlaceholder(properties, "myTestProperty", "default");

        //then
        assertThat(value).isEqualTo("the value set in syst properties");
    }

    @Test
    public void should_getPropertyWithPlaceholder_with_placeholder_return_default_value_from_property() throws Exception {
        //given
        SpringPlatformFileSystemBeanAccessor springPlatformFileSystemBeanAccessor = new SpringPlatformFileSystemBeanAccessor(null);
        Properties properties = new Properties();
        properties.setProperty("myTestProperty", "${system.test.property2:default value if not set in syst prop}");
        //when
        String value = springPlatformFileSystemBeanAccessor.getPropertyWithPlaceholder(properties, "myTestProperty", "default");

        //then
        assertThat(value).isEqualTo("default value if not set in syst prop");
    }

    @Test
    public void should_getPropertyWithPlaceholder_without_placeholder_return_default_value() throws Exception {
        //given
        SpringPlatformFileSystemBeanAccessor springPlatformFileSystemBeanAccessor = new SpringPlatformFileSystemBeanAccessor(null);
        Properties properties = new Properties();
        //when
        String value = springPlatformFileSystemBeanAccessor.getPropertyWithPlaceholder(properties, "myTestProperty", "default");

        //then
        assertThat(value).isEqualTo("default");
    }

    @Test
    public void should_getPropertyWithPlaceholder_without_placeholder_return_value() throws Exception {
        //given
        SpringPlatformFileSystemBeanAccessor springPlatformFileSystemBeanAccessor = new SpringPlatformFileSystemBeanAccessor(null);
        Properties properties = new Properties();
        properties.setProperty("myTestProperty", "toto}");
        //when
        String value = springPlatformFileSystemBeanAccessor.getPropertyWithPlaceholder(properties, "myTestProperty", "default");

        //then
        assertThat(value).isEqualTo("toto}");
    }
}