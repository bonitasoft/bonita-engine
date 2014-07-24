package com.bonitasoft.engine.bdm.dao.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.Serializable;
import java.util.Map;

import org.junit.Test;

import com.bonitasoft.engine.bdm.proxy.model.Employee;


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
