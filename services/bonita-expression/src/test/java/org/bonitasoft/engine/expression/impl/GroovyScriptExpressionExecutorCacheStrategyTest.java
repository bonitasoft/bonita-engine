/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.expression.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import groovy.lang.GroovyShell;
import org.bonitasoft.engine.cache.CacheConfiguration;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.cache.ehcache.EhCacheCacheService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.classloader.SClassLoaderException;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.ExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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

    private final static String diskStorePath = IOUtil.TMP_DIRECTORY + File.separator
            + GroovyScriptExpressionExecutorCacheStrategyTest.class.getSimpleName();
    private Class script2;
    private Map<String, Object> context;

    @Before
    public void setup() throws Exception {
        final CacheConfiguration cacheConfiguration = new CacheConfiguration();
        cacheConfiguration.setName("GROOVY_SCRIPT_CACHE_NAME");
        final List<CacheConfiguration> cacheConfigurations = Collections.singletonList(cacheConfiguration);
        cacheService = new EhCacheCacheService(logger, cacheConfigurations, defaultCacheConfiguration, diskStorePath,
                1);
        cacheService.start();
        groovyScriptExpressionExecutorCacheStrategy = new GroovyScriptExpressionExecutorCacheStrategy(cacheService,
                classLoaderService, logger);
        doReturn(GroovyScriptExpressionExecutorCacheStrategyTest.class.getClassLoader()).when(classLoaderService)
                .getLocalClassLoader(anyString(), anyLong());
        context = new HashMap<>();
        context.put(ExpressionExecutorStrategy.DEFINITION_ID, 123456789L);
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
        final GroovyShell shell1 = groovyScriptExpressionExecutorCacheStrategy.getShell(12L);
        final GroovyShell shell2 = groovyScriptExpressionExecutorCacheStrategy.getShell(13L);

        // then
        assertThat(shell1).isNotNull();
        assertThat(shell2).isNotNull();
        assertThat(shell1).isNotEqualTo(shell2);
    }

    @Test
    public void should_update_on_classloader_listener_clear_shell_cache() throws Exception {
        // given

        // when
        final GroovyShell shell1 = groovyScriptExpressionExecutorCacheStrategy.getShell(12l);
        groovyScriptExpressionExecutorCacheStrategy.onUpdate(null);
        final GroovyShell shell2 = groovyScriptExpressionExecutorCacheStrategy.getShell(12l);

        // then
        assertThat(shell1).isNotNull();
        assertThat(shell2).isNotNull();
        assertThat(shell1).isNotEqualTo(shell2);
    }

    @Test
    public void should_destroy_on_classloader_listener_clear_shell_cache() throws Exception {
        // given

        // when
        final GroovyShell shell1 = groovyScriptExpressionExecutorCacheStrategy.getShell(12l);
        groovyScriptExpressionExecutorCacheStrategy.onDestroy(null);
        final GroovyShell shell2 = groovyScriptExpressionExecutorCacheStrategy.getShell(12l);

        // then
        assertThat(shell1).isNotNull();
        assertThat(shell2).isNotNull();
        assertThat(shell1).isNotEqualTo(shell2);
    }

    @Test
    public void should_getShell_return_same_shell_for_1_definition() throws Exception {
        // when
        final GroovyShell shell1 = groovyScriptExpressionExecutorCacheStrategy.getShell(12L);
        final GroovyShell shell2 = groovyScriptExpressionExecutorCacheStrategy.getShell(12L);

        // then
        assertThat(shell1).isNotNull();
        assertThat(shell1).isEqualTo(shell2);
    }

    @Test
    public void should_getScriptFromCache_return_a_the_same_script_class_if_in_same_definition() throws Exception {
        // when
        final Class script1 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent", 12L);
        final Class script2 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent", 12L);

        // then
        assertThat(script1).isNotNull();
        assertThat(script1).isEqualTo(script2);

    }

    @Test
    public void should_getScriptFromCache_should_cache_only_once_when_on_different_threads() throws Exception {
        // when
        final Class script1 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent", 12L);

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    script2 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent", 12L);
                } catch (SCacheException | SClassLoaderException e) {
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
        final Class script1 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent", 12L);
        final Class script2 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent", 13L);

        // then
        assertThat(script1).isNotNull();
        assertThat(script2).isNotNull();
        assertThat(script1).isNotEqualTo(script2);
    }

    @Test
    public void should_getScriptFromCache_return_different_script_if_content_is_different_definition()
            throws Exception {
        // when
        final Class script1 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent1", 12L);
        final Class script2 = groovyScriptExpressionExecutorCacheStrategy.getScriptFromCache("MyScriptContent2", 12L);

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

    @Test
    public void should_evaluate_return_the_evaluation() throws Exception {
        //given
        final SExpressionImpl expression = new SExpressionImpl("myExpr", "'toto'", null, "java.lang.String", null,
                Collections.<SExpression> emptyList());
        // when
        final Object evaluate = groovyScriptExpressionExecutorCacheStrategy.evaluate(expression, context,
                Collections.<Integer, Object> emptyMap(),
                ContainerState.ACTIVE);

        // then
        assertThat(evaluate).isEqualTo("toto");
    }

    @Test(expected = SExpressionEvaluationException.class)
    public void should_evaluate_throw_SExpressionEvaluationException_when_script_throws_Error() throws Exception {
        //given
        final SExpressionImpl expression = new SExpressionImpl("myExpr", "throw new java.lang.NoClassDefFoundError()",
                null, "java.lang.String", null,
                Collections.<SExpression> emptyList());
        // when
        groovyScriptExpressionExecutorCacheStrategy.evaluate(expression, context,
                Collections.<Integer, Object> emptyMap(), ContainerState.ACTIVE);

        // then
        //exception
    }

    @Test(expected = SExpressionEvaluationException.class)
    public void should_evaluate_throw_SExpressionEvaluationException_when_script_throws_Throwable() throws Exception {
        //given
        final SExpressionImpl expression = new SExpressionImpl("myExpr", "throw new java.lang.Throwable()", null,
                "java.lang.String", null,
                Collections.<SExpression> emptyList());
        // when
        groovyScriptExpressionExecutorCacheStrategy.evaluate(expression, context,
                Collections.<Integer, Object> emptyMap(), ContainerState.ACTIVE);
        // then
        //exception
    }
}
