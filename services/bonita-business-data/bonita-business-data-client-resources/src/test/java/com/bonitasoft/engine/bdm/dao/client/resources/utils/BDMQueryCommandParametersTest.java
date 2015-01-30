/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.dao.client.resources.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.io.Serializable;
import java.util.Map;

import org.junit.Test;

import com.bonitasoft.engine.bdm.dao.client.resources.utils.BDMQueryCommandParameters;
import com.bonitasoft.engine.bdm.dao.client.resources.utils.EntityGetter;
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
