/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.supervisor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.category.Category;
import org.bonitasoft.engine.bpm.comment.SearchCommentsDescriptor;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedUserTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;
import org.bonitasoft.engine.bpm.flownode.UserTaskInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.RoleCreator;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProcessSupervisedTest extends CommonAPITest {

    private static final String SEARCH_S_COMMENT_SUPERVISED_BY = "searchSCommentSupervisedBy";

    private static final String SUPERVISOR_ID_CMD_KEY = "supervisorId";

    private User john;

    private User matti;

    private UserMembership membership;

    private Role role;

    private Group group;

    private ProcessDefinition definition;

    private List<ProcessDefinition> definitions;

    private ProcessSupervisor supervisorForUser;

    private ProcessSupervisor supervisorForRole;

    private ProcessSupervisor supervisorForGroup;

    private List<ProcessSupervisor> supervisors;

    @Before
    public void before() throws Exception {
        login();

        john = createUser("john", "bpm");
        matti = createUser("matti", "bpm");

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor(ACTOR_NAME).addDescription(DESCRIPTION).addUserTask("userTask1", ACTOR_NAME);
        processBuilder.addShortTextData("Application", null);
        definition = deployAndEnableWithActor(processBuilder.done(), ACTOR_NAME, john);
        definitions = new ArrayList<ProcessDefinition>();
        definitions.add(definition);

        // Three tasks
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(definition.getId());
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(definition.getId());
        final ProcessInstance processInstance3 = getProcessAPI().startProcess(definition.getId());
        waitForUserTaskAndAssigneIt("userTask1", processInstance1, john);
        waitForUserTaskAndAssigneIt("userTask1", processInstance2, john);
        waitForUserTask("userTask1", processInstance3);

        supervisorForUser = getProcessAPI().createProcessSupervisorForUser(definition.getId(), matti.getId());
        assertEquals(definition.getId(), supervisorForUser.getProcessDefinitionId());

        // add supervisor by role
        role = getIdentityAPI().createRole(new RoleCreator("developer"));
        supervisorForRole = getProcessAPI().createProcessSupervisorForRole(definition.getId(), role.getId());

        // add supervisor group
        group = getIdentityAPI().createGroup("R&D", null);
        supervisorForGroup = getProcessAPI().createProcessSupervisorForGroup(definition.getId(), group.getId());

        // add supervisor membership
        membership = getIdentityAPI().addUserMembership(john.getId(), group.getId(), role.getId());

        supervisors = new ArrayList<ProcessSupervisor>();
        supervisors.add(supervisorForGroup);
        supervisors.add(supervisorForRole);
        supervisors.add(supervisorForUser);
    }

    @After
    public void after() throws BonitaException, BonitaHomeNotSetException {
        deleteSupervisors(supervisors);
        disableAndDeleteProcess(definitions);
        getIdentityAPI().deleteUserMembership(membership.getId());
        getIdentityAPI().deleteUser(john.getId());
        getIdentityAPI().deleteUser(matti.getId());
        getIdentityAPI().deleteGroup(group.getId());
        getIdentityAPI().deleteRole(role.getId());
        logout();
    }

    @Test
    public void superviseMyAssignedTasks() throws Exception {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.DESC);
        builder.filter("state", "ready");
        builder.filter("name", "userTask1");
        builder.filter("priority", TaskPriority.NORMAL);

        final SearchResult<HumanTaskInstance> searchResult = getProcessAPI().searchAssignedTasksSupervisedBy(matti.getId(), builder.done());
        assertEquals(2, searchResult.getResult().size());
        final UserTaskInstance taskInstance = (UserTaskInstance) searchResult.getResult().get(0);
        assertEquals("userTask1", taskInstance.getName());
        assertEquals(john.getId(), taskInstance.getAssigneeId());
    }

    @Test
    public void superviseMyArchivedTask() throws Exception {
        final List<HumanTaskInstance> instanceList = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        HumanTaskInstance humanTaskInstance = instanceList.get(0);
        // one archive tasks
        final long activityInstanceId = humanTaskInstance.getId();

        final Map<String, Serializable> fieldValues = new HashMap<String, Serializable>(1);
        fieldValues.put("field_fieldId1", "Excel");
        final Expression rightOperand = new ExpressionBuilder().createInputExpression("field_fieldId1", String.class.getName());
        final Operation operation = buildOperation("Application", OperatorType.ASSIGNMENT, "=", rightOperand);
        final List<Operation> operationsMap = new ArrayList<Operation>(1);
        operationsMap.add(operation);
        final Map<String, Serializable> executeParameters = new HashMap<String, Serializable>(2);
        executeParameters.put("ACTIVITY_INSTANCE_ID_KEY", activityInstanceId);
        executeParameters.put("OPERATIONS_LIST_KEY", (Serializable) operationsMap);
        executeParameters.put("OPERATIONS_INPUT_KEY", (Serializable) fieldValues);
        getCommandAPI().execute("executeActionsAndTerminate", executeParameters);
        waitForProcessToFinish(humanTaskInstance.getParentProcessInstanceId());
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ArchivedHumanTaskInstanceSearchDescriptor.NAME, Order.DESC);

        final SearchResult<ArchivedHumanTaskInstance> searchResult = getProcessAPI().searchArchivedHumanTasksSupervisedBy(matti.getId(), builder.done());
        assertEquals(1, searchResult.getCount());
        assertEquals(1, searchResult.getResult().size());
        final ArchivedUserTaskInstance taskInstance = (ArchivedUserTaskInstance) searchResult.getResult().get(0);
        assertEquals("userTask1", taskInstance.getName());
        assertEquals(john.getId(), taskInstance.getAssigneeId());
        assertEquals(ActivityStates.COMPLETED_STATE, taskInstance.getState());
    }

    @Test
    public void getPendingTasksSupervisedBy() throws Exception {
        final SearchOptions searchOptions = new SearchOptionsBuilder(0, 10).sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC).done();
        final SearchResult<HumanTaskInstance> searchResult = getProcessAPI().searchPendingTasksSupervisedBy(matti.getId(), searchOptions);
        assertEquals(1, searchResult.getCount());
        final List<HumanTaskInstance> tasks = searchResult.getResult();
        final HumanTaskInstance taskInstance = tasks.get(0);
        assertEquals("userTask1", taskInstance.getName());
        assertEquals(0, taskInstance.getAssigneeId());
    }

    @Test
    public void searchProcessDefinitionsSupervisedBy() throws Exception {
        // create processDefintions
        final List<Long> proDefIds = new ArrayList<Long>();
        final int num = 5;
        for (int i = 0; i < num; i++) {
            final String actorName = "actorManu" + i;
            final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("My_Process" + i, "1." + i);
            processBuilder.addActor(actorName).addDescription("actor description" + i);
            final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("userTask1", actorName).getProcess();
            final ProcessDefinition processDefinition1 = deployAndEnableWithActor(designProcessDefinition, actorName, john);
            definitions.add(processDefinition1);
            proDefIds.add(processDefinition1.getId());
        }

        // add same supervisors
        for (int i = num - 1; i > 0; i--) {
            supervisors.add(getProcessAPI().createProcessSupervisorForUser(proDefIds.get(i), john.getId()));
        }

        // create category hr
        final String categoryName = "HR";
        final String categoryDescription = "This category for HR.";
        final Category category1 = getProcessAPI().createCategory(categoryName, categoryDescription);

        // create category sales
        final String categoryName2 = "sales";
        final String categoryDescription2 = "This category for sales.";
        final Category category2 = getProcessAPI().createCategory(categoryName2, categoryDescription2);

        // three processDefinitions for HR
        getProcessAPI().addProcessDefinitionsToCategory(category1.getId(), new ArrayList<Long>(proDefIds.subList(0, 3)));
        // two processDefinitions for sales
        getProcessAPI().addProcessDefinitionsToCategory(category2.getId(), new ArrayList<Long>(proDefIds.subList(3, proDefIds.size())));

        final int pageNum = 10;

        // test get all process Definitions without filter
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, pageNum);
        builder.sort(ProcessDeploymentInfoSearchDescriptor.ID, Order.DESC);
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfosSupervisedBy(john.getId(), builder.done());
        assertEquals(5, searchRes.getCount());

        // test search in order
        final SearchOptionsBuilder builder1 = new SearchOptionsBuilder(0, pageNum);
        builder1.filter(ProcessDeploymentInfoSearchDescriptor.CATEGORY_ID, category1.getId());
        builder1.sort(ProcessDeploymentInfoSearchDescriptor.ID, Order.DESC);

        final SearchResult<ProcessDeploymentInfo> searchRes1 = getProcessAPI().searchProcessDeploymentInfosSupervisedBy(john.getId(), builder1.done());
        assertEquals(2, searchRes1.getCount());
        final List<ProcessDeploymentInfo> processDeploymentInfos1 = searchRes1.getResult();
        assertNotNull(processDeploymentInfos1);
        assertEquals(2, processDeploymentInfos1.size());
        assertEquals(proDefIds.get(2).longValue(), processDeploymentInfos1.get(0).getProcessId());
        assertEquals(proDefIds.get(1).longValue(), processDeploymentInfos1.get(1).getProcessId());

        // test term
        final SearchOptionsBuilder builder2 = new SearchOptionsBuilder(0, pageNum);
        builder2.filter(ProcessDeploymentInfoSearchDescriptor.CATEGORY_ID, category2.getId());
        builder2.searchTerm("My_Process4"); // use name as term

        final SearchResult<ProcessDeploymentInfo> searchRes2 = getProcessAPI().searchProcessDeploymentInfosSupervisedBy(john.getId(), builder2.done());
        assertEquals(1, searchRes2.getCount());
        final List<ProcessDeploymentInfo> processDeploymentInfos2 = searchRes2.getResult();
        assertNotNull(processDeploymentInfos2);
        assertEquals(1, processDeploymentInfos2.size());
        assertEquals(proDefIds.get(4).longValue(), processDeploymentInfos2.get(0).getProcessId());

        getProcessAPI().deleteCategory(category1.getId());
        getProcessAPI().deleteCategory(category2.getId());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void searchCommentsSupervisedBy() throws Exception {
        // prepare commentContent
        final String commentContent1 = "commentContent1";
        final String commentContent2 = "commentContent2";
        final String commentContent3 = "commentContent3";
        final String commentContent4 = "commentContent4";
        final String commentContent5 = "commentContent5";

        final ProcessInstance pi0 = getProcessAPI().startProcess(definitions.get(0).getId());

        // add comment to processInstance
        getProcessAPI().addProcessComment(pi0.getId(), commentContent1);
        getProcessAPI().addProcessComment(pi0.getId(), commentContent2);
        getProcessAPI().addProcessComment(pi0.getId(), commentContent3);

        loginWith("john", "bpm");
        final ProcessDefinitionBuilder processBuilder2 = new ProcessDefinitionBuilder().createNewInstance("secondProcess", "2.0");
        processBuilder2.addDescription("definition2 description");
        processBuilder2.addActor(ACTOR_NAME).addUserTask("temporize", ACTOR_NAME);
        final DesignProcessDefinition designprocessDefinition2 = processBuilder2.done();
        final ProcessDefinition definition2 = deployAndEnableWithActor(designprocessDefinition2, ACTOR_NAME, matti);
        definitions.add(definition2);
        final ProcessInstance pi1 = getProcessAPI().startProcess(definition2.getId());

        getProcessAPI().addProcessComment(pi1.getId(), commentContent4);
        getProcessAPI().addProcessComment(pi1.getId(), commentContent5);

        // create supervisor for definition2
        final ProcessSupervisor createdSupervisor2 = getProcessAPI().createProcessSupervisorForUser(definition2.getId(), john.getId());
        assertEquals(definition2.getId(), createdSupervisor2.getProcessDefinitionId());

        final Map<String, Serializable> parameters1 = new HashMap<String, Serializable>();
        final SearchOptionsBuilder builder1 = new SearchOptionsBuilder(0, 10);
        parameters1.put(SUPERVISOR_ID_CMD_KEY, matti.getId());
        parameters1.put("SEARCH_OPTIONS_KEY", builder1.done());
        final SearchResult<Serializable> searchResult1 = (SearchResult<Serializable>) getCommandAPI().execute(SEARCH_S_COMMENT_SUPERVISED_BY, parameters1);
        assertEquals(3, searchResult1.getCount());

        final Map<String, Serializable> parameters2 = new HashMap<String, Serializable>();
        final SearchOptionsBuilder builder2 = new SearchOptionsBuilder(0, 10);
        builder2.sort(SearchCommentsDescriptor.ID, Order.ASC);
        parameters2.put(SUPERVISOR_ID_CMD_KEY, john.getId());
        parameters2.put("SEARCH_OPTIONS_KEY", builder2.done());
        final SearchResult<Serializable> searchResult2 = (SearchResult<Serializable>) getCommandAPI().execute(SEARCH_S_COMMENT_SUPERVISED_BY, parameters2);
        assertEquals(2, searchResult2.getCount());
    }

}
