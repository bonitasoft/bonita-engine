/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.expression.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.cache.CacheConfiguration;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.cache.ehcache.EhCacheCacheService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import groovy.lang.GroovyShell;

@RunWith(MockitoJUnitRunner.class)
public class GroovyScriptExpressionExecutorCacheStrategyTest {

    @Mock
    private ClassLoaderService classLoaderService;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private ReadSessionAccessor sessionAccessor;

    @Mock
    private CacheConfiguration defaultCacheConfiguration;

    private EhCacheCacheService cacheService;

    private GroovyScriptExpressionExecutorCacheStrategy groovyScriptExpressionExecutorCacheStrategy;

    private final static String diskStorePath = IOUtil.TMP_DIRECTORY + File.separator + GroovyScriptExpressionExecutorCacheStrategyTest.class.getSimpleName();
    private Class script2;

    @Before
    public void setup() throws Exception {
        final CacheConfiguration cacheConfiguration = new CacheConfiguration();
        cacheConfiguration.setName("GROOVY_SCRIPT_CACHE_NAME");
        final List<CacheConfiguration> cacheConfigurations = Arrays.asList(cacheConfiguration);
        cacheService = new EhCacheCacheService(logger, sessionAccessor, cacheConfigurations, defaultCacheConfiguration, diskStorePath);
        cacheService.start();
        groovyScriptExpressionExecutorCacheStrategy = new GroovyScriptExpressionExecutorCacheStrategy(cacheService, classLoaderService, logger);
        doReturn(GroovyScriptExpressionExecutorCacheStrategyTest.class.getClassLoader()).when(classLoaderService).getLocalClassLoader(anyString(), anyLong());
    }

    @After
    public void teardown() {
        cacheService.stop();
    }

    @AfterClass
    public static void cleanupClass() throws IOException {
        IOUtil.deleteDir(new File(diskStorePath));
    }

    @Test
    public void should_getShell_return_a_shell_for_each_definition() throws Exception {
        // given

        // when
        final GroovyShell shell1 = groovyScriptExpressionExecutorCacheStrategy.getShell(12l);
        final GroovyShell shell2 = groovyScriptExpressionExecutorCacheStrategy.getShell(13l);

        // then
        assertThat(shell1).isNotNull();
        assertThat(shell2).isNotNull();
        assertThat(shell1).isNotEqualTo(shell2);
    }
    @Test
    public void should_getShell_return_same_shell_for_1_definition() throws Exception {
        // when
        final GroovyShell shell1 = groovyScriptExpressionExecutorCacheStrategy.getShell(12l);
        final GroovyShell shell2 = groovyScriptExpressionExecutorCacheStrategy.getShell(12l);

        // then
        assertThat(shell1).isNotNull();
        assertThat(shell1).isEqualTo(shell2);
    }


    @Test
    public void should_getScriptFromCache_return_a_the_same_script_class_if_in_same_definition() throws Exception {
        // when
        final Class script1 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent", 12l);
        final Class script2 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent", 12l);

        // then
        assertThat(script1).isNotNull();
        assertThat(script1).isEqualTo(script2);

    }

    @Test
    public void should_getScriptFromCache_should_cache_only_once_when_on_different_threads() throws Exception {
        // when
        final Class script1 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent", 12l);

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    script2 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent", 12l);
                } catch (SCacheException e) {
                    e.printStackTrace();
                } catch (SClassLoaderException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        thread.join();

        // then
        assertThat(script1).isNotNull();
        assertThat(script2).isNotNull();
        assertThat(script1).isEqualTo(script2);

    }

    @Test
    public void should_getScriptFromCache_return_different_script_if_different_definition() throws Exception {
        // when
        final Class script1 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent", 12l);
        final Class script2 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent", 13l);

        // then
        assertThat(script1).isNotNull();
        assertThat(script2).isNotNull();
        assertThat(script1).isNotEqualTo(script2);
    }

    @Test
    public void should_getScriptFromCache_return_different_script_if_content_is_different_definition() throws Exception {
        // when
        final Class script1 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent1", 12l);
        final Class script2 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent2", 12l);

        // then
        assertThat(script1).isNotNull();
        assertThat(script2).isNotNull();
        assertThat(script1).isNotEqualTo(script2);
    }

    @Test(expected = SBonitaRuntimeException.class)
    public void should_not_put_in_cache_script_without_definition_id() throws Exception {

        // when
        groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent1", null);

        // then
        //exception
    }
}
