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
package org.bonitasoft.engine.supervisor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.category.Category;
import org.bonitasoft.engine.bpm.category.CategoryCriterion;
import org.bonitasoft.engine.bpm.document.ArchivedDocument;
import org.bonitasoft.engine.bpm.document.ArchivedDocumentsSearchDescriptor;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.document.DocumentsSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedUserTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;
import org.bonitasoft.engine.bpm.flownode.UserTaskInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisor;
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
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProcessSupervisedIT extends TestWithTechnicalUser {

    private static final String SEARCH_S_COMMENT_SUPERVISED_BY = "searchSCommentSupervisedBy";

    private static final String SUPERVISOR_ID_CMD_KEY = "supervisorId";

    private User john;

    private User matti;

    private UserMembership membership;

    private Role role;

    private Group group;

    private ProcessDefinition definition;

    private List<ProcessDefinition> processDefinitions;

    private List<ProcessInstance> processInstances;

    private ProcessSupervisor supervisorForUser;

    private ProcessSupervisor supervisorForRole;

    private ProcessSupervisor supervisorForGroup;

    private List<ProcessSupervisor> supervisors;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        john = createUser("john", "bpm");
        matti = createUser("matti", "bpm");

        logoutOnTenant();
        loginOnDefaultTenantWith("matti", PASSWORD);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor(ACTOR_NAME).addUserTask("step1", ACTOR_NAME);
        processBuilder.addShortTextData("Application", null);
        definition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, john);
        processDefinitions = new ArrayList<ProcessDefinition>();
        processDefinitions.add(definition);

        // Create supervisors
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

        // Three tasks
        processInstances = new ArrayList<ProcessInstance>();
        processInstances.add(getProcessAPI().startProcess(definition.getId()));
        processInstances.add(getProcessAPI().startProcess(definition.getId()));
        processInstances.add(getProcessAPI().startProcess(definition.getId()));
        waitForUserTaskAndAssigneIt(processInstances.get(0), "step1", john);
        waitForUserTaskAndAssigneIt(processInstances.get(1), "step1", john);
        waitForUserTask(processInstances.get(2), "step1");
    }

    @Override
    @After
    public void after() throws Exception {
        deleteSupervisors(supervisors);
        disableAndDeleteProcess(processDefinitions);
        getIdentityAPI().deleteUserMembership(membership.getId());
        getIdentityAPI().deleteUser(john.getId());
        getIdentityAPI().deleteUser(matti.getId());
        getIdentityAPI().deleteGroup(group.getId());
        getIdentityAPI().deleteRole(role.getId());
        super.after();
    }

    @Test
    public void searchAssignedTasksSupervisedBy() throws Exception {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.DESC);
        builder.filter("state", "ready");
        builder.filter("name", "step1");
        builder.filter("priority", TaskPriority.NORMAL);

        final SearchResult<HumanTaskInstance> searchResult = getProcessAPI().searchAssignedTasksSupervisedBy(matti.getId(), builder.done());
        assertEquals(2, searchResult.getResult().size());
        UserTaskInstance taskInstance = (UserTaskInstance) searchResult.getResult().get(0);
        assertEquals("step1", taskInstance.getName());
        assertEquals(john.getId(), taskInstance.getAssigneeId());

        taskInstance = (UserTaskInstance) searchResult.getResult().get(1);
        assertEquals("step1", taskInstance.getName());
        assertEquals(john.getId(), taskInstance.getAssigneeId());
    }

    @Test
    public void superviseMyArchivedTask() throws Exception {
        final List<HumanTaskInstance> instanceList = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        final HumanTaskInstance humanTaskInstance = instanceList.get(0);
        // one archive tasks
        final long activityInstanceId = humanTaskInstance.getId();

        final Map<String, Serializable> fieldValues = new HashMap<String, Serializable>(1);
        fieldValues.put("field_fieldId1", "Excel");
        final Expression rightOperand = new ExpressionBuilder().createInputExpression("field_fieldId1", String.class.getName());
        final Operation operation = BuildTestUtil.buildOperation("Application", false, OperatorType.ASSIGNMENT, "=", rightOperand);
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
        assertEquals("step1", taskInstance.getName());
        assertEquals(john.getId(), taskInstance.getAssigneeId());
        assertEquals(ActivityStates.COMPLETED_STATE, taskInstance.getState());
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
            final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("step1", actorName).getProcess();
            final ProcessDefinition processDefinition1 = deployAndEnableProcessWithActor(designProcessDefinition, actorName, john);
            processDefinitions.add(processDefinition1);
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

        // test get all process Definitions without filter
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ProcessDeploymentInfoSearchDescriptor.ID, Order.DESC);
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfosSupervisedBy(john.getId(), builder.done());
        assertEquals(5, searchRes.getCount());

        // test search in order
        final SearchOptionsBuilder builder1 = new SearchOptionsBuilder(0, 10);
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
        final SearchOptionsBuilder builder2 = new SearchOptionsBuilder(0, 10);
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
        final ProcessInstance processInstance3 = processInstances.get(2);

        // add comment to processInstance
        getProcessAPI().addProcessComment(processInstance3.getId(), "commentContent1");
        getProcessAPI().addProcessComment(processInstance3.getId(), "commentContent2");
        getProcessAPI().addProcessComment(processInstance3.getId(), "commentContent3");

        loginOnDefaultTenantWith("john", "bpm");
        final ProcessDefinitionBuilder processBuilder2 = new ProcessDefinitionBuilder().createNewInstance("secondProcess", "2.0");
        processBuilder2.addDescription("definition2 description");
        processBuilder2.addActor(ACTOR_NAME).addUserTask("temporize", ACTOR_NAME);
        final DesignProcessDefinition designprocessDefinition2 = processBuilder2.done();
        final ProcessDefinition definition2 = deployAndEnableProcessWithActor(designprocessDefinition2, ACTOR_NAME, matti);
        processDefinitions.add(definition2);
        final ProcessInstance pi1 = getProcessAPI().startProcess(definition2.getId());

        getProcessAPI().addProcessComment(pi1.getId(), "commentContent4");
        getProcessAPI().addProcessComment(pi1.getId(), "commentContent5");

        // create supervisor for definition2
        final ProcessSupervisor supervisor = getProcessAPI().createProcessSupervisorForUser(definition2.getId(), john.getId());
        supervisors.add(supervisor);
        assertEquals(definition2.getId(), supervisor.getProcessDefinitionId());

        final Map<String, Serializable> parameters1 = new HashMap<String, Serializable>();
        parameters1.put(SUPERVISOR_ID_CMD_KEY, matti.getId());
        parameters1.put("SEARCH_OPTIONS_KEY", new SearchOptionsBuilder(0, 10).done());
        final SearchResult<Serializable> searchResult1 = (SearchResult<Serializable>) getCommandAPI().execute(SEARCH_S_COMMENT_SUPERVISED_BY, parameters1);
        assertEquals(3, searchResult1.getCount());

        final Map<String, Serializable> parameters2 = new HashMap<String, Serializable>();
        parameters2.put(SUPERVISOR_ID_CMD_KEY, john.getId());
        parameters2.put("SEARCH_OPTIONS_KEY", new SearchOptionsBuilder(0, 10).done());
        final SearchResult<Serializable> searchResult2 = (SearchResult<Serializable>) getCommandAPI().execute(SEARCH_S_COMMENT_SUPERVISED_BY, parameters2);
        assertEquals(5, searchResult2.getCount());
    }

    @Test
    public void searchDocumentsSupervisedBy() throws Exception {
        final ProcessInstance processInstance = processInstances.get(2);
        buildAndAttachDocument(processInstance);

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(DocumentsSearchDescriptor.PROCESSINSTANCE_ID, processInstance.getId());
        final SearchResult<Document> documentSearch = getProcessAPI().searchDocumentsSupervisedBy(john.getId(), searchOptionsBuilder.done());
        assertEquals(1, documentSearch.getCount());
        assertEquals(processInstance.getId(), documentSearch.getResult().get(0).getProcessInstanceId());
    }

    @Test
    public void searchArchivedDocumentsSupervisedBy() throws Exception {
        final ProcessInstance processInstance = processInstances.get(2);
        buildAndAttachDocument(processInstance);

        skipTasks(processInstance);
        waitForProcessToFinish(processInstance);

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(ArchivedDocumentsSearchDescriptor.PROCESSINSTANCE_ID, processInstance.getId());
        searchOptionsBuilder.sort(ArchivedDocumentsSearchDescriptor.DOCUMENT_NAME, Order.ASC);
        final SearchResult<ArchivedDocument> documentSearch = getProcessAPI().searchArchivedDocumentsSupervisedBy(matti.getId(), searchOptionsBuilder.done());
        assertEquals(3, documentSearch.getCount());
        assertEquals(processInstance.getId(), documentSearch.getResult().get(0).getProcessInstanceId());
    }

    @Test
    public void searchPendingTasksSupervisedBy() throws Exception {
        final SearchOptionsBuilder searchOptions = BuildTestUtil.buildSearchOptions(0, 10, HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        final SearchResult<HumanTaskInstance> result = getProcessAPI().searchPendingTasksSupervisedBy(matti.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(3, result.getCount());
        final List<HumanTaskInstance> humanTaskInstanceList = result.getResult();
        assertNotNull(humanTaskInstanceList);
        assertEquals(3, humanTaskInstanceList.size());
        assertEquals(getProcessAPI().getActivities(processInstances.get(0).getId(), 0, 10).get(0).getId(), humanTaskInstanceList.get(0).getId());
        assertEquals(getProcessAPI().getActivities(processInstances.get(1).getId(), 0, 10).get(0).getId(), humanTaskInstanceList.get(1).getId());
        assertEquals(getProcessAPI().getActivities(processInstances.get(2).getId(), 0, 10).get(0).getId(), humanTaskInstanceList.get(2).getId());
    }

    @Test
    public void searchUncategorizedProcessDefinitionsSupervisedBy() throws Exception {
        // create process1
        final String processName1 = "processDefinition1";
        final DesignProcessDefinition designProcessDefinition1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(processName1, "1.1",
                Arrays.asList("step1_1", "step1_2"), Arrays.asList(true, true));
        processDefinitions.add(deployAndEnableProcessWithActor(designProcessDefinition1, ACTOR_NAME, matti));

        // create process2
        final String processName2 = "processDefinition2";
        final DesignProcessDefinition designProcessDefinition2 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(processName2, "1.2",
                Arrays.asList("step2_1", "step2_2"), Arrays.asList(true, true));
        processDefinitions.add(deployAndEnableProcessWithActor(designProcessDefinition2, ACTOR_NAME, matti));

        // create supervisor
        supervisors.add(getProcessAPI().createProcessSupervisorForUser(processDefinitions.get(1).getId(), john.getId()));
        supervisors.add(getProcessAPI().createProcessSupervisorForUser(processDefinitions.get(2).getId(), matti.getId()));

        // add categories to processDefinition1
        final ArrayList<Long> categoryIds = new ArrayList<Long>();
        final Category c1 = getProcessAPI().createCategory("category1", "categoryDescription1");
        final Category c2 = getProcessAPI().createCategory("category2", "categoryDescription2");
        final Category c3 = getProcessAPI().createCategory("category3", "categoryDescription3");
        categoryIds.add(c1.getId());
        categoryIds.add(c2.getId());
        categoryIds.add(c3.getId());
        getProcessAPI().addCategoriesToProcess(processDefinitions.get(0).getId(), categoryIds);
        final List<Category> categories = getProcessAPI()
                .getCategoriesOfProcessDefinition(processDefinitions.get(0).getId(), 0, 10, CategoryCriterion.NAME_ASC);
        assertTrue(!categories.isEmpty());

        // Get all process definitions:
        final SearchOptionsBuilder optsBuilder = new SearchOptionsBuilder(0, 5);
        optsBuilder.sort(ProcessDeploymentInfoSearchDescriptor.DEPLOYMENT_DATE, Order.DESC);
        SearchResult<ProcessDeploymentInfo> searchRes0 = getProcessAPI().searchProcessDeploymentInfos(optsBuilder.done());
        assertEquals(3, searchRes0.getCount());

        // Get all process definitions with no category associated, supervised by user:
        searchRes0 = getProcessAPI().searchUncategorizedProcessDeploymentInfosSupervisedBy(matti.getId(), optsBuilder.done());
        assertEquals(1, searchRes0.getCount());
        assertEquals(processDefinitions.get(2).getId(), searchRes0.getResult().get(0).getProcessId());
        assertEquals("processDefinition2", searchRes0.getResult().get(0).getName());

        searchRes0 = getProcessAPI().searchUncategorizedProcessDeploymentInfosSupervisedBy(john.getId(), optsBuilder.done());
        assertEquals(1, searchRes0.getCount());
        assertEquals(processDefinitions.get(1).getId(), searchRes0.getResult().get(0).getProcessId());
        assertEquals("processDefinition1", searchRes0.getResult().get(0).getName());

        deleteCategories(categories);
    }

    @Test
    public void searchOpenProcessInstancesSupervisedBy() throws Exception {
        final ProcessInstance instance = processInstances.get(2);

        // prepare search options
        final SearchOptionsBuilder searchOptions = BuildTestUtil.buildSearchOptions(0, 10, ProcessInstanceSearchDescriptor.ID, Order.ASC);
        final SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstancesSupervisedBy(matti.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(3, result.getCount());
        final List<ProcessInstance> processInstanceList = result.getResult();
        assertNotNull(processInstanceList);
        assertEquals(3, processInstanceList.size());
        assertEquals(instance.getId(), processInstanceList.get(2).getId());
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, jira = "BS-8387", keywords = { "Process instance", "Started for", "Search",
            "Involving user", "Open", "Archived" })
    @Test
    public void searchProcessInstancesInvolvingUserWithSupervisorStartedProcess() throws Exception {
        final long processDefinitionId = processDefinitions.get(0).getId();
        // assign pending task to jack
        final ProcessInstance processInstance = getProcessAPI().startProcess(john.getId(), processDefinitionId);
        processInstances.add(processInstance);
        final long step1Id = waitForUserTask(processInstance, "step1");

        logoutOnTenant();
        loginOnDefaultTenantWith("john", PASSWORD);

        final SearchOptionsBuilder searchOptions = BuildTestUtil.buildSearchOptions(processDefinitionId, 0, 5, ProcessInstanceSearchDescriptor.ID, Order.ASC);
        SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(john.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(1, result.getCount());
        assertEquals(john.getId(), result.getResult().get(0).getStartedBy());
        assertEquals(matti.getId(), result.getResult().get(0).getStartedBySubstitute());

        getProcessAPI().assignUserTask(step1Id, john.getId());
        getProcessAPI().executeFlowNode(matti.getId(), step1Id);

        waitForProcessToFinish(processInstance);
        result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(john.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        final SearchResult<ArchivedProcessInstance> result2 = getProcessAPI().searchArchivedProcessInstancesInvolvingUser(john.getId(), searchOptions.done());
        assertNotNull(result2);
        assertEquals(1, result2.getCount());
        assertEquals(john.getId(), result2.getResult().get(0).getStartedBy());
        assertEquals(matti.getId(), result2.getResult().get(0).getStartedBySubstitute());
    }

    @Test
    public void searchArchivedProcessInstancesSupervisedBy() throws Exception {
        final ProcessInstance processInstance = processInstances.get(2);

        final List<ActivityInstance> activityInstances = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        for (final ActivityInstance activityInstance : activityInstances) {
            final long activityInstanceId = activityInstance.getId();
            skipTask(activityInstanceId);
        }

        waitForProcessToFinish(processInstance);

        // test supervisor
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        final SearchResult<ArchivedProcessInstance> sapi = getProcessAPI().searchArchivedProcessInstancesSupervisedBy(matti.getId(), builder.done());
        assertEquals(1, sapi.getCount());
        final List<ArchivedProcessInstance> archivedProcessInstanceList = sapi.getResult();
        assertEquals(processInstance.getId(), archivedProcessInstanceList.get(0).getSourceObjectId());
    }
}
