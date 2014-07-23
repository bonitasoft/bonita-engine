/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
import org.bonitasoft.engine.core.process.instance.model.impl.SHiddenTaskInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.impl.SUserTaskInstanceImpl;
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

    private static final long PROCESS_DEFINITION_ID = 12L;

    private static final String PROCESS_NAME = "process name";

    private static final String PROCESS_VERSION = "version";

    private static final long ROOT_PROCESS_INSTANCE_ID = 4548L;

    private static final long ANOTHER_ROOT_PROCESS_INSTANCE_ID = 16858L;

    private static final long USER_ID = 654L;

    private static final long GROUP_ID = 9875L;

    private static final long ROLE_ID = 1235L;

    @Inject
    private ProcessDeploymentInfoRepository repository;

    @Before
    public void before() {
        repository.add(aUser().withId(USER_ID).build());

        // Create process definition
        buildAndCreateProcessDefinition(6L, PROCESS_DEFINITION_ID);
        final int anotherProcessDefinitionId = 36;
        buildAndCreateProcessDefinition(8L, anotherProcessDefinitionId);

        // Create process instance
        repository.add(aProcessInstance().withProcessDefinitionId(PROCESS_DEFINITION_ID).withName(PROCESS_NAME)
                .withId(ROOT_PROCESS_INSTANCE_ID).build());
        repository.add(aProcessInstance().withProcessDefinitionId(anotherProcessDefinitionId).withName(PROCESS_NAME)
                .withId(ANOTHER_ROOT_PROCESS_INSTANCE_ID).build());
    }

    protected void buildAndCreateProcessDefinition(final long id, long processDefinitionId) {
        final SProcessDefinitionDeployInfoImpl sProcessDefinitionDeployInfoImpl = new SProcessDefinitionDeployInfoImpl();
        sProcessDefinitionDeployInfoImpl.setId(id);
        sProcessDefinitionDeployInfoImpl.setName(PROCESS_NAME);
        sProcessDefinitionDeployInfoImpl.setVersion(PROCESS_VERSION);
        sProcessDefinitionDeployInfoImpl.setProcessId(processDefinitionId);
        sProcessDefinitionDeployInfoImpl.setTenantId(1L);
        repository.add(sProcessDefinitionDeployInfoImpl);
    }

    @Test
    public void getNumberOfProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser_should_return_number_of_process_definition_if_one_instance_has_assigned_tasks_to_the_user() {
        // Given
        repository.add(aUserTask().withName("normalTask1").withStateExecuting(false).withStable(true).withTerminal(false).withDeleted(false)
                .withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).withAssigneeId(USER_ID).build());
        repository.add(aUserTask().withName("normalTask2").withStateExecuting(false).withStable(true).withTerminal(false).withDeleted(false)
                .withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).withAssigneeId(USER_ID).build());
        repository.add(aUserTask().withName("deletedTask").withStateExecuting(false).withStable(true).withTerminal(false).withDeleted(true)
                .withRootProcessInstanceId(ANOTHER_ROOT_PROCESS_INSTANCE_ID).withAssigneeId(USER_ID).build());
        repository.add(aUserTask().withName("executingTask").withStateExecuting(true).withStable(true).withTerminal(false).withDeleted(false)
                .withRootProcessInstanceId(ANOTHER_ROOT_PROCESS_INSTANCE_ID).withAssigneeId(USER_ID).build());
        repository.add(aUserTask().withName("notStableTask").withStateExecuting(false).withStable(false).withTerminal(true).withDeleted(false)
                .withRootProcessInstanceId(ANOTHER_ROOT_PROCESS_INSTANCE_ID).withAssigneeId(USER_ID).build());
        repository.add(aUserTask().withName("terminalTask").withStateExecuting(false).withStable(true).withTerminal(true).withDeleted(false)
                .withRootProcessInstanceId(ANOTHER_ROOT_PROCESS_INSTANCE_ID).withAssigneeId(USER_ID).build());

        // When
        final long numberOfProcessDefinitionDeployInfos = repository.getNumberOfProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser(USER_ID);

        // Then
        assertThat(numberOfProcessDefinitionDeployInfos).isEqualTo(1);
    }

    @Test
    public void getNumberOfProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser_should_return_number_of_process_definition_if_one_instance_has_pending_tasks_for_the_user() {
        // Given
        final SFlowNodeInstance normalTask = repository.add(aUserTask().withName("normalTask1").withStateExecuting(false).withStable(true).withTerminal(false)
                .withDeleted(false).withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).build());
        buildAndCreatePendingActivityMapping(normalTask);

        final SFlowNodeInstance deletedTask = repository.add(aUserTask().withName("deletedTask").withStateExecuting(false).withStable(true).withTerminal(false)
                .withDeleted(true).withRootProcessInstanceId(ANOTHER_ROOT_PROCESS_INSTANCE_ID).build());
        buildAndCreatePendingActivityMapping(deletedTask);

        final SFlowNodeInstance executingTask = repository.add(aUserTask().withName("executingTask").withStateExecuting(true).withStable(true)
                .withTerminal(false).withDeleted(false).withRootProcessInstanceId(ANOTHER_ROOT_PROCESS_INSTANCE_ID).build());
        buildAndCreatePendingActivityMapping(executingTask);

        final SFlowNodeInstance notStableTask = repository.add(aUserTask().withName("notStableTask").withStateExecuting(false).withStable(false)
                .withTerminal(true).withDeleted(false).withRootProcessInstanceId(ANOTHER_ROOT_PROCESS_INSTANCE_ID).build());
        buildAndCreatePendingActivityMapping(notStableTask);

        final SFlowNodeInstance terminalTask = repository.add(aUserTask().withName("terminalTask").withStateExecuting(false).withStable(true)
                .withTerminal(true).withDeleted(false).withRootProcessInstanceId(ANOTHER_ROOT_PROCESS_INSTANCE_ID).build());
        buildAndCreatePendingActivityMapping(terminalTask);

        // When
        long numberOfUsers = repository.getNumberOfProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser(USER_ID);

        // Then
        assertThat(numberOfUsers).isEqualTo(1);
    }

    private void buildAndCreatePendingActivityMapping(final SFlowNodeInstance normalTask) {
        repository.add(aPendingActivityMapping().withUserId(USER_ID).withActivityId(normalTask.getId()).build());
    }

    @Test
    public void getNumberOfProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser_should_return_number_of_process_definition_if_one_instance_has_pending_tasks_for_a_actor_with_the_user_as_member() {
        // Given
        final SActor actor = repository.add(anActor().build());
        repository.add(anActorMember().forActor(actor).withUserId(USER_ID).build());

        final SFlowNodeInstance normalTask = repository.add(aUserTask().withName("normalTask1").withStateExecuting(false).withStable(true).withTerminal(false)
                .withDeleted(false).withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).build());
        repository.add(aPendingActivityMapping().withActorId(actor.getId()).withActivityId(normalTask.getId()).build());

        // When
        long numberOfUsers = repository.getNumberOfProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser(USER_ID);

        // Then
        assertThat(numberOfUsers).isEqualTo(1);
    }

    @Test
    public void getNumberOfProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser_should_return_number_of_process_definition_if_one_instance_has_pending_tasks_for_a_actor_with_a_membership_mapped_to_the_user_as_member() {
        // Given
        final SActor actor = repository.add(anActor().build());
        repository.add(anActorMember().forActor(actor).withGroupId(GROUP_ID).build());
        repository.add(aUserMembership().forUser(USER_ID).memberOf(GROUP_ID, ROLE_ID).build());

        final SFlowNodeInstance normalTask = repository.add(aUserTask().withName("normalTask1").withStateExecuting(false).withStable(true).withTerminal(false)
                .withDeleted(false).withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).build());
        repository.add(aPendingActivityMapping().withActorId(actor.getId()).withActivityId(normalTask.getId()).build());

        // When
        long numberOfUsers = repository.getNumberOfProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser(USER_ID);

        // Then
        assertThat(numberOfUsers).isEqualTo(1);
    }

    @Test
    public void getNumberOfProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser_should_not_count_the_hidden_human() {
        // Given
        final SUserTaskInstanceImpl activity = aUserTask().withName("normalTask1").withStateExecuting(false).withStable(true).withTerminal(false)
                .withDeleted(false).withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).withAssigneeId(USER_ID).build();
        repository.add(activity);

        final SHiddenTaskInstanceImpl sHiddenTaskInstanceImpl = new SHiddenTaskInstanceImpl();
        sHiddenTaskInstanceImpl.setActivityId(activity.getId());
        sHiddenTaskInstanceImpl.setUserId(USER_ID);
        sHiddenTaskInstanceImpl.setTenantId(1L);
        sHiddenTaskInstanceImpl.setId(48L);
        repository.add(sHiddenTaskInstanceImpl);

        // When
        final long numberOfProcessDefinitionDeployInfos = repository.getNumberOfProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser(USER_ID);

        // Then
        assertThat(numberOfProcessDefinitionDeployInfos).isEqualTo(0);
    }

    @Test
    public void searchProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser_should_return_number_of_process_definition_if_one_instance_has_assigned_tasks_to_the_user() {
        // Given
        repository.add(aUserTask().withName("normalTask").withStateExecuting(false).withStable(true).withTerminal(false)
                .withDeleted(false).withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).withAssigneeId(USER_ID).build());
        repository.add(aUserTask().withName("normalTask2").withStateExecuting(false).withStable(true).withTerminal(false).withDeleted(false)
                .withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).withAssigneeId(USER_ID).build());
        repository.add(aUserTask().withName("deletedTask").withStateExecuting(false).withStable(true).withTerminal(false).withDeleted(true)
                .withRootProcessInstanceId(ANOTHER_ROOT_PROCESS_INSTANCE_ID).withAssigneeId(USER_ID).build());
        repository.add(aUserTask().withName("executingTask").withStateExecuting(true).withStable(true).withTerminal(false).withDeleted(false)
                .withRootProcessInstanceId(ANOTHER_ROOT_PROCESS_INSTANCE_ID).withAssigneeId(USER_ID).build());
        repository.add(aUserTask().withName("notStableTask").withStateExecuting(false).withStable(false).withTerminal(true).withDeleted(false)
                .withRootProcessInstanceId(ANOTHER_ROOT_PROCESS_INSTANCE_ID).withAssigneeId(USER_ID).build());
        repository.add(aUserTask().withName("terminalTask").withStateExecuting(false).withStable(true).withTerminal(true).withDeleted(false)
                .withRootProcessInstanceId(ANOTHER_ROOT_PROCESS_INSTANCE_ID).withAssigneeId(USER_ID).build());

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser(USER_ID);

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(1);
        assertThat(sProcessDefinitionDeployInfos.get(0).getProcessId()).isEqualTo(PROCESS_DEFINITION_ID);
    }

    @Test
    public void searchProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser_should_return_number_of_process_definition_if_one_instance_has_pending_tasks_for_the_user() {
        // Given
        final SFlowNodeInstance normalTask = repository.add(aUserTask().withName("normalTask1").withStateExecuting(false).withStable(true).withTerminal(false)
                .withDeleted(false).withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).build());
        buildAndCreatePendingActivityMapping(normalTask);

        final SFlowNodeInstance deletedTask = repository.add(aUserTask().withName("deletedTask").withStateExecuting(false).withStable(true).withTerminal(false)
                .withDeleted(true).withRootProcessInstanceId(ANOTHER_ROOT_PROCESS_INSTANCE_ID).build());
        buildAndCreatePendingActivityMapping(deletedTask);

        final SFlowNodeInstance executingTask = repository.add(aUserTask().withName("executingTask").withStateExecuting(true).withStable(true)
                .withTerminal(false).withDeleted(false).withRootProcessInstanceId(ANOTHER_ROOT_PROCESS_INSTANCE_ID).build());
        buildAndCreatePendingActivityMapping(executingTask);

        final SFlowNodeInstance notStableTask = repository.add(aUserTask().withName("notStableTask").withStateExecuting(false).withStable(false)
                .withTerminal(true).withDeleted(false).withRootProcessInstanceId(ANOTHER_ROOT_PROCESS_INSTANCE_ID).build());
        buildAndCreatePendingActivityMapping(notStableTask);

        final SFlowNodeInstance terminalTask = repository.add(aUserTask().withName("terminalTask").withStateExecuting(false).withStable(true)
                .withTerminal(true).withDeleted(false).withRootProcessInstanceId(ANOTHER_ROOT_PROCESS_INSTANCE_ID).build());
        buildAndCreatePendingActivityMapping(terminalTask);

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser(USER_ID);

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(1);
        assertThat(sProcessDefinitionDeployInfos.get(0).getProcessId()).isEqualTo(PROCESS_DEFINITION_ID);
    }

    @Test
    public void searchProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser_should_return_number_of_process_definition_if_one_instance_has_pending_tasks_for_a_actor_with_the_user_as_member() {
        // Given
        final SActor actor = repository.add(anActor().build());
        repository.add(anActorMember().forActor(actor).withUserId(USER_ID).build());

        final SFlowNodeInstance normalTask = repository.add(aUserTask().withName("normalTask1").withStateExecuting(false).withStable(true).withTerminal(false)
                .withDeleted(false).withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).build());
        repository.add(aPendingActivityMapping().withActorId(actor.getId()).withActivityId(normalTask.getId()).build());

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser(USER_ID);

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(1);
        assertThat(sProcessDefinitionDeployInfos.get(0).getProcessId()).isEqualTo(PROCESS_DEFINITION_ID);
    }

    @Test
    public void searchProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser_should_return_number_of_process_definition_if_one_instance_has_pending_tasks_for_a_actor_with_a_membership_mapped_to_the_user_as_member() {
        // Given
        final SActor actor = repository.add(anActor().build());
        repository.add(anActorMember().forActor(actor).withGroupId(GROUP_ID).build());
        repository.add(aUserMembership().forUser(USER_ID).memberOf(GROUP_ID, ROLE_ID).build());

        final SFlowNodeInstance normalTask = repository.add(aUserTask().withName("normalTask1").withStateExecuting(false).withStable(true).withTerminal(false)
                .withDeleted(false).withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).build());
        repository.add(aPendingActivityMapping().withActorId(actor.getId()).withActivityId(normalTask.getId()).build());

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser(USER_ID);

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(1);
        assertThat(sProcessDefinitionDeployInfos.get(0).getProcessId()).isEqualTo(PROCESS_DEFINITION_ID);
    }

    @Test
    public void searchProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser_should_not_count_the_hidden_human() {
        // Given
        final SUserTaskInstanceImpl activity = aUserTask().withName("normalTask1").withStateExecuting(false).withStable(true).withTerminal(false)
                .withDeleted(false).withRootProcessInstanceId(ROOT_PROCESS_INSTANCE_ID).withAssigneeId(USER_ID).build();
        repository.add(activity);

        final SHiddenTaskInstanceImpl sHiddenTaskInstanceImpl = new SHiddenTaskInstanceImpl();
        sHiddenTaskInstanceImpl.setActivityId(activity.getId());
        sHiddenTaskInstanceImpl.setUserId(USER_ID);
        sHiddenTaskInstanceImpl.setTenantId(1L);
        sHiddenTaskInstanceImpl.setId(48L);
        repository.add(sHiddenTaskInstanceImpl);

        // When
        final List<SProcessDefinitionDeployInfo> sProcessDefinitionDeployInfos = repository
                .searchProcessDefinitionDeployInfoWithAssignedOrPendingHumanTasksForUser(USER_ID);

        // Then
        assertThat(sProcessDefinitionDeployInfos.size()).isEqualTo(0);
    }
}
