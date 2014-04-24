/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.search;

import static org.bonitasoft.engine.matchers.ListElementMatcher.versionAre;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.category.Category;
import org.bonitasoft.engine.bpm.category.CategoryCriterion;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.search.impl.SearchOptionsImpl;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class SearchProcessDefinitionTest extends CommonAPITest {

    private List<ProcessDefinition> processDefinitions;

    private List<User> users = null;

    private List<Category> categories = null;

    private List<Group> groups = null;

    private List<Role> roles = null;

    private List<UserMembership> userMemberships = null;

    @After
    public void afterTest() throws BonitaException {
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
    }

    @Test
    public void searchRecentlyStartedProcessDefinitions() throws Exception {
        // create user
        final User user1 = createUser(USERNAME, PASSWORD);
        final long userId1 = user1.getId();

        // create process1
        final DesignProcessDefinition designProcessDefinition1 = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));
        final ProcessDefinition processDefinition1 = deployAndEnableWithActor(designProcessDefinition1, ACTOR_NAME, user1);
        final ProcessInstance pi1 = getProcessAPI().startProcess(user1.getId(), processDefinition1.getId());
        assertEquals(user1.getId(), pi1.getStartedBy());
        assertEquals(-1, pi1.getStartedBySubstitute());
        waitForUserTask("step1", pi1);

        // create process2
        final DesignProcessDefinition designProcessDefinition2 = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_Process2", PROCESS_VERSION,
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition2 = deployAndEnableWithActor(designProcessDefinition2, ACTOR_NAME, user1);
        final ProcessInstance pi2 = getProcessAPI().startProcess(user1.getId(), processDefinition2.getId());
        assertEquals(user1.getId(), pi2.getStartedBy());
        assertEquals(-1, pi2.getStartedBySubstitute());
        waitForUserTask("step1", pi2);

        final SearchOptions searchOptions = new SearchOptionsImpl(0, 5);
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfosStartedBy(userId1, searchOptions);

        assertEquals(2, searchRes.getCount());
        boolean hasDef1 = false;
        boolean hasDef2 = false;
        for (final ProcessDeploymentInfo info : searchRes.getResult()) {
            if (processDefinition1.getId() == info.getProcessId()) {
                hasDef1 = true;
            }
            if (processDefinition2.getId() == info.getProcessId()) {
                hasDef2 = true;
            }
        }
        assertTrue(hasDef1 && hasDef2);

        // test search in order
        final SearchOptionsBuilder builder1 = new SearchOptionsBuilder(0, 5);
        builder1.sort(ProcessDeploymentInfoSearchDescriptor.ID, Order.DESC);
        final SearchResult<ProcessDeploymentInfo> searchRes1 = getProcessAPI().searchProcessDeploymentInfosStartedBy(userId1, builder1.done());
        assertEquals(2, searchRes1.getCount());
        final List<ProcessDeploymentInfo> processDeploymentInfos1 = searchRes1.getResult();
        assertNotNull(processDeploymentInfos1);
        assertEquals(2, processDeploymentInfos1.size());
        assertEquals(processDefinition2.getId(), processDeploymentInfos1.get(0).getProcessId());
        assertEquals(processDefinition1.getId(), processDeploymentInfos1.get(1).getProcessId());

        // test term
        final SearchOptionsBuilder builder2 = new SearchOptionsBuilder(0, 5);
        builder2.searchTerm("My_Process2"); // use name as term
        final SearchResult<ProcessDeploymentInfo> searchRes2 = getProcessAPI().searchProcessDeploymentInfosStartedBy(userId1, builder2.done());
        assertEquals(1, searchRes2.getCount());
        final List<ProcessDeploymentInfo> processDeploymentInfos2 = searchRes2.getResult();
        assertNotNull(processDeploymentInfos2);
        assertEquals(1, processDeploymentInfos2.size());
        assertEquals(processDefinition2.getId(), processDeploymentInfos2.get(0).getProcessId());

        // test filter
        final SearchOptionsBuilder builder3 = new SearchOptionsBuilder(0, 5);
        builder3.filter(ProcessDeploymentInfoSearchDescriptor.NAME, "My_Process2");
        final SearchResult<ProcessDeploymentInfo> searchRes3 = getProcessAPI().searchProcessDeploymentInfosStartedBy(userId1, builder3.done());
        assertEquals(1, searchRes3.getCount());
        final List<ProcessDeploymentInfo> processDeploymentInfos3 = searchRes3.getResult();
        assertNotNull(processDeploymentInfos3);
        assertEquals(1, processDeploymentInfos3.size());
        assertEquals(processDefinition2.getId(), processDeploymentInfos3.get(0).getProcessId());

        disableAndDeleteProcess(processDefinition1);
        disableAndDeleteProcess(processDefinition2);
        deleteUser(user1.getId());
    }

    @Test
    public void searchProcessDefinitions() throws Exception {
        // create user
        final User manu = createUser(USERNAME, PASSWORD);
        loginWith(USERNAME, PASSWORD);

        // create 2 process
        final List<ProcessDefinition> processDefinitions = createNbProcessDefinitionWithTwoHumanStepsAndDeployWithActor(2, manu);

        // Get all process definitions, reverse order:
        SearchOptionsBuilder optsBuilder = new SearchOptionsBuilder(0, 5);
        optsBuilder.sort(ProcessDeploymentInfoSearchDescriptor.DEPLOYMENT_DATE, Order.DESC);
        final SearchResult<ProcessDeploymentInfo> searchRes0 = getProcessAPI().searchProcessDeploymentInfos(optsBuilder.done());
        assertEquals(2, searchRes0.getCount());
        // reverse order:
        assertEquals(processDefinitions.get(0).getId(), searchRes0.getResult().get(1).getProcessId());
        assertEquals(processDefinitions.get(1).getId(), searchRes0.getResult().get(0).getProcessId());

        // filter search with version
        optsBuilder = new SearchOptionsBuilder(0, 5);
        optsBuilder.filter(ProcessDeploymentInfoSearchDescriptor.VERSION, "1.01");
        final SearchResult<ProcessDeploymentInfo> searchRes1 = getProcessAPI().searchProcessDeploymentInfos(optsBuilder.done());
        assertEquals(1, searchRes1.getCount());
        final ProcessDeploymentInfo process = searchRes1.getResult().get(0);
        assertEquals(getSession().getUserId(), process.getDeployedBy());
        assertEquals(PROCESS_NAME + "01", process.getName());

        // partial term search
        optsBuilder = new SearchOptionsBuilder(0, 5);
        optsBuilder.searchTerm(PROCESS_NAME); // use process def as term
        final SearchResult<ProcessDeploymentInfo> searchRes2 = getProcessAPI().searchProcessDeploymentInfos(optsBuilder.done());
        assertEquals(2, searchRes2.getCount());

        // partial term search
        optsBuilder = new SearchOptionsBuilder(0, 5);
        optsBuilder.searchTerm("1.01"); // use process def as term
        final SearchResult<ProcessDeploymentInfo> searchRes3 = getProcessAPI().searchProcessDeploymentInfos(optsBuilder.done());
        assertEquals(1, searchRes3.getCount());

        disableAndDeleteProcess(processDefinitions.get(0));
        disableAndDeleteProcess(processDefinitions.get(1));

        deleteUser(manu.getId());
    }

    @Test
    public void searchProcessDefinitionsSorted() throws Exception {
        // create user
        final User manu = createUser(USERNAME, PASSWORD);
        loginWith(USERNAME, PASSWORD);

        // create 2 process
        final List<ProcessDefinition> processDefinitions = createNbProcessDefinitionWithTwoHumanStepsAndDeployWithActor(5, manu);

        // Get all process definitions, reverse order:
        SearchOptionsBuilder optsBuilder = new SearchOptionsBuilder(0, 10);
        optsBuilder.sort(ProcessDeploymentInfoSearchDescriptor.VERSION, Order.ASC);
        SearchResult<ProcessDeploymentInfo> searchRes0 = getProcessAPI().searchProcessDeploymentInfos(optsBuilder.done());
        assertEquals(5, searchRes0.getCount());
        assertThat(searchRes0.getResult(), versionAre("1.00", "1.01", "1.02", "1.03", "1.04"));
        optsBuilder = new SearchOptionsBuilder(0, 10);
        optsBuilder.sort(ProcessDeploymentInfoSearchDescriptor.VERSION, Order.DESC);
        searchRes0 = getProcessAPI().searchProcessDeploymentInfos(optsBuilder.done());
        assertEquals(5, searchRes0.getCount());
        assertThat(searchRes0.getResult(), versionAre("1.04", "1.03", "1.02", "1.01", "1.00"));

        disableAndDeleteProcess(processDefinitions.get(0));
        disableAndDeleteProcess(processDefinitions.get(1));
        disableAndDeleteProcess(processDefinitions.get(2));
        disableAndDeleteProcess(processDefinitions.get(3));
        disableAndDeleteProcess(processDefinitions.get(4));

        deleteUser(manu.getId());
    }

    @Test
    public void searchProcessDefinitionsFilterOnVersion() throws Exception {
        // create user
        final User manu = createUser(USERNAME, PASSWORD);
        loginWith(USERNAME, PASSWORD);

        // create 2 process
        final List<ProcessDefinition> processDefinitions = createNbProcessDefinitionWithTwoHumanStepsAndDeployWithActor(5, manu);

        // Get all process definitions, reverse order:
        final SearchOptionsBuilder optsBuilder = new SearchOptionsBuilder(0, 10);
        optsBuilder.sort(ProcessDeploymentInfoSearchDescriptor.VERSION, Order.ASC);
        optsBuilder.filter(ProcessDeploymentInfoSearchDescriptor.VERSION, "1.03");
        final SearchResult<ProcessDeploymentInfo> searchRes0 = getProcessAPI().searchProcessDeploymentInfos(optsBuilder.done());
        assertEquals(1, searchRes0.getCount());
        assertThat(searchRes0.getResult(), versionAre("1.03"));

        disableAndDeleteProcess(processDefinitions.get(0));
        disableAndDeleteProcess(processDefinitions.get(1));
        disableAndDeleteProcess(processDefinitions.get(2));
        disableAndDeleteProcess(processDefinitions.get(3));
        disableAndDeleteProcess(processDefinitions.get(4));

        deleteUser(manu.getId());
    }

    @Cover(classes = { SearchOptionsBuilder.class, ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "SearchPendingTasks", "Apostrophe" }, jira = "ENGINE-366")
    @Test
    public void searchProcessDefinitionsWithApostrophe() throws Exception {
        searchProcessDefinitions("process'Name", PROCESS_VERSION);
        searchProcessDefinitions(PROCESS_NAME, "process'VERSION");
    }

    private void searchProcessDefinitions(final String processName, final String processVersion) throws Exception {
        // create user
        final User user = createUser(USERNAME, PASSWORD);
        loginWith(USERNAME, PASSWORD);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(processName, processVersion).addDescription(
                DESCRIPTION);
        processBuilder.addActor(ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableWithActor(processBuilder.done(), ACTOR_NAME, user);

        // Get all process definitions, reverse order:
        final SearchOptionsBuilder optsBuilder = new SearchOptionsBuilder(0, 10);
        optsBuilder.searchTerm("process'");
        final SearchResult<ProcessDeploymentInfo> searchResult = getProcessAPI().searchProcessDeploymentInfos(optsBuilder.done());
        assertEquals(1, searchResult.getCount());
        assertEquals(processName, searchResult.getResult().get(0).getName());

        disableAndDeleteProcess(processDefinition);
        deleteUser(user.getId());
    }

    @Test
    public void searchProcessDefinitionsSearchTermOnVersion() throws Exception {
        // create user
        final User manu = createUser(USERNAME, PASSWORD);
        loginWith(USERNAME, PASSWORD);

        // create 2 process
        final List<ProcessDefinition> processDefinitions = createNbProcessDefinitionWithTwoHumanStepsAndDeployWithActor(5, manu);

        // Get all process definitions, reverse order:
        final SearchOptionsBuilder optsBuilder = new SearchOptionsBuilder(0, 10);
        optsBuilder.sort(ProcessDeploymentInfoSearchDescriptor.VERSION, Order.ASC);
        optsBuilder.searchTerm("1.03");
        final SearchResult<ProcessDeploymentInfo> searchRes0 = getProcessAPI().searchProcessDeploymentInfos(optsBuilder.done());
        assertEquals(1, searchRes0.getCount());
        assertThat(searchRes0.getResult(), versionAre("1.03"));

        disableAndDeleteProcess(processDefinitions.get(0));
        disableAndDeleteProcess(processDefinitions.get(1));
        disableAndDeleteProcess(processDefinitions.get(2));
        disableAndDeleteProcess(processDefinitions.get(3));
        disableAndDeleteProcess(processDefinitions.get(4));

        deleteUser(manu.getId());
    }

    private List<ProcessDefinition> createNbProcessDefinitionWithTwoHumanStepsAndDeployWithActor(final int nbProcess, final User user) throws BonitaException {
        final List<ProcessDefinition> processDefinitions = new ArrayList<ProcessDefinition>();
        for (int i = 0; i < nbProcess; i++) {
            String processName = PROCESS_NAME;
            if (i >= 0 && i < 10) {
                processName += "0";
            }
            final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps(processName + i,
                    PROCESS_VERSION + i, Arrays.asList("step1_" + i, "step2_" + i), Arrays.asList(true, true));
            processDefinitions.add(deployAndEnableWithActor(designProcessDefinition, ACTOR_NAME, user));
        }
        return processDefinitions;
    }

    @Test
    public void searchProcessDefinitionsUserCanStart() throws Exception {
        beforeSearchProcessDefinitionsUserCanStart();

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 5).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfos(users.get(0).getId(), searchOptionsBuilder.done());
        assertEquals(1, searchRes.getCount());
        assertEquals(processDefinitions.get(0).getName(), searchRes.getResult().get(0).getName());

        searchRes = getProcessAPI().searchProcessDeploymentInfos(users.get(1).getId(), searchOptionsBuilder.done());
        assertEquals(2, searchRes.getCount());
        assertEquals(processDefinitions.get(1).getName(), searchRes.getResult().get(0).getName());
        assertEquals(processDefinitions.get(2).getName(), searchRes.getResult().get(1).getName());

        // user associated to a process without actor initiator
        searchRes = getProcessAPI().searchProcessDeploymentInfos(users.get(2).getId(), searchOptionsBuilder.done());
        assertEquals(0, searchRes.getCount());

        afterSearchProcessDefinitionsUserCanStart();
    }

    @Test
    public void searchProcessDefinitionsUserCanStartFromGroup() throws Exception {
        beforeSearchProcessDefinitionsUserCanStart();

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 5).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfos(users.get(4).getId(), searchOptionsBuilder.done());
        assertEquals(1, searchRes.getCount());
        assertEquals(processDefinitions.get(5).getName(), searchRes.getResult().get(0).getName());

        afterSearchProcessDefinitionsUserCanStart();
    }

    @Test
    public void searchProcessDefinitionsUserCanStartFromRole() throws Exception {
        beforeSearchProcessDefinitionsUserCanStart();

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 5).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfos(users.get(5).getId(), searchOptionsBuilder.done());
        assertEquals(1, searchRes.getCount());
        assertEquals(processDefinitions.get(6).getName(), searchRes.getResult().get(0).getName());

        afterSearchProcessDefinitionsUserCanStart();
    }

    @Test
    public void searchProcessDefinitionsUserCanStartFromRoleAndGroup() throws Exception {
        beforeSearchProcessDefinitionsUserCanStart();

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 5).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfos(users.get(3).getId(), searchOptionsBuilder.done());
        assertEquals(3, searchRes.getCount());
        assertEquals(processDefinitions.get(5).getName(), searchRes.getResult().get(0).getName()); // from group
        assertEquals(processDefinitions.get(6).getName(), searchRes.getResult().get(1).getName()); // from role
        assertEquals(processDefinitions.get(7).getName(), searchRes.getResult().get(2).getName()); // from role and group

        afterSearchProcessDefinitionsUserCanStart();
    }

    @Test
    public void searchProcessDefinitionsUserCanStartWithSearchTerm() throws Exception {
        beforeSearchProcessDefinitionsUserCanStart();

        // test term
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 5).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.searchTerm("My_Process2"); // use name as term
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfos(users.get(1).getId(), searchOptionsBuilder.done());
        assertEquals(1, searchRes.getCount());
        assertEquals(processDefinitions.get(1).getId(), searchRes.getResult().get(0).getProcessId());

        afterSearchProcessDefinitionsUserCanStart();
    }

    @Test
    public void searchProcessDefinitionsUserCanStartWithFilter() throws Exception {
        beforeSearchProcessDefinitionsUserCanStart();

        // test filter on process name
        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 5).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.filter(ProcessDeploymentInfoSearchDescriptor.NAME, "My_Process2");
        SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfos(users.get(1).getId(), searchOptionsBuilder.done());
        assertEquals(1, searchRes.getCount());
        assertEquals(processDefinitions.get(1).getId(), searchRes.getResult().get(0).getProcessId());

        // test filter category
        searchOptionsBuilder = new SearchOptionsBuilder(0, 5).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.filter(ProcessDeploymentInfoSearchDescriptor.CATEGORY_ID, categories.get(0).getId());
        searchRes = getProcessAPI().searchProcessDeploymentInfos(users.get(1).getId(), searchOptionsBuilder.done());
        assertEquals(1, searchRes.getCount());
        assertEquals(processDefinitions.get(2).getId(), searchRes.getResult().get(0).getProcessId());

        afterSearchProcessDefinitionsUserCanStart();
    }

    @Test
    public void searchProcessDefinitionsUsersManagedByCanStart() throws Exception {
        beforeSearchProcessDefinitionsUsersManagedByCanStart();

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfosUsersManagedByCanStart(users.get(4).getId(),
                searchOptionsBuilder.done());
        assertEquals(1, searchRes.getCount());
        List<ProcessDeploymentInfo> result = searchRes.getResult();
        assertEquals(processDefinitions.get(6).getName(), result.get(0).getName());

        searchRes = getProcessAPI().searchProcessDeploymentInfosUsersManagedByCanStart(users.get(3).getId(), searchOptionsBuilder.done());
        assertEquals(6, searchRes.getCount());
        result = searchRes.getResult();
        assertEquals(processDefinitions.get(0).getName(), result.get(0).getName());
        assertEquals(processDefinitions.get(1).getName(), result.get(1).getName());
        assertEquals(processDefinitions.get(2).getName(), result.get(2).getName());
        assertEquals(processDefinitions.get(5).getName(), result.get(3).getName());
        assertEquals(processDefinitions.get(6).getName(), result.get(4).getName());
        assertEquals(processDefinitions.get(7).getName(), result.get(5).getName());

        // user associated to a process without actor initiator
        searchRes = getProcessAPI().searchProcessDeploymentInfosUsersManagedByCanStart(users.get(2).getId(), searchOptionsBuilder.done());
        assertEquals(0, searchRes.getCount());

        afterSearchProcessDefinitionsUsersManagedByCanStart();
    }

    @Test
    public void searchProcessDefinitionsUsersManagedByCanStartFromRoleAndGroup() throws Exception {
        beforeSearchProcessDefinitionsUsersManagedByCanStart();

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfosUsersManagedByCanStart(users.get(9).getId(),
                searchOptionsBuilder.done());
        assertEquals(3, searchRes.getCount());
        assertEquals(processDefinitions.get(5).getName(), searchRes.getResult().get(0).getName()); // from group
        assertEquals(processDefinitions.get(6).getName(), searchRes.getResult().get(1).getName()); // from role
        assertEquals(processDefinitions.get(7).getName(), searchRes.getResult().get(2).getName()); // from role and group

        afterSearchProcessDefinitionsUsersManagedByCanStart();
    }

    @Test
    public void searchProcessDefinitionsUsersManagedByCanStartWithSearchTerm() throws Exception {
        beforeSearchProcessDefinitionsUsersManagedByCanStart();

        // test term
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.searchTerm("My_Process7"); // use name as term
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfosUsersManagedByCanStart(users.get(4).getId(),
                searchOptionsBuilder.done());
        assertEquals(1, searchRes.getCount());
        assertEquals(processDefinitions.get(6).getId(), searchRes.getResult().get(0).getProcessId());

        afterSearchProcessDefinitionsUsersManagedByCanStart();
    }

    @Test
    public void searchProcessDefinitionsUsersManagedByCanStartWithFilter() throws Exception {
        beforeSearchProcessDefinitionsUsersManagedByCanStart();

        // test filter on process name
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.filter(ProcessDeploymentInfoSearchDescriptor.NAME, "My_Process7");
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfosUsersManagedByCanStart(users.get(4).getId(),
                searchOptionsBuilder.done());
        assertEquals(1, searchRes.getCount());
        assertEquals(processDefinitions.get(6).getId(), searchRes.getResult().get(0).getProcessId());

        afterSearchProcessDefinitionsUsersManagedByCanStart();
    }

    @Test
    public void searchUncategorizedProcessDefinitions() throws Exception {
        // create user
        final String username = "Bole";
        final String password = "bpm";
        final User bole = createUser(username, password);

        loginWith(username, password);
        final String processName1 = "processWithCategory";
        // create process1
        final DesignProcessDefinition designProcessDefinition1 = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps(processName1, "1.1",
                Arrays.asList("step1_1", "step1_2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition1 = deployAndEnableWithActor(designProcessDefinition1, ACTOR_NAME, bole);

        // create process2
        final String processName2 = "processWithoutCategory";
        final DesignProcessDefinition designProcessDefinition2 = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps(processName2, "1.2",
                Arrays.asList("step2_1", "step2_2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition2 = deployAndEnableWithActor(designProcessDefinition2, ACTOR_NAME, bole);

        // add categories to processDefinition1
        final ArrayList<Long> categoryIds = new ArrayList<Long>();
        final Category c1 = getProcessAPI().createCategory("category1", "categoryDescription1");
        final Category c2 = getProcessAPI().createCategory("category2", "categoryDescription2");
        final Category c3 = getProcessAPI().createCategory("category3", "categoryDescription3");
        categoryIds.add(c1.getId());
        categoryIds.add(c2.getId());
        categoryIds.add(c3.getId());
        getProcessAPI().addCategoriesToProcess(processDefinition1.getId(), categoryIds);
        categories = getProcessAPI().getCategoriesOfProcessDefinition(processDefinition1.getId(), 0, 10, CategoryCriterion.NAME_ASC);
        assertTrue(!categories.isEmpty());

        // Get all process definitions:
        SearchOptionsBuilder optsBuilder = new SearchOptionsBuilder(0, 5);
        optsBuilder.sort(ProcessDeploymentInfoSearchDescriptor.DEPLOYMENT_DATE, Order.DESC);
        SearchResult<ProcessDeploymentInfo> searchRes0 = getProcessAPI().searchProcessDeploymentInfos(optsBuilder.done());
        assertEquals(2, searchRes0.getCount());
        assertEquals(processDefinition1.getId(), searchRes0.getResult().get(1).getProcessId());
        assertEquals("processWithCategory", searchRes0.getResult().get(1).getName());
        assertEquals(processDefinition2.getId(), searchRes0.getResult().get(0).getProcessId());
        assertEquals("processWithoutCategory", searchRes0.getResult().get(0).getName());

        // Get all process definitions with no category associated:
        optsBuilder = new SearchOptionsBuilder(0, 5);
        optsBuilder.sort(ProcessDeploymentInfoSearchDescriptor.DEPLOYMENT_DATE, Order.DESC);
        searchRes0 = getProcessAPI().searchUncategorizedProcessDeploymentInfos(optsBuilder.done());
        assertEquals(1, searchRes0.getCount());
        assertEquals(processDefinition2.getId(), searchRes0.getResult().get(0).getProcessId());
        assertEquals("processWithoutCategory", searchRes0.getResult().get(0).getName());

        disableAndDeleteProcess(processDefinition1, processDefinition2);
        deleteCategories(categories);
        deleteUser(bole);
    }

    @Test
    public void searchUncategorizedProcessDefinitionsUserCanStartFromGroup() throws Exception {
        beforeSearchUncategorizedProcessDefinitionsUserCanStart(true);
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 5).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfos(users.get(4).getId(), searchOptionsBuilder.done());
        assertEquals(1, searchRes.getCount());
        assertEquals(processDefinitions.get(5).getName(), searchRes.getResult().get(0).getName());
        afterSearchUncategorizedProcessDefinitionsUserCanStart();
    }

    @Test
    public void searchUncategorizedProcessDefinitionsUserCanStartFromRole() throws Exception {
        beforeSearchUncategorizedProcessDefinitionsUserCanStart(true);
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 5).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfos(users.get(5).getId(), searchOptionsBuilder.done());
        assertEquals(1, searchRes.getCount());
        assertEquals(processDefinitions.get(6).getName(), searchRes.getResult().get(0).getName());
        afterSearchUncategorizedProcessDefinitionsUserCanStart();
    }

    @Test
    public void searchUncategorizedProcessDefinitionsUserCanStartFromRoleAndGroup() throws Exception {
        beforeSearchUncategorizedProcessDefinitionsUserCanStart(true);
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 5).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfos(users.get(3).getId(), searchOptionsBuilder.done());
        assertEquals(3, searchRes.getCount());
        assertEquals(processDefinitions.get(5).getName(), searchRes.getResult().get(0).getName()); // from group
        assertEquals(processDefinitions.get(6).getName(), searchRes.getResult().get(1).getName()); // from role
        assertEquals(processDefinitions.get(7).getName(), searchRes.getResult().get(2).getName()); // from role and group
        afterSearchUncategorizedProcessDefinitionsUserCanStart();
    }

    @Test
    public void searchUncategorizedProcessDefinitionsUserCanStartWithSearchTearm() throws Exception {
        beforeSearchUncategorizedProcessDefinitionsUserCanStart(true);
        // test term
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 5).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.searchTerm("My_Process2"); // use name as term
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfos(users.get(1).getId(), searchOptionsBuilder.done());
        assertEquals(1, searchRes.getCount());
        assertEquals(processDefinitions.get(1).getId(), searchRes.getResult().get(0).getProcessId());
        afterSearchUncategorizedProcessDefinitionsUserCanStart();
    }

    @Test
    public void searchUncategorizedProcessDefinitionsUserCanStart() throws Exception {
        // test uncategorized process definitions.
        beforeSearchUncategorizedProcessDefinitionsUserCanStart(true);
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 5).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchUncategorizedProcessDeploymentInfosUserCanStart(users.get(0).getId(),
                searchOptionsBuilder.done());
        assertEquals(1, searchRes.getCount());
        assertEquals(processDefinitions.get(0).getName(), searchRes.getResult().get(0).getName());

        searchRes = getProcessAPI().searchUncategorizedProcessDeploymentInfosUserCanStart(users.get(1).getId(), searchOptionsBuilder.done());
        assertEquals(2, searchRes.getCount());
        assertEquals(processDefinitions.get(1).getName(), searchRes.getResult().get(0).getName());
        assertEquals(processDefinitions.get(2).getName(), searchRes.getResult().get(1).getName());

        // user associated to a process without actor initiator
        searchRes = getProcessAPI().searchUncategorizedProcessDeploymentInfosUserCanStart(users.get(2).getId(), searchOptionsBuilder.done());
        assertEquals(0, searchRes.getCount());
        afterSearchUncategorizedProcessDefinitionsUserCanStart();
    }

    @Test
    public void searchUncategorizedProcessDefinitionsUserCanStartWithCategories() throws Exception {
        // test method searchUncategorizedProcessDefinitionsUserCanStart to search process definitions with categories.
        beforeSearchProcessDefinitionsUserCanStart();
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 5).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchUncategorizedProcessDeploymentInfosUserCanStart(users.get(1).getId(),
                searchOptionsBuilder.done());
        assertEquals(0, searchRes.getCount());
        afterSearchProcessDefinitionsUserCanStart();
    }

    private void afterSearchProcessDefinitionsUsersManagedByCanStart() throws BonitaException {
        disableAndDeleteProcess(processDefinitions);
        deleteUserMemberships(userMemberships);
        deleteUsers(users);
        deleteGroups(groups);
        deleteRoles(roles);
    }

    private void beforeSearchProcessDefinitionsUsersManagedByCanStart() throws BonitaException {
        // create users
        users = new ArrayList<User>(10);
        final User magali = createUser("magali", "bpm");
        final User monica = createUser("monica", "bpm");
        final User manager = createUser("manager", "bpm");
        final User chico = createUser("chicobento", magali.getId());
        final User chico2 = createUser("chicobento2", manager.getId());
        final User cebolinha = createUser("cebolinha", magali.getId());
        final User cascao = createUser("cascao", monica.getId());
        final User dorinha = createUser("dorinha", magali.getId());
        final User dorinha2 = createUser("dorinha2", magali.getId());
        final User dorinha3 = createUser("dorinha3", monica.getId());
        users.add(chico);
        users.add(cebolinha);
        users.add(cascao);
        users.add(magali);
        users.add(monica);
        users.add(dorinha);
        users.add(dorinha2);
        users.add(dorinha3);
        users.add(chico2);
        users.add(manager);

        // create groups
        groups = new ArrayList<Group>(2);
        final Group group1 = createGroup("group1");
        groups.add(group1);
        final Group group2 = createGroup("group2");
        groups.add(group2);

        // create roles
        roles = new ArrayList<Role>(2);
        final Role role1 = createRole("role1");
        final Role role2 = createRole("role2");
        roles.add(role1);
        roles.add(role2);

        // create user memberships
        userMemberships = new ArrayList<UserMembership>(4);
        userMemberships.add(getIdentityAPI().addUserMembership(dorinha.getId(), group1.getId(), role1.getId()));
        userMemberships.add(getIdentityAPI().addUserMembership(dorinha2.getId(), group1.getId(), role2.getId()));
        userMemberships.add(getIdentityAPI().addUserMembership(dorinha3.getId(), group2.getId(), role1.getId()));
        userMemberships.add(getIdentityAPI().addUserMembership(chico2.getId(), group1.getId(), role1.getId()));

        // create processes
        createProcessesDefForSearchProcessUserCanStart();
    }

    private void createProcessesDefForSearchProcessUserCanStart() throws BonitaException {
        processDefinitions = new ArrayList<ProcessDefinition>(4);
        final String actor1 = ACTOR_NAME;
        final DesignProcessDefinition designProcessDefinition1 = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_Process1", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor1, true);
        final ProcessDefinition processDefinition1 = deployAndEnableWithActor(designProcessDefinition1, actor1, users.get(0));
        processDefinitions.add(processDefinition1);

        // create process2
        final String actor2 = "Actor2";
        final DesignProcessDefinition designProcessDefinition2 = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_Process2", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition2 = deployAndEnableWithActor(designProcessDefinition2, actor2, users.get(1));
        processDefinitions.add(processDefinition2);

        final DesignProcessDefinition designProcessDefinition3 = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_Process3", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition3 = deployAndEnableWithActor(designProcessDefinition3, actor2, users.get(1));
        processDefinitions.add(processDefinition3);

        // process not enabled
        final DesignProcessDefinition designProcessDefinition4 = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_Process4", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition4 = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition4).done());
        getProcessAPI().addUserToActor(actor2, processDefinition4, users.get(1).getId());
        processDefinitions.add(processDefinition4);

        // process without actor initiator
        final DesignProcessDefinition designProcessDefinition5 = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_Process5", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, false);
        final ProcessDefinition processDefinition5 = deployAndEnableWithActor(designProcessDefinition5, actor2, users.get(2));
        processDefinitions.add(processDefinition5);

        // actor initiator is a group
        final DesignProcessDefinition designProcessDefinition6 = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_Process6", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition6 = deployAndEnableWithActor(designProcessDefinition6, actor2, groups.get(0));
        processDefinitions.add(processDefinition6);

        // actor initiator is a role
        final DesignProcessDefinition designProcessDefinition7 = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_Process7", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition7 = deployAndEnableWithActor(designProcessDefinition7, actor2, roles.get(0));
        processDefinitions.add(processDefinition7);

        // actor initiator is a membership
        final DesignProcessDefinition designProcessDefinition8 = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_Process8", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition8 = deployAndEnableWithActor(designProcessDefinition8, actor2, roles.get(0), groups.get(0));
        processDefinitions.add(processDefinition8);
    }

    private void afterSearchProcessDefinitionsUserCanStart() throws BonitaException {
        disableAndDeleteProcess(processDefinitions);
        deleteUserMemberships(userMemberships);
        deleteUsers(users);
        deleteCategories(categories);
        deleteGroups(groups);
        deleteRoles(roles);
    }

    private void beforeSearchProcessDefinitionsUserCanStart() throws BonitaException {
        // create users
        users = new ArrayList<User>(2);
        final User chico = createUser("chicobento", "bpm");
        final User cebolinha = createUser("cebolinha", "bpm");
        final User cascao = createUser("cascao", "bpm");
        final User magali = createUser("magali", "bpm");
        final User monica = createUser("monica", "bpm");
        final User dorinha = createUser("dorinha", "bpm");
        users.add(chico);
        users.add(cebolinha);
        users.add(cascao);
        users.add(magali);
        users.add(monica);
        users.add(dorinha);

        // create groups
        groups = new ArrayList<Group>(2);
        final Group group1 = createGroup("group1");
        groups.add(group1);
        final Group group2 = createGroup("group2");
        groups.add(group2);

        // create roles
        roles = new ArrayList<Role>(2);
        final Role role1 = createRole("role1");
        final Role role2 = createRole("role2");
        roles.add(role1);
        roles.add(role2);

        // create user memberships
        userMemberships = new ArrayList<UserMembership>(3);
        userMemberships.add(getIdentityAPI().addUserMembership(magali.getId(), group1.getId(), role1.getId()));
        userMemberships.add(getIdentityAPI().addUserMembership(monica.getId(), group1.getId(), role2.getId()));
        userMemberships.add(getIdentityAPI().addUserMembership(dorinha.getId(), group2.getId(), role1.getId()));

        // create processes
        createProcessesDefForSearchProcessUserCanStart();

        categories = new ArrayList<Category>(3);
        final Category category1 = getProcessAPI().createCategory("category1", "the first known category");
        final Category category2 = getProcessAPI().createCategory("category2", "the second known category");
        final Category category3 = getProcessAPI().createCategory("category3", "the third known category");
        categories.add(category1);
        categories.add(category2);
        categories.add(category3);
        getProcessAPI().addProcessDefinitionToCategory(category1.getId(), processDefinitions.get(2).getId());
        getProcessAPI().addProcessDefinitionToCategory(category2.getId(), processDefinitions.get(2).getId());
        getProcessAPI().addProcessDefinitionToCategory(category2.getId(), processDefinitions.get(1).getId());
        getProcessAPI().addProcessDefinitionToCategory(category3.getId(), processDefinitions.get(3).getId());
    }

    private void afterSearchUncategorizedProcessDefinitionsUserCanStart() throws BonitaException {
        disableAndDeleteProcess(processDefinitions);
        deleteUserMemberships(userMemberships);
        deleteUsers(users);
        deleteGroups(groups);
        deleteRoles(roles);
    }

    private void beforeSearchUncategorizedProcessDefinitionsUserCanStart(final boolean createProcess) throws BonitaException {
        users = new ArrayList<User>(6);
        final User chico = createUser("chicobento", "bpm");
        final User cebolinha = createUser("cebolinha", "bpm");
        final User cascao = createUser("cascao", "bpm");
        final User magali = createUser("magali", "bpm");
        final User monica = createUser("monica", "bpm");
        final User dorinha = createUser("dorinha", "bpm");
        users.add(chico);
        users.add(cebolinha);
        users.add(cascao);
        users.add(magali);
        users.add(monica);
        users.add(dorinha);

        groups = new ArrayList<Group>(2);
        final Group group1 = createGroup("group1");
        groups.add(group1);
        final Group group2 = createGroup("group2");
        groups.add(group2);

        roles = new ArrayList<Role>(2);
        final Role role1 = createRole("role1");
        final Role role2 = createRole("role2");
        roles.add(role1);
        roles.add(role2);

        userMemberships = new ArrayList<UserMembership>(3);
        userMemberships.add(getIdentityAPI().addUserMembership(magali.getId(), group1.getId(), role1.getId()));
        userMemberships.add(getIdentityAPI().addUserMembership(monica.getId(), group1.getId(), role2.getId()));
        userMemberships.add(getIdentityAPI().addUserMembership(dorinha.getId(), group2.getId(), role1.getId()));
        if (createProcess) {
            createProcessesDefForSearchProcessUserCanStart();
        }
    }
}
