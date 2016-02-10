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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.cache.CacheService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
@RunWith(MockitoJUnitRunner.class)
public class DelegatingCacheServiceTest {

    @Mock
    private CacheService distributedCache;

    @Mock
    private CacheService localCache;

    private DelegatingCacheService delegatingCacheService;

    @Before
    public void setup() {
        delegatingCacheService = new DelegatingCacheService(localCache, distributedCache, Collections.singleton("local"));
    }

    @Test
    public void storeLocal() throws Exception {
        delegatingCacheService.store("local", 1, "test");
        verifyZeroInteractions(distributedCache);
        verify(localCache).store("local", 1, "test");
    }

    @Test
    public void storeDistributed() throws Exception {
        delegatingCacheService.store("distributed", 1, "test");
        verifyZeroInteractions(localCache);
        verify(distributedCache).store("distributed", 1, "test");
    }

    @Test
    public void removeLocal() throws Exception {
        delegatingCacheService.remove("local", 1);
        verifyZeroInteractions(distributedCache);
        verify(localCache).remove("local", 1);
    }

    @Test
    public void removeDistributed() throws Exception {
        delegatingCacheService.remove("distributed", 1);
        verifyZeroInteractions(localCache);
        verify(distributedCache).remove("distributed", 1);
    }

    @Test
    public void getLocal() throws Exception {
        when(localCache.get("local", 1)).thenReturn("test");
        assertEquals("test", delegatingCacheService.get("local", 1));
        verifyZeroInteractions(distributedCache);
    }

    @Test
    public void getDistributed() throws Exception {
        when(distributedCache.get("distributed", 1)).thenReturn("test");
        assertEquals("test", delegatingCacheService.get("distributed", 1));
        verifyZeroInteractions(localCache);
    }

    @Test
    public void getKeysLocal() throws Exception {
        final List expected = Arrays.asList("a", "b");
        when(localCache.getKeys("local")).thenReturn(expected);
        final List<?> keys = delegatingCacheService.getKeys("local");
        assertTrue("expected <" + expected + "> had <" + keys + ">", expected.equals(keys));
        verifyZeroInteractions(distributedCache);
    }

    @Test
    public void getKeysDistributed() throws Exception {
        final List expected = Arrays.asList("a", "b");
        when(distributedCache.getKeys("distributed")).thenReturn(expected);
        final List<?> keys = delegatingCacheService.getKeys("distributed");
        assertTrue("expected <" + expected + "> had <" + keys + ">", expected.equals(keys));
        verifyZeroInteractions(localCache);
    }

    @Test
    public void clearLocal() throws Exception {
        delegatingCacheService.clear("local");
        verifyZeroInteractions(distributedCache);
        verify(localCache).clear("local");
    }

    @Test
    public void clearDistributed() throws Exception {
        delegatingCacheService.clear("distributed");
        verifyZeroInteractions(localCache);
        verify(distributedCache).clear("distributed");
    }

    @Test
    public void clearAll() throws Exception {
        delegatingCacheService.clearAll();
        verify(distributedCache).clearAll();
        verify(distributedCache).clearAll();
    }

    @Test
    public void getCacheSizeLocal() throws Exception {
        when(localCache.getCacheSize("local")).thenReturn(5);
        assertEquals(5, delegatingCacheService.getCacheSize("local"));
        verifyZeroInteractions(distributedCache);
    }

    @Test
    public void getCacheSizeDistributed() throws Exception {
        when(distributedCache.getCacheSize("distributed")).thenReturn(5);
        assertEquals(5, delegatingCacheService.getCacheSize("distributed"));
        verifyZeroInteractions(localCache);
    }

    @Test
    public void getCachesNames() {
        when(localCache.getCachesNames()).thenReturn(Arrays.asList("a", "c"));
        when(distributedCache.getCachesNames()).thenReturn(Arrays.asList("b", "d"));
        final List<String> cachesNames = delegatingCacheService.getCachesNames();
        final List<String> expected = Arrays.asList("a", "b", "c", "d");
        assertTrue("expected <" + expected + "> had <" + cachesNames + ">", expected.equals(cachesNames));
    }

}
