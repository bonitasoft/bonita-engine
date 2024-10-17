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
package org.bonitasoft.engine.core.process.instance.model;

import static org.assertj.core.api.Assertions.*;
import static org.bonitasoft.engine.test.persistence.builder.UserBuilder.*;

import java.util.List;

import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.supervisor.mapping.model.SProcessSupervisor;
import org.bonitasoft.engine.test.persistence.repository.SupervisorRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class SupervisorQueriesTest {

    private static final long supervisorId = 1;

    private static final long tenantId = 2;

    private static final long processDefId = 3;

    private static final long userId = 4;

    private static final long groupId = 5;

    private static final long roleId = 6;

    @Autowired
    private SupervisorRepository repository;

    @Test
    public void searchSProcessSupervisorWithSUserSGroupSRole_should_return_supervisors_mapped_through_user() {
        SUser user = aUser().withId(userId).build();
        user.setTenantId(tenantId);
        repository.add(user);
        final SRole sRole = new SRole();
        sRole.setId(roleId);
        sRole.setTenantId(tenantId);
        repository.add(sRole);
        final SGroup sGroup = new SGroup();
        sGroup.setId(groupId);
        sGroup.setTenantId(tenantId);
        repository.add(sGroup);
        final SProcessSupervisor expectedSProcessSupervisor = repository
                .add(new SProcessSupervisor(supervisorId, tenantId, processDefId, userId, -1, -1));
        repository.add(new SProcessSupervisor(2, tenantId, processDefId, userId, groupId, roleId));

        final List<Long> sProcessSupervisors = repository.searchSProcessSupervisorWithSUserSGroupSRole();

        assertThat(sProcessSupervisors).containsOnly(expectedSProcessSupervisor.getId());
    }

    @Test
    public void searchSProcessSupervisorWithSUserSGroupSRole_should_return_supervisors_mapped_through_group() {
        SUser user = aUser().withId(userId).build();
        user.setTenantId(tenantId);
        repository.add(user);
        final SRole sRole = new SRole();
        sRole.setId(roleId);
        sRole.setTenantId(tenantId);
        repository.add(sRole);
        final SGroup sGroup = new SGroup();
        sGroup.setId(groupId);
        sGroup.setTenantId(tenantId);
        repository.add(sGroup);
        final SProcessSupervisor expectedSProcessSupervisor = repository
                .add(new SProcessSupervisor(supervisorId, tenantId, processDefId, 0, groupId, 0));
        repository.add(new SProcessSupervisor(2, tenantId, processDefId, userId, groupId, roleId));

        final List<Long> sProcessSupervisors = repository.searchSProcessSupervisorWithSUserSGroupSRole();

        assertThat(sProcessSupervisors).containsOnly(expectedSProcessSupervisor.getId());
    }

    @Test
    public void searchSProcessSupervisorWithSUserSGroupSRole_should_return_supervisors_mapped_through_role() {
        SUser user = aUser().withId(userId).build();
        user.setTenantId(tenantId);
        repository.add(user);
        final SRole sRole = new SRole();
        sRole.setId(roleId);
        sRole.setTenantId(tenantId);
        repository.add(sRole);
        final SGroup sGroup = new SGroup();
        sGroup.setId(groupId);
        sGroup.setTenantId(tenantId);
        repository.add(sGroup);
        final SProcessSupervisor expectedSProcessSupervisor = repository
                .add(new SProcessSupervisor(supervisorId, tenantId, processDefId, 0, 0, roleId));
        repository.add(new SProcessSupervisor(2, tenantId, processDefId, userId, groupId, roleId));

        final List<Long> sProcessSupervisors = repository.searchSProcessSupervisorWithSUserSGroupSRole();

        assertThat(sProcessSupervisors).containsOnly(expectedSProcessSupervisor.getId());
    }

    @Test
    public void searchSProcessSupervisorWithSUserSGroupSRole_should_return_supervisors_mapped_through_group_and_role() {
        SUser user = aUser().withId(userId).build();
        user.setTenantId(tenantId);
        repository.add(user);
        final SRole sRole = new SRole();
        sRole.setId(roleId);
        sRole.setTenantId(tenantId);
        repository.add(sRole);
        final SGroup sGroup = new SGroup();
        sGroup.setId(groupId);
        sGroup.setTenantId(tenantId);
        repository.add(sGroup);
        final SProcessSupervisor expectedSProcessSupervisor = repository
                .add(new SProcessSupervisor(supervisorId, tenantId, processDefId, 0, groupId,
                        roleId));
        repository.add(new SProcessSupervisor(2, tenantId, processDefId, userId, groupId, roleId));

        final List<Long> sProcessSupervisors = repository.searchSProcessSupervisorWithSUserSGroupSRole();

        assertThat(sProcessSupervisors).containsOnly(expectedSProcessSupervisor.getId());
    }
}
