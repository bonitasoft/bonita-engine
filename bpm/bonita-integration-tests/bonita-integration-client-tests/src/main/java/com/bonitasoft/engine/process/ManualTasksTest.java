package com.bonitasoft.engine.process;

import java.util.Date;

import org.bonitasoft.engine.bpm.model.ActivityInstance;
import org.bonitasoft.engine.bpm.model.ManualTaskInstance;
import org.bonitasoft.engine.bpm.model.ProcessDefinition;
import org.bonitasoft.engine.bpm.model.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.model.ProcessInstance;
import org.bonitasoft.engine.bpm.model.TaskPriority;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.InvalidSessionException;
import org.bonitasoft.engine.exception.ProcessDefinitionNotEnabledException;
import org.bonitasoft.engine.exception.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.exception.ProcessDefinitionReadException;
import org.bonitasoft.engine.exception.ProcessInstanceCreationException;
import org.bonitasoft.engine.exception.UnreleasableTaskException;
import org.bonitasoft.engine.identity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;

public class ManualTasksTest extends CommonAPISPTest {

    private static final String JOHN = "john";

    private User john;

    @After
    public void afterTest() throws BonitaException {
        deleteUser(JOHN);
        VariableStorage.clearAll();
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
        john = createUser(JOHN, "bpm");
        logout();
        loginWith(john);
    }

    private ProcessDefinition deployProcessWithUserTask(final User user1) throws BonitaException, InvalidSessionException, ProcessDefinitionNotFoundException,
            ProcessDefinitionReadException, ProcessDefinitionNotEnabledException, ProcessInstanceCreationException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor("myActor");
        processBuilder.addUserTask("Request", "myActor");
        return deployAndEnableWithActor(processBuilder.done(), "myActor", user1);
    }

    @Test(expected = UnreleasableTaskException.class)
    public void unableToReleaseManualTask() throws Exception {
        final User user = createUser("login1", "password");
        final ProcessDefinition processDefinition = deployProcessWithUserTask(user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance task = waitForUserTask("Request", startProcess);
        final long taskId = task.getId();
        login();
        loginWith(user);
        getProcessAPI().assignUserTask(taskId, user.getId());

        final ManualTaskInstance manualUserTask = getProcessAPI().addManualUserTask(taskId, "subtask", "MySubTask", user.getId(), "desk", new Date(),
                TaskPriority.NORMAL);
        try {
            getProcessAPI().releaseUserTask(manualUserTask.getId());
        } finally {
            deleteUser(user);
            disableAndDelete(processDefinition);
        }
    }

}
