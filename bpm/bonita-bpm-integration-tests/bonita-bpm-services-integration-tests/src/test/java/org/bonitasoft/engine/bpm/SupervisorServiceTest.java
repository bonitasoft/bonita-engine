package org.bonitasoft.engine.bpm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.IdentityModelBuilder;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.supervisor.mapping.SSupervisorNotFoundException;
import org.bonitasoft.engine.supervisor.mapping.SupervisorMappingService;
import org.bonitasoft.engine.supervisor.mapping.model.SMemberType;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorBuilder;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 */
public class SupervisorServiceTest extends CommonBPMServicesTest {

    private final BPMServicesBuilder servicesBuilder;

    private final TransactionService transactionService;

    private static SupervisorMappingService supervisorService;

    private final SProcessSupervisorBuilder sSupervisorBuilder;

    private final IdentityService identityService;

    private final IdentityModelBuilder identityBuilder;

    final long processDefId = 123L;

    final long memberId = 1L;

    final SMemberType memberType = SMemberType.USER;

    public SupervisorServiceTest() {
        super();
        this.servicesBuilder = getServicesBuilder();
        this.transactionService = this.servicesBuilder.getTransactionService();
        supervisorService = this.servicesBuilder.getSupervisorService();
        this.sSupervisorBuilder = this.servicesBuilder.getSSupervisorBuilders().getSSupervisorBuilder();
        this.identityService = this.servicesBuilder.getIdentityService();
        this.identityBuilder = this.servicesBuilder.getIdentityModelBuilder();
    }

    @Test
    public void testCreateAndGetSupervisor() throws Exception {
        final SUser user = createSUser("zé", "bpm");
        final SProcessSupervisor createdSupervisor = createUserSupervisors(Collections.singletonList(user)).get(0);
        final SProcessSupervisor gotSupervisor = getSSupevisor(createdSupervisor.getId());
        Assert.assertEquals(createdSupervisor, gotSupervisor);

        deleteSupervisor(createdSupervisor);
        deleteSUser(user);
    }

    @Test
    public void testCreateRoleSupervisor() throws Exception {
        final SRole role = createSRole("role1");
        final SProcessSupervisor createdSupervisor = createRoleSupervisors(Collections.singletonList(role)).get(0);
        final SProcessSupervisor gotSupervisor = getSSupevisor(createdSupervisor.getId());
        Assert.assertEquals(createdSupervisor, gotSupervisor);

        deleteSupervisor(createdSupervisor);
        deleteSRole(role);
    }

    private SGroup createSGroup(final String groupName) throws SBonitaException {
        this.transactionService.begin();
        final SGroup group = this.identityBuilder.getGroupBuilder().createNewInstance().setName(groupName).done();
        this.identityService.createGroup(group);
        this.transactionService.complete();
        return group;
    }

    @Test
    public void testCreateGroupSupervisor() throws Exception {
        final SGroup group = createSGroup("group1");
        final SProcessSupervisor createdSupervisor = createGroupSupervisors(Collections.singletonList(group)).get(0);
        final SProcessSupervisor gotSupervisor = getSSupevisor(createdSupervisor.getId());
        Assert.assertEquals(createdSupervisor, gotSupervisor);

        deleteSupervisor(createdSupervisor);
        deleteSGroup(group);
    }

    @Test
    public void testCreateMembershipSupervisor() throws Exception {
        final SRole role = createSRole("role1");
        final SGroup group = createSGroup("group1");
        final SProcessSupervisor createdSupervisor = createRoleAndGroupSupervisors(Collections.singletonMap(role.getId(), group.getId())).get(0);
        final SProcessSupervisor gotSupervisor = getSSupevisor(createdSupervisor.getId());
        Assert.assertEquals(createdSupervisor, gotSupervisor);
        // clean-up
        deleteSupervisor(gotSupervisor);
        deleteSGroup(group);
        deleteSRole(role);
    }

    // FIXME with hibernate the exception arrives only when transaction.complete is called,
    // with mybatis the exception arrives before calling transaction.complete
    // we don't get the same exception. todo: investigate this and not use generic exception
    @Test(expected = Exception.class)
    public void testCreateSupervisorWithSSupervisorCreationException() throws Exception {
        final SUser user = createSUser("zeca", "bpm");
        final SProcessSupervisor createdSupervisor = createUserSupervisors(Collections.singletonList(user)).get(0);
        try {
            createUserSupervisors(Collections.singletonList(user));
        } finally {
            // clean-up
            deleteSupervisor(createdSupervisor);
            deleteSUser(user);
        }
    }

    @Test(expected = SSupervisorNotFoundException.class)
    public void testGetSupervisorThrowException() throws Exception {
        this.transactionService.begin();
        supervisorService.getSupervisor(-1);
        this.transactionService.complete();
    }

    public void testDeleteSupervisor() throws Exception {
        final SUser user = createSUser("zeca", "bpm");
        final SProcessSupervisor createdSupervisor = createUserSupervisors(Collections.singletonList(user)).get(0);
        this.transactionService.begin();
        final SProcessSupervisor gotSupervisor = supervisorService.getSupervisor(createdSupervisor.getId());
        Assert.assertEquals(createdSupervisor, gotSupervisor);
        supervisorService.deleteSupervisor(gotSupervisor.getId());
        try {
            supervisorService.getSupervisor(createdSupervisor.getId());
            Assert.fail("supervisor not deleted successfully!");
        } catch (final SSupervisorNotFoundException e) {
        } finally {
            this.transactionService.complete();
            deleteSUser(user);
        }
    }

    @Test(expected = SSupervisorNotFoundException.class)
    public void testDeleteSupervisorThrowException() throws Exception {
        this.transactionService.begin();
        supervisorService.deleteSupervisor(-1);
        this.transactionService.complete();
    }

    private List<SProcessSupervisor> createUserSupervisors(final List<SUser> users) throws Exception {
        final List<SProcessSupervisor> supervisorList = new ArrayList<SProcessSupervisor>();
        for (final SUser sUser : users) {
            final SProcessSupervisor supervisor = this.sSupervisorBuilder.createNewInstance(this.processDefId).setUserId(sUser.getId()).done();
            final SProcessSupervisor createdSupervisor = createSupervisor(supervisor);
            supervisorList.add(createdSupervisor);
        }
        return supervisorList;
    }

    private SProcessSupervisor createSupervisor(final SProcessSupervisor supervisor) throws SBonitaException {
        this.transactionService.begin();
        SProcessSupervisor createdSupervisor = null;
        try {
            createdSupervisor = supervisorService.createSupervisor(supervisor);
        } finally {
            this.transactionService.complete();
        }
        return createdSupervisor;
    }

    private List<SProcessSupervisor> createRoleSupervisors(final List<SRole> roles) throws Exception {
        final List<SProcessSupervisor> supervisorList = new ArrayList<SProcessSupervisor>();
        this.transactionService.begin();
        for (final SRole sRole : roles) {
            final SProcessSupervisor supervisor = this.sSupervisorBuilder.createNewInstance(this.processDefId).setRoleId(sRole.getId()).done();
            final SProcessSupervisor createdSupervisor = supervisorService.createSupervisor(supervisor);
            supervisorList.add(createdSupervisor);
        }
        this.transactionService.complete();
        return supervisorList;
    }

    private List<SProcessSupervisor> createGroupSupervisors(final List<SGroup> groups) throws Exception {
        final List<SProcessSupervisor> supervisorList = new ArrayList<SProcessSupervisor>();
        this.transactionService.begin();
        for (final SGroup sGroup : groups) {
            final SProcessSupervisor supervisor = this.sSupervisorBuilder.createNewInstance(this.processDefId).setGroupId(sGroup.getId()).done();
            final SProcessSupervisor createdSupervisor = supervisorService.createSupervisor(supervisor);
            supervisorList.add(createdSupervisor);
        }
        this.transactionService.complete();
        return supervisorList;
    }

    private List<SProcessSupervisor> createRoleAndGroupSupervisors(final Map<Long, Long> roleGroupMap) throws Exception {
        final List<SProcessSupervisor> supervisorList = new ArrayList<SProcessSupervisor>();
        this.transactionService.begin();
        for (final Entry<Long, Long> roleGroup : roleGroupMap.entrySet()) {
            final SProcessSupervisor supervisor = this.sSupervisorBuilder.createNewInstance(this.processDefId).setRoleId(roleGroup.getKey())
                    .setGroupId(roleGroup.getValue()).done();
            final SProcessSupervisor createdSupervisor = supervisorService.createSupervisor(supervisor);
            supervisorList.add(createdSupervisor);
        }
        this.transactionService.complete();
        return supervisorList;
    }

    @Test
    public void testSearchProcessDefSupervisorsInOrder() throws Exception {
        final List<SUser> users = new ArrayList<SUser>(5);
        users.add(createSUser("roberto", "bpm"));
        users.add(createSUser("joao", "bpm"));
        users.add(createSUser("maria", "bpm"));
        users.add(createSUser("paula", "bpm"));
        users.add(createSUser("julia", "bpm"));
        final List<SProcessSupervisor> createdSupervisorList = createUserSupervisors(users);
        assertEquals(5, createdSupervisorList.size());
        this.transactionService.begin();
        final List<OrderByOption> oderByOptions = Collections.singletonList(new OrderByOption(SProcessSupervisor.class, this.sSupervisorBuilder.getUserIdKey(),
                OrderByType.DESC));
        final List<FilterOption> filterOptions = Collections.singletonList(new FilterOption(SProcessSupervisor.class, this.sSupervisorBuilder
                .getProcessDefIdKey(),
                this.processDefId));

        // test ASC
        final QueryOptions searchOptions = new QueryOptions(0, 6, oderByOptions, filterOptions, null);
        final List<SProcessSupervisor> gotSupervisorList1 = supervisorService.searchProcessDefSupervisors(searchOptions);
        assertEquals(5, gotSupervisorList1.size());
        assertEquals(createdSupervisorList.get(4).getId(), gotSupervisorList1.get(0).getId());
        assertEquals(createdSupervisorList.get(3).getId(), gotSupervisorList1.get(1).getId());
        assertEquals(createdSupervisorList.get(2).getId(), gotSupervisorList1.get(2).getId());
        assertEquals(createdSupervisorList.get(1).getId(), gotSupervisorList1.get(3).getId());
        assertEquals(createdSupervisorList.get(0).getId(), gotSupervisorList1.get(4).getId());

        for (final SProcessSupervisor supervisor : createdSupervisorList) {
            supervisorService.deleteSupervisor(supervisor.getId());
        }
        this.transactionService.complete();

        deleteSUsers(users);
    }

    private boolean isUserProcesSupervisor(final SUser user) throws SBonitaException {
        this.transactionService.begin();
        final boolean isUserSupervisor = supervisorService.isProcessSupervisor(this.processDefId, user.getId());
        this.transactionService.complete();
        return isUserSupervisor;
    }

    @Test
    public void testIsUserProcessSupervisor() throws Exception {
        final SUser user = createSUser("paula", "bpm");
        assertFalse(isUserProcesSupervisor(user));
        final SProcessSupervisor userSupervisor = createUserSupervisors(Collections.singletonList(user)).get(0);
        assertTrue(isUserProcesSupervisor(user));

        deleteSupervisor(userSupervisor);
        deleteSUser(user);
    }

    @Test
    public void testIsUserProcessSupervisorFromGroup() throws Exception {
        final SUser user = createSUser("paula", "bpm");
        final SGroup group = createSGroup("group1");
        final SRole role = createSRole("role1");

        createSUserMembership(user, group, role);

        assertFalse(isUserProcesSupervisor(user));
        final SProcessSupervisor supervisor = createGroupSupervisors(Collections.singletonList(group)).get(0);
        assertTrue(isUserProcesSupervisor(user));

        deleteSupervisor(supervisor);
        deleteSUser(user);
        deleteSGroup(group);
        deleteSRole(role);
    }

    @Test
    public void testIsUserProcessSupervisorFromRole() throws Exception {
        final SUser user = createSUser("paula", "bpm");
        final SGroup group = createSGroup("group1");
        final SRole role = createSRole("role1");

        createSUserMembership(user, group, role);

        assertFalse(isUserProcesSupervisor(user));
        final SProcessSupervisor supervisor = createRoleSupervisors(Collections.singletonList(role)).get(0);
        assertTrue(isUserProcesSupervisor(user));

        deleteSupervisor(supervisor);
        deleteSUser(user);
        deleteSGroup(group);
        deleteSRole(role);
    }

    @Test
    public void testIsUserProcessSupervisorFromRoleAndGroup() throws Exception {
        final SUser user1 = createSUser("paula", "bpm");
        final SUser user2 = createSUser("julia", "bpm");
        final SGroup group1 = createSGroup("group1");
        final SGroup group2 = createSGroup("group2");
        final SRole role1 = createSRole("role1");
        final SRole role2 = createSRole("role2");

        createSUserMembership(user1, group1, role1);
        createSUserMembership(user2, group1, role2);
        createSUserMembership(user2, group2, role1);

        assertFalse(isUserProcesSupervisor(user1));
        assertFalse(isUserProcesSupervisor(user2));
        final SProcessSupervisor supervisor = createRoleAndGroupSupervisors(Collections.singletonMap(role1.getId(), group1.getId())).get(0);
        assertTrue(isUserProcesSupervisor(user1));
        assertFalse(isUserProcesSupervisor(user2));

        deleteSupervisor(supervisor);
        deleteSUsers(user1, user2);
        deleteSGroups(group1, group2);
        deleteSRoles(role1, role2);
    }

    private SProcessSupervisor getSSupevisor(final long superviserId) throws SBonitaException {
        this.transactionService.begin();
        final SProcessSupervisor supervisor = supervisorService.getSupervisor(superviserId);
        this.transactionService.complete();
        return supervisor;
    }

    private void deleteSupervisor(final SProcessSupervisor supervisor) throws SBonitaException {
        this.transactionService.begin();
        supervisorService.deleteSupervisor(supervisor);
        this.transactionService.complete();
    }

}
