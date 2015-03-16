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
package org.bonitasoft.engine.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.aBO;
import static org.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder.aBOM;
import static org.bonitasoft.engine.bdm.builder.FieldBuilder.aRelationField;
import static org.bonitasoft.engine.bdm.builder.FieldBuilder.aSimpleField;
import static org.bonitasoft.engine.bdm.builder.QueryBuilder.aQuery;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import org.bonitasoft.engine.bdm.Entity;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField.Type;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.business.data.ClassloaderRefresher;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtils;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Romain Bioteau
 */
public class ExecuteBDMQueryCommandIT extends CommonAPIIT {

    private static final String EXECUTE_BDM_QUERY_COMMAND = "executeBDMQuery";

    private static final String EMPLOYEE_QUALIF_CLASSNAME = "org.bonita.pojo.BonitaEmployee";

    private static final String ADDRESS_QUALIF_CLASSNAME = "org.bonita.pojo.BonitaAddress";

    private static final String RETURNS_LIST = "returnsList";

    private static final String QUERY_PARAMETERS = "queryParameters";

    private static final String RETURN_TYPE = "returnType";

    private static final String START_INDEX = "startIndex";

    private static final String MAX_RESULTS = "maxResults";

    private static final String QUERY_NAME = "queryName";

    protected User businessUser;

    private ClassLoader contextClassLoader;

    private static File clientFolder;

    private BusinessObjectModel buildBOM() {
        final BusinessObject addressBO = aBO(ADDRESS_QUALIF_CLASSNAME).withField(aSimpleField().withName("street").ofType(FieldType.STRING).build()).build();
        final BusinessObject employee = aBO(EMPLOYEE_QUALIF_CLASSNAME).withDescription("Describe final a simple employee").
                withField(aSimpleField().withName("firstName").ofType(FieldType.STRING).withLength(10).build()).
                withField(aSimpleField().withName("lastName").ofType(FieldType.STRING).notNullable().build()).
                withField(aRelationField().withName("addresses").ofType(Type.COMPOSITION).referencing(addressBO).multiple().lazy().build()).
                withQuery(
                        aQuery().withName("getNoEmployees")
                                .withContent("SELECT e FROM BonitaEmployee e WHERE e.firstName = 'INEXISTANT'")
                                .withReturnType(List.class.getName()).build()).
                withQuery(
                        aQuery().withName("getEmployeeByFirstNameAndLastName")
                                .withContent("SELECT e FROM BonitaEmployee e WHERE e.firstName=:firstName AND e.lastName=:lastName")
                                .withReturnType(EMPLOYEE_QUALIF_CLASSNAME)
                                .withQueryParameter("firstName", String.class.getName())
                                .withQueryParameter("lastName", String.class.getName()).build())
                .build();
        final BusinessObjectModel model = aBOM().withBOs(addressBO, employee).build();
        return model;
    }

    @BeforeClass
    public static void initTestClass() throws IOException {
        clientFolder = IOUtils.createTempDirectory("ExecuteBDMQueryCommandIT_client");
        clientFolder.mkdirs();
    }

    @AfterClass
    public static void cleanTestClass() {
        try {
            FileUtils.deleteDirectory(clientFolder);
        } catch (final Exception e) {
            clientFolder.deleteOnExit();
        }
    }

    @Before
    public void beforeTest() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        businessUser = createUser(USERNAME, PASSWORD);
        logoutOnTenant();
        loginOnDefaultTenantWithDefaultTechnicalUser();

        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        final byte[] zip = converter.zip(buildBOM());

        assertThat(getTenantAdministrationAPI().isPaused()).as("should not have tenant is paused mode").isFalse();
        getTenantAdministrationAPI().pause();
        getTenantAdministrationAPI().installBusinessDataModel(zip);
        getTenantAdministrationAPI().resume();
        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);

        loadClientJars();

        addEmployee("Romain", "Bioteau", "54, Grand Rue", "38 , Gabrile Péri");
        addEmployee("Jules", "Bioteau", "78 , Colonel Bougault");
        addEmployee("Matthieu", "Chaffotte");
    }

    protected void loadClientJars() throws Exception {
        contextClassLoader = Thread.currentThread().getContextClassLoader();
        final byte[] clientBDMZip = getTenantAdministrationAPI().getClientBDMZip();
        final ClassLoader classLoaderWithBDM = new ClassloaderRefresher().loadClientModelInClassloader(clientBDMZip, contextClassLoader,
                EMPLOYEE_QUALIF_CLASSNAME, clientFolder);
        Thread.currentThread().setContextClassLoader(classLoaderWithBDM);
    }

    @After
    public void afterTest() throws BonitaException {
        // reset previous classloader:
        if (contextClassLoader != null) {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }

        logoutOnTenant();
        loginOnDefaultTenantWithDefaultTechnicalUser();
        if (!getTenantAdministrationAPI().isPaused()) {
            getTenantAdministrationAPI().pause();
            getTenantAdministrationAPI().cleanAndUninstallBusinessDataModel();
            getTenantAdministrationAPI().resume();
        }
        deleteUser(businessUser);
        logoutOnTenant();
    }

    @Test
    public void should_execute_returns_empty_list() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_NAME, "BonitaEmployee.getNoEmployees");
        parameters.put(RETURNS_LIST, true);
        parameters.put(RETURN_TYPE, EMPLOYEE_QUALIF_CLASSNAME);
        parameters.put(START_INDEX, 0);
        parameters.put(MAX_RESULTS, 10);
        final byte[] result = (byte[]) getCommandAPI().execute(EXECUTE_BDM_QUERY_COMMAND, parameters);

        assertThat(deserializeListResult(result)).isNotNull().isInstanceOf(List.class).isEmpty();
    }

    @Test
    public void should_execute_returns_employee_list() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_NAME, "BonitaEmployee.find");
        parameters.put(RETURNS_LIST, true);
        parameters.put(RETURN_TYPE, EMPLOYEE_QUALIF_CLASSNAME);
        parameters.put(RETURN_TYPE, EMPLOYEE_QUALIF_CLASSNAME);
        parameters.put(START_INDEX, 0);
        parameters.put(MAX_RESULTS, 10);
        final byte[] result = (byte[]) getCommandAPI().execute(EXECUTE_BDM_QUERY_COMMAND, parameters);

        assertThat(deserializeListResult(result)).isNotNull().isInstanceOf(List.class).hasSize(3);
    }

    @Test
    public void getListFromQueryShouldLimitToMaxResults() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_NAME, "BonitaEmployee.find");
        parameters.put(RETURNS_LIST, true);
        parameters.put(RETURN_TYPE, EMPLOYEE_QUALIF_CLASSNAME);
        parameters.put(RETURN_TYPE, EMPLOYEE_QUALIF_CLASSNAME);
        parameters.put(START_INDEX, 0);
        parameters.put(MAX_RESULTS, 2);
        final byte[] result = (byte[]) getCommandAPI().execute(EXECUTE_BDM_QUERY_COMMAND, parameters);

        assertThat(deserializeListResult(result)).isNotNull().isInstanceOf(List.class).hasSize(2);
    }

    @Test
    public void should_execute_returns_a_single_employee() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_NAME, "BonitaEmployee.getEmployeeByFirstNameAndLastName");
        parameters.put(RETURN_TYPE, EMPLOYEE_QUALIF_CLASSNAME);
        final Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
        queryParameters.put("firstName", "Romain");
        queryParameters.put("lastName", "Bioteau");
        parameters.put(QUERY_PARAMETERS, (Serializable) queryParameters);

        final byte[] result = (byte[]) getCommandAPI().execute(EXECUTE_BDM_QUERY_COMMAND, parameters);
        final Serializable employee = deserializeSimpleResult(result);
        assertThat(employee).isNotNull().isInstanceOf(Entity.class);
        assertThat(employee.getClass().getName()).isEqualTo(EMPLOYEE_QUALIF_CLASSNAME);
        assertThat(employee.getClass().getMethod("getFirstName", new Class[0]).invoke(employee)).isEqualTo("Romain");
        assertThat(employee.getClass().getMethod("getLastName", new Class[0]).invoke(employee)).isEqualTo("Bioteau");
        final Object invoke = employee.getClass().getMethod("getAddresses", new Class[0]).invoke(employee);
        assertThat(invoke).isInstanceOf(List.class);
        assertThat((List<?>) invoke).isEmpty();
    }

    @Test(expected = CommandExecutionException.class)
    public void should_execute_throw_a_CommandExecutionException_if_result_is_not_single() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(QUERY_NAME, "BonitaEmployee.findByLastName");
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

    public void addEmployee(final String firstName, final String lastName, final String... addresses) throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee",
                createNewEmployeeScriptContent(firstName, lastName, addresses),
                EMPLOYEE_QUALIF_CLASSNAME);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIF_CLASSNAME, null);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand("myEmployee"),
                OperatorType.ASSIGNMENT, null, null, employeeExpression);

        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();
        final ProcessDefinition definition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, businessUser);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());
        waitForUserTaskAndExecuteIt(instance, "step1", businessUser);

        disableAndDeleteProcess(definition.getId());
    }

    private String createNewEmployeeScriptContent(final String firstName, final String lastName, final String... addresses) {
        final StringBuilder sb = new StringBuilder();
        sb.append("import ");
        sb.append(EMPLOYEE_QUALIF_CLASSNAME);
        sb.append("\n");
        sb.append("import ");
        sb.append(ADDRESS_QUALIF_CLASSNAME);
        sb.append("\n");
        sb.append("BonitaEmployee e = new BonitaEmployee();");
        sb.append("\n");
        sb.append("e.firstName =");
        sb.append("'" + firstName + "'");
        sb.append("\n");
        sb.append("e.lastName =");
        sb.append("'" + lastName + "'");
        sb.append("\n");
        if (addresses != null) {
            for (int i = 0; i < addresses.length; i++) {
                final String addressVar = "a" + String.valueOf(i);
                sb.append("BonitaAddress " + addressVar + " = new BonitaAddress();");
                sb.append("\n");
                sb.append(addressVar + ".street =");
                sb.append("'" + addresses[i] + "'");
                sb.append("\n");
                sb.append("e.addToAddresses(" + addressVar + ")");
                sb.append("\n");
            }
        }

        sb.append("return e;");
        return sb.toString();
    }

    private Serializable deserializeSimpleResult(final byte[] result) throws Exception {
        final Class<?> loadClass = Thread.currentThread().getContextClassLoader().loadClass(EMPLOYEE_QUALIF_CLASSNAME);
        final ObjectMapper mapper = new ObjectMapper();
        return (Serializable) mapper.readValue(result, loadClass);
    }

    private List<?> deserializeListResult(final byte[] result) throws Exception {
        final Class<?> loadClass = Thread.currentThread().getContextClassLoader().loadClass(EMPLOYEE_QUALIF_CLASSNAME);
        final ObjectMapper mapper = new ObjectMapper();
        return (List<?>) mapper.readValue(result, mapper.getTypeFactory().constructCollectionType(List.class, loadClass));
    }

}
