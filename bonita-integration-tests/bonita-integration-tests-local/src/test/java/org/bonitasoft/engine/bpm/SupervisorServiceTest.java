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
package org.bonitasoft.engine.bpm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.identity.IdentityService;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.builder.SGroupBuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.supervisor.mapping.SSupervisorNotFoundException;
import org.bonitasoft.engine.supervisor.mapping.SupervisorMappingService;
import org.bonitasoft.engine.supervisor.mapping.model.SMemberType;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisorBuilderFactory;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 */
public class SupervisorServiceTest extends CommonBPMServicesTest {

    private final TransactionService transactionService;

    private static SupervisorMappingService supervisorService;

    private final IdentityService identityService;

    final long processDefId = 123L;

    final long memberId = 1L;

    final SMemberType memberType = SMemberType.USER;

    public SupervisorServiceTest() {
        super();
        this.transactionService = getTransactionService();
        supervisorService = getTenantAccessor().getSupervisorService();
        this.identityService = getTenantAccessor().getIdentityService();
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
        final SGroup group = BuilderFactory.get(SGroupBuilderFactory.class).createNewInstance().setName(groupName).done();
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
        supervisorService.getProcessSupervisor(-1);
        this.transactionService.complete();
    }

    public void testDeleteSupervisor() throws Exception {
        final SUser user = createSUser("zeca", "bpm");
        final SProcessSupervisor createdSupervisor = createUserSupervisors(Collections.singletonList(user)).get(0);
        this.transactionService.begin();
        final SProcessSupervisor gotSupervisor = supervisorService.getProcessSupervisor(createdSupervisor.getId());
        Assert.assertEquals(createdSupervisor, gotSupervisor);
        supervisorService.deleteProcessSupervisor(gotSupervisor.getId());
        try {
            supervisorService.getProcessSupervisor(createdSupervisor.getId());
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
        supervisorService.deleteProcessSupervisor(-1);
        this.transactionService.complete();
    }

    private List<SProcessSupervisor> createUserSupervisors(final List<SUser> users) throws Exception {
        final List<SProcessSupervisor> supervisorList = new ArrayList<SProcessSupervisor>();
        for (final SUser sUser : users) {
            final SProcessSupervisor supervisor = BuilderFactory.get(SProcessSupervisorBuilderFactory.class).createNewInstance(this.processDefId).setUserId(sUser.getId()).done();
            final SProcessSupervisor createdSupervisor = createSupervisor(supervisor);
            supervisorList.add(createdSupervisor);
        }
        return supervisorList;
    }

    private SProcessSupervisor createSupervisor(final SProcessSupervisor supervisor) throws SBonitaException {
        this.transactionService.begin();
        SProcessSupervisor createdSupervisor = null;
        try {
            createdSupervisor = supervisorService.createProcessSupervisor(supervisor);
        } finally {
            this.transactionService.complete();
        }
        return createdSupervisor;
    }

    private List<SProcessSupervisor> createRoleSupervisors(final List<SRole> roles) throws Exception {
        final List<SProcessSupervisor> supervisorList = new ArrayList<SProcessSupervisor>();
        this.transactionService.begin();
        for (final SRole sRole : roles) {
            final SProcessSupervisor supervisor = BuilderFactory.get(SProcessSupervisorBuilderFactory.class).createNewInstance(this.processDefId).setRoleId(sRole.getId()).done();
            final SProcessSupervisor createdSupervisor = supervisorService.createProcessSupervisor(supervisor);
            supervisorList.add(createdSupervisor);
        }
        this.transactionService.complete();
        return supervisorList;
    }

    private List<SProcessSupervisor> createGroupSupervisors(final List<SGroup> groups) throws Exception {
        final List<SProcessSupervisor> supervisorList = new ArrayList<SProcessSupervisor>();
        this.transactionService.begin();
        for (final SGroup sGroup : groups) {
            final SProcessSupervisor supervisor = BuilderFactory.get(SProcessSupervisorBuilderFactory.class).createNewInstance(this.processDefId).setGroupId(sGroup.getId()).done();
            final SProcessSupervisor createdSupervisor = supervisorService.createProcessSupervisor(supervisor);
            supervisorList.add(createdSupervisor);
        }
        this.transactionService.complete();
        return supervisorList;
    }

    private List<SProcessSupervisor> createRoleAndGroupSupervisors(final Map<Long, Long> roleGroupMap) throws Exception {
        final List<SProcessSupervisor> supervisorList = new ArrayList<SProcessSupervisor>();
        this.transactionService.begin();
        for (final Entry<Long, Long> roleGroup : roleGroupMap.entrySet()) {
            final SProcessSupervisor supervisor = BuilderFactory.get(SProcessSupervisorBuilderFactory.class).createNewInstance(this.processDefId).setRoleId(roleGroup.getKey())
                    .setGroupId(roleGroup.getValue()).done();
            final SProcessSupervisor createdSupervisor = supervisorService.createProcessSupervisor(supervisor);
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
        final List<OrderByOption> oderByOptions = Collections.singletonList(new OrderByOption(SProcessSupervisor.class, BuilderFactory.get(SProcessSupervisorBuilderFactory.class).getUserIdKey(),
                OrderByType.DESC));
        final List<FilterOption> filterOptions = Collections.singletonList(new FilterOption(SProcessSupervisor.class, BuilderFactory.get(SProcessSupervisorBuilderFactory.class)
                .getProcessDefIdKey(),
                this.processDefId));

        // test ASC
        final QueryOptions searchOptions = new QueryOptions(0, 6, oderByOptions, filterOptions, null);
        final List<SProcessSupervisor> gotSupervisorList1 = supervisorService.searchProcessSupervisors(searchOptions);
        assertEquals(5, gotSupervisorList1.size());
        assertEquals(createdSupervisorList.get(4).getId(), gotSupervisorList1.get(0).getId());
        assertEquals(createdSupervisorList.get(3).getId(), gotSupervisorList1.get(1).getId());
        assertEquals(createdSupervisorList.get(2).getId(), gotSupervisorList1.get(2).getId());
        assertEquals(createdSupervisorList.get(1).getId(), gotSupervisorList1.get(3).getId());
        assertEquals(createdSupervisorList.get(0).getId(), gotSupervisorList1.get(4).getId());

        for (final SProcessSupervisor supervisor : createdSupervisorList) {
            supervisorService.deleteProcessSupervisor(supervisor.getId());
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
        final SProcessSupervisor supervisor = supervisorService.getProcessSupervisor(superviserId);
        this.transactionService.complete();
        return supervisor;
    }

    private void deleteSupervisor(final SProcessSupervisor supervisor) throws SBonitaException {
        this.transactionService.begin();
        supervisorService.deleteProcessSupervisor(supervisor);
        this.transactionService.complete();
    }

}
