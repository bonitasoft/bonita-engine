/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.core.process.instance.model;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.commons.Pair.pair;
import static org.bonitasoft.engine.core.process.definition.model.SGatewayType.PARALLEL;
import static org.bonitasoft.engine.test.persistence.builder.ActorBuilder.anActor;
import static org.bonitasoft.engine.test.persistence.builder.ActorMemberBuilder.anActorMember;
import static org.bonitasoft.engine.test.persistence.builder.BoundaryInstanceBuilder.aBoundary;
import static org.bonitasoft.engine.test.persistence.builder.GatewayInstanceBuilder.aGatewayInstanceBuilder;
import static org.bonitasoft.engine.test.persistence.builder.LoopActivityInstanceBuilder.aLoopActivity;
import static org.bonitasoft.engine.test.persistence.builder.PendingActivityMappingBuilder.aPendingActivityMapping;
import static org.bonitasoft.engine.test.persistence.builder.ProcessInstanceBuilder.aProcessInstance;
import static org.bonitasoft.engine.test.persistence.builder.UserBuilder.aUser;
import static org.bonitasoft.engine.test.persistence.builder.UserMembershipBuilder.aUserMembership;
import static org.bonitasoft.engine.test.persistence.builder.UserTaskInstanceBuilder.aUserTask;
import static org.bonitasoft.engine.test.persistence.builder.archive.ArchivedUserTaskInstanceBuilder.anArchivedUserTask;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.instance.model.archive.SAFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SEndEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SIntermediateCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SIntermediateThrowEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SStartEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.PersistentObjectId;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.test.persistence.builder.PersistentObjectBuilder;
import org.bonitasoft.engine.test.persistence.repository.FlowNodeInstanceRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class FlowNodeInstanceTest {

    private static final long GROUP_FOR_BOB_ID = 74L;

    private static final long GROUP_FOR_JOHN_ID = 9875L;

    private static final long JOHN_ID = 654L;

    private static final long BOB_ID = 697L;

    private static final long PROCESS_DEFINITION_ID = 111L;

    private static final long ROOT_PROCESS_INSTANCE_ID = 963L;

    private static final long NORMAL_HUMAN_INSTANCE_ID = 743L;

    @Inject
    private FlowNodeInstanceRepository repository;

    @Inject
    private JdbcTemplate jdbcTemplate;

    @Before
    public void before() {
        repository.add(aUser().withId(JOHN_ID).build());
        repository.add(aUser().withId(BOB_ID).build());

        // Create process
        final String processNameSupervisedByJohn = "Process definition";
        buildAndCreateProcessDefinition(6L, PROCESS_DEFINITION_ID, processNameSupervisedByJohn);
        repository.add(
                aProcessInstance().withProcessDefinitionId(PROCESS_DEFINITION_ID).withName(processNameSupervisedByJohn)
                        .withId(ROOT_PROCESS_INSTANCE_ID).build());
    }

    protected void buildAndCreateProcessDefinition(final long id, final long processDefinitionId,
            final String processName) {
        final SProcessDefinitionDeployInfo sProcessDefinitionDeployInfo = new SProcessDefinitionDeployInfo();
        sProcessDefinitionDeployInfo.setId(id);
        sProcessDefinitionDeployInfo.setName(processName);
        sProcessDefinitionDeployInfo.setVersion("version");
        sProcessDefinitionDeployInfo.setProcessId(processDefinitionId);
        sProcessDefinitionDeployInfo.setTenantId(1L);
        repository.add(sProcessDefinitionDeployInfo);
    }

    @Test
    public void getFlowNodeInstanceIdsToRecover_should_return_ids_of_flow_nodes_that_needs_to_be_recovered() {
        // given
        repository
                .add(aUserTask().withName("normalTask1").withStateExecuting(false).withStable(true).withTerminal(false)
                        .build());
        final SFlowNodeInstance executing = repository
                .add(aUserTask().withName("executingTask").withStateExecuting(true).withStable(true).withTerminal(false)
                        .build());
        final SFlowNodeInstance notStable = repository.add(
                aUserTask().withName("notStableTask").withStateExecuting(false).withStable(false).withTerminal(true)
                        .build());
        final SFlowNodeInstance terminal = repository
                .add(aUserTask().withName("terminalTask").withStateExecuting(false).withStable(true).withTerminal(true)
                        .build());
        repository.add(aUserTask().withName("failedTask").withStateExecuting(true).withStable(true).withTerminal(true)
                .withStateId(3).build());
        repository.add(aUserTask().withName("normalTask2").withStateExecuting(false).withStable(true)
                .withTerminal(false).build());
        repository.add(aGatewayInstanceBuilder().withName("gateway_completed").withStateId(2).withStable(true)
                .withTerminal(true).withHitBys("FINISH:12").build());
        repository.add(aGatewayInstanceBuilder().withName("gateway_initializing_but_not_finished1").withStateId(61)
                .withStable(false).withHitBys("1").build());

        repository.add(aBoundary().withName("abortingBoundary").withActivity(terminal.getId()).withStateId(10)
                .withStateExecuting(false).withStable(true)
                .withTerminal(false).withStateName("WAITING")
                .withStateCategory(SStateCategory.ABORTING).build());
        repository.add(aBoundary().withName("cancellingBoundary").withActivity(terminal.getId()).withStateId(10)
                .withStateExecuting(false).withStable(true)
                .withTerminal(false).withStateName("WAITING")
                .withStateCategory(SStateCategory.CANCELLING).build());
        repository.add(aBoundary().withName("errorBoundary").withActivity(terminal.getId()).withStateId(10)
                .withStateExecuting(false).withStable(true)
                .withTerminal(false).withStateName("WAITING")
                .withStateCategory(SStateCategory.NORMAL).build());

        // when
        final QueryOptions options = new QueryOptions(0, 10);
        final List<Long> nodeToRestart = repository.getFlowNodeInstanceIdsToRecover(Duration.ZERO, options);

        // then
        assertThat(nodeToRestart.stream()
                .map(id -> repository.getSession()
                        .get(SFlowNodeInstance.class,
                                new PersistentObjectId(id, PersistentObjectBuilder.DEFAULT_TENANT_ID))
                        .getName()))
                                .containsOnly("executingTask", "notStableTask", "terminalTask",
                                        "abortingBoundary", "cancellingBoundary");
    }

    @Test
    public void getGatewayInstanceIdsToRecover_should_return_ids_of_gateways_flagged_as_FINISH() {
        // given
        repository.add(aGatewayInstanceBuilder().withName("gateway_initializing_but_not_finished1").withStateId(61)
                .withStable(false).withHitBys("1").build());
        repository.add(aGatewayInstanceBuilder().withName("gateway_initializing_but_not_finished2").withStateId(61)
                .withStable(false).withHitBys("1,2").build());
        repository.add(aGatewayInstanceBuilder().withName("gateway_initializing_but_finished").withStateId(61)
                .withStable(false).withHitBys("FINISH:12").build());
        repository.add(aGatewayInstanceBuilder().withName("gateway_failed").withStateExecuting(true).withStable(true)
                .withTerminal(true).withStateId(3).build());
        repository.add(aGatewayInstanceBuilder().withName("gateway_completed").withStateId(2).withStable(true)
                .withTerminal(true).withHitBys("FINISH:12").build());
        repository.add(aGatewayInstanceBuilder().withName("gateway_aborting").withStateId(61).withStable(true)
                .withHitBys("1,2").withStateCategory(SStateCategory.ABORTING).build());
        repository.add(aGatewayInstanceBuilder().withName("gateway_cancelling").withStateId(61).withStable(true)
                .withHitBys("1,2").withStateCategory(SStateCategory.ABORTING).build());

        repository.flush();
        // when
        final QueryOptions options = new QueryOptions(0, 10);
        final List<Long> nodeToRestart = repository.getGatewayInstanceIdsToRecover(Duration.ZERO, options);

        // then
        assertThat(nodeToRestart.stream()
                .map(id -> (repository.getSession().get(SFlowNodeInstance.class,
                        new PersistentObjectId(id, PersistentObjectBuilder.DEFAULT_TENANT_ID))).getName()))
                                .containsOnly(
                                        "gateway_initializing_but_finished",
                                        "gateway_completed",
                                        "gateway_aborting",
                                        "gateway_cancelling");
    }

    @Test
    public void getGatewayInstanceIdsToRecover_should_return_ids_of_gateways_flagged_as_FINISH_and_older_than_given_duration() {
        // given
        repository.add(aGatewayInstanceBuilder().withName("gateway_initializing_but_finished")
                .withLastUpdateDate(now().minusSeconds(200).toEpochMilli()).withStateId(61).withStable(false)
                .withHitBys("FINISH:12").build());
        repository.add(aGatewayInstanceBuilder().withName("gateway_completed").withStateId(2).withStable(true)
                .withLastUpdateDate(now().minusSeconds(600).toEpochMilli()).withTerminal(true).withHitBys("FINISH:12")
                .build());
        repository.add(aGatewayInstanceBuilder().withName("gateway_aborting")
                .withLastUpdateDate(now().minus(2, DAYS).toEpochMilli()).withStateId(61).withStable(true)
                .withHitBys("1,2").withStateCategory(SStateCategory.ABORTING).build());

        repository.flush();
        // when
        final QueryOptions options = new QueryOptions(0, 10);
        final List<Long> nodeToRestart = repository.getGatewayInstanceIdsToRecover(Duration.ofSeconds(500), options);

        // then
        assertThat(nodeToRestart.stream()
                .map(id -> (repository.getSession().get(SFlowNodeInstance.class,
                        new PersistentObjectId(id, PersistentObjectBuilder.DEFAULT_TENANT_ID))).getName()))
                                .containsOnly(
                                        "gateway_completed",
                                        "gateway_aborting");
    }

    @Test
    public void getFlowNodeInstanceIdsToRecover_should_return_only_element_older_than_given_duration() {
        // given
        long now = System.currentTimeMillis();

        SFlowNodeInstance oldTask1 = repository
                .add(aUserTask().withName("oldTask1").withTerminal(true)
                        .withLastUpdateDate(now().minus(2, DAYS).toEpochMilli()).build());
        SFlowNodeInstance oldTask2 = repository
                .add(aUserTask().withName("oldTask2").withTerminal(true)
                        .withLastUpdateDate(now().minusSeconds(600).toEpochMilli()).build());

        repository
                .add(aUserTask().withName("recentTask1").withTerminal(true)
                        .withLastUpdateDate(now().minusSeconds(400).toEpochMilli()).build());
        repository.add(aUserTask().withName("recentTask2").withTerminal(true).withLastUpdateDate(now).build());
        // when
        List<Long> nodeToRestart = repository.getFlowNodeInstanceIdsToRecover(Duration.ofSeconds(500),
                new QueryOptions(0, 10));

        // then
        assertThat(nodeToRestart).containsOnly(oldTask1.getId(), oldTask2.getId());
    }

    // For
    @Test
    public void getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcessFor_should_return_number_of_process_definition_if_one_instance_has_assigned_tasks_to_the_user() {
        // Given
        buildAndAddAssignedTasks();

        // When
        final long numberOfHumanTaskInstances = repository
                .getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcessFor(PROCESS_DEFINITION_ID,
                        JOHN_ID);
        // Then
        assertThat(numberOfHumanTaskInstances).isEqualTo(1);
    }

    @Test
    public void getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcessFor_should_return_number_of_process_definition_if_one_instance_has_pending_tasks_for_the_user() {
        // Given
        buildAndAddTasksWithPendingMappingForUser();

        // When
        final long numberOfHumanTaskInstances = repository
                .getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcessFor(PROCESS_DEFINITION_ID,
                        JOHN_ID);

        // Then
        assertThat(numberOfHumanTaskInstances).isEqualTo(1);
    }

    @Test
    public void getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcessFor_should_return_number_of_process_definition_if_one_instance_has_pending_tasks_for_a_actor_with_the_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUser();

        // When
        final long numberOfHumanTaskInstances = repository
                .getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcessFor(PROCESS_DEFINITION_ID,
                        JOHN_ID);

        // Then
        assertThat(numberOfHumanTaskInstances).isEqualTo(1);
    }

    @Test
    public void getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcessFor_should_return_number_of_process_definition_if_one_instance_has_pending_tasks_for_a_actor_with_a_membership_mapped_to_the_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUserMembershipMappedToUser();

        // When
        final long numberOfHumanTaskInstances = repository
                .getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcessFor(PROCESS_DEFINITION_ID,
                        JOHN_ID);

        // Then
        assertThat(numberOfHumanTaskInstances).isEqualTo(1);
    }

    @Test
    public void searchSHumanTaskInstanceAssignedAndPendingByRootProcessFor_should_return_number_of_process_definition_if_one_instance_has_assigned_tasks_to_the_user() {
        // Given
        buildAndAddAssignedTasks();

        // When
        final List<SHumanTaskInstance> sHumanTaskInstances = repository
                .searchSHumanTaskInstanceAssignedAndPendingByRootProcessFor(PROCESS_DEFINITION_ID,
                        JOHN_ID);

        // Then
        assertThat(sHumanTaskInstances).hasSize(1);
        assertThat(sHumanTaskInstances.get(0).getId()).isEqualTo(NORMAL_HUMAN_INSTANCE_ID);
    }

    @Test
    public void searchSHumanTaskInstanceAssignedAndPendingByRootProcessFor_should_return_number_of_process_definition_if_one_instance_has_pending_tasks_for_the_user() {
        // Given
        buildAndAddTasksWithPendingMappingForUser();

        // When
        final List<SHumanTaskInstance> sHumanTaskInstances = repository
                .searchSHumanTaskInstanceAssignedAndPendingByRootProcessFor(PROCESS_DEFINITION_ID,
                        JOHN_ID);

        // Then
        assertThat(sHumanTaskInstances).hasSize(1);
        assertThat(sHumanTaskInstances.get(0).getId()).isEqualTo(NORMAL_HUMAN_INSTANCE_ID);
    }

    @Test
    public void searchSHumanTaskInstanceAssignedAndPendingByRootProcessFor_should_return_number_of_process_definition_if_one_instance_has_pending_tasks_for_a_actor_with_the_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUser();

        // When
        final List<SHumanTaskInstance> sHumanTaskInstances = repository
                .searchSHumanTaskInstanceAssignedAndPendingByRootProcessFor(PROCESS_DEFINITION_ID,
                        JOHN_ID);

        // Then
        assertThat(sHumanTaskInstances).hasSize(1);
        assertThat(sHumanTaskInstances.get(0).getId()).isEqualTo(NORMAL_HUMAN_INSTANCE_ID);
    }

    @Test
    public void searchSHumanTaskInstanceAssignedAndPendingByRootProcessFor_should_return_number_of_process_definition_if_one_instance_has_pending_tasks_for_a_actor_with_a_membership_mapped_to_the_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUserMembershipMappedToUser();

        // When
        final List<SHumanTaskInstance> sHumanTaskInstances = repository
                .searchSHumanTaskInstanceAssignedAndPendingByRootProcessFor(PROCESS_DEFINITION_ID,
                        JOHN_ID);

        // Then
        assertThat(sHumanTaskInstances).hasSize(1);
        assertThat(sHumanTaskInstances.get(0).getId()).isEqualTo(NORMAL_HUMAN_INSTANCE_ID);
    }

    // All
    @Test
    public void getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasks_should_return_number_of_process_definition_supervised_if_one_instance_has_assigned_tasks() {
        // Given
        buildAndAddAssignedTasks();

        // When
        final long numberOfHumanTaskInstances = repository
                .getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcess(PROCESS_DEFINITION_ID);

        // Then
        assertThat(numberOfHumanTaskInstances).isEqualTo(2);
    }

    @Test
    public void getNumberOfFlowNodesInAllStates_should_return_results_aggregated_by_name_and_state() {
        buildAndAddUserTaskWithParentAndRootProcessInstanceId("step1", 147L, 0L, 28, "executing");
        buildAndAddUserTaskWithParentAndRootProcessInstanceId("step1", 147L, 0L, 28, "executing");
        buildAndAddUserTaskWithParentAndRootProcessInstanceId("step1", 147L, 0L, 3, "failed");
        buildAndAddUserTaskWithParentAndRootProcessInstanceId("step1", 0L, 147L, 29, "someflownodeStateName"); // should not be selected because it is a flownode in a sub-process
        buildAndAddUserTaskWithParentAndRootProcessInstanceId("step2", 147L, 0L, 3, "failed");
        buildAndAddUserTaskWithParentAndRootProcessInstanceId("step1", 147L, 0L, 4, "ready");

        List<SFlowNodeInstanceStateCounter> counters = repository.getNumberOfFlowNodesInAllStates(147L);
        assertThat(counters).hasSize(4);

        assertThat(counters.get(0).getFlownodeName()).isEqualTo("step1");
        assertThat(counters.get(0).getStateName()).isEqualTo("executing");
        assertThat(counters.get(0).getNumberOf()).isEqualTo(2L);

        assertThat(counters.get(1).getFlownodeName()).isEqualTo("step1");
        assertThat(counters.get(1).getStateName()).isEqualTo("failed");
        assertThat(counters.get(1).getNumberOf()).isEqualTo(1L);

        assertThat(counters.get(2).getFlownodeName()).isEqualTo("step1");
        assertThat(counters.get(2).getStateName()).isEqualTo("ready");
        assertThat(counters.get(2).getNumberOf()).isEqualTo(1L);

        assertThat(counters.get(3).getFlownodeName()).isEqualTo("step2");
        assertThat(counters.get(3).getStateName()).isEqualTo("failed");
        assertThat(counters.get(3).getNumberOf()).isEqualTo(1L);

    }

    private SFlowNodeInstance buildAndAddUserTaskWithParentAndRootProcessInstanceId(final String taskName,
            final long containingProcessInstanceId,
            final long rootProcessInstanceId, int stateId, String stateName) {
        return repository
                .add(aUserTask().withName(taskName).withStateExecuting(false).withStable(true).withTerminal(false)
                        .withLogicalGroup4(containingProcessInstanceId).withLogicalGroup2(rootProcessInstanceId)
                        .withStateId(stateId).withStateName(stateName).build());
    }

    @Test
    public void getNumberOfArchivedFlowNodesInAllStates_should_return_results_aggregated_by_name_and_state() {
        buildAndAddArchivedUserTaskWithParentAndRootProcessInstanceId("step1", 147L, 0L, 2, "completed", true);
        buildAndAddArchivedUserTaskWithParentAndRootProcessInstanceId("step1", 147L, 0L, 2, "completed", true);
        buildAndAddArchivedUserTaskWithParentAndRootProcessInstanceId("step1", 147L, 0L, 2, "completed", false); // should not be selected because non-terminal state
        buildAndAddArchivedUserTaskWithParentAndRootProcessInstanceId("step1", 0L, 147L, 221, "not_used", true); // should not be selected because it is a flownode in a sub-process
        buildAndAddArchivedUserTaskWithParentAndRootProcessInstanceId("step2", 147L, 0L, 2, "completed", true);
        buildAndAddArchivedUserTaskWithParentAndRootProcessInstanceId("step1", 147L, 0L, 4, "aborted", true);

        List<SFlowNodeInstanceStateCounter> counters = repository.getNumberOfArchivedFlowNodesInAllStates(147L);
        assertThat(counters).hasSize(3);

        assertThat(counters.get(0).getFlownodeName()).isEqualTo("step1");
        assertThat(counters.get(0).getStateName()).isEqualTo("aborted");
        assertThat(counters.get(0).getNumberOf()).isEqualTo(1L);

        assertThat(counters.get(1).getFlownodeName()).isEqualTo("step1");
        assertThat(counters.get(1).getStateName()).isEqualTo("completed");
        assertThat(counters.get(1).getNumberOf()).isEqualTo(2L);

        assertThat(counters.get(2).getFlownodeName()).isEqualTo("step2");
        assertThat(counters.get(2).getStateName()).isEqualTo("completed");
        assertThat(counters.get(2).getNumberOf()).isEqualTo(1L);

    }

    private SAFlowNodeInstance buildAndAddArchivedUserTaskWithParentAndRootProcessInstanceId(String taskName,
            long containingProcessInstanceId,
            long rootProcessInstanceId, int stateId, String stateName, boolean terminal) {
        return repository.add(anArchivedUserTask().withName(taskName).withLogicalGroup4(containingProcessInstanceId)
                .withLogicalGroup2(rootProcessInstanceId)
                .withStateId(stateId).withStateName(stateName).withTerminal(terminal).build());
    }

    @Test
    public void getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcess_should_return_number_of_process_definition_supervised_if_one_instance_has_pending_tasks_for_a_user() {
        // Given
        buildAndAddTasksWithPendingMappingForUser();

        // When
        final long numberOfHumanTaskInstances = repository
                .getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcess(PROCESS_DEFINITION_ID);

        // Then
        assertThat(numberOfHumanTaskInstances).isEqualTo(2);
    }

    @Test
    public void getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcess_should_return_number_of_process_definition_supervised_if_one_instance_has_pending_tasks_for_a_actor_with_a_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUser();

        // When
        final long numberOfHumanTaskInstances = repository
                .getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcess(PROCESS_DEFINITION_ID);

        // Then
        assertThat(numberOfHumanTaskInstances).isEqualTo(2);
    }

    @Test
    public void getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcess_should_return_number_of_process_definition_supervised_if_one_instance_has_pending_tasks_for_a_actor_with_a_membership_mapped_to_a_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUserMembershipMappedToUser();

        // When
        final long numberOfHumanTaskInstances = repository
                .getNumberOfSHumanTaskInstanceAssignedAndPendingByRootProcess(PROCESS_DEFINITION_ID);

        // Then
        assertThat(numberOfHumanTaskInstances).isEqualTo(2);
    }

    @Test
    public void searchSHumanTaskInstanceAssignedAndPendingByRootProcess_should_return_number_of_process_definition_supervised_if_one_instance_has_assigned_tasks() {
        // Given
        buildAndAddAssignedTasks();

        // When
        final List<SHumanTaskInstance> sHumanTaskInstances = repository
                .searchSHumanTaskInstanceAssignedAndPendingByRootProcess(PROCESS_DEFINITION_ID);

        // Then
        assertThat(sHumanTaskInstances).hasSize(2);
        assertThat(sHumanTaskInstances.get(0).getId()).isEqualTo(NORMAL_HUMAN_INSTANCE_ID);
    }

    @Test
    public void searchSHumanTaskInstanceAssignedAndPendingByRootProcess_should_return_number_of_process_definition_supervised_if_one_instance_has_pending_tasks_for_a_user() {
        // Given
        buildAndAddTasksWithPendingMappingForUser();

        // When
        final List<SHumanTaskInstance> sHumanTaskInstances = repository
                .searchSHumanTaskInstanceAssignedAndPendingByRootProcess(PROCESS_DEFINITION_ID);

        // Then
        assertThat(sHumanTaskInstances).hasSize(2);
        assertThat(sHumanTaskInstances.get(0).getId()).isEqualTo(NORMAL_HUMAN_INSTANCE_ID);
    }

    @Test
    public void searchSHumanTaskInstanceAssignedAndPendingByRootProcess_should_return_number_of_process_definition_supervised_if_one_instance_has_pending_tasks_for_a_actor_with_a_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUser();

        // When
        final List<SHumanTaskInstance> sHumanTaskInstances = repository
                .searchSHumanTaskInstanceAssignedAndPendingByRootProcess(PROCESS_DEFINITION_ID);

        // Then
        assertThat(sHumanTaskInstances).hasSize(2);
        assertThat(sHumanTaskInstances.get(0).getId()).isEqualTo(NORMAL_HUMAN_INSTANCE_ID);
    }

    @Test
    public void searchSHumanTaskInstanceAssignedAndPendingByRootProcess_should_return_number_of_process_definition_supervised_if_one_instance_has_pending_tasks_for_a_actor_with_a_membership_mapped_to_a_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUserMembershipMappedToUser();

        // When
        final List<SHumanTaskInstance> sHumanTaskInstances = repository
                .searchSHumanTaskInstanceAssignedAndPendingByRootProcess(PROCESS_DEFINITION_ID);

        // Then
        assertThat(sHumanTaskInstances).hasSize(2);
        assertThat(sHumanTaskInstances.get(0).getId()).isEqualTo(NORMAL_HUMAN_INSTANCE_ID);
    }

    @Test
    public void getActiveGatewayInstance_should_return_gateway_if_not_finished() {
        // Given
        final SGatewayInstance gatewayInstance = aGatewayInstanceBuilder().withHitBys("1,2").withName("gate1")
                .withTerminal(false)
                .withLogicalGroup4(ROOT_PROCESS_INSTANCE_ID).build();
        repository.add(gatewayInstance);

        // When
        final SGatewayInstance gate1 = repository.getActiveGatewayInstanceOfProcess(ROOT_PROCESS_INSTANCE_ID, "gate1");

        // Then
        assertThat(gate1).isEqualTo(gatewayInstance);
    }

    @Test
    public void getActiveGatewayInstance_should_not_return_gateway_if_finished() {
        // Given
        repository
                .add(aGatewayInstanceBuilder().withHitBys("FINISH:1").withTerminal(true).withName("gate1")
                        .withLogicalGroup4(ROOT_PROCESS_INSTANCE_ID).build());

        // When
        final SGatewayInstance gate1 = repository.getActiveGatewayInstanceOfProcess(ROOT_PROCESS_INSTANCE_ID, "gate1");

        // Then
        assertThat(gate1).isNull();
    }

    @Test
    public void getActiveGatewayInstance_should_not_return_gateway_if_wrong_name() {
        // Given
        final SGatewayInstance gatewayInstance = aGatewayInstanceBuilder().withHitBys("1,2")
                .withName("notTheGoodGateway").withTerminal(false)
                .withLogicalGroup4(ROOT_PROCESS_INSTANCE_ID).build();
        repository.add(gatewayInstance);

        // When
        final SGatewayInstance gate1 = repository.getActiveGatewayInstanceOfProcess(ROOT_PROCESS_INSTANCE_ID, "gate1");

        // Then
        assertThat(gate1).isNull();
    }

    @Test
    public void should_get_flownodeInstances_by_stateCategory() {
        // Given
        final SFlowNodeInstance sFlowNodeInstance = repository
                .add(aUserTask().withName("executingTask").withStateExecuting(true).build());
        repository.flush();
        final String stateCategory = jdbcTemplate.queryForObject("select stateCategory from flownode_instance",
                String.class);
        final String priority = jdbcTemplate.queryForObject("select priority from flownode_instance", String.class);
        assertThat(stateCategory).isEqualTo("NORMAL");
        assertThat(priority).isEqualTo("2");
    }

    @Test
    public void should_getDirectChildrenOfProcessInstance() {
        // Given
        //is directly contained in the process "5"
        repository.add(aUserTask().withLogicalGroup3(0).withLogicalGroup4(5).withName("multiInstance")
                .withStateExecuting(true).build());

        //is directly contained in the activity "6" that is in the process "5"
        repository.add(aUserTask().withLogicalGroup3(6).withLogicalGroup4(5).withName("callActivity")
                .withStateExecuting(true).build());
        repository.flush();

        List<SFlowNodeInstance> flowNodes = repository.selectList("getDirectChildrenOfProcessInstance",
                pair("parentProcessInstanceId", 5L));
        assertThat(flowNodes).hasSize(1).anyMatch(f -> f.getName().equals("multiInstance"));
    }

    @Test
    public void should_getAllChildrenOfProcessInstance() {
        // Given
        //is directly contained in the process "5"
        repository.add(aUserTask().withLogicalGroup3(0).withLogicalGroup4(5).withName("multiInstance")
                .withStateExecuting(true).build());

        //is directly contained in the activity "6" that is in the process "5"
        repository.add(aUserTask().withLogicalGroup3(6).withLogicalGroup4(5).withName("callActivity")
                .withStateExecuting(true).build());
        repository.flush();

        List<SFlowNodeInstance> flowNodes = repository.selectList("getAllChildrenOfProcessInstance",
                pair("parentProcessInstanceId", 5L));
        assertThat(flowNodes).hasSize(2)
                .anyMatch(f -> f.getName().equals("multiInstance"))
                .anyMatch(f -> f.getName().equals("callActivity"));
    }

    @Test
    public void should_have_loopCounter_on_loop_Activity() {
        // Given
        final SLoopActivityInstance sLoopActivityInstance = (SLoopActivityInstance) repository
                .add(aLoopActivity().withLoopCounter(6).build());
        repository.flush();
        final int loopCounter = jdbcTemplate.queryForObject("select loop_counter from flownode_instance",
                Integer.class);
        repository.getById(sLoopActivityInstance.getId());
        assertThat(loopCounter).isEqualTo(6);
        assertThat(sLoopActivityInstance.getLoopCounter()).isEqualTo(6);
    }

    @Test
    public void should_get_gateway_instances_by_gateway_type() {
        // Given
        final SGatewayInstance gatewayInstance = repository
                .add(aGatewayInstanceBuilder().withGatewayType(PARALLEL).build());
        repository.flush();
        final String gatewayType = jdbcTemplate.queryForObject("select gatewayType from flownode_instance",
                String.class);
        assertThat(gatewayType).isEqualTo("PARALLEL");
        assertThat(gatewayInstance.getGatewayType()).isEqualTo(PARALLEL);
    }

    private void buildAndAddAssignedTasks() {
        // Tasks OK assigned to John
        repository
                .add(aUserTask().withName("normalTask1").withStateExecuting(false).withStable(true).withTerminal(false)
                        .withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).withAssigneeId(JOHN_ID)
                        .withId(NORMAL_HUMAN_INSTANCE_ID).build());

        // Tasks KO assigned to john & OK not assigned
        repository
                .add(aUserTask().withName("executingTask").withStateExecuting(true).withStable(true).withTerminal(false)
                        .withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).withAssigneeId(JOHN_ID).build());
        repository.add(
                aUserTask().withName("notStableTask").withStateExecuting(false).withStable(false).withTerminal(true)
                        .withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).withAssigneeId(JOHN_ID).build());
        repository
                .add(aUserTask().withName("terminalTask").withStateExecuting(false).withStable(true).withTerminal(true)
                        .withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).withAssigneeId(JOHN_ID).build());
        buildAndAddNormalTask("normalTask4", ROOT_PROCESS_INSTANCE_ID);

        // Tasks OK assigned to Bob
        repository
                .add(aUserTask().withName("normalTask2").withStateExecuting(false).withStable(true).withTerminal(false)
                        .withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).withAssigneeId(BOB_ID).build());
    }

    private void buildAndAddTasksWithPendingMappingForUser() {
        // Tasks OK not assigned & pending for John
        final SFlowNodeInstance normalTask1 = repository
                .add(aUserTask().withName("normalTask1").withStateExecuting(false).withStable(true).withTerminal(false)
                        .withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).withId(NORMAL_HUMAN_INSTANCE_ID).build());
        repository.add(aPendingActivityMapping().withUserId(JOHN_ID).withActivityId(normalTask1.getId()).build());

        // Tasks KO not assigned & pending for john, and OK not assigned & not pending
        final SFlowNodeInstance executingTask = buildAndAddExecutingTask();
        repository.add(aPendingActivityMapping().withUserId(JOHN_ID).withActivityId(executingTask.getId()).build());
        final SFlowNodeInstance notStableTask = buildAndAddNotStableTask();
        repository.add(aPendingActivityMapping().withUserId(JOHN_ID).withActivityId(notStableTask.getId()).build());
        final SFlowNodeInstance terminalTask = buildAndAddTerminalTask();
        repository.add(aPendingActivityMapping().withUserId(JOHN_ID).withActivityId(terminalTask.getId()).build());
        buildAndAddNormalTask("normalTask4", ROOT_PROCESS_INSTANCE_ID);

        // Tasks OK not assigned & pending for Bob
        final SFlowNodeInstance normalTask4 = buildAndAddNormalTask("normalTask2", ROOT_PROCESS_INSTANCE_ID);
        repository.add(aPendingActivityMapping().withUserId(BOB_ID).withActivityId(normalTask4.getId()).build());
    }

    private void buildAndAddTasksWithPendingMappingForActorMappedToUser() {
        final SActor actorForJohn = repository.add(anActor().build());
        repository.add(anActorMember().forActor(actorForJohn).withUserId(JOHN_ID).build());
        final SActor actorForBob = repository.add(anActor().build());
        repository.add(anActorMember().forActor(actorForBob).withUserId(BOB_ID).build());

        buildAndAddTasksWithPendingMappingForActor(actorForJohn, actorForBob);
    }

    private void buildAndAddTasksWithPendingMappingForActorMappedToUserMembershipMappedToUser() {
        final long roleId = 1235L;
        repository.add(aUserMembership().forUser(JOHN_ID).memberOf(GROUP_FOR_JOHN_ID, roleId).build());
        repository.add(aUserMembership().forUser(BOB_ID).memberOf(GROUP_FOR_BOB_ID, roleId).build());

        final SActor actorForJohn = repository.add(anActor().build());
        repository.add(anActorMember().forActor(actorForJohn).withGroupId(GROUP_FOR_JOHN_ID).build());

        final SActor actorForBob = repository.add(anActor().build());
        repository.add(anActorMember().forActor(actorForBob).withGroupId(GROUP_FOR_BOB_ID).build());

        buildAndAddTasksWithPendingMappingForActor(actorForJohn, actorForBob);
    }

    private void buildAndAddTasksWithPendingMappingForActor(final SActor actorForJohn, final SActor actorForBob) {
        // Tasks OK not assigned & pending for John
        final SFlowNodeInstance normalTask1 = repository
                .add(aUserTask().withName("normalTask1").withStateExecuting(false).withStable(true).withTerminal(false)
                        .withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).withId(NORMAL_HUMAN_INSTANCE_ID).build());
        repository.add(aPendingActivityMapping().withActorId(actorForJohn.getId()).withActivityId(normalTask1.getId())
                .build());

        // Tasks KO not assigned & pending for john, and OK not assigned & not pending
        final SFlowNodeInstance executingTask = buildAndAddExecutingTask();
        repository.add(aPendingActivityMapping().withActorId(actorForJohn.getId()).withActivityId(executingTask.getId())
                .build());
        final SFlowNodeInstance notStableTask = buildAndAddNotStableTask();
        repository.add(aPendingActivityMapping().withActorId(actorForJohn.getId()).withActivityId(notStableTask.getId())
                .build());
        final SFlowNodeInstance terminalTask = buildAndAddTerminalTask();
        repository.add(aPendingActivityMapping().withActorId(actorForJohn.getId()).withActivityId(terminalTask.getId())
                .build());
        buildAndAddNormalTask("normalTask4", ROOT_PROCESS_INSTANCE_ID);

        // Tasks OK not assigned & pending for Bob
        final SFlowNodeInstance normalTask4 = buildAndAddNormalTask("normalTask2", ROOT_PROCESS_INSTANCE_ID);
        repository.add(
                aPendingActivityMapping().withActorId(actorForBob.getId()).withActivityId(normalTask4.getId()).build());
    }

    private SFlowNodeInstance buildAndAddNormalTask(final String taskName, final long rootProcessInstanceId) {
        return repository
                .add(aUserTask().withName(taskName).withStateExecuting(false).withStable(true).withTerminal(false)
                        .withRootProcessInstanceId(rootProcessInstanceId).build());
    }

    private SFlowNodeInstance buildAndAddExecutingTask() {
        return repository.add(aUserTask().withName("executingTask").withStateExecuting(true).withStable(true)
                .withTerminal(false).withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).build());
    }

    private SFlowNodeInstance buildAndAddNotStableTask() {
        return repository.add(aUserTask().withName("notStableTask").withStateExecuting(false).withStable(false)
                .withTerminal(true).withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).build());
    }

    private SFlowNodeInstance buildAndAddTerminalTask() {
        return repository.add(aUserTask().withName("terminalTask").withStateExecuting(false).withStable(true)
                .withTerminal(true).withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).build());
    }

    @Test
    public void should_save_and_get_SAutomaticTaskInstance() {
        SFlowNodeInstance entity = new SAutomaticTaskInstance();
        SFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getSFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("auto");
    }

    @Test
    public void should_save_and_get_SCallActivityInstance() {
        SFlowNodeInstance entity = new SCallActivityInstance();
        SFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getSFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("call");
    }

    @Test
    public void should_save_and_get_SGatewayInstance() {
        SFlowNodeInstance entity = new SGatewayInstance();
        SFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getSFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("gate");
    }

    @Test
    public void should_save_and_get_SLoopActivityInstance() {
        SFlowNodeInstance entity = new SLoopActivityInstance();
        SFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getSFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("loop");
    }

    @Test
    public void should_save_and_get_SManualTaskInstance() {
        SFlowNodeInstance entity = new SManualTaskInstance();
        SFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getSFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("manual");
    }

    @Test
    public void should_save_and_get_SMultiInstanceActivityInstance() {
        SFlowNodeInstance entity = new SMultiInstanceActivityInstance();
        SFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getSFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("multi");
    }

    @Test
    public void should_save_and_get_SReceiveTaskInstance() {
        SFlowNodeInstance entity = new SReceiveTaskInstance();
        SFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getSFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("receive");
    }

    @Test
    public void should_save_and_get_SSendTaskInstance() {
        SFlowNodeInstance entity = new SSendTaskInstance();
        SFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getSFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("send");
    }

    @Test
    public void should_save_and_get_SSubProcessActivityInstance() {
        SFlowNodeInstance entity = new SSubProcessActivityInstance();
        SFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getSFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("subProc");
    }

    @Test
    public void should_save_and_get_SUserTaskInstance() {
        SFlowNodeInstance entity = new SUserTaskInstance();
        SFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getSFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("user");
    }

    @Test
    public void should_save_and_get_SBoundaryEventInstance() {
        SFlowNodeInstance entity = new SBoundaryEventInstance();
        SFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getSFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("boundaryEvent");
    }

    @Test
    public void should_save_and_get_SEndEventInstance() {
        SFlowNodeInstance entity = new SEndEventInstance();
        SFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getSFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("endEvent");
    }

    @Test
    public void should_save_and_get_SIntermediateCatchEventInstance() {
        SFlowNodeInstance entity = new SIntermediateCatchEventInstance();
        SFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getSFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("intermediateCatchEvent");
    }

    @Test
    public void should_save_and_get_SIntermediateThrowEventInstance() {
        SFlowNodeInstance entity = new SIntermediateThrowEventInstance();
        SFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getSFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("intermediateThrowEvent");
    }

    @Test
    public void should_save_and_get_SStartEventInstance() {
        SFlowNodeInstance entity = new SStartEventInstance();
        SFlowNodeInstance flowNode = repository.add(entity);
        repository.flush();

        PersistentObject flowNodeFromQuery = repository.selectOne("getSFlowNodeInstanceById",
                pair("id", flowNode.getId()));
        Map<String, Object> flowNodeAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM flownode_instance where id = " + flowNode.getId());

        assertThat(flowNodeFromQuery).isEqualTo(flowNode);
        assertThat(flowNodeAsMap.get("KIND")).isEqualTo("startEvent");
    }

    @Test
    public void should_save_and_get_STimerEventTriggerInstance() {
        STimerEventTriggerInstance entity = new STimerEventTriggerInstance();
        entity.setEventInstanceName("eventInstanceName");
        STimerEventTriggerInstance trigger = repository.add(entity);
        repository.flush();

        PersistentObject triggerFromQuery = repository.selectOne("getEventTriggerInstanceById",
                pair("id", trigger.getId()));
        Map<String, Object> triggerAsMap = jdbcTemplate
                .queryForMap("SELECT * FROM event_trigger_instance where id = " + trigger.getId());

        assertThat(triggerFromQuery).isEqualTo(trigger);
        assertThat(triggerAsMap.get("EVENTINSTANCENAME")).isEqualTo("eventInstanceName");
    }

}
