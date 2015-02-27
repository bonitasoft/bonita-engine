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
package org.bonitasoft.engine.core.process.instance.model;

import static org.assertj.core.api.Assertions.*;
import static org.bonitasoft.engine.test.persistence.builder.UserBuilder.*;

import java.util.List;

import javax.inject.Inject;

import org.bonitasoft.engine.identity.model.impl.SGroupImpl;
import org.bonitasoft.engine.identity.model.impl.SRoleImpl;
import org.bonitasoft.engine.identity.model.impl.SUserImpl;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;
import org.bonitasoft.engine.supervisor.mapping.model.impl.SProcessSupervisorImpl;
import org.bonitasoft.engine.test.persistence.repository.SupervisorRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class SupervisorQueriesTest {

    private static final long supervisorId = 1;

    private static final long tenantId = 2;

    private static final long processDefId = 3;

    private static final long userId = 4;

    private static final long groupId = 5;

    private static final long roleId = 6;

    @Inject
    private SupervisorRepository repository;

    @Test
    public void searchSProcessSupervisorWithSUserSGroupSRole_should_return_supervisors_mapped_through_user() {
        SUserImpl user = aUser().withId(userId).build();
        user.setTenantId(tenantId);
        repository.add(user);
        final SRoleImpl sRole = new SRoleImpl();
        sRole.setId(roleId);
        sRole.setTenantId(tenantId);
        repository.add(sRole);
        final SGroupImpl sGroup = new SGroupImpl();
        sGroup.setId(groupId);
        sGroup.setTenantId(tenantId);
        repository.add(sGroup);
        final SProcessSupervisor expectedSProcessSupervisor = repository.add(new SProcessSupervisorImpl(supervisorId, tenantId, processDefId, userId, -1, -1));
        repository.add(new SProcessSupervisorImpl(2, tenantId, processDefId, userId, groupId, roleId));

        final List<Long> sProcessSupervisors = repository.searchSProcessSupervisorWithSUserSGroupSRole(tenantId);

        assertThat(sProcessSupervisors).containsOnly(expectedSProcessSupervisor.getId());
    }

    @Test
    public void searchSProcessSupervisorWithSUserSGroupSRole_should_return_supervisors_mapped_through_group() {
        SUserImpl user = aUser().withId(userId).build();
        user.setTenantId(tenantId);
        repository.add(user);
        final SRoleImpl sRole = new SRoleImpl();
        sRole.setId(roleId);
        sRole.setTenantId(tenantId);
        repository.add(sRole);
        final SGroupImpl sGroup = new SGroupImpl();
        sGroup.setId(groupId);
        sGroup.setTenantId(tenantId);
        repository.add(sGroup);
        final SProcessSupervisor expectedSProcessSupervisor = repository.add(new SProcessSupervisorImpl(supervisorId, tenantId, processDefId, 0, groupId, 0));
        repository.add(new SProcessSupervisorImpl(2, tenantId, processDefId, userId, groupId, roleId));

        final List<Long> sProcessSupervisors = repository.searchSProcessSupervisorWithSUserSGroupSRole(tenantId);

        assertThat(sProcessSupervisors).containsOnly(expectedSProcessSupervisor.getId());
    }

    @Test
    public void searchSProcessSupervisorWithSUserSGroupSRole_should_return_supervisors_mapped_through_role() {
        SUserImpl user = aUser().withId(userId).build();
        user.setTenantId(tenantId);
        repository.add(user);
        final SRoleImpl sRole = new SRoleImpl();
        sRole.setId(roleId);
        sRole.setTenantId(tenantId);
        repository.add(sRole);
        final SGroupImpl sGroup = new SGroupImpl();
        sGroup.setId(groupId);
        sGroup.setTenantId(tenantId);
        repository.add(sGroup);
        final SProcessSupervisor expectedSProcessSupervisor = repository.add(new SProcessSupervisorImpl(supervisorId, tenantId, processDefId, 0, 0, roleId));
        repository.add(new SProcessSupervisorImpl(2, tenantId, processDefId, userId, groupId, roleId));

        final List<Long> sProcessSupervisors = repository.searchSProcessSupervisorWithSUserSGroupSRole(tenantId);

        assertThat(sProcessSupervisors).containsOnly(expectedSProcessSupervisor.getId());
    }

    @Test
    public void searchSProcessSupervisorWithSUserSGroupSRole_should_return_supervisors_mapped_through_group_and_role() {
        SUserImpl user = aUser().withId(userId).build();
        user.setTenantId(tenantId);
        repository.add(user);
        final SRoleImpl sRole = new SRoleImpl();
        sRole.setId(roleId);
        sRole.setTenantId(tenantId);
        repository.add(sRole);
        final SGroupImpl sGroup = new SGroupImpl();
        sGroup.setId(groupId);
        sGroup.setTenantId(tenantId);
        repository.add(sGroup);
        final SProcessSupervisor expectedSProcessSupervisor = repository.add(new SProcessSupervisorImpl(supervisorId, tenantId, processDefId, 0, groupId,
                roleId));
        repository.add(new SProcessSupervisorImpl(2, tenantId, processDefId, userId, groupId, roleId));

        final List<Long> sProcessSupervisors = repository.searchSProcessSupervisorWithSUserSGroupSRole(tenantId);

        assertThat(sProcessSupervisors).containsOnly(expectedSProcessSupervisor.getId());
    }
}
