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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.bdm.BusinessObjectModelConverter;
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
import com.bonitasoft.engine.businessdata.SimpleBusinessDataReference;

public class CopyOfBDRepositoryIT extends CommonAPISPTest {

    private static final String ADDRESS_QUALIF_CLASSNAME = "org.bonita.pojo.Address";

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
        addressBO.setQualifiedName(ADDRESS_QUALIF_CLASSNAME);
        addressBO.addField(street);
        addressBO.addField(city);
        addressBO.addField(country);

        final RelationField addresses = new RelationField();
        addresses.setType(Type.AGGREGATION);
        addresses.setFetchType(FetchType.LAZY);
        addresses.setName("addresses");
        addresses.setCollection(Boolean.TRUE);
        addresses.setNullable(Boolean.TRUE);
        addresses.setReference(addressBO);

        final RelationField address = new RelationField();
        address.setType(Type.AGGREGATION);
        address.setFetchType(FetchType.LAZY);
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

        final BusinessObject employee = new BusinessObject();
        employee.setQualifiedName(EMPLOYEE_QUALIF_CLASSNAME);
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

        final BusinessObject productBO = new BusinessObject();
        productBO.setQualifiedName("org.bonita.pojo.Product");
        productBO.addField(name);

        final RelationField products = new RelationField();
        products.setType(Type.AGGREGATION);
        products.setFetchType(FetchType.LAZY);
        products.setName("products");
        products.setCollection(Boolean.TRUE);
        products.setNullable(Boolean.TRUE);
        products.setReference(productBO);

        final BusinessObject catalogBO = new BusinessObject();
        catalogBO.setQualifiedName("org.bonita.pojo.ProductCatalog");
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
                processDefinitionBuilder.addBusinessData(ref.getVarName(), ADDRESS_QUALIF_CLASSNAME, null);
                final Expression addressExpression = new ExpressionBuilder().createGroovyScriptExpression("createAddress" + ref.getVarName(),
                        createNewAddressScriptContent(ref.getStreet(), ref.getCity()),
                        ADDRESS_QUALIF_CLASSNAME);
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
        sb.append(ADDRESS_QUALIF_CLASSNAME);
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
        sb.append(ADDRESS_QUALIF_CLASSNAME);
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
        final InputStream stream = CopyOfBDRepositoryIT.class.getResourceAsStream(path);
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
            return new ExpressionBuilder().createBusinessDataExpression(getVarName(), ADDRESS_QUALIF_CLASSNAME);
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
    public void get_lazy_object_outside_a_transaction_should_throw_exception() throws Exception {
        final String bizDataGrenobleAdressName = "bizGrenobleAddress";
        final String bizDataSFAddressName = "bizSfAddress";
        final String bizDataEmployeeName = "bizEmployee";

        final String automaticTaskToInitBizData = "step1";
        final String automaticTaskToCallJavaMethodOperation = "step2";
        final String humanTask = "step3";

        final String citySF = "S.F.";

        //given
        final Expression grenobleAddressExpression = createGrovyExpressionThatCreateAddressWithCityName("Grenoble");
        final Expression sfAddressExpression = createGrovyExpressionThatCreateAddressWithCityName(citySF);
        final Expression employeeExpression = createGrovyExpressionThatCreateEmployeWithOneAddress(bizDataGrenobleAdressName);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance(
                "lazy", "1.0");

        processDefinitionBuilder.addBusinessData(bizDataEmployeeName, EMPLOYEE_QUALIF_CLASSNAME, null);
        processDefinitionBuilder.addBusinessData(bizDataGrenobleAdressName, ADDRESS_QUALIF_CLASSNAME, grenobleAddressExpression);
        processDefinitionBuilder.addBusinessData(bizDataSFAddressName, ADDRESS_QUALIF_CLASSNAME, sfAddressExpression);

        processDefinitionBuilder.addActor(ACTOR_NAME);

        //init employee and grenoble bizData
        processDefinitionBuilder.addAutomaticTask(automaticTaskToInitBizData)
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand(bizDataEmployeeName), OperatorType.ASSIGNMENT, null, null,
                        employeeExpression)
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand(bizDataSFAddressName), OperatorType.ASSIGNMENT, null, null,
                        sfAddressExpression);

        //                .addOperation(new OperationBuilder().createJavaMethodOperation("myEmployee", "addToAdresses",
        //                        ADDRESS_QUALIF_CLASSNAME,
        //                        new ExpressionBuilder().createInputExpression("address2", ADDRESS_QUALIF_CLASSNAME)));

        //call java operation
        processDefinitionBuilder.addAutomaticTask(automaticTaskToCallJavaMethodOperation)/*
                                                                                          * .addOperation(
                                                                                          * new OperationBuilder().createJavaMethodOperation("myEmployee",
                                                                                          * "addToAdresses",
                                                                                          * ADDRESS_QUALIF_CLASSNAME,
                                                                                          * new ExpressionBuilder().createInputExpression("address2",
                                                                                          * ADDRESS_QUALIF_CLASSNAME)))
                                                                                          */;

        //waiting task
        processDefinitionBuilder.addUserTask(humanTask, ACTOR_NAME);

        //transitions
        processDefinitionBuilder.addTransition(automaticTaskToInitBizData, automaticTaskToCallJavaMethodOperation);
        processDefinitionBuilder.addTransition(automaticTaskToCallJavaMethodOperation, humanTask);

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final long processInstanceId = getProcessAPI().startProcess(definition.getId()).getId();
        final HumanTaskInstance humanTaskInstance = waitForUserTask(humanTask, processInstanceId);

        final Map<String, Serializable> evaluateExpressionsAtProcessInstanciation = getProcessAPI().evaluateExpressionsOnActivityInstance(
                humanTaskInstance.getId(), createExpressionsToEvaluate(createBusinessDataExpressionToFindTheEmployee(),
                        createBusinessDataExpressionToFindAddresses(citySF)));

        final Serializable bizDataEmployeeFound = evaluateExpressionsAtProcessInstanciation.get(createBusinessDataExpressionToFindTheEmployee().getName());
        final Serializable bizDataSfAddressesFound = (Serializable) ((List) evaluateExpressionsAtProcessInstanciation
                .get(createBusinessDataExpressionToFindAddresses(citySF)
                        .getName())).get(0);

        final Expression countExpression = createGrovyExpressionToCountAdresses();

        final String employeeInputName = "myEmployee2";
        final String addressinputName = "address2";
        final Expression addAdressExpression = createGrovyExpressionToAddAddressToEmployee(employeeInputName, addressinputName);

        final Map<Expression, Map<String, Serializable>> addAdressExpressionMap = new HashMap<Expression, Map<String, Serializable>>();
        addAdressExpressionMap.put(addAdressExpression, Collections.singletonMap("address", bizDataSfAddressesFound));

        final Map<String, Serializable> mapForAddAdress = new HashMap<String, Serializable>();
        mapForAddAdress.put(employeeInputName, bizDataEmployeeFound);
        mapForAddAdress.put(addressinputName, bizDataSfAddressesFound);

        final Map<Expression, Map<String, Serializable>> expressions2 = new HashMap<Expression, Map<String, Serializable>>();
        expressions2.put(addAdressExpression, mapForAddAdress);

        final Map<String, Serializable> evaluateExpressionsOnActivityInstance = getProcessAPI().evaluateExpressionsOnActivityInstance(
                humanTaskInstance.getId(), expressions2);

        final Serializable employee = evaluateExpressionsOnActivityInstance.get(addAdressExpression.getName());

        //            then
        final Map<Expression, Map<String, Serializable>> expression3 = new HashMap<Expression, Map<String, Serializable>>();
        expression3.put(countExpression, Collections.singletonMap("myEmployee", employee));

        final Map<String, Serializable> evaluateExpressionsOnActivityInstance3 = getProcessAPI().evaluateExpressionsOnActivityInstance(
                humanTaskInstance.getId(), expressions2);

        final Serializable serializable = evaluateExpressionsOnActivityInstance3.get("countExpression");
        assertThat(serializable).as("should get 2 address count").isEqualTo(2);

    }

    private Expression createGrovyExpressionToAddAddressToEmployee(final String employeeInputName, final String addressinputName)
            throws InvalidExpressionException {
        final Expression addAdressExpression;
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(employeeInputName
                + ".addToAddresses("
                + addressinputName
                + "); ");
        stringBuilder.append("return "
                + employeeInputName
                + ";");
        addAdressExpression = new ExpressionBuilder().createGroovyScriptExpression("addAdressExpression",
                stringBuilder.toString()
                , EMPLOYEE_QUALIF_CLASSNAME, new ExpressionBuilder().createInputExpression(employeeInputName, EMPLOYEE_QUALIF_CLASSNAME),
                new ExpressionBuilder().createInputExpression(addressinputName, ADDRESS_QUALIF_CLASSNAME));
        return addAdressExpression;
    }

    private Expression createGrovyExpressionToCountAdresses() throws InvalidExpressionException {
        final Expression countExpression;
        countExpression = new ExpressionBuilder().createGroovyScriptExpression("countExpression", "myEmployee.getAddresses().size()"
                , Integer.class.getName(),
                new ExpressionBuilder().createInputExpression("myEmployee", EMPLOYEE_QUALIF_CLASSNAME));
        return countExpression;
    }

    private Map<Expression, Map<String, Serializable>> createExpressionsToEvaluate(final Expression... expresssions) {
        final Map<Expression, Map<String, Serializable>> expressions;
        expressions = new HashMap<Expression, Map<String, Serializable>>();
        final Map<String, Serializable> mapContext = new HashMap<String, Serializable>();
        for (final Expression expression : expresssions) {
            expressions.put(expression, mapContext);
        }
        return expressions;
    }

    private Expression createBusinessDataExpressionToFindAddresses(final String city) throws InvalidExpressionException {
        return new ExpressionBuilder().createQueryBusinessDataExpression(
                "createQueryBusinessDataExpressionFindAddress",
                "Address.findByCity", List.class.getName(),
                new ExpressionBuilder().createConstantStringExpression("city", city),
                new ExpressionBuilder().createExpression("startIndex", "0", Integer.class.getName(), ExpressionType.TYPE_CONSTANT),
                new ExpressionBuilder().createExpression("maxResults", "10", Integer.class.getName(), ExpressionType.TYPE_CONSTANT));
    }

    private Expression createBusinessDataExpressionToFindTheEmployee() throws InvalidExpressionException {
        return new ExpressionBuilder().createQueryBusinessDataExpression("createQueryBusinessDataExpression",
                "Employee.findByFirstNameAndLastNameNewOrder", EMPLOYEE_QUALIF_CLASSNAME,
                new ExpressionBuilder().createConstantStringExpression("firstName", "Alphonse"),
                new ExpressionBuilder().createConstantStringExpression("lastName", "Dupond"));
    }

    private Expression createGrovyExpressionThatCreateEmployeWithOneAddress(final String businessDataAdressName) throws InvalidExpressionException {
        return new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; import org.bonita.pojo.Address; Employee e = new Employee(); e.firstName = 'Alphonse';"
                + " e.lastName = 'Dupond'; e.addToAddresses("
                + businessDataAdressName
                + "); return e;", EMPLOYEE_QUALIF_CLASSNAME,
                createBusinessDataExpressionWithName(businessDataAdressName));
    }

    private Expression createBusinessDataExpressionWithName(final String businessDataName) throws InvalidExpressionException {
        Expression createBusinessDataExpression;
        createBusinessDataExpression = new ExpressionBuilder().createBusinessDataExpression(businessDataName, ADDRESS_QUALIF_CLASSNAME);
        return createBusinessDataExpression;
    }

    private Expression createGrovyExpressionThatCreateAddressWithCityName(final String city) throws InvalidExpressionException {
        final Expression addressExpression;
        final StringBuilder builder = new StringBuilder();
        builder.append("import org.bonita.pojo.Address; ");
        builder.append("import org.bonita.pojo.Country; ");
        builder.append("Country contry = new Country(); ");
        builder.append("contry.name='France'; ");
        builder.append("Address address = new Address();");
        builder.append("address.street='32, rue Gustave Eiffel'; ");
        builder.append("address.city='");
        builder.append(city);
        builder.append("'; ");
        builder.append("address.country = contry; ");
        builder.append("address;");
        final String script = builder.toString();

        addressExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewAddress",
                script,
                ADDRESS_QUALIF_CLASSNAME);
        return addressExpression;
    }

}
