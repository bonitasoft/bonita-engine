package org.bonitasoft.engine.test;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.BPMRemoteTests;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.flownode.CallActivityInstance;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.process.Employee;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DataInstanceIntegrationLocalTest extends CommonAPILocalTest {

    private User cebolinha;

    private User cascao;

    @Before
    public void beforeTest() throws BonitaException {
        login();
        cebolinha = createUser("cebolinha", "bpm");
        cascao = createUser("cascao", "bpm");
    }

    @After
    public void afterTest() throws BonitaException {
        deleteUser(cebolinha.getId());
        deleteUser(cascao.getId());
        logout();
    }

    @Test
    public void processWithCustomData() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("processWithCustomData", "1.0");
        builder.addUserTask("step1", "actor");
        builder.addActor("actor");
        final StringBuilder groovyBuilder = new StringBuilder("import org.bonitasoft.custom.Address; \n");
        groovyBuilder.append("return new Address(\"Santa Claus\", \"Tähtikuja 1\", \"96930\", \"Napapiiri\", \"Suomi\")");
        final Expression expression = new ExpressionBuilder().createGroovyScriptExpression("initAddress", groovyBuilder.toString(),
                "org.bonitasoft.custom.Address");
        builder.addData("address", "org.bonitasoft.custom.Address", expression);

        final BusinessArchive businessArchive = addClasspathRessource(builder).done();
        final ProcessDefinition processDefinition = deployAndEnableWithActor(businessArchive, "actor", cascao);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        assertNotNull(getProcessAPI().getProcessDataInstance("address", processInstance.getId()).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { CallActivityInstance.class }, concept = BPMNConcept.CALL_ACTIVITY, keywords = { "Call Activity" }, jira = "ENGINE-1498")
    @Test
    public void callActivityWith2CustomDatasUsingSameExternalLibrairy() throws Exception {
        final String targetProcessName = "targetProcess";
        final Expression targetProcessNameExpr = new ExpressionBuilder().createConstantStringExpression(targetProcessName);
        final Expression targetProcessVersionExpr = new ExpressionBuilder().createConstantStringExpression(PROCESS_VERSION);

        // Build target process
        final Expression defaultValueForEmployeeOnTStep1 = new ExpressionBuilder().createGroovyScriptExpression("initAddress",
                "import org.bonitasoft.custom.Address; \n" + "return new Address(\"Santa Claus\", \"Tähtikuja 1\", \"96930\", \"Napapiiri\", \"Suomi\")",
                "org.bonitasoft.custom.Address");
        final ProcessDefinitionBuilder targetProcessDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(targetProcessName, PROCESS_VERSION);
        targetProcessDefinitionBuilder.addActor(ACTOR_NAME).addStartEvent("tStart");
        targetProcessDefinitionBuilder.addUserTask("tStep1", ACTOR_NAME).addData("data", Employee.class.getName(), defaultValueForEmployeeOnTStep1);
        targetProcessDefinitionBuilder.addEndEvent("tEnd");
        targetProcessDefinitionBuilder.addTransition("tStart", "tStep1").addTransition("tStep1", "tEnd");
        final ProcessDefinition targetProcessDefinition = deployAndEnableWithActor(addClasspathRessource(targetProcessDefinitionBuilder).done(), ACTOR_NAME,
                cebolinha);

        // Build main process
        final Expression defaultValueForEmployeeOnStep1 = new ExpressionBuilder().createGroovyScriptExpression("initAddress",
                "import org.bonitasoft.custom.Address; \n" + "return new Address(\"plop\", \"plop 1\", \"45655\", \"plop3\", \"plop4\")",
                "org.bonitasoft.custom.Address");
        final ProcessDefinitionBuilder callingProcessDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("callingProcess", PROCESS_VERSION);
        callingProcessDefinitionBuilder.addActor(ACTOR_NAME).addStartEvent("start");
        callingProcessDefinitionBuilder.addUserTask("step1", ACTOR_NAME).addData("data", Employee.class.getName(), defaultValueForEmployeeOnStep1);
        callingProcessDefinitionBuilder.addCallActivity("callActivity", targetProcessNameExpr, targetProcessVersionExpr)
                .addDisplayName(new ExpressionBuilder().createConstantStringExpression("callActivityDisplayName")).addDescription("callActivityDescription")
                .addDisplayDescription(new ExpressionBuilder().createConstantStringExpression("callActivityDisplayDescription"));
        callingProcessDefinitionBuilder.addEndEvent("end");
        callingProcessDefinitionBuilder.addTransition("start", "step1").addTransition("step1", "callActivity").addTransition("callActivity", "end");
        final ProcessDefinition callingProcessDefinition = deployAndEnableWithActor(addClasspathRessource(callingProcessDefinitionBuilder).done(), ACTOR_NAME,
                cascao);
        final ProcessInstance callingProcessInstance = getProcessAPI().startProcess(callingProcessDefinition.getId());

        final long step1Id = waitForUserTask("step1", callingProcessInstance).getId();
        assertNotNull(getProcessAPI().getActivityDataInstance("data", step1Id).getValue());
        assignAndExecuteStep(step1Id, cascao.getId());
        final long tStep1Id = waitForUserTask("tStep1", callingProcessInstance).getId();
        assertNotNull(getProcessAPI().getActivityDataInstance("data", tStep1Id).getValue());
        assignAndExecuteStep(tStep1Id, cebolinha.getId());
        assertTrue("parent process was not archived", waitProcessToFinishAndBeArchived(callingProcessInstance));

        // Clean up
        disableAndDeleteProcess(callingProcessDefinition);
        disableAndDeleteProcess(targetProcessDefinition);
    }

    private BusinessArchiveBuilder addClasspathRessource(final ProcessDefinitionBuilder targetProcessDefinitionBuilder)
            throws InvalidProcessDefinitionException, IOException {
        final BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                targetProcessDefinitionBuilder.done());
        final InputStream stream = BPMRemoteTests.class.getResourceAsStream("/custom-0.1.jar.bak");
        try {
            assertNotNull(stream);
            final byte[] byteArray = IOUtils.toByteArray(stream);
            archiveBuilder.addClasspathResource(new BarResource("custom.jar", byteArray));
        } finally {
            stream.close();
        }
        return archiveBuilder;
    }
}
