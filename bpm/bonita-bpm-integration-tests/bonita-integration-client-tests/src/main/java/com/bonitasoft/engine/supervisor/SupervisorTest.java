package com.bonitasoft.engine.supervisor;

import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeployException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisor;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;

public class SupervisorTest extends CommonAPISPTest {

    @Before
    public void before() throws Exception {
        login();
    }

    @After
    public void after() throws BonitaException, BonitaHomeNotSetException {
        logout();
    }

    private void deleteSupervisors(final ProcessSupervisor... processSupervisors) throws BonitaException {
        if (processSupervisors != null) {
            for (final ProcessSupervisor processSupervisor : processSupervisors) {
                deleteSupervisor(processSupervisor.getSupervisorId());
            }
        }
    }

    private ProcessDefinition createProcessDefinition(final String processName) throws InvalidProcessDefinitionException, ProcessDeployException,
            InvalidBusinessArchiveFormatException, AlreadyExistsException {
        // test process definition with no supervisor
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0").done();

        return getProcessAPI().deploy(new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Search", "Process" }, story = "Search process supervisors for user.", jira = "ENGINE-766")
    @Test
    public void getNumberOfProcessSupervisorsForUser() throws Exception {
        final User user1 = getIdentityAPI().createUser(USERNAME, PASSWORD);
        final User user2 = createUser("user2", "bpm", "FirstName2", "LastName2");
        final User user3 = createUser("user3", "bpm", "FirstName3", "LastName3");

        final ProcessDefinition processDefinition1 = createProcessDefinition("myProcess1");

        final ProcessSupervisor supervisor1 = getProcessAPI().createProcessSupervisorForUser(processDefinition1.getId(), user1.getId());
        final ProcessSupervisor supervisor2 = getProcessAPI().createProcessSupervisorForUser(processDefinition1.getId(), user2.getId());
        final ProcessSupervisor supervisor3 = getProcessAPI().createProcessSupervisorForUser(processDefinition1.getId(), user3.getId());

        final long numberOfProcessSupervisorsForUser = getProcessAPI().getNumberOfProcessSupervisorsForUser(processDefinition1.getId());
        assertEquals(3, numberOfProcessSupervisorsForUser);

        // clean-up
        deleteSupervisors(supervisor1, supervisor2, supervisor3);
        deleteProcess(processDefinition1);
        deleteUsers(user1, user2, user3);
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Group", "Search", "Process" }, story = "Search process supervisors for group.", jira = "ENGINE-766")
    @Test
    public void getNumberOfProcessSupervisorsForGroup() throws Exception {
        final Group group1 = getIdentityAPI().createGroup("Engine", null);
        final ProcessDefinition processDefinition1 = createProcessDefinition("myProcess1");
        final ProcessSupervisor supervisor1 = getProcessAPI().createProcessSupervisorForGroup(processDefinition1.getId(), group1.getId());

        final long numberOfProcessSupervisorsForGroup = getProcessAPI().getNumberOfProcessSupervisorsForGroup(processDefinition1.getId());
        assertEquals(1, numberOfProcessSupervisorsForGroup);

        // clean-up
        deleteSupervisors(supervisor1);
        deleteProcess(processDefinition1);
        deleteGroups(group1);
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Role", "Search", "Process" }, story = "Search process supervisors for role.", jira = "ENGINE-766")
    @Test
    public void getNumberOfProcessSupervisorsForRole() throws Exception {
        final Role role1 = getIdentityAPI().createRole("Developer");
        final ProcessDefinition processDefinition1 = createProcessDefinition("myProcess1");
        final ProcessSupervisor supervisor1 = getProcessAPI().createProcessSupervisorForRole(processDefinition1.getId(), role1.getId());

        try {
            final long numberOfProcessSupervisorsForRole = getProcessAPI().getNumberOfProcessSupervisorsForRole(processDefinition1.getId());
            assertEquals(1, numberOfProcessSupervisorsForRole);
        } finally {
            // clean-up
            deleteSupervisors(supervisor1);
            deleteProcess(processDefinition1);
            deleteRoles(role1);
        }
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Role", "Group", "Search", "Process" }, story = "Search process supervisors for role and group.", jira = "ENGINE-766")
    @Test
    public void getNumberOfProcessSupervisorsForMembership() throws Exception {
        final Role role1 = getIdentityAPI().createRole("Developer");
        final Role role2 = createRole("role2");

        final Group group1 = getIdentityAPI().createGroup("Engine", null);
        final Group group2 = createGroup("group2", "level2");

        final ProcessDefinition processDefinition1 = createProcessDefinition("myProcess1");

        final ProcessSupervisor supervisor1 = getProcessAPI().createProcessSupervisorForMembership(processDefinition1.getId(), group1.getId(), role1.getId());
        final ProcessSupervisor supervisor2 = getProcessAPI().createProcessSupervisorForMembership(processDefinition1.getId(), group2.getId(), role2.getId());

        try {
            final long numberOfProcessSupervisorsForMembership = getProcessAPI().getNumberOfProcessSupervisorsForMembership(processDefinition1.getId());
            assertEquals(2, numberOfProcessSupervisorsForMembership);
        } finally {
            // clean-up
            deleteSupervisors(supervisor1, supervisor2);
            deleteProcess(processDefinition1);
            deleteRoles(role1, role2);
            deleteGroups(group1, group2);
        }
    }

}
