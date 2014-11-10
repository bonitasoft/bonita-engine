/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
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
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessEnablementException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.CallActivityBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.home.BonitaHome;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
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
import com.bonitasoft.engine.bdm.BusinessObjectDAOFactory;
import com.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import com.bonitasoft.engine.bdm.dao.BusinessObjectDAO;
import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.Query;
import com.bonitasoft.engine.bdm.model.field.FieldType;
import com.bonitasoft.engine.bdm.model.field.RelationField;
import com.bonitasoft.engine.bdm.model.field.RelationField.FetchType;
import com.bonitasoft.engine.bdm.model.field.RelationField.Type;
import com.bonitasoft.engine.bdm.model.field.SimpleField;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;
import com.bonitasoft.engine.businessdata.BusinessDataReference;
import com.bonitasoft.engine.businessdata.BusinessDataRepositoryException;
import com.bonitasoft.engine.businessdata.SimpleBusinessDataReference;

public class BDRepositoryIT extends CommonAPISPTest {

    private static final String ADDRESS_QUALIF_NAME = "org.bonita.pojo.Address";

    private static final String GET_EMPLOYEE_BY_LAST_NAME_QUERY_NAME = "findByLastName";

    private static final String GET_EMPLOYEE_BY_PHONE_NUMBER_QUERY_NAME = "findByPhoneNumber";

    private static final String CLIENT_BDM_ZIP_FILENAME = "client-bdm.zip";

    private static final String EMPLOYEE_QUALIF_CLASSNAME = "org.bonita.pojo.Employee";

    private User matti;

    private File clientFolder;

    private BusinessObjectModel buildBOM() {
        final SimpleField name = new SimpleField();
        name.setName("name");
        name.setType(FieldType.STRING);
        final BusinessObject countryBO = new BusinessObject();
        countryBO.setQualifiedName("org.bonita.pojo.Country");
        countryBO.addField(name);

        final SimpleField street = new SimpleField();
        street.setName("street");
        street.setType(FieldType.STRING);
        final SimpleField city = new SimpleField();
        city.setName("city");
        city.setType(FieldType.STRING);
        final RelationField country = new RelationField();
        country.setType(Type.AGGREGATION);
        country.setFetchType(FetchType.LAZY);
        country.setName("country");
        country.setCollection(Boolean.FALSE);
        country.setNullable(Boolean.TRUE);
        country.setReference(countryBO);

        final BusinessObject addressBO = new BusinessObject();
        addressBO.setQualifiedName(ADDRESS_QUALIF_NAME);
        addressBO.addField(street);
        addressBO.addField(city);
        addressBO.addField(country);

        final RelationField address = new RelationField();
        address.setType(Type.AGGREGATION);
        address.setFetchType(FetchType.LAZY);
        address.setName("addresses");
        address.setCollection(Boolean.TRUE);
        address.setNullable(Boolean.TRUE);
        address.setReference(addressBO);

        final RelationField saddress = new RelationField();
        saddress.setType(Type.AGGREGATION);
        saddress.setFetchType(FetchType.LAZY);
        saddress.setName("address");
        saddress.setCollection(Boolean.FALSE);
        saddress.setNullable(Boolean.TRUE);
        saddress.setReference(addressBO);

        final SimpleField firstName = new SimpleField();
        firstName.setName("firstName");
        firstName.setType(FieldType.STRING);
        firstName.setLength(Integer.valueOf(10));

        final SimpleField lastName = new SimpleField();
        lastName.setName("lastName");
        lastName.setType(FieldType.STRING);
        lastName.setNullable(Boolean.FALSE);

        final SimpleField phoneNumbers = new SimpleField();
        phoneNumbers.setName("phoneNumbers");
        phoneNumbers.setType(FieldType.STRING);
        phoneNumbers.setLength(Integer.valueOf(10));
        phoneNumbers.setCollection(Boolean.TRUE);

        final BusinessObject employee = new BusinessObject();
        employee.setQualifiedName(EMPLOYEE_QUALIF_CLASSNAME);
        employee.addField(firstName);
        employee.addField(lastName);
        employee.addField(phoneNumbers);
        employee.addField(address);
        employee.addField(saddress);
        employee.setDescription("Describe a simple employee");
        employee.addUniqueConstraint("uk_fl", "firstName", "lastName");

        final Query getEmployeeByPhoneNumber = employee.addQuery(GET_EMPLOYEE_BY_PHONE_NUMBER_QUERY_NAME,
                "SELECT e FROM Employee e WHERE :phoneNumber IN ELEMENTS(e.phoneNumbers)", List.class.getName());
        getEmployeeByPhoneNumber.addQueryParameter("phoneNumber", String.class.getName());

        final Query findByFirstNAmeAndLastNameNewOrder = employee.addQuery("findByFirstNameAndLastNameNewOrder",
                "SELECT e FROM Employee e WHERE e.firstName =:firstName AND e.lastName = :lastName ORDER BY e.lastName", List.class.getName());
        findByFirstNAmeAndLastNameNewOrder.addQueryParameter("firstName", String.class.getName());
        findByFirstNAmeAndLastNameNewOrder.addQueryParameter("lastName", String.class.getName());

        final Query findByFirstNameFetchAddresses = employee.addQuery("findByFirstNameFetchAddresses",
                "SELECT e FROM Employee e INNER JOIN FETCH e.addresses WHERE e.firstName =:firstName ORDER BY e.lastName", List.class.getName());
        findByFirstNameFetchAddresses.addQueryParameter("firstName", String.class.getName());

        employee.addQuery("countEmployee", "SELECT COUNT(e) FROM Employee e", Long.class.getName());

        employee.addIndex("IDX_LSTNM", "lastName");

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
        model.addBusinessObject(addressBO);
        model.addBusinessObject(countryBO);
        return model;
    }

    @Before
    public void setUp() throws Exception {
        clientFolder = IOUtil.createTempDirectoryInDefaultTempDirectory("bdr_it_client");
        loginOnDefaultTenantWithDefaultTechnicalUser();
        matti = createUser("matti", "bpm");

        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        final byte[] zip = converter.zip(buildBOM());
        getTenantManagementAPI().pause();
        getTenantManagementAPI().installBusinessDataModel(zip);
        getTenantManagementAPI().resume();
    }

    @After
    public void tearDown() throws Exception {
        try {
            FileUtils.deleteDirectory(clientFolder);
        } catch (final Exception e) {
            clientFolder.deleteOnExit();
        }
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
        final SimpleField field = new SimpleField();
        field.setName("aField");
        field.setType(FieldType.STRING);
        bo.addField(field);
        final BusinessObjectModel model = new BusinessObjectModel();
        model.addBusinessObject(bo);
        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        return converter.zip(model);
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

        assertCount(processInstanceId);
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
    public void deployABDRAndCreateABOAndUdpateThroughAGroovyScript() throws Exception {
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
        assertThat(employeeToString).isEqualTo("Employee [firstName=John, lastName=BPM]");

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
            deleteProcess(processDefinition);
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

        assertCount(instance.getId());
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
        loginOnDefaultTenantWithDefaultTechnicalUser();
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
    public void shouldBeAbleToRunDAOCallThroughGroovy() throws Exception {
        final String firstName = "FlofFlof";
        final String lastName = "Boudin";
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; import org.bonita.pojo.Address; Employee e = new Employee(); e.firstName = '" + firstName + "'; e.lastName = '" + lastName
                + "'; e.addToAddresses(myAddress); return e;", EMPLOYEE_QUALIF_CLASSNAME,
                new ExpressionBuilder().createBusinessDataExpression("myAddress", ADDRESS_QUALIF_NAME));
        final Expression addressExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewAddress",
                "import org.bonita.pojo.Address; Address a = new Address(); a.street='32, rue Gustave Eiffel'; a.city='Grenoble'; return a;",
                ADDRESS_QUALIF_NAME);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance(
                "shouldBeAbleToRunDAOCallThroughGroovy", "6.3.1");
        final String employeeDAOName = "employeeDAO";
        final String bizDataName = "myEmployee";
        processDefinitionBuilder.addBusinessData(bizDataName, EMPLOYEE_QUALIF_CLASSNAME, null);
        processDefinitionBuilder.addBusinessData("myAddress", ADDRESS_QUALIF_NAME, null);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addAutomaticTask("step1")
        .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand("myAddress"), OperatorType.ASSIGNMENT, null, null, addressExpression)
        .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand(bizDataName), OperatorType.ASSIGNMENT, null, null, employeeExpression);
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final long processInstanceId = getProcessAPI().startProcess(definition.getId()).getId();

        waitForUserTask("step2", processInstanceId);

        // Let's check we can retrieve firstName using DAO call:
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>(1);
        final String getLastNameWithDAOExpression = "retrieveEmployeeByFirstName";
        expressions.put(
                new ExpressionBuilder().createGroovyScriptExpression(getLastNameWithDAOExpression, employeeDAOName + ".findByFirstName('" + firstName
                        + "', 0, 10).get(0).getAddresses().get(0).city", String.class.getName(),
                        new ExpressionBuilder().buildBusinessObjectDAOExpression(employeeDAOName, EMPLOYEE_QUALIF_CLASSNAME + "DAO")), null);
        final Map<String, Serializable> evaluatedExpressions = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
        final String returnedLastName = (String) evaluatedExpressions.get(getLastNameWithDAOExpression);
        assertThat(returnedLastName).isEqualTo("Grenoble");

        assertCount(processInstanceId);
        disableAndDeleteProcess(definition.getId());
    }

    @Test
    public void should_use_factory_to_instantiate_dao_on_client_side() throws Exception {
        final AddressRef ref1 = new AddressRef("newYorkAddr", "33, corner street", "NY");
        final AddressRef ref2 = new AddressRef("romeAddr", "2, plaza del popolo", "Roma");
        addEmployee("Marcel", "Pagnol", ref1, ref2);
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
            final Object employee = daoMethod.invoke(daoImpl, "Marcel", "Pagnol");
            assertThat(employee).isNotNull();
            final List<?> lazyAddresses = (List<?>) employee.getClass().getMethod("getAddresses", new Class[0]).invoke(employee);
            assertThat(lazyAddresses).hasSize(2);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private void addEmployee(final String firstName, final String lastName, final AddressRef... addresses) throws Exception {
        final List<Expression> dependencies = new ArrayList<Expression>();
        if (addresses != null) {
            for (final AddressRef ref : addresses) {
                dependencies.add(ref.createDependency());
            }
        }
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee",
                createNewEmployeeScriptContent(firstName, lastName, addresses),
                EMPLOYEE_QUALIF_CLASSNAME, dependencies);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIF_CLASSNAME, null);
        final UserTaskDefinitionBuilder task = processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        if (addresses != null) {
            for (final AddressRef ref : addresses) {
                processDefinitionBuilder.addBusinessData(ref.getVarName(), ADDRESS_QUALIF_NAME, null);
                final Expression addressExpression = new ExpressionBuilder().createGroovyScriptExpression("createAddress" + ref.getVarName(),
                        createNewAddressScriptContent(ref.getStreet(), ref.getCity()),
                        ADDRESS_QUALIF_NAME);
                task.addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand(ref.getVarName()),
                        OperatorType.ASSIGNMENT, null, null, addressExpression);
            }
        }

        task.addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand("myEmployee"),
                OperatorType.ASSIGNMENT, null, null, employeeExpression);

        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();
        final ProcessDefinition definition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, matti);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());

        final HumanTaskInstance userTask = waitForUserTask("step1", instance.getId());
        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());
        getProcessAPI().executeFlowNode(userTask.getId());

        disableAndDeleteProcess(definition.getId());
    }

    private String createNewEmployeeScriptContent(final String firstName, final String lastName, final AddressRef... addresses) {
        final StringBuilder sb = new StringBuilder();
        sb.append("import ");
        sb.append(EMPLOYEE_QUALIF_CLASSNAME);
        sb.append("\n");
        sb.append("import ");
        sb.append(ADDRESS_QUALIF_NAME);
        sb.append("\n");
        sb.append("Employee e = new Employee();");
        sb.append("\n");
        sb.append("e.firstName =");
        sb.append("'" + firstName + "'");
        sb.append("\n");
        sb.append("e.lastName =");
        sb.append("'" + lastName + "'");
        sb.append("\n");
        if (addresses != null) {
            for (int i = 0; i < addresses.length; i++) {
                sb.append("e.addToAddresses(" + addresses[i].getVarName() + ")");
                sb.append("\n");
            }
        }

        sb.append("return e;");
        return sb.toString();
    }

    private String createNewAddressScriptContent(final String street, final String city) {
        final StringBuilder sb = new StringBuilder();
        sb.append("import ");
        sb.append(ADDRESS_QUALIF_NAME);
        sb.append("\n");
        sb.append("Address a = new Address();");
        sb.append("\n");
        sb.append("a.street =");
        sb.append("'" + street + "'");
        sb.append("\n");
        sb.append("a.city =");
        sb.append("'" + city + "'");
        sb.append("\n");
        sb.append("return a;");
        return sb.toString();
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

    @Cover(classes = { Operation.class }, concept = BPMNConcept.OPERATION, keywords = { "BusinessData", "java setter operation", "mandatory field",
    "intermixed" }, jira = "BS-8591", story = "Create business datas using intermixed java setter operations.")
    @Test
    public void shouldBeAbleToCreate2BusinessDataUsingIntermixedBizDataJavaSetterOperations() throws Exception {
        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance(
                "shouldBeAbleToUpdateBusinessDataUsingJavaSetterOperation", PROCESS_VERSION);
        final String businessDataName = "newBornBaby";
        final String businessDataName2 = "data2";
        processDefinitionBuilder.addBusinessData(businessDataName, EMPLOYEE_QUALIF_CLASSNAME, null);
        processDefinitionBuilder.addBusinessData(businessDataName2, EMPLOYEE_QUALIF_CLASSNAME, null);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder
        .addAutomaticTask("step1")
        .addOperation(
                new OperationBuilder().createBusinessDataSetAttributeOperation(businessDataName, "setFirstName", String.class.getName(),
                        new ExpressionBuilder().createConstantStringExpression("Manon")))
                        .addOperation(
                                new OperationBuilder().createBusinessDataSetAttributeOperation(businessDataName2, "setFirstName", String.class.getName(),
                                        new ExpressionBuilder().createConstantStringExpression("Plop")))
                                        .addOperation(
                                                new OperationBuilder().createBusinessDataSetAttributeOperation(businessDataName, "setLastName", String.class.getName(),
                                                        new ExpressionBuilder().createConstantStringExpression("Péuigrec")))
                                                        .addOperation(
                                                                new OperationBuilder().createBusinessDataSetAttributeOperation(businessDataName2, "setLastName", String.class.getName(),
                                                                        new ExpressionBuilder().createConstantStringExpression("Plip")));
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final long processInstanceId = getProcessAPI().startProcess(definition.getId()).getId();
        waitForUserTask("step2", processInstanceId);

        disableAndDeleteProcess(definition.getId());
    }

    @Test
    public void shouldBeAbleToDeleteABusinessDataUsingOperation() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; Employee e = new Employee(); e.firstName = 'John'; e.lastName = 'Doe'; return e;", EMPLOYEE_QUALIF_CLASSNAME);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance(
                "shouldBeAbleToUpdateBusinessDataUsingJavaSetterOperation", "6.3-beta");
        final String businessDataName = "employee";
        processDefinitionBuilder.addBusinessData(businessDataName, EMPLOYEE_QUALIF_CLASSNAME, employeeExpression);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addOperation(new OperationBuilder().deleteBusinessDataOperation(businessDataName));
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final long processInstanceId = getProcessAPI().startProcess(definition.getId()).getId();

        final HumanTaskInstance userTask = waitForUserTask("step1", processInstanceId);
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>(2);
        expressions.put(new ExpressionBuilder().createQueryBusinessDataExpression("countEmployee", "Employee.countEmployee", Long.class.getName()),
                Collections.<String, Serializable> emptyMap());

        Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
        assertThat(result.get("countEmployee")).isEqualTo(1L);

        assignAndExecuteStep(userTask, matti.getId());
        waitForUserTask("step2", processInstanceId);
        result = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
        assertThat(result.get("countEmployee")).isEqualTo(0L);

        disableAndDeleteProcess(definition.getId());
    }

    public void assertCount(final long processInstanceId) throws Exception {
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>(2);
        expressions.put(new ExpressionBuilder().createQueryBusinessDataExpression("countEmployee", "Employee.countEmployee", Long.class.getName()),
                Collections.<String, Serializable> emptyMap());

        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
        assertThat(result.get("countEmployee")).isEqualTo(1L);
    }

    class AddressRef {

        private final String varName;
        private final String street;
        private final String city;

        AddressRef(final String varName, final String street, final String city) {
            this.varName = varName;
            this.street = street;
            this.city = city;
        }

        public Expression createDependency() throws InvalidExpressionException {
            return new ExpressionBuilder().createBusinessDataExpression(getVarName(), ADDRESS_QUALIF_NAME);
        }

        public String getVarName() {
            return varName;
        }

        public String getStreet() {
            return street;
        }

        public String getCity() {
            return city;
        }

    }

    @Test
    public void deployABDRAndCreateAndUdpateAMultipleBusinessData() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployees", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; Employee john = new Employee(); john.firstName = 'John'; john.lastName = 'Doe';"
                + " Employee jane = new Employee(); jane.firstName = 'Jane'; jane.lastName = 'Doe'; return [jane, john];", List.class.getName());

        final Expression jackExpression = new ExpressionBuilder().createGroovyScriptExpression("createJack", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; Employee jack = new Employee(); jack.firstName = 'Jack'; jack.lastName = 'Doe'; return jack;", EMPLOYEE_QUALIF_CLASSNAME);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addBusinessData("myEmployees", EMPLOYEE_QUALIF_CLASSNAME, employeeExpression).setMultiple(true);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME)
        .addOperation(new OperationBuilder().createBusinessDataSetAttributeOperation("myEmployees", "add", Object.class.getName(), jackExpression));
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());

        final HumanTaskInstance userTask = waitForUserTask("step1", instance.getId());
        String employeeToString = getEmployeesToString("myEmployees", instance.getId());
        assertThat(employeeToString).isEqualTo("Employee [firstName=[Jane, John], lastName=[Doe, Doe]]");

        assignAndExecuteStep(userTask, matti.getId());
        waitForUserTask("step2", instance.getId());
        employeeToString = getEmployeesToString("myEmployees", instance.getId());
        assertThat(employeeToString).isEqualTo("Employee [firstName=[Jane, John, Jack], lastName=[Doe, Doe, Doe]]");

        disableAndDeleteProcess(definition.getId());
    }

    private String getEmployeesToString(final String businessDataName, final long processInstanceId) throws InvalidExpressionException {
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>(5);
        final String expressionEmployee = "retrieve_Employee";
        expressions.put(
                new ExpressionBuilder().createGroovyScriptExpression(expressionEmployee, "\"Employee [firstName=\" + " + businessDataName
                        + ".firstName + \", lastName=\" + " + businessDataName + ".lastName + \"]\";", String.class.getName(),
                        new ExpressionBuilder().createBusinessDataExpression(businessDataName, List.class.getName())), null);
        try {
            final Map<String, Serializable> evaluatedExpressions = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
            return (String) evaluatedExpressions.get(expressionEmployee);
        } catch (final ExpressionEvaluationException eee) {
            System.err.println(eee.getMessage());
            return null;
        }
    }

    @Test
    public void useMultipleBusinessDataInAUserTaskWithMultiInstance() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployees", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; Employee john = new Employee(); john.firstName = 'John'; john.lastName = 'Doe';"
                + " Employee jane = new Employee(); jane.firstName = 'Jane'; jane.lastName = 'Doe'; [jane, john]", List.class.getName());

        final ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt().createNewInstance("MBIMI", "1.2-beta");
        builder.addBusinessData("myEmployees", EMPLOYEE_QUALIF_CLASSNAME, employeeExpression).setMultiple(true);
        builder.addActor(ACTOR_NAME);
        final UserTaskDefinitionBuilder userTaskBuilder = builder.addUserTask("step1", ACTOR_NAME);
        userTaskBuilder.addBusinessData("employee", EMPLOYEE_QUALIF_CLASSNAME);
        userTaskBuilder.addMultiInstance(false, "myEmployees").addDataInputItemRef("employee");
        userTaskBuilder.addOperation(new OperationBuilder().createBusinessDataSetAttributeOperation("employee", "setLastName", String.class.getName(),
                new ExpressionBuilder().createConstantStringExpression("Smith")));
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addTransition("step1", "step2");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);

        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        HumanTaskInstance userTask = waitForUserTask("step1", instance);
        assignAndExecuteStep(userTask, matti.getId());
        userTask = waitForUserTask("step1", instance);
        assignAndExecuteStep(userTask, matti.getId());

        waitForUserTask("step2", instance.getId());
        final String employeeToString = getEmployeesToString("myEmployees", instance.getId());
        assertThat(employeeToString).isEqualTo("Employee [firstName=[Jane, John], lastName=[Smith, Smith]]");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void useMultipleBusinessDataInACallActivityWithSequentialMultiInstance() throws Exception {
        ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt().createNewInstance("UpdateEmployee", "1.2-beta");
        builder.addActor(ACTOR_NAME);
        builder.addBusinessData("employee", EMPLOYEE_QUALIF_CLASSNAME, null);
        final OperationBuilder operationBuilder = new OperationBuilder();
        builder.addUserTask("step1", ACTOR_NAME)
        .addOperation(operationBuilder.createBusinessDataSetAttributeOperation("employee", "setLastName", String.class.getName(),
                new ExpressionBuilder().createConstantStringExpression("Smith")));
        final ProcessDefinition subProcessDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);

        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployees", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; Employee john = new Employee(); john.firstName = 'John'; john.lastName = 'Doe';"
                + " Employee jane = new Employee(); jane.firstName = 'Jane'; jane.lastName = 'Doe'; return [jane, john];", List.class.getName());

        builder = new ProcessDefinitionBuilderExt().createNewInstance("MBIMI", "1.2-beta");
        builder.addBusinessData("myEmployees", EMPLOYEE_QUALIF_CLASSNAME, employeeExpression).setMultiple(true);
        builder.addActor(ACTOR_NAME);
        final CallActivityBuilder callActivity = builder.addCallActivity("step1",
                new ExpressionBuilder().createConstantStringExpression(subProcessDefinition.getName()),
                new ExpressionBuilder().createConstantStringExpression(subProcessDefinition.getVersion()));
        callActivity.addBusinessData("miEmployee", EMPLOYEE_QUALIF_CLASSNAME);
        callActivity.addDataInputOperation(
                operationBuilder.createNewInstance()
                .attachBusinessDataSetAttributeOperation("employee",
                        new ExpressionBuilder().createBusinessDataExpression("miEmployee", EMPLOYEE_QUALIF_CLASSNAME)))
                        .addMultiInstance(true, "myEmployees").addDataInputItemRef("miEmployee");
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addTransition("step1", "step2");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);

        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("step1", matti);
        waitForUserTaskAndExecuteIt("step1", matti);
        waitForUserTask("step2", instance.getId());

        final String employeeToString = getEmployeesToString("myEmployees", instance.getId());
        assertThat(employeeToString).contains("Jane", "John", "Smith").doesNotContain("Doe");

        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(subProcessDefinition);
    }

    @Test
    public void useMultipleBusinessDataInACallActivityWithInDataMultiInstance() throws Exception {
        ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt().createNewInstance("UpdateEmployee", "1.2-beta");
        builder.addActor(ACTOR_NAME);
        builder.addBusinessData("employee", EMPLOYEE_QUALIF_CLASSNAME, null);
        builder.addUserTask("step1", ACTOR_NAME)
        .addOperation(new OperationBuilder().createBusinessDataSetAttributeOperation("employee", "setLastName", String.class.getName(),
                new ExpressionBuilder().createConstantStringExpression("Smith")));
        final ProcessDefinition subProcessDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);

        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployees", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; Employee john = new Employee(); john.firstName = 'John'; john.lastName = 'Doe';"
                + " Employee jane = new Employee(); jane.firstName = 'Jane'; jane.lastName = 'Doe'; [jane, john];", List.class.getName());

        builder = new ProcessDefinitionBuilderExt().createNewInstance("MBIMI", "1.2-beta");
        builder.addBusinessData("myEmployees", EMPLOYEE_QUALIF_CLASSNAME, employeeExpression).setMultiple(true);
        builder.addBusinessData("myNewEmployees", EMPLOYEE_QUALIF_CLASSNAME, null).setMultiple(true);
        builder.addActor(ACTOR_NAME);
        final CallActivityBuilder callActivityBuilder = builder.addCallActivity("step1",
                new ExpressionBuilder().createConstantStringExpression(subProcessDefinition.getName()),
                new ExpressionBuilder().createConstantStringExpression(subProcessDefinition.getVersion()));
        callActivityBuilder.addBusinessData("miEmployee", EMPLOYEE_QUALIF_CLASSNAME);
        callActivityBuilder.addBusinessData("newEmployee", EMPLOYEE_QUALIF_CLASSNAME);
        callActivityBuilder.addDataInputOperation(
                new OperationBuilder().attachBusinessDataSetAttributeOperation("employee",
                        new ExpressionBuilder().createBusinessDataExpression("miEmployee", EMPLOYEE_QUALIF_CLASSNAME)));
        callActivityBuilder.addDataOutputOperation(
                new OperationBuilder().attachBusinessDataSetAttributeOperation("newEmployee",
                        new ExpressionBuilder().createBusinessDataExpression("employee", EMPLOYEE_QUALIF_CLASSNAME)));
        callActivityBuilder.addMultiInstance(false, "myEmployees").addDataInputItemRef("miEmployee")
        .addDataOutputItemRef("newEmployee").addLoopDataOutputRef("myNewEmployees");
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addTransition("step1", "step2");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);

        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("step1", matti);
        waitForUserTaskAndExecuteIt("step1", matti);
        waitForUserTask("step2", instance.getId());

        final String employeeToString = getEmployeesToString("myNewEmployees", instance.getId());
        assertThat(employeeToString).contains("Jane", "John", "Smith").doesNotContain("Doe");

        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(subProcessDefinition);
    }

    //@Test deactivated until it is stable
    public void useMultipleBusinessDataInACallActivityWithOutDataMultiInstance() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; Employee john = new Employee(); john.firstName = 'John' + activityInstanceId; john.lastName = 'Doe'; john;",
                EMPLOYEE_QUALIF_CLASSNAME, new ExpressionBuilder().createEngineConstant(ExpressionConstants.ACTIVITY_INSTANCE_ID));
        ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt().createNewInstance("createEmployee", "1.2-beta");
        builder.addActor(ACTOR_NAME);
        builder.addBusinessData("employee", EMPLOYEE_QUALIF_CLASSNAME, null);
        builder.addUserTask("step1", ACTOR_NAME).addOperation(new OperationBuilder().attachBusinessDataSetAttributeOperation("employee",
                employeeExpression));
        final ProcessDefinition subProcessDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);

        builder = new ProcessDefinitionBuilderExt().createNewInstance("MBIMI", "1.2-beta");
        builder.addBusinessData("myEmployees", EMPLOYEE_QUALIF_CLASSNAME, null).setMultiple(true);
        builder.addActor(ACTOR_NAME);
        final CallActivityBuilder callActivityBuilder = builder.addCallActivity("step1",
                new ExpressionBuilder().createConstantStringExpression(subProcessDefinition.getName()),
                new ExpressionBuilder().createConstantStringExpression(subProcessDefinition.getVersion()));
        callActivityBuilder.addBusinessData("newEmployee", EMPLOYEE_QUALIF_CLASSNAME);
        callActivityBuilder.addDataOutputOperation(
                new OperationBuilder().attachBusinessDataSetAttributeOperation("newEmployee",
                        new ExpressionBuilder().createBusinessDataExpression("employee", EMPLOYEE_QUALIF_CLASSNAME)));
        callActivityBuilder.addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(2)).addDataOutputItemRef("newEmployee")
        .addLoopDataOutputRef("myEmployees");
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addTransition("step1", "step2");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);

        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("step1", matti);
        waitForUserTaskAndExecuteIt("step1", matti);
        waitForUserTask("step2", instance.getId());

        final String employeeToString = getEmployeesToString("myEmployees", instance.getId());
        assertThat(employeeToString).contains("John", "Doe");

        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(subProcessDefinition);
    }

    @Test
    public void should_return_the_list_of_entities_from_the_multiple_instance() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployees", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; Employee john = new Employee(); john.firstName = 'John'; john.lastName = 'Doe';"
                + " Employee jane = new Employee(); jane.firstName = 'Jane'; jane.lastName = 'Doe'; [jane, john]", List.class.getName());

        final ProcessDefinitionBuilderExt builder = new ProcessDefinitionBuilderExt().createNewInstance("MBIMI", "1.2-beta");
        builder.addBusinessData("myEmployees", EMPLOYEE_QUALIF_CLASSNAME, employeeExpression).setMultiple(true);
        builder.addData("names", List.class.getName(), null);
        builder.addActor(ACTOR_NAME);
        final UserTaskDefinitionBuilder userTaskBuilder = builder.addUserTask("step1", ACTOR_NAME);
        userTaskBuilder.addBusinessData("employee", EMPLOYEE_QUALIF_CLASSNAME);
        userTaskBuilder.addShortTextData("name", null);
        userTaskBuilder.addMultiInstance(false, "myEmployees").addDataInputItemRef("employee").addDataOutputItemRef("name").addLoopDataOutputRef("names");
        userTaskBuilder.addOperation(new OperationBuilder().createSetDataOperation("name", new ExpressionBuilder().createConstantStringExpression("Doe")));
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addTransition("step1", "step2");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);

        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        HumanTaskInstance userTask = waitForUserTask("step1", instance);
        assignAndExecuteStep(userTask, matti.getId());
        userTask = waitForUserTask("step1", instance);
        assignAndExecuteStep(userTask, matti.getId());

        waitForUserTask("step2", instance.getId());

        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance("names", instance.getId());
        assertThat(dataInstance.getValue().toString()).isEqualTo("[Doe, Doe]");

        disableAndDeleteProcess(processDefinition);
    }

    public void getProcessBusinessDataReferencesShoulReturnTheListOfReferences() throws Exception {
        final String taskName = "step";
        final ProcessDefinition definition = buildProcessThatUpdateBizDataInsideConnector(taskName);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());
        waitForUserTask(taskName, instance.getId());

        final List<BusinessDataReference> references = getProcessAPI().getProcessBusinessDataReferences(instance.getId(), 0, 10);

        assertThat(references).hasSize(1);
        assertThat(((SimpleBusinessDataReference) references.get(0)).getStorageId()).isNotNull();

        disableAndDeleteProcess(definition);
    }

    @Test
    public void commandGetBusinessData_should_return_a_simple_lazy_child() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; import org.bonita.pojo.Address; Employee e = new Employee(); e.firstName = 'Alphonse';"
                + " e.lastName = 'Dupond'; e.setAddress(myAddress); return e;", EMPLOYEE_QUALIF_CLASSNAME,
                new ExpressionBuilder().createBusinessDataExpression("myAddress", ADDRESS_QUALIF_NAME));
        final Expression addressExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewAddress",
                "import org.bonita.pojo.Address; import org.bonita.pojo.Country; "
                        + "Country c = new Country(); c.name='France'; "
                        + "Address a = new Address(); a.street='32, rue Gustave Eiffel'; a.city='Grenoble'; a.country = c; a;",
                ADDRESS_QUALIF_NAME);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance(
                "rest", "1.0");
        final String bizDataName = "myEmployee";
        processDefinitionBuilder.addBusinessData(bizDataName, EMPLOYEE_QUALIF_CLASSNAME, null);
        processDefinitionBuilder.addBusinessData("myAddress", ADDRESS_QUALIF_NAME, null);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addAutomaticTask("step1")
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand("myAddress"), OperatorType.ASSIGNMENT, null, null, addressExpression)
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand(bizDataName), OperatorType.ASSIGNMENT, null, null, employeeExpression);
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final long processInstanceId = getProcessAPI().startProcess(definition.getId()).getId();

        waitForUserTask("step2", processInstanceId);

        final SimpleBusinessDataReference businessDataReference = (SimpleBusinessDataReference) getProcessAPI().getProcessBusinessDataReference(bizDataName,
                processInstanceId);

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("businessDataId", businessDataReference.getStorageId());
        parameters.put("entityClassName", EMPLOYEE_QUALIF_CLASSNAME);
        parameters.put("businessDataChildName", "address");
        parameters.put("businessDataURIPattern", "/businessdata/{className}/{id}/{field}");
        final String result = (String) getCommandAPI().execute("getBusinessDataById", parameters);

        assertThat(result).as("Address should have the right street and city").contains("\"street\" : \"32, rue Gustave Eiffel\"")
                .contains("\"city\" : \"Grenoble\"")
                .contains("\"rel\" : \"country\"");

        disableAndDeleteProcess(definition.getId());
    }

}
