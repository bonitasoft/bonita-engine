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
package org.bonitasoft.engine.cache.ehcache;

import static org.junit.Assert.assertTrue;

import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.bonitasoft.engine.cache.CacheConfiguration;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CommonEhCacheCacheServiceTest {

    @Mock
    private List<CacheConfiguration> cacheConfigurations;

    @Mock
    private CacheConfiguration defaultCacheConfiguration;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private ReadSessionAccessor sessionAccessor;

    @Mock
    private Cache cache;

    private EhCacheCacheService cacheService;

    @Before
    public void setup() {
        cacheService = new EhCacheCacheService(logger, sessionAccessor, cacheConfigurations, defaultCacheConfiguration, null) {

            @Override
            public synchronized void start() {
                cacheManager = CommonEhCacheCacheServiceTest.this.cacheManager;
            }
        };
    }

    @Test
    public void should_getKeys_return_empty_list_when_cache_manager_is_null() throws Exception {
        final List<Object> keys = cacheService.getKeys("unknownCache");

        assertTrue(keys.isEmpty());
    }

    @Test
    public void should_getKeys_return_empty_list_when_cache_manager_have_no_cache() throws Exception {
        cacheService.start();

        final List<Object> keys = cacheService.getKeys("unknownCache");

        assertTrue(keys.isEmpty());
    }

    // cache is final can't change methods... use powermock...
    // @Test
    // public void should_getKeys_return_cache_getKeys() throws Exception {
    // List<String> values = Arrays.asList("a", "b");
    // when(cacheManager.getCache("knownCache")).thenReturn(cache);
    // cacheService.start();
    // when(cache.getKeys()).thenReturn(values);
    //
    // List<Object> keys = cacheService.getKeys("knownCache");
    //
    // assertEquals(values, keys);
    // }

}
