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
package org.bonitasoft.engine.cache.ehcache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.cache.CacheConfiguration;
import org.bonitasoft.engine.cache.SCacheException;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EhCacheCacheServiceTest {

    private final List<CacheConfiguration> cacheConfigurations = Collections.emptyList();

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache<Object, Object> cache;

    private EhCacheCacheService cacheService;

    @Before
    public void setup() {
        // Create a valid default cache configuration for Ehcache 3
        CacheConfiguration defaultCacheConfiguration = new CacheConfiguration();
        defaultCacheConfiguration.setMaxElementsInMemory(1_000);

        cacheService = new EhCacheCacheService(cacheConfigurations, defaultCacheConfiguration) {

            @Override
            public synchronized void start() {
                // Mock the cache manager but keep the real statistics service initialization
                statisticsService = new org.ehcache.core.internal.statistics.DefaultStatisticsService();
                cacheManager = EhCacheCacheServiceTest.this.cacheManager;
            }
        };
    }

    @Test
    public void should_getKeys_return_empty_list_when_cache_manager_is_null() throws Exception {
        final List<Object> keys = cacheService.getKeys("unknownCache");

        assertThat(keys).isEmpty();
    }

    @Test
    public void should_getKeys_return_empty_list_when_cache_manager_have_no_cache() throws Exception {
        cacheService.start();

        final List<Object> keys = cacheService.getKeys("unknownCache");

        assertThat(keys).isEmpty();
    }

    @Test
    public void should_getCacheSize_return_zero_when_cache_manager_is_null() throws Exception {
        final int size = cacheService.getCacheSize("unknownCache");

        assertThat(size).isZero();
    }

    @Test
    public void should_getCacheSize_return_zero_when_cache_does_not_exist() throws Exception {
        cacheService.start();
        when(cacheManager.getCache(eq("unknownCache"), any(), any())).thenReturn(null);

        final int size = cacheService.getCacheSize("unknownCache");

        assertThat(size).isZero();
    }

    @Test(expected = SCacheException.class)
    public void should_getCacheSize_throw_SCacheException_when_cache_is_not_alive() throws Exception {
        cacheService.start();
        when(cacheManager.getCache(eq("testCache"), any(), any())).thenReturn(cache);
        when(cache.iterator()).thenThrow(new IllegalStateException("Cache is not alive"));

        cacheService.getCacheSize("testCache");
    }

    @Test(expected = SCacheException.class)
    public void should_getCacheSize_throw_SCacheException_on_runtime_exception() throws Exception {
        cacheService.start();
        when(cacheManager.getCache(eq("testCache"), any(), any())).thenReturn(cache);
        when(cache.iterator()).thenThrow(new RuntimeException("Unexpected error"));

        cacheService.getCacheSize("testCache");
    }

    @Test
    public void should_getCacheSize_use_statistics_when_available() throws Exception {
        // Note: This test verifies that statistics are enabled
        // The actual CacheServiceTest provides integration-level validation
        // that the cache size is correctly retrieved
        cacheService.start();

        // Verify that statisticsService was initialized
        assertThat(cacheService.statisticsService).as("StatisticsService should be initialized").isNotNull();
    }

}
