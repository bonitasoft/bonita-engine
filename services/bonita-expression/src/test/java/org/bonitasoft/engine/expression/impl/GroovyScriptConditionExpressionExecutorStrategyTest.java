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

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.cache.CacheConfiguration;
import org.bonitasoft.engine.cache.ehcache.EhCacheCacheService;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.expression.ContainerState;
import org.bonitasoft.engine.expression.ExpressionExecutorStrategy;
import org.bonitasoft.engine.expression.model.impl.SExpressionImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GroovyScriptConditionExpressionExecutorStrategyTest {

    @Mock
    private ClassLoaderService classLoaderService;

    private EhCacheCacheService cacheService;

    private GroovyScriptConditionExpressionExecutorStrategy executorStrategy;

    private Map<String, Object> context;

    @Before
    public void setup() throws Exception {
        // Create valid cache configurations for Ehcache 3 (heap-only)
        final CacheConfiguration cacheConfiguration = new CacheConfiguration();
        cacheConfiguration.setName("GROOVY_SCRIPT_CACHE_NAME");
        cacheConfiguration.setMaxElementsInMemory(1000);

        final CacheConfiguration defaultCacheConfiguration = new CacheConfiguration();
        defaultCacheConfiguration.setMaxElementsInMemory(1000);

        final List<CacheConfiguration> cacheConfigurations = Collections.singletonList(cacheConfiguration);
        cacheService = new EhCacheCacheService(cacheConfigurations, defaultCacheConfiguration);
        cacheService.start();
        executorStrategy = new GroovyScriptConditionExpressionExecutorStrategy(cacheService, classLoaderService);
        doReturn(GroovyScriptExpressionExecutorCacheStrategyTest.class.getClassLoader()).when(classLoaderService)
                .getClassLoader(any());
        context = new HashMap<>();
        context.put(ExpressionExecutorStrategy.DEFINITION_ID, 123456789L);
    }

    @After
    public void stop() {
        cacheService.stop();
    }

    @Test
    public void should_return_a_true_boolean_value() throws Exception {
        //given
        final SExpressionImpl expression = new SExpressionImpl("myExpr", "'toto'", null, "java.lang.Boolean", null,
                Collections.emptyList());
        // when
        final Object evaluate = executorStrategy.evaluate(expression, context, emptyMap(), ContainerState.ACTIVE);

        // then
        assertThat(evaluate).isEqualTo(true);
    }

    @Test
    public void should_return_a_false_boolean_value() throws Exception {
        //given
        context.put("toto", null);
        final SExpressionImpl expression = new SExpressionImpl("myExpr", "toto", null, "java.lang.Boolean", null,
                Collections.emptyList());
        // when
        final Object evaluate = executorStrategy.evaluate(expression, context, emptyMap(), ContainerState.ACTIVE);

        // then
        assertThat(evaluate).isEqualTo(false);
    }

}
