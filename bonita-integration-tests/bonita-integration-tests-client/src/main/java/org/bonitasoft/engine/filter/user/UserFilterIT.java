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
package org.bonitasoft.engine.filter.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.bpm.comment.Comment;
import org.bonitasoft.engine.bpm.comment.SearchCommentsDescriptor;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.check.CheckNbAssignedTaskOf;
import org.bonitasoft.engine.test.check.CheckNbPendingTaskOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 */
public class UserFilterIT extends TestWithTechnicalUser {

    private static final String JOHN = "john";

    private static final String JACK = "jack";

    private static final String JAMES = "james";

    private User john;

    private User jack;

    private User james;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        john = createUser(JOHN, "bpm");
        jack = createUser(JACK, "bpm");
        james = createUser(JAMES, "bpm");
        logoutOnTenant();
        loginOnDefaultTenantWith(JOHN, "bpm");
    }

    @Override
    @After
    public void after() throws Exception {
        deleteUser(JOHN);
        deleteUser(JACK);
        deleteUser(JAMES);
        VariableStorage.clearAll();
        super.after();
    }

    @Test
    public void filterTask() throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithUserFilter", "1.0");
        designProcessDefinition.addActor(ACTOR_NAME).addDescription("ACTOR_NAME all day and night long");
        designProcessDefinition.addAutomaticTask("step1");
        final UserTaskDefinitionBuilder addUserTask = designProcessDefinition.addUserTask("step2", ACTOR_NAME);
        addUserTask.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilter", "1.0").addInput("userId",
                new ExpressionBuilder().createConstantLongExpression(jack.getId()));
        designProcessDefinition.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployProcessWithTestFilter(designProcessDefinition, ACTOR_NAME, john, "TestFilter");

        getProcessAPI().startProcess(processDefinition.getId());

        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 1, jack).waitUntil());
        List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(1, pendingTasks.size());
        disableAndDeleteProcess(processDefinition);
    }

    /*
     * Task must be in failed state even for any exception thrown is the user filter
     */
    @Test
    public void filterTaskWithUserFilterNotFound() throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithUserFilter", "1.0");
        designProcessDefinition.addActor(ACTOR_NAME).addDescription("ACTOR_NAME all day and night long");
        designProcessDefinition.addAutomaticTask("step1");
        final UserTaskDefinitionBuilder addUserTask = designProcessDefinition.addUserTask("step2", ACTOR_NAME);
        addUserTask.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilterWithClassNotFound", "1.0").addInput("userId",
                new ExpressionBuilder().createConstantLongExpression(jack.getId()));
        designProcessDefinition.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployProcessWithTestFilter(designProcessDefinition, ACTOR_NAME, john, "TestFilterWithClassNotFound");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        waitForTaskToFail(processInstance);

        disableAndDeleteProcess(processDefinition.getId());
    }

    /*
     * Task must be in failed state even for any exception thrown is the user filter
     */
    @Test
    public void filterTaskWithUserFilterThatThrowException() throws Exception {
        final ProcessDefinitionBuilder processWithRuntime = new ProcessDefinitionBuilder().createNewInstance("processWithUserFilterRuntimeException", "1.0");
        processWithRuntime.addActor(ACTOR_NAME).addDescription("ACTOR_NAME all day and night long");
        processWithRuntime.addAutomaticTask("step1");
        final UserTaskDefinitionBuilder addUserTask = processWithRuntime.addUserTask("step2", ACTOR_NAME);
        addUserTask.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilterThatThrowException", "1.0").addInput("exception",
                new ExpressionBuilder().createConstantStringExpression("runtime"));
        processWithRuntime.addTransition("step1", "step2");
        final ProcessDefinition processDefinitionWithRuntime = deployProcessWithTestFilter(processWithRuntime, ACTOR_NAME, john,
                "TestFilterThatThrowException");
        final ProcessDefinitionBuilder processWithException = new ProcessDefinitionBuilder().createNewInstance("processWithUserFilterNormalException", "1.0");
        processWithException.addActor(ACTOR_NAME).addDescription("ACTOR_NAME all day and night long");
        processWithException.addAutomaticTask("step1");
        final UserTaskDefinitionBuilder addUserTask2 = processWithException.addUserTask("step2", ACTOR_NAME);
        addUserTask2.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilterThatThrowException", "1.0").addInput("exception",
                new ExpressionBuilder().createConstantStringExpression("normal"));
        processWithException.addTransition("step1", "step2");
        final ProcessDefinition processDefinitionWithException = deployProcessWithTestFilter(processWithException, ACTOR_NAME, john,
                "TestFilterThatThrowException");
        final ProcessDefinitionBuilder processWithNoClassDef = new ProcessDefinitionBuilder().createNewInstance("processWithUserFilterNoClassDef", "1.0");
        processWithNoClassDef.addActor(ACTOR_NAME).addDescription("ACTOR_NAME all day and night long");
        processWithNoClassDef.addAutomaticTask("step1");
        final UserTaskDefinitionBuilder addUserTask3 = processWithNoClassDef.addUserTask("step2", ACTOR_NAME);
        addUserTask3.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilterThatThrowNoClassDef", "1.0").addInput("exception",
                new ExpressionBuilder().createConstantStringExpression("normal"));
        processWithNoClassDef.addTransition("step1", "step2");
        final ProcessDefinition processDefinitionNoClassDef = deployProcessWithTestFilter(processWithNoClassDef, ACTOR_NAME, john,
                "TestFilterThatThrowNoClassDef");

        final ProcessInstance processInstanceException = getProcessAPI().startProcess(processDefinitionWithException.getId());
        final ProcessInstance processInstanceRuntime = getProcessAPI().startProcess(processDefinitionWithRuntime.getId());
        final ProcessInstance processInstanceNoClassDef = getProcessAPI().startProcess(processDefinitionNoClassDef.getId());

        waitForTaskToFail(processInstanceRuntime);
        waitForTaskToFail(processInstanceException);
        waitForTaskToFail(processInstanceNoClassDef);

        disableAndDeleteProcess(processDefinitionWithRuntime);
        disableAndDeleteProcess(processDefinitionWithException);
        disableAndDeleteProcess(processDefinitionNoClassDef);
    }

    @Test
    public void filterTaskWithAutoAssign() throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithUserFilterWithAutoAssign", "1.0");
        designProcessDefinition.addActor(ACTOR_NAME).addDescription("ACTOR_NAME all day and night long");
        designProcessDefinition.addAutomaticTask("step1");
        final UserTaskDefinitionBuilder addUserTask = designProcessDefinition.addUserTask("step2", ACTOR_NAME);
        addUserTask.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilterWithAutoAssign", "1.0").addInput("userId",
                new ExpressionBuilder().createConstantLongExpression(jack.getId()));
        addUserTask.addDisplayName(new ExpressionBuilder().createConstantStringExpression("A task to test user filter"));
        designProcessDefinition.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployProcessWithTestFilter(designProcessDefinition, ACTOR_NAME, john, "TestFilterWithAutoAssign");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        assertTrue(new CheckNbAssignedTaskOf(getProcessAPI(), 50, 5000, false, 1, jack).waitUntil());
        List<HumanTaskInstance> tasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, tasks.size());
        tasks = getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(1, tasks.size());
        final SearchResult<Comment> commentSearchResult = getProcessAPI().searchComments(
                new SearchOptionsBuilder(0, 10).filter(SearchCommentsDescriptor.PROCESS_INSTANCE_ID, processInstance.getId()).done());
        assertThat(commentSearchResult.getResult()).hasSize(1);
        assertThat(commentSearchResult.getResult().get(0).getContent()).isEqualTo("The task \"A task to test user filter\" is now assigned to jack");
        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void filterTaskWithNullInput() throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithUserFilterUsingActorName", "1.0");
        designProcessDefinition.addActor(ACTOR_NAME).addDescription("ACTOR_NAME all day and night long");
        designProcessDefinition.addAutomaticTask("step1");
        final UserTaskDefinitionBuilder addUserTask = designProcessDefinition.addUserTask("step2", ACTOR_NAME);
        addUserTask.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilterUsingActorName", "1.0").addInput("userIds", null);
        designProcessDefinition.addTransition("step1", "step2");
        deployProcessWithTestFilter(designProcessDefinition, ACTOR_NAME, john, "TestFilterUsingActorName");
    }

    @Test
    public void filterTaskUsingFilterName() throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("processWithUserFilterUsingActorName", "1.0");
        designProcessDefinition.addActor(ACTOR_NAME).addDescription("ACTOR_NAME all day and night long");
        designProcessDefinition.addAutomaticTask("step1");
        final UserTaskDefinitionBuilder addUserTask = designProcessDefinition.addUserTask("step2", ACTOR_NAME);
        addUserTask.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilterUsingActorName", "1.0").addInput(
                "userIds",
                new ExpressionBuilder().createGroovyScriptExpression("myScript", "['" + ACTOR_NAME + "':" + james.getId() + "l,'notACTOR_NAME':" + jack.getId()
                        + "l]", Map.class.getName()));
        designProcessDefinition.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployProcessWithTestFilter(designProcessDefinition, ACTOR_NAME, john, "TestFilterUsingActorName");

        getProcessAPI().startProcess(processDefinition.getId());

        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 6000, false, 1, james).waitUntil());
        List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(james.getId(), 0, 10, null);
        assertEquals(1, pendingTasks.size());
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1645", classes = { HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "update user filters" })
    @Test
    public void updateUserFilterAfterAUserDeletion() throws Exception {
        final Group group = createGroup("group1");
        final Role role = createRole("role1");
        createUserMembership(jack.getUserName(), "role1", "group1");

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("processWithUserFilterWithAutoAssign", "1.0");
        processBuilder.addActor(ACTOR_NAME);
        processBuilder.addAutomaticTask("step1");
        final UserTaskDefinitionBuilder addUserTask = processBuilder.addUserTask("step2", ACTOR_NAME);
        addUserTask.addUserFilter("test", "org.bonitasoft.engine.filter.user.GroupUserFilter", "1.0").addInput("groupId",
                new ExpressionBuilder().createConstantLongExpression(group.getId()));
        processBuilder.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployProcessWithTestFilter(processBuilder, ACTOR_NAME, john, "GroupUserFilter");
        getProcessAPI().startProcess(processDefinition.getId());

        assertTrue(new CheckNbAssignedTaskOf(getProcessAPI(), 50, 5000, false, 1, jack).waitUntil());
        List<HumanTaskInstance> tasks = getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(1, tasks.size());
        getIdentityAPI().deleteUser(jack.getId());
        createUserMembership(john.getUserName(), "role1", "group1");
        tasks = getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(1, tasks.size());
        getProcessAPI().updateActorsOfUserTask(tasks.get(0).getId());
        tasks = getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(0, tasks.size());
        tasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, tasks.size());

        deleteGroups(group);
        deleteRoles(role);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1645", classes = { HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "update user filters" })
    @Test(expected = UpdateException.class)
    public void unableToUpdateActorsOnAGateway() throws Exception {
        final Expression scriptExpression = new ExpressionBuilder().createGroovyScriptExpression("mycondition", "fzdfsdfsdfsdfsdf", Boolean.class.getName());
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
                .createNewInstance("My_Process_with_exclusive_gateway", PROCESS_VERSION).addActor(ACTOR_NAME)
                .addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addGateway("gateway1", GatewayType.EXCLUSIVE)
                .addTransition("step1", "gateway1").addTransition("gateway1", "step2", scriptExpression).addDefaultTransition("gateway1", "step3").getProcess();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, john);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        final FlowNodeInstance failFlowNodeInstance = waitForFlowNodeInFailedState(processInstance);
        assertEquals("gateway1", failFlowNodeInstance.getName());
        try {
            getProcessAPI().updateActorsOfUserTask(failFlowNodeInstance.getId());
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Cover(jira = "ENGINE-1645", classes = { HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "update user filters" })
    @Test
    public void doNotUpateAHumanTaskIfNoUserFilterIsDefined() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("doNotUpateAHumanTaskIfNoUserFilterIsDefined", "1.0");
        processBuilder.addActor(ACTOR_NAME);
        processBuilder.addAutomaticTask("step1");
        processBuilder.addUserTask("step2", ACTOR_NAME);
        processBuilder.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, john);
        getProcessAPI().startProcess(processDefinition.getId());

        waitForUserTask("step2");
        List<HumanTaskInstance> tasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, ActivityInstanceCriterion.DEFAULT);
        assertEquals(1, tasks.size());
        final HumanTaskInstance taskBefore = tasks.get(0);
        getProcessAPI().updateActorsOfUserTask(tasks.get(0).getId());
        tasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, ActivityInstanceCriterion.DEFAULT);
        assertEquals(1, tasks.size());
        assertEquals(taskBefore, tasks.get(0));

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1645", classes = { HumanTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, keywords = { "update user filters" })
    @Test
    public void updateUserFilterAfterAUserAdd() throws Exception {
        final Group group = createGroup("group1");
        final Role role = createRole("role1");
        createUserMembership(jack.getUserName(), "role1", "group1");

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("updateUserFilterAfterAUserAdd", "1.0");
        processBuilder.addActor(ACTOR_NAME);
        processBuilder.addAutomaticTask("step1");
        final UserTaskDefinitionBuilder addUserTask = processBuilder.addUserTask("step2", ACTOR_NAME);
        addUserTask.addUserFilter("test", "org.bonitasoft.engine.filter.user.GroupUserFilter", "1.0").addInput("groupId",
                new ExpressionBuilder().createConstantLongExpression(group.getId()));
        processBuilder.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployProcessWithTestFilter(processBuilder, ACTOR_NAME, john, "GroupUserFilter");
        getProcessAPI().startProcess(processDefinition.getId());

        assertTrue(new CheckNbAssignedTaskOf(getProcessAPI(), 50, 5000, false, 1, jack).waitUntil());
        List<HumanTaskInstance> tasks = getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(1, tasks.size());
        createUserMembership(john.getUserName(), "role1", "group1");
        tasks = getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(1, tasks.size());
        getProcessAPI().updateActorsOfUserTask(tasks.get(0).getId());

        tasks = getProcessAPI().getPendingHumanTaskInstances(jack.getId(), 0, 10, ActivityInstanceCriterion.DEFAULT);
        assertEquals(1, tasks.size());
        assertEquals(0, tasks.get(0).getAssigneeId());
        tasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, ActivityInstanceCriterion.DEFAULT);
        assertEquals(1, tasks.size());
        assertEquals(0, tasks.get(0).getAssigneeId());

        deleteGroups(group);
        deleteRoles(role);
        disableAndDeleteProcess(processDefinition);
    }

}
