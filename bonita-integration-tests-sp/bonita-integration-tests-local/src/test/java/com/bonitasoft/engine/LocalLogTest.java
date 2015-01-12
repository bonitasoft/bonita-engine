/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;

public class LocalLogTest extends CommonAPISPIT {

    @After
    public void afterTest() throws Exception {
        logoutOnTenant();
    }

    @Before
    public void beforeTest() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    // run this test in local test suite only, otherwise it's necessary to use a command to set the system property on the server side
    @Ignore("This test fails because Property 'org.bonitasoft.engine.services.queryablelog.disable' is only read at startup, so change is not taken into account")
    @Test
    public void testDisableLogs() throws Exception {
        final int initNumberOfLogs = getLogAPI().getNumberOfLogs();
        User user1 = getIdentityAPI().createUser("user1WrongSortKey", "bpm");
        getIdentityAPI().deleteUser(user1.getId());
        int numberOfLogs = getLogAPI().getNumberOfLogs();
        assertEquals("Number of logs should have increase of 1!", initNumberOfLogs + 2, numberOfLogs);

        System.setProperty("org.bonitasoft.engine.services.queryablelog.disable", "true");

        user1 = getIdentityAPI().createUser("user1WrongSortKey", "bpm");
        getIdentityAPI().deleteUser(user1.getId());
        numberOfLogs = getLogAPI().getNumberOfLogs();

        assertEquals("Number of logs should not have changed!", initNumberOfLogs + 2, numberOfLogs);

        System.clearProperty("org.bonitasoft.engine.services.queryablelog.disable");
    }

    @Test
    public void executeConnectorOnFinishOfAnAutomaticActivityWithDataAsOutputUsingAPIAccessor() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);
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
        designProcessDefinition.addData(getLogsData, List.class.getName(), null);
        designProcessDefinition.addData(profileData, Profile.class.getName(), null);
        designProcessDefinition.addActor(ACTOR_NAME).addDescription("Delivery all day and night long");
        designProcessDefinition.addUserTask("step0", ACTOR_NAME);
        designProcessDefinition
                .addAutomaticTask("step1")
                .addConnector("myConnector", "com.bonitasoft.connector.APIAccessorConnector", "1.0", ConnectorEvent.ON_FINISH)
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
        designProcessDefinition.addUserTask("step2", ACTOR_NAME);
        designProcessDefinition.addTransition("step0", "step1");
        designProcessDefinition.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActorAndAPIAccessorConnector(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        final long procInstanceId = startProcess.getId();
        assertEquals(0l, getProcessAPI().getProcessDataInstance(dataName, procInstanceId).getValue());
        waitForUserTaskAndExecuteIt(startProcess, "step0", user);
        waitForUserTask(startProcess, "step2");

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

        deleteUser(USERNAME);
        disableAndDeleteProcess(processDefinition);
    }
}
