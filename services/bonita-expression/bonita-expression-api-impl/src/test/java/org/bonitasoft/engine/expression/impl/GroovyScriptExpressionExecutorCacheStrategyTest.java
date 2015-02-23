package org.bonitasoft.engine.expression.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;

import org.bonitasoft.engine.cache.CacheConfiguration;
import org.bonitasoft.engine.cache.CacheConfigurations;
import org.bonitasoft.engine.cache.ehcache.EhCacheCacheService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
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

    private EhCacheCacheService cacheService;

    private GroovyScriptExpressionExecutorCacheStrategy groovyScriptExpressionExecutorCacheStrategy;

    @Before
    public void setup() throws Exception {
        CacheConfiguration cacheConfiguration = new CacheConfiguration();
        cacheConfiguration.setName("GROOVY_SCRIPT_CACHE_NAME");
        CacheConfigurations cacheConfigurations = new CacheConfigurations();
        cacheConfigurations.setConfigurations(Arrays.asList(cacheConfiguration));
        cacheService = new EhCacheCacheService(logger, sessionAccessor, cacheConfigurations);
        cacheService.start();
        groovyScriptExpressionExecutorCacheStrategy = new GroovyScriptExpressionExecutorCacheStrategy(cacheService, classLoaderService, logger);
        doReturn(GroovyScriptExpressionExecutorCacheStrategyTest.class.getClassLoader()).when(classLoaderService).getLocalClassLoader(anyString(), anyLong());
    }

    @Test
    public void should_getShell_return_a_shell_for_each_definition() throws Exception {
        // given

        // when
        GroovyShell shell1 = groovyScriptExpressionExecutorCacheStrategy.getShell(12l);
        GroovyShell shell2 = groovyScriptExpressionExecutorCacheStrategy.getShell(13l);

        // then
        assertThat(shell1).isNotNull();
        assertThat(shell2).isNotNull();
        assertThat(shell1).isNotEqualTo(shell2);
    }

    @Test
    public void should_getShell_return_same_shell_for_1_definition() throws Exception {
        // when
        GroovyShell shell1 = groovyScriptExpressionExecutorCacheStrategy.getShell(12l);
        GroovyShell shell2 = groovyScriptExpressionExecutorCacheStrategy.getShell(12l);

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
}
