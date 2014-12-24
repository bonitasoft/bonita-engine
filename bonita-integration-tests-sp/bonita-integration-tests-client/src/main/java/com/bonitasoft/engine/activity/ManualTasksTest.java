/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.activity;

import java.util.Date;

import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ManualTaskInstance;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPIT;
import com.bonitasoft.engine.bpm.flownode.ManualTaskCreator;
import com.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilderExt;

public class ManualTasksTest extends CommonAPISPIT {

    private static final String JOHN = "john";

    @After
    public void afterTest() throws BonitaException {
        deleteUser(JOHN);
        VariableStorage.clearAll();
        logoutOnTenant();
    }

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        createUser(JOHN, "bpm");
        logoutOnTenant();
        loginOnDefaultTenantWith(JOHN, "bpm");
    }

    private ProcessDefinition deployProcessWithUserTask(final User user1) throws Exception {
        final ProcessDefinitionBuilderExt processBuilder = new ProcessDefinitionBuilderExt().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor("myActor");
        processBuilder.addUserTask("Request", "myActor");
        return deployAndEnableProcessWithActor(processBuilder.done(), "myActor", user1);
    }

    @Test(expected = UpdateException.class)
    public void unableToReleaseManualTask() throws Exception {
        final User user = createUser("login1", "password");
        final ProcessDefinition processDefinition = deployProcessWithUserTask(user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance task = waitForUserTask(startProcess, "Request");
        final long taskId = task.getId();
        loginOnDefaultTenantWithDefaultTechnicalUser();
        loginOnDefaultTenantWith("login1", "password");
        getProcessAPI().assignUserTask(taskId, user.getId());

        final ManualTaskCreator taskCreator = buildManualTaskCreator(taskId, "subtask", user.getId(), "desk", new Date(), TaskPriority.NORMAL);
        final ManualTaskInstance manualUserTask = getProcessAPI().addManualUserTask(taskCreator);
        try {
            getProcessAPI().releaseUserTask(manualUserTask.getId());
        } finally {
            deleteUser(user);
            disableAndDeleteProcess(processDefinition);
        }
    }

}
