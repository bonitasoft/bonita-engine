/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.parameter.propertyfile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.bonitasoft.engine.cache.PlatformCacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.parameter.OrderBy;
import com.bonitasoft.engine.parameter.SParameter;
import com.bonitasoft.engine.parameter.SParameterNameNotFoundException;
import com.bonitasoft.engine.parameter.SParameterProcessNotFoundException;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class PropertyFileParameterServiceTest {

    private static final long PROCESS_DEFINITION_ID = 123l;

    @Mock
    private ReadSessionAccessor sessionAccessor;

    // here the mock will always return null when get is called: it's like the cache store nothing
    @Mock
    private PlatformCacheService cacheService;

    @InjectMocks
    private PropertyFileParameterService propertyFileParameterService;

    private static File bonitaHomeFolder;

    private static File propertyFile;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        bonitaHomeFolder = File.createTempFile("bonita", "home");
        bonitaHomeFolder.delete();
        bonitaHomeFolder.mkdir();
        final File processes = new File(new File(new File(new File(new File(bonitaHomeFolder, "server"), "tenants"), "1"), "work"), "processes");
        final File process123 = new File(processes, "123");
        process123.mkdirs();
        System.setProperty("bonita.home", bonitaHomeFolder.getAbsolutePath());
        propertyFile = new File(process123, "current-parameters.properties");

    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        IOUtil.deleteDir(bonitaHomeFolder);
    }

    @Before
    public void setUp() throws Exception {
        when(sessionAccessor.getTenantId()).thenReturn(1l);
        propertyFile.delete();
        propertyFile.createNewFile();

    }

    @Test
    public void update() throws Exception {
        propertyFileParameterService.addAll(PROCESS_DEFINITION_ID, Collections.<String, String> singletonMap("aParam", "paramValue"));

        propertyFileParameterService.update(PROCESS_DEFINITION_ID, "aParam", "newValue");

        assertEquals("newValue", propertyFileParameterService.get(PROCESS_DEFINITION_ID, "aParam").getValue());
    }

    @Test
    public void updateParameterWithNullValuye() throws Exception {
        propertyFileParameterService.addAll(PROCESS_DEFINITION_ID, Collections.<String, String> singletonMap("aParam", "paramValue"));

        propertyFileParameterService.update(PROCESS_DEFINITION_ID, "aParam", null);

        assertEquals(null, propertyFileParameterService.get(PROCESS_DEFINITION_ID, "aParam").getValue());
    }

    @Test(expected = SParameterNameNotFoundException.class)
    public void updateUnexistingParameter() throws Exception {
        propertyFileParameterService.update(PROCESS_DEFINITION_ID, "aParam", "newValue");
    }

    @Test
    public void addAll() throws Exception {
        final Map<String, String> parameters = new HashMap<String, String>(3);
        parameters.put("param1", "value1");
        parameters.put("param2", "value2");
        parameters.put("param3", "value3");
        propertyFileParameterService.addAll(PROCESS_DEFINITION_ID, parameters);
        assertEquals("value1", propertyFileParameterService.get(PROCESS_DEFINITION_ID, "param1").getValue());
        assertEquals("value2", propertyFileParameterService.get(PROCESS_DEFINITION_ID, "param2").getValue());
        assertEquals("value3", propertyFileParameterService.get(PROCESS_DEFINITION_ID, "param3").getValue());
    }

    @Test
    public void deleteAll() throws Exception {
        propertyFileParameterService.addAll(PROCESS_DEFINITION_ID, Collections.<String, String> singletonMap("param", "test"));

        propertyFileParameterService.deleteAll(PROCESS_DEFINITION_ID);
        propertyFileParameterService.addAll(PROCESS_DEFINITION_ID, null);

        final List<SParameter> list = propertyFileParameterService.get(PROCESS_DEFINITION_ID, 0, 10, OrderBy.NAME_ASC);
        assertTrue(list.isEmpty());
    }

    @Test(expected = SParameterProcessNotFoundException.class)
    public void deleteAllWhenParametersDoesNotExists() throws Exception {
        propertyFile.delete();
        propertyFileParameterService.deleteAll(PROCESS_DEFINITION_ID);
    }

    @Test
    public void containsNullValues() throws Exception {
        propertyFileParameterService.addAll(PROCESS_DEFINITION_ID, Collections.<String, String> singletonMap("nullParam", null));

        assertTrue(propertyFileParameterService.containsNullValues(PROCESS_DEFINITION_ID));
    }

    @Test
    public void notContainsNullValues() throws Exception {
        propertyFileParameterService.addAll(PROCESS_DEFINITION_ID, Collections.<String, String> singletonMap("param", "test"));

        assertFalse(propertyFileParameterService.containsNullValues(PROCESS_DEFINITION_ID));
    }

    @Test(expected = SParameterProcessNotFoundException.class)
    public void cacheThrowException() throws Exception {
        doThrow(new SCacheException("exception")).when(cacheService).store(anyString(), anyString(), Matchers.<Properties> any(Properties.class));
        propertyFileParameterService.addAll(PROCESS_DEFINITION_ID, Collections.<String, String> singletonMap("aParam", "paramValue"));
    }

    @Test
    public void getParameter() throws Exception {
        propertyFileParameterService.addAll(PROCESS_DEFINITION_ID, Collections.<String, String> singletonMap("aParam", "paramValue"));

        final SParameter sParameter = propertyFileParameterService.get(PROCESS_DEFINITION_ID, "aParam");
        assertEquals("paramValue", sParameter.getValue());
        verify(cacheService, atLeastOnce()).store(anyString(), anyString(), Matchers.<Properties> any(Properties.class));
        verify(cacheService).get(anyString(), anyString());
    }

    @Test
    public void getParameterWithCache() throws Exception {
        final Properties properties = new Properties();
        properties.put("aParam", "paramValue");
        when(cacheService.get(anyString(), anyString())).thenReturn(properties);
        final SParameter sParameter = propertyFileParameterService.get(PROCESS_DEFINITION_ID, "aParam");
        assertEquals("paramValue", sParameter.getValue());
    }

    @Test(expected = SParameterProcessNotFoundException.class)
    public void getUnexistingParameter() throws Exception {
        propertyFileParameterService.addAll(PROCESS_DEFINITION_ID, Collections.<String, String> singletonMap("otherParam", "paramValue"));

        propertyFileParameterService.get(PROCESS_DEFINITION_ID, "aParam");
    }

    @Test
    public void getParameters() throws Exception {
        final Map<String, String> map = new HashMap<String, String>(3);
        map.put("c", "value3");
        map.put("a", "value1");
        map.put("b", "value2");
        propertyFileParameterService.addAll(PROCESS_DEFINITION_ID, map);

        final List<SParameter> list = propertyFileParameterService.get(PROCESS_DEFINITION_ID, 0, 10, OrderBy.NAME_ASC);
        assertEquals("value1", list.get(0).getValue());
        assertEquals("value2", list.get(1).getValue());
        assertEquals("value3", list.get(2).getValue());

    }

    @Test
    public void getParametersOrdered() throws Exception {
        final Map<String, String> map = new HashMap<String, String>(3);
        map.put("c", "value3");
        map.put("a", "value1");
        map.put("b", "value2");
        propertyFileParameterService.addAll(PROCESS_DEFINITION_ID, map);

        final List<SParameter> list = propertyFileParameterService.get(PROCESS_DEFINITION_ID, 0, 10, OrderBy.NAME_DESC);
        assertEquals("value3", list.get(0).getValue());
        assertEquals("value2", list.get(1).getValue());
        assertEquals("value1", list.get(2).getValue());

    }

    @Test
    public void getParametersPaginated() throws Exception {
        final Map<String, String> map = new HashMap<String, String>(3);
        map.put("a", "value1");
        map.put("b", "value2");
        map.put("c", "value3");
        map.put("d", "value4");
        map.put("e", "value5");
        propertyFileParameterService.addAll(PROCESS_DEFINITION_ID, map);

        final List<SParameter> list = propertyFileParameterService.get(PROCESS_DEFINITION_ID, 2, 2, OrderBy.NAME_ASC);
        assertEquals("value3", list.get(0).getValue());
        assertEquals("value4", list.get(1).getValue());

    }

    @Test
    public void getParametersPaginatedOutOfBound() throws Exception {
        final Map<String, String> map = new HashMap<String, String>(3);
        map.put("a", "value1");
        map.put("b", "value2");
        propertyFileParameterService.addAll(PROCESS_DEFINITION_ID, map);

        final List<SParameter> list = propertyFileParameterService.get(PROCESS_DEFINITION_ID, 2, 10, OrderBy.NAME_ASC);
        assertEquals(0, list.size());
    }

    @Test
    public void getNullValues() throws Exception {
        final Map<String, String> map = new HashMap<String, String>(3);
        map.put("nullParam", null);
        map.put("emptyParam", "");
        map.put("notEmptyParam", "value");
        propertyFileParameterService.addAll(PROCESS_DEFINITION_ID, map);

        final List<SParameter> nullValues = propertyFileParameterService.getNullValues(PROCESS_DEFINITION_ID, 0, 10, OrderBy.NAME_ASC);

        assertEquals(1, nullValues.size());
        assertEquals("nullParam", nullValues.get(0).getName());
        assertEquals(null, nullValues.get(0).getValue());
    }

}
