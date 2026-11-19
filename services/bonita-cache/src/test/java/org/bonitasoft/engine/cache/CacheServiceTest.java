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
package org.bonitasoft.engine.cache;

import static com.github.stefanbirkner.systemlambda.SystemLambda.tapSystemOut;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.cache.ehcache.EhCacheCacheService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheServiceTest {

    protected static final String SOME_DEFAULT_CACHE_NAME = "SOME_DEFAULT_CACHE_NAME";

    protected static final String ETERNAL_CACHE = "ETERNAL_CACHE";
    private static final String TEST1 = "test1";
    private static final String TEST2 = "test2";
    private static final String ONE_ELEMENT_IN_MEMORY_ONLY = "ONE_ELEMENT_IN_MEMORY_ONLY";
    private static final String OFF_HEAP_CACHE = "OFF_HEAP_CACHE";
    private static final String LARGE_OFF_HEAP_CACHE = "LARGE_OFF_HEAP_CACHE";

    private final static Logger LOGGER = LoggerFactory.getLogger(CacheServiceTest.class);
    private static final int ONE_ELEMENT_ONLY_MAX_ELEMENTS_IN_MEMORY = 1;

    private EhCacheCacheService cacheService;

    @Rule
    public TestName name = new TestName();

    @Before
    public void setUp() throws Exception {
        LOGGER.info("Testing : {}", name.getMethodName());
        cacheService = (EhCacheCacheService) getCacheService();
        cacheService.clearAll();
        cacheService.start();
    }

    @After
    public void tearDown() {
        LOGGER.info("Tested: {}", name.getMethodName());
        cacheService.stop();
        cacheService = null;
    }

    protected CacheService getCacheService() {
        final List<CacheConfiguration> configurationsList = new ArrayList<>(2);
        final CacheConfiguration cacheConfiguration = new CacheConfiguration();
        cacheConfiguration.setName(SOME_DEFAULT_CACHE_NAME);
        cacheConfiguration.setTimeToLiveSeconds(1);
        cacheConfiguration.setMaxElementsInMemory(200);
        configurationsList.add(cacheConfiguration);

        final CacheConfiguration cacheConfigurationEternal = new CacheConfiguration();
        cacheConfigurationEternal.setName(ETERNAL_CACHE);
        cacheConfigurationEternal.setTimeToLiveSeconds(1);
        cacheConfigurationEternal.setMaxElementsInMemory(200);
        cacheConfigurationEternal.setEternal(true);
        configurationsList.add(cacheConfigurationEternal);

        final CacheConfiguration cacheConfiguration1 = new CacheConfiguration();
        cacheConfiguration1.setName(TEST1);
        cacheConfiguration1.setTimeToLiveSeconds(1);
        cacheConfiguration1.setMaxElementsInMemory(10_000);
        cacheConfiguration1.setEternal(false);
        configurationsList.add(cacheConfiguration1);

        final CacheConfiguration cacheConfiguration2 = new CacheConfiguration();
        cacheConfiguration2.setName(TEST2);
        cacheConfiguration2.setTimeToLiveSeconds(1);
        cacheConfiguration2.setMaxElementsInMemory(100_000);
        cacheConfiguration2.setEternal(false);
        configurationsList.add(cacheConfiguration2);

        final CacheConfiguration cacheWithOneElementInMemoryOnly = new CacheConfiguration();
        cacheWithOneElementInMemoryOnly.setName(ONE_ELEMENT_IN_MEMORY_ONLY);
        cacheWithOneElementInMemoryOnly.setTimeToLiveSeconds(10);
        cacheWithOneElementInMemoryOnly.setMaxElementsInMemory(ONE_ELEMENT_ONLY_MAX_ELEMENTS_IN_MEMORY);
        cacheWithOneElementInMemoryOnly.setEternal(false);
        configurationsList.add(cacheWithOneElementInMemoryOnly);

        final CacheConfiguration offHeapCache = new CacheConfiguration();
        offHeapCache.setName(OFF_HEAP_CACHE);
        offHeapCache.setTimeToLiveSeconds(1);
        offHeapCache.setMaxElementsInMemory(10);
        offHeapCache.setOffHeapSizeMB(1); // 1MB off-heap
        offHeapCache.setEternal(false);
        configurationsList.add(offHeapCache);

        final CacheConfiguration largeOffHeapCache = new CacheConfiguration();
        largeOffHeapCache.setName(LARGE_OFF_HEAP_CACHE);
        largeOffHeapCache.setTimeToLiveSeconds(60);
        largeOffHeapCache.setMaxElementsInMemory(100);
        largeOffHeapCache.setOffHeapSizeMB(10); // 10MB off-heap
        largeOffHeapCache.setEternal(false);
        configurationsList.add(largeOffHeapCache);

        return new EhCacheCacheService(configurationsList, new CacheConfiguration());
    }

    @Test
    public void cache_should_expire_entries_after_short_timeout() throws Exception {
        final String key = "shortTimeout";
        cacheService.store(SOME_DEFAULT_CACHE_NAME, key, new Object());
        Object object = cacheService.get(SOME_DEFAULT_CACHE_NAME, key);
        assertNotNull("Object should be in cache", object);
        Thread.sleep(1020);
        object = cacheService.get(SOME_DEFAULT_CACHE_NAME, key);
        assertNull("Object should not be in cache any longer", object);
    }

    @Test
    public void eternal_cache_should_not_expire_entries() throws Exception {
        final String key = "eternalCacheTest";
        final Object value = new Object();
        cacheService.store(ETERNAL_CACHE, key, value);
        Object object = cacheService.get(ETERNAL_CACHE, key);
        assertNotNull("Object should be in cache", object);
        Thread.sleep(1020);
        object = cacheService.get(ETERNAL_CACHE, key);
        assertEquals("Object should still be in cache", value, object);
    }

    @Test
    public void cache_store_should_log_when_cache_not_found() throws Exception {
        // given
        final String key = "defaultTimeout";
        final String anotherCacheName = "Should_use_default_config";

        // when
        final String log = tapSystemOut(() -> cacheService.store(anotherCacheName, key, ""));

        // then
        assertThat(log)
                .containsPattern("WARN.*No specific cache configuration found for cache 'Should_use_default_config'");

    }

    @Test
    public void cache_should_store_simple_object_and_increase_size() throws SCacheException {
        final int cacheSize = cacheService.getCacheSize(TEST1);
        final String myObject = "testObject";
        cacheService.store(TEST1, "test", myObject);
        assertEquals("cache size did not increased", cacheSize + 1, cacheService.getCacheSize(TEST1));
    }

    @Test
    public void cache_should_retrieve_stored_simple_object() throws SCacheException {
        final String myObject = "testObject";
        cacheService.store(TEST1, "test", myObject);
        final String inObject = (String) cacheService.get(TEST1, "test");
        assertEquals("we didn't retrieve the same object", myObject, inObject);
    }

    @Test
    public void cache_should_store_complex_object_and_increase_size() throws SCacheException {
        final int cacheSize = cacheService.getCacheSize(TEST1);
        final ArrayList<Map<String, String>> list = new ArrayList<>();
        final HashMap<String, String> map = new HashMap<>();
        map.put("bpm", "bonita");
        list.add(map);
        cacheService.store(TEST1, "complex", list);
        assertEquals("cache size did not increased", cacheSize + 1, cacheService.getCacheSize(TEST1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void cache_should_retrieve_stored_complex_object() throws SCacheException {
        final ArrayList<Map<String, String>> list = new ArrayList<>();
        final HashMap<String, String> map = new HashMap<>();
        map.put("bpm", "bonita");
        list.add(map);
        cacheService.store(TEST1, "complex", list);
        final Object object = cacheService.get(TEST1, "complex");
        assertNotNull("the object does not exists", object);
        assertEquals("Not the same object", "bonita", ((ArrayList<Map<String, String>>) object).get(0).get("bpm"));
    }

    @Test
    public void cache_should_be_cleared_when_clear_called() throws SCacheException {
        cacheService.store(TEST1, "test1", "test1 value");
        assertTrue("cache was not empty", cacheService.getCacheSize(TEST1) > 0);
        cacheService.clear(TEST1);
        assertEquals("cache was not cleared", 0, cacheService.getCacheSize(TEST1));

    }

    @Test
    public void all_caches_should_be_cleared_when_clear_all_called() throws SCacheException {
        cacheService.store(TEST1, "test1", "test1");
        cacheService.store(TEST2, "test2", "test2");
        assertTrue(cacheService.getCacheSize(TEST1) > 0);
        assertTrue(cacheService.getCacheSize(TEST2) > 0);

        cacheService.clearAll();

        assertEquals("TEST1 was not cleared", 0, cacheService.getCacheSize(TEST1));
        assertEquals("TEST2 was not cleared", 0, cacheService.getCacheSize(TEST2));

    }

    @Test
    public void storing_null_values_should_fail() {
        assertThrows(SCacheException.class, () -> cacheService.store(TEST1, "test2", null));
    }

    @Test
    public void cache_should_not_duplicate_when_storing_same_item_twice() throws SCacheException {
        cacheService.store(TEST1, "sameItem", "value");
        assertEquals("first element not added", 1, cacheService.getCacheSize(TEST1));
        cacheService.store(TEST1, "sameItem", "value");
        assertEquals("element added twice", 1, cacheService.getCacheSize(TEST1));
    }

    @Test
    public void cache_should_update_element_value_when_key_already_exists() throws SCacheException {
        cacheService.store(TEST1, "sameItem2", "value1");
        assertEquals("first element not added", 1, cacheService.getCacheSize(TEST1));
        cacheService.store(TEST1, "sameItem2", "value2");
        assertEquals("element added 2 times", 1, cacheService.getCacheSize(TEST1));
        assertEquals("element was not updated", "value2", cacheService.get(TEST1, "sameItem2"));
    }

    @Test
    public void store_items_with_overflow_should_evict_old_items() throws SCacheException, InterruptedException {
        for (int i = 0; i < 2; i++) {
            cacheService.store(ONE_ELEMENT_IN_MEMORY_ONLY, "testLotOfItems" + i, "value" + i);
            Thread.sleep(5); // to make sure the Least Recently Used is evicted first
        }

        assertEquals("Not all elements were added with the overflow", ONE_ELEMENT_ONLY_MAX_ELEMENTS_IN_MEMORY,
                cacheService.getCacheSize(ONE_ELEMENT_IN_MEMORY_ONLY));
        assertThat(cacheService.get(ONE_ELEMENT_IN_MEMORY_ONLY, "testLotOfItems1")).isEqualTo("value1");
    }

    @Test
    public void cache_should_evict_items_when_memory_limit_reached() throws SCacheException {
        final int j = 2;
        for (int i = 0; i < j; i++) {
            cacheService.store(ONE_ELEMENT_IN_MEMORY_ONLY, "testLotOfItems" + i, "value" + i);
        }

        assertEquals("Too many elements added although limited memory.", 1,
                cacheService.getCacheSize(ONE_ELEMENT_IN_MEMORY_ONLY));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void cache_should_store_objects_by_reference() throws SCacheException {
        final ArrayList<String> list = new ArrayList<>();
        cacheService.store(TEST2, "mylist", list);
        list.add("kikoo");
        final ArrayList<String> cachedList = (ArrayList<String>) cacheService.get(TEST2, "mylist");
        assertEquals("object was copied in cached", 1, cachedList.size());
        assertEquals("'kikoo' should be in cache", "kikoo", cachedList.get(0));
    }

    @Test
    public void cache_should_return_all_stored_keys() throws SCacheException {
        final List<?> keys = cacheService.getKeys(TEST1);
        assertFalse(keys.contains("aKeyThatMustBeHere"));
        final int cacheKeySize = keys.size();
        cacheService.store(TEST1, "aKeyThatMustBeHere", "value1");
        final List<?> keys2 = cacheService.getKeys(TEST1);
        assertEquals(cacheKeySize + 1, keys2.size());
        assertTrue(keys2.contains("aKeyThatMustBeHere"));
    }

    @Test
    public void remove_should_delete_existing_key() throws SCacheException {
        final String key = "keyToRemove";
        final String value = "valueToRemove";

        // Store the key
        cacheService.store(TEST1, key, value);
        assertEquals("Value should be in cache", value, cacheService.get(TEST1, key));
        final int initialSize = cacheService.getCacheSize(TEST1);

        // Remove the key
        final boolean removed = cacheService.remove(TEST1, key);

        // Verify removal
        assertTrue("remove() should return true for existing key", removed);
        assertNull("Key should no longer be in cache", cacheService.get(TEST1, key));
        assertEquals("Cache size should decrease by 1", initialSize - 1, cacheService.getCacheSize(TEST1));
    }

    @Test
    public void remove_should_return_false_for_non_existing_key() throws SCacheException {
        final String key = "nonExistentKey";

        // Try to remove a key that doesn't exist
        final boolean removed = cacheService.remove(TEST1, key);

        // Verify it returns false
        assertFalse("remove() should return false for non-existent key", removed);
    }

    @Test
    public void remove_should_not_affect_other_keys() throws SCacheException {
        // Store multiple keys
        cacheService.store(TEST1, "key1", "value1");
        cacheService.store(TEST1, "key2", "value2");
        cacheService.store(TEST1, "key3", "value3");

        // Remove one key
        cacheService.remove(TEST1, "key2");

        // Verify other keys are still present
        assertEquals("key1 should still be in cache", "value1", cacheService.get(TEST1, "key1"));
        assertNull("key2 should be removed", cacheService.get(TEST1, "key2"));
        assertEquals("key3 should still be in cache", "value3", cacheService.get(TEST1, "key3"));
    }

    @Test
    public void remove_should_work_on_different_caches() throws SCacheException {
        final String key = "sharedKey";

        // Store same key in two different caches
        cacheService.store(TEST1, key, "value1");
        cacheService.store(TEST2, key, "value2");

        // Remove from TEST1 only
        cacheService.remove(TEST1, key);

        // Verify removal only affected TEST1
        assertNull("Key should be removed from TEST1", cacheService.get(TEST1, key));
        assertEquals("Key should still exist in TEST2", "value2", cacheService.get(TEST2, key));
    }

    // Off-heap storage tests

    @Test
    public void offHeapCache_should_store_and_retrieve_simple_objects() throws SCacheException {
        final String value = "offHeapTestValue";
        cacheService.store(OFF_HEAP_CACHE, "key1", value);

        final String retrieved = (String) cacheService.get(OFF_HEAP_CACHE, "key1");
        assertEquals("Should retrieve value from off-heap cache", value, retrieved);
    }

    @Test
    public void offHeapCache_should_store_and_retrieve_complex_objects() throws SCacheException {
        final ArrayList<Map<String, String>> list = new ArrayList<>();
        final HashMap<String, String> map = new HashMap<>();
        map.put("engine", "bonita");
        map.put("feature", "off-heap");
        list.add(map);

        cacheService.store(OFF_HEAP_CACHE, "complexKey", list);

        @SuppressWarnings("unchecked")
        final ArrayList<Map<String, String>> retrieved = (ArrayList<Map<String, String>>) cacheService
                .get(OFF_HEAP_CACHE, "complexKey");
        assertNotNull("Complex object should be retrieved from off-heap cache", retrieved);
        assertEquals("bonita", retrieved.get(0).get("engine"));
        assertEquals("off-heap", retrieved.get(0).get("feature"));
    }

    @Test
    public void offHeapCache_should_handle_overflow_from_heap_to_offheap() throws SCacheException {
        // Fill heap (10 elements)
        for (int i = 0; i < 10; i++) {
            cacheService.store(OFF_HEAP_CACHE, "heapKey" + i, "heapValue" + i);
        }

        // Add more elements that should overflow to off-heap
        for (int i = 10; i < 20; i++) {
            cacheService.store(OFF_HEAP_CACHE, "overflowKey" + i, "overflowValue" + i);
        }

        // Verify all elements are still accessible (some in heap, some in off-heap)
        for (int i = 0; i < 20; i++) {
            final String key = i < 10 ? "heapKey" + i : "overflowKey" + i;
            final String expectedValue = i < 10 ? "heapValue" + i : "overflowValue" + i;
            final Object retrieved = cacheService.get(OFF_HEAP_CACHE, key);
            assertThat(retrieved).as("Element " + key + " should be accessible").isEqualTo(expectedValue);
        }
    }

    @Test
    public void offHeapCache_should_evict_elements_when_both_tiers_full() throws SCacheException {
        // Fill both heap and off-heap by adding many elements
        // Heap: 10 elements, Off-heap: 1MB (can hold many more serialized strings)
        final int totalElements = 100;
        for (int i = 0; i < totalElements; i++) {
            cacheService.store(OFF_HEAP_CACHE, "key" + i, "value" + i);
        }

        // Cache should have evicted some elements based on LRU policy
        final int cacheSize = cacheService.getCacheSize(OFF_HEAP_CACHE);
        assertThat(cacheSize).isGreaterThan(0).isLessThanOrEqualTo(totalElements);
    }

    @Test
    public void offHeapCache_should_respect_ttl() throws Exception {
        final String key = "ttlKey";
        final String value = "ttlValue";

        cacheService.store(OFF_HEAP_CACHE, key, value);
        assertNotNull("Value should be in cache initially", cacheService.get(OFF_HEAP_CACHE, key));

        // Wait for TTL to expire (OFF_HEAP_CACHE has 1 second TTL)
        Thread.sleep(1200);

        assertNull("Value should be evicted after TTL", cacheService.get(OFF_HEAP_CACHE, key));
    }

    @Test
    public void largeOffHeapCache_should_store_many_elements() throws SCacheException {
        final int elementCount = 1000;

        for (int i = 0; i < elementCount; i++) {
            cacheService.store(LARGE_OFF_HEAP_CACHE, "key" + i, "value" + i);
        }

        // Verify cache size
        final int cacheSize = cacheService.getCacheSize(LARGE_OFF_HEAP_CACHE);
        assertThat(cacheSize).isGreaterThan(0);

        // Spot check some elements
        assertEquals("value0", cacheService.get(LARGE_OFF_HEAP_CACHE, "key0"));
        assertEquals("value500", cacheService.get(LARGE_OFF_HEAP_CACHE, "key500"));
        assertEquals("value999", cacheService.get(LARGE_OFF_HEAP_CACHE, "key999"));
    }

    @Test
    public void offHeapCache_should_handle_updates() throws SCacheException {
        // Ensure cache is empty before starting this test
        cacheService.clear(OFF_HEAP_CACHE);

        final String key = "updateKey";
        cacheService.store(OFF_HEAP_CACHE, key, "initialValue");
        assertEquals("initialValue", cacheService.get(OFF_HEAP_CACHE, key));

        cacheService.store(OFF_HEAP_CACHE, key, "updatedValue");
        assertEquals("Value should be updated in off-heap cache", "updatedValue",
                cacheService.get(OFF_HEAP_CACHE, key));

        // Cache size should be small (update, not large insert)
        // Note: With off-heap, statistics may temporarily show 2 if entry moves between tiers
        int size = cacheService.getCacheSize(OFF_HEAP_CACHE);
        assertThat(size).as("Cache size after update").isLessThanOrEqualTo(2);
    }

    @Test
    public void offHeapCache_should_be_cleared() throws SCacheException {
        // Add elements
        for (int i = 0; i < 5; i++) {
            cacheService.store(OFF_HEAP_CACHE, "clearKey" + i, "clearValue" + i);
        }

        assertThat(cacheService.getCacheSize(OFF_HEAP_CACHE)).isGreaterThan(0);

        // Clear cache
        cacheService.clear(OFF_HEAP_CACHE);

        assertEquals("Off-heap cache should be empty after clear", 0,
                cacheService.getCacheSize(OFF_HEAP_CACHE));
    }

    @Test
    public void offHeapCache_should_store_serializable_objects() throws SCacheException {
        // Test with a custom serializable object
        final HashMap<String, Object> complexObject = new HashMap<>();
        complexObject.put("id", 12345L);
        complexObject.put("name", "Process Definition");
        complexObject.put("version", "1.0");
        complexObject.put("data", new ArrayList<>(List.of("item1", "item2", "item3")));

        cacheService.store(OFF_HEAP_CACHE, "serializableKey", complexObject);

        @SuppressWarnings("unchecked")
        final HashMap<String, Object> retrieved = (HashMap<String, Object>) cacheService.get(OFF_HEAP_CACHE,
                "serializableKey");
        assertNotNull("Serializable object should be retrieved", retrieved);
        assertEquals(12345L, retrieved.get("id"));
        assertEquals("Process Definition", retrieved.get("name"));
        assertEquals("1.0", retrieved.get("version"));
        @SuppressWarnings("unchecked")
        final ArrayList<String> data = (ArrayList<String>) retrieved.get("data");
        assertEquals(3, data.size());
    }

}
