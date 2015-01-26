/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ManualTaskInstance;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.check.CheckNbPendingTasksForUserUsingSearch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPIT;
import com.bonitasoft.engine.bpm.flownode.ManualTaskCreator;

public class HiddenTaskIT extends CommonAPISPIT {

    private ProcessDefinition processDefinition;

    private User user;

    private SearchOptions searchOptions;

    private CheckNbPendingTasksForUserUsingSearch checkNbOPendingTasks;

    private User user2;

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("ProcessContainingTasksToHide",
                "1.01beta", Arrays.asList("humanTask_1", "humanTask_2"), Arrays.asList(true, true), "actor", true, true);
        user = createUser("common_user", "abc");
        user2 = createUser("uncommon_user", "abc");
        processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, "actor", user);
        final long id = processDefinition.getId();
        getProcessAPI().startProcess(id);

        final SearchOptionsBuilder hBuilder = new SearchOptionsBuilder(0, 10);
        searchOptions = hBuilder.done();

        checkNbOPendingTasks = new CheckNbPendingTasksForUserUsingSearch(getProcessAPI(), 50, 3000, true, 2, user.getId(),
                new SearchOptionsBuilder(0, 100).done());
    }

    @After
    public void afterTest() throws BonitaException {
        disableAndDeleteProcess(processDefinition);
        deleteUsers(user, user2);
        logoutOnTenant();
    }

    @Test
    public void hiddenSubTasksShouldBeRetrieved() throws Exception {
        logoutLogin(user);
        assertTrue("There should be 2 pending tasks for " + user.getUserName(), checkNbOPendingTasks.waitUntil());

        final List<HumanTaskInstance> pendingTasks = checkNbOPendingTasks.getPendingHumanTasks();
        final HumanTaskInstance task1 = pendingTasks.get(0);
        getProcessAPI().assignUserTask(task1.getId(), user.getId());
        final ManualTaskCreator taskCreator = buildManualTaskCreator(task1.getId(), "MySubTask", user.getId(),
                "Sub task that should be shown as hidden when so", null, TaskPriority.NORMAL);
        final ManualTaskInstance manualTask = getProcessAPI().addManualUserTask(taskCreator);

        getProcessAPI().hideTasks(user.getId(), manualTask.getId());

        final SearchResult<HumanTaskInstance> tasks = getProcessAPI().searchPendingHiddenTasks(user.getId(), searchOptions);
        assertEquals(1, tasks.getCount());
        assertEquals(manualTask.getId(), tasks.getResult().get(0).getId());
    }

    private void logoutLogin(final User user) throws BonitaException {
        logoutOnTenant();
        if (user != null) {
            loginOnDefaultTenantWith(user.getUserName(), "abc");
            checkNbOPendingTasks = new CheckNbPendingTasksForUserUsingSearch(getProcessAPI(), 50, 3000, true, 2, user.getId(),
                    new SearchOptionsBuilder(0, 100).done());
        } else {
            loginOnDefaultTenantWithDefaultTechnicalUser();
            checkNbOPendingTasks = new CheckNbPendingTasksForUserUsingSearch(getProcessAPI(), 50, 3000, true, 2, this.user.getId(), new SearchOptionsBuilder(0,
                    100).done());
        }
    }

}
