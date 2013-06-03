/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.BPMRemoteTests;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.connector.ConnectorInstance;
import org.bonitasoft.engine.bpm.connector.ConnectorInstancesSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.AutomaticTaskDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.cache.CacheService;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.connectors.ConnectorExecutionTest;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Elias Ricken de Medeiros
 */
public class ConnectorExecutionsTestsLocal extends ConnectorExecutionTest {

    protected TenantServiceAccessor getTenantAccessor() {
        try {
            final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
            final long tenantId = sessionAccessor.getTenantId();
            return TenantServiceSingleton.getInstance(tenantId);
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "On finish", "Data input", "Automatic activity" }, story = "Test connector on finish of an automatic activity with data input.")
    @Test
    public void testExecuteConnectorOnFinishOfAnAutomaticActivityWithDataAsInput() throws Exception {
        final String delivery = "Delivery men";

        final String valueOfInput1 = "valueOfInput1";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput1);
        final String dataName = "myData1";
        final String inputName = "input1";
        final Expression myData1Expression = new ExpressionBuilder().createDataExpression(dataName, String.class.getName());
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnFinishOfAnAutomaticActivityWithDataAsInput", "1.0");
        designProcessDefinition.addShortTextData(dataName, input1Expression);
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addAutomaticTask("step1")
                .addConnector("myConnector", "org.bonitasoft.connector.testConnector", "1.0", ConnectorEvent.ON_FINISH).addInput(inputName, myData1Expression);
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");

        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, userId, designProcessDefinition, false);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("step2", processInstance);
        assertTrue(VariableStorage.getInstance().getVariableValue(inputName).equals(valueOfInput1));

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector, Several, On start, User task" }, jira = "ENGINE-472", story = "Test of several connectors on start of an user task.")
    @Test
    public void testExecuteSeveralConnectorsOnUserTaskOnStart() throws Exception {
        final String delivery = "Delivery men";

        final String valueOfInput1 = "valueOfInput1";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput1);
        final String valueOfInput2 = "valueOfInput2";
        final Expression input2Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput2);
        final String inputName1 = "input1";
        final String inputName2 = "input2";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("executeSeveralConnectorsOnUserTaskOnStart", "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and nigth long");

        final UserTaskDefinitionBuilder taskBuilder = new UserTaskDefinitionBuilder(processBuilder, (FlowElementContainerDefinitionImpl) processBuilder
                .getProcess().getProcessContainer(), "step1", delivery);
        taskBuilder.addConnector("myConnector1", "org.bonitasoft.connector.testConnector3", "1.0", ConnectorEvent.ON_ENTER).addInput(inputName1,
                input1Expression);
        taskBuilder.addConnector("myConnector2", "org.bonitasoft.connector.testConnector3", "1.0", ConnectorEvent.ON_ENTER).addInput(inputName2,
                input2Expression);

        processBuilder.addUserTask("step2", delivery).addTransition("step1", "step2");

        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, userId, processBuilder, false);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance task1 = waitForUserTask("step1", processInstance);

        final WaitUntil waitUntil = new WaitUntil(50, 2000, false) {

            @Override
            protected boolean check() throws Exception {
                final boolean check1 = VariableStorage.getInstance().getVariableValue(inputName1).equals(valueOfInput1);
                final boolean check2 = VariableStorage.getInstance().getVariableValue(inputName2).equals(valueOfInput2);
                return check1 && check2;
            }
        };
        assertTrue(waitUntil.waitUntil());
        assignAndExecuteStep(task1, userId);

        final ActivityInstance task2 = waitForUserTask("step2", processInstance);
        assignAndExecuteStep(task2, userId);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector, Several, On start, Automatic task" }, jira = "ENGINE-472", story = "Test of several connectors on start of an automatic task.")
    @Test
    public void testExecuteSeveralConnectorsOnAutomaticTaskOnStart() throws Exception {
        final String delivery = "Delivery men";

        final String valueOfInput1 = "valueOfInput1";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput1);
        final String valueOfInput2 = "valueOfInput2";
        final Expression input2Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput2);
        final String inputName1 = "input1";
        final String inputName2 = "input2";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("executeSeveralConnectorsOnAutomaticTaskOnStart",
                "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and nigth long");

        final AutomaticTaskDefinitionBuilder taskBuilder = new AutomaticTaskDefinitionBuilder(processBuilder,
                (FlowElementContainerDefinitionImpl) processBuilder.getProcess().getProcessContainer(), "step1");
        taskBuilder.addConnector("myConnector1", "org.bonitasoft.connector.testConnector3", "1.0", ConnectorEvent.ON_ENTER).addInput(inputName1,
                input1Expression);
        taskBuilder.addConnector("myConnector2", "org.bonitasoft.connector.testConnector3", "1.0", ConnectorEvent.ON_ENTER).addInput(inputName2,
                input2Expression);

        processBuilder.addAutomaticTask("step2").addTransition("step1", "step2");

        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, userId, processBuilder, false);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final WaitUntil waitUntil = new WaitUntil(50, 2000, false) {

            @Override
            protected boolean check() throws Exception {
                final boolean check1 = VariableStorage.getInstance().getVariableValue(inputName1).equals(valueOfInput1);
                final boolean check2 = VariableStorage.getInstance().getVariableValue(inputName2).equals(valueOfInput2);
                return check1 && check2;
            }
        };
        assertTrue(waitUntil.waitUntil());
        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector, Several, On finish, User task" }, jira = "ENGINE-472", story = "Test of several connectors on finish of an user task.")
    @Test
    public void testExecuteSeveralConnectorsOnUserTaskOnFinish() throws Exception {
        final String delivery = "Delivery men";

        final String valueOfInput1 = "valueOfInput1";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput1);
        final String valueOfInput2 = "valueOfInput2";
        final Expression input2Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput2);
        final String inputName1 = "input1";
        final String inputName2 = "input2";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("executeSeveralConnectorsOnUserTaskOnFinish", "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and nigth long");

        final UserTaskDefinitionBuilder taskBuilder = new UserTaskDefinitionBuilder(processBuilder, (FlowElementContainerDefinitionImpl) processBuilder
                .getProcess().getProcessContainer(), "step1", delivery);
        taskBuilder.addConnector("myConnector1", "org.bonitasoft.connector.testConnector3", "1.0", ConnectorEvent.ON_FINISH).addInput(inputName1,
                input1Expression);
        taskBuilder.addConnector("myConnector2", "org.bonitasoft.connector.testConnector3", "1.0", ConnectorEvent.ON_FINISH).addInput(inputName2,
                input2Expression);

        processBuilder.addUserTask("step2", delivery).addTransition("step1", "step2");

        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, userId, processBuilder, false);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final ActivityInstance task1 = waitForUserTask("step1", processInstance);
        assignAndExecuteStep(task1, userId);

        final ActivityInstance task2 = waitForUserTask("step2", processInstance);

        final WaitUntil waitUntil = new WaitUntil(50, 2000, false) {

            @Override
            protected boolean check() throws Exception {
                final boolean check1 = VariableStorage.getInstance().getVariableValue(inputName1).equals(valueOfInput1);
                final boolean check2 = VariableStorage.getInstance().getVariableValue(inputName2).equals(valueOfInput2);
                return check1 && check2;
            }
        };
        assertTrue(waitUntil.waitUntil());

        assignAndExecuteStep(task2, userId);
        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector, Several, On finish, Automatic task" }, jira = "ENGINE-472", story = "Test of several connectors on finish of an automatic task.")
    @Test
    public void testExecuteSeveralConnectorsOnAutomaticTaskOnFinish() throws Exception {
        final String delivery = "Delivery men";

        final String valueOfInput1 = "valueOfInput1";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput1);
        final String valueOfInput2 = "valueOfInput2";
        final Expression input2Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput2);
        final String inputName1 = "input1";
        final String inputName2 = "input2";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("executeSeveralConnectorsOnAutomaticTaskOnFinish",
                "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and nigth long");

        final AutomaticTaskDefinitionBuilder taskBuilder = new AutomaticTaskDefinitionBuilder(processBuilder,
                (FlowElementContainerDefinitionImpl) processBuilder.getProcess().getProcessContainer(), "step1");
        taskBuilder.addConnector("myConnector1", "org.bonitasoft.connector.testConnector3", "1.0", ConnectorEvent.ON_FINISH).addInput(inputName1,
                input1Expression);
        taskBuilder.addConnector("myConnector2", "org.bonitasoft.connector.testConnector3", "1.0", ConnectorEvent.ON_FINISH).addInput(inputName2,
                input2Expression);

        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, userId, processBuilder, false);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final WaitUntil waitUntil = new WaitUntil(50, 2000, false) {

            @Override
            protected boolean check() throws Exception {
                final boolean check1 = VariableStorage.getInstance().getVariableValue(inputName1).equals(valueOfInput1);
                final boolean check2 = VariableStorage.getInstance().getVariableValue(inputName2).equals(valueOfInput2);
                return check1 && check2;
            }
        };
        assertTrue(waitUntil.waitUntil());

        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector, Several, On start, On finish" }, jira = "ENGINE-472", story = "Test of several connectors on start and finish of an user task.")
    @Test
    public void testExecuteSeveralConnectorsOnStartAndOnFinishWithDataInput() throws Exception {
        final String delivery = "Delivery men";

        final String valueOfInput1 = "valueOfInput1";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput1);
        final String valueOfInput2 = "valueOfInput2";
        final Expression input2Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput2);
        final String valueOfInput3 = "valueOfInput3";
        final Expression input3Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput3);
        final String valueOfInput4 = "valueOfInput4";
        final Expression input4Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput4);
        final String inputName1 = "input1";
        final String inputName2 = "input2";
        final String inputName3 = "input3";
        final String inputName4 = "input4";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("executeSeveralConnectorsOnStartAndOnFinish", "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and nigth long");

        final UserTaskDefinitionBuilder taskBuilder = new UserTaskDefinitionBuilder(processBuilder, (FlowElementContainerDefinitionImpl) processBuilder
                .getProcess().getProcessContainer(), "step1", delivery);
        taskBuilder.addConnector("myConnector1", "org.bonitasoft.connector.testConnector3", "1.0", ConnectorEvent.ON_ENTER).addInput(inputName1,
                input1Expression);
        taskBuilder.addConnector("myConnector2", "org.bonitasoft.connector.testConnector3", "1.0", ConnectorEvent.ON_ENTER).addInput(inputName2,
                input2Expression);
        taskBuilder.addConnector("myConnector3", "org.bonitasoft.connector.testConnector3", "1.0", ConnectorEvent.ON_FINISH).addInput(inputName3,
                input3Expression);
        taskBuilder.addConnector("myConnector4", "org.bonitasoft.connector.testConnector3", "1.0", ConnectorEvent.ON_FINISH).addInput(inputName4,
                input4Expression);

        processBuilder.addUserTask("step2", delivery).addTransition("step1", "step2");

        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, userId, processBuilder, false);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final ActivityInstance task1 = waitForUserTask("step1", processInstance);

        final WaitUntil waitUntil1 = new WaitUntil(50, 2000, false) {

            @Override
            protected boolean check() throws Exception {
                final boolean check1 = VariableStorage.getInstance().getVariableValue(inputName1).equals(valueOfInput1);
                final boolean check2 = VariableStorage.getInstance().getVariableValue(inputName2).equals(valueOfInput2);
                return check1 && check2;
            }
        };
        assertTrue(waitUntil1.waitUntil());

        assignAndExecuteStep(task1, userId);

        final ActivityInstance task2 = waitForUserTask("step2", processInstance);

        final WaitUntil waitUntil2 = new WaitUntil(50, 2000, false) {

            @Override
            protected boolean check() throws Exception {
                final boolean check3 = VariableStorage.getInstance().getVariableValue(inputName3).equals(valueOfInput3);
                final boolean check4 = VariableStorage.getInstance().getVariableValue(inputName4).equals(valueOfInput4);
                return check3 && check4;
            }
        };
        assertTrue(waitUntil2.waitUntil());

        assignAndExecuteStep(task2, userId);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "On finish", "Automatic activity" }, story = "Test connector on finish of an automatic activity.")
    @Test
    public void testExecuteConnectorOnFinishOfAnAutomaticActivity() throws Exception {
        final String delivery = "Delivery men";

        final String valueOfInput1 = "valueOfInput1";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput1);
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnStartOfAnAutomaticActivity", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        final String inputName = "input1";
        designProcessDefinition.addAutomaticTask("step1")
                .addConnector("myConnector", "org.bonitasoft.connector.testConnector", "1.0", ConnectorEvent.ON_FINISH).addInput(inputName, input1Expression);

        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, userId, designProcessDefinition, false);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());

        final WaitUntil waitUntil = waitForVariableStorage(50, 2000, inputName, valueOfInput1);
        assertTrue(waitUntil.waitUntil());
        waitForProcessToFinish(startProcess);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "On enter", "User task" }, story = "Test connector on start of an user task.")
    @Test
    public void testExecuteConnectorOnEnterOfAnUserTask() throws Exception {
        final String delivery = "Delivery men";

        final String valueOfInput1 = "valueOfInput1";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput1);
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnStartOfAnAutomaticActivity", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        final String inputName = "input1";
        designProcessDefinition.addUserTask("step1", delivery)
                .addConnector("myConnector", "org.bonitasoft.connector.testConnector", "1.0", ConnectorEvent.ON_ENTER).addInput(inputName, input1Expression);

        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, johnUserId, designProcessDefinition, false);
        getProcessAPI().startProcess(processDefinition.getId());

        final WaitUntil waitUntil = waitForVariableStorage(50, 2000, inputName, valueOfInput1);
        assertTrue(waitUntil.waitUntil());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Connector deletion", "On enter", "User task", }, story = "Test connectors are deleted when the task is completed.")
    @Test
    public void testConnectorsAreDeletedAfterTaskCompletion() throws Exception {
        // deploy process
        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final String taskName = "step1";
        final ProcessDefinition processDefinition = deployProcessWithConnectorOnUserTask(userId, taskName);

        // start the process
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // wait for step containing the connector and execute it
        final ActivityInstance step1 = waitForUserTask(taskName, processInstance.getId());
        assignAndExecuteStep(step1, userId);
        waitForArchivedActivity(step1.getId(), TestStates.getNormalFinalState(step1));

        // check that there are no more connector instances
        final SearchResult<ConnectorInstance> searchResult = searchConnectors(step1.getId(), ConnectorInstance.FLOWNODE_TYPE, 10);
        assertEquals(0, searchResult.getCount());

        // clean up
        disableAndDeleteProcess(processDefinition);
    }

    private SearchResult<ConnectorInstance> searchConnectors(final long containerId, final String containerType, final int maxResults) throws SearchException {
        final SearchOptionsBuilder optionsBuilder = new SearchOptionsBuilder(0, maxResults);
        optionsBuilder.filter(ConnectorInstancesSearchDescriptor.CONTAINER_ID, containerId);
        optionsBuilder.filter(ConnectorInstancesSearchDescriptor.CONTAINER_TYPE, containerType);
        return getProcessAPI().searchConnectorInstances(optionsBuilder.done());
    }

    private ProcessDefinition deployProcessWithConnectorOnUserTask(final long userId, final String taskName) throws BonitaException, IOException {
        final String delivery = "Delivery men";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression("valueOfInput1");
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnStartOfAnAutomaticActivity", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addUserTask(taskName, delivery)
                .addConnector("myConnector", "org.bonitasoft.connector.testConnector", "1.0", ConnectorEvent.ON_ENTER).addInput("input1", input1Expression);

        return deployProcessWithDefaultTestConnector(delivery, userId, designProcessDefinition, false);
    }

    @Cover(classes = { Connector.class, HumanTaskInstance.class }, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "On finish", "User task",
            "Starting State" }, story = "Test connector on finish on starting state of an user task.", jira = "ENGINE-604")
    @Test
    public void testExecuteConnectorOnFinishOfAnUserTask() throws Exception {
        final String delivery = "Delivery men";

        final String inputName = "input1";
        final String valueOfInput1 = "valueOfInput1";

        // Configure process and human tasks
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput1);
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnStartOfAnAutomaticActivity", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addUserTask("step1", delivery)
                .addConnector("myConnector", "org.bonitasoft.connector.testConnector", "1.0", ConnectorEvent.ON_FINISH).addInput(inputName, input1Expression);
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");

        // Deploy and start process
        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, userId, designProcessDefinition, false);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // Assign human task with connector
        waitForStep("step1", processInstance);
        final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(userId, 0, 10, null);
        final long activityInstanceId = pendingTasks.get(0).getId();
        getProcessAPI().assignUserTask(activityInstanceId, userId);

        // Check Ready state of human task
        final List<HumanTaskInstance> assignedTasks = getProcessAPI().getAssignedHumanTaskInstances(userId, 0, 10, null);
        assertEquals("ready", assignedTasks.get(0).getState());

        // Check that the "input1" variable has no value for "valueOfInput1"
        final WaitUntil waitUntil = waitForVariableStorage(50, 800, inputName, valueOfInput1);
        assertFalse(waitUntil.waitUntil());

        // Run Started state of the human task
        executeAssignedTaskUntilEnd(activityInstanceId);
        waitForArchivedActivity(activityInstanceId, TestStates.getNormalFinalState(null));

        // Check that the "input1" variable has value for "valueOfInput1", in Started state of human task
        assertTrue(waitUntil.waitUntil());

        // Remove all for the end
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { Connector.class, HumanTaskInstance.class }, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "On finish", "User task",
            "Boundary event", "Timer event", "Starting State" }, story = "Test connector on finish on starting state of an user task, with a boundary timer evnet.", jira = "ENGINE-604")
    @Test
    public void testExecuteConnectorOnFinishStateOfAnUserTaskWithTimerEvent() throws Exception {
        final String inputName = "input1";
        final String valueOfInput1 = "valueOfInput1";
        final String actorName = "Delivery men";

        final long timerDuration = 3000;

        // Configure process and human tasks
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnStartOfAnAutomaticActivity", "1.0");
        designProcessDefinition.addActor(actorName).addDescription("Delivery all day and night long");
        final UserTaskDefinitionBuilder userTaskDefinitionBuilder = designProcessDefinition.addUserTask("step1", actorName);
        userTaskDefinitionBuilder.addConnector("myConnector", "org.bonitasoft.connector.testConnector", "1.0", ConnectorEvent.ON_FINISH).addInput(inputName,
                new ExpressionBuilder().createConstantStringExpression(valueOfInput1));
        designProcessDefinition.addStartEvent("start");
        userTaskDefinitionBuilder.addBoundaryEvent("timer", true).addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createConstantLongExpression(timerDuration));
        userTaskDefinitionBuilder.addUserTask("exceptionStep", actorName);
        designProcessDefinition.addEndEvent("end");
        designProcessDefinition.addAutomaticTask("step2");
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.addTransition("timer", "exceptionStep");

        // Deploy and start process
        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(actorName, userId, designProcessDefinition, false);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // Assign human task with connector
        waitForStep("step1", processInstance);
        final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(userId, 0, 10, null);
        final long activityInstanceId = pendingTasks.get(0).getId();
        getProcessAPI().assignUserTask(activityInstanceId, userId);

        // Check Ready state of human task
        assertEquals("ready", pendingTasks.get(0).getState());

        // Check that the "input1" variable has no value for "valueOfInput1", in Ready state of human task
        final WaitUntil waitUntil = waitForVariableStorage(50, 800, inputName, valueOfInput1);
        assertFalse(waitUntil.waitUntil());

        // Run Started state of the human task
        getProcessAPI().executeFlowNode(activityInstanceId);

        // Check that the "input1" variable has value for "valueOfInput1", in Started state of human task
        assertTrue(waitUntil.waitUntil());

        // Remove all for the end
        waitForProcessToFinish(processInstance, 10500);

        disableAndDeleteProcess(processDefinition);
    }

    private WaitUntil waitForVariableStorage(final int repeatEach, final int timeout, final String inputName, final String valueOfInput) {
        final WaitUntil waitUntil = new WaitUntil(repeatEach, timeout, false) {

            @Override
            protected boolean check() throws Exception {
                return VariableStorage.getInstance().getVariableValue(inputName).equals(valueOfInput);
            }
        };
        return waitUntil;
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "On enter", "Process" }, story = "Test connector on start of a process.")
    @Test
    public void testExecuteConnectorOnEnterOfProcess() throws Exception {
        final String delivery = "Delivery men";

        final String valueOfInput1 = "valueOfInput1";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput1);
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnStartOfAnAutomaticActivity", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        final String inputName = "input1";
        designProcessDefinition.addConnector("myConnector", "org.bonitasoft.connector.testConnector", "1.0", ConnectorEvent.ON_ENTER).addInput(inputName,
                input1Expression);
        designProcessDefinition.addAutomaticTask("step1");
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");

        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, userId, designProcessDefinition, false);
        final long processInstanceId = getProcessAPI().startProcess(processDefinition.getId()).getId();

        final WaitUntil waitUntil = waitForVariableStorage(50, 2000, inputName, valueOfInput1);
        assertTrue(waitUntil.waitUntil());

        waitForUserTask("step2", processInstanceId);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "On finish", "Process" }, story = "Test connector on finish of a process.")
    @Test
    public void testExecuteConnectorOnFinishOfAProcess() throws Exception {
        final String delivery = "Delivery men";

        final String valueOfInput1 = "valueOfInput1";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput1);
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnStartOfAnAutomaticActivity", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        final String inputName = "input1";
        designProcessDefinition.addConnector("myConnector", "org.bonitasoft.connector.testConnector", "1.0", ConnectorEvent.ON_FINISH).addInput(inputName,
                input1Expression);
        designProcessDefinition.addUserTask("step1", delivery);

        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, userId, designProcessDefinition, false);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final WaitUntil waitUntil = waitForVariableStorage(50, 800, inputName, valueOfInput1);
        assertFalse(waitUntil.waitUntil());

        final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(userId, 0, 10, null);
        assignAndExecuteStep(pendingTasks.get(0), userId);

        assertTrue(waitUntil.waitUntil());
        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Connector deletion", "On finish", "Process" }, story = "Test connectors attached to a process are deleted when the process completes.")
    @Test
    public void testConnectorsAreDeletedAfterProcessCompletion() throws Exception {
        // deploy the a process with a connector
        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final String delivery = "Delivery men";
        final String taskName = "step1";
        final ProcessDefinition processDefinition = deployProcessWithConnectorOnFinish(userId, delivery, taskName);

        // execute the process
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(taskName, processInstance, userId);
        waitForProcessToFinish(processInstance);

        // check there are no connector instances
        final SearchResult<ConnectorInstance> searchResult = searchConnectors(processInstance.getId(), ConnectorInstance.PROCESS_TYPE, 10);
        assertEquals(0, searchResult.getCount());

        // clean up
        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployProcessWithConnectorOnFinish(final long userId, final String delivery, final String taskName) throws BonitaException,
            IOException {
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression("valueOfInput1");
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnStartOfAnAutomaticActivity", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addConnector("myConnector", "org.bonitasoft.connector.testConnector", "1.0", ConnectorEvent.ON_FINISH).addInput("input1",
                input1Expression);
        designProcessDefinition.addUserTask(taskName, delivery);
        return deployProcessWithDefaultTestConnector(delivery, userId, designProcessDefinition, false);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "On enter", "Automatic activity" }, story = "Test connector on start of an automatic activity.")
    @Test
    public void testExecuteConnectorOnEnterOfAnAutomaticActivity() throws Exception {
        final String delivery = "Delivery men";

        final String valueOfInput1 = "valueOfInput1";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput1);
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnStartOfAnAutomaticActivity", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        final String inputName = "input1";
        designProcessDefinition.addAutomaticTask("step1").addConnector("myConnector", "org.bonitasoft.connector.testConnector", "1.0", ConnectorEvent.ON_ENTER)
                .addInput(inputName, input1Expression);
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");

        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, userId, designProcessDefinition, false);
        final long processInstanceId = getProcessAPI().startProcess(processDefinition.getId()).getId();

        final WaitUntil waitUntil = waitForVariableStorage(50, 2000, inputName, valueOfInput1);
        assertTrue(waitUntil.waitUntil());

        assertNotNull(waitForUserTask("step2", processInstanceId));

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Missing implementation", "Process instance" }, story = "Execute connector with missing implementation on process instance.")
    @Test
    public void testExecuteMissingImplConnectorOnProcessInstance() throws Exception {
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("executeConnectorOnActivityInstance", "1.0");
        designProcessDefinition.addActor(delivery).addUserTask("step1", delivery)
                .addConnector("UnkownConnector", "unkownConnectorId", "1.0.0", ConnectorEvent.ON_ENTER);
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition.done()).done());
        addMappingOfActorsForUser(delivery, johnUserId, processDefinition);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ConfigurationState.UNRESOLVED, processDeploymentInfo.getConfigurationState());
        final List<Problem> processResolutionProblems = getProcessAPI().getProcessResolutionProblems(processDefinition.getId());
        assertEquals(1, processResolutionProblems.size());
        final Problem problem = processResolutionProblems.get(0);
        assertEquals("connector", problem.getResource());
        getProcessAPI().deleteProcess(processDefinition.getId());
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Missing class connector", "Process instance" }, story = "Execute connector with missing class on process instance.")
    @Test
    public void testExecuteMissingClassConnectorOnProcessInstance() throws Exception {
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(myOut));
        try {

            final String delivery = "Delivery men";

            final ProcessDefinitionBuilder processDefBuilder = new ProcessDefinitionBuilder().createNewInstance("executeConnectorOnActivityInstance", "1.0");
            processDefBuilder.addActor(delivery).addUserTask("step1", delivery)
                    .addConnector("UnkownClassConnector", "unkownClassConnectorDef", "1.0.0", ConnectorEvent.ON_ENTER);
            final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                    processDefBuilder.done());
            businessArchiveBuilder.addConnectorImplementation(new BarResource("UnknownClassConnector.impl", IOUtils.toByteArray(BPMRemoteTests.class
                    .getResourceAsStream("/org/bonitasoft/engine/connectors/UnknownClassConnector.impl"))));
            final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchiveBuilder.done());
            addMappingOfActorsForUser(delivery, johnUserId, processDefinition);
            getProcessAPI().enableProcess(processDefinition.getId());

            final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
            final ActivityInstance failTask = waitForTaskToFail(processInstance);
            assertEquals("step1", failTask.getName());
            disableAndDeleteProcess(processDefinition);
        } finally {
            System.setOut(stdout);
        }
        final String logs = myOut.toString();
        System.out.println(logs);
        assertTrue("should have written in logs an exception", logs.contains("SConnectorException"));
        assertTrue("should have written in logs an exception", logs.contains("java.lang.ClassNotFoundException: org.unknown.MyUnknownClass"));
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Connector", "Multi-instance" }, story = "Execute connector on multi-instance.")
    @Test
    public void testExecuteConnectorOnMultiInstance() throws Exception {
        final String delivery = "Delivery men";

        final String valueOfInput1 = "valueOfInput1";
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput1);
        final String inputName1 = "input1";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("executeConnectorOnMultiInstance", "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and nigth long");

        final UserTaskDefinitionBuilder taskBuilder = new UserTaskDefinitionBuilder(processBuilder, (FlowElementContainerDefinitionImpl) processBuilder
                .getProcess().getProcessContainer(), "step1", delivery);
        taskBuilder.addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(3));
        taskBuilder.addConnector("myConnector", "org.bonitasoft.connector.testConnector", "1.0", ConnectorEvent.ON_ENTER)
                .addInput(inputName1, input1Expression);

        processBuilder.addUserTask("step2", delivery).addTransition("step1", "step2");

        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, userId, processBuilder, false);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        ActivityInstance task1 = waitForUserTask("step1", processInstance);
        final WaitUntil waitUntil1 = waitForVariableStorage(50, 2000, inputName1, valueOfInput1);
        assertTrue(waitUntil1.waitUntil());
        assignAndExecuteStep(task1, userId);

        task1 = waitForUserTask("step1", processInstance);
        final WaitUntil waitUntil2 = waitForVariableStorage(50, 2000, inputName1, valueOfInput1);
        assertTrue(waitUntil2.waitUntil());
        assignAndExecuteStep(task1, userId);

        task1 = waitForUserTask("step1", processInstance);
        final WaitUntil waitUntil3 = waitForVariableStorage(50, 2000, inputName1, valueOfInput1);
        assertTrue(waitUntil3.waitUntil());
        assignAndExecuteStep(task1, userId);

        final ActivityInstance task2 = waitForUserTask("step2", processInstance);
        assignAndExecuteStep(task2, userId);

        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.OTHERS, keywords = { "Connector", "Classpath" }, jira = "ENGINE-773")
    @Test
    public void testExecuteConnectorWithCustomOutputTypeOnActivity() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("testConnectorWithExecutionTooLong", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addShortTextData("value", null);
        designProcessDefinition
                .addAutomaticTask("step1")
                .addConnector("myConnector1", "connectorWithCustomType", "1.0.0", ConnectorEvent.ON_ENTER)
                .addOutput(
                        new OperationBuilder()
                                .createNewInstance()
                                .setLeftOperand("value", false)
                                .setType(OperatorType.ASSIGNMENT)
                                .setRightOperand(
                                        new ExpressionBuilder().createGroovyScriptExpression("script", "output.getValue()", String.class.getName(),
                                                new ExpressionBuilder().createInputExpression("output", "org.connector.custom.CustomType"))).done());
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");
        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final List<BarResource> resources = new ArrayList<BarResource>();
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnectorWithCustomType.impl", "TestConnectorWithCustomType.impl");
        addResource(resources, "/org/bonitasoft/engine/connectors/connector-with-custom-type.bak", "connector-with-custom-type.jar");
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchiveBuilder.addConnectorImplementation(resources.get(0));
        businessArchiveBuilder.addClasspathResource(resources.get(1));
        businessArchiveBuilder.setProcessDefinition(designProcessDefinition.done());
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchiveBuilder.done());
        addMappingOfActorsForUser(delivery, userId, processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance process = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("step2", process, 10000);
        assertEquals("value", getProcessAPI().getProcessDataInstance("value", process.getId()).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.OTHERS, keywords = { "Connector", "Classpath" }, jira = "ENGINE-773")
    @Test
    public void testExecuteConnectorWithCustomOutputTypeOnProcess() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("testConnectorWithExecutionTooLong", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addShortTextData("value", null);
        designProcessDefinition.addConnector("myConnector1", "connectorWithCustomType", "1.0.0", ConnectorEvent.ON_ENTER).addOutput(
                new OperationBuilder()
                        .createNewInstance()
                        .setLeftOperand("value", false)
                        .setType(OperatorType.ASSIGNMENT)
                        .setRightOperand(
                                new ExpressionBuilder().createGroovyScriptExpression("script", "output.getValue()", String.class.getName(),
                                        new ExpressionBuilder().createInputExpression("output", "org.connector.custom.CustomType"))).done());
        designProcessDefinition.addAutomaticTask("step1");
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");
        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final List<BarResource> resources = new ArrayList<BarResource>();
        addResource(resources, "/org/bonitasoft/engine/connectors/TestConnectorWithCustomType.impl", "TestConnectorWithCustomType.impl");
        addResource(resources, "/org/bonitasoft/engine/connectors/connector-with-custom-type.bak", "connector-with-custom-type.jar");
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchiveBuilder.addConnectorImplementation(resources.get(0));
        businessArchiveBuilder.addClasspathResource(resources.get(1));
        businessArchiveBuilder.setProcessDefinition(designProcessDefinition.done());
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchiveBuilder.done());
        addMappingOfActorsForUser(delivery, userId, processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance process = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("step2", process);
        assertEquals("value", getProcessAPI().getProcessDataInstance("value", process.getId()).getValue());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, jira = "ENGINE-783", keywords = { "Connector", "No connector implementation", "cache" }, story = "get connector implementation still work avec cache is cleared.")
    @Test
    public void getConnectorImplementationWorksAfterCacheCleared() throws Exception {
        final String delivery = "Delivery men";

        final String inputName = "input1";
        final String valueOfInput1 = "valueOfInput1";

        // Configure process and human tasks
        final Expression input1Expression = new ExpressionBuilder().createConstantStringExpression(valueOfInput1);
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnStartOfAnAutomaticActivity", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addUserTask("step1", delivery)
                .addConnector("myConnector", "org.bonitasoft.connector.testConnector", "1.0", ConnectorEvent.ON_FINISH).addInput(inputName, input1Expression);
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");

        // Deploy and start process
        final long userId = getIdentityAPI().getUserByUserName(JOHN).getId();
        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, userId, designProcessDefinition, false);

        assertTrue(getProcessAPI().getProcessResolutionProblems(processDefinition.getId()).isEmpty());

        final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
        sessionAccessor.setSessionInfo(getSession().getId(), getSession().getTenantId());

        final CacheService cacheservice = getTenantAccessor().getCacheService();
        cacheservice.clearAll();
        sessionAccessor.deleteSessionId();

        assertTrue(getProcessAPI().getProcessResolutionProblems(processDefinition.getId()).isEmpty());
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Failed", "Database", "Connector", "On enter", "Automatic activity" }, jira = "ENGINE-936")
    @Test
    public void testExecuteFailedNonSerializableOutputConnectorOnEnterOfAnAutomaticActivity() throws Exception {
        testExecuteFailedNonSerializableOutputConnectorOfAnAutomaticActivity(ConnectorEvent.ON_ENTER);
    }

    @Cover(classes = Connector.class, concept = BPMNConcept.CONNECTOR, keywords = { "Failed", "Database", "Connector", "On finish", "Automatic activity" }, jira = "ENGINE-936")
    @Test
    public void testExecuteFailedNonSerializableOutputConnectorOnFinishOfAnAutomaticActivity() throws Exception {
        testExecuteFailedNonSerializableOutputConnectorOfAnAutomaticActivity(ConnectorEvent.ON_FINISH);
    }

    protected void testExecuteFailedNonSerializableOutputConnectorOfAnAutomaticActivity(final ConnectorEvent connectorEvent) throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithNonSerializableOutputConnector", "1.0");
        processBuilder.addStartEvent("start").addEndEvent("end");
        final String actorName = "Employee";
        processBuilder.addActor(actorName);
        processBuilder.addData("resultProcess", Object.class.getName(), new ExpressionBuilder().createConstantLongExpression(0L));
        final AutomaticTaskDefinitionBuilder autoTask = processBuilder.addAutomaticTask("Step1");
        processBuilder.addTransition("start", "end");
        autoTask.addConnector("nonSerializableFailedConnector", "org.bonitasoft.connector.testConnectorWithNotSerializableOutput", "1.0", connectorEvent)
                .addOutput(
                        new OperationBuilder().createNewInstance().setLeftOperand("resultProcess", false).setType(OperatorType.ASSIGNMENT)
                                .setRightOperand(new ExpressionBuilder().createInputExpression("output1", Object.class.getName())).done());

        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(actorName, johnUserId, processBuilder, false);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final ActivityInstance task = waitForTaskToFail(processInstance);
        assertEquals("Step1", task.getName());

        // Clean-up
        Thread.sleep(1000);
        disableAndDeleteProcess(processDefinition);
    }

}
