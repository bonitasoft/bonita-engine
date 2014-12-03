/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
import com.bonitasoft.engine.business.data.ClassloaderRefresher;

public class BDRepositoryLocalIT extends CommonAPISPTest {

    private static final String FIND_BY_FIRST_NAME_AND_LAST_NAME_NEW_ORDER = "findByFirstNameAndLastNameNewOrder";

    private static final String BIZ_GRENOBLE_ADDRESS = "bizGrenobleAddress";
    private static final String BIZ_SF_ADDRESS = "bizSfAddress";
    private static final String BIZ_ROME_ADDRESS = "bizRomeAddress";
    private static final String BIZ_EMPLOYEE = "bizEmployee";

    private static final String PROCESS_NAME = "lazy";
    private static final String VERSION = "1.0";

    private static final String TASK_AUTOMATIC_TASK_TO_INIT_BIZ_DATA = "automaticTaskToInitBizData";
    private static final String TASK_TO_CALL_JAVA_METHOD_OPERATION = "automaticTaskToCallJavaMethodOperation";
    private static final String TASK_HUMAN_TASK = "humanTask";

    private static final String CITY_SF = "S.F.";
    private static final String CITY_GRENOBLE = "Grenoble";
    private static final String CITY_ROME = "Rome";

    private static final String COUNTRY_ITALY = "Italy";
    private static final String COUNTRY_FRANCE = "France";
    private static final String COUNTRY_USA = "USA";

    private static final String ADDRESS_QUALIF_CLASSNAME = "org.bonita.pojo.Address";
    private static final String EMPLOYEE_QUALIF_CLASSNAME = "org.bonita.pojo.Employee";

    private User matti;

    private File clientFolder;

    private ClassLoader contextClassLoaderBeforeAddingBPMClientZip;

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

        //needed for remote testing
        addClientBDMZipToClassLoader();
    }

    @After
    public void tearDown() throws Exception {
        resumeClassloader();
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
        resumeClassloader();

        deleteUser(matti);
        logoutOnTenant();
    }

    private void resumeClassloader() {
        Thread.currentThread().setContextClassLoader(contextClassLoaderBeforeAddingBPMClientZip);

    }

    @Test
    public void should_get_lazy_object_outside_a_transaction() throws Exception {
        //given
        final Expression addressGrenobleExpression = createGrovyExpressionThatCreateAddressWithCityName(CITY_GRENOBLE, COUNTRY_FRANCE);
        final Expression addressSfExpression = createGrovyExpressionThatCreateAddressWithCityName(CITY_SF, COUNTRY_USA);
        final Expression addressRomeExpression = createGrovyExpressionThatCreateAddressWithCityName(CITY_ROME, COUNTRY_ITALY);

        final Expression employeeExpression = createGrovyExpressionThatCreateEmployeWithOneAddress(BIZ_GRENOBLE_ADDRESS);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance(
                PROCESS_NAME, VERSION);

        processDefinitionBuilder.addBusinessData(BIZ_EMPLOYEE, EMPLOYEE_QUALIF_CLASSNAME, null);
        processDefinitionBuilder.addBusinessData(BIZ_GRENOBLE_ADDRESS, ADDRESS_QUALIF_CLASSNAME, addressGrenobleExpression);
        processDefinitionBuilder.addBusinessData(BIZ_SF_ADDRESS, ADDRESS_QUALIF_CLASSNAME, addressSfExpression);
        processDefinitionBuilder.addBusinessData(BIZ_ROME_ADDRESS, ADDRESS_QUALIF_CLASSNAME, addressRomeExpression);

        processDefinitionBuilder.addActor(ACTOR_NAME);

        // create employee and addresses
        processDefinitionBuilder.addAutomaticTask(TASK_AUTOMATIC_TASK_TO_INIT_BIZ_DATA)
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand(BIZ_SF_ADDRESS), OperatorType.ASSIGNMENT, null, null,
                        addressSfExpression)
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand(BIZ_GRENOBLE_ADDRESS), OperatorType.ASSIGNMENT, null, null,
                        addressGrenobleExpression)
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand(BIZ_ROME_ADDRESS), OperatorType.ASSIGNMENT, null, null,
                        addressRomeExpression)
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand(BIZ_EMPLOYEE), OperatorType.ASSIGNMENT, null, null,
                        employeeExpression);

        //call java operation to add address to employee
        processDefinitionBuilder.addAutomaticTask(TASK_TO_CALL_JAVA_METHOD_OPERATION)
                .addOperation(
                        new OperationBuilder().createBusinessDataSetAttributeOperation(BIZ_EMPLOYEE, "addToAddresses", ADDRESS_QUALIF_CLASSNAME,
                                new ExpressionBuilder().createBusinessDataExpression(BIZ_ROME_ADDRESS, ADDRESS_QUALIF_CLASSNAME)));

        //waiting task
        processDefinitionBuilder.addUserTask(TASK_HUMAN_TASK, ACTOR_NAME);

        //transitions
        processDefinitionBuilder.addTransition(TASK_AUTOMATIC_TASK_TO_INIT_BIZ_DATA, TASK_TO_CALL_JAVA_METHOD_OPERATION);
        processDefinitionBuilder.addTransition(TASK_TO_CALL_JAVA_METHOD_OPERATION, TASK_HUMAN_TASK);

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final long processInstanceId = getProcessAPI().startProcess(definition.getId()).getId();
        final HumanTaskInstance humanTaskInstance = waitForUserTask(TASK_HUMAN_TASK, processInstanceId);

        //then
        verifyLazyAddressesCount(humanTaskInstance, 2);
        verifySimpleFieldInAddresses(humanTaskInstance, CITY_GRENOBLE);
        verifyEagerCountryFieldInAddresses(humanTaskInstance, COUNTRY_FRANCE);

        //cleanup
        disableAndDeleteProcess(definition.getId());
    }

    private void addClientBDMZipToClassLoader() throws Exception {
        contextClassLoaderBeforeAddingBPMClientZip = Thread.currentThread().getContextClassLoader();
        final byte[] clientBDMZip = getTenantManagementAPI().getClientBDMZip();
        final ClassLoader classLoaderWithBDM = new ClassloaderRefresher().loadClientModelInClassloader(clientBDMZip,
                contextClassLoaderBeforeAddingBPMClientZip,
                EMPLOYEE_QUALIF_CLASSNAME, clientFolder);
        Thread.currentThread().setContextClassLoader(classLoaderWithBDM);
    }

    private void verifyLazyAddressesCount(final HumanTaskInstance humanTaskInstance, final int expectedCount) throws Exception {

        final Map<String, Serializable> map = new HashMap<String, Serializable>();
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>();

        final Expression createQueryBusinessDataExpression = new ExpressionBuilder().createQueryBusinessDataExpression("expression Name",
                "Employee." + FIND_BY_FIRST_NAME_AND_LAST_NAME_NEW_ORDER, EMPLOYEE_QUALIF_CLASSNAME,
                new ExpressionBuilder().createConstantStringExpression("firstName", "Alphonse"),
                new ExpressionBuilder().createConstantStringExpression("lastName", "Dupond"));

        final Expression countExpression = new ExpressionBuilder().createGroovyScriptExpression("countExpression", "myEmployee.getAddresses().size()"
                , Integer.class.getName(),
                new ExpressionBuilder().createInputExpression("myEmployee", EMPLOYEE_QUALIF_CLASSNAME));
        expressions.put(createQueryBusinessDataExpression, map);

        final Map<String, Serializable> evaluateExpressionsAtProcessInstanciation = getProcessAPI().evaluateExpressionsOnActivityInstance(
                humanTaskInstance.getId(), expressions);

        final Serializable businessData = evaluateExpressionsAtProcessInstanciation.get(createQueryBusinessDataExpression.getName());

        final Map<Expression, Map<String, Serializable>> expressions2 = new HashMap<Expression, Map<String, Serializable>>();
        expressions2.put(countExpression, Collections.singletonMap("myEmployee", businessData));

        final Map<String, Serializable> evaluateExpressionsOnActivityInstance = getProcessAPI().evaluateExpressionsOnActivityInstance(
                humanTaskInstance.getId(), expressions2);

        final Serializable serializable = evaluateExpressionsOnActivityInstance.get("countExpression");
        assertThat(serializable).as("should get " + expectedCount + " address count").isEqualTo(expectedCount);

    }

    private void verifyEagerCountryFieldInAddresses(final HumanTaskInstance humanTaskInstance, final String expectedCountry) throws Exception {

        final Map<String, Serializable> map = new HashMap<String, Serializable>();
        final Map<Expression, Map<String, Serializable>> mapGetEmployee = new HashMap<Expression, Map<String, Serializable>>();

        final Expression getEmployeeExpression = new ExpressionBuilder().createQueryBusinessDataExpression("expression Name",
                "Employee." + FIND_BY_FIRST_NAME_AND_LAST_NAME_NEW_ORDER, EMPLOYEE_QUALIF_CLASSNAME,
                new ExpressionBuilder().createConstantStringExpression("firstName", "Alphonse"),
                new ExpressionBuilder().createConstantStringExpression("lastName", "Dupond"));
        final String queryName = getEmployeeExpression.getName();

        final String getCountryExpressionName = "country";
        final String script = "myEmployee.getAddresses().get(0).getCountry().getName()";
        final Expression getCountryExpression = new ExpressionBuilder().createGroovyScriptExpression(getCountryExpressionName,
                script
                , String.class.getName(),
                new ExpressionBuilder().createInputExpression("myEmployee", EMPLOYEE_QUALIF_CLASSNAME));
        mapGetEmployee.put(getEmployeeExpression, map);

        final Map<String, Serializable> getEmployeeResultMap = getProcessAPI().evaluateExpressionsOnActivityInstance(
                humanTaskInstance.getId(), mapGetEmployee);

        final Serializable returnedEmployee = getEmployeeResultMap.get(queryName);

        final Map<Expression, Map<String, Serializable>> mapGetCountry = new HashMap<Expression, Map<String, Serializable>>();
        mapGetCountry.put(getCountryExpression, Collections.singletonMap("myEmployee", returnedEmployee));

        final Map<String, Serializable> getCountryResultMap = getProcessAPI().evaluateExpressionsOnActivityInstance(
                humanTaskInstance.getId(), mapGetCountry);

        final Serializable serializable = getCountryResultMap.get(getCountryExpressionName);
        assertThat(serializable).as("should get " + expectedCountry + " address count").isEqualTo(expectedCountry);

    }

    private void verifySimpleFieldInAddresses(final HumanTaskInstance humanTaskInstance, final String expectedCity) throws Exception {

        final Map<String, Serializable> map = new HashMap<String, Serializable>();
        final Map<Expression, Map<String, Serializable>> mapGetEmployee = new HashMap<Expression, Map<String, Serializable>>();

        final Expression getEmployeeExpression = new ExpressionBuilder().createQueryBusinessDataExpression("expression Name",
                "Employee." + FIND_BY_FIRST_NAME_AND_LAST_NAME_NEW_ORDER, EMPLOYEE_QUALIF_CLASSNAME,
                new ExpressionBuilder().createConstantStringExpression("firstName", "Alphonse"),
                new ExpressionBuilder().createConstantStringExpression("lastName", "Dupond"));
        final String queryName = getEmployeeExpression.getName();

        final String cityExpression = "city";
        final Expression getCountryExpression = new ExpressionBuilder().createGroovyScriptExpression(cityExpression,
                "myEmployee.getAddresses().get(0).getCity()"
                , String.class.getName(),
                new ExpressionBuilder().createInputExpression("myEmployee", EMPLOYEE_QUALIF_CLASSNAME));
        mapGetEmployee.put(getEmployeeExpression, map);

        final Map<String, Serializable> getEmployeeResultMap = getProcessAPI().evaluateExpressionsOnActivityInstance(
                humanTaskInstance.getId(), mapGetEmployee);

        final Serializable returnedEmployee = getEmployeeResultMap.get(queryName);

        final Map<Expression, Map<String, Serializable>> mapGetCountry = new HashMap<Expression, Map<String, Serializable>>();
        mapGetCountry.put(getCountryExpression, Collections.singletonMap("myEmployee", returnedEmployee));

        final Map<String, Serializable> cityResultMap = getProcessAPI().evaluateExpressionsOnActivityInstance(
                humanTaskInstance.getId(), mapGetCountry);

        final Serializable cityResult = cityResultMap.get(cityExpression);

        assertThat(cityResult).as("should get city name" + expectedCity).isEqualTo(expectedCity);

    }

    private Expression createGrovyExpressionThatCreateEmployeWithOneAddress(final String businessDataAdressName) throws InvalidExpressionException {
        final StringBuilder script = new StringBuilder();
        script.append("import ")
                .append(EMPLOYEE_QUALIF_CLASSNAME)
                .append(";")
                .append("import ")
                .append(ADDRESS_QUALIF_CLASSNAME)
                .append("; Employee e = new Employee(); e.firstName = 'Alphonse';")
                .append(" e.lastName = 'Dupond'; e.addToAddresses(")
                .append(businessDataAdressName)
                .append("); return e;");
        return new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", script.toString(), EMPLOYEE_QUALIF_CLASSNAME,
                createBusinessDataExpressionWithName(businessDataAdressName));
    }

    private Expression createBusinessDataExpressionWithName(final String businessDataName) throws InvalidExpressionException {
        Expression createBusinessDataExpression;
        createBusinessDataExpression = new ExpressionBuilder().createBusinessDataExpression(businessDataName, ADDRESS_QUALIF_CLASSNAME);
        return createBusinessDataExpression;
    }

    private Expression createGrovyExpressionThatCreateAddressWithCityName(final String city, final String country) throws InvalidExpressionException {
        final Expression addressExpression;
        final StringBuilder builder = new StringBuilder();
        builder.append("import org.bonita.pojo.Address; ")
                .append("import org.bonita.pojo.Country; ")
                .append("Country country = new Country(); ")
                .append("country.name='")
                .append(country)
                .append("'; ")
                .append("Address address = new Address();")
                .append("address.street='32, rue Gustave Eiffel'; ")
                .append("address.city='")
                .append(city)
                .append("'; ")
                .append("address.country = country; ")
                .append("address;");

        addressExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewAddress",
                builder.toString(),
                ADDRESS_QUALIF_CLASSNAME);
        return addressExpression;
    }

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
        country.setFetchType(FetchType.EAGER);
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
        employee.setDescription("Describe a simple employee");
        employee.addUniqueConstraint("uk_fl", "firstName", "lastName");

        final Query findByFirstNAmeAndLastNameNewOrder = employee.addQuery(FIND_BY_FIRST_NAME_AND_LAST_NAME_NEW_ORDER,
                "SELECT e FROM Employee e WHERE e.firstName =:firstName AND e.lastName = :lastName ORDER BY e.lastName", List.class.getName());
        findByFirstNAmeAndLastNameNewOrder.addQueryParameter("firstName", String.class.getName());
        findByFirstNAmeAndLastNameNewOrder.addQueryParameter("lastName", String.class.getName());

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

}
