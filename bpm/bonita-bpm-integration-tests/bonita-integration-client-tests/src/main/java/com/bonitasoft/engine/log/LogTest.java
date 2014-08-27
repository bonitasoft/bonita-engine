/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.log;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition.BEntry;
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
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;

public class LogTest extends CommonAPISPTest {

    @After
    public void afterTest() throws Exception {
       logoutOnTenant();
    }

    @Before
    public void beforeTest() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    @Test(expected = LogNotFoundException.class)
    public void testGetLogNotFound() throws BonitaException {
        getLogAPI().getLog(System.currentTimeMillis());
    }

    @Test
    public void testLogsPageOutOfRangException() {
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
        final User user1 = getIdentityAPI().createUser("user1", PASSWORD);
        getIdentityAPI().deleteUser(user1.getId());
        final User user2 = getIdentityAPI().createUser("user2", PASSWORD);
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
        final User user3 = createUser("loguser3", PASSWORD);
       logoutOnTenant();
        loginOnDefaultTenantWith("loguser3", PASSWORD);
        final User user2 = createUser("loguser2", PASSWORD);
       logoutOnTenant();
        loginOnDefaultTenantWith("loguser2", PASSWORD);
        final User user1 = createUser("loguser1", PASSWORD);
       logoutOnTenant();
        loginOnDefaultTenantWithDefaultTechnicalUser();
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
        final User user1 = getIdentityAPI().createUser("user1", PASSWORD);
        getIdentityAPI().deleteUser(user1.getId());
        final User user2 = getIdentityAPI().createUser("user2", PASSWORD);
        getIdentityAPI().deleteUser(user2.getId());

        final List<Log> logs = getLogAPI().getLogs(0, 10, LogCriterion.CREATION_DATE_ASC);
        assertTrue(logs.size() >= 4);
        for (int i = 0; i < logs.size() - 1; i++) {
            assertTrue(logs.get(i).getCreationDate().getTime() <= logs.get(i + 1).getCreationDate().getTime());
        }
    }

    @Test
    public void getPaginatedLogsByCreationDateDesc() throws BonitaException {
        final User user1 = getIdentityAPI().createUser("user1", PASSWORD);
        getIdentityAPI().deleteUser(user1.getId());
        final User user2 = getIdentityAPI().createUser("user2", PASSWORD);
        getIdentityAPI().deleteUser(user2.getId());

        final List<Log> logs = getLogAPI().getLogs(0, 10, LogCriterion.CREATION_DATE_DESC);
        assertTrue(logs.size() >= 4);
        for (int i = 0; i < logs.size() - 1; i++) {
            assertTrue(logs.get(i).getCreationDate().getTime() >= logs.get(i + 1).getCreationDate().getTime());
        }
    }

    @Test
    public void testDontLogJobExecution() {
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
        final User user1 = getIdentityAPI().createUser("user1WithAllAvailableFilters", PASSWORD);
        getIdentityAPI().deleteUser(user1.getId());
        final User user2 = getIdentityAPI().createUser("user2WithAllAvailableFilters", PASSWORD);
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

        final User user1 = getIdentityAPI().createUser("user1SearchTerm", PASSWORD);
        getIdentityAPI().deleteUser(user1.getId());
        final User user2 = getIdentityAPI().createUser("user2SearchTerm", PASSWORD);
        getIdentityAPI().deleteUser(user2.getId());

        searchLogs = getLogAPI().searchLogs(builder.done());
        assertEquals(initialCount + 2, searchLogs.getCount());
    }

    @Test(expected = SearchException.class)
    public void searchLogWithWrongSortKey() throws BonitaException {
        final User user1 = getIdentityAPI().createUser("user1WrongSortKey", PASSWORD);
        getIdentityAPI().deleteUser(user1.getId());
        final User user2 = getIdentityAPI().createUser("user2WrongSortKey", PASSWORD);
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
        final User user = createUser(USERNAME, PASSWORD);
        final ProcessDefinition receiveMessageProcess = deployAndEnableProcessWithStartMessageEvent("receiveMessageProcess", "step1", "ACTOR_NAME", user, "m1",
                null, null);

        final ProcessInstance sendMessageProcessInstance = getProcessAPI().startProcess(sendMessageProcess.getId());
        assertTrue(waitForProcessToFinishAndBeArchived(sendMessageProcessInstance));

        waitForUserTask("step1");

        final List<Log> logs = getLogAPI().getLogs(0, getLogAPI().getNumberOfLogs(), LogCriterion.CREATED_BY_DESC);
        assertFalse(containsLogWithActionType(logs, "JOB_EXECUTED", 1));

        disableAndDeleteProcess(sendMessageProcess);
        disableAndDeleteProcess(receiveMessageProcess);
        deleteUser(USERNAME);
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
