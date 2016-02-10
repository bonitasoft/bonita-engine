/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.cache.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

public class CommonClusteredCacheServiceTest {

    CacheService cacheService;

    IMap clusteredCache;

    private Manager manager;

    @Before
    public void setUp() {
        final HazelcastInstance hazelcastInstance = mock(HazelcastInstance.class);
        clusteredCache = mock(IMap.class);
        manager = mock(Manager.class);
        when(hazelcastInstance.getMap(anyString())).thenReturn(clusteredCache);

        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final ReadSessionAccessor sessionAccessor = mock(ReadSessionAccessor.class);

        when(manager.isFeatureActive(Features.ENGINE_CLUSTERING)).thenReturn(true);
        cacheService = new ClusteredCacheServiceExt(manager, logger, sessionAccessor, hazelcastInstance);
    }

    // TODO test with exceptions + clear all + get cache names

    @SuppressWarnings("unchecked")
    @Test
    public void testStore() throws Exception {
        cacheService.store("myCache", "myKey", "myValue");

        verify(clusteredCache).put("myKey", "myValue");
    }

    @Test
    public void testGet() throws Exception {
        when(clusteredCache.get("myKey")).thenReturn("myValue");

        assertEquals("myValue", cacheService.get("myCache", "myKey"));
    }

    @Test
    public void testGetKeys() throws Exception {
        when(clusteredCache.keySet()).thenReturn(new HashSet<String>(Arrays.asList("myKey1", "myKey2", "myKey3")));

        final List<?> keys = cacheService.getKeys("myCache");
        assertEquals(3, keys.size());
        assertTrue(keys.contains("myKey1"));
        assertTrue(keys.contains("myKey2"));
        assertTrue(keys.contains("myKey3"));
    }

    @Test
    public void testRemove() throws Exception {
        cacheService.remove("myCache", "myKey");

        verify(clusteredCache).remove("myKey");
    }

    @Test
    public void testClear() throws Exception {
        cacheService.clear("myCache");

        verify(clusteredCache).clear();
    }

    @Test
    public void testGetCacheSize() throws Exception {
        when(clusteredCache.size()).thenReturn(10);

        assertEquals(10, cacheService.getCacheSize("myCache"));
    }

    @Test(expected = IllegalStateException.class)
    public void protectedService() {
        when(manager.isFeatureActive(Features.ENGINE_CLUSTERING)).thenReturn(false);
        cacheService = new ClusteredCacheServiceExt(manager, null, null, null);
    }

}
