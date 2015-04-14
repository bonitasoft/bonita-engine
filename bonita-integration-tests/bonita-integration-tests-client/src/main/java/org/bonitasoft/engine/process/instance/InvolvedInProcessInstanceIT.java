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
package org.bonitasoft.engine.process.instance;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.actor.ActorMember;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCreator;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.junit.Test;

/**
 * @author Emmanuel Duchastenier
 */
public class InvolvedInProcessInstanceIT extends AbstractProcessInstanceIT {

    @Test
    public void isInvolvedInProcessInstanceAndTheirManager() throws Exception {
        //given
        //user and manager
        final User managerOfJim = createUser(new UserCreator("managerOfJim", "bpm"));
        // Jim starts the process instance:
        final User jim = createUser(new UserCreator("jim", "bpm").setManagerUserId(managerOfJim.getId()));

        final User managerOfJohn = createUser(new UserCreator("managerOfJohn", "bpm"));
        // john is mapped as direct user:
        final User john = createUser(new UserCreator("john", "bpm").setManagerUserId(managerOfJohn.getId()));

        final User managerOfJack = createUser(new UserCreator("managerOfJack", "bpm"));
        // Jack is mapped through his group:
        final User jack = createUser(new UserCreator("jack", "bpm").setManagerUserId(managerOfJack.getId()));
        final Group jackGroup = createGroup("jackGroup", null);
        final Role jackRole = createRole("jackRole");
        getIdentityAPI().addUserMembership(jack.getId(), jackGroup.getId(), jackRole.getId());

        final User managerOfJames = createUser(new UserCreator("managerOfJames", "bpm"));
        // James is mapped through his role:
        final User james = createUser(new UserCreator("james", "bpm").setManagerUserId(managerOfJames.getId()));
        final Group jamesGroup = createGroup("jamesGroup", null);
        final Role jamesRole = createRole("jamesRole");
        getIdentityAPI().addUserMembership(james.getId(), jamesGroup.getId(), jamesRole.getId());

        final User managerOfToto = createUser(new UserCreator("managerOfToto", "bpm"));
        // Toto is mapped through his group + role (=membership):
        final User toto = createUser(new UserCreator("toto", "bpm").setManagerUserId(managerOfToto.getId()));
        final Group totoGroup = createGroup("totoGroup", null);
        final Role totoRole = createRole("totoRole");
        getIdentityAPI().addUserMembership(toto.getId(), totoGroup.getId(), totoRole.getId());

        final ProcessDefinitionBuilder processBuilder1 = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder1.addActor(ACTOR_NAME);

        // 1 instance of process def:
        final DesignProcessDefinition designProcessDefinition = processBuilder1.addUserTask("step1", ACTOR_NAME).addUserTask("step2", ACTOR_NAME)
                .addTransition("step1", "step2").getProcess();
        final BusinessArchive businessArchive1 = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = deployProcess(businessArchive1);

        // map user, group, role, and membership to that actor
        final ActorInstance actor = getProcessAPI().getActors(processDefinition.getId(), 0, 1, ActorCriterion.NAME_ASC).get(0);
        final ActorMember jimActorMember = getProcessAPI().addUserToActor(actor.getId(), jim.getId());
        final ActorMember johnActorMember = getProcessAPI().addUserToActor(actor.getId(), john.getId());
        final ActorMember jackActorMember = getProcessAPI().addGroupToActor(actor.getId(), jackGroup.getId());
        final ActorMember jamesActorMember = getProcessAPI().addRoleToActor(actor.getId(), jamesRole.getId());
        getProcessAPI().addRoleAndGroupToActor(actor.getId(), totoRole.getId(), totoGroup.getId());

        logoutThenloginAs(jim.getUserName(), "bpm");
        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long processInstanceId = processInstance.getId();
        getProcessAPI().removeActorMember(jimActorMember.getId());

        // then
        assertThat(getProcessAPI().isInvolvedInProcessInstance(john.getId(), processInstanceId)).as("directly mapped user should be involved").isTrue();
        assertThat(getProcessAPI().isInvolvedInProcessInstance(managerOfJohn.getId(), processInstanceId)).as(
                "manager of directly mapped user should not be involved").isFalse();
        assertThat(getProcessAPI().isManagerOfUserInvolvedInProcessInstance(john.getId(), processInstanceId)).as("directly mapped user should be involved")
                .isFalse();
        assertThat(getProcessAPI().isManagerOfUserInvolvedInProcessInstance(managerOfJohn.getId(), processInstanceId)).as(
                "manager of directly mapped user should be involved").isTrue();

        assertThat(getProcessAPI().isInvolvedInProcessInstance(jack.getId(), processInstanceId)).as("user mapped using group should be involved")
                .isTrue();
        assertThat(getProcessAPI().isInvolvedInProcessInstance(managerOfJack.getId(), processInstanceId)).as(
                "manager of user mapped using group should not be involved").isFalse();
        assertThat(getProcessAPI().isManagerOfUserInvolvedInProcessInstance(jack.getId(), processInstanceId)).as("user mapped using group should be involved")
                .isFalse();
        assertThat(getProcessAPI().isManagerOfUserInvolvedInProcessInstance(managerOfJack.getId(), processInstanceId)).as(
                "manager of user mapped using group should be involved").isTrue();

        assertThat(getProcessAPI().isInvolvedInProcessInstance(james.getId(), processInstanceId)).as("user mapped using role should be involved")
                .isTrue();
        assertThat(getProcessAPI().isInvolvedInProcessInstance(managerOfJames.getId(), processInstanceId)).as(
                "manager of user mapped using role should not be involved").isFalse();
        assertThat(getProcessAPI().isManagerOfUserInvolvedInProcessInstance(james.getId(), processInstanceId)).as("user mapped using role should be involved")
                .isFalse();
        assertThat(getProcessAPI().isManagerOfUserInvolvedInProcessInstance(managerOfJames.getId(), processInstanceId)).as(
                "manager of user mapped using role should be involved").isTrue();

        assertThat(getProcessAPI().isInvolvedInProcessInstance(toto.getId(), processInstanceId)).as("user mapped using membership should be involved")
                .isTrue();
        assertThat(getProcessAPI().isInvolvedInProcessInstance(managerOfToto.getId(), processInstanceId)).as(
                "manager of user mapped using membership should not be involved").isFalse();
        assertThat(getProcessAPI().isManagerOfUserInvolvedInProcessInstance(toto.getId(), processInstanceId)).as(
                "user mapped using membership should be involved").isFalse();
        assertThat(getProcessAPI().isManagerOfUserInvolvedInProcessInstance(managerOfToto.getId(), processInstanceId)).as(
                "manager of user mapped using membership should be involved").isTrue();

        assertThat(getProcessAPI().isInvolvedInProcessInstance(user.getId(), processInstanceId)).as("not mapped user should not be involved").isFalse();
        assertThat(getProcessAPI().isManagerOfUserInvolvedInProcessInstance(user.getId(), processInstanceId))
                .as("not mapped user should not be involved").isFalse();

        assertThat(getProcessAPI().isInvolvedInProcessInstance(jim.getId(), processInstanceId)).as("the process instance initiator should be involved")
                .isTrue();
        assertThat(getProcessAPI().isManagerOfUserInvolvedInProcessInstance(jim.getId(), processInstanceId)).as(
                "the process instance initiator should not be involved (as a manager)").isFalse();
        assertThat(getProcessAPI().isInvolvedInProcessInstance(managerOfJim.getId(), processInstanceId)).as(
                "the manager of the process instance initiator should not be involved").isFalse();
        assertThat(getProcessAPI().isManagerOfUserInvolvedInProcessInstance(managerOfJim.getId(), processInstanceId)).as(
                "the manager of process instance initiator should be involved (as a manager)").isTrue();

        getProcessAPI().removeActorMember(johnActorMember.getId());
        logoutThenloginAs(john.getUserName(), "bpm");
        final HumanTaskInstance step1Instance = waitForUserTaskAndAssigneIt(processInstance, "step1", john);

        assertThat(getProcessAPI().isInvolvedInProcessInstance(john.getId(), processInstanceId)).as("assigned user should be involved").isTrue();

        assignAndExecuteStep(step1Instance, john);
        waitForActivityInCompletedState(processInstance, "step1", false);
        assertThat(getProcessAPI().isManagerOfUserInvolvedInProcessInstance(managerOfJohn.getId(), processInstanceId)).as(
                "the manager of the task executor should be involved").isTrue();

        getProcessAPI().removeActorMember(jamesActorMember.getId());
        logoutThenloginAs(james.getUserName(), "bpm");
        final HumanTaskInstance step2Instance = waitForUserTaskAndAssigneIt(processInstance, "step2", james);
        assertThat(getProcessAPI().isManagerOfUserInvolvedInProcessInstance(managerOfJames.getId(), processInstanceId)).as(
                "the manager of a user assigned to a task should be involved").isTrue();

        getProcessAPI().removeActorMember(jackActorMember.getId());
        logoutThenloginAs(jack.getUserName(), "bpm");
        assignAndExecuteStep(step2Instance, jack);
        waitForProcessToFinish(processInstanceId);
        assertThat(getProcessAPI().isInvolvedInProcessInstance(jack.getId(), processInstanceId)).as(
                "a user executer of a task in an archived process instance should be involved").isTrue();
        assertThat(getProcessAPI().isInvolvedInProcessInstance(managerOfJack.getId(), processInstanceId)).as(
                "a manager of a user executer of a task in an archived process instance should not be involved").isFalse();
        assertThat(getProcessAPI().isManagerOfUserInvolvedInProcessInstance(jack.getId(), processInstanceId)).as(
                "a user executer of a task in an archived process instance should not be involved (as a manager)").isFalse();
        assertThat(getProcessAPI().isManagerOfUserInvolvedInProcessInstance(managerOfJack.getId(), processInstanceId)).as(
                "a manager of a user executer of a task in an archived process instance should be involved (as a manager)").isTrue();

        assertThat(getProcessAPI().isInvolvedInProcessInstance(jim.getId(), processInstanceId)).as(
                "a user initiator of an archived process instance should be involved").isTrue();
        assertThat(getProcessAPI().isInvolvedInProcessInstance(managerOfJim.getId(), processInstanceId)).as(
                "a manager of a user initiator of an archived process instance should not be involved").isFalse();
        assertThat(getProcessAPI().isManagerOfUserInvolvedInProcessInstance(jim.getId(), processInstanceId)).as(
                "a user initiator of an archived process instance should not be involved (as a manager)").isFalse();
        assertThat(getProcessAPI().isManagerOfUserInvolvedInProcessInstance(managerOfJim.getId(), processInstanceId)).as(
                "a manager of a user initiator of an archived process instance should be involved (as a manager)").isTrue();

        //clean
        deleteUsers(john, jack, james, toto, managerOfJohn, managerOfJames, managerOfJack, managerOfToto, jim, managerOfJim);
        deleteGroups(jackGroup, jamesGroup, totoGroup);
        deleteRoles(jackRole, jamesRole, totoRole);

        disableAndDeleteProcess(processDefinition);
    }


    @Test
    public void isInvolvedInUserTask() throws Exception {
        //given
        // john is mapped as direct user:
        final User john = createUser(new UserCreator("john", "bpm"));

        final ProcessDefinitionBuilder processBuilder1 = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder1.addActor(ACTOR_NAME);

        // 1 instance of process def:
        final DesignProcessDefinition designProcessDefinition = processBuilder1.addUserTask("step1", ACTOR_NAME).addUserTask("step2", ACTOR_NAME)
                .addTransition("step1", "step2").getProcess();
        final BusinessArchive businessArchive1 = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = deployProcess(businessArchive1);

        // map user, group, role, and membership to that actor
        final ActorInstance actor = getProcessAPI().getActors(processDefinition.getId(), 0, 1, ActorCriterion.NAME_ASC).get(0);
        getProcessAPI().addUserToActor(actor.getId(), john.getId());
        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long processInstanceId = processInstance.getId();
        long step1 = waitForUserTask(processInstanceId, "step1");

        // then
        assertThat(getProcessAPI().isInvolvedInHumanTaskInstance(john.getId(), step1)).as("directly mapped user should be involved").isTrue();

        //clean
        deleteUsers(john);

        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = ProcessInstanceNotFoundException.class)
    public void isInvolvedInProcessInstanceWithProcessInstanceNotFoundException() throws Exception {
        getProcessAPI().isInvolvedInProcessInstance(user.getId(), 0);
    }

    @Test
    public void isInvolvedInProcessInstanceWithInvalidUser() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithActorAndThreeHumanStepsAndThreeTransition();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "step2");
        try {
            assertThat(getProcessAPI().isInvolvedInProcessInstance(999999999999L, processInstance.getId())).isFalse();
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

}
