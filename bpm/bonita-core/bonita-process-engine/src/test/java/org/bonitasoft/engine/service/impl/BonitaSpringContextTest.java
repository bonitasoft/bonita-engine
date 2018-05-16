/**
 * Copyright (C) 2017 Bonitasoft S.A.
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Baptiste Mesta.
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { BonitaSpringContextTest.TestConfiguration.class })
public class BonitaSpringContextTest implements ApplicationContextAware {

    private BonitaSpringContext bonitaSpringContext;
    private ApplicationContext applicationContext;

    @Before
    public void before() throws Exception {
        bonitaSpringContext = new BonitaSpringContext(applicationContext);
        bonitaSpringContext.refresh();
    }

    @Test
    public void should_cache_beans_resolution_when_getting_bean_by_class() throws Exception {
        MyService firstCallToGetBean = bonitaSpringContext.getBean(MyService.class);
        MyService secondCallToGetBean = bonitaSpringContext.getBean(MyService.class);

        assertThat(firstCallToGetBean).isEqualTo(secondCallToGetBean);
    }

    @Test
    public void should_cache_beans_resolution_when_getting_bean_by_name() throws Exception {
        MyService firstCallToGetBean = bonitaSpringContext.getBean("ThisIsMyService", MyService.class);
        MyService secondCallToGetBean = bonitaSpringContext.getBean("ThisIsMyService", MyService.class);

        assertThat(firstCallToGetBean).isEqualTo(secondCallToGetBean);
    }

    @Test(expected = IllegalStateException.class)
    public void should_clear_cache_on_destroy() throws Exception {
        bonitaSpringContext.getBean("ThisIsMyService", MyService.class);

        bonitaSpringContext.destroy();

        bonitaSpringContext.getBean("ThisIsMyService", MyService.class);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Configuration
    static class TestConfiguration {

        @Bean(name = "ThisIsMyService")
        MyService myService() {
            return new MyService();
        }
    }

    private static class MyService {

    }
}
