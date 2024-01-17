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

import net.sf.ehcache.CacheManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Rohart Bastien
 */
public class CacheUtilTest {

    protected CacheManager cacheManager = null;

    @Before
    public void setUp() {
        cacheManager = CacheUtil.getCacheManager(System.getProperty("java.io.tmpdir"));
        assertNotNull("Cannot create cache", CacheUtil.createCache(cacheManager, cacheManager.getName()));
    }

    @After
    public void tearDown() {
        if (cacheManager != null) {
            CacheUtil.clear(cacheManager.getConfiguration().getDiskStoreConfiguration().getPath(),
                    cacheManager.getName());
        }
    }

    @Test
    public void testCreateCaches() {
        try {
            assertNotNull("Cannot create caches", CacheUtil.createCache(cacheManager, cacheManager.getName()));
        } finally {
            CacheUtil.clear(cacheManager.getConfiguration().getDiskStoreConfiguration().getPath(),
                    cacheManager.getName());
        }
    }

    @Test
    public void testStore() {
        CacheUtil.store(cacheManager.getConfiguration().getDiskStoreConfiguration().getPath(), cacheManager.getName(),
                new String("testStoreKey"), new String("testStoreValue"));
        assertNotNull("Cannot store", cacheManager.getCache(cacheManager.getName()));
    }

    @Test
    public void testGet() {
        CacheUtil.store(cacheManager.getConfiguration().getDiskStoreConfiguration().getPath(), cacheManager.getName(),
                new String("testStoreKey"), new String("testStoreValue"));
        assertNotNull("Cannot get the element in the cache",
                CacheUtil.get(cacheManager.getConfiguration().getDiskStoreConfiguration().getPath(),
                        cacheManager.getName(),
                        "testStoreKey"));
    }

    @Test
    public void testClear() {
        CacheUtil.clear(cacheManager.getConfiguration().getDiskStoreConfiguration().getPath(), cacheManager.getName());
        assertNull("Cannot clear the cache",
                CacheUtil.get(cacheManager.getName(),
                        cacheManager.getConfiguration().getDiskStoreConfiguration().getPath(),
                        "testStoreKey"));
    }

}
