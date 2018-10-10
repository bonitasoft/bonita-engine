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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * @author Baptiste Mesta
 */
public class SpringBeanAccessorTest {

    private List<String> springFilesFromClasspath = new ArrayList<>();
    private List<BonitaConfiguration> configurationFromDatabase = new ArrayList<>();
    private SpringBeanAccessor springBeanAccessor;

    private Properties contextProperties = new Properties();

    @Rule
    public EnvironmentVariables envVar = new EnvironmentVariables();
    @Rule
    public RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties();

    private void createSpringContext() {
        springBeanAccessor = createSpringBeanAccessor();
    }

    private SpringBeanAccessor createSpringBeanAccessor() {
        FileSystemXmlApplicationContext parent = new FileSystemXmlApplicationContext();
        parent.refresh();
        return new SpringBeanAccessor(parent) {

            @Override
            protected Properties getProperties() {
                return contextProperties;
            }

            @Override
            protected List<BonitaConfiguration> getConfigurationFromDatabase() {
                return configurationFromDatabase;
            }

            @Override
            protected boolean isCluster() {
                return false;
            }

            @Override
            protected List<String> getSpringFileFromClassPath(boolean cluster) {
                return springFilesFromClasspath;
            }
        };
    }

    @Test
    public void should_getContext_create_context_using_classpathResources() {
        //given: these resources exists in the classpath (or else they are not added)
        springFilesFromClasspath.add("classpathresource1");
        springFilesFromClasspath.add("classpathresource2");
        createSpringContext();
        //when
        BonitaSpringContext context = (BonitaSpringContext) springBeanAccessor.getContext();
        //then: the resources in spring context are ClassPathResources with path as follow
        assertThat(asList(context.getConfigResources())).extracting("path").containsExactly("classpathresource1", "classpathresource2");
    }

    @Test
    public void should_create_context_using_BonitaConfiguration_as_byte_array_resources() {
        //given
        String xmlFile = "<beans xmlns=\"http://www.springframework.org/schema/beans\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.3.xsd\"/>";
        BonitaConfiguration res1 = new BonitaConfiguration("res1", xmlFile.getBytes());
        BonitaConfiguration res2 = new BonitaConfiguration("res2", xmlFile.getBytes());
        configurationFromDatabase.add(res1);
        configurationFromDatabase.add(res2);
        createSpringContext();
        //when
        BonitaSpringContext context = (BonitaSpringContext) springBeanAccessor.getContext();
        //then: the resources in spring context are ByteArrayResource with generated description as follow
        assertThat(asList(context.getConfigResources())).extracting("description")
                .containsExactly("Byte array resource [res1]", "Byte array resource [res2]");
    }

    @Test
    public void should_populate_environment_with_properties() {
        contextProperties.setProperty("a.property", "itsValue");
        createSpringContext();

        ApplicationContext context = springBeanAccessor.getContext();

        assertThat(context.getEnvironment().getProperty("a.property")).isEqualTo("itsValue");
    }

    @Test
    public void should_create_context_with_properties_from_database_that_override_env() {
        contextProperties.setProperty("myProperty", "databaseValue");
        envVar.set("myProperty", "envValue");
        createSpringContext();

        ApplicationContext context = springBeanAccessor.getContext();

        assertThat(context.getEnvironment().getProperty("myProperty")).isEqualTo("databaseValue");
    }
    @Test
    public void should_create_context_with_properties_from_database_that_override_system_properties() {
        contextProperties.setProperty("myProperty", "databaseValue");
        System.setProperty("myProperty", "sysPropValue");
        createSpringContext();

        ApplicationContext context = springBeanAccessor.getContext();

        assertThat(context.getEnvironment().getProperty("myProperty")).isEqualTo("databaseValue");
    }

    @Test
    public void should_getPropertyWithPlaceholder_with_placeholder_return_system_property() {
        //given
        createSpringContext();
        Properties properties = new Properties();
        properties.setProperty("myTestProperty", "${system.test.property:default value if not set in system properties}");
        System.setProperty("system.test.property", "the value set in syst properties");
        //when
        String value = springBeanAccessor.getPropertyWithPlaceholder(properties, "myTestProperty", "default");

        //then
        assertThat(value).isEqualTo("the value set in syst properties");
    }

    @Test
    public void should_getPropertyWithPlaceholder_with_placeholder_return_default_value_from_property() {
        //given
        createSpringContext();
        Properties properties = new Properties();
        properties.setProperty("myTestProperty", "${system.test.property2:default value if not set in system properties}");
        //when
        String value = springBeanAccessor.getPropertyWithPlaceholder(properties, "myTestProperty", "default");

        //then
        assertThat(value).isEqualTo("default value if not set in system properties");
    }

    @Test
    public void should_getPropertyWithPlaceholder_without_placeholder_return_default_value() {
        //given
        createSpringContext();
        Properties properties = new Properties();
        //when
        String value = springBeanAccessor.getPropertyWithPlaceholder(properties, "myTestProperty", "default");

        //then
        assertThat(value).isEqualTo("default");
    }

    @Test
    public void should_getPropertyWithPlaceholder_without_placeholder_return_value() {
        //given
        createSpringContext();
        Properties properties = new Properties();
        properties.setProperty("myTestProperty", "toto}");
        //when
        String value = springBeanAccessor.getPropertyWithPlaceholder(properties, "myTestProperty", "default");

        //then
        assertThat(value).isEqualTo("toto}");
    }


}
