/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.connector;

import static org.bonitasoft.engine.matchers.ListElementMatcher.nameAre;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.ConnectorExecutionException;
import org.bonitasoft.engine.bpm.connector.ConnectorImplementationDescriptor;
import org.bonitasoft.engine.bpm.connector.ConnectorInstance;
import org.bonitasoft.engine.bpm.connector.ConnectorInstanceCriterion;
import org.bonitasoft.engine.bpm.connector.ConnectorInstanceWithFailureInfo;
import org.bonitasoft.engine.bpm.connector.ConnectorState;
import org.bonitasoft.engine.bpm.connector.ConnectorStateReset;
import org.bonitasoft.engine.bpm.connector.InvalidConnectorImplementationException;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.data.DataNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityExecutionException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.AutomaticTaskDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.connectors.TestConnector;
import org.bonitasoft.engine.connectors.TestConnector2;
import org.bonitasoft.engine.connectors.TestConnectorThatThrowException;
import org.bonitasoft.engine.connectors.TestConnectorWithModifiedOutput;
import org.bonitasoft.engine.connectors.TestConnectorWithOutput;
import org.bonitasoft.engine.connectors.TestExternalConnector;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

import com.bonitasoft.engine.BPMTestSPUtil;
import com.bonitasoft.engine.bpm.parameter.ParameterInstance;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
@SuppressWarnings("javadoc")
public class RemoteConnectorExecutionTestSP extends ConnectorExecutionTest {

    private static final String FLOWNODE = "flowNode";

    private static final String CONNECTOR_INPUT_NAME = "input1";

    private static final String VALUE_INPUT1 = "Lily";

    private static final String VALUE_INPUT2 = "Lucy";

    private static final String VALUE_INPUT3 = "Mett";

    private static final String MAIN_EXP_CONTENT = "'welcome '+valueOfInput1+' and '+valueOfInput2+' and '+valueOfInput3";

    private static final String NAME_INPUT1 = "valueOfInput1";

    private static final String NAME_INPUT2 = "valueOfInput2";

    private static final String NAME_INPUT3 = "valueOfInput3";

    private static final String NAME_INPUT_MAIN = "param1";

    private static final String RESULT_CONTENT = "welcome Lily and Lucy and Mett";

    @Test
    public void getConnectorInstancesOnActivity() throws Exception {
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression("none");
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addShortTextData("myVar", defaultValueExpression).addUserTask("step2", ACTOR_NAME);
        final UserTaskDefinitionBuilder addUserTask = designProcessDefinition.addUserTask("step1", ACTOR_NAME);
        addUserTask.addConnector("myConnector1", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER).addInput("kind",
                input1Expression);
        addUserTask.addConnector("myConnector2", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_FINISH).addInput("kind",
                input1Expression);
        addUserTask.addConnector("myConnector3", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER).addInput("kind",
                input1Expression);
        addUserTask.addConnector("myConnector4", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_FINISH).addInput("kind",
                input1Expression);
        addUserTask.addConnector("myConnector5", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER).addInput("kind",
                input1Expression);
        addUserTask.addConnector("myConnector6", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_FINISH).addInput("kind",
                input1Expression);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorThatThrowException(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");
        final long step2Id = waitForUserTask(processInstance, "step2");
        final List<ConnectorInstance> connectorInstances = getProcessAPI().getConnectorInstancesOfActivity(step1Id, 0, 10, ConnectorInstanceCriterion.DEFAULT);
        assertThat(connectorInstances, nameAre("myConnector1", "myConnector2", "myConnector3", "myConnector4", "myConnector5", "myConnector6"));
        assertTrue(getProcessAPI().getConnectorInstancesOfActivity(step2Id, 0, 10, ConnectorInstanceCriterion.DEFAULT).isEmpty());
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "SubProcess" }, jira = "ENGINE-1725")
    @Test
    public void getConnectorInstancesOnActivityOnSubProcess() throws Exception {
        // Main process
        final Expression processVersionExpr = new ExpressionBuilder().createConstantStringExpression(PROCESS_VERSION);
        final ProcessDefinitionBuilderExt mainProcessDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("Main", PROCESS_VERSION);
        mainProcessDefinitionBuilder.addActor(ACTOR_NAME);
        mainProcessDefinitionBuilder.addStartEvent("MainStart").addEndEvent("MainEnd");
        // CallActivity
        final Expression sendExpr = new ExpressionBuilder().createConstantStringExpression("SubProcess");
        mainProcessDefinitionBuilder.addCallActivity("CallActivity", sendExpr, processVersionExpr);
        // Transitions
        mainProcessDefinitionBuilder.addTransition("MainStart", "CallActivity").addTransition("CallActivity", "MainEnd");
        final ProcessDefinition mainProcessDefinition = deployAndEnableProcessWithActor(mainProcessDefinitionBuilder.done(), ACTOR_NAME, user);

        // SubProcess
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression("plop");
        final ProcessDefinitionBuilderExt subProcessDefinitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("SubProcess", PROCESS_VERSION);
        subProcessDefinitionBuilder.addActor(ACTOR_NAME);
        subProcessDefinitionBuilder.addStartEvent("SubStart").addEndEvent("SubEnd");
        subProcessDefinitionBuilder.addUserTask("StepWithConnector", ACTOR_NAME)
                .addConnector("myConnector1", TEST_CONNECTOR_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER).addInput(CONNECTOR_INPUT_NAME, input1Expression);
        subProcessDefinitionBuilder.addUserTask("Step2", ACTOR_NAME);
        // Transitions
        subProcessDefinitionBuilder.addTransition("SubStart", "StepWithConnector").addTransition("StepWithConnector", "Step2").addTransition("Step2", "SubEnd");
        final ProcessDefinition subProcessDefinition = deployAndEnableProcessWithActorAndTestConnector(subProcessDefinitionBuilder, ACTOR_NAME, user);

        getProcessAPI().startProcess(mainProcessDefinition.getId());
        waitForUserTaskAndExecuteIt("StepWithConnector", user);
        waitForUserTask("Step2");
        disableAndDeleteProcess(mainProcessDefinition, subProcessDefinition);
    }

    @Test
    public void getConnectorInstancesOnProcess() throws Exception {
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression("none");
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addShortTextData("myVar", defaultValueExpression);
        designProcessDefinition.addUserTask("step1", ACTOR_NAME);
        designProcessDefinition.addConnector("myConnector1", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER).addInput(
                "kind", input1Expression);
        designProcessDefinition.addConnector("myConnector2", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_FINISH).addInput(
                "kind", input1Expression);
        designProcessDefinition.addConnector("myConnector3", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER).addInput(
                "kind", input1Expression);
        designProcessDefinition.addConnector("myConnector4", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_FINISH).addInput(
                "kind", input1Expression);
        designProcessDefinition.addConnector("myConnector5", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER).addInput(
                "kind", input1Expression);
        designProcessDefinition.addConnector("myConnector6", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_FINISH).addInput(
                "kind", input1Expression);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorThatThrowException(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "step1");
        final List<ConnectorInstance> connectorInstances = getProcessAPI().getConnectorInstancesOfProcess(processInstance.getId(), 0, 10,
                ConnectorInstanceCriterion.DEFAULT);
        assertThat(connectorInstances, nameAre("myConnector1", "myConnector2", "myConnector3", "myConnector4", "myConnector5", "myConnector6"));
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void setConnectorState() throws Exception {
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression("normal");
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");
        final String dataName = "myVar";

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addShortTextData(dataName, defaultValueExpression);
        designProcessDefinition
                .addAutomaticTask("step1")
                .addConnector("myConnector", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER)
                .addInput("kind", input1Expression)
                .addOutput(new LeftOperandBuilder().createNewInstance().setName(dataName).done(), OperatorType.ASSIGNMENT, "=", "",
                        new ExpressionBuilder().createInputExpression("output1", String.class.getName()));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorThatThrowException(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance waitForTaskToFail = waitForTaskToFail(processInstance);
        assertEquals("step1", waitForTaskToFail.getName());
        List<ConnectorInstance> connectorInstances = getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail.getId(), 0, 10,
                ConnectorInstanceCriterion.DEFAULT);
        ConnectorInstance failedConnector = connectorInstances.get(0);
        assertEquals(ConnectorState.FAILED, failedConnector.getState());
        getProcessAPI().setConnectorInstanceState(failedConnector.getId(), ConnectorStateReset.TO_RE_EXECUTE);
        connectorInstances = getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail.getId(), 0, 10, ConnectorInstanceCriterion.DEFAULT);
        failedConnector = connectorInstances.get(0);
        assertEquals(ConnectorState.TO_RE_EXECUTE, failedConnector.getState());
        getProcessAPI().setConnectorInstanceState(failedConnector.getId(), ConnectorStateReset.SKIPPED);
        connectorInstances = getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail.getId(), 0, 10, ConnectorInstanceCriterion.DEFAULT);
        failedConnector = connectorInstances.get(0);
        assertEquals(ConnectorState.SKIPPED, failedConnector.getState());
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.CONNECTOR, keywords = { "connector instance", "connector state" }, jira = "")
    @Test
    public void resetConnectorInstancesState() throws Exception {
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression("normal");
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");

        final String dataName = "myVar";

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addShortTextData(dataName, defaultValueExpression);
        final AutomaticTaskDefinitionBuilder autoTask = designProcessDefinition.addAutomaticTask("step1");
        autoTask.addConnector("myConnector", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER)
                .addInput("kind", input1Expression)
                .addOutput(new LeftOperandBuilder().createNewInstance().setName(dataName).done(), OperatorType.ASSIGNMENT, "=", "",
                        new ExpressionBuilder().createInputExpression("output1", String.class.getName()));
        autoTask.addConnector("myConnector2", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER).addInput("kind",
                new ExpressionBuilder().createConstantStringExpression("invalidInputParam"));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorThatThrowException(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance waitForTaskToFail = waitForTaskToFail(processInstance);
        assertEquals("step1", waitForTaskToFail.getName());
        List<ConnectorInstance> connectorInstances = getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail.getId(), 0, 10,
                ConnectorInstanceCriterion.DEFAULT);
        // First connector must have failed:
        assertEquals(ConnectorState.FAILED, connectorInstances.get(0).getState());
        // Second connector must then not have been executed:
        assertEquals(ConnectorState.TO_BE_EXECUTED, connectorInstances.get(1).getState());

        final Map<Long, ConnectorStateReset> connectorsToReset = new HashMap<Long, ConnectorStateReset>(2);
        connectorsToReset.put(connectorInstances.get(0).getId(), ConnectorStateReset.TO_RE_EXECUTE);
        // TODO: can we reset the connector instance state if it has never be executed?:
        connectorsToReset.put(connectorInstances.get(1).getId(), ConnectorStateReset.TO_RE_EXECUTE);
        try {
            getProcessAPI().setConnectorInstanceState(connectorsToReset);
            connectorInstances = getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail.getId(), 0, 10, ConnectorInstanceCriterion.DEFAULT);
            for (final ConnectorInstance connectorInstance : connectorInstances) {
                assertEquals(ConnectorState.TO_RE_EXECUTE, connectorInstance.getState());
            }
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Test(expected = UpdateException.class)
    public void setConnectorStateOnUnkownConnector() throws Exception {
        getProcessAPI().setConnectorInstanceState(-123456789l, ConnectorStateReset.SKIPPED);
    }

    @Test
    public void replayActivityWithConnectorToReExecuteAndThenSkipped() throws Exception {
        final Expression normal = new ExpressionBuilder().createConstantStringExpression("normal");
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addShortTextData("myVar", defaultValueExpression);
        designProcessDefinition.addAutomaticTask("step1")
                .addConnector("myConnector", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER).addInput("kind", normal);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorThatThrowException(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance waitForTaskToFail = waitForTaskToFail(processInstance);

        // put in TO BE EXECUTED and restart the task
        getProcessAPI().setConnectorInstanceState(
                getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail.getId(), 0, 1, ConnectorInstanceCriterion.DEFAULT).get(0).getId(),
                ConnectorStateReset.TO_RE_EXECUTE);
        getProcessAPI().replayActivity(waitForTaskToFail.getId());
        final ActivityInstance waitForTaskToFail2 = waitForTaskToFail(processInstance);

        // failed again, put in SKIPPED and restart the task
        getProcessAPI().setConnectorInstanceState(
                getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail2.getId(), 0, 1, ConnectorInstanceCriterion.DEFAULT).get(0).getId(),
                ConnectorStateReset.SKIPPED);
        getProcessAPI().replayActivity(waitForTaskToFail2.getId());

        // should finish
        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.CONNECTOR, keywords = { "connector instance", "connector state", "activity replay" }, jira = "")
    @Test
    public void replayActivityWithResetConnectorStates() throws Exception {
        final Expression normal = new ExpressionBuilder().createConstantStringExpression("normal");
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addShortTextData("myVar", defaultValueExpression);
        designProcessDefinition.addAutomaticTask("step1")
                .addConnector("myConnector", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER).addInput("kind", normal);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorThatThrowException(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance waitForTaskToFail = waitForTaskToFail(processInstance);

        final List<ConnectorInstance> connectorInstances = getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail.getId(), 0, 10,
                ConnectorInstanceCriterion.DEFAULT);
        Map<Long, ConnectorStateReset> connectorsToReset = new HashMap<Long, ConnectorStateReset>(1);
        connectorsToReset.put(connectorInstances.get(0).getId(), ConnectorStateReset.TO_RE_EXECUTE);
        // put in TO_RE_EXECUTE and restart the task:
        getProcessAPI().replayActivity(waitForTaskToFail.getId(), connectorsToReset);

        final ActivityInstance waitForTaskToFail2 = waitForTaskToFail(processInstance);

        connectorsToReset = new HashMap<Long, ConnectorStateReset>(1);
        connectorsToReset.put(connectorInstances.get(0).getId(), ConnectorStateReset.SKIPPED);
        // failed again, put in SKIPPED and restart the task:
        getProcessAPI().replayActivity(waitForTaskToFail2.getId(), connectorsToReset);

        // should finish
        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void replayActivityWithMultipleConnectors() throws Exception {
        final Expression normal = new ExpressionBuilder().createConstantStringExpression("normal");
        final Expression none = new ExpressionBuilder().createConstantStringExpression("none");
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addShortTextData("myVar", defaultValueExpression);
        final AutomaticTaskDefinitionBuilder addAutomaticTask = designProcessDefinition.addAutomaticTask("step1");
        addAutomaticTask.addConnector("myConnector1", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER)
                .addInput("kind", none);
        addAutomaticTask.addConnector("myConnector2", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER).addInput("kind",
                normal);
        addAutomaticTask.addConnector("myConnector3", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER)
                .addInput("kind", none);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorThatThrowException(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance waitForTaskToFail = waitForTaskToFail(processInstance);

        // put in TO BE EXECUTED and restart the task
        getProcessAPI().setConnectorInstanceState(
                getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail.getId(), 0, 3, ConnectorInstanceCriterion.DEFAULT).get(1).getId(),
                ConnectorStateReset.TO_RE_EXECUTE);
        getProcessAPI().replayActivity(waitForTaskToFail.getId());
        final ActivityInstance waitForTaskToFail2 = waitForTaskToFail(processInstance);

        // failed again, put in SKIPPED and restart the task
        getProcessAPI().setConnectorInstanceState(
                getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail2.getId(), 0, 3, ConnectorInstanceCriterion.DEFAULT).get(1).getId(),
                ConnectorStateReset.SKIPPED);
        getProcessAPI().replayActivity(waitForTaskToFail2.getId());

        // should finish
        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void replayActivityWithMultipleConnectorsOnFinish() throws Exception {
        final Expression normal = new ExpressionBuilder().createConstantStringExpression("normal");
        final Expression none = new ExpressionBuilder().createConstantStringExpression("none");
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addShortTextData("myVar", defaultValueExpression);
        final AutomaticTaskDefinitionBuilder addAutomaticTask = designProcessDefinition.addAutomaticTask("step1");
        addAutomaticTask.addConnector("myConnector1", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER)
                .addInput("kind", none);
        addAutomaticTask.addConnector("myConnector2", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_FINISH).addInput("kind",
                normal);
        addAutomaticTask.addConnector("myConnector3", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_FINISH).addInput("kind",
                none);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorThatThrowException(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance waitForTaskToFail = waitForTaskToFail(processInstance);

        // put in TO BE EXECUTED and restart the task
        getProcessAPI().setConnectorInstanceState(
                getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail.getId(), 0, 3, ConnectorInstanceCriterion.DEFAULT).get(1).getId(),
                ConnectorStateReset.TO_RE_EXECUTE);
        getProcessAPI().replayActivity(waitForTaskToFail.getId());
        final ActivityInstance waitForTaskToFail2 = waitForTaskToFail(processInstance);

        // failed again, put in SKIPPED and restart the task
        getProcessAPI().setConnectorInstanceState(
                getProcessAPI().getConnectorInstancesOfActivity(waitForTaskToFail2.getId(), 0, 3, ConnectorInstanceCriterion.DEFAULT).get(1).getId(),
                ConnectorStateReset.SKIPPED);
        getProcessAPI().replayActivity(waitForTaskToFail2.getId());

        // should finish
        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.CONNECTOR, keywords = { "connector instance", "connector state", "activity replay" }, jira = "")
    @Test(expected = ActivityExecutionException.class)
    public void replayActivityWithUnresolvedFailedConnectors() throws Exception {
        final Expression normal = new ExpressionBuilder().createConstantStringExpression("normal");
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");
        final String dataName = "myVar";

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addShortTextData(dataName, defaultValueExpression);
        designProcessDefinition
                .addAutomaticTask("step1")
                .addConnector("myConnector", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER)
                .addInput("kind", normal)
                .addOutput(new LeftOperandBuilder().createNewInstance().setName(dataName).done(), OperatorType.ASSIGNMENT, "=", "",
                        new ExpressionBuilder().createInputExpression("output1", String.class.getName()));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorThatThrowException(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance waitForTaskToFail = waitForTaskToFail(processInstance);
        // just restart, should not work
        try {
            getProcessAPI().replayActivity(waitForTaskToFail.getId());
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.CONNECTOR, keywords = { "connector instance", "connector instance with failure information",
            "exception on connector execution" }, jira = "")
    @Test
    public void getConnectorWithFailureInformationOnConnectorExecution() throws Exception {
        final ProcessDefinition processDefinition = deployAndEnableProcessWithFaillingConnector();
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance waitForTaskToFail = waitForTaskToFail(processInstance);
        final SearchResult<ConnectorInstance> searchResult = getProcessAPI().searchConnectorInstances(
                getFirst100ConnectorInstanceSearchOptions(waitForTaskToFail.getId(), FLOWNODE).done());
        assertEquals(1, searchResult.getCount());
        final ConnectorInstance connectorInstance = searchResult.getResult().get(0);
        final ConnectorInstanceWithFailureInfo connectorInstanceWithFailureInfo = getProcessAPI().getConnectorInstanceWithFailureInformation(
                connectorInstance.getId());
        assertEquals(ConnectorState.FAILED, connectorInstanceWithFailureInfo.getState());
        assertEquals(TestConnectorThatThrowException.BUSINESS_LOGIC_EXCEPTION_MESSAGE, connectorInstanceWithFailureInfo.getExceptionMessage());
        final String stackTrace = connectorInstanceWithFailureInfo.getStackTrace();
        assertTrue(stackTrace
                .contains("org.bonitasoft.engine.connector.exception.SConnectorException: java.util.concurrent.ExecutionException: org.bonitasoft.engine.connector.exception.SConnectorException: org.bonitasoft.engine.connector.ConnectorException: "
                        + TestConnectorThatThrowException.BUSINESS_LOGIC_EXCEPTION_MESSAGE));
        assertTrue(stackTrace
                .contains("at org.bonitasoft.engine.core.connector.impl.ConnectorServiceImpl.executeConnectorInClassloader(ConnectorServiceImpl.java:"));
        assertTrue(stackTrace.contains("at org.bonitasoft.engine.core.connector.impl.ConnectorServiceImpl.executeConnector(ConnectorServiceImpl.java:"));
        assertTrue(stackTrace.contains("at org.bonitasoft.engine.connector.ConnectorServiceDecorator.executeConnector(ConnectorServiceDecorator.java:"));
        assertTrue(stackTrace.contains("at org.bonitasoft.engine.execution.work.ExecuteConnectorWork.work(ExecuteConnectorWork.java:"));
        assertTrue(stackTrace
                .contains("at org.bonitasoft.engine.execution.work.FailureHandlingBonitaWork.work(FailureHandlingBonitaWork.java:"));

        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployAndEnableProcessWithFaillingConnector() throws InvalidExpressionException, BonitaException, IOException {
        final Expression normal = new ExpressionBuilder().createConstantStringExpression(TestConnectorThatThrowException.NORMAL);
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");
        final String dataName = "myVar";

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addShortTextData(dataName, defaultValueExpression);
        designProcessDefinition
                .addAutomaticTask("step1")
                .addConnector("myConnector", TEST_CONNECTOR_THAT_THROW_EXCEPTION_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER)
                .addInput("kind", normal)
                .addOutput(new LeftOperandBuilder().createNewInstance().setName(dataName).done(), OperatorType.ASSIGNMENT, "=", "",
                        new ExpressionBuilder().createInputExpression("output1", String.class.getName()));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorThatThrowException(designProcessDefinition, ACTOR_NAME, user);
        return processDefinition;
    }

    @Test(expected = NotFoundException.class)
    public void replayUnknownActivity() throws Exception {
        getProcessAPI().replayActivity(-123456789l);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Operation" }, story = "execute connector on activity instance and execute operations", jira = "ENGINE-1037")
    @Test
    public void executeConnectorOnActivityInstanceWithOperations() throws Exception {
        final Expression input1Expression = new ExpressionBuilder().createInputExpression("valueOfInput1", String.class.getName());
        final Expression input2Expression = new ExpressionBuilder().createInputExpression("valueOfInput2", String.class.getName());
        final Expression dataDefaultValueExpression = new ExpressionBuilder().createConstantStringExpression("Mett");
        final String dataName = "dataName";
        final Expression dataExpression = new ExpressionBuilder().createDataExpression(dataName, String.class.getName());

        final String welcomeMessage = "param1";
        final Expression welcomeMessageExpression = new ExpressionBuilder().createGroovyScriptExpression(welcomeMessage,
                "'welcome '+valueOfInput1+' and '+valueOfInput2+' and '+dataName", String.class.getName(),
                Arrays.asList(input1Expression, input2Expression, dataExpression));

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME,
                PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addUserTask("step1", ACTOR_NAME).addShortTextData(dataName, dataDefaultValueExpression);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithExternalTestConnectorAndActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");

        // execute connector with operations:
        // connector return param1="welcome Lily and Lucy and Mett"
        // operations: put "Jack" in data valueOfInput3, param1 in "externalData" and "John" in "externalDataConst"
        // Create Operation map:
        final List<Operation> operations = new ArrayList<Operation>();
        operations.add(new OperationBuilder().createSetDataOperation(dataName, new ExpressionBuilder().createConstantStringExpression("Jack")));
        operations
                .add(new OperationBuilder().createNewInstance().setLeftOperand("externalData", true)
                        .setRightOperand(new ExpressionBuilder().createInputExpression(welcomeMessage, String.class.getName()))
                        .setType(OperatorType.ASSIGNMENT).done());
        operations.add(new OperationBuilder().createNewInstance().setLeftOperand("externalDataConst", true)
                .setRightOperand(new ExpressionBuilder().createConstantStringExpression("John")).setType(OperatorType.ASSIGNMENT).done());

        final Map<String, Expression> connectorInputParameters = new HashMap<String, Expression>();
        connectorInputParameters.put(welcomeMessage, welcomeMessageExpression);

        final Map<String, Map<String, Serializable>> inputValues = new HashMap<String, Map<String, Serializable>>();
        final Map<String, Serializable> values = buildInputValues(Arrays.asList("valueOfInput1", "valueOfInput2"), Arrays.asList("Lily", "Lucy"));
        inputValues.put(welcomeMessage, values);

        final Map<String, Serializable> contexts = new HashMap<String, Serializable>();
        final Map<String, Serializable> res = getProcessAPI().executeConnectorOnActivityInstance(ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_ID,
                ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_VERSION, connectorInputParameters, inputValues, operations, contexts, step1Id);

        assertEquals("welcome Lily and Lucy and Mett", res.get("externalData"));
        assertEquals("John", res.get("externalDataConst"));
        assertEquals("Jack", getProcessAPI().getActivityDataInstance(dataName, step1Id).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Operation", "EngineExecutionContext" }, story = "Execute connector on activity instance with using EngineExecutionContext.", jira = "BS-8022")
    @Test
    public void executeConnectorOnActivityInstanceWithOperationsWithEngineExecutionContext() throws Exception {
        final Operation operation = new OperationBuilder().createNewInstance().setLeftOperand("externalData", true)
                .setRightOperand(new ExpressionBuilder().createInputExpression(ExpressionConstants.PROCESS_DEFINITION_ID.getEngineConstantName(),
                        Long.class.getName()))
                .setType(OperatorType.ASSIGNMENT).done();

        final ProcessDefinitionBuilderExt processDefinitionBuilderExt = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilderExt.addActor(ACTOR_NAME);
        processDefinitionBuilderExt.addUserTask("step1", ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorEngineExecutionContext(processDefinitionBuilderExt,
                ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");

        final Map<String, Serializable> res = getProcessAPI().executeConnectorOnActivityInstance(
                "org.bonitasoft.connector.testConnectorEngineExecutionContext", "1.0", Collections.<String, Expression> emptyMap(),
                Collections.<String, Map<String, Serializable>> emptyMap(), Collections.singletonList(operation),
                Collections.<String, Serializable> emptyMap(), step1Id);
        assertEquals(processDefinition.getId(), res.get("externalData"));

        // Clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Operation" }, story = "execute connector on activity instance and execute operations, with pattern expression and dependency data", jira = "BS-8049")
    @Test
    public void executeConnectorOnActivityInstanceWithOperationAndWithPatternExpression() throws Exception {
        final String dataName = "dataName";
        final Expression dataExpression = new ExpressionBuilder().createDataExpression(dataName, Long.class.getName());

        final String patternName = "param1";
        final Expression patternExpression = new ExpressionBuilder().createPatternExpression(patternName, "${dataName}", dataExpression);

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME,
                PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addUserTask("step1", ACTOR_NAME).addLongData(dataName, new ExpressionBuilder().createConstantLongExpression(5L));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithExternalTestConnectorAndActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");

        final List<Operation> operations = new ArrayList<Operation>();
        operations
                .add(new OperationBuilder().createNewInstance().setLeftOperand("externalData", true)
                        .setRightOperand(new ExpressionBuilder().createInputExpression(patternName, String.class.getName()))
                        .setType(OperatorType.ASSIGNMENT).done());

        final Map<String, Expression> connectorInputParameters = new HashMap<String, Expression>();
        connectorInputParameters.put(patternName, patternExpression);

        final Map<String, Map<String, Serializable>> inputValues = new HashMap<String, Map<String, Serializable>>();
        inputValues.put(patternName, Collections.<String, Serializable> emptyMap());

        final Map<String, Serializable> contexts = new HashMap<String, Serializable>();
        final Map<String, Serializable> res = getProcessAPI().executeConnectorOnActivityInstance(ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_ID,
                ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_VERSION, connectorInputParameters, inputValues, operations, contexts, step1Id);

        assertEquals("5", res.get("externalData"));

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Operation" }, story = "execute connector on completed activity instance and execute operations", jira = "ENGINE-1037")
    @Test
    public void executeConnectorOnCompletedActivityInstanceWithOperations() throws Exception {
        final Expression input1Expression = new ExpressionBuilder().createInputExpression("valueOfInput1", String.class.getName());
        final Expression input2Expression = new ExpressionBuilder().createInputExpression("valueOfInput2", String.class.getName());
        final Expression input3DefaultExpression = new ExpressionBuilder().createConstantStringExpression("Mett");
        final Expression input3Expression = new ExpressionBuilder().createDataExpression("valueOfInput3", String.class.getName());
        final Expression mainExp = new ExpressionBuilder().createGroovyScriptExpression("param1",
                "'welcome '+valueOfInput1+' and '+valueOfInput2+' and '+valueOfInput3",
                String.class.getName(), Arrays.asList(input1Expression, input2Expression, input3Expression));

        // process with data "Mett"
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME,
                PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addUserTask("step1", ACTOR_NAME).addShortTextData("valueOfInput3", input3DefaultExpression);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithExternalTestConnectorAndActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        waitForProcessToFinish(processInstance);

        // execute connector with operations:
        // connector return param1="welcome Lily and Lucy and Mett"
        // operations: put param1 in "externalData" and "John" in "externalDataConst"
        // Create Operation map:
        final List<Operation> operations = new ArrayList<Operation>();
        final Map<String, Serializable> contexts = new HashMap<String, Serializable>();
        operations.add(new OperationBuilder().createNewInstance().setLeftOperand("externalData", true)
                .setRightOperand(new ExpressionBuilder().createInputExpression("param1", String.class.getName())).setType(OperatorType.ASSIGNMENT).done());
        operations.add(new OperationBuilder().createNewInstance().setLeftOperand("externalDataConst", true)
                .setRightOperand(new ExpressionBuilder().createConstantStringExpression("John")).setType(OperatorType.ASSIGNMENT).done());
        final Map<String, Expression> connectorInputParameters = getConnectorInputParameters("param1", mainExp);
        final Map<String, Map<String, Serializable>> inputValues = getInputValues("param1", Arrays.asList("valueOfInput1", "valueOfInput2"),
                Arrays.asList("Lily", "Lucy"));

        final Map<String, Serializable> res = getProcessAPI().executeConnectorOnCompletedActivityInstance(ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_ID,
                ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_VERSION, connectorInputParameters, inputValues, operations, contexts, step1Id);

        assertEquals("welcome Lily and Lucy and Mett", res.get("externalData"));
        assertEquals("John", res.get("externalDataConst"));
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Operation", "EngineExecutionContext" }, story = "Execute connector on completed activity instance with using EngineExecutionContext.", jira = "BS-8022")
    @Test
    public void executeConnectorOnCompletedActivityInstanceWithOperationsWithEngineExecutionContext() throws Exception {
        final Operation operation = new OperationBuilder().createNewInstance().setLeftOperand("externalData", true)
                .setRightOperand(new ExpressionBuilder().createInputExpression(ExpressionConstants.PROCESS_DEFINITION_ID.getEngineConstantName(),
                        Long.class.getName()))
                .setType(OperatorType.ASSIGNMENT).done();

        final ProcessDefinitionBuilderExt processDefinitionBuilderExt = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilderExt.addActor(ACTOR_NAME);
        processDefinitionBuilderExt.addUserTask("step1", ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorEngineExecutionContext(processDefinitionBuilderExt,
                ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        waitForProcessToFinish(processInstance);

        final Map<String, Serializable> res = getProcessAPI().executeConnectorOnCompletedActivityInstance(
                "org.bonitasoft.connector.testConnectorEngineExecutionContext", "1.0", Collections.<String, Expression> emptyMap(),
                Collections.<String, Map<String, Serializable>> emptyMap(), Collections.singletonList(operation),
                Collections.<String, Serializable> emptyMap(), step1Id);

        assertEquals(processDefinition.getId(), res.get("externalData"));

        // Clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Operation" }, story = "execute connector on activity instance and execute operations", jira = "ENGINE-1037")
    @Test
    public void executeConnectorOnCompletedProcessInstanceWithOperations() throws Exception {
        final Expression input1Expression = new ExpressionBuilder().createInputExpression("valueOfInput1", String.class.getName());
        final Expression input2Expression = new ExpressionBuilder().createInputExpression("valueOfInput2", String.class.getName());
        final Expression input3DefaultExpression = new ExpressionBuilder().createConstantStringExpression("Mett");
        final Expression input3Expression = new ExpressionBuilder().createDataExpression("valueOfInput3", String.class.getName());
        final Expression mainExp = new ExpressionBuilder().createGroovyScriptExpression("param1",
                "'welcome '+valueOfInput1+' and '+valueOfInput2+' and '+valueOfInput3", String.class.getName(),
                Arrays.asList(input1Expression, input2Expression, input3Expression));

        // process with data "Mett"
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME,
                PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addShortTextData("valueOfInput3", input3DefaultExpression);
        designProcessDefinition.addUserTask("step1", ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithExternalTestConnectorAndActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        waitForProcessToFinish(processInstance);

        // execute connector with operations:
        // connector return param1="welcome Lily and Lucy and Mett"
        // operations: put "Jack" in data valueOfInput3, param1 in "externalData" and "John" in "externalDataConst"
        // Create Operation map:
        final List<Operation> operations = new ArrayList<Operation>();
        // set valueOfInput3
        final Map<String, Serializable> contexts = new HashMap<String, Serializable>();
        operations.add(new OperationBuilder().createNewInstance().setLeftOperand("externalData", true)
                .setRightOperand(new ExpressionBuilder().createInputExpression("param1", String.class.getName())).setType(OperatorType.ASSIGNMENT).done());
        operations.add(new OperationBuilder().createNewInstance().setLeftOperand("externalDataConst", true)
                .setRightOperand(new ExpressionBuilder().createConstantStringExpression("John")).setType(OperatorType.ASSIGNMENT).done());
        final Map<String, Expression> connectorInputParameters = getConnectorInputParameters("param1", mainExp);
        final Map<String, Map<String, Serializable>> inputValues = getInputValues("param1", Arrays.asList("valueOfInput1", "valueOfInput2"),
                Arrays.asList("Lily", "Lucy"));

        final Map<String, Serializable> res = getProcessAPI()
                .executeConnectorOnCompletedProcessInstance(ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_ID,
                        ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_VERSION, connectorInputParameters, inputValues, operations, contexts,
                        processInstance.getId());
        assertEquals("welcome Lily and Lucy and Mett", res.get("externalData"));
        assertEquals("John", res.get("externalDataConst"));

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Operation", "EngineExecutionContext" }, story = "Execute connector on process instance with using EngineExecutionContext.", jira = "BS-8022")
    @Test
    public void executeConnectorOnCompletedProcessInstanceWithOperationsWithEngineExecutionContext() throws Exception {
        final Operation operation = new OperationBuilder().createNewInstance().setLeftOperand("externalData", true)
                .setRightOperand(new ExpressionBuilder().createInputExpression(ExpressionConstants.PROCESS_DEFINITION_ID.getEngineConstantName(),
                        Long.class.getName()))
                .setType(OperatorType.ASSIGNMENT).done();

        final ProcessDefinitionBuilderExt processDefinitionBuilderExt = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilderExt.addActor(ACTOR_NAME);
        processDefinitionBuilderExt.addUserTask("step1", ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorEngineExecutionContext(processDefinitionBuilderExt,
                ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        waitForProcessToFinish(processInstance);

        final Map<String, Serializable> res = getProcessAPI().executeConnectorOnCompletedProcessInstance(
                "org.bonitasoft.connector.testConnectorEngineExecutionContext", "1.0", Collections.<String, Expression> emptyMap(),
                Collections.<String, Map<String, Serializable>> emptyMap(), Collections.singletonList(operation),
                Collections.<String, Serializable> emptyMap(), processInstance.getId());

        assertEquals(processDefinition.getId(), res.get("externalData"));

        // Clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Operation" }, story = "Execute connector on process instance and execute operations", jira = "ENGINE-1037")
    @Test
    public void executeConnectorOnProcessInstanceWithOperations() throws Exception {
        final Expression input1Expression = new ExpressionBuilder().createInputExpression("valueOfInput1", String.class.getName());
        final Expression input2Expression = new ExpressionBuilder().createInputExpression("valueOfInput2", String.class.getName());
        final Expression input3DefaultExpression = new ExpressionBuilder().createConstantStringExpression("Mett");
        final Expression input3Expression = new ExpressionBuilder().createDataExpression("valueOfInput3", String.class.getName());
        final Expression mainExp = new ExpressionBuilder().createGroovyScriptExpression("param1",
                "'welcome '+valueOfInput1+' and '+valueOfInput2+' and '+valueOfInput3", String.class.getName(),
                Arrays.asList(input1Expression, input2Expression, input3Expression));

        // process with data "Mett"
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME,
                PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addShortTextData("valueOfInput3", input3DefaultExpression);
        designProcessDefinition.addUserTask("step1", ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithExternalTestConnectorAndActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "step1");

        // execute connector with operations:
        // connector return param1="welcome Lily and Lucy and Mett"
        // operations: put "Jack" in data valueOfInput3, param1 in "externalData" and "John" in "externalDataConst"
        // Create Operation map:
        final List<Operation> operations = new ArrayList<Operation>();
        // set valueOfInput3
        final Map<String, Serializable> contexts = new HashMap<String, Serializable>();
        operations.add(new OperationBuilder().createSetDataOperation("valueOfInput3", new ExpressionBuilder().createConstantStringExpression("Jack")));
        operations.add(new OperationBuilder().createNewInstance().setLeftOperand("externalData", true)
                .setRightOperand(new ExpressionBuilder().createInputExpression("param1", String.class.getName())).setType(OperatorType.ASSIGNMENT).done());
        operations.add(new OperationBuilder().createNewInstance().setLeftOperand("externalDataConst", true)
                .setRightOperand(new ExpressionBuilder().createConstantStringExpression("John")).setType(OperatorType.ASSIGNMENT).done());
        final Map<String, Expression> connectorInputParameters = getConnectorInputParameters("param1", mainExp);
        final Map<String, Map<String, Serializable>> inputValues = getInputValues("param1", Arrays.asList("valueOfInput1", "valueOfInput2"),
                Arrays.asList("Lily", "Lucy"));

        final Map<String, Serializable> res = getProcessAPI()
                .executeConnectorOnProcessInstance(ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_ID,
                        ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_VERSION, connectorInputParameters, inputValues, operations, contexts,
                        processInstance.getId());

        assertEquals("welcome Lily and Lucy and Mett", res.get("externalData"));
        assertEquals("John", res.get("externalDataConst"));
        assertEquals("Jack", getProcessAPI().getProcessDataInstance("valueOfInput3", processInstance.getId()).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Operation", "EngineExecutionContext" }, story = "Execute connector on process instance with using EngineExecutionContext.", jira = "BS-8022")
    @Test
    public void executeConnectorOnProcessInstanceWithOperationsWithEngineExecutionContext() throws Exception {
        final Operation operation = new OperationBuilder().createNewInstance().setLeftOperand("externalData", true)
                .setRightOperand(new ExpressionBuilder().createInputExpression(ExpressionConstants.PROCESS_DEFINITION_ID.getEngineConstantName(),
                        Long.class.getName()))
                .setType(OperatorType.ASSIGNMENT).done();

        final ProcessDefinitionBuilderExt processDefinitionBuilderExt = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processDefinitionBuilderExt.addActor(ACTOR_NAME);
        processDefinitionBuilderExt.addUserTask("step1", ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorEngineExecutionContext(processDefinitionBuilderExt,
                ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "step1");

        final Map<String, Serializable> res = getProcessAPI().executeConnectorOnProcessInstance(
                "org.bonitasoft.connector.testConnectorEngineExecutionContext", "1.0", Collections.<String, Expression> emptyMap(),
                Collections.<String, Map<String, Serializable>> emptyMap(), Collections.singletonList(operation),
                Collections.<String, Serializable> emptyMap(), processInstance.getId());

        assertEquals(processDefinition.getId(), res.get("externalData"));

        // Clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "At process instanciation" }, story = "Execute connector at process instanciation.", jira = "")
    @Test
    public void executeConnectorAtProcessInstanciation() throws Exception {
        // Input expression
        final Expression input1Expression = new ExpressionBuilder().createInputExpression(NAME_INPUT1, String.class.getName());
        final Expression input2Expression = new ExpressionBuilder().createInputExpression(NAME_INPUT2, String.class.getName());
        // Data expression
        final Expression input3DefaultExpression = new ExpressionBuilder().createConstantStringExpression(VALUE_INPUT3);
        final Expression input3Expression = new ExpressionBuilder().createDataExpression(NAME_INPUT3, String.class.getName());

        // Main Expression
        final Expression mainExp = new ExpressionBuilder().createGroovyScriptExpression(NAME_INPUT_MAIN, MAIN_EXP_CONTENT,
                String.class.getName(), Arrays.asList(input1Expression, input2Expression, input3Expression));

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME,
                PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addUserTask("step0", ACTOR_NAME).addOperation(
                new OperationBuilder().createSetDataOperation(NAME_INPUT3, new ExpressionBuilder().createConstantStringExpression("not Mett")));
        designProcessDefinition.addUserTask("step1", ACTOR_NAME);
        designProcessDefinition.addShortTextData(NAME_INPUT3, input3DefaultExpression);
        designProcessDefinition.addTransition("step0", "step1");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithExternalTestConnectorAndActor(designProcessDefinition, ACTOR_NAME, user);

        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        // check first value is "Mett", then it's changed to "not Mett"
        final long step0Id = waitForUserTask(processInstance, "step0");
        assertEquals("Mett", getProcessAPI().getProcessDataInstance(NAME_INPUT3, processInstance.getId()).getValue());
        assignAndExecuteStep(step0Id, user);
        final long step1Id = waitForUserTask(processInstance, "step1");
        assertEquals("not Mett", getProcessAPI().getProcessDataInstance(NAME_INPUT3, processInstance.getId()).getValue());

        final Map<String, Expression> connectorInputParameters = getConnectorInputParameters(NAME_INPUT_MAIN, mainExp);
        final Map<String, Map<String, Serializable>> inputValues = getInputValues(NAME_INPUT_MAIN, Arrays.asList(NAME_INPUT1, NAME_INPUT2),
                Arrays.asList(VALUE_INPUT1, VALUE_INPUT2));

        final Map<String, Serializable> res = getProcessAPI().executeConnectorAtProcessInstantiation(ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_ID,
                ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_VERSION, connectorInputParameters, inputValues, processInstance.getId());
        // we should have the string finishing with "Mett" and not "no Mett"
        assertEquals(RESULT_CONTENT, res.get(NAME_INPUT_MAIN));
        // check also when finished
        assignAndExecuteStep(step1Id, user);
        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Activity instance" }, story = "Execute connector on an activity instance.", jira = "")
    @Test
    public void executeConnectorOnActivityInstance() throws Exception {
        // Input expression
        final Expression input1Expression = new ExpressionBuilder().createInputExpression(NAME_INPUT1, String.class.getName());
        final Expression input2Expression = new ExpressionBuilder().createInputExpression(NAME_INPUT2, String.class.getName());
        // Data expression
        final Expression input3DefaultExpression = new ExpressionBuilder().createConstantStringExpression(VALUE_INPUT3);
        final Expression input3Expression = new ExpressionBuilder().createDataExpression(NAME_INPUT3, String.class.getName());

        // Main Expression
        final Expression mainExp = new ExpressionBuilder().createGroovyScriptExpression(NAME_INPUT_MAIN, MAIN_EXP_CONTENT,
                String.class.getName(), Arrays.asList(input1Expression, input2Expression, input3Expression));

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addUserTask("step0", ACTOR_NAME).addShortTextData(NAME_INPUT3, input3DefaultExpression);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithExternalTestConnectorAndActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final long step0Id = waitForUserTask(processInstance, "step0");
        final Map<String, Expression> connectorInputParameters = getConnectorInputParameters(NAME_INPUT_MAIN, mainExp);
        final Map<String, Map<String, Serializable>> inputValues = getInputValues(NAME_INPUT_MAIN, Arrays.asList(NAME_INPUT1, NAME_INPUT2),
                Arrays.asList(VALUE_INPUT1, VALUE_INPUT2));

        final Map<String, Serializable> res = getProcessAPI().executeConnectorOnActivityInstance(ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_ID,
                ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_VERSION, connectorInputParameters, inputValues, step0Id);

        assertEquals(RESULT_CONTENT, res.get(NAME_INPUT_MAIN));
        assertTrue((Boolean) res.get("hasBeenValidated"));

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Activity instance", "Loop" }, story = "Execute connector on a loop.", jira = "BS-2090")
    @Test
    public void executeConnectorOnActivityInstanceWithLoop() throws Exception {
        // Input expression
        final Expression input1Expression = new ExpressionBuilder().createInputExpression(NAME_INPUT1, String.class.getName());
        final Expression input2Expression = new ExpressionBuilder().createInputExpression(NAME_INPUT2, String.class.getName());
        // Data expression
        final Expression input3DefaultExpression = new ExpressionBuilder().createConstantStringExpression(VALUE_INPUT3);
        final Expression input3Expression = new ExpressionBuilder().createDataExpression(NAME_INPUT3, String.class.getName());

        // Main Expression
        final Expression mainExp = new ExpressionBuilder().createGroovyScriptExpression(NAME_INPUT_MAIN, MAIN_EXP_CONTENT,
                String.class.getName(), Arrays.asList(input1Expression, input2Expression, input3Expression));

        final Expression condition = new ExpressionBuilder().createConstantBooleanExpression(true);

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME).addEndEvent("End").addTerminateEventTrigger();
        designProcessDefinition.addUserTask("step0", ACTOR_NAME).addLoop(false, condition).addShortTextData(NAME_INPUT3, input3DefaultExpression);
        designProcessDefinition.addTransition("step0", "End");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithExternalTestConnectorAndActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final long step0Id = waitForUserTask(processInstance, "step0");
        final Map<String, Expression> connectorInputParameters = getConnectorInputParameters(NAME_INPUT_MAIN, mainExp);
        final Map<String, Map<String, Serializable>> inputValues = getInputValues(NAME_INPUT_MAIN, Arrays.asList(NAME_INPUT1, NAME_INPUT2),
                Arrays.asList(VALUE_INPUT1, VALUE_INPUT2));

        final Map<String, Serializable> res = getProcessAPI().executeConnectorOnActivityInstance(ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_ID,
                ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_VERSION, connectorInputParameters, inputValues, step0Id);

        assertEquals(RESULT_CONTENT, res.get(NAME_INPUT_MAIN));
        assertTrue((Boolean) res.get("hasBeenValidated"));

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Activity instance", "No serializable output" }, story = "Execute connector on an activity instance with no serializable output.", jira = "ENGINE-823")
    @Test(expected = ConnectorExecutionException.class)
    public void executeConnectorOnActivityInstanceWithNotSerializableOutput() throws Exception {
        // Input expression
        final Expression input1Expression = new ExpressionBuilder().createInputExpression(NAME_INPUT1, String.class.getName());
        final Expression input2Expression = new ExpressionBuilder().createInputExpression(NAME_INPUT2, String.class.getName());
        // Data expression
        final Expression input3DefaultExpression = new ExpressionBuilder().createConstantStringExpression(VALUE_INPUT3);
        final Expression input3Expression = new ExpressionBuilder().createDataExpression(NAME_INPUT3, String.class.getName());

        // Main Expression
        final Expression mainExp = new ExpressionBuilder().createGroovyScriptExpression(NAME_INPUT_MAIN, MAIN_EXP_CONTENT,
                String.class.getName(), Arrays.asList(input1Expression, input2Expression, input3Expression));

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addUserTask("step0", ACTOR_NAME).addShortTextData(NAME_INPUT3, input3DefaultExpression);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithExternalTestConnectorAndActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step0Id = waitForUserTask(processInstance, "step0");

        final Map<String, Expression> connectorInputParameters = getConnectorInputParameters(NAME_INPUT_MAIN, mainExp);
        connectorInputParameters.put("returnNotSerializableOutput", new ExpressionBuilder().createConstantBooleanExpression(true));
        final Map<String, Map<String, Serializable>> inputValues = getInputValues(NAME_INPUT_MAIN, Arrays.asList(NAME_INPUT1, NAME_INPUT2),
                Arrays.asList(VALUE_INPUT1, VALUE_INPUT2));
        inputValues.putAll(getInputValues("returnNotSerializableOutput", null, null));

        try {
            getProcessAPI().executeConnectorOnActivityInstance(ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_ID,
                    ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_VERSION, connectorInputParameters, inputValues, step0Id);
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "On completed activity", "Activity instance" }, story = "Execute connector on completed activity instance.", jira = "")
    @Test
    public void executeConnectorOnCompletedActivityInstance() throws Exception {
        // Input expression
        final Expression input1Expression = new ExpressionBuilder().createInputExpression(NAME_INPUT1, String.class.getName());
        final Expression input2Expression = new ExpressionBuilder().createInputExpression(NAME_INPUT2, String.class.getName());
        // Data expression
        final Expression input3DefaultExpression = new ExpressionBuilder().createConstantStringExpression(VALUE_INPUT3);
        final Expression input3Expression = new ExpressionBuilder().createDataExpression(NAME_INPUT3, String.class.getName());

        // Main Expression
        final Expression mainExp = new ExpressionBuilder().createGroovyScriptExpression(NAME_INPUT_MAIN, MAIN_EXP_CONTENT,
                String.class.getName(), Arrays.asList(input1Expression, input2Expression, input3Expression));

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME,
                PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addUserTask("step0", ACTOR_NAME).addShortTextData(NAME_INPUT3, input3DefaultExpression);
        designProcessDefinition.addUserTask("step1", ACTOR_NAME);
        designProcessDefinition.addTransition("step0", "step1");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithExternalTestConnectorAndActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step0Id = waitForUserTaskAndExecuteIt(processInstance, "step0", user);
        // step0 should be completed
        final long step1Id = waitForUserTask(processInstance, "step1");

        final Map<String, Expression> connectorInputParameters = getConnectorInputParameters(NAME_INPUT_MAIN, mainExp);
        final Map<String, Map<String, Serializable>> inputValues = getInputValues(NAME_INPUT_MAIN, Arrays.asList(NAME_INPUT1, NAME_INPUT2),
                Arrays.asList(VALUE_INPUT1, VALUE_INPUT2));
        Map<String, Serializable> res = getProcessAPI().executeConnectorOnCompletedActivityInstance(ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_ID,
                ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_VERSION, connectorInputParameters, inputValues, step0Id);
        assertEquals(RESULT_CONTENT, res.get(NAME_INPUT_MAIN));
        assignAndExecuteStep(step1Id, user);
        waitForProcessToFinish(processInstance);
        // after process completion
        res = getProcessAPI().executeConnectorOnCompletedActivityInstance(ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_ID,
                ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_VERSION, connectorInputParameters, inputValues, step0Id);
        assertEquals(RESULT_CONTENT, res.get(NAME_INPUT_MAIN));

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "On completed activity", "No serializable output" }, story = "Execute connector on completed activity instance with no serializable output.", jira = "ENGINE-823")
    @Test(expected = ConnectorExecutionException.class)
    public void executeConnectorOnCompletedActivityInstanceWithNotSerializableOutput() throws Exception {
        // Input expression
        final Expression input1Expression = new ExpressionBuilder().createInputExpression(NAME_INPUT1, String.class.getName());
        final Expression input2Expression = new ExpressionBuilder().createInputExpression(NAME_INPUT2, String.class.getName());
        // Data expression
        final Expression input3DefaultExpression = new ExpressionBuilder().createConstantStringExpression(VALUE_INPUT3);
        final Expression input3Expression = new ExpressionBuilder().createDataExpression(NAME_INPUT3, String.class.getName());

        // Main Expression
        final Expression mainExp = new ExpressionBuilder().createGroovyScriptExpression(NAME_INPUT_MAIN, MAIN_EXP_CONTENT,
                String.class.getName(), Arrays.asList(input1Expression, input2Expression, input3Expression));

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME,
                PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addUserTask("step0", ACTOR_NAME).addShortTextData(NAME_INPUT3, input3DefaultExpression);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithExternalTestConnectorAndActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final long activityId = waitForUserTask(processInstance, "step0");
        final List<DataInstance> activityDataInstances = getProcessAPI().getActivityDataInstances(activityId, 0, 5);

        assertTrue(!activityDataInstances.isEmpty());
        assertEquals(1, activityDataInstances.size());
        assertEquals(NAME_INPUT3, activityDataInstances.get(0).getName());
        assertEquals(VALUE_INPUT3, activityDataInstances.get(0).getValue());

        assignAndExecuteStep(activityId, user);
        waitForProcessToFinish(processInstance);

        final Map<String, Expression> connectorInputParameters = getConnectorInputParameters(NAME_INPUT_MAIN, mainExp);
        connectorInputParameters.put("returnNotSerializableOutput", new ExpressionBuilder().createConstantBooleanExpression(true));
        final Map<String, Map<String, Serializable>> inputValues = getInputValues(NAME_INPUT_MAIN, Arrays.asList(NAME_INPUT1, NAME_INPUT2),
                Arrays.asList(VALUE_INPUT1, VALUE_INPUT2));
        inputValues.putAll(getInputValues("returnNotSerializableOutput", null, null));

        try {
            getProcessAPI().executeConnectorOnCompletedActivityInstance(ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_ID,
                    ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_VERSION, connectorInputParameters, inputValues, activityId);
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "On completed process instance", "Process instance" }, story = "Execute connector on completed process instance.", jira = "")
    @Test
    public void executeConnectorOnCompletedProcessInstance() throws Exception {
        // Input expression
        final Expression input1Expression = new ExpressionBuilder().createInputExpression(NAME_INPUT1, String.class.getName());
        final Expression input2Expression = new ExpressionBuilder().createInputExpression(NAME_INPUT2, String.class.getName());
        // Data expression
        final Expression input3DefaultExpression = new ExpressionBuilder().createConstantStringExpression(VALUE_INPUT3);
        final Expression input3Expression = new ExpressionBuilder().createDataExpression(NAME_INPUT3, String.class.getName());

        // Main Expression
        final Expression mainExp = new ExpressionBuilder().createGroovyScriptExpression(NAME_INPUT_MAIN, MAIN_EXP_CONTENT,
                String.class.getName(), Arrays.asList(input1Expression, input2Expression, input3Expression));

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME,
                PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addUserTask("step0", ACTOR_NAME);
        designProcessDefinition.addShortTextData(NAME_INPUT3, input3DefaultExpression);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithExternalTestConnectorAndActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        waitForUserTaskAndExecuteIt(processInstance, "step0", user);
        waitForProcessToFinish(processInstance);
        final Map<String, Expression> connectorInputParameters = getConnectorInputParameters(NAME_INPUT_MAIN, mainExp);
        final Map<String, Map<String, Serializable>> inputValues = getInputValues(NAME_INPUT_MAIN, Arrays.asList(NAME_INPUT1, NAME_INPUT2),
                Arrays.asList(VALUE_INPUT1, VALUE_INPUT2));

        final Map<String, Serializable> res = getProcessAPI().executeConnectorOnCompletedProcessInstance(ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_ID,
                ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_VERSION, connectorInputParameters, inputValues, processInstance.getId());
        assertEquals(RESULT_CONTENT, res.get(NAME_INPUT_MAIN));

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "On completed process instance", "Process instance",
            "No serializable output" }, story = "Execute connector on completed process instance with no serializable output.", jira = "ENGINE-823")
    @Test(expected = ConnectorExecutionException.class)
    public void executeConnectorOnCompletedProcessInstanceWithNotSerializableOutput() throws Exception {
        // Input expression
        final Expression input1Expression = new ExpressionBuilder().createInputExpression(NAME_INPUT1, String.class.getName());
        final Expression input2Expression = new ExpressionBuilder().createInputExpression(NAME_INPUT2, String.class.getName());
        // Data expression
        final Expression input3DefaultExpression = new ExpressionBuilder().createConstantStringExpression(VALUE_INPUT3);
        final Expression input3Expression = new ExpressionBuilder().createDataExpression(NAME_INPUT3, String.class.getName());

        // Main Expression
        final Expression mainExp = new ExpressionBuilder().createGroovyScriptExpression(NAME_INPUT_MAIN, MAIN_EXP_CONTENT,
                String.class.getName(), Arrays.asList(input1Expression, input2Expression, input3Expression));

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME,
                PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addUserTask("step0", ACTOR_NAME);
        designProcessDefinition.addShortTextData(NAME_INPUT3, input3DefaultExpression);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithExternalTestConnectorAndActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final long activityId = waitForUserTask(processInstance, "step0");
        // verify the retrieved data
        final List<DataInstance> processDataInstances = getProcessAPI().getProcessDataInstances(processInstance.getId(), 0, 5);

        assertTrue(!processDataInstances.isEmpty());
        assertEquals(1, processDataInstances.size());
        assertEquals(NAME_INPUT3, processDataInstances.get(0).getName());
        assertEquals(VALUE_INPUT3, processDataInstances.get(0).getValue());

        assignAndExecuteStep(activityId, user);
        waitForProcessToFinish(processInstance);

        final Map<String, Expression> connectorInputParameters = getConnectorInputParameters(NAME_INPUT_MAIN, mainExp);
        connectorInputParameters.put("returnNotSerializableOutput", new ExpressionBuilder().createConstantBooleanExpression(true));
        final Map<String, Map<String, Serializable>> inputValues = getInputValues(NAME_INPUT_MAIN, Arrays.asList(NAME_INPUT1, NAME_INPUT2),
                Arrays.asList(VALUE_INPUT1, VALUE_INPUT2));
        inputValues.putAll(getInputValues("returnNotSerializableOutput", null, null));

        try {
            getProcessAPI().executeConnectorOnCompletedProcessInstance(ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_ID,
                    ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_VERSION, connectorInputParameters, inputValues, processInstance.getId());
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Process instance" }, story = "Execute connector on process instance.", jira = "")
    @Test
    public void executeConnectorOnProcessInstance() throws Exception {
        // Input expression
        final Expression input1Expression = new ExpressionBuilder().createInputExpression(NAME_INPUT1, String.class.getName());
        final Expression input2Expression = new ExpressionBuilder().createInputExpression(NAME_INPUT2, String.class.getName());
        // Data expression
        final Expression input3DefaultExpression = new ExpressionBuilder().createConstantStringExpression(VALUE_INPUT3);
        final Expression input3Expression = new ExpressionBuilder().createDataExpression(NAME_INPUT3, String.class.getName());

        // Main Expression
        final Expression mainExp = new ExpressionBuilder().createGroovyScriptExpression(NAME_INPUT_MAIN, MAIN_EXP_CONTENT,
                String.class.getName(), Arrays.asList(input1Expression, input2Expression, input3Expression));

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME).addUserTask("step0", ACTOR_NAME);
        designProcessDefinition.addShortTextData(NAME_INPUT3, input3DefaultExpression);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithExternalTestConnectorAndActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "step0");

        final Map<String, Expression> connectorInputParameters = getConnectorInputParameters(NAME_INPUT_MAIN, mainExp);
        final Map<String, Map<String, Serializable>> inputValues = getInputValues(NAME_INPUT_MAIN, Arrays.asList(NAME_INPUT1, NAME_INPUT2),
                Arrays.asList(VALUE_INPUT1, VALUE_INPUT2));

        final Map<String, Serializable> res = getProcessAPI().executeConnectorOnProcessInstance(ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_ID,
                ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_VERSION, connectorInputParameters, inputValues, processInstance.getId());

        assertEquals(RESULT_CONTENT, res.get(NAME_INPUT_MAIN));
        assertTrue((Boolean) res.get("hasBeenValidated"));

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Process instance", "No serializable output" }, story = "Execute connector on process instance with no serializable output.", jira = "ENGINE-823")
    @Test(expected = ConnectorExecutionException.class)
    public void executeConnectorOnProcessInstanceWithNotSerializableOutput() throws Exception {
        // Input expression
        final Expression input1Expression = new ExpressionBuilder().createInputExpression(NAME_INPUT1, String.class.getName());
        final Expression input2Expression = new ExpressionBuilder().createInputExpression(NAME_INPUT2, String.class.getName());
        // Data expression
        final Expression input3DefaultExpression = new ExpressionBuilder().createConstantStringExpression(VALUE_INPUT3);
        final Expression input3Expression = new ExpressionBuilder().createDataExpression(NAME_INPUT3, String.class.getName());

        // Main Expression
        final Expression mainExp = new ExpressionBuilder().createGroovyScriptExpression(NAME_INPUT_MAIN, MAIN_EXP_CONTENT,
                String.class.getName(), Arrays.asList(input1Expression, input2Expression, input3Expression));

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME).addUserTask("step0", ACTOR_NAME);
        designProcessDefinition.addShortTextData(NAME_INPUT3, input3DefaultExpression);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithExternalTestConnectorAndActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "step0");

        final Map<String, Expression> connectorInputParameters = getConnectorInputParameters(NAME_INPUT_MAIN, mainExp);
        connectorInputParameters.put("returnNotSerializableOutput", new ExpressionBuilder().createConstantBooleanExpression(true));
        final Map<String, Map<String, Serializable>> inputValues = getInputValues(NAME_INPUT_MAIN, Arrays.asList(NAME_INPUT1, NAME_INPUT2),
                Arrays.asList(VALUE_INPUT1, VALUE_INPUT2));
        inputValues.putAll(getInputValues("returnNotSerializableOutput", null, null));

        try {
            getProcessAPI().executeConnectorOnProcessInstance(ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_ID,
                    ConnectorExecutionTest.DEFAULT_EXTERNAL_CONNECTOR_VERSION, connectorInputParameters, inputValues, processInstance.getId());
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    private Map<String, Expression> getConnectorInputParameters(final String mainName, final Expression mainExp) {
        final Map<String, Expression> connectorInputParameters = new HashMap<String, Expression>();
        connectorInputParameters.put(mainName, mainExp);
        return connectorInputParameters;
    }

    private Map<String, Map<String, Serializable>> getInputValues(final String mainName, final List<String> names, final List<String> vars) {
        final Map<String, Map<String, Serializable>> inputValues = new HashMap<String, Map<String, Serializable>>();
        final Map<String, Serializable> values = new HashMap<String, Serializable>();
        if (names != null && !names.isEmpty() && vars != null && !vars.isEmpty() && names.size() == vars.size()) {
            for (int i = 0; i < names.size(); i++) {
                values.put(names.get(i), vars.get(i));
            }
        }
        inputValues.put(mainName, values);
        return inputValues;
    }

    private Map<String, Serializable> buildInputValues(final List<String> names, final List<String> vars) {
        final Map<String, Serializable> values = new HashMap<String, Serializable>();
        if (names != null && !names.isEmpty() && vars != null && !vars.isEmpty() && names.size() == vars.size()) {
            for (int i = 0; i < names.size(); i++) {
                values.put(names.get(i), vars.get(i));
            }
        }
        return values;
    }

    @Test
    public void setConnectorImplementation() throws Exception {
        final String valueOfInput = "valueOfInput";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput);
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");
        final String dataName = "myVar";

        final ProcessDefinitionBuilderExt designProcessDefinition = buildProcessWithOuputConnector(ACTOR_NAME, input1Expression, defaultValueExpression,
                CONNECTOR_WITH_OUTPUT_ID, CONNECTOR_VERSION, dataName);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorWithOutputAndParameter(designProcessDefinition, ACTOR_NAME,
                user,
                null);
        final ProcessInstance procInstWithOutputConn = getProcessAPI().startProcess(processDefinition.getId());

        waitForStep2AndCheckDataInstanceValue(valueOfInput, dataName, procInstWithOutputConn);

        // prepare zip byte array of connector implementation
        final String implSourchFile = "/org/bonitasoft/engine/connectors/TestConnectorWithModifiedOutput.impl";
        final Class<TestConnectorWithModifiedOutput> implClass = TestConnectorWithModifiedOutput.class;
        final byte[] connectorImplementationArchive = generateZipByteArrayForConnector(implSourchFile, implClass);
        getProcessAPI().setConnectorImplementation(processDefinition.getId(), CONNECTOR_WITH_OUTPUT_ID, CONNECTOR_VERSION, connectorImplementationArchive);
        final ProcessInstance procInstWithModifOuputConn = getProcessAPI().startProcess(processDefinition.getId());

        waitForStep2AndCheckDataInstanceValue(valueOfInput + "->modified", dataName, procInstWithModifOuputConn);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class, ConnectorImplementationDescriptor.class }, concept = BPMNConcept.CONNECTOR, jira = "BS-6876", keywords = { "Set",
            "ConnectorImplementation", "Dependency" }, story = "Set the same connector implementation on 2 different processes after deployement.")
    @Test
    public void setSameConnectorImplementationOn2ProcessesDefinition() throws Exception {
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression("valueOfInput");

        // prepare zip byte array of connector implementation
        final String implSourchFile = "/org/bonitasoft/engine/connectors/TestConnectorWithModifiedOutput.impl";
        final Class<TestConnectorWithModifiedOutput> implClass = TestConnectorWithModifiedOutput.class;
        final byte[] connectorImplementationArchive = generateZipByteArrayForConnector(implSourchFile, implClass);

        // Build & deploy first process definition
        final ProcessDefinitionBuilderExt designProcessDefinition1 = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition1.addActor(ACTOR_NAME).addAutomaticTask("step1");
        designProcessDefinition1.addConnector("myConnector", TEST_CONNECTOR_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER)
                .addInput(CONNECTOR_INPUT_NAME, input1Expression);
        final ProcessDefinition processDefinition1 = deployAndEnableProcessWithActorAndTestConnectorAndParameter(designProcessDefinition1, ACTOR_NAME, user,
                null);

        // Build & deploy second process definition
        final ProcessDefinitionBuilderExt designProcessDefinition2 = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME + "2", PROCESS_VERSION);
        designProcessDefinition2.addActor(ACTOR_NAME).addAutomaticTask("step1");
        designProcessDefinition2.addConnector("myConnector", TEST_CONNECTOR_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER)
                .addInput(CONNECTOR_INPUT_NAME, input1Expression);
        final ProcessDefinition processDefinition2 = deployAndEnableProcessWithActorAndTestConnectorAndParameter(designProcessDefinition2, ACTOR_NAME, user,
                null);

        // Change implementation of connector of first process definition
        getProcessAPI().setConnectorImplementation(processDefinition1.getId(), CONNECTOR_WITH_OUTPUT_ID, CONNECTOR_VERSION, connectorImplementationArchive);

        // Change implementation of connector of second process definition
        getProcessAPI().setConnectorImplementation(processDefinition2.getId(), CONNECTOR_WITH_OUTPUT_ID, CONNECTOR_VERSION, connectorImplementationArchive);

        disableAndDeleteProcess(processDefinition1, processDefinition2);
    }

    @Cover(classes = { ProcessAPI.class, ConnectorImplementationDescriptor.class }, concept = BPMNConcept.CONNECTOR, keywords = { "Set",
            "ConnectorImplementation", "Bad", "ConnectorDefinitionId" }, jira = "ENGINE-737")
    @Test
    public void setConnectorImplementationWithBadConnectorDefinitionId() throws Exception {
        final String connectorId = "org.bonitasoft.connector.testExternalConnector";

        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression("valueOfInput");
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");

        final ProcessDefinitionBuilderExt designProcessDefinition = buildProcessWithOuputConnector(ACTOR_NAME, input1Expression, defaultValueExpression,
                connectorId, CONNECTOR_VERSION, "myVar");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithExternalTestConnectorAndActor(designProcessDefinition, ACTOR_NAME, user);
        final long proDefId = processDefinition.getId();
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(proDefId);
        assertEquals(ConfigurationState.RESOLVED, processDeploymentInfo.getConfigurationState());
        final ConnectorImplementationDescriptor connector = getProcessAPI().getConnectorImplementation(proDefId, connectorId, CONNECTOR_VERSION);
        assertEquals(TestExternalConnector.class.getName(), connector.getImplementationClassName());
        assertEquals(CONNECTOR_VERSION, connector.getVersion());

        final String implSourchFile = "/org/bonitasoft/engine/connectors/TestConnectorWithModifiedOutput.impl";
        final Class<TestConnectorWithModifiedOutput> implClass = TestConnectorWithModifiedOutput.class;
        final byte[] connectorImplementationArchive = generateZipByteArrayForConnector(implSourchFile, implClass);
        try {
            getProcessAPI().setConnectorImplementation(proDefId, connectorId, CONNECTOR_VERSION, connectorImplementationArchive);
            fail();
        } catch (final InvalidConnectorImplementationException e) {
            // ok
        } finally {
            disableAndDeleteProcess(proDefId);
        }
    }

    @Cover(classes = { ProcessAPI.class, ConnectorImplementationDescriptor.class }, concept = BPMNConcept.CONNECTOR, keywords = { "setConnectorImplementation" }, jira = "ENGINE-1215")
    @Test
    public void setConnectorImplementationWithNotAZipFile() throws Exception {
        final String valueOfInput = "valueOfInput";
        final String connectorId = "org.bonitasoft.connector.testExternalConnector";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput);
        final Expression defaultValueExpression = new ExpressionBuilder().createConstantStringExpression("initial");

        final ProcessDefinitionBuilderExt designProcessDefinition = buildProcessWithOuputConnector(ACTOR_NAME, input1Expression, defaultValueExpression,
                connectorId, CONNECTOR_VERSION, "myVar");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithExternalTestConnectorAndActor(designProcessDefinition, ACTOR_NAME, user);
        final long proDefId = processDefinition.getId();
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(proDefId);
        assertEquals(ConfigurationState.RESOLVED, processDeploymentInfo.getConfigurationState());
        final ConnectorImplementationDescriptor connector = getProcessAPI().getConnectorImplementation(proDefId, connectorId, CONNECTOR_VERSION);
        assertEquals(TestExternalConnector.class.getName(), connector.getImplementationClassName());
        assertEquals(CONNECTOR_VERSION, connector.getVersion());

        final String implSourchFile = "/org/bonitasoft/engine/connectors/TestConnectorWithModifiedOutput.impl";
        final Class<TestConnectorWithModifiedOutput> implClass = TestConnectorWithModifiedOutput.class;
        final byte[] connectorImplementationArchive = generateZipByteArrayForConnector(implSourchFile, implClass);
        try {
            getProcessAPI().setConnectorImplementation(proDefId, connectorId, CONNECTOR_VERSION, connectorImplementationArchive);
            fail();
        } catch (final InvalidConnectorImplementationException e) {
            // ok
        } finally {
            disableAndDeleteProcess(proDefId);
        }
    }

    @Test
    public void getConnectorImplementation() throws Exception {
        // connector information
        final String connectorImplementationClassName1 = "org.bonitasoft.engine.connectors.TestConnector";
        final String connectorImplementationClassName2 = "org.bonitasoft.engine.connectors.TestConnectorWithOutput";
        final String connectorImplementationClassName3 = "org.bonitasoft.engine.connectors.TestConnector2";
        final String valueOfInput = "valueOfInput";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput);
        // create process

        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addAutomaticTask("step1").addConnector("myConnector", TEST_CONNECTOR_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER)
                .addInput(CONNECTOR_INPUT_NAME, input1Expression);
        designProcessDefinition.addUserTask("step2", ACTOR_NAME);
        designProcessDefinition.addTransition("step1", "step2");

        final List<BarResource> connectorImplementations = Arrays.asList(
                BuildTestUtil.getContentAndBuildBarResource("TestConnector.impl", TestConnector.class),
                BuildTestUtil.getContentAndBuildBarResource("TestConnectorWithOutput.impl", TestConnectorWithOutput.class));
        final List<BarResource> generateConnectorDependencies = Arrays.asList(
                BuildTestUtil.generateJarAndBuildBarResource(TestConnector.class, "TestConnector.jar"),
                BuildTestUtil.generateJarAndBuildBarResource(TestConnectorWithOutput.class, "TestConnectorWithOutput.jar"));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndConnectorAndParameter(designProcessDefinition, ACTOR_NAME, user,
                connectorImplementations, generateConnectorDependencies, null);
        final long processDefinitionId = processDefinition.getId();

        // common check
        ConnectorImplementationDescriptor connectorImplementation = getProcessAPI().getConnectorImplementation(processDefinitionId, TEST_CONNECTOR_ID,
                CONNECTOR_VERSION);
        assertNotNull(connectorImplementation);
        assertEquals(TEST_CONNECTOR_ID, connectorImplementation.getDefinitionId());
        assertEquals(connectorImplementationClassName1, connectorImplementation.getImplementationClassName());

        connectorImplementation = getProcessAPI().getConnectorImplementation(processDefinitionId, CONNECTOR_WITH_OUTPUT_ID, CONNECTOR_VERSION);
        assertNotNull(connectorImplementation);
        assertEquals(CONNECTOR_WITH_OUTPUT_ID, connectorImplementation.getDefinitionId());
        assertEquals(connectorImplementationClassName2, connectorImplementation.getImplementationClassName());

        // set new connector implementation for connectorId1, and check
        final String implSourchFile = "/org/bonitasoft/engine/connectors/TestConnector2.impl";
        final Class<TestConnector2> implClass = TestConnector2.class;
        final byte[] connectorImplementationArchive = generateZipByteArrayForConnector(implSourchFile, implClass);
        getProcessAPI().setConnectorImplementation(processDefinition.getId(), TEST_CONNECTOR_ID, CONNECTOR_VERSION, connectorImplementationArchive);
        connectorImplementation = getProcessAPI().getConnectorImplementation(processDefinitionId, TEST_CONNECTOR_ID, CONNECTOR_VERSION);
        assertNotNull(connectorImplementation);
        assertEquals(TEST_CONNECTOR_ID, connectorImplementation.getDefinitionId());
        assertEquals(connectorImplementationClassName3, connectorImplementation.getImplementationClassName());

        disableAndDeleteProcess(processDefinition);
    }

    private void waitForStep2AndCheckDataInstanceValue(final String exptectedValue, final String dataName, final ProcessInstance procInst) throws Exception,
            DataNotFoundException {
        waitForUserTask(procInst, "step2");
        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance(dataName, procInst.getId());
        assertEquals(exptectedValue, dataInstance.getValue());
    }

    private ProcessDefinitionBuilderExt buildProcessWithOuputConnector(final String actor, final Expression input1Expression,
            final Expression defaultValueExpression, final String connectorId, final String CONNECTOR_VERSION, final String dataName)
            throws InvalidExpressionException {
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        designProcessDefinition.addActor(actor);
        designProcessDefinition.addShortTextData(dataName, defaultValueExpression);
        designProcessDefinition
                .addAutomaticTask("step1")
                .addConnector("myConnector", connectorId, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER)
                .addInput(CONNECTOR_INPUT_NAME, input1Expression)
                .addOutput(new LeftOperandBuilder().createNewInstance().setName(dataName).done(), OperatorType.ASSIGNMENT, "=", "",
                        new ExpressionBuilder().createInputExpression(CONNECTOR_OUTPUT_NAME, String.class.getName()));
        designProcessDefinition.addUserTask("step2", actor);
        designProcessDefinition.addTransition("step1", "step2");
        return designProcessDefinition;
    }

    @Cover(jira = "ENGINE-1265", classes = { Connector.class, ParameterInstance.class }, keywords = { "parameter", "connector", "input" }, concept = BPMNConcept.CONNECTOR)
    @Test
    public void useParameterAsInputOfTheConnector() throws Exception {
        final Expression parameterExpression = new ExpressionBuilder().createParameterExpression("paramExpr", "key1", String.class.getName());
        final ProcessDefinitionBuilderExt definitionBuilder = new ProcessDefinitionBuilderExt().createNewInstance("parameter", "4.1");
        definitionBuilder.addParameter("key1", String.class.getName());
        final String processData = "finalValue";
        definitionBuilder.addShortTextData(processData, new ExpressionBuilder().createConstantStringExpression("empty"));
        definitionBuilder.addActor(ACTOR_NAME);
        definitionBuilder
                .addUserTask("step1", ACTOR_NAME)
                .addConnector("paramConnector", CONNECTOR_WITH_OUTPUT_ID, CONNECTOR_VERSION, ConnectorEvent.ON_ENTER)
                .addInput(CONNECTOR_INPUT_NAME, parameterExpression)
                .addOutput(new LeftOperandBuilder().createNewInstance().setName(processData).done(), OperatorType.ASSIGNMENT, "=", "",
                        new ExpressionBuilder().createInputExpression(CONNECTOR_OUTPUT_NAME, String.class.getName()));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnectorWithOutputAndParameter(definitionBuilder, ACTOR_NAME, user,
                Collections.singletonMap("key1", "Hello world!"));
        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(instance, "step1");

        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance(processData, instance.getId());
        assertEquals("Hello world!", dataInstance.getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Tenants", "Process instance", "Delete" }, story = "Execute connector on completed process instance.", jira = "BS-8607")
    @Test
    public void should_be_able_to_delete_process_instance_when_process_definition_is_on_2_tenants_with_different_implementations_of_connector()
            throws Exception {
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(PROCESS_NAME,
                PROCESS_VERSION);
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addUserTask("step0", ACTOR_NAME).addConnector("connecteur", "org.bonitasoft.connector.testConnector", "1.0",
                ConnectorEvent.ON_FINISH);

        // Deploy the first process on the default tenant
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndTestConnector(designProcessDefinition, ACTOR_NAME, user);
        for (int i = 0; i < 10; i++) {
            final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
            waitForUserTaskAndExecuteIt(processInstance, "step0", user);
        }
        for (int i = 0; i < 10; i++) {
            getProcessAPI().startProcess(processDefinition.getId());
        }

        // Create the second tenant, and login on
        final long tenant2Id = BPMTestSPUtil.createAndActivateTenantWithDefaultTechnicalLogger("Tenant 2");

        loginOnTenantWithTechnicalLogger(tenant2Id);
        final User userForTenant2 = createUser(USERNAME, PASSWORD);
        loginOnTenantWith(USERNAME, PASSWORD, tenant2Id);
        final ProcessDefinition processDefinition2 = deployAndEnableProcessWithActorAndTestConnector2(designProcessDefinition, ACTOR_NAME, userForTenant2);
        for (int i = 0; i < 10; i++) {
            final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition2.getId());
            waitForUserTaskAndExecuteIt(processInstance, "step0", userForTenant2);
        }
        for (int i = 0; i < 10; i++) {
            getProcessAPI().startProcess(processDefinition2.getId());
        }
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition2.getId());
        waitForUserTask(processInstance2.getId(), "step0");
        getProcessAPI().deleteProcessInstances(processDefinition2.getId());

        // Clean up
        BPMTestSPUtil.deactivateAndDeleteTenant(tenant2Id);
        loginOnDefaultTenantWithDefaultTechnicalUser();
        disableAndDeleteProcess(processDefinition);
    }
}
