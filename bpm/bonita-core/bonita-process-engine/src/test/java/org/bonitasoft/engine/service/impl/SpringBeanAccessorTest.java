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
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class SpringBeanAccessorTest {

    @Mock
    private BonitaHomeServer bonitaHomeServer;
    private SpringBeanAccessor springBeanAccessor;
    @Mock
    private BonitaSpringContext context;

    private Properties contextProperties = new Properties();
    private ConfigurableEnvironment environment = new MockEnvironment();

    @Before
    public void before() throws Exception {
        springBeanAccessor = createSpringBeanAccessor();
        doReturn(bonitaHomeServer).when(springBeanAccessor).getBonitaHomeServer();
        Properties platformInitProperties = new Properties();
        platformInitProperties.put("activeProfiles", "community");
        doReturn(platformInitProperties).when(bonitaHomeServer).getPlatformInitProperties();
        doReturn(context).when(springBeanAccessor).createSpringContext();
        doReturn(environment).when(context).getEnvironment();
        doReturn(true).when(springBeanAccessor).isCluster();
    }

    @Test
    public void should_getContext_call_init_first() throws Exception {
        //when
        springBeanAccessor.getContext();
        //then
        InOrder inOrder = inOrder(springBeanAccessor, bonitaHomeServer);
        inOrder.verify(springBeanAccessor).init();
        inOrder.verify(springBeanAccessor).createSpringContext();
    }

    private SpringBeanAccessor createSpringBeanAccessor() {
        SpringBeanAccessor springBeanAccessor = spy(new SpringBeanAccessor(null) {

            @Override
            protected Properties getProperties() throws IOException {
                return contextProperties;
            }

            @Override
            protected List<BonitaConfiguration> getConfigurationFromDatabase() throws IOException {
                return Collections.emptyList();
            }

            @Override
            protected List<String> getSpringFileFromClassPath(boolean cluster) {
                return Collections.emptyList();
            }
        });
        return springBeanAccessor;
    }

    @Test
    public void should_getContext_create_context_using_classpathResources() throws Exception {
        //given
        doReturn(Arrays.asList("classpathResource1", "classpathResource2")).when(springBeanAccessor).getSpringFileFromClassPath(anyBoolean());
        //when
        springBeanAccessor.getContext();
        //then
        InOrder inOrder = inOrder(context);
        inOrder.verify(context).addClassPathResource("classpathResource1");
        inOrder.verify(context).addClassPathResource("classpathResource2");
    }

    @Test
    public void should_getContext_create_context_using_BonitaConfiguration() throws Exception {
        //given
        BonitaConfiguration res1 = new BonitaConfiguration("res1", "c1".getBytes());
        BonitaConfiguration res2 = new BonitaConfiguration("res2", "c2".getBytes());
        doReturn(Arrays.asList(res1, res2)).when(springBeanAccessor).getConfigurationFromDatabase();
        //when
        springBeanAccessor.getContext();
        //then
        InOrder inOrder = inOrder(context);
        inOrder.verify(context).addByteArrayResource(res1);
        inOrder.verify(context).addByteArrayResource(res2);
    }

    @Test
    public void should_populate_environment_with_properties() throws Exception {
        contextProperties.setProperty("a.property", "itsValue");

        ApplicationContext context = springBeanAccessor.getContext();

        assertThat(context.getEnvironment().getProperty("a.property")).isEqualTo("itsValue");
    }

    @Test
    public void should_getPropertyWithPlaceholder_with_placeholder_return_system_property() throws Exception {
        //given
        Properties properties = new Properties();
        properties.setProperty("myTestProperty", "${system.test.property:default value if not set in system properties}");
        System.setProperty("system.test.property", "the value set in syst properties");
        //when
        String value = springBeanAccessor.getPropertyWithPlaceholder(properties, "myTestProperty", "default");

        //then
        assertThat(value).isEqualTo("the value set in syst properties");
    }

    @Test
    public void should_getPropertyWithPlaceholder_with_placeholder_return_default_value_from_property() throws Exception {
        //given
        Properties properties = new Properties();
        properties.setProperty("myTestProperty", "${system.test.property2:default value if not set in system properties}");
        //when
        String value = springBeanAccessor.getPropertyWithPlaceholder(properties, "myTestProperty", "default");

        //then
        assertThat(value).isEqualTo("default value if not set in system properties");
    }

    @Test
    public void should_getPropertyWithPlaceholder_without_placeholder_return_default_value() throws Exception {
        //given
        Properties properties = new Properties();
        //when
        String value = springBeanAccessor.getPropertyWithPlaceholder(properties, "myTestProperty", "default");

        //then
        assertThat(value).isEqualTo("default");
    }

    @Test
    public void should_getPropertyWithPlaceholder_without_placeholder_return_value() throws Exception {
        //given
        Properties properties = new Properties();
        properties.setProperty("myTestProperty", "toto}");
        //when
        String value = springBeanAccessor.getPropertyWithPlaceholder(properties, "myTestProperty", "default");

        //then
        assertThat(value).isEqualTo("toto}");
    }


}
