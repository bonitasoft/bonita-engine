package com.bonitasoft.engine.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import com.bonitasoft.engine.bdm.Entity;
import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.Query;
import com.bonitasoft.engine.bdm.model.field.FieldType;
import com.bonitasoft.engine.bdm.model.field.SimpleField;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;
import com.bonitasoft.engine.business.data.ClassloaderRefresher;
import com.bonitasoft.engine.io.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Romain Bioteau
 */
public class ExecuteBDMQueryCommandIT extends CommonAPISPTest {

    private static final String EXECUTE_BDM_QUERY_COMMAND = "executeBDMQuery";

    private static final String EMPLOYEE_QUALIF_CLASSNAME = "org.bonita.pojo.BonitaEmployee";

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
        final SimpleField firstName = new SimpleField();
        firstName.setName("firstName");
        firstName.setType(FieldType.STRING);
        firstName.setLength(Integer.valueOf(10));

        final SimpleField lastName = new SimpleField();
        lastName.setName("lastName");
        lastName.setType(FieldType.STRING);
        lastName.setNullable(Boolean.FALSE);

        final BusinessObject employee = new BusinessObject();
        employee.setQualifiedName(EMPLOYEE_QUALIF_CLASSNAME);
        employee.addField(firstName);
        employee.addField(lastName);
        employee.setDescription("Describe a simple employee");
        // employee.addUniqueConstraint("uk_fl", "firstName", "lastName");

        employee.addQuery("getNoEmployees", "SELECT e FROM BonitaEmployee e WHERE e.firstName = 'INEXISTANT'", List.class.getName());
        final Query query = employee.addQuery("getEmployeeByFirstNameAndLastName",
                "SELECT e FROM BonitaEmployee e WHERE e.firstName=:firstName AND e.lastName=:lastName", EMPLOYEE_QUALIF_CLASSNAME);
        query.addQueryParameter("firstName", String.class.getName());
        query.addQueryParameter("lastName", String.class.getName());
        final BusinessObjectModel model = new BusinessObjectModel();
        model.addBusinessObject(employee);
        return model;
    }

    @BeforeClass
    public static void initTestClass() throws IOException {
        clientFolder = IOUtils.createTempDirectory("ExecuteBDMQueryCommandIT_client");
        clientFolder.mkdirs();
    }

    @AfterClass
    public static void cleanTestClass() throws IOException {
        try{
            FileUtils.deleteDirectory(clientFolder);
        } catch (final Exception e) {
            clientFolder.deleteOnExit();
        }

    }

    @Before
    public void beforeTest() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalLogger();
        businessUser = createUser(USERNAME, PASSWORD);
        logoutOnTenant();
        loginOnDefaultTenantWithDefaultTechnicalLogger();

        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        final byte[] zip = converter.zip(buildBOM());
        getTenantManagementAPI().pause();
        getTenantManagementAPI().installBusinessDataModel(zip);
        getTenantManagementAPI().resume();
        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);

        loadClientJars();

        addEmployee("Romain", "Bioteau");
        addEmployee("Jules", "Bioteau");
        addEmployee("Matthieu", "Chaffotte");
    }

    protected void loadClientJars() throws Exception {
        contextClassLoader = Thread.currentThread().getContextClassLoader();
        final byte[] clientBDMZip = getTenantManagementAPI().getClientBDMZip();
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
        loginOnDefaultTenantWithDefaultTechnicalLogger();
        if (!getTenantManagementAPI().isPaused()) {
            getTenantManagementAPI().pause();
            getTenantManagementAPI().cleanAndUninstallBusinessDataModel();
            getTenantManagementAPI().resume();
        }
        deleteUser(businessUser.getId());
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

    public void addEmployee(final String firstName, final String lastName) throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; BonitaEmployee e = new BonitaEmployee(); e.firstName = '" + firstName + "'; e.lastName = '" + lastName + "'; return e;",
                EMPLOYEE_QUALIF_CLASSNAME);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIF_CLASSNAME, null);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand("myEmployee"),
                OperatorType.ASSIGNMENT, null, null, employeeExpression);

        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();
        final ProcessDefinition definition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, businessUser);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());

        final HumanTaskInstance userTask = waitForUserTask("step1", instance.getId());
        getProcessAPI().assignUserTask(userTask.getId(), businessUser.getId());
        getProcessAPI().executeFlowNode(userTask.getId());

        disableAndDeleteProcess(definition.getId());
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
