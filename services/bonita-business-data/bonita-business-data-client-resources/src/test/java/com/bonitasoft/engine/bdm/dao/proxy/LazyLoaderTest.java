/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.bonitasoft.engine.bdm.dao.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

import org.bonitasoft.engine.session.APISession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.bdm.proxy.model.Employee;

/**
 * @author Romain Bioteau
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class LazyLoaderTest {

    @Mock
    private APISession apiSession;

    private LazyLoader lazyLoader;

    @Before
    public void setUp() throws Exception {
        lazyLoader = new LazyLoader(apiSession);
    }

    @Test
    public void should_getQueryNameForMethod_returns_a_valid_name() throws Exception {
        final Method method = Employee.class.getDeclaredMethod("getEmployees");
        
        Serializable name = lazyLoader.getQueryNameFor(method);
        
		assertThat(name).isEqualTo("Employee.findEmployeesByEmployeePersistenceId");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_createCommandParameters_with_List_returns_valid_parameters() throws Exception {
        final Method method = Employee.class.getDeclaredMethod("getEmployees");
        final Map<String, Serializable> commandParameters = lazyLoader.createCommandParameters(method, 12);
        assertThat(commandParameters).contains(entry("queryName", "Employee.findEmployeesByEmployeePersistenceId"));
        assertThat(commandParameters).contains(entry("returnType", java.util.List.class.getName()));
        assertThat(commandParameters).contains(entry("returnsList", Boolean.TRUE));
        assertThat(commandParameters).contains(entry("startIndex", 0));
        assertThat(commandParameters).contains(entry("maxResults", Integer.MAX_VALUE));
        assertThat(commandParameters).containsKey("queryParameters");
        assertThat((Map<String, Serializable>) commandParameters.get("queryParameters")).contains(entry("persistenceId", 12L));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_createCommandParameters_with_simple_ref_returns_valid_parameters() throws Exception {
        final Method method = Employee.class.getDeclaredMethod("getManager");
        final Map<String, Serializable> commandParameters = lazyLoader.createCommandParameters(method, 5);
        assertThat(commandParameters).contains(entry("queryName", "Employee.findManagerByEmployeePersistenceId"));
        assertThat(commandParameters).contains(entry("returnType", Employee.class.getName()));
        assertThat(commandParameters).contains(entry("returnsList", Boolean.FALSE));
        assertThat(commandParameters).contains(entry("startIndex", 0));
        assertThat(commandParameters).contains(entry("maxResults", Integer.MAX_VALUE));
        assertThat(commandParameters).containsKey("queryParameters");
        assertThat((Map<String, Serializable>) commandParameters.get("queryParameters")).contains(entry("persistenceId", 5L));
    }

}
