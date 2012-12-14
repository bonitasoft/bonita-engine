package com.bonitasoft.engine.connector;

import static org.bonitasoft.engine.matchers.ListElementMatcher.nameAre;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.bonitasoft.engine.bpm.model.ActivityInstance;
import org.bonitasoft.engine.bpm.model.AutomaticTaskDefinitionBuilder;
import org.bonitasoft.engine.bpm.model.ConnectorEvent;
import org.bonitasoft.engine.bpm.model.ConnectorInstance;
import org.bonitasoft.engine.bpm.model.ConnectorState;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.bpm.model.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.connector.ConnectorInstanceCriterion;
import org.bonitasoft.engine.core.operation.LeftOperandBuilder;
import org.bonitasoft.engine.core.operation.OperatorType;
import org.bonitasoft.engine.exception.ActivityExecutionFailedException;
import org.bonitasoft.engine.exception.ObjectModificationException;
import org.bonitasoft.engine.exception.ObjectNotFoundException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class RemoteConnectorExecutionTestsSP extends ConnectorExecutionTests {

    @Test
    public void testGetConnectorInstancesOnActivity() throws Exception {
        final String delivery = "Delivery men";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression("none");
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");
        final String connectorId = "testConnectorThatThrowException";
        final String connectorVersion = "1.0";
        final String dataName = "myVar";

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addStringData(dataName, defaultValueExpression).addUserTask("step2", delivery);
        final UserTaskDefinitionBuilder addUserTask = designProcessDefinition.addUserTask("step1", delivery);
        addUserTask.addConnector("myConnector1", connectorId, connectorVersion, ConnectorEvent.ON_ENTER).addInput("kind", input1Expression);
        addUserTask.addConnector("myConnector2", connectorId, connectorVersion, ConnectorEvent.ON_FINISH).addInput("kind", input1Expression);
        addUserTask.addConnector("myConnector3", connectorId, connectorVersion, ConnectorEvent.ON_ENTER).addInput("kind", input1Expression);
        addUserTask.addConnector("myConnector4", connectorId, connectorVersion, ConnectorEvent.ON_FINISH).addInput("kind", input1Expression);
        addUserTask.addConnector("myConnector5", connectorId, connectorVersion, ConnectorEvent.ON_ENTER).addInput("kind", input1Expression);
        addUserTask.addConnector("myConnector6", connectorId, connectorVersion, ConnectorEvent.ON_FINISH).addInput("kind", input1Expression);

        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithTestConnector(delivery, userId, designProcessDefinition);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1 = waitForUserTask("step1", processInstance);
        final ActivityInstance step2 = waitForUserTask("step2", processInstance);
        final List<ConnectorInstance> connectorInstances = getProcessAPI().getConnectorInstancesOfActivity(step1.getId(), 0, 10,
                ConnectorInstanceCriterion.DEFAULT);
        assertThat(connectorInstances, nameAre("myConnector1", "myConnector2", "myConnector3", "myConnector4", "myConnector5", "myConnector6"));
        assertTrue(getProcessAPI().getConnectorInstancesOfActivity(step2.getId(), 0, 10, ConnectorInstanceCriterion.DEFAULT).isEmpty());
        disableAndDelete(processDefinition);
        deleteUser(JOHN);
    }

    @Test
    public void testGetConnectorInstancesOnProcess() throws Exception {
        final String delivery = "Delivery men";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression("none");
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");
        final String connectorId = "testConnectorThatThrowException";
        final String connectorVersion = "1.0";
        final String dataName = "myVar";

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addStringData(dataName, defaultValueExpression);
        designProcessDefinition.addUserTask("step1", delivery);
        designProcessDefinition.addConnector("myConnector1", connectorId, connectorVersion, ConnectorEvent.ON_ENTER).addInput("kind", input1Expression);
        designProcessDefinition.addConnector("myConnector2", connectorId, connectorVersion, ConnectorEvent.ON_FINISH).addInput("kind", input1Expression);
        designProcessDefinition.addConnector("myConnector3", connectorId, connectorVersion, ConnectorEvent.ON_ENTER).addInput("kind", input1Expression);
        designProcessDefinition.addConnector("myConnector4", connectorId, connectorVersion, ConnectorEvent.ON_FINISH).addInput("kind", input1Expression);
        designProcessDefinition.addConnector("myConnector5", connectorId, connectorVersion, ConnectorEvent.ON_ENTER).addInput("kind", input1Expression);
        designProcessDefinition.addConnector("myConnector6", connectorId, connectorVersion, ConnectorEvent.ON_FINISH).addInput("kind", input1Expression);

        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithTestConnector(delivery, userId, designProcessDefinition);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("step1", processInstance);
        final List<ConnectorInstance> connectorInstances = getProcessAPI().getConnectorInstancesOfProcess(processInstance.getId(), 0, 10,
                ConnectorInstanceCriterion.DEFAULT);
        assertThat(connectorInstances, nameAre("myConnector1", "myConnector2", "myConnector3", "myConnector4", "myConnector5", "myConnector6"));
        disableAndDelete(processDefinition);
        deleteUser(JOHN);
    }

    @Test
    public void testSetConnectorState() throws Exception {
        final String delivery = "Delivery men";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression("normal");
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");
        final String connectorId = "testConnectorThatThrowException";
        final String connectorVersion = "1.0";
        final String dataName = "myVar";

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addStringData(dataName, defaultValueExpression);
        designProcessDefinition
                .addAutomaticTask("step1")
                .addConnector("myConnector", connectorId, connectorVersion, ConnectorEvent.ON_ENTER)
                .addInput("kind", input1Expression)
                .addOutput(new LeftOperandBuilder().createNewInstance().setDataName(dataName).done(), OperatorType.ASSIGNMENT, "=", "",
                        new ExpressionBuilder().createInputExpression("output1", String.class.getName()));

        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithTestConnector(delivery, userId, designProcessDefinition);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance waitForTaskToFail = waitForTaskToFail(processInstance);
        assertEquals("step1", waitForTaskToFail.getName());
        List<ConnectorInstance> connectorInstances = getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail.getId(), 0, 10,
                ConnectorInstanceCriterion.DEFAULT);
        ConnectorInstance failedConnector = connectorInstances.get(0);
        assertEquals(ConnectorState.FAILED, failedConnector.getState());
        getProcessAPI().setConnectorInstanceState(failedConnector.getId(), ConnectorState.TO_RE_EXECUTE);
        connectorInstances = getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail.getId(), 0, 10, ConnectorInstanceCriterion.DEFAULT);
        failedConnector = connectorInstances.get(0);
        assertEquals(ConnectorState.TO_RE_EXECUTE, failedConnector.getState());
        getProcessAPI().setConnectorInstanceState(failedConnector.getId(), ConnectorState.SKIPPED);
        connectorInstances = getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail.getId(), 0, 10, ConnectorInstanceCriterion.DEFAULT);
        failedConnector = connectorInstances.get(0);
        assertEquals(ConnectorState.SKIPPED, failedConnector.getState());
        getProcessAPI().setConnectorInstanceState(failedConnector.getId(), ConnectorState.DONE);
        connectorInstances = getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail.getId(), 0, 10, ConnectorInstanceCriterion.DEFAULT);
        failedConnector = connectorInstances.get(0);
        assertEquals(ConnectorState.DONE, failedConnector.getState());
        getProcessAPI().setConnectorInstanceState(failedConnector.getId(), ConnectorState.FAILED);
        connectorInstances = getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail.getId(), 0, 10, ConnectorInstanceCriterion.DEFAULT);
        failedConnector = connectorInstances.get(0);
        assertEquals(ConnectorState.FAILED, failedConnector.getState());
        disableAndDelete(processDefinition);
        deleteUser(JOHN);
    }

    @Test(expected = ObjectModificationException.class)
    public void testSetConnectorStateToTOEXECUTEFail() throws Exception {
        final String delivery = "Delivery men";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression("normal");
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");
        final String connectorId = "testConnectorThatThrowException";
        final String connectorVersion = "1.0";
        final String dataName = "myVar";

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addStringData(dataName, defaultValueExpression);
        designProcessDefinition
                .addAutomaticTask("step1")
                .addConnector("myConnector", connectorId, connectorVersion, ConnectorEvent.ON_ENTER)
                .addInput("kind", input1Expression)
                .addOutput(new LeftOperandBuilder().createNewInstance().setDataName(dataName).done(), OperatorType.ASSIGNMENT, "=", "",
                        new ExpressionBuilder().createInputExpression("output1", String.class.getName()));

        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithTestConnector(delivery, userId, designProcessDefinition);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance waitForTaskToFail = waitForTaskToFail(processInstance);
        assertEquals("step1", waitForTaskToFail.getName());
        final List<ConnectorInstance> connectorInstances = getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail.getId(), 0, 10,
                ConnectorInstanceCriterion.DEFAULT);
        final ConnectorInstance failedConnector = connectorInstances.get(0);
        assertEquals(ConnectorState.FAILED, failedConnector.getState());
        try {
            getProcessAPI().setConnectorInstanceState(failedConnector.getId(), ConnectorState.TO_BE_EXECUTED);
        } finally {
            disableAndDelete(processDefinition);
            deleteUser(JOHN);
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testSetConnectorStateOnUnkownConnector() throws Exception {
        try {
            getProcessAPI().setConnectorInstanceState(-123456789l, ConnectorState.FAILED);
        } finally {
            deleteUser(JOHN);
        }
    }

    @Test
    public void testReplayActivityWithConnectorToTeExecuteAndThenSkipped() throws Exception {
        final String delivery = "Delivery men";
        final Expression normal = new ExpressionBuilder().createConstantStringExpression("normal");
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");
        final String connectorId = "testConnectorThatThrowException";
        final String connectorVersion = "1.0";
        final String dataName = "myVar";

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addStringData(dataName, defaultValueExpression);
        designProcessDefinition.addAutomaticTask("step1").addConnector("myConnector", connectorId, connectorVersion, ConnectorEvent.ON_ENTER)
                .addInput("kind", normal);

        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithTestConnector(delivery, userId, designProcessDefinition);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance waitForTaskToFail = waitForTaskToFail(processInstance);

        // put in TO BE EXECUTED and restart the task
        getProcessAPI().setConnectorInstanceState(
                getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail.getId(), 0, 1, ConnectorInstanceCriterion.DEFAULT).get(0).getId(),
                ConnectorState.TO_RE_EXECUTE);
        getProcessAPI().replayActivity(waitForTaskToFail.getId());
        final ActivityInstance waitForTaskToFail2 = waitForTaskToFail(processInstance);

        // failed again, put in SKIPPED and restart the task
        getProcessAPI().setConnectorInstanceState(
                getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail2.getId(), 0, 1, ConnectorInstanceCriterion.DEFAULT).get(0).getId(),
                ConnectorState.SKIPPED);
        getProcessAPI().replayActivity(waitForTaskToFail2.getId());

        // should finish
        waitForProcessToFinish(processInstance);
        disableAndDelete(processDefinition);
        deleteUser(JOHN);
    }

    @Test
    public void testReplayActivityWithMultipleConnectors() throws Exception {
        final String delivery = "Delivery men";
        final Expression normal = new ExpressionBuilder().createConstantStringExpression("normal");
        final Expression none = new ExpressionBuilder().createConstantStringExpression("none");
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");
        final String connectorId = "testConnectorThatThrowException";
        final String connectorVersion = "1.0";
        final String dataName = "myVar";

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addStringData(dataName, defaultValueExpression);
        final AutomaticTaskDefinitionBuilder addAutomaticTask = designProcessDefinition.addAutomaticTask("step1");
        addAutomaticTask.addConnector("myConnector1", connectorId, connectorVersion, ConnectorEvent.ON_ENTER).addInput("kind", none);
        addAutomaticTask.addConnector("myConnector2", connectorId, connectorVersion, ConnectorEvent.ON_ENTER).addInput("kind", normal);
        addAutomaticTask.addConnector("myConnector3", connectorId, connectorVersion, ConnectorEvent.ON_ENTER).addInput("kind", none);

        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithTestConnector(delivery, userId, designProcessDefinition);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance waitForTaskToFail = waitForTaskToFail(processInstance);

        // put in TO BE EXECUTED and restart the task
        getProcessAPI().setConnectorInstanceState(
                getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail.getId(), 0, 3, ConnectorInstanceCriterion.DEFAULT).get(1).getId(),
                ConnectorState.TO_RE_EXECUTE);
        getProcessAPI().replayActivity(waitForTaskToFail.getId());
        final ActivityInstance waitForTaskToFail2 = waitForTaskToFail(processInstance);

        // failed again, put in SKIPPED and restart the task
        getProcessAPI().setConnectorInstanceState(
                getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail2.getId(), 0, 3, ConnectorInstanceCriterion.DEFAULT).get(1).getId(),
                ConnectorState.SKIPPED);
        getProcessAPI().replayActivity(waitForTaskToFail2.getId());

        // should finish
        waitForProcessToFinish(processInstance);
        disableAndDelete(processDefinition);
        deleteUser(JOHN);
    }

    @Test
    public void testReplayActivityWithMultipleConnectorsOnFinish() throws Exception {
        final String delivery = "Delivery men";
        final Expression normal = new ExpressionBuilder().createConstantStringExpression("normal");
        final Expression none = new ExpressionBuilder().createConstantStringExpression("none");
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");
        final String connectorId = "testConnectorThatThrowException";
        final String connectorVersion = "1.0";
        final String dataName = "myVar";

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addStringData(dataName, defaultValueExpression);
        final AutomaticTaskDefinitionBuilder addAutomaticTask = designProcessDefinition.addAutomaticTask("step1");
        addAutomaticTask.addConnector("myConnector1", connectorId, connectorVersion, ConnectorEvent.ON_ENTER).addInput("kind", none);
        addAutomaticTask.addConnector("myConnector2", connectorId, connectorVersion, ConnectorEvent.ON_FINISH).addInput("kind", normal);
        addAutomaticTask.addConnector("myConnector3", connectorId, connectorVersion, ConnectorEvent.ON_FINISH).addInput("kind", none);

        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithTestConnector(delivery, userId, designProcessDefinition);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance waitForTaskToFail = waitForTaskToFail(processInstance);

        // put in TO BE EXECUTED and restart the task
        getProcessAPI().setConnectorInstanceState(
                getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail.getId(), 0, 3, ConnectorInstanceCriterion.DEFAULT).get(1).getId(),
                ConnectorState.TO_RE_EXECUTE);
        getProcessAPI().replayActivity(waitForTaskToFail.getId());
        final ActivityInstance waitForTaskToFail2 = waitForTaskToFail(processInstance);

        // failed again, put in SKIPPED and restart the task
        getProcessAPI().setConnectorInstanceState(
                getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail2.getId(), 0, 3, ConnectorInstanceCriterion.DEFAULT).get(1).getId(),
                ConnectorState.SKIPPED);
        getProcessAPI().replayActivity(waitForTaskToFail2.getId());

        // should finish
        waitForProcessToFinish(processInstance);
        disableAndDelete(processDefinition);
        deleteUser(JOHN);
    }

    @Test(expected = ActivityExecutionFailedException.class)
    public void testReplayActivityWithConnectorFailed() throws Exception {
        final String delivery = "Delivery men";
        final Expression normal = new ExpressionBuilder().createConstantStringExpression("normal");
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");
        final String connectorId = "testConnectorThatThrowException";
        final String connectorVersion = "1.0";
        final String dataName = "myVar";

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithConnector", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addStringData(dataName, defaultValueExpression);
        designProcessDefinition
                .addAutomaticTask("step1")
                .addConnector("myConnector", connectorId, connectorVersion, ConnectorEvent.ON_ENTER)
                .addInput("kind", normal)
                .addOutput(new LeftOperandBuilder().createNewInstance().setDataName(dataName).done(), OperatorType.ASSIGNMENT, "=", "",
                        new ExpressionBuilder().createInputExpression("output1", String.class.getName()));

        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithTestConnector(delivery, userId, designProcessDefinition);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance waitForTaskToFail = waitForTaskToFail(processInstance);
        // just restart, should not work
        try {
            getProcessAPI().replayActivity(waitForTaskToFail.getId());
        } finally {
            disableAndDelete(processDefinition);
            deleteUser(JOHN);
        }
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testReplayUnknownActivity() throws Exception {
        try {
            getProcessAPI().replayActivity(-123456789l);
        } finally {
            deleteUser(JOHN);
        }
    }
}
