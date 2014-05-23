package com.bonitasoft.engine.business.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessEnablementException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.home.BonitaHome;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.bdm.BusinessObject;
import com.bonitasoft.engine.bdm.BusinessObjectDAOFactory;
import com.bonitasoft.engine.bdm.BusinessObjectModel;
import com.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import com.bonitasoft.engine.bdm.Field;
import com.bonitasoft.engine.bdm.FieldType;
import com.bonitasoft.engine.bdm.Query;
import com.bonitasoft.engine.bdm.dao.BusinessObjectDAO;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;
import com.bonitasoft.engine.businessdata.BusinessDataRepositoryException;

public class BDRepositoryIT extends CommonAPISPTest {

    private static final String GET_EMPLOYEE_BY_LAST_NAME_QUERY_NAME = "findByLastName";

    private static final String GET_EMPLOYEE_BY_PHONE_NUMBER_QUERY_NAME = "findByPhoneNumber";

    private static final String CLIENT_BDM_ZIP_FILENAME = "client-bdm.zip";

    private static final String EMPLOYEE_QUALIF_CLASSNAME = "org.bonita.pojo.Employee";

    private User matti;

    private File clientFolder;

    private BusinessObjectModel buildBOM() {
        final Field firstName = new Field();
        firstName.setName("firstName");
        firstName.setType(FieldType.STRING);
        firstName.setLength(Integer.valueOf(10));

        final Field lastName = new Field();
        lastName.setName("lastName");
        lastName.setType(FieldType.STRING);
        lastName.setNullable(Boolean.FALSE);

        final Field phoneNumbers = new Field();
        phoneNumbers.setName("phoneNumbers");
        phoneNumbers.setType(FieldType.STRING);
        phoneNumbers.setLength(Integer.valueOf(10));
        phoneNumbers.setCollection(Boolean.TRUE);

        final BusinessObject employee = new BusinessObject();
        employee.setQualifiedName(EMPLOYEE_QUALIF_CLASSNAME);
        employee.addField(firstName);
        employee.addField(lastName);
        employee.addField(phoneNumbers);
        employee.setDescription("Describe a simple employee");
        employee.addUniqueConstraint("uk_fl", "firstName", "lastName");

        final Query getEmployeeByPhoneNumber = employee.addQuery(GET_EMPLOYEE_BY_PHONE_NUMBER_QUERY_NAME,
                "SELECT e FROM Employee e WHERE :phoneNumber IN ELEMENTS(e.phoneNumbers)", List.class.getName());
        getEmployeeByPhoneNumber.addQueryParameter("phoneNumber", String.class.getName());

        final Query findByFirstNAmeAndLastNameNewOrder = employee.addQuery("findByFirstNameAndLastNameNewOrder",
                "SELECT e FROM Employee e WHERE e.firstName =:firstName AND e.lastName = :lastName ORDER BY e.lastName", List.class.getName());
        findByFirstNAmeAndLastNameNewOrder.addQueryParameter("firstName", String.class.getName());
        findByFirstNAmeAndLastNameNewOrder.addQueryParameter("lastName", String.class.getName());

        employee.addQuery("countEmployee", "SELECT COUNT(e) FROM Employee e", Long.class.getName());

        final BusinessObject person = new BusinessObject();
        person.setQualifiedName("org.bonitasoft.pojo.Person");
        person.addField(firstName);
        person.addField(lastName);
        person.addField(phoneNumbers);
        person.setDescription("Describe a simple person");
        person.addUniqueConstraint("uk_fl", "firstName", "lastName");

        final BusinessObjectModel model = new BusinessObjectModel();
        model.addBusinessObject(employee);
        model.addBusinessObject(person);
        return model;
    }

    @Before
    public void setUp() throws Exception {
        clientFolder = com.bonitasoft.engine.io.IOUtils.createTempDirectory("bdr_it_client");
        loginOnDefaultTenantWithDefaultTechnicalLogger();
        matti = createUser("matti", "bpm");

        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        final byte[] zip = converter.zip(buildBOM());
        getTenantManagementAPI().pause();
        getTenantManagementAPI().installBusinessDataModel(zip);
        getTenantManagementAPI().resume();
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(clientFolder);

        if (!getTenantManagementAPI().isPaused()) {
            getTenantManagementAPI().pause();
            getTenantManagementAPI().cleanAndUninstallBusinessDataModel();
            getTenantManagementAPI().resume();
        }

        deleteUser(matti);
       logoutOnTenant();
    }

    @Test
    public void deploying_bdm_after_process_should_put_process_in_resolved_state() throws Exception {
        final String aQualifiedName = "org.bonitasoft.test.Bo";
        final byte[] bom = buildSimpleBom(aQualifiedName);

        final ProcessDefinition processDefinition = deploySimpleProcessWithBusinessData(aQualifiedName);

        ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertThat(processDeploymentInfo.getConfigurationState()).isEqualTo(ConfigurationState.UNRESOLVED);

        installBusinessDataModel(bom);

        processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertThat(processDeploymentInfo.getConfigurationState()).isEqualTo(ConfigurationState.RESOLVED);

        deleteProcess(processDefinition);
    }

    private void installBusinessDataModel(final byte[] bdm) throws Exception {
        getTenantManagementAPI().pause();
        getTenantManagementAPI().cleanAndUninstallBusinessDataModel();
        getTenantManagementAPI().installBusinessDataModel(bdm);
        getTenantManagementAPI().resume();
    }

    private ProcessDefinition deploySimpleProcessWithBusinessData(final String aQualifiedName) throws Exception {
        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        final String bizDataName = "myBizData";
        processDefinitionBuilder.addBusinessData(bizDataName, aQualifiedName, null);

        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinitionBuilder.done()).done());
        getProcessAPI().addUserToActor(ACTOR_NAME, processDefinition, matti.getId());
        return processDefinition;
    }

    private byte[] buildSimpleBom(final String boQualifiedName) throws IOException, JAXBException, SAXException {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName(boQualifiedName);
        final Field field = new Field();
        field.setName("aField");
        field.setType(FieldType.STRING);
        bo.addField(field);
        final BusinessObjectModel model = new BusinessObjectModel();
        model.addBusinessObject(bo);
        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        final byte[] zip = converter.zip(model);
        return zip;
    }

    @Test
    public void deployABDRAndCreateABusinessData() throws Exception {

        assertThat(getTenantManagementAPI().getBusinessDataModelVersion()).isNotEmpty();

        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; Employee e = new Employee(); e.firstName = 'John'; e.lastName = 'Doe'; return e;", EMPLOYEE_QUALIF_CLASSNAME);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        final String bizDataName = "myEmployee";
        processDefinitionBuilder.addBusinessData(bizDataName, EMPLOYEE_QUALIF_CLASSNAME, null);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand(bizDataName),
                OperatorType.ASSIGNMENT, null, null, employeeExpression);

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());

        final HumanTaskInstance userTask = waitForUserTask("step1", instance.getId());
        String employeeToString = getEmployeeToString(bizDataName, instance.getId());
        assertThat(employeeToString).isNull();

        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());
        getProcessAPI().executeFlowNode(userTask.getId());

        employeeToString = getEmployeeToString(bizDataName, instance.getId());
        assertThat(employeeToString).isEqualTo("Employee [firstName=John, lastName=Doe]");

        disableAndDeleteProcess(definition.getId());
    }

    @Cover(classes = { Operation.class }, concept = BPMNConcept.OPERATION, keywords = { "BusinessData", "business data java setter operation" }, jira = "BS-7217", story = "update a business data using a java setter operation")
    @Test
    public void shouldBeAbleToUpdateBusinessDataUsingBizDataJavaSetterOperation() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; Employee e = new Employee(); e.firstName = 'Jules'; e.lastName = 'UnNamed'; return e;", EMPLOYEE_QUALIF_CLASSNAME);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance(
                "shouldBeAbleToUpdateBusinessDataUsingJavaSetterOperation", "6.3-beta");
        final String businessDataName = "newBornBaby";
        final String newEmployeeFirstName = "Manon";
        final String newEmployeeLastName = "Péuigrec";
        processDefinitionBuilder.addBusinessData(businessDataName, EMPLOYEE_QUALIF_CLASSNAME, employeeExpression);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder
                .addAutomaticTask("step1")
                .addOperation(
                        new OperationBuilder().createBusinessDataSetAttributeOperation(businessDataName, "setFirstName", String.class.getName(),
                                new ExpressionBuilder().createConstantStringExpression(newEmployeeFirstName)))
                .addOperation(
                        new OperationBuilder().createBusinessDataSetAttributeOperation(businessDataName, "setLastName", String.class.getName(),
                                new ExpressionBuilder().createConstantStringExpression(newEmployeeLastName)));
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final long processInstanceId = getProcessAPI().startProcess(definition.getId()).getId();

        waitForUserTask("step2", processInstanceId);

        // Let's check the updated firstName + lastName values by calling an expression:
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>(2);
        final String expressionFirstName = "retrieve_FirstName";
        expressions.put(new ExpressionBuilder().createGroovyScriptExpression(expressionFirstName, businessDataName + ".firstName", String.class.getName(),
                new ExpressionBuilder().createBusinessDataExpression(businessDataName, EMPLOYEE_QUALIF_CLASSNAME)), null);
        final String expressionLastName = "retrieve_new_lastName";
        expressions.put(new ExpressionBuilder().createGroovyScriptExpression(expressionLastName, businessDataName + ".lastName", String.class.getName(),
                new ExpressionBuilder().createBusinessDataExpression(businessDataName, EMPLOYEE_QUALIF_CLASSNAME)), null);
        final Map<String, Serializable> evaluatedExpressions = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
        final String returnedFirstName = (String) evaluatedExpressions.get(expressionFirstName);
        final String returnedLastName = (String) evaluatedExpressions.get(expressionLastName);
        assertThat(returnedFirstName).isEqualTo(newEmployeeFirstName);
        assertThat(returnedLastName).isEqualTo(newEmployeeLastName);

        disableAndDeleteProcess(definition.getId());
    }

    @Test
    public void deployABDRAndCreateADefaultBusinessDataAndReuseReference() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; Employee e = new Employee(); e.firstName = 'Jane'; e.lastName = 'Doe'; return e;", EMPLOYEE_QUALIF_CLASSNAME);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIF_CLASSNAME, employeeExpression);
        final String secondBizData = "people";
        processDefinitionBuilder.addBusinessData(secondBizData, EMPLOYEE_QUALIF_CLASSNAME, null);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addOperation(
                new OperationBuilder().attachBusinessDataSetAttributeOperation(secondBizData, new ExpressionBuilder().createQueryBusinessDataExpression(
                        "oneEmployee", "Employee." + GET_EMPLOYEE_BY_LAST_NAME_QUERY_NAME, EMPLOYEE_QUALIF_CLASSNAME,
                        new ExpressionBuilder().createConstantStringExpression("lastName", "Doe"))));
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());

        final HumanTaskInstance userTask = waitForUserTask("step1", processInstance.getId());
        final String employeeToString = getEmployeeToString("myEmployee", processInstance.getId());
        assertThat(employeeToString).isEqualTo("Employee [firstName=Jane, lastName=Doe]");

        assignAndExecuteStep(userTask, matti);
        waitForUserTask("step2", processInstance.getId());
        final String people = getEmployeeToString(secondBizData, processInstance.getId());
        assertThat(people).isEqualTo("Employee [firstName=Jane, lastName=Doe]");

        disableAndDeleteProcess(definition.getId());
    }

    @Test
    public void deployABDRAndCreateAndUdpateABusinessData() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; Employee e = new Employee(); e.firstName = 'John'; e.lastName = 'Doe'; return e;", EMPLOYEE_QUALIF_CLASSNAME);

        final Expression getEmployeeExpression = new ExpressionBuilder().createBusinessDataExpression("myEmployee", EMPLOYEE_QUALIF_CLASSNAME);
        // try to modify the business data
        final Expression scriptExpression = new ExpressionBuilder().createGroovyScriptExpression("updateBizData", "myEmployee.lastName = 'BPM'; return 'BPM'",
                String.class.getName(), getEmployeeExpression);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIF_CLASSNAME, employeeExpression);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addDisplayDescription(scriptExpression);

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());

        waitForUserTask("step1", instance.getId());
        final String employeeToString = getEmployeeToString("myEmployee", instance.getId());
        assertThat(employeeToString).isEqualTo("Employee [firstName=John, lastName=Doe]");

        disableAndDeleteProcess(definition.getId());
    }

    @Test(expected = ProcessEnablementException.class)
    public void deployProcessWithWrongBusinessDataTypeShouldNotBeDeployable() throws Exception {
        final User user = createUser("login1", "password");
        ProcessDefinition processDefinition = null;
        try {
            final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
            processBuilder.addActor("myActor");
            processBuilder.addBusinessData("myBizData", Long.class.getName(), new ExpressionBuilder().createConstantLongExpression(12L));
            processBuilder.addUserTask("Request", "myActor");
            processDefinition = getProcessAPI().deploy(processBuilder.done());
            addUserToFirstActorOfProcess(user.getId(), processDefinition);
            getProcessAPI().enableProcess(processDefinition.getId());
            // Should not fail here, if the Server process model is valid:
        } finally {
            disableAndDeleteProcess(processDefinition);
            deleteUser(user);
        }
    }

    @Test
    public void deployABDRAndExecuteAGroovyScriptWhichContainsAPOJOFromTheBDR() throws BonitaException {
        final Expression stringExpression = new ExpressionBuilder()
                .createGroovyScriptExpression(
                        "alive",
                        "import "
                                + EMPLOYEE_QUALIF_CLASSNAME
                                + "; Employee e = new Employee(); e.firstName = 'John'; e.lastName = 'Doe'; return \"Employee [firstName=\" + e.firstName + \", lastName=\" + e.lastName + \"]\"",
                        String.class.getName());
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>();
        expressions.put(stringExpression, new HashMap<String, Serializable>());

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addAutomaticTask("stepO");
        final ProcessDefinition processDefinition = getProcessAPI().deploy(processDefinitionBuilder.done());
        getProcessAPI().enableProcess(processDefinition.getId());
        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnProcessDefinition(processDefinition.getId(), expressions);
        assertThat(result).hasSize(1);

        final Set<Entry<String, Serializable>> entrySet = result.entrySet();
        final Entry<String, Serializable> entry = entrySet.iterator().next();
        assertThat(entry.getValue()).isEqualTo("Employee [firstName=John, lastName=Doe]");

        disableAndDeleteProcess(processDefinition.getId());
    }

    @Test(expected = BonitaRuntimeException.class)
    public void createAnEmployeeWithARequiredFieldAtNullThrowsAnException() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee",
                "import org.bonita.pojo.Employee; Employee e = new Employee(); e.firstName = 'John'; return e;", EMPLOYEE_QUALIF_CLASSNAME);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIF_CLASSNAME, employeeExpression);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        try {
            getProcessAPI().startProcess(definition.getId());
        } finally {
            disableAndDeleteProcess(definition.getId());
        }
    }

    @Test(expected = BonitaRuntimeException.class)
    public void createAnEmployeeWithATooSmallFieldAtNullThrowsAnException() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee",
                "import org.bonita.pojo.Employee; Employee e = new Employee(); e.firstName = 'John124578/'; e.lastName = 'Doe'; return e;",
                EMPLOYEE_QUALIF_CLASSNAME);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIF_CLASSNAME, employeeExpression);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        try {
            getProcessAPI().startProcess(definition.getId());
        } finally {
            disableAndDeleteProcess(definition.getId());
        }
    }

    @Test
    public void updateBusinessDataShouldWorkOutsideATransaction() throws Exception {
        final String taskName = "step";

        final ProcessDefinition definition = buildProcessThatUpdateBizDataInsideConnector(taskName);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());
        waitForUserTask(taskName, instance.getId());

        final String employeeToString = getEmployeeToString("myEmployee", instance.getId());
        assertThat(employeeToString).isEqualTo("Employee [firstName=John, lastName=Hakkinen]");

        disableAndDeleteProcess(definition);
    }

    @Test
    public void should_deploy_generate_client_bdm_jar_in_bonita_home() throws Exception {
        final String bonitaHomePath = System.getProperty(BonitaHome.BONITA_HOME);
        final String clientBdmJarPath = bonitaHomePath + File.separator + "server" + File.separator + "tenants" + File.separator + "1" + File.separator
                + "data-management" + File.separator + "client";
        assertThat(new File(clientBdmJarPath, CLIENT_BDM_ZIP_FILENAME)).exists().isFile();

        assertThat(getTenantManagementAPI().getClientBDMZip()).isNotEmpty();
    }

    @Test(expected = BusinessDataRepositoryException.class)
    public void should_undeploy_delete_generate_client_bdm_jar_in_bonita_home() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalLogger();
        getTenantManagementAPI().pause();
        getTenantManagementAPI().uninstallBusinessDataModel();
        getTenantManagementAPI().resume();

        final String bonitaHomePath = System.getProperty(BonitaHome.BONITA_HOME);
        final String clientBdmJarPath = bonitaHomePath + File.separator + "server" + File.separator + "tenants" + File.separator + "1" + File.separator
                + "data-management" + File.separator + "client";
        assertThat(new File(clientBdmJarPath, CLIENT_BDM_ZIP_FILENAME)).doesNotExist();

        getTenantManagementAPI().getClientBDMZip();
    }

    @Test
    public void should_use_factory_to_instantiate_dao_on_client_side() throws Exception {
        addEmployee("Marcel", "Pagnol");
        final APISession apiSession = getSession();
        final byte[] clientBDMZip = getTenantManagementAPI().getClientBDMZip();

        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        final ClassLoader classLoaderWithBDM = new ClassloaderRefresher().loadClientModelInClassloader(clientBDMZip, contextClassLoader,
                EMPLOYEE_QUALIF_CLASSNAME, clientFolder);

        try {
            Thread.currentThread().setContextClassLoader(classLoaderWithBDM);

            @SuppressWarnings("unchecked")
            final Class<? extends BusinessObjectDAO> daoInterface = (Class<? extends BusinessObjectDAO>) Class.forName(EMPLOYEE_QUALIF_CLASSNAME + "DAO", true,
                    classLoaderWithBDM);
            final BusinessObjectDAOFactory businessObjectDAOFactory = new BusinessObjectDAOFactory();
            final BusinessObjectDAO daoImpl = businessObjectDAOFactory.createDAO(apiSession, daoInterface);
            assertThat(daoImpl.getClass().getName()).isEqualTo(EMPLOYEE_QUALIF_CLASSNAME + "DAOImpl");

            Method daoMethod = daoImpl.getClass().getMethod("findByLastName", String.class, int.class, int.class);
            assertThat(daoMethod).isNotNull();
            assertThat(daoMethod.getReturnType().getName()).isEqualTo(List.class.getName());
            List<?> result = (List<?>) daoMethod.invoke(daoImpl, "Pagnol", 0, 10);
            assertThat(result).isNotEmpty().hasSize(1);

            result = (List<?>) daoMethod.invoke(daoImpl, "Hanin", 0, 10);
            assertThat(result).isEmpty();

            daoMethod = daoImpl.getClass().getMethod("findByFirstNameAndLastName", String.class, String.class);
            assertThat(daoMethod).isNotNull();
            assertThat(daoMethod.getReturnType().getName()).isEqualTo(EMPLOYEE_QUALIF_CLASSNAME);
            assertThat(daoMethod.invoke(daoImpl, "Marcel", "Pagnol")).isNotNull();
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private void addEmployee(final String firstName, final String lastName) throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; Employee e = new Employee(); e.firstName = '" + firstName + "'; e.lastName = '" + lastName + "'; return e;", EMPLOYEE_QUALIF_CLASSNAME);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIF_CLASSNAME, null);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand("myEmployee"),
                OperatorType.ASSIGNMENT, null, null, employeeExpression);

        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();
        final ProcessDefinition definition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, matti);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());

        final HumanTaskInstance userTask = waitForUserTask("step1", instance.getId());
        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());
        getProcessAPI().executeFlowNode(userTask.getId());

        disableAndDeleteProcess(definition.getId());
    }

    private ProcessDefinition buildProcessThatUpdateBizDataInsideConnector(final String taskName) throws BonitaException, IOException {
        final Expression getEmployeeExpression = new ExpressionBuilder().createBusinessDataExpression("myEmployee", EMPLOYEE_QUALIF_CLASSNAME);

        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; Employee e = new Employee(); e.firstName = 'John'; e.lastName = 'Doe'; e.addToPhoneNumbers('78945612'); return e;",
                EMPLOYEE_QUALIF_CLASSNAME);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("BizDataAndConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIF_CLASSNAME, employeeExpression);
        processDefinitionBuilder
                .addUserTask(taskName, ACTOR_NAME)
                .addConnector("updateBusinessData", "com.bonitasoft.connector.BusinessDataUpdateConnector", "1.0", ConnectorEvent.ON_ENTER)
                .addInput("bizData", getEmployeeExpression)
                .addOutput(
                        new OperationBuilder().createBusinessDataSetAttributeOperation("myEmployee", "setLastName", String.class.getName(),
                                new ExpressionBuilder().createGroovyScriptExpression("retrieve modified lastname from connector", "output1.getLastName()",
                                        String.class.getName(), new ExpressionBuilder().createBusinessDataExpression("output1", EMPLOYEE_QUALIF_CLASSNAME))));

        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                processDefinitionBuilder.done());
        BarResource barResource = getResource("/com/bonitasoft/engine/business/data/BusinessDataUpdateConnector.impl", "BusinessDataUpdateConnector.impl");
        businessArchiveBuilder.addConnectorImplementation(barResource);

        barResource = BuildTestUtil.generateJarAndBuildBarResource(BusinessDataUpdateConnector.class, "BusinessDataUpdateConnector.jar");
        businessArchiveBuilder.addClasspathResource(barResource);

        return deployAndEnableProcessWithActor(businessArchiveBuilder.done(), ACTOR_NAME, matti);
    }

    private BarResource getResource(final String path, final String name) throws IOException {
        final InputStream stream = BDRepositoryIT.class.getResourceAsStream(path);
        assertThat(stream).isNotNull();
        try {
            final byte[] byteArray = IOUtils.toByteArray(stream);
            return new BarResource(name, byteArray);
        } finally {
            stream.close();
        }
    }

    private String getEmployeeToString(final String businessDataName, final long processInstanceId) throws InvalidExpressionException {
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>(5);
        final String expressionEmployee = "retrieve_Employee";
        expressions.put(
                new ExpressionBuilder().createGroovyScriptExpression(expressionEmployee, "\"Employee [firstName=\" + " + businessDataName
                        + ".firstName + \", lastName=\" + " + businessDataName + ".lastName + \"]\";", String.class.getName(),
                        new ExpressionBuilder().createBusinessDataExpression(businessDataName, EMPLOYEE_QUALIF_CLASSNAME)), null);
        try {
            final Map<String, Serializable> evaluatedExpressions = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
            return (String) evaluatedExpressions.get(expressionEmployee);
        } catch (final ExpressionEvaluationException eee) {
            System.err.println(eee.getMessage());
            return null;
        }
    }

}
