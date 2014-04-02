/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.bdm.BusinessObject;
import com.bonitasoft.engine.bdm.BusinessObjectModel;
import com.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.bdm.Field;
import com.bonitasoft.engine.bdm.FieldType;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;

/**
 * 
 * @author Romain Bioteau
 */
public class ExecuteBDMQueryCommandIT extends CommonAPISPTest {

    private static final String EXECUTE_BDM_QUERY_COMMAND = "executeBDMQuery";

    private static final String EMPLOYEE_QUALIF_CLASSNAME = "org.bonita.pojo.BonitaEmployee";

    private static final String RETURNS_LIST = "returnsList";

    private static final String QUERY_PARAMETERS = "queryParameters";

    private static final String RETURN_TYPE = "returnType";

    private static final String QUERY_NAME = "queryName";

    private BusinessObjectModel buildBOM() {
        final Field firstName = new Field();
        firstName.setName("firstName");
        firstName.setType(FieldType.STRING);
        firstName.setLength(Integer.valueOf(10));

        final Field lastName = new Field();
        lastName.setName("lastName");
        lastName.setType(FieldType.STRING);
        lastName.setNullable(Boolean.FALSE);

        final BusinessObject employee = new BusinessObject();
        employee.setQualifiedName(EMPLOYEE_QUALIF_CLASSNAME);
        employee.addField(firstName);
        employee.addField(lastName);
        employee.setDescription("Describe a simple employee");
        // employee.addUniqueConstraint("uk_fl", "firstName", "lastName");

        employee.addQuery("getEmployees", "SELECT e FROM BonitaEmployee e", List.class.getName());
        employee.addQuery("getEmployeeByFirstNameAndLastName", "SELECT e FROM BonitaEmployee e WHERE e.firstName=:firstName AND e.lastName=:lastName",
                EMPLOYEE_QUALIF_CLASSNAME);
        employee.addQuery("getEmployeeByLastName", "SELECT e FROM BonitaEmployee e WHERE e.lastName=:lastName", EMPLOYEE_QUALIF_CLASSNAME);
        final BusinessObjectModel model = new BusinessObjectModel();
        model.addBusinessObject(employee);
        return model;
    }

    protected User businessUser;

    private static boolean alreadyAdded = false;

    @Before
    public void beforeTest() throws Exception {
        login();
        businessUser = createUser(USERNAME, PASSWORD);
        logout();
        login();

        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        final byte[] zip = converter.zip(buildBOM());
        getTenantManagementAPI().pause();
        getTenantManagementAPI().installBusinessDataRepository(zip);
        getTenantManagementAPI().resume();
        logout();
        loginWith(USERNAME, PASSWORD);
        if (!alreadyAdded) {
            addEmployee("Romain", "Bioteau");
            addEmployee("Jules", "Bioteau");
            addEmployee("Matthieu", "Chaffotte");
            alreadyAdded = true;
        }
    }

    @After
    public void afterTest() throws BonitaException {
        logout();
        login();
        if (!getTenantManagementAPI().isPaused()) {
            getTenantManagementAPI().pause();
            getTenantManagementAPI().uninstallBusinessDataRepository();
            getTenantManagementAPI().resume();
        }
        deleteUser(businessUser.getId());
        logout();
    }

    @SuppressWarnings("unchecked")
    @Test
    @Ignore("Need to clean/remove operation on Business Data first")
    public void should_execute_returns_empty_list() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_NAME, "getEmployees");
        parameters.put(RETURNS_LIST, true);
        parameters.put(RETURN_TYPE, EMPLOYEE_QUALIF_CLASSNAME);
        Serializable result = getCommandAPI().execute(EXECUTE_BDM_QUERY_COMMAND, parameters);
        assertThat(result).isNotNull().isInstanceOf(List.class);
        assertThat((List<Serializable>) result).isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void should_execute_returns_employee_list() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_NAME, "getEmployees");
        parameters.put(RETURNS_LIST, true);
        parameters.put(RETURN_TYPE, EMPLOYEE_QUALIF_CLASSNAME);
        Serializable result = getCommandAPI().execute(EXECUTE_BDM_QUERY_COMMAND, parameters);
        assertThat(result).isNotNull().isInstanceOf(List.class);
        assertThat((List<Serializable>) result).hasSize(3);
    }

    @Test
    public void should_execute_returns_a_single_employee() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_NAME, "getEmployeeByFirstNameAndLastName");
        parameters.put(RETURN_TYPE, EMPLOYEE_QUALIF_CLASSNAME);
        final Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
        queryParameters.put("firstName", "Romain");
        queryParameters.put("lastName", "Bioteau");
        parameters.put(QUERY_PARAMETERS, (Serializable) queryParameters);

        Serializable result = getCommandAPI().execute(EXECUTE_BDM_QUERY_COMMAND, parameters);
        assertThat(result).isNotNull().isInstanceOf(Entity.class);
        assertThat(result.getClass().getName()).isEqualTo(EMPLOYEE_QUALIF_CLASSNAME);
        assertThat(result.getClass().getMethod("getFirstName", new Class[0]).invoke(result)).isEqualTo("Romain");
        assertThat(result.getClass().getMethod("getLastName", new Class[0]).invoke(result)).isEqualTo("Bioteau");
    }

    @Test(expected = CommandExecutionException.class)
    public void should_execute_throw_a_CommandExecutionException_if_result_is_not_single() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_NAME, "getEmployeeByLastName");
        parameters.put(RETURN_TYPE, EMPLOYEE_QUALIF_CLASSNAME);
        final Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
        queryParameters.put("lastName", "Bioteau");
        parameters.put(QUERY_PARAMETERS, (Serializable) queryParameters);
        getCommandAPI().execute(EXECUTE_BDM_QUERY_COMMAND, parameters);
    }

    @Test(expected = BonitaRuntimeException.class)
    public void should_execute_throw_BonitaRuntimeException_if_query_not_exists() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_NAME, "unknownQuery");
        parameters.put(RETURN_TYPE, EMPLOYEE_QUALIF_CLASSNAME);
        getCommandAPI().execute(EXECUTE_BDM_QUERY_COMMAND, parameters);
    }

    public void addEmployee(String firstName, String lastName) throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; BonitaEmployee e = new BonitaEmployee(); e.firstName = '" + firstName + "'; e.lastName = '" + lastName + "'; return e;",
                EMPLOYEE_QUALIF_CLASSNAME);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIF_CLASSNAME, null);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addOperation(new LeftOperandBuilder().createNewInstance("myEmployee").done(),
                OperatorType.CREATE_BUSINESS_DATA, null, null, employeeExpression);

        DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();
        final ProcessDefinition definition = deployAndEnableWithActor(designProcessDefinition, ACTOR_NAME, businessUser);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());

        final HumanTaskInstance userTask = waitForUserTask("step1", instance.getId());
        getProcessAPI().assignUserTask(userTask.getId(), businessUser.getId());
        getProcessAPI().executeFlowNode(userTask.getId());

        disableAndDeleteProcess(definition.getId());
    }

}
