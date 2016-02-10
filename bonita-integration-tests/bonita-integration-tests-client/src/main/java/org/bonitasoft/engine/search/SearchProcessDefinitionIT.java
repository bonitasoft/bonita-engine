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
package org.bonitasoft.engine.search;

import static org.bonitasoft.engine.matchers.ListElementMatcher.versionAre;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class SearchProcessDefinitionIT extends TestWithUser {

    private List<ProcessDefinition> enabledProcessDefinitions;

    private List<ProcessDefinition> disabledProcessDefinitions;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        enabledProcessDefinitions = new ArrayList<ProcessDefinition>(2);
        disabledProcessDefinitions = new ArrayList<ProcessDefinition>(2);
    }

    @Override
    @After
    public void after() throws Exception {
        disableAndDeleteProcess(enabledProcessDefinitions);
        deleteProcess(disabledProcessDefinitions);
        super.after();
    }

    @Test
    public void searchRecentlyStartedByProcessDefinitions() throws Exception {
        final long userId = user.getId();

        // create process1
        final DesignProcessDefinition designProcessDefinition1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"),
                Arrays.asList(true, true));
        enabledProcessDefinitions.add(deployAndEnableProcessWithActor(designProcessDefinition1, ACTOR_NAME, user));
        final ProcessInstance pi1 = getProcessAPI().startProcess(userId, enabledProcessDefinitions.get(0).getId());
        waitForUserTask(pi1, "step1");

        // create process2
        final DesignProcessDefinition designProcessDefinition2 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process2",
                PROCESS_VERSION,
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        enabledProcessDefinitions.add(deployAndEnableProcessWithActor(designProcessDefinition2, ACTOR_NAME, user));
        final ProcessInstance pi2 = getProcessAPI().startProcess(userId, enabledProcessDefinitions.get(1).getId());
        waitForUserTask(pi2, "step1");

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 5);
        builder.sort(ProcessDeploymentInfoSearchDescriptor.ID, Order.ASC);
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfosStartedBy(userId, builder.done());
        assertEquals(2, searchRes.getCount());
        final List<ProcessDeploymentInfo> processDeploymentInfos = searchRes.getResult();
        assertEquals("The first process definition must be " + designProcessDefinition1.getName(), enabledProcessDefinitions.get(0).getId(),
                processDeploymentInfos.get(0).getProcessId());
        assertEquals("The second process definition must be " + designProcessDefinition2.getName(), enabledProcessDefinitions.get(1).getId(),
                processDeploymentInfos.get(1).getProcessId());

        // test search in order
        final SearchOptionsBuilder builder1 = new SearchOptionsBuilder(0, 5);
        builder1.sort(ProcessDeploymentInfoSearchDescriptor.ID, Order.DESC);
        final SearchResult<ProcessDeploymentInfo> searchRes1 = getProcessAPI().searchProcessDeploymentInfosStartedBy(userId, builder1.done());
        assertEquals(2, searchRes1.getCount());
        final List<ProcessDeploymentInfo> processDeploymentInfos1 = searchRes1.getResult();
        assertNotNull(processDeploymentInfos1);
        assertEquals(2, processDeploymentInfos1.size());
        assertEquals(enabledProcessDefinitions.get(1).getId(), processDeploymentInfos1.get(0).getProcessId());
        assertEquals(enabledProcessDefinitions.get(0).getId(), processDeploymentInfos1.get(1).getProcessId());

        // test term
        final SearchOptionsBuilder builder2 = new SearchOptionsBuilder(0, 5);
        builder2.searchTerm("My_Process2"); // use name as term
        final SearchResult<ProcessDeploymentInfo> searchRes2 = getProcessAPI().searchProcessDeploymentInfosStartedBy(userId, builder2.done());
        assertEquals(1, searchRes2.getCount());
        final List<ProcessDeploymentInfo> processDeploymentInfos2 = searchRes2.getResult();
        assertNotNull(processDeploymentInfos2);
        assertEquals(1, processDeploymentInfos2.size());
        assertEquals(enabledProcessDefinitions.get(1).getId(), processDeploymentInfos2.get(0).getProcessId());

        // test filter
        final SearchOptionsBuilder builder3 = new SearchOptionsBuilder(0, 5);
        builder3.filter(ProcessDeploymentInfoSearchDescriptor.NAME, "My_Process2");
        final SearchResult<ProcessDeploymentInfo> searchRes3 = getProcessAPI().searchProcessDeploymentInfosStartedBy(userId, builder3.done());
        assertEquals(1, searchRes3.getCount());
        final List<ProcessDeploymentInfo> processDeploymentInfos3 = searchRes3.getResult();
        assertNotNull(processDeploymentInfos3);
        assertEquals(1, processDeploymentInfos3.size());
        assertEquals(enabledProcessDefinitions.get(1).getId(), processDeploymentInfos3.get(0).getProcessId());
    }

    @Test
    public void searchProcessDefinitions() throws Exception {
        // create 2 process
        createNbProcessDefinitionWithTwoHumanStepsAndDeployWithActor(2, user);

        // Get all process definitions, reverse order:
        SearchOptionsBuilder optsBuilder = new SearchOptionsBuilder(0, 5);
        optsBuilder.sort(ProcessDeploymentInfoSearchDescriptor.DEPLOYMENT_DATE, Order.DESC);
        final SearchResult<ProcessDeploymentInfo> searchRes0 = getProcessAPI().searchProcessDeploymentInfos(optsBuilder.done());
        assertEquals(2, searchRes0.getCount());
        // reverse order:
        assertEquals(enabledProcessDefinitions.get(0).getId(), searchRes0.getResult().get(1).getProcessId());
        assertEquals(enabledProcessDefinitions.get(1).getId(), searchRes0.getResult().get(0).getProcessId());

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
    }

    @Test
    public void searchProcessDefinitionsSorted() throws Exception {
        // create 5 process
        createNbProcessDefinitionWithTwoHumanStepsAndDeployWithActor(5, user);

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
    }

    @Test
    public void searchProcessDefinitionsFilter() throws Exception {
        // create 5 enabled process
        createNbProcessDefinitionWithTwoHumanStepsAndDeployWithActor(5, user);

        // create 1 disabled process
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("plop",
                PROCESS_VERSION, Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        disabledProcessDefinitions.add(getProcessAPI().deploy(designProcessDefinition));

        // Filter on version
        SearchOptionsBuilder optsBuilder = new SearchOptionsBuilder(0, 10);
        optsBuilder.sort(ProcessDeploymentInfoSearchDescriptor.VERSION, Order.ASC);
        optsBuilder.filter(ProcessDeploymentInfoSearchDescriptor.VERSION, "1.03");
        SearchResult<ProcessDeploymentInfo> searchResult = getProcessAPI().searchProcessDeploymentInfos(optsBuilder.done());
        assertEquals(1, searchResult.getCount());
        assertThat(searchResult.getResult(), versionAre("1.03"));

        // Filter on activation state
        optsBuilder = new SearchOptionsBuilder(0, 10);
        optsBuilder.sort(ProcessDeploymentInfoSearchDescriptor.VERSION, Order.ASC);
        optsBuilder.filter(ProcessDeploymentInfoSearchDescriptor.ACTIVATION_STATE, ActivationState.ENABLED.name());
        searchResult = getProcessAPI().searchProcessDeploymentInfos(optsBuilder.done());
        assertEquals(5, searchResult.getCount());
        assertFalse("Don't have to contain the process definition \"plop\" !!", searchResult.getResult().contains(enabledProcessDefinitions.get(4)));
    }

    @Cover(classes = { SearchOptionsBuilder.class, ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "SearchPendingTasks", "Apostrophe" }, jira = "ENGINE-366")
    @Test
    public void searchProcessDefinitionsWithApostropheOnProcessName() throws Exception {
        searchProcessDefinitions("process'Name", PROCESS_VERSION);
    }

    @Cover(classes = { SearchOptionsBuilder.class, ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "SearchPendingTasks", "Apostrophe" }, jira = "ENGINE-366")
    @Test
    public void searchProcessDefinitionsWithApostropheOnProcessVersion() throws Exception {
        searchProcessDefinitions(PROCESS_NAME, "process'VERSION");
    }

    private void searchProcessDefinitions(final String processName, final String processVersion) throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(processName, processVersion);
        processBuilder.addActor(ACTOR_NAME);
        enabledProcessDefinitions.add(deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, user));

        // Get all process definitions, reverse order:
        final SearchOptionsBuilder optsBuilder = new SearchOptionsBuilder(0, 10);
        optsBuilder.searchTerm("process'");
        final SearchResult<ProcessDeploymentInfo> searchResult = getProcessAPI().searchProcessDeploymentInfos(optsBuilder.done());
        assertEquals(1, searchResult.getCount());
        assertEquals(processName, searchResult.getResult().get(0).getName());
    }

    @Test
    public void searchProcessDefinitionsSearchTermOnVersion() throws Exception {
        // create 5 process
        createNbProcessDefinitionWithTwoHumanStepsAndDeployWithActor(5, user);

        // Get all process definitions, reverse order:
        final SearchOptionsBuilder optsBuilder = new SearchOptionsBuilder(0, 10);
        optsBuilder.sort(ProcessDeploymentInfoSearchDescriptor.VERSION, Order.ASC);
        optsBuilder.searchTerm("1.03");
        final SearchResult<ProcessDeploymentInfo> searchRes0 = getProcessAPI().searchProcessDeploymentInfos(optsBuilder.done());
        assertEquals(1, searchRes0.getCount());
        assertThat(searchRes0.getResult(), versionAre("1.03"));
    }

    private void createNbProcessDefinitionWithTwoHumanStepsAndDeployWithActor(final int nbProcess, final User user) throws BonitaException {
        for (int i = 0; i < nbProcess; i++) {
            String processName = PROCESS_NAME;
            if (i >= 0 && i < 10) {
                processName += "0";
            }
            final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(processName + i,
                    PROCESS_VERSION + i, Arrays.asList("step1_" + i, "step2_" + i), Arrays.asList(true, true));
            enabledProcessDefinitions.add(deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user));
        }
    }

}
