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
package org.bonitasoft.engine.business.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.LocalServerTestsInitializer;
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.Query;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BonitaTestRunner.class)
@BonitaSuiteRunner.Initializer(LocalServerTestsInitializer.class)
public class BDRepositoryLocalIT extends CommonAPIIT {

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

    private static final String BDM_PACKAGE_PREFIX = "com.company.model";

    public static final String PRODUCT_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".pojo.Product";

    public static final String PRODUCT_CATALOG_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".pojo.ProductCatalog";

    private static final String ADDRESS_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".pojo.Address";

    private static final String EMPLOYEE_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".pojo.Employee";

    private static final String COUNTRY_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".pojo.Country";

    public static final String PERSON_QUALIFIED_NAME = BDM_PACKAGE_PREFIX + ".pojo.Person";

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
        getTenantAdministrationAPI().pause();
        getTenantAdministrationAPI().installBusinessDataModel(zip);
        getTenantAdministrationAPI().resume();

        // needed for remote testing
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
        if (!getTenantAdministrationAPI().isPaused()) {
            getTenantAdministrationAPI().pause();
            getTenantAdministrationAPI().cleanAndUninstallBusinessDataModel();
            getTenantAdministrationAPI().resume();
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
        // given
        final Expression addressGrenobleExpression = createGrovyExpressionThatCreateAddressWithCityName(CITY_GRENOBLE, COUNTRY_FRANCE);
        final Expression addressSfExpression = createGrovyExpressionThatCreateAddressWithCityName(CITY_SF, COUNTRY_USA);
        final Expression addressRomeExpression = createGrovyExpressionThatCreateAddressWithCityName(CITY_ROME, COUNTRY_ITALY);

        final Expression employeeExpression = createGrovyExpressionThatCreateEmployeWithOneAddress(BIZ_GRENOBLE_ADDRESS);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(
                PROCESS_NAME, VERSION);

        processDefinitionBuilder.addBusinessData(BIZ_EMPLOYEE, EMPLOYEE_QUALIFIED_NAME, null);
        processDefinitionBuilder.addBusinessData(BIZ_GRENOBLE_ADDRESS, ADDRESS_QUALIFIED_NAME, addressGrenobleExpression);
        processDefinitionBuilder.addBusinessData(BIZ_SF_ADDRESS, ADDRESS_QUALIFIED_NAME, addressSfExpression);
        processDefinitionBuilder.addBusinessData(BIZ_ROME_ADDRESS, ADDRESS_QUALIFIED_NAME, addressRomeExpression);

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

        // call java operation to add address to employee
        processDefinitionBuilder.addAutomaticTask(TASK_TO_CALL_JAVA_METHOD_OPERATION)
                .addOperation(
                        new OperationBuilder().createBusinessDataSetAttributeOperation(BIZ_EMPLOYEE, "addToAddresses", ADDRESS_QUALIFIED_NAME,
                                new ExpressionBuilder().createBusinessDataExpression(BIZ_ROME_ADDRESS, ADDRESS_QUALIFIED_NAME)));

        // waiting task
        processDefinitionBuilder.addUserTask(TASK_HUMAN_TASK, ACTOR_NAME);

        // transitions
        processDefinitionBuilder.addTransition(TASK_AUTOMATIC_TASK_TO_INIT_BIZ_DATA, TASK_TO_CALL_JAVA_METHOD_OPERATION);
        processDefinitionBuilder.addTransition(TASK_TO_CALL_JAVA_METHOD_OPERATION, TASK_HUMAN_TASK);

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        final HumanTaskInstance humanTaskInstance = waitForUserTaskAndGetIt(processInstance, TASK_HUMAN_TASK);

        // then
        verifyLazyAddressesCount(humanTaskInstance, 2);
        verifySimpleFieldInAddresses(humanTaskInstance, CITY_GRENOBLE);
        verifyEagerCountryFieldInAddresses(humanTaskInstance, COUNTRY_FRANCE);

        // cleanup
        disableAndDeleteProcess(definition.getId());
    }

    private void addClientBDMZipToClassLoader() throws Exception {
        contextClassLoaderBeforeAddingBPMClientZip = Thread.currentThread().getContextClassLoader();
        final byte[] clientBDMZip = getTenantAdministrationAPI().getClientBDMZip();
        final ClassLoader classLoaderWithBDM = new ClassloaderRefresher().loadClientModelInClassloader(clientBDMZip,
                contextClassLoaderBeforeAddingBPMClientZip,
                EMPLOYEE_QUALIFIED_NAME, clientFolder);
        Thread.currentThread().setContextClassLoader(classLoaderWithBDM);
    }

    private void verifyLazyAddressesCount(final HumanTaskInstance humanTaskInstance, final int expectedCount) throws Exception {

        final Map<String, Serializable> map = new HashMap<String, Serializable>();
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>();

        final Expression createQueryBusinessDataExpression = new ExpressionBuilder().createQueryBusinessDataExpression("expression Name",
                "Employee." + FIND_BY_FIRST_NAME_AND_LAST_NAME_NEW_ORDER, EMPLOYEE_QUALIFIED_NAME,
                new ExpressionBuilder().createConstantStringExpression("firstName", "Alphonse"),
                new ExpressionBuilder().createConstantStringExpression("lastName", "Dupond"));

        final Expression countExpression = new ExpressionBuilder().createGroovyScriptExpression("countExpression", "myEmployee.getAddresses().size()"
                , Integer.class.getName(),
                new ExpressionBuilder().createInputExpression("myEmployee", EMPLOYEE_QUALIFIED_NAME));
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
                "Employee." + FIND_BY_FIRST_NAME_AND_LAST_NAME_NEW_ORDER, EMPLOYEE_QUALIFIED_NAME,
                new ExpressionBuilder().createConstantStringExpression("firstName", "Alphonse"),
                new ExpressionBuilder().createConstantStringExpression("lastName", "Dupond"));
        final String queryName = getEmployeeExpression.getName();

        final String getCountryExpressionName = "country";
        final String script = "myEmployee.getAddresses().get(0).getCountry().getName()";
        final Expression getCountryExpression = new ExpressionBuilder().createGroovyScriptExpression(getCountryExpressionName,
                script
                , String.class.getName(),
                new ExpressionBuilder().createInputExpression("myEmployee", EMPLOYEE_QUALIFIED_NAME));
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
                "Employee." + FIND_BY_FIRST_NAME_AND_LAST_NAME_NEW_ORDER, EMPLOYEE_QUALIFIED_NAME,
                new ExpressionBuilder().createConstantStringExpression("firstName", "Alphonse"),
                new ExpressionBuilder().createConstantStringExpression("lastName", "Dupond"));
        final String queryName = getEmployeeExpression.getName();

        final String cityExpression = "city";
        final Expression getCountryExpression = new ExpressionBuilder().createGroovyScriptExpression(cityExpression,
                "myEmployee.getAddresses().get(0).getCity()"
                , String.class.getName(),
                new ExpressionBuilder().createInputExpression("myEmployee", EMPLOYEE_QUALIFIED_NAME));
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
                .append(EMPLOYEE_QUALIFIED_NAME)
                .append(";")
                .append("import ")
                .append(ADDRESS_QUALIFIED_NAME)
                .append("; Employee e = new Employee(); e.firstName = 'Alphonse';")
                .append(" e.lastName = 'Dupond'; e.addToAddresses(")
                .append(businessDataAdressName)
                .append("); return e;");
        return new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", script.toString(), EMPLOYEE_QUALIFIED_NAME,
                createBusinessDataExpressionWithName(businessDataAdressName));
    }

    private Expression createBusinessDataExpressionWithName(final String businessDataName) throws InvalidExpressionException {
        Expression createBusinessDataExpression;
        createBusinessDataExpression = new ExpressionBuilder().createBusinessDataExpression(businessDataName, ADDRESS_QUALIFIED_NAME);
        return createBusinessDataExpression;
    }

    private Expression createGrovyExpressionThatCreateAddressWithCityName(final String city, final String country) throws InvalidExpressionException {
        final Expression addressExpression;
        final StringBuilder builder = new StringBuilder();
        builder.append("import ")
                .append(ADDRESS_QUALIFIED_NAME)
                .append("; ")
                .append("import ")
                .append(COUNTRY_QUALIFIED_NAME)
                .append("; ")
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
                ADDRESS_QUALIFIED_NAME);
        return addressExpression;
    }

    private BusinessObjectModel buildBOM() {
        final SimpleField name = new SimpleField();
        name.setName("name");
        name.setType(FieldType.STRING);

        final BusinessObject countryBO = new BusinessObject();
        countryBO.setQualifiedName(COUNTRY_QUALIFIED_NAME);
        countryBO.addField(name);

        final SimpleField street = new SimpleField();
        street.setName("street");
        street.setType(FieldType.STRING);

        final SimpleField city = new SimpleField();
        city.setName("city");
        city.setType(FieldType.STRING);

        final RelationField country = new RelationField();
        country.setType(RelationField.Type.AGGREGATION);
        country.setFetchType(RelationField.FetchType.EAGER);
        country.setName("country");
        country.setCollection(Boolean.FALSE);
        country.setNullable(Boolean.TRUE);
        country.setReference(countryBO);

        final BusinessObject addressBO = new BusinessObject();
        addressBO.setQualifiedName(ADDRESS_QUALIFIED_NAME);
        addressBO.addField(street);
        addressBO.addField(city);
        addressBO.addField(country);

        final RelationField addresses = new RelationField();
        addresses.setType(RelationField.Type.AGGREGATION);
        addresses.setFetchType(RelationField.FetchType.LAZY);
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
        employee.setQualifiedName(EMPLOYEE_QUALIFIED_NAME);
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
        person.setQualifiedName(PERSON_QUALIFIED_NAME);
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

}
