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
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class SearchProcessDeploymentInfosWithAssignedOrPendingHumanTasksForIT extends TestWithTechnicalUser {

    private List<ProcessDefinition> enabledProcessDefinitions;

    private List<User> users = null;

    private List<Group> groups = null;

    private List<Role> roles = null;

    private List<UserMembership> userMemberships = null;

    @Override
    @After
    public void after() throws Exception {
        disableAndDeleteProcess(enabledProcessDefinitions);
        deleteUserMemberships(userMemberships);
        deleteUsers(users);
        deleteGroups(groups);
        deleteRoles(roles);
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
        users.add(createUser("cascao", "bpm"));

        // create groups
        groups = new ArrayList<Group>(2);
        groups.add(createGroup("group1"));
        groups.add(createGroup("group2"));

        // create roles
        roles = new ArrayList<Role>(2);
        roles.add(createRole("role1"));
        roles.add(createRole("role2"));

        // create user memberships
        userMemberships = new ArrayList<UserMembership>(3);
        userMemberships.add(getIdentityAPI().addUserMembership(users.get(2).getId(), groups.get(0).getId(), roles.get(0).getId()));
        userMemberships.add(getIdentityAPI().addUserMembership(users.get(0).getId(), groups.get(0).getId(), roles.get(1).getId()));
        userMemberships.add(getIdentityAPI().addUserMembership(users.get(1).getId(), groups.get(1).getId(), roles.get(0).getId()));

        // create processes
        enabledProcessDefinitions = new ArrayList<ProcessDefinition>(4);
        createProcessesDefinitions();
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "For user", "Assignee", "Pending", "Task", "Process definition" }, jira = "BS-1635")
    @Test
    public void searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksFor() throws Exception {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 5).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksFor(
                users.get(0).getId(), searchOptionsBuilder.done());
        assertEquals(2, searchRes.getCount());
        assertEquals(enabledProcessDefinitions.get(0).getName(), searchRes.getResult().get(0).getName());

        searchRes = getProcessAPI().searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksFor(users.get(1).getId(), searchOptionsBuilder.done());
        assertEquals(3, searchRes.getCount());
        assertEquals(enabledProcessDefinitions.get(1).getName(), searchRes.getResult().get(0).getName());
        assertEquals(enabledProcessDefinitions.get(2).getName(), searchRes.getResult().get(1).getName());
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "For user", "Assignee", "Pending", "Task", "Process definition" }, jira = "BS-1635")
    @Test
    public void searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksForWithFilter() throws Exception {
        // test filter on process name
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 5).sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.filter(ProcessDeploymentInfoSearchDescriptor.NAME, "My_Process2");
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI().searchProcessDeploymentInfosWithAssignedOrPendingHumanTasksFor(
                users.get(1).getId(), searchOptionsBuilder.done());
        assertEquals(1, searchRes.getCount());
        assertEquals(enabledProcessDefinitions.get(1).getId(), searchRes.getResult().get(0).getProcessId());
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

        final DesignProcessDefinition designProcessDefinition3 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process3", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition3 = deployAndEnableProcessWithActor(designProcessDefinition3, actor2, users.get(1));
        enabledProcessDefinitions.add(processDefinition3);
        startProcessAndWaitForTask(processDefinition3.getId(), "step1");

        // actor initiator is a group
        final DesignProcessDefinition designProcessDefinition6 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process6", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition6 = deployAndEnableProcessWithActor(designProcessDefinition6, actor2, groups.get(0));
        enabledProcessDefinitions.add(processDefinition6);
        startProcessAndWaitForTask(processDefinition6.getId(), "step1");

        // actor initiator is a role
        final DesignProcessDefinition designProcessDefinition7 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process7", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition7 = deployAndEnableProcessWithActor(designProcessDefinition7, actor2, roles.get(0));
        enabledProcessDefinitions.add(processDefinition7);
        startProcessAndWaitForTask(processDefinition7.getId(), "step1");

        // actor initiator is a membership
        final DesignProcessDefinition designProcessDefinition8 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process8", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition8 = deployAndEnableProcessWithActor(designProcessDefinition8, actor2, roles.get(0), groups.get(0));
        enabledProcessDefinitions.add(processDefinition8);
        startProcessAndWaitForTask(processDefinition8.getId(), "step1");
    }

}
