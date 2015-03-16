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
package org.bonitasoft.engine.bdm.dao.client.resources.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.Serializable;
import java.util.Map;

import org.bonitasoft.engine.bdm.proxy.model.Employee;
import org.junit.Test;


public class BDMQueryCommandParametersTest {

    @Test
    @SuppressWarnings("unchecked")
    public void should_create_bdm_command_parameters_for_method_returning_a_list() throws Exception {
        EntityGetter getter = new EntityGetter(Employee.class.getDeclaredMethod("getEmployees"));
        long persistenceId = 12L;
        
        Map<String, Serializable> commandParameters = BDMQueryCommandParameters.createCommandParameters(getter, persistenceId);
        
        assertThat(commandParameters).contains(
                entry("queryName", "Employee.findEmployeesByEmployeePersistenceId"),
                entry("returnType", java.util.List.class.getName()),
                entry("returnsList", Boolean.TRUE),
                entry("startIndex", 0),
                entry("maxResults", Integer.MAX_VALUE));
        assertThat((Map<String, Serializable>) commandParameters.get("queryParameters")).contains(entry("persistenceId", persistenceId));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_createCommandParameters_with_simple_ref_returns_valid_parameters() throws Exception {
        EntityGetter getter = new EntityGetter(Employee.class.getDeclaredMethod("getManager"));
        long persistenceId = 5L;
        
        Map<String, Serializable> commandParameters = BDMQueryCommandParameters.createCommandParameters(getter, persistenceId);
        
        assertThat(commandParameters).contains(
                entry("queryName", "Employee.findManagerByEmployeePersistenceId"),
                entry("returnType", Employee.class.getName()),
                entry("returnsList", Boolean.FALSE),
                entry("startIndex", 0),
                entry("maxResults", Integer.MAX_VALUE));
        assertThat((Map<String, Serializable>) commandParameters.get("queryParameters")).contains(entry("persistenceId", persistenceId));
    }
}
