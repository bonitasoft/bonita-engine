package org.bonitasoft.engine.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.cache.ehcache.EhCacheCacheService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
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

    private static final String TEST1 = "test1";// with copy

    private static final String TEST2 = "test2";// without copy

    private final static Logger LOGGER = LoggerFactory.getLogger(CacheServiceTest.class);

    private CacheService cacheService;

    @Rule
    public TestName name = new TestName();

    @Before
    public void setUp() throws Exception {
        LOGGER.info("Testing : {}", name.getMethodName());
        cacheService = getCacheService();
        cacheService.clearAll();
        cacheService.start();
    }

    @After
    public void tearDown() throws SBonitaException {
        LOGGER.info("Tested: {}", name.getMethodName());
        cacheService.stop();
        cacheService = null;
    }

    protected CacheService getCacheService() {
        final List<CacheConfiguration> configurationsList = new ArrayList<CacheConfiguration>(2);
        CacheConfiguration cacheConfiguration = new CacheConfiguration();
        cacheConfiguration.setName(SOME_DEFAULT_CACHE_NAME);
        cacheConfiguration.setTimeToLiveSeconds(1);
        cacheConfiguration.setMaxElementsInMemory(100);
        cacheConfiguration.setMaxElementsInMemory(200);
        cacheConfiguration.setInMemoryOnly(true);
        configurationsList.add(cacheConfiguration);
        cacheConfiguration = new CacheConfiguration();
        cacheConfiguration.setName(ETERNAL_CACHE);
        cacheConfiguration.setTimeToLiveSeconds(1);
        cacheConfiguration.setMaxElementsInMemory(100);
        cacheConfiguration.setMaxElementsInMemory(200);
        cacheConfiguration.setInMemoryOnly(true);
        cacheConfiguration.setEternal(true);
        configurationsList.add(cacheConfiguration);
        final CacheConfigurations cacheConfigurations = new CacheConfigurations();
        cacheConfigurations.setConfigurations(configurationsList);
        return new EhCacheCacheService(new TechnicalLoggerService() {

            @Override
            public void log(final Class<?> callerClass, final TechnicalLogSeverity severity, final String message, final Throwable t) {
            }

            @Override
            public void log(final Class<?> callerClass, final TechnicalLogSeverity severity, final String message) {
            }

            @Override
            public void log(final Class<?> callerClass, final TechnicalLogSeverity severity, final Throwable t) {
            }

            @Override
            public boolean isLoggable(final Class<?> callerClass, final TechnicalLogSeverity severity) {
                return false;
            }
        }, new ReadSessionAccessor() {

            @Override
            public long getTenantId() {
                return 1;
            }

            @Override
            public long getSessionId() {
                return 1;
            }
        }, cacheConfigurations, this.getClass().getResource("/ehcache.xml"));
    }

    @Test
    public void shortTimeoutConfig() throws Exception {
        final String key = "shortTimeout";
        cacheService.store(SOME_DEFAULT_CACHE_NAME, key, new Object());
        Object object = cacheService.get(SOME_DEFAULT_CACHE_NAME, key);
        assertNotNull("Object should be in cache", object);
        Thread.sleep(1020);
        object = cacheService.get(SOME_DEFAULT_CACHE_NAME, key);
        assertNull("Object should not be in cache any longer", object);
    }

    @Test
    public void eternalCacheTest() throws Exception {
        final String key = "eternalCacheTest";
        final Object value = new Object();
        cacheService.store(ETERNAL_CACHE, key, value);
        Object object = cacheService.get(ETERNAL_CACHE, key);
        assertNotNull("Object should be in cache", object);
        Thread.sleep(1020);
        object = cacheService.get(ETERNAL_CACHE, key);
        assertEquals("Object should still be in cache", value, object);
    }

    @Test(expected = SCacheException.class)
    public void cacheNotDefined() throws Exception {
        final String key = "defaultTimeout";
        final String anotherCacheName = "Should_use_default_config";
        cacheService.store(anotherCacheName, key, new String());
    }

    @Test(expected = SCacheException.class)
    public void defaultConfigNotSerializablePutObject() throws SCacheException {
        final String cacheName = "Should_use_default_config";
        final String key = "someObjectCacheKey";
        // We cannot store non-Serializable objects if copyOnRead or copyOnWrite is set to true:
        cacheService.store(cacheName, key, new Object());
    }

    @Test
    public void testPutSimpleObjectInCache() throws SCacheException {
        final int cacheSize = cacheService.getCacheSize(TEST1);
        final String myObject = "testObject";
        cacheService.store(TEST1, "test", myObject);
        assertEquals("cache size did not increased", cacheSize + 1, cacheService.getCacheSize(TEST1));
    }

    @Test
    public void testGetSimpleObjectInCache() throws SCacheException {
        final String myObject = "testObject";
        cacheService.store(TEST1, "test", myObject);
        final String inObject = (String) cacheService.get(TEST1, "test");
        assertEquals("we didn't retrieve the same object", myObject, inObject);
    }

    @Test
    public void testPutComplexObjectInCache() throws SCacheException {
        final int cacheSize = cacheService.getCacheSize(TEST1);
        final ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put("bpm", "bonita");
        list.add(map);
        cacheService.store(TEST1, "complex", list);
        assertEquals("cache size did not increased", cacheSize + 1, cacheService.getCacheSize(TEST1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetComplexObjectInCache() throws SCacheException {
        final ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put("bpm", "bonita");
        list.add(map);
        cacheService.store(TEST1, "complex", list);
        final Object object = cacheService.get(TEST1, "complex");
        assertNotNull("the object does not exists", object);
        assertEquals("Not the same object", "bonita", ((ArrayList<Map<String, String>>) object).get(0).get("bpm"));
    }

    @Test
    public void testClearTestCache() throws SCacheException {
        cacheService.store(TEST1, "test1", "test1 value");
        assertTrue("cache was not empty", cacheService.getCacheSize(TEST1) > 0);
        cacheService.clear(TEST1);
        assertTrue("cache was not cleared", cacheService.getCacheSize(TEST1) == 0);

    }

    @Test
    public void testClearAllCache() throws SCacheException {
        cacheService.store(TEST1, "test1", "test1");
        assertTrue("cache was not empty", cacheService.getCacheSize(TEST1) > 0);
        cacheService.clearAll();
        assertTrue("cache was not cleared", cacheService.getCacheSize(TEST1) == 0);
    }

    @Test
    public void testPutNullInCacheShouldWork() throws SCacheException {
        cacheService.store(TEST1, "test2", null);
        assertEquals("Null should be added to the cache", 1, cacheService.getCacheSize(TEST1));
        assertTrue("Null value can't be put", cacheService.get(TEST1, "test2") == null);
    }

    @Test
    public void testPutInDifferentCache() throws SCacheException {
        final int cacheSize = cacheService.getCacheSize(TEST1);
        cacheService.store(TEST2, "testdifferentCache", "value");
        assertEquals("element added in the wrong cache", cacheSize, cacheService.getCacheSize(TEST1));
    }

    @Test
    public void testPutSameItemTwice() throws SCacheException {
        cacheService.store(TEST1, "sameItem", "value");
        assertEquals("first element not added", 1, cacheService.getCacheSize(TEST1));
        cacheService.store(TEST1, "sameItem", "value");
        assertEquals("element added twice", 1, cacheService.getCacheSize(TEST1));
    }

    @Test
    public void testUpdateElementInCache() throws SCacheException {
        cacheService.store(TEST1, "sameItem2", "value1");
        assertEquals("first element not added", 1, cacheService.getCacheSize(TEST1));
        cacheService.store(TEST1, "sameItem2", "value2");
        assertEquals("element added 2 times", 1, cacheService.getCacheSize(TEST1));
        assertEquals("element was not updated", "value2", cacheService.get(TEST1, "sameItem2"));
    }

    @Test
    public void testPutLotOfItems() throws SCacheException {
        cacheService.store(TEST2, "testLotOfItems0", "value0");
        final int j = 20000;
        for (int i = 1; i <= j; i++) {
            cacheService.store(TEST2, "testLotOfItems" + i, "value" + i);
        }
        assertEquals("Not all elements were added", j + 1, cacheService.getCacheSize(TEST2));
    }

    @Test
    public void testPutLotOfItemsWithOverflow() throws SCacheException {
        final int cacheSize = cacheService.getCacheSize(TEST1);
        final int j = 20000;
        for (int i = 0; i < j; i++) {
            cacheService.store(TEST1, "testLotOfItems" + i, "value" + i);
        }
        assertEquals("Not all elements were added with the overflow", cacheSize + j, cacheService.getCacheSize(TEST1));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testChangeCachedElementWithCopy() throws SCacheException {
        final ArrayList<String> list = new ArrayList<String>();
        cacheService.store(TEST1, "mylist", list);
        list.add("kikoo");
        final ArrayList<String> cachedList = (ArrayList<String>) cacheService.get(TEST1, "mylist");
        assertTrue("object was not copied in cache", cachedList.size() == 0);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testChangeCachedElementWithoutCopy() throws SCacheException {
        final ArrayList<String> list = new ArrayList<String>();
        cacheService.store(TEST2, "mylist", list);
        list.add("kikoo");
        final ArrayList<String> cachedList = (ArrayList<String>) cacheService.get(TEST2, "mylist");
        assertTrue("object was copied in cached", cachedList.size() == 1);
    }

    @Test
    public void getKeysOfACache() throws SCacheException {
        final List<?> keys = cacheService.getKeys(TEST1);
        assertFalse(keys.contains("aKeyThatMustBeHere"));
        final int cacheKeySize = keys.size();
        cacheService.store(TEST1, "aKeyThatMustBeHere", "value1");
        final List<?> keys2 = cacheService.getKeys(TEST1);
        assertEquals(cacheKeySize + 1, keys2.size());
        assertTrue(keys2.contains("aKeyThatMustBeHere"));
    }

}
