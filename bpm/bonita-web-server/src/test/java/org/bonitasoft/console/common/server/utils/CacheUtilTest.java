/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.console.common.server.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.ehcache.CacheManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Rohart Bastien
 */
public class CacheUtilTest {

    protected CacheManager cacheManager = null;
    protected String cacheName = "testCache";

    @Before
    public void setUp() {
        cacheManager = CacheUtil.getCacheManager();
        assertNotNull("Cannot create cache", CacheUtil.createCache(cacheManager, cacheName));
    }

    @After
    public void tearDown() {
        if (cacheManager != null) {
            CacheUtil.clear(cacheName);
        }
    }

    @Test
    public void testCreateCaches() {
        try {
            assertNotNull("Cannot create caches", CacheUtil.createCache(cacheManager, cacheName));
        } finally {
            CacheUtil.clear(cacheName);
        }
    }

    @Test
    public void testStore() {
        CacheUtil.store(cacheName, "testStoreKey", "testStoreValue");
        assertNotNull("Cannot store", cacheManager.getCache(cacheName, Object.class, Object.class));
    }

    @Test
    public void testGet() {
        CacheUtil.store(cacheName, "testStoreKey", "testStoreValue");
        assertNotNull("Cannot get the element in the cache",
                CacheUtil.get(cacheName, "testStoreKey"));
    }

    @Test
    public void testClear() {
        CacheUtil.clear(cacheName);
        assertNull("Cannot clear the cache", CacheUtil.get(cacheName, "testStoreKey"));
    }

}
