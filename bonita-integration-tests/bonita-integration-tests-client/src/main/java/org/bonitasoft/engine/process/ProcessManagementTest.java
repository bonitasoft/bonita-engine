package org.bonitasoft.engine.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.ExternalResourceContribution;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeType;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessActivationException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeployException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoUpdater;
import org.bonitasoft.engine.bpm.process.ProcessEnablementException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.bpm.process.impl.AutomaticTaskDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.StartProcessUntilStep;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.check.CheckNbOfActivities;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 * @author Frédéric Bouquet
 * @author Céline Souchet
 */
public class ProcessManagementTest extends CommonAPITest {

    @After
    public void afterTest() throws BonitaException {
        logoutOnTenant();
    }

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    @Test
    public void getProcessDeployInfo() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);
        logoutThenloginAs(USERNAME, PASSWORD);

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));
        final Date before = new Date();
        Thread.sleep(10);
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        addUserToFirstActorOfProcess(user.getId(), processDefinition);
        Thread.sleep(10);
        final Date after = new Date();

        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        deleteProcess(processDefinition);
        assertNotNull(processDeploymentInfo);
        assertEquals(TestStates.getProcessDepInfoResolvedState(), processDeploymentInfo.getConfigurationState());
        assertEquals(APITestUtil.PROCESS_NAME, processDeploymentInfo.getName());
        assertEquals(APITestUtil.PROCESS_VERSION, processDeploymentInfo.getVersion());
        assertEquals(getSession().getUserId(), processDeploymentInfo.getDeployedBy());
        final Date deployDate = processDeploymentInfo.getDeploymentDate();
        assertTrue("deploy date was too soon (expected > " + before.getTime() + " but was " + deployDate.getTime() + ")", deployDate.after(before));
        assertTrue("deploy date was too late (expected < " + after.getTime() + " but was " + deployDate.getTime() + ")", deployDate.before(after));

        logoutThenlogin();
        deleteUser(user);
    }

    @Test(expected = ProcessActivationException.class)
    public void runDisabledProcess() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));

        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        addUserToFirstActorOfProcess(user.getId(), processDefinition);

        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(TestStates.getProcessDepInfoResolvedState(), processDeploymentInfo.getConfigurationState());

        try {
            getProcessAPI().startProcess(user.getId(), processDefinition.getId());
        } finally {
            deleteProcess(processDefinition);
            deleteUser(user);
        }
    }

    @Test
    public void disableProcess() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));
        final User user = createUser(USERNAME, PASSWORD);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(TestStates.getProcessDepInfoEnabledState(), processDeploymentInfo.getActivationState());
        getProcessAPI().disableProcess(processDefinition.getId());
        processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(TestStates.getProcessDepInfoResolvedState(), processDeploymentInfo.getConfigurationState());

        deleteProcess(processDefinition);
        deleteUser(user);
    }

    @Test(expected = ProcessActivationException.class)
    public void disableDisabledProcess() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));

        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        try {
            getProcessAPI().disableProcess(processDefinition.getId());
            getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        } finally {
            deleteProcess(processDefinition);
        }
    }

    @Test
    public void enableEnabledProcess() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));

        final User user = createUser(USERNAME, PASSWORD);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        try {
            getProcessAPI().enableProcess(processDefinition.getId());
            final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
            assertEquals(TestStates.getProcessDepInfoResolvedState(), processDeploymentInfo.getConfigurationState());
            fail("expected a ProcessEnablementException");
        } catch (final ProcessEnablementException e) {
            // ok
        } finally {
            getProcessAPI().disableProcess(processDefinition.getId());
            deleteProcess(processDefinition);
            deleteUser(user);
        }
    }

    @Test(expected = DeletionException.class)
    public void deleteEnabledProcess() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));

        final User user = createUser(USERNAME, PASSWORD);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        try {
            deleteProcess(processDefinition);
        } finally {
            getProcessAPI().disableProcess(processDefinition.getId());
            deleteProcess(processDefinition);
            deleteUser(user);
        }
    }

    @Test
    public void deleteProcess() throws Exception {
        final long numberOfProcesses = getProcessAPI().getNumberOfProcessDeploymentInfos();
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));

        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        assertNotNull(getProcessAPI().getProcessDefinition(processDefinition.getId()));
        assertNotNull(getProcessAPI().getProcessDeploymentInfo(processDefinition.getId()));
        assertEquals(numberOfProcesses + 1, getProcessAPI().getNumberOfProcessDeploymentInfos());
        deleteProcess(processDefinition);
        assertEquals(numberOfProcesses, getProcessAPI().getNumberOfProcessDeploymentInfos());
        try {
            getProcessAPI().getProcessDefinition(processDefinition.getId());
            fail("Should throw ProcessDefinitionNotFoundException");
        } catch (final ProcessDefinitionNotFoundException e) {
            // ok
        }
        try {
            getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
            fail("Should throw ProcessDefinitionNotFoundException");
        } catch (final ProcessDefinitionNotFoundException e) {
            // ok
        }
    }

    @Test
    public void getProcessesList() throws Exception {
        assertEquals(0, getProcessAPI().getNumberOfProcessDeploymentInfos());
        final List<Long> ids = createProcessDefinitionWithTwoHumanStepsAndDeployBusinessArchive(89);

        assertEquals(89, getProcessAPI().getNumberOfProcessDeploymentInfos());
        final List<ProcessDeploymentInfo> processes = getProcessAPI().getProcessDeploymentInfos(0, 10, ProcessDeploymentInfoCriterion.NAME_DESC);
        assertEquals(10, processes.size());
        assertEquals("ProcessName88", processes.get(0).getName());
        assertEquals("ProcessName79", processes.get(9).getName());
        final List<ProcessDeploymentInfo> processes1 = getProcessAPI().getProcessDeploymentInfos(0, 10, ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(10, processes1.size());
        assertEquals("ProcessName00", processes1.get(0).getName());
        assertEquals("ProcessName09", processes1.get(9).getName());
        final List<ProcessDeploymentInfo> processes2 = getProcessAPI().getProcessDeploymentInfos(20, 10, ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(10, processes2.size());
        assertEquals("ProcessName20", processes2.get(0).getName());
        assertEquals("ProcessName29", processes2.get(9).getName());
        getProcessAPI().deleteProcesses(ids);
        assertEquals(0, getProcessAPI().getNumberOfProcessDeploymentInfos());
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void createProcessWithNoName() throws Exception {
        final List<String> emptyList = Collections.emptyList();
        final List<Boolean> emptyList2 = Collections.emptyList();
        final DesignProcessDefinition processDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(null, null, emptyList,
                emptyList2);
        getProcessAPI().deploy(new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinition).done());
    }

    @Test
    public void getArchivedActivityInstances() throws Exception {
        final DesignProcessDefinition designProcessDefinition1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(PROCESS_NAME + "1",
                PROCESS_VERSION, Arrays.asList("step1", "step2"), Arrays.asList(false, false));
        final ProcessDefinition processDefinition1 = deployAndEnableProcess(designProcessDefinition1);
        final DesignProcessDefinition designProcessDefinition2 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(PROCESS_NAME + "2",
                PROCESS_VERSION, Arrays.asList("task1", "task2", "task3"), Arrays.asList(false, false, false));
        final ProcessDefinition processDefinition2 = deployAndEnableProcess(designProcessDefinition2);
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition1.getId());
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition2.getId());
        // one archive for each change in the activity state. For automatic tasks we have initializingAndexecuting, completed
        checkNbOfArchivedActivityInstances(processInstance1, 2 * 2);
        checkNbOfArchivedActivityInstances(processInstance2, 3 * 2);
        assertTrue(waitForProcessToFinishAndBeArchived(processInstance1));
        assertTrue(waitForProcessToFinishAndBeArchived(processInstance2));
        disableAndDeleteProcess(processDefinition1, processDefinition2);
    }

    @Ignore("Pb : ReachedStateDate has 0 for value for activities in state executing")
    @Test
    public void getArchivedActivityInstancesOrderByStartDate() throws Exception {
        getArchivedActivityInstancesOrderByPagingCriterion(ActivityInstanceCriterion.REACHED_STATE_DATE_ASC, 0, 1, 2,
                ActivityInstanceCriterion.REACHED_STATE_DATE_DESC, 2, 1, 0);
    }

    @Ignore("Pb : lastUpdateDate has 0 for value for activities in state executing")
    @Test
    public void getArchivedActivityInstancesOrderByLastUpdateDate() throws Exception {
        getArchivedActivityInstancesOrderByPagingCriterion(ActivityInstanceCriterion.LAST_UPDATE_ASC, 0, 1, 2, ActivityInstanceCriterion.LAST_UPDATE_DESC, 2,
                1, 0);
    }

    @Test
    public void getArchivedActivityInstancesOrderByName() throws Exception {
        getArchivedActivityInstancesOrderByPagingCriterion(ActivityInstanceCriterion.NAME_ASC, 0, 1, 2, ActivityInstanceCriterion.NAME_DESC, 2, 1, 0);
    }

    private void getArchivedActivityInstancesOrderByPagingCriterion(final ActivityInstanceCriterion criterionAsc, final int asc1, final int asc2,
            final int asc3, final ActivityInstanceCriterion criterionDsc, final int desc1, final int desc2, final int desc3) throws Exception {

        final DesignProcessDefinition designProcessDefinition2 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(PROCESS_NAME
                + criterionAsc,
                PROCESS_VERSION, Arrays.asList("task1", "task2", "task3"), Arrays.asList(false, false, false));
        final ProcessDefinition processDefinition2 = deployAndEnableProcess(designProcessDefinition2);
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition2.getId());
        final int nbActivities = 3; // task1, task2, task3
        final int nbOfStates = 2; // executing, completed
        checkNbOfArchivedActivityInstances(processInstance2, 3 * nbOfStates);
        List<ArchivedActivityInstance> archivedActivityInstances = getProcessAPI().getArchivedActivityInstances(processInstance2.getId(), 0, 100, criterionAsc);
        for (int i = 0; i < nbOfStates; i++) {
            if (criterionAsc.equals(ActivityInstanceCriterion.REACHED_STATE_DATE_ASC) || criterionAsc.equals(ActivityInstanceCriterion.LAST_UPDATE_ASC)) {
                assertEquals("task1", archivedActivityInstances.get(asc1 + i * nbActivities).getName());
                assertEquals("task2", archivedActivityInstances.get(asc2 + i * nbActivities).getName());
                assertEquals("task3", archivedActivityInstances.get(asc3 + i * nbActivities).getName());
            }
            if (criterionAsc.equals(ActivityInstanceCriterion.NAME_ASC)) {
                assertEquals("task1", archivedActivityInstances.get(asc1 * nbOfStates + i).getName());
                assertEquals("task2", archivedActivityInstances.get(asc2 * nbOfStates + i).getName());
                assertEquals("task3", archivedActivityInstances.get(asc3 * nbOfStates + i).getName());
            }
        }

        archivedActivityInstances = getProcessAPI().getArchivedActivityInstances(processInstance2.getId(), 0, 100, criterionDsc);
        for (int i = 0; i < nbOfStates; i++) {
            if (criterionDsc.equals(ActivityInstanceCriterion.REACHED_STATE_DATE_DESC) || criterionDsc.equals(ActivityInstanceCriterion.LAST_UPDATE_DESC)) {
                assertEquals("task1", archivedActivityInstances.get(desc1 + i * nbActivities).getName());
                assertEquals("task2", archivedActivityInstances.get(desc2 + i * nbActivities).getName());
                assertEquals("task3", archivedActivityInstances.get(desc3 + i * nbActivities).getName());
            }
            if (criterionDsc.equals(ActivityInstanceCriterion.NAME_DESC)) {
                assertEquals("task1", archivedActivityInstances.get(desc1 * nbOfStates + i).getName());
                assertEquals("task2", archivedActivityInstances.get(desc2 * nbOfStates + i).getName());
                assertEquals("task3", archivedActivityInstances.get(desc3 * nbOfStates + i).getName());
            }
        }
        assertTrue(waitForProcessToFinishAndBeArchived(processInstance2));
        disableAndDeleteProcess(processDefinition2);
    }

    @Test
    public void getArchivedActivityInstancesOfAnUnknownProcess() {
        final List<ArchivedActivityInstance> archivedActivityInstances = getProcessAPI().getArchivedActivityInstances(456213846564l, 0, 100,
                ActivityInstanceCriterion.REACHED_STATE_DATE_ASC);
        assertEquals(0, archivedActivityInstances.size());
    }

    /**
     * checks that {@link ProcessManagementAPI#getOpenedActivityInstances(long, int, int, ActivityInstanceCriterion)} returns the good list of open activities
     * only.
     * An open activity is an activity with state NON-FINAL and STABLE.
     */
    @Test
    public void checkActivityInstancesWithOpenState() throws Exception {
        final DesignProcessDefinition designProcessDefinition1 = BuildTestUtil.buildProcessDefinitionWithActorAndThreeHumanStepsAndThreeTransition();

        final BusinessArchive businessArchive1 = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition1).done();
        final ProcessDefinition processDefinition1 = getProcessAPI().deploy(businessArchive1);

        addUserToFirstActorOfProcess(1, processDefinition1);

        getProcessAPI().enableProcess(processDefinition1.getId());
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition1.getId());

        // 1 instance of process def 2:
        final ProcessDefinitionBuilder processBuilder2 = new ProcessDefinitionBuilder().createNewInstance("checkActivityInstancesWithOpenState",
                PROCESS_VERSION);
        processBuilder2.addActor(ACTOR_NAME);

        final DesignProcessDefinition designProcessDefinition2 = processBuilder2.addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME)
                .addUserTask("step3", ACTOR_NAME).addTransition("step1", "step2").addTransition("step1", "step3").getProcess();
        final BusinessArchive businessArchive2 = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition2).done();
        final ProcessDefinition processDefinition2 = getProcessAPI().deploy(businessArchive2);

        addUserToFirstActorOfProcess(1, processDefinition2);

        getProcessAPI().enableProcess(processDefinition2.getId());
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition2.getId());

        // Check the size returned, and the state of each one:
        checkNbOfOpenActivities(processInstance1, 3);
        List<ActivityInstance> openedActivityInstances = getProcessAPI().getOpenActivityInstances(processInstance1.getId(), 0, 3000,
                ActivityInstanceCriterion.DEFAULT);
        for (final ActivityInstance activityInstance : openedActivityInstances) {
            // Check that all TestStates are Open (Ready):
            assertEquals(activityInstance.getState(), TestStates.getReadyState());
        }

        checkNbOfOpenActivities(processInstance2, 2);
        openedActivityInstances = getProcessAPI().getOpenActivityInstances(processInstance1.getId(), 0, 200, ActivityInstanceCriterion.DEFAULT);
        for (final ActivityInstance activityInstance : openedActivityInstances) {
            // Check that all TestStates are Open (Ready):
            assertEquals(activityInstance.getState(), TestStates.getReadyState());
        }

        disableAndDeleteProcess(processDefinition1, processDefinition2);
    }

    @Test
    public void openActivityInstancesOrder() throws Exception {
        checkOpenActivityInstanceOrder(ActivityInstanceCriterion.NAME_ASC, ActivityInstanceCriterion.NAME_DESC);
        // checkOpenActivityInstanceOrder(ActivityInstanceCriterion.PRIORITY_ASC, ActivityInstanceCriterion.PRIORITY_DESC);
    }

    @Test
    public void openActivityInstancesOrderByLastUpdate() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);

        // Process def : one starting automatic activity that fires 3 human activities:
        final DesignProcessDefinition designProcessDefinition = processBuilder.addAutomaticTask("step0").addUserTask("step1", ACTOR_NAME)
                .addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addTransition("step0", "step1").addTransition("step0", "step2")
                .addTransition("step0", "step3").getProcess();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        addUserToFirstActorOfProcess(1, processDefinition);

        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance procInstance = getProcessAPI().startProcess(processDefinition.getId());

        // We check the size first, to be sure to wait long enough before retrieving the list:
        checkNbOfOpenActivities(procInstance, 3);

        List<ActivityInstance> openedActivityInstances;
        openedActivityInstances = getProcessAPI().getOpenActivityInstances(procInstance.getId(), 0, 200, ActivityInstanceCriterion.LAST_UPDATE_ASC);
        assertTrue(openedActivityInstances.get(0).getLastUpdateDate().compareTo(openedActivityInstances.get(1).getLastUpdateDate()) <= 0);
        assertTrue(openedActivityInstances.get(1).getLastUpdateDate().compareTo(openedActivityInstances.get(2).getLastUpdateDate()) <= 0);

        openedActivityInstances = getProcessAPI().getOpenActivityInstances(procInstance.getId(), 0, 200, ActivityInstanceCriterion.LAST_UPDATE_DESC);
        assertTrue(openedActivityInstances.get(0).getLastUpdateDate().compareTo(openedActivityInstances.get(1).getLastUpdateDate()) >= 0);
        assertTrue(openedActivityInstances.get(1).getLastUpdateDate().compareTo(openedActivityInstances.get(2).getLastUpdateDate()) >= 0);

        disableAndDeleteProcess(processDefinition);
    }

    private void checkOpenActivityInstanceOrder(final ActivityInstanceCriterion ascendingCriterion, final ActivityInstanceCriterion descendingCriterion)
            throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);

        // Process def : one starting automatic activity that fires 3 human activities:
        final DesignProcessDefinition designProcessDefinition = processBuilder.addAutomaticTask("step0").addUserTask("step1", ACTOR_NAME)
                .addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addTransition("step0", "step1").addTransition("step0", "step2")
                .addTransition("step0", "step3").getProcess();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        addUserToFirstActorOfProcess(1, processDefinition);

        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance procInstance = getProcessAPI().startProcess(processDefinition.getId());

        checkNbOfOpenActivities(procInstance, 3);

        List<ActivityInstance> openedActivityInstances;
        openedActivityInstances = getProcessAPI().getOpenActivityInstances(procInstance.getId(), 0, 200, ascendingCriterion);
        assertEquals("step1", openedActivityInstances.get(0).getName());
        assertEquals("step2", openedActivityInstances.get(1).getName());
        assertEquals("step3", openedActivityInstances.get(2).getName());

        openedActivityInstances = getProcessAPI().getOpenActivityInstances(procInstance.getId(), 0, 200, descendingCriterion);
        assertEquals("step1", openedActivityInstances.get(2).getName());
        assertEquals("step2", openedActivityInstances.get(1).getName());
        assertEquals("step3", openedActivityInstances.get(0).getName());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getNumberOfOpenedActivityInstances() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithActorAndThreeHumanStepsAndThreeTransition();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        addUserToFirstActorOfProcess(1, processDefinition);

        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance procInstance = getProcessAPI().startProcess(processDefinition.getId());
        checkNbOfOpenActivities(procInstance, 3);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getProcessDefinitionIdFromProcessInstanceId() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());

        final long processDefinitionId = getProcessAPI().getProcessDefinitionIdFromProcessInstanceId(pi0.getId());
        assertEquals(processDefinition.getId(), processDefinitionId);

        // Clean up
        waitForUserTask("step1", pi0);
        disableAndDeleteProcess(processDefinition);
        assertEquals(0, getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.DEFAULT).size());
        deleteUser(user);
    }

    @Test
    public void getProcessDefinitionIdFromActivityInstanceId() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());

        final List<ActivityInstance> activityInstances = getProcessAPI().getActivities(pi0.getId(), 0, 10);
        for (final ActivityInstance activityInstance : activityInstances) {
            final long processDefinitionId = getProcessAPI().getProcessDefinitionIdFromActivityInstanceId(activityInstance.getId());
            assertEquals(processDefinition.getId(), processDefinitionId);
        }

        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    @Test
    public void getProcessResources() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                processDefinitionBuilder.done());

        // Add a resource to the biz archive:
        final String dummyText = "DUMMY";
        final String dummyFile = "dummy.txt";
        final String documentText = "SOME DOCUMENT TEXT";
        final String documentFile = "folder/document.txt";
        businessArchiveBuilder.addExternalResource(new BarResource(dummyFile, dummyText.getBytes()));
        businessArchiveBuilder.addExternalResource(new BarResource(documentFile, documentText.getBytes()));
        businessArchiveBuilder.addExternalResource(new BarResource("folder/image.jpg", "UNUSED".getBytes()));

        // deploy the process to unzip the .bar in BONITA_HOME:
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchiveBuilder.done());
        final Map<String, byte[]> resources = getProcessAPI().getProcessResources(processDefinition.getId(), ".*/.*\\.txt");
        assertEquals(2, resources.size());
        assertTrue("Searched resource not returned", resources.containsKey(ExternalResourceContribution.EXTERNAL_RESOURCE_FOLDER + "/" + dummyFile));
        assertTrue("Searched resource not returned", resources.containsKey(ExternalResourceContribution.EXTERNAL_RESOURCE_FOLDER + "/" + documentFile));
        final byte[] dum = resources.get(ExternalResourceContribution.EXTERNAL_RESOURCE_FOLDER + "/" + dummyFile);
        assertTrue("File content not the expected", Arrays.equals(dummyText.getBytes(), dum));
        final byte[] doc = resources.get(ExternalResourceContribution.EXTERNAL_RESOURCE_FOLDER + "/" + documentFile);
        assertTrue("File content not the expected", Arrays.equals(documentText.getBytes(), doc));

        deleteProcess(processDefinition);
    }

    @Test
    public void getLatestProcessDefinitionId() throws Exception {
        // create two process definitions
        final String processName = "getLatestProcessDefinitionId";
        final DesignProcessDefinition designProcessDefinition1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(processName, "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition1 = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition1).done());

        final DesignProcessDefinition designProcessDefinition2 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(processName, "2.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition2 = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition2).done());

        // test and do assert
        final long latestProcessDefinitionId = getProcessAPI().getLatestProcessDefinitionId(processName);
        assertEquals(processDefinition2.getId(), latestProcessDefinitionId);

        deleteProcess(processDefinition1, processDefinition2);
    }

    @Test(expected = ProcessDefinitionNotFoundException.class)
    public void getLatestProcessDefinitionIdWithProcessInstanceNotFoundException() throws Exception {
        final String PROCESS_NAME = String.valueOf(System.currentTimeMillis());
        getProcessAPI().getLatestProcessDefinitionId(PROCESS_NAME);
    }

    @Test
    public void getSupportedStates() {
        Set<String> TestStatesName = getProcessAPI().getSupportedStates(FlowNodeType.USER_TASK);
        assertEquals(5, TestStatesName.size());
        TestStatesName = getProcessAPI().getSupportedStates(FlowNodeType.AUTOMATIC_TASK);
        assertEquals(3, TestStatesName.size());
        TestStatesName = getProcessAPI().getSupportedStates(FlowNodeType.START_EVENT);
        assertEquals(2, TestStatesName.size());
        TestStatesName = getProcessAPI().getSupportedStates(FlowNodeType.END_EVENT);
        assertEquals(2, TestStatesName.size());
        TestStatesName = getProcessAPI().getSupportedStates(FlowNodeType.GATEWAY);
        assertEquals(2, TestStatesName.size());
        TestStatesName = getProcessAPI().getSupportedStates(FlowNodeType.INTERMEDIATE_CATCH_EVENT);
        assertEquals(4, TestStatesName.size());
        TestStatesName = getProcessAPI().getSupportedStates(FlowNodeType.CALL_ACTIVITY);
        assertEquals(5, TestStatesName.size());

    }

    @Test
    public void getActivityInstanceState() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        final List<ActivityInstance> activityInstances = getProcessAPI().getActivities(pi0.getId(), 0, 10);
        for (final ActivityInstance activityInstance : activityInstances) {
            final String stateName = getProcessAPI().getActivityInstanceState(activityInstance.getId());
            assertTrue("initializing".equals(stateName) || "ready".equals(stateName));
        }
        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    @Cover(classes = { ProcessAPI.class, ActivityInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "ActivityInstance", "Pagination" }, jira = "ENGINE-680")
    @Test
    public void getActivityInstancePaginated() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);
        final ProcessDefinitionBuilder definitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        definitionBuilder.addStartEvent("start");
        definitionBuilder.addActor(ACTOR_NAME);
        definitionBuilder.addUserTask("initTask1", ACTOR_NAME);
        definitionBuilder.addUserTask("initTask2", ACTOR_NAME);
        definitionBuilder.addUserTask("initTask3", ACTOR_NAME);
        definitionBuilder.addEndEvent("end");
        definitionBuilder.addTransition("start", "initTask1");
        definitionBuilder.addTransition("start", "initTask2");
        definitionBuilder.addTransition("start", "initTask3");
        final DesignProcessDefinition designProcessDefinition = definitionBuilder.done();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        checkNbOfHumanTasks(3);

        List<ActivityInstance> activityInstances = getProcessAPI().getActivities(processInstance.getId(), 0, 2);
        assertEquals(2, activityInstances.size());
        activityInstances = getProcessAPI().getActivities(processInstance.getId(), 2, 2);
        assertEquals(1, activityInstances.size());

        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    @Test
    public void getProcessDefinitionIdByNameAndVersion() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        // do test and assert
        final long processDefId = getProcessAPI().getProcessDefinitionId(APITestUtil.PROCESS_NAME, APITestUtil.PROCESS_VERSION);
        assertEquals(processDefId, processDefinition.getId());

        deleteProcess(processDefinition);
    }

    @Test(expected = ProcessDefinitionNotFoundException.class)
    public void getProcessDefinitionIdByNameAndVersionWithExcepton() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        // do test and assert
        try {
            getProcessAPI().getProcessDefinitionId(APITestUtil.PROCESS_NAME + "_wrongName", APITestUtil.PROCESS_VERSION);
        } finally {
            deleteProcess(processDefinition);
        }
    }

    @Test
    public void severalActivityUpdates() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);
        final String delivery = "Delivery men";
        final UserTaskDefinitionBuilder userTask = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION).addActor(delivery)
                .addDescription("Delivery all day and night long").addUserTask("step1", delivery);
        final String dataName1 = "dataName";
        final String dataName2 = "myHero";
        userTask.addShortTextData(dataName1, new ExpressionBuilder().createConstantStringExpression("beforeUpdate"));
        userTask.addShortTextData(dataName2, new ExpressionBuilder().createConstantStringExpression("Actarus"));
        final DesignProcessDefinition processDef = userTask.getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, delivery, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        final StartProcessUntilStep startProcessAndWaitForTask = startProcessAndWaitForTask(processDeploymentInfo.getProcessId(), "step1");

        final long activityInstanceId = startProcessAndWaitForTask.getActivityInstance().getId();

        DataInstance dataInstance = getProcessAPI().getActivityDataInstance(dataName1, activityInstanceId);
        final String newConstantValue1 = "afterUpdate";
        final Operation stringOperation = BuildTestUtil.buildStringOperation(dataInstance.getName(), newConstantValue1, false);

        final String newConstantValue2 = "GOLDORAK";
        dataInstance = getProcessAPI().getActivityDataInstance(dataName2, activityInstanceId);
        final Operation stringOperation2 = BuildTestUtil.buildStringOperation(dataInstance.getName(), newConstantValue2, false);
        final List<Operation> operations = new ArrayList<Operation>();
        operations.add(stringOperation);
        operations.add(stringOperation2);
        getProcessAPI().updateActivityInstanceVariables(operations, activityInstanceId, null);

        DataInstance dataI = getProcessAPI().getActivityDataInstance(dataName1, activityInstanceId);
        assertEquals(newConstantValue1, dataI.getValue());

        dataI = getProcessAPI().getActivityDataInstance(dataName2, activityInstanceId);
        assertEquals(newConstantValue2, dataI.getValue());

        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    @Test
    public void updateActivityInstanceVariables() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        UserTaskDefinitionBuilder addUserTask = processDefinitionBuilder
                .addActor(ACTOR_NAME)
                .addDescription("Delivery all day and night long")
                .addUserTask("step1", ACTOR_NAME);
        processDefinitionBuilder.addShortTextData("a", new ExpressionBuilder().createConstantStringExpression("aprocess"));
        processDefinitionBuilder.addShortTextData("b", new ExpressionBuilder().createConstantStringExpression("bprocess"));
        processDefinitionBuilder.addShortTextData("c", new ExpressionBuilder().createConstantStringExpression("cprocess"));
        processDefinitionBuilder.addShortTextData("d", new ExpressionBuilder().createConstantStringExpression("dprocess"));
        processDefinitionBuilder.addShortTextData("e", new ExpressionBuilder().createConstantStringExpression("eprocess"));
        addUserTask.addShortTextData("a", new ExpressionBuilder().createConstantStringExpression("aacti"));
        addUserTask.addShortTextData("b", new ExpressionBuilder().createConstantStringExpression("bacti")).isTransient();
        addUserTask.addShortTextData("f", new ExpressionBuilder().createConstantStringExpression("facti"));
        addUserTask.addShortTextData("g", new ExpressionBuilder().createConstantStringExpression("gacti")).isTransient();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDefinitionBuilder.getProcess(), ACTOR_NAME, user);
        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        HumanTaskInstance step1 = waitForUserTask("step1", processInstance);

        List<DataInstance> dataInstances = getProcessAPI().getActivityDataInstances(step1.getId(), 0, 10);
        assertThat(dataInstances).hasSize(6);
        ArrayList<String> names = new ArrayList<String>(6);
        ArrayList<String> values = new ArrayList<String>(6);
        for (DataInstance dataInstance2 : dataInstances) {
            names.add(dataInstance2.getName());
            values.add((String) dataInstance2.getValue());
        }
        assertThat(names).contains("a", "b", "c", "d", "e", "f");
        assertThat(values).contains("aacti", "bprocess", "cprocess", "dprocess", "eprocess", "facti");
        final List<Operation> operations = new ArrayList<Operation>();
        for (DataInstance dataInstance2 : dataInstances) {
            final Operation stringOperation = BuildTestUtil.buildStringOperation(dataInstance2.getName(), dataInstance2.getValue() + "+up", false);
            operations.add(stringOperation);
        }
        getProcessAPI().updateActivityInstanceVariables(operations, step1.getId(), null);

        dataInstances = getProcessAPI().getActivityDataInstances(step1.getId(), 0, 10);
        assertThat(dataInstances).hasSize(6);
        values = new ArrayList<String>(6);
        for (DataInstance dataInstance2 : dataInstances) {
            values.add((String) dataInstance2.getValue());
        }
        assertThat(values).contains("aacti+up", "bprocess+up", "cprocess+up", "dprocess+up", "eprocess+up", "facti+up");
        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    @Test
    public void updateActivityInstanceVariable() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        final DesignProcessDefinition processDef = createProcessWithActorAndHumanTaskAndStringData();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        waitForStep("step1", processInstance);

        final List<ActivityInstance> activityInstances = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        final long activityInstanceId = activityInstances.get(0).getId();
        final String updatedValue = "afterUpdate";

        final Map<String, Serializable> variables = new HashMap<String, Serializable>(2);
        variables.put("dataName", updatedValue);
        getProcessAPI().updateActivityInstanceVariables(activityInstanceId, variables);

        final DataInstance dataInstance = getProcessAPI().getActivityDataInstance("dataName", activityInstanceId);
        assertEquals(updatedValue, dataInstance.getValue());
        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    @Test(expected = UpdateException.class)
    public void cannotUpdateAnActivityInstanceVariable() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        final DesignProcessDefinition processDef = createProcessWithActorAndHumanTaskAndStringData();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        waitForStep("step1", processInstance);

        final List<ActivityInstance> activityInstances = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        final long activityInstanceId = activityInstances.get(0).getId();
        final String updatedValue = "afterUpdate";

        final Map<String, Serializable> variables = new HashMap<String, Serializable>(2);
        variables.put("dataName1", updatedValue);
        try {
            getProcessAPI().updateActivityInstanceVariables(activityInstanceId, variables);
        } finally {
            disableAndDeleteProcess(processDefinition);
            deleteUser(user);
        }
    }

    @Test
    public void canExecuteTask() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);
        final long userId = user.getId();

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1"),
                Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long activityInstanceId = waitForUserTask("step1", processInstance).getId();
        assertFalse("The user " + USERNAME + " shouldn't be able to execute the task step1.", getProcessAPI().canExecuteTask(activityInstanceId, userId));

        getProcessAPI().assignUserTask(activityInstanceId, userId);
        Thread.sleep(100);
        assertTrue("The user " + USERNAME + " should be able to execute the task step1.", getProcessAPI().canExecuteTask(activityInstanceId, userId));

        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    @Test
    public void getOneAssignedUserTaskInstanceOfProcessInstance() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        final DesignProcessDefinition processDef = createProcessWithActorAndHumanTaskAndStringData();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, ACTOR_NAME, user);
        final ActivityInstance activityInstance = createProcessAndAssignUserTask(user, processDefinition);

        final long userTaskId = getProcessAPI().getOneAssignedUserTaskInstanceOfProcessInstance(activityInstance.getParentContainerId(), user.getId());
        assertEquals(activityInstance.getId(), userTaskId);

        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    @Test
    public void getOneAssignedUserTaskInstanceOfProcessDefinition() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        final DesignProcessDefinition processDef = createProcessWithActorAndHumanTaskAndStringData();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, ACTOR_NAME, user);
        final ActivityInstance activityInstance = createProcessAndAssignUserTask(user, processDefinition);

        final long userTaskId = getProcessAPI().getOneAssignedUserTaskInstanceOfProcessDefinition(processDefinition.getId(), user.getId());
        assertEquals(activityInstance.getId(), userTaskId);

        deleteUser(user);
        disableAndDeleteProcess(processDefinition);
    }

    private ActivityInstance createProcessAndAssignUserTask(final User user, final ProcessDefinition processDefinition) throws Exception {
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        waitForStep("step1", processInstance);

        final List<ActivityInstance> activityInstances = new ArrayList<ActivityInstance>(getProcessAPI().getActivities(processInstance.getId(), 0, 20));
        final ActivityInstance activityInstance = activityInstances.get(activityInstances.size() - 1);

        assertEquals("ready", activityInstance.getState());

        getProcessAPI().assignUserTask(activityInstance.getId(), user.getId());
        return activityInstance;
    }

    @Test
    public void updateProcessDeploymentInfo() throws Exception {
        // create process definition;
        final String processDescription = "myProcessDisplayName";
        final String processDisplayName = "myProcessDescription";
        final String processDisplayDescription = "myProcessDisplayDescription";

        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION)
                .addDisplayName(processDisplayName).addDisplayDescription(processDisplayDescription).addDescription(processDescription)
                .addUserTask("task1", "actor").getProcess();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        final long processId = processDefinition.getId();
        // before update, display name should be the same as process name
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processId);
        assertEquals(PROCESS_NAME, processDeploymentInfo.getName());
        assertEquals(processDisplayName, processDeploymentInfo.getDisplayName());
        assertEquals(processDisplayDescription, processDeploymentInfo.getDisplayDescription());
        assertEquals(processDescription, processDeploymentInfo.getDescription());
        assertEquals(null, processDeploymentInfo.getIconPath());

        // update and do assert
        final ProcessDeploymentInfoUpdater processDeploymentInfoUpdateDescriptor = new ProcessDeploymentInfoUpdater();
        final String updatedDisplayName = "updatedDisplayName";
        final String updatedDisplayDescription = "updatedDisplayDescription";
        final String iconPath = "iconPathOne";
        processDeploymentInfoUpdateDescriptor.setDisplayName(updatedDisplayName);
        processDeploymentInfoUpdateDescriptor.setDisplayDescription(updatedDisplayDescription);
        processDeploymentInfoUpdateDescriptor.setIconPath(iconPath);
        getProcessAPI().updateProcessDeploymentInfo(processId, processDeploymentInfoUpdateDescriptor);

        final ProcessDeploymentInfo updatedProcessDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processId);
        assertEquals(updatedDisplayName, updatedProcessDeploymentInfo.getDisplayName());
        assertEquals(updatedDisplayDescription, updatedProcessDeploymentInfo.getDisplayDescription());
        assertEquals(iconPath, updatedProcessDeploymentInfo.getIconPath());

        deleteProcess(processDefinition);
    }

    @Test
    public void updateProcessDisplayDescriptionToNull() throws Exception {
        // create process definition;
        final String processDescription = "myProcessDisplayName";
        final String processDisplayName = "myProcessDescription";
        final String processDisplayDescription = "myProcessDisplayDescription";

        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION)
                .addDisplayName(processDisplayName).addDisplayDescription(processDisplayDescription).addDescription(processDescription)
                .addUserTask("task1", "actor").getProcess();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        final long processId = processDefinition.getId();
        // before update, display name should be the same as process name
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processId);
        assertEquals(PROCESS_NAME, processDeploymentInfo.getName());
        assertEquals(processDisplayName, processDeploymentInfo.getDisplayName());
        assertEquals(processDisplayDescription, processDeploymentInfo.getDisplayDescription());
        assertEquals(processDescription, processDeploymentInfo.getDescription());

        // update and do assert
        final ProcessDeploymentInfoUpdater processDeploymentInfoUpdateDescriptor = new ProcessDeploymentInfoUpdater();
        final String updatedDisplayDescription = null;
        processDeploymentInfoUpdateDescriptor.setDisplayDescription(updatedDisplayDescription);
        getProcessAPI().updateProcessDeploymentInfo(processId, processDeploymentInfoUpdateDescriptor);

        final ProcessDeploymentInfo updatedProcessDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processId);
        assertEquals(updatedDisplayDescription, updatedProcessDeploymentInfo.getDisplayDescription());

        deleteProcess(processDefinition);
    }

    @Test(expected = ProcessDefinitionNotFoundException.class)
    public void updateProcessDeploymentInfoWithProcessDefinitionNotFoundException() throws Exception {
        // create process definition;
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        final long processId = processDefinition.getId();
        // update with wrong processId
        final ProcessDeploymentInfoUpdater processDeploymentInfoUpdateDescriptor = new ProcessDeploymentInfoUpdater();
        final String updatedDisplayName = "updatedDisplayName";
        processDeploymentInfoUpdateDescriptor.setDisplayName(updatedDisplayName);
        try {
            getProcessAPI().updateProcessDeploymentInfo(processId + 1, processDeploymentInfoUpdateDescriptor);
        } finally {
            deleteProcess(processDefinition);
        }
    }

    @Test(expected = UpdateException.class)
    public void updateProcessDeploymentInfoWithException() throws Exception {
        // create process definition;
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));
        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        final long processId = processDefinition.getId();
        // update and do assert
        final ProcessDeploymentInfoUpdater processDeploymentInfoUpdateDescriptor = new ProcessDeploymentInfoUpdater();
        try {
            getProcessAPI().updateProcessDeploymentInfo(processId, processDeploymentInfoUpdateDescriptor);
        } finally {
            deleteProcess(processDefinition);
        }
    }

    @Test
    public void checkDataNameInProcess() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        // create process definition with integer data;
        final String dataName1 = "$nAéç_mèE";
        final String dataName2 = "refhbh bgrtg";
        final String dataName3 = "refhbh-bgrtg";
        final String dataName4 = "refhbh+bgrtg";
        final String dataName5 = "refhbh?bgrtg";
        try {
            new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION)
                    .addIntegerData(dataName1, new ExpressionBuilder().createConstantIntegerExpression(1))
                    .addIntegerData(dataName2, new ExpressionBuilder().createConstantIntegerExpression(2))
                    .addIntegerData(dataName3, new ExpressionBuilder().createConstantIntegerExpression(3))
                    .addIntegerData(dataName4, new ExpressionBuilder().createConstantIntegerExpression(4))
                    .addIntegerData(dataName5, new ExpressionBuilder().createConstantIntegerExpression(5)).getProcess();

            fail("This test should not reach this statement");
        } catch (final InvalidProcessDefinitionException ipde) {
            // System.out.println(ipde.getMessage());
            assertTrue(!ipde.getMessage().contains(dataName1));
            assertTrue(ipde.getMessage().contains(dataName2));
            assertTrue(ipde.getMessage().contains(dataName3));
            assertTrue(ipde.getMessage().contains(dataName4));
            assertTrue(ipde.getMessage().contains(dataName5));
        } finally {
            deleteUser(user);
        }
    }

    @Test
    public void checkProcessInstanceDataValue() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        // create process definition with integer data;
        final String dataName = "var1";
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION).addActor(ACTOR_NAME)
                .addIntegerData(dataName, new ExpressionBuilder().createConstantIntegerExpression(1)).addUserTask("step1", ACTOR_NAME)
                .addAutomaticTask("step2").addTransition("step1", "step2").getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, ACTOR_NAME, user);

        // create Operation keyed map
        final Operation integerOperation = BuildTestUtil.buildIntegerOperation(dataName, 2);
        final Map<String, Serializable> context = new HashMap<String, Serializable>();
        context.put("page", "1");
        final long processDefinitionId = processDefinition.getId();
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinitionId, Arrays.asList(integerOperation), context);
        final DataInstance dataInstance = getProcessAPI().getProcessDataInstance(dataName, processInstance.getId());
        assertEquals(2, dataInstance.getValue());

        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    @Test
    public void getActivityReachedStateDate() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step one", "step two"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition1 = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition1.getId());

        HumanTaskInstance humanTaskInstance = checkNbPendingTaskOf(user).getPendingHumanTaskInstances().get(0);
        final Date reachedDate = humanTaskInstance.getReachedStateDate();
        assertNotNull(reachedDate);
        humanTaskInstance = getProcessAPI().getHumanTaskInstance(humanTaskInstance.getId());
        final long readyDate = humanTaskInstance.getReachedStateDate().getTime();
        final long processStartDate = startProcess.getStartDate().getTime();
        assertTrue("The process start at " + processStartDate + ", and the user task " + humanTaskInstance.getName() + " reached state at " + readyDate,
                processStartDate <= readyDate);
        getProcessAPI().assignUserTask(humanTaskInstance.getId(), user.getId());
        getProcessAPI().executeFlowNode(humanTaskInstance.getId());
        // look in archive
        assertEquals(reachedDate, getProcessAPI().getActivityReachedStateDate(humanTaskInstance.getId(), TestStates.getReadyState()));
        disableAndDeleteProcess(processDefinition1);
        deleteUser(user);
    }

    @Test
    public void checkOrderPriorityEnum() {
        assertEquals(0, TaskPriority.LOWEST.ordinal());
        assertEquals(1, TaskPriority.UNDER_NORMAL.ordinal());
        assertEquals(2, TaskPriority.NORMAL.ordinal());
        assertEquals(3, TaskPriority.ABOVE_NORMAL.ordinal());
        assertEquals(4, TaskPriority.HIGHEST.ordinal());
    }

    @Test
    public void activityWithDisplayNameAndDescription() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final String displayName = "display name";
        final String displayDescriptionAfterCompletion = "display description after completion";
        final String displayDescription = "display description";
        final Expression dispDescAfterCompletionExpression = new ExpressionBuilder().createGroovyScriptExpression("dynGroovyScriptWithLongDep", "return '"
                + displayDescriptionAfterCompletion + "' + rootProcessInstanceId", String.class.getName(),
                new ExpressionBuilder().createEngineConstant(ExpressionConstants.ROOT_PROCESS_INSTANCE_ID));
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("step1", ACTOR_NAME)
                .addDisplayName(new ExpressionBuilder().createConstantStringExpression(displayName))
                .addDisplayDescriptionAfterCompletion(dispDescAfterCompletionExpression)
                .addDisplayDescription(new ExpressionBuilder().createConstantStringExpression(displayDescription)).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());

        final long activityInstanceId = waitForStep("step1", pi0).getStepId();
        final HumanTaskInstance userTaskInstance = getProcessAPI().getHumanTaskInstance(activityInstanceId);
        assertEquals(displayName, userTaskInstance.getDisplayName());
        assertEquals(displayDescription, userTaskInstance.getDisplayDescription());
        assignAndExecuteStep(userTaskInstance, user.getId());
        waitForCompletedArchivedStep("step1", processDefinition.getId(), displayName, displayDescriptionAfterCompletion + pi0.getId());

        // Clean up
        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    @Test
    public void activityWithDisplayNameAndDescriptionAndNoAfterCompletion() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final String displayName = "display name";
        final String displayDescription = "display description";
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("step1", ACTOR_NAME)
                .addDisplayName(new ExpressionBuilder().createConstantStringExpression(displayName))
                .addDisplayDescription(new ExpressionBuilder().createConstantStringExpression(displayDescription)).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1 = waitForUserTask("step1", pi0);
        assertNotNull(step1);

        final long activityInstanceId = step1.getId();
        final HumanTaskInstance userTaskInstance = getProcessAPI().getHumanTaskInstance(activityInstanceId);
        assertEquals(displayName, userTaskInstance.getDisplayName());
        assertEquals(displayDescription, userTaskInstance.getDisplayDescription());
        assignAndExecuteStep(userTaskInstance, user.getId());
        waitForCompletedArchivedStep("step1", processDefinition.getId(), displayName, displayDescription);

        // Clean up
        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    @Test
    public void activityWithNoDisplayNameAndDescription() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        final String stepName = "staticName";
        final String stepDescription = "staticDescription";
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask(stepName, ACTOR_NAME).addDescription(stepDescription).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());

        final long activityInstanceId = waitForStep(stepName, pi0).getStepId();
        final HumanTaskInstance userTaskInstance = getProcessAPI().getHumanTaskInstance(activityInstanceId);
        assertEquals(stepName, userTaskInstance.getDisplayName());
        assertEquals(stepDescription, userTaskInstance.getDisplayDescription());
        assertEquals(stepDescription, userTaskInstance.getDescription());
        assignAndExecuteStep(userTaskInstance, user.getId());
        waitForCompletedArchivedStep(stepName, processDefinition.getId(), stepName, stepDescription);

        // Clean up
        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    @Test
    public void getNumberOfOpenTasksForUsers() throws Exception {
        final String username1 = "jack";
        final String username2 = "john";
        final String username3 = "lucy";

        final User jack = createUser(username1, PASSWORD);
        final User john = createUser(username2, PASSWORD);
        final User lucy = createUser(username3, PASSWORD);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME).addDescription("Coding all scrum-sprint-long").addUserTask("userTask1", ACTOR_NAME)
                .addUserTask("userTask2", ACTOR_NAME)
                .addUserTask("userTask3", ACTOR_NAME);
        final DesignProcessDefinition processDefinition = processBuilder.done();

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinition, ACTOR_NAME, jack);

        final ProcessInstance startedProcess = getProcessAPI().startProcess(definition.getId());
        assertTrue("expected 2 activities",
                new CheckNbOfActivities(getProcessAPI(), 200, 5000, true, startedProcess, 3, TestStates.getReadyState()).waitUntil());
        // add lucy to actor
        getProcessAPI().addUserToActor(ACTOR_NAME, definition, lucy.getId());
        // assign first user task to jack, second one to john, leaving the third pending
        final List<ActivityInstance> activities = getProcessAPI().getActivities(startedProcess.getId(), 0, 10);
        getProcessAPI().assignUserTask(activities.get(0).getId(), jack.getId());
        getProcessAPI().assignUserTask(activities.get(1).getId(), john.getId());
        // check
        final List<Long> userIds = new ArrayList<Long>();
        userIds.add(jack.getId());
        userIds.add(john.getId());
        userIds.add(lucy.getId());
        final Map<Long, Long> myAssignedTasksNb = getProcessAPI().getNumberOfOpenTasks(userIds);
        assertNotNull(myAssignedTasksNb);
        assertEquals(3, myAssignedTasksNb.size());
        assertEquals(2L, (long) myAssignedTasksNb.get(jack.getId())); // jack has one assigned task and one pending task
        assertEquals(1L, (long) myAssignedTasksNb.get(john.getId())); // john has one assigned task
        assertEquals(1L, (long) myAssignedTasksNb.get(lucy.getId())); // lucy has one pending task

        disableAndDeleteProcess(definition);
        deleteUser(jack);
        deleteUser(john);
        deleteUser(lucy);
    }

    @Test
    public void getNumberOfOverdueTasksForUsers() throws Exception {
        final String username1 = "jack";
        final String username2 = "john";
        final String username3 = "lucy";

        final User jack = createUser(username1, PASSWORD);
        final User john = createUser(username2, PASSWORD);
        final User lucy = createUser(username3, PASSWORD);
        // default expectedDuration is null for HumanTaskDefinition, so the expectedEndDate is 0 by default, no need to set it in particular.
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME).addDescription("Coding all scrum-sprint-long").addUserTask("userTask1", ACTOR_NAME)
                .addUserTask("userTask2", ACTOR_NAME)
                .addUserTask("userTask3", ACTOR_NAME);
        final DesignProcessDefinition processDefinition = processBuilder.done();

        final ProcessDefinition definition = deployAndEnableProcessWithActor(processDefinition, ACTOR_NAME, jack);

        final ProcessInstance startedProcess = getProcessAPI().startProcess(definition.getId());
        assertTrue("expected 2 activities",
                new CheckNbOfActivities(getProcessAPI(), 200, 5000, true, startedProcess, 3, TestStates.getReadyState()).waitUntil());
        // add lucy to actor
        getProcessAPI().addUserToActor(ACTOR_NAME, definition, lucy.getId());
        // assign first user task to jack, second one to john, leaving the third pending
        final List<ActivityInstance> activities = getProcessAPI().getActivities(startedProcess.getId(), 0, 10);
        getProcessAPI().assignUserTask(activities.get(0).getId(), jack.getId());
        getProcessAPI().assignUserTask(activities.get(1).getId(), john.getId());
        // check
        final List<Long> userIds = new ArrayList<Long>();
        userIds.add(jack.getId());
        userIds.add(john.getId());
        userIds.add(lucy.getId());
        final Map<Long, Long> myAssignedTasksNb = getProcessAPI().getNumberOfOverdueOpenTasks(userIds);
        assertNotNull(myAssignedTasksNb);
        assertEquals(3, myAssignedTasksNb.size());
        assertEquals(2L, (long) myAssignedTasksNb.get(jack.getId())); // jack has one assigned overdue task and one pending overdue task
        assertEquals(1L, (long) myAssignedTasksNb.get(john.getId())); // john has one assigned overdue task
        assertEquals(1L, (long) myAssignedTasksNb.get(lucy.getId())); // lucy has one pending overdue task

        disableAndDeleteProcess(definition);
        deleteUser(jack);
        deleteUser(john);
        deleteUser(lucy);
    }

    @Test
    public void getProcessDefinitionsDeployInfo() throws Exception {
        // create process1
        final String PROCESS_NAME1 = "processDefinition1";
        final DesignProcessDefinition designProcessDefinition1 = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME1, "1.1")
                .addDescription("My process 1").addDisplayName("Process 1").addDisplayDescription("The process definition that is cool").done();
        final ProcessDefinition processDefinition1 = deployAndEnableProcess(designProcessDefinition1);

        assertNotNull(processDefinition1);
        assertEquals(PROCESS_NAME1, processDefinition1.getName());
        assertEquals("1.1", processDefinition1.getVersion());
        assertEquals("My process 1", processDefinition1.getDescription());
        // put all processDefinitionId to a list as parameter
        final long processDefinitionIdA = processDefinition1.getId();
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinitionIdA);
        assertNotNull(processDeploymentInfo);
        assertEquals(PROCESS_NAME1, processDeploymentInfo.getName());
        assertEquals("1.1", processDeploymentInfo.getVersion());
        assertEquals("My process 1", processDeploymentInfo.getDescription());
        assertEquals("The process definition that is cool", processDeploymentInfo.getDisplayDescription());
        assertEquals("Process 1", processDeploymentInfo.getDisplayName());
        disableAndDeleteProcess(processDefinition1);
    }

    @Test
    public void getProcessDefinitionsFromIds() throws Exception {
        // create user
        final User john = createUser(USERNAME, PASSWORD);
        loginOnDefaultTenantWith(USERNAME, PASSWORD);

        // create process1
        final String PROCESS_NAME1 = "processDefinition1";
        final DesignProcessDefinition designProcessDefinition1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(PROCESS_NAME1, "1.1",
                Arrays.asList("step1_1", "step1_2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition1 = deployAndEnableProcessWithActor(designProcessDefinition1, ACTOR_NAME, john);

        // create process2
        final String PROCESS_NAME2 = "processDefinition2";
        final DesignProcessDefinition designProcessDefinition2 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(PROCESS_NAME2, "1.2",
                Arrays.asList("step2_1", "step2_2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition2 = deployAndEnableProcessWithActor(designProcessDefinition2, ACTOR_NAME, john);

        // create process3
        final String PROCESS_NAME3 = "processDefinition3";
        final DesignProcessDefinition designProcessDefinition3 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(PROCESS_NAME3, "1.3",
                Arrays.asList("step2_1", "step2_2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition3 = deployAndEnableProcessWithActor(designProcessDefinition3, ACTOR_NAME, john);

        // create process4
        final String PROCESS_NAME4 = "processDefinition4";
        final DesignProcessDefinition designProcessDefinition4 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(PROCESS_NAME4, "1.4",
                Arrays.asList("step2_1", "step2_2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition4 = deployAndEnableProcessWithActor(designProcessDefinition4, ACTOR_NAME, john);

        // put all processDefinitionId to a list as parameter
        final long processDefinitionIdA = processDefinition1.getId();
        final long processDefinitionIdB = processDefinition2.getId();
        final long processDefinitionIdC = processDefinition3.getId();
        final long processDefinitionIdD = processDefinition4.getId();
        final List<Long> processDefinitionIds = new ArrayList<Long>();
        processDefinitionIds.add(processDefinitionIdA);
        processDefinitionIds.add(processDefinitionIdB);
        processDefinitionIds.add(processDefinitionIdC);
        processDefinitionIds.add(processDefinitionIdD);

        // do search and assert
        final Map<Long, ProcessDeploymentInfo> processDeploymentInfos = getProcessAPI().getProcessDeploymentInfosFromIds(processDefinitionIds);
        assertNotNull(processDeploymentInfos);
        assertEquals(4, processDeploymentInfos.size());
        assertEquals(processDefinition1.getId(), processDeploymentInfos.get(processDefinitionIdA).getProcessId());
        assertEquals(processDefinition2.getId(), processDeploymentInfos.get(processDefinitionIdB).getProcessId());
        assertEquals(processDefinition3.getId(), processDeploymentInfos.get(processDefinitionIdC).getProcessId());
        assertEquals(processDefinition4.getId(), processDeploymentInfos.get(processDefinitionIdD).getProcessId());

        // delete data for test
        disableAndDeleteProcess(processDefinition1);
        disableAndDeleteProcess(processDefinition2);
        disableAndDeleteProcess(processDefinition3);
        disableAndDeleteProcess(processDefinition4);
        deleteUser(john);
    }

    @Test
    public void getProcessDefinitionsFromProcessInstanceIds() throws Exception {
        // create user
        final User john = createUser(USERNAME, PASSWORD);
        loginOnDefaultTenantWith(USERNAME, PASSWORD);

        // create process1
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addDescription("description");
        processBuilder.addDisplayDescription("displayDescription");
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition1 = processBuilder.addUserTask("step1", ACTOR_NAME).getProcess();

        final ProcessDefinition processDefinition1 = deployAndEnableProcessWithActor(designProcessDefinition1, ACTOR_NAME, john);
        final ProcessInstance pi1 = getProcessAPI().startProcess(processDefinition1.getId());

        // create process2
        final String processName2 = "processDefinition2";
        final DesignProcessDefinition designProcessDefinition2 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(processName2, "1.2",
                Arrays.asList("step2_1", "step2_2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition2 = deployAndEnableProcessWithActor(designProcessDefinition2, ACTOR_NAME, john);
        final ProcessInstance pi2 = getProcessAPI().startProcess(processDefinition2.getId());

        // create process3
        final String processName3 = "processDefinition3";
        final DesignProcessDefinition designProcessDefinition3 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(processName3, "1.3",
                Arrays.asList("step3_1", "step3_2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition3 = deployAndEnableProcessWithActor(designProcessDefinition3, ACTOR_NAME, john);
        final ProcessInstance pi3 = getProcessAPI().startProcess(processDefinition3.getId());

        // create process4
        final String processName4 = "processDefinition4";
        final DesignProcessDefinition designProcessDefinition4 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(processName4, "1.4",
                Arrays.asList("step4_1", "step4_2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition4 = deployAndEnableProcessWithActor(designProcessDefinition4, ACTOR_NAME, john);
        final ProcessInstance pi4 = getProcessAPI().startProcess(processDefinition4.getId());

        // put all processInstantsId to a list as parameter
        final long processInstantsIdA = pi1.getId();
        final long processInstantsIdB = pi2.getId();
        final long processInstantsIdC = pi3.getId();
        final long processInstantsIdD = pi4.getId();
        final List<Long> processInstantsIds = new ArrayList<Long>();
        processInstantsIds.add(processInstantsIdA);
        processInstantsIds.add(processInstantsIdB);
        processInstantsIds.add(processInstantsIdC);
        processInstantsIds.add(processInstantsIdD);

        // do search and assert
        final Map<Long, ProcessDeploymentInfo> processDeploymentInfos = getProcessAPI().getProcessDeploymentInfosFromProcessInstanceIds(processInstantsIds);
        assertNotNull(processDeploymentInfos);
        assertEquals(4, processDeploymentInfos.size());
        assertEquals(processDefinition1.getId(), processDeploymentInfos.get(processInstantsIdA).getProcessId());
        assertEquals(processDefinition2.getId(), processDeploymentInfos.get(processInstantsIdB).getProcessId());
        assertEquals(processDefinition3.getId(), processDeploymentInfos.get(processInstantsIdC).getProcessId());
        assertEquals(processDefinition4.getId(), processDeploymentInfos.get(processInstantsIdD).getProcessId());

        // delete data for test
        disableAndDeleteProcess(processDefinition1, processDefinition2, processDefinition3, processDefinition4);
        deleteUser(john.getId());
    }

    @Test
    public void retryTask() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("step1", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        waitForStep("step1", pi0);

        final List<ActivityInstance> activityInstances = getProcessAPI().getActivities(pi0.getId(), 0, 10);
        final long activityInstanceId = activityInstances.get(0).getId();
        getProcessAPI().assignUserTask(activityInstanceId, user.getId());
        HumanTaskInstance userTaskInstance = getProcessAPI().getHumanTaskInstance(activityInstanceId);
        assertEquals(ActivityStates.READY_STATE, userTaskInstance.getState());
        getProcessAPI().setActivityStateById(activityInstanceId, 3);
        userTaskInstance = getProcessAPI().getHumanTaskInstance(activityInstanceId);
        assertEquals(ActivityStates.FAILED_STATE, userTaskInstance.getState());
        getProcessAPI().retryTask(activityInstanceId);
        waitForProcessToFinish(pi0);
        disableAndDeleteProcess(processDefinition);
        deleteUser(user.getId());
    }

    @Test(expected = NotFoundException.class)
    public void getArchiveCommentNotFound() throws Exception {
        getProcessAPI().getArchivedComment(123456789l);
    }

    @Test
    public void getProcessDeploymentInfosFromArchivedProcessInstanceIds() throws Exception {
        final DesignProcessDefinition designProcessDefinition1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1_1"),
                Arrays.asList(false));
        final ProcessDefinition processDefinition1 = deployAndEnableProcess(designProcessDefinition1);

        final ProcessInstance pi1 = getProcessAPI().startProcess(processDefinition1.getId());
        checkProcessInstanceIsArchived(pi1);

        // get archived process instances. It will have only the state completed.
        final List<ArchivedProcessInstance> aProcessInstances = getProcessAPI().getArchivedProcessInstances(0, 10, ProcessInstanceCriterion.ARCHIVE_DATE_DESC);
        assertEquals(1, aProcessInstances.size());
        //
        final ArchivedProcessInstance archivedProcessInstance1 = aProcessInstances.get(0);

        // We check that the retrieved process are the good one :
        assertEquals(pi1.getId(), archivedProcessInstance1.getSourceObjectId());

        // put processInstantsId to a list as parameter
        final List<Long> archivedProcessInstantsIds = new ArrayList<Long>();
        archivedProcessInstantsIds.add(archivedProcessInstance1.getId());

        // do search and assert
        final Map<Long, ProcessDeploymentInfo> processDeploymentInfos = getProcessAPI().getProcessDeploymentInfosFromArchivedProcessInstanceIds(
                archivedProcessInstantsIds);
        assertNotNull(processDeploymentInfos);
        assertEquals(1, processDeploymentInfos.size());
        assertEquals(processDefinition1.getId(), processDeploymentInfos.get(archivedProcessInstance1.getId()).getProcessId());

        // delete data for test
        disableAndDeleteProcess(processDefinition1);
    }

    @Test
    public void cantResolveDataInExpressionInDataDefaultValue() throws Exception {
        final User jack = createUserAndLogin(USERNAME, USERNAME);

        final Expression aExpression = new ExpressionBuilder().createDataExpression("name", String.class.getName());
        final Expression aScript = new ExpressionBuilder().createGroovyScriptExpression("script", "return name", String.class.getName(), aExpression);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("cantResolveDataInExpressionInDataDefaultValue", "1");
        processBuilder.addActor(ACTOR_NAME).addDescription("Process to test archiving mechanism");

        processBuilder.addShortTextData("name", new ExpressionBuilder().createConstantStringExpression("a value"));

        final AutomaticTaskDefinitionBuilder automaticTaskDefinitionBuilder = processBuilder.addAutomaticTask("activity");
        automaticTaskDefinitionBuilder.addShortTextData("taskData", aExpression);
        automaticTaskDefinitionBuilder.addShortTextData("taskDataFromString", aScript);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, jack);

        final ProcessInstance processInstance = getProcessAPI().startProcess(jack.getId(), processDefinition.getId());
        try {
            waitForProcessToFinish(processInstance, TestStates.getNormalFinalState());
        } catch (final Exception e) {
            fail("Process should finish");
        }
        disableAndDeleteProcess(processDefinition);
        deleteUser(jack.getId());
    }

    @Test
    public void errorMessageWhileStartingProcessForClassCastToInteger() throws Exception {
        final User jack = createUserAndLogin(USERNAME, USERNAME);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("cantResolveDataInExpressionInDataDefaultValue", "1");
        processBuilder.addActor(ACTOR_NAME).addDescription("Process to test archiving mechanism");
        processBuilder.addIntegerData("aData", null);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, jack);

        final Operation stringOperation = BuildTestUtil.buildStringOperation("aData", "15", false);
        final Map<String, Serializable> context = new HashMap<String, Serializable>();
        context.put("page", "1");

        try {
            getProcessAPI().startProcess(processDefinition.getId(), Arrays.asList(stringOperation), context);
        } catch (final ExecutionException e) {
            assertThat(e.getMessage(), CoreMatchers.containsString("aData"));
            assertThat(e.getMessage(), CoreMatchers.containsString("incompatible type"));
        }

        disableAndDeleteProcess(processDefinition);
        deleteUser(jack.getId());
    }

    @Test
    public void errorMessageWhileStartingProcessForClassCastToLong() throws Exception {
        final User jack = createUserAndLogin(USERNAME, USERNAME);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("cantResolveDataInExpressionInDataDefaultValue", "1");
        processBuilder.addActor(ACTOR_NAME).addDescription("Process to test archiving mechanism");
        processBuilder.addLongData("aData", null);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, jack);

        final Operation stringOperation = BuildTestUtil.buildStringOperation("aData", "15", false);
        final Map<String, Serializable> context = new HashMap<String, Serializable>();
        context.put("page", "1");

        try {
            getProcessAPI().startProcess(processDefinition.getId(), Arrays.asList(stringOperation), context);
        } catch (final ExecutionException e) {
            assertThat(e.getMessage(), CoreMatchers.containsString("aData"));
            assertThat(e.getMessage(), CoreMatchers.containsString("incompatible type"));
        }

        disableAndDeleteProcess(processDefinition);
        deleteUser(jack.getId());
    }

    @Test
    public void errorMessageWhileStartingProcessForClassCastToDouble() throws Exception {
        final User jack = createUserAndLogin(USERNAME, USERNAME);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("cantResolveDataInExpressionInDataDefaultValue", "1");
        processBuilder.addActor(ACTOR_NAME).addDescription("Process to test archiving mechanism");
        processBuilder.addDoubleData("aData", null);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, jack);

        final Operation stringOperation = BuildTestUtil.buildStringOperation("aData", "15", false);
        final ArrayList<Operation> operations = new ArrayList<Operation>(1);
        operations.add(stringOperation);
        final Map<String, Serializable> context = new HashMap<String, Serializable>();
        context.put("page", "1");

        try {
            getProcessAPI().startProcess(processDefinition.getId(), operations, context);
        } catch (final ExecutionException e) {
            assertThat(e.getMessage(), CoreMatchers.containsString("aData"));
            assertThat(e.getMessage(), CoreMatchers.containsString("incompatible type"));
        }

        disableAndDeleteProcess(processDefinition);
        deleteUser(jack.getId());
    }

    private List<Long> createProcessDefinitionWithTwoHumanStepsAndDeployBusinessArchive(final int nbProcess) throws InvalidProcessDefinitionException,
            ProcessDeployException, InvalidBusinessArchiveFormatException, AlreadyExistsException {
        final List<Long> ids = new ArrayList<Long>();
        for (int i = 0; i < nbProcess; i++) {
            String processName = PROCESS_NAME;
            if (i >= 0 && i < 10) {
                processName += "0";
            }
            final DesignProcessDefinition processDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(processName + i,
                    PROCESS_VERSION
                            + i, Arrays.asList("step1", "step2"), Arrays.asList(true, true));
            ids.add(getProcessAPI().deploy(new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinition).done()).getId());
        }
        return ids;
    }

    private DesignProcessDefinition createProcessWithActorAndHumanTaskAndStringData() throws Exception {
        return new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION).addActor(ACTOR_NAME)
                .addDescription("Delivery all day and night long").addUserTask("step1", ACTOR_NAME)
                .addShortTextData("dataName", new ExpressionBuilder().createConstantStringExpression("beforeUpdate")).getProcess();
    }

    @Cover(jira = "ENGINE-1601", classes = { DataInstance.class, ProcessInstance.class }, concept = BPMNConcept.DATA, keywords = { "initilize process data" })
    @Test
    public void startProcessUsingInitialVariableValues() throws Exception {
        final User jack = createUserAndLogin(USERNAME, USERNAME);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("cantResolveDataInExpressionInDataDefaultValue", "1");
        processBuilder.addActor(ACTOR_NAME).addDescription("Process to test archiving mechanism");
        processBuilder.addDoubleData("D", new ExpressionBuilder().createConstantDoubleExpression(3.14));
        processBuilder.addData("bigD", BigDecimal.class.getName(), null);
        processBuilder.addUserTask("step1", ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, jack);

        final Map<String, Serializable> variables = new HashMap<String, Serializable>();
        variables.put("bigD", new BigDecimal("3.141592653589793"));
        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId(), variables);

        DataInstance dataInstance = getProcessAPI().getProcessDataInstance("bigD", instance.getId());
        assertEquals(new BigDecimal("3.141592653589793"), dataInstance.getValue());
        dataInstance = getProcessAPI().getProcessDataInstance("D", instance.getId());
        assertEquals(Double.valueOf(3.14), dataInstance.getValue());

        disableAndDeleteProcess(processDefinition);
        deleteUser(jack.getId());
    }

}
