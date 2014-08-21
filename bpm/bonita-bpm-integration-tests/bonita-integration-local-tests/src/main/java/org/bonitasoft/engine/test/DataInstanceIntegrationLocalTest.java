package org.bonitasoft.engine.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.flownode.CallActivityInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.CallActivityBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.process.Employee;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DataInstanceIntegrationLocalTest extends CommonAPILocalTest {

    private User cebolinha;

    private User cascao;

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        cebolinha = createUser("cebolinha", "bpm");
        cascao = createUser("cascao", "bpm");
    }

    @After
    public void afterTest() throws BonitaException {
        deleteUser(cebolinha.getId());
        deleteUser(cascao.getId());
       logoutOnTenant();
    }

    @Test
    public void processWithCustomData() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("processWithCustomData", "1.0");
        builder.addUserTask("step1", "actor");
        builder.addActor("actor");
        final Expression expression = new ExpressionBuilder().createGroovyScriptExpression("initEmployee", "import org.bonitasoft.engine.process.Employee; \n"
                + "return new Employee(\"TÃ¤htikuja\", \"firstName48\")", "org.bonitasoft.engine.process.Employee");
        builder.addData("address", "org.bonitasoft.engine.process.Employee", expression);

        final BusinessArchive businessArchive = addClasspathRessource(builder).done();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(businessArchive, "actor", cascao);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        assertNotNull(getProcessAPI().getProcessDataInstance("address", processInstance.getId()).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { CallActivityInstance.class }, concept = BPMNConcept.CALL_ACTIVITY, keywords = { "Call Activity", "Custom data on activity" }, jira = "ENGINE-1498")
    @Test
    public void callActivityWith2CustomDatasOnActivityUsingSameExternalLibrairy() throws Exception {
        final String targetProcessName = "targetProcess";
        final Expression targetProcessNameExpr = new ExpressionBuilder().createConstantStringExpression(targetProcessName);
        final Expression targetProcessVersionExpr = new ExpressionBuilder().createConstantStringExpression(PROCESS_VERSION);

        // Build target process
        final Expression defaultValueForEmployeeOnTStep1 = new ExpressionBuilder().createGroovyScriptExpression("initEmployee",
                "import org.bonitasoft.engine.process.Employee; \n" + "return new Employee(\"TÃ¤htikuja\", \"firstName48\")",
                "org.bonitasoft.engine.process.Employee");
        final ProcessDefinitionBuilder targetProcessDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(targetProcessName, PROCESS_VERSION);
        targetProcessDefinitionBuilder.addActor(ACTOR_NAME).addStartEvent("tStart");
        targetProcessDefinitionBuilder.addUserTask("tStep1", ACTOR_NAME).addData("data", Employee.class.getName(), defaultValueForEmployeeOnTStep1);
        targetProcessDefinitionBuilder.addEndEvent("tEnd");
        targetProcessDefinitionBuilder.addTransition("tStart", "tStep1").addTransition("tStep1", "tEnd");
        final ProcessDefinition targetProcessDefinition = deployAndEnableProcessWithActor(addClasspathRessource(targetProcessDefinitionBuilder).done(),
                ACTOR_NAME,
                cebolinha);

        // Build main process
        final Expression defaultValueForEmployeeOnStep1 = new ExpressionBuilder().createGroovyScriptExpression("initEmployee",
                "import org.bonitasoft.engine.process.Employee; \n" + "return new Employee(\"name\", \"firstName\")", "org.bonitasoft.engine.process.Employee");
        final ProcessDefinitionBuilder callingProcessDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("callingProcess", PROCESS_VERSION);
        callingProcessDefinitionBuilder.addActor(ACTOR_NAME).addStartEvent("start");
        callingProcessDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addData("data", Employee.class.getName(), defaultValueForEmployeeOnStep1);
        callingProcessDefinitionBuilder.addCallActivity("callActivity", targetProcessNameExpr, targetProcessVersionExpr)
                .addDisplayName(new ExpressionBuilder().createConstantStringExpression("callActivityDisplayName")).addDescription("callActivityDescription")
                .addDisplayDescription(new ExpressionBuilder().createConstantStringExpression("callActivityDisplayDescription"));
        callingProcessDefinitionBuilder.addEndEvent("end");
        callingProcessDefinitionBuilder.addTransition("start", "step1").addTransition("step1", "callActivity").addTransition("callActivity", "end");
        final ProcessDefinition callingProcessDefinition = deployAndEnableProcessWithActor(addClasspathRessource(callingProcessDefinitionBuilder).done(),
                ACTOR_NAME,
                cascao);
        final ProcessInstance callingProcessInstance = getProcessAPI().startProcess(callingProcessDefinition.getId());

        final long step1Id = waitForUserTask("step1", callingProcessInstance).getId();
        assertNotNull(getProcessAPI().getActivityDataInstance("data", step1Id).getValue());
        assignAndExecuteStep(step1Id, cascao.getId());
        final long tStep1Id = waitForUserTask("tStep1", callingProcessInstance).getId();
        assertNotNull(getProcessAPI().getActivityDataInstance("data", tStep1Id).getValue());
        assignAndExecuteStep(tStep1Id, cebolinha.getId());
        assertTrue("parent process was not archived", waitForProcessToFinishAndBeArchived(callingProcessInstance));

        // Clean up
        disableAndDeleteProcess(callingProcessDefinition);
        disableAndDeleteProcess(targetProcessDefinition);
    }

    @Cover(classes = { CallActivityInstance.class }, concept = BPMNConcept.CALL_ACTIVITY, keywords = { "Call Activity", "Custom data on process",
            "Sub process with terminate end event" }, jira = "ENGINE-1719")
    @Test
    public void callActivityWithMappingAnd2CustomDatasOnProcessUsingSameExternalLibrairy() throws Exception {
        // getCommandAPI().addDependency("Employee.jar", IOUtil.generateJar(Employee.class));

        final String dataName = "data";
        final String dataType = Employee.class.getName();
        final String targetProcessName = "targetProcess";
        final Expression targetProcessNameExpr = new ExpressionBuilder().createConstantStringExpression(targetProcessName);
        final Expression targetProcessVersionExpr = new ExpressionBuilder().createConstantStringExpression(PROCESS_VERSION);

        // Build target process
        final ProcessDefinitionBuilder targetProcessDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(targetProcessName, PROCESS_VERSION);
        targetProcessDefinitionBuilder.addActor(ACTOR_NAME + 2).addStartEvent("tStart");
        targetProcessDefinitionBuilder.addUserTask("tStep1", ACTOR_NAME + 2);
        targetProcessDefinitionBuilder.addData(dataName, dataType, null);
        targetProcessDefinitionBuilder.addEndEvent("tEnd").addTerminateEventTrigger();
        targetProcessDefinitionBuilder.addTransition("tStart", "tStep1").addTransition("tStep1", "tEnd");
        final ProcessDefinition targetProcessDefinition = deployAndEnableProcessWithActor(addClasspathRessource(targetProcessDefinitionBuilder).done(),
                ACTOR_NAME + 2, cebolinha);

        // Build main process
        final Expression defaultValueForEmployeeOnStep1 = new ExpressionBuilder().createGroovyScriptExpression("initEmployee",
                "import org.bonitasoft.engine.process.Employee; \n" + "return new Employee(\"name\", \"firstName\")", "org.bonitasoft.engine.process.Employee");
        final ProcessDefinitionBuilder callingProcessDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("callingProcess", PROCESS_VERSION);
        callingProcessDefinitionBuilder.addActor(ACTOR_NAME).addStartEvent("start");
        callingProcessDefinitionBuilder.addData(dataName, dataType, defaultValueForEmployeeOnStep1);
        callingProcessDefinitionBuilder.addUserTask("Step1", ACTOR_NAME);
        final CallActivityBuilder callActivity = callingProcessDefinitionBuilder.addCallActivity("callActivity", targetProcessNameExpr,
                targetProcessVersionExpr);
        final Expression rightOperand = new ExpressionBuilder().createDataExpression(dataName, dataType);
        callActivity.addDataInputOperation(BuildTestUtil.buildOperation(dataName, false, OperatorType.ASSIGNMENT, "=", rightOperand));
        callActivity.addDataOutputOperation(BuildTestUtil.buildOperation(dataName, false, OperatorType.ASSIGNMENT, "=", rightOperand));
        callingProcessDefinitionBuilder.addEndEvent("end");
        callingProcessDefinitionBuilder.addTransition("start", "callActivity").addTransition("callActivity", "Step1").addTransition("Step1", "end");
        final ProcessDefinition callingProcessDefinition = deployAndEnableProcessWithActor(addClasspathRessource(callingProcessDefinitionBuilder).done(),
                ACTOR_NAME,
                cebolinha);
        final ProcessInstance callingProcessInstance = getProcessAPI().startProcess(callingProcessDefinition.getId());

        final HumanTaskInstance humanTaskInstance = waitForUserTask("tStep1", callingProcessInstance);
        assertNotNull(getProcessAPI().getProcessDataInstance(dataName, humanTaskInstance.getParentProcessInstanceId()).getValue());
        assignAndExecuteStep(humanTaskInstance, cebolinha.getId());
        final HumanTaskInstance step1 = waitForUserTask("Step1", callingProcessInstance);
        assertNotNull(getProcessAPI().getProcessDataInstance(dataName, step1.getParentProcessInstanceId()).getValue());
        assignAndExecuteStep(step1, cebolinha.getId());
        assertTrue("parent process was not archived", waitForProcessToFinishAndBeArchived(callingProcessInstance));

        // Clean up
        disableAndDeleteProcess(callingProcessDefinition);
        disableAndDeleteProcess(targetProcessDefinition);
    }

    private BusinessArchiveBuilder addClasspathRessource(final ProcessDefinitionBuilder targetProcessDefinitionBuilder)
            throws InvalidProcessDefinitionException, IOException {
        final BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                targetProcessDefinitionBuilder.done());
        archiveBuilder.addClasspathResource(new BarResource("Employee.jar", IOUtil.generateJar(Employee.class)));
        return archiveBuilder;
    }
}
