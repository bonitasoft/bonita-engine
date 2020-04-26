/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoSearchDescriptor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class SearchProcessDeploymentInfosCanBeStartedByUsersManagedByIT extends TestWithTechnicalUser {

    private List<ProcessDefinition> enabledProcessDefinitions;

    private List<ProcessDefinition> disabledProcessDefinitions;

    private List<User> users = null;

    private List<Group> groups = null;

    private List<Role> roles = null;

    private List<UserMembership> userMemberships = null;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        // create users
        users = new ArrayList<User>(10);
        users.add(createUser("magali", "bpm"));
        users.add(createUser("monica", "bpm"));
        users.add(createUser("manager", "bpm"));
        users.add(createUser("chicobento", users.get(0).getId()));
        users.add(createUser("chicobento2", users.get(2).getId()));
        users.add(createUser("cebolinha", users.get(0).getId()));
        users.add(createUser("cascao", users.get(1).getId()));
        users.add(createUser("dorinha", users.get(0).getId()));
        users.add(createUser("dorinha2", users.get(0).getId()));
        users.add(createUser("dorinha3", users.get(1).getId()));

        // create groups
        groups = new ArrayList<Group>(2);
        groups.add(createGroup("groups.get(0)"));
        groups.add(createGroup("groups.get(1)"));

        // create roles
        roles = new ArrayList<Role>(2);
        roles.add(createRole("roles.get(0)"));
        roles.add(createRole("roles.get(1)"));

        // create user memberships
        userMemberships = new ArrayList<UserMembership>(4);
        userMemberships.add(
                getIdentityAPI().addUserMembership(users.get(7).getId(), groups.get(0).getId(), roles.get(0).getId()));
        userMemberships.add(
                getIdentityAPI().addUserMembership(users.get(8).getId(), groups.get(0).getId(), roles.get(1).getId()));
        userMemberships.add(
                getIdentityAPI().addUserMembership(users.get(9).getId(), groups.get(1).getId(), roles.get(0).getId()));
        userMemberships.add(
                getIdentityAPI().addUserMembership(users.get(4).getId(), groups.get(0).getId(), roles.get(0).getId()));

        // create processes
        enabledProcessDefinitions = new ArrayList<ProcessDefinition>(4);
        disabledProcessDefinitions = new ArrayList<ProcessDefinition>(1);
        createProcessesDefForSearchProcessUserCanStart();
    }

    @Override
    @After
    public void after() throws Exception {
        disableAndDeleteProcess(enabledProcessDefinitions);
        deleteProcess(disabledProcessDefinitions);
        deleteUserMemberships(userMemberships);
        deleteUsers(users);
        deleteGroups(groups);
        deleteRoles(roles);
        super.after();
    }

    @Test
    public void searchProcessDefinitionsUsersManagedByCanStart() throws Exception {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10)
                .sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI()
                .searchProcessDeploymentInfosCanBeStartedByUsersManagedBy(users.get(8).getId(),
                        searchOptionsBuilder.done());
        assertEquals(1, searchRes.getCount());
        List<ProcessDeploymentInfo> result = searchRes.getResult();
        assertEquals(enabledProcessDefinitions.get(4).getName(), result.get(0).getName());

        searchRes = getProcessAPI().searchProcessDeploymentInfosCanBeStartedByUsersManagedBy(users.get(0).getId(),
                searchOptionsBuilder.done());
        assertEquals(4, searchRes.getCount());
        result = searchRes.getResult();
        assertEquals(enabledProcessDefinitions.get(0).getName(), result.get(0).getName());
        assertEquals(enabledProcessDefinitions.get(4).getName(), result.get(1).getName());
        assertEquals(enabledProcessDefinitions.get(5).getName(), result.get(2).getName());
        assertEquals(enabledProcessDefinitions.get(6).getName(), result.get(3).getName());

        // user associated to a process without actor initiator
        searchRes = getProcessAPI().searchProcessDeploymentInfosCanBeStartedByUsersManagedBy(users.get(6).getId(),
                searchOptionsBuilder.done());
        assertEquals(0, searchRes.getCount());
    }

    @Test
    public void searchProcessDefinitionsUsersManagedByCanStartFromRoleAndGroup() throws Exception {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10)
                .sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI()
                .searchProcessDeploymentInfosCanBeStartedByUsersManagedBy(users.get(2).getId(),
                        searchOptionsBuilder.done());
        assertEquals(3, searchRes.getCount());
        assertEquals(enabledProcessDefinitions.get(4).getName(), searchRes.getResult().get(0).getName()); // from group
        assertEquals(enabledProcessDefinitions.get(5).getName(), searchRes.getResult().get(1).getName()); // from role
        assertEquals(enabledProcessDefinitions.get(6).getName(), searchRes.getResult().get(2).getName()); // from role and group
    }

    @Test
    public void searchProcessDefinitionsUsersManagedByCanStartWithSearchTerm() throws Exception {
        // test term
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10)
                .sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.searchTerm("My_Process7"); // use name as term
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI()
                .searchProcessDeploymentInfosCanBeStartedByUsersManagedBy(users.get(4).getId(),
                        searchOptionsBuilder.done());
        assertEquals(1, searchRes.getCount());
        assertEquals(enabledProcessDefinitions.get(5).getId(), searchRes.getResult().get(0).getProcessId());
    }

    @Test
    public void searchProcessDefinitionsUsersManagedByCanStartWithFilter() throws Exception {
        // test filter on process name
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10)
                .sort(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.filter(ProcessDeploymentInfoSearchDescriptor.NAME, "My_Process7");
        final SearchResult<ProcessDeploymentInfo> searchRes = getProcessAPI()
                .searchProcessDeploymentInfosCanBeStartedByUsersManagedBy(users.get(4).getId(),
                        searchOptionsBuilder.done());
        assertEquals(1, searchRes.getCount());
        assertEquals(enabledProcessDefinitions.get(5).getId(), searchRes.getResult().get(0).getProcessId());
    }

    private void createProcessesDefForSearchProcessUserCanStart() throws BonitaException {
        final String actor1 = ACTOR_NAME;
        final DesignProcessDefinition designProcessDefinition1 = BuildTestUtil
                .buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process1", "1.0",
                        Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor1, true);
        final ProcessDefinition processDefinition1 = deployAndEnableProcessWithActor(designProcessDefinition1, actor1,
                users.get(3));
        enabledProcessDefinitions.add(processDefinition1);

        // create process2
        final String actor2 = "Actor2";
        final DesignProcessDefinition designProcessDefinition2 = BuildTestUtil
                .buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process2", "1.0",
                        Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition2 = deployAndEnableProcessWithActor(designProcessDefinition2, actor2,
                users.get(1));
        enabledProcessDefinitions.add(processDefinition2);

        final DesignProcessDefinition designProcessDefinition3 = BuildTestUtil
                .buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process3", "1.0",
                        Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition3 = deployAndEnableProcessWithActor(designProcessDefinition3, actor2,
                users.get(1));
        enabledProcessDefinitions.add(processDefinition3);

        // process not enabled
        final DesignProcessDefinition designProcessDefinition4 = BuildTestUtil
                .buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process4", "1.0",
                        Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition4 = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition4)
                        .done());
        getProcessAPI().addUserToActor(actor2, processDefinition4, users.get(1).getId());
        disabledProcessDefinitions.add(processDefinition4);

        // process without actor initiator
        final DesignProcessDefinition designProcessDefinition5 = BuildTestUtil
                .buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process5", "1.0",
                        Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, false);
        final ProcessDefinition processDefinition5 = deployAndEnableProcessWithActor(designProcessDefinition5, actor2,
                users.get(2));
        enabledProcessDefinitions.add(processDefinition5);

        // actor initiator is a group
        final DesignProcessDefinition designProcessDefinition6 = BuildTestUtil
                .buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process6", "1.0",
                        Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition6 = deployAndEnableProcessWithActor(designProcessDefinition6, actor2,
                groups.get(0));
        enabledProcessDefinitions.add(processDefinition6);

        // actor initiator is a role
        final DesignProcessDefinition designProcessDefinition7 = BuildTestUtil
                .buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process7", "1.0",
                        Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition7 = deployAndEnableProcessWithActor(designProcessDefinition7, actor2,
                roles.get(0));
        enabledProcessDefinitions.add(processDefinition7);

        // actor initiator is a membership
        final DesignProcessDefinition designProcessDefinition8 = BuildTestUtil
                .buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process8", "1.0",
                        Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition8 = deployAndEnableProcessWithActor(designProcessDefinition8, actor2,
                roles.get(0), groups.get(0));
        enabledProcessDefinitions.add(processDefinition8);
    }

}
