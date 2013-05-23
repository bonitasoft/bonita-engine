/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.BPMRemoteTests;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition.BEntry;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.CatchMessageEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ThrowMessageEventTriggerBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserUpdater;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.check.CheckNbPendingTaskOf;
import org.bonitasoft.engine.test.wait.WaitForStep;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;
import com.bonitasoft.engine.connector.APIAccessorConnector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LogTest extends CommonAPISPTest {

    @Before
    public void setUp() throws BonitaException {
        login();
    }

    @After
    public void tearDown() throws BonitaException {
        logout();
    }

    @Test(expected = LogNotFoundException.class)
    public void testGetLogNotFound() throws BonitaException {
        getLogAPI().getLog(System.currentTimeMillis());
    }

    @Test
    public void testLogsPageOutOfRangException() throws BonitaException {
        final List<Log> logs = getLogAPI().getLogs(getLogAPI().getNumberOfLogs() * 2, 10, LogCriterion.DEFAULT);
        assertTrue(logs.isEmpty());
    }

    @Test
    public void getLogById() throws Exception {
        // add user: add one log
        final User oneNewUser = getIdentityAPI().createUser("user_for_getLogById", "contrasena");
        final List<Log> logs = getLogAPI().getLogs(0, 1, LogCriterion.DEFAULT);
        final Log expectedLog = logs.get(0);
        final Log retrievedLog = getLogAPI().getLog(expectedLog.getLogId());
        assertEquals(expectedLog, retrievedLog);
        // delete user:
        getIdentityAPI().deleteUser(oneNewUser.getId());
    }

    @Test
    public void getNumberOfLogs() throws BonitaException, Exception {
        final int before = getLogAPI().getNumberOfLogs();

        // add user: add one log
        final User userOld = getIdentityAPI().createUser("old", "oldPassword");

        int actual = getLogAPI().getNumberOfLogs();
        assertEquals(before + 1, actual);

        // update user: add one log
        final UserUpdater updateDescriptor = new UserUpdater();
        updateDescriptor.setUserName("new");
        updateDescriptor.setPassword("newPassword");
        getIdentityAPI().updateUser(userOld.getId(), updateDescriptor);

        actual = getLogAPI().getNumberOfLogs();
        assertEquals(before + 2, actual);

        // delete user: add one log
        getIdentityAPI().deleteUser(userOld.getId());

        actual = getLogAPI().getNumberOfLogs();
        assertEquals(before + 3, actual);
    }

    @Test
    public void getPaginatedLogsByDefault() throws BonitaException {
        final User user1 = getIdentityAPI().createUser("user1", "bpm");
        getIdentityAPI().deleteUser(user1.getId());
        final User user2 = getIdentityAPI().createUser("user2", "bpm");
        getIdentityAPI().deleteUser(user2.getId());

        final List<Log> logs = getLogAPI().getLogs(0, 10, LogCriterion.CREATION_DATE_DESC);
        assertTrue(logs.size() >= 4);
        for (int i = 0; i < logs.size() - 1; i++) {
            assertTrue(logs.get(i).getCreationDate().getTime() >= logs.get(i + 1).getCreationDate().getTime());
        }
    }

    @Test
    public void getPaginatedLogsByCreatedByAsc() throws BonitaException {
        getPaginatedLogsByCreatedBy("ASC");
    }

    private void getPaginatedLogsByCreatedBy(final String orderByType) throws BonitaException {
        final User user3 = getIdentityAPI().createUser("loguser3", "bpm");
        logout();
        loginWith("loguser3", "bpm");
        final User user2 = getIdentityAPI().createUser("loguser2", "bpm");
        logout();
        loginWith("loguser2", "bpm");
        final User user1 = getIdentityAPI().createUser("loguser1", "bpm");
        logout();
        login();
        getIdentityAPI().deleteUser(user1.getId());
        getIdentityAPI().deleteUser(user2.getId());
        getIdentityAPI().deleteUser(user3.getId());

        if ("ASC".equals(orderByType)) {
            final List<Log> logs = getLogAPI().getLogs(0, getLogAPI().getNumberOfLogs(), LogCriterion.CREATED_BY_ASC);
            assertTrue(getIndexOfLogCreatedBy(logs, "loguser2") < getIndexOfLogCreatedBy(logs, "loguser3"));
        } else {
            final List<Log> logs = getLogAPI().getLogs(0, getLogAPI().getNumberOfLogs(), LogCriterion.CREATED_BY_DESC);
            assertTrue(getIndexOfLogCreatedBy(logs, "loguser2") > getIndexOfLogCreatedBy(logs, "loguser3"));
        }
    }

    private int getIndexOfLogCreatedBy(final List<Log> logs, final String username) {
        int index = -1;
        int current = 0;
        final Iterator<Log> iterator = logs.iterator();
        while (iterator.hasNext() && index == -1) {
            final Log log = iterator.next();
            if (log.getCreatedBy().equals(username)) {
                index = current;
            }
            current++;
        }
        return index;
    }

    @Test
    public void getPaginatedLogsByCreatedByDesc() throws BonitaException {
        getPaginatedLogsByCreatedBy("DESC");
    }

    @Test
    public void getPaginatedLogsByCreationDateAsc() throws BonitaException {
        final User user1 = getIdentityAPI().createUser("user1", "bpm");
        getIdentityAPI().deleteUser(user1.getId());
        final User user2 = getIdentityAPI().createUser("user2", "bpm");
        getIdentityAPI().deleteUser(user2.getId());

        final List<Log> logs = getLogAPI().getLogs(0, 10, LogCriterion.CREATION_DATE_ASC);
        assertTrue(logs.size() >= 4);
        for (int i = 0; i < logs.size() - 1; i++) {
            assertTrue(logs.get(i).getCreationDate().getTime() <= logs.get(i + 1).getCreationDate().getTime());
        }
    }

    @Test
    public void getPaginatedLogsByCreationDateDesc() throws BonitaException {
        final User user1 = getIdentityAPI().createUser("user1", "bpm");
        getIdentityAPI().deleteUser(user1.getId());
        final User user2 = getIdentityAPI().createUser("user2", "bpm");
        getIdentityAPI().deleteUser(user2.getId());

        final List<Log> logs = getLogAPI().getLogs(0, 10, LogCriterion.CREATION_DATE_DESC);
        assertTrue(logs.size() >= 4);
        for (int i = 0; i < logs.size() - 1; i++) {
            assertTrue(logs.get(i).getCreationDate().getTime() >= logs.get(i + 1).getCreationDate().getTime());
        }
    }

    @Test
    public void testDontLogJobExecution() throws BonitaException {
        final List<Log> logs = getLogAPI().getLogs(0, getLogAPI().getNumberOfLogs(), LogCriterion.CREATED_BY_DESC);
        assertFalse(contains(logs, "JOB_EXECUTED", 1));
    }

    private boolean contains(final List<Log> logs, final String actionType, final int minimalFrequency) {
        int count = 0;
        final Iterator<Log> iterator = logs.iterator();
        while (iterator.hasNext() && count < minimalFrequency) {
            final Log log = iterator.next();
            if (actionType.equals(log.getActionType())) {
                count++;
            }
        }
        return count == minimalFrequency;
    }

    @Test
    public void testSearchLogsWithAllAvailableFilters() throws Exception {
        final User user1 = getIdentityAPI().createUser("user1WithAllAvailableFilters", "bpm");
        getIdentityAPI().deleteUser(user1.getId());
        final User user2 = getIdentityAPI().createUser("user2WithAllAvailableFilters", "bpm");
        getIdentityAPI().deleteUser(user2.getId());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 140);
        builder.filter(LogSearchDescriptor.ACTION_SCOPE, String.valueOf(user1.getId()));
        builder.filter(LogSearchDescriptor.ACTION_TYPE, "IDENTITY_USER_DELETED");
        builder.filter(LogSearchDescriptor.CREATED_BY, "install");
        builder.filter(LogSearchDescriptor.MESSAGE, "Deleting user with username user1WithAllAvailableFilters");
        builder.filter(LogSearchDescriptor.SEVERITY, "INTERNAL");

        builder.sort(LogSearchDescriptor.CREATED_BY, Order.ASC);
        final SearchResult<Log> searchLogs = getLogAPI().searchLogs(builder.done());

        assertEquals(1, searchLogs.getCount());

    }

    @Test
    public void testSearchLogsWithAllSearchTerm() throws Exception {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 140);
        builder.searchTerm("Adding");
        builder.sort(LogSearchDescriptor.ACTION_TYPE, Order.ASC);
        SearchResult<Log> searchLogs = getLogAPI().searchLogs(builder.done());
        final long initialCount = searchLogs.getCount();

        final User user1 = getIdentityAPI().createUser("user1SearchTerm", "bpm");
        getIdentityAPI().deleteUser(user1.getId());
        final User user2 = getIdentityAPI().createUser("user2SearchTerm", "bpm");
        getIdentityAPI().deleteUser(user2.getId());

        searchLogs = getLogAPI().searchLogs(builder.done());
        assertEquals(initialCount + 2, searchLogs.getCount());
    }

    @Test(expected = SearchException.class)
    public void searchLogWithWrongSortKey() throws BonitaException {
        final User user1 = getIdentityAPI().createUser("user1WrongSortKey", "bpm");
        getIdentityAPI().deleteUser(user1.getId());
        final User user2 = getIdentityAPI().createUser("user2WrongSortKey", "bpm");
        getIdentityAPI().deleteUser(user2.getId());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 140);
        builder.searchTerm("with username user1WrongSortKey");
        builder.sort("WRONG_SORT_KEY", Order.ASC);
        getLogAPI().searchLogs(builder.done());
    }

    /*
     * Similar to messageStartEventMessageSentAfterEnable
     * Just one more check : There is no log with actionType JOB_EXECUTED
     */
    @Test
    public void testDontLogJobExecutionEvent() throws Exception {
        final ProcessDefinition sendMessageProcess = deployAndEnableProcessWithEndMessageEvent("sendMessageProcess", "m1", "receiveMessageProcess",
                "startEvent", null, null, null, null);
        final User user = createUser("john", "bpm");
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithStartMessageEvent("receiveMessageProcess", "step1", "delivery", user, "m1",
                null, null);

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());
        assertTrue(isProcessInstanceFinishedAndArchived(20, 5000, sendMessageProcessInstance, getProcessAPI()));

        final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 100, 6000, true, 1, user);
        assertTrue("there is no pending task", checkNbPendingTaskOf.waitUntil());

        final List<HumanTaskInstance> taskInstances = getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 10, ActivityInstanceCriterion.NAME_ASC);
        assertEquals(1, taskInstances.size());
        final HumanTaskInstance taskInstance = taskInstances.get(0);
        assertEquals("step1", taskInstance.getName());

        final List<Log> logs = getLogAPI().getLogs(0, getLogAPI().getNumberOfLogs(), LogCriterion.CREATED_BY_DESC);
        assertFalse(containsLogWithActionType(logs, "JOB_EXECUTED", 1));

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);

        deleteUser("john");
    }

    private ProcessDefinition deployAndEnableProcessWithEndMessageEvent(final String processName, final String messageName, final String targetProcess,
            final String targetFlowNode, final List<BEntry<Expression, Expression>> correlations, final Map<String, String> processData,
            final Map<String, String> messageData, final Map<String, String> dataInputMapping) throws BonitaException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt();
        processBuilder.createNewInstance(processName, "1.0");
        addProcessData(processData, processBuilder);
        processBuilder.addStartEvent("startEvent");
        processBuilder.addAutomaticTask("auto1");
        // create expression for target process/flowNode
        final Expression targetProcessExpression = new ExpressionBuilder().createConstantStringExpression(targetProcess);
        final Expression targetFlowNodeExpression = new ExpressionBuilder().createConstantStringExpression(targetFlowNode);
        final ThrowMessageEventTriggerBuilder throwMessageEventTriggerBuilder = processBuilder.addEndEvent("endEvent").addMessageEventTrigger(messageName,
                targetProcessExpression, targetFlowNodeExpression);
        addCorrelations(correlations, throwMessageEventTriggerBuilder);
        addMessageData(messageData, dataInputMapping, throwMessageEventTriggerBuilder);
        processBuilder.addTransition("startEvent", "auto1");
        processBuilder.addTransition("auto1", "endEvent");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();

        final ProcessDefinition sendMessageProcess = deployAndEnableProcess(designProcessDefinition);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(sendMessageProcess.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        return sendMessageProcess;
    }

    private void addProcessData(final Map<String, String> data, final ProcessDefinitionBuilderExt processBuilder) {
        if (data != null) {
            for (final Entry<String, String> entry : data.entrySet()) {
                processBuilder.addData(entry.getKey(), entry.getValue(), null);
            }
        }
    }

    private void addCorrelations(final List<BEntry<Expression, Expression>> correlations, final ThrowMessageEventTriggerBuilder throwMessageEventTriggerBuilder) {
        if (correlations != null) {
            for (final Entry<Expression, Expression> entry : correlations) {
                throwMessageEventTriggerBuilder.addCorrelation(entry.getKey(), entry.getValue());
            }
        }
    }

    private void addMessageData(final Map<String, String> messageData, final Map<String, String> dataInputMapping,
            final ThrowMessageEventTriggerBuilder throwMessageEventTriggerBuilder) throws InvalidExpressionException {
        if (messageData != null) {
            for (final Entry<String, String> entry : messageData.entrySet()) {
                final Expression displayName = new ExpressionBuilder().createConstantStringExpression(entry.getKey());

                Expression defaultValue = null;
                if (dataInputMapping.containsKey(entry.getKey())) {
                    defaultValue = new ExpressionBuilder().createDataExpression(dataInputMapping.get(entry.getKey()), entry.getValue());
                }
                throwMessageEventTriggerBuilder.addMessageContentExpression(displayName, defaultValue);
            }
        }

    }

    private ProcessDefinition deployAndEnableProcessWithStartMessageEvent(final String processName, final String userTaskName, final String actorName,
            final User user, final String messageName, final Map<String, String> data, final List<Operation> catchMessageOperations) throws BonitaException {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt();
        processBuilder.createNewInstance(processName, "1.0");
        addProcessData(data, processBuilder);
        final CatchMessageEventTriggerDefinitionBuilder messageEventTrigger = processBuilder.addStartEvent("startEvent").addMessageEventTrigger(messageName);
        addCatchMessageOperations(catchMessageOperations, messageEventTrigger);
        processBuilder.addActor(actorName);
        processBuilder.addUserTask(userTaskName, actorName);
        processBuilder.addEndEvent("endEvent");
        processBuilder.addTransition("startEvent", userTaskName);
        processBuilder.addTransition(userTaskName, "endEvent");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();

        final BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder();
        archiveBuilder.createNewBusinessArchive().setProcessDefinition(designProcessDefinition);
        final BusinessArchive receiveMessaceArchive = archiveBuilder.done();
        final ProcessDefinition receiveMessageProcess = getProcessAPI().deploy(receiveMessaceArchive);

        final List<ActorInstance> actors = getProcessAPI().getActors(receiveMessageProcess.getId(), 0, 1, ActorCriterion.NAME_ASC);
        getProcessAPI().addUserToActor(actors.get(0).getId(), user.getId());

        getProcessAPI().enableProcess(receiveMessageProcess.getId());

        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(receiveMessageProcess.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        return receiveMessageProcess;
    }

    private void addCatchMessageOperations(final List<Operation> catchMessageOperations, final CatchMessageEventTriggerDefinitionBuilder messageEventTrigger) {
        if (catchMessageOperations != null) {
            for (final Operation operation : catchMessageOperations) {
                messageEventTrigger.addOperation(operation);
            }
        }
    }

    @Test
    public void executeConnectorOnFinishOfAnAutomaticActivityWithDataAsOutputUsingAPIAccessor() throws Exception {
        final String johnName = "john";
        createUser(johnName, "bpm");
        final String delivery = "Delivery men";
        final Expression dataDefaultValue = new ExpressionBuilder().createConstantLongExpression(0);
        final ProcessDefinitionBuilderExt designProcessDefinition = new ProcessDefinitionBuilderExt().createNewInstance(
                "executeConnectorOnFinishOfAnAutomaticActivityWithDataAsOutput", "1.0");
        final String dataName = "myData1";
        final String procInstIdData = "procInstId";
        final String nbLogsData = "nbLogsData";
        final String searchLogsData = "searchLogsData";
        final String getLogsData = "getLogsData";
        final String profileData = "profileData";
        designProcessDefinition.addLongData(dataName, dataDefaultValue);
        designProcessDefinition.addLongData(procInstIdData, dataDefaultValue);
        designProcessDefinition.addIntegerData(nbLogsData, new ExpressionBuilder().createConstantIntegerExpression(0));
        designProcessDefinition.addData(searchLogsData, SearchResult.class.getName(), null);
        designProcessDefinition.addData(getLogsData, List.class.getName(), dataDefaultValue);
        designProcessDefinition.addData(profileData, Profile.class.getName(), dataDefaultValue);
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addUserTask("step0", delivery);
        designProcessDefinition
                .addAutomaticTask("step1")
                .addConnector("myConnector", "org.bonitasoft.connector.APIAccessorConnector", "1.0", ConnectorEvent.ON_FINISH)
                .addOutput(new LeftOperandBuilder().createNewInstance().setName(dataName).done(), OperatorType.ASSIGNMENT, "=", "",
                        new ExpressionBuilder().createInputExpression("numberOfUsers", Long.class.getName()))
                .addOutput(new LeftOperandBuilder().createNewInstance().setName(procInstIdData).done(), OperatorType.ASSIGNMENT, "=", null,
                        new ExpressionBuilder().createInputExpression("procInstId", Long.class.getName()))
                .addOutput(new LeftOperandBuilder().createNewInstance().setName(nbLogsData).done(), OperatorType.ASSIGNMENT, "=", null,
                        new ExpressionBuilder().createInputExpression("nbLogs", Integer.class.getName()))
                .addOutput(new LeftOperandBuilder().createNewInstance().setName(searchLogsData).done(), OperatorType.ASSIGNMENT, "=", null,
                        new ExpressionBuilder().createInputExpression("searchLogs", SearchResult.class.getName()))
                .addOutput(new LeftOperandBuilder().createNewInstance().setName(getLogsData).done(), OperatorType.ASSIGNMENT, "=", null,
                        new ExpressionBuilder().createInputExpression("getLogs", List.class.getName()))
                .addOutput(new LeftOperandBuilder().createNewInstance().setName(profileData).done(), OperatorType.ASSIGNMENT, "=", null,
                        new ExpressionBuilder().createInputExpression("profile", Profile.class.getName()));
        designProcessDefinition.addUserTask("step2", delivery);
        designProcessDefinition.addTransition("step0", "step1");
        designProcessDefinition.addTransition("step1", "step2");

        final long userId = getIdentityAPI().getUserByUserName(johnName).getId();
        final ProcessDefinition processDefinition = deployProcessWithDefaultTestConnector(delivery, userId, designProcessDefinition);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        final long procInstanceId = startProcess.getId();
        assertEquals(0l, getProcessAPI().getProcessDataInstance(dataName, procInstanceId).getValue());
        final WaitForStep waitForStep0 = waitForStep("step0", startProcess);
        assignAndExecuteStep(waitForStep0.getResult(), userId);

        waitForStep("step2", startProcess);

        final long numberOfUsers = getIdentityAPI().getNumberOfUsers();
        assertEquals(numberOfUsers, getProcessAPI().getProcessDataInstance(dataName, procInstanceId).getValue());
        // check processInstanceId retrieved from injected context:
        assertEquals(procInstanceId, getProcessAPI().getProcessDataInstance(procInstIdData, procInstanceId).getValue());
        assertTrue("Number of Logs should be > 0", (Integer) getProcessAPI().getProcessDataInstance(nbLogsData, procInstanceId).getValue() > 0);
        assertTrue("Number of SearchResult should be > 0",
                ((SearchResult<?>) getProcessAPI().getProcessDataInstance(searchLogsData, procInstanceId).getValue()).getCount() > 0);
        assertTrue("Number of getLogs should be > 0", ((List<?>) getProcessAPI().getProcessDataInstance(getLogsData, procInstanceId).getValue()).size() > 0);
        final Profile profile = (Profile) getProcessAPI().getProcessDataInstance(profileData, procInstanceId).getValue();
        assertEquals("addProfileCommandFromConnector", profile.getName());

        deleteUser(johnName);
        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployProcessWithDefaultTestConnector(final String delivery, final long userId,
            final ProcessDefinitionBuilderExt designProcessDefinition) throws BonitaException, IOException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                designProcessDefinition.done());
        final List<BarResource> connectorImplementations = generateDefaultConnectorImplementations();
        for (final BarResource barResource : connectorImplementations) {
            businessArchiveBuilder.addConnectorImplementation(barResource);
        }

        final List<BarResource> generateConnectorDependencies = generateDefaultConnectorDependencies();
        for (final BarResource barResource : generateConnectorDependencies) {
            businessArchiveBuilder.addClasspathResource(barResource);
        }

        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchiveBuilder.done());
        addMappingOfActorsForUser(delivery, userId, processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    private List<BarResource> generateDefaultConnectorImplementations() throws IOException {
        final List<BarResource> resources = new ArrayList<BarResource>(1);
        addResource(resources, "/com/bonitasoft/engine/connector/APIAccessorConnector.impl", "APIAccessorConnector.impl");
        return resources;
    }

    private List<BarResource> generateDefaultConnectorDependencies() throws IOException {
        final List<BarResource> resources = new ArrayList<BarResource>(1);
        addResource(resources, APIAccessorConnector.class, "APIAccessorConnector.jar");
        return resources;
    }

    private void addResource(final List<BarResource> resources, final Class<?> clazz, final String name) throws IOException {
        final byte[] data = IOUtil.generateJar(clazz);
        resources.add(new BarResource(name, data));
    }

    private void addResource(final List<BarResource> resources, final String path, final String name) throws IOException {
        final InputStream stream = BPMRemoteTests.class.getResourceAsStream(path);
        assertNotNull(stream);
        final byte[] byteArray = IOUtils.toByteArray(stream);
        stream.close();
        resources.add(new BarResource(name, byteArray));
    }

    @Test
    public void log() throws BonitaException, InterruptedException {
        final Group group = getIdentityAPI().createGroup("group1", null);
        getIdentityAPI().deleteGroup(group.getId());

        final int initialNumberOfLogs = getLogAPI().getNumberOfLogs();

        final User userOld = getIdentityAPI().createUser("old", "oldPassword");
        assertEquals(initialNumberOfLogs + 1, getLogAPI().getNumberOfLogs());
        Thread.sleep(10);
        final UserUpdater updateDescriptor = new UserUpdater();
        updateDescriptor.setUserName("new");
        updateDescriptor.setPassword("newPassword");
        getIdentityAPI().updateUser(userOld.getId(), updateDescriptor);
        assertEquals(initialNumberOfLogs + 2, getLogAPI().getNumberOfLogs());

        Thread.sleep(10);
        getIdentityAPI().deleteUser(userOld.getId());
        assertEquals(initialNumberOfLogs + 3, getLogAPI().getNumberOfLogs());

        final List<Log> logs = getLogAPI().getLogs(0, 3, LogCriterion.CREATION_DATE_DESC);
        assertEquals("IDENTITY_USER_DELETED", logs.get(0).getActionType());
        assertEquals(SeverityLevel.INTERNAL, logs.get(0).getSeverityLevel());
        assertEquals("org.bonitasoft.engine.identity.impl.IdentityServiceImpl", logs.get(0).getCallerClassName());
        assertEquals("deleteUser", logs.get(0).getCallerMethodName());

        assertEquals("IDENTITY_USER_UPDATED", logs.get(1).getActionType());
        assertEquals(SeverityLevel.INTERNAL, logs.get(1).getSeverityLevel());
        assertEquals("org.bonitasoft.engine.identity.impl.IdentityServiceImpl", logs.get(1).getCallerClassName());
        assertEquals("updateUser", logs.get(1).getCallerMethodName());

        assertEquals("IDENTITY_USER_CREATED", logs.get(2).getActionType());
        assertEquals(SeverityLevel.INTERNAL, logs.get(2).getSeverityLevel());
        assertEquals("org.bonitasoft.engine.identity.impl.IdentityServiceImpl", logs.get(2).getCallerClassName());
        assertEquals("createUser", logs.get(2).getCallerMethodName());
    }

}
