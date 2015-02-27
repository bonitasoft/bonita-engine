/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.process.instance.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.test.persistence.builder.ActorBuilder.anActor;
import static org.bonitasoft.engine.test.persistence.builder.ActorMemberBuilder.anActorMember;
import static org.bonitasoft.engine.test.persistence.builder.PendingActivityMappingBuilder.aPendingActivityMapping;
import static org.bonitasoft.engine.test.persistence.builder.ProcessInstanceBuilder.aProcessInstance;
import static org.bonitasoft.engine.test.persistence.builder.UserBuilder.aUser;
import static org.bonitasoft.engine.test.persistence.builder.UserMembershipBuilder.aUserMembership;
import static org.bonitasoft.engine.test.persistence.builder.UserTaskInstanceBuilder.aUserTask;

import java.util.List;

import javax.inject.Inject;

import org.bonitasoft.engine.actor.mapping.model.SActor;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinitionDeployInfo;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionDeployInfoImpl;
import org.bonitasoft.engine.supervisor.mapping.model.impl.SProcessSupervisorImpl;
import org.bonitasoft.engine.test.persistence.repository.ProcessDeploymentInfoRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class ProcessDeploymentInfoQueriesTest {

    private static final long GROUP_FOR_BOB_ID = 74L;

    private static final long GROUP_FOR_JOHN_ID = 9875L;

    private static final long GROUP_FOR_SUPERVISOR_FOR_BOB_ID = 369L;

    private static final long GROUP_FOR_SUPERVISOR_FOR_JOHN_ID = 7453L;

    private static final long JOHN_ID = 654L;

    private static final long BOB_ID = 697L;

    private static final long PAUL_ID = 94L;

    private static final long JACK_ID = 63L;

    private static final long PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN = 111L;

    private static final long PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS = 112L;

    private static final long PROCESS_DEFINITION_ID_SUPERVISED_BY_BOB = 113L;

    private static final long PROCESS_DEFINITION_ID_NOT_SUPERVISED = 114L;

    private static final long ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN = 963L;

    private static final long ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS = 1234L;

    private static final long ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_BOB = 1258L;

    private static final long ROOT_PROCESS_INSTANCE_ID_NOT_SUPERVISED = 1269L;

    @Inject
    private ProcessDeploymentInfoRepository repository;

    @Before
    public void before() {
        repository.add(aUser().withId(JOHN_ID).build());
        repository.add(aUser().withId(BOB_ID).build());
        repository.add(aUser().withId(PAUL_ID).build());
        repository.add(aUser().withId(JACK_ID).build());

        // Create process supervised by john
        final String processNameSupervisedByJohn = "Process supervised by John";
        buildAndCreateProcessDefinition(6L, PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN, processNameSupervisedByJohn);
        repository.add(aProcessInstance().withProcessDefinitionId(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN).withName(processNameSupervisedByJohn)
                .withId(ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN).build());

        // Create process supervised by john with only KO taks
        final String processNameSupervisedByJohnWithOnlyKOTasks = "Process supervised by John with only KO taks";
        buildAndCreateProcessDefinition(8L, PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS, processNameSupervisedByJohnWithOnlyKOTasks);
        repository.add(aProcessInstance().withProcessDefinitionId(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS)
                .withName(processNameSupervisedByJohnWithOnlyKOTasks).withId(ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS).build());

        // Create process supervised by bob
        final String processNameSupervisedByBob = "Process supervised by Bob";
        buildAndCreateProcessDefinition(10L, PROCESS_DEFINITION_ID_SUPERVISED_BY_BOB, processNameSupervisedByBob);
        repository.add(aProcessInstance().withProcessDefinitionId(PROCESS_DEFINITION_ID_SUPERVISED_BY_BOB).withName(processNameSupervisedByBob)
                .withId(ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_BOB).build());

        // Create not supervised process
        final String processNameNotSupervised = "Process not supervised";
        buildAndCreateProcessDefinition(12L, PROCESS_DEFINITION_ID_NOT_SUPERVISED, processNameNotSupervised);
        repository.add(aProcessInstance().withProcessDefinitionId(PROCESS_DEFINITION_ID_NOT_SUPERVISED).withName(processNameNotSupervised)
                .withId(ROOT_PROCESS_INSTANCE_ID_NOT_SUPERVISED).build());
    }

    protected void buildAndCreateProcessDefinition(final long id, long processDefinitionId, final String processName) {
        final SProcessDefinitionDeployInfoImpl sProcessDefinitionDeployInfoImpl = new SProcessDefinitionDeployInfoImpl();
        sProcessDefinitionDeployInfoImpl.setId(id);
        sProcessDefinitionDeployInfoImpl.setName(processName);
        sProcessDefinitionDeployInfoImpl.setVersion("version");
        sProcessDefinitionDeployInfoImpl.setProcessId(processDefinitionId);
        sProcessDefinitionDeployInfoImpl.setTenantId(1L);
        repository.add(sProcessDefinitionDeployInfoImpl);
    }

    // For
    @Test
    public void getNumberOfProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksFor_should_return_number_of_process_definition_if_one_instance_has_assigned_tasks_to_the_user() {
        // Given
        buildAndAddAssignedTasks();

        // When
        final long numberOfProcessDefinitionDeployInfos = repository.getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksFor(JACK_ID);

        // Then
        assertThat(numberOfProcessDefinitionDeployInfos).isEqualTo(1);
    }

    @Test
    public void getNumberOfProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksFor_should_return_number_of_process_definition_if_one_instance_has_pending_tasks_for_the_user() {
        // Given
        buildAndAddTasksWithPendingMappingForUser();

        // When
        long numberOfProcessDefinitionDeployInfos = repository.getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksFor(JACK_ID);

        // Then
        assertThat(numberOfProcessDefinitionDeployInfos).isEqualTo(1);
    }

    @Test
    public void getNumberOfProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksFor_should_return_number_of_process_definition_if_one_instance_has_pending_tasks_for_a_actor_with_the_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUser();

        // When
        long numberOfProcessDefinitionDeployInfos = repository.getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksFor(JACK_ID);

        // Then
        assertThat(numberOfProcessDefinitionDeployInfos).isEqualTo(1);
    }

    @Test
    public void getNumberOfProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksFor_should_return_number_of_process_definition_if_one_instance_has_pending_tasks_for_a_actor_with_a_membership_mapped_to_the_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUserMembershipMappedToUser();

        // When
        long numberOfProcessDefinitionDeployInfos = repository.getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksFor(JACK_ID);

        // Then
        assertThat(numberOfProcessDefinitionDeployInfos).isEqualTo(1);
    }

    @Test
    public void searchProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksFor_should_return_number_of_process_definition_if_one_instance_has_assigned_tasks_to_the_user() {
        // Given
        buildAndAddAssignedTasks();

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksFor(JACK_ID);

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(1);
        assertThat(sProcessDefinitionDeployInfos.get(0).getProcessId()).isEqualTo(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
    }

    @Test
    public void searchProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksFor_should_return_number_of_process_definition_if_one_instance_has_pending_tasks_for_the_user() {
        // Given
        buildAndAddTasksWithPendingMappingForUser();

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksFor(JACK_ID);

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(1);
        assertThat(sProcessDefinitionDeployInfos.get(0).getProcessId()).isEqualTo(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
    }

    @Test
    public void searchProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksFor_should_return_number_of_process_definition_if_one_instance_has_pending_tasks_for_a_actor_with_the_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUser();

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksFor(JACK_ID);

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(1);
        assertThat(sProcessDefinitionDeployInfos.get(0).getProcessId()).isEqualTo(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
    }

    @Test
    public void searchProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksFor_should_return_number_of_process_definition_if_one_instance_has_pending_tasks_for_a_actor_with_a_membership_mapped_to_the_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUserMembershipMappedToUser();

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksFor(JACK_ID);

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(1);
        assertThat(sProcessDefinitionDeployInfos.get(0).getProcessId()).isEqualTo(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
    }

    // Supervised By
    @Test
    public void getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy_should_return_number_of_process_definition_supervised_by_user_if_one_instance_has_assigned_tasks() {
        // Given
        buildAndAddAssignedTasks();
        buildAndAddSupervisorMappedToUser();

        // When
        final long numberOfProcessDefinitionDeployInfos = repository.getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(JOHN_ID);

        // Then
        assertThat(numberOfProcessDefinitionDeployInfos).isEqualTo(1);
    }

    @Test
    public void getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy_should_return_number_of_process_definition_supervised_by_userMembership_if_one_instance_has_assigned_tasks() {
        // Given
        buildAndAddAssignedTasks();
        buildAndAddSupervisorMappedToUserMembershipMappedToUser();

        // When
        final long numberOfProcessDefinitionDeployInfos = repository.getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(JOHN_ID);

        // Then
        assertThat(numberOfProcessDefinitionDeployInfos).isEqualTo(1);
    }

    @Test
    public void getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy_should_return_number_of_process_definition_supervised_by_user_if_one_instance_has_pending_tasks_for_a_user() {
        // Given
        buildAndAddTasksWithPendingMappingForUser();
        buildAndAddSupervisorMappedToUser();

        // When
        long numberOfProcessDefinitionDeployInfos = repository.getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(JOHN_ID);

        // Then
        assertThat(numberOfProcessDefinitionDeployInfos).isEqualTo(1);
    }

    @Test
    public void getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy_should_return_number_of_process_definition_supervised_by_userMembership_if_one_instance_has_pending_tasks_for_a_user() {
        // Given
        buildAndAddTasksWithPendingMappingForUser();
        buildAndAddSupervisorMappedToUserMembershipMappedToUser();

        // When
        long numberOfProcessDefinitionDeployInfos = repository.getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(JOHN_ID);

        // Then
        assertThat(numberOfProcessDefinitionDeployInfos).isEqualTo(1);
    }

    @Test
    public void getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy_should_return_number_of_process_definition_supervised_by_user_if_one_instance_has_pending_tasks_for_a_actor_with_a_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUser();
        buildAndAddSupervisorMappedToUser();

        // When
        long numberOfProcessDefinitionDeployInfos = repository.getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(JOHN_ID);

        // Then
        assertThat(numberOfProcessDefinitionDeployInfos).isEqualTo(1);
    }

    @Test
    public void getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy_should_return_number_of_process_definition_supervised_by_userMembership_if_one_instance_has_pending_tasks_for_a_actor_with_a_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUser();
        buildAndAddSupervisorMappedToUserMembershipMappedToUser();

        // When
        long numberOfProcessDefinitionDeployInfos = repository.getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(JOHN_ID);

        // Then
        assertThat(numberOfProcessDefinitionDeployInfos).isEqualTo(1);
    }

    @Test
    public void getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy_should_return_number_of_process_definition_supervised_by_user_if_one_instance_has_pending_tasks_for_a_actor_with_a_membership_mapped_to_a_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUserMembershipMappedToUser();
        buildAndAddSupervisorMappedToUser();

        // When
        long numberOfProcessDefinitionDeployInfos = repository.getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(JOHN_ID);

        // Then
        assertThat(numberOfProcessDefinitionDeployInfos).isEqualTo(1);
    }

    @Test
    public void getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy_should_return_number_of_process_definition_supervised_by_userMembership_if_one_instance_has_pending_tasks_for_a_actor_with_a_membership_mapped_to_a_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUserMembershipMappedToUser();
        buildAndAddSupervisorMappedToUserMembershipMappedToUser();

        // When
        long numberOfProcessDefinitionDeployInfos = repository.getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(JOHN_ID);

        // Then
        assertThat(numberOfProcessDefinitionDeployInfos).isEqualTo(1);
    }

    @Test
    public void searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy_should_return_number_of_process_definition_supervised_by_user_if_one_instance_has_assigned_tasks() {
        // Given
        buildAndAddAssignedTasks();
        buildAndAddSupervisorMappedToUser();

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(JOHN_ID);

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(1);
        assertThat(sProcessDefinitionDeployInfos.get(0).getProcessId()).isEqualTo(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
    }

    @Test
    public void searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy_should_return_number_of_process_definition_supervised_by_userMembership_if_one_instance_has_assigned_tasks() {
        // Given
        buildAndAddAssignedTasks();
        buildAndAddSupervisorMappedToUserMembershipMappedToUser();

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(JOHN_ID);

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(1);
        assertThat(sProcessDefinitionDeployInfos.get(0).getProcessId()).isEqualTo(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
    }

    @Test
    public void searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy_should_return_number_of_process_definition_supervised_by_user_if_one_instance_has_pending_tasks_for_a_user() {
        // Given
        buildAndAddTasksWithPendingMappingForUser();
        buildAndAddSupervisorMappedToUser();

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(JOHN_ID);

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(1);
        assertThat(sProcessDefinitionDeployInfos.get(0).getProcessId()).isEqualTo(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
    }

    @Test
    public void searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy_should_return_number_of_process_definition_supervised_by_userMembership_if_one_instance_has_pending_tasks_for_a_user() {
        // Given
        buildAndAddTasksWithPendingMappingForUser();
        buildAndAddSupervisorMappedToUserMembershipMappedToUser();

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(JOHN_ID);

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(1);
        assertThat(sProcessDefinitionDeployInfos.get(0).getProcessId()).isEqualTo(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
    }

    @Test
    public void searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy_should_return_number_of_process_definition_supervised_by_user_if_one_instance_has_pending_tasks_for_a_actor_with_a_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUser();
        buildAndAddSupervisorMappedToUser();

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(JOHN_ID);

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(1);
        assertThat(sProcessDefinitionDeployInfos.get(0).getProcessId()).isEqualTo(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
    }

    @Test
    public void searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy_should_return_number_of_process_definition_supervised_by_userMembership_if_one_instance_has_pending_tasks_for_a_actor_with_a_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUser();
        buildAndAddSupervisorMappedToUserMembershipMappedToUser();

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(JOHN_ID);

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(1);
        assertThat(sProcessDefinitionDeployInfos.get(0).getProcessId()).isEqualTo(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
    }

    @Test
    public void searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy_should_return_number_of_process_definition_supervised_by_user_if_one_instance_has_pending_tasks_for_a_actor_with_a_membership_mapped_to_a_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUserMembershipMappedToUser();
        buildAndAddSupervisorMappedToUser();

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(JOHN_ID);

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(1);
        assertThat(sProcessDefinitionDeployInfos.get(0).getProcessId()).isEqualTo(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
    }

    @Test
    public void searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy_should_return_number_of_process_definition_supervised_by_userMembership_if_one_instance_has_pending_tasks_for_a_actor_with_a_membership_mapped_to_a_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUserMembershipMappedToUser();
        buildAndAddSupervisorMappedToUserMembershipMappedToUser();

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(JOHN_ID);

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(1);
        assertThat(sProcessDefinitionDeployInfos.get(0).getProcessId()).isEqualTo(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
    }

    // All (for admin)
    @Test
    public void getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasks_should_return_number_of_process_definition_supervised_if_one_instance_has_assigned_tasks() {
        // Given
        buildAndAddAssignedTasks();
        buildAndAddSupervisorMappedToUser();

        // When
        final long numberOfProcessDefinitionDeployInfos = repository.getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasks();

        // Then
        assertThat(numberOfProcessDefinitionDeployInfos).isEqualTo(4);
    }

    @Test
    public void getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasks_should_return_number_of_process_definition_supervised_if_one_instance_has_pending_tasks_for_a_user() {
        // Given
        buildAndAddTasksWithPendingMappingForUser();
        buildAndAddSupervisorMappedToUser();

        // When
        long numberOfProcessDefinitionDeployInfos = repository.getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasks();

        // Then
        assertThat(numberOfProcessDefinitionDeployInfos).isEqualTo(4);
    }

    @Test
    public void getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasks_should_return_number_of_process_definition_supervised_if_one_instance_has_pending_tasks_for_a_actor_with_a_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUser();
        buildAndAddSupervisorMappedToUser();

        // When
        long numberOfProcessDefinitionDeployInfos = repository.getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasks();

        // Then
        assertThat(numberOfProcessDefinitionDeployInfos).isEqualTo(1);
    }

    @Test
    public void getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasks_should_return_number_of_process_definition_supervised_if_one_instance_has_pending_tasks_for_a_actor_with_a_membership_mapped_to_a_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUserMembershipMappedToUser();
        buildAndAddSupervisorMappedToUser();

        // When
        long numberOfProcessDefinitionDeployInfos = repository.getNumberOfProcessDeploymentInfosWithAssignedOrPendingHumanTasks();

        // Then
        assertThat(numberOfProcessDefinitionDeployInfos).isEqualTo(1);
    }

    @Test
    public void searchProcessDeploymentInfosWithAssignedOrPendingHumanTasks_should_return_number_of_process_definition_supervised_if_one_instance_has_assigned_tasks() {
        // Given
        buildAndAddAssignedTasks();
        buildAndAddSupervisorMappedToUser();

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDeploymentInfosWithAssignedOrPendingHumanTasks();

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(4);
        assertThat(sProcessDefinitionDeployInfos.get(0).getProcessId()).isEqualTo(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
    }

    @Test
    public void searchProcessDeploymentInfosWithAssignedOrPendingHumanTasks_should_return_number_of_process_definition_supervised_if_one_instance_has_pending_tasks_for_a_user() {
        // Given
        buildAndAddTasksWithPendingMappingForUser();
        buildAndAddSupervisorMappedToUser();

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDeploymentInfosWithAssignedOrPendingHumanTasks();

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(4);
        assertThat(sProcessDefinitionDeployInfos.get(0).getProcessId()).isEqualTo(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
    }

    @Test
    public void searchProcessDeploymentInfosWithAssignedOrPendingHumanTasks_should_return_number_of_process_definition_supervised_if_one_instance_has_pending_tasks_for_a_actor_with_a_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUser();
        buildAndAddSupervisorMappedToUser();

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDeploymentInfosWithAssignedOrPendingHumanTasks();

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(1);
        assertThat(sProcessDefinitionDeployInfos.get(0).getProcessId()).isEqualTo(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
    }

    @Test
    public void searchProcessDeploymentInfosWithAssignedOrPendingHumanTasks_should_return_number_of_process_definition_supervised_if_one_instance_has_pending_tasks_for_a_actor_with_a_membership_mapped_to_a_user_as_member() {
        // Given
        buildAndAddTasksWithPendingMappingForActorMappedToUserMembershipMappedToUser();
        buildAndAddSupervisorMappedToUser();

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDeploymentInfosWithAssignedOrPendingHumanTasks();

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(1);
        assertThat(sProcessDefinitionDeployInfos.get(0).getProcessId()).isEqualTo(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
    }

    private void buildAndAddSupervisorMappedToUser() {
        repository.add(new SProcessSupervisorImpl(3, 1, PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN, JOHN_ID, -1, -1));
        repository.add(new SProcessSupervisorImpl(4, 1, PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS, JOHN_ID, -1, -1));
        repository.add(new SProcessSupervisorImpl(5, 1, PROCESS_DEFINITION_ID_SUPERVISED_BY_BOB, BOB_ID, -1, -1));
    }

    private void buildAndAddSupervisorMappedToUserMembershipMappedToUser() {
        final long roleId = 1295L;
        repository.add(aUserMembership().forUser(JOHN_ID).memberOf(GROUP_FOR_SUPERVISOR_FOR_JOHN_ID, roleId).build());
        repository.add(aUserMembership().forUser(BOB_ID).memberOf(GROUP_FOR_SUPERVISOR_FOR_BOB_ID, roleId).build());

        repository.add(new SProcessSupervisorImpl(3, 1, PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN, -1, GROUP_FOR_SUPERVISOR_FOR_JOHN_ID, -1));
        repository.add(new SProcessSupervisorImpl(4, 1, PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS, -1, GROUP_FOR_SUPERVISOR_FOR_JOHN_ID, -1));
        repository.add(new SProcessSupervisorImpl(5, 1, PROCESS_DEFINITION_ID_SUPERVISED_BY_BOB, -1, GROUP_FOR_SUPERVISOR_FOR_BOB_ID, -1));
    }

    private void buildAndAddAssignedTasks() {
        // Tasks OK assigned to John
        repository.add(aUserTask().withName("normalTask1").withStateExecuting(false).withStable(true).withTerminal(false)
                .withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN).withAssigneeId(JACK_ID).withStateId(4).withProcessDefinition(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN).build());
        repository.add(aUserTask().withName("normalTask2").withStateExecuting(false).withStable(true).withTerminal(false)
                .withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN).withAssigneeId(JACK_ID).withStateId(4).withProcessDefinition(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN).build());
        repository.add(aUserTask().withName("normalTask3").withStateExecuting(false).withStable(true).withTerminal(false)
                .withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN).withAssigneeId(JACK_ID).withStateId(4).withProcessDefinition(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN).build());

        // Tasks KO assigned to john & OK not assigned
        repository.add(aUserTask().withName("executingTask").withStateExecuting(true).withStable(true).withTerminal(false)
                .withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS).withAssigneeId(JACK_ID).withProcessDefinition(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS).build());
        repository.add(aUserTask().withName("notStableTask").withStateExecuting(false).withStable(false).withTerminal(true)
                .withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS).withAssigneeId(JACK_ID).withProcessDefinition(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS).build());
        repository.add(aUserTask().withName("notStableTask").withStateExecuting(false).withStable(false).withTerminal(true)
                .withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS).withAssigneeId(JACK_ID).withStateId(4).withProcessDefinition(PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS).build());
        repository.add(aUserTask().withName("terminalTask").withStateExecuting(false).withStable(true).withTerminal(true)
                .withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS).withAssigneeId(JACK_ID).build());
        buildAndAddNormalTask("normalTask1", ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS, PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS);

        // Tasks OK assigned to Bob
        repository.add(aUserTask().withName("normalTask1").withStateExecuting(false).withStable(true).withTerminal(false)
                .withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_BOB).withStateId(4).withProcessDefinition(PROCESS_DEFINITION_ID_SUPERVISED_BY_BOB).withAssigneeId(PAUL_ID).build());

        // Tasks OK assigned to Bob, process not supervised
        repository.add(aUserTask().withName("normalTask1").withStateExecuting(false).withStable(true).withTerminal(false)
                .withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID_NOT_SUPERVISED).withStateId(4).withProcessDefinition(PROCESS_DEFINITION_ID_NOT_SUPERVISED).withAssigneeId(PAUL_ID).build());
    }

    private void buildAndAddTasksWithPendingMappingForUser() {
        // Tasks OK not assigned & pending for John
        final SFlowNodeInstance normalTask1 = buildAndAddNormalTask("normalTask1", ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN, PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
        repository.add(aPendingActivityMapping().withUserId(JACK_ID).withActivityId(normalTask1.getId()).build());
        final SFlowNodeInstance normalTask2 = buildAndAddNormalTask("normalTask2", ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN, PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
        repository.add(aPendingActivityMapping().withUserId(JACK_ID).withActivityId(normalTask2.getId()).build());
        final SFlowNodeInstance normalTask3 = buildAndAddNormalTask("normalTask3", ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN, PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
        repository.add(aPendingActivityMapping().withUserId(JACK_ID).withActivityId(normalTask3.getId()).build());

        // Tasks KO not assigned & pending for john, and OK not assigned & not pending
        final SFlowNodeInstance executingTask = buildAndAddExecutingTask();
        repository.add(aPendingActivityMapping().withUserId(JACK_ID).withActivityId(executingTask.getId()).build());
        final SFlowNodeInstance notStableTask = buildAndAddNotStableTask();
        repository.add(aPendingActivityMapping().withUserId(JACK_ID).withActivityId(notStableTask.getId()).build());
        final SFlowNodeInstance terminalTask = buildAndAddTerminalTask();
        repository.add(aPendingActivityMapping().withUserId(JACK_ID).withActivityId(terminalTask.getId()).build());
        buildAndAddNormalTask("normalTask1", ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS, PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS);

        // Tasks OK not assigned & pending for Bob
        final SFlowNodeInstance normalTask4 = buildAndAddNormalTask("normalTask1", ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_BOB, PROCESS_DEFINITION_ID_SUPERVISED_BY_BOB);
        repository.add(aPendingActivityMapping().withUserId(PAUL_ID).withActivityId(normalTask4.getId()).build());

        // Tasks OK not assigned & pending for Bob, process not supervised
        final SFlowNodeInstance normalTask5 = buildAndAddNormalTask("normalTask1", ROOT_PROCESS_INSTANCE_ID_NOT_SUPERVISED, PROCESS_DEFINITION_ID_NOT_SUPERVISED);
        repository.add(aPendingActivityMapping().withUserId(PAUL_ID).withActivityId(normalTask5.getId()).build());
    }

    private void buildAndAddTasksWithPendingMappingForActorMappedToUser() {
        final SActor actorForJohn = repository.add(anActor().build());
        repository.add(anActorMember().forActor(actorForJohn).withUserId(JACK_ID).build());
        final SActor actorForBob = repository.add(anActor().build());
        repository.add(anActorMember().forActor(actorForBob).withUserId(PAUL_ID).build());

        buildAndAddTasksWithPendingMappingForActor(actorForJohn, actorForBob);
    }

    private void buildAndAddTasksWithPendingMappingForActorMappedToUserMembershipMappedToUser() {
        final long roleId = 1235L;
        repository.add(aUserMembership().forUser(JACK_ID).memberOf(GROUP_FOR_JOHN_ID, roleId).build());
        repository.add(aUserMembership().forUser(PAUL_ID).memberOf(GROUP_FOR_BOB_ID, roleId).build());

        final SActor actorForJohn = repository.add(anActor().build());
        repository.add(anActorMember().forActor(actorForJohn).withGroupId(GROUP_FOR_JOHN_ID).build());

        final SActor actorForBob = repository.add(anActor().build());
        repository.add(anActorMember().forActor(actorForBob).withGroupId(GROUP_FOR_BOB_ID).build());

        buildAndAddTasksWithPendingMappingForActor(actorForJohn, actorForBob);
    }

    private void buildAndAddTasksWithPendingMappingForActor(final SActor actorForJohn, final SActor actorForBob) {
        // Tasks OK not assigned & pending for John
        final SFlowNodeInstance normalTask1 = buildAndAddNormalTask("normalTask1", ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN, PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
        repository.add(aPendingActivityMapping().withActorId(actorForJohn.getId()).withActivityId(normalTask1.getId()).build());
        final SFlowNodeInstance normalTask2 = buildAndAddNormalTask("normalTask2", ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN, PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
        repository.add(aPendingActivityMapping().withActorId(actorForJohn.getId()).withActivityId(normalTask2.getId()).build());
        final SFlowNodeInstance normalTask3 = buildAndAddNormalTask("normalTask3", ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN, PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
        repository.add(aPendingActivityMapping().withActorId(actorForJohn.getId()).withActivityId(normalTask3.getId()).build());

        // Tasks KO not assigned & pending for john, and OK not assigned & not pending
        final SFlowNodeInstance executingTask = buildAndAddExecutingTask();
        repository.add(aPendingActivityMapping().withActorId(actorForJohn.getId()).withActivityId(executingTask.getId()).build());
        final SFlowNodeInstance notStableTask = buildAndAddNotStableTask();
        repository.add(aPendingActivityMapping().withActorId(actorForJohn.getId()).withActivityId(notStableTask.getId()).build());
        final SFlowNodeInstance terminalTask = buildAndAddTerminalTask();
        repository.add(aPendingActivityMapping().withActorId(actorForJohn.getId()).withActivityId(terminalTask.getId()).build());
        buildAndAddNormalTask("normalTask1", ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS, PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);

        // Tasks OK not assigned & pending for Bob
        final SFlowNodeInstance normalTask4 = buildAndAddNormalTask("normalTask1", ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_BOB, PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
        repository.add(aPendingActivityMapping().withActorId(actorForBob.getId()).withActivityId(normalTask4.getId()).build());

        // Tasks OK not assigned & pending for Bob, process not supervised
        final SFlowNodeInstance normalTask5 = buildAndAddNormalTask("normalTask1", ROOT_PROCESS_INSTANCE_ID_NOT_SUPERVISED, PROCESS_DEFINITION_ID_SUPERVISED_BY_JOHN);
        repository.add(aPendingActivityMapping().withActorId(actorForBob.getId()).withActivityId(normalTask5.getId()).build());
    }

    private SFlowNodeInstance buildAndAddNormalTask(final String taskName, final long rootProcessInstanceId, long processDefinitionId) {
        return repository.add(aUserTask().withName(taskName).withStateExecuting(false).withStable(true).withTerminal(false)
                .withStateId(4).withRootProcessInstanceId(rootProcessInstanceId).withProcessDefinition(processDefinitionId).build());
    }

    private SFlowNodeInstance buildAndAddExecutingTask() {
        return repository.add(aUserTask().withName("executingTask").withStateExecuting(true).withStable(true)
                .withTerminal(false).withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS).build());
    }

    private SFlowNodeInstance buildAndAddNotStableTask() {
        return repository.add(aUserTask().withName("notStableTask").withStateExecuting(false).withStable(false)
                .withTerminal(true).withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS).build());
    }

    private SFlowNodeInstance buildAndAddTerminalTask() {
        return repository.add(aUserTask().withName("terminalTask").withStateExecuting(false).withStable(true)
                .withTerminal(true).withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID_SUPERVISED_BY_JOHN_WITH_ONLY_KO_TASKS).build());
    }
}
