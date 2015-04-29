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
package org.bonitasoft.engine.parameter.propertyfile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.cache.SCacheException;
import org.bonitasoft.engine.home.BonitaHomeServer;
import org.bonitasoft.engine.sessionaccessor.ReadSessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;

import org.bonitasoft.engine.parameter.OrderBy;
import org.bonitasoft.engine.parameter.SParameter;
import org.bonitasoft.engine.parameter.SParameterNameNotFoundException;
import org.bonitasoft.engine.parameter.SParameterProcessNotFoundException;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(BonitaHomeServer.class)
public class PropertyFileParameterServiceTest {

    private static final long P_ID = 123l;
    private static final long T_ID = 11;

    @Mock
    private BonitaHomeServer bonitaHomeServer;

    @Mock
    private ReadSessionAccessor sessionAccessor;

    // here the mock will always return null when get is called: it's like the cache store nothing
    @Mock
    private CacheService cacheService;

    @InjectMocks
    private PropertyFileParameterService propertyFileParameterService;

    @Before
    public void setUp() throws Exception {
        mockStatic(BonitaHomeServer.class);

        when(BonitaHomeServer.getInstance()).thenReturn(bonitaHomeServer);

        when(sessionAccessor.getTenantId()).thenReturn(T_ID);

    }

    private Properties getProperties(final Map<String, String> parameters) {
        final Properties properties = new Properties();
        for (final Map.Entry<String, String> parameter : parameters.entrySet()) {
            String value = parameter.getValue();
            if (parameter.getValue() == null) {
                value = PropertyFileParameterService.NULL;
            }
            properties.put(parameter.getKey(), value);
        }
        return properties;
    }
    @Test
    public void update() throws Exception {
        final Map<String, String> initial = Collections.<String, String> singletonMap("aParam", "paramValue");
        propertyFileParameterService.addAll(P_ID, initial);
        verify(bonitaHomeServer, times(1)).storeParameters(T_ID, P_ID, getProperties(initial));

        doReturn(getProperties(initial)).when(bonitaHomeServer).getParameters(T_ID, P_ID);
        propertyFileParameterService.update(P_ID, "aParam", "newValue");
        final Map<String, String> updated = Collections.<String, String> singletonMap("aParam", "newValue");
        verify(bonitaHomeServer, times(1)).storeParameters(T_ID, P_ID, getProperties(updated));


        doReturn(getProperties(updated)).when(bonitaHomeServer).getParameters(T_ID, P_ID);
        assertEquals("newValue", propertyFileParameterService.get(P_ID, "aParam").getValue());
    }

    @Test
    public void updateParameterWithNullValue() throws Exception {
        final Map<String, String> initial = Collections.<String, String> singletonMap("aParam", "paramValue");
        propertyFileParameterService.addAll(P_ID, initial);
        doReturn(getProperties(initial)).when(bonitaHomeServer).getParameters(T_ID, P_ID);
        propertyFileParameterService.update(P_ID, "aParam", null);

        assertEquals(null, propertyFileParameterService.get(P_ID, "aParam").getValue());
    }

    @Test(expected = SParameterNameNotFoundException.class)
    public void updateUnexistingParameter() throws Exception {
        doReturn(new Properties()).when(bonitaHomeServer).getParameters(T_ID, P_ID);
        propertyFileParameterService.update(P_ID, "aParam", "newValue");
    }

    @Test
    public void addAll() throws Exception {
        final Map<String, String> parameters = new HashMap<String, String>(3);
        parameters.put("param1", "value1");
        parameters.put("param2", "value2");
        parameters.put("param3", "value3");
        propertyFileParameterService.addAll(P_ID, parameters);
        final Properties properties = getProperties(parameters);
        doReturn(properties).when(bonitaHomeServer).getParameters(T_ID, P_ID);
        verify(bonitaHomeServer, times(1)).storeParameters(T_ID, P_ID, properties);
        assertEquals("value1", propertyFileParameterService.get(P_ID, "param1").getValue());
        assertEquals("value2", propertyFileParameterService.get(P_ID, "param2").getValue());
        assertEquals("value3", propertyFileParameterService.get(P_ID, "param3").getValue());
    }

    @Test
    public void deleteAll() throws Exception {
        propertyFileParameterService.addAll(P_ID, Collections.<String, String> singletonMap("param", "test"));
        doReturn(true).when(bonitaHomeServer).hasParameters(T_ID, P_ID);
        doReturn(true).when(bonitaHomeServer).deleteParameters(T_ID, P_ID);
        propertyFileParameterService.deleteAll(P_ID);
        doReturn(false).when(bonitaHomeServer).hasParameters(T_ID, P_ID);
        propertyFileParameterService.addAll(P_ID, null);
        doReturn(new Properties()).when(bonitaHomeServer).getParameters(T_ID, P_ID);
        final List<SParameter> list = propertyFileParameterService.get(P_ID, 0, 10, OrderBy.NAME_ASC);
        assertTrue(list.isEmpty());
    }

    @Test(expected = SParameterProcessNotFoundException.class)
    public void deleteAllWhenParametersDoesNotExists() throws Exception {
        doReturn(false).when(bonitaHomeServer).hasParameters(T_ID, P_ID);
        propertyFileParameterService.deleteAll(P_ID);
    }

    @Test
    public void containsNullValues() throws Exception {
        final Map<String, String> initial = Collections.<String, String> singletonMap("nullParam", null);
        propertyFileParameterService.addAll(P_ID, initial);
        doReturn(getProperties(initial)).when(bonitaHomeServer).getParameters(T_ID, P_ID);
        assertTrue(propertyFileParameterService.containsNullValues(P_ID));
    }

    @Test
    public void notContainsNullValues() throws Exception {
        final Map<String, String> initial = Collections.<String, String> singletonMap("param", "test");
        propertyFileParameterService.addAll(P_ID, initial);
        doReturn(getProperties(initial)).when(bonitaHomeServer).getParameters(T_ID, P_ID);
        assertFalse(propertyFileParameterService.containsNullValues(P_ID));
    }

    @Test(expected = SParameterProcessNotFoundException.class)
    public void cacheThrowException() throws Exception {
        doThrow(new SCacheException("exception")).when(cacheService).store(anyString(), anyString(), Matchers.<Properties> any(Properties.class));
        propertyFileParameterService.addAll(P_ID, Collections.<String, String> singletonMap("aParam", "paramValue"));
    }

    @Test
    public void getParameter() throws Exception {
        final Map<String, String> initial = Collections.<String, String> singletonMap("aParam", "paramValue");
        propertyFileParameterService.addAll(P_ID, initial);
        doReturn(getProperties(initial)).when(bonitaHomeServer).getParameters(T_ID, P_ID);

        final SParameter sParameter = propertyFileParameterService.get(P_ID, "aParam");
        assertEquals("paramValue", sParameter.getValue());
        verify(cacheService, atLeastOnce()).store(anyString(), anyString(), Matchers.<Properties> any(Properties.class));
        verify(cacheService).get(anyString(), anyString());
    }

    @Test
    public void getParameterWithCache() throws Exception {
        final Properties properties = new Properties();
        properties.put("aParam", "paramValue");
        when(cacheService.get(anyString(), anyString())).thenReturn(properties);
        final SParameter sParameter = propertyFileParameterService.get(P_ID, "aParam");
        assertEquals("paramValue", sParameter.getValue());
    }

    @Test(expected = SParameterProcessNotFoundException.class)
    public void getUnexistingParameter() throws Exception {
        final Map<String, String> initial = Collections.<String, String> singletonMap("otherParam", "paramValue");
        propertyFileParameterService.addAll(P_ID, initial);
        doReturn(getProperties(initial)).when(bonitaHomeServer).getParameters(T_ID, P_ID);
        propertyFileParameterService.get(P_ID, "aParam");
    }

    @Test
    public void getParameters() throws Exception {
        final Map<String, String> map = new HashMap<String, String>(3);
        map.put("c", "value3");
        map.put("a", "value1");
        map.put("b", "value2");
        propertyFileParameterService.addAll(P_ID, map);
        doReturn(getProperties(map)).when(bonitaHomeServer).getParameters(T_ID, P_ID);

        final List<SParameter> list = propertyFileParameterService.get(P_ID, 0, 10, OrderBy.NAME_ASC);
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
        propertyFileParameterService.addAll(P_ID, map);
        doReturn(getProperties(map)).when(bonitaHomeServer).getParameters(T_ID, P_ID);

        final List<SParameter> list = propertyFileParameterService.get(P_ID, 0, 10, OrderBy.NAME_DESC);
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
        propertyFileParameterService.addAll(P_ID, map);
        doReturn(getProperties(map)).when(bonitaHomeServer).getParameters(T_ID, P_ID);

        final List<SParameter> list = propertyFileParameterService.get(P_ID, 2, 2, OrderBy.NAME_ASC);
        assertEquals("value3", list.get(0).getValue());
        assertEquals("value4", list.get(1).getValue());

    }

    @Test
    public void getParametersPaginatedOutOfBound() throws Exception {
        final Map<String, String> map = new HashMap<String, String>(3);
        map.put("a", "value1");
        map.put("b", "value2");
        propertyFileParameterService.addAll(P_ID, map);
        doReturn(getProperties(map)).when(bonitaHomeServer).getParameters(T_ID, P_ID);

        final List<SParameter> list = propertyFileParameterService.get(P_ID, 2, 10, OrderBy.NAME_ASC);
        assertEquals(0, list.size());
    }

    @Test
    public void getNullValues() throws Exception {
        final Map<String, String> map = new HashMap<String, String>(3);
        map.put("nullParam", null);
        map.put("emptyParam", "");
        map.put("notEmptyParam", "value");
        propertyFileParameterService.addAll(P_ID, map);
        doReturn(getProperties(map)).when(bonitaHomeServer).getParameters(T_ID, P_ID);

        final List<SParameter> nullValues = propertyFileParameterService.getNullValues(P_ID, 0, 10, OrderBy.NAME_ASC);

        assertEquals(1, nullValues.size());
        assertEquals("nullParam", nullValues.get(0).getName());
        assertEquals(null, nullValues.get(0).getValue());
    }

}
