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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoSearchDescriptor;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisor;
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
public class SearchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedByIT extends TestWithTechnicalUser {

    private List<ProcessDefinition> enabledProcessDefinitions;

    private List<User> users = null;

    private ProcessSupervisor supervisor;

    @Override
    @After
    public void after() throws Exception {
        deleteSupervisor(supervisor);
        disableAndDeleteProcess(enabledProcessDefinitions);
        deleteUsers(users);
        super.after();
    }

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        // create users
        users = new ArrayList<User>(2);
        users.add(createUser("chicobento", "bpm"));
        users.add(createUser("cebolinha", "bpm"));

        // create processes
        enabledProcessDefinitions = new ArrayList<ProcessDefinition>(4);
        createProcessesDefinitions();

        supervisor = getProcessAPI().createProcessSupervisorForUser(enabledProcessDefinitions.get(0).getId(), users.get(0).getId());
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Supervisor", "Assignee", "Pending", "Task", "Process definition" }, jira = "BS-1635")
    @Test
    public void searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy() throws Exception {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 5).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(
                users.get(0).getId(), searchOptionsBuilder.done());
        assertEquals(1, searchRes.getCount());
        assertEquals(enabledProcessDefinitions.get(0).getName(), searchRes.getResult().get(0).getName());

        searchRes = getProcessAPI().searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(users.get(1).getId(), searchOptionsBuilder.done());
        assertEquals(0, searchRes.getCount());
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Supervisor", "Assignee", "Pending", "Task", "Process definition" }, jira = "BS-1635")
    @Test
    public void searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedByWithFilter() throws Exception {
        // test filter on process name
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 5).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.filter(ProcessDeploymentInfoSearchDescriptor.NAME, "My_Process1");
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksSupervisedBy(
                users.get(0).getId(), searchOptionsBuilder.done());
        assertEquals(1, searchRes.getCount());
        assertEquals(enabledProcessDefinitions.get(0).getId(), searchRes.getResult().get(0).getProcessId());
    }

    private void createProcessesDefinitions() throws Exception {
        final DesignProcessDefinition designProcessDefinition1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process1", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), ACTOR_NAME, true);
        final ProcessDefinition processDefinition1 = deployAndEnableProcessWithActor(designProcessDefinition1, ACTOR_NAME, users.get(0));
        enabledProcessDefinitions.add(processDefinition1);
        startProcessAndWaitForTask(processDefinition1.getId(), "step1");

        // create process2
        final String actor2 = "Actor2";
        final DesignProcessDefinition designProcessDefinition2 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process2", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition2 = deployAndEnableProcessWithActor(designProcessDefinition2, actor2, users.get(1));
        enabledProcessDefinitions.add(processDefinition2);
        startProcessAndWaitForTask(processDefinition2.getId(), "step1");
    }

}
