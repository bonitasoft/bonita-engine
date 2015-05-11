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
 */
package org.bonitasoft.engine.business.data;

import static net.javacrumbs.jsonunit.assertj.JsonAssert.assertThatJson;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.bdm.BusinessObjectDAOFactory;
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import org.bonitasoft.engine.bdm.dao.BusinessObjectDAO;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessEnablementException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.CallActivityBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.engine.expression.ExpressionType;
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

public class BDRepositoryIT extends CommonAPIIT {

    private static final String BDM_PACKAGE_PREFIX = "com.company.model";

    private static final String COUNTRY_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".Country";

    private static final String ADDRESS_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".Address";

    private static final String EMPLOYEE_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".Employee";

    private static final String PRODUCT_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".Product";

    private static final String PRODUCT_CATALOG_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".ProductCatalog";

    private static final String PERSON_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".Person";

    private static final String GET_EMPLOYEE_BY_LAST_NAME_QUERY_NAME = "findByLastName";

    private static final String GET_EMPLOYEE_BY_PHONE_NUMBER_QUERY_NAME = "findByPhoneNumber";

    private static final String FIND_BY_FIRST_NAME_FETCH_ADDRESSES = "findByFirstNameFetchAddresses";

    private static final String FIND_BY_FIRST_NAME_AND_LAST_NAME_NEW_ORDER = "findByFirstNameAndLastNameNewOrder";

    private static final String COUNT_EMPLOYEE = "countEmployee";

    private static final String COUNT_ADDRESS = "countAddress";

    private static final String CLIENT_BDM_ZIP_FILENAME = "client-bdm.zip";

    public static final String BUSINESS_DATA_CLASS_NAME_ID_FIELD = "/businessdata/{className}/{id}/{field}";

    public static final String ENTITY_CLASS_NAME = "entityClassName";

    public static final String FIND_BY_HIRE_DATE_RANGE = "findByHireDateRange";

    private User matti;

    private File clientFolder;

    private BusinessObjectModel buildBOM() {
        final SimpleField name = new SimpleField();
        name.setName("name");
        name.setType(FieldType.STRING);

        final BusinessObject countryBO = new BusinessObject();
        countryBO.setQualifiedName(COUNTRY_QUALIFIED_NAME);
        countryBO.addField(name);
        countryBO.addUniqueConstraint("uk_name", "name");

        final SimpleField street = new SimpleField();
        street.setName("street");
        street.setType(FieldType.STRING);

        final SimpleField city = new SimpleField();
        city.setName("city");
        city.setType(FieldType.STRING);

        final RelationField country = new RelationField();
        country.setType(RelationField.Type.AGGREGATION);
        country.setFetchType(RelationField.FetchType.LAZY);
        country.setName("country");
        country.setCollection(Boolean.FALSE);
        country.setNullable(Boolean.TRUE);
        country.setReference(countryBO);

        final BusinessObject addressBO = new BusinessObject();
        addressBO.setQualifiedName(ADDRESS_QUALIFIED_NAME);
        addressBO.addField(street);
        addressBO.addField(city);
        addressBO.addField(country);
        addressBO.addQuery(COUNT_ADDRESS, "SELECT count(a) FROM Address a", Long.class.getName());

        final RelationField addresses = new RelationField();
        addresses.setType(RelationField.Type.AGGREGATION);
        addresses.setFetchType(RelationField.FetchType.EAGER);
        addresses.setName("addresses");
        addresses.setCollection(Boolean.TRUE);
        addresses.setNullable(Boolean.TRUE);
        addresses.setReference(addressBO);

        final RelationField address = new RelationField();
        address.setType(RelationField.Type.AGGREGATION);
        address.setFetchType(RelationField.FetchType.LAZY);
        address.setName("address");
        address.setCollection(Boolean.FALSE);
        address.setNullable(Boolean.TRUE);
        address.setReference(addressBO);

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

        final SimpleField hireDate = new SimpleField();
        hireDate.setName("hireDate");
        hireDate.setType(FieldType.DATE);

        final SimpleField booleanField = new SimpleField();
        booleanField.setName("booleanField");
        booleanField.setType(FieldType.BOOLEAN);

        final BusinessObject employee = new BusinessObject();
        employee.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
        employee.addField(hireDate);
        employee.addField(booleanField);
        employee.addField(firstName);
        employee.addField(lastName);
        employee.addField(phoneNumbers);
        employee.addField(addresses);
        employee.addField(address);
        employee.setDescription("Describe a simple employee");
        employee.addUniqueConstraint("uk_fl", "firstName", "lastName");

        final Query getEmployeeByPhoneNumber = employee.addQuery(GET_EMPLOYEE_BY_PHONE_NUMBER_QUERY_NAME,
                "SELECT e FROM Employee e WHERE :phoneNumber IN ELEMENTS(e.phoneNumbers)", List.class.getName());
        getEmployeeByPhoneNumber.addQueryParameter("phoneNumber", String.class.getName());

        final Query findByFirstNAmeAndLastNameNewOrder = employee.addQuery(FIND_BY_FIRST_NAME_AND_LAST_NAME_NEW_ORDER,
                "SELECT e FROM Employee e WHERE e.firstName =:firstName AND e.lastName = :lastName ORDER BY e.lastName", List.class.getName());
        findByFirstNAmeAndLastNameNewOrder.addQueryParameter("firstName", String.class.getName());
        findByFirstNAmeAndLastNameNewOrder.addQueryParameter("lastName", String.class.getName());

        final Query findByFirstNameFetchAddresses = employee.addQuery(FIND_BY_FIRST_NAME_FETCH_ADDRESSES,
                "SELECT e FROM Employee e INNER JOIN FETCH e.addresses WHERE e.firstName =:firstName ORDER BY e.lastName", List.class.getName());
        findByFirstNameFetchAddresses.addQueryParameter("firstName", String.class.getName());

        final Query findByHireDate = employee.addQuery(FIND_BY_HIRE_DATE_RANGE,
                "SELECT e FROM Employee e WHERE e.hireDate >=:date1 and e.hireDate <=:date2", List.class.getName());
        findByHireDate.addQueryParameter("date1", Date.class.getName());
        findByHireDate.addQueryParameter("date2", Date.class.getName());

        employee.addQuery(COUNT_EMPLOYEE, "SELECT COUNT(e) FROM Employee e", Long.class.getName());

        employee.addIndex("IDX_LSTNM", "lastName");
        employee.addIndex("IDX_LSTNM", "address");

        final BusinessObject person = new BusinessObject();
        person.setQualifiedName(PERSON_QUALIFIED_NAME);
        person.addField(hireDate);
        person.addField(firstName);
        person.addField(lastName);
        person.addField(phoneNumbers);
        person.setDescription("Describe a simple person");
        person.addUniqueConstraint("uk_fl", "firstName", "lastName");

        final BusinessObject productBO = new BusinessObject();
        productBO.setQualifiedName(PRODUCT_QUALIFIED_NAME);
        productBO.addField(name);

        final RelationField products = new RelationField();
        products.setType(RelationField.Type.AGGREGATION);
        products.setFetchType(RelationField.FetchType.LAZY);
        products.setName("products");
        products.setCollection(Boolean.TRUE);
        products.setNullable(Boolean.TRUE);
        products.setReference(productBO);

        final BusinessObject catalogBO = new BusinessObject();
        catalogBO.setQualifiedName(PRODUCT_CATALOG_QUALIFIED_NAME);
        catalogBO.addField(name);
        catalogBO.addField(products);

        final BusinessObjectModel model = new BusinessObjectModel();
        model.addBusinessObject(employee);
        model.addBusinessObject(person);
        model.addBusinessObject(addressBO);
        model.addBusinessObject(countryBO);
        model.addBusinessObject(productBO);
        model.addBusinessObject(catalogBO);
        return model;
    }

    private Long tenantId;

    @Before
    public void setUp() throws Exception {
        clientFolder = IOUtil.createTempDirectoryInDefaultTempDirectory("bdr_it_client");
        loginOnDefaultTenantWithDefaultTechnicalUser();
        matti = createUser("matti", "bpm");

        assertThat(getTenantAdministrationAPI().isPaused()).as("should not have tenant is paused mode").isFalse();

        installBusinessDataModel(buildBOM());

        assertThat(getTenantAdministrationAPI().isPaused()).as("should have resume tenant after installing Business Object Model").isFalse();

        tenantId = getSession().getTenantId();
    }

    @After
    public void tearDown() throws Exception {
        try {
            FileUtils.deleteDirectory(clientFolder);
        } catch (final Exception e) {
            clientFolder.deleteOnExit();
        }
        uninstallBusinessObjectModel();

        deleteUser(matti);
        logoutOnTenant();
    }

    private void uninstallBusinessObjectModel() throws UpdateException, BusinessDataRepositoryDeploymentException {
        if (!getTenantAdministrationAPI().isPaused()) {
            getTenantAdministrationAPI().pause();
            getTenantAdministrationAPI().cleanAndUninstallBusinessDataModel();
            getTenantAdministrationAPI().resume();
        }
    }

    @Test
    public void deploying_bdm_after_process_should_put_process_in_resolved_state() throws Exception {
        final String qualifiedName = "com.company.test.Bo";
        final BusinessObjectModel bom = buildSimpleBom(qualifiedName);

        final ProcessDefinition processDefinition = deploySimpleProcessWithBusinessData(qualifiedName);

        ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertThat(processDeploymentInfo.getConfigurationState()).isEqualTo(ConfigurationState.UNRESOLVED);

        installBusinessDataModel(bom);

        processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertThat(processDeploymentInfo.getConfigurationState()).isEqualTo(ConfigurationState.RESOLVED);

        deleteProcess(processDefinition);
    }

    private void installBusinessDataModel(final BusinessObjectModel bom) throws Exception {
        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        final byte[] zip = converter.zip(bom);
        getTenantAdministrationAPI().pause();
        getTenantAdministrationAPI().cleanAndUninstallBusinessDataModel();
        getTenantAdministrationAPI().installBusinessDataModel(zip);
        getTenantAdministrationAPI().resume();
    }

    private ProcessDefinition deploySimpleProcessWithBusinessData(final String aQualifiedName) throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        final String bizDataName = "myBizData";
        processDefinitionBuilder.addBusinessData(bizDataName, aQualifiedName, null);

        final ProcessDefinition processDefinition = deployProcess(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinitionBuilder.done()).done());
        getProcessAPI().addUserToActor(ACTOR_NAME, processDefinition, matti.getId());
        return processDefinition;
    }

    private BusinessObjectModel buildSimpleBom(final String boQualifiedName) throws IOException, JAXBException, SAXException {
        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName(boQualifiedName);
        final SimpleField field = new SimpleField();
        field.setName("aField");
        field.setType(FieldType.STRING);
        bo.addField(field);
        final BusinessObjectModel model = new BusinessObjectModel();
        model.addBusinessObject(bo);
        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        return model;
    }

    @Cover(classes = { Operation.class }, concept = BPMNConcept.OPERATION, keywords = { "BusinessData", "business data java setter operation" }, jira = "BS-7217", story = "update a business data using a java setter operation")
    @Test
    public void shouldBeAbleToUpdateBusinessDataUsingBizDataJavaSetterOperation() throws Exception {
        final String processContractInputName = "lastName_input";
        final String initialLastNameValue = "Trebi";
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression(
                "createNewEmployee",
                new StringBuilder().append("import ")
                        .append(EMPLOYEE_QUALIFIED_NAME)
                        .append("; Employee e = new Employee(); e.firstName = 'Jules'; e.lastName = " + processContractInputName + "; return e;").toString(),
                EMPLOYEE_QUALIFIED_NAME, new ExpressionBuilder().createContractInputExpression(processContractInputName, String.class.getName()));

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(
                "shouldBeAbleToUpdateBusinessDataUsingJavaSetterOperation", "6.3-beta");
        final String businessDataName = "newBornBaby";
        final String newEmployeeFirstName = "Manon";
        final String newEmployeeLastName = "Péuigrec";
        processDefinitionBuilder.addBusinessData(businessDataName, EMPLOYEE_QUALIFIED_NAME, employeeExpression);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step0", ACTOR_NAME);
        processDefinitionBuilder
                .addAutomaticTask("step1")
                .addOperation(
                        new OperationBuilder().createBusinessDataSetAttributeOperation(businessDataName, "setFirstName", String.class.getName(),
                                new ExpressionBuilder().createConstantStringExpression(newEmployeeFirstName)))
                .addOperation(
                        new OperationBuilder().createBusinessDataSetAttributeOperation(businessDataName, "setLastName", String.class.getName(),
                                new ExpressionBuilder().createConstantStringExpression(newEmployeeLastName)));
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step0", "step1");
        processDefinitionBuilder.addTransition("step1", "step2");
        processDefinitionBuilder.addContract().addInput(processContractInputName, Type.TEXT, null);

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance processInstance = getProcessAPI().startProcessWithInputs(definition.getId(),
                Collections.singletonMap(processContractInputName, (Serializable) initialLastNameValue));

        final long step0 = waitForUserTask(processInstance, "step0");

        // Check that initial BizData value used process contract input:
        Map<Expression, Map<String, Serializable>> expressions = new HashMap<>(1);
        final String expressionName = "bizDataExprName";
        expressions.put(new ExpressionBuilder().createGroovyScriptExpression(expressionName, businessDataName + ".lastName", String.class.getName(),
                new ExpressionBuilder().createBusinessDataExpression(businessDataName, EMPLOYEE_QUALIFIED_NAME)), null);
        final String returnedInitialLastName = (String) getProcessAPI().evaluateExpressionsOnProcessInstance(processInstance.getId(), expressions).get(
                expressionName);
        assertThat(returnedInitialLastName).isEqualTo(initialLastNameValue);

        assignAndExecuteStep(step0, matti);
        waitForUserTask(processInstance, "step2");

        // Let's check the updated firstName + lastName values by calling an expression:
        expressions = new HashMap<>(2);
        final String expressionFirstName = "retrieve_FirstName";
        expressions.put(new ExpressionBuilder().createGroovyScriptExpression(expressionFirstName, businessDataName + ".firstName", String.class.getName(),
                new ExpressionBuilder().createBusinessDataExpression(businessDataName, EMPLOYEE_QUALIFIED_NAME)), null);
        final String expressionLastName = "retrieve_new_lastName";
        expressions.put(new ExpressionBuilder().createGroovyScriptExpression(expressionLastName, businessDataName + ".lastName", String.class.getName(),
                new ExpressionBuilder().createBusinessDataExpression(businessDataName, EMPLOYEE_QUALIFIED_NAME)), null);
        final Map<String, Serializable> evaluatedExpressions = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstance.getId(), expressions);
        final String returnedFirstName = (String) evaluatedExpressions.get(expressionFirstName);
        final String returnedLastName = (String) evaluatedExpressions.get(expressionLastName);
        assertThat(returnedFirstName).isEqualTo(newEmployeeFirstName);
        assertThat(returnedLastName).isEqualTo(newEmployeeLastName);

        assertCount(processInstance.getId());
        disableAndDeleteProcess(definition.getId());
    }

    @Test
    public void deployABDRAndCreateADefaultBusinessDataAndReuseReference() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", new StringBuilder().append("import ")
                .append(EMPLOYEE_QUALIFIED_NAME).append("; Employee e = new Employee(); e.firstName = 'Jane'; e.lastName = 'Doe'; return e;").toString(),
                EMPLOYEE_QUALIFIED_NAME);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIFIED_NAME, employeeExpression);
        final String secondBizData = "people";
        processDefinitionBuilder.addBusinessData(secondBizData, EMPLOYEE_QUALIFIED_NAME, null);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addOperation(
                new OperationBuilder().attachBusinessDataSetAttributeOperation(secondBizData, new ExpressionBuilder().createQueryBusinessDataExpression(
                        "oneEmployee", "Employee." + GET_EMPLOYEE_BY_LAST_NAME_QUERY_NAME, EMPLOYEE_QUALIFIED_NAME,
                        new ExpressionBuilder().createConstantStringExpression("lastName", "Doe"))));
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());

        final long step1Id = waitForUserTask(processInstance, "step1");
        final String employeeToString = getEmployeeToString("myEmployee", processInstance.getId());
        assertThat(employeeToString).isEqualTo("Employee [firstName=Jane, lastName=Doe]");

        assignAndExecuteStep(step1Id, matti);
        waitForUserTask(processInstance, "step2");
        final String people = getEmployeeToString(secondBizData, processInstance.getId());
        assertThat(people).isEqualTo("Employee [firstName=Jane, lastName=Doe]");

        disableAndDeleteProcess(definition.getId());
    }

    @Test
    public void deployABDRAndCreateABOAndUdpateThroughAGroovyScript() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", new StringBuilder().append("import ")
                .append(EMPLOYEE_QUALIFIED_NAME).append("; Employee e = new Employee(); e.firstName = 'John'; e.lastName = 'Doe'; return e;").toString(),
                EMPLOYEE_QUALIFIED_NAME);

        final Expression getEmployeeExpression = new ExpressionBuilder().createBusinessDataExpression("myEmployee", EMPLOYEE_QUALIFIED_NAME);
        // try to modify the business data
        final Expression scriptExpression = new ExpressionBuilder().createGroovyScriptExpression("updateBizData", "myEmployee.lastName = 'BPM'; return 'BPM'",
                String.class.getName(), getEmployeeExpression);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIFIED_NAME, employeeExpression);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addDisplayDescription(scriptExpression);

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());
        waitForUserTask(instance, "step1");

        final String employeeToString = getEmployeeToString("myEmployee", instance.getId());
        assertThat(employeeToString).isEqualTo("Employee [firstName=John, lastName=BPM]");

        disableAndDeleteProcess(definition.getId());
    }

    @Test(expected = ProcessEnablementException.class)
    public void deployProcessWithWrongBusinessDataTypeShouldNotBeDeployable() throws Exception {
        final User user = createUser("login1", "password");
        ProcessDefinition processDefinition = null;
        try {
            final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
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
                        new StringBuilder()
                                .append("import ")
                                .append(EMPLOYEE_QUALIFIED_NAME)
                                .append("; Employee e = new Employee(); e.firstName = 'John'; e.lastName = 'Doe'; return \"Employee [firstName=\" + e.firstName + \", lastName=\" + e.lastName + \"]\"")
                                .toString(),
                        String.class.getName());
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>();
        expressions.put(stringExpression, new HashMap<String, Serializable>());

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("test", "1.2-alpha");
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
                new StringBuilder().append("import ").append(EMPLOYEE_QUALIFIED_NAME).append("; Employee e = new Employee(); e.firstName = 'John'; return e;")
                        .toString(), EMPLOYEE_QUALIFIED_NAME);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIFIED_NAME, employeeExpression);
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
                new StringBuilder().append("import ").append(EMPLOYEE_QUALIFIED_NAME)
                        .append("; Employee e = new Employee(); e.firstName = 'John124578/'; e.lastName = 'Doe'; return e;").toString(),
                EMPLOYEE_QUALIFIED_NAME);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIFIED_NAME, employeeExpression);
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
        waitForUserTask(instance, taskName);

        final String employeeToString = getEmployeeToString("myEmployee", instance.getId());
        assertThat(employeeToString).isEqualTo("Employee [firstName=John, lastName=Hakkinen]");

        assertCount(instance.getId());
        disableAndDeleteProcess(definition);
    }

    @Test
    public void should_deploy_generate_client_bdm_jar_in_bonita_home() throws Exception {
        final String bonitaHomePath = System.getProperty(BonitaHome.BONITA_HOME);
        assertThat(new File(getClientBdmJarClassPath(bonitaHomePath), CLIENT_BDM_ZIP_FILENAME)).exists().isFile();

        assertThat(getTenantAdministrationAPI().getClientBDMZip()).isNotEmpty();
    }

    @Test(expected = BusinessDataRepositoryException.class)
    public void should_undeploy_delete_generate_client_bdm_jar_in_bonita_home() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        getTenantAdministrationAPI().pause();
        getTenantAdministrationAPI().uninstallBusinessDataModel();
        getTenantAdministrationAPI().resume();

        final String bonitaHomePath = System.getProperty(BonitaHome.BONITA_HOME);
        assertThat(new File(getClientBdmJarClassPath(bonitaHomePath), CLIENT_BDM_ZIP_FILENAME)).doesNotExist();

        getTenantAdministrationAPI().getClientBDMZip();
    }

    @Test
    public void shouldBeAbleToRunDAOCallThroughGroovy() throws Exception {
        final String firstName = "FlofFlof";
        final String lastName = "Boudin";
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", new StringBuilder().append("import ")
                .append(EMPLOYEE_QUALIFIED_NAME).append("; import ").append(ADDRESS_QUALIFIED_NAME).append("; Employee e = new Employee(); e.firstName = '")
                .append(firstName).append("'; e.lastName = '").append(lastName).append("'; e.addToAddresses(myAddress); return e;").toString(),
                EMPLOYEE_QUALIFIED_NAME,
                new ExpressionBuilder().createBusinessDataExpression("myAddress", ADDRESS_QUALIFIED_NAME));
        final Expression addressExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewAddress",
                "import " +
                        ADDRESS_QUALIFIED_NAME +
                        "; Address a = new Address(); a.street='32, rue Gustave Eiffel'; a.city='Grenoble'; return a;",
                ADDRESS_QUALIFIED_NAME);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(
                "shouldBeAbleToRunDAOCallThroughGroovy", "6.3.1");
        final String employeeDAOName = "employeeDAO";
        final String bizDataName = "myEmployee";
        processDefinitionBuilder.addBusinessData(bizDataName, EMPLOYEE_QUALIFIED_NAME, null);
        processDefinitionBuilder.addBusinessData("myAddress", ADDRESS_QUALIFIED_NAME, null);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addAutomaticTask("step1")
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand("myAddress"), OperatorType.ASSIGNMENT, null, null, addressExpression)
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand(bizDataName), OperatorType.ASSIGNMENT, null, null, employeeExpression);
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        waitForUserTask(processInstance, "step2");

        // Let's check we can retrieve firstName using DAO call:
        final long processInstanceId = processInstance.getId();
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>(1);
        final String getLastNameWithDAOExpression = "retrieveEmployeeByFirstName";
        expressions.put(
                new ExpressionBuilder().createGroovyScriptExpression(getLastNameWithDAOExpression, "import " + EMPLOYEE_QUALIFIED_NAME + "; Employee e = "
                        + employeeDAOName + ".findByFirstName('" + firstName + "', 0, 10).get(0); e.getAddresses().get(0).city", String.class.getName(),
                        new ExpressionBuilder().buildBusinessObjectDAOExpression(employeeDAOName, EMPLOYEE_QUALIFIED_NAME + "DAO")), null);
        expressions.put(new ExpressionBuilder().createQueryBusinessDataExpression(COUNT_ADDRESS, "Address." + COUNT_ADDRESS, Long.class.getName()), null);
        expressions.put(new ExpressionBuilder().createQueryBusinessDataExpression(COUNT_EMPLOYEE, "Employee." + COUNT_EMPLOYEE, Long.class.getName()), null);
        Map<String, Serializable> evaluatedExpressions = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
        String returnedLastName = (String) evaluatedExpressions.get(getLastNameWithDAOExpression);
        assertThat(returnedLastName).isEqualTo("Grenoble");

        Serializable nbOfAddress = evaluatedExpressions.get(COUNT_ADDRESS);
        Serializable nbOfEmployee = evaluatedExpressions.get(COUNT_EMPLOYEE);

        assertThat(nbOfAddress).isEqualTo(1L);
        assertThat(nbOfEmployee).isEqualTo(1L);

        logoutOnTenant();

        loginOnDefaultTenantWithDefaultTechnicalUser();
        getTenantAdministrationAPI().pause();
        getTenantAdministrationAPI().resume();
        logoutOnTenant();

        loginOnDefaultTenantWith("matti", "bpm");

        evaluatedExpressions = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
        returnedLastName = (String) evaluatedExpressions.get(getLastNameWithDAOExpression);
        assertThat(returnedLastName).isEqualTo("Grenoble");
        logoutOnTenant();
        loginOnDefaultTenantWithDefaultTechnicalUser();

        assertCount(processInstanceId);

        disableAndDeleteProcess(definition.getId());
    }

    @Test
    public void should_use_factory_to_instantiate_dao_on_client_side() throws Exception {
        final AddressRef ref1 = new AddressRef("newYorkAddr", "33, corner street", "NY");
        final AddressRef ref2 = new AddressRef("romeAddr", "2, plaza del popolo", "Roma");
        addEmployee("Marcel", "Pagnol", ref1, ref2);
        final APISession apiSession = getSession();
        final byte[] clientBDMZip = getTenantAdministrationAPI().getClientBDMZip();

        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        final ClassLoader classLoaderWithBDM = new ClassloaderRefresher().loadClientModelInClassloader(clientBDMZip, contextClassLoader,
                EMPLOYEE_QUALIFIED_NAME, clientFolder);

        try {
            Thread.currentThread().setContextClassLoader(classLoaderWithBDM);

            @SuppressWarnings("unchecked")
            final Class<? extends BusinessObjectDAO> daoInterface = (Class<? extends BusinessObjectDAO>) Class.forName(EMPLOYEE_QUALIFIED_NAME + "DAO", true,
                    classLoaderWithBDM);
            final BusinessObjectDAOFactory businessObjectDAOFactory = new BusinessObjectDAOFactory();
            final BusinessObjectDAO daoImpl = businessObjectDAOFactory.createDAO(apiSession, daoInterface);
            assertThat(daoImpl.getClass().getName()).isEqualTo(EMPLOYEE_QUALIFIED_NAME + "DAOImpl");

            Method daoMethod = daoImpl.getClass().getMethod("findByLastName", String.class, int.class, int.class);
            assertThat(daoMethod).isNotNull();
            assertThat(daoMethod.getReturnType().getName()).isEqualTo(List.class.getName());
            List<?> result = (List<?>) daoMethod.invoke(daoImpl, "Pagnol", 0, 10);
            assertThat(result).isNotEmpty().hasSize(1);

            result = (List<?>) daoMethod.invoke(daoImpl, "Hanin", 0, 10);
            assertThat(result).isEmpty();

            daoMethod = daoImpl.getClass().getMethod("findByFirstNameAndLastName", String.class, String.class);
            assertThat(daoMethod).isNotNull();
            assertThat(daoMethod.getReturnType().getName()).isEqualTo(EMPLOYEE_QUALIFIED_NAME);
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
                EMPLOYEE_QUALIFIED_NAME, dependencies);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIFIED_NAME, null);
        final UserTaskDefinitionBuilder task = processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        if (addresses != null) {
            for (final AddressRef ref : addresses) {
                processDefinitionBuilder.addBusinessData(ref.getVarName(), ADDRESS_QUALIFIED_NAME, null);
                final Expression addressExpression = new ExpressionBuilder().createGroovyScriptExpression("createAddress" + ref.getVarName(),
                        createNewAddressScriptContent(ref.getStreet(), ref.getCity()),
                        ADDRESS_QUALIFIED_NAME);
                task.addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand(ref.getVarName()),
                        OperatorType.ASSIGNMENT, null, null, addressExpression);
            }
        }

        task.addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand("myEmployee"),
                OperatorType.ASSIGNMENT, null, null, employeeExpression);

        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();
        final ProcessDefinition definition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, matti);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());
        waitForUserTaskAndExecuteIt(instance, "step1", matti);

        disableAndDeleteProcess(definition.getId());
    }

    private String createNewEmployeeScriptContent(final String firstName, final String lastName, final AddressRef... addresses) {
        final StringBuilder sb = new StringBuilder();
        sb.append("import ");
        sb.append(EMPLOYEE_QUALIFIED_NAME);
        sb.append("\n");
        sb.append("import ");
        sb.append(ADDRESS_QUALIFIED_NAME);
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
        sb.append(ADDRESS_QUALIFIED_NAME);
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
        final Expression getEmployeeExpression = new ExpressionBuilder().createBusinessDataExpression("myEmployee", EMPLOYEE_QUALIFIED_NAME);

        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression(
                "createNewEmployee",
                new StringBuilder().append("import ").append(EMPLOYEE_QUALIFIED_NAME)
                        .append("; Employee e = new Employee(); e.firstName = 'John'; e.lastName = 'Doe'; e.addToPhoneNumbers('78945612'); return e;")
                        .toString(),
                EMPLOYEE_QUALIFIED_NAME);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("BizDataAndConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIFIED_NAME, employeeExpression);
        processDefinitionBuilder
                .addUserTask(taskName, ACTOR_NAME)
                .addConnector("updateBusinessData", "org.bonitasoft.connector.BusinessDataUpdateConnector", "1.0", ConnectorEvent.ON_ENTER)
                .addInput("bizData", getEmployeeExpression)
                .addOutput(
                        new OperationBuilder().createBusinessDataSetAttributeOperation("myEmployee", "setLastName", String.class.getName(),
                                new ExpressionBuilder().createGroovyScriptExpression("retrieve modified lastname from connector", "output1.getLastName()",
                                        String.class.getName(), new ExpressionBuilder().createBusinessDataExpression("output1", EMPLOYEE_QUALIFIED_NAME))));

        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                processDefinitionBuilder.done());
        BarResource barResource = getBarResource("/org/bonitasoft/engine/business/data/BusinessDataUpdateConnector.impl", "BusinessDataUpdateConnector.impl",
                BDRepositoryIT.class);
        businessArchiveBuilder.addConnectorImplementation(barResource);

        barResource = BuildTestUtil.generateJarAndBuildBarResource(BusinessDataUpdateConnector.class, "BusinessDataUpdateConnector.jar");
        businessArchiveBuilder.addClasspathResource(barResource);

        return deployAndEnableProcessWithActor(businessArchiveBuilder.done(), ACTOR_NAME, matti);
    }

    private String getEmployeeToString(final String businessDataName, final long processInstanceId) throws InvalidExpressionException {
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>(5);
        final String expressionEmployee = "retrieve_Employee";
        expressions.put(
                new ExpressionBuilder().createGroovyScriptExpression(expressionEmployee, "\"Employee [firstName=\" + " + businessDataName
                        + ".firstName + \", lastName=\" + " + businessDataName + ".lastName + \"]\";", String.class.getName(),
                        new ExpressionBuilder().createBusinessDataExpression(businessDataName, EMPLOYEE_QUALIFIED_NAME)), null);
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
        final Expression countryQueryNameParameter = new ExpressionBuilder().createExpression("name", "France", String.class.getName(), ExpressionType.TYPE_CONSTANT);
        final Expression countryQueryExpression = new ExpressionBuilder().createQueryBusinessDataExpression("country", "Country.findByName",
                COUNTRY_QUALIFIED_NAME, countryQueryNameParameter);
        final Expression createNewAddressExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewAddress", "import " + ADDRESS_QUALIFIED_NAME + "; Address a = new Address(street:'32, rue Gustave Eiffel', city:'Grenoble'); a;",
                ADDRESS_QUALIFIED_NAME);
        final Expression createNewCountryExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewCountry", "import " + COUNTRY_QUALIFIED_NAME + "; Country c = new Country(name:'France'); c;",
                COUNTRY_QUALIFIED_NAME);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(
                "shouldBeAbleToUpdateBusinessDataUsingJavaSetterOperation", PROCESS_VERSION);
        final String businessDataName = "newBornBaby";
        final String businessDataName2 = "data2";
        processDefinitionBuilder.addBusinessData(businessDataName, EMPLOYEE_QUALIFIED_NAME, null);
        processDefinitionBuilder.addBusinessData(businessDataName2, EMPLOYEE_QUALIFIED_NAME, null);
        processDefinitionBuilder.addBusinessData("address", ADDRESS_QUALIFIED_NAME, createNewAddressExpression);
        processDefinitionBuilder.addBusinessData("country", COUNTRY_QUALIFIED_NAME, createNewCountryExpression);
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
                                new ExpressionBuilder().createConstantStringExpression("Plip")))
                .addOperation(
                        new OperationBuilder().createBusinessDataSetAttributeOperation("address", "setCountry", COUNTRY_QUALIFIED_NAME,
                                countryQueryExpression));
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        waitForUserTask(processInstance, "step2");

        disableAndDeleteProcess(definition.getId());
    }

    @Test
    public void shouldBeAbleToDeleteABusinessDataUsingOperation() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", new StringBuilder().append("import ")
                .append(EMPLOYEE_QUALIFIED_NAME).append("; Employee e = new Employee(); e.firstName = 'John'; e.lastName = 'Doe'; return e;").toString(),
                EMPLOYEE_QUALIFIED_NAME);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(
                "shouldBeAbleToUpdateBusinessDataUsingJavaSetterOperation", "6.3-beta");
        final String businessDataName = "employee";
        processDefinitionBuilder.addBusinessData(businessDataName, EMPLOYEE_QUALIFIED_NAME, employeeExpression);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addOperation(new OperationBuilder().deleteBusinessDataOperation(businessDataName));
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());

        final long step1Id = waitForUserTask(processInstance, "step1");
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>(2);
        expressions.put(new ExpressionBuilder().createQueryBusinessDataExpression("countEmployee", "Employee.countEmployee", Long.class.getName()),
                Collections.<String, Serializable> emptyMap());

        final long processInstanceId = processInstance.getId();
        Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
        assertThat(result.get("countEmployee")).isEqualTo(1L);

        assignAndExecuteStep(step1Id, matti);
        waitForUserTask(processInstance, "step2");
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
            return new ExpressionBuilder().createBusinessDataExpression(getVarName(), ADDRESS_QUALIFIED_NAME);
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
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression(
                "createNewEmployees",
                new StringBuilder().append("import ").append(EMPLOYEE_QUALIFIED_NAME)
                        .append("; Employee john = new Employee(); john.firstName = 'John'; john.lastName = 'Doe';")
                        .append(" Employee jane = new Employee(); jane.firstName = 'Jane'; jane.lastName = 'Doe'; return [jane, john];").toString(),
                List.class.getName());

        final Expression jackExpression = new ExpressionBuilder().createGroovyScriptExpression("createJack", "import " + EMPLOYEE_QUALIFIED_NAME
                + "; Employee jack = new Employee(); jack.firstName = 'Jack'; jack.lastName = 'Doe'; return jack;", EMPLOYEE_QUALIFIED_NAME);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addBusinessData("myEmployees", EMPLOYEE_QUALIFIED_NAME, employeeExpression).setMultiple(true);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME)
                .addOperation(new OperationBuilder().createBusinessDataSetAttributeOperation("myEmployees", "add", Object.class.getName(), jackExpression));
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());

        final long step1Id = waitForUserTask(instance, "step1");
        String employeeToString = getEmployeesToString("myEmployees", instance.getId());
        assertThat(employeeToString).isEqualTo("Employee [firstName=[Jane, John], lastName=[Doe, Doe]]");

        assignAndExecuteStep(step1Id, matti);
        waitForUserTask(instance, "step2");
        employeeToString = getEmployeesToString("myEmployees", instance.getId());
        assertThat(employeeToString).isEqualTo("Employee [firstName=[Jane, John, Jack], lastName=[Doe, Doe, Doe]]");

        disableAndDeleteProcess(definition.getId());
    }

    private String getEmployeesToString(final String businessDataName, final long processInstanceId) throws InvalidExpressionException {
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<>(5);
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
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression(
                "createNewEmployees",
                new StringBuilder().append("import ").append(EMPLOYEE_QUALIFIED_NAME)
                        .append("; Employee john = new Employee(); john.firstName = 'John'; john.lastName = 'Doe';")
                        .append(" Employee jane = new Employee(); jane.firstName = 'Jane'; jane.lastName = 'Doe'; [jane, john]").toString(),
                List.class.getName());

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MBIMI", "1.2-beta");
        builder.addBusinessData("myEmployees", EMPLOYEE_QUALIFIED_NAME, employeeExpression).setMultiple(true);
        builder.addActor(ACTOR_NAME);
        final UserTaskDefinitionBuilder userTaskBuilder = builder.addUserTask("step1", ACTOR_NAME);
        userTaskBuilder.addBusinessData("employee", EMPLOYEE_QUALIFIED_NAME);
        userTaskBuilder.addMultiInstance(false, "myEmployees").addDataInputItemRef("employee");
        userTaskBuilder.addOperation(new OperationBuilder().createBusinessDataSetAttributeOperation("employee", "setLastName", String.class.getName(),
                new ExpressionBuilder().createConstantStringExpression("Smith")));
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addTransition("step1", "step2");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);

        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(instance, "step1", matti);
        waitForUserTaskAndExecuteIt(instance, "step1", matti);

        waitForUserTask(instance, "step2");
        final String employeeToString = getEmployeesToString("myEmployees", instance.getId());
        assertThat(firstNames(employeeToString)).containsOnlyOnce("Jane", "John");
        assertThat(lastNames(employeeToString)).containsExactly("Smith", "Smith");

        disableAndDeleteProcess(processDefinition);
    }

    private String[] firstNames(final String employeeToString) {
        String firstNames = substringAfter(employeeToString, "firstName=[");
        firstNames = substringBefore(firstNames, "], lastName=");
        return StringUtils.split(firstNames, ", ");
    }

    private String[] lastNames(final String employeeToString) {
        String lastNames = substringAfter(employeeToString, "lastName=[");
        lastNames = substringBefore(lastNames, "]]");
        return StringUtils.split(lastNames, ", ");
    }

    @Test
    public void useMultipleBusinessDataInACallActivityWithSequentialMultiInstance() throws Exception {
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("UpdateEmployee", "1.2-beta");
        builder.addActor(ACTOR_NAME);
        builder.addBusinessData("employee", EMPLOYEE_QUALIFIED_NAME, null);
        final OperationBuilder operationBuilder = new OperationBuilder();
        builder.addUserTask("step1", ACTOR_NAME)
                .addOperation(operationBuilder.createBusinessDataSetAttributeOperation("employee", "setLastName", String.class.getName(),
                        new ExpressionBuilder().createConstantStringExpression("Smith")));
        final ProcessDefinition subProcessDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);

        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression(
                "createNewEmployees",
                new StringBuilder().append("import ").append(EMPLOYEE_QUALIFIED_NAME)
                        .append("; Employee john = new Employee(); john.firstName = 'John'; john.lastName = 'Doe';")
                        .append(" Employee jane = new Employee(); jane.firstName = 'Jane'; jane.lastName = 'Doe'; return [jane, john];").toString(),
                List.class.getName());

        builder = new ProcessDefinitionBuilder().createNewInstance("MBIMI", "1.2-beta");
        builder.addBusinessData("myEmployees", EMPLOYEE_QUALIFIED_NAME, employeeExpression).setMultiple(true);
        builder.addActor(ACTOR_NAME);
        final CallActivityBuilder callActivity = builder.addCallActivity("step1",
                new ExpressionBuilder().createConstantStringExpression(subProcessDefinition.getName()),
                new ExpressionBuilder().createConstantStringExpression(subProcessDefinition.getVersion()));
        callActivity.addBusinessData("miEmployee", EMPLOYEE_QUALIFIED_NAME);
        callActivity.addDataInputOperation(
                operationBuilder.createNewInstance()
                        .attachBusinessDataSetAttributeOperation("employee",
                                new ExpressionBuilder().createBusinessDataExpression("miEmployee", EMPLOYEE_QUALIFIED_NAME)))
                .addMultiInstance(true, "myEmployees").addDataInputItemRef("miEmployee");
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addTransition("step1", "step2");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);

        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(instance, "step1", matti);
        waitForUserTaskAndExecuteIt(instance, "step1", matti);
        waitForUserTask(instance, "step2");

        final String employeeToString = getEmployeesToString("myEmployees", instance.getId());
        assertThat(employeeToString).contains("Jane", "John", "Smith").doesNotContain("Doe");

        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(subProcessDefinition);
    }

    @Test
    public void useMultipleBusinessDataInACallActivityWithInDataMultiInstance() throws Exception {
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("UpdateEmployee", "1.2-beta");
        builder.addActor(ACTOR_NAME);
        builder.addBusinessData("employee", EMPLOYEE_QUALIFIED_NAME, null);
        builder.addUserTask("step1", ACTOR_NAME)
                .addOperation(new OperationBuilder().createBusinessDataSetAttributeOperation("employee", "setLastName", String.class.getName(),
                        new ExpressionBuilder().createConstantStringExpression("Smith")));
        final ProcessDefinition subProcessDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);

        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression(
                "createNewEmployees",
                new StringBuilder().append("import ").append(EMPLOYEE_QUALIFIED_NAME)
                        .append("; Employee john = new Employee(); john.firstName = 'John'; john.lastName = 'Doe';")
                        .append(" Employee jane = new Employee(); jane.firstName = 'Jane'; jane.lastName = 'Doe'; [jane, john];").toString(),
                List.class.getName());

        builder = new ProcessDefinitionBuilder().createNewInstance("MBIMI", "1.2-beta");
        builder.addBusinessData("myEmployees", EMPLOYEE_QUALIFIED_NAME, employeeExpression).setMultiple(true);
        builder.addBusinessData("myNewEmployees", EMPLOYEE_QUALIFIED_NAME, null).setMultiple(true);
        builder.addActor(ACTOR_NAME);
        final CallActivityBuilder callActivityBuilder = builder.addCallActivity("step1",
                new ExpressionBuilder().createConstantStringExpression(subProcessDefinition.getName()),
                new ExpressionBuilder().createConstantStringExpression(subProcessDefinition.getVersion()));
        callActivityBuilder.addBusinessData("miEmployee", EMPLOYEE_QUALIFIED_NAME);
        callActivityBuilder.addBusinessData("newEmployee", EMPLOYEE_QUALIFIED_NAME);
        callActivityBuilder.addDataInputOperation(
                new OperationBuilder().attachBusinessDataSetAttributeOperation("employee",
                        new ExpressionBuilder().createBusinessDataExpression("miEmployee", EMPLOYEE_QUALIFIED_NAME)));
        callActivityBuilder.addDataOutputOperation(
                new OperationBuilder().attachBusinessDataSetAttributeOperation("newEmployee",
                        new ExpressionBuilder().createBusinessDataExpression("employee", EMPLOYEE_QUALIFIED_NAME)));
        callActivityBuilder.addMultiInstance(false, "myEmployees").addDataInputItemRef("miEmployee")
                .addDataOutputItemRef("newEmployee").addLoopDataOutputRef("myNewEmployees");
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addTransition("step1", "step2");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);

        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(instance, "step1", matti);
        waitForUserTaskAndExecuteIt(instance, "step1", matti);
        waitForUserTask(instance, "step2");

        final String employeeToString = getEmployeesToString("myNewEmployees", instance.getId());
        assertThat(employeeToString).contains("Jane", "John", "Smith").doesNotContain("Doe");

        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(subProcessDefinition);
    }

    @Test
    public void useMultipleBusinessDataInACallActivityWithOutDataMultiInstance() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression(
                "createNewEmployee",
                new StringBuilder().append("import ").append(EMPLOYEE_QUALIFIED_NAME)
                        .append("; Employee john = new Employee(); john.firstName = 'John' + activityInstanceId; john.lastName = 'Doe'; john;").toString(),
                EMPLOYEE_QUALIFIED_NAME, new ExpressionBuilder().createEngineConstant(ExpressionConstants.ACTIVITY_INSTANCE_ID));
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("createEmployee", "1.2-beta");
        builder.addActor(ACTOR_NAME);
        builder.addBusinessData("employee", EMPLOYEE_QUALIFIED_NAME, null);
        builder.addUserTask("step1", ACTOR_NAME).addOperation(new OperationBuilder().attachBusinessDataSetAttributeOperation("employee",
                employeeExpression));
        final ProcessDefinition subProcessDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);

        builder = new ProcessDefinitionBuilder().createNewInstance("MBIMI", "1.2-beta");
        builder.addBusinessData("myEmployees", EMPLOYEE_QUALIFIED_NAME, null).setMultiple(true);
        builder.addActor(ACTOR_NAME);
        final CallActivityBuilder callActivityBuilder = builder.addCallActivity("step1",
                new ExpressionBuilder().createConstantStringExpression(subProcessDefinition.getName()),
                new ExpressionBuilder().createConstantStringExpression(subProcessDefinition.getVersion()));
        callActivityBuilder.addBusinessData("newEmployee", EMPLOYEE_QUALIFIED_NAME);
        callActivityBuilder.addDataOutputOperation(
                new OperationBuilder().attachBusinessDataSetAttributeOperation("newEmployee",
                        new ExpressionBuilder().createBusinessDataExpression("employee", EMPLOYEE_QUALIFIED_NAME)));
        callActivityBuilder.addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(2)).addDataOutputItemRef("newEmployee")
                .addLoopDataOutputRef("myEmployees");
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addTransition("step1", "step2");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);

        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(instance, "step1", matti);
        waitForUserTaskAndExecuteIt(instance, "step1", matti);
        waitForUserTask(instance, "step2");

        final String employeeToString = getEmployeesToString("myEmployees", instance.getId());
        assertThat(employeeToString).contains("John", "Doe");

        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(subProcessDefinition);
    }

    @Test
    public void should_return_the_list_of_entities_from_the_multiple_instance() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression(
                "createNewEmployees",
                new StringBuilder().append("import ").append(EMPLOYEE_QUALIFIED_NAME)
                        .append("; Employee john = new Employee(); john.firstName = 'John'; john.lastName = 'Doe';")
                        .append(" Employee jane = new Employee(); jane.firstName = 'Jane'; jane.lastName = 'Doe'; [jane, john]").toString(),
                List.class.getName());

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MBIMI", "1.2-beta");
        builder.addBusinessData("myEmployees", EMPLOYEE_QUALIFIED_NAME, employeeExpression).setMultiple(true);
        builder.addData("names", List.class.getName(), null);
        builder.addActor(ACTOR_NAME);
        final UserTaskDefinitionBuilder userTaskBuilder = builder.addUserTask("step1", ACTOR_NAME);
        userTaskBuilder.addBusinessData("employee", EMPLOYEE_QUALIFIED_NAME);
        userTaskBuilder.addShortTextData("name", null);
        userTaskBuilder.addMultiInstance(false, "myEmployees").addDataInputItemRef("employee").addDataOutputItemRef("name").addLoopDataOutputRef("names");
        userTaskBuilder.addOperation(new OperationBuilder().createSetDataOperation("name", new ExpressionBuilder().createConstantStringExpression("Doe")));
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addTransition("step1", "step2");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);

        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(instance, "step1", matti);
        waitForUserTaskAndExecuteIt(instance, "step1", matti);
        final long step2 = waitForUserTask(instance, "step2");

        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance("names", instance.getId());
        assertThat(dataInstance.getValue().toString()).isEqualTo("[Doe, Doe]");
        final Map<String, Serializable> employee = getProcessAPI().evaluateExpressionsOnProcessInstance(
                instance.getId(),
                Collections.singletonMap(new ExpressionBuilder().createBusinessDataReferenceExpression("myEmployees"),
                        Collections.<String, Serializable> emptyMap()));
        assertThat(employee).hasSize(1);
        assertThat(employee.get("myEmployees")).isInstanceOf(MultipleBusinessDataReference.class);
        final MultipleBusinessDataReference myEmployees = (MultipleBusinessDataReference) employee.get("myEmployees");
        assertThat(myEmployees.getName()).isEqualTo("myEmployees");
        assertThat(myEmployees.getType()).isEqualTo(EMPLOYEE_QUALIFIED_NAME);
        assertThat(myEmployees.getStorageIds()).hasSize(2);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getProcessBusinessDataReferencesShoulReturnTheListOfReferences() throws Exception {
        final String taskName = "step";
        final ProcessDefinition definition = buildProcessThatUpdateBizDataInsideConnector(taskName);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());
        waitForUserTask(instance, taskName);

        final List<BusinessDataReference> references = getBusinessDataAPI().getProcessBusinessDataReferences(instance.getId(), 0, 10);

        assertThat(references).hasSize(1);
        assertThat(((SimpleBusinessDataReference) references.get(0)).getStorageId()).isNotNull();

        disableAndDeleteProcess(definition);
    }

    @Test
    public void getBusinessDataCommand_should_return_json_entities() throws Exception {
        final ProcessDefinition processDefinition;
        final ProcessDefinitionBuilder processDefinitionBuilder;
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression(
                "createNewEmployee",
                new StringBuilder().append("import ").append(EMPLOYEE_QUALIFIED_NAME).append("; import ").append(ADDRESS_QUALIFIED_NAME)
                        .append("; Employee e = new Employee(); e.firstName = 'Alphonse';").append(" e.hireDate=new Date(1422742559000L); ")
                        .append(" e.lastName = 'Dupond'; e.addToPhoneNumbers('123456789'); e.setAddress(myAddress);e.addToAddresses(myAddress); return e;")
                        .toString(),
                EMPLOYEE_QUALIFIED_NAME,
                new ExpressionBuilder().createBusinessDataExpression("myAddress", ADDRESS_QUALIFIED_NAME));
        final Expression addressExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewAddress",
                new StringBuilder().append("import ").append(ADDRESS_QUALIFIED_NAME).append("; import ").append(COUNTRY_QUALIFIED_NAME).append("; ")
                        .append("Address a = new Address(); a.street='32, rue Gustave Eiffel'; a.city='Grenoble'; a.country = myCountry ; a;").toString(),
                ADDRESS_QUALIFIED_NAME);
        final Expression countryExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewCountry",
                new StringBuilder().append("import ").append(COUNTRY_QUALIFIED_NAME).append("; ").append("Country c = new Country(); c.name='France'; ")
                        .append(" c;").toString(),
                COUNTRY_QUALIFIED_NAME);

        processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(
                "rest", "1.0");
        final String bizDataName = "myEmployee";
        processDefinitionBuilder.addBusinessData("myCountry", COUNTRY_QUALIFIED_NAME, null);
        processDefinitionBuilder.addBusinessData("myAddress", ADDRESS_QUALIFIED_NAME, null);
        processDefinitionBuilder.addBusinessData(bizDataName, EMPLOYEE_QUALIFIED_NAME, null);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addAutomaticTask("step1")
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand("myCountry"), OperatorType.ASSIGNMENT, null, null, countryExpression)
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand("myAddress"), OperatorType.ASSIGNMENT, null, null, addressExpression)
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand(bizDataName), OperatorType.ASSIGNMENT, null, null, employeeExpression);
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");

        processDefinition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "step2");

        final SimpleBusinessDataReference businessDataReference = (SimpleBusinessDataReference) getBusinessDataAPI().getProcessBusinessDataReference(
                bizDataName,
                processInstance.getId());

        verifyCommandGetBusinessDataById(businessDataReference);
        verifyCommandGetQuery_findByFirstNameAndLastNameNewOrder();
        verifyCommandGetQuery_getEmployeeByPhoneNumber();
        verifyCommandGetQuery_findByFirstNameFetchAddresses();
        verifyCommandGetQuery_countEmployee();
        verifyCommandGetQuery_findByHireDate();

        disableAndDeleteProcess(processDefinition.getId());
    }

    private void verifyCommandGetBusinessDataById(final SimpleBusinessDataReference businessDataReference) throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("businessDataId", businessDataReference.getStorageId());
        parameters.put("entityClassName", EMPLOYEE_QUALIFIED_NAME);
        parameters.put("businessDataChildName", "address");
        parameters.put("businessDataURIPattern", "/businessdata/{className}/{id}/{field}");

        // when
        final String lazyAddressResultWithChildName = (String) getCommandAPI().execute("getBusinessDataById", parameters);

        // then
        assertThatJson(lazyAddressResultWithChildName).as("should get address with lazy link to country")
                .hasSameStructureAs(getJsonContent("getBusinessDataByIdAddress.json"));

        // when
        parameters.remove("businessDataChildName");
        final String employeeResultWithAddress = (String) getCommandAPI().execute("getBusinessDataById", parameters);

        // then
        assertThatJson(employeeResultWithAddress).as("should get employee with lazy link to country in addresses")
                .hasSameStructureAs(getJsonContent("getBusinessDataByIdEmployee.json"));

    }

    private void verifyCommandGetQuery_findByFirstNameAndLastNameNewOrder() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        final Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();

        queryParameters.put("firstName", "Alphonse");
        queryParameters.put("lastName", "Dupond");

        parameters.put("queryName", FIND_BY_FIRST_NAME_AND_LAST_NAME_NEW_ORDER);
        parameters.put(ENTITY_CLASS_NAME, EMPLOYEE_QUALIFIED_NAME);
        parameters.put("startIndex", 0);
        parameters.put("maxResults", 10);
        parameters.put("businessDataURIPattern", "/businessdata/{className}/{id}/{field}");
        parameters.put("queryParameters", (Serializable) queryParameters);

        // when
        final String jsonResult = (String) getCommandAPI().execute("getBusinessDataByQueryCommand", parameters);

        // then
        assertThatJson(jsonResult).as("should get employee").hasSameStructureAs(getJsonContent("findByFirstNameAndLastNameNewOrder.json"));

    }

    private void verifyCommandGetQuery_getEmployeeByPhoneNumber() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        final Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();

        queryParameters.put("phoneNumber", "123456789");

        parameters.put("queryName", GET_EMPLOYEE_BY_PHONE_NUMBER_QUERY_NAME);
        parameters.put(ENTITY_CLASS_NAME, EMPLOYEE_QUALIFIED_NAME);
        parameters.put("startIndex", 0);
        parameters.put("maxResults", 10);
        parameters.put("businessDataURIPattern", BUSINESS_DATA_CLASS_NAME_ID_FIELD);
        parameters.put("queryParameters", (Serializable) queryParameters);

        // when
        final String jsonResult = (String) getCommandAPI().execute("getBusinessDataByQueryCommand", parameters);

        // then
        assertThatJson(jsonResult).as("should get employee").hasSameStructureAs(getJsonContent("getEmployeeByPhoneNumber.json"));

    }

    private void verifyCommandGetQuery_findByFirstNameFetchAddresses() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        final Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();

        queryParameters.put("firstName", "Alphonse");

        parameters.put("queryName", FIND_BY_FIRST_NAME_FETCH_ADDRESSES);
        parameters.put(ENTITY_CLASS_NAME, EMPLOYEE_QUALIFIED_NAME);
        parameters.put("startIndex", 0);
        parameters.put("maxResults", 10);
        parameters.put("businessDataURIPattern", BUSINESS_DATA_CLASS_NAME_ID_FIELD);
        parameters.put("queryParameters", (Serializable) queryParameters);

        // when
        final String jsonResult = (String) getCommandAPI().execute("getBusinessDataByQueryCommand", parameters);

        // then
        assertThatJson(jsonResult).as("should get employee").hasSameStructureAs(getJsonContent("findByFirstNameFetchAddresses.json"));

    }

    private void verifyCommandGetQuery_countEmployee() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();

        parameters.put("queryName", COUNT_EMPLOYEE);
        parameters.put(ENTITY_CLASS_NAME, EMPLOYEE_QUALIFIED_NAME);
        parameters.put("startIndex", 0);
        parameters.put("maxResults", 10);
        parameters.put("businessDataURIPattern", BUSINESS_DATA_CLASS_NAME_ID_FIELD);

        // when
        final String jsonResult = (String) getCommandAPI().execute("getBusinessDataByQueryCommand", parameters);

        // then
        assertThatJson(jsonResult).as("should get employee count ").isEqualTo(getJsonContent("countEmployee.json"));

    }

    private void verifyCommandGetQuery_findByHireDate() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        final Map<String, Serializable> queryParameters = new HashMap<String, Serializable>();
        queryParameters.put("date1", "1930-01-15");
        queryParameters.put("date2", "2050-12-31");

        parameters.put("queryName", FIND_BY_HIRE_DATE_RANGE);
        parameters.put(ENTITY_CLASS_NAME, EMPLOYEE_QUALIFIED_NAME);
        parameters.put("startIndex", 0);
        parameters.put("maxResults", 10);
        parameters.put("businessDataURIPattern", BUSINESS_DATA_CLASS_NAME_ID_FIELD);
        parameters.put("queryParameters", (Serializable) queryParameters);

        // when
        final String jsonResult = (String) getCommandAPI().execute("getBusinessDataByQueryCommand", parameters);

        // then
        assertThatJson(jsonResult).as("should get employee").hasSameStructureAs(getJsonContent("findByHireDate.json"));

    }

    @Override
    public BarResource getResource(final String path, final String name) throws IOException {
        return super.getResource(path, name);
    }

    private String getJsonContent(final String jsonFileName) throws IOException {
        final String json;
        json = new String(IOUtils.toByteArray(this.getClass().getResourceAsStream(jsonFileName)));
        return json;
    }

    @Test
    public void deployABDRAndCreateInOperationAMultipleBusinessData() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression(
                "createNewEmployees",
                new StringBuilder().append("import ").append(EMPLOYEE_QUALIFIED_NAME)
                        .append("; Employee john = new Employee(); john.firstName = 'John'; john.lastName = 'Doe';")
                        .append(" Employee jane = new Employee(); jane.firstName = 'Jane'; jane.lastName = 'Doe'; return [jane, john];").toString(),
                List.class.getName());

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addBusinessData("myEmployees", EMPLOYEE_QUALIFIED_NAME, null).setMultiple(true);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addOperation(new OperationBuilder().
                createBusinessDataSetAttributeOperation("myEmployees", "addAll", "java.util.Collection", employeeExpression));
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());

        final long step1Id = waitForUserTask(instance, "step1");
        String employeeToString = getEmployeesToString("myEmployees", instance.getId());
        assertThat(firstNames(employeeToString)).isEmpty();
        assertThat(lastNames(employeeToString)).isEmpty();

        assignAndExecuteStep(step1Id, matti);
        waitForUserTask(instance, "step2");
        employeeToString = getEmployeesToString("myEmployees", instance.getId());
        assertThat(firstNames(employeeToString)).containsOnlyOnce("Jane", "John");
        assertThat(lastNames(employeeToString)).containsExactly("Doe", "Doe");

        disableAndDeleteProcess(definition.getId());
    }

    @Test
    public void should_get_the_lazy_list_in_a_multiple_business_data() throws Exception {
        final Expression initProducts = new ExpressionBuilder().createGroovyScriptExpression("initProducts",
                new StringBuilder().append("import ").append(PRODUCT_QUALIFIED_NAME).append(";").append(" Product p1 = new Product(); p1.name = 'Rock'; ")
                        .append(" Product p2 = new Product(); p2.name = 'Paper'; ").append(" return [p1, p2];").toString(), List.class.getName());

        final Expression productDependency = new ExpressionBuilder().createBusinessDataExpression("products", List.class.getName());

        final Expression initCatalogs = new ExpressionBuilder().createGroovyScriptExpression(
                "initCatalogs",
                new StringBuilder().append("import ").append(PRODUCT_CATALOG_QUALIFIED_NAME).append(";")
                        .append(" ProductCatalog pc = new ProductCatalog(); pc.name = 'MyFirstCatalog'; pc.setProducts(products);").append(" return [pc];")
                        .toString(), List.class.getName(), productDependency);

        final Expression catalogDependency = new ExpressionBuilder().createBusinessDataExpression("productCatalogs", List.class.getName());

        final Expression nbOfProducts = new ExpressionBuilder().createGroovyScriptExpression("nbOfProducts",
                new StringBuilder().append("import ").append(PRODUCT_CATALOG_QUALIFIED_NAME).append(";").append(" productCatalogs.get(0).getProducts().size()")
                        .toString(), Integer.class.getName(), catalogDependency);

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("def", "6.3-beta");
        builder.addActor(ACTOR_NAME);
        builder.addBusinessData("products", PRODUCT_QUALIFIED_NAME, initProducts).setMultiple(true);
        builder.addBusinessData("productCatalogs", PRODUCT_CATALOG_QUALIFIED_NAME, null).setMultiple(true);
        builder.addIntegerData("count", null);
        builder.addAutomaticTask("initCatalogs")
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand("productCatalogs"), OperatorType.ASSIGNMENT, null, null, initCatalogs);
        builder.addUserTask("next", ACTOR_NAME)
                .addOperation(new OperationBuilder().createSetDataOperation("count", nbOfProducts));
        builder.addTransition("initCatalogs", "next");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, matti);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        waitForUserTaskAndExecuteIt(processInstance, "next", matti);

        disableAndDeleteProcess(definition.getId());
    }

    private String getClientBdmJarClassPath(final String bonitaHomePath) {
        String clientBdmJarPath;
        clientBdmJarPath = new StringBuilder().append(bonitaHomePath).append(File.separator).append("engine-server").append(File.separator).append("work").append(File.separator).append("tenants")
                .append(File.separator).append(tenantId).append(File.separator).append("data-management-client").toString();
        return clientBdmJarPath;
   }

    @Test
    public void should_associate_the_right_address() throws Exception {
        final Expression addressExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewAddress",
                "import " + ADDRESS_QUALIFIED_NAME + "; new Address(street:'32, rue Gustave Eiffel', city:'Grenoble')",
                ADDRESS_QUALIFIED_NAME);
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", "import " + EMPLOYEE_QUALIFIED_NAME
                + "; new Employee(firstName:'John', lastName:'Doe', address:myAddress)", EMPLOYEE_QUALIFIED_NAME,
                new ExpressionBuilder().createBusinessDataExpression("myAddress", ADDRESS_QUALIFIED_NAME));

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(
                "theProcess", "6.3.1");
        final String bizDataName = "myEmployee";
        processDefinitionBuilder.addBusinessData(bizDataName, EMPLOYEE_QUALIFIED_NAME, null);
        processDefinitionBuilder.addBusinessData("myAddress", ADDRESS_QUALIFIED_NAME, addressExpression);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addAutomaticTask("step1")
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand(bizDataName), OperatorType.ASSIGNMENT, null, null, employeeExpression);
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        waitForUserTask(processInstance, "step2");

        final Long numberOfAddresses = getNumberOfAddresses(processInstance.getId());
        assertThat(numberOfAddresses).isEqualTo(1L);
        final String address = getAddressAsAString("myAddress", processInstance.getId());
        assertThat(address).isEqualTo("Address [street=32, rue Gustave Eiffel, city=Grenoble]");

        disableAndDeleteProcess(definition.getId());
    }

    @Test
    public void should_associate_the_right_addresses() throws Exception {
        final Expression addressExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewAddress",
                "import " + ADDRESS_QUALIFIED_NAME + "; new Address(street:'32, rue Gustave Eiffel', city:'Grenoble')",
                ADDRESS_QUALIFIED_NAME);
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", "import " + EMPLOYEE_QUALIFIED_NAME
                + "; new Employee(firstName:'John', lastName:'Doe', addresses:[myAddress])", EMPLOYEE_QUALIFIED_NAME,
                new ExpressionBuilder().createBusinessDataExpression("myAddress", ADDRESS_QUALIFIED_NAME));

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(
                "theProcess", "6.3.1");
        final String bizDataName = "myEmployee";
        processDefinitionBuilder.addBusinessData(bizDataName, EMPLOYEE_QUALIFIED_NAME, null);
        processDefinitionBuilder.addBusinessData("myAddress", ADDRESS_QUALIFIED_NAME, addressExpression);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addAutomaticTask("step1")
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand(bizDataName), OperatorType.ASSIGNMENT, null, null, employeeExpression);
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME)
                .addOperation(
                        new OperationBuilder().createBusinessDataSetAttributeOperation(bizDataName, "addToAddresses", ADDRESS_QUALIFIED_NAME,
                                new ExpressionBuilder().createBusinessDataExpression("myAddress", ADDRESS_QUALIFIED_NAME)))
                .addOperation(
                        new OperationBuilder().createBusinessDataSetAttributeOperation(bizDataName, "setLastName", String.class.getName(),
                                new ExpressionBuilder().createConstantStringExpression("Smith")));
        processDefinitionBuilder.addUserTask("step3", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");
        processDefinitionBuilder.addTransition("step2", "step3");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        final long userTaskId = waitForUserTask(processInstance, "step2");

        Long numberOfAddresses = getNumberOfAddresses(processInstance.getId());
        assertThat(numberOfAddresses).isEqualTo(1L);
        String address = getAddressAsAString("myAddress", processInstance.getId());
        assertThat(address).isEqualTo("Address [street=32, rue Gustave Eiffel, city=Grenoble]");

        assignAndExecuteStep(userTaskId, matti);
        waitForUserTask(processInstance, "step3");

        numberOfAddresses = getNumberOfAddresses(processInstance.getId());
        assertThat(numberOfAddresses).isEqualTo(1L);
        address = getAddressAsAString("myAddress", processInstance.getId());
        assertThat(address).isEqualTo("Address [street=32, rue Gustave Eiffel, city=Grenoble]");
        final String employee = getEmployeeAsAString(bizDataName, processInstance.getId());
        assertThat(employee).isEqualTo("Employee [firstName=John, lastName=Smith, address=null, addresses.count=2 ]");

        disableAndDeleteProcess(definition.getId());
    }

    public Long getNumberOfAddresses(final long processInstanceId) throws Exception {
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>(2);
        expressions.put(new ExpressionBuilder().createQueryBusinessDataExpression("countAddresses", "Address.countAddress", Long.class.getName()),
                Collections.<String, Serializable> emptyMap());

        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
        return (Long) result.get("countAddresses");
    }

    public String getAddressAsAString(final String addressName, final long processInstanceId) throws Exception {
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>(2);
        expressions.put(
                new ExpressionBuilder().createGroovyScriptExpression("getAddress", "\"Address [street=\" + " + addressName
                        + ".street + \", city=\" + " + addressName + ".city + \"]\";", String.class.getName(),
                        new ExpressionBuilder().createBusinessDataExpression(addressName, ADDRESS_QUALIFIED_NAME)), null);
        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
        return (String) result.get("getAddress");
    }

    private String getEmployeeAsAString(final String businessDataName, final long processInstanceId) throws InvalidExpressionException {
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>(5);
        final String expressionEmployee = "retrieve_Employee";
        expressions.put(
                new ExpressionBuilder().createGroovyScriptExpression(expressionEmployee,
                        "\"Employee [firstName=\" + " + businessDataName + ".firstName + \", lastName=\" + " + businessDataName
                                + ".lastName + \", address=\" + " + businessDataName + ".address + \", addresses.count=\" + "
                                + businessDataName + ".addresses.size() + \" ]\";", String.class.getName(),
                        new ExpressionBuilder().createBusinessDataExpression(businessDataName, EMPLOYEE_QUALIFIED_NAME)), null);
        try {
            final Map<String, Serializable> evaluatedExpressions = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
            return (String) evaluatedExpressions.get(expressionEmployee);
        } catch (final ExpressionEvaluationException eee) {
            System.err.println(eee.getMessage());
            return null;
        }
    }

}
