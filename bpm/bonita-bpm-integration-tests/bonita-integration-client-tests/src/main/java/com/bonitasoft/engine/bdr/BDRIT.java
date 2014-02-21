package com.bonitasoft.engine.bdr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;

public class BDRIT extends CommonAPISPTest {

    private static final String EMPLOYEE_QUALIF_CLASSNAME = "org.bonita.pojo.Employee";

    private User matti;

    @Before
    public void setUp() throws Exception {
        login();
        matti = createUser("matti", "bpm");

        final InputStream stream = BDRIT.class.getResourceAsStream("/bdr-jar.bak");
        assertNotNull(stream);
        final byte[] jar = IOUtils.toByteArray(stream);
        getTenantManagementAPI().deployBusinessDataRepository(jar);
    }

    @After
    public void tearDown() throws BonitaException {
        deleteUser(matti);
        logout();
    }

    // @Test
    @Ignore("Disabled until we support undeploy of a bdr, otherwise the following tests fail")
    public void deployABDRAndExecuteAGroovyScriptWhichContainsAPOJOFromTheBDR() throws BonitaException, IOException {

        final Expression stringExpression = new ExpressionBuilder().createGroovyScriptExpression("alive",
                "import org.bonita.pojo.Employee; Employee e = new Employee(); e.firstName = 'John'; e.lastName = 'Doe'; return e.toString(); ",
                String.class.getName());
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>();
        expressions.put(stringExpression, new HashMap<String, Serializable>());

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addAutomaticTask("stepO");
        final ProcessDefinition processDefinition = getProcessAPI().deploy(processDefinitionBuilder.done());
        getProcessAPI().enableProcess(processDefinition.getId());
        final Map<String, Serializable> result = getProcessAPI().evaluateExpressionsOnProcessDefinition(processDefinition.getId(), expressions);
        assertEquals(1, result.size());

        final Set<Entry<String, Serializable>> entrySet = result.entrySet();
        final Entry<String, Serializable> entry = entrySet.iterator().next();
        assertEquals("Employee [id=null, firstName=John, lastName=Doe]", entry.getValue());

        disableAndDeleteProcess(processDefinition.getId());
    }

    // @Test
    @Ignore("Disabled until we support undeploy of a bdr, otherwise the following tests fail")
    public void deployABDRAndCreateABusinessData() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee",
                "import org.bonita.pojo.Employee; Employee e = new Employee(); e.firstName = 'John'; e.lastName = 'Doe'; return e;", EMPLOYEE_QUALIF_CLASSNAME);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIF_CLASSNAME, null);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addOperation(new LeftOperandBuilder().createNewInstance("myEmployee").done(),
                OperatorType.CREATE_BUSINESS_DATA, null, null, employeeExpression);

        final ProcessDefinition definition = deployAndEnableWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());

        final HumanTaskInstance userTask = waitForUserTask("step1", instance.getId());
        Object businessDataInstance = getProcessAPI().getBusinessDataInstance("myEmployee", instance.getId());
        assertNull(businessDataInstance);
        getProcessAPI().assignUserTask(userTask.getId(), matti.getId());
        getProcessAPI().executeFlowNode(userTask.getId());

        businessDataInstance = getProcessAPI().getBusinessDataInstance("myEmployee", instance.getId());
        assertNotNull(businessDataInstance);

        disableAndDeleteProcess(definition.getId());
    }

    // @Test
    @Ignore("Disabled until we support undeploy of a bdr, otherwise the following tests fail")
    public void deployABDRAndCreateADefaultBusinessData() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee",
                "import org.bonita.pojo.Employee; Employee e = new Employee(); e.firstName = 'John'; e.lastName = 'Doe'; return e;", EMPLOYEE_QUALIF_CLASSNAME);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addBusinessData("myEmployee", EMPLOYEE_QUALIF_CLASSNAME, employeeExpression);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);

        final ProcessDefinition definition = deployAndEnableWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());

        waitForUserTask("step1", instance.getId());
        final Object businessDataInstance = getProcessAPI().getBusinessDataInstance("myEmployee", instance.getId());
        assertNotNull(businessDataInstance);

        disableAndDeleteProcess(definition.getId());
    }

    // @Test
    @Ignore("Disabled until we support undeploy of a bdr, otherwise the following tests fail")
    public void deployABDRAndCreateAndUdpateABusinessData() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder()
                .createGroovyScriptExpression("createNewEmployee",
                        "import org.bonita.pojo.Employee; Employee e = new Employee(); e.firstName = 'John'; e.lastName = 'Doe'; return e;",
                        "org.bonita.pojo.Employee");

        final Expression getEmployeeExpression = new ExpressionBuilder().createBusinessDataExpression("myEmployee", "org.bonita.pojo.Employee");

        final Expression scriptExpression = new ExpressionBuilder().createGroovyScriptExpression("updateBizData", "myEmployee.lastName = 'BPM'; return 'BPM'",
                String.class.getName(), getEmployeeExpression);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("test", "1.2-alpha");
        processDefinitionBuilder.addBusinessData("myEmployee", "org.bonita.pojo.Employee", employeeExpression);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addDisplayDescription(scriptExpression);

        final ProcessDefinition definition = deployAndEnableWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());

        waitForUserTask("step1", instance.getId());
        final Object businessDataInstance = getProcessAPI().getBusinessDataInstance("myEmployee", instance.getId());
        assertNotNull(businessDataInstance);

        disableAndDeleteProcess(definition.getId());
    }

    @Cover(classes = { Operation.class }, concept = BPMNConcept.OPERATION, keywords = { "BusinessData", "business data java setter operation" }, jira = "BS-7217", story = "update a business data using a java setter operation")
    @Test
    public void shouldBeAbleToUpdateBusinessDataUsingBizDataJavaSetterOperation() throws Exception {
        final Expression employeeExpression = new ExpressionBuilder().createGroovyScriptExpression("createNewEmployee", "import " + EMPLOYEE_QUALIF_CLASSNAME
                + "; Employee e = new Employee(); e.firstName = 'Jules'; return e;", EMPLOYEE_QUALIF_CLASSNAME);

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance(
                "shouldBeAbleToUpdateBusinessDataUsingJavaSetterOperation", "6.3-beta");
        final String businessDataName = "newBornBaby";
        final String newEmployeeFirstName = "Manon";
        processDefinitionBuilder.addBusinessData(businessDataName, EMPLOYEE_QUALIF_CLASSNAME, employeeExpression);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addAutomaticTask("step1").addOperation(
                new OperationBuilder().createBusinessDataSetAttributeOperation(businessDataName, "setFirstName", String.class.getName(),
                        new ExpressionBuilder().createConstantStringExpression(newEmployeeFirstName)));
        processDefinitionBuilder.addUserTask("step2", ACTOR_NAME);
        processDefinitionBuilder.addTransition("step1", "step2");

        final ProcessDefinition definition = deployAndEnableWithActor(processDefinitionBuilder.done(), ACTOR_NAME, matti);
        final long processInstanceId = getProcessAPI().startProcess(definition.getId()).getId();

        waitForUserTask("step2", processInstanceId);

        // Let's check the updated firstName value by calling an expression:
        final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>(1);
        final String expressionName = "retrieve_FirstName";
        expressions.put(new ExpressionBuilder().createGroovyScriptExpression(expressionName, businessDataName + ".firstName", String.class.getName(),
                new ExpressionBuilder().createBusinessDataExpression(businessDataName, EMPLOYEE_QUALIF_CLASSNAME)), null);
        final Map<String, Serializable> evaluatedExpressions = getProcessAPI().evaluateExpressionsOnProcessInstance(processInstanceId, expressions);
        final String returnedFirstName = (String) evaluatedExpressions.get(expressionName);
        assertEquals(newEmployeeFirstName, returnedFirstName);

        disableAndDeleteProcess(definition.getId());
    }

    private ProcessDefinition buildProcess(final String taskName) throws BonitaException, IOException {

        final Expression getEmployeeExpression = new ExpressionBuilder().createBusinessDataExpression("myEmployee", "org.bonita.pojo.Employee");

        final Expression employeeExpression = new ExpressionBuilder()
                .createGroovyScriptExpression("createNewEmployee",
                        "import org.bonita.pojo.Employee; Employee e = new Employee(); e.firstName = 'John'; e.lastName = 'Doe'; return e;",
                        "org.bonita.pojo.Employee");

        final ProcessDefinitionBuilderExt processDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("BizDataAndConnector", "1.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addBusinessData("myEmployee", "org.bonita.pojo.Employee", employeeExpression);
        processDefinitionBuilder
                .addUserTask(taskName, ACTOR_NAME)
                .addConnector("updateBusinessData", "com.bonitasoft.connector.BusinessDataUpdateConnector", "1.0", ConnectorEvent.ON_ENTER)
                .addInput("bizData", getEmployeeExpression)
                .addOutput(
                        new OperationBuilder().createBusinessDataSetAttributeOperation("output1", "setFirstName", String.class.getName(),
                                new ExpressionBuilder().createConstantStringExpression("Dana")));

        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                processDefinitionBuilder.done());
        BarResource barResource = getResource("/com/bonitasoft/engine/bdr/BusinessDataUpdateConnector.impl", "BusinessDataUpdateConnector.impl");
        businessArchiveBuilder.addConnectorImplementation(barResource);

        barResource = buildBarResource(BusinessDataUpdateConnector.class, "BusinessDataUpdateConnector.jar");
        businessArchiveBuilder.addClasspathResource(barResource);

        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchiveBuilder.done());
        addMappingOfActorsForUser(ACTOR_NAME, matti.getId(), processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    private BarResource getResource(final String path, final String name) throws IOException {
        final InputStream stream = BDRIT.class.getResourceAsStream(path);
        assertNotNull(stream);
        try {
            final byte[] byteArray = IOUtils.toByteArray(stream);
            return new BarResource(name, byteArray);
        } finally {
            stream.close();
        }
    }

    // @Test
    @Ignore("Disabled until we support undeploy of a bdr, otherwise the following tests fail")
    public void updateBusinessDataOutsideATransaction() throws Exception {
        final String taskName = "step";

        final ProcessDefinition definition = buildProcess(taskName);
        final ProcessInstance instance = getProcessAPI().startProcess(definition.getId());
        waitForUserTask(taskName, instance.getId());

        final Object businessDataInstance = getProcessAPI().getBusinessDataInstance("myEmployee", instance.getId());
        assertNotNull(businessDataInstance);

        Method method = businessDataInstance.getClass().getMethod("getFirstName", null);
        final String firstName = (String) method.invoke(businessDataInstance, null);
        assertThat(firstName).isEqualTo("Dana");

        method = businessDataInstance.getClass().getMethod("getLastName", null);
        final String lastName = (String) method.invoke(businessDataInstance, null);
        assertThat(lastName).isEqualTo("Hakkinen");

        disableAndDeleteProcess(definition);
    }

}
