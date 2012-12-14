package com.bonitasoft.engine.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.BPMTestUtil;
import org.bonitasoft.engine.CommonAPISPTest;
import org.bonitasoft.engine.bpm.model.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.model.HumanTaskInstance;
import org.bonitasoft.engine.bpm.model.ManualTaskInstance;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.TaskPriority;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.wait.CheckNbPendingTasksForUserUsingSearch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HiddenTaskTest extends CommonAPISPTest {

    private ProcessDefinition processDefinition;

    private User user;

    private SearchOptions searchOptions;

    private CheckNbPendingTasksForUserUsingSearch checkNbOPendingTasks;

    private User user2;

    @Before
    public void beforeTest() throws BonitaException {
        login();

        final DesignProcessDefinition designProcessDefinition = BPMTestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("ProcessContainingTasksToHide",
                "1.01beta", Arrays.asList("humanTask_1", "humanTask_2"), Arrays.asList(true, true), "actor", true, true);
        user = createUser("common_user", "abc");
        user2 = createUser("uncommon_user", "abc");
        processDefinition = deployAndEnableWithActor(designProcessDefinition, "actor", user);
        final long id = processDefinition.getId();
        getProcessAPI().startProcess(id);

        final SearchOptionsBuilder hBuilder = new SearchOptionsBuilder(0, 10);
        searchOptions = hBuilder.done();

        checkNbOPendingTasks = new CheckNbPendingTasksForUserUsingSearch(getProcessAPI(), 50, 3000, true, 2, user.getId(),
                new SearchOptionsBuilder(0, 100).done());
    }

    @After
    public void afterTest() throws BonitaException {
        disableAndDelete(processDefinition);
        deleteUser(user.getId());
        deleteUser(user2.getId());
        logout();
    }

    @Test
    public void hiddenSubTasksShouldBeRetrieved() throws Exception {
        logoutLogin(user);
        assertTrue("There should be 2 pending tasks for " + user.getUserName(), checkNbOPendingTasks.waitUntil());

        final List<HumanTaskInstance> pendingTasks = checkNbOPendingTasks.getPendingHumanTasks();
        final HumanTaskInstance task1 = pendingTasks.get(0);
        getProcessAPI().assignUserTask(task1.getId(), user.getId());
        final ManualTaskInstance manualTask = getProcessAPI().addManualUserTask(task1.getId(), "MySubTask", "My visible hidden sub-task", user.getId(),
                "Sub task that should be shown as hidden when so", null, TaskPriority.NORMAL);

        getProcessAPI().hideTasks(user.getId(), manualTask.getId());

        final SearchResult<HumanTaskInstance> tasks = getProcessAPI().searchPendingHiddenTasks(user.getId(), searchOptions);
        assertEquals(1, tasks.getCount());
        assertEquals(manualTask.getId(), tasks.getResult().get(0).getId());
    }

    private void logoutLogin(final User user) throws BonitaException {
        logout();
        if (user != null) {
            loginWith(user.getUserName(), user.getPassword());
            checkNbOPendingTasks = new CheckNbPendingTasksForUserUsingSearch(getProcessAPI(), 50, 3000, true, 2, user.getId(),
                    new SearchOptionsBuilder(0, 100).done());
        } else {
            login();
            checkNbOPendingTasks = new CheckNbPendingTasksForUserUsingSearch(getProcessAPI(), 50, 3000, true, 2, this.user.getId(), new SearchOptionsBuilder(0,
                    100).done());
        }
    }

}
