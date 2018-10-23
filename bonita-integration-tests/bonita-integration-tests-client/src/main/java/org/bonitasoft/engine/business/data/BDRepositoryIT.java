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

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;
import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
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
import org.apache.commons.lang3.StringUtils;
import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.api.APIClient;
import org.bonitasoft.engine.bdm.BusinessObjectDAOFactory;
import org.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import org.bonitasoft.engine.bdm.dao.BusinessObjectDAO;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.businessdata.BusinessDataQueryMetadata;
import org.bonitasoft.engine.bpm.businessdata.BusinessDataQueryResult;
import org.bonitasoft.engine.bpm.businessdata.impl.BusinessDataQueryResultImpl;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.contract.Type;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.document.DocumentNotFoundException;
import org.bonitasoft.engine.bpm.document.DocumentValue;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessEnablementException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.CallActivityBuilder;
import org.bonitasoft.engine.bpm.process.impl.CatchMessageEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.IntermediateThrowEventDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.StartEventDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SubProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ThrowMessageEventTriggerBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.expression.ExpressionEvaluationException;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.expression.impl.ExpressionImpl;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.SAXException;

public class BDRepositoryIT extends CommonAPIIT {

    private static final String BUSINESS_DATA_CLASS_NAME_ID_FIELD = "/businessdata/{className}/{id}/{field}";
    private static final String ENTITY_CLASS_NAME = "entityClassName";
    private static String bdmDeployedVersion = "0";
    private static int iterator = 1;
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private User testUser;
    private File clientFolder;
    private long tenantId;

    @Before
    public void setUp() throws Exception {
        clientFolder = temporaryFolder.newFolder();
        loginOnDefaultTenantWithDefaultTechnicalUser();
        testUser = createUser("testUser", "bpm");

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
        if (!getTenantAdministrationAPI().isPaused()) {
            getTenantAdministrationAPI().pause();
            getTenantAdministrationAPI().cleanAndUninstallBusinessDataModel();
            getTenantAdministrationAPI().resume();
        }

        deleteUser(testUser);
        logoutOnTenant();
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
        final String businessDataModelVersion = getTenantAdministrationAPI().installBusinessDataModel(zip);
        getTenantAdministrationAPI().resume();
        assertThat(businessDataModelVersion).as("should have deployed BDM").isNotNull();
        verifyBdmIsWellDeployed();
    }

    private void verifyBdmIsWellDeployed() throws Exception {
        final String businessDataModelVersion = getTenantAdministrationAPI().getBusinessDataModelVersion();
        APITestUtil.LOGGER.warn("previous businessDataModelVersion:" + this.bdmDeployedVersion);
        APITestUtil.LOGGER.warn("new businessDataModelVersion     :" + businessDataModelVersion);
        assertThat(businessDataModelVersion).as("should have deployed a new version of BDM").isNotEqualTo(this.bdmDeployedVersion);
        this.bdmDeployedVersion = businessDataModelVersion;
    }

    private ProcessDefinition deploySimpleProcessWithBusinessData(final String aQualifiedName) throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        final String bizDataName = "myBizData";
        processDefinitionBuilder.addBusinessData(bizDataName, aQualifiedName, null);

        final ProcessDefinition processDefinition = deployProcess(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinitionBuilder.done()).done());
        getProcessAPI().addUserToActor(ACTOR_NAME, processDefinition, testUser.getId());
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
        return model;
    }

    @Test
    public void shouldBeAbleToUpdateBusinessDataUsingBizDataJavaSetterOperation() throws Exception {
        final String processContractInputName = "lastName_input";
        final String initialLastNameValue = "Trebi";
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression(
                "createNewEmployee",
                new StringBuilder("import ")
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

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, testUser);
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

        assignAndExecuteStep(step0, testUser);
        final long step2 = waitForUserTask(processInstance, "step2");

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

        assignAndExecuteStep(step2, testUser);

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

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, testUser);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());

        final long step1Id = waitForUserTask(processInstance, "step1");
        final String employeeToString = getEmployeeToString("myEmployee", processInstance.getId());
        assertThat(employeeToString).isEqualTo("Employee [firstName=Jane, lastName=Doe]");

        assignAndExecuteStep(step1Id, testUser);
        waitForUserTask(processInstance, "step2");
        final String people = getEmployeeToString(secondBizData, processInstance.getId());
        assertThat(people).isEqualTo("Employee [firstName=Jane, lastName=Doe]");

        disableAndDeleteProcess(definition.getId());
    }

    @Test
    public void deployABDRAndCreateABOAndUpdateThroughAGroovyScript() throws Exception {
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

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, testUser);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());
        waitForUserTask(instance, "step1");

        final String employeeToString = getEmployeeToString("myEmployee", instance.getId());
        assertThat(employeeToString).isEqualTo("Employee [firstName=John, lastName=BPM]");

        disableAndDeleteProcess(definition.getId());
    }

    @Test
    public void updatingABOThroughAGroovyScriptShouldUpdateTheBO() throws Exception {
        String businessDataName = "myEmployee";
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("BS-18402", "7.7.2");
        processDefinitionBuilder.addBusinessData(businessDataName, EMPLOYEE_QUALIFIED_NAME, new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee",
                "import " + EMPLOYEE_QUALIFIED_NAME + "; Employee e = new Employee(); e.firstName = 'John'; e.lastName = 'Doe'; return e;", EMPLOYEE_QUALIFIED_NAME));
        processDefinitionBuilder.addBusinessData("myAddress", ADDRESS_QUALIFIED_NAME,
                new ExpressionBuilder().createGroovyScriptExpression("CreateNewAddress", "import " +
                                ADDRESS_QUALIFIED_NAME + "; Address a = new Address(); a.street='32, rue Gustave Eiffel'; a.city='Grenoble'; return a;",
                        ADDRESS_QUALIFIED_NAME));
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addAutomaticTask("step1")
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand(businessDataName), OperatorType.ASSIGNMENT, null, null,
                        new ExpressionBuilder().createGroovyScriptExpression("setEmployee", "import " +
                                        EMPLOYEE_QUALIFIED_NAME + "; myEmployee.setAddress(myAddress); return myEmployee;",
                                EMPLOYEE_QUALIFIED_NAME, new ExpressionBuilder().createBusinessDataExpression("myAddress", ADDRESS_QUALIFIED_NAME),
                                new ExpressionBuilder().createBusinessDataExpression(businessDataName, EMPLOYEE_QUALIFIED_NAME)));
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addStartEvent("Start");
        processDefinitionBuilder.addEndEvent("End");
        processDefinitionBuilder.addTransition("Start", "step1");
        processDefinitionBuilder.addTransition("step1", "step2");
        processDefinitionBuilder.addTransition("step2", "End");
        processDefinitionBuilder.addContextEntry("myEmployee_context_key",
                new ExpressionBuilder().createGroovyScriptExpression("retrieveEmployeeAddressBasicInfo",
                        "\"Employee [ address street = \" + " + businessDataName + ".address.street + \" ]\";",
                        String.class.getName(),
                        new ExpressionBuilder().createBusinessDataExpression(businessDataName, EMPLOYEE_QUALIFIED_NAME)));

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, testUser);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());
        waitForUserTask(instance.getId(), "step2");

        final Serializable employeeFromContext = getProcessAPI().getProcessInstanceExecutionContext(instance.getId()).get("myEmployee_context_key");
        assertThat(employeeFromContext).isEqualTo("Employee [ address street = 32, rue Gustave Eiffel ]");

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
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<>();
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
                        .toString(),
                EMPLOYEE_QUALIFIED_NAME);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIFIED_NAME, employeeExpression);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, testUser);
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

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, testUser);
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
    public void should_deploy_generate_client_bdm_zip() throws Exception {
        assertThat(getTenantAdministrationAPI().getClientBDMZip()).isNotEmpty();
    }

    @Test
    public void should_undeploy_delete_generate_client_bdm_zip() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        getTenantAdministrationAPI().pause();
        getTenantAdministrationAPI().cleanAndUninstallBusinessDataModel();
        getTenantAdministrationAPI().resume();
        expectedException.expect(BusinessDataRepositoryException.class);
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

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, testUser);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        waitForUserTask(processInstance, "step2");

        // Let's check we can retrieve firstName using DAO call:
        final long processInstanceId = processInstance.getId();
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<>(1);
        final String getLastNameWithDAOExpression = "retrieveEmployeeByFirstName";
        expressions.put(
                new ExpressionBuilder().createGroovyScriptExpression(getLastNameWithDAOExpression, "import " + EMPLOYEE_QUALIFIED_NAME + "; Employee e = "
                        + employeeDAOName + ".findByFirstName('" + firstName + "', 0, 10).get(0); e.getAddresses().get(0).city", String.class.getName(),
                        new ExpressionBuilder().buildBusinessObjectDAOExpression(employeeDAOName, EMPLOYEE_QUALIFIED_NAME + "DAO")),
                null);
        expressions.put(new ExpressionBuilder().createQueryBusinessDataExpression(COUNT_ADDRESS, "Address." + COUNT_ADDRESS, Long.class.getName()), null);
        expressions.put(new ExpressionBuilder().createQueryBusinessDataExpression(COUNT_EMPLOYEE, "Employee." + COUNT_EMPLOYEE, Long.class.getName()), null);
        Map<String, Serializable> evaluatedExpressions = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
        String returnedLastName = (String) evaluatedExpressions.get(getLastNameWithDAOExpression);
        assertThat(returnedLastName).isEqualTo("Grenoble");

        final Serializable nbOfAddress = evaluatedExpressions.get(COUNT_ADDRESS);
        final Serializable nbOfEmployee = evaluatedExpressions.get(COUNT_EMPLOYEE);

        assertThat(nbOfAddress).isEqualTo(1L);
        assertThat(nbOfEmployee).isEqualTo(1L);

        logoutOnTenant();

        loginOnDefaultTenantWithDefaultTechnicalUser();
        getTenantAdministrationAPI().pause();
        getTenantAdministrationAPI().resume();
        logoutOnTenant();

        loginOnDefaultTenantWith("testUser", "bpm");

        evaluatedExpressions = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
        returnedLastName = (String) evaluatedExpressions.get(getLastNameWithDAOExpression);
        assertThat(returnedLastName).isEqualTo("Grenoble");
        logoutOnTenant();
        loginOnDefaultTenantWithDefaultTechnicalUser();

        assertCount(processInstanceId);

        disableAndDeleteProcess(definition.getId());
    }

    /**
     * {@link BDRepositoryIT#should_use_apiClient_to_instantiate_dao_on_client_side}
     *
     * @throws Exception
     */
    @Test
    @Deprecated
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

    @Test
    public void should_use_apiClient_to_instantiate_dao_on_client_side() throws Exception {
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
            final APIClient client = new APIClient(apiSession);
            final BusinessObjectDAO daoImpl = client.getDAO(daoInterface);
            assertThat(daoImpl.getClass().getName()).isEqualTo(EMPLOYEE_QUALIFIED_NAME + "DAOImpl");

            Method daoMethod = daoImpl.getClass().getMethod("countForFindByLastName", String.class);
            assertThat(daoMethod).isNotNull();
            assertThat(daoMethod.getReturnType().getName()).isEqualTo(Long.class.getName());
            final Long count = (Long) daoMethod.invoke(daoImpl, "Pagnol");
            assertThat(count).isEqualTo(1);

            daoMethod = daoImpl.getClass().getMethod("findByLastName", String.class, int.class, int.class);
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

    @Test
    public void should_retrieve_bdm_object_with_lazy_and_non_lazy_composition_objects_using_dao() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIFIED_NAME,
                new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee",
                        "import " + EMPLOYEE_QUALIFIED_NAME + "\n" +
                                "import " + DOG_QUALIFIED_NAME + "\n" +
                                "import " + CAT_QUALIFIED_NAME + "\n" +
                                "Employee e = new Employee()\n" +
                                "e.firstName ='john'\n" +
                                "e.lastName ='doe'\n" +
                                "def d = new Dog()\n" +
                                "d.name = 'kiki'\n" +
                                "d.age = 2\n" +
                                "e.setDog(d)\n" +
                                "def c = new Cat()\n" +
                                "c.name = 'fifi'\n" +
                                "c.age = 5\n" +
                                "e.setCat(c)\n" +
                                "return e",
                        EMPLOYEE_QUALIFIED_NAME));
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, testUser);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());
        waitForUserTask(instance, "step1");

        disableAndDeleteProcess(definition.getId());
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        final ClassLoader classLoaderWithBDM = new ClassloaderRefresher().loadClientModelInClassloader(getTenantAdministrationAPI().getClientBDMZip(),
                contextClassLoader,
                EMPLOYEE_QUALIFIED_NAME, clientFolder);

        try {
            Thread.currentThread().setContextClassLoader(classLoaderWithBDM);
            final BusinessObjectDAO daoImpl = new APIClient(getSession())
                    .getDAO((Class<? extends BusinessObjectDAO>) Class.forName(EMPLOYEE_QUALIFIED_NAME + "DAO", true,
                            classLoaderWithBDM));

            List<?> employees = (List<?>) daoImpl.getClass().getMethod("find", int.class, int.class).invoke(daoImpl, 0, 100);
            assertThat(invokeMethod(invokeMethod(employees.get(0), "getCat"), "getName")).isEqualTo("fifi");
            assertThat(invokeMethod(invokeMethod(employees.get(0), "getDog"), "getName")).isEqualTo("kiki");
            Object employee = daoImpl.getClass().getMethod("findByFirstNameAndLastName", String.class, String.class).invoke(daoImpl, "john", "doe");
            assertThat(invokeMethod(invokeMethod(employee, "getCat"), "getName")).isEqualTo("fifi");
            assertThat(invokeMethod(invokeMethod(employee, "getDog"), "getName")).isEqualTo("kiki");
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private Object invokeMethod(Object object, String method) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        return object.getClass().getMethod(method, new Class[0]).invoke(object);
    }

    private void addEmployee(final String firstName, final String lastName, final AddressRef... addresses) throws Exception {
        final List<Expression> dependencies = new ArrayList<>();
        for (final AddressRef ref : addresses) {
            dependencies.add(ref.getExpression());
        }
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee",
                createNewEmployeeScriptContent(firstName, lastName, addresses),
                EMPLOYEE_QUALIFIED_NAME, dependencies);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIFIED_NAME, null);
        final UserTaskDefinitionBuilder task = processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        for (final AddressRef ref : addresses) {
            processDefinitionBuilder.addBusinessData(ref.getVarName(), ADDRESS_QUALIFIED_NAME, null);
            task.addOperation(ref.getCreationOperation());
        }
        task.addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand("myEmployee"),
                OperatorType.ASSIGNMENT, null, null, employeeExpression);

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, testUser);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());
        waitForUserTaskAndExecuteIt(instance, "step1", testUser);
        waitForProcessToFinish(instance);

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
            for (AddressRef address : addresses) {
                sb.append("e.addToAddresses(" + address.getVarName() + ")");
                sb.append("\n");
            }
        }

        sb.append("return e;");
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

        return deployAndEnableProcessWithActor(businessArchiveBuilder.done(), ACTOR_NAME, testUser);
    }

    private String getEmployeeToString(final String businessDataName, final long processInstanceId) throws InvalidExpressionException {
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<>(5);
        final String expressionEmployee = "retrieve_Employee";
        expressions.put(
                new ExpressionBuilder().createGroovyScriptExpression(expressionEmployee, "\"Employee [firstName=\" + " + businessDataName
                        + ".firstName + \", lastName=\" + " + businessDataName + ".lastName + \"]\";", String.class.getName(),
                        new ExpressionBuilder().createBusinessDataExpression(businessDataName, EMPLOYEE_QUALIFIED_NAME)),
                null);
        try {
            final Map<String, Serializable> evaluatedExpressions = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
            return (String) evaluatedExpressions.get(expressionEmployee);
        } catch (final ExpressionEvaluationException eee) {
            System.err.println(eee.getMessage());
            return null;
        }
    }

    @Test
    public void shouldBeAbleToCreate2BusinessDataUsingIntermixedBizDataJavaSetterOperations() throws Exception {
        final Expression countryQueryNameParameter = new ExpressionBuilder().createExpression("name", "France", String.class.getName(),
                ExpressionType.TYPE_CONSTANT);
        final Expression countryQueryExpression = new ExpressionBuilder().createQueryBusinessDataExpression("country", "Country.findByName",
                COUNTRY_QUALIFIED_NAME, countryQueryNameParameter);
        final Expression createNewAddressExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewAddress",
                "import " + ADDRESS_QUALIFIED_NAME + "; Address a = new Address(street:'32, rue Gustave Eiffel', city:'Grenoble'); a;",
                ADDRESS_QUALIFIED_NAME);
        final Expression createNewCountryExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewCountry",
                "import " + COUNTRY_QUALIFIED_NAME + "; Country c = new Country(name:'France'); c;",
                COUNTRY_QUALIFIED_NAME);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(
                "shouldBeAbleToUpdateBusinessDataUsingJavaSetterOperation", PROCESS_VERSION);
        final String businessDataName = "newBornBaby";
        final String businessDataName2 = "data2";
        processDefinitionBuilder.addBusinessData(businessDataName, EMPLOYEE_QUALIFIED_NAME, null);
        processDefinitionBuilder.addBusinessData(businessDataName2, EMPLOYEE_QUALIFIED_NAME, null);
        processDefinitionBuilder.addBusinessData("address", ADDRESS_QUALIFIED_NAME, createNewAddressExpression);
        processDefinitionBuilder.addBusinessData("country", COUNTRY_QUALIFIED_NAME, createNewCountryExpression);
        final String retrievedCountryData = "retrievedCountry";
        processDefinitionBuilder.addBusinessData(retrievedCountryData, COUNTRY_QUALIFIED_NAME, null);
        processDefinitionBuilder.addBusinessData("noneAddress", ADDRESS_QUALIFIED_NAME, null);
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
                                countryQueryExpression))
                .addOperation(
                        new OperationBuilder().createBusinessDataSetAttributeOperation(businessDataName2, "setAddress", ADDRESS_QUALIFIED_NAME,
                                new ExpressionBuilder().createBusinessDataExpression("noneAddress", ADDRESS_QUALIFIED_NAME)))
                .addAutomaticTask("step2")
                .addOperation(
                        new OperationBuilder().attachBusinessDataSetAttributeOperation(retrievedCountryData, countryQueryExpression))
                .addOperation(
                        new OperationBuilder().createBusinessDataSetAttributeOperation(retrievedCountryData, "setName", String.class.getName(),
                                new ExpressionBuilder().createConstantStringExpression("FRANCE")));
        processDefinitionBuilder.addUserTask("step3", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");
        processDefinitionBuilder.addTransition("step2", "step3");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, testUser);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        waitForUserTask(processInstance, "step3");

        final Expression getEmployeeAddressExpression = new ExpressionBuilder().createGroovyScriptExpression("getEmployeeAddress",
                "if (" + businessDataName2 + ".address == null) return \"null\"\n" + businessDataName2 + ".address.city", String.class.getName(),
                new ExpressionBuilder().createBusinessDataExpression(businessDataName2, EMPLOYEE_QUALIFIED_NAME));
        final Expression getCountryExpression = new ExpressionBuilder().createGroovyScriptExpression("getCountry",
                "if (" + retrievedCountryData + " == null) return \"null\"\n" + retrievedCountryData + ".name", String.class.getName(),
                new ExpressionBuilder().createBusinessDataExpression(retrievedCountryData, COUNTRY_QUALIFIED_NAME));

        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<>(2);
        expressions.put(getEmployeeAddressExpression, null);
        expressions.put(getCountryExpression, null);
        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstance.getId(),
                expressions);
        assertThat(result.get(getEmployeeAddressExpression.getName())).isEqualTo("null");
        assertThat(result.get(getCountryExpression.getName())).isEqualTo("FRANCE");

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

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, testUser);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());

        final long step1Id = waitForUserTask(processInstance, "step1");
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<>(2);
        expressions.put(new ExpressionBuilder().createQueryBusinessDataExpression("countEmployee", "Employee.countEmployee", Long.class.getName()),
                Collections.<String, Serializable> emptyMap());

        final long processInstanceId = processInstance.getId();
        Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
        assertThat(result.get("countEmployee")).isEqualTo(1L);

        assignAndExecuteStep(step1Id, testUser);
        waitForUserTask(processInstance, "step2");
        result = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
        assertThat(result.get("countEmployee")).isEqualTo(0L);

        disableAndDeleteProcess(definition.getId());
    }

    public void assertCount(final long processInstanceId) throws Exception {
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<>(2);
        expressions.put(new ExpressionBuilder().createQueryBusinessDataExpression("countEmployee", "Employee.countEmployee", Long.class.getName()),
                Collections.<String, Serializable> emptyMap());

        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
        assertThat(result.get("countEmployee")).isEqualTo(1L);
    }

    @Test
    public void deployABDRAndCreateAndUpdateAMultipleBusinessData() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression(
                "createNewEmployees",
                new StringBuilder().append("import ").append(EMPLOYEE_QUALIFIED_NAME)
                        .append("; Employee john = new Employee(); john.firstName = 'John'; john.lastName = '저는 7년 동안 한국에서 살았어요';")
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

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, testUser);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());

        final long step1Id = waitForUserTask(instance, "step1");
        String employeeToString = getEmployeesToString("myEmployees", instance.getId());
        assertThat(employeeToString).isEqualTo("Employee [firstName=[Jane, John], lastName=[Doe, 저는 7년 동안 한국에서 살았어요]]");

        assignAndExecuteStep(step1Id, testUser);
        waitForUserTask(instance, "step2");
        employeeToString = getEmployeesToString("myEmployees", instance.getId());
        assertThat(employeeToString)
                .isEqualTo("Employee [firstName=[Jane, John, Jack], lastName=[Doe, 저는 7년 동안 한국에서 살았어요, Doe]]");

        disableAndDeleteProcess(definition.getId());
    }

    @Test
    public void deployBDRAndCreateAndUpdateAInitiallyEmptyMultipleBusinessData() throws Exception {
        final Expression employeeExpression1 = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployees",
                new StringBuilder().append("import ").append(EMPLOYEE_QUALIFIED_NAME)
                        .append("; return new ArrayList<>();").toString(),
                List.class.getName());
        final Expression myEmployeesDependency = new ExpressionBuilder().createBusinessDataExpression("myEmployees", List.class.getName());
        final Expression jackExpression = new ExpressionBuilder().createGroovyScriptExpression("createJack", "import " + EMPLOYEE_QUALIFIED_NAME
                + "; Employee jack = new Employee(); jack.firstName = 'Jack'; jack.lastName = 'Doe'; myEmployees.add(jack) ; return myEmployees;",
                List.class.getName(), myEmployeesDependency);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addBusinessData("myEmployees", EMPLOYEE_QUALIFIED_NAME, employeeExpression1).setMultiple(true);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME)
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand("myEmployees"), OperatorType.ASSIGNMENT, null, null, jackExpression);
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, testUser);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());

        final long step1Id = waitForUserTask(instance, "step1");
        String employeeToString = getEmployeesToString("myEmployees", instance.getId());
        assertThat(employeeToString).isEqualTo("Employee [firstName=[], lastName=[]]");

        assignAndExecuteStep(step1Id, testUser);
        waitForUserTask(instance, "step2");
        employeeToString = getEmployeesToString("myEmployees", instance.getId());
        assertThat(employeeToString)
                .isEqualTo("Employee [firstName=[Jack], lastName=[Doe]]");

        disableAndDeleteProcess(definition.getId());
    }

    private String getEmployeesToString(final String businessDataName, final long processInstanceId) throws InvalidExpressionException {
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<>(5);
        final String expressionEmployee = "retrieve_Employee";
        expressions.put(
                new ExpressionBuilder().createGroovyScriptExpression(expressionEmployee, "\"Employee [firstName=\" + " + businessDataName
                        + ".firstName + \", lastName=\" + " + businessDataName + ".lastName + \"]\";", String.class.getName(),
                        new ExpressionBuilder().createBusinessDataExpression(businessDataName, List.class.getName())),
                null);
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
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, testUser);

        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(instance, "step1", testUser);
        waitForUserTaskAndExecuteIt(instance, "step1", testUser);

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
        final ProcessDefinition subProcessDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, testUser);

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
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, testUser);

        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(instance, "step1", testUser);
        waitForUserTaskAndExecuteIt(instance, "step1", testUser);
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
        final ProcessDefinition subProcessDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, testUser);

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
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, testUser);

        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(instance, "step1", testUser);
        waitForUserTaskAndExecuteIt(instance, "step1", testUser);
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
        final ProcessDefinition subProcessDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, testUser);

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
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, testUser);

        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(instance, "step1", testUser);
        waitForUserTaskAndExecuteIt(instance, "step1", testUser);
        waitForUserTask(instance, "step2");

        final String employeeToString = getEmployeesToString("myEmployees", instance.getId());
        assertThat(employeeToString).contains("John", "Doe");

        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(subProcessDefinition);
    }

    //BS-13803
    @Test
    public void initializeBusinessDataInCalledProcessWithContractInput() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression(
                "createNewEmployee",
                "import " + EMPLOYEE_QUALIFIED_NAME + "; Employee john = new Employee(); john.firstName = theInput; john.lastName = 'Doe'; john;",
                EMPLOYEE_QUALIFIED_NAME, new ExpressionBuilder().createContractInputExpression("theInput", String.class.getName()));
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("createEmployeeInCallActivity", "1.2-beta");
        builder.addActor(ACTOR_NAME);
        builder.addContract().addInput("theInput", Type.TEXT, "the input");
        builder.addBusinessData("employee", EMPLOYEE_QUALIFIED_NAME, employeeExpression);
        builder.addUserTask("step1", ACTOR_NAME);
        final ProcessDefinition subProcessDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, testUser);

        builder = new ProcessDefinitionBuilder().createNewInstance("createEmployeeInCallActivityMaster", "1.2-beta");
        builder.addActor(ACTOR_NAME);
        builder.addCallActivity("call",
                new ExpressionBuilder().createConstantStringExpression(subProcessDefinition.getName()),
                new ExpressionBuilder().createConstantStringExpression(subProcessDefinition.getVersion())).addProcessStartContractInput("theInput",
                        new ExpressionBuilder().createConstantStringExpression("theValue"));
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addTransition("call", "step2");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, testUser);

        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask("step1");
        final Expression employee = new ExpressionBuilder().createGroovyScriptExpression("script", "employee.firstName", String.class.getName(),
                new ExpressionBuilder().createBusinessDataExpression("employee", EMPLOYEE_QUALIFIED_NAME));
        final Serializable employeeResult = getProcessAPI().evaluateExpressionsOnActivityInstance(step1Id,
                Collections.<Expression, Map<String, Serializable>> singletonMap(employee, null)).get("script");
        assertThat(employeeResult).isEqualTo("theValue");
        getProcessAPI().assignUserTask(step1Id, testUser.getId());
        getProcessAPI().executeFlowNode(step1Id);
        waitForUserTask(instance, "step2");

        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(subProcessDefinition);
    }

    @Test
    public void should_return_the_list_of_entities_from_the_multiple_instance() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression(
                "createNewEmployees",
                new StringBuilder().append("import ").append(EMPLOYEE_QUALIFIED_NAME)
                        .append("; Employee john = new Employee(); john.firstName = 'John'; john.lastName = 'Doe';")
                        .append(" Employee jane = new Employee(); jane.firstName = 'Jane'; jane.lastName = 'Doe';")
                        .append(" Employee rambo = new Employee(); rambo.firstName = 'John'; rambo.lastName = 'Rambo'; [jane, john, rambo]")
                        .toString(),
                List.class.getName());


        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("MBIMI", "1.2-beta");
        builder.addBusinessData("myEmployees", EMPLOYEE_QUALIFIED_NAME, employeeExpression).setMultiple(true);
        builder.addData("names", List.class.getName(), null);
        builder.addBusinessData("firstNames", EMPLOYEE_QUALIFIED_NAME, null).setMultiple(true);
        builder.addContextEntry("firstNames_ref",
                new ExpressionBuilder().createBusinessDataReferenceExpression("firstNames"));
        builder.addActor(ACTOR_NAME);
        final UserTaskDefinitionBuilder userTaskBuilder = builder.addUserTask("step1", ACTOR_NAME);
        userTaskBuilder.addBusinessData("employee", EMPLOYEE_QUALIFIED_NAME);
        userTaskBuilder.addShortTextData("name", null);
        userTaskBuilder.addMultiInstance(false, "myEmployees").addDataInputItemRef("employee")
                .addDataOutputItemRef("name").addLoopDataOutputRef("names");
        userTaskBuilder.addOperation(new OperationBuilder().createSetDataOperation("name",
                new ExpressionBuilder().createConstantStringExpression("Doe")));
        UserTaskDefinitionBuilder step2Builder = builder.addUserTask("step2", ACTOR_NAME);
        step2Builder.addOperation(new OperationBuilder().attachBusinessDataSetAttributeOperation("firstNames",
                new ExpressionBuilder()
                .createQueryBusinessDataExpression("findFirstNames", "Employee." + FIND_EMPLOYEE_WITH_FIRSTNAMES,
                        List.class.getName(),
                                new ExpressionBuilder().createGroovyScriptExpression("firstNames",
                                        "['John'] as String[]",
                                        String[].class.getName()),
                new ExpressionBuilder().createExpression("startIndex", "0", Integer.class.getName(),
                        ExpressionType.TYPE_CONSTANT),
                new ExpressionBuilder().createExpression("maxResults", "10", Integer.class.getName(),
                                ExpressionType.TYPE_CONSTANT))));
        builder.addUserTask("step3", ACTOR_NAME);
        builder.addTransition("step1", "step2");
        builder.addTransition("step2", "step3");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, testUser);

        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(instance, "step1", testUser);
        waitForUserTaskAndExecuteIt(instance, "step1", testUser);
        waitForUserTaskAndExecuteIt(instance, "step1", testUser);
        waitForUserTaskAndExecuteIt(instance, "step2", testUser);
        waitForUserTask(instance, "step3");

        final DataInstance namesDataInstance = getProcessAPI().getProcessDataInstance("names", instance.getId());
        assertThat(namesDataInstance.getValue().toString()).isEqualTo("[Doe, Doe, Doe]");

        final Serializable firstNamesDataInstance = getProcessAPI().getProcessInstanceExecutionContext(instance.getId())
                .get("firstNames_ref");

        //Only employee with firstname == john
        assertThat(((MultipleBusinessDataReference) firstNamesDataInstance).getStorageIds()).hasSize(2);

        final Map<String, Serializable> employee = getProcessAPI().evaluateExpressionsOnProcessInstance(
                instance.getId(),
                Collections.singletonMap(new ExpressionBuilder().createBusinessDataReferenceExpression("myEmployees"),
                        Collections.<String, Serializable> emptyMap()));
        assertThat(employee).hasSize(1);
        assertThat(employee.get("myEmployees")).isInstanceOf(MultipleBusinessDataReference.class);
        final MultipleBusinessDataReference myEmployees = (MultipleBusinessDataReference) employee.get("myEmployees");
        assertThat(myEmployees.getName()).isEqualTo("myEmployees");
        assertThat(myEmployees.getType()).isEqualTo(EMPLOYEE_QUALIFIED_NAME);
        assertThat(myEmployees.getStorageIds()).hasSize(3);

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

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(
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

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, testUser);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "step2");

        final SimpleBusinessDataReference businessDataReference = (SimpleBusinessDataReference) getBusinessDataAPI().getProcessBusinessDataReference(
                bizDataName, processInstance.getId());

        verifyCommandGetBusinessDataById(businessDataReference);
        verifyCommandGetBusinessDataByIds(businessDataReference);
        verifyCommandGetQuery_findByFirstNameAndLastNameNewOrder();
        verifyCommandGetQuery_getEmployeeByPhoneNumber();
        verifyCommandGetQuery_findByFirstNameFetchAddresses();
        verifyCommandGetQuery_countEmployee();
        verifyCommandGetQuery_findByHireDate();

        disableAndDeleteProcess(processDefinition.getId());
    }

    private void verifyCommandGetBusinessDataByIds(final SimpleBusinessDataReference businessDataReference) throws Exception {
        final List<Long> ids = new ArrayList<>();
        ids.add(businessDataReference.getStorageId());
        final Map<String, Serializable> parameters = new HashMap<>();
        parameters.put("businessDataIds", (Serializable) ids);
        parameters.put("entityClassName", EMPLOYEE_QUALIFIED_NAME);
        parameters.put("businessDataURIPattern", "/businessdata/{className}/{id}/{field}");

        // when
        final String lazyAddressResultWithChildName = (String) getCommandAPI().execute("getBusinessDataByIds", parameters);

        // then
        assertThatJson(lazyAddressResultWithChildName).as("should get address with lazy link to country")
                .hasSameStructureAs(getJsonContent("getBusinessDataByIdsEmployee.json"));
    }

    private void verifyCommandGetBusinessDataById(final SimpleBusinessDataReference businessDataReference) throws Exception {
        final Map<String, Serializable> parameters = new HashMap<>();
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
        final Map<String, Serializable> parameters = new HashMap<>();
        final Map<String, Serializable> queryParameters = new HashMap<>();

        queryParameters.put("firstName", "Alphonse");
        queryParameters.put("lastName", "Dupond");

        parameters.put("queryName", FIND_BY_FIRST_NAME_AND_LAST_NAME_NEW_ORDER);
        parameters.put(ENTITY_CLASS_NAME, EMPLOYEE_QUALIFIED_NAME);
        parameters.put("startIndex", 0);
        parameters.put("maxResults", 10);
        parameters.put("businessDataURIPattern", "/businessdata/{className}/{id}/{field}");
        parameters.put("queryParameters", (Serializable) queryParameters);

        // when
        ((BusinessDataQueryResult) getCommandAPI().execute("getBusinessDataByQueryCommand", parameters)).getJsonResults();
        getCommandAPI().addDependency("temporaryDeps" + iterator, new byte[] { 0, 1 });
        iterator++;
        final Serializable jsonResult = ((BusinessDataQueryResult) getCommandAPI().execute("getBusinessDataByQueryCommand", parameters)).getJsonResults();

        // then
        assertThatJson(jsonResult).as("should get employee").hasSameStructureAs(getJsonContent("findByFirstNameAndLastNameNewOrder.json"));

    }

    private void verifyCommandGetQuery_getEmployeeByPhoneNumber() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<>();
        final Map<String, Serializable> queryParameters = new HashMap<>();

        queryParameters.put("phoneNumber", "123456789");

        parameters.put("queryName", GET_EMPLOYEE_BY_PHONE_NUMBER_QUERY_NAME);
        parameters.put(ENTITY_CLASS_NAME, EMPLOYEE_QUALIFIED_NAME);
        parameters.put("startIndex", 0);
        parameters.put("maxResults", 10);
        parameters.put("businessDataURIPattern", BUSINESS_DATA_CLASS_NAME_ID_FIELD);
        parameters.put("queryParameters", (Serializable) queryParameters);

        // when
        final BusinessDataQueryResultImpl businessDataQueryResult = (BusinessDataQueryResultImpl) getCommandAPI().execute("getBusinessDataByQueryCommand",
                parameters);
        final String jsonResult = (String) businessDataQueryResult.getJsonResults();

        // then
        assertThatJson(jsonResult).as("should get employee").hasSameStructureAs(getJsonContent("getEmployeeByPhoneNumber.json"));

    }

    private void verifyCommandGetQuery_findByFirstNameFetchAddresses() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<>();
        final Map<String, Serializable> queryParameters = new HashMap<>();

        queryParameters.put("firstName", "Alphonse");

        parameters.put("queryName", FIND_BY_FIRST_NAME_FETCH_ADDRESSES);
        parameters.put(ENTITY_CLASS_NAME, EMPLOYEE_QUALIFIED_NAME);
        parameters.put("startIndex", 0);
        parameters.put("maxResults", 10);
        parameters.put("businessDataURIPattern", BUSINESS_DATA_CLASS_NAME_ID_FIELD);
        parameters.put("queryParameters", (Serializable) queryParameters);

        // when
        final BusinessDataQueryResult businessDataQueryResult = (BusinessDataQueryResult) getCommandAPI().execute("getBusinessDataByQueryCommand", parameters);

        // then
        assertThat(businessDataQueryResult.getBusinessDataQueryMetadata()).as("should have no metadata when custom countFor is not here").isNull();
        assertThatJson(businessDataQueryResult.getJsonResults()).as("should get employee")
                .hasSameStructureAs(getJsonContent("findByFirstNameFetchAddresses.json"));

    }

    private void verifyCommandGetQuery_countEmployee() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<>();

        parameters.put("queryName", COUNT_EMPLOYEE);
        parameters.put(ENTITY_CLASS_NAME, EMPLOYEE_QUALIFIED_NAME);
        parameters.put("startIndex", 0);
        parameters.put("maxResults", 10);
        parameters.put("businessDataURIPattern", BUSINESS_DATA_CLASS_NAME_ID_FIELD);

        // when
        final BusinessDataQueryResult businessDataQueryResult = (BusinessDataQueryResult) getCommandAPI().execute("getBusinessDataByQueryCommand", parameters);

        // then
        assertThatJson(businessDataQueryResult.getJsonResults()).as("should get employee count ").isEqualTo(getJsonContent("countEmployee.json"));

    }

    private void verifyCommandGetQuery_findByHireDate() throws Exception {
        final Map<String, Serializable> parameters = new HashMap<>();
        final Map<String, Serializable> queryParameters = new HashMap<>();
        queryParameters.put("date1", "1930-01-15");
        queryParameters.put("date2", "2050-12-31");

        parameters.put("queryName", FIND_BY_HIRE_DATE_RANGE);
        parameters.put(ENTITY_CLASS_NAME, EMPLOYEE_QUALIFIED_NAME);
        parameters.put("startIndex", 0);
        parameters.put("maxResults", 10);
        parameters.put("businessDataURIPattern", BUSINESS_DATA_CLASS_NAME_ID_FIELD);
        parameters.put("queryParameters", (Serializable) queryParameters);

        // when
        final BusinessDataQueryResult businessDataQueryResult = (BusinessDataQueryResult) getCommandAPI().execute("getBusinessDataByQueryCommand", parameters);

        // then
        assertThatJson(businessDataQueryResult.getJsonResults()).as("should get employee").hasSameStructureAs(getJsonContent("findByHireDate.json"));
        final BusinessDataQueryMetadata businessDataQueryMetadata = businessDataQueryResult.getBusinessDataQueryMetadata();
        assertThat(businessDataQueryMetadata).as("should have metadata").isNotNull();
        assertThat(businessDataQueryMetadata.getCount()).isEqualTo(1L);
        assertThat(businessDataQueryMetadata.getStartIndex()).isEqualTo(0);
        assertThat(businessDataQueryMetadata.getMaxResults()).isEqualTo(10);

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
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addOperation(
                new OperationBuilder().createBusinessDataSetAttributeOperation("myEmployees", "addAll", "java.util.Collection", employeeExpression));
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, testUser);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());

        final long step1Id = waitForUserTask(instance, "step1");
        String employeeToString = getEmployeesToString("myEmployees", instance.getId());
        assertThat(firstNames(employeeToString)).isEmpty();
        assertThat(lastNames(employeeToString)).isEmpty();

        assignAndExecuteStep(step1Id, testUser);
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
                        .append(" Product p2 = new Product(); p2.name = 'Paper'; ").append(" return [p1, p2];").toString(),
                List.class.getName());

        final Expression productDependency = new ExpressionBuilder().createBusinessDataExpression("products", List.class.getName());

        final Expression initCatalogs = new ExpressionBuilder().createGroovyScriptExpression(
                "initCatalogs",
                new StringBuilder().append("import ").append(PRODUCT_CATALOG_QUALIFIED_NAME).append(";")
                        .append(" ProductCatalog pc = new ProductCatalog(); pc.name = 'MyFirstCatalog'; pc.setProducts(products);").append(" return [pc];")
                        .toString(),
                List.class.getName(), productDependency);

        final Expression catalogDependency = new ExpressionBuilder().createBusinessDataExpression("productCatalogs", List.class.getName());

        final Expression nbOfProducts = new ExpressionBuilder().createGroovyScriptExpression("nbOfProducts",
                new StringBuilder().append("import ").append(PRODUCT_CATALOG_QUALIFIED_NAME).append(";").append(" productCatalogs.get(0).getProducts().size()")
                        .toString(),
                Integer.class.getName(), catalogDependency);

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

        final ProcessDefinition definition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, testUser);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        waitForUserTaskAndExecuteIt(processInstance, "next", testUser);

        disableAndDeleteProcess(definition.getId());
    }

    @Test
    public void should_update_composition_entities() throws Exception {
        final StringBuilder initCatalog = new StringBuilder();
        initCatalog.append("import ").append(PRODUCT_CATALOG_QUALIFIED_NAME).append("\n");
        initCatalog.append("import ").append("com.company.model.Edition").append("\n");
        initCatalog.append("Edition edition = new Edition() \n");
        initCatalog.append("edition.releaseYear = '2015' \n");
        initCatalog.append("ProductCatalog pc = new ProductCatalog() \n");
        initCatalog.append("pc.name = 'MyFirstCatalog' \n");
        initCatalog.append("pc.setEditions([edition]) \n");
        initCatalog.append("pc\n");

        final Expression initCatalogExpression = new ExpressionBuilder().createGroovyScriptExpression("initCatalog", initCatalog.toString(),
                PRODUCT_CATALOG_QUALIFIED_NAME);

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("compo", "8.2");
        builder.addActor(ACTOR_NAME);
        builder.addBusinessData("productCatalog", PRODUCT_CATALOG_QUALIFIED_NAME, initCatalogExpression);
        builder.addUserTask("updateCatalog", ACTOR_NAME)
                .addOperation(
                        new OperationBuilder().createBusinessDataSetAttributeOperation("productCatalog", "setName", String.class.getName(),
                                new ExpressionBuilder().createConstantStringExpression("myUdaptedCatalog")));
        builder.addUserTask("unreferenceCatalog", ACTOR_NAME)
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand("productCatalog"), OperatorType.ASSIGNMENT, null, null,
                        new ExpressionBuilder().createGroovyScriptExpression("nullExpression", "null", PRODUCT_CATALOG_QUALIFIED_NAME));
        builder.addUserTask("result", ACTOR_NAME);
        builder.addTransition("updateCatalog", "unreferenceCatalog");
        builder.addTransition("unreferenceCatalog", "result");

        final ProcessDefinition definition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, testUser);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        waitForUserTaskAndExecuteIt("updateCatalog", testUser);
        waitForUserTaskAndExecuteIt("unreferenceCatalog", testUser);
        waitForUserTask(processInstance, "result");

        final SimpleBusinessDataReference businessDataReference = (SimpleBusinessDataReference) getBusinessDataAPI().getProcessBusinessDataReference(
                "productCatalog", processInstance.getId());
        assertThat(businessDataReference.getStorageId()).isNull();
        assertThat(businessDataReference.getStorageIdAsString()).isNull();

        disableAndDeleteProcess(definition.getId());
    }

    private String getClientBdmJarClassPath(final String bonitaHomePath) {
        return new StringBuilder().append(bonitaHomePath).append(File.separator).append("engine-server").append(File.separator).append("work")
                .append(File.separator).append("tenants").append(File.separator).append(tenantId).append(File.separator).append("data-management-client")
                .toString();
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

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, testUser);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        waitForUserTask(processInstance, "step2");

        final Long numberOfAddresses = getNumberOfAddresses(processInstance.getId());
        assertThat(numberOfAddresses).isEqualTo(1L);
        final String address = getAddressAsAString("myAddress", processInstance.getId());
        assertThat(address).isEqualTo("Address [street=32, rue Gustave Eiffel, city=Grenoble]");

        final SimpleBusinessDataReference businessDataReference = (SimpleBusinessDataReference) getBusinessDataAPI().getProcessBusinessDataReference(
                bizDataName, processInstance.getId());

        final Expression idExpression = new ExpressionBuilder().createExpression("id", String.valueOf(businessDataReference.getStorageId()),
                Long.class.getName(), ExpressionType.TYPE_CONSTANT);
        final Expression getEmployeeExpression = new ExpressionBuilder().createQueryBusinessDataExpression("getEmployee", "Employee.findByPersistId",
                EMPLOYEE_QUALIFIED_NAME, idExpression);
        final ProcessDefinitionBuilder processDefBuilder = new ProcessDefinitionBuilder().createNewInstance("init", "3.2");
        processDefBuilder.addBusinessData(bizDataName, EMPLOYEE_QUALIFIED_NAME, getEmployeeExpression);
        processDefBuilder.addActor(ACTOR_NAME);
        processDefBuilder.addUserTask("step2", ACTOR_NAME);

        final ProcessDefinition definition2 = deployAndEnableProcessWithActor(processDefBuilder.done(), ACTOR_NAME, testUser);
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(definition2.getId());
        waitForUserTask(processInstance2, "step2");

        disableAndDeleteProcess(definition.getId());
        disableAndDeleteProcess(definition2.getId());
    }

    @Test
    public void should_associate_the_right_addresses() throws Exception {
        final Expression addressExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewAddress",
                "import " + ADDRESS_QUALIFIED_NAME + "; new Address(street:'32, rue Gustave Eiffel', city:'Grenoble')",
                ADDRESS_QUALIFIED_NAME);
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", "import " + EMPLOYEE_QUALIFIED_NAME
                + ";import java.time.LocalDate;LocalDate localDate = LocalDate.of(1984,10,24); Employee e = new Employee(firstName:'John', lastName:'Doe', addresses:[myAddress]);e.birthDate = localDate; return e; ",
                EMPLOYEE_QUALIFIED_NAME,
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

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, testUser);
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        final long userTaskId = waitForUserTask(processInstance, "step2");

        Long numberOfAddresses = getNumberOfAddresses(processInstance.getId());
        assertThat(numberOfAddresses).isEqualTo(1L);
        String address = getAddressAsAString("myAddress", processInstance.getId());
        assertThat(address).isEqualTo("Address [street=32, rue Gustave Eiffel, city=Grenoble]");

        assignAndExecuteStep(userTaskId, testUser);
        waitForUserTask(processInstance, "step3");

        numberOfAddresses = getNumberOfAddresses(processInstance.getId());
        assertThat(numberOfAddresses).isEqualTo(1L);
        address = getAddressAsAString("myAddress", processInstance.getId());
        assertThat(address).isEqualTo("Address [street=32, rue Gustave Eiffel, city=Grenoble]");
        final String employee = getEmployeeAsAString(bizDataName, processInstance.getId());
        assertThat(employee).isEqualTo("Employee [firstName=John, lastName=Smith, address=null, addresses.count=2, birthDate = 1984-10-24 ]");
        disableAndDeleteProcess(definition.getId());
    }

    @Test
    public void should_initialize_a_bo_with_empty_query_result() throws Exception {
        //given
        final Expression queryBusinessDataExpression = new ExpressionBuilder().createQueryBusinessDataExpression("findQuery",
                "Employee." + GET_EMPLOYEE_BY_LAST_NAME_QUERY_NAME, EMPLOYEE_QUALIFIED_NAME,
                new ExpressionBuilder().createConstantStringExpression("lastName", "notExists"));

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(
                "emptyQueryResult", "1.0");
        final String bizDataName = "myEmployee";
        processDefinitionBuilder.addBusinessData(bizDataName, EMPLOYEE_QUALIFIED_NAME, queryBusinessDataExpression);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, testUser);

        //when
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        waitForUserTask(processInstance, "step1");

        //then
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<>();
        final String expressionEmployee = "retrieve_Employee";
        expressions.put(
                new ExpressionBuilder().createGroovyScriptExpression(expressionEmployee,
                        " if (" + bizDataName + "==null) { return Boolean.TRUE } else {return Boolean.FALSE} ",
                        Boolean.class.getName(),
                        new ExpressionBuilder().createBusinessDataExpression(bizDataName, EMPLOYEE_QUALIFIED_NAME)),
                null);

        final Map<String, Serializable> evaluatedExpressions = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstance.getId(), expressions);
        assertThat(evaluatedExpressions).as("should not have a reference to business data").contains(entry(expressionEmployee, Boolean.TRUE));

        disableAndDeleteProcess(definition.getId());
    }

    @Test
    public void should_initialize_a_multiple_bo_with_empty_query_result() throws Exception {
        //given
        final ExpressionImpl dependencyStartIndex = new ExpressionImpl();
        dependencyStartIndex.setExpressionType(ExpressionType.TYPE_CONSTANT.name());
        dependencyStartIndex.setName("startIndex");
        dependencyStartIndex.setReturnType(Integer.class.getName());
        dependencyStartIndex.setContent("0");

        final ExpressionImpl dependencyMaxResults = new ExpressionImpl();
        dependencyMaxResults.setExpressionType(ExpressionType.TYPE_CONSTANT.name());
        dependencyMaxResults.setName("maxResults");
        dependencyMaxResults.setReturnType(Integer.class.getName());
        dependencyMaxResults.setContent("10");

        final Expression queryBusinessDataExpression = new ExpressionBuilder().createQueryBusinessDataExpression("findQuery",
                "Employee.find", List.class.getName(),
                dependencyStartIndex, dependencyMaxResults);

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(
                "multipleEmptyQueryResult", "1.0");
        final String bizDataName = "myEmployees";
        processDefinitionBuilder.addBusinessData(bizDataName, EMPLOYEE_QUALIFIED_NAME, queryBusinessDataExpression).setMultiple(true);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, testUser);

        //when
        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        waitForUserTask(processInstance, "step1");

        //then
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<>();
        final String expressionEmployee = "retrieve_Employee";
        expressions.put(
                new ExpressionBuilder().createGroovyScriptExpression(expressionEmployee,
                        bizDataName + ".toString()",
                        String.class.getName(),
                        new ExpressionBuilder().createBusinessDataExpression(bizDataName, List.class.getName())),
                null);

        final Map<String, Serializable> evaluatedExpressions = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstance.getId(), expressions);
        assertThat(evaluatedExpressions).as("should not have a reference to business data").contains(entry(expressionEmployee, "[]"));

        disableAndDeleteProcess(definition.getId());
    }

    public Long getNumberOfAddresses(final long processInstanceId) throws Exception {
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<>(2);
        expressions.put(new ExpressionBuilder().createQueryBusinessDataExpression("countAddresses", "Address.countAddress", Long.class.getName()),
                Collections.<String, Serializable> emptyMap());

        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
        return (Long) result.get("countAddresses");
    }

    public String getAddressAsAString(final String addressName, final long processInstanceId) throws Exception {
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<>(2);
        expressions.put(
                new ExpressionBuilder().createGroovyScriptExpression("getAddress", "\"Address [street=\" + " + addressName
                        + ".street + \", city=\" + " + addressName + ".city + \"]\";", String.class.getName(),
                        new ExpressionBuilder().createBusinessDataExpression(addressName, ADDRESS_QUALIFIED_NAME)),
                null);
        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
        return (String) result.get("getAddress");
    }

    private String getEmployeeAsAString(final String businessDataName, final long processInstanceId) throws InvalidExpressionException {
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<>(5);
        final String expressionEmployee = "retrieve_Employee";
        expressions.put(
                new ExpressionBuilder().createGroovyScriptExpression(expressionEmployee,
                        "\"Employee [firstName=\" + " + businessDataName + ".firstName + \", lastName=\" + " + businessDataName
                                + ".lastName + \", address=\" + " + businessDataName + ".address + \", addresses.count=\" + "
                                + businessDataName + ".addresses.size() + \", birthDate = \"+" + businessDataName + ".birthDate + \" ]\";",
                        String.class.getName(),
                        new ExpressionBuilder().createBusinessDataExpression(businessDataName, EMPLOYEE_QUALIFIED_NAME)),
                null);
        try {
            final Map<String, Serializable> evaluatedExpressions = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
            return (String) evaluatedExpressions.get(expressionEmployee);
        } catch (final ExpressionEvaluationException eee) {
            System.err.println(eee.getMessage());
            return null;
        }
    }

    @Test
    public void evaluate_context_on_process_and_task() throws Exception {
        final ProcessDefinitionBuilder p1Builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithContext", "1.0");
        final Expression bizDataValue = new ExpressionBuilder()
                .createGroovyScriptExpression("createNewEmployee",
                        "import " + EMPLOYEE_QUALIFIED_NAME + "; Employee e = new Employee(); e.firstName = 'Jane'; e.lastName = 'Doe'; return e;",
                        EMPLOYEE_QUALIFIED_NAME);
        p1Builder.addBusinessData("bizData", EMPLOYEE_QUALIFIED_NAME, bizDataValue);
        p1Builder.addDocumentDefinition("myDoc").addFile("myDoc.txt").addContentFileName("myDoc.txt");

        final Expression bizData = new ExpressionBuilder().createBusinessDataExpression("bizData", EMPLOYEE_QUALIFIED_NAME);
        p1Builder.addContextEntry("process_key1",
                new ExpressionBuilder().createGroovyScriptExpression("retrieve_firstname", "bizData.firstName", String.class.getName(), bizData));
        final UserTaskDefinitionBuilder task1 = p1Builder.addUserTask("step1", "actor");
        task1.addShortTextData("task1Data", new ExpressionBuilder().createConstantStringExpression("task1DataValue"));
        task1.addContextEntry("task_key1", new ExpressionBuilder().createDataExpression("task1Data", String.class.getName()));
        task1.addContextEntry("task_key2", new ExpressionBuilder().createConstantStringExpression("constantValue"));
        task1.addContextEntry("processBizDataFromTask1",
                new ExpressionBuilder().createGroovyScriptExpression("retrieve_firstname", "bizData.lastName", String.class.getName(), bizData));
        task1.addContextEntry("doc_key", new ExpressionBuilder().createGroovyScriptExpression("doc.name", "myDoc.fileName", String.class.getName(),
                new ExpressionBuilder().createDocumentReferenceExpression("myDoc")));
        UserTaskDefinitionBuilder task2 = p1Builder.addUserTask("step2", "actor");
        task2.addShortTextData("task2Data", new ExpressionBuilder().createConstantStringExpression("task2DataValue"));
        p1Builder.addActor("actor");
        final String myDocumentContent = "Some document content";
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(p1Builder.getProcess())
                .addDocumentResource(new BarResource("myDoc.txt", myDocumentContent.getBytes())).done();
        ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchive, "actor", testUser);
        ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        long step1 = waitForUserTask(processInstance1.getId(), "step1");
        long step2 = waitForUserTask(processInstance1.getId(), "step2");

        assertThat(getProcessAPI().getProcessInstanceExecutionContext(processInstance1.getId())).containsOnly(entry("process_key1", "Jane"));
        assertThat(getProcessAPI().getUserTaskExecutionContext(step1)).containsOnly(entry("task_key1", "task1DataValue"), entry("task_key2", "constantValue"),
                entry("processBizDataFromTask1", "Doe"), entry("doc_key", "myDoc.txt"));
        assertThat(getProcessAPI().getUserTaskExecutionContext(step2)).isEmpty();

        assignAndExecuteStep(step1, testUser.getId());
        assignAndExecuteStep(step2, testUser.getId());
        waitForProcessToFinish(processInstance1);
        Thread.sleep(10);
        final ArchivedProcessInstance finalArchivedProcessInstance = getProcessAPI().getFinalArchivedProcessInstance(processInstance1.getId());
        final ArchivedActivityInstance archivedStep1 = getProcessAPI().getArchivedActivityInstance(step1);
        final ArchivedActivityInstance archivedStep2 = getProcessAPI().getArchivedActivityInstance(step2);
        assertThat(getProcessAPI().getArchivedProcessInstanceExecutionContext(finalArchivedProcessInstance.getId()))
                .containsOnly(entry("process_key1", "Jane"));
        assertThat(getProcessAPI().getArchivedUserTaskExecutionContext(archivedStep1.getId())).containsOnly(entry("task_key1", "task1DataValue"),
                entry("task_key2", "constantValue"), entry("processBizDataFromTask1", "Doe"), entry("doc_key", "myDoc.txt"));
        assertThat(getProcessAPI().getArchivedUserTaskExecutionContext(archivedStep2.getId())).isEmpty();

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void should_event_sub_process_only_start_element_in_the_event_sub_process() throws Exception {
        /*
         * We test here that an event sub process instantiation do nothing on the parent process
         * see bug BS-15123 and BS-15275
         */
        //given
        ProcessDefinitionBuilder parentProcessBuilder = new ProcessDefinitionBuilder().createNewInstance("ParentProcessWithSignalEventSubProcess", "1.0");
        parentProcessBuilder.addActor(ACTOR_NAME);
        parentProcessBuilder.addAutomaticTask("updateTask").addOperation(new OperationBuilder().createSetDocument("myDoc",
                new ExpressionBuilder().createGroovyScriptExpression("updateDocContent",
                        "import org.bonitasoft.engine.bpm.document.DocumentValue;return new DocumentValue('updatedContents'.getBytes(),'plain/text','myDoc.txt');",
                        DocumentValue.class.getName())));
        parentProcessBuilder.addUserTask("userTask", ACTOR_NAME);
        parentProcessBuilder.addTransition("updateTask", "userTask");
        parentProcessBuilder.addContract().addInput("employeeName", Type.TEXT, "the name of the business data");
        parentProcessBuilder.addBusinessData("myBusinessData", EMPLOYEE_QUALIFIED_NAME,
                new ExpressionBuilder().createGroovyScriptExpression("initBD",
                        "import " + EMPLOYEE_QUALIFIED_NAME + "\n" +
                                "Employee e = new Employee(); e.firstName = 'Jules'; e.lastName = employeeName; return e;",
                        EMPLOYEE_QUALIFIED_NAME,
                        new ExpressionBuilder().createContractInputExpression("employeeName", String.class.getName())));
        parentProcessBuilder.addShortTextData("textData", new ExpressionBuilder().createConstantStringExpression("parentVar"));
        parentProcessBuilder.addIntegerData("intData", new ExpressionBuilder().createConstantIntegerExpression(1));
        parentProcessBuilder.addDocumentDefinition("myDoc").addInitialValue(new ExpressionBuilder().createGroovyScriptExpression("updateDocContent",
                "import org.bonitasoft.engine.bpm.document.DocumentValue;return new DocumentValue('initialContent'.getBytes(),'plain/text','myDoc.txt');",
                DocumentValue.class.getName()));
        parentProcessBuilder.addDocumentListDefinition("MyList").addInitialValue(new ExpressionBuilder().createGroovyScriptExpression("updateDocContent",
                "import org.bonitasoft.engine.bpm.document.DocumentValue;return [new DocumentValue('initialContent'.getBytes(),'plain/text','myDoc1.txt'), new DocumentValue('initialContent'.getBytes(),'plain/text','myDoc2.txt')];",
                List.class.getName()));
        //construct sub process
        SubProcessDefinitionBuilder subProcessBuilder = parentProcessBuilder.addSubProcess("interruptWithSignalProcess", true).getSubProcessBuilder();
        StartEventDefinitionBuilder startEventDefinitionBuilder = subProcessBuilder.addStartEvent("signalStart");
        startEventDefinitionBuilder.addSignalEventTrigger("theSignal");
        subProcessBuilder.addUserTask("userTaskInSubProcess", ACTOR_NAME);
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition("signalStart", "userTaskInSubProcess");
        subProcessBuilder.addTransition("userTaskInSubProcess", "endSubProcess");
        subProcessBuilder.addShortTextData("textDataInSub", new ExpressionBuilder().createConstantStringExpression("childVar"));
        subProcessBuilder.addDoubleData("value", new ExpressionBuilder().createConstantDoubleExpression(10.0));
        DesignProcessDefinition processDefinition1 = parentProcessBuilder.done();
        BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinition1);
        ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchiveBuilder.done(), ACTOR_NAME, testUser);
        ProcessInstance processInstance = getProcessAPI().startProcessWithInputs(processDefinition.getId(),
                Collections.<String, Serializable> singletonMap("employeeName", "Doe"));
        //when
        waitForUserTask("userTask");
        assertThat(new String(getProcessAPI().getDocumentContent(getProcessAPI().getLastDocument(processInstance.getId(), "myDoc").getContentStorageId())))
                .isEqualTo("updatedContents");

        getProcessAPI().sendSignal("theSignal");
        //then
        ActivityInstance eventSubProcessActivity = getProcessAPI().getActivityInstance(waitForUserTask("userTaskInSubProcess"));
        //instantiation of the event sub process work and did not reinitialized elements
        assertThat(new String(getProcessAPI().getDocumentContent(getProcessAPI().getLastDocument(processInstance.getId(), "myDoc").getContentStorageId())))
                .isEqualTo("updatedContents");
        assertThat(getProcessAPI().getDocumentList(processInstance.getId(), "MyList", 0, 100)).hasSize(2);
        try {
            getProcessAPI().getLastDocument(eventSubProcessActivity.getParentProcessInstanceId(), "myDoc");
            fail("should not be found");
        } catch (DocumentNotFoundException ignored) {
        }
        assertThat(getProcessAPI().getDocumentList(eventSubProcessActivity.getParentProcessInstanceId(), "MyList", 0, 100)).isEmpty();
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void should_be_able_to_update_business_object_in_event_sub_process() throws Exception {
        //given
        ProcessDefinitionBuilder parentProcessBuilder = new ProcessDefinitionBuilder().createNewInstance("UpdateBusinessDataInEventSubProcess", "1.0");
        parentProcessBuilder.addActor(ACTOR_NAME);
        parentProcessBuilder.addUserTask("userTask", ACTOR_NAME);
        parentProcessBuilder.addContextEntry("ref_myBusinessData", new ExpressionBuilder().createBusinessDataReferenceExpression("myBusinessData"));
        parentProcessBuilder.addContract().addInput("employeeName", Type.TEXT, "the name of the business data");
        parentProcessBuilder.addBusinessData("myBusinessData", EMPLOYEE_QUALIFIED_NAME,
                new ExpressionBuilder().createGroovyScriptExpression("initBD",
                        "import " + EMPLOYEE_QUALIFIED_NAME + "\n" +
                                "Employee e = new Employee(); e.firstName = 'Jules'; e.lastName = employeeName; return e;",
                        EMPLOYEE_QUALIFIED_NAME,
                        new ExpressionBuilder().createContractInputExpression("employeeName", String.class.getName())));
        //construct sub process
        SubProcessDefinitionBuilder subProcessBuilder = parentProcessBuilder.addSubProcess("updateBusinessData", true).getSubProcessBuilder();
        StartEventDefinitionBuilder startEventDefinitionBuilder = subProcessBuilder.addStartEvent("signalStart");
        startEventDefinitionBuilder.addSignalEventTrigger("theSignal");
        subProcessBuilder.addAutomaticTask("updateBD")
                .addOperation(new OperationBuilder().createBusinessDataSetAttributeOperation("myBusinessData", "setLastName", String.class.getName(),
                        new ExpressionBuilder().createConstantStringExpression("newName")));
        subProcessBuilder.addUserTask("userTaskInSubProcess", ACTOR_NAME);
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition("signalStart", "updateBD");
        subProcessBuilder.addTransition("updateBD", "userTaskInSubProcess");
        subProcessBuilder.addTransition("userTaskInSubProcess", "endSubProcess");
        DesignProcessDefinition processDefinition1 = parentProcessBuilder.done();
        BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinition1);
        ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchiveBuilder.done(), ACTOR_NAME, testUser);
        ProcessInstance processInstance = getProcessAPI().startProcessWithInputs(processDefinition.getId(),
                Collections.<String, Serializable> singletonMap("employeeName", "Doe"));
        waitForUserTask("userTask");
        assertThatJson(getBusinessDataAsJson((SimpleBusinessDataReference) getProcessAPI().getProcessInstanceExecutionContext(
                processInstance.getId()).get("ref_myBusinessData")))
                        .node("lastName").isEqualTo("\"Doe\"");
        //when
        getProcessAPI().sendSignal("theSignal");
        waitForUserTask("userTaskInSubProcess");

        //then
        assertThatJson(getBusinessDataAsJson((SimpleBusinessDataReference) getProcessAPI().getProcessInstanceExecutionContext(
                processInstance.getId()).get("ref_myBusinessData")))
                        .node("lastName").isEqualTo("\"newName\"");
        disableAndDeleteProcess(processDefinition);
    }

    private String getBusinessDataAsJson(SimpleBusinessDataReference myBusinessData)
            throws CommandNotFoundException, CommandParameterizationException, CommandExecutionException {
        final Map<String, Serializable> parameters = new HashMap<>();
        parameters.put("businessDataId", myBusinessData.getStorageId());
        parameters.put("entityClassName", EMPLOYEE_QUALIFIED_NAME);
        parameters.put("businessDataURIPattern", "/businessdata/{className}/{id}/{field}");
        return (String) getCommandAPI().execute("getBusinessDataById", parameters);
    }

    @Test
    public void shouldRetrieveBDMObjectsInLeftOperandsInCatchMessages() throws Exception {

        ProcessDefinitionBuilder throwProcessBuilder = new ProcessDefinitionBuilder().createNewInstance("MSG", "1.0");
        throwProcessBuilder.addStartEvent("startEvent");
        throwProcessBuilder.addActor(ACTOR_NAME);
        IntermediateThrowEventDefinitionBuilder intermediateThrowEvent = throwProcessBuilder.addIntermediateThrowEvent("sendMessage");
        Expression targetProcessExpression = new ExpressionBuilder().createConstantStringExpression("BDM");
        Expression targetFlowNodeExpression = new ExpressionBuilder().createConstantStringExpression("message1");
        ThrowMessageEventTriggerBuilder messageEventTriggerBuilder = intermediateThrowEvent.addMessageEventTrigger("msg_name", targetProcessExpression,
                targetFlowNodeExpression);
        messageEventTriggerBuilder.addMessageContentExpression(new ExpressionBuilder().createConstantStringExpression("msg_name"),
                new ExpressionBuilder().createConstantStringExpression("fabrice"));
        throwProcessBuilder.addTransition("startEvent", "sendMessage");
        DesignProcessDefinition designThrowProcessDefinition = throwProcessBuilder.done();
        ProcessDefinitionBuilder catchProcessBuilder = new ProcessDefinitionBuilder().createNewInstance("BDM", "1.0");
        catchProcessBuilder.addStartEvent("startEvent");
        catchProcessBuilder.addActor(ACTOR_NAME);
        catchProcessBuilder.addBusinessData("myBusinessData", EMPLOYEE_QUALIFIED_NAME,
                new ExpressionBuilder().createGroovyScriptExpression("initBD",
                        "import " + EMPLOYEE_QUALIFIED_NAME + "\n" +
                                "Employee e = new Employee(); e.firstName = 'Jules'; e.lastName = 'employeeName'; return e;",
                        EMPLOYEE_QUALIFIED_NAME));
        CatchMessageEventTriggerDefinitionBuilder catchMessageEventTriggerDefinitionBuilder = catchProcessBuilder.addIntermediateCatchEvent("message1")
                .addMessageEventTrigger("msg_name");
        catchMessageEventTriggerDefinitionBuilder
                .addOperation(new OperationBuilder().createBusinessDataSetAttributeOperation("myBusinessData", "setFirstName", String.class.getName(),
                        new ExpressionBuilder().createDataExpression("msg_name", String.class.getName())));
        catchProcessBuilder.addTransition("startEvent", "message1");
        DesignProcessDefinition designCatchProcessDefinition = catchProcessBuilder.done();

        BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive()
                .setProcessDefinition(designCatchProcessDefinition);
        ProcessDefinition catchProcessDefinition = deployAndEnableProcessWithActor(businessArchiveBuilder.done(), ACTOR_NAME, testUser);
        ProcessInstance catchProcessInstance = getProcessAPI().startProcessWithInputs(catchProcessDefinition.getId(), Collections.emptyMap());
        businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designThrowProcessDefinition);
        ProcessDefinition throwProcessDefinition = deployAndEnableProcessWithActor(businessArchiveBuilder.done(), ACTOR_NAME, testUser);
        waitForEventInWaitingState(catchProcessInstance, "message1");
        ProcessInstance throwProcessInstance = getProcessAPI().startProcess(throwProcessDefinition.getId());

        // Message should have been received and process should have finished:
        waitForProcessToFinish(throwProcessInstance);
        waitForProcessToFinish(catchProcessInstance);

        disableAndDeleteProcess(catchProcessDefinition);
        disableAndDeleteProcess(throwProcessDefinition);
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

        public Expression getExpression() throws InvalidExpressionException {
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

        public Operation getCreationOperation() throws InvalidExpressionException {
            String sb = "import " + ADDRESS_QUALIFIED_NAME + "\n" +
                    "Address a = new Address();\n" +
                    "a.street ='" + street + "'\n" +
                    "a.city ='" + city + "'\n" +
                    "return a;";
            final Expression addressExpression = new ExpressionBuilder().createGroovyScriptExpression("createAddress" + varName,
                    sb,
                    ADDRESS_QUALIFIED_NAME);
            return new OperationBuilder().createNewInstance()
                    .setLeftOperand(new LeftOperandBuilder().createBusinessDataLeftOperand(varName))
                    .setType(OperatorType.ASSIGNMENT)
                    .setRightOperand(addressExpression).done();
        }

    }
}
