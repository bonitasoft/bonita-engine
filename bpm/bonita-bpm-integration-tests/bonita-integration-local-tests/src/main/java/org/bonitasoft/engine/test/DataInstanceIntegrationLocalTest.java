package org.bonitasoft.engine.test;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.BPMRemoteTests;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DataInstanceIntegrationLocalTest extends CommonAPILocalTest {

    @Before
    public void beforeTest() throws BonitaException {
        login();
    }

    @After
    public void afterTest() throws BonitaException {
        logout();
    }

    @Cover(classes = DataInstance.class, concept = BPMNConcept.DATA, jira = "ENGINE-736", keywords = { "data", "container" }, story = "get archived values of data")
    @Test
    public void getDataValueForCompletedActivitesAndProcess() throws Exception {
        final User user = createUser("john", "bpm");
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("processWithArchivedData", "1.0");
        processDefinitionBuilder.addActor("actor");
        processDefinitionBuilder.addUserTask("step1", "actor").addShortTextData("step1Data", new ExpressionBuilder().createConstantStringExpression("s1_1"));
        processDefinitionBuilder.addUserTask("step2", "actor");
        processDefinitionBuilder.addTransition("step1", "step2");
        processDefinitionBuilder.addShortTextData("processData1", new ExpressionBuilder().createConstantStringExpression("p1_1"));
        processDefinitionBuilder.addShortTextData("processData2", new ExpressionBuilder().createConstantStringExpression("p2_1"));
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.done();
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, "actor", user);
        final long processDefinitionId = processDefinition.getId();

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1 = waitForUserTask("step1", processInstance.getId());
        getProcessAPI().updateProcessDataInstance("processData2", processInstance.getId(), "p2_2");
        getProcessAPI().updateActivityDataInstance("step1Data", step1.getId(), "s1_2");
        assignAndExecuteStep(step1, user.getId());
        final ActivityInstance step2 = waitForUserTask("step2", processInstance.getId());
        // check value at end of the activity
        final ArchivedActivityInstance archivedActivityInstance = getProcessAPI().getArchivedActivityInstance(step1.getId());
        assertEquals("s1_2", getProcessAPI().getActivityDataInstance("step1Data", archivedActivityInstance.getId()));
        assertEquals("p1_1", getProcessAPI().getProcessDataInstance("processData1", processDefinitionId));
        assertEquals("p2_2", getProcessAPI().getProcessDataInstance("processData2", processDefinitionId));
        assignAndExecuteStep(step2, user.getId());
        waitForProcessToFinish(processInstance);
        // after archived same value
        assertEquals("s1_2", getProcessAPI().getActivityDataInstance("step1Data", archivedActivityInstance.getId()));
        assertEquals("p1_1", getProcessAPI().getProcessDataInstance("processData1", processDefinitionId));
        assertEquals("p2_2", getProcessAPI().getProcessDataInstance("processData2", processDefinitionId));

        this.disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    @Test
    public void processWithCustomData() throws Exception {
        final User john = createUser("john", "bpm");
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("processWithCustomData", "1.0");
        builder.addUserTask("step1", "actor");
        builder.addActor("actor");
        final StringBuilder groovyBuilder = new StringBuilder("import org.bonitasoft.custom.Address; \n");
        groovyBuilder.append("return new Address(\"Santa Claus\", \"Tähtikuja 1\", \"96930\", \"Napapiiri\", \"Suomi\")");
        final Expression expression = new ExpressionBuilder().createGroovyScriptExpression("initAddress", groovyBuilder.toString(),
                "org.bonitasoft.custom.Address");
        builder.addData("address", "org.bonitasoft.custom.Address", expression);

        final BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(builder.done());
        final InputStream stream = BPMRemoteTests.class.getResourceAsStream("/custom-0.1.jar.bak");
        try {
            assertNotNull(stream);
            final byte[] byteArray = IOUtils.toByteArray(stream);
            archiveBuilder.addClasspathResource(new BarResource("custom.jar", byteArray));
        } finally {
            stream.close();
        }

        final BusinessArchive businessArchive = archiveBuilder.done();
        final ProcessDefinition processDefinition = deployAndEnableWithActor(businessArchive, "actor", john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        assertNotNull(getProcessAPI().getProcessDataInstance("address", processInstance.getId()).getValue());

        disableAndDeleteProcess(processDefinition);
        deleteUser(john);
    }
}
